import com.adobe.epubcheck.api.*;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.messages.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONArray;

public class JSONReport implements Report {
    private int errorCount = 0;
    private int warningCount = 0;
    private int fatalErrorCount = 0;
    private int infoCount = 0;
    private int usageCount = 0;
    private int reportingLevel = 0;

    private String epubFileName;
    private String customMessageFile;
    private File overrideFile;

    private int seq = 0;
    private final List<HashMap<String, String>> messages = new ArrayList<>();
    private HashMap<String, HashMap<String, String>> resources = new HashMap<>();
    private MessageDictionary dictionary = new LocalizedMessageDictionary();

    @Override
    public void message(MessageId id, EPUBLocation location, Object... args) {
        HashMap<String, String> map = new HashMap<>();
        map.put("seq", "" + ++seq);

        map.put("id", id.toString());
        map.put("url", location.url.toString());
        map.put("path", location.path);
        map.put("line", "" + location.line);
        map.put("column", "" + location.column);
        // map.put("context", "" + location.context);

        Message message = dictionary.getMessage(id);
        map.put("severity", message.getSeverity().toString().toLowerCase());
        map.put("original_severity", message.getOriginalSeverity().toString().toLowerCase());
        map.put("message", message.getMessage(args));
        map.put("suggestion", message.getSuggestion());

        messages.add(map);
    }

    @Override
    public void message(Message message, EPUBLocation location, Object... args) {
        HashMap<String, String> map = new HashMap<>();
        map.put("seq", "" + ++seq);

        map.put("url", location.url.toString());
        map.put("path", location.path);
        map.put("line", "" + location.line);
        map.put("column", "" + location.column);
        // map.put("context", "" + location.context);

        map.put("severity", message.getSeverity().toString().toLowerCase());
        map.put("original_severity", message.getOriginalSeverity().toString().toLowerCase());
        map.put("message", message.getMessage(args));
        map.put("suggestion", message.getSuggestion());

        messages.add(map);
    }

    @Override
    public void info(String resource, FeatureEnum feature, String value) {
        HashMap<String, String> rs = (HashMap<String, String>) resources.get(resource);
        if (rs == null) {
            rs = new HashMap<String, String>();
            resources.put(resource, rs);
        }

        rs.put(feature.toString(), value);
    }

    @Override
    public int getErrorCount() {
        return errorCount;
    }

    @Override
    public int getWarningCount() {
        return warningCount;
    }

    @Override
    public int getFatalErrorCount() {
        return fatalErrorCount;
    }

    @Override
    public int getInfoCount() {
        return infoCount;
    }

    @Override
    public int getUsageCount() {
        return usageCount;
    }

    @Override
    public int generate() {
        // Implement the report generation logic
        return 0; // Return a status code or similar
    }

    @Override
    public void initialize() {
        // Initialize or reset the report
    }

    @Override
    public void setEpubFileName(String value) {
        this.epubFileName = value;
    }

    @Override
    public String getEpubFileName() {
        return epubFileName;
    }

    @Override
    public void setCustomMessageFile(String customMessageFileName) {
        this.customMessageFile = customMessageFileName;
    }

    @Override
    public String getCustomMessageFile() {
        return customMessageFile;
    }

    @Override
    public int getReportingLevel() {
        return reportingLevel;
    }

    @Override
    public void setReportingLevel(int level) {
        this.reportingLevel = level;
    }

    @Override
    public void close() {
        // Implement the logic to close and finalize the report
    }

    @Override
    public void setOverrideFile(File customMessageFile) {
        this.overrideFile = customMessageFile;
    }

    @Override
    public MessageDictionary getDictionary() {
        // Return a MessageDictionary instance, if applicable
        return null; // Placeholder for actual implementation
    }

    @Override
    public String toString() {

        JSONObject jsonObject = new JSONObject();

        JSONObject res = new JSONObject();
        jsonObject.put("resources", res);

        // flatten
        for (Map.Entry<String, HashMap<String, String>> outerEntry : resources.entrySet()) {

            JSONObject outer = null;
            String outerKey = outerEntry.getKey();
            JSONObject dest = null;

            if (outerKey != null) {
                outer = new JSONObject();
                res.put(outerKey, outer);
            }

            HashMap<String, String> innerMap = outerEntry.getValue();

            // Now iterate through the inner HashMap
            for (Map.Entry<String, String> innerEntry : innerMap.entrySet()) {

                String innerKey = innerEntry.getKey();
                String innerValue = innerEntry.getValue();

                innerKey = innerKey.replaceAll("[^a-zA-Z0-9]", "_");

                if (innerKey == null) {
                    innerKey = "undefined";
                }

                dest = outer == null ? jsonObject : outer;

                if (innerValue.equals("true")) {
                    dest.put(innerKey, true);
                } else if (innerValue.equals("false")) {
                    dest.put(innerKey, false);
                } else {
                    try {
                        int val = Integer.parseInt(innerValue);
                        dest.put(innerKey, val);
                    } catch (NumberFormatException e) {
                        dest.put(innerKey, innerValue);
                    }
                }

                // check for messages

            }

            for (int i = 0; i < messages.size(); i++) {
                HashMap<String, String> item = messages.get(i);
                if (dest != null && item.get("path").equals(outerKey)) {

                    JSONArray jar;
                    if (dest.has("messages")) {
                        jar = dest.getJSONArray("messages");
                    } else {
                        jar = new JSONArray();
                        dest.put("messages", jar);
                    }

                    JSONObject msg = new JSONObject();
                    for (Map.Entry<String, String> entry : item.entrySet()) {
                        if (!entry.getKey().equals("path")) {
                            msg.put(entry.getKey(), entry.getValue());
                        }
                    }

                    jar.put(msg);
                }
            }

        }

        return jsonObject.toString();
    }
}
