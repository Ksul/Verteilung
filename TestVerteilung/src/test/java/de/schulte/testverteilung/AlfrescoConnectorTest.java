package de.schulte.testverteilung;

import junit.framework.Assert;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import java.nio.charset.Charset;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 09.01.14
 * Time: 13:06
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoConnectorTest extends AlfrescoTest{

    AlfrescoConnector con;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        con = new AlfrescoConnector("admin", properties.getProperty("password"), properties.getProperty("bindingUrl") );
        assertNotNull(con);
        CmisObject cmisObject = con.getNode("/Archiv/TestDocument.txt");
        if (cmisObject != null && cmisObject instanceof Document)
            cmisObject.delete(true);
        cmisObject = con.getNode("/Archiv/Fehler/TestDocument.txt");
        if (cmisObject != null && cmisObject instanceof Document)
            cmisObject.delete(true);
        cmisObject = con.getNode("/Archiv/Test.pdf");
        if (cmisObject != null && cmisObject instanceof Document)
            cmisObject.delete(true);
        cmisObject = con.getNode("/Archiv/TestFolder");
        if (cmisObject != null && cmisObject instanceof Folder)
            ((Folder) cmisObject).deleteTree(true, UnfileObject.DELETE, true);
    }


    @Test
    public void testListFolder() throws Exception {

        ItemIterable<CmisObject> list = con.listFolder(con.getNode("company_home/Archiv").getId());
        Assert.assertEquals(4, list.getTotalNumItems());
        list = con.listFolder(con.getNode("company_home/Archiv/Fehler").getId());
        int count = 0;
        Iterator<CmisObject> it = list.iterator();
        while (it.hasNext()){
            CmisObject obj = it.next();
            if (obj instanceof Folder)
              count++;
        }
        assertEquals(1, count);
    }


    @Test
    public void testGetNode() throws Exception {
        CmisObject cmisObject = null;
        cmisObject = con.getNode("/Archiv");
        assertNotNull(cmisObject);
        assertTrue(cmisObject instanceof Folder);
        cmisObject = con.getNode("/Archiv/Fehler");
        assertNotNull(cmisObject);
        assertTrue(cmisObject instanceof Folder);
        cmisObject = con.getNode("/Archiv/Unbekannt");
        assertNotNull(cmisObject);
        assertTrue(cmisObject instanceof Folder);
        cmisObject = con.getNode("/Datenverzeichnis/Skripte/recognition.js");
        assertNotNull(cmisObject);
        assertTrue(cmisObject instanceof Document);
    }

    @Test
    public void testFindDocument() throws Exception{
        Document doc = con.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'");
        assertNotNull(doc);
        assertEquals("doc.xml", doc.getName());
    }

    @Test
    public void testGetDocumentContent() throws Exception{
        byte[] content = con.getDocumentContent(con.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'").getId());
        assertNotNull(content);
        assertTrue(content.length > 0);
        String document =  new String(content, Charset.forName("UTF-8"));
        assertTrue(document.startsWith("<documentTypes"));
        assertTrue(document.indexOf("xmlns:my=\"http://www.schulte.local/archiv\"") != -1);
    }

    @Test
    public void testUploadDocument() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        String id = con.uploadDocument(((Folder) folder), new File(properties.getProperty("testPDF")), "application/pdf");
        assertNotNull(id);
        CmisObject document = con.getNode("/Archiv/Test.pdf");
        assertNotNull(document);
        assertTrue(document instanceof Document);
        document.delete(true);
    }

    @Test
    public void testCreateDocument() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.NONE);
        assertNotNull(document);
        assertTrue(document instanceof Document);
        assertEquals("TestDocument.txt", document.getName());
        assertEquals("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?", IOUtils.toString(document.getContentStream().getStream(), "UTF-8"));
        document.delete(true);
    }

    @Test
    public void testUpdateDocument() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        String content = "";
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.NONE);
        assertNotNull(document);
        assertTrue(document instanceof Document);
        assertEquals("TestDocument.txt", document.getName());
        content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        con.updateDocument(document, content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, false, null);
        byte[] cont = con.getDocumentContent(document );
        assertNotNull(cont);
        assertTrue(cont instanceof byte[]);
        assertEquals(content, new String(cont));
        document.delete(true);
        document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MAJOR);
        assertEquals("1.0", document.getVersionLabel());
        assertEquals("Initial Version", document.getCheckinComment());
        content = "Dies ist ein neuer Inhalt";
        document = con.updateDocument(document, content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, true, "neuer Versionskommentar");
        cont = con.getDocumentContent(document );
        assertNotNull(cont);
        assertTrue(cont instanceof byte[]);
        assertEquals(content, new String(cont));
        assertEquals("2.0", document.getVersionLabel());
        assertEquals("neuer Versionskommentar", document.getCheckinComment());
        document.delete(true);
    }

    @Test
    public void testMoveDocument() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.NONE);
        assertNotNull(document);
        assertTrue(document instanceof Document);
        assertEquals("TestDocument.txt", document.getName());
        CmisObject newFolder = con.getNode("/Archiv/Fehler");
        assertNotNull(newFolder);
        assertTrue(newFolder instanceof Folder);
        CmisObject cmisObject = con.moveDocument(document, (Folder) folder, (Folder) newFolder);
        assertNotNull(cmisObject);
        assertTrue(cmisObject instanceof Document);
        assertEquals("TestDocument.txt", cmisObject.getName());
        CmisObject obj = con.getNode("/Archiv/Fehler/TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj instanceof Document);
        assertEquals("TestDocument.txt", obj.getName());
        cmisObject.delete(true);
    }

    @Test
    public void testCreateFolder() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        folder = con.createFolder((Folder) folder, "TestFolder");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        assertEquals("TestFolder", ((Folder) folder).getName());
        ((Folder) folder).deleteTree(true, UnfileObject.DELETE, true);
    }



}
