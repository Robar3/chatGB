package ru.robar3.chatgb.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class InMemoryAuthService implements AuthService {

    private List<UserData> users;

    private static class UserData{
         String login;
         String password;
         String nick;

        private UserData(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }
    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        try(Connection connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM  users WHERE login=? AND password=?")) {
            statement.setString(1,login);
            statement.setString(2,password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                String nick = rs.getString("nick");
                return nick;
            }else {
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e+"sql exwption");

        }
    }


    @Override
    public void close() {
        System.out.println("Сервис аутентификации остановлен");
    }
}
