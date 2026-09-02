import java.io.File;
import java.nio.file.Files;
public class TestRead {
    public static void main(String[] args) throws Exception {
        File file = new File("app/src/main/assets/quran/quran_text.json");
        System.out.println("File length: " + file.length());
        byte[] bytes = Files.readAllBytes(file.toPath());
        System.out.println("Byte length: " + bytes.length);
        String text = new String(bytes, "UTF-8");
        System.out.println("Text length: " + text.length());
    }
}
