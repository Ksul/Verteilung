/**
 * Created by m500288 on 19.02.15.
 */

FormatTest = TestCase("FormatTest");

FormatTest.prototype.setUp = function() {
    REC.init();
};


FormatTest.prototype.test1 = function() {
    var rules = ' <format formatString="YYYY" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var format = new Format(new XMLObject(XMLDoc.docNode));
    erg = format.resolve(new Date(2015, 0 ,1));
    assertEquals("2015", erg);
};

FormatTest.prototype.test2 = function() {
    var rules = '<format formatString="MMMM YYYY" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var format = new Format(new XMLObject(XMLDoc.docNode));
    erg = format.resolve(new Date(2015, 0 ,1));
    assertEquals("Januar 2015", erg);
};

FormatTest.prototype.test3 = function() {
    var rules = '<format formatString="MMM YYYY" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var format = new Format(new XMLObject(XMLDoc.docNode));
    erg = format.resolve(new Date(2015, 0 ,1));
    assertEquals("Jan 2015", erg);
};

FormatTest.prototype.test4 = function() {
    var rules = ' <format formatString="d.MM.YYYY" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var format = new Format(new XMLObject(XMLDoc.docNode));
    erg = format.resolve(new Date(2015, 0 ,1));
    assertEquals("01.01.2015", erg);
};

FormatTest.prototype.test5 = function() {
    var rules = ' <format formatString="###" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var format = new Format(new XMLObject(XMLDoc.docNode));
    erg = format.resolve(2);
    assertEquals("2", erg);
};

FormatTest.prototype.test6 = function() {
    var rules = ' <format formatString="000" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var format = new Format(new XMLObject(XMLDoc.docNode));
    erg = format.resolve(2);
    assertEquals("002", erg);
};

FormatTest.prototype.test7 = function() {
    var rules = ' <format formatString="000,00" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var format = new Format(new XMLObject(XMLDoc.docNode));
    erg = format.resolve(2);
    assertEquals("002,00", erg);
};

FormatTest.prototype.test8 = function() {
    var rules = ' <format formatString="000,00" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var format = new Format(new XMLObject(XMLDoc.docNode));
    erg = format.resolve(2.1);
    assertEquals("002,10", erg);
};

FormatTest.prototype.test9 = function() {
    var rules = ' <format formatString="0.000,00" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var format = new Format(new XMLObject(XMLDoc.docNode));
    erg = format.resolve(2257.1);
    assertEquals("2.257,10", erg);
};
