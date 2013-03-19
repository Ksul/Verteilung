package de.schulte.testverteilung;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.stax.FOMEntry;
import org.apache.abdera.parser.stax.FOMExtensibleElement;
import org.apache.abdera.protocol.Response.ResponseType;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class WebScriptTest implements HttpServletRequest, HttpServletResponse {

  public static String NS_CMIS_RESTATOM = "http://docs.oasis-open.org/ns/cmis/restatom/200908/";

  public static String NS_CMIS_CORE = "http://docs.oasis-open.org/ns/cmis/core/200908/";

  public static String CMISRA = "cmisra";

  public static String CMIS = "cmis";
  
	public Properties prop = new Properties();
	
	public ByteArrayOutputStream bos = new ByteArrayOutputStream();
	

  public static void main(String[] args) {
  	
  	WebScriptTest wt = new WebScriptTest();

//		wt.prop.put("proxyPort", "8080");
//		wt.prop.put("proxyHost", "www-proxy");
		//wt.prop.put("credentials", null);
		wt.prop.put("server", "http://lashilfes.lvm.de");
		wt.prop.put("username", "testa");
		wt.prop.put("password", "abcde");
		wt.prop.put("docName", "startseite.jpg");
		// wt.prop.put("documentId",                          "72429975-5afc-464c-bf78-642129e079ef");
		wt.prop.put("documentId", "146260d2-5511-4925-a1e6-9fb9d95499d0");
		// prop.put("docId",                          "8ab628ae-c8cb-4e37-bf31-846d60aec3a4");   
		wt.prop.put("docText", "Dies ist ein neuer Test");
		wt.prop.put("docDescription", "XML Beschreibung der Dokumente");
		wt.prop.put("mimeType", "application/x-javascript");
		// prop.put("mimeType",                          "plain/text");                          
		wt.prop.put("destinationFolder", "Datenverzeichnis/Skripte");
	
 
    // updateDocument(docId, docText, docDescription, mimeType, server,
    // username, password, proxy, port, credentials);
    // uploadFile(proxyPort, proxyHost, credentials, server, username, password,
    // docDescription, mimeType,
    // destinationFolder);
   // fetchContent(proxyPort, proxyHost, credentials, server, username, password,
    //    docId);
    VerteilungServlet serv = new VerteilungServlet();
  
   try {
      //serv.updateDocument(documentId, "abcde", "",  server, username, password, proxyHost, proxyPort);
  	 //serv.updateDocumentByFile(documentId, "file:///u/m500288/workspace/RSA8/TestVerteilung/WebContent/recognition.js", docDescription, mimeType, server, username, password, proxyHost, proxyPort);
//  	 String a = (String) serv.getContent(documentId, true,server, username, password, proxyHost, proxyPort);
//  	 System.out.println(a); 
//  	 ArrayList ret = (ArrayList) serv.listFolder("Firmen-Home/Archiv/Fehler", true, server, username, password, proxyHost, proxyPort);
//  	  for (int i = 0; i < ret.size(); i++) {
//  	  	System.out.println(ret.get(i));
//			}
  	  //String a = (String) serv.getTicket(server, username, password, proxyHost, proxyPort, null);
  		wt.prop.put("function", "getNodeId");
  		wt.prop.put("fileName", "startseite.jpg");
  		wt.prop.put("searchFolder", "false");
  		serv.doPost(wt, wt);
  		JSONObject  a = new JSONObject(wt.bos.toString());
  	  System.out.println(a);
  	  wt.prop.put("function", "getContent");
  	  wt.prop.put("documentId",  a.getJSONArray("result").get(0));
  	  wt.prop.put("extract", "false");
  		serv.doPost(wt, wt);
  		JSONObject  b = new JSONObject(wt.bos.toString());
  	  System.out.println(b);
//  		wt.prop.put("fileName", "Ablösevollmacht.pdf");
//  		wt.prop.put("searchFolder", "false");
//  		serv.doPost(wt, wt);
//  		JSONObject  b = new JSONObject(wt.bos.toString());
//  	  System.out.println(b);
//  	  wt.prop.put("function", "moveDocument");
//  	  wt.prop.put("documentId",  b.getJSONArray("result").get(0));
//  	  wt.prop.put("destinationId", a.getJSONArray("result").get(0));
//  	  serv.doPost(wt, wt);
//  		JSONObject  c = new JSONObject(wt.bos.toString());
//  	  System.out.println(c);
//    String b = (String) serv.getNodeId("Ablösevollmacht.pdf", false, server, username, password, proxyHost, proxyPort);
//    String c = (String) serv.moveDocument(b, a, server, username, password, proxyHost, proxyPort, credentials);
//  
//    System.out.println(b);
// 	  System.out.println(c);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		} 
  }

	@Override
	public Object getAttribute(String arg0) {
		return null;
	}

	@Override
	public Enumeration getAttributeNames() {
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public int getContentLength() {
		return 0;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public Enumeration getLocales() {
		return null;
	}

	@Override
	public String getParameter(String arg0) {
		return prop.getProperty(arg0);
	}

	@Override
	public Map getParameterMap() {
		return null;
	}

	@Override
	public Enumeration getParameterNames() {
		return null;
	}

	@Override
	public String[] getParameterValues(String arg0) {
		return null;
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return null;
	}

	@Override
	public String getRemoteHost() {
		return null;
	}

	@Override
	public int getRemotePort() {
		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	@Override
	public String getScheme() {
		return null;
	}

	@Override
	public String getServerName() {
		return null;
	}

	@Override
	public int getServerPort() {
		return 0;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
	}

	@Override
	public void setCharacterEncoding(String arg0)  {
	}

	@Override
	public String getAuthType() {
		return null;
	}

	@Override
	public String getContextPath() {
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		return null;
	}

	@Override
	public long getDateHeader(String arg0) {
		return 0;
	}

	@Override
	public String getHeader(String arg0) {
		return null;
	}

	@Override
	public Enumeration getHeaderNames() {
		return null;
	}

	@Override
	public Enumeration getHeaders(String arg0) {
		return null;
	}

	@Override
	public int getIntHeader(String arg0) {
		return 0;
	}

	@Override
	public String getMethod() {
		return null;
	}

	@Override
	public String getPathInfo() {
		return null;
	}

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getQueryString() {
		return null;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public String getRequestURI() {
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return null;
	}

	@Override
	public String getServletPath() {
		return null;
	}

	@Override
	public HttpSession getSession() {
		return null;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		return false;
	}

	@Override
	public void flushBuffer() throws IOException {
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		bos.reset();
		return new PrintWriter(bos);
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {
	}

	@Override
	public void resetBuffer() {
	}

	@Override
	public void setBufferSize(int arg0) {
	}

	@Override
	public void setContentLength(int arg0) {
	}

	@Override
	public void setContentType(String arg0) {
	}

	@Override
	public void setLocale(Locale arg0) {
	}

	@Override
	public void addCookie(Cookie arg0) {
	}

	@Override
	public void addDateHeader(String arg0, long arg1) {
	}

	@Override
	public void addHeader(String arg0, String arg1) {
	}

	@Override
	public void addIntHeader(String arg0, int arg1) {
	}

	@Override
	public boolean containsHeader(String arg0) {
		return false;
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		return null;
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		return null;
	}

	@Override
	public String encodeURL(String arg0) {
		return null;
	}

	@Override
	public String encodeUrl(String arg0) {
		return null;
	}

	@Override
	public void sendError(int arg0) throws IOException {
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
	}

	@Override
	public void setHeader(String arg0, String arg1) {
	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
	}

	@Override
	public void setStatus(int arg0) {
	}

	@Override
	public void setStatus(int arg0, String arg1) {
	}

 

 
}
