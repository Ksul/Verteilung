package de.schulte.testverteilung;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
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
        services.deleteDocument(services.getNodeId("/Archiv").getString("result"), "Test.pdf");
        services.deleteDocument(services.getNodeId("/Archiv").getString("result"), "TestDocument.txt");
        services.deleteDocument(services.getNodeId("/Archiv/Fehler").getString("result"), "TestDocument.txt");
        services.deleteDocument(services.getNodeId("/Archiv/Fehler").getString("result"), "Test.pdf");
        services.deleteFolder(services.getNodeId("/Archiv/TestFolder").getString("result"));
    }

    @Test
    public void testGetTicket() throws Exception {
        JSONObject obj = services.getTicket();
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testListFolder() throws Exception {
        JSONObject obj = services.listFolder("-1", 0);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertTrue(obj.get("result") instanceof JSONArray);
        assertEquals(4, ((JSONArray) obj.get("result")).length());
        for (int i = 0; i < ((JSONArray) obj.get("result")).length(); i++) {
            assertThat(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("name"), anyOf(is("Dokumente"), is("Fehler"), is("Unbekannt"), is("Inbox")));
            if (((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("name").equalsIgnoreCase("Fehler"))
                assertTrue(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getBoolean("hasChildFolder"));
            assertNotNull(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("objectId"));
        }
        obj = services.uploadDocument(services.getNodeId("/Archiv/Fehler").getString("result"), System.getProperty("user.dir") + properties.getProperty("testPDF"), VersioningState.MINOR.value());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.listFolder("-1", -1);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertTrue(obj.get("result") instanceof JSONArray);
        assertEquals(4, ((JSONArray) obj.get("result")).length());
        for (int i = 0; i < ((JSONArray) obj.get("result")).length(); i++) {
            assertThat(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("name"), anyOf(is("Dokumente"), is("Fehler"), is("Unbekannt"), is("Inbox")));
            assertNotNull(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("objectId"));
        }
        obj = services.getNodeId("/Archiv");
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String id = obj.getString("result");
        obj = services.listFolder(id, 1);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertTrue(obj.get("result") instanceof JSONArray);
        assertEquals(0, ((JSONArray) obj.get("result")).length());
        obj = services.listFolder(id, 0);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        for (int i = 0; i < ((JSONArray) obj.get("result")).length(); i++) {
            if (((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("name").equalsIgnoreCase("Fehler")) {
                assertTrue(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getBoolean("hasChildFolder"));
                assertTrue(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getBoolean("hasChildren"));
            }
        }
        assertEquals(4, ((JSONArray) obj.get("result")).length());
        obj = services.deleteDocument(services.getNodeId("/Archiv/Fehler").getString("result"), "Test.pdf");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.listFolder(services.getNodeId("/Archiv/Inbox").getString("result"), 0);
        System.out.println(obj);
    }

    @Test
    public void testGetNodeID() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.getNodeId("/Datenverzeichnis/Skripte/recognition.js");
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        ;
        obj = services.getNodeId("/Datenverzeichnis/Skripte/doc.xml");
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testFindDocument() throws Exception {
        JSONObject obj = services.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertNotNull(result);
        assertTrue(result.getString("name").equalsIgnoreCase("doc.xml"));
    }

    @Test
    public void testGetDocumentContent() throws Exception {
        JSONObject obj = services.getDocumentContent(services.getNodeId("/Datenverzeichnis/Skripte/doc.xml").getString("result"), false);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String document = obj.getString("result");
        assertTrue(document.startsWith("<documentTypes"));
        assertTrue(document.indexOf("xmlns:my=\"http://www.schulte.local/archiv\"") != -1);
    }

    @Test
    public void testUploadDocument() throws Exception {
        JSONObject obj = services.uploadDocument(services.getNodeId("/Archiv").getString("result"), System.getProperty("user.dir") + properties.getProperty("testPDF"), VersioningState.MINOR.value());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.deleteDocument(services.getNodeId("/Archiv").getString("result"), "Test.pdf");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testCreateDocument() throws Exception {
        JSONObject obj = services.getNodeId("/");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33'}, 'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.createDocument(obj.getString("result"), "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MINOR.value());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertNotNull(result);
        assertTrue(result.getString("name").equalsIgnoreCase("TestDocument.txt"));
        obj = services.getDocumentContent(result.getString("objectId"), false);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String document = obj.getString("result");
        assertEquals("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?", document);
        obj = services.deleteDocument(services.getNodeId("/").getString("result"), "TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testCreateFolder() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String extraProperties = "{'cmis:folder': {'cmis:objectTypeId': 'cmis:folder', 'cmis:name': 'Testfolder'}, 'P:cm:titled':{'cm:title': 'Testtitel', 'cm:description':'Dies ist ein Test Folder'}}";
        obj = services.createFolder(obj.getString("result"), extraProperties);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertNotNull(result);
        assertTrue(result.getString("name").equalsIgnoreCase("TestFolder"));
        obj = services.deleteFolder(result.getString("objectId"));
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testUpdateDocument() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String archivId = obj.getString("result");
        String content = "";
        obj = services.createDocument(archivId, "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR.value());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertNotNull(result);
        assertTrue(result.getString("name").equalsIgnoreCase("TestDocument.txt"));
        assertNotNull(result.getString("objectId"));
        content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        obj = services.updateDocument(result.getString("objectId"), Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR.value(), null);
        result = new JSONObject(obj.getString("result"));
        obj = services.getDocumentContent(result.getString("objectId"), false);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertEquals(content, obj.getString("result"));
        obj = services.deleteDocument(archivId, "TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));

        obj = services.createDocument(archivId, "TestDocument.txt", Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MAJOR.value());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        result = new JSONObject(obj.getString("result"));
        assertNotNull(result);
        assertEquals("1.0", result.getString("versionLabel"));
        assertEquals("Initial Version", result.getString("checkinComment"));
        content = "Dies ist ein neuer Inhalt";
        obj = services.updateDocument(result.getString("objectId"), Base64.encodeBase64String(content.getBytes()), CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MAJOR.value(), "neuer Versionskommentar");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        result = new JSONObject(obj.getString("result"));
        assertNotNull(result);
        assertEquals("2.0", result.getString("versionLabel"));
        assertEquals("neuer Versionskommentar", result.getString("checkinComment"));
        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33', 'my:tax':'true'}, 'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.updateDocument(result.getString("objectId"), null, CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MAJOR.value(), "2. Versionskommentar");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        result = new JSONObject(obj.getString("result"));
        assertNotNull(result);
        assertEquals("3.0", result.getString("versionLabel"));
        assertEquals("2. Versionskommentar", result.getString("checkinComment"));
        assertEquals("25.33", result.getString("amount"));
        assertTrue(result.getBoolean("tax"));
        obj = services.deleteDocument(archivId, "TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testChangeFolder() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv/Fehler");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        String extraProperties = "{'cmis:folder':{'cmis:objectTypeId': 'cmis:folder','cmis:name': 'Fehler1'}, 'P:cm:titled': {'cm:title': 'Titel','cm:description': 'Beschreibung' }}";
        String id = obj.getString("result");
        obj = services.updateProperties(id, extraProperties);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        extraProperties = "{'cmis:folder':{'cmis:objectTypeId': 'cmis:folder','cmis:name': 'Fehler'}, 'P:cm:titled': {'cm:title': '','cm:description': '' }}";
        obj = services.updateProperties(id, extraProperties);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testUpdateProperties() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        String archivId = obj.getString("result");
        String content = "";
        String extraProperties = "{'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        obj = services.createDocument(archivId, "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, extraProperties, VersioningState.MINOR.value());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertNotNull(result);
        assertTrue(result.getString("name").equalsIgnoreCase("TestDocument.txt"));
        assertNotNull(result.getString("objectId"));
        extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33', 'my:tax':'true'}}";
        obj = services.updateProperties(result.getString("objectId"), extraProperties);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        result = new JSONObject(obj.getString("result"));
        assertNotNull(result);
        assertEquals("25.33", result.getString("amount"));
        assertTrue(result.getBoolean("tax"));
        obj = services.deleteDocument(archivId, "TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testMoveDocument() throws Exception {
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
        services.deleteDocument(oldFolderId, "TestDocument.txt");
        String content = "";
        JSONObject document = services.createDocument(oldFolderId, "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, null, VersioningState.MINOR.value());
        assertNotNull(document);
        assertTrue(document.length() >= 2);
        assertNotNull(document.get("result"));
        assertTrue(document.get("result").toString(), document.getBoolean("success"));
        JSONObject documentResult = new JSONObject(document.get("result").toString());
        assertNotNull(documentResult);
        assertNotNull(documentResult.getString("objectId"));
        JSONObject obj = services.moveDocument(documentResult.getString("objectId"), oldFolderId, newFolderId);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.getNodeId("/Archiv/Fehler/TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        obj = services.deleteDocument(newFolderId, "TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }

    @Test
    public void testLoadProperties() throws Exception {
        String fileName = "/test.properties";
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = services.loadProperties(fullPath);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject props = new JSONObject(obj.get("result").toString());
        assertNotNull(props);
        assertTrue(props.length() > 0);
    }

    @Test
    public void testExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertTrue(content.length > 0);
        JSONObject obj = services.extractPDFToInternalStorage(Base64.encodeBase64String(content), fileName);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 1);
        assertTrue(services.getEntries().size() == 1);
        FileEntry entry = services.getEntries().iterator().next();
        assertNotNull(entry);
        assertEquals(fileName, entry.getName());
        assertTrue(entry.getData().length > 0);
        assertTrue(entry.getExtractedData().length() > 0);
        assertTrue(entry.getExtractedData().startsWith("HerrKlaus SchulteBredeheide 3348161 Münster"));
    }

    @Test
    public void testExtractPDFContent() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertTrue(content.length > 0);
        JSONObject obj = services.extractPDFContent(Base64.encodeBase64String(content));
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith("HerrKlaus SchulteBredeheide 3348161 Münster"));
    }

    @Test
    public void testExtractPDFFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = services.extractPDFFile(fullPath);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith("HerrKlaus SchulteBredeheide 3348161 Münster"));
    }

    @Test
    public void testExtractZIP() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertNotNull(fileName);
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertTrue(content.length > 0);
        Collection<FileEntry> entries = new ArrayList<FileEntry>();
        JSONObject obj = services.extractZIP(Base64.encodeBase64String(content));
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONArray erg = obj.getJSONArray("result");
        assertNotNull(erg);
        assertTrue(erg.length() == 2);
        String str = erg.getString(0);
        assertTrue(str.length() > 0);
        str = erg.getString(1);
        assertTrue(str.length() > 0);
    }

    @Test
    public void testExtractZIPToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertNotNull(fileName);
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertTrue(content.length > 0);
        JSONObject obj = services.extractZIPToInternalStorage(Base64.encodeBase64String(content));
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 2);
        assertTrue(services.getEntries().size() == 2);
        for (FileEntry entry : services.getEntries()) {
            assertTrue(!entry.getName().isEmpty());
            assertTrue(entry.getData().length > 0);
            assertTrue(entry.getExtractedData() == null || entry.getExtractedData().isEmpty());
        }
    }

    @Test
    public void testExtractZipAndExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertNotNull(fileName);
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertTrue(content.length > 0);
        JSONObject obj = services.extractZIPAndExtractPDFToInternalStorage(Base64.encodeBase64String(content));
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 2);
        assertTrue(services.getEntries().size() == 2);
        for (FileEntry entry : services.getEntries()) {
            assertTrue(!entry.getName().isEmpty());
            assertTrue(entry.getData().length > 0);
            assertTrue(!entry.getExtractedData().isEmpty());
        }
    }

    @Test
    public void testGetDataFromInternalStorage() throws Exception {
        services.getEntries().clear();
        JSONObject obj = services.getDataFromInternalStorage();
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
        services.getEntries().add(new FileEntry("Test1", new byte[]{0, 1, 2}, "Test Inhalt 1"));
        services.getEntries().add(new FileEntry("Test2", new byte[]{2, 3, 4}, "Test Inhalt 2"));
        obj = services.getDataFromInternalStorage();
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        JSONObject result = (JSONObject) obj.get("result");
        assertNotNull(result);
        assertTrue(result.length() == 2);
        assertTrue(result.has("Test1"));
        assertTrue(result.has("Test2"));
        assertTrue(result.get("Test1") instanceof JSONObject);
        assertTrue(result.get("Test2") instanceof JSONObject);
        JSONObject entry = (JSONObject) result.get("Test1");
        assertTrue(new String(Base64.decodeBase64(entry.getString("data"))).equals(new String(new byte[]{0, 1, 2})));
        assertTrue(entry.getString("extractedData").equals("Test Inhalt 1"));
        entry = (JSONObject) result.get("Test2");
        assertTrue(new String(Base64.decodeBase64(entry.getString("data"))).equals(new String(new byte[]{2, 3, 4})));
        assertTrue(entry.getString("extractedData").equals("Test Inhalt 2"));
        obj = services.getDataFromInternalStorage("Test2");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        result = (JSONObject) obj.get("result");
        assertNotNull(result);
        assertTrue(result.length() == 1);
        assertTrue(result.has("Test2"));
        assertTrue(result.get("Test2") instanceof JSONObject);
        entry = (JSONObject) result.get("Test2");
        assertTrue(new String(Base64.decodeBase64(entry.getString("data"))).equals(new String(new byte[]{2, 3, 4})));
        assertTrue(entry.getString("extractedData").equals("Test Inhalt 2"));
        obj = services.getDataFromInternalStorage("Test3");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
        services.getEntries().clear();
    }

    @Test
    public void testClearInternalStoreage() throws Exception {
        services.getEntries().add(new FileEntry("Test1", new byte[]{0, 1, 2}, "Test Inhalt 1"));
        JSONObject obj = services.clearInternalStorage();
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        assertTrue(services.getEntries().isEmpty());
    }

    @Test
    public void testOpenFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = services.openFile(fullPath);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
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
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
        urlString = "http://www.spiegel.dumm";
        obj = services.isURLAvailable(urlString);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"));
    }
}
