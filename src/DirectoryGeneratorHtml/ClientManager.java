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
    String sharedDir;
    static String currentDir= "C:\\Programms\\Java\\projects IDEA\\repositoris\\DirectoryGeneratorHtml";

    ClientManager(Socket s, String sharedDir) {
        this.s= s;
        this.sharedDir= sharedDir;
        //currentDir= sharedDir;
    }

    @Override
    public void run() {
        try {
            InputStream input= s.getInputStream();
            BufferedReader reader= new BufferedReader(new InputStreamReader(input, "Cp1251"));
            OutputStream output= s.getOutputStream();
            OutputStreamWriter writer= new OutputStreamWriter(output);
            String stringCmd= "";
            String [] cmdSplit;

            while (true) {
                stringCmd= reader.readLine();

                if (stringCmd == null) break;

                System.out.println("Получена строка:");
                System.out.println(stringCmd);
                cmdSplit= stringCmd.split(" ");

                if (cmdSplit.length > 0) {
                    if (CMDHEAD.compareToIgnoreCase(cmdSplit[0]) == 0) {
                        doCmdHead(output, cmdSplit);
                        output.flush();
                    }
                    else if (CMDGET.compareToIgnoreCase(cmdSplit[0]) == 0) {
                        doCmdGet(output, cmdSplit);
                        output.flush();
                    }
                    else {
                        doErrorAnswer(output, ErrorCode.UNKNOWNCMD);
                        output.flush();
                    }
                }
            }

            output.close();
            System.out.println("Клиентский поток отработал");
        } catch (IOException e) {
            e.printStackTrace();
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
                currentDir= resource.getAbsolutePath();
                pathIndex= resourcePath + "index.html";

                fileIndex= new File(pathIndex);

                if (!fileIndex.exists()) {
                    DirectoryGeneratorHtml dgh= new DirectoryGeneratorHtml();

                    try {
                        OutputStreamWriter indexWriter= new OutputStreamWriter(writer);
                        dgh.buildHtml(indexWriter, resourcePath);

                        sendHead(writer, "text/html", "0");
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

            try {
                //sendHead(writer, new MimetypesFileTypeMap().getContentType(resource), String.valueOf(resource.length()));
                sendHead(writer, URLConnection.getFileNameMap().getContentTypeFor(resource.getAbsolutePath()), String.valueOf(resource.length()));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        else {
            System.out.println("Запрошенный ресурс не найден");
            doErrorAnswer(writer, ErrorCode.NORESOURCE);
        }
    }

    protected String formPathResource(String resName) {
        final String parentDir= "..";
        String resourcePath= currentDir;
        int i;

        if (resName.length() >= parentDir.length())
            if (resName.endsWith(parentDir)) {
                if (resourcePath.length() > sharedDir.length()) {
                    i= resourcePath.lastIndexOf('\\');
                    if (i > 0)
                        resourcePath= resourcePath.substring(0, i);
                }
                resName= "";
            }

        return (resourcePath + resName);
    }

    protected void doCmdGet(OutputStream writer, String [] cmdSplit) {
        if (cmdSplit.length == 0)
            return;

        File resource, fileIndex;
        String resourcePath= formPathResource(cmdSplit[1]);
        String pathIndex;

        resource= new File(resourcePath);

        System.out.println("\tПытаемся послать ресурс: " + resource.getAbsolutePath());

        if (resource.exists()) {
            if (resource.isDirectory()) {
                currentDir= resource.getAbsolutePath();
                pathIndex= resourcePath + "index.html";

                System.out.println("\tНовая текущая директория: " + currentDir);

                fileIndex= new File(pathIndex);

                if (!fileIndex.exists()) {
                    DirectoryGeneratorHtml dgh= new DirectoryGeneratorHtml();

                    try {
                        sendHead(writer, "text/html", "1");
                        dgh.buildHtml(new OutputStreamWriter(writer), resourcePath);
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

        outPut.write(("Content-Type: " + fileType + "\r\n"));
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

        //String errData = "<html><title>Error " + err.getCode() + "</title><body>" + err.getErrText() + "</body></html>";
        String errData = "<html><title>Error " + err.getCode() + "</title>" +
                "\t\t<META http-equiv= \"content-type\"  content= \"text/html; charset=utf-8\">\n" +
                "<body>" + err.getErrText() + "</body></html>";


        try {
            sendHead(output, "text/html", String.valueOf(errData.length()));
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
