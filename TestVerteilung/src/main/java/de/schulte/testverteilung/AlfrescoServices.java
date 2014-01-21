package de.schulte.testverteilung;

import java.util.logging.Logger;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.Response;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 19.12.13
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoServices {

    private AlfrescoConnectorNew con;

    private static Logger logger = Logger.getLogger(AlfrescoServices.class.getName());

    public AlfrescoServices(String server, String username, String password)  {
        super();
        this.con = new AlfrescoConnectorNew(username, password, server);
    }


    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     *
     * @param filePath   der Pfad, der geliefert werden soll
     * @param listFolder was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @return ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *         false    ein Fehler ist aufgetreten
     *         ret               der Inhalt des Verzeichnisses als JSON Objekte
     */
    public JSONObject listFolderAsJSON(String filePath, int listFolder) {
        JSONObject o;
        JSONObject o1;
        JSONArray list = new JSONArray();
        JSONObject ret = new JSONObject();
        try {
            // keine Parameter mit gegeben, also den Rooteintrag erzeugen
            if (filePath == null || filePath.length() == 0) {
                o = new JSONObject();
                o1 = new JSONObject();
                o.put("id", filePath);
                o.put("rel", "root");
                o.put("state", "closed");
                o1.put("attr", o);
                o1.put("data", "Archiv");
                o1.put("state", "closed");
                list.put(o1);
            } else {
                // das Root Object übergen?
                if (filePath.equals("-1"))
                    filePath = con.getNodeId("company_home/Archiv");

                Iterator<CmisObject> it = con.listFolder(filePath).iterator();

                while (it.hasNext()) {
                    CmisObject obj = it.next();
                    o = new JSONObject();
                    o1 = new JSONObject();
                    o.put("id", obj.getId());
                    if (obj instanceof Folder) {
                        o.put("rel", "folder");
                        o1.put("state", "closed");
                    } else {
                        o.put("rel", "default");
                        o1.put("state", "");
                    }
                    if (obj instanceof AlfrescoDocument && ((AlfrescoDocument) obj).hasAspect("P:cm:titled") && obj.getPropertyValue("cm:title").toString().length() > 0)
                        o1.put("data", obj.getPropertyValue("cm:title"));
                    else
                        o1.put("data", obj.getName());
                    o1.put("attr", o);
                    list.put(o1);
                }
            }
            ret.put("success", true);
            ret.put("result", list);
        } catch (Throwable t) {
            try {
                ret.put("success", false);
                ret.put("result", t.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getMessage());
                jse.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * liefert eine NodeId
     * reine Wrapper Methode, die hier nichts zusätzliches mehr machen muss.
     *
     * @param path der Pfad zum Dokument
     * @return ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                 false   ein Fehler ist aufgetreten
     *                                        ret     die Id des Knotens
     */
    public JSONObject getNodeId(String path) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("success", true);
            obj.put("result", con.getNodeId(path));
        } catch (Throwable t) {
            try {
                obj.put("success", false);
                obj.put("result", t.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getMessage());
                jse.printStackTrace();
            }
        }
        return obj;
    }

    /**
     * liefert eine NodeId  mit Hilfe einer CMIS Query
     * reine Wrapper Methode, die hier nichts zusätzliches mehr machen muss.
     *
     * @param cmisQuery die CMIS Query zum suchen
     * @return ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                 false   ein Fehler ist aufgetreten
     *                                        ret     die Id des Knotens
     */
    public JSONObject findDocument(String cmisQuery)  {


        JSONObject obj = new JSONObject();
        try {
            obj.put("success", true);
            obj.put("result", con.findDocument(cmisQuery));
        } catch (Throwable t) {
            try {
                obj.put("success", false);
                obj.put("result", t.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getMessage());
                jse.printStackTrace();
            }
        }
        return obj;

    }


}
