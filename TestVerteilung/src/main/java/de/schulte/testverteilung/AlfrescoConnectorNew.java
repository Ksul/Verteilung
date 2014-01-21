package de.schulte.testverteilung;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.commons.io.IOUtils;
import java.io.IOException;



/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 09.01.14
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoConnectorNew {

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
        this.session = initSession();
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

    public ItemIterable<CmisObject> listFolder(String folderId, int maxItemsPerPage, int pagesToSkip){
        CmisObject object = session.getObject(session.createObjectId(folderId));
        Folder folder = (Folder) object;
        OperationContext operationContext = session.createOperationContext();
        operationContext.setMaxItemsPerPage(maxItemsPerPage);

        ItemIterable<CmisObject> children = folder.getChildren(operationContext);
        ItemIterable<CmisObject> page = children.skipTo(pagesToSkip).getPage();

        return page;
    }

    /**
     * listet den Inhalt eines Folders
     * @param folderId              die Id des Folders
     * @return
     */
    public ItemIterable<CmisObject> listFolder( String folderId){
        return listFolder(folderId, 99999, 0);
    }

    /**
     * liefert die ID eines Knotens
     * @param path      der Pfad zum Knoten
     * @return
     */
    public String getNodeId(String path){
        CmisObject object = session.getObjectByPath(path);
        return object.getId();
    }

    /**
     * liefert ein Dokument
     * @param queryString           die Abfragequery
     * @return
     */
    public Document findDocument(String queryString){

        ItemIterable<QueryResult> results = session.query(queryString, false);

        for (QueryResult qResult : results) {
                 String objectId = qResult.getPropertyValueByQueryName("cmis:objectId");
            return (Document) session.getObject(session.createObjectId(objectId));

        }
        return null;
    }

    /**
     * liefert den Inhalt eines Dokumentes
     * @param documentId            die Id des Dokumentes
     * @return
     */
    public byte[] getDocumentContents( String documentId){
        CmisObject object = session.getObject(session.createObjectId(documentId));
        Document document = (Document) object;

        return getDocumentContents(document);
    }

    /**
     * liefert den Inhalt eines Dokumentes
     * @param document              das Dokument
     * @return
     */
    public byte[] getDocumentContents(Document document){
        byte fileBytes[] = null;

        try{
            fileBytes = IOUtils.toByteArray(document.getContentStream().getStream());
        } catch (IOException ioe){
            // TODO: throw some sort of exception & log
            ;
        }

        return fileBytes;
    }
}
