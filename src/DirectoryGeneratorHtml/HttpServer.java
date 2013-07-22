package DirectoryGeneratorHtml;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: Паша
 * Date: 17.07.13
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class HttpServer {
    public void serverRun(int serverPort) throws IOException {
        ServerSocket servSocket= new ServerSocket(serverPort);

        System.out.println("Открыто серверное соединение " + servSocket.getLocalSocketAddress());

        while (true) {
            Socket clientSocket= servSocket.accept();
            System.out.println("Подключение");
            Thread clientThread= new Thread(new ClientManager(clientSocket, System.getProperty("user.dir")));
            clientThread.start();
        }
    }
}
