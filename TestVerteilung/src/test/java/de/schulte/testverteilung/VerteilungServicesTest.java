package de.schulte.testverteilung;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

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
    }

    @Test
    public void testListFolderAsJSON() throws Exception {

        // Root Eintrag
        JSONObject obj = services.listFolderAsJSON(null, 0);
        assertTrue(obj.length() == 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.get("result") instanceof JSONArray);
        assertTrue(((JSONArray) obj.get("result")).length() == 1);
        assertTrue(((JSONArray) obj.get("result")).get(0) instanceof JSONObject);
        assertTrue(((JSONObject) ((JSONArray) obj.get("result")).get(0)).length() == 3);
        assertEquals("closed", ((JSONObject) ((JSONArray) obj.get("result")).get(0)).getString("state"));
        assertEquals("Archiv", ((JSONObject) ((JSONArray) obj.get("result")).get(0)).getString("data"));
        obj = services.listFolderAsJSON("-1", 0);
        assertTrue(obj.length() == 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.get("result") instanceof JSONArray);
        assertTrue(((JSONArray) obj.get("result")).length() == 4);
        for (int i = 0; i < ((JSONArray) obj.get("result")).length(); i++){
            assertEquals("closed", ((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("state"));
            assertThat(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getString("data"), anyOf(is ("Archiv"), is("Fehler"), is("Unbekannt"), is("Inbox")));
            assertNotNull(((JSONObject) ((JSONArray) obj.get("result")).get(i)).getJSONObject("attr").getString("id"));
            assertEquals("folder", ((JSONObject) ((JSONArray) obj.get("result")).get(i)).getJSONObject("attr").getString("rel"));
        }
    }

    @Test
    public void testGetNodeID() throws Exception {
        JSONObject obj = services.getNodeId("/Archiv");
        assertNotNull(obj);
        assertTrue(obj.length() == 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith(("workspace")));
        obj = services.getNodeId("/Datenverzeichnis/Skripte/recognition.js");
        assertTrue(obj.length() == 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));;
        obj = services.getNodeId("/Datenverzeichnis/Skripte/doc.xml");
        assertTrue(obj.length() == 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testFindDocument() throws Exception {
        JSONObject obj = services.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'") ;
        assertNotNull(obj);
        assertTrue(obj.length() == 2);
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
        assertTrue(obj.length() == 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        String document =  obj.getString("result");
        Assert.assertTrue(document.startsWith("<documentTypes"));
        Assert.assertTrue(document.indexOf("xmlns:my=\"http://www.schulte.local/archiv\"") != -1);
    }

    @Test
    public void testUploadDocument() throws Exception {
        services.deleteDocument("/Archiv", "Test.pdf");
        JSONObject obj = services.uploadDocument("/Archiv", properties.getProperty("testFile"));
        assertNotNull(obj);
        assertTrue(obj.length() == 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        obj = services.deleteDocument("/Archiv", "Test.pdf");
        assertNotNull(obj);
        assertTrue(obj.length() == 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testLoadProperties() throws Exception {
        String file =  "TestVerteilung/test.properties";
        String fullPath = "file://"+System.getProperty("user.dir").substring(0, System.getProperty("user.dir").lastIndexOf('/') +1)  + file;
        JSONObject obj = services.loadProperties(fullPath);
        assertNotNull(obj);
        assertTrue(obj.length() == 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        JSONObject props = new JSONObject(obj.get("result").toString());
        assertNotNull(props);
        assertTrue(props.length() > 0);
    }
}
