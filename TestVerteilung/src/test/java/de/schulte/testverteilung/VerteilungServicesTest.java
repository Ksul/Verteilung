package de.schulte.testverteilung;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    }

    @Test
    public void testBuildProperties() throws Exception {
        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33'}, 'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        Map<String, Object> prop = services.buildProperties(extraProperties);
        assertThat(prop, notNullValue());
        assertThat(prop.containsKey(PropertyIds.SECONDARY_OBJECT_TYPE_IDS), Matchers.is(true));
        List<String> aspekte = (List<String>) prop.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
        assertThat(aspekte.contains("P:cm:emailed"), is(true));
        assertThat(aspekte.contains("P:cm:titled"), is(true));
        assertThat(aspekte.contains("P:my:amountable"), is(true));
        assertThat(prop.size(), is(7));
        assertThat(prop.containsKey("cm:description"), is(true));
        assertThat(prop.containsKey("cm:sentdate"), is(true));
        assertThat(prop.containsKey("my:amount"), is(true));
        assertThat(prop.containsKey("my:person"), is(true));
        assertThat(prop.containsKey("my:documentDate"), is(true));
        assertThat(prop.containsKey(PropertyIds.OBJECT_TYPE_ID), is(true));
        assertThat(prop.get(PropertyIds.OBJECT_TYPE_ID), is("D:my:archivContent"));
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
        CmisObject folder = buildTestFolder("TestFolder", null);

        buildDocument("TestDocument", folder);
        buildDocument("TestDocument1", folder);
        buildDocument("TestDocument2", folder);
        buildTestFolder("FolderTest", folder);

        JSONObject obj = services.listFolder(folder.getId(), null, null, VerteilungConstants.LIST_MODUS_FOLDER, -1, 0, 0);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.get("data"), instanceOf(JSONArray.class));
        assertThat(obj.getJSONArray("data").length(), is(1));
        assertThat( obj.getJSONArray("data").getJSONObject(0).getString("name"), is("FolderTest"));
        assertThat( obj.getJSONArray("data").getJSONObject(0).getBoolean("hasChildFolder"), is(false));
        assertThat(obj.getJSONArray("data").getJSONObject(0).getString("objectId"), Matchers.notNullValue());

        obj = services.listFolder(folder.getId(), null, null, VerteilungConstants.LIST_MODUS_ALL, -1, 0, 0);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.get("data"), instanceOf(JSONArray.class));
        assertThat( obj.getJSONArray("data").length(), Matchers.is(4));
        for (int i = 0; i < obj.getJSONArray("data").length(); i++) {
            assertThat( obj.getJSONArray("data").getJSONObject(i).getString("name"), anyOf(is("TestDocument"), is("TestDocument1"), is("TestDocument2"), is("FolderTest")));
            assertThat(obj.getJSONArray("data").getJSONObject(i).getString("objectId"), Matchers.notNullValue());
        }

        obj = services.listFolder(folder.getId(), null, null, VerteilungConstants.LIST_MODUS_DOCUMENTS, -1, 0, 0);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.get("data"), instanceOf(JSONArray.class));
        assertThat( obj.getJSONArray("data").length(), Matchers.is(3));
        for (int i = 0; i < obj.getJSONArray("data").length(); i++) {
            assertThat( obj.getJSONArray("data").getJSONObject(i).getString("name"), anyOf(is("TestDocument"), is("TestDocument1"), is("TestDocument2")));
            assertThat(obj.getJSONArray("data").getJSONObject(i).getString("objectId"), Matchers.notNullValue());
        }
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
        JSONObject obj = services.getDocumentContent(services.getNodeId("/Datenverzeichnis/Skripte/backup.js.sample").getString("data"), false);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String document = obj.getString("data");
        assertThat(document.startsWith("//"), is(true));
    }

    @Test
    public void testUploadDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        JSONObject obj = services.uploadDocument(folder.getId(), System.getProperty("user.dir") + properties.getProperty("testPDF"), VersioningState.MINOR.value());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        obj = services.deleteDocument(services.getNodeId("/TestFolder/Test.pdf").getString("data"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testCreateDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}}";
        JSONObject obj = services.createDocument(folder.getId(), "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), VerteilungConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MINOR.value());
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
        obj = services.deleteDocument(services.getNodeId("/TestFolder/TestDocument.txt").getString("data"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length() >= 2, is(true));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), is(true));
    }

    @Test
    public void testCreateDocumentWithCustomModel() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33'}, 'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        JSONObject obj = services.createDocument(folder.getId(), "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), VerteilungConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MINOR.value());
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
        obj = services.deleteDocument(services.getNodeId("/TestFolder/TestDocument.txt").getString("data"));
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
        CmisObject folder = buildTestFolder("TestFolder", null);
        Document document = (Document) buildDocument("TestDocument", folder);

        String extraProperties;
        String aspects;
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        JSONObject obj = services.updateDocument(document.getId(), Base64.encodeBase64String(content.getBytes()), VerteilungConstants.DOCUMENT_TYPE_TEXT,  null, VersioningState.MINOR.value(), null);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject doc = new JSONObject(obj.getString("data"));
        obj = services.getDocumentContent(doc.getString("objectId"), false);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertEquals(content, obj.getString("data"));

        obj = services.deleteDocument(doc.getString("objectId"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));

        extraProperties = "{'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.createDocument(folder.getId(), "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), VerteilungConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MAJOR.value());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        doc = new JSONObject(obj.getString("data"));
        assertThat(doc, Matchers.notNullValue());
        assertThat(doc.getString("versionLabel"), Matchers.equalTo("1.0"));
        assertThat(doc.getString("checkinComment"), Matchers.equalTo("Initial Version"));

        content = "Dies ist ein neuer Inhalt";
        obj = services.updateDocument(doc.getString("objectId"), Base64.encodeBase64String(content.getBytes()), VerteilungConstants.DOCUMENT_TYPE_TEXT,  null, VersioningState.MAJOR.value(), "neuer Versionskommentar");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());
        assertThat(data.getString("versionLabel"), Matchers.equalTo("2.0"));
        assertThat(data.getString("checkinComment"), Matchers.equalTo("neuer Versionskommentar"));

        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33', 'my:tax':'true'}, 'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.updateDocument(data.getString("objectId"), null, VerteilungConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MAJOR.value(), "2. Versionskommentar");
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

        obj = services.deleteDocument(doc.getString("objectId"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testChangeFolder() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);

        String extraProperties = "{'cmis:folder':{'cmis:objectTypeId': 'cmis:folder','cmis:name': 'FolderTest'}, 'P:cm:titled': {'cm:title': 'Titel','cm:description': 'Beschreibung' }}";
        JSONObject obj = services.updateProperties(folder.getId(), extraProperties);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        folder.refresh();
        assertThat(folder.getName(), is(("FolderTest")));

        extraProperties = "{'cmis:folder':{'cmis:objectTypeId': 'cmis:folder','cmis:name': 'TestFolder'}, 'P:cm:titled': {'cm:title': '','cm:description': '' }}";
        obj = services.updateProperties(folder.getId(), extraProperties);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        folder.refresh();
        //assertThat(folder.getName(), is(("TestFolder")));
    }

    @Test
    public void testUpdateProperties() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        long time = new Date().getTime();

        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument', 'cm:title':'Testdokument \tTest'}, 'D:my:archivContent': { 'my:documentDate': '" + time + "', 'my:person': 'Katja'},'P:cm:emailed':{'cm:sentdate':'" + time + "'}, 'P:my:amountable':{'my:amount':'25.33', 'my:tax':'true'}, 'P:my:idable': {'my:idvalue': 'null'}}";
        JSONObject obj = services.updateProperties(document.getId(), extraProperties);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());
        assertThat(data.get("title"), Matchers.equalTo("Testdokument \tTest"));
        assertThat(data.getDouble("amount"), Matchers.equalTo(25.33));
        assertThat(data.getBoolean("tax"), is(true));
        assertThat(data.getString("person"), is("Katja"));
        assertThat(data.getLong("documentDate"), is(time));
        assertThat(data.getLong("sentdate"), is(time));

        document.refresh();
        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument','cm:title':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + time + "'}, 'P:my:amountable':{'my:amount':'25.34', 'my:tax':'true'}, 'P:my:idable': {'my:idvalue': '123'}}";
        obj = services.updateProperties(document.getId(), extraProperties);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());
        assertThat(data.get("title"), Matchers.equalTo("Testdokument"));
        assertThat(data.getDouble("amount"), Matchers.equalTo(25.34));
        assertThat(data.getBoolean("tax"), is(true));
        assertThat(data.getInt("idvalue"), is(123));

        document.refresh();
        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + time + "'}, 'P:my:amountable':{'my:amount':'', 'my:tax':''}}";
        obj = services.updateProperties(document.getId(), extraProperties);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        data = new JSONObject(obj.getString("data"));
        assertThat(data, Matchers.notNullValue());

        document.refresh();
        obj = services.deleteDocument(document.getId());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testMoveNode() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        CmisObject newFolder = buildTestFolder("FolderTest", null);

        JSONObject obj = services.moveNode(document.getId(), folder.getId(), newFolder.getId());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        document = con.getNode("/FolderTest/TestDocument");
        assertThat(document, Matchers.notNullValue());
        assertThat(document.getName(), Matchers.is("TestDocument"));
        document.delete();
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
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        JSONObject obj = services.getTicket();
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject ticket = (JSONObject) obj.get("data");

        obj = services.addComment(document.getId(), ((JSONObject) ticket.get("data")).getString("ticket"), "Testkommentar");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));

        obj = services.getComments(document.getId(), ((JSONObject) ticket.get("data")).getString("ticket"));
        JSONObject comment = (JSONObject) obj.get("data");
        assertThat("Testkommentar", equalTo(((JSONObject) ((JSONArray) comment.get("items")).get(0)).getString("content")));

        document.delete();
    }
}
