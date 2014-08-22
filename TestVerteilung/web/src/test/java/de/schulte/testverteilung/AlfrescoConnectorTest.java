package de.schulte.testverteilung;

import junit.framework.Assert;
import org.alfresco.cmis.client.AlfrescoDocument;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

        if (cmisObject != null && cmisObject instanceof AlfrescoDocument) {
            if (((AlfrescoDocument) cmisObject).isVersionSeriesCheckedOut())
                ((AlfrescoDocument) cmisObject).cancelCheckOut();
            cmisObject.delete(true);
        }
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

        ItemIterable<CmisObject> list = con.listFolder(con.getNode("/Archiv").getId());
        Assert.assertEquals(4, list.getTotalNumItems());
        list = con.listFolder(con.getNode("/Archiv/Fehler").getId());
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
        String id = con.uploadDocument(((Folder) folder), new File(System.getProperty("user.dir") + properties.getProperty("testPDF")), "application/pdf", VersioningState.MINOR);
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
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> titledMap = new HashMap<>();
        Map<String, Object> amountMap = new HashMap<>();
        Map<String, Object> archivModelMap = new HashMap<>();
        titledMap.put("cm:description","Testdokument");
        amountMap.put("my:amount","25.33");
        archivModelMap.put("my:person", "Klaus");
        archivModelMap.put("my:documentDate", new Date().toGMTString());
        properties.put("P:cm:titled", titledMap);
        properties.put("P:my:amountable", amountMap);
        properties.put("D:my:archivContent", archivModelMap);
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR);
        assertNotNull(document);
        assertTrue(document instanceof Document);
        assertEquals("TestDocument.txt", document.getName());
        assertEquals("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?", IOUtils.toString(document.getContentStream().getStream(), "UTF-8"));
        document.delete(true);
    }

    @Test
    public void testUpdateProperties() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        String content = "";
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> archivModelMap = new HashMap<>();
        archivModelMap.put("my:person", "Klaus");
        archivModelMap.put("my:documentDate", new Date().toGMTString());
        properties.put("D:my:archivContent", archivModelMap);
        //Achtung: Wenn hier das Dokument noch nicht auf den Typ my:archivContent gesetzt würde, dann ist das mit dem nachfolgenden Update nicht mehr zu ändern.
        // Wird im Alfresco eine Regel verwendet, die den Typ automatisch setzt, so muss das Dokument neu gelesen werden, denn die Rückgabe des create wird nicht automatisch aktualisiert
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR);
        assertNotNull(document);
        assertTrue(document instanceof Document);
        assertEquals("TestDocument.txt", document.getName());
        properties = new HashMap<>();
        Map<String, Object> titledMap = new HashMap<>();
        Map<String, Object> standardMap = new HashMap<>();
        Map<String, Object> amountMap = new HashMap<>();
        titledMap.put("cm:description","Testdokument");
        amountMap.put("my:amount","25.33");
        amountMap.put("my:tax","true");
        properties.put("P:cm:titled", titledMap);
        properties.put("P:my:amountable", amountMap);
        properties.put("cmis:document", standardMap);
        document = con.updateProperties(document, properties);
        assertNotNull(document);
        // Die Überprüfung auf Aspekte funktioniert nicht
        //assertTrue(((AlfrescoDocument) document).hasAspect("P:cm:titled"));
        assertEquals(new BigDecimal(25.33).doubleValue(), ((BigDecimal) document.getProperty("my:amount").getValue()).doubleValue(), 0);
        assertTrue((boolean) document.getProperty("my:tax").getValue());
        document.delete(true);
    }

    @Test
    public void testUpdateDocument() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        String content = "";
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> archivModelMap = new HashMap<>();
        archivModelMap.put("my:person", "Klaus");
        archivModelMap.put("my:documentDate", new Date().toGMTString());
        properties.put("D:my:archivContent", archivModelMap);

        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR);   // NONE führt zu einem Fehler
        assertNotNull(document);
        assertTrue(document instanceof Document);
        assertEquals("TestDocument.txt", document.getName());
        content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        properties = new HashMap<>();
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("my:amount","24.33");
        properties.put("P:my:amountable", amountMap);

        document = con.updateDocument(document, content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR, null);
        byte[] cont = con.getDocumentContent(document );
        assertNotNull(cont);
        assertTrue(cont instanceof byte[]);
        assertEquals(content, new String(cont));
        assertEquals("0.2", document.getVersionLabel());
        properties = new HashMap<>();
        amountMap = new HashMap<>();
        amountMap.put("my:amount","23.33");
        properties.put("P:my:amountable", amountMap);

        document = con.updateDocument(document, content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR, null);
        assertEquals("0.3", document.getVersionLabel());
        assertEquals(new BigDecimal(23.33).doubleValue(), ((BigDecimal) document.getProperty("my:amount").getValue()).doubleValue(), 0);
        document.delete(true);

        document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MAJOR);
        assertEquals("1.0", document.getVersionLabel());
        assertEquals("Initial Version", document.getCheckinComment());
        content = "Dies ist ein neuer Inhalt";

        document = con.updateDocument(document, content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MAJOR, "neuer Versionskommentar");
        cont = con.getDocumentContent(document );
        assertNotNull(cont);
        assertTrue(cont instanceof byte[]);
        assertEquals(content, new String(cont));
        assertEquals("2.0", document.getVersionLabel());
        assertEquals("neuer Versionskommentar", document.getCheckinComment());
        properties = new HashMap<>();
        Map<String, Object> titledMap = new HashMap<>();
        Map<String, Object> standardMap = new HashMap<>();
        amountMap = new HashMap<>();
        titledMap.put("cm:description","Testdokument");
        amountMap.put("my:amount","25.33");
        properties.put("P:cm:titled", titledMap);
        properties.put("P:my:amountable", amountMap);
        properties.put("cmis:document", standardMap);

        document = con.updateDocument(document, null, CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MAJOR, "2. Versionskommentar");
        assertNotNull(document);
        assertEquals("3.0", document.getVersionLabel());
        assertEquals("2. Versionskommentar", document.getCheckinComment());
        assertEquals(new BigDecimal(25.33).doubleValue(), ((BigDecimal) document.getProperty("my:amount").getValue()).doubleValue(), 0);
        assertEquals("Testdokument", document.getProperty("cm:description").getValueAsString());
        cont = con.getDocumentContent(document );
        assertNotNull(cont);
        assertTrue(cont instanceof byte[]);
        assertEquals(content, new String(cont));
        document.delete(true);
    }

    @Test
    public void testMoveDocument() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertNotNull(folder);
        assertTrue(folder instanceof Folder);
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR);
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
        assertEquals("TestFolder", folder.getName());
        ((Folder) folder).deleteTree(true, UnfileObject.DELETE, true);
    }



}
