package de.schulte.testverteilung;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 03.12.13
 * Time: 17:29
 * To change this template use File | Settings | File Templates.
 */
public class AlfrescoConnectorTest {

    AlfrescoConnector conn;
    AlfrescoResponse resp;

    @Before
    public void setUp() throws Exception {
        conn = new AlfrescoConnector("https://ksul.spdns.org", "admin", "admin", "www-proxy", "08080", null);
    }

    @Test
    public void testListFolder() throws Exception {
        resp = conn.listFolder("", true);
        System.out.println(resp);
    }

    @Test
    public void testGetNode() throws VerteilungException {
        resp = conn.getNode("SELECT * from cmis:folder where CONTAINS('PATH:\"//app:company_home/cm:Archiv\"')");
        Assert.assertEquals("SUCCESS", resp.getResponseType());
        Assert.assertEquals((Integer) HttpStatus.SC_OK, Integer.valueOf(resp.getStatusCode()));
        Assert.assertEquals("OK", resp.getStatusText());
        System.out.println(resp);
    }
}
