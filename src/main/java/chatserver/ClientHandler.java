package chatserver;

import channels.Channel;
import channels.MessageChannel;
import entities.User;
import messages.*;
import service.UserService;
import shared.EventDistributor;
import shared.HandlerBase;
import shared.HandlerManager;
import shared.MessageSender;
import states.State;
import states.StateException;
import states.StateResult;
import util.Config;
import util.Keys;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.util.encoders.Base64;

class ClientHandler extends HandlerBase {
    private static final Logger LOGGER = Logger.getAnonymousLogger();

    private final Channel channel;
    private final UserService userService;
    private final EventDistributor eventDistributor;
    
    private Config config;
    
    private HashMap<User, byte[]> serverChallenges;
    private HashMap<User, Key> keys;

    public ClientHandler(Channel channel, UserService userService, EventDistributor eventDistributor,
                         ExecutorService executorService, HandlerManager handlerManager, Config config) {
        this.channel = channel;
        this.userService = userService;
        this.eventDistributor = eventDistributor;
        this.config = config;
        
        serverChallenges = new HashMap<User, byte[]>();
        keys = new HashMap<User, Key>();
        ((MessageChannel)this.channel).setReceiveAES(false);
		((MessageChannel)this.channel).setSendAES(false);

        init(channel, executorService, handlerManager, new StateOffline());
    }

    private class StateOffline extends State {
        @Override
        public StateResult handleAuthenticateRequest(AuthenticateRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOffline::handleAuthenticateRequest with parameters: " + request);

            final AuthenticateResponse response = new AuthenticateResponse(request);
            State nextState = this;

            final User user = userService.find(request.getUsername());
            if (user != null) {
                if (user.getPresence() == User.Presence.Offline) {
                	// Get clients public key
            		try {
            			PublicKey publicKey = Keys.readPublicPEM(new File(config.getString("keys.dir") + "/" + user.getUsername() + ".pub.pem"));
            			((MessageChannel)channel).setPublicKey(publicKey);
            		} catch(IOException e) {
            			System.err.println("Public key of user" + user.getUsername() + " not found! " + e.getMessage());
            		}
            		
					response.setClientChallenge(request.getClientChallenge());
					
					// generates a 32 byte secure random number
					SecureRandom secureRandom = new SecureRandom();
					final byte[] number = new byte[32];
					secureRandom.nextBytes(number);
					// encode number into Base64 format
					byte[] serverChallenge = Base64.encode(number);
					response.setServerChallenge(serverChallenge);
					serverChallenges.put(user, number);
					
					KeyGenerator generator = null;
					try {
						generator = KeyGenerator.getInstance("AES");
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					// KEYSIZE is in bits
					generator.init(256);
					Key key = generator.generateKey(); 
					// encode key into Base64 format
					byte[] keyBase64 = Base64.encode(key.getEncoded());
					response.setKey(keyBase64);
					keys.put(user, key);
					
					// generates a 16 byte secure random number
					final byte[] iv = new byte[16];
					secureRandom.nextBytes(iv);
					// encode number into Base64 format
					byte[] ivBase64 = Base64.encode(iv);
					response.setIV(ivBase64);
					
					((MessageChannel)channel).setReceiveAES(true);
					((MessageChannel)channel).setReceiveAlgorithm("AES/CTR/NoPadding");
					((MessageChannel)channel).setPrivateKey(key);
					((MessageChannel)channel).setIV(iv);

					//response.setResponse(AuthenticateResponse.ResponseCode.OkSent);
					response.setResponseCode("OkSent");
					nextState = new StateWaitForOkResponse(user);
                } else {
                	//response.setResponse(AuthenticateResponse.ResponseCode.UserAlreadyAuthenticated);
                	response.setResponseCode("UserAlreadyAuthenticated");
                }
            } else {
            	//response.setResponse(AuthenticateResponse.ResponseCode.UnknownUser);
            	response.setResponseCode("UnknownUser");
            }

            return new StateResult(nextState, response);
        }

        @Override
        public StateResult handleExitEvent(ExitEvent event) throws StateException {
            LOGGER.info("ClientHandler::StateOffline::handleExitEvent with parameters: " + event);

            return new StateResult(new StateExit());
        }
    }
    
    private class StateWaitForOkResponse extends State {
    	private final User user;
    	
    	public StateWaitForOkResponse(User user) {
            this.user = user;
        }
    	
