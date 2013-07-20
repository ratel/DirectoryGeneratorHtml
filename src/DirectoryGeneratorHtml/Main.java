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
        /*File f1= new File("index.html");
        File f2= new File("2.htm");
        File f3= new File("01.gif");
        System.out.println("Content-Type: " + new MimetypesFileTypeMap().getContentType(f1));
        System.out.println("Content-Type: " + new MimetypesFileTypeMap().getContentType(f2));
        System.out.println("Content-Type: " + new MimetypesFileTypeMap().getContentType(f3));


        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(f2.getAbsolutePath());
        System.out.println("");
        System.out.println("Content-Type: " + type);    */
        //System.out.println("Content-Type: " + f3.);

        DirectoryGeneratorHtml dgh= new DirectoryGeneratorHtml();

        try (Writer otputFile= new FileWriter(new File("index.html"))) {
            dgh.buildHtml(otputFile, System.getProperty("user.dir"));
        } catch (IOException e) {
            System.out.println("Ошибка при работе с файлом!");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        HttpServer server= new HttpServer();
        try {
            server.serverRun();
        } catch (IOException e) {
            System.err.println("Не удалось открыть серверное соедение!");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
