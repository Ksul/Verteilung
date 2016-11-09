package de.schulte.testverteilung;

import org.apache.chemistry.opencmis.client.api.CmisObject;
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
import javax.servlet.WriteListener;
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
        @Override
        public void write(int i) throws IOException {
            baos.write(i);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

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
        when(request.getParameter(VerteilungServlet.PARAMETER_BINDING)).thenReturn(properties.getProperty("binding"));
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
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testDoGetAndDoPost() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn("unbekannt");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testGetTitles() throws Exception{
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETTITLES);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.get("data"), Matchers.instanceOf(JSONArray.class));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testGetConnection() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETCONNECTION);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject connection = (JSONObject) obj.get("data");
        assertThat(connection.getString("server"), Matchers.equalTo(properties.getProperty("server")));
        assertThat(connection.getString("binding"), Matchers.equalTo(properties.getProperty("binding")));
        assertThat(connection.getString("user"), Matchers.equalTo(properties.getProperty("user")));
        assertThat(connection.getString("password"), Matchers.equalTo(properties.getProperty("password")));
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_SETPARAMETER);
        when(request.getParameter(VerteilungServlet.PARAMETER_SERVER)).thenReturn(" ");
        when(request.getParameter(VerteilungServlet.PARAMETER_BINDING)).thenReturn(" ");
        when(request.getParameter(VerteilungServlet.PARAMETER_USERNAME)).thenReturn(" ");
        when(request.getParameter(VerteilungServlet.PARAMETER_PASSWORD)).thenReturn(" ");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETCONNECTION);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getBoolean("data"), Matchers.is(false));
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
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getBoolean("data"), Matchers.is(true));
    }

    @Test
    public void testGetTicket() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETTICKET);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject ticket = (JSONObject) obj.get("data");
        assertThat(((JSONObject) ticket.get("data")).getString("ticket"), Matchers.startsWith("TICKET_"));
    }

    @Test
    public void testGetComments() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);

        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETTICKET);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject ticket = (JSONObject) obj.get("data");

        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_ADDCOMMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(document.getId());
        when(request.getParameter(VerteilungServlet.PARAMETER_TICKET)).thenReturn(((JSONObject) ticket.get("data")).getString("ticket"));
        when(request.getParameter(VerteilungServlet.PARAMETER_COMMENT)).thenReturn("Testkommentar");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));

        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETCOMMENTS);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(document.getId());
        when(request.getParameter(VerteilungServlet.PARAMETER_TICKET)).thenReturn(((JSONObject) ticket.get("data")).getString("ticket"));
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        sr.getBuffer().delete(0, 9999);
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject comment = (JSONObject) obj.get("data");
        assertThat(((JSONObject) ((JSONArray) comment.get("items")).get(0)).getString("content"), Matchers.equalTo("Testkommentar"));

        document.delete();
    }

    @Test
    public void testGetNodeId() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Datenverzeichnis/Skripte/backup.js.sample");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testFindDocument() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_FINDDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_CMISQUERY)).thenReturn("SELECT * from cmis:document where cmis:name='backup.js.sample'");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject doc = (JSONObject) ((JSONArray) obj.get("data")).get(0);
        assertThat(doc, Matchers.notNullValue());
        assertThat(doc.getString("name"), Matchers.equalToIgnoringCase("backup.js.sample"));
    }

    @Test
    public void testQuery() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_QUERY);
        when(request.getParameter(VerteilungServlet.PARAMETER_CMISQUERY)).thenReturn("SELECT cmis:objectId, cmis:name from cmis:document where cmis:name='backup.js.sample'");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
    }

    @Test
    public void testGetDocumentContent() throws Exception {
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/Datenverzeichnis/Skripte/backup.js.sample");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETDOCUMENTCONTENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(obj.getString("data"));
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRACT)).thenReturn("false");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("data"), Matchers.startsWith("//"));
    }

    @Test
    public void testListFolder() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        buildDocument("TestDocument", folder);
        buildDocument("TestDocument1", folder);
        buildDocument("TestDocument2", folder);
        buildTestFolder("FolderTest", folder);

        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_LISTFOLDERASJSON);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn(folder.getId());
        when(request.getParameter(VerteilungServlet.PARAMETER_ORDER)).thenReturn(null);
        when(request.getParameter(VerteilungServlet.PARAMETER_ORDERDIRECTION)).thenReturn(null);
        when(request.getParameter(VerteilungServlet.PARAMETER_WITHFOLDER)).thenReturn("0"); // alles suchen

        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        assertThat(sr.toString(), Matchers.containsString("\"name\":\"TestDocument\""));
        assertThat(sr.toString(), Matchers.containsString("\"name\":\"TestDocument1\""));
        assertThat(sr.toString(), Matchers.containsString("\"name\":\"TestDocument2\""));
        assertThat(sr.toString(), Matchers.containsString("\"name\":\"FolderTest\""));
    }

    @Test
    public void testUploadDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);

        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_UPLOADDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(folder.getId());
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn(System.getProperty("user.dir") + properties.getProperty("testPDF"));
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn(VersioningState.MINOR.value());
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
    }

    @Test
    public void testCreateDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);

        // Dokument erstellen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CREATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(folder.getId());
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("TestDocument.txt");
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn(VersioningState.MINOR.value());
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content.getBytes()));
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(VerteilungConstants.DOCUMENT_TYPE_TEXT);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject doc = new JSONObject(obj.getString("data"));
        assertThat(doc, Matchers.notNullValue());
        assertThat(doc.getString("name"), Matchers.equalToIgnoringCase("TestDocument.txt"));
        sr.getBuffer().delete(0, 9999);
     }

    @Test
    public void testCreateFolder() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);

        // Folder erstellen
        String extraProperties = "{'cmis:folder': {'cmis:objectTypeId': 'cmis:folder', 'cmis:name': 'FolderTest'}, 'P:cm:titled':{'cm:title': 'Testtitel', 'cm:description':'Dies ist ein Test Folder'}}";
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_CREATEFOLDER);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(folder.getId());
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRAPROPERTIES)).thenReturn(extraProperties);
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject fold = new JSONObject(obj.getString("data"));
        assertThat(fold, Matchers.notNullValue());
        assertThat(fold.getString("name"), Matchers.equalToIgnoringCase("FolderTest"));
    }

    @Test
    public void testUpdateDocument() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);

               // Dokument ändern
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_UPDATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(document.getId());
        String content = "Dies ist ein Inhalt mit Umlauten: äöüßÄÖÜ/?";
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(Base64.encodeBase64String(content.getBytes()));
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(VerteilungConstants.DOCUMENT_TYPE_TEXT);
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn(VersioningState.MAJOR.value());
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        JSONObject doc = new JSONObject(obj.getString("data"));
        assertThat(doc, Matchers.notNullValue());
        assertThat(doc.getString("objectId"), Matchers.notNullValue());
        assertEquals("1.0", doc.getString("versionLabel"));
        sr.getBuffer().delete(0, 9999);
        // Inhalt lesen
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETDOCUMENTCONTENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRACT)).thenReturn("false");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertEquals(content, obj.getString("data"));
        sr.getBuffer().delete(0, 9999);

        // Dokument ändern
        String extraProperties = "{'P:cm:titled':{'cm:description':'Testdokument'}, 'P:cm:emailed':{'cm:sentdate':'" + new Date().getTime() + "'}, 'P:my:amountable':{'my:amount':'25.33', 'my:tax':'true'}, 'D:my:archivContent':{'my:person':'Katja', 'my:documentDate':'" + new Date().getTime() + "'}}";
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_UPDATEDOCUMENT);
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(doc.getString("objectId"));
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTTEXT)).thenReturn(null);
        when(request.getParameter(VerteilungServlet.PARAMETER_EXTRAPROPERTIES)).thenReturn(extraProperties);
        when(request.getParameter(VerteilungServlet.PARAMETER_MIMETYPE)).thenReturn(VerteilungConstants.DOCUMENT_TYPE_TEXT);
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONSTATE)).thenReturn(VersioningState.MINOR.value());
        when(request.getParameter(VerteilungServlet.PARAMETER_VERSIONCOMMENT)).thenReturn("1. Versionskommentar");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        doc = new JSONObject(obj.getString("data"));
        assertThat(doc, Matchers.notNullValue());
        // wegen der 2 Aspekte nicht 2.1 sondern 2.3
        assertThat(doc.getString("versionLabel"), Matchers.equalTo("1.1"));
        assertThat(doc.getString("checkinComment"), Matchers.equalTo("1. Versionskommentar"));
        assertThat(doc.getDouble("amount"), Matchers.equalTo(25.33));
        assertThat(doc.getBoolean("tax"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);

    }


    @Test
    public void testMoveNode() throws Exception {
        CmisObject folder = buildTestFolder("TestFolder", null);
        CmisObject document = buildDocument("TestDocument", folder);
        CmisObject newFolder = buildTestFolder("FolderTest", null);


        // Dokument verschieben
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn("moveNode");
        when(request.getParameter(VerteilungServlet.PARAMETER_DOCUMENTID)).thenReturn(document.getId());
        when(request.getParameter(VerteilungServlet.PARAMETER_CURENTLOCATIONID)).thenReturn(folder.getId());
        when(request.getParameter(VerteilungServlet.PARAMETER_DESTINATIONID)).thenReturn(newFolder.getId());
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        JSONObject obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FUNCTION)).thenReturn(VerteilungServlet.FUNCTION_GETNODEID);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILEPATH)).thenReturn("/FolderTest/TestDocument");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
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
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("data"), Matchers.startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
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
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("data"), Matchers.startsWith("Herr\nKlaus Schulte\nBredeheide 33\n48161 Münster"));
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
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("data"), Matchers.is(1));
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
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
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
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("data"), Matchers.is(2));
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
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getInt("data"), Matchers.is(2));
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
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.getBoolean("success"), Matchers.is(false));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter(VerteilungServlet.PARAMETER_FILENAME)).thenReturn("Test");
        servlet.doPost(request, response);
        writer.flush();
        assertThat(sr, Matchers.notNullValue());
        obj = new JSONObject(sr.toString());
        assertThat(obj, Matchers.notNullValue());
        assertThat(obj.length(), Matchers.greaterThanOrEqualTo(2));
        assertThat(obj.get("data"), Matchers.notNullValue());
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
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
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
        assertThat(obj.get("data"), Matchers.notNullValue());
        assertThat(obj.get("data") + (obj.has("error") ? obj.getString("error") : ""), obj.getBoolean("success"), Matchers.is(true));
        assertThat(obj.getString("data").isEmpty(), Matchers.is(false));
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
