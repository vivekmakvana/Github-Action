import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class HelloWorldServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HelloWorldHandler());
        server.setExecutor(null);
        System.out.println("Server started on http://localhost:8080");
        server.start();
    }

    static class HelloWorldHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Hello World Java App</title>
                    <style>
                        body { font-family: Arial, sans-serif; text-align: center; margin-top: 100px; }
                        h1 { color: #333; font-size: 3em; }
                    </style>
                </head>
                <body>
                    <h1>Hello! World</h1>
                    <p>Java application running in Docker with Ubuntu</p>
                </body>
                </html>
                """;
            
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}