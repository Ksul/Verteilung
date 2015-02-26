/**
 * Created by m500288 on 23.02.15.
 */


ArchivTypTest = TestCase("ArchivTypTest");

ArchivTypTest.prototype.setUp = function() {
    REC.init();
    companyhome.init();
    REC.currentDocument = companyhome.createNode('WebScriptTest', "my:archivContent");
};

ArchivTypTest.prototype.test1 = function() {
    REC.content ="ZAUBERFRAU";
    var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">' +
                    ' <archivZiel type="my:archivContent" /> ' +
                    ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
                    ' <archivZiel type="my:archivFolder" /> ' +
                    ' </archivPosition>' +
                    ' <searchItem name="tmp" fix="2015" />' +
                    ' </archivTyp>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    assertNotNull(companyhome.childByNamePath("/WebScriptTest"));
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
    assertNull(companyhome.childByNamePath("/WebScriptTest"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    assertTrue(doc.isSubType("my:archivContent"));
    assertTrue(doc.parent[0].isSubType("my:archivFolder"));
};
