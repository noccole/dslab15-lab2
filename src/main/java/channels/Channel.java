package channels;

public interface Channel<T> {
    interface EventHandler {
        /**
         * Emitted when the channel was closed by calling close() or when a exception (e.g. socket exception) happened
         */
        void onChannelClosed();
    }

    /**
     * Send the packet over the channel
     *
     * @param packet Packet which should be send
     * @throws ChannelException
     */
    void send(Packet<T> packet) throws ChannelException;

    /**
     * Read a packet form the channel
     *
     * Will block unit a packet is available.
     *
     * @return Packet which was received
     * @throws ChannelException
     */
    Packet<T> receive() throws ChannelException;

    /**
     * Closes the channel
     *
     * No packet can be send or received anymore after invoking this method!
     *
     * @throws ChannelException
     */
    void close() throws ChannelException;

    void addEventHandler(EventHandler eventHandler);

    void removeEventHandler(EventHandler eventHandler);
}
