package de.schulte.testverteilung;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.math.BigInteger;
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

    private static Logger logger = Logger.getLogger(AlfrescoConnector.class.getName());

    private String user = null;
    private String password = null;
    private String bindingUrl = null;


    private Session session = null;

    /**
     * Konstruktor
     * @param user        der Username
     * @param password    das Passwort
     * @param bindingUrl  die CMIS AtomPUB BindingURL
     */
    public AlfrescoConnector(String user, String password, String bindingUrl)  {
        this.user = user;
        this.password = password;
        this.bindingUrl = bindingUrl;
        logger.info("URL: " + this.bindingUrl);
        logger.info("User: " + this.user);
        logger.info("Password: " + this.password);
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
                String error = " Mit den Parametern Server: " + this.bindingUrl + " User: " + this.user + " Password: " + this.password + " konnte keine Cmis Session etabliert werden!";
                throw new VerteilungException(error, e);
            }
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
     * liefert ein Dokument
     * @param queryString           die Abfragequery
     * @return                      ein Document
     */
    public Document findDocument(String queryString) throws VerteilungException {

        ItemIterable<QueryResult> results = getSession().query(queryString, false);

        for (Iterator<QueryResult> iterator = results.iterator(); iterator.hasNext(); ) {
            QueryResult qResult = iterator.next();
            String objectId = qResult.getPropertyValueByQueryName("cmis:objectId");
            return (Document) getSession().getObject(getSession().createObjectId(objectId));

        }
        return null;
    }

    /**
     * liefert den Inhalt eines Dokumentes
     * @param documentId            die Id des Dokumentes
     * @throws IOException
     * @return                      der Inhalt als Bytearray
     */
    public byte[] getDocumentContent(String documentId) throws VerteilungException, IOException {
        CmisObject object = getSession().getObject(getSession().createObjectId(documentId));
        Document document = (Document) object;

        return getDocumentContent(document);
    }

    /**
     * liefert den Inhalt eines Dokumentes als String
     * @param documentId            die Id des Dokumentes
     * @throws IOException
     * @return                      der Inhalt als String
     */
    public String getDocumentContentAsString(String documentId) throws VerteilungException, IOException {
        CmisObject object = getSession().getObject(getSession().createObjectId(documentId));
        Document document = (Document) object;

        return getDocumentContentAsString(document);
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
     * @return                      die Id des neuen Documentes als String
     * @throws IOException
     */
    public String uploadDocument(Folder folder, File file, String typ) throws IOException, VerteilungException {

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
                VersioningState.NONE, policies, removeAces, addAces, getSession().getDefaultContext());
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
                                   VersioningState versioningState) {

        Document newDocument;

        Map<String, Object> properties = new HashMap<>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, documentName);

        if (extraCMSProperties != null) {
            for (String key : extraCMSProperties.keySet())
                properties.put(key, extraCMSProperties.get(key));
        }
        InputStream stream = new ByteArrayInputStream(documentContent);
        ContentStream contentStream = new ContentStreamImpl(documentName, BigInteger.valueOf(documentContent.length), documentType, stream);

        // create a major version
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
     * @param newFolderName             der Name des neuen Folder
     * @return                          der neue Folder
     */
    public Folder createFolder(Folder targetFolder, String newFolderName) {
        Map<String, String> props = new HashMap<>();
        props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        props.put(PropertyIds.NAME, newFolderName);
        return targetFolder.createFolder(props);
    }

    /**
     * aktualisiert den Inhalt eines Dokumentes
     * @param  document                  das zu aktualisierende Dokument
     * @param  documentContent           der neue Inhalt
     * @param  documentType              der Typ des Dokumentes
     * @param  extraCMSProperties        zusätzliche Properties
     * @param  majorVersion              falls Dokument versionierbar, dann wird eine neue Major-Version erzeugt, falls true
     * @param  versionComment            falls Dokuemnt versionierbar, dann kann hier eine Kommentar zur Version mitgegeben werden
     * @return document                  das geänderte Dokument
     */
    public Document updateDocument(Document document,
                                   byte documentContent[],
                                   String documentType,
                                   Map<String, Object> extraCMSProperties,
                                   boolean majorVersion,
                                   String versionComment){


        HashMap<String, Object> properties = new HashMap<>();
        InputStream stream = new ByteArrayInputStream(documentContent);
        ContentStream contentStream = new ContentStreamImpl(document.getName(), BigInteger.valueOf(documentContent.length), documentType, stream);

        if (extraCMSProperties != null) {
            for (String key : extraCMSProperties.keySet())
                properties.put(key, extraCMSProperties.get(key));
        }
        ObjectId id = checkOutDocument(document) ;
        if (id != null) {
            document = (Document) session.getObject(id);
            id = document.checkIn(majorVersion, properties, contentStream, versionComment);
        }
        else
            id = document.setContentStream(contentStream, true, true);
        return (Document) session.getObject(id);
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
     * @return objectId                 die Id des Objectes, oder null falss es nicht auszuchecken ist.
     */
    public ObjectId checkOutDocument(Document document) {
        if (isDocumentVersionable(document))
            return document.checkOut();
        else
            return null;
    }

}