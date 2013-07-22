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

    final static String DEFAULT_FILENAME= "index.html";
    final static String DEFAULT_MIMETYPEFILES= "text/html";
    private Socket s;
    private String sharedDir;

    ClientManager(Socket s, String sharedDir) {
        this.s= s;
        this.sharedDir= sharedDir;
    }

    @Override
    public void run() {
        OutputStream output= null;
        BufferedReader reader= null;

            try {
            InputStream input= s.getInputStream();
            reader= new BufferedReader(new InputStreamReader(input, "Cp1251"));
            output= s.getOutputStream();
            String stringCmd= "";
            String [] cmdSplit;

            while (true) {
                stringCmd= reader.readLine();                                               // Считывание строки из сокета.

                if (stringCmd == null) break;

                System.out.println("Получена строка:");
                System.out.println(stringCmd);
                cmdSplit= stringCmd.split(" ");
                                                                                            // Поиск команды.
                if (cmdSplit.length > 0) {
                    if (CMDHEAD.compareToIgnoreCase(cmdSplit[0]) == 0) {
                        doCmdHead(output, cmdSplit);
                        break;
                    }
                    else if (CMDGET.compareToIgnoreCase(cmdSplit[0]) == 0) {
                        doCmdGet(output, cmdSplit);
                        break;
                    }
                    else {
                        doErrorAnswer(output, ErrorCode.UNKNOWNCMD);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (output != null) output.close();
                if (reader != null) reader.close();
            } catch (IOException e) {
                System.err.println("Ошибка при закрытии потоков ввода/вывода сокета!");
            }

            try {
                s.close();
            } catch (IOException e) {
                System.err.println("ошибка при закрытии клиентского сокета!");
            }
        }

    }

    protected void doCmdHead(OutputStream writer, String [] cmdSplit) {
        if (cmdSplit.length < 2)
            return;

        File resource, fileIndex;
        String resourcePath= formPathResource(cmdSplit[1]);
        String pathIndex;

        resource= new File(resourcePath);

        if (resource.exists()) {
            if (resource.isDirectory()) {
                pathIndex= resourcePath + DEFAULT_FILENAME;

                fileIndex= new File(pathIndex);

                if (!fileIndex.exists()) {
                    DirectoryGeneratorHtml dgh= new DirectoryGeneratorHtml();

                    try {
                        OutputStreamWriter indexWriter= new OutputStreamWriter(writer);
                        dgh.buildHtml(indexWriter, resourcePath);

                        sendHead(writer, DEFAULT_MIMETYPEFILES, "0");
                    } catch (IOException e) {
                        ErrorCode err = ErrorCode.EXCEPTION;
                        err.setErrText("Ошибка при генерации HTML-файла!");
                        doErrorAnswer(writer, err);

                        System.err.println("Ошибка при генерации HTML-файла!");
                        e.printStackTrace();
                    }

                    return;
                }

                resource= fileIndex;
            }

            try (BufferedInputStream readerResource= new BufferedInputStream(new FileInputStream(resource))) {
                //sendHead(writer, new MimetypesFileTypeMap().getContentType(resource), String.valueOf(resource.length()));
                sendHead(writer, URLConnection.getFileNameMap().getContentTypeFor(resource.getAbsolutePath()), String.valueOf(resource.length()));
            } catch (IOException e) {
                ErrorCode err = ErrorCode.EXCEPTION;
                err.setErrText("Ошибка при отправке файла по HTTP!");
                doErrorAnswer(writer, err);

                System.out.println("Ошибка при отправке файла по HTTP!");
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Запрошенный ресурс не найден");
            doErrorAnswer(writer, ErrorCode.NORESOURCE);
        }
    }

    protected String formPathResource(String resName) {
        return (sharedDir + resName);
    }

    protected void doCmdGet(OutputStream writer, String [] cmdSplit) {
        if (cmdSplit.length < 2)
            return;

        File resource, fileIndex;
        String resourcePath= formPathResource(cmdSplit[1]);                                 // Получение имени запрошенного ресурса.
        String pathIndex;

        resource= new File(resourcePath);

        System.out.println("\tПытаемся отправить ресурс: " + resource.getAbsolutePath());

        if (resource.exists()) {
            if (resource.isDirectory()) {                                                   // Если ресурс- директория,
                pathIndex= resourcePath + DEFAULT_FILENAME;                                 // отправляем индекс-файл.

                fileIndex= new File(pathIndex);

                if (!fileIndex.exists()) {                                                  // Если индекс-файла нет,
                    DirectoryGeneratorHtml dgh= new DirectoryGeneratorHtml();

                    try {
                        sendHead(writer, DEFAULT_MIMETYPEFILES, null);
                        dgh.buildHtml(new OutputStreamWriter(writer), resourcePath);        // сгенерируем его и отправим в поток.
                    } catch (IOException e) {
                        ErrorCode err = ErrorCode.EXCEPTION;
                        err.setErrText("Ошибка при генерации HTML-файла!");
                        doErrorAnswer(writer, err);

                        System.err.println("Ошибка при генерации HTML-файла!");
                        e.printStackTrace();
                    }

                    return;
                }

                resource= fileIndex;                                                        // Если индекс-файл существует, отправим его.
            }
                                                                                            // Отправляем запрошенный файл.
            try (BufferedInputStream readerResource= new BufferedInputStream(new FileInputStream(resource))) {
                //sendHead(writer, new MimetypesFileTypeMap().getContentType(resource), String.valueOf(resource.length()));
                sendHead(writer, URLConnection.getFileNameMap().getContentTypeFor(resource.getAbsolutePath()), String.valueOf(resource.length()));
                sendData(readerResource, new BufferedOutputStream(writer));
            } catch (IOException e) {
                ErrorCode err = ErrorCode.EXCEPTION;
                err.setErrText("Ошибка при отправке файла по HTTP!");
                doErrorAnswer(writer, err);

                System.out.println("Ошибка при отправке файла по HTTP!");
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Запрошенный ресурс не найден");
            doErrorAnswer(writer, ErrorCode.NORESOURCE);
        }
    }

    protected void sendHead(OutputStream writer, String fileType, String fileLength) throws IOException {
        OutputStreamWriter outPut= new OutputStreamWriter(writer);

        outPut.write("HTTP/1.0 200 OK\r\n");

        if (fileType != null)
            if (fileType.length() > 0)
                outPut.write(("Content-Type: " + fileType + "\r\n"));

        if (fileLength != null)
            outPut.write(("Content-Length: " + fileLength + "\r\n"));

        outPut.write("\r\n");
        outPut.flush();
    }

    protected void sendData(BufferedInputStream data, BufferedOutputStream out) throws IOException {
        byte buf[] = new byte[4096];
        int count;

        while ((count = data.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }

        out.flush();
    }

    protected void doErrorAnswer(OutputStream output, ErrorCode err) {
        Writer outPutW= new OutputStreamWriter(output);

        String errData = "<html><title>Error " + err.getCode() + "</title>" +
                "\t\t<META http-equiv= \"content-type\"  content= \"text/html; charset=utf-8\">\n" +
                "<body>" + err.getErrText() + "</body></html>";

        try {
            sendHead(output, DEFAULT_MIMETYPEFILES, String.valueOf(errData.length()));
            outPutW.write(errData);
            outPutW.flush();
        } catch (IOException e) {
            System.out.println("Ошибка при пересылке данных по HTTP!");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}


enum ErrorCode {
    NORESOURCE(404), EXCEPTION(500), UNKNOWNCMD(501);

    private int code;
    private String errText;

    ErrorCode(int code) {
        this.code= code;

        switch (code) {
            case 404: errText= "Ресурс не найден!";
                break;
            case 500: errText= "Ошибка выполнения!";
                break;
            case 501: errText= "Неизвестная команда!";
                break;
            default: errText= "";
        }

        /*switch (this) {
            case NORESOURCE: errText= "Ресурс не найден!";
                break;
            case EXCEPTION: errText= "Ошибка выполнения!";
                break;
            case UNKNOWNCMD: errText= "Неизвестная команда!";
                break;
            default: errText= "";
        }                  */
    }

    int getCode() {
        return code;
    }

    String getErrText() {
        return errText;
    }

    void setErrText(String errText) {
        if (this.equals(EXCEPTION))
            this.errText= errText;
    }
}
