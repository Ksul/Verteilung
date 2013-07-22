package de.schulte.testverteilung;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: m500288
 * Date: 22.07.13
 * Time: 16:18
 * To change this template use File | Settings | File Templates.
 */
public class VerteilungAppletTest {
    @Test
    public void testIsURLAvailable() throws Exception {
        System.out.println(VerteilungApplet.isURLAvailable("http://ksul.dyndns.org:9080", "www-proxy", "8080"));
    }
}
