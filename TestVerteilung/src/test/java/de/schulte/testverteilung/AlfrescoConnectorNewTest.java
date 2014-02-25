package de.schulte.testverteilung;

import junit.framework.Assert;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

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
public class AlfrescoConnectorNewTest extends AlfrescoTest{

    AlfrescoConnectorNew con;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        con = new AlfrescoConnectorNew("admin", properties.getProperty("password"), properties.getProperty("bindingUrl") );
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
        CmisObject document = con.getNode("/Archiv/Test.pdf");
        if (document != null && document instanceof Document)
            document.delete(true);
        String id = con.uploadDocument(((Folder) folder), new File(properties.getProperty("testFile")), "application/pdf");
        assertNotNull(id);
        document = con.getNode("/Archiv/Test.pdf");
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
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null);
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
        CmisObject cmisObject = con.getNode("/Archiv/TestDocument.txt");
        if (cmisObject != null && cmisObject instanceof Document)
            cmisObject.delete(true);
        String content = "";
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null);
        assertNotNull(document);
        assertTrue(document instanceof Document);
        assertEquals("TestDocument.txt", document.getName());
        content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        con.updateDocument(document, content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT);
        byte[] cont = con.getDocumentContent(document );
        assertNotNull(cont);
        assertTrue(cont instanceof byte[]);
        assertEquals(content, new String(cont));
        document.delete(true);
    }

    @Test
    public void TestMoveDocument() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null);
        assertNotNull(document);
        assertTrue(document instanceof Document);
        assertEquals("TestDocument.txt", document.getName());
        CmisObject newFolder = con.getNode("/Archiv/Fehler");
        assertNotNull(newFolder);
        assertTrue(newFolder instanceof Folder);
        con.moveDocument(document, (Folder) folder, (Folder) newFolder);
        CmisObject cmisObject = con.getNode("/Archiv/Fehler/TestDocument.txt");
        assertNotNull(cmisObject);
        assertTrue(cmisObject instanceof Document);
        cmisObject.delete(true);
    }

    @Test
    public void testCreateFolder() throws Exception {
        CmisObject folder = con.getNode("/Archiv/TestFolder");
        if (folder != null && folder instanceof Folder)
            ((Folder) folder).deleteTree(true, UnfileObject.DELETE, true);
        folder = con.getNode("/Archiv");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        folder = con.createFolder((Folder) folder, "TestFolder");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        assertEquals("TestFolder", ((Folder) folder).getName());
        ((Folder) folder).deleteTree(true, UnfileObject.DELETE, true);
    }



}
