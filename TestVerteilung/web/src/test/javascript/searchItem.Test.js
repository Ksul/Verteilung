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
"Der Verbrauch ist hoch.";

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
};

RecognitionTest.prototype.testResolveSearchItem2 = function() {
    var rules = '<searchItem name="Datum" eval="new Date(2012,01,01)"  objectTyp="date" target="my:documentDate" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Date(2012,01,01), searchItem.resolve());
};

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
};

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
};

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
};

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
};

RecognitionTest.prototype.testResolveSearchItem8 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 5" text="Datum" readOverReturn="true" direction="left" objectTyp="float">' +
                '<delimitter typ="start" count="-3" text="\n" />' +
                '<delimitter typ="end" count="1" text="\n" />' +
                '</searchItem>';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Number(21.65), searchItem.resolve());
    assertEquals(3, REC.positions[0].startRow);
    assertEquals(3, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(5, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem9 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 6" text="ID-Value" objectTyp="int">' +
        '<delimitter typ="start" count="2" text="\u0020" />' +
        '<delimitter typ="end" count="1" text="\u0009" />' +
        '</searchItem>';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Number(21), searchItem.resolve());
    assertEquals(3, REC.positions[0].startRow);
    assertEquals(3, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(2, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem10 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 7" text="Nachtrag" objectTyp="date" >' +
        '<delimitter typ="start" count="1" text="\n" />'
        '</searchItem>';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Date(2012, 2, 22), searchItem.resolve());
    assertEquals(13, REC.positions[0].startRow);
    assertEquals(13, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(12, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem11 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 8" text="Wert" objectTyp="float" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Number(123.5), searchItem.resolve());
    assertEquals(18, REC.positions[0].startRow);
    assertEquals(18, REC.positions[0].endRow);
    assertEquals(5, REC.positions[0].startColumn);
    assertEquals(10, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem12 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 9" text="Gültig" objectTyp="date" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Date(2012, 2, 10), searchItem.resolve());
    assertEquals(19, REC.positions[0].startRow);
    assertEquals(19, REC.positions[0].endRow);
    assertEquals(11, REC.positions[0].startColumn);
    assertEquals(23, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem13 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 10" kind="amount,1" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Number(22), searchItem.resolve());
    assertEquals(6, REC.positions[0].startRow);
    assertEquals(6, REC.positions[0].endRow);
    assertEquals(28, REC.positions[0].startColumn);
    assertEquals(30, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem14 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 11" kind="date,4" direction="left"/>';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Date(2010, 11, 24), searchItem.resolve());
    assertEquals(20, REC.positions[0].startRow);
    assertEquals(20, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(10, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem15 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 12" text="KUSA" objectTyp="date">' +
            '<delimitter typ="start" count="1" text="\n" />' +
            '<check lowerValue="01/01/2005" upperValue="01/01/2020" />' +
            '</searchItem>';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Date(2008, 0, 7), searchItem.resolve());
    assertEquals(22, REC.positions[0].startRow);
    assertEquals(22, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(14, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem16 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 13" text="buchen" word="2" objectTyp="float" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(349.10, searchItem.resolve());
    assertEquals(24, REC.positions[0].startRow);
    assertEquals(24, REC.positions[0].endRow);
    assertEquals(33, REC.positions[0].startColumn);
    assertEquals(39, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem17 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 14" text="buchen" word="2" direction="left" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals('nächsten', searchItem.resolve());
    assertEquals(24, REC.positions[0].startRow);
    assertEquals(24, REC.positions[0].endRow);
    assertEquals(7, REC.positions[0].startColumn);
    assertEquals(15, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem18 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 15" text="Konto" required="false" objectTyp="int" word="1">' +
                '<check lowerValue="124" />' +
                ' </searchItem>';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(123, searchItem.resolve());
    assertEquals(24, REC.positions[0].startRow);
    assertEquals(24, REC.positions[0].endRow);
    assertEquals(60, REC.positions[0].startColumn);
    assertEquals(63, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem19 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 16" text="Rechnungsdatum" objectTyp="date" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Date(2011, 7, 23), searchItem.resolve());
    assertEquals(23, REC.positions[0].startRow);
    assertEquals(23, REC.positions[0].endRow);
    assertEquals(14, REC.positions[0].startColumn);
    assertEquals(24, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem20 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 17" text="Gesamt in EUR" objectTyp="float" word="2" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(21.49, searchItem.resolve());
    assertEquals(17, REC.positions[0].startRow);
    assertEquals(17, REC.positions[0].endRow);
    assertEquals(1, REC.positions[0].startColumn);
    assertEquals(6, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem21 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 18" kind="date,1" direction="left" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(new Date(2009, 02, 21), searchItem.resolve());
    assertEquals(25, REC.positions[0].startRow);
    assertEquals(25, REC.positions[0].endRow);
    assertEquals(6, REC.positions[0].startColumn);
    assertEquals(19, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem22 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="txt" text="Rechnungsbetrag" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = '<searchItem name="Test 19" value="txt" kind="amount" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(189.13, searchItem.resolve());
    assertEquals(26, REC.positions[0].startRow);
    assertEquals(26, REC.positions[0].endRow);
    assertEquals(20, REC.positions[0].startColumn);
    assertEquals(28, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem23 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="txt" text="Rechnungsbetrag" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = '<searchItem name="Test 19" value="txt" kind="amount" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = '<searchItem name="Test 20" eval="{Test 19} * 2"/>';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(378.26, searchItem.resolve());
};

RecognitionTest.prototype.testResolveSearchItem24 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="bet1" text="Wertentwicklung in EUR" word="1" objectTyp="float" required="false" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = '<searchItem name="bet2" text="Gesamt in EUR" word="2" objectTyp="float" required="false" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = '<searchItem name="Test 21" eval="{bet1}||{bet2}" objectTyp="float"/>';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(21.49, searchItem.resolve());
    assertEquals(17, REC.positions[0].startRow);
    assertEquals(17, REC.positions[0].endRow);
    assertEquals(1, REC.positions[0].startColumn);
    assertEquals(6, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem25 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 22" text="XYZ|Gesamt" word="2" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals("EUR", searchItem.resolve());
    assertEquals(14, REC.positions[0].startRow);
    assertEquals(14, REC.positions[0].endRow);
    assertEquals(10, REC.positions[0].startColumn);
    assertEquals(13, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem26 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 23" text="960|959" included="true" word="0,3" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals('959 622 2280', searchItem.resolve());
    assertEquals(28, REC.positions[0].startRow);
    assertEquals(28, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(12, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem27 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 24" text="3966" included="true" word="0,3" direction="left" />';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals('560 525 3966', searchItem.resolve());
    assertEquals(29, REC.positions[0].startRow);
    assertEquals(29, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(12, REC.positions[0].endColumn);
};

RecognitionTest.prototype.testResolveSearchItem28 = function() {
    REC.positions = new PositionContainer();
    var rules = '<searchItem name="Test 25" text="H " objectTyp="float" readOverReturn="true" word="1" direction="left" backwards="true" >' +
                '<check lowerValue="400" upperValue="10000" />' +
                '</searchItem>';
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals(4300.01, searchItem.resolve());
    assertEquals(30, REC.positions[0].startRow);
    assertEquals(30, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(8, REC.positions[0].endColumn);
};

