import java.io.*;

public class FileHelper {
    public void testRead(String fileName) throws IOException {
        File file = new File(fileName);
        Reader reader = new FileReader(file);
        BufferedReader breader = new BufferedReader(reader);
        String line;
        while ((line = breader.readLine()) != null) {
            System.out.println(line);
        }
    }
}
