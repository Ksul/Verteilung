package de.schulte.testverteilung;

import java.net.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class VerteilungServlet
 */
public class VerteilungServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    private VerteilungServices services;

    private String bindingUrl;

    private String user;

    private String password;

    private Logger logger = Logger.getLogger(VerteilungApplet.class.getName());

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public VerteilungServlet() {
		super();

	}


    /**
     * setzt die Parameter
     * @param url          die Binding Url
     * @param userName     der Username
     * @param pass         das Password
     * @return             ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    ret               die Parameter als String
     */
    public JSONObject setParameter(String url, String userName, String pass) {
        bindingUrl = url;
        user = userName;
        password = pass;
        JSONObject obj = new JSONObject();
        try {
            obj.put("success", true);
            obj.put("result", bindingUrl + " " + user + " " + password);
        } catch (JSONException jse) {
            logger.severe(jse.getMessage());
            jse.printStackTrace();
        }
        return obj;
    }

    /**
     * liefert die Alfresco Services
     * @param url               Binding URL des Servers
     * @param user              User Name
     * @param password          Passwort
     * @return
     */
    public VerteilungServices getServices(String url, String user, String password) throws VerteilungException {
        if (url == null || user == null || password == null)
            throw new VerteilungException("Parameter fehlen!");
        if (services == null)
            services = new VerteilungServices(url, user, password);
        return services;
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doGetOrPost(request, response);
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		} catch (JSONException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doGetOrPost(request, response);
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		} catch (JSONException e) {
			throw new ServletException(e);
		}
	}

    /**
     * diese Methode handelt get und post Requests
     * @param req
     * @param resp
     * @throws IOException
     * @throws URISyntaxException
     * @throws JSONException
     */
    private void doGetOrPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, URISyntaxException,
            JSONException {
        // Get the value of a request parameter; the name is case-sensitive
        Object ret = null;
        JSONObject obj = new JSONObject();
        String value = req.getParameter("function");
        String documentId = req.getParameter("documentId");
        String destinationId = req.getParameter("destinationId");
        String currentLocationId = req.getParameter("currentLocationId");
        String documentText = req.getParameter("documentText");
        String filePath = req.getParameter("filePath");
        String fileName = req.getParameter("fileName");
        String cmisQuery = req.getParameter("cmisQuery");
        String destinationFolder = req.getParameter("destinationFolder");
        String description = req.getParameter("description");
        String mimeType = req.getParameter("mimeType");
        String server = req.getParameter("server");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String withFolder = req.getParameter("withFolder");
        String extract = req.getParameter("extract");
        String versionState = req.getParameter("versionState");
        String majorVersion = req.getParameter("majorVersion");
        String versionComment = req.getParameter("versionComment");
        String extraProperties = req.getParameter("extraProperties");
        resp.setHeader("Content-Type", "application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            if (value == null || "".equals(value)) {
                obj.put("success", false);
                obj.put("result", "Function Name is missing!\nPlease check for Tomcat maxPostSize and maxHeaderSizer Property for HTTPConnector");
            } else {
                if (value.equalsIgnoreCase("openPDF")) {
                    openPDF(fileName, resp);
                    return;
                } else if (value.equalsIgnoreCase("setParameter")) {
                    obj = setParameter(server, username, password);
                } else if (value.equalsIgnoreCase("isURLAvailable")) {
                    obj = isURLAvailable(server);
                } else if (value.equalsIgnoreCase("getNodeId")) {
                    obj = getNodeId(filePath);
                } else if (value.equalsIgnoreCase("findDocument")) {
                    obj = findDocument(cmisQuery);
                } else if (value.equalsIgnoreCase("uploadDocument")) {
                    obj = uploadDocument(destinationFolder, fileName);
                } else if (value.equalsIgnoreCase("deleteDocument")) {
                    obj = deleteDocument(destinationFolder, fileName);
                } else if (value.equalsIgnoreCase("createDocument")) {
                    obj = createDocument(destinationFolder, fileName, documentText, mimeType, extraProperties, versionState);
                } else if (value.equalsIgnoreCase("createFolder")) {
                    obj = createFolder(destinationFolder, fileName);
                } else if (value.equalsIgnoreCase("deleteFolder")) {
                    obj = deleteFolder(destinationFolder);
                } else if (value.equalsIgnoreCase("getDocumentContent")) {
                    obj = getDocumentContent(documentId, extract.equalsIgnoreCase("true"));
                } else if (value.equalsIgnoreCase("updateDocument")) {
                    obj = updateDocument(documentId, documentText, mimeType, extraProperties, majorVersion, versionComment);
                } else if (value.equalsIgnoreCase("moveDocument")) {
                    obj = moveDocument(documentId, currentLocationId, destinationId);
                } else if (value.equalsIgnoreCase("listFolderAsJSON")) {
                    obj = listFolderAsJSON(filePath, withFolder);
                } else if (value.equalsIgnoreCase("extractPDFContent")) {
                    obj = extractPDFContent(documentText);
                } else if (value.equalsIgnoreCase("extractPDFFile")) {
                    obj = extractPDFFile(filePath);
                } else if (value.equalsIgnoreCase("extractPDFToInternalStorage")) {
                    obj = extractPDFToInternalStorage(documentText, fileName);
                } else if (value.equalsIgnoreCase("extractZIPToInternalStorage")) {
                    obj = extractZIPToInternalStorage(documentText);
                } else if (value.equalsIgnoreCase("extractZIP")) {
                    obj = extractZIP(documentText);
                } else if (value.equalsIgnoreCase("extractZIPAndExtractPDFToInternalStorage")) {
                    obj = extractZIPAndExtractPDFToInternalStorage(documentText);
                } else if (value.equalsIgnoreCase("getDataFromInternalStorage")) {
                    if (fileName == null || fileName.isEmpty())
                        obj = getDataFromInternalStorage();
                    else
                        obj = getDataFromInternalStorage(fileName);
                } else if (value.equalsIgnoreCase("clearInternalStorage")) {
                    obj = clearInternalStorage();
                } else if (value.equalsIgnoreCase("openFile")) {
                    obj = openFile(filePath);
            } else {
                    obj.put("success", false);
                    obj.put("result", "Function " + value + " ist unbekannt!");
                }
            }
        } catch (VerteilungException e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        } catch (JSONException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        out.write(obj.toString());
    }

    /**
     * prüft, ob eine Url verfügbar ist
     * @param urlString    URL des Servers
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            true, wenn die URL verfügbar ist
     */
    protected JSONObject isURLAvailable(String urlString) {

        VerteilungServices services = new VerteilungServices();
        return services.isURLAvailable(urlString);

    }

    /**
     * liefert eine NodeID als String zurück
     * @param path         der Pfad zum Knoten, der der Knoten gesucht werden soll
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            die NodeId als String
     */
    protected JSONObject getNodeId(String path)  {

        VerteilungServices services = new VerteilungServices(bindingUrl, user, password);
        return services.getNodeId(path);
    }

    /**
     * liefert den Inhalt eines Dokumentes. Wenn es sich um eine PDF Dokument handelt, dann wird
     * der Text extrahiert.
     * @param documentId   die Id des Documentes
     * @param extract      legt fest,ob einPDF Document umgewandelt werden soll
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            das Document als JSONObject
     */
    protected JSONObject getDocumentContent(String documentId, boolean extract) {
        VerteilungServices services = new VerteilungServices(bindingUrl, user, password);
        return services.getDocumentContent(documentId, extract);
    }


    /**
     * lädt ein Document in den Server
     * @param filePath       der Folder als String, in das Document geladen werden soll
     * @param fileName       der Dateiname ( mit Pfad) als String, die hochgeladen werden soll
     * @return               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                               false    ein Fehler ist aufgetreten
     *                                                      result            Dokument als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject uploadDocument(String filePath, String fileName) throws  VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.uploadDocument(filePath, fileName);
	}

    /**
     * löscht ein Document
     * @param filePath       der Folder als String, in das Documentliegt
     * @param fileName       der Name des Documentes
     * @return               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                               false    ein Fehler ist aufgetreten
     *                                                      result            Dokument als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject deleteDocument(String filePath, String fileName) throws  VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.deleteDocument(filePath, fileName);
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
     *                                                             result            Dokument als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject createDocument(String filePath,
                                        String fileName,
                                        String documentContent,
                                        String documentType,
                                        String extraCMSProperties,
                                        String versionState) throws  VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.createDocument(filePath, fileName, documentContent, documentType, extraCMSProperties, versionState);
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
     * @throws VerteilungException
     */
    protected JSONObject updateDocument(String documentId,
                                     String documentContent,
                                     String documentType,
                                     String extraCMSProperties,
                                     String majorVersion,
                                     String versionComment) throws VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.updateDocument(documentId, documentContent, documentType, extraCMSProperties, majorVersion, versionComment);
    }

    /**
     * verschiebt ein Dokument
     * @param  documentId                das zu verschiebende Dokument
     * @param  oldFolderId               der alte Folder in dem das Dokument liegt
     * @param  newFolderId               der Folder, in das Dokument verschoben werden soll
     * @return obj                       ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                           false   ein Fehler ist aufgetreten
     *                                                                  result           bei Erfolg das Document als JSONObject, ansonsten der Fehler
     * @throws VerteilungException
     */
    protected JSONObject moveDocument(String documentId,
                                   String oldFolderId,
                                   String newFolderId) throws VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.moveDocument(documentId, oldFolderId, newFolderId);
    }

    /**
     * erzeugt einen Pfad
     * @param  targetPath             der Name des Folders in dem der Folder erstellt werden soll als String
     * @param  folderName           der Name des neuen Pfades als String
     * @return                      ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result            Folder als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject createFolder(String targetPath,
                                        String folderName) throws  VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.createFolder(targetPath, folderName);
    }

    /**
     * löscht einen Pfad
     * @param  folderPath           der Name des zu löschenden Pfades als String
     * @return                      ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result
     * @throws VerteilungException
     */
    protected JSONObject deleteFolder(String folderPath) throws  VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.deleteFolder(folderPath);
    }

    /**
     * findet ein Document
     * @param cmisQuery    die CMIS Query, mit der der Knoten gesucht werden soll
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            Dokument als JSONObject
     */
    protected JSONObject findDocument(String cmisQuery) throws VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.findDocument(cmisQuery);
    }

    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     * @param filePath     der Pfad, der geliefert werden soll (als NodeId)
     * @param listFolder   was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            der Inhalt des Verzeichnisses als JSON Objekte
     */
    protected JSONObject listFolderAsJSON(String filePath,
                                          String listFolder) throws VerteilungException {

        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.listFolderAsJSON(filePath, Integer.parseInt(listFolder));
    }

    /**
     * extrahiert den Inhalt einer PDF Datei.
     * @param pdfContent        der Inhalt der Datei als Base64 encodeter String
     * @return                  ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der PDF Datei als String
     */
    protected JSONObject extractPDFContent(String pdfContent) {

        VerteilungServices services = new VerteilungServices();
        return services.extractPDFContent(pdfContent);
    }

    /**
     * extrahiert eine PDF Datei.
     * @param filePath          der Pfad zur PDF-Datei
     * @return                  ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der PDF Datei als String
     */
    protected JSONObject extractPDFFile(String filePath) {

        VerteilungServices services = new VerteilungServices();
        return services.extractPDFFile(filePath);
    }

    /**
     * extrahiert den Text aus einer PDF Datei und speichert ihn in den internen Speicher
     * @param documentText       der Inhalt der PDF Datei als Base64 encodeter String
     * @param fileName           der Name der PDF Datei
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg die Anzahl der PDF's, ansonsten der Fehler
     */
    protected JSONObject extractPDFToInternalStorage(String documentText,
                                                     String fileName) throws VerteilungException {

        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.extractPDFToInternalStorage(documentText, fileName);
    }

    /**
     * entpackt ein ZIP File in den internen Speicher
     * @param documentText       der Inhalt des ZIP's als Base64 encodeter String
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg die Anzahl der Dokumente im ZIP File, ansonsten der Fehler
     */
    protected JSONObject extractZIPToInternalStorage(String documentText) throws VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.extractZIPToInternalStorage(documentText);
     }

    /**
     * extrahiert ein ZIP File und gibt den Inhalt als Base64 encodete Strings zurück
     * @param zipContent        der Inhalt des ZIP Files
     * @return                  ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der ZIP Datei als JSON Aray mit Base64 encodeten STrings
     */
    protected JSONObject extractZIP(String zipContent) {

        VerteilungServices services = new VerteilungServices();
        return services.extractZIP(zipContent);
    }

    /**
     * entpackt ein ZIP File und stellt die Inhalte und die extrahierten PDF Inhalte in den internen Speicher
     * @param zipContent          der Inhalt der ZIP Datei als Base64 encodeter String
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg die Anzahl der Dokumente im ZIP File, ansonsten der Fehler
     */
    protected JSONObject extractZIPAndExtractPDFToInternalStorage(String zipContent) {

        VerteilungServices services = new VerteilungServices();
        return services.extractZIPAndExtractPDFToInternalStorage(zipContent);
    }

    /**
     * liefert den kompletten Inhalt aus dem internen Speicher
      * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg ein JSONObjekt mit den Binärdaten (Base64 encoded) und er Inhalt als Text, ansonsten der Fehler
     */
    protected JSONObject getDataFromInternalStorage() {

        VerteilungServices services = new VerteilungServices();
        return services.getDataFromInternalStorage();
    }

    /**
     * liefert den Inhalt aus dem internen Speicher
     * @param fileName           der Name der zu suchenden Datei
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg ein JSONObjekt mit den Binärdaten (Base64 encoded) und er Inhalt als Text, ansonsten der Fehler
     */
    protected JSONObject getDataFromInternalStorage(String fileName) {

        VerteilungServices services = new VerteilungServices();
        return services.getDataFromInternalStorage(fileName);
    }


    /**
     * löscht den internen Speicher
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg nichts, ansonsten der Fehler
     */
    protected JSONObject clearInternalStorage() {

        VerteilungServices services = new VerteilungServices();
        return services.clearInternalStorage();
    }

    /**
     * öffnet eine Datei
     * @param filePath           der Pfad der zu öffnenden Datei
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg der Inhalt der Datei als String, ansonsten der Fehler
     */
    protected JSONObject openFile(String filePath) throws VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.openFile(filePath);
    }

    /**
     * öffnet ein PDF im Browser
     * @param fileName           der Filename des PDF Dokumentes
     * @param resp               der Response zum Öffnen des PDFs
     */
    protected void openPDF(String fileName,
                           HttpServletResponse resp) {
  /*      ServletOutputStream sout = null;
        try {
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
                final byte[] bytes = entry.getData();
                final String name = entry.getName();
                resp.reset();
                resp.resetBuffer();
                resp.setContentType("application/pdf");
                resp.setHeader("Content-Disposition", "inline; filename=" + name + "\"");
                resp.setHeader("Cache-Control", "max-age=0");
                resp.setContentLength(bytes.length);
                sout = resp.getOutputStream();
                sout.write(bytes, 0, bytes.length);
                sout.flush();
                sout.close();
            }
        } catch (Exception e) {
            logger.severe(e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            try {
                sout.close();
            } catch (IOException io) {
                logger.severe(io.getLocalizedMessage());
                io.printStackTrace();
            }
        }
*/        return;
    }



}
