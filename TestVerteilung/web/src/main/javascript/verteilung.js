
/**
 * Prüft, ob ein Alfresco Server antwortet
 * @param url         URL des Servers
 * @returns {boolean} true, wenn der Server verfügbar ist
 */
function checkServerStatus(url) {

        var obj = executeService("isURLAvailable", [{"name":"server", "value":url}], null, true);
    return obj.result.toString() == "true";
}

/**
 * liefert die Einstellungen
 * wenn noch keine Einstellungen gesetzt sind, dann sucht die Funktion einen passenden URL-Parameter
 * und trägt diesen dann ein. Ist dieser auch nicht vorhanden, dann wird <null> zurück geliefert.
 * @param key    Schlüssel der Einstellung
 * @returns {*}  Den Wert der Einstellung
 */
function getSettings(key) {
    var ret;
    if (!exist(settings) || settings.settings.filter(function (o) {
        return o.key.indexOf(key) >= 0;
    }).length == 0) {
        var urlPar = getUrlParam(key);
        if (urlPar == null)
            return null;
        else
            settings.settings.push({"key": key, "value": urlPar});
    }
    return settings.settings.filter(function (o) {
        return o.key.indexOf(key) >= 0;
    })[0].value;
}

/**
 * zeigt die Progressbar
 * wird momentan nicht verwendet
 */
function showProgress() {
    $(function() {
        var progressbar = $("#progressbar"), progressLabel = $(".progress-label");
        progressbar.progressbar({
            value : 0,
            change : function() {
                progressLabel.text(Math.round(progressbar.progressbar("value")) + "%");
            },
            complete : function() {
                setTimeout(function(){progressLabel.text("");progressbar.progressbar("destroy");}, 3000);
            }
        });
    });
}

/**
 * verwaltet die Controls
 */
function manageControls() {
    //document.getElementById('tree').style.display = 'none';
    document.getElementById('dtable').style.display = 'none';
    document.getElementById('inTxt').style.display = 'block';
    document.getElementById('filesinput').style.display = 'block';
    document.getElementById('settings').style.display = 'block';
    //document.getElementById('docAlfresco').removeAttribute("disabled");
    //document.getElementById('closeAlfresco').style.display = 'none';
    document.getElementById('play').style.display = 'block';
    document.getElementById('play').removeAttribute("disabled");
    document.getElementById('test').style.display = 'block';
    document.getElementById('pdf').style.display = 'block';
    document.getElementById('pdf').setAttribute("disabled", true);
    document.getElementById('loadScript').style.display = 'block';
    document.getElementById('loadScript').removeAttribute("disabled");
    document.getElementById('searchCont').style.display = 'block';
    document.getElementById('searchCont').removeAttribute("disabled");
    document.getElementById('beautifyScript').style.display = 'none';
    document.getElementById('back').style.display = 'none';
    document.getElementById('closeScript').style.display = 'none';
    document.getElementById('closeTest').style.display = 'none';
    document.getElementById('reloadScript').style.display = 'none';
    document.getElementById('saveScript').style.display = 'none';
    document.getElementById('saveScript').setAttribute("disabled", true);
    document.getElementById('getScript').style.display = 'none';
    document.getElementById('sendScript').style.display = 'none';
    document.getElementById('beautifyRules').style.display = 'block';
    document.getElementById('searchRules').style.display = 'block';
    document.getElementById('foldAll').style.display = 'block';
    document.getElementById('unfoldAll').style.display = 'block';
    document.getElementById('getRules').removeAttribute("disabled");
    document.getElementById('sendRules').removeAttribute("disabled");
    document.getElementById('saveRules').setAttribute("disabled", true);
    document.getElementById('sendToInbox').style.display = 'block';

    if (testMode) {
        document.getElementById('test').style.display = 'none';
        document.getElementById('closeTest').style.display = 'block';
        //document.getElementById('docAlfresco').setAttribute("disabled", true);
        document.getElementById('loadScript').setAttribute("disabled", true);
        document.getElementById('pdf').setAttribute("disabled", true);
    }
/*
    if (alfrescoMode) {
        document.getElementById('tree').style.display = 'block';
        document.getElementById('dtable').style.display = 'none';
        document.getElementById('inTxt').style.display = 'none';
        document.getElementById('closeAlfresco').style.display = 'block';
        document.getElementById('docAlfresco').setAttribute("disabled", true);
        document.getElementById('play').setAttribute("disabled", true);
    }
*/
    if (textEditor.getSession().getValue().length == 0)
        document.getElementById('searchCont').setAttribute("disabled", true);
    if (isLocal()) {
        document.getElementById('saveRules').removeAttribute("disabled");
        document.getElementById('saveScript').removeAttribute("disabled");
    }
    if (multiMode) {
        document.getElementById('inTxt').style.display = 'none';
        document.getElementById('dtable').style.display = 'block';
        document.getElementById('sendToInbox').style.display = 'none';
    }
    if (showMulti) {
        document.getElementById('back').style.display = 'block';
        document.getElementById('dtable').style.display = 'none';
    }
    if (currentPDF)
        document.getElementById('pdf').removeAttribute("disabled");

    if (runLocal || (scriptID == null && rulesID == null)) {
        document.getElementById('sendScript').setAttribute("disabled", true);
        document.getElementById('getScript').setAttribute("disabled", true);
        document.getElementById('getRules').setAttribute("disabled", true);
        document.getElementById('sendRules').setAttribute("disabled", true);
    }
    // Muss als letztes stehen
    if (scriptMode) {
        //document.getElementById('tree').style.display = 'none';
        document.getElementById('dtable').style.display = 'none';
        document.getElementById('inTxt').style.display = 'block';
        //document.getElementById('docAlfresco').setAttribute("disabled", true);
        document.getElementById('filesinput').style.display = 'none';
        document.getElementById('play').style.display = 'none';
        document.getElementById('test').style.display = 'none';
        document.getElementById('back').style.display = 'none';
        document.getElementById('pdf').style.display = 'none';
        document.getElementById('loadScript').style.display = 'none';
        document.getElementById('closeScript').style.display = 'block';
        document.getElementById('sendScript').style.display = 'block';
        document.getElementById('getScript').style.display = 'block';
        document.getElementById('saveScript').style.display = 'block';
        document.getElementById('reloadScript').style.display = 'block';
        document.getElementById('beautifyScript').style.display = 'block';
    }
}

/**
 * TODO prüfen, wie das mit den Services umgesetzt werden kann
 * Öffnet ein PDF
 * @param name       Name des Dokuments
 * @param fromServer legt fest, ob das Dokument vom Server geholt werden soll
 */
