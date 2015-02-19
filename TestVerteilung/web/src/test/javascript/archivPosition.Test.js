/**
 * Created by m500288 on 18.02.15.
 */

ArchivPositionTest = TestCase("ArchivPositionTest");

ArchivPositionTest.prototype.setUp = function() {
    REC.init();
};


ArchivPositionTest.prototype.test1 = function() {
    var rules = ' <archivPosition folder="Dokumente/Auto/KFZ Steuern" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode));
    erg = archivPosition.resolve();
    assertEquals("Dokumente/Auto/KFZ Steuern", erg);
};

ArchivPositionTest.prototype.test2 = function() {
    var rules = ' <archivPosition folder="Dokumente/Auto/KFZ :Steuern" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode));
    erg = archivPosition.resolve();
    assertEquals(null, erg);
    assertEquals("Ung\ufffdtige Zeichen f\ufffdr Foldernamen!\nDokumente/Auto/KFZ :Steuern\nPosition 19:\n:\n", REC.errors[0]);
};

ArchivPositionTest.prototype.test3 = function() {
    var rules = '<searchItem name="tmp2" fix="Test"  />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
    REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
    rules = ' <archivPosition folder="Dokumente/Rechnungen/Sonstige Rechnungen/{tmp2}">';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode));
    erg = archivPosition.resolve();
    assertEquals("Dokumente/Rechnungen/Sonstige Rechnungen/Test", erg);
};

ArchivPositionTest.prototype.test4 = function() {
    var rules = ' <archivPosition folder="Dokumente/Rechnungen/Sonstige Rechnungen/{tmp2}">'
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode));
    erg = archivPosition.resolve();
    assertEquals(null, erg);
    assertEquals("Variabel konnte nicht im Foldernamen ersetzt werden!\n", REC.errors[1]);
};
