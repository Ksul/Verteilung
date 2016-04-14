/**
 * Created by m500288 on 09.03.15.
 */


describe("Test für Recognition", function() {

    beforeEach(function () {
        REC.init();
    });

    it("testRecognize", function() {
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
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.isSubType("my:archivContent")).toBeTruthy();
        expect(doc.properties["my:amount"]).toBe(200);
        expect(doc.properties["my:documentDate"].getTime()).toBe(new Date(2015, 1, 14).getTime());
        expect(doc.properties["cm:title"]).toBe("Januar 2015");
        expect(doc.properties["my:person"]).toBe("Klaus");
        expect(doc.hasTag("Rechnung")).toBeTruthy();
        expect(doc.hasTag("Zauberfrau")).toBeTruthy();
        expect(doc.hasTag("Steuerrelevant")).toBeTruthy();
        expect(doc.hasAspect("my:amountable")).toBeTruthy();
        expect(doc.properties["cm:categories"][0].name).toBe("Rechnung Zauberfrau");
    });

    it("testUnknownDocument", function() {
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
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Unbekannt/WebScriptTest")).not.toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });
});




