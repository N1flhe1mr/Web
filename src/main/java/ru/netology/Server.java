package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService threadPool;
    private final ConcurrentHashMap<String, Handler> handlers = new ConcurrentHashMap<>();

    public Server(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(64);
    }

    public void start() {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleConnection(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private void handleConnection(Socket clientSocket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                final var out = new BufferedOutputStream(clientSocket.getOutputStream());
        ) {
            Request request = new Request(in);
            String key = request.getMethod() + request.getPath();
            Handler handler = handlers.get(key);
            if (handler == null) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }
                handler.handle(request, out);
            } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.put(method + path, handler);
    }
}