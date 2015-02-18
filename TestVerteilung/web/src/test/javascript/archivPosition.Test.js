/**
 * Created by m500288 on 18.02.15.
 */

ArchivPositionTest = TestCase("ArchivPositionTest");


ArchivPositionTest.prototype.test1 = function() {
    var text = "     Dies ist ein  Test";
    var erg = new SearchResultContainer();
    var result = new SearchResult(text, "Test", 0, text.length, "String", "asd");
    erg.addResult(result);
    var rules = '<delimitter typ="start" count="5" text="&#0032;" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
    erg = delimitter.resolve(erg, false);
    assertEquals("Dies ist ein  Test", erg.getResult().text);
    assertEquals(5, erg.getResult().start);
};