function openPDF(name, fromServer) {
    try {
        if (fromServer) {
            if (isLocal()) {
                var ticket = document.reader.getTicket(getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"), null);
                window.open(name + "?alf_ticket=" + ticket);
            }
            else {
                var dataString = {
                    "function": "getTicket",
                    "server": getSettings("server"),
                    "username": getSettings("user"),
                    "password": getSettings("password"),
                    "proxyHost": getSettings("proxy"),
                    "proxyPort": getSettings("port")
                };
                $.ajax({
                    type: "POST",
                    data: dataString,
                    datatype: "json",
                    url: "/TestVerteilung/VerteilungServlet",
                    error: function (response) {
                        try {
                            var r = jQuery.parseJSON(response.responseText);
                            message("Fehler", "Fehler: " + r.Message + "<br>StackTrace: " + r.StackTrace + "<br>ExceptionType: " + r.ExceptionType);
                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                    success: function (data) {
                        window.open(name + "?alf_ticket=" + data.result.toString());
                    }
                });
            }
        }
        else {
            if (isLocal())
                document.reader.openPDF(name);
            else
                window.open("/TestVerteilung/VerteilungServlet?function=openPDF&fileName=" + name, "_blank");
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * lädt einen Text
 * diese Methode wird auch aus dem Applet aufgerufen
 * @param content    der originale Inhalt der Datei
 * @param txt        der Inhalt des Dokumentes in Textform
 * @param name       der Name des Dokumentes
 * @param typ        der Dokuemttyp  (wird der eigentlich noch gebraucht)
 * @param container  ???
 */
function loadText(content, txt, name, typ, container) {
    try {
        multiMode = false;
        currentFile = name;
        currentContent = content;
        currentText = txt;
        currentContainer = container;
        removeMarkers(markers, textEditor);
        textEditor.getSession().setValue(txt);
        document.getElementById('headerWest').textContent = name;
        propsEditor.getSession().setValue("");
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * lädt ein Dokument und trägt die Inhalte in die Tabelle ein
 * Methode wird benutzt wenn mehr als ein Dokument geladen werden soll
 * Methode wird auch aus dem Applet aufgerufen
 * @param content           der originale Inhalt der Datei
 * @param txt               Textinhalt des Dokumentes
 * @param name              Name des Dokumentes
 * @param typ               Typ des Dokumentes
 * @param notDeleteable     Merker, ob das Dokument gelöscht werden kann (geht nur bei lokalen)
 * @param container         ???
 */
function loadMultiText(content, txt, name, typ,  notDeleteable, container) {
    try {
        multiMode = true;
        var dat = [];
        REC.currentDocument.setContent(txt);
        REC.testRules(rulesEditor.getSession().getValue());
        dat["text"] = txt;
        dat["file"] = name;
        dat["content"] = content;
        dat["log"] = REC.getMessage();
        dat["result"] = REC.results;
        dat["position"] = REC.positions;
        dat["xml"] = REC.currXMLName;
        dat["typ"] = typ;
        dat["error"] = REC.errors;
        dat["container"] = container;
        dat["notDeleteable"] = notDeleteable;
        daten[name] = dat;
        var ergebnis = [];
        ergebnis["error"] = REC.errors.length > 0;
        var row = [ null,name,  REC.currXMLName.join(" : "), ergebnis, uuid(), REC.errors ];
        tabelle.row.add(row).draw();
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * handelt den File-Select der Dateiauswahl
 * @param evt    das Event
 */
function handleFileSelect(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    var files = evt.dataTransfer.files;
    readFiles(files);
}

/**
 * handelt das Verhalten, wenn eine Datei Über den Bereich fallen gelassen wird
 * @param evt    das Event
 */
function handleDragOver(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    evt.dataTransfer.dropEffect = 'copy';
}

/**
 * Eventlistener für den Dateiauswahldialog, wenn mehrere Dateien ausgewählt worden sind
 * @param evt    das Event
 */
function readMultiFile(evt) {
    multiMode = false;
    currentPDF = false;
    var files = evt.target.files;
    readFiles(files);
}

/**
 * liest die ausgewählten Dateien
 * @param files  die Dateien
 */
function readFiles(files) {
    try {
        if (currentRules == null || !currentRules.endsWith("doc.xml")) {
            var open = openFile("doc.xml");
            currentRules = open[1];
            rulesEditor.getSession().setValue(open[0]);
            rulesEditor.getSession().foldAll(1);
        }
        textEditor.getSession().setValue("");
        tabelle.clear();
        daten = [];
        var count = files.length;
        var maxLen = 1000000;
        var first = true;
        var reader;
        var blob;
        for (var i = 0; i < count; i++) {
            var f = files[i];
            if (f) {
                // PDF Files
                if (f.name.toLowerCase().endsWith(".pdf")) {
                    currentPDF = true;
                    reader = new FileReader();
                    reader.onloadend = (function (theFile, clear) {
                        return function (evt) {
                            try {
                                if (evt.target.readyState == FileReader.DONE) {// DONE == 2
                                    var json = executeService("extractPDFContent", [
                                        {"name": "documentText", "value": evt.target.result, "type": "byte"}
                                    ], "PDF Datei konte nicht geparst werden:");
                                    if (json.success) {
                                        if (count == 1)
                                            loadText(evt.target.result, json.result, theFile.name, theFile.type, null);
                                        else
                                            loadMultiText(evt.target.result, json.result, theFile.name, theFile.type, "false", null);
                                    }
                                }
                            } catch (e) {
                                errorHandler(e);
                            }
                        };
                    })(f, first);
                    blob = f.slice(0, f.size + 1);
                    reader.readAsBinaryString(blob);
                }
                // ZIP Files
                if (f.name.toLowerCase().endsWith(".zip")) {
                    reader = new FileReader();
                    reader.onloadend = (function (theFile) {
                        return function (evt) {
                            try {
                                if (evt.target.readyState == FileReader.DONE) {
                                    var json = executeService("extractZIPAndExtractPDFToInternalStorage", [
                                        {"name": "documentText", "value": evt.target.result, "type": "byte"}
                                    ], "ZIP Datei konte nicht entpackt werden:");
                                    if (json.success) {
                                        count = count + json.result - 1;
                                        var json1 = executeService("getDataFromInternalStorage");
                                        if (json1.success) {
                                            var erg = json1.result;
                                            for (var pos in erg) {
                                                var entry = erg[pos];
                                                if (count == 1)
                                                    loadText(atob(entry.data), entry.extractedData, entry.name, "application/zip", null);
                                                else {
                                                    // die originalen Bytes kommen decodiert, also encoden!
                                                    loadMultiText(atob(entry.data), entry.extractedData, entry.name, entry.name.toLowerCase().endsWith(".pdf") ? "application/pdf" : "text/plain", "true",  null);
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (e) {
                                errorHandler(e);
                            }
                        };
                    })(f);
                    blob = f.slice(0, f.size + 1);
                    reader.readAsBinaryString(blob);
                }
                // Text Files
                if (f.type == "text/plain") {
                    var r = new FileReader();
                    if (files.length == 1) {
                        r.onload = (function (theFile) {
                            return function (e) {
                                loadText(e.target.result, e.target.result, theFile.name, theFile.mozFullPath, theFile.type);
                            };
                        })(f);
                    } else {
                        r.onload = (function (theFile) {
                            return function (e) {
                                loadMultiText(e.target.result, e.target.result, theFile.name,  theFile.type, "false", null);
                            };
                        })(f);
                    }
                    r.readAsText(f);
                }
            } else {
                textEditor.getSession().setValue(textEditor.getSession().getValue() + " Failed to load file!\n");
            }
            first = false;
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * führt einen Service aus
 * die Methode prüft dabei im Appletzweig, ob ein String Parameter zu lang ist und überträgt ihn dann häppchenweise.
 * der entsprechende Parameter wird dann nicht mehr übergeben und muss dann in der entsprechenden Servicemethode im
 * Applet aus dem internenen Spreicher besorgt werden. Bislang funktioniert dieses Verfahren aber nur mit einem
 * Parameter.
 * @param service           der Name des Service
 * @param params            die Parameter als JSON Objekt
 *                          name:  der Name des Parameters ( wird nur für das Servlet gebraucht)
 *                          value: der Inhalt des Paramaters
 *                          type: der Typ des Parameters
 * @param messages          Array mit Meldungen. Die erste ist die Fehlermeldung, der zweite Eintrag ist eine Erfolgsmeldung
 * @param ignoreError       Flag, ob ein Fehler ignoriert werden soll
 * @return das Ergebnis als JSON Objekt
 */
function executeService(service, params, messages, ignoreError) {
    var json;
    var errorMessage;
    var successMessage;
    var longParameter = false;
    try {
        if (exist(messages)) {
            if (typeof messages == "object") {
                if (messages.length == 2) {
                    errorMessage = messages[0];
                    successMessage = messages[1];
                } else {
                    errorMessage = messages[0];
                }
            } else if (typeof messages == "string") {
                errorMessage = messages;
            }
        }
        if (isLocal()) {
            // Aufruf über Applet
            var maxLen = 1100000;
            var execute = "document.reader." + service + "(";
            var first = true;
            if (exist(params)) {
                for (index = 0; index < params.length; ++index) {
                    // falls Baytecode übertragen werden soll, dann Umwandlung damit es nicht zu Konvertierungsproblemen kommt
                    if (exist(params[index].type) && params[index].type == "byte")
                        params[index].value = btoa(params[index].value);
                    // prüfen, ob Parameter zu lang ist
                    if (typeof params[index].value == "String" && params[index].value.length > maxLen) {
                        // den Inhalt häppchenweise übertragen
                        longParameter = true;
                        for (var k = 0; k < Math.ceil(params[index].value.length / maxLen); k++)
                            document.reader.fillParameter(params[index].value.substr(k * maxLen, maxLen), k == 0);
                    } else {
                        // der Inhalt ist nicht zu lang und kann direkt zum Applet übertragen werden
                        if (!first)
                            execute = execute + ", ";
                        execute = execute + "params[" + index + "].value";
                        first = false;
                    }
                }
            }
            execute = execute + ")";
            var obj = eval(execute);
            json = jQuery.parseJSON(obj);
        } else {
            // Aufruf über Servlet
            var dataString = {
                "function": service
            };
            if (exist(params)) {
                for (index = 0; index < params.length; ++index) {
                    // falls Baytecode übertragen werden soll, dann Umwandlung damit es nicht zu Konvertierungsproblemen kommt
                    if (exist(params[index].type) && params[index].type == "byte")
                        params[index].value = btoa(params[index].value);
                    eval("dataString." + params[index].name + " = params[" + index + "].value");
                }
            }
            $.ajax({
                type: "POST",
                data: dataString,
                datatype: "json",
                cache: false,
                async: false,
                url: "/TestVerteilung/VerteilungServlet",
                error: function (response) {
                    try {
                        var r = jQuery.parseJSON(response.responseText);
                        message("Fehler", "Fehler: " + r.Message + "<br>StackTrace: " + r.StackTrace + "<br>ExceptionType: " + r.ExceptionType);
                    } catch (e) {
                        var str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for (var prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        message("Fehler", str + "<br>" + response.responseText);
                    }
                },
                success: function (data) {
                    json = data;
                }
            });
        }
        if (!json.success) {
            if (exist(errorMessage))
                errorString = errorMessage + "<br>" + json.result;
            else
                errorString = json.result;
            // gibt es eine Fehlermeldung aus dem Service?
            if (exist(json.error))
                errorString = errorString + "<br>" + json.error;
            throw new Error(errorString);
        } else {
            if (exist(successMessage))
                fillMessageBox(successMessage);
        }
        return json;
    } catch (e) {
        var p = "Service: " + service + "<br>";
        if (exist(params)) {
            for (index = 0; index < params.length; ++index) {
                p = p + "Parameter: " + params[index].name
                if (exist(params[index].value) && typeof params[index].value =="string")
                    p = p + " : " + params[index].value.substr(0, 40) + "<br>";
                else
                    p = p + " : Parameter Value fehlt!<br>";
            }
        }
        if (exist(errorMessage))
            p = errorMessage + "<br>" + e.toString() + "<br>" + p;
        else
            p = errorMessage + "<br>" + e.toString();
        if (!ignoreError)
            errorHandler(e, p);
        return {result: e, success: false};
    }
}


/**
 * generiert eine eindeutige Id
 * @returns {string}
 */
function uuid() {
    var chars = '0123456789abcdef'.split('');
    var uuid = [], rnd = Math.random, r;
    uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
    uuid[14] = '4';
    // version 4
    for ( var i = 0; i < 36; i++) {
        if (!uuid[i]) {
            r = 0 | rnd() * 16;
            uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r & 0xf];
        }
    }
    return uuid.join('');
}

/**
 * startet für alle Dokumente nochmal die Verteilung neu
 */
function doReRunAll() {
    try {
        textEditor.getSession().setValue("");
        clearMessageBox();
        var tabData =  tabelle.fnGetData();
        tabelle._fnClearTable();
        for ( var i = 0; i < tabData.length; i++) {
            var name = tabData[i][1];
            REC.currentDocument.setContent(daten[name].text);
            REC.testRules(rulesEditor.getSession().getValue());
            daten[name].log = REC.mess;
            daten[name].result = REC.results;
            daten[name].position = REC.positions;
            daten[name].xml = REC.currXMLName;
            daten[name].error = REC.errors;
            var ergebnis = [];
            ergebnis["error"] = REC.errors.length > 0;
            var row = [ null,name,  REC.currXMLName.join(" : "), ergebnis, uuid(), REC.errors ];
            tabelle.fnAddData(row);
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * markiert in den Regeln die verendeten Stellen
 * @param positions   die Positionen im Text
 * @param editor      der zuständige Editor
 * @returns {Array}   die erzeugten Markierungen im Editor
 */
function setMarkers(positions, editor) {
    var markers = [];
    if (REC.exist(positions)) {
        /*
         * var cssText = ".ace-chrome .ace_marker-layer .ace_step1 {background:
         * rgb(252, 0, 0);}"; var cssClass = "ace-chrome"; var dom =
         * require("ace/lib/dom"); dom.importCssString(cssText, cssClass);
         */
        var markerId;
        for ( var i = 0; i < positions.length; i++) {
            var pos = positions[i];
            var r = new Range(pos.startRow, pos.startColumn, pos.endRow, pos.endColumn);
            if (pos.type)
                markerId = editor.getSession().addMarker(r, "ace_selection", pos.desc, false);
            else
                markerId = editor.getSession().addMarker(r, "ace_step", pos.desc, false);
            markers.push(markerId);
        }
    }
    return markers;
}


/**
 * entfernt die Markierungen im Editor
 * @param markers  die Markierungen
 * @param editor   der verwendete Editor
 */
function removeMarkers(markers, editor) {
    for ( var i = 0; i < markers.length; i++) {
        editor.getSession().removeMarker(markers[i]);
    }
}

/**
 * zeigt die verwendete Regel
 * @param position die Position der Regel im Text
 */
function setXMLPosition(position) {
    rulesEditor.getSession().foldAll(1);
    var text = rulesEditor.getSession().getValue();
    var pos = 0;
    for ( var i = 0; i < position.length; i++)
        pos = text.indexOf("<archivTyp name=\"" + position[i] + "\"", pos);
    if (pos != -1) {
        pos1 = text.indexOf("</archivTyp>", pos);
        if (pos1 != -1) {
            var p = REC.convertPosition(text, pos, pos1 + 12, "");
            rulesEditor.getSession().unfold(p.startRow + 1, true);
            rulesEditor.gotoLine(p.startRow + 1);
            rulesEditor.selection.setSelectionRange(new Range(p.startRow, p.startColumn, p.endRow, p.endColumn));
        }
    }
}

/**
 * gibt die Ergebnisse im entsprechenden Fenster aus
 * @param results
 * @returns {string}
 */
function printResults(results) {
    var ret = "";
    var blanks = "                                               ";
    var maxLength = 0;
    for (key in results) {
        if (key.length > maxLength)
            maxLength = key.length;
    }
    maxLength++;
    for (key in results) {
        if (REC.exist(results[key])) {
            ret = ret + key + blanks.substr(0, maxLength - key.length) + ": " + results[key].getValue();
            if (REC.exist(results[key].expected)) {
                var tmp = eval(results[key].expected);
                if (REC.exist(results[key].getValue()) && tmp.valueOf() == results[key].getValue().valueOf())
                    ret = ret + " [OK]";
                else
                    ret = ret + " [FALSE] " + tmp;
            }
            ret = ret + "\n";
        }
    }
    return ret;
}

/**
 * gibt die Meldungen im entsprechenden Fenster aus
 * die Meldungen werden auf 2 verschiedene Arten verarbeitet:
 *  die Meldung kommt als Text dann wird ein Zeitstempel und ein Log-Level hinzugefügt
 *  oder die Meldung ist ein fertiges Array, dann werden sie einfach angehängt
 * @param message   die Message als String oder als Array von Strings
 * @param reverse   die Reihenfolge wird umgedreht
 * @param level     ein LogLevel
 */
function fillMessageBox(message, reverse, level) {
    var output;
    var ident = "                    ";
    var tmp = [];
    var out = [];
    var pos = 0;
    if (typeof message == "string") {
        if (!REC.exist(level))
            level = INFORMATIONAL;
        tmp.pop(REC.dateFormat(new Date(), "G:i:s,u") + " " + level.text + " " + message);
    }
    else {
        tmp = message;
    }
    message = [];
    for (var j = 0; j < tmp.length; j++) {
        var zeile = tmp[j];
        var z = zeile.split("\n");
        var i = 0;
        for (var k = 0; k < z.length; k++) {
            var z1 = z[k];
            if (i == 0 && z1.length > 0) {
                pos = z1.indexOf(" ", z1.indexOf(" ") + 1) + 1;
                out.push(z1);
                i++;
            } else if (z1.length > 0)
                out.push(ident.substr(0, pos) + z1);
        }
        message.push(out.join("\n"));
        out = [];
    }
    messages = messages.concat(message);
    if (reverse) {
        output = messages.reverse().join("\n");
    } else {
        output = messages.join("\n");
    }
    outputEditor.getSession().setValue(output);
}

/**
 * gibt einen aktuellen Timestamp zurück "m/d/yy h:MM:ss TT"
 * @param withDate mit Datum
 * @type {Date}
 */
function timeStamp(withDate) {
    var returnString = "";
    var now = new Date();
    var time = [ now.getHours(), now.getMinutes(), now.getSeconds() ];
    for ( var i = 1; i < 3; i++ ) {
        if ( time[i] < 10 ) {
            time[i] = "0" + time[i];
        }
    }
    if (withDate){
        var date = [ now.getMonth() + 1, now.getDate(), now.getFullYear() ];
        returnString = returnString + date.join(".") + " ";
    }
    returnString = returnString +   time.join(":");
    return returnString;
}

/**
 * löscht den Inhalt des Meldungsfensters
 */
function clearMessageBox(){
    outputEditor.getSession().setValue("");
}

/**
 * stellt die Funktionalität für den Zurück Button zur Verfügung
 */
function doBack() {
    try {
        multiMode = true;
        showMulti = false;
        textEditor.getSession().setValue("");
        clearMessageBox();
        propsEditor.getSession().setValue("");
        rulesEditor.getSession().foldAll(1);
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * Fuktionalität für den Test Button
 */
function doTest() {
    try {
        if (isLocal()) {
            var open = openFile("test.txt");
            var content = open;
            REC.currentDocument.setContent(content);
            REC.currentDocument.name = "TEST";
            document.getElementById('headerWest').textContent = "TEST";
            removeMarkers(markers, textEditor);
            textEditor.getSession().setValue(content);
            if (!currentRules.endsWith("test.xml")) {
                open = openFile("test.xml");
                currentRules = open;
                rulesEditor.getSession().setValue(open);
                document.getElementById('headerCenter').textContent = "Regeln (test.xml)";
            }
            REC.testRules(rulesEditor.getSession().getValue());
            setXMLPosition(REC.currXMLName);
            markers = setMarkers(REC.positions, textEditor);
            propsEditor.getSession().setValue(printResults(REC.results));
            fillMessageBox(REC.getMessage(), true);
            testMode = true;
            manageControls();
        } else {
            var dataString = {
                "function": "doTest",
                "fileName": "test.txt",
                "filePath": "test.xml"
            };
            $.ajax({
                type: "POST",
                data: dataString,
                datatype: "json",
                url: "/TestVerteilung/VerteilungServlet",
                error: function (response) {
                    try {
                        var r = jQuery.parseJSON(response.responseText);
                        message("Fehler", "Fehler: " + r.Message + "<br>StackTrace: " + r.StackTrace + "<br>ExceptionType: " + r.ExceptionType);
                    } catch (e) {
                        var str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for (var prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        message("Fehler", str + "<br>" + response.responseText);
                    }
                },
                success: function (data) {
                    if (data.success[0]) {
                        REC.currentDocument.setContent(data.result[0].text.toString());
                        removeMarkers(markers, textEditor);
                        textEditor.getSession().setValue(data.result[0].text.toString());
                        currentRules = "test.xml";
                        document.getElementById('headerCenter').textContent = "Regeln (test.xml)";
                        rulesEditor.getSession().setValue(data.result[0].xml.toString());
                        REC.testRules(rulesEditor.getSession().getValue());
                        setXMLPosition(REC.currXMLName);
                        markers = setMarkers(REC.positions, textEditor);
                        propsEditor.getSession().setValue(printResults(REC.results));
                        fillMessageBox(REC.getMessage(), true);
                        testMode = true;
                        manageControls();
                    } else
                        message("Fehler", "Fehler: " + data.result[0]);
                }
            });
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * schliesst die Testanzeige
 */
function closeTest() {
    try {
        testMode = false;
        textEditor.getSession().setValue(currentContent);
        propsEditor.getSession().setValue("");
        outputEditor.getSession().setValue("");
        document.getElementById('headerWest').textContent = currentFile;
        openRules();
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * Funktionalität für den Run Button
 */
function work() {
    try {
        var selectMode = false;
        if (multiMode)
            doReRunAll();
        else {
            var range = rulesEditor.getSelectionRange();
            var sel = rulesEditor.getSession().getTextRange(range);
            if (sel.length > 0) {
                if (!sel.startsWith("<")) {
                    var start = rulesEditor.find('<', {
                        backwards: true,
                        wrap: false,
                        caseSensitive: false,
                        wholeWord: false,
                        start: range,
                        regExp: false
                    });
                    range.setStart(start.start);
                }
                if (!sel.endsWith("/>")) {
                    var end = rulesEditor.find('>', {
                        backwards: false,
                        wrap: false,
                        caseSensitive: false,
                        wholeWord: false,
                        start: new Range(range.end.row, range.end.column, range.end.row, range.end.column),
                        regExp: false
                    });
                    range.setEnd(end.end);
                }
                sel = rulesEditor.getSession().getTextRange(range);
                if (!sel.endsWith("/>")) {
                    var tmp = sel.substring(1, sel.indexOf(" "));
                    tmp = "</" + tmp + ">";
                    end = rulesEditor.find(tmp, {
                        backwards: false,
                        wrap: false,
                        caseSensitive: false,
                        wholeWord: false,
                        start: new Range(range.end.row, range.end.column, range.end.row, range.end.column),
                        regExp: false
                    });
                    range.setEnd(end.end);
                }
                rulesEditor.selection.setSelectionRange(range);
                sel = rulesEditor.getSession().getTextRange(range);
                if (!sel.startsWith("<tags") && !sel.startsWith("<category") && !sel.startsWith("<archivPosition")) {
                    selectMode = true;
                    if (!sel.startsWith("<searchItem ")) {
                        start = rulesEditor.find('<searchItem', {
                            backwards: true,
                            wrap: false,
                            caseSensitive: false,
                            wholeWord: false,
                            start: range,
                            regExp: false
                        });
                        range.setStart(start.start);
                        end = rulesEditor.find('</searchItem>', {
                            backwards: false,
                            wrap: false,
                            caseSensitive: false,
                            wholeWord: false,
                            start: new Range(range.end.row, range.end.column, range.end.row, range.end.column),
                            regExp: false
                        });
                        range.setEnd(end.end);
                        rulesEditor.selection.setSelectionRange(range);
                        sel = rulesEditor.getSession().getTextRange(range);
                    }
                    if (!sel.startsWith("<archivTyp "))
                        sel = "<archivTyp name='' searchString=''>" + sel;
                    if (!sel.endsWith("</archivTyp>"))
                        sel = sel + "</archivTyp>";
                    if (!sel.startsWith("<documentTypes "))
                        sel = "<documentTypes>" + sel;
                    if (!sel.endsWith("</documentTypes>"))
                        sel = sel + "</documentTypes>";
                } else
                    sel = rulesEditor.getSession().getValue();
            } else
                sel = rulesEditor.getSession().getValue();
            REC.init();
            REC.currentDocument.properties.content.write(new Content(textEditor.getSession().getValue()));
            REC.currentDocument.name = currentFile;
            removeMarkers(markers, textEditor);
            REC.testRules(sel);
            if (!selectMode)
                setXMLPosition(REC.currXMLName);
            markers = setMarkers(REC.positions, textEditor);
            fillMessageBox(REC.getMessage(), true);
            propsEditor.getSession().setValue(printResults(REC.results));
            document.getElementById('inTxt').style.display = 'block';
            document.getElementById('dtable').style.display = 'none';
        }
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * aktualisiert die geänderten Regeln auf dem Server
 * @returns {boolean}  liefert true zurück, wenn alles geklappt hat
 */
function sendRules() {
    try {
        var erg = false;
        if (currentRules.endsWith("doc.xml")) {
            vkbeautify.xml(rulesEditor.getSession().getValue());
            var json = executeService("updateDocument", [
                {"name": "documentId", "value": rulesID},
                {"name": "documentText", "value": rulesEditor.getSession().getValue()},
                {"name": "mimeType", "value": "application/xml"},
                {"name": "extraProperties", "value": ""},
                {"name": "majorVersion", "value": ""},
                {"name": "versionComment", "value": ""}
            ], "Regeln konnten nicht übertragen werden:");
            if (json.success) {
                fillMessageBox("Regeln erfolgreich zum Server übertragen!");
                erg = true;
            }
            return erg;
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * liest die Regeln entweder vom Server oder lokal von der Platte
 * @param rulesId   die DokumentenId der Regeln auf dem Server
 * @param loadLocal legt fest, ob lokal oder vom Server gelesen werden soll
 */
function getRules(rulesId, loadLocal) {
    try {
        var ret;
        if (loadLocal) {
            var open = openFile("doc.xml");
            rulesEditor.getSession().setValue(open);
            rulesEditor.getSession().foldAll(1);
            fillMessageBox("Regeln erfolgreich lokal gelesen!");
        } else {
            var json = executeService("getDocumentContent", [
                {"name": "documentId", "value": rulesID},
                {"name": "extract", "value": "false"}
            ], "Regeln konnten nicht gelesen werden:");
            if (json.success) {
                rulesEditor.getSession().setValue(json.result);
                rulesEditor.getSession().foldAll(1);
                fillMessageBox("Regeln erfolgreich vom Server übertragen!");
            } else
                message("Fehler", "Fehler bei der Übertragung: " + json.result);
        }
        currentRules = "doc.xml";
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * Öffnet die Regeln
 */
function openRules() {
    var id;
    try {
        if (rulesID != null && typeof rulesID =="string") {
            id = rulesID.substring(rulesID.lastIndexOf('/') + 1);
            getRules(id, !alfrescoServerAvailable);
            document.getElementById('headerCenter').textContent = "Regeln (Server: doc.xml)";
        } else {
            if (isLocal()) {
                getRules("doc.xml", true);
            } else {
                $.get('doc.xml', function (msg) {
                    rulesEditor.getSession().setValue((new XMLSerializer()).serializeToString($(msg)[0]));
                    rulesEditor.getSession().foldAll(1);
                    currentRules = "doc.xml";
                });
            }
            document.getElementById('headerCenter').textContent = "Regeln (doc.xml)";
            //	window.parent.frames.rules.rulesEditor.getSession().setValue("Regeln konnten nicht geladen werden!");
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert die Regeln
 */
function format() {
    try {
        var xml = rulesEditor.getSession().getValue();
        xml = vkbeautify.xml(xml);
        rulesEditor.getSession().setValue(xml);
        // window.parent.frames.rules.rulesEditor.getSession().foldAll(1);
        if (typeof currXMLName != "undefined" && currXMLName != null) {
            setXMLPosition(currXMLName);
            markers = setMarkers(positions, textEditor);
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert das Script
 */
function formatScript() {
    try {
        var txt = textEditor.getSession().getValue();
        txt = js_beautify(txt);
        textEditor.getSession().setValue(txt);
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * öffnet eine lokale Datei
 * @param file  der Name der Datei
 * @returns den Inhalt
 */
function openFile(file) {
    try {
        var name = convertPath(file);
        var json = executeService("openFile", [
            {"name": "filePath", "value": name}
        ], "Datei konnte nicht geöffnet werden:");
        if (json.success) {
            fillMessageBox("Datei " + name + " erfolgreich geöffnet!");
            return atob(json.result);
        }
        else
            return "";
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * konvertiert den Pfad in einen absoluten Pfad
 * @param name
 * @returns {string}
 */
function convertPath(name) {
    return "file://" + window.location.pathname.substring(0, window.location.pathname.lastIndexOf("/") + 1) + name;
}

/**
 * sichert einen Text in eine Datei
 * @param file         die zu erzeugende Datei
 * @param text         der in die Datei zu speichernde Text
 * @returns {boolean}  true, wenn alles geklappt hat
 */
function save(file, text) {
    try {
        var name =  convertPath(file);
        var json = executeService("saveToFile", [
            {"name": "filePath", "value": name},
            {"name": "documentText", "value": text}
        ], "Skript konnte nicht gespeichert werden:");
        if (json.success)
            fillMessageBox(file + " erfolgreich gesichert!");
        return json.success;
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * Eventhandler der die Verarbeitung von fallen gelassen Dateien auf den Regelbereich zuständig ist
 * @param evt  das Event
 */
function handleRulesSelect(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    var files = evt.dataTransfer.files;
    for ( var i = 0; i < files.length; i++) {
        var f = files[i];
        if (f) {
            var r = new FileReader();
            r.onload = function(e) {
                var contents = e.target.result;
                rulesEditor.getSession().setValue(contents);
                rulesEditor.getSession().foldAll(1);
            };
            r.readAsText(f);
        } else {
            message("Fehler", "Failed to load file!");
        }
    }
}


/**
 * lädt das Verteilungsscript entweder lokal oder vom Server
 */
function loadScript() {
    try {
        panelSizeReminder = verteilungLayout.state.west.size;
        verteilungLayout.sizePane("west", "100%");
        oldContent = textEditor.getSession().getValue();
        var content;
        if (REC.exist(modifiedScript) && modifiedScript - length > 0) {
            content = modifiedScript;
        } else {
            if (REC.exist(scriptID)) {
                var json = executeService("getDocumentContent", [
                    {"name": "documentId", "value": scriptID},
                    {"name": "extract", "value": "false"}
                ], "Skript konnte nicht gelesen werden:");
                if (json.success) {
                    content = json.result;
                    workDocument = "recognition.js";
                    eval(content);
                    REC = new REC();
                    removeMarkers(markers, textEditor);
                    textEditor.getSession().setMode(new jsMode());
                    textEditor.getSession().setValue(content);
                    textEditor.setShowInvisibles(false);
                    scriptMode = true;
                    manageControls();
                }
            }
            else {
                $.get('recognition.js', function (msg) {
                    textEditor.getSession().setMode(new jsMode());
                    textEditor.getSession().setValue(msg);
                    textEditor.setShowInvisibles(false);
                    scriptMode = true;
                    manageControls();
                });
            }
        }
    } catch (e) {
        errorHandler(e);
        verteilungLayout.sizePane("west", layoutState.west.size);
    }
}


/**
 * lädt ein geändertes Verteilungsscript in den Kontext der Anwendung, damit die Änderungen wirksam werden
 */
function reloadScript() {
    try {
        modifiedScript = textEditor.getSession().getValue();
        eval(modifiedScript);
        REC.set(REC);
        fillMessageBox("Script erfolgreich aktualisiert");
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * lädt das Verteilungsscript vom Server
 */
function getScript() {
    try {
        var json = executeService("getDocumentContent", [
            {"name": "documentId", "value": scriptID},
            {"name": "extract", "value": "false"}
        ], "Skript konnte nicht gelesen werden:");
        if (json.success) {
            save(workDocument, textEditor.getSession().getValue());
            textEditor.getSession().setValue(json.result);
            fillMessageBox("Script erfolgreich heruntergeladen!");
        }
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * sendet das Script zum Server
 * @returns {boolean}  true, wenn alles geklappt hat
 */
function sendScript() {
    try {
        var erg = false;
        if (workDocument.endsWith("recognition.js")) {
            var json = executeService("updateDocument", [
                {"name": "documentId", "value": scriptID},
                {"name": "documentText", "value": textEditor.getSession().getValue()},
                {"name": "mimetype", "value": "application/javascript"},
                {"name": "extraProperties", "value": ""},
                {"name": "majorVersion", "value": ""},
                {"name": "versionComment", "value": ""}
            ], "Skript konnte nicht zum Server gesendet werden:");
            erg = json.success;
            if (erg)
                fillMessageBox("Script erfolgreich zum Server gesendet!");
        }
        return erg;
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * sendet ein Dokument zur Inbox
 */
function sendToInbox() {

    var json = executeService("createDocument", [
        {"name": "documentId", "value": inboxID},
        { "name": "fileName", "value": currentFile},
        { "name": "documentContent", "value": currentContent, "type": "byte"},
        { "name": "documentType", "value": "application/pdf"},
        { "name": "extraCMSProperties", "value": ""},
        { "name": "versionState", "value": "none"}
    ], ["Dokument konnte nicht auf den Server geladen werden:", "Dokument " + name + " wurde erfolgreich in die Inbox verschoben!"]);
}

/**
 * schliesst den Scripteditor
 */
function closeScript() {
    try {
        verteilungLayout.sizePane("west", panelSizeReminder);
        textEditor.getSession().setMode(new txtMode());
        if (REC.exist(oldContent) && oldContent.length > 0)
            textEditor.getSession().setValue(oldContent);
        else
            textEditor.getSession().setValue("");
        textEditor.setShowInvisibles(true);
        scriptMode = false;
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

//noinspection JSUnusedGlobalSymbols
/**
 * konvertiert einen String zu einem Bytearray
 * @param str        der zu konvertierende String
 * @returns {Array}  der String als Bytearray
 */
function stringToBytes(str) {
    var ch, st, re = [], j = 0;
    for ( var i = 0; i < str.length; i++) {
        ch = str.charCodeAt(i);
        if (ch < 127) {
            re[j++] = ch & 0xFF;
        } else {
            st = [];
            // clear stack
            do {
                st.push(ch & 0xFF);
                // push byte to stack
                ch = ch >> 8;
                // shift value down by 1 byte
            } while (ch);
            // add stack contents to result
            // done because chars have "wrong" endianness
            st = st.reverse();
            for ( var k = 0; k < st.length; ++k)
                re[j++] = st[k];
        }
    }
    // return an array of bytes
    return re;
}

/**
 * prüft und baut das Alfresco Environment auf.
 * @return true, wenn alles geklappt hat
 * TODO Regel für die Inbox
 */
function checkAndBuidAlfrescoEnvironment() {
    var ret;
    // prüfen, ob Server ansprechbar ist
    if (exist(getSettings("server")))
        alfrescoServerAvailable = checkServerStatus(getSettings("server"));
    // falls ja, dann Server Parameter eintragen
    if (alfrescoServerAvailable) {
        var erg;
        var extraProperties;
        erg = executeService("setParameter", [
            {"name": "server", "value": getSettings("server")},
            {"name": "binding", "value": getSettings("binding")},
            {"name": "user", "value": getSettings("user")},
            {"name": "password", "value": getSettings("password")}
        ], "Parameter für die Services konnten nicht gesetzt werden:");
        // Verteilskript prüfen
        if (erg.success) {
            erg = executeService("getNodeId", [
                {"name": "filePath", "value": "/Datenverzeichnis/Skripte/recognition.js"}
            ], null, true);
            if (!erg.success) {
                erg = executeService("getNodeId", [
                    {"name": "filePath", "value": "/Datenverzeichnis/Skripte"}
                ], null, true);
                scriptFolderId = reg.result;
            }
            if (!erg.success) {
                var script = openFile('src/main/javascript/recognition.js');
                if (exist(script)) {
                    erg = executeService("createDocument", [
                        {"name": "documentId", "value": scriptFolderId},
                        {"name": "fileName", "value": "recognition.js"},
                        {"name": "documentText", "value": btoa(script)},
                        {"name": "mimeType", "value": "application/x-javascript"},
                        {"name": "extraProperties", "value": "{P:cm:titled':{'cm:description':'Skript zum Verteilen der Dokumente'}}"},
                        {"name": "versionState", "value": "major"}
                    ], "Verteilungsskript konnte nicht erstellt werden!");
                    if (erg.success)
                        scriptID = $.parseJSON(erg.result).objectId;
                }
            } else {
                scriptID = erg.result;
            }
            if (erg.success) {
                // Regeln prüfen
                erg = executeService("getNodeId", [
                    {"name": "filePath", "value": "/Datenverzeichnis/Skripte/doc.xml"}
                ], null, true);
                if (!erg.success) {
                    var doc = openFile('doc.xml');
                    if (exist(doc)) {
                        erg = executeService("createDocument", [
                            {"name": "documentId", "value":scriptFolderId},
                            {"name": "fileName", "value": "doc.xml"},
                            {"name": "documentText", "value": btoa(doc)},
                            {"name": "mimeType", "value": "application/xml"},
                            {"name": "extraProperties", "value": "{P:cm:titled':{'cm:description':'Dokument mit den Verteil-Regeln'}}"},
                            {"name": "versionState", "value": "major"}

                        ], "Verteilungsregeln konnten nicht erstellt werden!");
                        if (erg.success)
                            rulesID = $.parseJSON(erg.result).objectId;
                    }
                } else {
                    rulesID = erg.result;
                }
                if (erg.success) {
                    // Archiv prüfen
                    erg = executeService("getNodeId", [
                        {"name": "filePath", "value": "/Archiv"}
                    ], null, true);
                    if (!erg.success) {
                        erg = executeService("getNodeId", [
                            {"name": "filePath", "value": "/"}
                        ], "Archiv konnte nicht gefunden werden:");
                        if (erg.success) {
                            extraProperties = "{'cmis:folder': {'cmis:objectTypeId': 'cmis:folder', 'cmis:name': 'Archiv'}, 'P:cm:titled':{'cm:title': 'Archiv', 'cm:description':'Der Archiv Root Ordner'}}";
                            erg = executeService("createFolder", [
                                {"name": "documentId", "value": erg.result},
                                {"name": "fileName", "value": "Archiv"}
                            ]);
                        }
                        if (erg.success)
                            erg = executeService("getNodeId", [
                                {"name": "filePath", "value": archivFolderId}
                            ], "Archiv konnte nicht gefunden werden:");
                        archivFolderId = erg.result;
                    } else {
                        archivFolderId = erg.result;
                    }
                    if (erg.success) {
                        // Archiv Root prüfen
                        erg = executeService("getNodeId", [
                            {"name": "filePath", "value": "/Archiv/Dokumente"}
                        ], null, true);
                        if (!erg.success) {
                            extraProperties = "{'cmis:folder': {'cmis:objectTypeId': 'cmis:folder', 'cmis:name': 'Dokumente'}, 'P:cm:titled':{'cm:title': 'Dokumente', 'cm:description':'Der Ordner für die abgelegten Dokumente'}}"
                            erg = executeService("createFolder", [
                                {"name": "documentId", "value": archivFolderId},
                                {"name": "fileName", "value": extraProperties}
                            ]);
                            if (erg.success) {
                                erg = executeService("getNodeId", [
                                    {"name": "filePath", "value": "/Dokumente"}
                                ], "Archiv Root konnte nicht gefunden werden:");
                                if (erg.success)
                                    rootID = erg.result;
                            }
                        } else {
                            rootID = erg.result;
                        }
                    }
                    if (erg.success) {
                        // Inbox prüfen
                        erg = executeService("getNodeId", [
                            {"name": "filePath", "value": "/Archiv/Inbox"}
                        ], null, true);
                        if (!erg.success) {
                            extraProperties = "{'cmis:folder': {'cmis:objectTypeId': 'cmis:folder', 'cmis:name': 'Inbox'}, 'P:cm:titled':{'cm:title': 'Inbox', 'cm:description':'Der Poseingangsordner'}}"
                            erg = executeService("createFolder", [
                                {"name": "documentId", "value": archivFolderId},
                                {"name": "fileName", "value": extraProperties}
                            ]);
                            if (erg.success) {
                                erg = executeService("getNodeId", [
                                    {"name": "filePath", "value": "/Archiv/Inbox"}
                                ], "Inbox konnte nicht gefunden werden:");
                                if (erg.success)
                                    inboxID = erg.result;
                            }
                        } else {
                            inboxID = erg.result;
                        }
                    }
                    if (erg.success) {
                        // Fehlerbox prüfen
                        erg = executeService("getNodeId", [
                            {"name": "filePath", "value": "/Archiv/Fehler"}
                        ], null, true);
                        if (!erg.success) {
                            extraProperties = "{'cmis:folder': {'cmis:objectTypeId': 'cmis:folder', 'cmis:name': 'Fehler'}, 'P:cm:titled':{'cm:title': 'Fehler', 'cm:description':'Der Ordner für nicht verteilbare Dokumente'}}"
                            erg = executeService("createFolder", [
                                {"name": "documentId", "value": archivFolderId},
                                {"name": "fileName", "value": extraProperties}
                            ]);

                            if (erg.success)
                                erg = executeService("getNodeId", [
                                    {"name": "filePath", "value": "/Archiv/Fehler"}
                                ], "Verzeichnis für fehlerhafte Dokumente konnte nicht gefunden werden:");
                            fehlerFolderId = erg.result;
                        } else {
                            fehlerFolderId = erg.result;
                        }
                    }
                    if (erg.success) {
                        // Unbekanntbox prüfen
                        erg = executeService("getNodeId", [
                            {"name": "filePath", "value": "/Archiv/Unbekannt"}
                        ], null, true);
                        if (!erg.success) {
                            extraProperties = "{'cmis:folder': {'cmis:objectTypeId': 'cmis:folder', 'cmis:name': 'Unbekannt'}, 'P:cm:titled':{'cm:title': 'Unbekannt', 'cm:description':'Der Ordner für unbekannte Dokumente'}}"
                            erg = executeService("createFolder", [
                                {"name": "documentId", "value": archivFolderId},
                                {"name": "fileName", "value":extraProperties}
                            ]);
                            if (erg.success)
                                erg = executeService("getNodeId", [
                                    {"name": "filePath", "value": "/Archiv/Unbekannt"}
                                ], "Verzeichnis für unbekannte Dokumente konnte nicht gefunden werden:");
                        }
                    }
                    if (erg.success) {
                        // Doppelte Box prüfen
                        erg = executeService("getNodeId", [
                            {"name": "filePath", "value": "/Archiv/Fehler/Doppelte"}
                        ], null, true);
                        if (!erg.success) {
                            extraProperties = "{'cmis:folder': {'cmis:objectTypeId': 'cmis:folder', 'cmis:name': 'Doppelte'}, 'P:cm:titled':{'cm:title': 'Doppelte', 'cm:description':'Der Ordner für doppelte Dokumente'}}"
                            erg = executeService("createFolder", [
                                {"name": "documentId", "value": fehlerFolderId},
                                {"name": "fileName", "value": extraProperties}
                            ]);
                            if (erg.success)
                                erg = executeService("getNodeId", [
                                    {"name": "filePath", "value": "/Archiv/Fehler/Doppelte"}
                                ], "Verzeichnis für doppelte Dokumente konnte nicht gefunden werden:");
                        }
                    }
                }
            }
        }
        tabLayout.tabs("option", "active", 0);
        ret = erg.success;
    } else {
        tabLayout.tabs({ disabled: [ 0,1 ] });
        ret = true;
    }
    return ret;
}

/**
 * initialisiert die Anwendung
 */
function init() {
    try {
        // Settings schon vorhanden?
        if (!exist(getSettings("server")) || !exist(getSettings("binding")) || !exist(getSettings("user")) || !exist(getSettings("password"))) {
            var cookie = $.cookie("settings");
            // prüfen, ob ein Cookie vorhanden ist
            if (REC.exist(cookie)) {
                // Cookie ist vorhanden, also die Daten aus diesem verwenden
                settings = $.parseJSON(cookie);
            } else {
                settings = {};
                settings.settings = [];
                // Settings aus test.properties laden. Das wird nur lokal mit Applet funktionieren
                var obj = executeService("loadProperties", [
                        {"name": "filePath", "value": convertPath("../test.properties")}
                    ],
                    "", true);
                if (obj.success) {
                    // Datei wurde gefunden. Settings setzen, aber nur wenn diese noch nicht über URL Parameter gesetzt worden sind.
                    if (!exist(getSettings("server")))
                        settings.settings.push({"key": "server", "value": obj.result.server});
                    if (!exist(getSettings("user")))
                        settings.settings.push({"key": "user", "value": obj.result.user});
                    if (!exist(getSettings("password")))
                        settings.settings.push({"key": "password", "value": obj.result.password});
                    if (!exist(getSettings("binding")))
                        settings.settings.push({"key": "binding", "value": obj.result.bindingUrl});
                }
            }
        }
        checkAndBuidAlfrescoEnvironment();
        openRules();
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

var Range = require("ace/range").Range;
var markers = [];
var results = [];
var oldContent = null;
var modifiedScript = null;

