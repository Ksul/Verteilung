package de.schulte.testverteilung;

import junit.framework.Assert;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

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

        ItemIterable<CmisObject> list = con.listFolder(con.getNodeId("company_home/Archiv"));
        Assert.assertEquals(4, list.getTotalNumItems());
        list = con.listFolder(con.getNodeId("company_home/Archiv/Fehler"));
        int count = 0;
        Iterator<CmisObject> it = list.iterator();
        while (it.hasNext()){
            CmisObject obj = it.next();
            if (obj instanceof Folder)
              count++;
        }
        Assert.assertEquals(1, count);
    }


    @Test
    public void testGetNodeId() throws Exception {
        String id = null;
        id = con.getNodeId("company_home/Archiv");
        Assert.assertNotNull(id);
        id = null;
        id = con.getNodeId("company_home/Archiv/Fehler");
        Assert.assertNotNull(id);
        id = null;
        id = con.getNodeId("company_home/Archiv/Unbekannt");
        Assert.assertNotNull(id);
        id = con.getNodeId("company_home/dictionary/scripts/recognition.js");
        Assert.assertNotNull(id);
    }

    @Test
    public void testFindDocument() throws Exception{
        Document doc = con.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'");
        Assert.assertNotNull(doc);
        Assert.assertEquals("doc.xml", doc.getName());
    }

    @Test
    public void testGetDocumentContent() throws Exception{
        byte[] content = con.getDocumentContents(con.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'").getId());
        Assert.assertNotNull(content);
        Assert.assertTrue(content.length > 0);
        String document =  new String(content, Charset.forName("UTF-8"));
        Assert.assertTrue(document.startsWith("<documentTypes"));
        Assert.assertTrue(document.indexOf("xmlns:my=\"http://www.schulte.local/archiv\"") != -1);
    }

}
