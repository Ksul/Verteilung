package de.schulte.testverteilung;

import java.io.File;
import java.net.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.Response.ResponseType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class VerteilungServlet
 */
public class VerteilungServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	Collection<FileEntry> entries = new ArrayList<FileEntry>();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public VerteilungServlet() {
		super();

		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doGetOrPost(request, response);
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		} catch (JSONException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doGetOrPost(request, response);
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		} catch (JSONException e) {
			throw new ServletException(e);
		}
	}

	// This method handles both GET and POST requests.
	private void doGetOrPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, URISyntaxException,
			JSONException {
		// Get the value of a request parameter; the name is case-sensitive
		Object ret = null;
		JSONObject obj = new JSONObject();
		String value = req.getParameter("function");
		String documentId = req.getParameter("documentId");
		String destinationId = req.getParameter("destinationId");
		String documentText = req.getParameter("documentText");
		String filePath = req.getParameter("filePath");
		String fileName = req.getParameter("fileName");
        String cmisQuery = req.getParameter("cmisQuery");
		String destinationFolder = req.getParameter("destinationFolder");
		String description = req.getParameter("description");
		String mimeType = req.getParameter("mimeType");
		String server = req.getParameter("server");
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		String clear = req.getParameter("clear");
        String withFolder = req.getParameter("withFolder");
		String extract = req.getParameter("extract");
		String searchFolder = req.getParameter("searchFolder");
		String proxyHost = "".equals(req.getParameter("proxyHost")) ? null : req.getParameter("proxyHost");
		String proxyPort = "".equals(req.getParameter("proxyPort")) ? null : req.getParameter("proxyPort");
        resp.setHeader("Content-Type", "application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();
		
		try {
			if (value == null || "".equals(value)) {
				obj.append("success", false);
				obj.append("result", "Function Name is missing!\nPlease check for Tomcat maxPostSize and maxHeaderSizer Property for HTTPConnector");
			} else {
				if (value.equalsIgnoreCase("openPDF")) {
					openPDF(resp, fileName);
					return;
				}

                if (value.equalsIgnoreCase("isURLAvailable")) {
                    ret = isURLAvailable(server, proxyHost, proxyPort);
                }
			
				if (value.equalsIgnoreCase("getContent")) {
					ret = getContent(documentId, extract.equalsIgnoreCase("true"), server, username, password, proxyHost,
							proxyPort);
				}
				if (value.equalsIgnoreCase("updateDocument")) {
					ret = updateDocument(documentId, documentText, description, server, username, password, proxyHost, proxyPort);
				}
				if (value.equalsIgnoreCase("getTicket")) {
					ret = getTicket(server, username, password, proxyHost, proxyPort, null);
				}
				if (value.equalsIgnoreCase("moveDocument")) {
					ret = moveDocument(documentId, destinationId, server, username, password, proxyHost, proxyPort, null);
				}
				if (value.equalsIgnoreCase("updateDocumentByFile")) {
					ret = updateDocumentByFile(documentId, fileName, description, mimeType, server, username, password,
							proxyHost, proxyPort);
				}
				if (value.equalsIgnoreCase("uploadFile")) {
					ret = uploadFile(filePath, fileName, destinationFolder, description, mimeType, server, username, password,
							proxyHost, proxyPort);
				}
				if (value.equalsIgnoreCase("getNodeId")) {
					ret = getNodeId(cmisQuery, server, username, password, proxyHost,
							proxyPort);
				}
				if (value.equalsIgnoreCase("listFolder")) {
					ret = listFolder(filePath, withFolder, true, server, username, password, proxyHost, proxyPort);
				}
                if (value.equalsIgnoreCase("listFolderAsJSON")) {
                    ret = listFolderAsJSON(filePath, withFolder, server, username, password, proxyHost, proxyPort);
                    out.write(ret.toString());
                    out.close();
                    return;
                }
				if (value.equalsIgnoreCase("extract")) {
					ret = extract(documentText, fileName, clear);
				}

				if (value.equalsIgnoreCase("extractZIP")) {
					ret = extractZIP(documentText);
				}
				if (value.equalsIgnoreCase("doTest")) {
					ret = doTest(fileName, filePath);
				}
				obj.append("success", true);
				obj.append("result", ret);
			}
		} catch (VerteilungException e) {
			obj.append("success", false);
			obj.append("result", e.getMessage());

		} catch (JSONException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		out.write(obj.toString());
		out.close();
	}

    protected  boolean isURLAvailable(String urlString, String proxyHost, String proxyPort)  {

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
           return false;
        }

        HttpURLConnection httpUrlConn;
        try {
            if (proxyHost != null && proxyPort != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                httpUrlConn = (HttpURLConnection) url.openConnection(proxy);
            } else
                httpUrlConn = (HttpURLConnection) url.openConnection();

            httpUrlConn.setRequestMethod("HEAD");

            // Set timeouts in milliseconds
            httpUrlConn.setConnectTimeout(30000);
            httpUrlConn.setReadTimeout(30000);

            // Print HTTP status code/message for your information.
            System.out.println("Response Code: "
                    + httpUrlConn.getResponseCode());
            System.out.println("Response Message: "
                    + httpUrlConn.getResponseMessage());

            return (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }



    protected void openPDF(HttpServletResponse resp, String fileName) throws IOException {
		boolean found = false;
		FileEntry entry = null;
		Iterator<FileEntry> it = entries.iterator();
		while (it.hasNext()) {
			entry = it.next();
			if (entry.getName().equalsIgnoreCase(fileName)) {
				found = true;
				break;
			}
		}
		if (found) {
			final byte[] bytes = entry.getData();
			final String name = entry.getName();
			resp.reset();
			resp.resetBuffer();
			resp.setContentType("application/pdf");
			resp.setHeader("Content-Disposition", "inline; filename=" + name + "\"");
			resp.setHeader("Cache-Control", "max-age=0");
			resp.setContentLength(bytes.length);
			ServletOutputStream sout = resp.getOutputStream();
			sout.write(bytes, 0, bytes.length);
			sout.flush();
			sout.close();
		}
	}

	protected Object getContent(String documentId, boolean extract, String server, String username, String password,
			String proxyHost, String proxyPort) throws VerteilungException {
		Object ret;
		AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort, null);
		AlfrescoResponse response = connector.getContent(documentId);
		if (!ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
			throw new VerteilungException("Dokument konnte nicht gelesen werden." + response.getStatusText());
		} else {
			if (extract) {
				PDFConnector con = new PDFConnector();
				byte[] bytes = response.getContent();
				InputStream is = new ByteArrayInputStream(bytes);
				ret = con.pdftoText(is);
			} else
				try {
					ret = new String(response.getContent(), "utf-8");
				} catch (UnsupportedEncodingException e) {
					System.out.println(e.getLocalizedMessage());
					e.printStackTrace();
					throw new VerteilungException(e.getLocalizedMessage());
				}
		}
		return ret;
	}

	protected Object getTicket(String server, String username, String password, String proxyHost, String proxyPort,
			Credentials credentials) {
		String ret;
		AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort, credentials);
		ret = connector.getTicket();

		return ret;
	}

	protected Object moveDocument(String documentId, String destinationId, String server, String username,
			String password, String proxyHost, String proxyPort, Credentials credentials) throws VerteilungException,
			JSONException {
		AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort, credentials);
		JSONObject obj = new JSONObject(connector.moveDocument(documentId, destinationId));
		if (!obj.getBoolean("overallSuccess"))
			throw new VerteilungException(obj.toString());
		return obj.toString();
	}

	protected Object updateDocument(String documentId, String documentText, String description, String server,
			String username, String password, String proxyHost, String proxyPort) throws IOException, VerteilungException {
		String ret = null;
		AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort, null);
		AlfrescoResponse response = connector.checkout(documentId);
		if (response != null && ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
			Document<Element> doc = response.getDocument();
			Entry responseEntry = (Entry) doc.getRoot();
			String tmp = responseEntry.getId().getPath();
            final String id = tmp.substring(tmp.lastIndexOf(":") + 1);
			response = connector.updateContent(documentText, description, id);
			if (response != null && ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
				response = connector.checkin(id, false, "");
				if (response == null || !ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
					ret = "Dokument konnte nicht eingecheckt werden: " + response.getStatusText();
					ret = ret + "\n" + response.getStackTrace();
					throw new VerteilungException(ret);
				}
			} else {
				ret = "Dokument konnte nicht aktualisiert werden: " + response.getStatusText();
				ret = ret + "\n" + response.getStackTrace();
				throw new VerteilungException(ret);
			}
		} else {
			ret = "Dokument konnte nicht ausgecheckt werden: " + response.getStatusText();
			ret = ret + "\n" + response.getStackTrace();
			throw new VerteilungException(ret);
		}
		return ret;
	}

	protected Object updateDocumentByFile(String documentId, String uri, String description, String mimeType,
			String server, String username, String password, String proxyHost, String proxyPort) throws IOException,
			URISyntaxException, VerteilungException {
		String ret = null;
		AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort, null);
		AlfrescoResponse response = connector.checkout(documentId);
		if (response != null && ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
			Document<Element> doc = response.getDocument();
			Entry responseEntry = (Entry) doc.getRoot();
			String tmp = responseEntry.getId().getPath();
			final String id = tmp.substring(tmp.lastIndexOf(":") + 1);
			response = connector.updateCheckedOutFile(uri, description, mimeType, id);
			if (response != null && ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
				response = connector.checkin(id, false, "");
				if (response == null || !ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
					ret = "Dokument konnte nicht eingecheckt werden: " + response.getStatusText();
					ret = ret + "\n" + response.getStackTrace();
					throw new VerteilungException(ret);
				}
			} else {
				ret = "Dokument konnte nicht aktualisiert werden: " + response.getStatusText();
				ret = ret + "\n" + response.getStackTrace();
				throw new VerteilungException(ret);
			}
		} else {
			ret = "Dokument konnte nicht ausgecheckt werden: " + response.getStatusText();
			ret = ret + "\n" + response.getStackTrace();
			throw new VerteilungException(ret);
		}
		return ret;
	}

    protected Object getNodeId(String cmisQuery, String server, String username, String password,
                               String proxyHost, String proxyPort) throws VerteilungException {

        String ret = "";

        AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort, null);
        AlfrescoResponse response = connector.getNode(cmisQuery);

        if (!ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
            throw new VerteilungException("Dokument konnte nicht gefunden werden."
                    + response.getStatusText());
        } else {
            Document<Feed> entryDoc = response.getDocument();
            Feed root = entryDoc.getRoot();
            List<Entry> entries = root.getEntries();
            if (entries.size() > 0) {
                Iterator<Element> it = root.getEntries().get(0).getElements().iterator();
                while (it.hasNext()) {

                    Element element = (Element) it.next();
                    if (element.getQName().equals(CMISConstants.ATOMOBJECT)) {

                        Iterator it1 = element.getElements().get(0).getElements().iterator();
                        while (it1.hasNext()) {
                            Element el = (Element) it1.next();
                            if (el.getAttributeValue("propertyDefinitionId") != null
                                    && el.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cmis:objectId")) {
                                ret = el.getFirstChild(CMISConstants.VALUE).getText();
                                ret = ret.substring(ret.lastIndexOf('/') + 1);
                                break;
                            }
                        }

                    }
                }
            } else {
                throw new VerteilungException("Kein Knoten zu Kriterium " + cmisQuery + " gefunden!");
            }
        }
        return ret;
    }

    protected Object uploadFile(String filePath, String fileName, String destinationFolder, String description,
			String mimeType, String server, String username, String password, String proxyHost, String proxyPort)
			throws IOException, VerteilungException {
		String ret = null;
		AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort, null);
		AlfrescoResponse response = connector
				.uploadFileByPath(filePath, fileName, description, mimeType, destinationFolder);
		if (!ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
			throw new VerteilungException("Dokument konnte nicht hochgeladen werden." + response.getStatusText() + "\n" + response.getStackTrace());
		}
		return ret;
	}

    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     * @param filePath     der Pfad, der geliefert werden soll
     * @param listFolder   was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @param server       der Alfresco-Servername
     * @param username     der Alfresco-Username
     * @param password     das Alfresco-Passwort
     * @param proxyHost    der Proxy-Host, falls verwendet
     * @param proxyPort    der Proxyport, falls verwendet
     * @return             der Inhalt des Verzeichnisses als JSON Objekte
     * @throws IOException
     * @throws VerteilungException
     * @throws JSONException
     */
	protected Object listFolder(String filePath, String listFolder, boolean byPath, String server, String username, String password,
			String proxyHost, String proxyPort) throws IOException, VerteilungException {
		ArrayList<Properties> liste = new ArrayList<Properties>();
        boolean folder = false;
		AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort, null);
		AlfrescoResponse response = connector.listFolder(filePath, byPath);
		if (!ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
			throw new VerteilungException("Verzeichnis konnte nicht gelesen werden: " + response.getStatusText()+ "\n" + response.getStackTrace());
		} else {
			Document<Feed> entryDoc = response.getDocument();

			Feed root = entryDoc.getRoot();
			Iterator<Entry> it = root.getEntries().iterator();
			while (it.hasNext()) {
				Entry ent = it.next();
				Iterator<Element> it1 = ent.getElements().iterator();
				while (it1.hasNext()) {
					Element element = it1.next();
					if (element.getQName().equals(CMISConstants.ATOMOBJECT)) {
						Iterator<Element> it2 = element.getElements().get(0).getElements().iterator();
						Properties p = new Properties();
						while (it2.hasNext()) {
							Element el = it2.next();
							if (el.getAttributeValue("propertyDefinitionId") != null
									&& el.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cmis:objectId")) {
                                String id = el.getFirstChild(CMISConstants.VALUE).getText();
								p.put("id", id.substring(id.lastIndexOf('/') + 1));
                            }
							if (el.getAttributeValue("propertyDefinitionId") != null
									&& el.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cmis:name"))
								p.put("name", el.getFirstChild(CMISConstants.VALUE).getText());
                            if (el.getElements().size() > 0) {
                                Iterator<Element> it3 = el.getElements().iterator();
                                while (it3.hasNext()){
                                    Element el1 = it3.next();
                                    if (el1.getElements().size() > 0) {
                                        Iterator<Element> it4 = el1.getElements().iterator();
                                        while (it4.hasNext()){
                                            Element el2 = it4.next();
                                            if (el2.getAttributeValue("propertyDefinitionId") != null && el2.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cm:title"))
                                                p.put("title", el2.getFirstChild(CMISConstants.VALUE).getText());
                                        }
                                    }
                                }
                            }
							if (el.getAttributeValue("propertyDefinitionId") != null
									&& el.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cmis:objectTypeId")) {
								folder = el.getFirstChild(CMISConstants.VALUE).getText().equals("cmis:folder") || el.getFirstChild(CMISConstants.VALUE).getText().equals("F:my:archivFolder");
                                p.put("folder", folder);
                            }
							if (el.getAttributeValue("propertyDefinitionId") != null
									&& el.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cmis:contentStreamMimeType"))
								p.put("typ", el.getFirstChild(CMISConstants.VALUE).getText());
						}
						if (p.containsKey("name") && p.containsKey("id") && (Integer.parseInt(listFolder) > -1|| !folder)) {
							liste.add(p);
							break;
						}
					}
				}
			}
		}
		return liste;
	}

    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     * @param filePath     der Pfad, der geliefert werden soll (als NodeId)
     * @param listFolder   was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @param server       der Alfresco-Servername
     * @param username     der Alfresco-Username
     * @param password     das Alfresco-Passwort
     * @param proxyHost    der Proxy-Host, falls verwendet
     * @param proxyPort    der Proxyport, falls verwendet
     * @return             der Inhalt des Verzeichnisses als JSON Objekte
     * @throws IOException
     * @throws VerteilungException
     * @throws JSONException
     */
    protected JSONArray listFolderAsJSON(String filePath, String listFolder, String server, String username, String password,
                                         String proxyHost, String proxyPort) throws IOException, VerteilungException, JSONException {
        JSONObject o;
        JSONObject o1;
        JSONArray list = new JSONArray();
        if (filePath == null || filePath.length() == 0) {
            filePath = (String) getNodeId("SELECT * from cmis:folder where CONTAINS('PATH:\"//app:company_home/cm:Archiv\"')", server, username, password, proxyHost, proxyPort);
            o = new JSONObject();
            o1 = new JSONObject();
            o.put("id", filePath);
            o.put("rel", "root");
            o.put("state", "closed");
            o1.put("attr", o);
            o1.put("data", "Archiv");
            o1.put("state", "closed");
            list.put(o1);
        } else {
            if (filePath.equals("-1"))
                filePath = (String) getNodeId("SELECT * from cmis:folder where CONTAINS('PATH:\"//app:company_home/cm:Archiv\"')", server, username, password, proxyHost, proxyPort);

            ArrayList<Properties> liste = (ArrayList<Properties>) listFolder(filePath, listFolder, false, server, username, password, proxyHost, proxyPort);


            for (int i = 0; i < liste.size(); i++) {
                Properties p = (Properties) liste.get(i);
                o = new JSONObject();
                o1 = new JSONObject();
                o.put("id", p.getProperty("id"));
                if (((Boolean) p.get("folder")).booleanValue()) {
                    o.put("rel", "folder");
                    o1.put("state", "closed");
                } else {
                    o.put("rel", "default");
                    o1.put("state", "");
                }
                if (p.containsKey("title") &&  p.getProperty("title").length() > 0)
                    o1.put("data", p.getProperty("title"));
                else
                    o1.put("data", p.getProperty("name"));
                o1.put("attr", o);
                list.put(o1);
            }
        }
        return list;
    }

    protected Object extract(String documentText, String fileName, String clear) {
		Object ret;
		if (clear.equalsIgnoreCase("true"))
			entries.clear();
		System.out.println(fileName);
		final byte[] bytes = Base64.decodeBase64(documentText);
		entries.add(new FileEntry(fileName, bytes));
		InputStream bais = new ByteArrayInputStream(bytes);
		PDFConnector con = new PDFConnector();
		ret = con.pdftoText(bais);
		return ret;
	}

	protected Object extractZIP(String documentText) throws VerteilungException{
		Object ret;
		ret = new JSONObject();
		ZipInputStream zipin = null;
		final byte[] bytes = Base64.decodeBase64(documentText);
		InputStream bais = new ByteArrayInputStream(bytes);
		zipin = new ZipInputStream(bais);
		ZipEntry entry = null;
		int size;
		entries.clear();
		try {
			while ((entry = zipin.getNextEntry()) != null) {
				byte[] buffer = new byte[2048];
				ByteArrayOutputStream bys = new ByteArrayOutputStream();
				BufferedOutputStream bos = new BufferedOutputStream(bys, buffer.length);
				while ((size = zipin.read(buffer, 0, buffer.length)) != -1) {
					bos.write(buffer, 0, size);
				}
				bos.flush();
				bos.close();
				entries.add(new FileEntry(entry.getName(), bys.toByteArray()));
			}
			Iterator<FileEntry> it = entries.iterator();
			while (it.hasNext()) {
				FileEntry ent = it.next();
				String entryFileName = ent.getName();
				InputStream b = new ByteArrayInputStream(ent.getData());
				PDFConnector con = new PDFConnector();
				String result = con.pdftoText(b);
				JSONObject resultObj = new JSONObject();
				resultObj.append("entryFileName", entryFileName);
				resultObj.append("result", result);
				((JSONObject) ret).append("entry", resultObj);

			}
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
			throw new VerteilungException("Fehler beim Entpacken der ZIP-Datei! " + e.getLocalizedMessage());
		} finally {
			try {
				zipin.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
		return ret;
	}

	protected Object doTest(String fileName, String filePath) {
		JSONObject o = new JSONObject();
		try {
			String f1 = openFile(fileName);
			String f2 = openFile(filePath);
			o.append("text", f1);
			o.append("xml", f2);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return o;
	}

	protected String openFile(String filePath) throws VerteilungException{
		StringBuffer fileData = new StringBuffer(1000);
		String ret = null;
		try {
			InputStream inp = getServletContext().getResourceAsStream(filePath);
			InputStreamReader isr = new InputStreamReader(inp, "UTF-8");
			BufferedReader reader = new BufferedReader(isr);
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			ret = fileData.toString();
		} catch (IOException e) {
			System.out.println("Fehler beim Öffnen der Datei: " + e.getLocalizedMessage());
			e.printStackTrace();
			throw new VerteilungException("Fehler beim Öffnen der Datei: " + e.getLocalizedMessage());
		}
		return ret;
	}


}
