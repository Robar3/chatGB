package ru.robar3.chatgb.server;

import ru.robar3.chatgb.Command;

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

    private boolean isAuth=false;

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
        new Thread(()->{
            try {
                Thread.sleep(12000);
                if (!isAuth){
                    closeConnection();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        while (true) {
            try {
                String msg = in.readUTF();
                if (Command.isCommand(msg)) {
                    Command command = Command.getCommand(msg);
                    String[] params = command.parse(msg);

                    if (command==Command.AUTH) ;
                    String login = params[0];
                    String password = params[1];
                    String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage(Command.ERROR,"Польхователь уже авторизован");
                            continue;
                        }
                        sendMessage("/authok " + nick);
                        this.nick = nick;
                        server.broadcast("Пользователь " + nick + " вошел в чат");
                        server.subscribe(this);
                        isAuth=true;
                        break;
                    }else {
                        sendMessage(Command.ERROR,"Неверные логин и пароль");
                    }
                }
                } catch(IOException e){
                    throw new RuntimeException(e);
                }

        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }

    private void readMessage() {
        while (true){
            try {
                final String msg = in.readUTF();
                if (Command.isCommand(msg)){
                    final Command command = Command.getCommand(msg);
                    String[] params = command.parse(msg);
                    if (command ==Command.END){
                        break;
                    }
                   if (command==Command.PRIVATE_MESSAGE){
                       server.sendMessageToClient(this,params[0],params[1]);
                       continue;
                   }
                }
                System.out.println("Получено сообщение: "+msg);
                server.broadcast(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void closeConnection() {
        sendMessage(Command.END);

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

