package de.schulte.testverteilung;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 19.12.13
 * Time: 15:37
 */
public class VerteilungServices {

    private AlfrescoConnector con;

    private static Logger logger = Logger.getLogger(VerteilungServices.class.getName());


    // Speicher für Files
    private Collection<FileEntry> entries = new ArrayList<>();

    /**
     * Konstruktor
     */
    public VerteilungServices() {
        super();
    }

    /**
     * Konstruktor
     * @param server         die URL des Alfresco Servers
     * @param binding        der Binding Teil der URL
     * @param username       der verwendete Username
     * @param password       das Passwort
     */
    public VerteilungServices(String server,
                              String binding,
                              String username,
                              String password)  {

        super();
        this.con = new AlfrescoConnector(username, password, server, binding);
    }

    /**
     * über diese Methode können die Alfresco Parameter nachträglich gesetzt werden.
     * @param server         der Name des Alfresco Servers
     * @param binding        der Binding teil der URL
     * @param username       der verwendete Username
     * @param password       das Passwort
     */
    public void setParameter(String server,
                             String binding,
                             String username,
                             String password) {
        this.con = new AlfrescoConnector(username, password, server, binding);
    }


    /**
     * liefert ein Ticket zur Authentifizierung
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            das Ticket als JSON Objekt
     */
    public JSONObject getTicket() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("success", true);
            obj.put("result", con.getTicket());

        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * liefert die Kommentare zu einem Knoten
     * @param documentId    die Id des Knoten/Folder
     * @param ticket        das Ticket zur Identifizierung
     * @return obj          ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                              false    ein Fehler ist aufgetreten
     *                                                     result            die Kommentare  als JSON Objekt
     */
    public JSONObject getComments(String documentId, String ticket) {

        JSONObject obj = new JSONObject();
        try {
            CmisObject cmisObject = con.getNodeById(documentId);
            obj.put("success", true);
            obj.put("result", con.getComments(cmisObject, ticket));
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * Fügt zu einem Knoten einen neuen Kommentar hinzu
     * @param documentId    die Id des Knoten/Folder
     * @param ticket        das Ticket zur Identifizierung
     * @param comment       der Kommentar
     * @return obj          ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                              false    ein Fehler ist aufgetreten
     *                                                     result            der neue Kommentare  als JSON Objekt
     */
    public JSONObject addComment(String documentId, String ticket, String comment){
        JSONObject obj = new JSONObject();
        try {
            CmisObject cmisObject = con.getNodeById(documentId);
            obj.put("success", true);
            obj.put("result", con.addComment(cmisObject, ticket, comment));
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
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
    public JSONObject listFolder(String filePath,
                                 int listFolder) {

        JSONObject o;
        JSONObject o1;
        JSONArray list = new JSONArray();
        JSONObject obj = new JSONObject();
        JSONObject state;
        try {
            state = new JSONObject("{state: {opened: false, disabled: false, selected: false}}");
             // das Root Object übergeben?
            if (filePath.equals("-1"))
                filePath = con.getNode("/Archiv").getId();

            for (CmisObject cmisObject : con.listFolder(filePath)) {

                // prüfen, ob das gefundene Objekt überhaupt ausgegeben werden soll
                if ((cmisObject instanceof Folder && listFolder < 1) || (cmisObject instanceof Document && listFolder > -1)) {
                    o = convertCMISObjectToJSON(cmisObject);
                    // prüfen, ob Children vorhanden sind
                    if (cmisObject instanceof Folder) {
                        ItemIterable<CmisObject> children = con.listFolder(cmisObject.getId());
                        o.put("hasChildren", children.getTotalNumItems() > 0);
                        boolean hasChildFolder = false;
                        boolean hasChildDocuments = false;
                        for (CmisObject childObject : con.listFolder(cmisObject.getId())) {
                            if (childObject instanceof Folder) {
                                hasChildFolder = true;
                            }
                            if (childObject instanceof Document) {
                                hasChildDocuments = true;
                            }
                            if (hasChildDocuments && hasChildFolder)
                                break;
                        }
                        o.put("hasChildFolder", hasChildFolder);
                        o.put("hasChildDocuments", hasChildDocuments);
                    }
                    o.put("parentId", filePath);
                    list.put(o);
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
            Document document = con.findDocument(cmisQuery);
            obj.put("success", true);
            obj.put("result", convertCMISObjectToJSON(document).toString());
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
    private JSONObject convertCMISObjectToJSON(CmisObject doc) throws JSONException {

        JSONObject obj1 = new JSONObject();
        for (Property prop : doc.getProperties()) {
            // falls Datumswert dann konvertieren
            if (prop.getDefinition().getPropertyType().equals(PropertyType.DATETIME) && prop.getValue() != null) {
                obj1.put(prop.getLocalName(), ((GregorianCalendar) prop.getValue()).getTime().getTime());
            } else if (prop.getLocalName().equals("objectId")) {
                String id = prop.getValueAsString();
                obj1.put(prop.getLocalName(), id);
                if (id.contains(";"))
                    id = id.substring(0, id.lastIndexOf(';'));
                if (id.startsWith("workspace://SpacesStore/"))
                    id = id.substring(24);
                obj1.put("objectID", id);
            } else
                obj1.put(prop.getLocalName(), prop.getValueAsString());
        }
        return obj1;
    }

    /**
     * liefert den Inhalt eines Dokumentes als String
     * @param  documentId            die Document Id als String
     * @param  extract               wenn gesetzt, wird der Inhalt als lesbarer String zuürckgegeben
     * @return obj                   ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                       false   ein Fehler ist aufgetreten
     *                                                              result           der Inhalt als String
     */
    public JSONObject getDocumentContent(String documentId,
                                         boolean extract) {

        JSONObject obj = new JSONObject();
        try {
            Document document = (Document) con.getNodeById(documentId);
            obj.put("success", true);
            obj.put("result", con.getDocumentContent(document));
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
     * @param  documentId              die Id des Zielfolders als String
     * @param  fileName                der Name der Datei als String
     * @return obj                     ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                         false   ein Fehler ist aufgetreten
     *                                                                 result          bei Erfolg die Id als String, ansonsten der Fehler
     */
    public JSONObject uploadDocument(String documentId,
                                     String fileName,
                                     String versionState) {

        JSONObject obj = new JSONObject();
        try {
            String typ = null;
            if (fileName.toLowerCase().endsWith(".pdf"))
                typ = "application/pdf";
            File file = new File(fileName);
            CmisObject cmisObject = con.getNodeById(documentId);
            if (cmisObject != null && cmisObject instanceof Folder) {
                String id = con.uploadDocument(((Folder) cmisObject), file, typ, createVersionState(versionState));
                obj.put("success", true);
                obj.put("result", id);
            } else {
                obj.put("success", false);
                obj.put("result", "Der verwendete Pfad ist kein Folder!");
            }
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * löscht ein Dokument
     * @param  documentId        die Id des Folders in dem sich das Dokument befindet als String
     * @return obj               ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                   false   ein Fehler ist aufgetreten
     *                                                          result           bei Erfolg nichts, ansonsten der Fehler
     */
    public JSONObject deleteDocument(String documentId) {

        JSONObject obj = new JSONObject();
        try {
            CmisObject document;
            CmisObject folderObject;
            document = con.getNodeById(documentId);
            if (document != null && document instanceof Document) {
                if (((Document) document).isVersionSeriesCheckedOut())
                    ((Document) document).cancelCheckOut();
                document.delete(true);
                obj.put("success", true);
                obj.put("result", "");
            } else {
                obj.put("success", false);
                obj.put("result", document == null ? "Das Document ist nicht vorhanden!" : "Das Document ist nicht vom Typ Document!");
            }
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * erstellt ein Dokument
     * @param  documentId           die Id des Folders in dem das Dokument erstellt werden soll als String
     * @param  documentName         der Name des Dokumentes als String
     * @param  documentContent      der Inhalt als Base64 decodierter String
     * @param  documentType         der Typ des Dokumentes
     * @param  extraCMSProperties   zusätzliche Properties
     * @param  versionState         der VersionsStatus ( none, major, minor, checkedout)
     * @return obj                  ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                      false   ein Fehler ist aufgetreten
     *                                                             result           das Document als JSON Object
     */
    public JSONObject createDocument(String documentId,
                                     String documentName,
                                     String documentContent,
                                     String documentType,
                                     String extraCMSProperties,
                                     String versionState) {

        //TODO Content als String oder als Stream?
        JSONObject obj = new JSONObject();
        try {
            CmisObject document;
            CmisObject folderObject;

            folderObject = con.getNodeById(documentId);
            Map<String, Object> outMap = null;
            if (folderObject != null && folderObject instanceof Folder) {

                if (extraCMSProperties != null && extraCMSProperties.length() > 0)
                  outMap = buildProperties(extraCMSProperties);

                document = con.createDocument((Folder) folderObject, documentName, Base64.decodeBase64(documentContent), documentType, outMap, createVersionState(versionState));
                if (document != null) {
                    obj.put("success", true);
                    obj.put("result", convertCMISObjectToJSON(document).toString());
                } else {
                    obj.put("success", false);
                    obj.put("result", "Ein Document mit dem Namen " + documentName + " ist nicht vorhanden!");
                }
            } else {
                obj.put("success", false);
                obj.put("result", folderObject == null ? "Der angegebene Pfad  ist nicht vorhanden!" : "Der verwendete Pfad ist kein Folder!");
            }
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }


    /**
     * aktualisiert den Inhalt eines Dokumentes
     * @param  documentId                Die Id des zu aktualisierenden Dokumentes
     * @param  documentContent           der neue Inhalt als Base64 decodierter String. Falls der Content <null> ist, dann werden nur die Properties upgedated.
     * @param  documentType              der Typ des Dokumentes
     * @param  extraCMSProperties        zusätzliche Properties
     * @param  versionState              der VersionsStatus ( none, major, minor, checkedout)
     * @param  versionComment            falls Dokuemnt versionierbar, dann kann hier eine Kommentar zur Version mitgegeben werden
     * @return obj                       ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                           false   ein Fehler ist aufgetreten
     *                                                                  result           bei Erfolg das Document als JSON Object, ansonsten der Fehler
     */
    public JSONObject updateDocument(String documentId,
                                     String documentContent,
                                     String documentType,
                                     String extraCMSProperties,
                                     String versionState,
                                     String versionComment) {

        //TODO Content als String oder als Stream?
        JSONObject obj = new JSONObject();
        try {
            Map<String, Object> outMap = new HashMap<>();

            CmisObject cmisObject = con.getNodeById(documentId);
            if (cmisObject != null && cmisObject instanceof Document) {
                if (extraCMSProperties != null && extraCMSProperties.length() > 0) {
                    outMap = buildProperties(extraCMSProperties);
                }
                Document document = con.updateDocument((Document) cmisObject, Base64.decodeBase64(documentContent), documentType, outMap, createVersionState(versionState), versionComment);
                obj.put("success", true);
                obj.put("result", convertCMISObjectToJSON(document).toString());
            } else {
                obj.put("success", false);
                obj.put("result", cmisObject == null ? "Ein Document mit der Id " + documentId + " ist nicht vorhanden!" : "Das verwendete Document mit der Id" + documentId + " ist nicht vom Typ Document!");
            }
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * aktualisiert die Properties eines Objectes
     * @param  documentId                Die Id des zu aktualisierenden Objectes
     * @param  extraCMSProperties        zusätzliche Properties
     * @return obj                       ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                           false   ein Fehler ist aufgetreten
     *                                                                  result           bei Erfolg das CmisObject als JSON Object, ansonsten der Fehler
     */
    public JSONObject updateProperties(String documentId,
                                       String extraCMSProperties) {
        JSONObject obj = new JSONObject();
        try {
            Map<String, Object> outMap = null;
            CmisObject cmisObject = con.getNodeById(documentId);
            if (cmisObject != null) {


                if (extraCMSProperties != null && extraCMSProperties.length() > 0)
                    outMap = buildProperties(extraCMSProperties);
                else {
                    obj.put("success", false);
                    obj.put("result", "keine Properties vorhanden!");
                }

                cmisObject = con.updateProperties(cmisObject, outMap);
                obj.put("success", true);
                obj.put("result", convertCMISObjectToJSON(cmisObject).toString());
            } else {
                obj.put("success", false);
                obj.put("result","Ein Document mit der Id " + documentId + " ist nicht vorhanden!");
            }
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * verschiebt ein Dokument
     * @param  documentId                das zu verschiebende Dokument
     * @param  oldFolderId               der alte Folder in dem das Dokument liegt
     * @param  newFolderId               der Folder, in das Dokument verschoben werden soll
     * @return obj                       ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                           false   ein Fehler ist aufgetreten
     *                                                                  result           bei Erfolg das Document als JSONObject, ansonsten der Fehler
     */
    public JSONObject moveDocument(String documentId,
                                   String oldFolderId,
                                   String newFolderId) {

        JSONObject obj = new JSONObject();
        try {
            CmisObject document = con.getNodeById(documentId);
            CmisObject oldFolder = con.getNodeById(oldFolderId);
            CmisObject newFolder = con.getNodeById(newFolderId);
            if (document != null && document instanceof Document) {
                if (oldFolder != null && oldFolder instanceof Folder) {
                    if (newFolder != null && newFolder instanceof Folder) {
                        CmisObject doc = con.moveDocument((Document) document, (Folder) oldFolder, (Folder) newFolder);
                        obj.put("success", true);
                        obj.put("result", convertCMISObjectToJSON(doc).toString());
                    } else {
                        obj.put("success", false);
                        obj.put("result", "Der verwendete Pfad mit der Id" + newFolderId + " ist kein Folder!");

                    }
                } else {
                    obj.put("success", false);
                    obj.put("result", oldFolder == null ? "Der Pfad mit der Id " + oldFolderId + "  ist nicht vorhanden!" : "Der verwendete Pfad mit der Id" + oldFolderId + " ist kein Folder!");

                }
            } else {
                obj.put("success", false);
                obj.put("result", document == null ? "Ein Document mit der Id " + documentId + " ist nicht vorhanden!" : "Das verwendete Document mit der Id" + documentId + " ist nicht vom Typ Document!");
            }
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * erstellt ein Dokument
     * @param  documentId              die Id des Folders in dem das Dokument erstellt werden soll als String
     * @param  extraCMSProperties      zusätzliche Properties
     * @return obj                     ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                         false   ein Fehler ist aufgetreten
     *                                                                result           der Folder als JSON Object
     */
    public JSONObject createFolder(String documentId,
                                   String extraCMSProperties) {

        JSONObject obj = new JSONObject();
        try {
            Folder folder;
            CmisObject target;
            Map<String, Object> outMap = null;

            if (extraCMSProperties != null && extraCMSProperties.length() > 0)
                outMap = buildProperties(extraCMSProperties);
            else {
                obj.put("success", false);
                obj.put("result", "keine Properties vorhanden!");
            }

            target = con.getNodeById(documentId);
            if (target != null && target instanceof Folder) {
                folder = con.createFolder((Folder) target, outMap);
                if (folder != null ) {
                    obj.put("success", true);
                    obj.put("result", convertCMISObjectToJSON(folder).toString());
                } else {
                    obj.put("success", false);
                    obj.put("result", "Ein Folder konnte nicht angelegt werden!" );
                }
            } else {
                obj.put("success", false);
                obj.put("result", target == null ? "Der Pfad " + target + "  ist nicht vorhanden!" : "Der verwendete Pfad " + target + " ist kein Folder!");
            }
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * löscht einen Folder
     * @param  documentId        die Id des Folders, der gelöscht werden soll
     * @return obj               ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                   false   ein Fehler ist aufgetreten
     *                                                          result           bei Erfolg nichts, ansonsten der Fehler
     */
    public JSONObject deleteFolder(String documentId) {

        JSONObject obj = new JSONObject();
        try {
            CmisObject folder;
            folder = con.getNodeById(documentId);
            if (folder != null && folder instanceof Folder) {
                List<String> list = ((Folder) folder).deleteTree(true, UnfileObject.DELETE, true);
                    obj.put("success", true);
                    obj.put("result", new JSONObject(list).toString());
            } else {
                obj.put("success", false);
                obj.put("result", folder == null ? "Der  angegebene Pfad ist nicht vorhanden!" : "Der verwendete Pfad st kein Folder!");
            }
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * liest die Testproperties
     *
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

    /**
     * prüft, ob eine Url verfügbar ist
     * @param urlString    URL des Servers
     * @return             ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            true, wenn die URL verfügbar ist
     */
    public JSONObject isURLAvailable(String urlString) {

        JSONObject obj = new JSONObject();
        URL url;
        try {
            url = new URL(urlString);
            logger.info("Umwandlung in URL " + url);
            HttpURLConnection httpUrlConn;
            httpUrlConn = (HttpURLConnection) url.openConnection();
            logger.info("Open Connection " + httpUrlConn);
            httpUrlConn.setRequestMethod("HEAD");
            logger.info("Set Request ");
            // Set timeouts in milliseconds
            httpUrlConn.setConnectTimeout(30000);
            httpUrlConn.setReadTimeout(30000);

            int erg = httpUrlConn.getResponseCode();
            logger.info("ResponseCode " + erg);
            logger.info(httpUrlConn.getResponseMessage());
            obj.put("success", true);
            obj.put("result", erg == HttpURLConnection.HTTP_OK);

        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * extrahiert eine PDF Datei und trägt den Inhalt in den internen Speicher ein.
     * @param pdfContent        der Inhalt der Datei als Base64 encodeter String
     * @param fileName          der Name der PDF Datei
     * @return                  ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der PDF Datei als String
     */
    public JSONObject extractPDFToInternalStorage(String pdfContent,
                                                  String fileName) {

        JSONObject obj = new JSONObject();
        try {
            byte[] bytes = Base64.decodeBase64(pdfContent);
            InputStream bais = new ByteArrayInputStream(bytes);
            PDFConnector con = new PDFConnector();
            if (entries != null) {
                entries.add(new FileEntry(fileName, bytes, con.pdftoText(bais)));
            }
            obj.put("success", true);
            obj.put("result", 1);
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * extrahiert eine PDF Datei.
     * @param filePath          der Pfad zur PDF-Datei
     * @return                  ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der PDF Datei als String
     */
    public JSONObject extractPDFFile(String filePath) {

        JSONObject obj = new JSONObject();
        try {
            byte[] bytes = readFile(filePath);
            PDFConnector con = new PDFConnector();
            obj.put("success", true);
            obj.put("result", con.pdftoText(new ByteArrayInputStream(bytes)));
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * extrahiert den Inhalt einer PDF Datei.
     * @param pdfContent        der Inhalt der Datei als Base64 encodeter String
     * @return                  ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der PDF Datei als String
     */
    public JSONObject extractPDFContent(String pdfContent) {

        JSONObject obj = new JSONObject();
        try {
            byte[] bytes = Base64.decodeBase64(pdfContent);
            PDFConnector con = new PDFConnector();
            obj.put("success", true);
            obj.put("result", con.pdftoText(new ByteArrayInputStream(bytes)));
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * extrahiert ein ZIP File und gibt den Inhalt als Base64 encodete Strings zurück
     * @param zipContent        der Inhalt des ZIP Files
     * @return                  ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der ZIP Datei als JSON Array mit Base64 encodeten STrings
     */
    protected JSONObject extractZIP(String zipContent) {

        JSONObject obj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        ZipInputStream zipin = null;
        try {

            final byte[] bytes = Base64.decodeBase64(zipContent);
            InputStream bais = new ByteArrayInputStream(bytes);
            zipin = new ZipInputStream(bais);
            int size;
            while ((zipin.getNextEntry()) != null) {
                byte[] buffer = new byte[2048];
                ByteArrayOutputStream bys = new ByteArrayOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(bys, buffer.length);
                while ((size = zipin.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }
                bos.flush();
                bos.close();
                jsonArray.put(Base64.encodeBase64String(bys.toByteArray()));
                bys.toByteArray();
            }
            if (jsonArray.length() == 0) {
                obj.put("success", false);
                obj.put("result", "Keine Files im ZIP File gefunden!");
            } else {
                obj.put("success", true);
                obj.put("result", jsonArray);
            }
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        } finally {
            try {
                if (zipin != null)
                    zipin.close();
            } catch (IOException e) {
                obj = VerteilungHelper.convertErrorToJSON(e);
            }
        }
        return obj;
    }

    /**
     * entpackt ein ZIP File in den internen Speicher
     * @param zipContent         der Inhalt des ZIP's als Base64 dekodierter String String
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg die Anzahl der Dokumente im ZIP File, ansonsten der Fehler
     */
    protected JSONObject extractZIPToInternalStorage(String zipContent) {
        JSONObject obj = new JSONObject();
        ZipInputStream zipin = null;
        try {

            final byte[] bytes = Base64.decodeBase64(zipContent);
            InputStream bais = new ByteArrayInputStream(bytes);
            zipin = new ZipInputStream(bais);
            ZipEntry entry;
            int size;
            int counter = 0;
            entries.clear();

            while ((entry = zipin.getNextEntry()) != null) {
                byte[] buffer = new byte[2048];
                ByteArrayOutputStream bys = new ByteArrayOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(bys, buffer.length);
                while ((size = zipin.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }
                bos.flush();
                bos.close();
                entries.add(new FileEntry(entry.getName(), bys.toByteArray()));
                counter++;
            }
            if (counter == 0) {
                obj.put("success", false);
                obj.put("result", "Keine Files im ZIP File gefunden!");
            } else {
                obj.put("success", true);
                obj.put("result", counter);
            }
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        } finally {
            try {
                if (zipin != null)
                    zipin.close();
            } catch (IOException e) {
                obj = VerteilungHelper.convertErrorToJSON(e);
            }
        }
        return obj;
    }

    /**
     * entpackt ein ZIP File und stellt die Inhalte und die extrahierten PDF Inhalte in den internen Speicher
     * @param zipContent          der Inhalt der ZIP Datei als Base64 encodeter String
     * @return obj                ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                    false    ein Fehler ist aufgetreten
     *                                                           result            bei Erfolg die Anzahl der Dokumente im ZIP File, ansonsten der Fehler
     */
    public JSONObject extractZIPAndExtractPDFToInternalStorage(String zipContent) {

        JSONObject obj;
        String extractedData;
        int counter = 0;
        try {
            obj = extractZIPToInternalStorage(zipContent);
            if (obj.getBoolean("success")) {
                PDFConnector con = new PDFConnector();
                for (FileEntry entry : entries) {

                    if (entry.getName().toLowerCase().endsWith(".pdf")) {
                        InputStream bais = new ByteArrayInputStream(entry.getData());
                        extractedData = con.pdftoText(bais);
                    }
                    else
                        extractedData = new String(entry.getData());
                    entry.setExtractedData(extractedData);
                    counter++;
                }
                obj.put("result", counter);
            }
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * liefert den Inhalt aus dem internen Speicher
     * @param fileName           der Name der zu suchenden Datei
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg ein JSONObjekt mit den Binärdaten (als Base64 String encoded) und er Inhalt als Text, ansonsten der Fehler
     */
    public JSONObject getDataFromInternalStorage(String fileName) {

        JSONObject obj = new JSONObject();
        JSONObject result = new JSONObject();
        boolean found = false;
        try {
            if (entries.isEmpty()) {
                obj.put("success", false);
                obj.put("result", "keine Einträge vorhanden");
            } else {
                for (FileEntry entry : entries) {
                    if (entry.getName().equals(fileName)) {
                        obj.put("success", true);
                        JSONObject jEntry = new JSONObject();
                        jEntry.put("name", entry.getName());
                        if (entry.getData().length > 0) {
                            jEntry.put("data", Base64.encodeBase64String(entry.getData()));
                            if (entry.getExtractedData() != null && !entry.getExtractedData().isEmpty())
                                jEntry.put("extractedData", entry.getExtractedData());
                            result.put(entry.getName(), jEntry);
                            obj.put("result", result);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    obj.put("success", false);
                    obj.put("result", "keine Einträge vorhanden");
                }
            }

        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * liefert den kompletten Inhalt aus dem internen Speicher
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg ein JSONObjekt mit den Binärdaten (als Base64 String encoded) und er Inhalt als Text, ansonsten der Fehler
     */

    public JSONObject getDataFromInternalStorage() {

        JSONObject obj = new JSONObject();
        JSONObject results = new JSONObject();
        try {
            if (entries.isEmpty()) {
                obj.put("success", false);
                obj.put("result", "keine Einträge vorhanden");
            } else {
                for (FileEntry entry: entries) {
                    JSONObject jEntry = new JSONObject();
                    jEntry.put("name", entry.getName());
                    if (entry.getData().length > 0) {
                        jEntry.put("data", Base64.encodeBase64String(entry.getData()));
                        if (entry.getExtractedData() != null && !entry.getExtractedData().isEmpty())
                            jEntry.put("extractedData", entry.getExtractedData());
                        results.put(entry.getName(), jEntry);
                    }
                }
                obj.put("success", true);
                obj.put("result", results);
            }
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * löscht den internen Speicher
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg nichts, ansonsten der Fehler
     */
    public JSONObject clearInternalStorage() {

        JSONObject obj = new JSONObject();
        try {
            entries.clear();
            obj.put("success", true);
            obj.put("result", "");
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * öfnnet eine Datei und liest den Inhalt
     * @param filePath          der Pfad zur Datei
     * @return                  ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der Datei als Base64 encodeter String oder der Fehler
     */
    public JSONObject openFile(String filePath) {

        JSONObject obj = new JSONObject();
        try {
            byte[] buffer = readFile(filePath);
            obj.put("success", true);
            obj.put("result", Base64.encodeBase64String(buffer));
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * liest eine Datei
     * @param filePath                  der Pfad zur Datei
     * @return                          der Inhalt als Byte Array
     * @throws URISyntaxException
     * @throws IOException
     */

    private byte[] readFile(String filePath) throws URISyntaxException, IOException {

        File sourceFile = new File(new URI(filePath.replace("\\", "/")));
        InputStream inp = new FileInputStream(sourceFile);
        byte[] buffer = new byte[(int) sourceFile.length()];
        //noinspection ResultOfMethodCallIgnored
        inp.read(buffer);
        inp.close();
        return buffer;
    }

    /**
     * nur für Testzwecke
     * @return    die FileEntries
     */
    public Collection<FileEntry> getEntries() {
        return entries;
    }

    /**
     * bereitet die Properties auf
     * @param extraCMSProperties    der String mit den Properties im JSON Format
     * @throws JSONException
     */
    private Map<String, Object> buildProperties(String extraCMSProperties) throws JSONException {

        Map<String, Object> outMap = null;
        JSONObject props = new JSONObject(extraCMSProperties);
        Iterator nameItr = props.keys();
        outMap = new HashMap<>();
        while (nameItr.hasNext()) {
            String name = (String) nameItr.next();
            Map<String, Object> inMap = new HashMap<>();
            Iterator innerItr = ((JSONObject) props.get(name)).keys();
            while (innerItr.hasNext()) {
                String innerName = (String) innerItr.next();
                inMap.put(innerName, ((JSONObject) props.get(name)).get(innerName));
            }
            outMap.put(name, inMap);
        }
        return outMap;
    }

    /**
     * baute den Versionstate aus dem String auf
     * @param  versionState  der VersionsStatus ( none, major, minor, checkedout) als String
     * @return               das VersionsState Object
     */
    private VersioningState createVersionState(String versionState) throws VerteilungException {

        if (versionState != null && versionState.length() > 0 && !versionState.equals("none") && !versionState.equals("major") && !versionState.equals("minor") && !versionState.equals("checkedout"))
            throw new VerteilungException("ungültiger VersionsStatus");
        VersioningState vs = VersioningState.fromValue(versionState);
        if (vs == null)
            vs = VersioningState.NONE;
        return vs;
    }
}