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
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogManager;
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


import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VerteilungApplet extends Applet {

	private static final long serialVersionUID = 1L;

	JSObject jsobject;

	ByteArrayOutputStream bys = new ByteArrayOutputStream();

	BufferedOutputStream bos = new BufferedOutputStream(bys);

	Collection<FileEntry> entries = new ArrayList<FileEntry>();

    private static Logger logger = Logger.getLogger(VerteilungApplet.class.getName());

    private static String level = "WARNING";


	public void init() {
		try {
			logger.info("Hier ist das Verteilungsapplet");
            level = getParameter ("debug");
            Logger log = LogManager.getLogManager().getLogger("");
            for (Handler h : log.getHandlers()) {
                h.setLevel(Level.parse(level));
            }
			jsobject = JSObject.getWindow(this);
		} catch (Exception jse) {
			logger.severe(jse.getMessage());
			jse.printStackTrace();
		}
	}


    /**
     * prüft, ob eine Url verfügbar ist
     * @param urlString    die Url
     * @return             ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    ret               true, wenn die URL verfügbar ist
     */
    public static String isURLAvailable(final String urlString) {

        JSONObject ret = new JSONObject();

        try {
        logger.info("Aufruf Methode isURLAvailabel with " + urlString);
        ret = AccessController.doPrivileged(new PrivilegedAction<JSONObject>() {

            public JSONObject run() {

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
                    } catch(JSONException jse){
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
                    } catch(JSONException jse){
                        logger.severe(jse.getMessage());
                        jse.printStackTrace();
                    }
                }
                return obj;
            }
        });
        } catch (Exception e) {
            String error = "Fehler beim Check der URL: " + e.getMessage();
            logger.severe(error);
            e.printStackTrace();
            try {
                ret.put("success", false);
                ret.put("result", error);
            } catch(JSONException jse){
                logger.severe(jse.getMessage());
                jse.printStackTrace();
            }
        }
        return ret.toString();
    }

    /**
     * liefert ein Alfreco Ticket
     * @param server          der Alfresco Server
     * @param username        der verwendete Username
     * @param password        das Passwort
     * @param proxyHost       der Proxyhost, falls verwendet
     * @param proxyPort       der Proxyport, falls verwendet
     * @param credentials
     * @return                das Ticket als String
     */
    public String getTicket(final String server, final String username, final String password, final String proxyHost,
                            final String proxyPort, final Credentials credentials) {
        String ret;
        try {
            ret = AccessController.doPrivileged(new PrivilegedAction<String>() {

                public String run() {
                    AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort,
                            credentials);
                    return connector.getTicket();
                }
            });
        } catch (Exception e) {
           logger.severe(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return ret;
    }

    /**
     * verschiebt ein Dokument
     * @param documentId         die Id des zu verschiebenden Dokumentes
     * @param destinationId      die Id des Folders, wo das Dokument hin verschoben werden soll
     * @param server             der Alfresco Server
     * @param username           der verwendete Username
     * @param password           das Passwort
     * @param proxyHost          der Proxyhost, falls verwendet
     * @param proxyPort          der Proxyport, falls verwendet
     * @param credentials
     * @return                   das Ergebnis als JSON String
     * @throws JSONException
     */
	public String moveDocument(final String documentId, final String destinationId, final String server,
			final String username, final String password, final String proxyHost, final String proxyPort,
			final Credentials credentials) throws JSONException {
		JSONObject ret = new JSONObject();
		JSONObject obj;
		try {
			obj = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

				public JSONObject run() throws VerteilungException, JSONException {
					JSONObject o;
					AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort,
							credentials);
					o = new JSONObject(connector.moveDocument(documentId, destinationId));
					if (!o.getBoolean("overallSuccess"))
						throw new VerteilungException(o.toString());
					return o;
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

    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     * @param filePath     der Pfad, der geliefert werden soll
     * @param listFolder   was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @param server       der Alfresco-Servername
     * @param username     der Alfresco-Username
     * @param password     das Alfresco-Passwort
     * @return             ein JSONObject mit den Feldern success: true     die Opertation war erfolgreich
     *                                                             false    ein Fehler ist aufgetreten
     *                                                    ret               der Inhalt des Verzeichnisses als JSON Objekte
     * @throws IOException
     * @throws VerteilungException
     * @throws JSONException
     */
    public String listFolderAsJSON(final String filePath, final String listFolder, final String server, final String username, final String password) throws IOException, VerteilungException, JSONException {
        JSONObject ret = null;

        try {
            ret = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

                public JSONObject run() throws VerteilungException, IOException, JSONException {
                    AlfrescoServices services = new AlfrescoServices(server, username, password);
                    return services.listFolderAsJSON(filePath, Integer.parseInt(listFolder));
                }
            });
        } catch (PrivilegedActionException e) {
            ret.put("success", false);
            ret.put("result", e.getMessage());
        }

        return ret.toString();
    }


    /**
     * liefert eine NodeID als String zurück
     * @param cmisQuery   die CMIS Query, mit der der Knoten gesucht werden soll
     * @param server       der Alfresco-Servername
     * @param username     der Alfresco-Username
     * @param password     das Alfresco-Passwort
     * @return die NodeId als String
     */
	public static String getNodeId(final String cmisQuery, final String server, final String username,
			final String password) {

		JSONObject ret = new JSONObject();
		try {
			try {
				ret = AccessController.doPrivileged(new PrivilegedExceptionAction<JSONObject>() {

					public JSONObject run() throws JSONException {
                        try {
						AlfrescoServices services = new AlfrescoServices(server, username, password);
						return services.getNodeId(cmisQuery);
                        } catch (Throwable t) {
                            JSONObject obj = new JSONObject();
                            obj.put("success", false);
                            obj.put("result", t.getMessage());
                            return obj;
                        }
					}
				});
			} catch (PrivilegedActionException e) {
				ret.put("success", false);
				ret.put("result", e.getMessage());
				return ret.toString();
			}

        } catch (JSONException e) {
            logger.severe(e.getLocalizedMessage());
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
					logger.severe(e.getMessage());
					e.printStackTrace();
					AlfrescoResponse response = new AlfrescoResponse();
					response.setStatusCode(ResponseType.SERVER_ERROR.toString());
					response.setStatusText(e.getMessage());
					return response;
				}
			}
		});
		if (!ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
			logger.warning("Dokument konnte nicht hochgeladen werden." + response.getStatusText());
			logger.warning(response.getStackTrace());
		}

	}

	public static String getContent(final String docId, final boolean extract, final String server,
			final String username, final String password, final String proxyHost, final String proxyPort,
			final Credentials credentials) throws JSONException {
		String ret;
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
								logger.severe(e.getMessage());
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
		int ret;

		final AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort,
				credentials);
		AlfrescoResponse response = AccessController.doPrivileged(new PrivilegedAction<AlfrescoResponse>() {

			public AlfrescoResponse run() {
				try {
					return connector.checkout(documentId);
				} catch (IOException e) {
					logger.severe(e.getMessage());
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
						logger.severe(e.getMessage());
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
							logger.severe(e.getMessage());
							e.printStackTrace();
							AlfrescoResponse response = new AlfrescoResponse();
							response.setStatusCode(ResponseType.SERVER_ERROR.toString());
							response.setStatusText(e.getMessage());
							return response;
						}
					}
				});
				if (response == null || !ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
					logger.warning("Dokument konnte nicht eingecheckt werden: " + response != null ? response.getStatusText(): "");
					logger.warning(response.getStackTrace());
				}
			} else {
				logger.warning("Dokument konnte nicht aktualisiert werden: " + response != null ? response.getStatusText(): "");
				logger.warning(response.getStackTrace());
			}
		} else {
			logger.warning("Dokument konnte nicht ausgecheckt werden: " + response != null ? response.getStatusText(): "");
			logger.warning(response.getStackTrace());
		}
		ret = Integer.parseInt(response.getStatusCode());

		return ret;
	}

	public static int updateDocumentByFile(final String documentId, final String uri, final String description,
			final String mimeType, final String server, final String username, final String password, final String proxyHost,
			final String proxyPort, final Credentials credentials) {
		int ret;

		final AlfrescoConnector connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort,
				credentials);
		AlfrescoResponse response = AccessController.doPrivileged(new PrivilegedAction<AlfrescoResponse>() {

			public AlfrescoResponse run() {
				try {
					return connector.checkout(documentId);
				} catch (IOException e) {
					logger.severe(e.getMessage());
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
							logger.severe(e.getMessage());
							e.printStackTrace();
							AlfrescoResponse response = new AlfrescoResponse();
							response.setStatusCode(ResponseType.SERVER_ERROR.toString());
							response.setStatusText(e.getMessage());
							return response;
						}
					} catch (IOException e) {
						logger.severe(e.getMessage());
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
							logger.severe(e.getMessage());
							e.printStackTrace();
							AlfrescoResponse response = new AlfrescoResponse();
							response.setStatusCode(ResponseType.SERVER_ERROR.toString());
							response.setStatusText(e.getMessage());
							return response;
						}
					}
				});
				if (response == null || !ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
					logger.warning("Dokument konnte nicht eingecheckt werden: " + response != null ? response.getStatusText(): "");
					logger.warning(response.getStackTrace());
				}
			} else {
				logger.warning("Dokument konnte nicht aktualisiert werden: " + response != null ? response.getStatusText(): "");
				logger.warning(response.getStackTrace());
			}
		} else {
			logger.warning("Dokument konnte nicht ausgecheckt werden: " + response != null ? response.getStatusText(): "");
			logger.warning(response.getStackTrace());
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
					logger.severe(e1.getMessage());
					e1.printStackTrace();
				} catch (URISyntaxException e) {
					logger.severe(e.getMessage());
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
							getAppletContext().showDocument(file.toURI().toURL(), "_blank");
						} catch (IOException e) {
							logger.severe(e.getMessage());
							e.printStackTrace();

						}
						return "";
					}

				});
			} catch (Exception e) {
				logger.severe("Unable to extract ZIP.");
				logger.severe(e.getMessage());
                e.printStackTrace();
			}
		} else {
            logger.warning("Unable to find PDF in extracted ZIP.");
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
						ZipEntry entry;
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
						logger.severe(e.getMessage());
						e.printStackTrace();
					} finally {
						try {
							zipin.close();
						} catch (IOException e) {
							logger.severe(e.getMessage());
							e.printStackTrace();
						}
					}
					return counter;
				}
			});
		} catch (Exception e) {
			logger.severe("Unable to extract ZIP.");
			logger.severe(e.getMessage());
            e.printStackTrace();
		}
		return counter;
	}

	// Extracts text from a PDF Document and writes it to a text file
	public void extract(String fileName, boolean multi, String typ) {
		entries.clear();
		final byte[] ret = bys.toByteArray();
		entries.add(new FileEntry(fileName, ret));
		String result;
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
			logger.severe(e.getMessage());
			e.printStackTrace();
		}
	}

	public void getData(final String str, final boolean init) {

		try {
            final byte[] ret = Base64.decodeBase64(str);
			if (init)
                bys.reset();
			bos.write(ret, 0, ret.length);
			bos.flush();
		} catch (Exception e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}

	}

    public String openFile(final String filePath) {

        String ret = AccessController.doPrivileged(new PrivilegedAction<String>() {

            public String run() {
                String r;
                StringBuilder fileData = new StringBuilder(1000);
                try {
                    InputStream inp = new FileInputStream(new File(new URI(filePath)));
                    InputStreamReader isr = new InputStreamReader(inp, "UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                    char[] buf = new char[1024];
                    int numRead;
                    while ((numRead = reader.read(buf)) != -1) {
                        String readData = String.valueOf(buf, 0, numRead);
                        fileData.append(readData);
                        buf = new char[1024];
                    }
                    reader.close();
                    r = fileData.toString();
                } catch (IOException e) {
                    r = "Fehler beim Öffnen der Datei: " + e.getMessage();
                    e.printStackTrace();
                } catch (URISyntaxException e1) {
                    r = "Fehler beim Öffnen der Datei: " + e1.getMessage();
                    e1.printStackTrace();
                }
                return r;
            }
        });
        return ret;
    }

    /**
     * Testmethode
     * @return
     */
    public static String test() {
		return "Hier ist das Verteilungs Applet!";
	}

}