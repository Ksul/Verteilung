package de.schulte.testverteilung;

/*
 * VerteilungApplet.java
 *
 * Created on January 24, 2009, 11:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Klaus Schulte
 */

import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import netscape.javascript.JSObject;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.Response.ResponseType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VerteilungApplet extends Applet {

	private static final long serialVersionUID = 1L;

	JSObject jsobject;

    ByteArrayOutputStream bys = new ByteArrayOutputStream();

	BufferedOutputStream bos = new BufferedOutputStream(bys);

    // Speicher für entpackte Files aus einem ZIP File
	Collection<FileEntry> entries = new ArrayList<FileEntry>();

    private static VerteilungServices services;

    private static String bindingUrl;

    private static String user;

    private static String password;

    private static Logger logger = Logger.getLogger(VerteilungApplet.class.getName());

    private static String level = "WARNING";

    /**
     * Initialisierung
     */
	public void init() {
		try {
			logger.info("Hier ist das Verteilungsapplet");
            level = getParameter ("debug");
            this.bindingUrl = getParameter("url");
            this.user = getParameter("user");
            this.password = getParameter("password");
            Logger log = LogManager.getLogManager().getLogger("");
            for (Handler h : log.getHandlers()) {
                h.setLevel(Level.parse(level));
            }
			jsobject = JSObject.getWindow(this);
		} catch (Exception jse) {
			logger.severe(jse.getMessage());
			jse.printStackTrace();
		}
	}

    /**
     * setzt die Parameter
     * @param  url          die Binding Url
     * @param  userName     der Username
     * @param  pass         das Password
     * @return obj          ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                              false    ein Fehler ist aufgetreten
     *                                                     result            die Parameter als String
     */
    public String setParameter(String url, String userName, String pass) {
        logger.fine("Aufruf setParameter mit Url: " + url + " User: " + userName + " Password: " + password);
        this.bindingUrl = url;
        this.user = userName;
        this.password = pass;
        JSONObject obj = new JSONObject();
        try {
            obj.put("success", true);
            obj.put("result", bindingUrl + " " + user + " " + password);
        } catch (JSONException jse) {
            logger.severe(jse.getMessage());
            jse.printStackTrace();
        }
        return obj.toString();
    }

    /**
     * liefert die Alfresco Services
     * @param url               Binding URL des Servers
     * @param user              User Name
     * @param password          Passwort
     * @return
     */
    public VerteilungServices getServices(String url, String user, String password) {
        logger.fine("Aufruf getServices mit Url: " + url + " User: " + user + " Password: " + password);
        if (services == null)
            services = new VerteilungServices(url, user, password);
        return services;
    }


    /**
     * prüft, ob eine Url verfügbar ist
     * @param  urlString   URL des Servers
     * @return obj            ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                false    ein Fehler ist aufgetreten
     *                                                       result            true, wenn die URL verfügbar ist
     */
    public String isURLAvailable(final String urlString) {
        logger.fine("Aufruf isURLAvailabel mit urlString: " + urlString);
        JSONObject obj = new JSONObject();

        try {

            obj = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {

                public JSONObject run() {

                    JSONObject obj = new JSONObject();
                    URL url = null;
                    try {
                        url = new URL(urlString);
                        logger.info("Umwandlung in URL " + url);
                    } catch (MalformedURLException e) {
                        String error = "Fehler beim Check der URL: " + e.getMessage();
                        logger.severe(error);
                        e.printStackTrace();
                        try {
                            obj.put("success", false);
                            obj.put("result", error);
                        } catch (JSONException jse) {
                            logger.severe(jse.getMessage());
                            jse.printStackTrace();
                        }
                    }
                    try {
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
                        String error = "Fehler beim Check der URL: " + t.getMessage();
                        logger.severe(error);
                        t.printStackTrace();
                        try {
                            obj.put("success", false);
                            obj.put("result", error);
                        } catch (JSONException jse) {
                            logger.severe(jse.getMessage());
                            jse.printStackTrace();
                        }
                    }
                    return obj;
                }
            });
        } catch (Exception e) {
            String error = "Fehler beim Check der URL: " + e.getMessage();
            logger.severe(error);
            e.printStackTrace();
            try {
                obj.put("success", false);
                obj.put("result", error);
            } catch (JSONException jse) {
                logger.severe(jse.getMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }

    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     * @param filePath     der Pfad, der geliefert werden soll
     * @param listFolder   was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @return obj         ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            der Inhalt des Verzeichnisses als JSON Objekte
     */
    public String listFolderAsJSON(final String filePath, final String listFolder)  {
        logger.fine("Aufruf listFolderAsJSON mit filePath: " + filePath + " listFolder: " + listFolder);
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws VerteilungException, IOException, JSONException {
                    VerteilungServices services = new VerteilungServices(bindingUrl, user, password);
                    return services.listFolderAsJSON(filePath, Integer.parseInt(listFolder));
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }


    /**
     * liefert eine NodeID als String zurück
     * @param path         der Pfad zum Knoten, der der Knoten gesucht werden soll
     * @return obj         ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            die NodeId als String
     */
    public String getNodeId(final String path) {
        logger.fine("Aufruf getNodeId mit path: " + path);
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    VerteilungServices services = getServices(bindingUrl, user, password);
                    return services.getNodeId(path);
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }


    /**
     * findet ein Document
     * @param cmisQuery    die CMIS Query, mit der der Knoten gesucht werden soll
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            Dokument als JSONObject
     */
    public String findDocument(final String cmisQuery) {
        logger.fine("Aufruf findDocument mit cmisQuery: " + cmisQuery);
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    VerteilungServices services = getServices(bindingUrl, user, password);
                    return services.findDocument(cmisQuery);
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }

    /**
     * liefert den Inhalt eines Dokumentes. Wenn es sich um eine PDF Dokument handelt, dann wird
     * der Text extrahiert.
     * @param docId                 die Id des Documentes
     * @param extract               legt fest,ob einPDF Document umgewandelt werden soll
     * @return                      ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result            der Inhalt als String
     */
    public String getDocumentContent(final String docId, final boolean extract) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    VerteilungServices services = getServices(bindingUrl, user, password);
                    return services.getDocumentContent(docId, extract);
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }

    /**
     * lädt ein Document in den Server
     * @param filePath       der Folder als String, in das Document geladen werden soll
     * @param fileName       der Dateiname ( mit Pfad) als String, die hochgeladen werden soll
     * @return               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                               false    ein Fehler ist aufgetreten
     *                                                      result            Dokument als JSONObject
     */
    public String uploadDocument(final String filePath, final String fileName) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    VerteilungServices services = getServices(bindingUrl, user, password);
                    return services.uploadDocument(filePath, fileName);
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();

	}

    /**
     * löscht ein Document
     * @param filePath       der Folder als String, in das Documentliegt
     * @param fileName       der Name des Documentes
     * @return               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                               false    ein Fehler ist aufgetreten
     *                                                      result            Dokument als JSONObject
     */
    public String deleteDocument(final String filePath, final String fileName) {


        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    VerteilungServices services = getServices(bindingUrl, user, password);
                    return services.deleteDocument(filePath, fileName);
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();

    }

    /**
     * erzeugt ein Document
     * @param  filePath             der Name des Folders in dem das Dokument erstellt werden soll als String
     * @param  fileName             der Name des Dokumentes als String
     * @param  documentContent      der Inhalt als String
     * @param  documentType         der Typ des Dokumentes
     * @param  extraCMSProperties   zusätzliche Properties
     * @param  versionState         der versionsStatus ( none, major, minor, checkedout)
     * @return                      ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                                      result            Dokument als JSONObject
     */
    public String createDocument(final String filePath,
                                        final String fileName,
                                        final String documentContent,
                                        final String documentType,
                                        final String extraCMSProperties,
                                        final String versionState)  {


        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    VerteilungServices services = getServices(bindingUrl, user, password);
                    return services.createDocument(filePath, fileName, documentContent, documentType, extraCMSProperties, versionState);
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();

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
     *                                                                  result           bei Erfolg nichts, ansonsten der Fehler
     */
    public String updateDocument(final String documentId,
                                 final String documentContent,
                                 final String documentType,
                                 final String extraCMSProperties,
                                 final String majorVersion,
                                 final String versionComment)  {
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {

                    VerteilungServices services = getServices(bindingUrl, user, password);
                    return services.updateDocument(documentId, documentContent, documentType, extraCMSProperties, majorVersion, versionComment);
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();

    }

    /**
     * verschiebt ein Dokument
     * @param  documentId                das zu verschiebende Dokument
     * @param  oldFolderId               der alte Folder in dem das Dokument liegt
     * @param  newFolderId               der Folder, in das Dokument verschoben werden soll
     * @return obj                       ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                           false   ein Fehler ist aufgetreten
     *                                                                  result           bei Erfolg nichts, ansonsten der Fehler
     */
    public String moveDocument(final String documentId,
                               final String oldFolderId,
                               final String newFolderId) {
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    VerteilungServices services = getServices(bindingUrl, user, password);
                    return services.moveDocument(documentId, oldFolderId, newFolderId);
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }


    /**
     * erzeugt einen Pfad
     * @param  targetPath           der Name des Folders in dem der Folder erstellt werden soll als String
     * @param  folderName           der Name des neuen Pfades als String
     * @return                      ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result            Folder als JSONObject
     */
    public String createFolder(final String targetPath,
                               final String folderName) {
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    VerteilungServices services = getServices(bindingUrl, user, password);
                    return services.createFolder(targetPath, folderName);
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }

    /**
     * löscht einen Pfad
     * @param  folderPath           der Name des zu löschenden Pfades als String
     * @return                      ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result
     */
    public String deleteFolder(final String folderPath) {
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    VerteilungServices services = getServices(bindingUrl, user, password);
                    return services.deleteFolder(folderPath);
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }


    /**
     * liest die Testproperties
     * nur für Testzwecke
     * @param  propFile      der Name der Properties Datei
     * @return obj           ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                               false    ein Fehler ist aufgetreten
     *                                                      result            die Properties als JSON Objekte
     */
    public String loadProperties(final String propFile) {
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    VerteilungServices services = new VerteilungServices();
                    return services.loadProperties(propFile);
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }


    /**
     * sichert einen String in eine Datei
     * @param fileName           der Name der Datei
     * @param string             der zu sichernde String
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg nichts, ansonsten der Fehler
     */
    public String save(final String fileName,
                       final String string) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws URISyntaxException, IOException, JSONException {
                    JSONObject obj = new JSONObject();
                    File file = new File(new URI(fileName));
                    StringReader stringReader = new StringReader(string);
                    BufferedReader bufferedReader = new BufferedReader(stringReader);
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
                    for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                    }
                    bufferedReader.close();
                    bufferedWriter.close();
                    obj.put("success", true);
                    obj.put("result", "");
                    return obj;
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }

    /**
     * öffnet ein PDF
     * @param fileName           der Filename des PDF Dokumentes
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg nichts, ansonsten der Fehler
     */
    public String openPDF(final String fileName) {
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws IOException, JSONException {
                    JSONObject obj = new JSONObject();
                    boolean found = false;
                    FileEntry entry = null;
                    Iterator<FileEntry> it = entries.iterator();
                    while (it.hasNext()) {
                        entry = it.next();
                        if (entry.getName().equalsIgnoreCase(fileName)) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        byte[] ret = entry.getData();
                        String name = entry.getName();

                        String property = "java.io.tmpdir";

                        // Get the temporary directory and print it.
                        String tempDir = System.getProperty(property);
                        File file = new File(tempDir + "/" + name);
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(ret);
                        fos.flush();
                        fos.close();
                        getAppletContext().showDocument(file.toURI().toURL(), "_blank");
                        obj.put("success", true);
                        obj.put("result", "");
                    } else {
                        obj.put("success", false);
                        obj.put("result", "PDF konnte nicht gefunden werden!");
                    }
                    return obj;
                }

            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }


    /**
     * entpackt ein ZIP File
     * @param name          der Name des ZIP Files
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg die Anzahl der Dokumente im ZIP File, ansonsten der Fehler
     */
    public String extractZIP(final String name) {
        JSONObject obj = new JSONObject();

        try {

            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws IOException, JSONException {
                    JSONObject obj = new JSONObject();
                    byte[] ret = bys.toByteArray();
                    ZipInputStream zipin = null;
                    int counter = 0;
                    boolean multi = false;
                    InputStream bais = new ByteArrayInputStream(ret);
                    zipin = new ZipInputStream(bais);
                    ZipEntry entry;
                    int size;
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
                    }
                    Iterator<FileEntry> it = entries.iterator();
                    while (it.hasNext()) {
                        FileEntry ent = it.next();
                        counter++;
                        if (it.hasNext())
                            multi = true;
                        String entryFileName = ent.getName();
                        InputStream b = new ByteArrayInputStream(ent.getData());
                        PDFConnector con = new PDFConnector();
                        String result = con.pdftoText(b);
                        if (multi)
                            jsobject.call("loadMultiText", new String[]{result, entryFileName, "application/zip", "true",
                                    "false", name});
                        else
                            jsobject.call("loadText", new String[]{result, entryFileName, "application/zip", name});
                    }
                    obj.put("success", true);
                    obj.put("result", counter);
                    return obj;
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }

    /**
     * extrahiert den Text aus einer PDF Datei und lädt den Text in der Anwendung
     * @param fileName          der Dateiname
     * @param multi
     * @param typ
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg nichts, ansonsten der Fehler
     */
    public String extract(final String fileName,
                          final boolean multi,
                          final String typ) {

        JSONObject obj = new JSONObject();

        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    JSONObject obj = new JSONObject();
                    entries.clear();
                    final byte[] ret = bys.toByteArray();
                    entries.add(new FileEntry(fileName, ret));
                    InputStream bais = new ByteArrayInputStream(ret);
                    PDFConnector con = new PDFConnector();
                    String result = con.pdftoText(bais);
                    if (!multi)
                        jsobject.call("loadText", new String[]{result, fileName, typ, null});
                    else
                        jsobject.call("loadMultiText", new String[]{result, fileName, typ, "false", "false", null});
                    obj.put("success", true);
                    obj.put("result", "");
                    return obj;
                }
            });

        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }

    /**
     * TODO Was macht diese Methode
     * @param str           der übegebene String
     * @param init
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg nichts, ansonsten der Fehler
     */
    public String getData(final String str,
                          final boolean init) {
        JSONObject obj = new JSONObject();
        try {
            final byte[] ret = Base64.decodeBase64(str);
            if (init)
                bys.reset();
            bos.write(ret, 0, ret.length);
            bos.flush();
            obj.put("success", true);
            obj.put("result", "");
        } catch (Exception e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }

    /**
     * öffnet eine Datei
     * @param filePath          der Pfad der zu öffnenden Datei
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg der Inhalt als String, ansonsten der Fehler
     */
    public String openFile(final String filePath) {
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws IOException, JSONException, URISyntaxException {
                    JSONObject obj = new JSONObject();
                    String r;
                    StringBuilder fileData = new StringBuilder(1000);

                    InputStream inp = new FileInputStream(new File(new URI(filePath)));
                    InputStreamReader isr = new InputStreamReader(inp, "UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                    char[] buf = new char[1024];
                    int numRead;
                    while ((numRead = reader.read(buf)) != -1) {
                        String readData = String.valueOf(buf, 0, numRead);
                        fileData.append(readData);
                        buf = new char[1024];
                    }
                    reader.close();
                    obj.put("success", true);
                    obj.put("result", fileData.toString());
                    return obj;
                }
            });
        } catch (PrivilegedActionException e) {
            try {
                obj.put("success", false);
                obj.put("result", e.getMessage());
            } catch (JSONException jse) {
                logger.severe(jse.getLocalizedMessage());
                jse.printStackTrace();
            }
        }
        return obj.toString();
    }


    /**
     * Testmethode
     * @return
     */
    public String test() {
		return "Hier ist das Verteilungs Applet!";
	}

}