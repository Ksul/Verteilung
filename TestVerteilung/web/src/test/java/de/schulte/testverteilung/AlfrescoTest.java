package de.schulte.testverteilung;

import org.junit.Before;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.01.14
 * Time: 16:05
 */
public class AlfrescoTest {

    Properties properties;

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
        FileInputStream fileInputStream = new FileInputStream("test.properties");
        assertNotNull(fileInputStream);
        properties.load(fileInputStream);
        assertNotNull(properties.getProperty("server"));
        assertNotNull(properties.getProperty("password"));
        assertNotNull(properties.getProperty("testPDF"));
        properties.put("bindingUrl", properties.getProperty("bindingUrl"));
        if (needsProxy()) {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", "www-proxy");
            System.getProperties().put("proxyPort", "8080");
        } else {
            System.getProperties().put("proxySet", "false");
            System.getProperties().put("proxyHost", "");
            System.getProperties().put("proxyPort", "");
        }
    }

    protected byte[] readFile(String name) throws IOException {

        File sourceFile = new File(name);
        FileInputStream in = new FileInputStream(sourceFile);
        byte[] buffer = new byte[(int) sourceFile.length()];
        //noinspection ResultOfMethodCallIgnored
        in.read(buffer);
        return buffer;
    }

    protected boolean needsProxy() {

        try {
            return InetAddress.getByName("www-proxy").isReachable(5000);
        } catch (Exception e) {
            return false;
        }
    }
}
