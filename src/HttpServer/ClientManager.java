package HttpServer;

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
    BufferedReader readerS= null;
    //InputStreamReader readerS= null;
    private String sharedDir;
    enum CmdType {HEAD, GET}

    ClientManager(Socket s, String sharedDir) {
        this.s= s;
        this.sharedDir= sharedDir;
    }

    @Override
    public void run() {
        OutputStream output= null;
        //BufferedReader reader= null;

            try {
            InputStream input= s.getInputStream();
            //readerS= new BufferedReader(new InputStreamReader(input, "UTF-8"));
            output= s.getOutputStream();
            String stringCmd= "";
            String [] cmdSplit;

            while (true) {
                //stringCmd= readerS.readLine();                                              // Считывание строки из сокета.
                stringCmd= getCmdHttp(input);                                               // Считывание строки из сокета.

                if (stringCmd == null) break;
                if (stringCmd.length() == 0) continue;

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
                if (readerS != null) readerS.close();
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

    String getCmdHttp(InputStream input) throws IOException {
        if (readerS == null)
            readerS= new BufferedReader(new InputStreamReader(input, "UTF-8"));

        return readerS.readLine();                                               // Считывание строки из сокета.

/*        if (readerS == null)                                                   // Пробовал считывать непосредственно
            readerS= new InputStreamReader(input, "UTF-8");                      // из InputStreamReader с указанной
                                                                                 // кодировкой..результат тот же
        char buf[] = new char[4096];
        StringBuilder cmdBuild = new StringBuilder();
        int count;

        cmdBuild.setLength(0);

        while (readerS.ready()) {
            count = readerS.read(buf);
            if (count > 0)
                cmdBuild.append(buf, 0, count);
        }

        return cmdBuild.toString();*/
    }

    protected String formPathResource(String resName) {
        return (sharedDir + resName);
    }

    protected void doCmdHead(OutputStream writer, String [] cmdSplit) {
        doCmdGetOrHead(writer, cmdSplit, CmdType.HEAD);
    }

    protected void doCmdGet(OutputStream writer, String [] cmdSplit) {
        doCmdGetOrHead(writer, cmdSplit, CmdType.GET);
    }

    protected void doCmdGetOrHead(OutputStream writer, String [] cmdSplit, CmdType cmdType) {
        if (cmdSplit.length < 2) {
            ErrorCode err = ErrorCode.EXCEPTION;
            err.setErrText("Для данной команды указано недостаточное число аргументов!");
            doErrorAnswer(writer, err);

            return;
        }

        File resource, fileIndex;
        String resourcePath= formPathResource(cmdSplit[1]);                                 // Получение имени запрошенного ресурса.
        //String pathIndex;

        resource= new File(resourcePath);

        System.out.println("\tПытаемся отправить ресурс: " + resource.getAbsolutePath());

        if (resource.exists()) {
            if (resource.isDirectory()) {                                                   // Если ресурс- директория,
                //pathIndex= resourcePath + DEFAULT_FILENAME;                                 // отправляем индекс-файл.

                fileIndex= new File(/*pathIndex*/(resourcePath + DEFAULT_FILENAME));

                if (!fileIndex.exists()) {                                                  // Если индекс-файла нет,
                    try {
                        if (cmdType == CmdType.GET) {
                            DirectoryGeneratorHtml dgh= new DirectoryGeneratorHtml();
                            sendHead(writer, DEFAULT_MIMETYPEFILES, null);
                            dgh.buildHtml(new OutputStreamWriter(writer), resourcePath);    // сгенерируем его и отправим в поток.
                        }
                        else
                            sendHead(writer, DEFAULT_MIMETYPEFILES, "0");
                    } catch (IOException e) {
                        if (cmdType == CmdType.GET) {
                            ErrorCode err = ErrorCode.EXCEPTION;
                            err.setErrText("Ошибка при генерации HTML-файла!");
                            doErrorAnswer(writer, err);
                        }

                        System.err.println("Ошибка при генерации HTML-файла!");
                        e.printStackTrace();
                    }

                    return;
                }

                resource= fileIndex;                                                        // Если индекс-файл существует, отправим его.
            }

            try ( BufferedInputStream readerResource=
                         ((cmdType == CmdType.GET) ? new BufferedInputStream(new FileInputStream(resource)) : null) ) {
                //sendHead(writer, new MimetypesFileTypeMap().getContentType(resource), String.valueOf(resource.length()));
                sendHead(writer, URLConnection.getFileNameMap().getContentTypeFor(resource.getAbsolutePath()), String.valueOf(resource.length()));

                if (cmdType == CmdType.GET) {
                    sendData(readerResource, new BufferedOutputStream(writer));             // Отправляем запрошенный файл.
                }
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
        /*OutputStreamWriter outPutW= null;                                                  // пробовал отправлять в OutputStreamWriter
        try {                                                                                // с заданием кодировки..без указания
            outPutW = new OutputStreamWriter(output, "utf-8");                               // META в html отображение зависит от браузера
        } catch (UnsupportedEncodingException e) {
            System.err.println("Неподдерживаемая кодировка!");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/


        String errData = "<html><title>Error " + err.getCode() + "</title>" +
                "\t\t<META http-equiv= \"content-type\"  content= \"text/html; charset=utf-8\">\n" +
                "<body>" + err.getErrText() + "</body></html>";

        System.out.println("Выводим ошибку " + err.getCode() + " " + err.getErrText());
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
