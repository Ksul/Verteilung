/**
 * Created by m500288 on 23.02.15.
 */

TagsTest = TestCase("TagsTest");

TagsTest.prototype.setUp = function() {
   REC.init();
};

TagsTest.prototype.test1 = function() {
    var rules = '<tags name="Rückrechnung"/>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var tags = new Tags(new XMLObject(XMLDoc.docNode));
    tags.resolve(REC.currentDocument);
    assertTrue(REC.currentDocument.hasTag("Rückrechnung"));
}
