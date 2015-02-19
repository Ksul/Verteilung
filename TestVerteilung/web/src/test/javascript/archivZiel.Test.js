/**
 * Created by m500288 on 19.02.15.
 */

ArchivZielTest = TestCase("ArchivZielTest");

ArchivZielTest.prototype.setUp = function() {
    REC.init();
};

ArchivZielTest.prototype.test1 = function() {
    var rules = ' <archivZiel type="my:archivContent" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var archivZiel = new ArchivZiel(new XMLObject(XMLDoc.docNode));
    erg = archivZiel.resolve();
    assertTrue("falsches Archiv Ziel", REC.currentDocument.isSubType("my:archivContent"));
};

ArchivZielTest.prototype.test2 = function() {
    var rules = ' <archivZiel aspect="my:idable" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var archivZiel = new ArchivZiel(new XMLObject(XMLDoc.docNode));
    erg = archivZiel.resolve();
    assertTrue("falscher Aspect", REC.currentDocument.hasAspect("my:idable"));
};

ArchivZielTest.prototype.test3 = function() {
    var rules = ' <archivZiel type="my:archivContent" aspect="my:idable" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var archivZiel = new ArchivZiel(new XMLObject(XMLDoc.docNode));
    erg = archivZiel.resolve();
    assertTrue("falscher Aspect", REC.currentDocument.hasAspect("my:idable"));
    assertTrue("falsches Archiv Ziel", REC.currentDocument.isSubType("my:archivContent"));
};

