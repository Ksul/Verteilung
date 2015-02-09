RecognitionTest = TestCase("RecognitionTest");

REC.content = " Dies ist ein Test!Datum: 01.05.1965\n" +
"Wert:\n"+
"\n" +
"21,65\n" +
"\n" +
"Datum\n" +
"Datum: März 15 ID-Value  21	22 Euro	23\n" +
"\n" +
"06.04.09\n" +
" \n" +
"\n" +
"\n" +
"Nachtrag zum\n" +
"22 März 2012   \n" +
"Gesamt in EUR \n" +
"950,56 \n" +
"    \n" +
"+21,49 \n" +
"Wert 123,5\n"  +
"Gültig     10.März 2012  \n" +
" 24.12.2010 \n" +
"KUSA Nr. 43124431 \n" +
"7. Januar 2008 \n" +
"Rechnungsdatum23.08.2011 \n" +
"In den nächsten Tagen buchen wir 349,10 EUR von Ihrem Konto 123\n" +
"Datum 21. März 2009 \n" +
"Rechnungsbetrag 'ue 189.13 € \n" +
"270 646 2793 \n"+
"959 622 2280 \n"+
"560 525 3966 \n" +
"4.300,01 H \n"+
"300 H \n"+
"Der Verbrauch ist hoch."

RecognitionTest.prototype.testDateFormat = function() {
    var rec = new Recognition();
    var date = new Date();
    date.setFullYear(2014, 4, 22);
    var dateString = rec.dateFormat(date, "dd.MM.YYYY");
    assertEquals("Datumstring ist nicht gleich!", "22.05.2014", dateString);
};

RecognitionTest.prototype.testResolveSearchItem1 = function() {
    var rules = '<searchItem name="Titel" fix="Test f�r Titel" target="cm:title" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals("Test f�r Titel", searchItem.resolve());
}

RecognitionTest.prototype.testResolveSearchItem2 = function() {
    var rules = '<searchItem name="Datum" eval="new Date(2012,01,01)"  objectTyp="date" target="my:documentDate" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Date(2012,01,01), searchItem.resolve());
}

RecognitionTest.prototype.testResolveSearchItem3 = function() {
    var rules = '<searchItem name="Datum" eval="new Date(2012,01,01)" objectTyp="date" target="my:documentDate" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    var tmp = REC.currentSearchItems.concat(searchItem);
    REC.currentSearchItems = tmp;
    rules = '<searchItem name="id" value="Datum" objectTyp="date"> <format formatString="YYYY" /> </searchItem>';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals("2012", searchItem.resolve());
}

RecognitionTest.prototype.testResolveSearchItem4 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 1" text="Datum" word="1" readOverReturn="true" objectTyp="date" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Date(1965, 4, 1), searchItem.resolve());
    assertEquals(0, REC.positions[0].startRow);
    assertEquals(0, REC.positions[0].endRow);
    assertEquals(26, REC.positions[0].startColumn);
    assertEquals(36, REC.positions[0].endColumn);
}

RecognitionTest.prototype.testResolveSearchItem6 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 3" text="ID-Value" word="1,2" direction="left" objectTyp="date" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Date(2015, 2, 1), searchItem.resolve());
    assertEquals(6, REC.positions[0].startRow);
    assertEquals(6, REC.positions[0].endRow);
    assertEquals(7, REC.positions[0].startColumn);
    assertEquals(14, REC.positions[0].endColumn);
}

RecognitionTest.prototype.testResolveSearchItem7 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 4" text="Wert" word="1" readOverReturn="true" objectTyp="float" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Number(21.65), searchItem.resolve());
    assertEquals(3, REC.positions[0].startRow);
    assertEquals(3, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(5, REC.positions[0].endColumn);
}

RecognitionTest.prototype.testResolveSearchItem8 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 5" text="Datum" readOverReturn="true" direction="left" objectTyp="float"> <delimitter typ="start" count="-3" text="\n" /><delimitter typ="end" count="1" text="\n" /></searchItem>';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Number(21.65), searchItem.resolve());
    assertEquals(3, REC.positions[0].startRow);
    assertEquals(3, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(5, REC.positions[0].endColumn);
}