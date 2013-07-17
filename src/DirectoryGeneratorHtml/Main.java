package DirectoryGeneratorHtml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 13.07.13
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    public static void main(String [] args) {
        DirectoryGeneratorHtml dgh= new DirectoryGeneratorHtml();

        try (Writer otputFile= new FileWriter(new File("index.html"))) {
            //Writer otputFile= new FileWriter(new File("out.txt"));
            dgh.buildHtml(otputFile, System.getProperty("user.dir"));
        } catch (IOException e) {
            System.out.println("Ошибка при работе с файлом!");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        HttpServer server= new HttpServer();
        try {
            server.serverRun();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
