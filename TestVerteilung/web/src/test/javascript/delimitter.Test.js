/**
 * Created by klaus on 09.02.2015.
 */

DelimitterTest = TestCase("DelimitterTest");

var text = "\n \n\ Dies ist\n \n\n ein  Test  \n \n";

DelimitterTest.prototype.test1 = function() {
    var text = "     Dies ist ein  Test";
    var erg = new SearchResultContainer();
    var result = new SearchResult(text, "Test", 1, text.length, "String", "asd");
    erg.addResult(result);
    var rules = '<delimitter typ="start" count="5" text=" " />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    var delimitter = new Delimitter(new XMLObject(XMLDOC.docNode));
    erg = delimitter.resolve(erg, false);
    jstestdriver.console.log(erg.toString());
    assertEquals("Dies ist ein  Test", erg.getResult().text);
    assertEquals(6, erg.getResult().start);
};

DelimitterTest.prototype.test2 = function() {
    REC.mess ="";
    var text = "     Dies ist ein  Test";
    var erg = new SearchResultContainer();
    var result = new SearchResult(text, "Test", 1, text.length, "String", "asd");
    erg.addResult(result);
    var rules = '<delimitter debugLevel="trace" typ="start" count="6" text="1" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    var delimitter = new Delimitter(new XMLObject(XMLDOC.docNode));
    erg = delimitter.resolve(erg, false);
    jstestdriver.console.log(REC.getMessage());
    assertEquals("ist ein  Test", erg.getResult().text);
    assertEquals(11, erg.getResult().start);
};


