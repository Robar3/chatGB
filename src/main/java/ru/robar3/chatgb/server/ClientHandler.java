package ru.robar3.chatgb.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private ChatServer server;
    private String nick;
    private final DataInputStream in;
    private final DataOutputStream out;
    private AuthService authService;

    public ClientHandler(Socket socket, ChatServer server,AuthService authService) {

        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = authService;
            new Thread(() -> {
                try {
                    authenticate();
                    readMessage();
                } finally {
                    closeConnection();
                }

            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания подключения к клиенту" + e);
        }

    }

    private void authenticate() {
        while (true){
            try {
                String msg = in.readUTF();
                if (msg.startsWith("/auth"));
                String[] s = msg.split(" ");
                String login = s[1];
                String password = s[2];
                String nick = authService.getNickByLoginAndPassword(login, password);
                if (nick!=null){
                    if (server.isNickBusy(nick)){
                        sendMessage("Польхователь уже авторизован");
                        continue;
                    }
                    sendMessage("/authok "+nick);
                    this.nick=nick;
                    server.broadcast("Пользователь "+nick+" вошел в чат");
                    server.subscribe(this);
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void readMessage() {
        while (true){
            try {
                final String msg = in.readUTF();
                if ("/end".equals(msg)){
                    break;
                }
                System.out.println("Получено сообщение: "+msg);
                server.broadcast(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void closeConnection() {
        sendMessage("/end");

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (socket != null) {
            try {
                socket.close();
                server.unsubscribe(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void sendMessage(String  message) {
        try {
            System.out.println("Отправляю сообщение: "+message);
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNick() {
        return nick;
    }
}
