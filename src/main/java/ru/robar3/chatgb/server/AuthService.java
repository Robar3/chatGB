package ru.robar3.chatgb.server;

import java.io.Closeable;
import java.sql.Connection;

public interface AuthService extends Closeable {
    String getNickByLoginAndPassword(String login, String password);

    Connection getConnection();
    void close();

}
