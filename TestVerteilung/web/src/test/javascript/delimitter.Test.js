/**
 * Created by klaus on 09.02.2015.
 */

DelimitterTest = TestCase("DelimitterTest");


DelimitterTest.prototype.test1 = function() {
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

DelimitterTest.prototype.test2 = function() {
    var text = "     Dies ist ein  Test";
    var erg = new SearchResultContainer();
    var result = new SearchResult(text, "Test", 0, text.length, "String", "asd");
    erg.addResult(result);
    var rules = '<delimitter debugLevel="trace" typ="start" count="6"  text="&#0032;" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
    erg = delimitter.resolve(erg, false);
    assertEquals("ist ein  Test", erg.getResult().text);
    assertEquals(10, erg.getResult().start);
};

DelimitterTest.prototype.test3 = function() {
    var text = "\n \nDies ist ein Test";
    var erg = new SearchResultContainer();
    var result = new SearchResult(text, "Test", 0, text.length, "String", "asd");
    erg.addResult(result);
    var rules = '<delimitter debugLevel="trace" typ="start" count="2"  text="&#0010;"/>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
    erg = delimitter.resolve(erg, false);
    assertEquals("Dies ist ein Test", erg.getResult().text);
    assertEquals(3, erg.getResult().start);
};

DelimitterTest.prototype.test4 = function() {
    var text = "Dies\nist\nein\nTest";
    var erg = new SearchResultContainer();
    var result = new SearchResult(text, "Test", 0, text.length, "String", "asd");
    erg.addResult(result);
    var rules = '<delimitter debugLevel="trace" typ="start" count="-3"  text="&#0010;"/>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
    erg = delimitter.resolve(erg, false);
    assertEquals("ist\nein\nTest", erg.getResult().text);
    assertEquals(5, erg.getResult().start);
};

DelimitterTest.prototype.test5 = function() {
    var text = "Dies\nist\nein\nTest\n";
    var erg = new SearchResultContainer();
    var result = new SearchResult(text, "Test", 0, text.length, "String", "asd");
    erg.addResult(result);
    var rules = '<delimitter debugLevel="trace" typ="start" count="-3"  text="&#0010;"/>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
    erg = delimitter.resolve(erg, false);
    assertEquals("ein\nTest\n", erg.getResult().text);
    assertEquals(9, erg.getResult().start);
};

DelimitterTest.prototype.test6 = function() {
    var text = "Dies ist ein Test";
    var erg = new SearchResultContainer();
    var result = new SearchResult(text, "Test", 0, text.length, "String", "asd");
    erg.addResult(result);
    var rules = '<delimitter debugLevel="trace" typ="start" count="3"  text="e"/>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
    erg = delimitter.resolve(erg, false);
    assertEquals("st", erg.getResult().text);
    assertEquals(15, erg.getResult().start);
};

DelimitterTest.prototype.test7 = function() {
    var text = "Dies ist ein Test";
    var erg = new SearchResultContainer();
    var result = new SearchResult(text, "Test", 0, text.length, "String", "asd");
    erg.addResult(result);
    var rules = '<delimitter debugLevel="trace" typ="end" count="3"  text="e"/>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
    erg = delimitter.resolve(erg, false);
    assertEquals("Dies ist ein T", erg.getResult().text);
    assertEquals(14, erg.getResult().end);
};

DelimitterTest.prototype.test8 = function() {
    var text = "Dies ist ein Test";
    var erg = new SearchResultContainer();
    var result = new SearchResult(text, "Test", 0, text.length, "String", "asd");
    erg.addResult(result);
    var rules = '<delimitter debugLevel="trace" typ="end" count="2"  text="e"/>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var delimitter = new Delimitter(new XMLObject(XMLDoc.docNode));
    erg = delimitter.resolve(erg, false);
    assertEquals("Dies ist ", erg.getResult().text);
    assertEquals(9, erg.getResult().end);
};


