package de.schulte.testverteilung;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 19.12.13
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoServices {

    private AlfrescoConnector connector;

    public AlfrescoServices(String server, String username, String password, String proxyHost, String proxyPort) {
        super();
        this.connector = new AlfrescoConnector(server, username, password, proxyHost, proxyPort, null);
    }


    /**
     * liefert die Dokumente eines Alfresco Folders als JSON Objekte
     * @param filePath     der Pfad, der geliefert werden soll
     * @return             der Inhalt des Verzeichnisses als JSON Objekte
     * @throws IOException
     * @throws VerteilungException
     * @throws org.json.JSONException
     */
    protected Object listFolder(String filePath, boolean byPath) throws IOException, VerteilungException {

        ArrayList<Properties> liste = new ArrayList<Properties>();
        boolean folder = false;


        AlfrescoResponse response = connector.listFolder(filePath, byPath);
           if (!Response.ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
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
                                    && el.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cmis:objectId")) {
                                String id = el.getFirstChild(CMISConstants.VALUE).getText();
                                p.put("id", id.substring(id.lastIndexOf('/') + 1));
                            }
                            if (el.getAttributeValue("propertyDefinitionId") != null
                                    && el.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cmis:name"))
                                p.put("name", el.getFirstChild(CMISConstants.VALUE).getText());
                            if (el.getElements().size() > 0) {
                                Iterator<Element> it3 = el.getElements().iterator();
                                while (it3.hasNext()) {
                                    Element el1 = it3.next();
                                    if (el1.getElements().size() > 0) {
                                        Iterator<Element> it4 = el1.getElements().iterator();
                                        while (it4.hasNext()) {
                                            Element el2 = it4.next();
                                            if (el2.getAttributeValue("propertyDefinitionId") != null && el2.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("cm:title"))
                                                p.put("title", el2.getFirstChild(CMISConstants.VALUE).getText());
                                            if (el2.getAttributeValue("propertyDefinitionId") != null && el2.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("my:documentDate"))
                                                p.put("date", el2.getFirstChild(CMISConstants.VALUE).getText());
                                            if (el2.getAttributeValue("propertyDefinitionId") != null && el2.getAttributeValue("propertyDefinitionId").equalsIgnoreCase("my:person"))
                                                p.put("person", el2.getFirstChild(CMISConstants.VALUE).getText());
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
                        if (p.containsKey("name") && p.containsKey("id")) {
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
     * @param filePath     der Pfad, der geliefert werden soll
     * @param listFolder   was soll geliefert werden: 0: Folder und Dokumente,  1: nur Dokumente,  -1: nur Folder
     * @return             der Inhalt des Verzeichnisses als JSON Objekte
     * @throws IOException
     * @throws VerteilungException
     * @throws org.json.JSONException
     */
    public JSONArray listFolderAsJSON(String filePath, int listFolder) throws IOException, VerteilungException, JSONException {
        JSONObject o;
        JSONObject o1;
        JSONArray list = new JSONArray();
        if (filePath == null || filePath.length() == 0) {
            filePath = (String) getNodeId("SELECT * from cmis:folder where CONTAINS('PATH:\"//app:company_home/cm:Archiv\"')");
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
                filePath = (String) getNodeId("SELECT * from cmis:folder where CONTAINS('PATH:\"//app:company_home/cm:Archiv\"')");

            ArrayList<Properties> liste = (ArrayList<Properties>) listFolder(filePath, false);

            for (int i = 0; i < liste.size(); i++) {
                Properties p = (Properties) liste.get(i);
                boolean isFolder = ((Boolean) p.get("folder")).booleanValue();
                if (listFolder == 0 || (isFolder && listFolder < 1) || (!isFolder && listFolder > -1)) {
                    o = new JSONObject();
                    o1 = new JSONObject();
                    o.put("id", p.getProperty("id"));
                    if (isFolder) {
                        o.put("rel", "folder");
                        o1.put("state", "closed");
                    } else {
                        o.put("rel", "default");
                        o1.put("state", "");
                    }

                    if (p.containsKey("title") && p.getProperty("title").length() > 0)
                        o1.put("data", p.getProperty("title"));
                    else
                        o1.put("data", p.getProperty("name"));
                    o1.put("attr", o);
                    list.put(o1);
                }
            }
        }
        return list;
    }

    /**
     * liefert eine NodeId  mit Hilfe einer CMIS Query
     * @param cmisQuery
     * @return die Id
     * @throws VerteilungException
     */
    protected Object getNodeId(String cmisQuery) throws VerteilungException {

        String ret = "";

        AlfrescoResponse response = connector.getNode(cmisQuery);

        if (!Response.ResponseType.SUCCESS.toString().equals(response.getResponseType())) {
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


}
