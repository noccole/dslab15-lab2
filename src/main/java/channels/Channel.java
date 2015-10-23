package channels;

import java.io.UnsupportedEncodingException;

public interface Channel<T> {
    void send(Packet<T> packet) throws ChannelException;

    Packet<T> receive() throws ChannelException;

    class Encoder {
        private final static String DEFAULT_ENCODING = "UTF-8";

        public static String encodeByteArray(byte[] bytes) throws ChannelException {
            try {
                return new String(bytes, DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new ChannelException("could not encode to " + DEFAULT_ENCODING, e);
            }
        }

        public static byte[] decodeString(String data) throws ChannelException {
            try {
                return data.getBytes(DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new ChannelException("could not encode from " + DEFAULT_ENCODING, e);
            }
        }
    }
}
