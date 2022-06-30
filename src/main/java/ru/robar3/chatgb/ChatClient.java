package ru.robar3.chatgb;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javafx.collections.transformation.SortedList;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class ChatClient {
    private String nick;
    private String login;
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
        showChatLog();
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
                chatLog(message);
            }else  if (Command.isCommand(message)) {
                Command command = Command.getCommand(message);
                String[] params = command.parse(message);
                if (command==Command.CHANGE_NICK){

                    if (renameFile((String.format("log%s.txt",nick)),String.format("log%s.txt",params[0]))){
                        this.nick=params[0];
                    }else {
                        controller.showError(new String[]{"Не удалось изменить ник"});
                    }

                }

            }
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));

    }

    public void chatLog(String msg){
        GregorianCalendar cannes = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("dd MMM yyy kk mm ss");
        String fileName = String.format("log%s.txt",nick);
        try (BufferedWriter writer = new BufferedWriter(new
                FileWriter(fileName,true))){
            writer.write(( df.format(cannes.getTime()))+" "+msg+"\n");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void showChatLog(){
        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(String.format("log%s.txt",nick)),Charset.defaultCharset());
        ){
            List<String>lines;
            lines = reader.readLines(100);
            if (lines != null) {
                Collections.reverse(lines);
                for (String line: lines) {
                    controller.addMessage(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean renameFile(String LastPath, String newPath){
        File file = new File(LastPath);
        File newFile = new File(newPath);
       return file.renameTo(newFile);
    }
}
