package de.schulte.testverteilung;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.01.14
 * Time: 09:54
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoServicesTest extends AlfrescoTest{

    AlfrescoServices services;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        services = new AlfrescoServices(host, "admin", password);
    }

    @Test
    public void testListFolderAsJSON() throws Exception {

        // Root Eintrag
        JSONObject obj = services.listFolderAsJSON(null, 0);
        assertTrue(obj.length() == 2);
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.get("result") instanceof JSONArray);
        assertTrue(((JSONArray) obj.get("result")).length() == 1);
        assertTrue(((JSONArray) obj.get("result")).get(0) instanceof JSONObject);
        assertTrue(((JSONObject) ((JSONArray) obj.get("result")).get(0)).length() == 3);
        assertEquals("closed", ((JSONObject) ((JSONArray) obj.get("result")).get(0)).getString("state"));
        assertEquals("Archiv", ((JSONObject) ((JSONArray) obj.get("result")).get(0)).getString("data"));
        obj = services.listFolderAsJSON("-1", 0);
        assertTrue(obj.length() == 2);
        assertTrue(obj.getBoolean("success"));
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
        JSONObject obj = services.getNodeId("company_home/Archiv");
        assertNotNull(obj);
        assertTrue(obj.length() == 2);
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith(("workspace")));
    }

    @Test
    public void testFindDocument() throws Exception {
        JSONObject obj = services.findDocument("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'") ;
        assertNotNull(obj);
        assertTrue(obj.length() == 2);
        assertTrue(obj.getBoolean("success"));
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result") instanceof CmisObject);
        assertTrue(((CmisObject) obj.get("result")).getName().equalsIgnoreCase("doc.xml"));
    }
}
