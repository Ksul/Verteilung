package de.schulte.testverteilung;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
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

    /**
     * Konstruktor
     */
    public VerteilungServices() {
        super();
    }

    /**
     * Konstruktor
     * @param server
     * @param username
     * @param password
     */
    public VerteilungServices(String server, String username, String password)  {
        super();
        this.con = new AlfrescoConnectorNew(username, password, server);
    }


    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     *
     * @param  filePath          der Pfad, der geliefert werden soll
     * @param  listFolder        was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            der Inhalt des Verzeichnisses als JSON Objekte
     */
    public JSONObject listFolderAsJSON(String filePath, int listFolder) {
        JSONObject o;
        JSONObject o1;
        JSONArray list = new JSONArray();
        JSONObject obj = new JSONObject();
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
                    filePath = con.getNode("/Archiv").getId();

                Iterator<CmisObject> it = con.listFolder(filePath).iterator();

                while (it.hasNext()) {
                    CmisObject cmisObject = it.next();
                    o = new JSONObject();
                    o1 = new JSONObject();
                    o.put("id", cmisObject.getId());
                    if (cmisObject instanceof Folder) {
                        o.put("rel", "folder");
                        o1.put("state", "closed");
                    } else {
                        o.put("rel", "default");
                        o1.put("state", "");
                    }
                    if (cmisObject instanceof AlfrescoDocument && ((AlfrescoDocument) cmisObject).hasAspect("P:cm:titled") && cmisObject.getPropertyValue("cm:title").toString().length() > 0)
                        o1.put("data", cmisObject.getPropertyValue("cm:title"));
                    else
                        o1.put("data", cmisObject.getName());
                    o1.put("attr", o);
                    list.put(o1);
                }
            }
            obj.put("success", true);
            obj.put("result", list);
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * liefert eine NodeId
     * reine Wrapper Methode, die hier nichts zusätzliches mehr machen muss.
     *
     * @param  path         der Pfad zum Dokument
     * @return obj          ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                              false   ein Fehler ist aufgetreten
     *                                                     result   die Id des Knotens
     */
    public JSONObject getNodeId(String path) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("success", true);
            obj.put("result", con.getNode(path).getId());
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * liefert eine NodeId  mit Hilfe einer CMIS Query
     * reine Wrapper Methode, die hier nichts zusätzliches mehr machen muss.
     *
     * @param  cmisQuery        die CMIS Query zum suchen
     * @return obj              ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                  false   ein Fehler ist aufgetreten
     *                                                         result           das Document als JSON Object
     */
    public JSONObject findDocument(String cmisQuery)  {

        JSONObject obj = new JSONObject();
        try {
            obj.put("success", true);
            JSONObject obj1 = new JSONObject();

            Document doc = con.findDocument(cmisQuery);

            obj.put("result", convertDocumentToJSON(doc).toString());
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * konvertiert ein Document in ein JSON Objekt
     * @param  doc           das Dokument
     * @return obj1          das Dokument als JSON Objekt
     * @throws JSONException
     */
    private JSONObject convertDocumentToJSON(Document doc) throws JSONException {
        JSONObject obj1 = new JSONObject();
        Iterator<Property<?>> iter = doc.getProperties().iterator();
        while (iter.hasNext()){
            Property prop = iter.next();
            obj1.put(prop.getLocalName(), prop.getValueAsString());
        }
        return obj1;
    }

    /**
     * liefert den Inhalt eines Dokumentes
     * @param  documentId            die Document Id als String
     * @param  extract               wenn gesetzt, wird der Inhalt als lesbarer String zuürckgegeben
     * @return obj                   ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                       false   ein Fehler ist aufgetreten
     *                                                              result           der Inhalt als String
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
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * lädt ein Dokument hoch
     * @param  folderPath       der Pfad des Zielfolders als String
     * @param  fileName         der Name der Datei als String
     * @return obj              ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                  false   ein Fehler ist aufgetreten
     *                                                         result           bei Erfolg die Id als String, ansonsten der Fehler
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
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * löscht ein Dokument
     * @param  folderName        der Name des Folders in dem sich das Dokument befindet als String
     * @param  documentName      der Name des Dokumentes als String
     * @return obj               ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                   false   ein Fehler ist aufgetreten
     *                                                          result           bei Erfolg nichts, ansonsten der Fehler
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
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * erstellt ein Dokument
     * @param  folderName           der Name des Folders in dem das Dokument erstellt werden soll als String
     * @param  documentName         der Name des Dokumentes als String
     * @param  documentContent      der Inhalt als String
     * @param  documentType         der Typ des Dokumentes
     * @param  extraCMSProperties   zusätzliche Properties
     * @return obj                  ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                      false   ein Fehler ist aufgetreten
     *                                                             result           das Document als JSON Object
     */
    public JSONObject createDocument(String folderName, String documentName,
                                     String documentContent,
                                     String documentType,
                                     String extraCMSProperties) {
        JSONObject obj = new JSONObject();
        try {
            CmisObject document;
            CmisObject folder;
            folder = con.getNode(folderName);
            if (folder != null && folder instanceof Folder) {
                Map<String, Object> outMap = null;
                if (extraCMSProperties != null && extraCMSProperties.length() > 0) {
                    JSONObject props = new JSONObject(extraCMSProperties);
                    Iterator<String> nameItr = props.keys();
                    outMap = new HashMap<String, Object>();
                    while (nameItr.hasNext()) {
                        String name = nameItr.next();
                        outMap.put(name, props.get(name));
                    }
                }
                document = con.createDocument((Folder) folder, documentName, documentContent.getBytes(), documentType, outMap);
                if (document != null && document instanceof Document) {
                    obj.put("success", true);
                    obj.put("result", convertDocumentToJSON((Document) document).toString());
                } else {
                    obj.put("success", false);
                    obj.put("result", document == null ? "Ein Document mit dem Namen " + documentName + " ist nicht vorhanden!" : "Das verwendete Document " + documentName + " ist nicht vom Typ Document!");
                }
            } else {
                obj.put("success", false);
                obj.put("result", folder == null ? "Der Pfad " + folderName + "  ist nicht vorhanden!" : "Der verwendete Pfad " + folderName + " ist kein Folder!");
            }
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * aktualisiert den Inhalt eines Documentes
     * @param documentName          der Pfad des Dokumentes
     * @param documentContent       der neue Inhalt als Bytearray
     * @return obj                  ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                      false   ein Fehler ist aufgetreten
     *                                                             result           bei Erfolg die ObjectId
     */
    public JSONObject updateDocument(String documentName, byte documentContent[]) {
        JSONObject obj = new JSONObject();
        try {
            Document document = (Document) con.getNode(documentName);
            InputStream stream = new ByteArrayInputStream(documentContent);
            ContentStream contentStream = new ContentStreamImpl(document.getName(), BigInteger.valueOf(documentContent.length), "text/plain", stream);
            ObjectId objectId = document.setContentStream(contentStream, true, true);
            obj.put("success", true);
            obj.put("result", objectId);
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }


    /**
     * liest die Testproperties
     * nur für Testzwecke
     * @param propFile       der Name der Properties Datei
     * @return               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                               false    ein Fehler ist aufgetreten
     *                                                      result            die Properties als JSON Objekte
     */
    public JSONObject loadProperties(String propFile) {
        JSONObject obj = new JSONObject();
        try {
            Properties properties = new Properties();
            InputStream inp = new FileInputStream(new File(new URI(propFile)));
            properties.load(inp);
            obj.put("success", true);
            obj.put("result", new JSONObject(properties));
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }
 }