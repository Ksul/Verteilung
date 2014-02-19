package de.schulte.testverteilung;

import junit.framework.Assert;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
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
        con = new AlfrescoConnectorNew("admin", password, host );
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
        URL url =  AlfrescoConnectorNewTest.class.getClassLoader().getResource("Test.pdf");
        assertNotNull(url);
        String id = con.uploadDocument(((Folder) folder), new File(url.toURI()), "application/pdf");
        assertNotNull(id);
        document = con.getNode("/Archiv/Test.pdf");
        assertNotNull(document);
        assertTrue(document instanceof Document);
        document.delete(true);
    }

}
