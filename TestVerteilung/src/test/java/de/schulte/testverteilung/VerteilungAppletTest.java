package de.schulte.testverteilung;

import org.apache.commons.codec.binary.Base64;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertThat;

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
        JSONObject obj = applet.setParameter(properties.getProperty("server"), properties.getProperty("binding"), properties.getProperty("user"), properties.getProperty("password"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testgetTicket() throws Exception {
        JSONObject obj = applet.getTicket();
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
    }

    @Test
    public void testGetConnection() throws Exception {
        JSONObject obj = applet.getConnection();
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.get("result"), Matchers.instanceOf(JSONObject.class));
        JSONObject connection = (JSONObject) obj.get("result");
        assertThat(connection.getString("server"), Matchers.equalTo(properties.getProperty("server")));
        assertThat(connection.getString("binding"), Matchers.equalTo(properties.getProperty("binding")));
        assertThat(connection.getString("user"), Matchers.equalTo(properties.getProperty("user")));
        assertThat(connection.getString("password"), Matchers.equalTo(properties.getProperty("password")));
        applet.setParameter(" ", " ", " ", " ");
        obj = applet.getConnection();
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getBoolean("result"), Matchers.is(false));
    }

    @Test
    public void tesGetComments() throws Exception {
        JSONObject obj = applet.getComments("a", "b");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
    }

    @Test
    public void testAddComment() throws Exception {
        JSONObject obj = applet.addComment("a", "b", "c");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
    }

    @Test
    public void testIsURLAvailable() throws Exception {
        String urlString = "http://www.spiegel.de";
        JSONObject obj = applet.isURLAvailable(urlString, "10000");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testListFolderAsJSON() throws Exception {
        JSONObject obj = applet.listFolder("-1", "0");
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testGetNodeId() throws Exception {
        JSONObject obj = applet.getNodeId("/Archiv");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testGetNodeById() throws Exception {
        JSONObject obj = applet.getNodeId("/Archiv");
        assertThat(obj, Matchers.notNullValue());
        obj = applet.getNodeById(obj.getString("result"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testGetNode() throws Exception {
        JSONObject obj = applet.getNode("/Archiv");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testFindDocument() throws Exception {
        JSONObject obj = applet.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'") ;
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testQuery() throws Exception {
        JSONObject obj = applet.query("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'") ;
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testGetDocumentContent() throws Exception {
        JSONObject obj = applet.getDocumentContent("abcde", "false");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
    }

    @Test
    public void testUploadDocument() throws Exception {
        JSONObject obj = applet.uploadDocument("/Archiv", "abcde", "none");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
    }

    @Test
    public void testDeleteDocument() throws Exception {
        JSONObject obj = applet.deleteDocument("/Archiv/abcde");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
    }

    @Test
    public void testCreateDocument() throws Exception {
        JSONObject obj = applet.createDocument("/Archiv", "abcde", "", "", "", "");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
    }

    @Test
    public void testUpdateDocument() throws Exception {
        JSONObject obj = applet.updateDocument("/Archiv", "abcde", "", "", "", "");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
    }

    @Test
    public void testMoveNode() throws Exception {
        JSONObject obj = applet.moveNode("/Archiv", "abcde", "");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.getBoolean("success"), Matchers.is(false));
    }

    @Test
    public void testCreateFolder() throws Exception {
        JSONObject obj = applet.createFolder("/abcde", "abcde");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
    }

    @Test
    public void testDeleteFolder() throws Exception {
        JSONObject obj = applet.deleteFolder("abcde");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
    }

    @Test
    public void testLoadProperties() throws Exception {
        String fileName =  "/test.properties";
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = applet.loadProperties(fullPath);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testFileOperations() throws Exception {
        String file =  "TestVerteilung/Test.test";
        String content = "abcde\näöüßÄÖÜ";
        String dir = System.getProperty("user.dir").replace("\\", "/");
        String fullPath = "file:///" + dir.substring(0, dir.lastIndexOf('/') +1)  + file;
        applet.deleteFile(fullPath);
        JSONObject obj = applet.saveToFile(fullPath, content);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
        byte[] bytes = readFile("Test.test");
        assertThat(content, Matchers.equalTo(new String(bytes)));
        obj = applet.deleteFile(fullPath);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testExtractPDFContent() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        String encodedContent =  Base64.encodeBase64String(content);
        for (int k = 0; k <= Math.ceil(encodedContent.length() / 1000); k++)
            applet.fillParameter(encodedContent.substring(k * 1000, Math.min(encodedContent.length(), k * 1000 + 1000)), k == 0);
        JSONObject obj = applet.extractPDFContent();
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("result"), Matchers.startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
    }

    @Test
    public void testExtractPDFFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = applet.extractPDFFile(fullPath);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("result"), Matchers.startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
    }

    @Test
    public void testExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        String encodedContent =  Base64.encodeBase64String(content);
        for (int k = 0; k <= Math.ceil(encodedContent.length() / 1000); k++)
            applet.fillParameter(encodedContent.substring(k * 1000, Math.min(encodedContent.length(), k * 1000 + 1000)), k == 0);
        JSONObject obj = applet.extractPDFToInternalStorage(System.getProperty("user.dir"));
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("result"), Matchers.is(1));
    }

    @Test
    public void testExtractZIP() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        String encodedContent =  Base64.encodeBase64String(content);
        for (int k = 0; k <= Math.ceil(encodedContent.length() / 1000); k++)
            applet.fillParameter(encodedContent.substring(k * 1000, Math.min(encodedContent.length(), k * 1000 + 1000)), k == 0);
        JSONObject obj = applet.extractZIP();
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
        JSONArray erg = obj.getJSONArray("result");
        assertThat(erg, Matchers.notNullValue());
        assertThat(erg.length(), Matchers.is(2));
        String str = erg.getString(0);
        assertThat(str.length(), Matchers.greaterThan(0));
        str = erg.getString(1);
        assertThat(str.length(), Matchers.greaterThan(0));
    }

    @Test
    public void testExtractZIPAndExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, Matchers.notNullValue());
        String fullPath = System.getProperty("user.dir").replace("\\", "/") + fileName;
        byte[] content = readFile(fullPath);
        assertThat(content.length, Matchers.greaterThan(0));
        String encodedContent =  Base64.encodeBase64String(content);
        for (int k = 0; k <= Math.ceil(encodedContent.length() / 1000); k++)
            applet.fillParameter(encodedContent.substring(k * 1000, Math.min(encodedContent.length(), k * 1000 + 1000)), k == 0);
        JSONObject obj = applet.extractZIPAndExtractPDFToInternalStorage();
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("result"), Matchers.is(2));
    }

    @Test
    public void testExtractZIPToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        String encodedContent =  Base64.encodeBase64String(content);
        for (int k = 0; k <= Math.ceil(encodedContent.length() / 1000); k++)
            applet.fillParameter(encodedContent.substring(k * 1000, Math.min(encodedContent.length(), k * 1000 + 1000)), k == 0);
        assertThat(content.length, Matchers.greaterThan(0));
        JSONObject obj = applet.extractZIPToInternalStorage();
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("result"), Matchers.is(2));
    }

    @Test
    public void testGetDataFromInternalStorage() throws Exception {
        JSONObject obj = applet.getDataFromInternalStorage("abcde");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
        obj = applet.getDataFromInternalStorage();
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
    }

    @Test
    public void testClearInternalStorage() throws Exception {
        JSONObject obj = applet.clearInternalStorage();
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testOpenPDF() throws Exception {
        JSONObject obj = applet.openPDF("test");
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
        assertThat(obj.getString("result"), Matchers.equalTo("PDF konnte nicht gefunden werden!"));
    }

    @Test
    public void testOpenFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        String fullPath = "file:///" + System.getProperty("user.dir").replace("\\", "/") + fileName;
        JSONObject obj = applet.openFile(fullPath);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result").toString(), obj.getBoolean("success"), Matchers.is(true));
    }
}
