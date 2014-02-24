package de.schulte.testverteilung;

import java.net.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.util.*;
import java.util.logging.Logger;
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
import org.apache.abdera.protocol.Response.ResponseType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class VerteilungServlet
 */
public class VerteilungServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	Collection<FileEntry> entries = new ArrayList<FileEntry>();

    private VerteilungServices services;

    private String bindingUrl;

    private String user;

    private String password;

    private Logger logger = Logger.getLogger(VerteilungApplet.class.getName());

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public VerteilungServlet() {
		super();

	}


    /**
     * setzt die Parameter
     * @param url          die Binding Url
     * @param userName     der Username
     * @param pass         das Password
     * @return             ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    ret               die Parameter als String
     */
    public JSONObject setParameter(String url, String userName, String pass) {
        bindingUrl = url;
        user = userName;
        password = pass;
        JSONObject obj = new JSONObject();
        try {
            obj.put("success", true);
            obj.put("result", bindingUrl + " " + user + " " + password);
        } catch (JSONException jse) {
            logger.severe(jse.getMessage());
            jse.printStackTrace();
        }
        return obj;
    }

    /**
     * liefert die Alfresco Services
     * @param url               Binding URL des Servers
     * @param user              User Name
     * @param password          Passwort
     * @return
     */
    public VerteilungServices getServices(String url, String user, String password) throws VerteilungException {
        if (url == null || user == null || password == null)
            throw new VerteilungException("Parameter fehlen!");
        if (services == null)
            services = new VerteilungServices(url, user, password);
        return services;
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

    /**
     * diese Methode handelt get und post Requests
     * @param req
     * @param resp
     * @throws IOException
     * @throws URISyntaxException
     * @throws JSONException
     */
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
        String extraProperties = req.getParameter("extraProperties");
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
                if (value.equalsIgnoreCase("setParameter")) {
                    obj = setParameter(server, username, password);
                }
                if (value.equalsIgnoreCase("isURLAvailable")) {
                    obj = isURLAvailable(server);
                }
                if (value.equalsIgnoreCase("getNodeId")) {
                    obj = getNodeId(filePath);
                }
                if (value.equalsIgnoreCase("findDocument")) {
                    obj = findDocument(cmisQuery);
                }
                if (value.equalsIgnoreCase("uploadDocument")) {
                    obj = uploadDocument(destinationFolder, fileName);
                }
                if (value.equalsIgnoreCase("deleteDocument")) {
                    obj = deleteDocument(destinationFolder, fileName);
                }
                if (value.equalsIgnoreCase("createDocument")) {
                    obj = createDocument(destinationFolder, fileName, documentText, mimeType, extraProperties);
                }
                if (value.equalsIgnoreCase("createFolder")) {
                    obj = createFolder(destinationFolder, fileName);
                }
                if (value.equalsIgnoreCase("deleteFolder")) {
                    obj = deleteFolder(destinationFolder);
                }
				if (value.equalsIgnoreCase("getDocumentContent")) {
					obj = getDocumentContent(documentId, extract.equalsIgnoreCase("true"));
				}
				if (value.equalsIgnoreCase("updateDocument")) {
					obj = updateDocument(documentId, documentText, mimeType);
				}
				if (value.equalsIgnoreCase("getTicket")) {
					ret = getTicket(server, username, password, proxyHost, proxyPort, null);
				}
				if (value.equalsIgnoreCase("moveDocument")) {
					ret = moveDocument(documentId, destinationId, server, username, password, proxyHost, proxyPort, null);
				}
                if (value.equalsIgnoreCase("listFolderAsJSON")) {
                    obj = listFolderAsJSON(filePath, withFolder, server, username, password);
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
			}
		} catch (VerteilungException e) {
			obj.append("success", false);
			obj.append("result", e.getMessage());

		} catch (JSONException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		out.write(obj.toString());
	}

    /**
     * prüft, ob eine Url verfügbar ist
     * @param urlString    URL des Servers
     * @return             ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            true, wenn die URL verfügbar ist
     */
    protected JSONObject isURLAvailable(String urlString) {

        JSONObject obj = new JSONObject();
        URL url = null;
        try {
            url = new URL(urlString);
            logger.info("Umwandlung in URL " + url);
        } catch (MalformedURLException e) {
            String error = "Fehler beim Check der URL: " + e.getMessage();
            logger.severe(error);
            e.printStackTrace();
            try {
                obj.put("success", false);
                obj.put("result", error);
            } catch (JSONException jse) {
                logger.severe(jse.getMessage());
                jse.printStackTrace();
            }
        }
        try {
            HttpURLConnection httpUrlConn;
            httpUrlConn = (HttpURLConnection) url.openConnection();
            logger.info("Open Connection " + httpUrlConn);
            httpUrlConn.setRequestMethod("HEAD");
            logger.info("Set Request ");
            // Set timeouts in milliseconds
            httpUrlConn.setConnectTimeout(30000);
            httpUrlConn.setReadTimeout(30000);

            int erg = httpUrlConn.getResponseCode();
            logger.info("ResponseCode " + erg);
            logger.info(httpUrlConn.getResponseMessage());
            obj.put("success", true);
            obj.put("result", erg == HttpURLConnection.HTTP_OK);

        } catch (Throwable t) {
            String error = "Fehler beim Check der URL: " + t.getMessage();
            logger.severe(error);
            t.printStackTrace();
            try {
                obj.put("success", false);
                obj.put("result", error);
            } catch (JSONException jse) {
                logger.severe(jse.getMessage());
                jse.printStackTrace();
            }
        }
        return obj;
    }

    /**
     * liefert eine NodeID als String zurück
     * @param path         der Pfad zum Knoten, der der Knoten gesucht werden soll
     * @return             ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            die NodeId als String
     */
    protected JSONObject getNodeId(String path)  {

        VerteilungServices services = new VerteilungServices(bindingUrl, user, password);
        return services.getNodeId(path);
    }

    /**
     * liefert den Inhalt eines Dokumentes. Wenn es sich um eine PDF Dokument handelt, dann wird
     * der Text extrahiert.
     * @param documentId   die Id des Documentes
     * @param extract      legt fest,ob einPDF Document umgewandelt werden soll
     * @return             ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            das Document als JSONObject
     */
    protected JSONObject getDocumentContent(String documentId, boolean extract) {
        VerteilungServices services = new VerteilungServices(bindingUrl, user, password);
        return services.getDocumentContent(documentId, extract);
    }


    /**
     * lädt ein Document in den Server
     * @param filePath       der Folder als String, in das Document geladen werden soll
     * @param fileName       der Dateiname ( mit Pfad) als String, die hochgeladen werden soll
     * @return               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                               false    ein Fehler ist aufgetreten
     *                                                      result            Dokument als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject uploadDocument(String filePath, String fileName) throws  VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.uploadDocument(filePath, fileName);
	}

    /**
     * löscht ein Document
     * @param filePath       der Folder als String, in das Documentliegt
     * @param fileName       der Name des Documentes
     * @return               ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                               false    ein Fehler ist aufgetreten
     *                                                      result            Dokument als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject deleteDocument(String filePath, String fileName) throws  VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.deleteDocument(filePath, fileName);
    }

    /**
     * erzeugt ein Document
     * @param  filePath             der Name des Folders in dem das Dokument erstellt werden soll als String
     * @param  fileName             der Name des Dokumentes als String
     * @param  documentContent      der Inhalt als String
     * @param  documentType         der Typ des Dokumentes
     * @param  extraCMSProperties   zusätzliche Properties
     * @return                      ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result            Dokument als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject createDocument(String filePath,
                                        String fileName,
                                        String documentContent,
                                        String documentType,
                                        String extraCMSProperties) throws  VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.createDocument(filePath, fileName, documentContent, documentType, extraCMSProperties);
    }

    /**
     * aktualisiert den Inhalt eines Dokumentes
     * @param  documentId                Die Id des zu aktualisierenden Dokumentes
     * @param  documentContent           der neue Inhalt
     * @param  documentType              der Typ des Dokumentes
     * @return obj                       ein JSONObject mit den Feldern success: true    die Operation war erfolgreich
     *                                                                           false   ein Fehler ist aufgetreten
     *                                                                  result           bei Erfolg nichts, ansonsten der Fehler
     * @throws VerteilungException
     */
    public JSONObject updateDocument(String documentId,
                                     String documentContent,
                                     String documentType) throws VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.updateDocument(documentId, documentContent, documentType);
    }

    /**
     * erzeugt einen Pfad
     * @param  targetPath             der Name des Folders in dem der Folder erstellt werden soll als String
     * @param  folderName           der Name des neuen Pfades als String
     * @return                      ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result            Folder als JSONObject
     * @throws VerteilungException
     */
    protected JSONObject createFolder(String targetPath,
                                        String folderName) throws  VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.createFolder(targetPath, folderName);
    }

    /**
     * löscht einen Pfad
     * @param  folderPath           der Name des zu löschenden Pfades als String
     * @return                      ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                                      false    ein Fehler ist aufgetreten
     *                                                             result
     * @throws VerteilungException
     */
    protected JSONObject deleteFolder(String folderPath) throws  VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.deleteFolder(folderPath);
    }

    /**
     * findet ein Document
     * @param cmisQuery    die CMIS Query, mit der der Knoten gesucht werden soll
     * @return             ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            Dokument als JSONObject
     */
    protected JSONObject findDocument(String cmisQuery) throws VerteilungException {
        VerteilungServices services = getServices(bindingUrl, user, password);
        return services.findDocument(cmisQuery);
    }


    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     * @param filePath     der Pfad, der geliefert werden soll (als NodeId)
     * @param listFolder   was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @param server       der Alfresco-Servername
     * @param username     der Alfresco-Username
     * @param password     das Alfresco-Passwort
     * @return             ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    result            der Inhalt des Verzeichnisses als JSON Objekte
     */
    protected JSONObject listFolderAsJSON(String filePath, String listFolder, String server, String username, String password)  {

        VerteilungServices services = new VerteilungServices(server, username, password);
        return services.listFolderAsJSON(filePath, Integer.parseInt(listFolder));
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





}
