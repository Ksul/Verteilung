package de.schulte.testverteilung;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.logging.Logger;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 19.12.13
 * Time: 15:37
 */
public class VerteilungServices {

    private AlfrescoConnectorNew con;

    private static Logger logger = Logger.getLogger(VerteilungServices.class.getName());

    public VerteilungServices(String server, String username, String password)  {
        super();
        this.con = new AlfrescoConnectorNew(username, password, server);
    }


    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     *
     * @param filePath   der Pfad, der geliefert werden soll
     * @param listFolder was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @return ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                 false    ein Fehler ist aufgetreten
     *                                        ret               der Inhalt des Verzeichnisses als JSON Objekte
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
                // das Root Object übergeben?
                if (filePath.equals("-1"))
                    filePath = con.getNode("company_home/Archiv").getId();

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
     *                                        ret      die Id des Knotens
     */
    public JSONObject getNodeId(String path) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("success", true);
            obj.put("result", con.getNode(path).getId());
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
     *                                        ret      das Document als JSON Object
     */
    public JSONObject findDocument(String cmisQuery)  {

        JSONObject obj = new JSONObject();
        try {
            obj.put("success", true);
            JSONObject obj1 = new JSONObject();

            AlfrescoDocument doc = con.findDocument(cmisQuery);

            Iterator<Property<?>> iter = doc.getProperties().iterator();
            while (iter.hasNext()){
                Property prop = iter.next();
                obj1.put(prop.getLocalName(), prop.getValueAsString());
            }
           obj.put("result", obj1.toString());
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
     * liefert den Inhalt eines Dokumentes
     * @param documentId            die Document Id als String
     * @param extract               wenn gesetzt, wird der Inhalt als lesbarer String zuürckgegeben
     * @return ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                 false   ein Fehler ist aufgetreten
     *                                        ret      der Inhalt als String
     */
    public JSONObject getDocumentContent(String documentId, boolean extract) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("success", true);
            obj.put("result", con.getDocumentContent(documentId));
            if (obj.getBoolean("success")) {
                if (extract) {
                    PDFConnector con = new PDFConnector();
                    InputStream is = new ByteArrayInputStream((byte[]) obj.get("result"));
                    obj.put("result", con.pdftoText(is));
                } else
                    try {
                        obj.put("result", new String((byte[]) obj.get("result"), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        obj.put("success", false);
                        obj.put("result", e.getMessage());
                    }
            }
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
     * lädt ein Dokument hoch
     * @param folderPath         der Pfad des Zielfolders als String
     * @param fileName          der Name der Datei als String
     * @return ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                 false   ein Fehler ist aufgetreten
     *                                        ret      bei Erfolg die Id als String, ansonsten der Fehler
     */
    public JSONObject uploadDocument(String folderPath, String fileName) {
        JSONObject obj = new JSONObject();
        try {
            String typ = null;
            if (fileName.toLowerCase().endsWith(".pdf"))
                typ = "application/pdf";
            File file = new File(fileName);
            CmisObject cmisObject = con.getNode(folderPath);
            if (cmisObject != null && cmisObject instanceof Folder) {
                String id = con.uploadDocument(((Folder) cmisObject), file, typ);
                obj.put("success", true);
                obj.put("result", id);
            } else {
                obj.put("success", false);
                obj.put("result", "Der verwendete Pfad " + folderPath + " ist kein Folder!");
            }
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
     * löscht ein Dokument
     * @param folderName        der Name des Folders in dem sich das Dokument befindet als String
     * @param documentName      der Name des Dokumentes als String
     * @return ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                 false   ein Fehler ist aufgetreten
     *                                        ret      bei Erfolg nichts, ansonsten der Fehler
     */
    public JSONObject deleteDocument(String folderName, String documentName) {
        JSONObject obj = new JSONObject();
        try {
            CmisObject document;
            CmisObject folder;
            folder = con.getNode(folderName);
            if (folder != null && folder instanceof Folder) {
                document = con.getNode(((Folder) folder).getPath() + "/" + documentName);
                if (document != null && document instanceof Document) {
                    ((Document) document).delete(true);
                    obj.put("success", true);
                    obj.put("result", "");
                } else {
                    obj.put("success", false);
                    obj.put("result", document == null ? "Ein Document mit dem Namen " + documentName + " ist nicht vorhanden!" : "Das verwendete Document " + documentName + " ist nicht vom Typ Document!");
                }
            } else {
                obj.put("success", false);
                obj.put("result", folder == null ? "Der Pfad " + folderName + "  ist nicht vorhanden!" : "Der verwendete Pfad " + folderName + " ist kein Folder!");
            }
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
