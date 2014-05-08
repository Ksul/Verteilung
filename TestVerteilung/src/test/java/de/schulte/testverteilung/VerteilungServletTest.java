package de.schulte.testverteilung;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests für das Verteilungsservlet
 * diese Test laufen nur mit einem erreichbaren Alfresco Server
 * User: m500288
 * Date: 03.06.13
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */
public class VerteilungServletTest extends AlfrescoTest {

    HttpServletRequest request;
    HttpServletResponse response;
    StubServletOutputStream servletOutputStream = new StubServletOutputStream();
    VerteilungServlet servlet;
    StringWriter sr;
    PrintWriter writer;

    /**
     * Mock für den ServletOutputStream
     */
    private class StubServletOutputStream extends ServletOutputStream {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        public void write(int i) throws IOException {
            baos.write(i);
        }
    }


    @Before
    public void setUp() throws Exception {
        super.setUp();
        servlet = new VerteilungServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(request.getParameter(VerteilungServlet.PARAMETER_SERVER)).thenReturn(properties.getProperty("bindingUrl"));
        when(request.getParameter(VerteilungServlet.PARAMETER_USERNAME)).thenReturn("admin");
        when(request.getParameter(VerteilungServlet.PARAMETER_PASSWORD)).thenReturn(properties.getProperty("password"));
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_SETPARAMETER);
        sr = new StringWriter();
        writer = new PrintWriter(sr);
        when(response.getWriter()).thenReturn(writer);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith(("workspace")));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("Test.pdf");
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv/Fehler");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEFOLDER);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv/TestFolder");
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testDoGetAndDoPost() throws Exception {
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn("unbekannt");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testIsURLAvailable() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_ISURLAVAILABLE);
        when(request.getParameter(VerteilungServlet.PARAMETER_SERVER)).thenReturn(properties.getProperty("host"));
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.getBoolean("result"));
    }

    @Test
    public void testGetNodeId() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Datenverzeichnis/Skripte/doc.xml");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith(("workspace")));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Datenverzeichnis/Skripte/recognition.js");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith(("workspace")));
    }

    @Test
    public void testFindDocument() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_FINDDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_CMISQUERY)).thenReturn("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'");
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
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Datenverzeichnis/Skripte/doc.xml");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETDOCUMENTCONTENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("result"));
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRACT)).thenReturn("false");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertTrue(obj.getBoolean("success"));
        assertTrue(obj.getString("result").indexOf("xmlns:my=\"http://www.schulte.local/archiv\"") != -1);
    }

    @Test
    public void testListFolderAsJSON() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_LISTFOLDERASJSON);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("-1");
        when(request.getParameter(VerteilungServlet.PARAMETER_WITHFOLDER)).thenReturn("0"); // alles suchen
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertTrue(sr.toString().contains("{\"data\":\"Inbox\""));
        assertTrue(sr.toString().contains("{\"data\":\"Fehler\""));
        assertTrue(sr.toString().contains("{\"data\":\"Unbekannt\""));
        assertTrue(sr.toString().contains("{\"data\":\"Archiv\""));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("8e6d4fbd-32f1-41ed-b0a2-b7ff8a9934ab");
        when(request.getParameter(VerteilungServlet.PARAMETER_WITHFOLDER)).thenReturn("1");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
    }

    @Test
    public void testUploadDocument() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_UPLOADDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn(properties.getProperty("testPDF"));
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("Test.pdf");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }

    @Test
    public void testCreateDocument() throws Exception {
        // Dokument erstellen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CREATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn("none");
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(content);
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(CMISConstants.DOCUMENT_TYPE_TEXT);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        JSONObject doc = new JSONObject(obj.getString("result"));
        assertNotNull(doc);
        assertTrue(doc.getString("name").equalsIgnoreCase("TestDocument.txt"));
        sr.getBuffer().delete(0, 9999);
        // Inhalt lesen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETDOCUMENTCONTENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRACT)).thenReturn("false");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertEquals(content, obj.getString("result"));
        // und das Dokument wieder löschen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testCreateFolder() throws Exception {
        // Folder erstellen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CREATEFOLDER);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestFolder");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        JSONObject folder = new JSONObject(obj.getString("result"));
        assertNotNull(folder);
        assertTrue(folder.getString("name").equalsIgnoreCase("TestFolder"));
        // und den Folder wieder löschen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEFOLDER);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv/TestFolder");
        servlet.doPost(request, response);
        writer.flush();
        obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertNotNull(result);
    }

    @Test
    public void testUpdateDocument() throws Exception {
        // Dokument erstellen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CREATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn("none");
        String content = " ";
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(content);
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(CMISConstants.DOCUMENT_TYPE_TEXT);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        JSONObject doc = new JSONObject(obj.getString("result"));
        assertNotNull(doc);
        assertNotNull(doc.getString("objectId"));
        sr.getBuffer().delete(0, 9999);
        // Dokument ändern
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_UPDATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(content);
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(CMISConstants.DOCUMENT_TYPE_TEXT);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
        // Inhalt lesen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETDOCUMENTCONTENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRACT)).thenReturn("false");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertEquals(content, obj.getString("result"));
        sr.getBuffer().delete(0, 9999);
        // und das Dokument wieder löschen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
    }


    @Test
    public void testMoveDocument() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        String currentLocationId = obj.getString("result");
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv/Fehler");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        String destinationId = obj.getString("result");
        sr.getBuffer().delete(0, 9999);
        // Dokument erstellen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CREATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn("none");
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(content);
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(CMISConstants.DOCUMENT_TYPE_TEXT);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        JSONObject doc = new JSONObject(obj.getString("result"));
        assertNotNull(doc);
        assertNotNull(doc.getString("objectId"));
        sr.getBuffer().delete(0, 9999);
        // Dokument verschieben
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn("moveDocument");
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        when(request.getParameter(VerteilungServlet.PARAMETER_CURENTLOCATIONID)).thenReturn(currentLocationId);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONID)).thenReturn(destinationId);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv/Fehler/TestDocument.txt");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        // und das Dokument wieder löschen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONFOLDER)).thenReturn("/Archiv/Fehler");
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractPDFContent() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        byte[] content = readFile(fileName);
        assertTrue(content.length > 0);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTPDFCONTENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content));
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith("HerrKlaus SchulteBredeheide 3348161 Münster"));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractPDFFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTPDFFILE);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("file://" + fileName);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getString("result").startsWith("HerrKlaus SchulteBredeheide 3348161 Münster"));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        byte[] content = readFile(fileName);
        assertTrue(content.length > 0);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTPDFTOINTERNALSTORAGE);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content));
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn(fileName);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 1);
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractZIP() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertNotNull(fileName);
        byte[] content = readFile(fileName);
        assertTrue(content.length > 0);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTZIP);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content));
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractZIPAndExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertNotNull(fileName);
        byte[] content = readFile(fileName);
        assertTrue(content.length > 0);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTZIPANDEXTRACTPDFTOINTERNALSTORAGE);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content));
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 2);
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractZIPToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertNotNull(fileName);
        byte[] content = readFile(fileName);
        assertTrue(content.length > 0);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTZIPTOINTERNALSTORAGE);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content));
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertTrue(obj.getInt("result") == 2);
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testGetDataFromInternalStorage() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETDATAFROMINTERNALSTORAGE);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("Test");
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertFalse(obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testClearInternalStorage() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CLEARINTERNALSTORAGE);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testOpenFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_OPENFILE);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("file://" + fileName);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
        assertFalse(obj.getString("result").isEmpty());
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testOpenPDF() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertNotNull(fileName);
        VerteilungServices services = servlet.getServices();
        services.getEntries().add(new FileEntry(fileName, readFile(fileName)));
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_OPENPDF);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn(fileName);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(servletOutputStream.baos);
        assertTrue(servletOutputStream.baos.toString().startsWith("%PDF"));
    }

    @Test
    public void testLoadProperties() throws Exception {
        String file =  "TestVerteilung/test.properties";
        String fullPath = "file://"+System.getProperty("user.dir").substring(0, System.getProperty("user.dir").lastIndexOf('/') +1)  + file;
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_LOADPROPERTIES);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn(fullPath);
        servlet.doPost(request, response);
        writer.flush();
        assertNotNull(sr);
        JSONObject obj = new JSONObject(sr.toString());
        assertNotNull(obj);
        assertTrue(obj.length() >= 2);
        assertNotNull(obj.get("result"));
        assertTrue(obj.get("result").toString(), obj.getBoolean("success"));
    }
}
