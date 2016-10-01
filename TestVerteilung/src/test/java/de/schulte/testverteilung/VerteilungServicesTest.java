package de.schulte.testverteilung;

import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.01.14
 * Time: 09:54
 * To change this template use File | Settings | File Templates.
 */
public class VerteilungServicesTest extends AlfrescoTest {

    private VerteilungServices services;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        services = new VerteilungServices(properties.getProperty("server"), properties.getProperty("binding"), properties.getProperty("user"), properties.getProperty("password"));
        assertThat(services, Matchers.notNullValue());
        services.deleteDocument(services.getNodeId("/Archiv/Test.pdf").getString("data"));
        services.deleteDocument(services.getNodeId("/Archiv/TestDocument.txt").getString("data"));
        services.deleteDocument(services.getNodeId("/Archiv/Fehler/TestDocument.txt").getString("data"));
        services.deleteDocument(services.getNodeId("/Archiv/Fehler/Test.pdf").getString("data"));
        services.deleteFolder(services.getNodeId("/Archiv/TestFolder").getString("data"));
    }

    @After
    public void cleanUp() throws Exception {
        services.deleteDocument(services.getNodeId("/Archiv/Test.pdf").getString("data"));
        services.deleteDocument(services.getNodeId("/Archiv/TestDocument.txt").getString("data"));
        services.deleteDocument(services.getNodeId("/Archiv/Fehler/TestDocument.txt").getString("data"));
        services.deleteDocument(services.getNodeId("/Archiv/Fehler/Test.pdf").getString("data"));
    }

    @Test
    public void testSetParameter() throws Exception {
        services.setParameter(properties.getProperty("server"), properties.getProperty("binding"), properties.getProperty("user"), properties.getProperty("password"));
        JSONObject obj = services.getTitles();
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testGetConnection() throws Exception {
        JSONObject obj = services.getConnection();
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject connection = (JSONObject) obj.get("data");
        assertThat(connection.getString("server"), Matchers.equalTo(properties.getProperty("server")));
        assertThat(connection.getString("binding"), Matchers.equalTo(properties.getProperty("binding")));
        assertThat(connection.getString("user"), Matchers.equalTo(properties.getProperty("user")));
        assertThat(connection.getString("password"), Matchers.equalTo(properties.getProperty("password")));
        services.setParameter(null, null, null, null);
        obj = services.getConnection();
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getBoolean("data"), Matchers.is(false));
    }

    @Test
    public void testGetTicket() throws Exception {
        JSONObject obj = services.getTicket();
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject ticket = (JSONObject) obj.get("data");
        assertThat(((JSONObject) ticket.get("data")).getString("ticket"), Matchers.startsWith("TICKET_"));
    }

    @Test
    public void testListFolder() throws Exception {
        JSONObject obj = services.listFolder("-1", null, null, 0, -1, 0, 0);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.get("data"), instanceOf(JSONArray.class));
        assertThat(obj.getJSONArray("data").length(), is(4));
        for (int i = 0; i < obj.getJSONArray("data").length(); i++) {
            assertThat( obj.getJSONArray("data").getJSONObject(i).getString("name"), anyOf(is("Dokumente"), is("Fehler"), is("Unbekannt"), is("Inbox")));
            if (obj.getJSONArray("data").getJSONObject(i).getString("name").equalsIgnoreCase("Fehler"))
                assertTrue(obj.getJSONArray("data").getJSONObject(i).getBoolean("hasChildFolder"));
            assertThat(obj.getJSONArray("data").getJSONObject(i).getString("objectId"), Matchers.notNullValue());
        }
        obj = services.uploadDocument(services.getNodeId("/Archiv/Fehler").getString("data"), System.getProperty("user.dir") + properties.getProperty("testPDF"), VersioningState.MINOR.value());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.listFolder("-1", null, null, -1, -1, 0, 0);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.get("data"), instanceOf(JSONArray.class));
        assertThat( obj.getJSONArray("data").length(), Matchers.is(4));
        for (int i = 0; i < obj.getJSONArray("data").length(); i++) {
            assertThat( obj.getJSONArray("data").getJSONObject(i).getString("name"), anyOf(is("Dokumente"), is("Fehler"), is("Unbekannt"), is("Inbox")));
            assertThat(obj.getJSONArray("data").getJSONObject(i).getString("objectId"), Matchers.notNullValue());
        }
        obj = services.getNodeId("/Archiv");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String id = obj.getString("data");
        obj = services.listFolder(id, null, null, 1, -1, 0, 0);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.get("data"), instanceOf(JSONArray.class));
        assertThat(obj.getJSONArray("data").length(), is(0));
        obj = services.listFolder(id, null, null, 0, -1, 0, 0);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.get("data"), instanceOf(JSONArray.class));
        assertThat(obj.getJSONArray("data").length(), Matchers.is(4));
        for (int i = 0; i < obj.getJSONArray("data").length(); i++) {
            if ( obj.getJSONArray("data").getJSONObject(i).getString("name").equalsIgnoreCase("Fehler")) {
                assertThat(obj.getJSONArray("data").getJSONObject(i).getBoolean("hasChildFolder"), Matchers.is(true));
                assertThat(obj.getJSONArray("data").getJSONObject(i).getBoolean("hasChildren"), Matchers.is(true));
            }
        }

        obj = services.deleteDocument(services.getNodeId("/Archiv/Fehler/Test.pdf").getString("data"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        //obj = services.listFolder(services.getNodeId("/Archiv/Inbox").getString("data"), 0, -1, 0, 0);
    }

    @Test
    public void testGetNodeID() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.getNodeId("/Datenverzeichnis/Skripte/");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.getNodeId("/Datenverzeichnis/Skripte/backup.js.sample");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testGetNode() throws Exception {
        JSONObject obj = services.getNode("/");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.getNode("/Datenverzeichnis/Skripte/");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.getNode("/Datenverzeichnis/Skripte/backup.js.sample");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testGetNodeByID() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.getNodeById(obj.getString("data"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.getNodeId("/Datenverzeichnis/Skripte/backup.js.sample");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.getNodeById(obj.getString("data"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testFindDocument() throws Exception {
        JSONObject obj = services.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='backup.js.sample'");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(((JSONArray) obj.get("data")).length(), Matchers.greaterThanOrEqualTo(1));
        JSONObject data = (JSONObject) ((JSONArray) obj.get("data")).get(0);
        assertThat(data, Matchers.notNullValue());
        assertThat(data.getString("name"), Matchers.equalToIgnoringCase("backup.js.sample"));
    }

    @Test
    public void testQuery() throws Exception {
        JSONObject obj = services.query("SELECT cmis:objectId, cmis:name from cmis:document where cmis:name='backup.js.sample'");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testGetDocumentContent() throws Exception {
        JSONObject obj = services.getDocumentContent(services.getNodeId("/Datenverzeichnis/Skripte/doc.xml").getString("data"), false);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String document = obj.getString("data");
        assertThat(document.startsWith("<documentTypes"), is(true));
        assertThat(document.indexOf("xmlns:my=\"http://www.schulte.local/archiv\""), Matchers.not(-1));
    }

    @Test
    public void testUploadDocument() throws Exception {
        JSONObject obj = services.uploadDocument(services.getNodeId("/Archiv").getString("data"), System.getProperty("user.dir") + properties.getProperty("testPDF"), VersioningState.MINOR.value());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.deleteDocument(services.getNodeId("/Archiv/Test.pdf").getString("data"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testCreateDocument() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length() >= 2, is(true));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), is(true));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}}";
        obj = services.createDocument(obj.getString("data"), "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MINOR.value());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length() >= 2, is(true));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), is(true));
        JSONObject data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());
        assertThat(data.getString("name").equalsIgnoreCase("TestDocument.txt"), is(true));
        obj = services.getDocumentContent(data.getString("objectId"), false);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length() >= 2, is(true));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), is(true));
        String document = obj.getString("data");
        assertThat(document, is("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?"));
        obj = services.deleteDocument(services.getNodeId("/TestDocument.txt").getString("data"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length() >= 2, is(true));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), is(true));
    }

    @Test
    public void testCreateDocumentWithCustomModel() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33'}, 'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.createDocument(obj.getString("data"), "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MINOR.value());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());
        assertThat(data.getString("name"), Matchers.equalToIgnoringCase("TestDocument.txt"));
        obj = services.getDocumentContent(data.getString("objectId"), false);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String document = obj.getString("data");
        assertThat(document, Matchers.equalTo("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?"));
        obj = services.deleteDocument(services.getNodeId("/TestDocument.txt").getString("data"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testCreateFolder() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String extraProperties = "{'cmis:folder':{'cmis:name': 'Testfolder'}, 'P:cm:titled':{'cm:title': 'Testtitel', 'cm:description':'Dies ist ein Test Folder'}}";
        obj = services.createFolder(obj.getString("data"), extraProperties);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());
        assertThat(data.getString("name"), Matchers.equalToIgnoringCase("TestFolder"));
        obj = services.deleteFolder(data.getString("objectId"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testUpdateDocument() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String archivId = obj.getString("data");
        String content = "";
        obj = services.createDocument(archivId, "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR.value());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject document = new JSONObject(obj.getString("data"));
        assertThat(document, Matchers.notNullValue());
        assertThat(document.getString("name"), Matchers.equalToIgnoringCase("TestDocument.txt"));
        assertThat(document.getString("objectId"), Matchers.notNullValue());
        content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        obj = services.updateDocument(document.getString("objectId"), Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR.value(), null);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        document = new JSONObject(obj.getString("data"));
        obj = services.getDocumentContent(document.getString("objectId"), false);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertEquals(content, obj.getString("data"));
        obj = services.deleteDocument(document.getString("objectId"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String extraProperties = "{'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.createDocument(archivId, "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MAJOR.value());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        document = new JSONObject(obj.getString("data"));
        assertThat(document, Matchers.notNullValue());
        assertThat(document.getString("versionLabel"), Matchers.equalTo("1.0"));
        assertThat(document.getString("checkinComment"), Matchers.equalTo("Initial Version"));
        content = "Dies ist ein neuer Inhalt";
        obj = services.updateDocument(document.getString("objectId"), Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MAJOR.value(), "neuer Versionskommentar");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());
        assertThat(data.getString("versionLabel"), Matchers.equalTo("2.0"));
        assertThat(data.getString("checkinComment"), Matchers.equalTo("neuer Versionskommentar"));
        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33', 'my:tax':'true'}, 'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.updateDocument(data.getString("objectId"), null, CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MAJOR.value(), "2. Versionskommentar");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());
        assertThat(data.getString("versionLabel"), Matchers.equalTo("3.0"));
        assertThat(data.getString("checkinComment"), Matchers.equalTo("2. Versionskommentar"));
        assertThat(data.getDouble("amount"), Matchers.equalTo(25.33));
        assertThat(data.getBoolean("tax"), Matchers.is(true));
        obj = services.deleteDocument(document.getString("objectId"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testChangeFolder() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv/Fehler");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String extraProperties = "{'cmis:folder':{'cmis:objectTypeId': 'cmis:folder','cmis:name': 'Fehler1'}, 'P:cm:titled': {'cm:title': 'Titel','cm:description': 'Beschreibung' }}";
        String id = obj.getString("data");
        obj = services.updateProperties(id, extraProperties);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        extraProperties = "{'cmis:folder':{'cmis:objectTypeId': 'cmis:folder','cmis:name': 'Fehler'}, 'P:cm:titled': {'cm:title': '','cm:description': '' }}";
        obj = services.updateProperties(id, extraProperties);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testUpdateProperties() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data").toString(), obj.getBoolean("success"), Matchers.is(true));
        String archivId = obj.getString("data");
        String content = "";
        long time = new Date().getTime();
        String extraProperties = "{'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.createDocument(archivId, "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MINOR.value());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject document = new JSONObject(obj.getString("data"));
        assertThat(document, Matchers.notNullValue());
        assertThat(document.getString("name"), Matchers.equalToIgnoringCase("TestDocument.txt"));
        assertThat(document.getString("objectID"), Matchers.notNullValue());
        assertThat(document.getLong("documentDate"), is(time));
        time = new Date().getTime();
        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument', 'cm:title':'Testdokument \tTest'}, 'D:my:archivContent': { 'my:documentDate': '" + time + "', 'my:person': 'Katja'},'P:cm:emailed':{'cm:sentdate':'" + time + "'}, 'P:my:amountable':{'my:amount':'25.33', 'my:tax':'true'}, 'P:my:idable': {'my:idvalue': 'null'}}";
        obj = services.updateProperties(document.getString("objectID"), extraProperties);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());
        assertThat(data.get("title"), Matchers.equalTo((Object) "Testdokument \tTest"));
        assertThat(data.getDouble("amount"), Matchers.equalTo(25.33));
        assertThat(data.getBoolean("tax"), is(true));
        assertThat(data.getString("person"), is("Katja"));
        assertThat(data.getLong("documentDate"), is(time));
        assertThat(data.getLong("sentdate"), is(time));
        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument','cm:title':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + time + "'}, 'P:my:amountable':{'my:amount':'25.34', 'my:tax':'true'}, 'P:my:idable': {'my:idvalue': '123'}}";
        obj = services.updateProperties(document.getString("objectID"), extraProperties);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());
        assertThat(data.get("title"), Matchers.equalTo((Object) "Testdokument"));
        assertThat(data.getDouble("amount"), Matchers.equalTo(25.34));
        assertThat(data.getBoolean("tax"), is(true));
        assertThat(data.getInt("idvalue"), is(123));
        document = new JSONObject(obj.getString("data"));
        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + time + "'}, 'P:my:amountable':{'my:amount':'', 'my:tax':''}}";
        obj = services.updateProperties(document.getString("objectID"), extraProperties);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());
        document = new JSONObject(obj.getString("data"));
        obj = services.deleteDocument(document.getString("objectID"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testMoveNode() throws Exception {
        JSONObject oldFolder = services.getNodeId("/Archiv");
        assertThat(oldFolder, Matchers.notNullValue());
        assertThat(oldFolder.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(oldFolder.get("data"), Matchers.notNullValue());
        assertThat(oldFolder.get("data").toString(), oldFolder.getBoolean("success"), Matchers.is(true));
        String oldFolderId = oldFolder.getString("data");
        JSONObject newFolder = services.getNodeId("/Archiv/Fehler");
        assertThat(newFolder, Matchers.notNullValue());
        assertThat(newFolder.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(newFolder.get("data"), Matchers.notNullValue());
        String newFolderId = newFolder.getString("data");
        assertThat(newFolder.get("data").toString(), newFolder.getBoolean("success"), Matchers.is(true));
        String content = "";
        JSONObject document = services.createDocument(oldFolderId, "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR.value());
        assertThat(document, Matchers.notNullValue());
        assertThat(document.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(document.get("data"), Matchers.notNullValue());
        assertThat(document.get("data").toString(), document.getBoolean("success"), Matchers.is(true));
        JSONObject documentResult = new JSONObject(document.get("data").toString());
        assertThat(documentResult, Matchers.notNullValue());
        assertThat(documentResult.getString("objectId"), Matchers.notNullValue());
        JSONObject obj = services.moveNode(documentResult.getString("objectId"), oldFolderId, newFolderId);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.getNodeId("/Archiv/Fehler/TestDocument.txt");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.deleteDocument(obj.getString("data"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testLoadProperties() throws Exception {
        String fileName = "/src/test/resources/connection.properties";
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = services.loadProperties(fullPath);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject props = new JSONObject(obj.get("data").toString());
        assertThat(props, Matchers.notNullValue());
        assertThat(props.length(), Matchers.greaterThan(0));
    }

    @Test
    public void testExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        JSONObject obj = services.extractPDFToInternalStorage(Base64.encodeBase64String(content), fileName);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("data"), is(1));
        assertThat(services.getEntries().size(), is(1));
        FileEntry entry = services.getEntries().iterator().next();
        assertThat(entry, Matchers.notNullValue());
        assertThat(entry.getName(), Matchers.equalTo(fileName));
        assertThat(entry.getData().length, Matchers.greaterThan(0));
        assertThat(entry.getExtractedData().length(), Matchers.greaterThan(0));
        assertThat(entry.getExtractedData(), startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
    }

    @Test
    public void testExtractPDFContent() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        JSONObject obj = services.extractPDFContent(Base64.encodeBase64String(content));
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("data"), startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
    }

    @Test
    public void testExtractPDFFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = services.extractPDFFile(fullPath);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("data"), startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
    }

    @Test
    public void testExtractZIP() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        JSONObject obj = services.extractZIP(Base64.encodeBase64String(content));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONArray erg = obj.getJSONArray("data");
        assertThat(erg, Matchers.notNullValue());
        assertThat(erg.length(), is(2));
        String str = erg.getString(0);
        assertThat(str.length(), Matchers.greaterThan(0));
        str = erg.getString(1);
        assertThat(str.length(), Matchers.greaterThan(0));
    }

    @Test
    public void testExtractZIPToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        JSONObject obj = services.extractZIPToInternalStorage(Base64.encodeBase64String(content));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("data"), is(2));
        assertThat(services.getEntries().size(), is(2));
        for (FileEntry entry : services.getEntries()) {
            assertThat(entry.getName().isEmpty(), is(false));
            assertThat(entry.getData().length, Matchers.greaterThan(0));
            assertThat(entry.getExtractedData() == null || entry.getExtractedData().isEmpty(), Matchers.is(true));
        }
    }

    @Test
    public void testExtractZipAndExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        JSONObject obj = services.extractZIPAndExtractPDFToInternalStorage(Base64.encodeBase64String(content));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("data"), is(2));
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
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), is(false));
        services.getEntries().add(new FileEntry("Test1", new byte[]{0, 1, 2}, "Test Inhalt 1"));
        services.getEntries().add(new FileEntry("Test2", new byte[]{2, 3, 4}, "Test Inhalt 2"));
        obj = services.getDataFromInternalStorage();
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject data = (JSONObject) obj.get("data");
        assertThat(data, Matchers.notNullValue());
        assertThat(data.length(), is(2));
        assertThat(data.has("Test1"), is(true));
        assertThat(data.has("Test2"), is(true));
        assertThat(data.get("Test1"), instanceOf(JSONObject.class));
        assertThat(data.get("Test2"), instanceOf(JSONObject.class));
        JSONObject entry = (JSONObject) data.get("Test1");
        assertThat(new String(Base64.decodeBase64(entry.getString("data"))), equalTo((new String(new byte[]{0, 1, 2}))));
        assertThat(entry.getString("extractedData"), equalTo("Test Inhalt 1"));
        entry = (JSONObject) data.get("Test2");
        assertThat(new String(Base64.decodeBase64(entry.getString("data"))), equalTo(new String(new byte[]{2, 3, 4})));
        assertThat(entry.getString("extractedData"), equalTo("Test Inhalt 2"));
        obj = services.getDataFromInternalStorage("Test2");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        data = (JSONObject) obj.get("data");
        assertThat(data, Matchers.notNullValue());
        assertThat(data.length(), is(1));
        assertThat(data.has("Test2"), is(true));
        assertThat(data.get("Test2"), instanceOf(JSONObject.class));
        entry = (JSONObject) data.get("Test2");
        assertThat(new String(Base64.decodeBase64(entry.getString("data"))), equalTo(new String(new byte[]{2, 3, 4})));
        assertThat(entry.getString("extractedData"), equalTo("Test Inhalt 2"));
        obj = services.getDataFromInternalStorage("Test3");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), is(false));
        services.getEntries().clear();
    }

    @Test
    public void testClearInternalStoreage() throws Exception {
        services.getEntries().add(new FileEntry("Test1", new byte[]{0, 1, 2}, "Test Inhalt 1"));
        JSONObject obj = services.clearInternalStorage();
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(services.getEntries().isEmpty(), is(true));
    }

    @Test
    public void testOpenFilePdf() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = services.openFile(fullPath);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        byte[] contentRead = Base64.decodeBase64(obj.getString("data"));
        assertThat("Unterschiedliche Länge gelesen!", content.length == contentRead.length, Matchers.is(true));
        for (int i = 0; i < content.length; i++) {
            assertThat("Unterschiedlicher Inhalt gelesen Position: " + i + " !", content[i] == contentRead[i], Matchers.is(true));
        }
    }

    @Test
    public void testOpenFileTxt() throws Exception {
        String fileName = properties.getProperty("testTXT");
        assertThat(fileName, Matchers.notNullValue());
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = services.openFile(fullPath);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        byte[] contentRead = Base64.decodeBase64(obj.getString("data"));
        assertThat("Unterschiedliche Länge gelesen!", content.length == contentRead.length, Matchers.is(true));
        for (int i = 0; i < content.length; i++) {
            assertThat("Unterschiedlicher Inhalt gelesen Position: " + i + " !", content[i] == contentRead[i], Matchers.is(true));
        }
    }

    @Test
    public void testIsURLAvailable() throws Exception {
        String urlString = "http://www.spiegel.de";
        JSONObject obj = services.isURLAvailable(urlString, 5000);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        urlString = "http://www.spiegel.dumm";
        obj = services.isURLAvailable(urlString, 5000);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(false));
    }

    @Test
    public void testGetComments() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        obj = services.createDocument(obj.getString("data"), "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR.value());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject document = new JSONObject(obj.getString("data"));
        assertThat(document, Matchers.notNullValue());
        obj = services.getTicket();
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject ticket = (JSONObject) obj.get("data");

        obj = services.addComment(document.getString("objectId"), ((JSONObject) ticket.get("data")).getString("ticket"), "Testkommentar");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));

        obj = services.getComments(document.getString("objectId"), ((JSONObject) ticket.get("data")).getString("ticket"));
        JSONObject comment = (JSONObject) obj.get("data");
        assertThat("Testkommentar", equalTo(((JSONObject) ((JSONArray) comment.get("items")).get(0)).getString("content")));

        obj = services.deleteDocument(document.getString("objectId"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }
}
