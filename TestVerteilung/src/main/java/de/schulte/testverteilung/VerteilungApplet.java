package de.schulte.testverteilung;

/*
 * VerteilungApplet.java
 *
 * Created on January 24, 2009, 11:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Klaus Schulte
 */

import java.applet.Applet;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.Response.ResponseType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VerteilungApplet extends Applet {

	private static final long serialVersionUID = 1L;

	JSObject jsobject;

	ByteArrayOutputStream bys = new ByteArrayOutputStream();

	BufferedOutputStream bos = new BufferedOutputStream(bys);

	Collection<FileEntry> entries = new ArrayList<FileEntry>();

	public void init() {
		try {
			System.out.println("Hier ist das Verteilungsapplet");
			jsobject = JSObject.getWindow(this);
			jsobject = (JSObject) jsobject.getMember("parent");
			jsobject = (JSObject) jsobject.getMember("control");
		} catch (JSException jse) {
			System.out.println(jse.getMessage());
			jse.printStackTrace();
		}
	}

	public String getTicket(final String server, final String username, final String password, final String proxyHost,
			final String proxyPort, final Credentials credentials) {
		String ret;
		AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort, null);
		ret = connector.getTicket();

		ret = AccessController.doPrivileged(new PrivilegedAction<String>() {

			public String run() {
				AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort,
						credentials);
				return connector.getTicket();
			}
		});

		return ret;
	}

	public String moveDocument(final String documentId, final String destinationId, final String server,
			final String username, final String password, final String proxyHost, final String proxyPort,
			final Credentials credentials) throws JSONException {
		JSONObject ret = new JSONObject();
		JSONObject obj;
		try {
			obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

				public JSONObject run() throws VerteilungException, JSONException {
					JSONObject obj = new JSONObject();
					AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort,
							credentials);
					obj = new JSONObject(connector.moveDocument(documentId, destinationId));
					if (!obj.getBoolean("overallSuccess"))
						throw new VerteilungException(obj.toString());
					return obj;
				}
			});
		} catch (PrivilegedActionException e) {
			ret.put("success", false);
			ret.put("result", e.getMessage());
			return ret.toString();
		}
		ret.put("success", true);
		ret.put("result", obj.toString());
		return ret.toString();
	}

	public String listFolder(final String filePath, final String listFolder, final String server, final String username,
			final String password, final String proxyHost, final String proxyPort) throws IOException {
		ArrayList<Properties> liste = new ArrayList<Properties>();
		boolean folder = false;
		boolean showFolder = listFolder.equalsIgnoreCase("true");
		AlfrescoResponse response = AccessController.doPrivileged(new PrivilegedAction<AlfrescoResponse>() {

			public AlfrescoResponse run() {
				AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort, null);
				try {
					return connector.listFolder(filePath, true);
				} catch (IOException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					AlfrescoResponse response = new AlfrescoResponse();
					response.setStatusCode(ResponseType.SERVER_ERROR.toString());
					response.setStatusText(e.getMessage());
					return response;
				}
			}
		});
		if (!ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
			String name = "Verzeichnis konnte nicht gelesen werden: " + response.getStatusText();
			name = name + "\n" + response.getStackTrace();
			Properties p = new Properties();
			p.put("fehler", name);
			liste.add(p);
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
									&& el.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cmis:objectId"))
								p.put("id", el.getFirstChild(CMISConstants.VALUE).getText());
							if (el.getAttributeValue("propertyDefinitionId") != null
									&& el.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cmis:name"))
								p.put("name", el.getFirstChild(CMISConstants.VALUE).getText());
							if (el.getAttributeValue("propertyDefinitionId") != null
									&& el.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cmis:objectTypeId"))
								folder = el.getFirstChild(CMISConstants.VALUE).getText().equals("cmis:folder");
							if (el.getAttributeValue("propertyDefinitionId") != null
									&& el.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cmis:contentStreamMimeType"))
								p.put("typ", el.getFirstChild(CMISConstants.VALUE).getText());
						}
						if (p.containsKey("name") && p.containsKey("id") && p.containsKey("typ") && (showFolder || !folder)) {
							liste.add(p);
							break;
						}
					}
				}
			}
		}
		final ArrayList<Properties> ergebnis = liste;
		String ret = AccessController.doPrivileged(new PrivilegedAction<String>() {

			public String run() {
				JSONObject o = new JSONObject();
				try {
					o.put("result", ergebnis);

				} catch (JSONException e) {
					// Bitte kommentieren !!!
				}
				return o.toString();
			}
		});
		return ret;
	}

	public static String getNodeId(final String cmisQuery, final String server, final String username,
			final String password, final String proxyHost, final String proxyPort, final Credentials credentials) {

		JSONObject ret = new JSONObject();
		AlfrescoResponse response;
		try {
			try {
				response = AccessController.doPrivileged(new PrivilegedExceptionAction<AlfrescoResponse>() {

					public AlfrescoResponse run() throws VerteilungException {
						AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort,
								credentials);
						return connector.getNode(cmisQuery);
					}
				});
			} catch (PrivilegedActionException e) {
				ret.put("success", false);
				ret.put("result", e.getMessage());
				return ret.toString();
			}

			if (!ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
				ret.put("success", false);
				ret.put("result",
						"Dokument konnte nicht gefunden werden." + response.getStatusText() + "\n" + response.getStackTrace());
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
                                    String erg = el.getFirstChild(CMISConstants.VALUE).getText();
                                    erg = erg.substring(erg.lastIndexOf('/') + 1);
                                    ret.put("success", true);
                                    ret.put("result", erg);
                                    break;
                                }
                            }

                        }
                    }
                } else {
                    ret.put("success", false);
                    ret.put("result", "Kein Knoten zu Kriterium " + cmisQuery + " gefunden!");
                }
			}
		} catch (JSONException e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		}

		return ret.toString();
	}

	public static void uploadFile(final String filePath, final String fileName, final String description,
			final String mimeType, final String destinationFolder, final String server, final String username,
			final String password, final String proxyHost, final String proxyPort, final Credentials credentials) {

		AlfrescoResponse response = AccessController.doPrivileged(new PrivilegedAction<AlfrescoResponse>() {

			public AlfrescoResponse run() {
				try {
					AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort,
							credentials);
					return connector.uploadFileByPath(filePath, fileName, description, mimeType, destinationFolder);
				} catch (IOException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					AlfrescoResponse response = new AlfrescoResponse();
					response.setStatusCode(ResponseType.SERVER_ERROR.toString());
					response.setStatusText(e.getMessage());
					return response;
				}
			}
		});
		if (!ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
			System.err.println("Dokument konnte nicht hochgeladen werden." + response.getStatusText());
			System.err.println(response.getStackTrace());
		}

	}

	public static String getContent(final String docId, final boolean extract, final String server,
			final String username, final String password, final String proxyHost, final String proxyPort,
			final Credentials credentials) throws JSONException {
		String ret = "";
		JSONObject obj = new JSONObject();
		try {
			ret = AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {

				public String run() throws VerteilungException {
					String ret = null;
					AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort,
							credentials);
					AlfrescoResponse response = connector.getContent(docId);
					if (!ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
						System.err.println("Dokument konnte nicht gelesen werden." + response.getStatusText());
						System.err.println(response.getStackTrace());
					} else {
						if (extract) {
							PDFConnector con = new PDFConnector();
							byte[] bytes = response.getContent();
							InputStream is = new ByteArrayInputStream(bytes);
							ret = con.pdftoText(is);
						} else
							try {
								ret = new String(response.getContent(), "UTF-8");
							} catch (UnsupportedEncodingException e) {
								System.out.println(e.getMessage());
								e.printStackTrace();
								ret = e.getMessage();
							}

					}
					return ret;
				}
			});
			obj.put("success", true);
			obj.put("result", ret.toString());
		} catch (PrivilegedActionException e) {
			obj.put("success", false);
			obj.put("result", e.getMessage());
		}

		return obj.toString();
	}

	public static int updateDocument(final String documentId, final String documentText, final String description,
			final String server, final String username, final String password, final String proxyHost,
			final String proxyPort, final Credentials credentials) {
		int ret = 0;

		final AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort,
				credentials);
		AlfrescoResponse response = AccessController.doPrivileged(new PrivilegedAction<AlfrescoResponse>() {

			public AlfrescoResponse run() {
				try {
					return connector.checkout(documentId);
				} catch (IOException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					AlfrescoResponse response = new AlfrescoResponse();
					response.setStatusCode(ResponseType.SERVER_ERROR.toString());
					response.setStatusText(e.getMessage());
					return response;
				}
			}
		});
		if (response != null && ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
			Document<Element> doc = response.getDocument();
			Entry responseEntry = (Entry) doc.getRoot();
			String tmp = responseEntry.getId().getPath();
			final String id = tmp.substring(tmp.lastIndexOf(":") + 1);
			response = AccessController.doPrivileged(new PrivilegedAction<AlfrescoResponse>() {

				public AlfrescoResponse run() {
					try {

						return connector.updateContent(documentText, description, id);
					} catch (IOException e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
						AlfrescoResponse response = new AlfrescoResponse();
						response.setStatusCode(ResponseType.SERVER_ERROR.toString());
						response.setStatusText(e.getMessage());
						return response;
					}
				}
			});
			if (response != null && ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
				response = AccessController.doPrivileged(new PrivilegedAction<AlfrescoResponse>() {

					public AlfrescoResponse run() {
						try {
							return connector.checkin(id, false, "");
						} catch (IOException e) {
							System.out.println(e.getMessage());
							e.printStackTrace();
							AlfrescoResponse response = new AlfrescoResponse();
							response.setStatusCode(ResponseType.SERVER_ERROR.toString());
							response.setStatusText(e.getMessage());
							return response;
						}
					}
				});
				if (response == null || !ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
					System.err.println("Dokument konnte nicht eingecheckt werden: " + response.getStatusText());
					System.err.println(response.getStackTrace());
				}
			} else {
				System.err.println("Dokument konnte nicht aktualisiert werden: " + response.getStatusText());
				System.err.println(response.getStackTrace());
			}
		} else {
			System.err.println("Dokument konnte nicht ausgecheckt werden: " + response.getStatusText());
			System.err.println(response.getStackTrace());
		}
		ret = Integer.parseInt(response.getStatusCode());

		return ret;
	}

	public static int updateDocumentByFile(final String documentId, final String uri, final String description,
			final String mimeType, final String server, final String username, final String password, final String proxyHost,
			final String proxyPort, final Credentials credentials) {
		int ret = 0;

		final AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort,
				credentials);
		AlfrescoResponse response = AccessController.doPrivileged(new PrivilegedAction<AlfrescoResponse>() {

			public AlfrescoResponse run() {
				try {
					return connector.checkout(documentId);
				} catch (IOException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					AlfrescoResponse response = new AlfrescoResponse();
					response.setStatusCode(ResponseType.SERVER_ERROR.toString());
					response.setStatusText(e.getMessage());
					return response;
				}
			}
		});
		if (response != null && ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
			Document<Element> doc = response.getDocument();
			Entry responseEntry = (Entry) doc.getRoot();
			String tmp = responseEntry.getId().getPath();
			final String id = tmp.substring(tmp.lastIndexOf(":") + 1);
			response = AccessController.doPrivileged(new PrivilegedAction<AlfrescoResponse>() {

				public AlfrescoResponse run() {
					try {

						try {
							return connector.updateCheckedOutFile(uri, description, mimeType, id);
						} catch (URISyntaxException e) {
							System.out.println(e.getMessage());
							e.printStackTrace();
							AlfrescoResponse response = new AlfrescoResponse();
							response.setStatusCode(ResponseType.SERVER_ERROR.toString());
							response.setStatusText(e.getMessage());
							return response;
						}
					} catch (IOException e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
						AlfrescoResponse response = new AlfrescoResponse();
						response.setStatusCode(ResponseType.SERVER_ERROR.toString());
						response.setStatusText(e.getMessage());
						return response;
					}
				}
			});
			if (response != null && ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
				response = AccessController.doPrivileged(new PrivilegedAction<AlfrescoResponse>() {

					public AlfrescoResponse run() {
						try {
							return connector.checkin(id, false, "");
						} catch (IOException e) {
							System.out.println(e.getMessage());
							e.printStackTrace();
							AlfrescoResponse response = new AlfrescoResponse();
							response.setStatusCode(ResponseType.SERVER_ERROR.toString());
							response.setStatusText(e.getMessage());
							return response;
						}
					}
				});
				if (response == null || !ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
					System.err.println("Dokument konnte nicht eingecheckt werden: " + response.getStatusText());
					System.err.println(response.getStackTrace());
				}
			} else {
				System.err.println("Dokument konnte nicht aktualisiert werden: " + response.getStatusText());
				System.err.println(response.getStackTrace());
			}
		} else {
			System.err.println("Dokument konnte nicht ausgecheckt werden: " + response.getStatusText());
			System.err.println(response.getStackTrace());
		}
		ret = Integer.parseInt(response.getStatusCode());

		return ret;
	}

	public void save(final String fileName, final String string) {
		AccessController.doPrivileged(new PrivilegedAction<String>() {

			public String run() {
				try {
					File file = new File(new URI(fileName));
					StringReader stringReader = new StringReader(string);
					BufferedReader bufferedReader = new BufferedReader(stringReader);
					FileOutputStream fos = new FileOutputStream(file);
					BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
					for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
						bufferedWriter.write(line);
						bufferedWriter.newLine();
					}
					bufferedReader.close();
					bufferedWriter.close();
				} catch (IOException e1) {
					System.err.println(e1.getMessage());
					e1.printStackTrace();
				} catch (URISyntaxException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
				return null;
			}
		});

	}

	public void openPDF(final String fileName) {
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
			final byte[] ret = entry.getData();
			final String name = entry.getName();
			try {
				AccessController.doPrivileged(new PrivilegedAction<String>() {

					public String run() {
						try {
							String property = "java.io.tmpdir";

							// Get the temporary directory and print it.
							String tempDir = System.getProperty(property);
							File file = new File(tempDir + "/" + name);
							FileOutputStream fos = new FileOutputStream(file);
							fos.write(ret);
							fos.flush();
							fos.close();
							getAppletContext().showDocument(file.toURL(), "_blank");
						} catch (IOException e) {
							System.out.println(e.getMessage());
							e.printStackTrace();

						}
						return "";
					}

				});
			} catch (Exception e) {
				System.out.println("Unable to extract ZIP.");
				System.out.println(e.getMessage());
			}
		}
		return;
	}

	public int extractZIP(final String name) {

		int counter = 0;
		try {
			final byte[] ret = bys.toByteArray();
			counter = AccessController.doPrivileged(new PrivilegedAction<Integer>() {

				public Integer run() {
					ZipInputStream zipin = null;
					int counter = 0;
					boolean multi = false;
					try {
						InputStream bais = new ByteArrayInputStream(ret);
						zipin = new ZipInputStream(bais);
						ZipEntry entry = null;
						int size;
						entries.clear();
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
							counter++;
							if (it.hasNext())
								multi = true;
							String entryFileName = ent.getName();
							InputStream b = new ByteArrayInputStream(ent.getData());
							PDFConnector con = new PDFConnector();
							String result = con.pdftoText(b);
							if (multi)
								jsobject.call("loadMultiText", new String[] { result, entryFileName, "application/zip", "true",
										"false", name });
							else
								jsobject.call("loadText", new String[] { result, entryFileName, "application/zip", name });
						}
					} catch (Exception e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
					} finally {
						try {
							zipin.close();
						} catch (IOException e) {
							System.err.println(e.getMessage());
							e.printStackTrace();
						}
					}
					return counter;
				}
			});
		} catch (Exception e) {
			System.out.println("Unable to extract ZIP.");
			System.out.println(e.getMessage());
		}
		return counter;
	}

	// Extracts text from a PDF Document and writes it to a text file
	public void extract(String fileName, boolean multi, String typ) {
		entries.clear();
		final byte[] ret = bys.toByteArray();
		entries.add(new FileEntry(fileName, ret));
		String result = "";
		try {
			result = AccessController.doPrivileged(new PrivilegedAction<String>() {

				public String run() {
					InputStream bais = new ByteArrayInputStream(ret);
					PDFConnector con = new PDFConnector();
					return con.pdftoText(bais);
				}
			});
			if (!multi)
				jsobject.call("loadText", new String[] { result, fileName, typ, null });
			else
				jsobject.call("loadMultiText", new String[] { result, fileName, typ, "false", "false", null });
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public void getData(final String str, final boolean init) {
		final byte[] ret = Base64.decodeBase64(str);
		try {
			if (init)
				bys.reset();
			bos.write(ret, 0, ret.length);
			bos.flush();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}

	public String openFile(final String filePath) {

		String ret = null;
		ret = AccessController.doPrivileged(new PrivilegedAction<String>() {

			public String run() {
				String ret = null;
				StringBuffer fileData = new StringBuffer(1000);
				try {
					InputStream inp = new FileInputStream(new File(new URI(filePath)));
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
					ret = "Fehler beim Öffnen der Datei: " + e.getMessage();
					e.printStackTrace();
				} catch (URISyntaxException e1) {
					ret = "Fehler beim Öffnen der Datei: " + e1.getMessage();
					e1.printStackTrace();
				}
				return ret;
			}
		});
		return ret;
	}

	public String test() {
		return "Hier ist das Verteilungs Applet!";
	}

}