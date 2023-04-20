package exmp.server;

import exmp.App;
import exmp.commands.CommandData;
import exmp.commands.CommandResult;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final int port;
    private final App app;

    public Server(int port, App app) {
        this.port = port;
        this.app = app;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен и ожидает подключений...");

            while (true) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                    System.out.println("Клиент подключился: " + clientSocket.getRemoteSocketAddress());

                    try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                         ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {

                        while (app.getStatus()) {
                            CommandData commandData = (CommandData) input.readObject();
                            String commandName = commandData.getCommandName();
                            String commandInput = commandData.getArguments();
                            CommandResult result = app.executeCommand(commandName, commandInput);
                            output.writeObject(result);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка при работе с клиентом: " + e.getMessage());
                } finally {
                    if (clientSocket != null) {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            System.err.println("Ошибка при закрытии сокета клиента: " + e.getMessage());
                        }
                    }
                    System.out.println("Клиент отключился: " + (clientSocket != null ? clientSocket.getRemoteSocketAddress() : ""));
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
        }
    }
}