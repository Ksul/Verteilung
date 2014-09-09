package de.schulte.testverteilung;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.codec.binary.Base64;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 10.04.14
 * Time: 16:00
 */
public class VerteilungAppletTest extends AlfrescoTest {

    VerteilungApplet applet = new VerteilungApplet();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        JSONObject obj = applet.setParameter(properties.getProperty("server"), properties.getProperty("bindingUrl"), properties.getProperty("user"), properties.getProperty("password"));
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testgetTicket() throws Exception {
        JSONObject obj = applet.getTicket();
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
    }

    @Test
    public void testIsURLAvailable() throws Exception {
        String urlString = "http://www.spiegel.de";
        JSONObject obj = applet.isURLAvailable(urlString);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testListFolderAsJSON() throws Exception {
        JSONObject obj = applet.listFolder("-1", "0");
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testGetNodeId() throws Exception {
        JSONObject obj = applet.getNodeId("/Archiv");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testFindDocument() throws Exception {
        JSONObject obj = applet.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'") ;
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testGetDocumentContent() throws Exception {
        JSONObject obj = applet.getDocumentContent("abcde", "false");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
    }

    @Test
    public void testUploadDocument() throws Exception {
        JSONObject obj = applet.uploadDocument("/Archiv", "abcde", "none");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
    }

    @Test
    public void testDeleteDocument() throws Exception {
        JSONObject obj = applet.deleteDocument("/Archiv", "abcde");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
    }

    @Test
    public void testCreateDocument() throws Exception {
        JSONObject obj = applet.createDocument("/Archiv", "abcde", "", "", "", "");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
    }

    @Test
    public void testUpdateDocument() throws Exception {
        JSONObject obj = applet.updateDocument("/Archiv", "abcde", "", "", "", "");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
    }

    @Test
    public void testMoveDocument() throws Exception {
        JSONObject obj = applet.moveDocument("/Archiv", "abcde", "");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
    }

    @Test
    public void testCreateFolder() throws Exception {
        JSONObject obj = applet.createFolder("/abcde", "abcde");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
    }

    @Test
    public void testDeleteFolder() throws Exception {
        JSONObject obj = applet.deleteFolder("abcde");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
    }

    @Test
    public void testLoadProperties() throws Exception {
        String fileName =  "/test.properties";
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = applet.loadProperties(fullPath);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testFileOperations() throws Exception {
        String file =  "TestVerteilung/Test.test";
        String content = "abcde\näöüßÄÖÜ";
        String dir = System.getProperty("user.dir").replace("\\", "/");
        String fullPath = "file:///" + dir.substring(0, dir.lastIndexOf('/') +1)  + file;
        applet.deleteFile(fullPath);
        JSONObject obj = applet.saveToFile(fullPath, content);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        byte[] bytes = readFile("Test.test");
        assertTrue(new String(bytes).equals(content));
        obj = applet.deleteFile(fullPath);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testExtractPDFContent() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertTrue(content.length > 0);
        String encodedContent =  Base64.encodeBase64String(content);
        for (int k = 0; k <= Math.ceil(encodedContent.length() / 1000); k++)
            applet.fillParameter(encodedContent.substring(k * 1000, Math.min(encodedContent.length(), k * 1000 + 1000)), k == 0);
        JSONObject obj = applet.extractPDFContent();
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith("HerrKlaus SchulteBredeheide 3348161 Münster"));
    }

    @Test
    public void testExtractPDFFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = applet.extractPDFFile(fullPath);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith("HerrKlaus SchulteBredeheide 3348161 Münster"));
    }

    @Test
    public void testExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertTrue(content.length > 0);
        String encodedContent =  Base64.encodeBase64String(content);
        for (int k = 0; k <= Math.ceil(encodedContent.length() / 1000); k++)
            applet.fillParameter(encodedContent.substring(k * 1000, Math.min(encodedContent.length(), k * 1000 + 1000)), k == 0);
        JSONObject obj = applet.extractPDFToInternalStorage(fileName, System.getProperty("user.dir"));
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 1);
    }

    @Test
    public void testExtractZIP() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertNotNull(fileName);
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertTrue(content.length > 0);
        String encodedContent =  Base64.encodeBase64String(content);
        for (int k = 0; k <= Math.ceil(encodedContent.length() / 1000); k++)
            applet.fillParameter(encodedContent.substring(k * 1000, Math.min(encodedContent.length(), k * 1000 + 1000)), k == 0);
        JSONObject obj = applet.extractZIP();
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
    public void testExtractZIPAndExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertNotNull(fileName);
        String fullPath = System.getProperty("user.dir").replace("\\", "/") + fileName;
        byte[] content = readFile(fullPath);
        assertTrue(content.length > 0);
        String encodedContent =  Base64.encodeBase64String(content);
        for (int k = 0; k <= Math.ceil(encodedContent.length() / 1000); k++)
            applet.fillParameter(encodedContent.substring(k * 1000, Math.min(encodedContent.length(), k * 1000 + 1000)), k == 0);
        JSONObject obj = applet.extractZIPAndExtractPDFToInternalStorage();
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 2);
    }

    @Test
    public void testExtractZIPToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertNotNull(fileName);
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        String encodedContent =  Base64.encodeBase64String(content);
        for (int k = 0; k <= Math.ceil(encodedContent.length() / 1000); k++)
            applet.fillParameter(encodedContent.substring(k * 1000, Math.min(encodedContent.length(), k * 1000 + 1000)), k == 0);
        assertTrue(content.length > 0);
        JSONObject obj = applet.extractZIPToInternalStorage();
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 2);
    }

    @Test
    public void testGetDataFromInternalStorage() throws Exception {
        JSONObject obj = applet.getDataFromInternalStorage("abcde");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
        obj = applet.getDataFromInternalStorage();
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
    }

    @Test
    public void testClearInternalStorage() throws Exception {
        JSONObject obj = applet.clearInternalStorage();
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.getBoolean("success"));
    }

    @Test
    public void testOpenPDF() throws Exception {
        JSONObject obj = applet.openPDF("test");
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
        assertEquals("PDF konnte nicht gefunden werden!", obj.get("result"));
    }

    @Test
    public void testOpenFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = applet.openFile(fullPath);
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }
}
