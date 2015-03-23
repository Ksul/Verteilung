SearchItemTest = TestCase("SearchItemTest");



SearchItemTest.prototype.setUp = function() {
    REC.init();
    REC.content = " Dies ist ein Test!Datum: 01.05.1965\r\n" +
    "Wert:\r\n"+
    " \r\n" +
    "21,65\r\n" +
    " \r\n" +
    "Datum\r\n" +
    "Datum: März 15 ID-Value  21\t22 Euro\t23\r\n" +
    "   \r\n" +
    "06.04.09\r\n" +
    "   \r\n" +
    "   \r\n" +
    "   \r\n" +
    "Nachtrag zum\r\n" +
    "22 März 2012   \r\n" +
    "Gesamt in EUR \r\n" +
    "950,56 \r\n" +
    "    \r\n" +
    "+21,49 \r\n" +
    "Wert 123,5\r\n"  +
    "Gültig     10.März 2012     \r\n" +
    "24.12.2010 \r\n" +
    "KUSA Nr. 43124431\r\n" +
    "7. Januar 2008  \r\n" +
    "Rechnungsdatum23.08.2011\r\n" +
    "In den nächsten Tagen buchen wir 349,10 EUR von Ihrem Konto 123\r\n" +
    "Datum 21. März 2009 \r\n" +
    "Rechnungsbetrag 'ue 189.13 € \r\n" +
    "270 646 2793 \r\n"+
    "959 622 2280 \r\n"+
    "560 525 3966 \r\n" +
    "4.300,01 H \r\n"+
    "300 H \r\n"+
    "Der Verbrauch ist hoch.\r\n" +
    "Betrag dankend erhalten 302,26 €\r\n" +
    "Unsere Lieferungen";
    REC.content = REC.content.replace(/\r\n/g,'\n');
};

/*SearchItemTest.prototype.testDateFormat = function() {
    var rec = new Recognition();
    var date = new Date();
    date.setFullYear(2014, 4, 22);
    var dateString = rec.dateFormat(date, "dd.MM.YYYY");
    assertEquals("Datumstring ist nicht gleich!", "22.05.2014", dateString);
};*/

SearchItemTest.prototype.testResolveSearchItemWithFix = function() {
    var rules = '<searchItem name="Titel" fix="Test für Titel" target="cm:title" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals("Test für Titel", searchItem.resolve());
};

SearchItemTest.prototype.testResolveSearchItemWithEval = function() {
    var rules = '<searchItem name="Datum" eval="new Date(2012,01,01)"  objectTyp="date" target="my:documentDate" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(new Date(2012, 1, 1), searchItem.resolve());
};

SearchItemTest.prototype.testResolveSearchItemWithEvalAndFormat = function() {
    var rules = '<searchItem name="Datum" eval="new Date(2012,01,01)" objectTyp="date" target="my:documentDate" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = '<searchItem name="id" value="Datum" objectTyp="date"> <format formatString="YYYY" /> </searchItem>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals("2012", searchItem.resolve());
};

