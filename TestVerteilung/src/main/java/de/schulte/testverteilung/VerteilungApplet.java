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
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import netscape.javascript.JSObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VerteilungApplet extends Applet {

	private static final long serialVersionUID = 1L;

	private JSObject jsobject;

    private ByteArrayOutputStream bys = new ByteArrayOutputStream();

	private BufferedOutputStream bos = new BufferedOutputStream(bys);

    private VerteilungServices services = new VerteilungServices();

    private String bindingUrl;

    private String user;

    private String password;

    private  Logger logger = Logger.getLogger(VerteilungApplet.class.getName());

    private String level = "WARNING";

    /**
     * liefert die Services
     * nur für Testzwecke
     * @return
     */
    public VerteilungServices getServices() {
        return services;
    }

    /**
     * Initialisierung
     */
	public void init() {
		try {
			logger.info("Hier ist das Verteilungsapplet");
            level = getParameter ("debug");
            Logger log = LogManager.getLogManager().getLogger("");
            for (Handler h : log.getHandlers()) {
                h.setLevel(Level.parse(level));
            }
            setParameter(getParameter("url"), getParameter("user"), getParameter("password"));
			jsobject = JSObject.getWindow(this);
		} catch (Exception jse) {
			logger.severe(jse.getMessage());
			jse.printStackTrace();
		}
	}

    /**
     * setzt die Parameter
     * @param  bindingUrl   die Binding Url
     * @param  user         der Username
     * @param  password     das Password
     * @return obj          ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                              false    ein Fehler ist aufgetreten
     *                                                     result            die Parameter als String
     */
    public JSONObject setParameter(String bindingUrl,
                                   String user,
                                   String password) {

        logger.fine("Aufruf setParameter mit Url: " + bindingUrl + " User: " + user + " Password: " + password);
        this.bindingUrl = bindingUrl;
        this.user = user;
        this.password = password;
        JSONObject obj = new JSONObject();
        try {
            if (this.bindingUrl != null && !this.bindingUrl.isEmpty() && this.user != null && !this.user.isEmpty() && this.password != null && !this.password.isEmpty()) {
                services.setParameter(this.bindingUrl, this.user, this.password);
                obj.put("success", true);
                obj.put("result", "BindingUrl:" + this.bindingUrl + " User:" + this.user + " Password:" + this.password);
            } else {
                obj.put("success", false);
                obj.put("result", "Parameter fehlt: BindingUrl:" + this.bindingUrl + " User: " + this.user + " Password:" + this.password);
            }
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * prüft, ob eine Url verfügbar ist
     * @param  urlString   URL des Servers
     * @return obj            ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                false    ein Fehler ist aufgetreten
     *                                                       result            true, wenn die URL verfügbar ist
     */
    public JSONObject isURLAvailable(final String urlString) {

        logger.fine("Aufruf isURLAvailabel mit urlString: " + urlString);

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws VerteilungException, IOException, JSONException {
                    return services.isURLAvailable(urlString);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     * @param filePath     der Pfad, der geliefert werden soll
     * @param listFolder   was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @return obj         ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            der Inhalt des Verzeichnisses als JSON Objekte
     */
    public JSONObject listFolderAsJSON(final String filePath,
                                       final String listFolder)  {

        logger.fine("Aufruf listFolderAsJSON mit filePath: " + filePath + " listFolder: " + listFolder);
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws VerteilungException, IOException, JSONException {
                    return services.listFolderAsJSON(filePath, Integer.parseInt(listFolder));
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * liefert eine NodeID als String zurück
     * @param path         der Pfad zum Knoten, der der Knoten gesucht werden soll
     * @return obj         ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            die NodeId als String
     */
    public JSONObject getNodeId(final String path) {

        logger.fine("Aufruf getNodeId mit path: " + path);
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.getNodeId(path);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * findet ein Document
     * @param cmisQuery    die CMIS Query, mit der der Knoten gesucht werden soll
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            Dokument als JSONObject
     */
    public JSONObject findDocument(final String cmisQuery) {

        logger.fine("Aufruf findDocument mit cmisQuery: " + cmisQuery);
        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.findDocument(cmisQuery);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
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
    public JSONObject getDocumentContent(final String docId,
                                         final boolean extract) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.getDocumentContent(docId, extract);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * lädt ein Document in den Server
     * @param filePath       der Folder als String, in das Document geladen werden soll
     * @param fileName       der Dateiname ( mit Pfad) als String, die hochgeladen werden soll
     * @return               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                               false    ein Fehler ist aufgetreten
     *                                                      result            Dokument als JSONObject
     */
    public JSONObject uploadDocument(final String filePath,
                                     final String fileName) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.uploadDocument(filePath, fileName);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;

	}

    /**
     * löscht ein Document
     * @param filePath       der Folder als String, in das Documentliegt
     * @param fileName       der Name des Documentes
     * @return               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                               false    ein Fehler ist aufgetreten
     *                                                      result            Dokument als JSONObject
     */
    public JSONObject deleteDocument(final String filePath,
                                     final String fileName) {


        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.deleteDocument(filePath, fileName);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;

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
    public JSONObject createDocument(final String filePath,
                                     final String fileName,
                                     final String documentContent,
                                     final String documentType,
                                     final String extraCMSProperties,
                                     final String versionState)  {


        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.createDocument(filePath, fileName, documentContent, documentType, extraCMSProperties, versionState);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
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
     *                                                                  result           bei Erfolg nichts, ansonsten der Fehler
     */
    public JSONObject updateDocument(final String documentId,
                                     final String documentContent,
                                     final String documentType,
                                     final String extraCMSProperties,
                                     final String majorVersion,
                                     final String versionComment)  {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.updateDocument(documentId, documentContent, documentType, extraCMSProperties, majorVersion, versionComment);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
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
     *                                                                  result           bei Erfolg nichts, ansonsten der Fehler
     */
    public JSONObject moveDocument(final String documentId,
                                   final String oldFolderId,
                                   final String newFolderId) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.moveDocument(documentId, oldFolderId, newFolderId);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * erzeugt einen Pfad
     * @param  targetPath           der Name des Folders in dem der Folder erstellt werden soll als String
     * @param  folderName           der Name des neuen Pfades als String
     * @return                      ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result            Folder als JSONObject
     */
    public JSONObject createFolder(final String targetPath,
                                   final String folderName) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.createFolder(targetPath, folderName);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * löscht einen Pfad
     * @param  folderPath           der Name des zu löschenden Pfades als String
     * @return                      ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result
     */
    public JSONObject deleteFolder(final String folderPath) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.deleteFolder(folderPath);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * liest die Testproperties
     * nur für Testzwecke
     * @param  propFile      der Name der Properties Datei
     * @return obj           ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                               false    ein Fehler ist aufgetreten
     *                                                      result            die Properties als JSON Objekte
     */
    public JSONObject loadProperties(final String propFile) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.loadProperties(propFile);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * sichert einen String in eine lokale Datei
     * @param fileName           der Name der Datei
     * @param string             der zu sichernde String
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg nichts, ansonsten der Fehler
     */
    public JSONObject saveToFile(final String fileName,
                                 final String string) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws URISyntaxException, IOException, JSONException {
                    JSONObject obj = new JSONObject();
                    File file = new File(new URI(fileName));
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
                    bufferedWriter.write(string);
                    bufferedWriter.close();
                    obj.put("success", true);
                    obj.put("result", "");
                    return obj;
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * löscht eine lokale Datei
     * wir nur zum Testen verwendet
     * @param fileName           der Name der Datei
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg nichts, ansonsten der Fehler
     */
    public JSONObject deleteFile(final String fileName) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws URISyntaxException, IOException, JSONException {
                    JSONObject obj = new JSONObject();
                    File file = new File(new URI(fileName));
                    file.delete();
                    obj.put("success", true);
                    obj.put("result", "");
                    return obj;
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * extrahiert den Inhalt einer PDF Datei.
     * der Inhalt der ZIP Datei muss vorher mit @see fillParamter gefüllt worden sein.
     * @return                  ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der PDF Datei als String
     */
    public JSONObject extractPDFContent() {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException, VerteilungException {
                    byte[] bytes = bys.toByteArray();
                    if (bytes.length == 0)
                        throw new VerteilungException("kein Fileinhalt vorhanden! fillParameter ist nicht vorher benutzt worden.");
                    return services.extractPDFContent(new String(bytes));
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * extrahiert eine PDF Datei.
     * @param filePath          der Pfad zur PDF-Datei
     * @return                  ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der PDF Datei als String
     */
    public JSONObject extractPDFFile(final String filePath) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.extractPDFFile(filePath);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * extrahiert eine PDF Datei und trägt den Inhalt in den internen Speicher ein.
     * der Inhalt der ZIP Datei muss vorher mit @see fillParamter gefüllt worden sein.
     * @param fileName          der Name der PDF Datei
     * @return                  ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der PDF Datei als String
     */
    public JSONObject extractPDFToInternalStorage(final String fileName) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException, VerteilungException {
                    byte[] bytes = bys.toByteArray();
                    if (bytes.length == 0)
                        throw new VerteilungException("kein Fileinhalt vorhanden! fillParameter ist nicht vorher benutzt worden.");
                    return services.extractPDFToInternalStorage(new String(bytes), fileName);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * extrahiert ein ZIP File und gibt den Inhalt als Base64 encodete Strings zurück
     * der Inhalt der ZIP Datei muss vorher mit @see fillParamter gefüllt worden sein.
     * @return                  ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der ZIP Datei als JSON Aray mit Base64 encodeten STrings
     */
    public JSONObject extractZIP()  {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException, VerteilungException {
                    byte[] bytes = bys.toByteArray();
                    if (bytes.length == 0)
                        throw new VerteilungException("kein Fileinhalt vorhanden! fillParameter ist nicht vorher benutzt worden.");
                    return services.extractZIP(new String(bytes));
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * entpackt ein ZIP File und stellt die Inhalte und die extrahierten PDF Inhalte in den internen Speicher
     * der Inhalt der ZIP Datei muss vorher mit @see fillParamter gefüllt worden sein.
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg die Anzahl der Dokumente im ZIP File, ansonsten der Fehler
     */
    public JSONObject extractZIPAndExtractPDFToInternalStorage() {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException, VerteilungException {
                    byte[] bytes = bys.toByteArray();
                    if (bytes.length == 0)
                        throw new VerteilungException("kein Fileinhalt vorhanden! fillParameter ist nicht vorher benutzt worden.");
                    return services.extractZIPAndExtractPDFToInternalStorage(new String(bytes));
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * entpackt ein ZIP File in den internen Speicher
     * der Inhalt der ZIP Datei muss vorher mit @see fillParamter gefüllt worden sein.
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg die Anzahl der Dokumente im ZIP File, ansonsten der Fehler
     */
    public JSONObject extractZIPToInternalStorage()  {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException, VerteilungException {
                    byte[] bytes = bys.toByteArray();
                    if (bytes.length == 0)
                        throw new VerteilungException("kein Fileinhalt vorhanden! fillParameter ist nicht vorher benutzt worden.");
                    return services.extractZIPToInternalStorage(new String(bytes));
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * liefert den Inhalt aus dem internen Speicher
     * @param fileName           der Name der zu suchenden Datei
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg ein JSONObjekt mit den Binärdaten (Base64 encoded) und er Inhalt als Text, ansonsten der Fehler
     */
    public JSONObject getDataFromInternalStorage(final String fileName) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.getDataFromInternalStorage(fileName);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * liefert den kompletten Inhalt aus dem internen Speicher
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg ein JSONObjekt mit den Binärdaten (Base64 encoded) und er Inhalt als Text, ansonsten der Fehler
     */
    public JSONObject getDataFromInternalStorage() {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.getDataFromInternalStorage();
                }
            });
        } catch (PrivilegedActionException e) {
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
    public JSONObject clearInternalStorage()  {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws JSONException {
                    return services.clearInternalStorage();
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * öffnet ein PDF im Browser
     * @param fileName           der Filename des PDF Dokumentes
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg nichts, ansonsten der Fehler
     */
    public JSONObject openPDF(final String fileName) {

        JSONObject obj = new JSONObject();
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws IOException, JSONException {
                    JSONObject obj = new JSONObject();
                    boolean found = false;
                    FileEntry entry = null;
                    Iterator<FileEntry> it = services.getEntries().iterator();
                    while (it.hasNext()) {
                        entry = it.next();
                        if (entry.getName().equalsIgnoreCase(fileName)) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        byte[] ret = entry.getData();
                        File file = File.createTempFile("Tmp", ".pdf");
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
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * bei der Übertragung von Parametern in das Applet sind lt. HTML Spezifikation Längenbegrenzungen gesetzt.
     * Damit auch große Datenmengen an das Applet übertragen werden können, gibt es dieses Methode, die einen langen String,
     * z.B. den Inhalt einer Datei, in Häppchen unterteilt und dann lokal im Applet zwischen speichert.
     * @param str                der übergebene String
     * @param init               legt fest, ob der Zwischenspeicher im Applet initialisiert werden soll
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg nichts, ansonsten der Fehler
     */
    public JSONObject fillParameter(final String str,
                                    final boolean init) {

        JSONObject obj = new JSONObject();
        try {
            final byte[] ret = str.getBytes();
            if (init)
                bys.reset();
            bos.write(ret, 0, ret.length);
            bos.flush();
            obj.put("success", true);
            obj.put("result", "");
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * öffnet eine Datei
     * @param filePath          der Pfad der zu öffnenden Datei
     * @return obj               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg der Inhalt als Base64 encodeter String, ansonsten der Fehler
     */
    public JSONObject openFile(final String filePath) {

        JSONObject obj;
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws IOException, JSONException, URISyntaxException {
                    return services.openFile(filePath);
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }

    /**
     * Testmethode
     * @return
     */
    public JSONObject testReturn() {

        JSONObject obj;
        try {
            obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws IOException, JSONException, URISyntaxException {
                    JSONObject obj = new JSONObject();
                    obj.put("success", true);
                    obj.put("result", "dies ist das Testergebnis");
                    return obj;
                }
            });
        } catch (PrivilegedActionException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


    /**
     * Testmethode
     * @return
     */
    public String test() {
		return "Hier ist das Verteilungs Applet!";
	}

}