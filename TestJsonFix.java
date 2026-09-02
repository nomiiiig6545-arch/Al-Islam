import java.io.File;
import java.nio.file.Files;
public class TestJsonFix {
    public static void main(String[] args) throws Exception {
        File file = new File("app/src/main/assets/quran/quran_text.json");
        byte[] bytes = Files.readAllBytes(file.toPath());
        String text = new String(bytes, "UTF-8");
        
        System.out.println("Ends with: " + text.substring(text.length() - 20));
        
        // Find the last valid completely closed surah object
        // The JSON is like {"1":[{"num":...}],"2":[...]}
        // We can just find the last "]" and truncate everything after it, then add "}"
        int lastBracket = text.lastIndexOf(']');
        if (lastBracket != -1) {
            String fixed = text.substring(0, lastBracket + 1) + "}";
            System.out.println("Fixed ends with: " + fixed.substring(fixed.length() - 20));
            // Let's try parsing it with standard library or just trust it.
        }
    }
}
