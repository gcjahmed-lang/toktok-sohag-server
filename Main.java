import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Main {
    static List<String> orders = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        String portEnv = System.getenv("PORT");
        int port = portEnv != null ? Integer.parseInt(portEnv) : 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", (exchange) -> {
            send(exchange, "سيرفر توكتوك سوهاج شغال 🚀🛺");
        });
        server.createContext("/request", (exchange) -> {
            String query = exchange.getRequestURI().getQuery();
            if (query == null) query = "طلب فاضي";
            orders.add(query);
            send(exchange, "تم استلام طلبك ✅ عدد الطلبات: " + orders.size());
        });
        server.createContext("/orders", (exchange) -> {
            String res = String.join("\n", orders);
            if (res.isEmpty()) res = "مفيش طلبات دلوقتي";
            send(exchange, res);
        });
        server.createContext("/accept", (exchange) -> {
            String query = exchange.getRequestURI().getQuery();
            send(exchange, "تم قبول الطلب: " + query + " يلا روح للراكب 🛺");
        });
        server.setExecutor(null);
        server.start();
        System.out.println("سيرفر شغال على بورت " + port);
    }
    static void send(HttpExchange ex, String text) {
        try {
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
            byte[] b = text.getBytes("UTF-8");
            ex.sendResponseHeaders(200, b.length);
            OutputStream os = ex.getResponseBody();
            os.write(b);
            os.close();
        } catch (Exception e) {
            System.out.println("خطأ: " + e.getMessage());
        }
    }
}