    	@Override
        public StateResult handleAuthConfirmationRequest(AuthConfirmationRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOffline::handleAuthenticateRequest with parameters: " + request);
            
            State nextState = new StateOffline();
            final AuthConfirmationResponse response = new AuthConfirmationResponse(request);
            
            final User user = userService.find(request.getUsername());
            if (user != null) {
                if (user.getPresence() == User.Presence.Offline) {
            		byte[] serverChallenge = Base64.decode(request.getServerChallenge());

            		if(Arrays.equals(serverChallenges.get(user), serverChallenge)) {
            			nextState = new StateOnline(user);
            			((MessageChannel)channel).setSendAES(true);
            			((MessageChannel)channel).setSendAlgorithm("AES/CTR/NoPadding");
            			((MessageChannel)channel).setPublicKey(keys.get(user));
            			System.out.println("pass");
            		} else {
            			System.err.println("Wrong challenge received!");
            		}
                }
            }
            
            return new StateResult(nextState, response);
    	}

    	@Override
        public StateResult handleExitEvent(ExitEvent event) throws StateException {
            LOGGER.info("ClientHandler::StateOffline::handleExitEvent with parameters: " + event);

            return new StateResult(new StateExit());
        }
    }

    private class StateOnline extends State {
        private final User user;
        private Channel.EventHandler channelEventHandler;

        public StateOnline(User user) {
            this.user = user;
        }

        @Override
        public void onEntered() throws StateException {
            LOGGER.info("ClientHandler::StateOnline::onEntered");

            channelEventHandler = new Channel.EventHandler() {
                @Override
                public void onChannelClosed() {
                    userService.logout(user);
                }
            };
            channel.addEventHandler(channelEventHandler);

            eventDistributor.subscribe(getSender());
        }

        @Override
        public void onExited() throws StateException {
            LOGGER.info("ClientHandler::StateOnline::onExited");

            eventDistributor.unsubscribe(getSender());
            channel.removeEventHandler(channelEventHandler);

            userService.logout(user); // guarantee that the user is logged out when we leave this state
        }

        @Override
        public StateResult handleLogoutRequest(LogoutRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOnline::handleLogoutRequest with parameters: " + request);

            userService.logout(user);

            final LogoutResponse response = new LogoutResponse(request);

            return new StateResult(new StateOffline(), response);
        }

        @Override
        public StateResult handleSendMessageRequest(SendMessageRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOnline::handleSendMessageRequest with parameters: " + request);

            final MessageEvent event = new MessageEvent();
            event.setUsername(user.getUsername());
            event.setMessage(request.getMessage());
            eventDistributor.publish(event, new HashSet<MessageSender>() {{
                add(getSender()); // don't forward this event to our sender
            }});

            final SendMessageResponse response = new SendMessageResponse(request);

            return new StateResult(this, response);
        }

        @Override
        public StateResult handleRegisterRequest(RegisterRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOnline::handleRegisterRequest with parameters: " + request);

            user.addPrivateAddress(request.getPrivateAddress());

            final RegisterResponse response = new RegisterResponse(request);

            return new StateResult(this, response);
        }

        @Override
        public StateResult handleLookupRequest(LookupRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOnline::handleLookupRequest with parameters: " + request);

            final User requestedUser = userService.find(request.getUsername());

            if (requestedUser != null) {
                final LookupResponse response = new LookupResponse(request);
                response.setPrivateAddresses(requestedUser.getPrivateAddresses());

                return new StateResult(this, response);
            } else {
                final ErrorResponse response = new ErrorResponse(request);
                response.setReason("user '" + request.getUsername() + "' not found");
                return new StateResult(this, response);
            }
        }
        
        @Override
        public StateResult handleAuthenticateRequest(AuthenticateRequest request) throws StateException {
            LOGGER.info("ClientHandler::StateOnline::handleAuthenticateRequest with parameters: " + request);

            final User requestedUser = userService.find(request.getUsername());

            if (requestedUser != null) {
                final AuthenticateResponse response = new AuthenticateResponse(request);
                response.setClientChallenge(request.getClientChallenge());
                return new StateResult(this, response);
            } else {
                final ErrorResponse response = new ErrorResponse(request);
                response.setReason("user '" + request.getUsername() + "' not found");
                return new StateResult(this, response);
            }
        }

        @Override
        public StateResult handleExitEvent(ExitEvent event) throws StateException {
            LOGGER.info("ClientHandler::StateOnline::handleExitEvent with parameters: " + event);

            return new StateResult(new StateExit());
        }
    }

    private class StateExit extends State {
        @Override
        public void onEntered() throws StateException {
            LOGGER.info("ClientHandler::StateExit::onEntered");

            stop();
        }
    }
}
