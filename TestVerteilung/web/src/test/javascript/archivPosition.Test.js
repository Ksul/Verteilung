/**
 * Created by m500288 on 18.02.15.
 */

ArchivPositionTest = TestCase("ArchivPositionTest");

ArchivPositionTest.prototype.setUp = function() {
    REC.init();
    companyhome.init();
};


ArchivPositionTest.prototype.test1 = function() {
    var rules = ' <archivPosition folder="Dokumente/Auto/KFZ Steuern" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode));
    erg = archivPosition.resolve();
    assertEquals("/Archiv/Dokumente/Auto/KFZ Steuern", erg.displayPath.split("/").slice(2).join("/") + "/" + erg.name);
};

ArchivPositionTest.prototype.test2 = function() {
    var rules = ' <archivPosition folder="Dokumente/Auto/KFZ :Steuern" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode));
    erg = archivPosition.resolve();
    assertEquals(null, erg);
    assertEquals("Ung\ufffdtige Zeichen f\ufffdr Foldernamen!\n/Archiv/Dokumente/Auto/KFZ :Steuern\nPosition 27:\n:\n", REC.errors[0]);
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
    assertEquals("/Archiv/Dokumente/Rechnungen/Sonstige Rechnungen/Test", erg.displayPath.split("/").slice(2).join("/") + "/" + erg.name);
};

ArchivPositionTest.prototype.test4 = function() {
    var rules = ' <archivPosition folder="Dokumente/Rechnungen/Sonstige Rechnungen/{tmp2}">';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode));
    erg = archivPosition.resolve();
    assertEquals(null, erg);
    assertEquals("Variabel konnte nicht im Foldernamen ersetzt werden!\n", REC.errors[1]);
};

ArchivPositionTest.prototype.testResolveFolder1 = function() {
    var archivPosition = new ArchivPosition({});
    var newFolder = archivPosition.resolveFolder("/aa/bb/cc");
    assertNotNull(newFolder);
    assertNotNull(companyhome.childByNamePath("aa"));
    assertNotNull(companyhome.childByNamePath("aa/bb"));
    assertNotNull(companyhome.childByNamePath("aa/bb/cc"));
};

ArchivPositionTest.prototype.testResolveFolder2 = function() {
    companyhome.createFolder("aa");
    var archivPosition = new ArchivPosition({});
    var newFolder = archivPosition.resolveFolder("/aa/bb/cc");
    assertNotNull(newFolder);
    assertNotNull(companyhome.childByNamePath("aa"));
    assertNotNull(companyhome.childByNamePath("aa/bb"));
    assertNotNull(companyhome.childByNamePath("aa/bb/cc"));
};
