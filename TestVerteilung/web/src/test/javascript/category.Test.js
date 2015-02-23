/**
 * Created by m500288 on 20.02.15.
 */

CategoryTest = TestCase("CategoryTest");

CategoryTest.prototype.setUp = function() {
    REC.init();
    classification.init();
};

CategoryTest.prototype.test1 = function() {
    var rules = '<category name="Steuern/Einkommen" debugLevel="trace" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var category = new Category(new XMLObject(XMLDoc.docNode));
    category.resolve(REC.currentDocument);
    assertTrue(REC.mess.search("Root Category created!") != -1);
    assertTrue(REC.mess.search("Category \\[Steuern] not found! Create Category") != -1);
    assertTrue(REC.mess.search("Category \\[Einkommen] not found! Create Category") != -1);
    assertTrue(REC.mess.search("Add Category \\[Steuern/Einkommen] to document") != -1);
};

CategoryTest.prototype.test2 = function() {
    classification.createRootCategory("cm:generalclassifiable", "Steuern");
    var rules = '<category name="Steuern/Einkommen" debugLevel="trace" />';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    var category = new Category(new XMLObject(XMLDoc.docNode));
    category.resolve(REC.currentDocument);
    assertTrue(REC.mess.search("Category \\[Steuern] found") != -1);
    assertTrue(REC.mess.search("Category \\[Einkommen] not found! Create Category") != -1);
    assertTrue(REC.mess.search("Add Category \\[Steuern/Einkommen] to document") != -1);
};