import static spark.Spark.*;
import java.io.File;
import com.adobe.epubcheck.api.EpubCheck;

public class Main {
    public static void main(String[] args) {
        port(8080);

        get("/validate/:filename", (request, response) -> {
            response.type("application/json"); // Set response type to JSON

            String filename = request.params(":filename");

            File epubFile = new File("/tmp/" + filename);

            JSONReport report = new JSONReport();
            EpubCheck epubcheck = new EpubCheck(epubFile, report);

            epubcheck.check();
            return report.toString();
        });
    }
}
