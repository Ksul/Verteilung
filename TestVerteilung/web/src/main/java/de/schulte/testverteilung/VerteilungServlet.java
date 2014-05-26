package de.schulte.testverteilung;

import java.net.*;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.sun.deploy.net.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class VerteilungServlet
 */
public class VerteilungServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    private VerteilungServices services;

    private Logger logger = Logger.getLogger(VerteilungApplet.class.getName());


    public static final String PARAMETER_FUNCTION = "function";
    public static final String PARAMETER_DOCUMENTID = "documentId";
    public static final String PARAMETER_DESTINATIONID = "destinationId";
    public static final String PARAMETER_CURENTLOCATIONID = "currentLocationId";
    public static final String PARAMETER_DOCUMENTTEXT = "documentText";
    public static final String PARAMETER_FILEPATH = "filePath";
    public static final String PARAMETER_FILENAME = "fileName";
    public static final String PARAMETER_CMISQUERY = "cmisQuery";
    public static final String PARAMETER_FOLDER = "folder";
    public static final String PARAMETER_MIMETYPE = "mimeType";
    public static final String PARAMETER_SERVER = "server";
    public static final String PARAMETER_USERNAME = "user";
    public static final String PARAMETER_PASSWORD = "password";
    public static final String PARAMETER_WITHFOLDER = "withFolder";
    public static final String PARAMETER_EXTRACT = "extract";
    public static final String PARAMETER_VERSIONSTATE = "versionState";
    public static final String PARAMETER_MAJORVERSION = "majorVersion";
    public static final String PARAMETER_VERSIONCOMMENT = "versionComment";
    public static final String PARAMETER_EXTRAPROPERTIES = "extraProperties";

    public static final String FUNCTION_CLEARINTERNALSTORAGE = "clearInternalStorage";
    public static final String FUNCTION_CREATEDOCUMENT = "createDocument";
    public static final String FUNCTION_CREATEFOLDER = "createFolder";
    public static final String FUNCTION_DELETEDOCUMENT = "deleteDocument";
    public static final String FUNCTION_DELETEFOLDER = "deleteFolder";
    public static final String FUNCTION_EXTRACTPDFCONTENT = "extractPDFContent";
    public static final String FUNCTION_EXTRACTPDFFILE = "extractPDFFile";
    public static final String FUNCTION_EXTRACTPDFTOINTERNALSTORAGE = "extractPDFToInternalStorage";
    public static final String FUNCTION_EXTRACTZIP = "extractZIP";
    public static final String FUNCTION_EXTRACTZIPANDEXTRACTPDFTOINTERNALSTORAGE = "extractZIPAndExtractPDFToInternalStorage";
    public static final String FUNCTION_EXTRACTZIPTOINTERNALSTORAGE = "extractZIPToInternalStorage";
    public static final String FUNCTION_FINDDOCUMENT = "findDocument";
    public static final String FUNCTION_GETDATAFROMINTERNALSTORAGE = "getDataFromInternalStorage";
    public static final String FUNCTION_GETDOCUMENTCONTENT = "getDocumentContent";
    public static final String FUNCTION_GETNODEID = "getNodeId";
    public static final String FUNCTION_ISURLAVAILABLE = "isURLAvailable";
    public static final String FUNCTION_LISTFOLDERASJSON = "listFolderAsJSON";
    public static final String FUNCTION_MOVEDOCUMENT = "moveDocument";
    public static final String FUNCTION_OPENFILE = "openFile";
    public static final String FUNCTION_OPENPDF = "openPDF";
    public static final String FUNCTION_SETPARAMETER = "setParameter";
    public static final String FUNCTION_UPDATEDOCUMENT = "updateDocument";
    public static final String FUNCTION_UPLOADDOCUMENT = "uploadDocument";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public VerteilungServlet() {
		super();
        this.services = new VerteilungServices();

	}

    /**
     * liefert die Services
     * nur für Testzwecke
     * @return  die Services
     */
    public VerteilungServices getServices() {
        return services;
    }



    /**
     * setzt die Parameter
     * @param bindingUrl   die Binding Url
     * @param user         der Username
     * @param password     das Password
     * @return             ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    ret               die Parameter als String
     */
    public JSONObject setParameter(String bindingUrl,
                                   String user,
                                   String password) {

        JSONObject obj = new JSONObject();
        try {
            if (bindingUrl != null && !bindingUrl.isEmpty() && user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
                services.setParameter(bindingUrl, user, password);
                obj.put("success", true);
                obj.put("result", "BindingUrl:" + bindingUrl + " User:" + user + " Password:" + password);
            } else {
                obj.put("success", false);
                obj.put("result", "Parameter fehlt: BindingUrl:" + bindingUrl + " User: " + user + " Password:" + password);
            }        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        return obj;
    }


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {

		doGetOrPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {

        doGetOrPost(request, response);
	}

    /**
     * diese Methode handelt get und post Requests
     * @param req   der Request
     * @param resp  der Response
     * @throws IOException
     * @throws URISyntaxException
     * @throws JSONException
     */
    private void doGetOrPost(HttpServletRequest req,
                             HttpServletResponse resp) throws IOException {

        // Get the value of a request parameter; the name is case-sensitive
        JSONObject obj = new JSONObject();
        String value = req.getParameter(PARAMETER_FUNCTION);
        resp.setHeader("Content-Type", "application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            if (value == null || "".equals(value)) {
                obj.put("success", false);
                obj.put("result", "Function Name is missing!\nPlease check for Tomcat maxPostSize and maxHeaderSizer Property for HTTPConnector");
            } else {
                if (value.equalsIgnoreCase(FUNCTION_OPENPDF)) {
                    openPDF(getURLParameter(req, PARAMETER_FILENAME, true), resp);
                    return;
                } else if (value.equalsIgnoreCase(FUNCTION_SETPARAMETER)) {
                    obj = setParameter(getURLParameter(req, PARAMETER_SERVER, true), getURLParameter(req, PARAMETER_USERNAME, true), getURLParameter(req, PARAMETER_PASSWORD, true));
                } else if (value.equalsIgnoreCase(FUNCTION_ISURLAVAILABLE)) {
                    obj = isURLAvailable(getURLParameter(req, PARAMETER_SERVER, true));
                } else if (value.equalsIgnoreCase(FUNCTION_GETNODEID)) {
                    obj = getNodeId(getURLParameter(req, PARAMETER_FILEPATH, true));
                } else if (value.equalsIgnoreCase(FUNCTION_FINDDOCUMENT)) {
                    obj = findDocument(getURLParameter(req, PARAMETER_CMISQUERY, true));
                } else if (value.equalsIgnoreCase(FUNCTION_UPLOADDOCUMENT)) {
                    obj = uploadDocument(getURLParameter(req, PARAMETER_FOLDER, true), getURLParameter(req, PARAMETER_FILENAME, true));
                } else if (value.equalsIgnoreCase(FUNCTION_DELETEDOCUMENT)) {
                    obj = deleteDocument(getURLParameter(req, PARAMETER_FOLDER, true), getURLParameter(req, PARAMETER_FILENAME, true));
                } else if (value.equalsIgnoreCase(FUNCTION_CREATEDOCUMENT)) {
                    obj = createDocument(getURLParameter(req, PARAMETER_FOLDER, true), getURLParameter(req, PARAMETER_FILENAME, true), getURLParameter(req, PARAMETER_DOCUMENTTEXT, true), getURLParameter(req, PARAMETER_MIMETYPE, false), getURLParameter(req, PARAMETER_EXTRAPROPERTIES, false), getURLParameter(req, PARAMETER_VERSIONSTATE, false));
                } else if (value.equalsIgnoreCase(FUNCTION_CREATEFOLDER)) {
                    obj = createFolder(getURLParameter(req, PARAMETER_FOLDER, true), getURLParameter(req, PARAMETER_FILENAME, true));
                } else if (value.equalsIgnoreCase(FUNCTION_DELETEFOLDER)) {
                    obj = deleteFolder(getURLParameter(req, PARAMETER_FOLDER, true));
                } else if (value.equalsIgnoreCase(FUNCTION_GETDOCUMENTCONTENT)) {
                    obj = getDocumentContent(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_EXTRACT, true).equalsIgnoreCase("true"));
                } else if (value.equalsIgnoreCase(FUNCTION_UPDATEDOCUMENT)) {
                    obj = updateDocument(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_DOCUMENTTEXT, true), getURLParameter(req, PARAMETER_MIMETYPE, false), getURLParameter(req, PARAMETER_EXTRAPROPERTIES, false), getURLParameter(req, PARAMETER_MAJORVERSION, false), getURLParameter(req, PARAMETER_VERSIONCOMMENT, false));
                } else if (value.equalsIgnoreCase(FUNCTION_MOVEDOCUMENT)) {
                    obj = moveDocument(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_CURENTLOCATIONID, true), getURLParameter(req, PARAMETER_DESTINATIONID, true));
                } else if (value.equalsIgnoreCase(FUNCTION_LISTFOLDERASJSON)) {
                    obj = listFolderAsJSON(getURLParameter(req, PARAMETER_FILEPATH, true), getURLParameter(req, PARAMETER_WITHFOLDER, true));
                } else if (value.equalsIgnoreCase(FUNCTION_EXTRACTPDFCONTENT)) {
                    obj = extractPDFContent(getURLParameter(req, PARAMETER_DOCUMENTTEXT, true));
                } else if (value.equalsIgnoreCase(FUNCTION_EXTRACTPDFFILE)) {
                    obj = extractPDFFile(getURLParameter(req, PARAMETER_FILENAME, true));
                } else if (value.equalsIgnoreCase(FUNCTION_EXTRACTPDFTOINTERNALSTORAGE)) {
                    obj = extractPDFToInternalStorage(getURLParameter(req, PARAMETER_DOCUMENTTEXT, true), getURLParameter(req, PARAMETER_FILENAME, true));
                } else if (value.equalsIgnoreCase(FUNCTION_EXTRACTZIPTOINTERNALSTORAGE)) {
                    obj = extractZIPToInternalStorage(getURLParameter(req, PARAMETER_DOCUMENTTEXT, true));
                } else if (value.equalsIgnoreCase(FUNCTION_EXTRACTZIP)) {
                    obj = extractZIP(getURLParameter(req, PARAMETER_DOCUMENTTEXT, true));
                } else if (value.equalsIgnoreCase(FUNCTION_EXTRACTZIPANDEXTRACTPDFTOINTERNALSTORAGE)) {
                    obj = extractZIPAndExtractPDFToInternalStorage(getURLParameter(req, PARAMETER_DOCUMENTTEXT, true));
                } else if (value.equalsIgnoreCase(FUNCTION_GETDATAFROMINTERNALSTORAGE)) {
                    String fileName = getURLParameter(req, PARAMETER_FILENAME, false);
                    if (fileName == null || fileName.isEmpty())
                        obj = getDataFromInternalStorage();
                    else
                        obj = getDataFromInternalStorage(fileName);
                } else if (value.equalsIgnoreCase(FUNCTION_CLEARINTERNALSTORAGE)) {
                    obj = clearInternalStorage();
                } else if (value.equalsIgnoreCase("openFile")) {
                    obj = openFile(getURLParameter(req, PARAMETER_FILENAME, true));
            } else {
                    obj.put("success", false);
                    obj.put("result", "Function " + value + " ist dem Servlet unbekannt!");
                }
            }
        } catch (Exception e) {
            obj = VerteilungHelper.convertErrorToJSON(e);
        }
        out.write(obj.toString());
    }

    /**
     * liefert den für den Service notwendigen Parameter
     * falls der Parameter benötigt wird und dieser nicht vorhanden ist, dann wird eine Exception geworfen.
     * @param req           der Request
     * @param parameter     der Name des Parameters
     * @param neccesary     Flag, ob der Parameter notwendig ist
     * @return
     * @throws VerteilungException wenn ein notwendiger Parameter nicht gesetzt ist.
     */
    private String getURLParameter(HttpServletRequest req,
                                   String parameter,
                                   boolean neccesary) throws VerteilungException {

        String param = req.getParameter(parameter);
        if (param == null && neccesary)
            throw new VerteilungException("Parameter " + parameter + " fehlt");
        return param;
    }

    /**
     * prüft, ob eine Url verfügbar ist
     * @param urlString    URL des Servers
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            true, wenn die URL verfügbar ist
     */
    protected JSONObject isURLAvailable(String urlString) {

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
    protected JSONObject getDocumentContent(String documentId,
                                            boolean extract) {

        return services.getDocumentContent(documentId, extract);
    }


    /**
     * lädt ein Document in den Server
     * @param folder                  der Folder als String, in das Document geladen werden soll
     * @param fileName                der Dateiname ( mit Pfad) als String, die hochgeladen werden soll
     * @return                        ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                        false    ein Fehler ist aufgetreten
     *                                                               result            Dokument als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject uploadDocument(String folder,
                                        String fileName) throws VerteilungException {

        return services.uploadDocument(folder, fileName);
    }

    /**
     * löscht ein Document
     * @param folder                  der Folder als String, in das Documentliegt
     * @param fileName                der Name des Documentes
     * @return                        ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                        false    ein Fehler ist aufgetreten
     *                                                               result            Dokument als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject deleteDocument(String folder,
                                        String fileName) throws  VerteilungException {

        return services.deleteDocument(folder, fileName);
    }

    /**
     * erzeugt ein Document
     * @param  folder               der Name des Folders in dem das Dokument erstellt werden soll als String
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
    protected JSONObject createDocument(String folder,
                                        String fileName,
                                        String documentContent,
                                        String documentType,
                                        String extraCMSProperties,
                                        String versionState) throws  VerteilungException {

        return services.createDocument(folder, fileName, documentContent, documentType, extraCMSProperties, versionState);
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

        return services.moveDocument(documentId, oldFolderId, newFolderId);
    }

    /**
     * erzeugt einen Pfad
     * @param  folder               der Name des Folders in dem der Folder erstellt werden soll als String
     * @param  fileName             der Name des neuen Pfades als String
     * @return                      ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result            Folder als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject createFolder(String folder,
                                      String fileName) throws  VerteilungException {

        return services.createFolder(folder, fileName);
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

        return services.extractPDFContent(pdfContent);
    }

    /**
     * TODO Funktioniert das im Servlet Kontext?
     * extrahiert eine PDF Datei.
     * @param fileName          der Name der PDF-Datei
     * @return                  ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                  false    ein Fehler ist aufgetreten
     *                                                         result            der Inhalt der PDF Datei als String
     */
    protected JSONObject extractPDFFile(String fileName) {

        return services.extractPDFFile(getServletContext().getRealPath(fileName));
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

        return services.extractZIPAndExtractPDFToInternalStorage(zipContent);
    }

    /**
     * liefert den kompletten Inhalt aus dem internen Speicher
      * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg ein JSONObjekt mit den Binärdaten (Base64 encoded) und er Inhalt als Text, ansonsten der Fehler
     */
    protected JSONObject getDataFromInternalStorage() {

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

        return services.getDataFromInternalStorage(fileName);
    }


    /**
     * löscht den internen Speicher
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg nichts, ansonsten der Fehler
     */
    protected JSONObject clearInternalStorage() {

        return services.clearInternalStorage();
    }

    /**
     * TODO macht diese Methode im Servlert Kontext Sinn?
     * öffnet eine Datei
     * @param fileName           der Name der zu öffnenden Datei
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            bei Erfolg der Inhalt der Datei als String, ansonsten der Fehler
     */
    protected JSONObject openFile(String fileName) throws VerteilungException {

        return services.openFile(getServletContext().getRealPath(fileName));
    }

    /**
     * öffnet ein PDF im Browser
     * @param fileName           der Filename des PDF Dokumentes
     * @param resp               der Response zum Öffnen des PDFs
     */
    protected void openPDF(String fileName,
                           HttpServletResponse resp) {
        ServletOutputStream sout = null;
        try {
            for (FileEntry entry : services.getEntries()) {
                if (entry.getName().equalsIgnoreCase(fileName)) {
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
                    break;
                }
            }
        } catch (Exception e) {
            logger.severe(e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            try {
                if (sout != null)
                    sout.close();
            } catch (IOException io) {
                logger.severe(io.getLocalizedMessage());
                io.printStackTrace();
            }
        }
    }
}
