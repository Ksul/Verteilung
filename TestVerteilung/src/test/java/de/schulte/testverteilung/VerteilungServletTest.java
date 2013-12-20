package de.schulte.testverteilung;

import org.apache.commons.io.FileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: m500288
 * Date: 03.06.13
 * Time: 16:38
 * To change this template use File | Settings | File Templates.
 */
public class VerteilungServletTest {

    HttpServletRequest request;
    HttpServletResponse response;
    VerteilungServlet servlet;
    StringWriter sr;
    PrintWriter writer;


    @Before
    public void setUp() throws Exception {
        servlet = new VerteilungServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(request.getParameter("server")).thenReturn("https://ksul.spdns.org");
        when(request.getParameter("username")).thenReturn("admin");
        when(request.getParameter("password")).thenReturn("admin");
        when(request.getParameter("proxyHost")).thenReturn("www-proxy");
        when(request.getParameter("proxyPort")).thenReturn("8080");
        sr = new StringWriter();
        writer = new PrintWriter(sr);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void testListFolderAsJSON() throws Exception {
        when(request.getParameter("function")).thenReturn("listFolderAsJSON");
        when(request.getParameter("filePath")).thenReturn("-1");
        when(request.getParameter("withFolder")).thenReturn("0"); // alles suchen
        when(request.getParameter("searchFolder")).thenReturn("true");
        servlet.doPost(request, response);
        verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...
        writer.flush(); // it may not have been flushed yet...
        System.out.println(sr.toString()) ;
        assertTrue(sr.toString().contains("{\"data\":\"Inbox\""));
        assertTrue(sr.toString().contains("{\"data\":\"Fehler\""));
        assertTrue(sr.toString().contains("{\"data\":\"Unbekannt\""));
        assertTrue(sr.toString().contains("{\"data\":\"Archiv\""));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter("searchFolder")).thenReturn("true");
        servlet.doPost(request, response);
        verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...
        writer.flush(); // it may not have been flushed yet...
        System.out.println(sr.toString()) ;
        assertTrue(sr.toString().contains("{\"data\":\"Inbox\""));
        assertTrue(sr.toString().contains("{\"data\":\"Fehler\""));
        assertTrue(sr.toString().contains("{\"data\":\"Unbekannt\""));
        assertTrue(sr.toString().contains("{\"data\":\"Archiv\""));
        sr.getBuffer().delete(0, 9999);
        when(request.getParameter("filePath")).thenReturn("8e6d4fbd-32f1-41ed-b0a2-b7ff8a9934ab");
        when(request.getParameter("withFolder")).thenReturn("1");
        servlet.doPost(request, response);
        verify(request, atLeast(1)).getParameter("username"); // only if you want to verify username was called...
        writer.flush(); // it may not have been flushed yet...
        System.out.println(sr.toString()) ;

    }

    @Test
     public void testGetNodeId() throws Exception {
        when(request.getParameter("function")).thenReturn("getNodeId");
        when(request.getParameter("cmisQuery")).thenReturn("SELECT * from cmis:folder where CONTAINS('PATH:\"//app:company_home/cm:Archiv/cm:Fehler\"')");
        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        System.out.println(sr.toString()) ;
        assertTrue(sr.toString().contains("success\":[true]"));
    }

    @Test
    public void testIsURLAvailable() throws Exception {
        when(request.getParameter("function")).thenReturn("isURLAvailable");
        when(request.getParameter("server")).thenReturn("http://ksul.dyndns.org:9080");
        when(request.getParameter("proxyHost")).thenReturn("www-proxy");
        when(request.getParameter("proxyPort")).thenReturn("8080");

        servlet.doPost(request, response);
        writer.flush(); // it may not have been flushed yet...
        System.out.println(sr.toString()) ;
        assertTrue(sr.toString().contains("success\":[true]"));
    }
}
