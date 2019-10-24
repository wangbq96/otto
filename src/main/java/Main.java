import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("charset", "UTF-8");
        props.put("debug", true);
//
//        WebServer server = new WebServer(new InetSocketAddress(5555), props);
//        while (true) {
//            server.run();
//            Thread.sleep(100);
//        }

        Server s = new Server(new InetSocketAddress(5555), props);
        System.out.println("Server start");
        s.start();

        //byte[] content = FileUtils.readFileToByteArray(new File("data/index.html"));
    }
}
