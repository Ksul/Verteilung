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

ArchivTypTest.prototype.testNormal = function() {
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

ArchivTypTest.prototype.testDuplicateWithError1 = function() {
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
    assertNull(companyhome.childByNamePath("/Inbox/WebScriptTest"));
    var doc = companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    doc = companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest");
    assertNotNull(doc);
    assertTrue(doc.isSubType("my:archivContent"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.testDuplicateWithError2 = function() {
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

ArchivTypTest.prototype.testDuplicateWithNothing1 = function() {
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

ArchivTypTest.prototype.testDuplicateWithNothing2 = function() {
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

ArchivTypTest.prototype.testDuplicateWithOverwrite1 = function() {
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

ArchivTypTest.prototype.testDuplicateWithOverwrite2 = function() {
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

ArchivTypTest.prototype.testDuplicateWithNewVersion1 = function() {
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

ArchivTypTest.prototype.testDuplicateWithNewVersion2 = function() {
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
    assertFalse(doc.hasAspect(new BasicObject("cm:workingcopy")));
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

ArchivTypTest.prototype.testComplete1 = function() {
    REC.content ="Verdienstabrechnung     0000123456  3000 Abrechnungsmonat Mai 2015";
    var rules = '<archivTyp name="LVMGehalt" searchString="Verdienstabrechnung" debugLevel="debug">                              ' +
        ' <archivZiel type="my:archivContent" />                                                      ' +
        '<archivPosition folder="Dokumente/Gehalt/Gehalt Hansel/{tmp}">                                              ' +
        '<archivZiel type="my:archivFolder" />                                                                    ' +
        '</archivPosition>                                                                                        ' +
        '<archivPosition link="true" folder="Dokumente/Hansel/Gehalt Hansel">                                           ' +
        '<archivZiel type="my:archivFolder" />                                                                    ' +
        '</archivPosition>                                                                                        ' +
        '<tags name="Gehalt" />                                                                                   ' +
        '<tags name="Hansel" />                                                                                      ' +
        '<category name="Gehalt/Gehalt Hansel" />                                                                  ' +
        '<searchItem name="person" fix="Hansel" target="my:person" />                                              ' +
        '<searchItem name="tmp" objectTyp="date" value="datum">                                                   ' +
        '<format formatString="YYYY" />                                                                           ' +
        '</searchItem>                                                                                            ' +
        '<archivTyp name="Rückrechnung" searchString="Rückrechnungsdifferenz">                                    ' +
        '<tags name="Rückrechnung" />                                                                             ' +
        '<searchItem name="titel" text="Abrechnungsmonat" word="2,2"  />  ' +
        '<searchItem name="title" fix="Rückrechnung {titel}" target="cm:title"/>  ' +
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
    var doc = companyhome.childByNamePath("/Dokumente/Gehalt/Gehalt Hansel/2015/WebScriptTest");
    assertNotNull(doc);
    assertTrue(doc.isSubType("my:archivContent"));
    assertEquals(3000, doc.properties["my:amount"]);
    assertEquals(new Date(2015, 4, 1), doc.properties["my:documentDate"]);
    assertEquals("Mai 2015", doc.properties["cm:title"]);
    assertEquals("Hansel", doc.properties["my:person"]);
    assertTrue(doc.hasTag("Gehalt"));
    assertTrue(doc.hasTag("Hansel"));
    assertTrue(doc.hasAspect("my:amountable"));
    assertTrue(doc.properties["cm:categories"][0].name == "Gehalt Hansel");
    //assertTrue(doc.isCategory());
    //assertTrue(doc.category.contains(new BasicObject("")));
    var linkDoc = companyhome.childByNamePath("Dokumente/Hansel/Gehalt Hansel/WebScriptTest");
    assertNotNull(linkDoc);
    assertTrue(linkDoc.isSubType("my:archivContent"));
    assertEquals(3000, linkDoc.properties["my:amount"]);
    assertEquals(new Date(2015, 4, 1), linkDoc.properties["my:documentDate"]);
    assertEquals("Mai 2015", linkDoc.properties["cm:title"]);
    assertEquals("Hansel", linkDoc.properties["my:person"]);
    assertTrue(linkDoc.hasTag("Gehalt"));
    assertTrue(linkDoc.hasTag("Hansel"));
    assertTrue(linkDoc.hasAspect("my:amountable"));
    assertTrue(linkDoc.parent[0].isSubType("my:archivFolder"));
    assertTrue(linkDoc.properties["cm:categories"][0].name == "Gehalt Hansel");
    assertTrue(doc.id == linkDoc.id);
    assertNull(companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.testComplete2 = function() {
    REC.content ="Verdienstabrechnung     0000123456 Rückrechnungsdifferenz 200 Abrechnungsmonat R Mai 2015";
    var rules = '<archivTyp name="LVMGehalt" searchString="Verdienstabrechnung" debugLevel="debug">                              ' +
        ' <archivZiel type="my:archivContent" />                                                      ' +
        '<archivPosition folder="Dokumente/Gehalt/Gehalt Hansel/{tmp}">                                              ' +
        '<archivZiel type="my:archivFolder" />                                                                    ' +
        '</archivPosition>                                                                                        ' +
        '<archivPosition link="true" folder="Dokumente/Hansel/Gehalt Hansel">                                           ' +
        '<archivZiel type="my:archivFolder" />                                                                    ' +
        '</archivPosition>                                                                                        ' +
        '<tags name="Gehalt" />                                                                                   ' +
        '<tags name="Hansel" />                                                                                      ' +
        '<category name="Gehalt/Gehalt Hansel" />                                                                  ' +
        '<searchItem name="person" fix="Hansel" target="my:person" />                                              ' +
        '<searchItem name="tmp" objectTyp="date" value="datum">                                                   ' +
        '<format formatString="YYYY" />                                                                           ' +
        '</searchItem>                                                                                            ' +
        '<archivTyp name="Rückrechnung" searchString="Rückrechnungsdifferenz">                                    ' +
        '<tags name="Rückrechnung" />                                                                             ' +
        '<searchItem name="titel" text="Abrechnungsmonat" word="2,2"  />  ' +
        '<searchItem name="title" fix="Rückrechnung {titel}" target="cm:title"/>  ' +
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
    var doc = companyhome.childByNamePath("/Dokumente/Gehalt/Gehalt Hansel/2015/WebScriptTest");
    assertNotNull(doc);
    assertTrue(doc.isSubType("my:archivContent"));
    assertEquals(200, doc.properties["my:amount"]);
    assertEquals(new Date(2015, 4, 1), doc.properties["my:documentDate"]);
    assertEquals("Rückrechnung Mai 2015", doc.properties["cm:title"]);
    assertEquals("Hansel", doc.properties["my:person"]);
    assertTrue(doc.hasTag("Gehalt"));
    assertTrue(doc.hasTag("Hansel"));
    assertTrue(doc.hasAspect("my:amountable"));
    assertTrue(doc.properties["cm:categories"][0].name == "Gehalt Hansel");
    //assertTrue(doc.isCategory());
    //assertTrue(doc.category.contains(new BasicObject("")));
    var linkDoc = companyhome.childByNamePath("Dokumente/Hansel/Gehalt Hansel/WebScriptTest");
    assertNotNull(linkDoc);
    assertTrue(linkDoc.isSubType("my:archivContent"));
    assertEquals(200, linkDoc.properties["my:amount"]);
    assertEquals(new Date(2015, 4, 1), linkDoc.properties["my:documentDate"]);
    assertEquals("Rückrechnung Mai 2015", linkDoc.properties["cm:title"]);
    assertEquals("Hansel", linkDoc.properties["my:person"]);
    assertTrue(linkDoc.hasTag("Gehalt"));
    assertTrue(linkDoc.hasTag("Hansel"));
    assertTrue(linkDoc.hasAspect("my:amountable"));
    assertTrue(linkDoc.parent[0].isSubType("my:archivFolder"));
    assertTrue(linkDoc.properties["cm:categories"][0].name == "Gehalt Hansel");
    assertTrue(doc.id == linkDoc.id);
    assertNull(companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

ArchivTypTest.prototype.testWithMissingMandatoryField = function() {
    var folder = companyhome.createFolder("Dokumente");
    folder = folder.createFolder("Rechnungen");
    folder = folder.createFolder("Rechnungen Zauberfrau");
    folder = folder.createFolder("2015");
    folder.createNode("WebScriptTest", "my:archivContent");
    REC.content ="ZAUBERFRAU";
    REC.mandatoryElements = ["cm:hansel"];
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
    assertNotNull(companyhome.childByNamePath("/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Fehler/Doppelte/WebScriptTest"));
    assertNotNull(companyhome.childByNamePath("/Fehler/WebScriptTest"));
};

