package de.schulte.testverteilung;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.alfresco.cmis.client.AlfrescoFolder;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDateTimeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 09.01.14
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoConnector {


    private static final String NODES_URL = "service/api/node/workspace/SpacesStore/";
    private static final String LOGIN_URL = "service/api/login";
    private static Logger logger = Logger.getLogger(AlfrescoConnector.class.getName());
    private String user = null;
    private String password = null;
    private String bindingUrl = null;
    private String server = null;
    private static enum RequestType {
        POST("POST"),
        GET("GET");

        private final String name;
        RequestType(String name) {
            this.name = name;
        }
        String getName() {
            return this.name;
        }
    }

    private Session session = null;

    /**
     * Konstruktor
     * @param user        der Username
     * @param password    das Password
     * @param server      die Server URL
     * @param bindingUrl  die CMIS AtomPUB Binding Teil der URL
     */
    public AlfrescoConnector(String user, String password, String server, String bindingUrl)  {
        this.user = user;
        this.password = password;
        this.server = server;
        this.bindingUrl = bindingUrl;
        logger.info("Server: " + this.server);
        logger.info("Binding: " + this.bindingUrl);
        logger.info("User: " + this.user);
        logger.info("Password: " + this.password);
    }

    /**
     * liefert ein Ticket zur Authenfizierung
     * @return             das Ticket als JSON Objekt
     * @throws IOException
     */
    public JSONObject getTicket() throws IOException, JSONException{

        URL url = new URL(this.server + (this.server.endsWith("/") ? "" : "/") + LOGIN_URL);
        String urlParameters = "{ \"username\" : \"" + this.user + "\", \"password\" : \"" + this.password + "\" }";
        JSONObject obj = new JSONObject(startRequest(url, RequestType.POST, urlParameters));
        logger.info(("Ticket für User " + this.user + " und Passord " + this.password + " ausgestellt."));
        return obj;
    }

    /**
     * liefert die CMIS Session
     * @return  die CMIS Session
     */
    private Session getSession() throws VerteilungException {
        if (this.session != null)
          return this.session;
        else {
            try {
            this.session = initSession();
            } catch (Exception e) {
                String error = " Mit den Parametern Server: " + this.server + " Binding: " + this.bindingUrl + " User: " + this.user + " Password: " + this.password + " konnte keine Cmis Session etabliert werden!";
                throw new VerteilungException(error, e);
            }
            logger.fine(" Mit den Parametern Server: " + this.server + " Binding: " + this.bindingUrl + " User: " + this.user + " Password: " + this.password + " konnte eine Cmis Session erfolgreich etabliert werden!");
            return this.session;
        }
    }

    /**
     * initialisiert eine CMIS Session zum Alfresco
     * @return Session   die CMIS Session
     */
    private Session initSession()  {

        // CMISSession Generator aufbauen
        CMISSessionGenerator gen = new CMISSessionGenerator(this.user, this.password, this.bindingUrl, "Session");
        return gen.generateSession();
    }

    /**
     * Hilfsmethode, um den Content eines Dokumentes als String zu liefern
     *
     * @param stream                           der Stream
     * @return                                 der Inhalt des Dokumentes als String
     * @throws IOException
     */
    private static String getContentAsString(ContentStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (Reader reader = new InputStreamReader(stream.getStream(), "UTF-8")) {
            final char[] buffer = new char[4 * 1024];
            int b;
            while (true) {
                b = reader.read(buffer, 0, buffer.length);
                if (b > 0) {
                    sb.append(buffer, 0, b);
                } else if (b == -1) {
                    break;
                }
            }
        }

        return sb.toString();
    }


    /**
     * listet den Inhalt eines Folders
     * @param folderId              die Id des Folders
     * @param maxItemsPerPage       die maximale Anzahl
     * @param pagesToSkip           die Anzahl Seiten die übersprungen werden soll
     * @return                      die gefundenen Children
     */

    public ItemIterable<CmisObject> listFolder(String folderId, int maxItemsPerPage, int pagesToSkip) throws VerteilungException{
        CmisObject object = getSession().getObject(getSession().createObjectId(folderId));
        Folder folder = (Folder) object;
        OperationContext operationContext = getSession().createOperationContext();
        operationContext.setMaxItemsPerPage(maxItemsPerPage);

        ItemIterable<CmisObject> children = folder.getChildren(operationContext);
        return children.skipTo(pagesToSkip).getPage();
    }

    /**
     * listet den Inhalt eines Folders
     * @param folderId              die Id des Folders
     * @return                      eine Liste mit CmisObjekten
     */
    public ItemIterable<CmisObject> listFolder( String folderId) throws VerteilungException{
        return listFolder(folderId, 99999, 0);
    }

    /**
     * liefert einen Knotens
     * @param path      der Pfad zum Knoten
     * @return          der Knoten als CMISObject
     */
    public CmisObject getNode(String path) throws VerteilungException {
        try {
            return getSession().getObjectByPath(path);
        } catch (CmisObjectNotFoundException e) {
            return null;
        }
    }

    /**
     * sucht ein Objekt nach seiner ObjektId
     * @param  nodeId                die Id des Objektes
     * @return das CmisObject
     * @throws VerteilungException
     */
    public CmisObject getNodeById(String nodeId) throws VerteilungException {
        return getSession().getObject(getSession().createObjectId(nodeId));
    }

    /**
     * sucht Dokumente
     * @param queryString           die Abfragequery
     * @return                      eine Liste mit CmisObjekten
     */
    public List<CmisObject>  findDocument(String queryString) throws VerteilungException {
        List<CmisObject> erg = new ArrayList<CmisObject>();

        ItemIterable<QueryResult> results =  getSession().query(queryString, false);
        for (Iterator<QueryResult> iterator = results.iterator(); iterator.hasNext(); ) {
            QueryResult qResult = iterator.next();
            String objectId = qResult.getPropertyValueByQueryName("cmis:objectId");
            erg.add(getSession().getObject(getSession().createObjectId(objectId)));
        }
        return erg;
    }

    /**
     * liefert den Inhalt eines Dokumentes
     * @param document              das Dokument
     * @return                      der Inhalt als Bytearray
     * @throws IOException
     */
    public byte[] getDocumentContent(Document document)  throws IOException{
        byte fileBytes[] = null;

        fileBytes = IOUtils.toByteArray(document.getContentStream().getStream());
        return fileBytes;
    }

    /**
     * liefert den Inhalt eines Dokumentes als String
     * @param document              das Dokument
     * @return                      der Inhalt als String
     * @throws IOException
     */
    public String getDocumentContentAsString(Document document)  throws IOException{
        return getContentAsString(document.getContentStream());
    }

    /**
     * lädt eine Document hoch
     * @param folder                Der Folder, in den das Dokument geladen werden soll
     * @param file                  Die Datei, die hochgeladen werden soll
     * @param typ                   Der Typ der Datei
     * @param versioningState       der Versionsstatus @see VersioningState
     * @return                      die Id des neuen Documentes als String
     * @throws IOException
     */
    public String uploadDocument(Folder folder,
                                 File file,
                                 String typ,
                                 VersioningState versioningState) throws IOException, VerteilungException {

        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        byte[] bytes = new byte[(int) file.length()];
        dis.readFully(bytes);

        Map<String, String> newDocProps = new HashMap<String, String>();
        newDocProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        newDocProps.put(PropertyIds.NAME, file.getName());

        List<Ace> addAces = new LinkedList<>();
        List<Ace> removeAces = new LinkedList<>();
        List<Policy> policies = new LinkedList<>();
        ContentStream contentStream = new ContentStreamImpl(file.getAbsolutePath(), null, "application/pdf",
                new ByteArrayInputStream(bytes));
        Document doc = folder.createDocument(newDocProps, contentStream,
                versioningState, policies, removeAces, addAces, getSession().getDefaultContext());
        return doc.getId();
    }

    /**
     * erzeugt ein neues Dokument
     * @param parentFolder              der Folder, in dem das Dokument angelegt werden soll
     * @param documentName              der Name des neuen Dokumentes
     * @param documentContent           der Inhalt des Dokumentes
     * @param documentType              der Typ des Dokumentes
     * @param extraCMSProperties        zusätzliche Properties
     * @param versioningState           der Versionsstatus @see VersioningState
     * @return newDocument              das neue Dokument
     */
    public Document createDocument(Folder parentFolder,
                                   String documentName,
                                   byte documentContent[],
                                   String documentType,
                                   Map<String, Object> extraCMSProperties,
                                   VersioningState versioningState) throws VerteilungException {

        logger.fine("Create Document: " + documentName + " Type: " + documentType + " in Folder " + parentFolder.getName() + " Version: " + versioningState.value());
        Document newDocument;

        Map<String, Object> properties = buildProperties(extraCMSProperties);
        properties.put(PropertyIds.NAME, documentName);

        InputStream stream = new ByteArrayInputStream(documentContent);
        ContentStream contentStream = new ContentStreamImpl(documentName, BigInteger.valueOf(documentContent.length), documentType, stream);

        newDocument = parentFolder.createDocument(properties, contentStream, versioningState);

        return newDocument;
    }



    /**
     * verschiebt ein Dokument
     * @param document              das zu verschiebende Dokument
     * @param oldFolder             der alte Folder in dem das Dokument liegt
     * @param newFolder             der Folder, in das Dokument verschoben werden soll
     *
     * @return                     das verschobene Dokument
     */
    public CmisObject moveDocument(Document document,
                             Folder oldFolder,
                             Folder newFolder) {

         return document.move(oldFolder, newFolder);
    }

    /**
     * erstellt einen Folder
     *
     * @param targetFolder              der Folder, in dem der neue Folder angelegt werden soll.
     * @param extraCMSProperties        Map mit den Properties
     * @return                          der neue Folder
     */
    public Folder createFolder(Folder targetFolder,
                               Map<String, Object> extraCMSProperties ) throws VerteilungException {

        Map<String, Object> properties = buildProperties(extraCMSProperties);
        if (!properties.containsKey(PropertyIds.OBJECT_TYPE_ID))
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        return targetFolder.createFolder(properties);
    }

    /**
     * aktualisiert den Inhalt eines Dokumentes
     * @param  document                  das zu aktualisierende Dokument
     * @param  documentContent           der neue Inhalt. Falls der Content <null> ist, dann werden nur die Properties upgedated.
     * @param  documentType              der Typ des Dokumentes
     * @param  extraCMSProperties        zusätzliche Properties
     * @param  versionState              bestimmt die Versionierung @seeVersionState
     * @param  versionComment            falls Dokuemnt versionierbar, dann kann hier eine Kommentar zur Version mitgegeben werden
     * @return document                  das geänderte Dokument
     */
    public Document updateDocument(Document document,
                                   byte documentContent[],
                                   String documentType,
                                   Map<String, Object> extraCMSProperties,
                                   VersioningState versionState,
                                   String versionComment) throws VerteilungException {

        ContentStream contentStream = null;

        if (documentContent != null) {
            InputStream stream = new ByteArrayInputStream(documentContent);
            contentStream = new ContentStreamImpl(document.getName(), BigInteger.valueOf(documentContent.length), documentType, stream);
        }
        Map<String, Object> properties = null;

        if (extraCMSProperties != null)
            properties = buildProperties(extraCMSProperties);

        ObjectId id = null;
        if (versionState.equals(VersioningState.MAJOR) || versionState.equals(VersioningState.MINOR)) {
            id = checkOutDocument(document);
            if (id != null) {
                if (extraCMSProperties != null && extraCMSProperties.size() > 0) {
                    id = createAspectsFromProperties(extraCMSProperties, (Document) session.getObject(id));
                }
                if (properties != null && properties.size() > 0) {
                    session.getObject(id).updateProperties(properties, true);
                }
                id = ((Document) session.getObject(id)).checkIn(versionState.equals(VersioningState.MAJOR), properties, contentStream, versionComment);
            } else {
                if (contentStream != null) {
                    id = document.setContentStream(contentStream, true, true);
                    session.clear();
                }
                id = createAspectsFromProperties(extraCMSProperties, id == null ? document : (Document) session.getObject(id));
                id = (Document) session.getObject(id).updateProperties(properties, true);
            }
        } else {
            if (contentStream != null) {
                id = document.setContentStream(contentStream, true, true);
                session.clear();
            }
            id = createAspectsFromProperties(extraCMSProperties, id == null ? document : (Document) session.getObject(id));
            id = ((Document) session.getObject(id)).updateProperties(properties, true);


        }
        return (Document) session.getObject(id);
    }

    /**
     * aktualisiert die Metadaten eines Dokumentes
     * @param  object                    das zu aktualisierende Objekt
     * @param  extraCMSProperties        zusätzliche Properties
     * @return CmisObject                das geänderte Objekt
     */
    public CmisObject updateProperties(CmisObject object,
                                     Map<String, Object> extraCMSProperties) throws VerteilungException {


        Map<String, Object> properties = null;
        ObjectId id = null;
        if (extraCMSProperties != null) {

            properties = buildProperties(extraCMSProperties);
        }
        if (extraCMSProperties != null && extraCMSProperties.size() > 0)
            id = createAspectsFromProperties(extraCMSProperties, object);
        id = session.getObject(id).updateProperties(properties, true);

        return session.getObject(id);
    }

    /**
     * prüft, ob ein Dokument versionierbar ist
     * @param  doc                       das Document
     * @return                           true, wenn das Document versionierbar ist
     */
    public boolean isDocumentVersionable(Document doc) {
     return (((DocumentType)(doc.getType())).isVersionable());
    }

    /**
     * checked ein Dokument aus
     * @param  document                 das auszucheckende Dokument
     * @return objectId                 die Id des Objectes, oder null falls es nicht auszuchecken ist.
     */
    public ObjectId checkOutDocument(Document document) {
        if (isDocumentVersionable(document))
            return document.checkOut();
        else
            return null;
    }

    /**
     * liefert die Kommentare zu einem Knoten
     * @param obj           der Knoten/Folder als Cmis Objekt
     * @param ticket        das Ticket zur Identifizierung
     * @return              ein JSON Objekt mit den Kommentaren
     * @throws IOException
     * @throws JSONException
     */
    public JSONObject getComments(CmisObject obj, String ticket) throws IOException, JSONException {

        String id = VerteilungHelper.normalizeObjectId(obj.getId());
        URL url = new URL(this.server + (this.server.endsWith("/") ? "" : "/") + NODES_URL + id + "/comments?alf_ticket=" +ticket);
        return new JSONObject(startRequest(url, RequestType.GET, null));
    }

    /**
     * fügt einen Kommentar hinzu
     * @param obj           der Knoten/Folder als Cmis Objekt
     * @param ticket        das Ticket zur Identifizierung
     * @param comment       der neue Kommentar
     * @return              ein JSON Objekt mit dem neuen Kommentar
     * @throws IOException
     * @throws JSONException
     */
    public JSONObject addComment(CmisObject obj, String ticket, String comment) throws IOException, JSONException {
        String id = VerteilungHelper.normalizeObjectId(obj.getId());
        URL url = new URL(this.server + (this.server.endsWith("/") ? "" : "/") + NODES_URL + id + "/comments?alf_ticket=" +ticket);
        String urlParameters = "{\"content\": \"" + comment + "\"}";
        return new JSONObject(startRequest(url, RequestType.POST, urlParameters));
    }

    /**
     * baut die Properties für Alfresco auf.
     * @param  extraCMSProperties   die übergebenen Properties
     * @return properties           die für Alfresco aufgearbeiteten Properties
     */
    private Map<String, Object> buildProperties(Map<String, Object> extraCMSProperties) throws VerteilungException {
        Map<String, Object> properties = new HashMap<>();
        if (extraCMSProperties != null) {

            for (String key : extraCMSProperties.keySet()) {
                if (! key.isEmpty()) {
                    // hier werden die Typen concateniert. WICHTIG: Die Typ Deklarationen 'D:...' müssen vor den Properties 'P:...' sein.
                    properties.put(PropertyIds.OBJECT_TYPE_ID, properties.containsKey(PropertyIds.OBJECT_TYPE_ID) ? key.toUpperCase().startsWith("D:") ? key + "," + properties.get(PropertyIds.OBJECT_TYPE_ID) : properties.get(PropertyIds.OBJECT_TYPE_ID) + "," + key : key);
                    properties.putAll(convertProperties((Map<String, Object>) extraCMSProperties.get(key), key));

                } else
                    properties.putAll(convertProperties((Map<String, Object>) extraCMSProperties.get(key), key));
            }
        }

        if (!properties.containsKey(PropertyIds.OBJECT_TYPE_ID))
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        else if (!((String) properties.get(PropertyIds.OBJECT_TYPE_ID)).toUpperCase().startsWith("D:"))
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document," + properties.get(PropertyIds.OBJECT_TYPE_ID));


        return properties;
    }

    /**
     * bereitet die Typen der Properties auf
     * @param properties  die Property Werte
     * @param type        der verwendete Typ
     * @return            die Properties mit den richtigen Typen
     */
    private Map<String, Object> convertProperties(Map<String, Object> properties,
                                                         String type)  {
        HashMap<String, Object> props = new HashMap<>();
        Map<String, PropertyDefinition<?>> definitions = this.session.getTypeDefinition(type).getPropertyDefinitions();
        for (String key : properties.keySet()) {
            PropertyDefinition<?> definition = definitions.get(key);
            //TODO Hier fehlt noch das parsen auf die anderen Datentypen
            if (definition instanceof PropertyDateTimeDefinition) {
                if (properties.get(key) instanceof Long) {
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(new Date((Long) properties.get(key)));
                    props.put(key, gc);
                }
            } else {
                props.put(key, properties.get(key));
            }
        }
        return props;
    }

    private ObjectId createAspectsFromProperties(Map<String, Object> Properties,
                                                 CmisObject obj) {
        ObjectId id = null;
        for (String key : Properties.keySet()) {
            if (!key.isEmpty() && key.startsWith("P:")) {
                if (obj instanceof Folder)
                    id = ((AlfrescoFolder) obj).addAspect(key);
                if (obj instanceof Document)
                    id = ((AlfrescoDocument) obj).addAspect(key);
                obj =  session.getObject(id);
                obj.refresh();
            }
        }
        return id;
    }

    /**
     * startet einen Http Request
     * @param url               die aufzurufende URL
     * @param type              der Typ des Aufrufs, entweder POST oder GET
     * @param urlParameters     die Parameter für den Aufruf
     * @return                  den Response als String
     * @throws IOException
     */
    private String startRequest(URL url, RequestType type, String urlParameters) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(type.getName());

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            if (urlParameters != null) {
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();
            }
            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
