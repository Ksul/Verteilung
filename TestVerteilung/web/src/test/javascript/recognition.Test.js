/**
 * Created by m500288 on 09.03.15.
 */

RecognitionTest = TestCase("RecognitionTest");

var iBox;

RecognitionTest.prototype.setUp = function() {
    REC.init();
};

RecognitionTest.prototype.testRecognize = function() {
    var iBox = companyhome.childByNamePath("/Archiv/Inbox");
    var doc = iBox.createNode("WebScriptTest", "my:archivContent");
    doc.properties.content.write(new Content("Zauberfrau Rechnung Nr 1001 Gesamtbetrag 200  Datum 14.02.2015"));
    var rules =
   '<documentTypes                                                                                                                                                                                                                                                                                 ' +
   'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"                                                                                                                                                                                                                                          ' +
   'xmlns:cm="http://www.alfresco.org/model/content/1.0"                                                                                                                                                                                                                                           ' +
   'xmlns:my="http://www.schulte.local/archiv"  xsi:noNamespaceSchemaLocation="doc.xsd" archivRoot="Archiv/" inBox="Inbox" mandatory="cm:title,my:documentDate,my:person" unknownBox="Unbekannt" errorBox="Fehler"  duplicateBox="Fehler/Doppelte" debugLevel="informational" maxDebugLength="40"> ' +
   '<archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">                                                                                                                                                                                                                                        ' +
   '<archivZiel type="my:archivContent" />                                                                                                                                                                                                                                                         ' +
   '<archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}">                                                                                                                                                                                                                     ' +
   '<archivZiel type="my:archivFolder" />                                                                                                                                                                                                                                                          ' +
   '</archivPosition>                                                                                                                                                                                                                                                                              ' +
   '<tags name="Rechnung" />                                                                                                                                                                                                                                                                       ' +
   '<tags name="Zauberfrau" />                                                                                                                                                                                                                                                                     ' +
   '<tags name="Steuerrelevant" />                                                                                                                                                                                                                                                                 ' +
   '<category name="Rechnung/Rechnung Zauberfrau" />                                                                                                                                                                                                                                               ' +
   '<searchItem name="person" fix="Klaus" target="my:person" />                                                                                                                                                                                                                                    ' +
   '<searchItem name="title" eval="new Date(new Date(\'{datum}\').setMonth(new Date(\'{datum}\').getMonth()-1))" target="cm:title">                                                                                                                                                                    ' +
   '<format formatString="MMMM YYYY" />                                                                                                                                                                                                                                                            ' +
   '</searchItem>                                                                                                                                                                                                                                                                                  ' +
   '<searchItem name="datum" text="Datum" objectTyp="date" target="my:documentDate">                                                                                                                                                                                                               ' +
   '<delimitter typ="start" text="&#0032;" count="1" />                                                                                                                                                                                                                                            ' +
   '<check lowerValue="01/01/2005" upperValue="01/01/2020" />                                                                                                                                                                                                                                      ' +
   '</searchItem>                                                                                                                                                                                                                                                                                  ' +
   '<searchItem name="betrag" text="Gesamtbetrag" word="1" objectTyp="float" target="my:amount">                                                                                                                                                                                                   ' +
   '<check lowerValue="0" upperValue="250" />                                                                                                                                                                                                                                                      ' +
   '<archivZiel aspect="my:amountable" />                                                                                                                                                                                                                                                          ' +
   '</searchItem>                                                                                                                                                                                                                                                                                  ' +
   '<searchItem name="id" text="Rechnung Nr" word="1" objectTyp="int" target="my:idvalue">                                                                                                                                                                                                         ' +
   '<check lowerValue="1000" upperValue="15000" />                                                                                                                                                                                                                                                 ' +
   '<archivZiel aspect="my:idable" />                                                                                                                                                                                                                                                              ' +
   '<format formatString="00000" />                                                                                                                                                                                                                                                                ' +
   '</searchItem>                                                                                                                                                                                                                                                                                  ' +
   '<searchItem name="tmp" objectTyp="date" value="datum">                                                                                                                                                                                                                                         ' +
   '<format formatString="YYYY" />                                                                                                                                                                                                                                                                 ' +
   '</searchItem>                                                                                                                                                                                                                                                                                  ' +
   '</archivTyp>                                                                                                                                                                                                                                                                                   ' +
   '</documentTypes>                                                                                                                                                                                                                                                                               ';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    REC.recognize(doc, new XMLObject(XMLDoc.docNode));
    jstestdriver.log(REC.getMessage()) ;
    assertNull(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest"));
    doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
    assertNotNull(doc);
    assertTrue(doc.isSubType("my:archivContent"));
    assertEquals(200, doc.properties["my:amount"]);
    assertEquals(new Date(2015, 1, 14), doc.properties["my:documentDate"]);
    assertEquals("Januar 2015", doc.properties["cm:title"]);
    assertEquals("Klaus", doc.properties["my:person"]);
    assertTrue(doc.hasTag("Rechnung"));
    assertTrue(doc.hasTag("Zauberfrau"));
    assertTrue(doc.hasTag("Steuerrelevant"));
    assertTrue(doc.hasAspect("my:amountable"));
    assertTrue(doc.properties["cm:categories"][0].name == "Rechnung Zauberfrau");
};

RecognitionTest.prototype.testUnknownDocument = function() {
    var iBox = companyhome.childByNamePath("Archiv/Inbox");
    var doc = iBox.createNode("WebScriptTest", "my:archivContent");
    doc.properties.content.write(new Content("Hansel Rechnung Nr 1001 Gesamtbetrag 200  Datum 14.02.2015"));
    var rules =
        '<documentTypes                                                                                                                                                                                                                                                                                 ' +
        'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"                                                                                                                                                                                                                                          ' +
        'xmlns:cm="http://www.alfresco.org/model/content/1.0"                                                                                                                                                                                                                                           ' +
        'xmlns:my="http://www.schulte.local/archiv"  xsi:noNamespaceSchemaLocation="doc.xsd" archivRoot="Archiv/" inBox="Inbox" mandatory="cm:title,my:documentDate,my:person" unknownBox="Unbekannt" errorBox="Fehler"  duplicateBox="Fehler/Doppelte" debugLevel="informational" maxDebugLength="40"> ' +
        '<archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">                                                                                                                                                                                                                                        ' +
        '<archivZiel type="my:archivContent" />                                                                                                                                                                                                                                                         ' +
        '<archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}">                                                                                                                                                                                                                     ' +
        '<archivZiel type="my:archivFolder" />                                                                                                                                                                                                                                                          ' +
        '</archivPosition>                                                                                                                                                                                                                                                                              ' +
        '</archivTyp>                                                                                                                                                                                                                                                                                   ' +
        '</documentTypes>                                                                                                                                                                                                                                                                               ';
    XMLDoc.loadXML(rules);
    XMLDoc.parse();
    REC.recognize(doc, new XMLObject(XMLDoc.docNode));
    jstestdriver.log(REC.getMessage()) ;
    assertNull(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest"));
    assertNotNull(companyhome.childByNamePath("/Archiv/Unbekannt/WebScriptTest"));
    assertNull(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest"));
};
