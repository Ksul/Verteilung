package de.schulte.testverteilung;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.01.14
 * Time: 09:54
 * To change this template use File | Settings | File Templates.
 */
public class VerteilungServicesTest extends AlfrescoTest {

    VerteilungServices services;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        services = new VerteilungServices(properties.getProperty("server"), properties.getProperty("bindingUrl"), properties.getProperty("user"), properties.getProperty("password"));
        assertNotNull(services);
        services.deleteDocument(services.getNodeId("/Archiv/Test.pdf").getString("result"));
        services.deleteDocument(services.getNodeId("/Archiv/TestDocument.txt").getString("result"));
        services.deleteDocument(services.getNodeId("/Archiv/Fehler/TestDocument.txt").getString("result"));
        services.deleteDocument(services.getNodeId("/Archiv/Fehler/Test.pdf").getString("result"));
        services.deleteFolder(services.getNodeId("/Archiv/TestFolder").getString("result"));
    }

    @Test
    public void testGetTicket() throws Exception {
        JSONObject obj = services.getTicket();
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject ticket = (JSONObject) obj.get("result");
        assertTrue(((JSONObject) ticket.get("data")).getString("ticket").startsWith("TICKET_"));
    }

    @Test
    public void testT() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv/Unbekannt");
        String id = obj.getString("result");
        long t = System.currentTimeMillis();
        obj = services.listFolder(id, 0);
        System.out.println(System.currentTimeMillis() - t);
    }

    @Test
    public void testListFolder() throws Exception {
        JSONObject obj = services.listFolder("-1", 0);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertThat(obj.get("result"), instanceOf(JSONArray.class));
        assertThat(((JSONArray) obj.get("result")).length(), is(4));
        for (int i = 0; i < ((JSONArray) obj.get("result")).length(); i++) {
            assertThat(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("name"), anyOf(is("Dokumente"), is("Fehler"), is("Unbekannt"), is("Inbox")));
            if (((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("name").equalsIgnoreCase("Fehler"))
                assertTrue(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getBoolean("hasChildFolder"));
            assertThat(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("objectId"), notNullValue());
        }
        obj = services.uploadDocument(services.getNodeId("/Archiv/Fehler").getString("result"), System.getProperty("user.dir") + properties.getProperty("testPDF"), VersioningState.MINOR.value());
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.listFolder("-1", -1);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertTrue(obj.get("result") instanceof JSONArray);
        assertEquals(4, ((JSONArray) obj.get("result")).length());
        for (int i = 0; i < ((JSONArray) obj.get("result")).length(); i++) {
            assertThat(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("name"), anyOf(is("Dokumente"), is("Fehler"), is("Unbekannt"), is("Inbox")));
            assertThat(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("objectId"), notNullValue());
        }
        obj = services.getNodeId("/Archiv");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String id = obj.getString("result");
        obj = services.listFolder(id, 1);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertThat(obj.get("result"), instanceOf(JSONArray.class));
        assertThat(((JSONArray) obj.get("result")).length(), is(0));
        obj = services.listFolder(id, 0);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        for (int i = 0; i < ((JSONArray) obj.get("result")).length(); i++) {
            if (((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("name").equalsIgnoreCase("Fehler")) {
                assertTrue(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getBoolean("hasChildFolder"));
                assertTrue(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getBoolean("hasChildren"));
            }
        }
        assertEquals(4, ((JSONArray) obj.get("result")).length());
        obj = services.deleteDocument(services.getNodeId("/Archiv/Fehler/Test.pdf").getString("result"));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.listFolder(services.getNodeId("/Archiv/Inbox").getString("result"), 0);
        System.out.println(obj);
    }

    @Test
    public void testGetNodeID() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.getNodeId("/Datenverzeichnis/Skripte/");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.getNodeId("/Datenverzeichnis/Skripte/backup.js.sample");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testGetNode() throws Exception {
        JSONObject obj = services.getNode("/");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.getNode("/Datenverzeichnis/Skripte/");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.getNode("/Datenverzeichnis/Skripte/backup.js.sample");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testGetNodeByID() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.getNodeById(obj.getString("result"));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.getNodeId("/Datenverzeichnis/Skripte/backup.js.sample");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.getNodeById(obj.getString("result"));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testFindDocument() throws Exception {
        JSONObject obj1 = services.getNodeId("/Archiv");
        JSONObject obj = services.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='backup.js.sample'");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertThat(((JSONArray) obj.get("result")).length(), Matchers.greaterThanOrEqualTo(1));
        JSONObject result = (JSONObject) ((JSONArray) obj.get("result")).get(0);
        assertThat(result, notNullValue());
        assertThat(result.getString("name"), Matchers.equalToIgnoringCase("backup.js.sample"));
    }

    @Test
    public void testGetDocumentContent() throws Exception {
        JSONObject obj = services.getDocumentContent(services.getNodeId("/Datenverzeichnis/Skripte/doc.xml").getString("result"), false);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String document = obj.getString("result");
        assertThat(document.startsWith("<documentTypes"), is(true));
        assertThat(document.indexOf("xmlns:my=\"http://www.schulte.local/archiv\""), Matchers.not(-1));
    }

    @Test
    public void testUploadDocument() throws Exception {
        JSONObject obj = services.uploadDocument(services.getNodeId("/Archiv").getString("result"), System.getProperty("user.dir") + properties.getProperty("testPDF"), VersioningState.MINOR.value());
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.deleteDocument(services.getNodeId("/Archiv/Test.pdf").getString("result"));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testCreateDocument() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, notNullValue());
        assertThat(obj.length() >= 2, is(true));
        assertThat(obj.get("result"), notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), is(true));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}}";
        obj = services.createDocument(obj.getString("result"), "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MINOR.value());
        assertThat(obj, notNullValue());
        assertThat(obj.length() >= 2, is(true));
        assertThat(obj.get("result"), notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), is(true));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertThat(result, notNullValue());
        assertThat(result.getString("name").equalsIgnoreCase("TestDocument.txt"), is(true));
        obj = services.getDocumentContent(result.getString("objectId"), false);
        assertThat(obj, notNullValue());
        assertThat(obj.length() >= 2, is(true));
        assertThat(obj.get("result"), notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), is(true));
        String document = obj.getString("result");
        assertThat("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?", is(document));
        obj = services.deleteDocument(services.getNodeId("/TestDocument.txt").getString("result"));
        assertThat(obj, notNullValue());
        assertThat(obj.length() >= 2, is(true));
        assertThat(obj.get("result"), notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), is(true));
    }

    @Test
    public void testCreateDocumentWithCustomModel() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33'}, 'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.createDocument(obj.getString("result"), "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MINOR.value());
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertThat(result, notNullValue());
        assertTrue(result.getString("name").equalsIgnoreCase("TestDocument.txt"));
        obj = services.getDocumentContent(result.getString("objectId"), false);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String document = obj.getString("result");
        assertEquals("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?", document);
        obj = services.deleteDocument(services.getNodeId("/TestDocument.txt").getString("result"));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testCreateFolder() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String extraProperties = "{'cmis:folder':{'cmis:name': 'Testfolder'}, 'P:cm:titled':{'cm:title': 'Testtitel', 'cm:description':'Dies ist ein Test Folder'}}";
        obj = services.createFolder(obj.getString("result"), extraProperties);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertThat(result, notNullValue());
        assertTrue(result.getString("name").equalsIgnoreCase("TestFolder"));
        obj = services.deleteFolder(result.getString("objectId"));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testUpdateDocument() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String archivId = obj.getString("result");
        String content = "";
        obj = services.createDocument(archivId, "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR.value());
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject document = new JSONObject(obj.getString("result"));
        assertNotNull(document);
        assertTrue(document.getString("name").equalsIgnoreCase("TestDocument.txt"));
        assertNotNull(document.getString("objectId"));
        content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        obj = services.updateDocument(document.getString("objectId"), Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR.value(), null);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        document = new JSONObject(obj.getString("result"));
        obj = services.getDocumentContent(document.getString("objectId"), false);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertEquals(content, obj.getString("result"));
        obj = services.deleteDocument(document.getString("objectId"));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String extraProperties = "{'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.createDocument(archivId, "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MAJOR.value());
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        document = new JSONObject(obj.getString("result"));
        assertNotNull(document);
        assertEquals("1.0", document.getString("versionLabel"));
        assertEquals("Initial Version", document.getString("checkinComment"));
        content = "Dies ist ein neuer Inhalt";
        obj = services.updateDocument(document.getString("objectId"), Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MAJOR.value(), "neuer Versionskommentar");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertThat(result, notNullValue());
        assertEquals("2.0", result.getString("versionLabel"));
        assertEquals("neuer Versionskommentar", result.getString("checkinComment"));
        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33', 'my:tax':'true'}, 'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.updateDocument(result.getString("objectId"), null, CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MAJOR.value(), "2. Versionskommentar");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        result = new JSONObject(obj.getString("result"));
        assertThat(result, notNullValue());
        assertEquals("3.0", result.getString("versionLabel"));
        assertEquals("2. Versionskommentar", result.getString("checkinComment"));
        assertEquals("25.33", result.getString("amount"));
        assertTrue(result.getBoolean("tax"));
        obj = services.deleteDocument(document.getString("objectId"));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testChangeFolder() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv/Fehler");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String extraProperties = "{'cmis:folder':{'cmis:objectTypeId': 'cmis:folder','cmis:name': 'Fehler1'}, 'P:cm:titled': {'cm:title': 'Titel','cm:description': 'Beschreibung' }}";
        String id = obj.getString("result");
        obj = services.updateProperties(id, extraProperties);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        extraProperties = "{'cmis:folder':{'cmis:objectTypeId': 'cmis:folder','cmis:name': 'Fehler'}, 'P:cm:titled': {'cm:title': '','cm:description': '' }}";
        obj = services.updateProperties(id, extraProperties);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testUpdateProperties() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        String archivId = obj.getString("result");
        String content = "";
        String extraProperties = "{'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.createDocument(archivId, "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MINOR.value());
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject document = new JSONObject(obj.getString("result"));
        assertNotNull(document);
        assertTrue(document.getString("name").equalsIgnoreCase("TestDocument.txt"));
        assertNotNull(document.getString("objectID"));
        long time = new Date().getTime();
        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + time + "'}, 'P:my:amountable':{'my:amount':'25.33', 'my:tax':'true'}}";
        obj = services.updateProperties(document.getString("objectID"), extraProperties);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertThat(result, notNullValue());
        assertEquals("25.33", result.getString("amount"));
        assertThat(result.getBoolean("tax"), is(true));
        document = new JSONObject(obj.getString("result"));
        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + time + "'}, 'P:my:amountable':{'my:amount':'25.34', 'my:tax':'true'}}";
        obj = services.updateProperties(document.getString("objectID"), extraProperties);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        result = new JSONObject(obj.getString("result"));
        assertThat(result, notNullValue());
        assertEquals("25.34", result.getString("amount"));
        assertThat(result.getBoolean("tax"), is(true));
        document = new JSONObject(obj.getString("result"));
        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + time + "'}, 'P:my:amountable':{'my:amount':'', 'my:tax':''}}";
        obj = services.updateProperties(document.getString("objectID"), extraProperties);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        result = new JSONObject(obj.getString("result"));
        assertThat(result, notNullValue());
        document = new JSONObject(obj.getString("result"));
        obj = services.deleteDocument(document.getString("objectID"));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testMoveNode() throws Exception {
        JSONObject oldFolder = services.getNodeId("/Archiv");
        assertNotNull(oldFolder);
        assertTrue(oldFolder.length() >= 2);
        assertNotNull(oldFolder.get("result"));
        assertTrue(oldFolder.get("result").toString(), oldFolder.getBoolean("success"));
        String oldFolderId = oldFolder.getString("result");
        JSONObject newFolder = services.getNodeId("/Archiv/Fehler");
        assertNotNull(newFolder);
        assertTrue(newFolder.length() >= 2);
        assertNotNull(newFolder.get("result"));
        String newFolderId = newFolder.getString("result");
        assertTrue(newFolder.get("result").toString(), newFolder.getBoolean("success"));
        String content = "";
        JSONObject document = services.createDocument(oldFolderId, "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR.value());
        assertNotNull(document);
        assertTrue(document.length() >= 2);
        assertNotNull(document.get("result"));
        assertTrue(document.get("result").toString(), document.getBoolean("success"));
        JSONObject documentResult = new JSONObject(document.get("result").toString());
        assertNotNull(documentResult);
        assertNotNull(documentResult.getString("objectId"));
        JSONObject obj = services.moveNode(documentResult.getString("objectId"), oldFolderId, newFolderId);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.getNodeId("/Archiv/Fehler/TestDocument.txt");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.deleteDocument(obj.getString("result"));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testLoadProperties() throws Exception {
        String fileName = "/test.properties";
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = services.loadProperties(fullPath);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject props = new JSONObject(obj.get("result").toString());
        assertNotNull(props);
        assertTrue(props.length() > 0);
    }

    @Test
    public void testExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        JSONObject obj = services.extractPDFToInternalStorage(Base64.encodeBase64String(content), fileName);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertThat(obj.getInt("result"), is(1));
        assertThat(services.getEntries().size(), is(1));
        FileEntry entry = services.getEntries().iterator().next();
        assertThat(entry, notNullValue());
        assertEquals(fileName, entry.getName());
        assertThat(entry.getData().length, Matchers.greaterThan(0));
        assertThat(entry.getExtractedData().length(), Matchers.greaterThan(0));
        assertThat(entry.getExtractedData(), startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
    }

    @Test
    public void testExtractPDFContent() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        JSONObject obj = services.extractPDFContent(Base64.encodeBase64String(content));
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertThat(obj.getString("result"), startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
    }

    @Test
    public void testExtractPDFFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, notNullValue());
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = services.extractPDFFile(fullPath);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertThat(obj.getString("result"), startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
    }

    @Test
    public void testExtractZIP() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        JSONObject obj = services.extractZIP(Base64.encodeBase64String(content));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONArray erg = obj.getJSONArray("result");
        assertThat(erg, notNullValue());
        assertThat(erg.length(), is(2));
        String str = erg.getString(0);
        assertThat(str.length(), Matchers.greaterThan(0));
        str = erg.getString(1);
        assertThat(str.length(), Matchers.greaterThan(0));
    }

    @Test
    public void testExtractZIPToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        JSONObject obj = services.extractZIPToInternalStorage(Base64.encodeBase64String(content));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertThat(obj.getInt("result"), is(2));
        assertThat(services.getEntries().size(), is(2));
        for (FileEntry entry : services.getEntries()) {
            assertThat(entry.getName().isEmpty(), is(false));
            assertThat(entry.getData().length, Matchers.greaterThan(0));
            assertTrue(entry.getExtractedData() == null || entry.getExtractedData().isEmpty());
        }
    }

    @Test
    public void testExtractZipAndExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        JSONObject obj = services.extractZIPAndExtractPDFToInternalStorage(Base64.encodeBase64String(content));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertThat(obj.getInt("result"), is(2));
        assertThat(services.getEntries().size(), is(2));
        for (FileEntry entry : services.getEntries()) {
            assertThat(entry.getName().isEmpty(), is(false));
            assertThat(entry.getData().length, Matchers.greaterThan(0));
            assertThat(entry.getExtractedData().isEmpty(), is(false));
        }
    }

    @Test
    public void testGetDataFromInternalStorage() throws Exception {
        services.getEntries().clear();
        JSONObject obj = services.getDataFromInternalStorage();
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertThat(obj.getBoolean("success"), is(false));
        services.getEntries().add(new FileEntry("Test1", new byte[]{0, 1, 2}, "Test Inhalt 1"));
        services.getEntries().add(new FileEntry("Test2", new byte[]{2, 3, 4}, "Test Inhalt 2"));
        obj = services.getDataFromInternalStorage();
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject result = (JSONObject) obj.get("result");
        assertThat(result, notNullValue());
        assertThat(result.length(), is(2));
        assertThat(result.has("Test1"), is(true));
        assertThat(result.has("Test2"), is(true));
        assertThat(result.get("Test1"), instanceOf(JSONObject.class));
        assertThat(result.get("Test2"), instanceOf(JSONObject.class));
        JSONObject entry = (JSONObject) result.get("Test1");
        assertThat(new String(Base64.decodeBase64(entry.getString("data"))), equalTo((new String(new byte[]{0, 1, 2}))));
        assertThat(entry.getString("extractedData"), equalTo("Test Inhalt 1"));
        entry = (JSONObject) result.get("Test2");
        assertThat(new String(Base64.decodeBase64(entry.getString("data"))), equalTo(new String(new byte[]{2, 3, 4})));
        assertThat(entry.getString("extractedData"), equalTo("Test Inhalt 2"));
        obj = services.getDataFromInternalStorage("Test2");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        result = (JSONObject) obj.get("result");
        assertThat(result, notNullValue());
        assertThat(result.length(), is(1));
        assertThat(result.has("Test2"), is(true));
        assertThat(result.get("Test2"), instanceOf(JSONObject.class));
        entry = (JSONObject) result.get("Test2");
        assertThat(new String(Base64.decodeBase64(entry.getString("data"))), equalTo(new String(new byte[]{2, 3, 4})));
        assertThat(entry.getString("extractedData"), equalTo("Test Inhalt 2"));
        obj = services.getDataFromInternalStorage("Test3");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertThat(obj.getBoolean("success"), is(false));
        services.getEntries().clear();
    }

    @Test
    public void testClearInternalStoreage() throws Exception {
        services.getEntries().add(new FileEntry("Test1", new byte[]{0, 1, 2}, "Test Inhalt 1"));
        JSONObject obj = services.clearInternalStorage();
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertThat(services.getEntries().isEmpty(), is(true));
    }

    @Test
    public void testOpenFilePdf() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, notNullValue());
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = services.openFile(fullPath);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        byte[] contentRead = Base64.decodeBase64(obj.getString("result"));
        assertTrue("Unterschiedliche Länge gelesen!", content.length == contentRead.length);
        for (int i = 0; i < content.length; i++) {
            assertTrue("Unterschiedlicher Inhalt gelesen Position: " + i + " !", content[i] == contentRead[i]);
        }
    }

    @Test
    public void testOpenFileTxt() throws Exception {
        String fileName = properties.getProperty("testTXT");
        assertNotNull(fileName);
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = services.openFile(fullPath);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        byte[] contentRead = Base64.decodeBase64(obj.getString("result"));
        assertTrue("Unterschiedliche Länge gelesen!", content.length == contentRead.length);
        for (int i = 0; i < content.length; i++) {
            assertTrue("Unterschiedlicher Inhalt gelesen Position: " + i + " !", content[i] == contentRead[i]);
        }
    }

    @Test
    public void testIsURLAvailable() throws Exception {
        String urlString = "http://www.spiegel.de";
        JSONObject obj = services.isURLAvailable(urlString);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        urlString = "http://www.spiegel.dumm";
        obj = services.isURLAvailable(urlString);
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertFalse(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testGetComments() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        obj = services.createDocument(obj.getString("result"), "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR.value());
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject document = new JSONObject(obj.getString("result"));
        assertNotNull(document);
        obj = services.getTicket();
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject ticket = (JSONObject) obj.get("result");

        obj = services.addComment(document.getString("objectId"), ((JSONObject) ticket.get("data")).getString("ticket"), "Testkommentar");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));

        obj = services.getComments(document.getString("objectId"), ((JSONObject) ticket.get("data")).getString("ticket"));
        JSONObject comment = (JSONObject) obj.get("result");
        assertThat("Testkommentar", equalTo(((JSONObject) ((JSONArray) comment.get("items")).get(0)).getString("content")));

        obj = services.deleteDocument(document.getString("objectId"));
        assertThat(obj, notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), notNullValue());
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }
}
