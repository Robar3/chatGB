package ru.robar3.chatgb.server;

import ru.robar3.chatgb.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatServer {


    private final Map<String,ClientHandler> clients;

    public ChatServer() {
        clients = new HashMap<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService =new InMemoryAuthService()) {
            while (true) {
                System.out.println("Ожидаем подключения клиента");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Клиент подключился");
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка сервера" + e);
        }
    }

    public boolean isNickBusy(String nick) {
       return clients.containsKey(nick);
    }

    public void broadcast(String message) {
           clients.values().forEach(client->client.sendMessage(message));
    }

    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(),client);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientList();
    }

    private void broadcastClientList() {
        final String nicks = clients.values().stream()
                .map(client -> client.getNick()).
                collect(Collectors.joining(" "));
        broadcast(Command.CLIENTS,nicks);
    }

    private void broadcast(Command command, String nicks) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(command,nicks);
        }
    }

    public void sendMessageToClient(ClientHandler sender, String to, String msg) {
        ClientHandler receiver = clients.get(to);
        if (receiver!=null){
            receiver.sendMessage("От "+ sender.getNick() +": "+ msg);
            sender.sendMessage("Участнику "+ to+ ": "+msg);
        }else {
            sender.sendMessage(Command.ERROR,"Участника с ником "+to+" нет в чате");
        }
    }
}
