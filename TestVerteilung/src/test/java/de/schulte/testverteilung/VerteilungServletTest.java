package de.schulte.testverteilung;

import org.apache.commons.io.FileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: m500288
 * Date: 03.06.13
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */
public class VerteilungServletTest extends AlfrescoTest {

    HttpServletRequest request;
    HttpServletResponse response;
    VerteilungServlet servlet;
    StringWriter sr;
    PrintWriter writer;


    @Before
    public void setUp() throws Exception {
        super.setUp();
        servlet = new VerteilungServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(request.getParameter("server")).thenReturn(host);
        when(request.getParameter("username")).thenReturn("admin");
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("function")).thenReturn("setParameter");
        sr = new StringWriter();
        writer = new PrintWriter(sr);
        when(response.getWriter()).thenReturn(writer);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
    }

    public void testIsURLAvailable() throws Exception {
        when(request.getParameter("function")).thenReturn("isURLAvailable");
        when(request.getParameter("server")).thenReturn(host + "/alfresco/cmis");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.getBoolean("result"));
    }

    @Test
    public void testGetNodeId() throws Exception {
        when(request.getParameter("function")).thenReturn("getNodeId");
        when(request.getParameter("filePath")).thenReturn("/Archiv");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith(("workspace")));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter("filePath")).thenReturn("/Datenverzeichnis/Skripte/doc.xml");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith(("workspace")));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter("filePath")).thenReturn("/Datenverzeichnis/Skripte/recognition.js");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith(("workspace")));
    }

    @Test
    public void testFindDocument() throws Exception {
        when(request.getParameter("function")).thenReturn("findDocument");
        when(request.getParameter("cmisQuery")).thenReturn("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        JSONObject doc = new JSONObject(obj.getString("result"));
        assertNotNull(doc);
        assertTrue(doc.getString("name").equalsIgnoreCase("doc.xml"));
    }

    @Test
    public void testGetDocumentContent() throws Exception {
        when(request.getParameter("function")).thenReturn("getNodeId");
        when(request.getParameter("filePath")).thenReturn("/Datenverzeichnis/Skripte/doc.xml");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter("function")).thenReturn("getDocumentContent");
        when(request.getParameter("documentId")).thenReturn(obj.getString("result"));
        when(request.getParameter("extract")).thenReturn("false");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.getString("result").indexOf("xmlns:my=\"http://www.schulte.local/archiv\"") != -1);
    }

    @Test
    public void testListFolderAsJSON() throws Exception {
        when(request.getParameter("function")).thenReturn("listFolderAsJSON");
        when(request.getParameter("filePath")).thenReturn("-1");
        when(request.getParameter("withFolder")).thenReturn("0"); // alles suchen
        when(request.getParameter("searchFolder")).thenReturn("true");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertTrue(sr.toString().contains("{\"data\":\"Inbox\""));
        assertTrue(sr.toString().contains("{\"data\":\"Fehler\""));
        assertTrue(sr.toString().contains("{\"data\":\"Unbekannt\""));
        assertTrue(sr.toString().contains("{\"data\":\"Archiv\""));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter("searchFolder")).thenReturn("true");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertTrue(sr.toString().contains("{\"data\":\"Inbox\""));
        assertTrue(sr.toString().contains("{\"data\":\"Fehler\""));
        assertTrue(sr.toString().contains("{\"data\":\"Unbekannt\""));
        assertTrue(sr.toString().contains("{\"data\":\"Archiv\""));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter("filePath")).thenReturn("8e6d4fbd-32f1-41ed-b0a2-b7ff8a9934ab");
        when(request.getParameter("withFolder")).thenReturn("1");
        servlet.doPost(request, response);
         writer.flush(); // it may not have been flushed yet...
    }

    @Test
    public void testUploadDocument() throws Exception {
        when(request.getParameter("function")).thenReturn("deleteDocument");
        when(request.getParameter("destinationFolder")).thenReturn("/Archiv");
        when(request.getParameter("fileName")).thenReturn("Test.pdf");
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter("function")).thenReturn("uploadDocument");
        when(request.getParameter("destinationFolder")).thenReturn("/Archiv");
        URL url =  AlfrescoConnectorNewTest.class.getClassLoader().getResource("Test.pdf");
        File file = new File(url.toURI());
        when(request.getParameter("fileName")).thenReturn(file.getPath());
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() == 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter("function")).thenReturn("deleteDocument");
        when(request.getParameter("destinationFolder")).thenReturn("/Archiv");
        when(request.getParameter("fileName")).thenReturn("Test.pdf");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() == 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }
}
