package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final String version;
    private final Path filePath;
    private final String mimeType;
    private final long lenght;
    private final Map<String, String> headers;
    private final String body;

    public Request(BufferedReader in) throws IOException {
        var requestLine = in.readLine();
        final var parts = requestLine.split(" ");
        this.method = parts[0];
        this.path = parts[1];
        this.version = parts[2];
        this.filePath = Path.of(".", "public", path);
        this.mimeType = Files.probeContentType(filePath);
        this.lenght = Files.size(filePath);

        this.headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            final var headerParts = line.split(": ");
            headers.put(headerParts[0], headerParts[1]);
        }

        final var contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
        if (contentLength > 0) {
            final var bodyChars = new char[contentLength];
            in.read(bodyChars);
            this.body = new String(bodyChars);
        } else {
            this.body = null;
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getLenght() {
        return lenght;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
