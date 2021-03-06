package ru.robar3.chatgb.server;

import java.io.Closeable;

public interface AuthService extends Closeable {
    String getNickByLoginAndPassword(String login, String password);
    void start();
    void close();

}
