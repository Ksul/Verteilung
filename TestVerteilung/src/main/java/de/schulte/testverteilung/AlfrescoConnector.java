package de.schulte.testverteilung;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.util.AbstractPageFetcher;
import org.apache.chemistry.opencmis.client.runtime.util.CollectionIterable;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.definitions.PropertyBooleanDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDateTimeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDecimalDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    private static Logger logger = LoggerFactory.getLogger(AlfrescoConnector.class.getName());
    private String user = null;
    private String password = null;
    private String binding = null;
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

    private class IdHelper implements ObjectId {
        String id;

        public IdHelper(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }

    private Session session = null;

    /**
     * Konstruktor
     * @param user        der Username
     * @param password    das Password
     * @param server      die Server URL
     * @param binding     die CMIS AtomPUB Binding Teil der URL
     */
    public AlfrescoConnector(String user, String password, String server, String binding)  {
        this.user = user;
        this.password = password;
        this.server = server;
        this.binding = binding;
        this.session = null;
    }

    /**
     * Konstruktor
     */
    public AlfrescoConnector() {}

    /*
    liefert den User
     */
    public String getUser() {
        return user;
    }

    /**
     * liefert das Password
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * liefert das Binding
      * @return
     */
    public String getBinding() {
        return binding;
    }

    /**
     * liefert den Server
     * @return
     */
    public String getServer() {
        return server;
    }

    /**
     * liefert ein Ticket zur Authenfizierung
     * @return             das Ticket als JSON Objekt
     * @throws IOException
     */
    public JSONObject getTicket() throws IOException, JSONException{

        return getTicket(this.user, this.password, this.server);
    }

    /**
     * liefert ein Ticket zur Authentifizierung
     * @param user         der Name des Users
     * @param password     das Password
     * @param server       der Alfresco Server
     * @return             das Ticket als JSON Objekt
     * @throws IOException
     */
    public JSONObject getTicket(String user, String password, String server) throws IOException, JSONException{
        URL url = new URL(server + (server.endsWith("/") ? "" : "/") + LOGIN_URL);
        String urlParameters = "{ \"username\" : \"" + user + "\", \"password\" : \"" + password + "\" }";
        JSONObject obj = new JSONObject(startRequest(url, RequestType.POST, urlParameters));
        logger.trace("Ticket für User " + user + " und Password " + password + " ausgestellt.");
        return obj;
    }

    /**
     * liefert die CMIS Session
     * @return  die CMIS Session
     */
    protected Session getSession() {
        if (this.session != null)
          return this.session;
        else {
            try {
            this.session = initSession();
            } catch (Exception e) {
                logger.error(" Mit den Parametern Server: " + this.server + " Binding: " + this.binding + " User: " + this.user + " Password: " + this.password + " konnte keine Cmis Session etabliert werden!");
                return null;
            }
            logger.trace(" Mit den Parametern Server: " + this.server + " Binding: " + this.binding + " User: " + this.user + " Password: " + this.password + " konnte eine Cmis Session erfolgreich etabliert werden!");
            return this.session;
        }
    }

    /**
     * gibt Auskunft ob eine Alfresco Verbindung besteht
     * @return
     */
    public boolean isConnected(){
        return getSession() != null;
    }

    /**
     * initialisiert eine CMIS Session zum Alfresco
     * @return Session   die CMIS Session
     */
    private Session initSession()  {

        // CMISSession Generator aufbauen
        CMISSessionGenerator gen = new CMISSessionGenerator(this.user, this.password, this.binding, "Session");
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
     * @param order                 Die Spalte nach der sortiuert werden soll
     * @param orderDirection        die Sortierreihenfolge: ASC oder DESC
     * @param modus                 was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @return                      die gefundenen Children
     */

    public ItemIterable<CmisObject> listFolder(String folderId,
                                               String order,
                                               String orderDirection,
                                               int modus) throws VerteilungException{

        ItemIterable<CmisObject> result = null;

        OperationContext operationContext = getSession().createOperationContext();
        operationContext.setIncludeAllowableActions(false);
        operationContext.setIncludePolicies(false);
        operationContext.setIncludeAcls(false);
        switch (modus) {
            case VerteilungConstants.LIST_MODUS_ALL: {  // Dokumente und Folder
                operationContext.setOrderBy(order + " " + orderDirection);
                CmisObject object = getSession().getObject(getSession().createObjectId(folderId));
                Folder folder = (Folder) object;
                result = folder.getChildren(operationContext);
                break;
            }
            case VerteilungConstants.LIST_MODUS_DOCUMENTS:{  // nur Dokumente
                StringBuilder query = new StringBuilder("select d.*, o.*, c.*, i.* from my:archivContent as d " +
                                                            "join cm:titled as o on d.cmis:objectId = o.cmis:objectId " +
                                                            "join my:amountable as c on d.cmis:objectId = c.cmis:objectId " +
                                                            "join my:idable as i on d.cmis:objectId = i.cmis:objectId " +
                                                            "WHERE IN_FOLDER(d, ?) ");

                if (order != null && order.trim().length() > 0) {
                    query.append(" ORDER BY ");
                    query.append(order + " ");
                    if (orderDirection != null && orderDirection.trim().length() > 0)
                        query.append(orderDirection);
                }
                QueryStatement stmt = session.createQueryStatement(query.toString());
                stmt.setString(1, folderId);
                result = getCmisObjects(stmt, operationContext);

                break;
            }
            case VerteilungConstants.LIST_MODUS_FOLDER: { // nur Folder
                StringBuilder query = new StringBuilder("select d.*, o.* from cmis:folder as d " +
                                                            "join cm:titled as o on d.cmis:objectId = o.cmis:objectId " +
                                                            "WHERE IN_FOLDER(d, ?) AND d.cmis:objectTypeId<>'F:cm:systemfolder'");

                if (order != null && order.trim().length() > 0) {
                    query.append(" ORDER BY ");
                    query.append(order + " ");
                    if (orderDirection != null && orderDirection.trim().length() > 0)
                        query.append(orderDirection);
                }
                QueryStatement stmt = session.createQueryStatement(query.toString());
                stmt.setString(1, folderId);
                result = getCmisObjects(stmt, operationContext);

                break;
            }
        }

        return result;
    }

    /**
     * führt die Query aus
     * @param stmt                      die Such Query
     * @param operationContext          der Operation Context
     * @return                          eine Liste mit Cmis Objekten
     */
    public ItemIterable<CmisObject> getCmisObjects(QueryStatement stmt,
                                                   final OperationContext operationContext) {
        ItemIterable<CmisObject> result;

        ItemIterable<QueryResult> cntResult = session.query(stmt.toString(), false, operationContext).skipTo(Long.MAX_VALUE).getPage(Integer.MAX_VALUE);
        final long totalNumItems = cntResult.getTotalNumItems();
        final DiscoveryService discoveryService = getSession().getBinding().getDiscoveryService();
        final ObjectFactory of = getSession().getObjectFactory();

        result = new CollectionIterable<CmisObject>(new AbstractPageFetcher<CmisObject>(operationContext.getMaxItemsPerPage()) {

            @Override
            protected Page<CmisObject> fetchPage(long skipCount) {
                // fetch the data
                ObjectList resultList = discoveryService.query(getSession().getRepositoryInfo().getId(), stmt.toString(),
                        false, operationContext.isIncludeAllowableActions(), operationContext.getIncludeRelationships(),
                        operationContext.getRenditionFilterString(), BigInteger.valueOf(this.maxNumItems),
                        BigInteger.valueOf(skipCount), null);

                // convert query results
                List<CmisObject> page = new ArrayList<CmisObject>();
                if (resultList.getObjects() != null) {
                    for (ObjectData objectData : resultList.getObjects()) {
                        if (objectData == null) {
                            continue;
                        }

                        page.add(of.convertObject(objectData, operationContext));
                    }
                }

                return new Page<CmisObject>(page, totalNumItems,
                        resultList.hasMoreItems());
            }
        });
        return result;
    }


    /**
     * liefert einen Knotens
     * @param path      der Pfad zum Knoten
     * @return          der Knoten als CMISObject
     */
    public CmisObject getNode(String path) throws VerteilungException {
        try {
            CmisObject cmisObject = getSession().getObjectByPath(path);
            logger.trace("getNode with " + path + " found " + cmisObject.getId());
            return cmisObject;
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
        CmisObject cmisObject = getSession().getObject(getSession().createObjectId(nodeId));
        logger.trace("getNodeById with " + nodeId + " found " + cmisObject.getId());
        return cmisObject;
    }


    /**
     * sucht Dokumente
     * @param queryString           die Abfragequery
     * @param order                 Die Spalte nach der sortiuert werden soll
     * @param orderDirection        die Sortierreihenfolge: ASC oder DESC
     * @return                      die gefundenen Dokumente
     * //TODO Das hier unterstüzt keine Aliase ala SELECT * from cmis:document AS D!!!
     */
    public ItemIterable<CmisObject> findDocument(String queryString,
                                         String order,
                                         String orderDirection) throws VerteilungException {

        OperationContext operationContext = getSession().createOperationContext();
        operationContext.setOrderBy(order + " " + orderDirection);
        operationContext.setIncludeAllowableActions(false);
        operationContext.setIncludePolicies(false);
        operationContext.setIncludeAcls(false);

        StringBuilder query = new StringBuilder(queryString);
        if (order != null && order.trim().length() > 0) {
            query.append(" ORDER BY ");
            query.append(order + " ");
            if (orderDirection != null && orderDirection.trim().length() > 0)
                query.append(orderDirection);
        }
        QueryStatement stmt = session.createQueryStatement(query.toString());

        return getCmisObjects(stmt, operationContext);

    }

    /**
     * führt eine Query durch
     * @param query   der Select als String
     * @return        eine Liste mit den jeweiligen Properties
     * @throws VerteilungException
     */
    public List<List<PropertyData<?>>> query(String query) throws VerteilungException{

        List<List<PropertyData<?>>> erg = new ArrayList<>();
        ItemIterable<QueryResult> results =  getSession().query(query, false);
        for (Iterator<QueryResult> iterator = results.iterator(); iterator.hasNext(); ) {
            QueryResult qResult = iterator.next();
            erg.add(qResult.getProperties());
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
     * @param properties                die Properties
     * @param versioningState           der Versionsstatus @see VersioningState
     * @return newDocument              das neue Dokument
     */
    public Document createDocument(Folder parentFolder,
                                   String documentName,
                                   byte documentContent[],
                                   String documentType,
                                   Map<String, Object> properties,
                                   VersioningState versioningState) throws VerteilungException {

        logger.trace("Create Document: " + documentName + " Type: " + documentType + " in Folder " + parentFolder.getName() + " Version: " + versioningState.value());

        Document newDocument;

        if (documentContent == null)
            throw new IllegalArgumentException("Content darf nicht null sein!");

        if (properties == null)
            properties = new HashMap<>();

        if (!properties.containsKey(PropertyIds.OBJECT_TYPE_ID))
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        else if (!((String) properties.get(PropertyIds.OBJECT_TYPE_ID)).toUpperCase().startsWith("D:") && !((String) properties.get(PropertyIds.OBJECT_TYPE_ID)).toLowerCase().contains("cmis:document"))
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document," + properties.get(PropertyIds.OBJECT_TYPE_ID));

        properties.put(PropertyIds.NAME, documentName);

        InputStream stream = new ByteArrayInputStream(documentContent);
        ContentStream contentStream = new ContentStreamImpl(documentName, BigInteger.valueOf(documentContent.length), documentType, stream);

        newDocument = parentFolder.createDocument(convertProperties(properties), contentStream, versioningState);

        return newDocument;
    }



    /**
     * verschiebt ein Dokument
     * @param fileableCmisObject    der zu verschiebende Knoten
     * @param oldFolder             der alte Folder in dem der Knoten liegt
     * @param newFolder             der Folder, in das der Knoten verschoben werden soll
     *
     * @return                     der verschobene Knoten
     */
    public FileableCmisObject moveNode(FileableCmisObject fileableCmisObject,
                               Folder oldFolder,
                               Folder newFolder) {

        FileableCmisObject object = fileableCmisObject.move(oldFolder, newFolder);
        logger.trace("Object " + fileableCmisObject.getId() + " moved from " + oldFolder.getId() + " to folder " + newFolder.getId());
        return object;
    }



    /**
     * erstellt einen Folder
     *
     * @param targetFolder              der Folder, in dem der neue Folder angelegt werden soll.
     * @param properties                Map mit den Properties
     * @return                          der neue Folder
     */
    public Folder createFolder(Folder targetFolder,
                               Map<String, Object> properties ) throws VerteilungException {

        logger.trace("createFolder: " + targetFolder.getPath() + " Properties: " + properties);

        if (!properties.containsKey(PropertyIds.OBJECT_TYPE_ID))
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        else if (!((String) properties.get(PropertyIds.OBJECT_TYPE_ID)).toUpperCase().startsWith("D:") && !((String) properties.get(PropertyIds.OBJECT_TYPE_ID)).toLowerCase().contains("cmis:folder"))
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder," + properties.get(PropertyIds.OBJECT_TYPE_ID));

        return targetFolder.createFolder(properties);
    }

    /**
     * füllt den Inhalt eines Dokumentes
     * @param document          das Dokument
     * @param contentStream     der Contentstream mit dem neuen Inhalt
     * @param overwrite         legt fest, ob der Inhalt überschrieben werden soll
     * @param refresh           legt fest, ob das Dokument nach der Operation gefresht werden soll
     * @return
     */
    public CmisObject setContent(Document document,  ContentStream contentStream, boolean overwrite, boolean refresh){
        return session.getObject(document.setContentStream(contentStream, overwrite, refresh));
    }

    /**
     * aktualisiert den Inhalt eines Dokumentes
     * @param  document                  das zu aktualisierende Dokument
     * @param  documentContent           der neue Inhalt. Falls der Content <null> ist, dann werden nur die Properties upgedated.
     * @param  documentType              der Typ des Dokumentes
     * @param  properties                die Properties
     * @param  versionState              bestimmt die Versionierung @seeVersionState
     * @param  versionComment            falls Dokument versionierbar, dann kann hier eine Kommentar zur Version mitgegeben werden
     * @return document                  das geänderte Dokument
     */
    public Document updateDocument(Document document,
                                   byte documentContent[],
                                   String documentType,
                                   Map<String, Object> properties,
                                   VersioningState versionState,
                                   String versionComment) throws VerteilungException {

        ContentStream contentStream = null;
        List<String> asp = null;

        if (documentContent != null) {
            InputStream stream = new ByteArrayInputStream(documentContent);
            contentStream = new ContentStreamImpl(document.getName(), BigInteger.valueOf(documentContent.length), documentType, stream);
        }

        CmisObject obj = document;

        properties = convertProperties(properties);

        asp = (List<String>) properties.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);

        //Aspekte hinzufügen
        if (properties != null && properties.size() > 0 ) {
            obj.updateProperties(properties, asp, null, true);
            properties.clear();
        }

        if (versionState.equals(VersioningState.MAJOR) || versionState.equals(VersioningState.MINOR)) {

            obj = checkOutDocument((Document) obj);
            if (obj != null) {

                obj = checkInDocument((Document) obj, versionState.equals(VersioningState.MAJOR), properties, contentStream, versionComment);
                session.clear();

            } else {

                if (contentStream != null) {
                    obj = setContent(document, contentStream, true, true);
                    session.clear();
                }

                obj = session.getObject(obj.updateProperties(properties, true));
            }
        } else {
            // Update ohne Versionierung (das funktioniert wohl nur genau einmal denn Alfresco lässt bei Documenten mit Versionierung kein
            // Update ohnen diese zu.
            obj = document;
            if (contentStream != null) {
                obj = setContent(document, contentStream, true, true);
                session.clear();
            }

            if (properties != null && properties.size() > 0)
                obj = session.getObject(obj.updateProperties(properties, true));


        }
        return (Document) obj;
    }

    /**
     * aktualisiert die Metadaten eines Dokumentes
     * @param  obj                       das zu aktualisierende Objekt
     * @param  properties                die Properties
     * @return CmisObject                das geänderte Objekt
     */

    public CmisObject updateProperties(CmisObject obj,
                                       Map<String, Object> properties) throws VerteilungException {

        if (properties != null && properties.size() == 0)
                 throw new IllegalArgumentException("keine Properties zum Updaten!");
        if (!properties.containsKey(PropertyIds.OBJECT_TYPE_ID))
            properties.put(PropertyIds.OBJECT_TYPE_ID, obj.getPropertyValue(PropertyIds.OBJECT_TYPE_ID));

        obj = session.getObject(obj.updateProperties(convertProperties(properties), true));
        logger.trace("updateProperties for node " + obj.getId());
        return obj;
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
     * @return obj                      das Objekt, oder null falls es nicht auszuchecken ist.
     */
    public CmisObject checkOutDocument(Document document) {
        if (isDocumentVersionable(document)) {
            CmisObject cmisObject = session.getObject(document.checkOut());
            logger.trace("Object " + cmisObject.getId() + " checked out!");
            return cmisObject;
        }
        else
            return null;
    }

    /**
     * checked ein Dokument ein
     * @param document          das einzucheckende Dokument
     * @param major             Major Version
     * @param properties        die properties zum Dokument
     * @param contentStream     der Content des Dokumentes
     * @param checkinComment    der Versionskommentar
     * @return                  das eingecheckte Dokument
     */
    public CmisObject checkInDocument(Document document,
                                      boolean major,
                                      Map<String, Object> properties,
                                      ContentStream contentStream,
                                      String checkinComment) {

        if (isDocumentVersionable(document)) {
            CmisObject cmisObject = session.getObject(document.checkIn(major, convertProperties(properties), contentStream, checkinComment));
            logger.trace("Object " + cmisObject.getId() + " checked in with Version " + cmisObject.getPropertyValue("versionLabel"));
            return cmisObject;
        }
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
     * bereitet die Typen der Properties auf
     * @param properties  die Property Werte
     * @return            die Properties mit den richtigen Typen
     */
    private Map<String, Object> convertProperties(Map<String, Object> properties) {


        HashMap<String, Object> props = new HashMap<>();
        Map<String, PropertyDefinition<?>> definitions = new HashMap<>();
        List<String> types = new ArrayList<>();
        if (properties!= null && properties.size() > 0) {
            // Typens suchen
            types.add((String) properties.get(PropertyIds.OBJECT_TYPE_ID));
            if (properties.containsKey(PropertyIds.SECONDARY_OBJECT_TYPE_IDS))
                types.addAll((List<String>) properties.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS));
            for (String type : types) {
                definitions.putAll(this.session.getTypeDefinition(type).getPropertyDefinitions());
            }

            for (String key : properties.keySet()) {
                PropertyDefinition<?> definition = definitions.get(key);
                //TODO Hier fehlt eventuell noch das parsen auf die anderen Datentypen
                if (definition instanceof PropertyDateTimeDefinition) {
                    Date date = new Date();
                    if (properties.get(key) instanceof Long)
                        date.setTime((Long) properties.get(key));
                    else if (properties.get(key) instanceof String)
                        date.setTime(Long.parseLong((String) properties.get(key)));
                    props.put(key, date);
                } else if (definition instanceof PropertyDecimalDefinition) {
                    if (properties.get(key) instanceof String)
                        props.put(key, ((String) properties.get(key)).isEmpty() ? 0 : Double.parseDouble((String) properties.get(key)));
                    if (properties.get(key) instanceof BigDecimal || properties.get(key) instanceof Double || properties.get(key) instanceof Float || properties.get(key) instanceof Byte || properties.get(key) instanceof Short || properties.get(key) instanceof Integer || properties.get(key) instanceof Long)
                        props.put(key, properties.get(key));
                } else if (definition instanceof PropertyBooleanDefinition) {
                    if (properties.get(key) instanceof String)
                        props.put(key, Boolean.parseBoolean((String) properties.get(key)));
                    if (properties.get(key) instanceof Boolean)
                        props.put(key, properties.get(key));
                } else {
                    props.put(key, properties.get(key));
                }
            }
        }
        return props;
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
