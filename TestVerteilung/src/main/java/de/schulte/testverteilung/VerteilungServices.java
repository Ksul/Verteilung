package de.schulte.testverteilung;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.apache.chemistry.opencmis.client.api.*;
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
     * @param server         der Name des Alfresco Servers
     * @param username       der verwendete Username
     * @param password       das Passwort
     */
    public VerteilungServices(String server,
                              String username,
                              String password)  {

        super();
        this.con = new AlfrescoConnector(username, password, server);
    }

    /**
     * über diese Methode können die Alfresco Parameter nachträglich gesetzt werden.
     * @param server         der Name des Alfresco Servers
     * @param username       der verwendete Username
     * @param password       das Passwort
     */
    public void setParameter(String server,
                        String username,
                        String password) {
        this.con = new AlfrescoConnector(username, password, server);
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
    public JSONObject listFolderAsJSON(String filePath,
                                       int listFolder) {

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

                for (CmisObject cmisObject : con.listFolder(filePath)) {

                    // prüfen, ob das gefundene Objekt überhaupt ausgegeben werden soll
                    if ((cmisObject instanceof Folder && listFolder < 1) || (cmisObject instanceof Document && listFolder > -1)) {
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
        for (Property prop : doc.getProperties()){
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
    public JSONObject getDocumentContent(String documentId,
                                         boolean extract) {

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
    public JSONObject uploadDocument(String folderPath,
                                     String fileName) {

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
    public JSONObject deleteDocument(String folderName,
                                     String documentName) {

        JSONObject obj = new JSONObject();
        try {
            CmisObject document;
            CmisObject folder;
            folder = con.getNode(folderName);
            if (folder != null && folder instanceof Folder) {
                document = con.getNode(((Folder) folder).getPath() + "/" + documentName);
                if (document != null && document instanceof Document) {
                    document.delete(true);
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
     * @param  versionState         der versionsStatus ( none, major, minor, checkedout)
     * @return obj                  ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                      false   ein Fehler ist aufgetreten
     *                                                             result           das Document als JSON Object
     */
    public JSONObject createDocument(String folderName, String documentName,
                                     String documentContent,
                                     String documentType,
                                     String extraCMSProperties,
                                     String versionState) {

        //TODO Content als String oder als Stream?
        JSONObject obj = new JSONObject();
        try {
            CmisObject document;
            CmisObject folder;
            if (versionState != null && versionState.length() > 0 && !versionState.equals("none") && !versionState.equals("major") && !versionState.equals("minor") && !versionState.equals("checkedout"))
                throw new VerteilungException("ungültiger VersionsStatus");
            folder = con.getNode(folderName);
            if (folder != null && folder instanceof Folder) {

                Map<String, Object> outMap = null;
                if (extraCMSProperties != null && extraCMSProperties.length() > 0) {
                    JSONObject props = new JSONObject(extraCMSProperties);
                    Iterator nameItr = props.keys();
                    outMap = new HashMap<>();
                    while (nameItr.hasNext()) {
                        String name = (String) nameItr.next();
                        outMap.put(name, props.get(name));
                    }
                }

                VersioningState vs = VersioningState.fromValue(versionState);
                document = con.createDocument((Folder) folder, documentName, documentContent.getBytes(), documentType, outMap, vs);
                if (document != null) {
                    obj.put("success", true);
                    obj.put("result", convertCMISObjectToJSON(document).toString());
                } else {
                    obj.put("success", false);
                    obj.put("result", "Ein Document mit dem Namen " + documentName + " ist nicht vorhanden!");
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
     * aktualisiert den Inhalt eines Dokumentes
     * @param  documentId                Die Id des zu aktualisierenden Dokumentes
     * @param  documentContent           der neue Inhalt
     * @param  documentType              der Typ des Dokumentes
     * @param  extraCMSProperties        zusätzliche Properties
     * @param  majorVersion              falls Dokument versionierbar, dann wird eine neue Major-Version erzeugt, falls true
     * @param  versionComment            falls Dokuemnt versionierbar, dann kann hier eine Kommentar zur Version mitgegeben werden
     * @return obj                       ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                           false   ein Fehler ist aufgetreten
     *                                                                  result           bei Erfolg das Document als JSON Object, ansonsten der Fehler
     */
    public JSONObject updateDocument(String documentId,
                                     String documentContent,
                                     String documentType,
                                     String extraCMSProperties,
                                     String majorVersion,
                                     String versionComment) {

        //TODO Content als String oder als Stream?
        JSONObject obj = new JSONObject();
        try {

            if (majorVersion == null)
                majorVersion = "false";

            CmisObject cmisObject = con.getNodeById(documentId);
            if (cmisObject != null && cmisObject instanceof Document) {

                Map<String, Object> outMap = null;
                if (extraCMSProperties != null && extraCMSProperties.length() > 0) {
                    JSONObject props = new JSONObject(extraCMSProperties);
                    Iterator nameItr = props.keys();
                    outMap = new HashMap<>();
                    while (nameItr.hasNext()) {
                        String name = (String) nameItr.next();
                        outMap.put(name, props.get(name));
                    }
                }
                Document document = con.updateDocument((Document) cmisObject, documentContent.getBytes(), documentType, outMap, majorVersion.equalsIgnoreCase("true"), versionComment);
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
     * @param  targetFolder           der Name des Folders in dem das Dokument erstellt werden soll als String
     * @param  folderName             der Name des Folders als String
     * @return obj                     ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                         false   ein Fehler ist aufgetreten
     *                                                                result           der Folder als JSON Object
     */
    public JSONObject createFolder(String targetFolder,
                                   String folderName) {

        JSONObject obj = new JSONObject();
        try {
            Folder folder;
            CmisObject target;
            target = con.getNode(targetFolder);
            if (target != null && target instanceof Folder) {
                folder = con.createFolder((Folder) target, folderName);
                if (folder != null ) {
                    obj.put("success", true);
                    obj.put("result", convertCMISObjectToJSON(folder).toString());
                } else {
                    obj.put("success", false);
                    obj.put("result", "Ein Folder mit dem Namen " + folderName + " ist nicht vorhanden!" );
                }
            } else {
                obj.put("success", false);
                obj.put("result", target == null ? "Der Pfad " + targetFolder + "  ist nicht vorhanden!" : "Der verwendete Pfad " + targetFolder + " ist kein Folder!");
            }
        } catch (Throwable t) {
            obj = VerteilungHelper.convertErrorToJSON(t);
        }
        return obj;
    }

    /**
     * löscht einen Folder
     * @param  folderName        der Name des Folders
     * @return obj               ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                   false   ein Fehler ist aufgetreten
     *                                                          result           bei Erfolg nichts, ansonsten der Fehler
     */
    public JSONObject deleteFolder(String folderName) {

        JSONObject obj = new JSONObject();
        try {
            CmisObject folder;
            folder = con.getNode(folderName);
            if (folder != null && folder instanceof Folder) {
                List<String> list = ((Folder) folder).deleteTree(true, UnfileObject.DELETE, true);
                    obj.put("success", true);
                    obj.put("result", new JSONObject(list).toString());
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
            ZipEntry entry = null;
            int size = 0;
            while ((entry = zipin.getNextEntry()) != null) {
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
     * @return obj                ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg die Anzahl der Dokumente im ZIP File, ansonsten der Fehler
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

        File sourceFile = new File(new URI(filePath));
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

}