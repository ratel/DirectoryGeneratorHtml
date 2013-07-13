package DirectoryGeneratorHtml;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 13.07.13
 * Time: 11:24
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryGeneratorHtml {
    private File resource= null;

    void buildHtml() {
        if (resource == null) {
            resource= new File("");
        }

        File [] fList= resource.listFiles();

        System.out.println(Runtime.getRuntime().totalMemory());
/*        for(File f: fList) {
            System.out.println(f.getAbsoluteFile());
        }*/
    }
}
