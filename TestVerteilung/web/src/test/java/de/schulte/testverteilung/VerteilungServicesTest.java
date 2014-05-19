package de.schulte.testverteilung;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.01.14
 * Time: 09:54
 * To change this template use File | Settings | File Templates.
 */
public class VerteilungServicesTest extends AlfrescoTest{

    VerteilungServices services;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        services = new VerteilungServices(properties.getProperty("bindingUrl"), "admin", properties.getProperty("password"));
        assertNotNull(services);
        services.deleteDocument("/Archiv", "Test.pdf");
        services.deleteDocument("/Archiv", "TestDocument.txt");
        services.deleteDocument("/Archiv/Fehler", "TestDocument.txt");
        services.deleteFolder("/Archiv/TestFolder");
    }

    @Test
    public void testListFolderAsJSON() throws Exception {
        JSONObject obj = services.listFolderAsJSON(null, 0);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.get("result") instanceof JSONArray);
        assertTrue(((JSONArray) obj.get("result")).length() == 1);
        assertTrue(((JSONArray) obj.get("result")).get(0) instanceof JSONObject);
        assertTrue(((JSONObject) ((JSONArray) obj.get("result")).get(0)).length() == 3);
        assertEquals("closed", ((JSONObject) ((JSONArray) obj.get("result")).get(0)).getString("state"));
        assertEquals("Archiv", ((JSONObject) ((JSONArray) obj.get("result")).get(0)).getString("data"));
        obj = services.listFolderAsJSON("-1", 0);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.get("result") instanceof JSONArray);
        assertEquals(4, ((JSONArray) obj.get("result")).length());
        for (int i = 0; i < ((JSONArray) obj.get("result")).length(); i++){
            assertEquals("closed", ((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("state"));
            assertThat(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("data"), anyOf(is ("Archiv"), is("Fehler"), is("Unbekannt"), is("Inbox")));
            assertNotNull(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getJSONObject("attr").getString("objectId"));
            assertEquals("folder", ((JSONObject) ((JSONArray) obj.get("result")).get(i)).getJSONObject("attr").getString("rel"));
        }
        obj = services.uploadDocument("/Archiv", System.getProperty("user.dir") + properties.getProperty("testPDF"));
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        obj = services.listFolderAsJSON("-1", -1);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.get("result") instanceof JSONArray);
        assertEquals(4, ((JSONArray) obj.get("result")).length());
        for (int i = 0; i < ((JSONArray) obj.get("result")).length(); i++){
            assertEquals("closed", ((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("state"));
            assertThat(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("data"), anyOf(is ("Archiv"), is("Fehler"), is("Unbekannt"), is("Inbox")));
            assertNotNull(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getJSONObject("attr").getString("objectId"));
            assertEquals("folder", ((JSONObject) ((JSONArray) obj.get("result")).get(i)).getJSONObject("attr").getString("rel"));
        }
        obj = services.listFolderAsJSON("-1", 1);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.get("result") instanceof JSONArray);
        assertEquals(1, ((JSONArray) obj.get("result")).length());
        obj = services.deleteDocument("/Archiv", "Test.pdf");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testGetNodeID() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith(("workspace")));
        obj = services.getNodeId("/Datenverzeichnis/Skripte/recognition.js");
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));;
        obj = services.getNodeId("/Datenverzeichnis/Skripte/doc.xml");
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testFindDocument() throws Exception {
        JSONObject obj = services.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'") ;
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.get("result").toString());
        assertNotNull(result);
        assertTrue(result.getString("name").equalsIgnoreCase("doc.xml"));
    }

    @Test
    public void testGetDocumentContent() throws Exception {
        JSONObject obj = services.getDocumentContent(services.getNodeId("/Datenverzeichnis/Skripte/doc.xml").getString("result"), false);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        String document =  obj.getString("result");
        assertTrue(document.startsWith("<documentTypes"));
        assertTrue(document.indexOf("xmlns:my=\"http://www.schulte.local/archiv\"") != -1);
    }

    @Test
    public void testUploadDocument() throws Exception {
        JSONObject obj = services.uploadDocument("/Archiv", System.getProperty("user.dir") + properties.getProperty("testPDF"));
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        obj = services.deleteDocument("/Archiv", "Test.pdf");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testCreateDocument() throws Exception {
        JSONObject obj = services.uploadDocument("/Archiv", System.getProperty("user.dir") + properties.getProperty("testPDF"));
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        obj = services.createDocument("/Archiv",  "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, null, "none");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.get("result").toString());
        assertNotNull(result);
        assertTrue(result.getString("name").equalsIgnoreCase("TestDocument.txt"));
        obj = services.getDocumentContent(result.getString("objectId"), false);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        String document =  obj.getString("result");
        assertEquals("Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?", document);
        obj = services.deleteDocument("/Archiv", "TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testCreateFolder() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        obj = services.createFolder("/Archiv", "TestFolder");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.get("result").toString());
        assertNotNull(result);
        assertTrue(result.getString("name").equalsIgnoreCase("TestFolder"));
        obj = services.deleteFolder("/Archiv/TestFolder");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testUpdateDocument() throws Exception {
        String content = "";
        JSONObject obj = services.createDocument("/Archiv",  "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, null, "none");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.get("result").toString());
        assertNotNull(result);
        assertTrue(result.getString("name").equalsIgnoreCase("TestDocument.txt"));
        assertNotNull(result.getString("objectId"));
        content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        services.updateDocument(result.getString("objectId"), content, CMISConstants.DOCUMENT_TYPE_TEXT, null, "false", null);
        obj = services.getDocumentContent(result.getString("objectId"), false);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertEquals(content, obj.getString("result"));
        obj = services.deleteDocument("/Archiv", "TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));

        obj = services.createDocument("/Archiv",  "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, null, "major");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        result = new JSONObject(obj.get("result").toString());
        assertNotNull(result);
        assertEquals("1.0", result.getString("versionLabel"));
        assertEquals("Initial Version", result.getString("checkinComment"));
        content = "Dies ist ein neuer Inhalt";
        obj = services.updateDocument(result.getString("objectId"), content, CMISConstants.DOCUMENT_TYPE_TEXT, null, "true", "neuer Versionskommentar");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        result = new JSONObject(obj.get("result").toString());
        assertNotNull(result);
        assertEquals("2.0", result.getString("versionLabel"));
        assertEquals("neuer Versionskommentar", result.getString("checkinComment"));

        obj = services.deleteDocument("/Archiv", "TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testMoveDocument() throws Exception {
        JSONObject oldFolder = services.getNodeId("/Archiv");
        assertNotNull(oldFolder);
        assertTrue(oldFolder.length() >= 2);
        assertNotNull(oldFolder.get("result"));
        assertTrue(oldFolder.get("result").toString(), oldFolder.getBoolean("success"));
        assertTrue(oldFolder.getString("result").startsWith(("workspace")));
        String oldFolderId = oldFolder.getString("result");
        JSONObject newFolder = services.getNodeId("/Archiv/Fehler");
        assertNotNull(newFolder);
        assertTrue(newFolder.length() >= 2);
        assertNotNull(newFolder.get("result"));
        assertTrue(newFolder.getString("result").startsWith(("workspace")));
        String newFolderId = newFolder.getString("result");
        assertTrue(newFolder.get("result").toString(), newFolder.getBoolean("success"));
        services.deleteDocument("/Archiv", "TestDocument.txt");
        String content = "";
        JSONObject document = services.createDocument("/Archiv",  "TestDocument.txt", content, CMISConstants.DOCUMENT_TYPE_TEXT, null, "none");
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
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        obj = services.getNodeId("/Archiv/Fehler/TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith(("workspace")));
        obj = services.deleteDocument("/Archiv/Fehler", "TestDocument.txt");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testLoadProperties() throws Exception {
        String file =  "TestVerteilung/test.properties";
        String fullPath = "file://"+System.getProperty("user.dir").substring(0, System.getProperty("user.dir").lastIndexOf('/') +1)  + file;
        JSONObject obj = services.loadProperties(fullPath);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
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
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 1);
        assertTrue(services.getEntries().size() == 1);
        FileEntry entry =  services.getEntries().iterator().next();
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
        byte[] content = readFile(fileName);
        assertTrue(content.length > 0);
        JSONObject obj = services.extractPDFContent(Base64.encodeBase64String(content));
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith("HerrKlaus SchulteBredeheide 3348161 Münster"));
    }

    @Test
    public void testExtractPDFFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        JSONObject obj = services.extractPDFFile("file://" + System.getProperty("user.dir") + fileName);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
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
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
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
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 2);
        assertTrue(services.getEntries().size() == 2);
        for (FileEntry entry : services.getEntries()){
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
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 2);
        assertTrue(services.getEntries().size() == 2);
        for (FileEntry entry : services.getEntries()){
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
        services.getEntries().add(new FileEntry("Test1", new byte[]{0,1,2}, "Test Inhalt 1"));
        services.getEntries().add(new FileEntry("Test2", new byte[]{2,3,4}, "Test Inhalt 2"));
        obj = services.getDataFromInternalStorage();
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        JSONObject result = (JSONObject) obj.get("result");
        assertNotNull(result);
        assertTrue(result.length() == 2);
        assertTrue(result.has("Test1"));
        assertTrue(result.has("Test2"));
        assertTrue(result.get("Test1") instanceof JSONObject);
        assertTrue(result.get("Test2") instanceof JSONObject);
        JSONObject entry = (JSONObject) result.get("Test1");
        assertTrue(new String(Base64.decodeBase64(entry.getString("data"))).equals(new String(new byte[]{0,1,2})));
        assertTrue(entry.getString("extractedData").equals("Test Inhalt 1"));
        entry = (JSONObject) result.get("Test2");
        assertTrue(new String(Base64.decodeBase64(entry.getString("data"))).equals(new String(new byte[]{2,3,4})));
        assertTrue(entry.getString("extractedData").equals("Test Inhalt 2"));
        obj = services.getDataFromInternalStorage("Test2");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        result = (JSONObject) obj.get("result");
        assertNotNull(result);
        assertTrue(result.length() == 1);
        assertTrue(result.has("Test2"));
        assertTrue(result.get("Test2") instanceof JSONObject);
        entry = (JSONObject) result.get("Test2");
        assertTrue(new String(Base64.decodeBase64(entry.getString("data"))).equals(new String(new byte[]{2,3,4})));
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
        services.getEntries().add(new FileEntry("Test1", new byte[]{0,1,2}, "Test Inhalt 1"));
        JSONObject obj = services.clearInternalStorage();
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(services.getEntries().isEmpty());
    }

    @Test
    public void testOpenFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        JSONObject obj = services.openFile("file://" + System.getProperty("user.dir") + fileName);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        byte[] content = readFile(fileName);
        byte[] contentRead = Base64.decodeBase64(obj.getString("result"));
        assertTrue("Unterschiedliche Länge gelesen!", content.length == contentRead.length);
        for (int i = 0; i < content.length; i++){
            assertTrue("Unterschiedlicher Inhalt gelesen Position: " + i +" !", content[i] == contentRead[i]);
        }
    }

    @Test
    public void testIsURLAvailable() throws Exception {
        String urlString = "http://www.spiegel.de";
        JSONObject obj = services.isURLAvailable(urlString);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getBoolean("result"));
        urlString = "http://www.spiegel.dumm";
        obj = services.isURLAvailable(urlString);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertFalse(obj.getBoolean("result"));
    }
}
