package ru.netology;

import org.apache.hc.client5.http.classic.methods.HttpGet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Request {
    private final String method;
    private final String path;
    private final String version;
    private final Path filePath;
    private final String mimeType;
    private final long length;
    private final List<String> headers;
    private final String body;

    public Request(BufferedInputStream in) throws IOException, URISyntaxException {
        final var limit = 4096;
        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");

        this.method = requestLine[0];
        this.path = requestLine[1];
        this.version = requestLine[2];
        this.filePath = Path.of(".", "public", path);
        this.mimeType = Files.probeContentType(filePath);
        this.length = Files.size(filePath);

        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);

        in.reset();
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        this.headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        String body = null;
        if (!method.equals("GET")) {
            in.skip(headersDelimiter.length);
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                body = new String(bodyBytes);
            }
        }
        this.body = body;
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

    public long getLength() {
        return length;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public void getQueryParam(String name) {
//        httpGet
//        URIBuilder builder = new URIBuilder();
//        HttpGet get = new HttpGet(builder.build());
//        URI uri = get.getUri();
//        List<NameValuePair> params = URLEncodedUtils.parse(uri, Charset.defaultCharset());
    }

    public void getQueryParams() {
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
