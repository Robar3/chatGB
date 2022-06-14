package ru.robar3.chatgb.server;

public class ChatRunner {
    public static void main(String[] args) {
        final ChatServer chatServer = new ChatServer();
        chatServer.run();
    }
}
