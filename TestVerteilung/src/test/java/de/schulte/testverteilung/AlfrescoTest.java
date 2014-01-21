package de.schulte.testverteilung;

import junit.framework.Assert;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 13.01.14
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoTest {

    String host;
    String password;

    @Before
    public void setUp() throws Exception {
        FileReader fr = new FileReader("passwd");
        Assert.assertNotNull(fr);
        BufferedReader br = new BufferedReader(fr);
        password = br.readLine();
        Assert.assertNotNull(password);
        Assert.assertTrue(password.length() > 0);
        FileReader fr1 = new FileReader("host");
        Assert.assertNotNull(fr1);
        br = new BufferedReader(fr1);
        String hostname = br.readLine();
        Assert.assertNotNull(hostname);
        Assert.assertTrue(hostname.length() > 0);
        host = hostname + "/alfresco/service/cmis";
    }


}
