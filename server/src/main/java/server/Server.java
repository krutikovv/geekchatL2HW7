package server;

import commands.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler clientHandler, String msg){
        String[] token = msg.split("\\s", 3);
        String message = String.format("[ %s ]: %s", clientHandler.getNickname(), msg);
        if (token[0].equals(Command.SEND_ONE)){
            if(token.length > 2){
                String messagePrv = String.format("[ %s ]: %s", clientHandler.getNickname(), token[2]);
                for (ClientHandler c : clients) {
                    if(c.getNickname().equals(token[1])) {
                        c.sendMsg(messagePrv);
                        clientHandler.sendMsg(messagePrv);
                        break;
                    }
                }
            } else {
                System.out.println("Error syntax of command");
            }
        } else {
            for (ClientHandler c : clients) {
                c.sendMsg(message);
            }
        }
    }

    void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public AuthService getAuthService() {
        return authService;
    }
}