SearchItemTest.prototype.testResolveSearchItemWithReadOverReturn = function() {
    var rules = '<searchItem name="Test 1" text="Datum" word="1" readOverReturn="true" objectTyp="date" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(new Date(1965, 4, 1), searchItem.resolve());
    assertEquals(0, REC.positions[0].startRow);
    assertEquals(0, REC.positions[0].endRow);
    assertEquals(26, REC.positions[0].startColumn);
    assertEquals(36, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithWordAndDate = function() {
    var rules = '<searchItem name="Test 3" text="ID-Value" word="1,2" direction="left" objectTyp="date" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(new Date(2015, 2, 1), searchItem.resolve());
    assertEquals(6, REC.positions[0].startRow);
    assertEquals(6, REC.positions[0].endRow);
    assertEquals(7, REC.positions[0].startColumn);
    assertEquals(14, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithWordAndFloat = function() {
    var rules = '<searchItem name="Test 4" text="Wert" word="1" readOverReturn="true" objectTyp="float" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(21.65, searchItem.resolve());
    assertEquals(3, REC.positions[0].startRow);
    assertEquals(3, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(5, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithDelimitterAndFloat = function() {
    var rules = '<searchItem name="Test 5" text="Datum" readOverReturn="true" direction="left" objectTyp="float">' +
                '<delimitter typ="start" count="-3" text="&#0010;" />' +
                '<delimitter typ="end" count="1" text="&#0010;" />' +
                '</searchItem>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(21.65, searchItem.resolve());
    assertEquals(3, REC.positions[0].startRow);
    assertEquals(3, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(5, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithDelimitterAndInt = function() {
    var rules = '<searchItem name="Test 6" text="ID-Value" objectTyp="int">' +
        '<delimitter typ="start" count="2" text="&#0032;" />' +
        '<delimitter typ="end" count="1 "text="&#0009;" />' +
        '</searchItem>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(21, searchItem.resolve());
    assertEquals(6, REC.positions[0].startRow);
    assertEquals(6, REC.positions[0].endRow);
    assertEquals(25, REC.positions[0].startColumn);
    assertEquals(27, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithDelimitterAndDate = function() {
    var rules = '<searchItem name="Test 7" text="Nachtrag" objectTyp="date" >' +
        '<delimitter typ="start" count="1" text="&#0010;" />' +
        '</searchItem>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(new Date(2012, 2, 22), searchItem.resolve());
    assertEquals(13, REC.positions[0].startRow);
    assertEquals(13, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(12, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithFloat = function() {
    var rules = '<searchItem name="Test 8" text="Wert" objectTyp="float" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(new Number(123.5), searchItem.resolve());
    assertEquals(18, REC.positions[0].startRow);
    assertEquals(18, REC.positions[0].endRow);
    assertEquals(5, REC.positions[0].startColumn);
    assertEquals(10, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithDate = function() {
    var rules = '<searchItem name="Test 9" text="Gültig" objectTyp="date" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(new Date(2012, 2, 10), searchItem.resolve());
    assertEquals(19, REC.positions[0].startRow);
    assertEquals(19, REC.positions[0].endRow);
    assertEquals(11, REC.positions[0].startColumn);
    assertEquals(23, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithKindAndAmount = function() {
    var rules = '<searchItem name="Test 10" kind="amount,1" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(new Number(22), searchItem.resolve());
    assertEquals(6, REC.positions[0].startRow);
    assertEquals(6, REC.positions[0].endRow);
    assertEquals(28, REC.positions[0].startColumn);
    assertEquals(30, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithKindAndDate = function() {
    var rules = '<searchItem name="Test 11" kind="date,4" direction="left"/>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(new Date(2010, 11, 24), searchItem.resolve());
    assertEquals(20, REC.positions[0].startRow);
    assertEquals(20, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(10, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithCheck = function() {
    var rules = '<searchItem name="Test 12" text="KUSA" objectTyp="date">' +
            '<delimitter typ="start" count="1" text="&#0010;" />' +
            '<check lowerValue="01/01/2005" upperValue="01/01/2020" />' +
            '</searchItem>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(new Date(2008, 0, 7), searchItem.resolve());
    assertEquals(22, REC.positions[0].startRow);
    assertEquals(22, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(14, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItem16 = function() {
    var rules = '<searchItem name="Test 13" text="buchen" word="2" objectTyp="float" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(349.10, searchItem.resolve());
    assertEquals(24, REC.positions[0].startRow);
    assertEquals(24, REC.positions[0].endRow);
    assertEquals(33, REC.positions[0].startColumn);
    assertEquals(39, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithDirectionLeft = function() {
    var rules = '<searchItem name="Test 14" text="buchen" word="2" direction="left" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals('nächsten', searchItem.resolve());
    assertEquals(24, REC.positions[0].startRow);
    assertEquals(24, REC.positions[0].endRow);
    assertEquals(7, REC.positions[0].startColumn);
    assertEquals(15, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItem18 = function() {
    var rules = '<searchItem name="Test 15" text="Konto" required="false" objectTyp="int" word="1">' +
                '<check lowerValue="123" />' +
                ' </searchItem>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(123, searchItem.resolve());
    assertEquals(24, REC.positions[0].startRow);
    assertEquals(24, REC.positions[0].endRow);
    assertEquals(60, REC.positions[0].startColumn);
    assertEquals(63, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithDateAndWithoutSpace = function() {
    var rules = '<searchItem name="Test 16" text="Rechnungsdatum" objectTyp="date" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(new Date(2011, 7, 23), searchItem.resolve());
    assertEquals(23, REC.positions[0].startRow);
    assertEquals(23, REC.positions[0].endRow);
    assertEquals(14, REC.positions[0].startColumn);
    assertEquals(24, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItem20 = function() {
    var rules = '<searchItem name="Test 17" text="Gesamt in EUR" objectTyp="float" word="2" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(21.49, searchItem.resolve());
    assertEquals(17, REC.positions[0].startRow);
    assertEquals(17, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(6, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItem21 = function() {
    var rules = '<searchItem name="Test 18" kind="date,1" direction="left" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(new Date(2009, 02, 21), searchItem.resolve());
    assertEquals(25, REC.positions[0].startRow);
    assertEquals(25, REC.positions[0].endRow);
    assertEquals(6, REC.positions[0].startColumn);
    assertEquals(19, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItem22 = function() {
    var rules = '<searchItem name="txt" text="Rechnungsbetrag" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = '<searchItem name="Test 19" value="txt" kind="amount" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(189.13, searchItem.resolve());
    assertEquals(26, REC.positions[0].startRow);
    assertEquals(26, REC.positions[0].endRow);
    assertEquals(20, REC.positions[0].startColumn);
    assertEquals(26, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItem23 = function() {
    var rules = '<searchItem name="txt" text="Rechnungsbetrag" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = '<searchItem name="Test 19" value="txt" kind="amount" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = '<searchItem name="Test 20" eval="{Test 19} * 2"/>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(378.26, searchItem.resolve());
};

SearchItemTest.prototype.testResolveSearchItem24 = function() {
    var rules = '<searchItem name="bet1" text="Wertentwicklung in EUR" word="1" objectTyp="float" required="false" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = '<searchItem name="bet2" text="Gesamt in EUR" word="2" objectTyp="float" required="false" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = '<searchItem name="Test 21" eval="{bet1}||{bet2}" objectTyp="float"/>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(21.49, searchItem.resolve());
};

SearchItemTest.prototype.testResolveSearchItem25 = function() {
    var rules = '<searchItem name="Test 22" text="XYZ|Gesamt" word="2" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals("EUR", searchItem.resolve());
    assertEquals(14, REC.positions[0].startRow);
    assertEquals(14, REC.positions[0].endRow);
    assertEquals(10, REC.positions[0].startColumn);
    assertEquals(13, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItem26 = function() {
    var rules = '<searchItem name="Test 23" text="960|959" included="true" word="0,3" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals('959 622 2280', searchItem.resolve());
    assertEquals(28, REC.positions[0].startRow);
    assertEquals(28, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(12, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItem27 = function() {
    var rules = '<searchItem name="Test 24" text="3966" included="true" word="0,3" direction="left" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals('560 525 3966', searchItem.resolve());
    assertEquals(29, REC.positions[0].startRow);
    assertEquals(29, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(12, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItem28 = function() {
    var rules = '<searchItem name="Test 25" text="H " objectTyp="float" readOverReturn="true" word="1" direction="left" backwards="true" >' +
                '<check lowerValue="400" upperValue="10000" />' +
                '</searchItem>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(4300.01, searchItem.resolve());
    assertEquals(30, REC.positions[0].startRow);
    assertEquals(30, REC.positions[0].endRow);
    assertEquals(0, REC.positions[0].startColumn);
    assertEquals(8, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithPositionAndInt = function() {
    var rules = ' <searchItem name="betrag" text="erhalten" readOverReturn="true" objectTyp="int" target="my:amount" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(302, searchItem.resolve());
    assertEquals(33, REC.positions[0].startRow);
    assertEquals(33, REC.positions[0].endRow);
    assertEquals(24, REC.positions[0].startColumn);
    assertEquals(27, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testResolveSearchItemWithPositionAndFloat = function() {
    var rules = ' <searchItem name="betrag" text="erhalten" readOverReturn="true" objectTyp="float" target="my:amount" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    assertEquals(302.26, searchItem.resolve());
    assertEquals(33, REC.positions[0].startRow);
    assertEquals(33, REC.positions[0].endRow);
    assertEquals(24, REC.positions[0].startColumn);
    assertEquals(30, REC.positions[0].endColumn);
};

SearchItemTest.prototype.testFindForWord1 = function() {
    var text = "Dies ist ein Test";
    var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
    searchItem = new SearchItem({});
    searchItem.erg.addResult(result);
    searchItem.findForWords( [1], false);
    assertEquals("ist", searchItem.erg.getResult().text);
    assertEquals(5, searchItem.erg.getResult().getStart());
    assertEquals(8, searchItem.erg.getResult().getEnd());
};

SearchItemTest.prototype.testFindForWord2 = function() {
    var text = "Dies ist ein Test";
    var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
    searchItem = new SearchItem({});
    searchItem.erg.addResult(result);
    searchItem.findForWords([1,2], false);
    assertEquals("ist ein", searchItem.erg.getResult().text);
    assertEquals(5, searchItem.erg.getResult().getStart());
    assertEquals(12, searchItem.erg.getResult().getEnd());
};

SearchItemTest.prototype.testFindForWord3 = function() {
    var text = "Dies ist ein Test";
    var erg = new SearchResultContainer();
    var result = new SearchResult(text, text, "Test", 0, text.length, "String", "asd");
    searchItem = new SearchItem({});
    searchItem.erg.addResult(result);
    searchItem.findForWords([2,2], true);
    assertEquals("Dies ist", searchItem.erg.getResult().text);
    assertEquals(0, searchItem.erg.getResult().getStart());
    assertEquals(8, searchItem.erg.getResult().getEnd());
};

SearchItemTest.prototype.testFindForSpecialType1 = function() {
    var text = "Dies 01.01.2010 ist hoffentlich ein Datum";
    searchItem = new SearchItem({});
    erg =  searchItem.findSpecialType(text, ["date"], false, null);
    assertEquals("01.01.2010", erg[0].text);
    assertEquals(5, erg[0].getStart());
    assertEquals(15, erg[0].getEnd());
    assertEquals(new Date(2010,0,1), erg[0].val);
};

SearchItemTest.prototype.testFindForSpecialType2 = function() {
    var text = "Dies 125,78 €ist hoffentlich ein Betrag";
    searchItem = new SearchItem({});
    erg =  searchItem.findSpecialType(text, ["amount"], false, null);
    assertEquals("125,78 €", erg[0].text);
    assertEquals(5, erg[0].getStart());
    assertEquals(11, erg[0].getEnd());
    assertEquals(125.78, erg[0].val);
};

SearchItemTest.prototype.testFindForSpecialType3 = function() {
    var text = "Dies 125,78 €ist hoffentlich ein Betrag";
    searchItem = new SearchItem({});
    erg =  searchItem.findSpecialType(text, ["float"], false, null);
    assertEquals("125,78", erg[0].text);
    assertEquals(5, erg[0].getStart());
    assertEquals(11, erg[0].getEnd());
    assertEquals(125.78, erg[0].val);
};

SearchItemTest.prototype.testFindForSpecialType4 = function() {
    var text = "Dies 01. März 2010 ist hoffentlich ein Datum";
    searchItem = new SearchItem({});
    erg =  searchItem.findSpecialType(text, ["date"], false, null);
    assertEquals("01. März 2010", erg[0].text);
    assertEquals(5, erg[0].getStart());
    assertEquals(18, erg[0].getEnd());
    assertEquals(new Date(2010,2,1), erg[0].val);
};

SearchItemTest.prototype.testFindForSpecialType5 = function() {
    var text = "Dies März 2010 ist hoffentlich ein Datum";
    searchItem = new SearchItem({});
    erg =  searchItem.findSpecialType(text, ["date"], false, null);
    assertEquals("März 2010", erg[0].text);
    assertEquals(5, erg[0].getStart());
    assertEquals(14, erg[0].getEnd());
    assertEquals(new Date(2010,2,1), erg[0].val);
};



