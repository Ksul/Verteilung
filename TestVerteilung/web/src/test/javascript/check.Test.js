/**
 * Created by m500288 on 19.02.15.
 */


CheckTest = TestCase("CheckTest");

CheckTest.prototype.setUp = function() {
    REC.init();
};

CheckTest.prototype.test1 = function() {
    var erg = new SearchResultContainer();
    var result = new SearchResult("100", 100, 0, 3, "int", "asd");
    erg.addResult(result);
    var rules = '  <check lowerValue="100" upperValue="300" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var check = new Check(new XMLObject(XMLDoc.docNode), {name: "Wert", objectTyp:"int"});
    erg = check.resolve(erg);
    assertTrue("Nicht geprüft", erg[0].check);
    assertNull("Fehler gefunden", erg[0].error);
};

CheckTest.prototype.test2 = function() {
    var erg = new SearchResultContainer();
    var result = new SearchResult("99", 99, 0, 3, "int", "asd");
    erg.addResult(result);
    var rules = '  <check lowerValue="100" upperValue="300" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var check = new Check(new XMLObject(XMLDoc.docNode), {name: "Wert", objectTyp:"int"});
    erg = check.resolve(erg);
    assertFalse("Fehler nicht gefunden", erg[0].check);
    assertEquals("Wert maybe wrong [99] is smaller 100", erg[0].error);
};

CheckTest.prototype.test3 = function() {
    var erg = new SearchResultContainer();
    var result = new SearchResult("301", 301, 0, 3, "int", "asd");
    erg.addResult(result);
    var rules = '  <check lowerValue="100" upperValue="300" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var check = new Check(new XMLObject(XMLDoc.docNode), {name: "Wert", objectTyp:"int"});
    erg = check.resolve(erg);
    assertFalse("Fehler nicht gefunden", erg[0].check);
    assertEquals("Wert maybe wrong [301] is bigger 300", erg[0].error);
};

CheckTest.prototype.test4 = function() {
    var erg = new SearchResultContainer();
    var result = new SearchResult("300", 300.00, 0, 3, "float", "asd");
    erg.addResult(result);
    var rules = '  <check lowerValue="100" upperValue="300" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var check = new Check(new XMLObject(XMLDoc.docNode), {name: "Wert", objectTyp:"float"});
    erg = check.resolve(erg);
    assertTrue("Nicht geprüft", erg[0].check);
    assertNull("Fehler gefunden", erg[0].error);
};

CheckTest.prototype.test5 = function() {
    var erg = new SearchResultContainer();
    var result = new SearchResult("300", 300.01, 0, 3, "float", "asd");
    erg.addResult(result);
    var rules = '  <check lowerValue="100" upperValue="300" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var check = new Check(new XMLObject(XMLDoc.docNode), {name: "Wert", objectTyp:"float"});
    erg = check.resolve(erg);
    assertFalse("Fehler nicht gefunden", erg[0].check);
    assertEquals("Wert maybe wrong [300.01] is bigger 300", erg[0].error);
};


CheckTest.prototype.test6 = function() {
    var erg = new SearchResultContainer();
    var result = new SearchResult("02.01.2015", new Date(2015,0,2), 0, 3, "date", "asd");
    erg.addResult(result);
    var rules = '  <check lowerValue="01/02/2015" upperValue="12/31/2015" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var check = new Check(new XMLObject(XMLDoc.docNode), {name: "Wert", objectTyp:"date"});
    erg = check.resolve(erg);
    assertTrue("Nicht geprüft", erg[0].check);
    assertNull("Fehler gefunden", erg[0].error);
};

CheckTest.prototype.test7 = function() {
    var erg = new SearchResultContainer();
    var result = new SearchResult("01.01.2015", new Date(2015,0,1), 0, 3, "date", "asd");
    erg.addResult(result);
    var rules = '  <check lowerValue="01/02/2015" upperValue="12/31/2015" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var check = new Check(new XMLObject(XMLDoc.docNode), {name: "Wert", objectTyp:"date"});
    erg = check.resolve(erg);
    assertFalse("Fehler nicht gefunden", erg[0].check);
    assertEquals("Wert maybe wrong [Thu Jan 01 2015 00:00:00 GMT+0100 (CET)] is smaller Fri Jan 02 2015 00:00:00 GMT+0100 (CET)", erg[0].error);
};

CheckTest.prototype.test8 = function() {
    var erg = new SearchResultContainer();
    var result = new SearchResult("01.01.2015", new Date(2016,0,1), 0, 3, "date", "asd");
    erg.addResult(result);
    var rules = '  <check lowerValue="01/02/2015" upperValue="12/31/2015" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var check = new Check(new XMLObject(XMLDoc.docNode), {name: "Wert", objectTyp:"date"});
    erg = check.resolve(erg);
    assertFalse("Fehler nicht gefunden", erg[0].check);
    assertEquals("Wert maybe wrong [Fri Jan 01 2016 00:00:00 GMT+0100 (CET)] is bigger Thu Dec 31 2015 00:00:00 GMT+0100 (CET)", erg[0].error);
};

CheckTest.prototype.test9 = function() {
    var erg = new SearchResultContainer();
    var result = new SearchResult("k", "k", 0, 3, "string", "asd");
    erg.addResult(result);
    var rules = '  <check lowerValue="b" upperValue="y" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var check = new Check(new XMLObject(XMLDoc.docNode), {name: "Wert"});
    erg = check.resolve(erg);
    assertTrue("Nicht geprüft", erg[0].check);
    assertNull("Fehler gefunden", erg[0].error);
};

CheckTest.prototype.test10 = function() {
    var erg = new SearchResultContainer();
    var result = new SearchResult("a", "a", 0, 3, "string", "asd");
    erg.addResult(result);
    var rules = '  <check lowerValue="b" upperValue="y" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var check = new Check(new XMLObject(XMLDoc.docNode), {name: "Wert"});
    erg = check.resolve(erg);
    assertFalse("Fehler nicht gefunden", erg[0].check);
    assertEquals("Wert maybe wrong [a] is smaller b", erg[0].error);
};

CheckTest.prototype.test11 = function() {
    var erg = new SearchResultContainer();
    var result = new SearchResult("z", "z", 0, 3, "string", "asd");
    erg.addResult(result);
    var rules = '  <check lowerValue="b" upperValue="y" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var check = new Check(new XMLObject(XMLDoc.docNode), {name: "Wert"});
    erg = check.resolve(erg);
    assertFalse("Fehler nicht gefunden", erg[0].check);
    assertEquals("Wert maybe wrong [z] is bigger y", erg[0].error);
};

