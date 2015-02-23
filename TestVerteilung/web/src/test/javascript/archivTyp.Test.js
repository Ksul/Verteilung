/**
 * Created by m500288 on 23.02.15.
 */


ArchivTypTest = TestCase("ArchivTypTest");

ArchivTypTest.prototype.setUp = function() {

};

ArchivTypTest.prototype.test1 = function() {
    var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU"> />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
}
