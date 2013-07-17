package DirectoryGeneratorHtml;

import java.io.*;
import java.net.Socket;

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
                System.out.println("Ожидаем команду");
                stringCmd= reader.readLine();

                if (stringCmd == null) break;

                System.out.println("Получена строка:");
                System.out.println(stringCmd);
                cmdSplit= stringCmd.split(" ");
                if (cmdSplit.length > 0) {
                    if (CMDHEAD.compareToIgnoreCase(stringCmd) == 0) {
                        sendHead(writer);
                    }
                    if (CMDGET.compareToIgnoreCase(stringCmd) == 0) {
                        sendResource(writer, cmdSplit);
                    }
                }
            }

            writer.close();
            System.out.println("Клиентский поток отработал");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void sendHead(OutputStreamWriter writer) {
        //writer.write("");
        //writer.flush();
    }

    private void sendResource(OutputStreamWriter writer, String [] cmdSplit) {

    }
}
