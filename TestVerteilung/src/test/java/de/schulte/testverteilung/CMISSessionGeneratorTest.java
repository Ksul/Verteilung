package de.schulte.testverteilung;

import de.schulte.testverteilung.AlfrescoConnectorNew;
import de.schulte.testverteilung.AlfrescoTest;
import de.schulte.testverteilung.CMISSessionGenerator;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.chemistry.opencmis.client.api.Session;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 08.01.14
 * Time: 17:18
 * To change this template use File | Settings | File Templates.
 */
public class CMISSessionGeneratorTest extends AlfrescoTest{


    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGenerateSession() throws Exception {
      CMISSessionGenerator gen = new CMISSessionGenerator("admin", properties.getProperty("password"), properties.getProperty("bindingUrl"), "Archiv");
      Session ses = gen.generateSession();
      Assert.assertNotNull(ses);
      Assert.assertEquals("Archiv", gen.getRepositoryName());
    }
}
