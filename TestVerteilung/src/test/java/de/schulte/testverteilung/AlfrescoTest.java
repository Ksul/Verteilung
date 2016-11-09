package de.schulte.testverteilung;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.01.14
 * Time: 16:05
 */
public class AlfrescoTest {

    Properties properties;
    protected AlfrescoConnector con;

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/connection.properties");
        Assert.assertThat(fileInputStream, Matchers.notNullValue());
        properties.load(fileInputStream);
        Assert.assertThat(properties.getProperty("server"), Matchers.notNullValue());
        Assert.assertThat(properties.getProperty("binding"), Matchers.notNullValue());
        Assert.assertThat(properties.getProperty("password"), Matchers.notNullValue());
        Assert.assertThat(properties.getProperty("testPDF"), Matchers.notNullValue());
        if (needsProxy()) {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", "www-proxy");
            System.getProperties().put("proxyPort", "8080");
        } else {
            System.getProperties().put("proxySet", "false");
            System.getProperties().put("proxyHost", "");
            System.getProperties().put("proxyPort", "");
        }
        con = new AlfrescoConnector(properties.getProperty("user"), properties.getProperty("password"), properties.getProperty("server"), properties.getProperty("binding"));
        assertThat(con, Matchers.notNullValue());
        shutDown();
    }

    @After
    public void shutDown() throws Exception {
        CmisObject cmisObject = con.getNode("/TestFolder/TestDocument.txt");
        if (cmisObject != null && cmisObject instanceof Document) {
            if (((Document) cmisObject).isVersionSeriesCheckedOut())
                ((Document) cmisObject).cancelCheckOut();
            cmisObject.delete(true);
        }
        cmisObject = con.getNode("/TestFolder/TestDocument.txt");
        if (cmisObject != null && ((Document) cmisObject).isVersionSeriesCheckedOut())
            ((Document) cmisObject).cancelCheckOut();
        if (cmisObject != null && cmisObject instanceof Document)
            cmisObject.delete(true);
        cmisObject = con.getNode("/Archiv/Test.pdf");
        if (cmisObject != null && ((Document) cmisObject).isVersionSeriesCheckedOut())
            ((Document) cmisObject).cancelCheckOut();
        if (cmisObject != null && cmisObject instanceof Document)
            cmisObject.delete(true);
        cmisObject = con.getNode("/TestFolder");
        if (cmisObject != null && cmisObject instanceof Folder)
            ((Folder) cmisObject).deleteTree(true, UnfileObject.DELETE, true);
        cmisObject = con.getNode("/FolderTest");
        if (cmisObject != null && cmisObject instanceof Folder)
            ((Folder) cmisObject).deleteTree(true, UnfileObject.DELETE, true);
    }

    /**
     * liest eine Date
     * @param   name            der Name der Datei
     * @return  byte[]          der Inhalt der Datei als byte Array
     * @throws IOException
     */
    protected byte[] readFile(String name) throws IOException {

        File sourceFile = new File(name);
        FileInputStream in = new FileInputStream(sourceFile);
        byte[] buffer = new byte[(int) sourceFile.length()];
        in.read(buffer);
        return buffer;
    }

    /**
     * prüft, ob ein Proxy zur Verbindung ins Internet beötigt wird
     * @return
     */
    protected boolean needsProxy() {

        try {
            return InetAddress.getByName("www-proxy").isReachable(5000);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * baut einen Folder auf
     * @param  name                        der Name des Folders
     * @return CmisObject                  der Folder als CmisObject
     * @throws VerteilungException
     */
    protected CmisObject buildTestFolder(String name, CmisObject folder) throws VerteilungException {
        if (folder == null)
            folder = con.getNode("/");
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        Map<String, Object> props = new HashMap<>();
        List<String> aspects = new ArrayList<>();
        aspects.add("P:cm:titled");
        props.put("cm:title", "");
        props.put(PropertyIds.NAME, name);
        props.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);
        folder = con.createFolder((Folder) folder, props);
        assertThat(folder, Matchers.notNullValue());
        assertThat(folder, Matchers.instanceOf( Folder.class));
        assertThat(folder.getName(), Matchers.equalTo(name));
        return folder;
    }


    /**
     * baut ein Document auf
     * @param  name                     der Name
     * @param  folder                   der Folder, in dem es erstellt werden soll
     * @return CmisObject               das Dokument als CmisObjeect
     * @throws VerteilungException
     */
    protected CmisObject buildDocument(String name, CmisObject folder) throws VerteilungException {
        Document document;
        assertThat(folder instanceof Folder, Matchers.is(true));
        List<String> aspects = new ArrayList<>();
        Map<String, Object> props = new HashMap<>();
        props.put("my:person", "Klaus");
        props.put("my:documentDate", Long.toString(new Date().getTime()));
        props.put("cm:title", "");
        props.put(PropertyIds.OBJECT_TYPE_ID, "D:my:archivContent");
        aspects.add("P:cm:titled");
        aspects.add("P:my:amountable");
        aspects.add("P:my:idable");
        props.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects);
        document = con.createDocument((Folder) folder, name, new byte[]{}, VerteilungConstants.DOCUMENT_TYPE_TEXT, props, VersioningState.MINOR);
        assertThat(document, Matchers.notNullValue());
        assertThat(document, Matchers.instanceOf( Document.class));
        assertThat(document.getName(), Matchers.is(name));
        return document;
    }
}
