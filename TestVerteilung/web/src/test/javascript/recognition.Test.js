RecognitionTest = TestCase("RecognitionTest");

RecognitionTest.prototype.testDateFormat = function() {
    var rec = new Recognition();
    var date = new Date();
    date.setFullYear(2014, 4, 22);
    var dateString = rec.dateFormat(date, "dd.MM.YYYY");
    assertEquals("Datumstring ist nicht gleich!", "22.05.2014", dateString);
};

RecognitionTest.prototype.testResolveSearchItem = function() {
    var rules = '<searchItem name="Titel" fix="Test für Titel" target="cm:title" />';
    var REC = new Recognition();
    REC.positions = new PositionContainer();
    XMLDOC.loadXML(rules);
    XMLDOC.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDOC.docNode));
    assertEquals("Test für Titel", searchItem.resolve());

}