package ru.robar3.chatgb.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final AuthService authService;
    private final List<ClientHandler> clients;

    public ChatServer() {
        authService = new InMemoryAuthService();
        clients = new ArrayList<>();
        authService.start();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
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
        for (ClientHandler client : clients) {
            if (client.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void broadcast(String message) {
        if (message.startsWith("/w")){
            String[] split = message.split(" ");
            ClientHandler client =clients.stream()
                    .filter(a -> split[1].equals(a.getNick()))
                    .findFirst()
                    .orElse(null);
            if (client!=null){
               client.sendMessage(split[2]);
            }
        }else {
            for (ClientHandler client: clients) {
                client.sendMessage(message);
            }
        }

    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }
}
