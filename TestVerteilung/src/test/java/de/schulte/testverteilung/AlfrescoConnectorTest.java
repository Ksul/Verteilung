package de.schulte.testverteilung;

import org.alfresco.cmis.client.AlfrescoAspects;
import org.alfresco.cmis.client.AlfrescoDocument;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 09.01.14
 * Time: 13:06
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoConnectorTest extends AlfrescoTest{

    private AlfrescoConnector con;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        con = new AlfrescoConnector(properties.getProperty("user"), properties.getProperty("password"), properties.getProperty("server"), properties.getProperty("bindingUrl"));
        assertThat(con, Matchers.notNullValue());
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
    public void testGetTicket() throws Exception {
        JSONObject ticket = con.getTicket();
        assertThat(ticket, Matchers.notNullValue());
        assertThat(((JSONObject) ticket.get("data")).getString("ticket"), Matchers.startsWith("TICKET_"));
    }

    @Test
    public void testListFolder() throws Exception {

        ItemIterable<CmisObject> list = con.listFolder(con.getNode("/Archiv").getId());
        assertThat(list.getTotalNumItems(), Matchers.is(4L));
        list = con.listFolder(con.getNode("/Archiv/Fehler").getId());
        int count = 0;
        for (CmisObject obj : list) {
            if (obj instanceof Folder)
                count++;
        }
        assertThat(count, Matchers.is(1));
    }


    @Test
    public void testGetNode() throws Exception {
        CmisObject cmisObject;
        cmisObject = con.getNode("/Archiv");
        assertThat(cmisObject, Matchers.notNullValue());
        assertThat(cmisObject, Matchers.instanceOf(Folder.class));
        cmisObject = con.getNode("/Archiv/Fehler");
        assertThat(cmisObject, Matchers.notNullValue());
        assertThat(cmisObject, Matchers.instanceOf(Folder.class));
        cmisObject = con.getNode("/Archiv/Unbekannt");
        assertThat(cmisObject, Matchers.notNullValue());
        assertThat(cmisObject, Matchers.instanceOf(Folder.class));
        cmisObject = con.getNode("/Datenverzeichnis/Skripte/recognition.js");
        assertThat(cmisObject, Matchers.notNullValue());
        assertThat(cmisObject, Matchers.instanceOf(Document.class));
    }

    @Test
    public void testFindDocument() throws Exception{
        List<CmisObject> erg = con.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'");
        assertThat(erg, Matchers.notNullValue());
        assertThat(erg.size(), Matchers.is(1));
        Document doc = (Document) erg.get(0);
        assertThat(doc.getName(), Matchers.equalTo("doc.xml"));
    }

    @Test
    public void testGetDocumentContent() throws Exception{
        byte[] content = con.getDocumentContent((Document) con.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'").get(0));
        assertThat(content, Matchers.notNullValue());
        assertThat(content.length, Matchers.greaterThan(0));
        String document =  new String(content, Charset.forName("UTF-8"));
        assertThat(document, Matchers.startsWith("<documentTypes"));
        assertThat(document, Matchers.containsString("xmlns:my=\"http://www.schulte.local/archiv\""));
    }

    @Test
    public void testUploadDocument() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        String id = con.uploadDocument(((Folder) folder), new File(System.getProperty("user.dir") + properties.getProperty("testPDF")), "application/pdf", VersioningState.MINOR);
        assertThat(id, Matchers.notNullValue());
        CmisObject document = con.getNode("/Archiv/Test.pdf");
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
        document.delete(true);
    }

    @Test
    public void testCreateDocument() throws Exception {
        CmisObject folder = con.getNode("/");
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> titledMap = new HashMap<>();
        titledMap.put("cm:description","Testdokument");
        properties.put("P:cm:titled", titledMap);
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR);
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
        assertThat(document.getName(), Matchers.equalTo("TestDocument.txt"));
        assertThat(IOUtils.toString(document.getContentStream().getStream(), "UTF-8"), Matchers.equalTo("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?"));
        document.delete(true);
    }

    @Test
    public void testCreateArchivDocument() throws Exception {
        CmisObject folder = con.getNode("/");
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> titledMap = new HashMap<>();
        Map<String, Object> amountMap = new HashMap<>();
        Map<String, Object> archivModelMap = new HashMap<>();
        titledMap.put("cm:description","Testdokument");
        amountMap.put("my:amount","25.33");
        archivModelMap.put("my:person", "Klaus");
        archivModelMap.put("my:documentDate", new Date().getTime());
       // properties.put(PropertyIds.OBJECT_TYPE_ID, "D:my:archivContent");
        properties.put("P:cm:titled", titledMap);
        properties.put("P:my:amountable", amountMap);
        properties.put("D:my:archivContent", archivModelMap);
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR);
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
        assertThat(document.getName(), Matchers.equalTo("TestDocument.txt"));
        assertThat(IOUtils.toString(document.getContentStream().getStream(), "UTF-8"), Matchers.equalTo("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?"));
        document.delete(true);
    }

    @Test
    public void testUpdateProperties() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        String content = "";
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> archivModelMap = new HashMap<>();
        archivModelMap.put("my:person", "Klaus");
        archivModelMap.put("my:documentDate", new Date().getTime());
        properties.put("D:my:archivContent", archivModelMap);
        //Achtung: Wenn hier das Dokument noch nicht auf den Typ my:archivContent gesetzt würde, dann ist das mit dem nachfolgenden Update nicht mehr zu ändern.
        // Wird im Alfresco eine Regel verwendet, die den Typ automatisch setzt, so muss das Dokument neu gelesen werden, denn die Rückgabe des create wird nicht automatisch aktualisiert
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR);
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
        assertThat(document.getName(), Matchers.equalTo("TestDocument.txt"));
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
        document = (Document) con.updateProperties(document, properties);
        assertThat(document, Matchers.notNullValue());
        assertThat(((AlfrescoDocument) document).hasAspect("P:cm:titled"), Matchers.is(true));
        assertThat(((BigDecimal) document.getProperty("my:amount").getValue()).doubleValue(), Matchers.equalTo(new BigDecimal(25.33).doubleValue()));
        assertThat((boolean) document.getProperty("my:tax").getValue(), Matchers.is(true));
        document.delete(true);
    }

    @Test
    public void testUpdateDocument() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        String content = "";
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> archivModelMap = new HashMap<>();
        archivModelMap.put("my:person", "Klaus");
        archivModelMap.put("my:documentDate", new Date().getTime());
        properties.put("D:my:archivContent", archivModelMap);
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR);   // NONE führt zu einem Fehler
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
        assertThat(document.getName(), Matchers.equalTo("TestDocument.txt"));
        assertThat(document.getVersionLabel(), Matchers.equalTo("0.1"));
        content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        properties = new HashMap<>();
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("my:amount","24.33");
        properties.put("P:my:amountable", amountMap);
        document = con.updateDocument(document, content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR, null);
        byte[] cont = con.getDocumentContent(document );
        assertThat(cont, Matchers.notNullValue());
        assertThat(cont, Matchers.instanceOf(byte[].class));
        assertThat(content, Matchers.equalTo(new String(cont)));
        // wegen einer zusätzlichen Version durch die Aspekte
        assertThat(document.getVersionLabel(), Matchers.equalTo("0.3"));
        properties = new HashMap<>();
        amountMap = new HashMap<>();
        amountMap.put("my:amount","23.33");
        properties.put("P:my:amountable", amountMap);

        document = con.updateDocument(document, content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MINOR, null);
        // wegen einer zusätzlichen Version durch die Aspekte
        assertThat(document.getVersionLabel(), Matchers.equalTo("0.4"));
        assertThat(((BigDecimal) document.getProperty("my:amount").getValue()).doubleValue(), Matchers.equalTo(new BigDecimal(23.33).doubleValue()));
        document.delete(true);

        document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MAJOR);
        assertThat(document.getVersionLabel(), Matchers.equalTo("1.0"));
        assertThat(document.getCheckinComment(), Matchers.equalTo("Initial Version"));
        content = "Dies ist ein neuer Inhalt";

        document = con.updateDocument(document, content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MAJOR, "neuer Versionskommentar");
        cont = con.getDocumentContent(document );
        assertThat(cont, Matchers.notNullValue());
        assertThat(cont, Matchers.instanceOf(byte[].class));
        assertThat(content, Matchers.equalTo(new String(cont)));
        assertThat(document.getVersionLabel(), Matchers.equalTo("2.0"));
        assertThat(document.getCheckinComment(), Matchers.equalTo("neuer Versionskommentar"));
        properties = new HashMap<>();
        Map<String, Object> titledMap = new HashMap<>();
        Map<String, Object> emailMap = new HashMap<>();
        amountMap = new HashMap<>();
        titledMap.put("cm:description","Testdokument");
        amountMap.put("my:amount","25.33");
        long time =  new Date().getTime();
        emailMap.put("cm:sentdate", time);
        properties.put("P:cm:titled", titledMap);
        properties.put("P:my:amountable", amountMap);
        properties.put("P:cm:emailed", emailMap);

        document = con.updateDocument(document, null, CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MAJOR, "2. Versionskommentar");
        assertThat(document, Matchers.notNullValue());
        assertThat(document.getVersionLabel(), Matchers.equalTo("3.0"));
        assertThat(document.getCheckinComment(), Matchers.equalTo("2. Versionskommentar"));
        assertThat(((AlfrescoAspects) document).hasAspect(("P:my:amountable")), Matchers.is(true));
        assertThat(((BigDecimal) document.getProperty("my:amount").getValue()).doubleValue(), Matchers.equalTo(new BigDecimal(25.33).doubleValue()));
        assertThat(((AlfrescoAspects) document).hasAspect(("P:cm:emailed")), Matchers.is(true));
        assertThat(((GregorianCalendar) document.getProperty("cm:sentdate").getValue()).getTime().getTime(), Matchers.equalTo(time));
        assertThat(document.getProperty("cm:description").getValueAsString(), Matchers.equalTo("Testdokument"));
        cont = con.getDocumentContent(document );
        assertThat(cont, Matchers.notNullValue());
        assertThat(cont, Matchers.instanceOf(byte[].class));
        assertThat(content, Matchers.equalTo(new String(cont)));

        amountMap.put("my:amount","");
        emailMap.put("cm:sentdate", "");
        document = con.updateDocument(document, null, CMISConstants.DOCUMENT_TYPE_TEXT, properties, VersioningState.MAJOR, "3. Versionskommentar");
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf(AlfrescoAspects.class));
        assertThat(((AlfrescoAspects) document).hasAspect(("P:my:amountable")), Matchers.is(false));
        assertThat(((AlfrescoAspects) document).hasAspect(("P:cm:emailed")), Matchers.is(false));
        assertThat(document.getVersionLabel(), Matchers.equalTo("4.0"));

        document.delete(true);
    }

    @Test
    public void testMoveNode() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR);
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
        assertThat(document.getName(), Matchers.equalTo("TestDocument.txt"));
        CmisObject newFolder = con.getNode("/Archiv/Fehler");
        assertThat(newFolder, Matchers.notNullValue());
        assertThat(newFolder, Matchers.instanceOf( Folder.class));
        CmisObject cmisObject = con.moveNode(document, (Folder) folder, (Folder) newFolder);
        assertThat(cmisObject, Matchers.notNullValue());
        assertThat(cmisObject, Matchers.instanceOf( Document.class));
        assertThat(cmisObject.getName(), Matchers.equalTo("TestDocument.txt"));
        CmisObject obj = con.getNode("/Archiv/Fehler/TestDocument.txt");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj, Matchers.instanceOf( Document.class));
        assertThat(obj.getName(), Matchers.equalTo("TestDocument.txt"));
        cmisObject.delete(true);
    }

    @Test
    public void testCreateFolder() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        Map<String, Object> props = new HashMap<>();
        Map<String, Object> standard = new HashMap<>();
        standard.put(PropertyIds.NAME, "TestFolder");
        props.put("cmis:folder", standard);
        folder = con.createFolder((Folder) folder, props);
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        assertThat(folder.getName(), Matchers.equalTo("TestFolder"));
        ((Folder) folder).deleteTree(true, UnfileObject.DELETE, true);
    }

    @Test
    public void testGetComments() throws Exception {
        CmisObject folder = con.getNode("/Archiv");
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        Document document = con.createDocument((Folder) folder, "TestDocument.txt", content.getBytes(), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR);
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
         assertThat(document.getName(), Matchers.equalTo("TestDocument.txt"));
        JSONObject ticket = con.getTicket();
        assertThat(ticket, Matchers.notNullValue());
        JSONObject abd = con.addComment(document, ((JSONObject) ticket.get("data")).getString("ticket"), "Testkommentar");
        assertThat(abd, Matchers.notNullValue());
        JSONObject result =  con.getComments(document, ((JSONObject) ticket.get("data")).getString("ticket"));
        assertThat(((JSONObject) ((JSONArray) result.get("items")).get(0)).getString("content"), Matchers.equalTo("Testkommentar"));
        document.delete(true);
    }

}
