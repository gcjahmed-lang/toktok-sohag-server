import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    static Map<String, String> drivers = new ConcurrentHashMap<>();
    static Map<String, String> requests = new ConcurrentHashMap<>();
    static Map<String, String> requestDriver = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8000"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/", ex -> {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            if (method.equals("OPTIONS")) { send(ex, "ok"); return; }

            try {
                if (path.equals("/drivers")) {
                    StringBuilder sb = new StringBuilder("[");
                    boolean first = true;
                    for (String d : drivers.values()) {
                        if (!first) sb.append(",");
                        sb.append(d);
                        first = false;
                    }
                    sb.append("]");
                    send(ex, sb.toString());
                } else if (path.equals("/register") && method.equals("POST")) {
                    String body = read(ex);
                    String id = extract(body, "id");
                    drivers.put(id, body);
                    send(ex, "{\"ok\":true}");
                } else if (path.equals("/request") && method.equals("POST")) {
                    String body = read(ex);
                    String id = extract(body, "id");
                    requests.put(id, body);
                    send(ex, "{\"ok\":true}");
                } else if (path.equals("/requests")) {
                    StringBuilder sb = new StringBuilder("[");
                    boolean first = true;
                    for (String r : requests.values()) {
                        if (!first) sb.append(",");
                        sb.append(r);
                        first = false;
                    }
                    sb.append("]");
                    send(ex, sb.toString());
                } else if (path.equals("/accept") && method.equals("POST")) {
                    String body = read(ex);
                    String reqId = extract(body, "requestId");
                    String driverId = extract(body, "driverId");
                    requestDriver.put(reqId, driverId);
                    requests.remove(reqId);
                    send(ex, "{\"ok\":true}");
                } else if (path.startsWith("/status")) {
                    String id = ex.getRequestURI().getQuery();
                    if (id != null && id.startsWith("id=")) id = id.substring(3);
                    String d = requestDriver.get(id);
                    if (d == null) send(ex, "{\"status\":\"waiting\"}");
                    else send(ex, "{\"status\":\"accepted\",\"driverId\":\"" + d + "\"}");
                } else {
                    send(ex, "{\"ok\":true,\"msg\":\"toktok server running\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                send(ex, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.start();
        System.out.println("Server running on port " + port);
    }

    static String read(HttpExchange ex) throws Exception {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    static void send(HttpExchange ex, String s) throws Exception {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(200, b.length);
        ex.getResponseBody().write(b);
        ex.getResponseBody().close();
    }

    static String extract(String json, String key) {
        String p = "\"" + key + "\":\"";
        int i = json.indexOf(p);
        if (i < 0) {
            p = "\"" + key + "\":";
            i = json.indexOf(p);
            if (i < 0) return "";
            int s = i + p.length();
            int e = json.indexOf(",", s);
            if (e < 0) e = json.indexOf("}", s);
            return json.substring(s, e).trim().replace("\"", "");
        }
        int s = i + p.length();
        int e = json.indexOf("\"", s);
        return json.substring(s, e);
    }
                         }
