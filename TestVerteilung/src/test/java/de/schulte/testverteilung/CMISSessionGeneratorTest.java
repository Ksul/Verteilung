package de.schulte.testverteilung;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
      CMISSessionGenerator gen = new CMISSessionGenerator(properties.getProperty("user"), properties.getProperty("password"), properties.getProperty("bindingUrl"), "Archiv");
      Session ses = gen.generateSession();
      Assert.assertThat(ses, Matchers.notNullValue());
      Assert.assertThat(gen.getRepositoryName(), Matchers.equalTo("Archiv"));
    }
}
