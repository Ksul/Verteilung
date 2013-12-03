package de.schulte.testverteilung;

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

    @Before
    public void setUp() throws Exception {
        conn = new AlfrescoConnector("http://localhost:8080", "admin", "admin", null, null, null);
    }

    @Test
    public void testListFolder() throws Exception {
        conn.listFolder("", true);
    }

    @Test
    public void testGetNode() throws VerteilungException {
        conn.getNode("");
    }
}
