package de.schulte.testverteilung;

import org.alfresco.cmis.client.AlfrescoDocument;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 09.01.14
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoConnectorNew {

    private static Logger logger = Logger.getLogger(AlfrescoConnectorNew.class.getName());

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
    public AlfrescoConnectorNew(String user, String password, String bindingUrl)  {
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
     * listet den Inhalt eines Folders
     * @param folderId              die Id des Folders
     * @param maxItemsPerPage       die maximale Anzahl
     * @param pagesToSkip           die Anzahl Seiten die übersprungen werden soll
     * @return
     */

    public ItemIterable<CmisObject> listFolder(String folderId, int maxItemsPerPage, int pagesToSkip) throws VerteilungException{
        CmisObject object = getSession().getObject(getSession().createObjectId(folderId));
        Folder folder = (Folder) object;
        OperationContext operationContext = getSession().createOperationContext();
        operationContext.setMaxItemsPerPage(maxItemsPerPage);

        ItemIterable<CmisObject> children = folder.getChildren(operationContext);
        ItemIterable<CmisObject> page = children.skipTo(pagesToSkip).getPage();

        return page;
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
            CmisObject object = getSession().getObjectByPath(path);
            return object;
        } catch (CmisObjectNotFoundException e) {
            return null;
        }
    }

    /**
     * liefert ein Dokument
     * @param queryString           die Abfragequery
     * @return                      ein AlfrescoDocument
     */
    public AlfrescoDocument findDocument(String queryString) throws VerteilungException {

        ItemIterable<QueryResult> results = getSession().query(queryString, false);

        for (QueryResult qResult : results) {
                 String objectId = qResult.getPropertyValueByQueryName("cmis:objectId");
            return (AlfrescoDocument) getSession().getObject(getSession().createObjectId(objectId));

        }
        return null;
    }

    /**
     * liefert den Inhalt eines Dokumentes
     * @param documentId            die Id des Dokumentes
     * @return                      der Inhalt als Bytearray
     */
    public byte[] getDocumentContent( String documentId) throws VerteilungException {
        CmisObject object = getSession().getObject(getSession().createObjectId(documentId));
        Document document = (Document) object;

        return getDocumentContent(document);
    }

    /**
     * liefert den Inhalt eines Dokumentes
     * @param document              das Dokument
     * @return                      der Inhalt als Bytearray
     */
    public byte[] getDocumentContent(Document document) {
        byte fileBytes[] = null;

        try{
            fileBytes = IOUtils.toByteArray(document.getContentStream().getStream());
        } catch (IOException ioe){
            // TODO: throw some sort of exception & log
            ;
        }

        return fileBytes;
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

        Map newDocProps = new HashMap();
        newDocProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        newDocProps.put(PropertyIds.NAME, file.getName());

        List addAces = new LinkedList();
        List removeAces = new LinkedList();
        List policies = new LinkedList();
        ContentStream contentStream = new ContentStreamImpl(file.getAbsolutePath(), null, "application/pdf",
                new ByteArrayInputStream(bytes));
        Document doc = folder.createDocument(newDocProps, contentStream,
                VersioningState.NONE, policies, removeAces, addAces, getSession().getDefaultContext());
        return doc.getId();
    }

}
