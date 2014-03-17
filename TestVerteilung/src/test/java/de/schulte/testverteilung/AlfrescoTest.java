package de.schulte.testverteilung;

import junit.framework.Assert;
import org.junit.Before;

import java.io.*;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.01.14
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoTest {

    Properties properties;

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
        FileInputStream fileInputStream = new FileInputStream("test.properties");
        Assert.assertNotNull(fileInputStream);
        properties.load(fileInputStream);
        Assert.assertNotNull(properties.getProperty("host"));
        Assert.assertNotNull(properties.getProperty("bindingUrl"));
        Assert.assertNotNull(properties.getProperty("password"));
        Assert.assertNotNull(properties.getProperty("testPDF"));
    }

    protected byte[] readFile(String name) throws FileNotFoundException, IOException {
        File sourceFile = new File(name);
        FileInputStream in = new FileInputStream(sourceFile);
        byte[] buffer = new byte[(int) sourceFile.length()];
        in.read(buffer);
        return buffer;
    }
}
