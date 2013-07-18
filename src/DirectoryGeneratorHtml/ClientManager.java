package DirectoryGeneratorHtml;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: Паша
 * Date: 17.07.13
 * Time: 10:56
 * To change this template use File | Settings | File Templates.
 */
public class ClientManager implements Runnable {
    final static String CMDHEAD= "HEAD";
    final static String CMDGET= "GET";
    Socket s;

    ClientManager(Socket s) {
        this.s= s;
    }

    @Override
    public void run() {
        try {
            InputStream input= s.getInputStream();
            BufferedReader reader= new BufferedReader(new InputStreamReader(input));
            OutputStream output= s.getOutputStream();
            OutputStreamWriter writer= new OutputStreamWriter(output);
            String stringCmd= "";
            String [] cmdSplit;

            while (true) {
                //System.out.println("Ожидаем команду");
                stringCmd= reader.readLine();

                //if (stringCmd == null) continue;
                if (stringCmd == null) break;

                System.out.println("Получена строка:");
                System.out.println(stringCmd);
                cmdSplit= stringCmd.split(" ");

                if (cmdSplit.length > 0) {
                    if (CMDHEAD.compareToIgnoreCase(cmdSplit[0]) == 0) {
                        //sendHead(output);
                        //output.flush();
                    }

                    if (CMDGET.compareToIgnoreCase(cmdSplit[0]) == 0) {
                        sendResource(output, cmdSplit);
                        output.flush();
                    }
                }
            }

            output.close();
            System.out.println("Клиентский поток отработал");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void sendHead(OutputStream writer, String fileType, String fileLength) throws IOException {
        OutputStreamWriter outPut= new OutputStreamWriter(writer);

        outPut.write("HTTP/1.0 200 OK\r\n");

        outPut.write(("Content-Type: " + fileType + "\r\n"));
        outPut.write(("Content-Length: " + fileLength + "\r\n"));

        outPut.write("\r\n");
        outPut.flush();
    }

    private void sendResource(OutputStream writer, String [] cmdSplit) {
        if (cmdSplit.length == 0)
            return;

        File resource, fileIndex;
        String resourcePath= System.getProperty("user.dir");
        String pathIndex;

        resourcePath= resourcePath + cmdSplit[1];
        resource= new File(resourcePath);

        System.out.println("\tПытаемся послать ресурс: " + resource.getAbsolutePath());

        if (resource.exists()) {
            if (resource.isDirectory()) {
                pathIndex= resourcePath + /*File.separator + */"index.html";

                fileIndex= new File(pathIndex);

                if (!fileIndex.exists()) {
                    DirectoryGeneratorHtml dgh= new DirectoryGeneratorHtml();

                    try {
                        dgh.buildHtml(new OutputStreamWriter(writer), resourcePath);
                    } catch (IOException e) {
                        System.err.println("Ошибка при генерации HTML-файла!");
                        e.printStackTrace();
                    }

                    return;
                }

                resource= fileIndex;
            }

            System.out.println("\tОтправляем ресурс: " + resource.getAbsolutePath());

            try (BufferedInputStream readerResource= new BufferedInputStream(new FileInputStream(resource))) {
                //sendHead(writer, new MimetypesFileTypeMap().getContentType(resource), String.valueOf(resource.length()));
                sendHead(writer, URLConnection.getFileNameMap().getContentTypeFor(resource.getAbsolutePath()), String.valueOf(resource.length()));
                sendData(readerResource, new BufferedOutputStream(writer));
            } catch (IOException e) {
                System.out.println("Ошибка при отправке файла по HTTP!");
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Запрошенный ресурс не найден");
        }


    }

    private void sendData(BufferedInputStream data, BufferedOutputStream out) {
        byte buf[] = new byte[4096];
        int count;

        try {
            while ((count = data.read(buf)) >= 0) {
                out.write(buf, 0, count);
            }

            out.flush();
        } catch (IOException e) {
            System.err.println("Ошибка при пересылке данных файла!");
            e.printStackTrace();
        }
    }
}
