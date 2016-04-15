package de.schulte.testverteilung;

import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests für das Verteilungsservlet
 * diese Test laufen nur mit einem erreichbaren Alfresco Server
 * User: m500288
 * Date: 03.06.13
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */
public class VerteilungServletTest extends AlfrescoTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;
    private StubServletOutputStream servletOutputStream = new StubServletOutputStream();
    private VerteilungServlet servlet;
    private StringWriter sr;
    private PrintWriter writer;

    /**
     * Mock für den ServletOutputStream
     */
    private class StubServletOutputStream extends ServletOutputStream {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        public void write(int i) throws IOException {
            baos.write(i);
        }
    }

    private class TestVerteilungServlet extends VerteilungServlet {

        public ServletContext getServletContext() {
            return servletContext;
        }
    }


    @Before
    public void setUp() throws Exception {
        super.setUp();
        servlet = new TestVerteilungServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        servletContext = mock(ServletContext.class);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                String theString = (String) invocation.getArguments()[0];
                return "file:///" + System.getProperty("user.dir").replace("\\", "/") + theString;
            }
        }).when(servletContext).getRealPath(anyString());
        when(response.getOutputStream()).thenReturn(servletOutputStream);
        when(request.getParameter(VerteilungServlet.PARAMETER_SERVER)).thenReturn(properties.getProperty("server"));
        when(request.getParameter(VerteilungServlet.PARAMETER_BINDING)).thenReturn(properties.getProperty("bindingUrl"));
        when(request.getParameter(VerteilungServlet.PARAMETER_USERNAME)).thenReturn(properties.getProperty("user"));
        when(request.getParameter(VerteilungServlet.PARAMETER_PASSWORD)).thenReturn(properties.getProperty("password"));
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_SETPARAMETER);
        sr = new StringWriter();
        writer = new PrintWriter(sr);
        when(response.getWriter()).thenReturn(writer);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv/TestFolder");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        String testFolderId = obj.getString("result");
        sr.getBuffer().delete(0, 9999);

        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv/Test.pdf");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        if (obj.getBoolean("success")) {
            when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
            when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("result"));
            servlet.doPost(request, response);
            writer.flush();
            sr.getBuffer().delete(0, 9999);
        }
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv/TestDocument.txt");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        if (obj.getBoolean("success")) {
            when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
            when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("result"));
            servlet.doPost(request, response);
            writer.flush();
            sr.getBuffer().delete(0, 9999);
        }
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv/Fehler/TestDocument.txt");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        if (obj.getBoolean("success")) {
            when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
            when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("result"));
            servlet.doPost(request, response);
            writer.flush();
            sr.getBuffer().delete(0, 9999);
        }
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEFOLDER);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(testFolderId);
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testDoGetAndDoPost() throws Exception {
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn("unbekannt");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testIsURLAvailable() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_ISURLAVAILABLE);
        when(request.getParameter(VerteilungServlet.PARAMETER_SERVER)).thenReturn(properties.getProperty("server"));
        when(request.getParameter(VerteilungServlet.PARAMETER_TIMEOUT)).thenReturn("10000");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getBoolean("result"), Matchers.is(true));
    }

    @Test
    public void testGetTicket() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETTICKET);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject ticket = (JSONObject) obj.get("result");
        assertThat(((JSONObject) ticket.get("data")).getString("ticket"), Matchers.startsWith("TICKET_"));
    }

    @Test
    public void testGetComments() throws Exception {

        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        // Dokument erstellen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CREATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("result"));
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn(VersioningState.MINOR.value());
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content.getBytes()));
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(CMISConstants.DOCUMENT_TYPE_TEXT);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject document = new JSONObject(obj.getString("result"));

        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETTICKET);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject ticket = (JSONObject) obj.get("result");

        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_ADDCOMMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(document.getString("objectId"));
        when(request.getParameter(VerteilungServlet.PARAMETER_TICKET)).thenReturn(((JSONObject) ticket.get("data")).getString("ticket"));
        when(request.getParameter(VerteilungServlet.PARAMETER_COMMENT)).thenReturn("Testkommentar");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));

        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETCOMMENTS);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(document.getString("objectId"));
        when(request.getParameter(VerteilungServlet.PARAMETER_TICKET)).thenReturn(((JSONObject) ticket.get("data")).getString("ticket"));
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject comment = (JSONObject) obj.get("result");
        assertThat(((JSONObject) ((JSONArray) comment.get("items")).get(0)).getString("content"), Matchers.equalTo("Testkommentar"));

        // und das Dokument wieder löschen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(document.getString("objectId"));
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testGetNodeId() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Datenverzeichnis/Skripte/doc.xml");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Datenverzeichnis/Skripte/recognition.js");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testFindDocument() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_FINDDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_CMISQUERY)).thenReturn("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject doc = (JSONObject) ((JSONArray) obj.get("result")).get(0);
        assertThat(doc, Matchers.notNullValue());
        assertThat(doc.getString("name"), Matchers.equalToIgnoringCase("doc.xml"));
    }

    @Test
    public void testGetDocumentContent() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Datenverzeichnis/Skripte/doc.xml");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETDOCUMENTCONTENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("result"));
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRACT)).thenReturn("false");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("result"), Matchers.containsString("xmlns:my=\"http://www.schulte.local/archiv\""));
    }

    @Test
    public void testListFolder() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_LISTFOLDERASJSON);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("-1");
        when(request.getParameter(VerteilungServlet.PARAMETER_WITHFOLDER)).thenReturn("0"); // alles suchen
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr.toString(), Matchers.containsString("\"name\":\"Inbox\""));
        assertThat(sr.toString(), Matchers.containsString("\"name\":\"Fehler\""));
        assertThat(sr.toString(), Matchers.containsString("\"name\":\"Unbekannt\""));
        assertThat(sr.toString(), Matchers.containsString("\"name\":\"Dokumente\""));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("8e6d4fbd-32f1-41ed-b0a2-b7ff8a9934ab");
        when(request.getParameter(VerteilungServlet.PARAMETER_WITHFOLDER)).thenReturn("1");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
    }

    @Test
    public void testUploadDocument() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        String archivId = obj.getString("result");
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_UPLOADDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(archivId);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn(System.getProperty("user.dir") + properties.getProperty("testPDF"));
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn(VersioningState.MINOR.value());
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("result"));
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testCreateDocument() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        // Dokument erstellen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CREATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("result"));
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn(VersioningState.MINOR.value());
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content.getBytes()));
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(CMISConstants.DOCUMENT_TYPE_TEXT);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject doc = new JSONObject(obj.getString("result"));
        assertThat(doc, Matchers.notNullValue());
        assertThat(doc.getString("name"), Matchers.equalToIgnoringCase("TestDocument.txt"));
        sr.getBuffer().delete(0, 9999);
        // Inhalt lesen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETDOCUMENTCONTENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRACT)).thenReturn("false");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("result"), Matchers.equalTo(content));
        // und das Dokument wieder löschen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testCreateFolder() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        // Folder erstellen
        String extraProperties = "{'cmis:folder': {'cmis:objectTypeId': 'cmis:folder', 'cmis:name': 'Testfolder'}, 'P:cm:titled':{'cm:title': 'Testtitel', 'cm:description':'Dies ist ein Test Folder'}}";
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CREATEFOLDER);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("result"));
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRAPROPERTIES)).thenReturn(extraProperties);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject folder = new JSONObject(obj.getString("result"));
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder.getString("name"), Matchers.equalToIgnoringCase("TestFolder"));
        // und den Folder wieder löschen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEFOLDER);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(folder.getString("objectId"));
        servlet.doPost(request, response);
        writer.flush();
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject result = new JSONObject(obj.getString("result"));
        assertNotNull(result);
    }

    @Test
    public void testUpdateDocument() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        // Dokument erstellen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CREATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("result"));
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn(VersioningState.MAJOR.value());
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRAPROPERTIES)).thenReturn("{'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}");
        String content = " ";
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content.getBytes()));
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(CMISConstants.DOCUMENT_TYPE_TEXT);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject doc = new JSONObject(obj.getString("result"));
        assertThat(doc, Matchers.notNullValue());
        assertThat(doc.getString("objectId"), Matchers.notNullValue());
        assertThat(doc.getString("versionLabel"), Matchers.equalTo("1.0"));
        sr.getBuffer().delete(0, 9999);
        // Dokument ändern
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_UPDATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content.getBytes()));
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(CMISConstants.DOCUMENT_TYPE_TEXT);
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn(VersioningState.MAJOR.value());
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        doc = new JSONObject(obj.getString("result"));
        assertThat(doc, Matchers.notNullValue());
        assertThat(doc.getString("objectId"), Matchers.notNullValue());
        assertEquals("2.0", doc.getString("versionLabel"));
        sr.getBuffer().delete(0, 9999);
        // Inhalt lesen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETDOCUMENTCONTENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRACT)).thenReturn("false");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertEquals(content, obj.getString("result"));
        sr.getBuffer().delete(0, 9999);

        // Dokument ändern
        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33', 'my:tax':'true'}, 'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_UPDATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(null);
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRAPROPERTIES)).thenReturn(extraProperties);
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(CMISConstants.DOCUMENT_TYPE_TEXT);
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn(VersioningState.MINOR.value());
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONCOMMENT)).thenReturn("1. Versionskommentar");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        doc = new JSONObject(obj.getString("result"));
        assertThat(doc, Matchers.notNullValue());
        // wegen der 2 Aspekte nicht 2.1 sondern 2.3
        assertThat(doc.getString("versionLabel"), Matchers.equalTo("2.3"));
        assertThat(doc.getString("checkinComment"), Matchers.equalTo("1. Versionskommentar"));
        assertThat(doc.getString("amount"), Matchers.equalTo("25.33"));
        assertThat(doc.getBoolean("tax"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        // und das Dokument wieder löschen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn("/Archiv/TestDocument.txt");
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
    }


    @Test
    public void testMoveNode() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String currentLocationId = obj.getString("result");
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv/Fehler");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        String destinationId = obj.getString("result");
        sr.getBuffer().delete(0, 9999);
        // Dokument erstellen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CREATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(currentLocationId);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn(VersioningState.MINOR.value());
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(content);
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(CMISConstants.DOCUMENT_TYPE_TEXT);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject doc = new JSONObject(obj.getString("result"));
        assertThat(doc, Matchers.notNullValue());
        assertThat(doc.getString("objectId"), Matchers.notNullValue());
        sr.getBuffer().delete(0, 9999);
        // Dokument verschieben
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn("moveNode");
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        when(request.getParameter(VerteilungServlet.PARAMETER_CURENTLOCATIONID)).thenReturn(currentLocationId);
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONID)).thenReturn(destinationId);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Archiv/Fehler/TestDocument.txt");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        // und das Dokument wieder löschen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_DELETEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("result"));
        servlet.doPost(request, response);
        writer.flush();
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractPDFContent() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTPDFCONTENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content));
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("result"), Matchers.startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractPDFFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTPDFFILE);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn(fileName);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("result"), Matchers.startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTPDFTOINTERNALSTORAGE);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content));
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn(fileName);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("result"), Matchers.is(1));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractZIP() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTZIP);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content));
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractZIPAndExtractPDFToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTZIPANDEXTRACTPDFTOINTERNALSTORAGE);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content));
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("result"), Matchers.is(2));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testExtractZIPToInternalStorage() throws Exception {
        String fileName = properties.getProperty("testZIP");
        assertThat(fileName, Matchers.notNullValue());
        byte[] content = readFile(System.getProperty("user.dir") + fileName);
        assertThat(content.length, Matchers.greaterThan(0));
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_EXTRACTZIPTOINTERNALSTORAGE);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content));
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("result"), Matchers.is(2));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testGetDataFromInternalStorage() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETDATAFROMINTERNALSTORAGE);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("Test");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testClearInternalStorage() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CLEARINTERNALSTORAGE);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testOpenFile() throws Exception {
        String fileName = properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_OPENFILE);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn(fileName);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("result"), Matchers.notNullValue());
        assertThat(obj.get("result") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("result").isEmpty(), Matchers.is(false));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testOpenPDF() throws Exception {
        String fileName = System.getProperty("user.dir") + properties.getProperty("testPDF");
        assertThat(fileName, Matchers.notNullValue());
        VerteilungServices services = servlet.getServices();
        services.getEntries().add(new FileEntry(fileName, readFile(fileName)));
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_OPENPDF);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn(fileName);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(servletOutputStream.baos, Matchers.notNullValue());
        assertThat(servletOutputStream.baos.toString(), Matchers.startsWith("%PDF"));
    }
}
