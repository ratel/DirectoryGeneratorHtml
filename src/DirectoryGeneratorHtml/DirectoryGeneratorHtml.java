package DirectoryGeneratorHtml;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 13.07.13
 * Time: 11:24
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryGeneratorHtml {

    void buildHtml(Writer outStream, String dir) throws IOException {
        outStream.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n");
        outStream.write("<HTML>\n");

        buildHead(outStream);
        buildBody(outStream, dir);

        outStream.write("</HTML>\n");
        outStream.flush();
    }

    void buildHead(Writer outStream) throws IOException {
        outStream.write("\t<HEAD>\n");

        outStream.write("\t\t<TITLE> Содержание каталога </TITLE>\n");
        outStream.write("\t\t<META NAME=\"Generator\" CONTENT=\"HtmlGenerator\">\n");
        outStream.write("\t\t<META NAME=\"Author\" CONTENT=\"\">\n");
        outStream.write("\t\t<META NAME=\"Keywords\" CONTENT=\"\">\n");
        outStream.write("\t\t<META NAME=\"Description\" CONTENT=\"\">\n");
        outStream.write("\t\t<META http-equiv= \"content-type\"  content= \"text/html; charset=utf-8\">\n");

        outStream.write("\t</HEAD>\n");
    }

    void buildBody(Writer outStream, String dir) throws IOException {
        File resource= new File(dir);

        outStream.write("\t<BODY>\n");

        outStream.write("\t\t<Table border= \"0\"  cellpadding= \"5\"  cellspacing= \"0\"  align= \"left\">\n");
        outStream.write("\t\t\t<caption> Содержимое каталога " + resource.getAbsolutePath() + "\n");
        outStream.write("\t\t\t</caption>\n");

        File [] fMas= resource.listFiles();
        List<File> fList= new ArrayList<>();

        if (fMas == null)
            return;

        Collections.addAll(fList, fMas);
        Collections.sort(fList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isFile() == o2.isFile()) {
                    return o1.getAbsolutePath().compareToIgnoreCase(o2.getAbsolutePath());
                }
                else
                    return ((o1.isFile()) ?  1 : -1);
            }
        });

        BasicFileAttributes fileAttr;

        outStream.write("\t\t\t<tr>\n");
        outStream.write("\t\t\t\t<th align= \"left\"><a href= \""+ "../"/*resource.getParentFile().getName()*/ + "\">" +
                resource.getParent() + "</a> </th>\n");
        outStream.write("\t\t\t</tr>\n");

        outStream.write("\t\t\t<tr>\n");
        outStream.write("\t\t\t\t<th align= \"left\">Имя</th>\n");
        outStream.write("\t\t\t\t<th align= \"left\">Размер</th>\n");
        outStream.write("\t\t\t\t<th align= \"left\">Дата модификации</th>\n");
        outStream.write("\t\t\t</tr>\n");

        if (fList != null)
            for (File f: fList) {
                fileAttr= Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                outStream.write("\t\t\t<tr>\n");
                //outStream.write("\t\t\t\t<th align= \"left\"><a href= \"file:///"+ f.getAbsolutePath() + "\">" +
                //        f.getName() + "</a> </th>\n");
                outStream.write("\t\t\t\t<th align= \"left\"><a href= \""+ f.getName() +
                        (f.isDirectory() ? "/" : "") +
                        "\">" +
                        f.getName() + "</a> </th>\n");
                outStream.write("\t\t\t\t<th align= \"left\">" + ((f.isFile()) ? f.length() : " ") + "</th>\n");
                outStream.write("\t\t\t\t<th align= \"left\">" + fileAttr.creationTime() + "</th>\n");
                outStream.write("\t\t\t</tr>\n");
            }

        outStream.write("\t\t</Table>\n");
        outStream.write("\t</BODY>\n");
    }
}
