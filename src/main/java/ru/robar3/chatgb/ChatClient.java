package ru.robar3.chatgb;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private String nick;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private ClientController controller;

    public ChatClient(ClientController controller) {
        this.controller = controller;
    }

    public void openConnection() throws Exception {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        Thread thread = new Thread(() -> {
            try {
                waitAuth();
                readMessage();
            } finally {
                closeConnection();
            }


        });
        thread.setDaemon(true);
        thread.start();
    }

    private void closeConnection() {
    }

    private void readMessage() {
        while (true) {
            try {
                String msg = in.readUTF();
                System.out.println("Receive message: " + msg);
                if (Command.isCommand(msg)) {
                    Command command = Command.getCommand(msg);
                    String[] parse = command.parse(msg);


                    if (command == Command.END) {
                        controller.toggleBoxesVisibility(false);
                        break;
                    }
                    if (command == Command.ERROR) {
                        Platform.runLater(()->controller.showError(parse));
                        continue;
                    }
                    if (command==Command.CLIENTS){
                        controller.updateClientList(parse);
                        continue;
                    }
                }
                controller.addMessage(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void waitAuth() {
        while (true) {
            try {
                final String msg = in.readUTF();
                if (Command.isCommand(msg)) {
                    Command command = Command.getCommand(msg);
                    String[] params = command.parse(msg);
                    if (command == Command.AUTHOK) {
                        nick = params[0];
                        controller.addMessage("Успешная авторизация под ником: " + nick);
                        controller.toggleBoxesVisibility(true);
                        break;
                    }
                    if (command == Command.ERROR) {
                        Platform.runLater(() -> controller.showError(params));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            if (nick != null && !message.startsWith("/")) {
                message = this.nick + ": " + message;
            }
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
        ;
    }
}
