import org.apache.commons.io.FileUtils;
import reference.HTTPResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Server {
    private Selector selector;
    private ServerSocketChannel server;

    private boolean isRunning = true;
    private Charset charset;
    private CharsetEncoder encoder;
    private boolean debug;

    public Server(InetSocketAddress address, Map<String, Object> props) throws IOException {
        this.charset = Charset.forName((String)props.get("charset"));
        this.encoder = this.charset.newEncoder();
        this.debug = (Boolean)props.get("debug");

        selector = Selector.open();
        server = ServerSocketChannel.open();
        server.socket().bind(address);
        //server.socket().setReuseAddress(false);
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        while (true) {
            while (selector.select() > 0) {
                Iterator<SelectionKey> i = selector.selectedKeys().iterator();
                while (i.hasNext()) {
                    SelectionKey key = i.next();
                    i.remove();
                    if (key.isAcceptable()) {
                        SocketChannel channel = server.accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        HttpRequest request = (HttpRequest) key.attachment();
                        if (request == null) {
                            request = parseRequest(readFromSocket(channel));
                            key.attach(request);
                        }
                        System.out.println("process request");
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_WRITE, request);
                    } else if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        HttpRequest request = (HttpRequest) key.attachment();
                        if (request != null) {
                            HttpResponse response = generateResponse(request);
                            writeToSocket(response, channel);
                            channel.close();
                        }
                    }
                }
            }
        }
    }

    private String readFromSocket(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] bytes = null;
        int size = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((size = channel.read(buffer)) > 0) {
            buffer.flip();
            bytes = new byte[size];
            buffer.get(bytes);
            baos.write(bytes);
            buffer.clear();
        }
        bytes = baos.toByteArray();

        return new String(bytes);
    }

    private HttpRequest parseRequest(String request) {
        Map<String, String> headers = new LinkedHashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(request);
        String method = tokenizer.nextToken().toUpperCase();
        String location = tokenizer.nextToken();
        String version = tokenizer.nextToken();
        String[] lines = request.split("\r\n");
        for (int i = 1; i < lines.length; i++) {
            String[] keyVal = lines[i].split(":", 2);
            headers.put(keyVal[0], keyVal[1]);
        }
        return new HttpRequest(method, location, version, headers);
    }

    private HttpResponse generateResponse(HttpRequest request) throws IOException {
        byte[] content;
        String resourceName;
        int responseCode;
        if (request.getLocation().equals("/")) {
            resourceName = "data/index.html";
        } else {
            resourceName = "data"+request.getLocation();
        }
        try {
            content = FileUtils.readFileToByteArray(new File(resourceName));
            responseCode = 200;
        } catch (FileNotFoundException e) {
            content = "".getBytes();
            responseCode = 404;
        }
        HttpResponse response = new HttpResponse();
        response.setContent(content);
        response.setResponseCode(responseCode);
        return response;
    }

    private void writeToSocket(HttpResponse response, SocketChannel channel) {
        response.addDefaultHeaders();
        StringBuilder sb = new StringBuilder();
        try {
            String line = response.version+" "+response.responseCode+" "+response.responseReason+"\r\n";
            sb.append(line);
            for (Map.Entry<String, String> header : response.headers.entrySet()) {
                line = header.getKey()+": "+header.getValue()+"\r\n";
                sb.append(line);
            }
            sb.append("\r\n");
            System.out.println(sb.toString());
            channel.write(encoder.encode(CharBuffer.wrap(sb.toString())));
            channel.write(ByteBuffer.wrap(response.content));
        } catch (IOException ex) {
            // slow silently
        }
    }
}
