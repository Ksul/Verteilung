/**
 * Created by m500288 on 20.02.15.
 */

CategoryTest = TestCase("CategoryTest");

CategoryTest.prototype.setUp = function() {
    REC.init();
};

CategoryTest.prototype.test1 = function() {
    var rules = '<category name="Steuern/Einkommen" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var category = new Category(new XMLObject(XMLDoc.docNode));
    category.resolve(REC.currentDocument);

};