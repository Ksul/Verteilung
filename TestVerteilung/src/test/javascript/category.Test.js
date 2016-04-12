/**
 * Created by m500288 on 20.02.15.
 */

CategoryTest = TestCase("CategoryTest");

CategoryTest.prototype.setUp = function() {
    REC.init();
    companyhome.init();
    classification.init();
};

CategoryTest.prototype.test1 = function() {
    var rules = '<category name="Steuern/Einkommen" debugLevel="trace" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var category = new Category(new XMLObject(XMLDoc.docNode));
    category.resolve(REC.currentDocument);
    assertTrue( classification.rootCategories.contains((new BasicObject("Steuern"))));
    var cat = classification.rootCategories.get(new BasicObject("Steuern"));
    assertTrue(cat.subCategories.contains(new BasicObject("Einkommen")));
};

CategoryTest.prototype.test2 = function() {
    classification.createRootCategory("cm:generalclassifiable", "Steuern");
    var rules = '<category name="Steuern/Einkommen" debugLevel="trace" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var category = new Category(new XMLObject(XMLDoc.docNode));
    category.resolve(REC.currentDocument);
    assertTrue( classification.rootCategories.contains((new BasicObject("Steuern"))));
    var cat = classification.rootCategories.get(new BasicObject("Steuern"));
    assertTrue(cat.subCategories.contains(new BasicObject("Einkommen")));
};