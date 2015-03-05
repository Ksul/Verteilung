/**
 * Created by m500288 on 23.02.15.
 */


ArchivTypTest = TestCase("ArchivTypTest");

ArchivTypTest.prototype.setUp = function() {
    REC.init();
    companyhome.init();
    REC.unknownBox = companyhome.createFolder("Unbekannt");
    REC.inbox = companyhome.createFolder("Inbox");
    REC.errorBox = companyhome.createFolder("Fehler");
    REC.duplicateBox = REC.errorBox.createFolder("Doppelte");
    REC.currentDocument = REC.inbox.createNode('WebScriptTest', "my:archivContent");
    REC.currentDocument.setProperty("cm:title", "Test Title");
    REC.currentDocument.setProperty("my:person", "Klaus");
    REC.currentDocument.properties.content.write(new Content("Test"));
    search.setFind(false);
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
    assertNotNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
    assertNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    assertTrue(doc.isSubType("my:archivContent"));
    assertTrue(doc.parent[0].isSubType("my:archivFolder"));
    assertNull(companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.test2 = function() {
    var folder = companyhome.createFolder("Dokumente");
    folder = folder.createFolder("Rechnungen");
    folder = folder.createFolder("Rechnungen Zauberfrau");
    folder = folder.createFolder("2015");
    folder.createNode("WebScriptTest", "my:archivContent");
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
    assertNotNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
    assertNull(companyhome.childByNamePath("/WebScriptTest"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    doc = companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest");
    assertNotNull(doc);
    assertTrue(doc.isSubType("my:archivContent"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.test3 = function() {
    var folder = companyhome.createFolder("Dokumente");
    folder = folder.createFolder("Rechnungen");
    folder = folder.createFolder("Rechnungen Zauberfrau");
    folder = folder.createFolder("2015");
    var node = folder.createNode("WebScriptTest", "my:archivContent");
    node.setProperty("cm:title", "Rechnung 1");
    search.setFind(true, node);
    REC.content ="ZAUBERFRAU";
    var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">' +
        ' <archivZiel type="my:archivContent" /> ' +
        ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
        ' <archivZiel type="my:archivFolder" /> ' +
        ' </archivPosition>' +
        ' <searchItem name="tmp" fix="2015" />' +
        ' <searchItem name="title" fix="Rechnung 1" />' +
        ' </archivTyp>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    assertNotNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
    assertNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    doc = companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest");
    assertNotNull(doc);
    assertTrue(doc.isSubType("my:archivContent"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.test4 = function() {
    var folder = companyhome.createFolder("Dokumente");
    folder = folder.createFolder("Rechnungen");
    folder = folder.createFolder("Rechnungen Zauberfrau");
    folder = folder.createFolder("2015");
    folder.createNode("WebScriptTest", "my:archivContent");
    REC.content ="ZAUBERFRAU";
    var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU" unique="nothing">' +
        ' <archivZiel type="my:archivContent" /> ' +
        ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
        ' <archivZiel type="my:archivFolder" /> ' +
        ' </archivPosition>' +
        ' <searchItem name="tmp" fix="2015" />' +
        ' </archivTyp>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    assertNotNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
    assertNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    assertNull(companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.test5 = function() {
    var folder = companyhome.createFolder("Dokumente");
    folder = folder.createFolder("Rechnungen");
    folder = folder.createFolder("Rechnungen Zauberfrau");
    folder = folder.createFolder("2015");
    folder.createNode("WebScriptTest", "my:archivContent");
    REC.content ="ZAUBERFRAU";
    var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU" unique="nothing"> ' +
        ' <archivZiel type="my:archivContent" /> ' +
        ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
        ' <archivZiel type="my:archivFolder" /> ' +
        ' </archivPosition>' +
        ' <searchItem name="tmp" fix="2015" />' +
        ' </archivTyp>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    assertNotNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
    assertNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    assertNull(companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.test6 = function() {
    var folder = companyhome.createFolder("Dokumente");
    folder = folder.createFolder("Rechnungen");
    folder = folder.createFolder("Rechnungen Zauberfrau");
    folder = folder.createFolder("2015");
    var node = folder.createNode("WebScriptTest", "my:archivContent");
    node.setProperty("my:person", "Till");
    REC.content ="ZAUBERFRAU";
    var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU" unique="overWrite">' +
        ' <archivZiel type="my:archivContent" /> ' +
        ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
        ' <archivZiel type="my:archivFolder" /> ' +
        ' </archivPosition>' +
        ' <searchItem name="tmp" fix="2015" />' +
        ' </archivTyp>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    assertNotNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
    assertNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    assertEquals("Klaus", doc.properties["my:person"]);
    assertNull(companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.test7 = function() {
    var folder = companyhome.createFolder("Dokumente");
    folder = folder.createFolder("Rechnungen");
    folder = folder.createFolder("Rechnungen Zauberfrau");
    folder = folder.createFolder("2015");
    var node = folder.createNode("Test", "my:archivContent");
    node.setProperty("cm:title", "Rechnung 1");
    search.setFind(true, node);
    REC.content ="ZAUBERFRAU";
    var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU" unique="overWrite">' +
        ' <archivZiel type="my:archivContent" /> ' +
        ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
        ' <archivZiel type="my:archivFolder" /> ' +
        ' </archivPosition>' +
        ' <searchItem name="tmp" fix="2015" />' +
        ' <searchItem name="title" fix="Rechnung 1" />' +
        ' </archivTyp>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    assertNotNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
    assertNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/Test"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    assertEquals("Klaus", doc.properties["my:person"]);
    assertNull(companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.test8 = function() {
    var folder = companyhome.createFolder("Dokumente");
    folder = folder.createFolder("Rechnungen");
    folder = folder.createFolder("Rechnungen Zauberfrau");
    folder = folder.createFolder("2015");
    var node = folder.createNode("WebScriptTest", "my:archivContent");
    node.properties.content.write(new Content("Hallo"));
    REC.content ="ZAUBERFRAU";
    var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU" unique="newVersion">' +
        ' <archivZiel type="my:archivContent" /> ' +
        ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
        ' <archivZiel type="my:archivFolder" /> ' +
        ' </archivPosition>' +
        ' <searchItem name="tmp" fix="2015" />' +
        ' </archivTyp>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    assertNotNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
    assertNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    assertTrue(doc.isVersioned());
    assertFalse(doc.hasAspect(new BasicObject("cm:workingcopy")))
    assertEquals("Test", doc.properties.content.content);
    var version = doc.getVersion(1);
    assertNotNull(version);
    assertEquals("Hallo", version.properties.content.content);
    version = doc.getVersion(2);
    assertNotNull(version);
    assertEquals("Test", version.properties.content.content);
    assertNull(companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.test9 = function() {
    var folder = companyhome.createFolder("Dokumente");
    folder = folder.createFolder("Rechnungen");
    folder = folder.createFolder("Rechnungen Zauberfrau");
    folder = folder.createFolder("2015");
    var node = folder.createNode("Test", "my:archivContent");
    node.properties.content.write(new Content("Hallo"));
    node.setProperty("cm:title", "Rechnung 1");
    search.setFind(true, node);
    REC.content ="ZAUBERFRAU";
    var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU" unique="newVersion">' +
        ' <archivZiel type="my:archivContent" /> ' +
        ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
        ' <archivZiel type="my:archivFolder" /> ' +
        ' </archivPosition>' +
        ' <searchItem name="tmp" fix="2015" />' +
        ' <searchItem name="title" fix="Rechnung 1" />' +
        ' </archivTyp>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    assertNotNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
    assertNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/Test");
    assertNotNull(doc);
    assertTrue(doc.isVersioned());
    assertFalse(doc.hasAspect(new BasicObject("cm:workingcopy")))
    assertEquals("Test", doc.properties.content.content);
    var version = doc.getVersion(1);
    assertNotNull(version);
    assertEquals("Hallo", version.properties.content.content);
    version = doc.getVersion(2);
    assertNotNull(version);
    assertEquals("Test", version.properties.content.content);
    assertNull(companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.test10 = function() {
    REC.content ="Verdienstabrechnung     0000123456  3000 Abrechnungsmonat Mai 2015";
    var rules = '<archivTyp name="LVMGehalt" searchString="Verdienstabrechnung">                              ' +
                ' <archivZiel type="my:archivContent" />                                                      ' +
    '<archivPosition folder="Dokumente/Gehalt/Gehalt LVM/{tmp}">                                              ' +
    '<archivZiel type="my:archivFolder" />                                                                    ' +
    '</archivPosition>                                                                                        ' +
    '<archivPosition link="true" folder="Dokumente/LVM/Gehalt LVM">                                           ' +
    '<archivZiel type="my:archivFolder" />                                                                    ' +
    '</archivPosition>                                                                                        ' +
    '<tags name="Gehalt" />                                                                                   ' +
    '<tags name="LVM" />                                                                                      ' +
    '<category name="Gehalt/Gehalt Klaus" />                                                                  ' +
    '<searchItem name="person" fix="Klaus" target="my:person" />                                              ' +
    '<searchItem name="tmp" objectTyp="date" value="datum">                                                   ' +
    '<format formatString="YYYY" />                                                                           ' +
    '</searchItem>                                                                                            ' +
    '<archivTyp name="Rückrechnung" searchString="Rückrechnungsdifferenz">                                    ' +
    '<tags name="Rückrechnung" />                                                                             ' +
    '<searchItem name="title" fix="Rückrechnung {datum}" target="cm:title" />                                 ' +
    '<searchItem name="datum" text="Abrechnungsmonat" word="2,2" objectTyp="date" target="my:documentDate">   ' +
    '<check lowerValue="01/01/2005" upperValue="01/01/2020" />                                                ' +
    '</searchItem>                                                                                            ' +
    '<searchItem name="betrag" text="Rückrechnungsdifferenz" objectTyp="float" target="my:amount">            ' +
    '<check lowerValue="-200" upperValue="200" />                                                             ' +
    '<delimitter typ="start" text="&#0032;" count="1" removeBlanks="after" />                                 ' +
    '<archivZiel aspect="my:amountable" />                                                                    ' +
    '</searchItem>                                                                                            ' +
    '</archivTyp>                                                                                             ' +
    '<archivTyp name="Verdienstabrechnung" searchString="" unique="error">                                    ' +
    '<searchItem name="title" text="Abrechnungsmonat" word="1,2" target="cm:title" />                         ' +
    '<searchItem name="datum" value="title" objectTyp="date" target="my:documentDate">                        ' +
    '<check lowerValue="01/01/2005" upperValue="01/01/2020" />                                                ' +
    '</searchItem>                                                                                            ' +
    '<searchItem name="betrag" text="0000123456" objectTyp="float" target="my:amount">                        ' +
    '<check lowerValue="3000" upperValue="15000" />                                                           ' +
    '<archivZiel aspect="my:amountable" />                                                                    ' +
    '</searchItem>                                                                                            ' +
    '</archivTyp>                                                                                             ' +
    '</archivTyp>';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    assertNotNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
    archivTyp.resolve();
    assertNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    assertTrue(doc.isSubType("my:archivContent"));
    assertTrue(doc.parent[0].isSubType("my:archivFolder"));
    assertNull(companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

