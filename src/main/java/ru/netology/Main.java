package ru.netology;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    private static final List<String> validPath = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public static void main(String[] args) {
        Server server = new Server(9999);

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            String content = "Hello from GET!";
            responseStream.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + "text/plain" + "\r\n" +
                    "Content-Length: " + content.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes());
            responseStream.write(content.getBytes());
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {
            String content = "Hello from POST!";
            responseStream.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + "text/plain" + "\r\n" +
                    "Content-Length: " + content.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes());
            responseStream.write(content.getBytes());
        });

        server.addHandler("GET", "/classic.html", (request, responseStream) -> {
            final var filePath = Path.of(".", "public", "classic.html");
            final var mimeType = Files.probeContentType(filePath);
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()).getBytes();
            responseStream.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + mimeType + "\r\n" +
                    "Content-Length: " + content.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes());
            responseStream.write(content);
        });

        for (String validPath : validPath) {
            server.addHandler("GET", validPath, (request, responseStream) -> {
                final var filePath = Path.of(".", "public", validPath);
                final var mimeType = Files.probeContentType(filePath);

                final var length = Files.size(filePath);

                responseStream.write(("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n").getBytes());
                Files.copy(filePath, responseStream);
            });
        }

        server.start();
    }
}