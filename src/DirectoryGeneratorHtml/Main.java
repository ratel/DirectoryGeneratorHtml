package DirectoryGeneratorHtml;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 13.07.13
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    public static void main(String [] args) {
        /*DirectoryGeneratorHtml dgh= new DirectoryGeneratorHtml();

        try (Writer otputFile= new FileWriter(new File("index.html"))) {
            dgh.buildHtml(otputFile, System.getProperty("user.dir"));
        } catch (IOException e) {
            System.out.println("Ошибка при работе с файлом!");
            e.printStackTrace();
        }*/

        HttpServer server= new HttpServer();
        try {
            server.serverRun(1700);
        } catch (IOException e) {
            System.err.println("Не удалось открыть серверное соедение!");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
