package ru.robar3.chatgb;

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
    public ChatClient(ClientController controller){
        this.controller=controller;
    }

    public void openConnection() throws IOException {
        socket =new Socket("localhost",8189);
        in=new DataInputStream(socket.getInputStream());
        out=new DataOutputStream(socket.getOutputStream());
        new Thread(()->{
            try {
                waitAuth();
                readMessage();
            }finally {
                closeConnection();
            }
          
        }).start();
    }

    private void closeConnection() {
    }

    private void readMessage() {
        while (true){
            try {
                String msg = in.readUTF();
                if ("/end".equals(msg)){
                    controller.toggleBoxesVisibility(false);
                    break;
                }
                controller.addMessage(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void waitAuth() {
        while (true){
            try {
                final String msg = in.readUTF();
                if (msg.startsWith("/authok")){
                    String[] split = msg.split(" ");
                    nick = split[1];
                    controller.addMessage("Успешная авторизация под ником: "+ nick);
                    controller.toggleBoxesVisibility(true);
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            if (nick != null && !message.startsWith("/")){
                message=this.nick+": "+ message;
            }
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
