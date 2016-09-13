package de.schulte.testverteilung;

import java.io.*;
import java.net.*;

import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class VerteilungServlet
 */
@WebServlet(name = "VerteilungServlet")
public class VerteilungServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    private VerteilungServices services = new VerteilungServices();

    private Logger logger = Logger.getLogger(VerteilungApplet.class.getName());

    public static final String PARAMETER_FUNCTION = "function";
    public static final String PARAMETER_DOCUMENTID = "documentId";
    public static final String PARAMETER_DESTINATIONID = "destinationId";
    public static final String PARAMETER_CURENTLOCATIONID = "currentLocationId";
    public static final String PARAMETER_DOCUMENTTEXT = "documentText";
    public static final String PARAMETER_FILEPATH = "filePath";
    public static final String PARAMETER_FILENAME = "fileName";
    public static final String PARAMETER_IMAGELINK = "imageLink";
    public static final String PARAMETER_CMISQUERY = "cmisQuery";
    public static final String PARAMETER_MIMETYPE = "mimeType";
    public static final String PARAMETER_SERVER = "server";
    public static final String PARAMETER_BINDING = "binding";
    public static final String PARAMETER_USERNAME = "user";
    public static final String PARAMETER_PASSWORD = "password";
    public static final String PARAMETER_WITHFOLDER = "withFolder";
    public static final String PARAMETER_EXTRACT = "extract";
    public static final String PARAMETER_VERSIONSTATE = "versionState";
    public static final String PARAMETER_VERSIONCOMMENT = "versionComment";
    public static final String PARAMETER_EXTRAPROPERTIES = "extraProperties";
    public static final String PARAMETER_TICKET = "ticket";
    public static final String PARAMETER_COMMENT = "comment";
    public static final String PARAMETER_MAXITEMSPERPAGE = "maxItemsPerPage";
    public static final String PARAMETER_PAGESTOSKIP = "pagesToSkip";
    public static final String PARAMETER_TIMEOUT = "timeout";

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
    public static final String FUNCTION_GETNODE = "getNode";
    public static final String FUNCTION_GETNODEID = "getNodeId";
    public static final String FUNCTION_GETNODEBYID = "getNodeById";
    public static final String FUNCTION_ISURLAVAILABLE = "isURLAvailable";
    public static final String FUNCTION_LISTFOLDERASJSON = "listFolder";
    public static final String FUNCTION_LISTFOLDERASJSONWITHPAGINATION = "listFolderWithPagination";
    public static final String FUNCTION_MOVENODE = "moveNode";
    public static final String FUNCTION_OPENFILE = "openFile";
    public static final String FUNCTION_OPENPDF = "openPDF";
    public static final String FUNCTION_OPENIMAGE = "openImage";
    public static final String FUNCTION_SETPARAMETER = "setParameter";
    public static final String FUNCTION_UPDATEDOCUMENT = "updateDocument";
    public static final String FUNCTION_UPLOADDOCUMENT = "uploadDocument";
    public static final String FUNCTION_UPDATEPROPERTIES = "updateproperties";
    public static final String FUNCTION_GETTICKET = "getTicket";
    public static final String FUNCTION_GETTICKETWITHUSERANDPASSWORD = "getTicketWithUserAndPassword";
    public static final String FUNCTION_GETCOMMENTS = "getComments";
    public static final String FUNCTION_ADDCOMMENT = "addComment";
    public static final String FUNCTION_QUERY = "query";
    public static final String FUNCTION_GETTITLES = "getTitles";
    public static final String FUNCTION_ISCONNECTED = "isConnected";
    public static final String FUNCTION_GETCONNECTION = "getConnection";
    public static final String FUNCTION_CLEARCACHE = "clearCache";


    @Override
    public void init() {
        String server, binding, user, password;
        try {
            Properties properties = new Properties();
            InputStream inputStream = getServletContext().getResourceAsStream("resources/connection.properties");
            if (inputStream != null) {
                properties.load(inputStream);
                server = properties.getProperty("server");
                password = properties.getProperty("password");
                user = properties.getProperty("user");
                binding = properties.getProperty("binding");
                if (server != null && server.length() > 0 && password != null && password.length() > 0 &&
                        user != null && user.length() > 0 && binding!= null && binding.length() > 0)
                    services.setParameter(server,binding, user, password);
            }
        }catch (IOException e) {
        System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * liefert die Services
     * nur für Testzwecke!!!
     * @return  die Services
     */
    protected VerteilungServices getServices() {
        return services;
    }



    /**
     * setzt die Parameter
     * @param server       die Server URL
     * @param binding      das Binding Teil
     * @param user         der Username
     * @param password     das Password
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    ret               die Parameter als String
     */
    protected JSONObject setParameter(String server,
                                      String binding,
                                      String user,
                                      String password) {

        JSONObject obj = new JSONObject();
        try {
            if (server != null && !server.isEmpty() && binding != null && !binding.isEmpty() && user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
                services.setParameter(server, binding, user, password);
                obj.put("success", true);
                obj.put("result", "Server:" + server + " Binding:" + binding + " User:" + user + " Password:" + password);
            } else {
                obj.put("success", false);
                obj.put("result", "Parameter fehlt: Server: " + server + " Binding: " + binding + " User: " + user + " Password:" + password);
            }
        } catch (Exception e) {
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
                logger.info("Call of Method " + value);
                if (value.equalsIgnoreCase(FUNCTION_OPENPDF)) {
                    openPDF(getURLParameter(req, PARAMETER_FILENAME, true), resp);
                    return;
                } else if (value.equalsIgnoreCase(FUNCTION_OPENIMAGE)) {
                    openImage(getURLParameter(req, PARAMETER_IMAGELINK, true), resp);
                    return;
                } else if (value.equalsIgnoreCase(FUNCTION_SETPARAMETER)) {
                    obj = setParameter(getURLParameter(req, PARAMETER_SERVER, true), getURLParameter(req, PARAMETER_BINDING, true), getURLParameter(req, PARAMETER_USERNAME, true), getURLParameter(req, PARAMETER_PASSWORD, true));
                } else if (value.equalsIgnoreCase(FUNCTION_ISURLAVAILABLE)) {
                    obj = isURLAvailable(getURLParameter(req, PARAMETER_SERVER, true), getURLParameter(req, PARAMETER_TIMEOUT, true));
                } else if (value.equalsIgnoreCase(FUNCTION_GETTICKET)) {
                    obj = getTicket();
                } else if (value.equalsIgnoreCase(FUNCTION_GETTICKETWITHUSERANDPASSWORD)) {
                    obj = getTicketWithUserAndPassword(getURLParameter(req, PARAMETER_USERNAME, true), getURLParameter(req, PARAMETER_PASSWORD, true), getURLParameter(req, PARAMETER_SERVER, true));
                } else if (value.equalsIgnoreCase(FUNCTION_GETCOMMENTS)) {
                    obj = getComments(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_TICKET, true));
                } else if (value.equalsIgnoreCase(FUNCTION_ADDCOMMENT)) {
                    obj = addComment(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_TICKET, true), getURLParameter(req, PARAMETER_COMMENT, true));
                } else if (value.equalsIgnoreCase(FUNCTION_GETNODE)) {
                    obj = getNode(getURLParameter(req, PARAMETER_FILEPATH, true));
                } else if (value.equalsIgnoreCase(FUNCTION_GETNODEID)) {
                    obj = getNodeId(getURLParameter(req, PARAMETER_FILEPATH, true));
                } else if (value.equalsIgnoreCase(FUNCTION_GETNODEBYID)) {
                    obj = getNodeById(getURLParameter(req, PARAMETER_DOCUMENTID, true));
                } else if (value.equalsIgnoreCase(FUNCTION_FINDDOCUMENT)) {
                    obj = findDocument(getURLParameter(req, PARAMETER_CMISQUERY, true));
                } else if (value.equalsIgnoreCase(FUNCTION_QUERY)) {
                    obj = query(getURLParameter(req, PARAMETER_CMISQUERY, true));
                } else if (value.equalsIgnoreCase(FUNCTION_UPLOADDOCUMENT)) {
                    obj = uploadDocument(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_FILENAME, true), getURLParameter(req, PARAMETER_VERSIONSTATE, true));
                } else if (value.equalsIgnoreCase(FUNCTION_DELETEDOCUMENT)) {
                    obj = deleteDocument(getURLParameter(req, PARAMETER_DOCUMENTID, true));
                } else if (value.equalsIgnoreCase(FUNCTION_CREATEDOCUMENT)) {
                    obj = createDocument(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_FILENAME, true), getURLParameter(req, PARAMETER_DOCUMENTTEXT, true), getURLParameter(req, PARAMETER_MIMETYPE, false), getURLParameter(req, PARAMETER_EXTRAPROPERTIES, false), getURLParameter(req, PARAMETER_VERSIONSTATE, false));
                } else if (value.equalsIgnoreCase(FUNCTION_CREATEFOLDER)) {
                    obj = createFolder(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_EXTRAPROPERTIES, true));
                } else if (value.equalsIgnoreCase(FUNCTION_DELETEFOLDER)) {
                    obj = deleteFolder(getURLParameter(req, PARAMETER_DOCUMENTID, true));
                } else if (value.equalsIgnoreCase(FUNCTION_GETDOCUMENTCONTENT)) {
                    obj = getDocumentContent(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_EXTRACT, true).equalsIgnoreCase("true"));
                } else if (value.equalsIgnoreCase(FUNCTION_UPDATEDOCUMENT)) {
                    obj = updateDocument(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_DOCUMENTTEXT, false), getURLParameter(req, PARAMETER_MIMETYPE, false), getURLParameter(req, PARAMETER_EXTRAPROPERTIES, false), getURLParameter(req, PARAMETER_VERSIONSTATE, false), getURLParameter(req, PARAMETER_VERSIONCOMMENT, false));
                } else if (value.equalsIgnoreCase(FUNCTION_UPDATEPROPERTIES)) {
                    obj = updateProperties(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_EXTRAPROPERTIES, true));
                } else if (value.equalsIgnoreCase(FUNCTION_MOVENODE)) {
                    obj = moveNode(getURLParameter(req, PARAMETER_DOCUMENTID, true), getURLParameter(req, PARAMETER_CURENTLOCATIONID, true), getURLParameter(req, PARAMETER_DESTINATIONID, true));
                } else if (value.equalsIgnoreCase(FUNCTION_LISTFOLDERASJSON)) {
                    obj = listFolder(getURLParameter(req, PARAMETER_FILEPATH, true), getURLParameter(req, PARAMETER_WITHFOLDER, true));
                } else if (value.equalsIgnoreCase(FUNCTION_LISTFOLDERASJSONWITHPAGINATION)) {
                    obj = listFolderWithPagination(getURLParameter(req, PARAMETER_FILEPATH, true), getURLParameter(req, PARAMETER_WITHFOLDER, true), getURLParameter(req, PARAMETER_MAXITEMSPERPAGE, true), getURLParameter(req, PARAMETER_PAGESTOSKIP, true));
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
                } else if (value.equalsIgnoreCase(FUNCTION_GETTITLES)) {
                    obj = getTitles();
                } else if (value.equalsIgnoreCase(FUNCTION_ISCONNECTED)) {
                    obj = isConnected();
                } else if (value.equalsIgnoreCase(FUNCTION_GETCONNECTION)) {
                    obj = getConnection();
                } else if (value.equalsIgnoreCase(FUNCTION_CLEARCACHE)) {
                    obj = clearCache();
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
     * liefert Informationen zur Connection
     * @return obj               ein JSONObject mit den Feldern success: true        die Operation war erfolgreich
     *                                                                   false       ein Fehler ist aufgetreten
     *                                                          result   false       keine Connection
     *                                                                   JSONObjekt  Die Verbindungsparameter
     */
    protected JSONObject getConnection() {
        return services.getConnection();
    }

    /**
     * löscht den Cache
     * @return obj               ein JSONObject mit den Feldern success: true        die Operation war erfolgreich
     *                                                                   false       ein Fehler ist aufgetreten
     *                                                          result   false       keine Connection
     *                                                                   JSONObjekt  Die Verbindungsparameter
     */
    protected JSONObject clearCache() {
        return services.clearCache();
    }

    /**
     * prüft, ob schon eine Verbindung zu einem Alfresco Server besteht
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            true, wenn Verbindung vorhanden
     */
    protected JSONObject isConnected() {
        return services.isConnected();
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
     * liefert ein Ticket zur Authentifizierung
     * @param user         der Name des Users
     * @param password     das Password
     * @param server       der Alfresco Server
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            das Ticket als String
     */
    protected JSONObject getTicketWithUserAndPassword(String user, String password, String server) {

        return services.getTicketWithUserAndPassword(user, password, server);

    }

    /**
     * liefert die vorhandenen Titel
     * @return obj               ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                   false    ein Fehler ist aufgetreten
     *                                                          result            die Titel als String
     */
    protected JSONObject getTitles() {
        return services.getTitles();
    }

    /**
     * liefert ein Ticket zur Authentifizierung
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            das Ticket als String
     */
    protected JSONObject getTicket() {

        return services.getTicket();

    }

    /**
     * liefert die Kommentare zu einem Knoten
     * @param documentId   die Id des Knoten
     * @param ticket       das Ticket zur Identifizierung am Alfresco
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            die Kommentare als JSON Objekt
     */
    protected JSONObject getComments(String documentId, String ticket) {

        return services.getComments(documentId, ticket);

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
    protected JSONObject addComment(String documentId, String ticket, String comment) {

        return services.addComment(documentId, ticket, comment);

    }

    /**
     * prüft, ob eine Url verfügbar ist
     * @param urlString    URL des Servers
     * @param timeout      der Timeout
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            true, wenn die URL verfügbar ist
     */
    protected JSONObject isURLAvailable(String urlString, String timeout) {

        return services.isURLAvailable(urlString, Integer.parseInt(timeout));

    }

    /**
     * liefert einen Knoten als JSON Objekt zurück
     * @param path         der Pfad zum Knoten
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            der Node als JSONObject
     */
    protected JSONObject getNodeById(String path)  {

        return services.getNodeById(path);
    }

    /**
     * liefert die ID eines Knoten zurück
     * @param path         der Pfad zum Knoten
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            der Id des Knotens als String
     */
    protected JSONObject getNodeId(String path)  {

        return services.getNodeId(path);
    }

    /**
     * liefert einen Knoten als JSON Objekt zurück
     * @param documentId   die Id des Knotens
     * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            der Node als JSONObject
     */
    protected JSONObject getNode(String documentId)  {

        return services.getNodeById(documentId);
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
     * @param documentId              die Id des Folders als String, in das Document geladen werden soll
     * @param fileName                der Dateiname ( mit Pfad) als String, die hochgeladen werden soll
     * @param  versionState              der VersionsStatus ( none, major, minor, checkedout)
     * @return                        ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                        false    ein Fehler ist aufgetreten
     *                                                               result            Dokument als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject uploadDocument(String documentId,
                                        String fileName,
                                        String versionState) throws VerteilungException {

        return services.uploadDocument(documentId, fileName, versionState);
    }

    /**
     * löscht ein Document
     * @param documentId              die Id des Dokumentes das gelöscht werden soll als String
     * @return                        ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                        false    ein Fehler ist aufgetreten
     *                                                               result            Dokument als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject deleteDocument(String documentId) throws  VerteilungException {

        return services.deleteDocument(documentId);
    }

    /**
     * erzeugt ein Document
     * @param  documentId           die Id des Folders in dem das Dokument erstellt werden soll als String
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
    protected JSONObject createDocument(String documentId,
                                        String fileName,
                                        String documentContent,
                                        String documentType,
                                        String extraCMSProperties,
                                        String versionState) throws  VerteilungException {

        return services.createDocument(documentId, fileName, documentContent, documentType, extraCMSProperties, versionState);
    }

    /**
     * aktualisiert die Properties eines Dokumentes
     * @param  documentId                Die Id des zu aktualisierenden Dokumentes
     * @param  extraCMSProperties        zusätzliche Properties
     * @return obj                       ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                           false   ein Fehler ist aufgetreten
     *                                                                  result           bei Erfolg nichts, ansonsten der Fehler
     * @throws VerteilungException
     */
    protected JSONObject updateProperties(String documentId,
                                     String extraCMSProperties) throws VerteilungException {

        return services.updateProperties(documentId, extraCMSProperties);
    }

    /**
     * aktualisiert den Inhalt eines Dokumentes
     * @param  documentId                Die Id des zu aktualisierenden Dokumentes
     * @param  documentContent           der neue Inhalt
     * @param  documentType              der Typ des Dokumentes
     * @param  extraCMSProperties        zusätzliche Properties
     * @param  majorVersion              falls Dokument versionierbar, dann wird eine neue Major-Version erzeugt, falls true
     * @param  versionComment            falls Dokument versionierbar, dann kann hier eine Kommentar zur Version mitgegeben werden
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
     * @param  documentId                die Id des des zu verschiebende Knoten
     * @param  oldFolderId               der alte Folder in dem der Knoten liegt
     * @param  newFolderId               der Folder, in das der Knoten verschoben werden soll
     * @return obj                       ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                           false   ein Fehler ist aufgetreten
     *                                                                  result           bei Erfolg das Document als JSONObject, ansonsten der Fehler
     * @throws VerteilungException
     */
    protected JSONObject moveNode(String documentId,
                                  String oldFolderId,
                                  String newFolderId) throws VerteilungException {

        return services.moveNode(documentId, oldFolderId, newFolderId);
    }

    /**
     * erzeugt einen Pfad
     * @param  documentId           die Id des Folders in dem der Folder erstellt werden soll als String
     * @param  extraProperties      die Prperties des neuen Folders
     * @return                      ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result            Folder als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject createFolder(String documentId,
                                      String extraProperties) throws  VerteilungException {

        return services.createFolder(documentId, extraProperties);
    }

    /**
     * löscht einen Pfad
     * @param  documentId           die Id des zu löschenden Pfades als String
     * @return                      ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result
     * @throws VerteilungException
     */
    protected JSONObject deleteFolder(String documentId) throws  VerteilungException {

        return services.deleteFolder(documentId);
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
     * führt eien Query durch und liefert die Ergebnisse als JSON Objekte zurück
     * @param cmisQuery    die Query als String
     * @return obj         ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                             false   ein Fehler ist aufgetreten
     *                                                    result           eine Liste mit Strings
     */
    protected JSONObject query(String cmisQuery) throws VerteilungException {
        return services.query(cmisQuery);
    }


        /**
         * liefert die Dokumente eines Alfresco Folders als JSON Objekte
         * @param filePath     der Pfad, der geliefert werden soll (als NodeId)
         * @param listFolder   was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
         * @return             ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
         *                                                             false    ein Fehler ist aufgetreten
         *                                                    result            der Inhalt des Verzeichnisses als JSON Objekte
         */
    protected JSONObject listFolder(String filePath,
                                          String listFolder) throws VerteilungException {

        return services.listFolder(filePath, Integer.parseInt(listFolder));
    }

    /**
     * liefert die Dokumente eines Alfresco Folders seitenweise als JSON Objekte
     * @param filePath           der Pfad, der geliefert werden soll (als NodeId)
     * @param listFolder         was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @param  maxItemsPerPage   die maximale Anzahl
     * @param  pagesToSkip       die Anzahl Seiten die übersprungen werden soll
     * @return                    ein JSONObject mit den Feldern success: true     die Operation war erfolgreich
     *                                                                    false    ein Fehler ist aufgetreten
     *                                                           result            der Inhalt des Verzeichnisses als JSON Objekte
     */
    protected JSONObject listFolderWithPagination(String filePath,
                                                  String listFolder,
                                                  String maxItemsPerPage,
                                                  String pagesToSkip) throws VerteilungException {

        return services.listFolder(filePath, Integer.parseInt(listFolder), Integer.parseInt(maxItemsPerPage), Integer.parseInt(pagesToSkip));
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

        return services.openFile(getServletContext().getRealPath(fileName).replace("\\", "/"));
    }

    /**
     * öffnet ein Image auf dem Alfrsco Server
     * @param link      der link zu dem Image
     * @param resp       der Response zum Öffnen
     */
    protected void openImage(String link,
                             HttpServletResponse resp) {
        ServletOutputStream sout = null;
        String server = services.getServer();

        if (!server.endsWith("/"))
            server = server + "/";
        try {

            String ticket = services.getTicket().getJSONObject("result").getJSONObject("data").getString("ticket");
            URL url = new URL(server + link + "&alf_ticket=" + ticket);
            InputStream is = url.openStream();

            byte[] b = new byte[2048];
            int length;

            resp.reset();
            resp.resetBuffer();
            resp.setContentType("image/jpeg");
            sout = resp.getOutputStream();

            while ((length = is.read(b)) != -1) {
                sout.write(b, 0, length);
            }

            is.close();
            sout.flush();
            sout.close();
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
