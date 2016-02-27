package de.schulte.testverteilung;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 21.02.14
 * Time: 14:17
 * To change this template use File | Settings | File Templates.
 */
public class VerteilungHelper {

    private static Logger logger = Logger.getLogger(VerteilungHelper.class.getName());

    /**
     * verpackt ein Throwable in ein JSON Object
     * @param   e             das Throwable
     * @return  obj           das JSON Object
     */
    public static JSONObject convertErrorToJSON(Throwable e) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("success", false);
            obj.put("result", "ExceptionMessage: " + e.getMessage() != null ? e.getMessage() +"\n" : e.toString() + "\n");
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                sb.append(element.toString());
                sb.append("\n");
            }
            obj.put("error", sb.toString());
        } catch (JSONException jse) {
            logger.severe(jse.getMessage());
            jse.printStackTrace();
        }
        return obj;
    }

    /**
     * konvertiert die ObjectId in ein verwertbares Format
     * das bedeudet, dass die Versionsinformation und die Store Information abgeschnitten wird
     * @param id            die übergebene Id
     * @return              die konvertirte Id
     */
    public static String normalizeObjectId(String id) {
        if (id.contains(";"))
            id = id.substring(0, id.lastIndexOf(';'));
        if (id.startsWith("workspace://SpacesStore/"))
            id = id.substring(24);
        return id;
    }
}
