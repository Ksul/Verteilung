/**
 * prüft, ob ein String mit einem Vergleichsstring endet
 * @param str               der Vergleichsstring
 * @returns {boolean}
 */
String.prototype.endsWith = function (str) {
    return (this.match(str + "$") == str);
};

/**
 * prüft, ob ein String mit einem Vergleichsstring beginnt
 * @param str               der Vergleichsstring
 * @returns {boolean}
 */
String.prototype.startsWith = function (str) {
    return (this.match("^" + str) == str);
};

/**
 * liefert einen Url-Paramter
 * @param name          der Name des Parameters
 * @returns {String}
 */
function getUrlParam(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(this.location.href);
    if (results == null)
        return null;
    else
        return results[1];
}

/**
 * Prüft, ob die Anwendung lokal mit Applet läuft
 * @returns {*}
 */
function isLocal() {
    return (location.href.startsWith("file"));
}


/**
 * Prüft, ob ein URL Parameter vorhanden ist
 * @returns {boolean}
 */
function hasUrlParam(){
    return this.location.href.search(/\?/) != -1;
}

/**
 * Globale Fehlerroutine
 * @param e  der auslösende Fehler
 */
function errorHandler(e) {
    var str = "FEHLER:\n";
    str = str + e.toString() + "\n";
    for (var prop in e)
        str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
    alert(str);
}


/**
 * lädt das Applet
 */
function loadApplet(level, bindingUrl, user, password) {
    if (isLocal()) {
        var obj = document.createElement('applet');
        obj.setAttribute('name', 'reader');
        obj.setAttribute('id', 'reader');
        obj.setAttribute('width', '1');
        obj.setAttribute('height', '1');
        obj.setAttribute('codebase', './WEB-INF/lib');
        if (typeof level != "undefined" && level != null ){
            var param = document.createElement( "param" );
            param.setAttribute('name', 'debug');
            param.setAttribute('value', level);
            obj.appendChild(param);
        }
        if (typeof bindingUrl != "undefined" && bindingUrl != null ){
            var param = document.createElement( "param" );
            param.setAttribute('name', 'url');
            param.setAttribute('value', bindingUrl);
            obj.appendChild(param);
        }
        if (typeof user != "undefined" && user != null ){
            var param = document.createElement( "param" );
            param.setAttribute('name', 'user');
            param.setAttribute('value', user);
            obj.appendChild(param);
        }
        if (typeof password != "undefined" && password != null ){
            var param = document.createElement( "param" );
            param.setAttribute('name', 'password');
            param.setAttribute('value', password);
            obj.appendChild(param);
        }
        obj.setAttribute('archive',
            'vt.jar, ' +
                'pdfbox-1.6.0.jar, ' +
                'plugin.jar, ' +
                'bcprov-jdk15on-150.jar, ' +
                'commons-codec-1.6.jar, ' +
                'commons-logging-1.1.1.jar, ' +
                'fontbox-1.6.0.jar, ' +
                'jempbox-1.6.0.jar, ' +
                'slf4j-api-1.7.5.jar, ' +
                'commons-io-2.4.jar,' +
                'alfresco-opencmis-extension-0.7.jar, ' +
                'chemistry-opencmis-client-api-0.10.0.jar, ' +
                'chemistry-opencmis-client-bindings-0.10.0.jar, ' +
                'chemistry-opencmis-client-impl-0.10.0.jar, ' +
                'chemistry-opencmis-commons-api-0.10.0.jar, ' +
                'chemistry-opencmis-commons-impl-0.10.0.jar');
        obj.setAttribute('code', 'de.schulte.testverteilung.VerteilungApplet.class');
        document.getElementById('appl').appendChild(obj);
    }
}

/**
 * Prüft, ob ein Alfresco Server antwortet
 * @param url         URL des Servers
 * @param proxy       Proxy Server
 * @param port        Port des Proxy Servers
 * @returns {boolean} true, wenn der Server verfügbar ist
 */
function checkServerStatus(url, proxy, port) {
    var ret;
    if (isLocal()){
        ret = document.reader.isURLAvailable(url,  proxy, port);
    }
    else {
        var dataString = {
            "function"  : "isURLAvailable",
            "server"    : url,
            "proxyHost" : proxy,
            "proxyPort" : port
        };
        $.ajax({
            type        : "POST",
            data        : dataString,
            datatype    : "json",
            cache       : false,
            async       : false,
            url         : "/TestVerteilung/VerteilungServlet",
            error    : function (response) {
                try {
                    var r = jQuery.parseJSON(response.responseText);
                    alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                } catch(e)  {
                    var str = "FEHLER:\n";
                    str = str + e.toString() + "\n";
                    for ( var prop in e)
                        str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                    alert(str + "\n" + response.responseText);
                }
            },
            success     : function(data) {
                ret = data.result.toString();
            }
        });
    }
    return ret == "true";
}

/**
 * liefert die Einstellungen
 * @param key    Schlüssel der Einstellung
 * @returns {*}  Den Wert der Einstellung
 */
function getSettings(key){
    var ret;
    if (settings.settings.filter(function(o) {return o.key.indexOf(key) >= 0;}).length == 0){
        var urlPar = getUrlParam(key);
        if (urlPar == null)
            return null;
        else
            settings.settings.push({"key":key, "value":urlPar});
    }
    return settings.settings.filter(function(o) {return o.key.indexOf(key) >= 0;})[0].value;
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
 * Ãffnet ein PDF
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
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                        } catch (e) {
                            var str = "FEHLER:\n";
                            str = str + e.toString() + "\n";
                            for (var prop in e)
                                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                            alert(str + "\n" + response.responseText);
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
 * @param txt        der Inhalt des Dokumentes in Textform
 * @param name       der Name des Dokumentes
 * @param typ        der Dokuemttyp  (wird der eigentlich noch gebraucht)
 * @param container  ???
 */
function loadText(txt, name, typ, container) {
    try {
        multiMode = false;
        currentFile = name;
        currentContent = txt;
        currentContainer = container;
        removeMarkers(markers, textEditor);
        textEditor.getSession().setValue(txt);
        document.getElementById("headerWest").firstChild.nodeValue = name;
        fillMessageBox("", false);
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
 * @param txt               Textinhalt des Dokumentes
 * @param name              Name des Dokumentes
 * @param typ               Typ des Dokumentes
 * @param notDeleteable     Merker, ob das Dokument gelöscht werden kann (geht nur bei lokalen)
 * @param alfContainer      ???
 * @param container         ???
 */
function loadMultiText(txt, name, typ,  notDeleteable, alfContainer, container) {
    try {
        multiMode = true;
        var dat = new Array();
        REC.currentDocument.setContent(txt);
        REC.testRules(rulesEditor.getSession().getValue());
        dat["text"] = txt;
        dat["file"] = name;
        dat["log"] = REC.getMessage();
        dat["result"] = REC.results;
        dat["position"] = REC.positions;
        dat["xml"] = REC.currXMLName;
        dat["typ"] = typ;
        dat["error"] = REC.errors;
        dat["container"] = container; name,
            dat["notDeleteable"] = notDeleteable;
        dat["alfContainer"] = alfContainer;
        daten[name] = dat;
        var ergebnis = new Array();
        ergebnis["error"] = REC.errors.length > 0;
        var row = [ null,name,  REC.currXMLName.join(" : "), ergebnis, uuid(), REC.errors ];
        tabelle.fnAddData(row);
        manageControls();
    } catch (e) {
        var str = "FEHLER:\n";
        str = str + e.toString() + "\n";
        for ( var prop in e)
            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
        fillMessageBox(str, false);
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
        fillMessageBox("", false);
        tabelle.fnClearTable();
        daten = new Array();
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
                            if (evt.target.readyState == FileReader.DONE) {// DONE == 2
                                var str = btoa(evt.target.result);
                                if (isLocal()) {
                                    // den Inhalt häppchenweise übertragen.
                                    for (var k = 0; k < Math.ceil(str.length / maxLen); k++)
                                        document.reader.fillParameter(str.substr(k * maxLen, maxLen), k == 0);
                                    var json = jQuery.parseJSON(document.reader.extractPDF(theFile.name, files.length > 1, theFile.type));
                                    if (!json.success) {
                                    if (count == 1)
                                        loadText(json.result, theFile.name, theFile.type, null);
                                    else {
                                        loadMultiText(json.result, theFile.name, theFile.type, "false", "false", null);
                                    }
                                    }
                                } else {
                                    var dataString = {
                                        "function": "extractPDFToInternalStorage",
                                        "documentText": str,
                                        "fileName": theFile.name,
                                        "clear": clear
                                    };
                                    $.ajax({
                                        type: "POST",
                                        data: dataString,
                                        datatype: "json",
                                        async: false,
                                        url: "/TestVerteilung/VerteilungServlet",
                                        error: function (response) {
                                            try {
                                                var r = jQuery.parseJSON(response.responseText);
                                                alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                                            } catch (e) {
                                                var str = "FEHLER:\n";
                                                str = str + e.toString() + "\n";
                                                for (var prop in e)
                                                    str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                                                alert(str + "\n" + response.responseText);
                                            }
                                        },
                                        success: function (data) {
                                            if (data.success[0]) {
                                                if (count == 1)
                                                    loadText(data.result[0].toString(), theFile.name, theFile.type, null);
                                                else {
                                                    loadMultiText(data.result[0].toString(), theFile.name, theFile.type, "false", "false", null);
                                                }
                                            } else
                                                alert("Fehler beim Lesen des PDF: " + data.result[0]);
                                        }
                                    });
                                }
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
                            if (evt.target.readyState == FileReader.DONE) {// DONE == 2
                                var str = btoa(evt.target.result);
                                if (isLocal()) {
                                    // den Inhalt häppchenweise übertragen
                                    for (var k = 0; k < Math.ceil(str.length / maxLen); k++)
                                        document.reader.fillParameter(str.substr(k * maxLen, maxLen), k == 0);
                                    count = count + document.reader.extractZIP(theFile.name) - 1;
                                } else {
                                    var dataString = {
                                        "function": "extractZIPToInternalStorage",
                                        "documentText": str
                                    };
                                    $.ajax({
                                        type: "POST",
                                        data: dataString,
                                        datatype: "json",
                                        url: "/TestVerteilung/VerteilungServlet",
                                        error: function (response) {
                                            try {
                                                var r = jQuery.parseJSON(response.responseText);
                                                alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                                            } catch (e) {
                                                var str = "FEHLER:\n";
                                                str = str + e.toString() + "\n";
                                                for (var prop in e)
                                                    str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                                                alert(str + "\n" + response.responseText);
                                            }
                                        },
                                        success: function (data) {
                                            if (data.success[0]) {
                                                if (data.result[0].entry.length == 1)
                                                    loadText(data.result[0].entry[0].result.toString(), data.result[0].entry[0].entryFileName.toString(), "application/zip", null);
                                                else {
                                                    for (var index = 0; index < data.result[0].entry.length; index++) {
                                                        var array_element = data.result[0].entry[index];
                                                        loadMultiText(array_element.result.toString(), array_element.entryFileName.toString(), "application/zip", "true", "false", null);
                                                    }
                                                }
                                            } else
                                                alert("ZIP Datei konnte nicht entpackt werden: " + data.result[0]);
                                        }
                                    });
                                }
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
                                loadText(e.target.result, theFile.name, theFile.mozFullPath, theFile.type);
                            };
                        })(f);
                    } else {
                        r.onload = (function (theFile) {
                            return function (e) {
                                loadMultiText(e.target.result, theFile.name, theFile.mozFullPath, theFile.type, "false", "false", null);
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
 * handelt die Clicks auf die Icons in der Tabelle
 */
function handleImageClicks() {
    $(document).on("click", ".run",function () {
        var aPos = tabelle.fnGetPosition(this.parentNode.parentNode);
        var row = tabelle.fnGetData(aPos[0]);
        var name = row[1];
        REC.currentDocument.setContent(daten[name]["text"]);
        REC.testRules(rulesEditor.getSession().getValue());
        daten[name].log = REC.mess;
        daten[name].result = results;
        daten[name].position = REC.positions;
        daten[name].xml = REC.currXMLName;
        daten[name].error = REC.errors;
        var ergebnis = new Array();
        ergebnis["error"] = REC.errors.length > 0;
        row[2] = REC.currXMLName.join(" : ");
        row[3] = ergebnis;
        row[5] = REC.errors;
        if (tabelle.fnUpdate(row, aPos[0]) > 0)
            alert("Tabelle konnte nicht aktualisiert werden!");
    });
    $(document).on("click", ".glass", function () {
        var aPos = tabelle.fnGetPosition(this.parentNode.parentNode);
        var row = tabelle.fnGetData(aPos[0]);
        var name = row[1];
        multiMode = false;
        showMulti = true;
        currentFile = daten[name]["file"];
        document.getElementById('headerWest').textContent = currentFile;
        setXMLPosition(daten[name]["xml"]);
        removeMarkers(markers, textEditor);
        markers = setMarkers(daten[name]["position"], textEditor);
        textEditor.getSession().setValue(daten[name]["text"]);
        propsEditor.getSession().setValue(printResults(daten[name]["result"]));
        fillMessageBox(daten[name]["log"], true);
        manageControls();
    });
    $(document).on("click", ".loeschen", function () {
        var answer = confirm("Eintrag löschen?");
        if (answer) {
            var aPos = tabelle.fnGetPosition(this.parentNode.parentNode);
            var row = tabelle.fngetData(aPos[0]);
            var name = row[1];
            try {
                netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
            } catch (e) {
                alert("Permission to delete file was denied.");
            }
            currentFile = daten[name]["file"];
            textEditor.getSession().setValue("");
            propsEditor.getSession().setValue("");
            fillMessageBox("", false);
            rulesEditor.getSession().foldAll(1);
            if (currentFile.length > 0) {
                var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
                file.initWithPath(currentFile);
                if (file.exists() == true)
                    file.remove(false);
            }
            tabelle.fnDeleteRow(aPos[0]);
        }
    });
    $(document).on("click", ".pdf", function (name) {
        var aPos = tabelle.fnGetPosition(this.parentNode.parentNode);
        var row = tabelle.fnGetData(aPos[0]);
        var name = row[1];
        if (daten[name]["alfContainer"].toLowerCase() == "true" && typeof daten[name]["container"] != "undefined" && daten[name]["container"] != null) {
            openPDF(daten[name]["container"], true);
        } else {
            openPDF(daten[name]["file"]);
        }
    });
    $(document).on("click", ".moveToInbox", function () {
        var aPos = tabelle.fnGetPosition(this);
        var row = tabelle.fnGetData(aPos[0]);
        var name = row[1];
        var docId = "workspace:/SpacesStore/" + daten[name]["container"];
        if (isLocal()) {
            var json = jQuery.parseJSON(document.reader.moveDocument(docId, inboxID, getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"), null));
            if (!json.success)
                alert("Dokument nicht verschoben: " + json.result);
        }
        else {
            var dataString = {
                "function": "moveDocument",
                "documentId": docId,
                "destinationId": inboxID,
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
                async: false,
                error: function (response) {
                    try {
                        var r = jQuery.parseJSON(response.responseText);
                        alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                    } catch (e) {
                        var str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for (var prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        alert(str + "\n" + response.responseText);
                    }
                },
                success: function (data) {
                    if (data.success[0])
                        alert("Dokument verschoben!")
                    else
                        alert("Dokument nicht verschoben: " + data.result[0]);
                }
            });
        }
    });

}

/**
 * tauscht das Icon bei fehlerhaften Sätzen in der Tabelle aus und ermöglicht damit das Aufklappen der Zeile
 * @param o
 * @returns {string}
 */
function expandFieldFormatter(o){
    if (o.aData[3].error) {
        return '<a class="control" href="#"><img src="./resource/Details_open.png" title="Details anzeigen" width="20px" height="20px" /></a>';
    }
    return '<a class="nothing"/>';
}

/**
 * formatiert die Iconspalte in der Tabelle
 * @param o
 * @returns {string}
 */
function imageFieldFormatter(o) {
    if (o.iDataRow == 0) {
        //	o.cell.setStyle('width', '102px');
    }
    var container =  document.createElement("div");
    var image = document.createElement("div");
    image.href = "#";
    image.className = "run";
    if (o.aData[3].error) {
        image.style.backgroundImage = "url(resource/error.png)";
        image.title = "Verteilung fehlerhaft";
    } else {
        image.style.backgroundImage = "url(resource/ok.png)";
        image.title = "Verteilung erfolgreich";
    }
    image.style.cursor = "pointer";
    image.style.width = "16px";
    image.style.height = "16px";
    image.style.cssFloat = "left";
    image.style.marginRight = "5px";
    container.appendChild(image);
    image = document.createElement("div");
    image.href = "#";
    image.className = "glass";
    image.title = "Ergebnis anzeigen";
    image.style.backgroundImage = "url(resource/glass.png)";
    image.style.width = "16px";
    image.style.height = "16px";
    image.style.cursor = "pointer";
    image.style.cssFloat = "left";
    image.style.marginRight = "5px";
    container.appendChild(image);
    image = document.createElement("div");
    image.href = "#";
    image.className = "loeschen";
    image.title = "Ergebnis lÃÂ¶schen";
    if (daten[o.aData[1]]["notDeleteable"] != "true") {
        image.style.backgroundImage = "url(resource/delete.png)";
        image.style.cursor = "pointer";
    }
    else {
        image.style.backgroundImage = "url(resource/delete-bw.png)";
        image.style.cursor = "not-allowed";
    }
    image.style.width = "16px";
    image.style.height = "16px";
    image.style.cssFloat = "left";
    image.style.marginRight = "5px";
    container.appendChild(image);
    image = document.createElement("div");
    image.className = "pdf";
    if (o.aData[1].toLowerCase().endsWith(".pdf")) {
        image.style.backgroundImage = "url(resource/pdf.png)";
        image.style.cursor = "pointer";
    } else {
        image.style.backgroundImage = "url(resource/pdf-bw.png)";
        image.style.cursor = "not-allowed";
    }
    image.style.cssFloat = "left";
    image.style.width = "16px";
    image.style.height = "16px";
    image.style.marginRight = "5px";
    image.title = "PDF anzeigen";
    container.appendChild(image);
    image = document.createElement("div");
    image.className = "moveToInbox";
    if (daten[o.aData[1]]["alfContainer"] == "true") {
        image.style.backgroundImage = "url(resource/move-file.png)";
        image.style.cursor = "pointer";
    } else {
        image.style.backgroundImage = "url(resource/move-file-bw.png)";
        image.style.cursor = "not-allowed";
    }
    image.style.cssFloat = "left";
    image.style.width = "16px";
    image.style.height = "16px";
    // image.style.marginRight = "5px";
    image.title = "Zur Inbox verschieben";
    container.appendChild(image);
    return container.outerHTML;
}

/**
 * berechnet die Höhe für die Tabelle
 * @returns {string}
 */
function calcDataTableHeight()  {
    var h = verteilungLayout.state.west.innerHeight -68;
    return h + 'px';
};

/**
 * formatiert die Fehlerdetails in der zusätzlichen Zeile(n) der Tabelle
 * @param oTable
 * @param nTr
 * @param tableid
 * @returns {string}
 */
function formatDetails(oTable, nTr, tableid) {
    var oData = oTable.fnGetData(nTr);
    var sOut = '<div class="innerDetails" style="overflow: auto; width: 100%; " ><table>' +
        '<tr><tr style="height: 0px;" > '+
        '<th style="width: 100px; padding-top: 0px; padding-bottom: 0px; border-top-width: 0px; border-bottom-width: 0px; height: 0px; font-size: 12px"' +
        'colspan="1" rowspan="1" tabindex="0" class="control center">Fehler</th>' +
        '<th style="width: auto; padding-left: 10px; padding-top: 0px; padding-bottom: 0px; border-top-width: 0px; border-bottom-width: 0px; height: 0px; font-size: 12px"' +
        'colspan="1" rowspan="1" tabindex="0" class="alignLeft">Beschreibung</th></tr><td>';
    var txt = "<tr>";
    for ( var i = 0; i < oData[5].length; i++) {
        txt = txt + "<td class='alignCenter' style='font-size: 11px; padding-top: 0px; padding-bottom: 0px'>" + (i+1) + "</td><td style='font-size: 11px; padding-top: 0px; padding-bottom: 0px'>" + oData[5][i] + "</td>";
        txt = txt + "</tr>";
    }
    sOut = sOut + txt;
    sOut += '</table></div>';
    return sOut;
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
        fillMessageBox("", false);
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
            var ergebnis = new Array();
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
    var markers = new Array();
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
 * @param text
 * @param reverse
 */
function fillMessageBox(text, reverse) {
    if (reverse) {
        text = text.split('\n').reverse().join('\n');
        if (text.startsWith("\n"))
            text = text.substr(1);
    }
    outputEditor.getSession().setValue(text);
}

/**
 * stellt die Funktionalität für den Zurück Button zur Verfügung
 */
function doBack() {
    try {
        multiMode = true;
        showMulti = false;
        textEditor.getSession().setValue("");
        fillMessageBox("", false);
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
            var content = open[0];
            REC.currentDocument.setContent(content);
            REC.currentDocument.name = "TEST";
            document.getElementById('headerWest').textContent = "TEST";
            removeMarkers(markers, textEditor);
            textEditor.getSession().setValue(content);
            if (!currentRules.endsWith("test.xml")) {
                open = openFile("test.xml");
                currentRules = open[1];
                rulesEditor.getSession().setValue(open[0]);
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
                        alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                    } catch (e) {
                        var str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for (var prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        alert(str + "\n" + response.responseText);
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
                        alert("Fehler: " + data.result[0]);
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
            doReRunAll()
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
            REC.currentDocument.setContent(textEditor.getSession().getValue());
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
 * Öffnet den Einstellungsdialog
 */
function openSettings() {
    try {
        var serverInput = document.getElementById('server');
        serverInput.value = getSettings("server");
        var userInput = document.getElementById('user');
        userInput.value = getSettings("user");
        var passInput = document.getElementById('password');
        passInput.value = getSettings("password");
        var proxyInput = document.getElementById('proxy');
        proxyInput.value = getSettings("proxy");
        var portInput = document.getElementById('port');
        portInput.value = getSettings("port");
        $("#dialog-form").dialog("open");
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * aktualisiert die geänderten Regeln auf dem Server
 * @param dialog       Merker ob ein Hinweisfenster angezeigt werden soll
 * @returns {boolean}  liefert true zurück, wenn alles geklappt hat
 */
function sendRules(dialog) {
    try {
        var erg = false;
        if (currentRules.endsWith("doc.xml")) {
            vkbeautify.xml(rulesEditor.getSession().getValue());
            if (isLocal()) {
                var ret = document.reader.updateDocumentByFile(rulesID, currentRules,
                    "XML-Beschreibung der Dokumente", "application/xml", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"), null);
                if (dialog) {
                    if (ret == 200) {
                        alert("Regeln erfolgreich übertragen!");
                        erg = true;
                    } else
                        alert("Fehler bei der Übertragung: " + ret);
                }
            } else {
                var dataString = {
                    "function"     : "updateDocument",
                    "documentText" : rulesEditor.getSession().getValue(),
                    "description"  : "XML-Beschreibung der Dokumente",
                    "documentId"   : rulesID,
                    "mimeType"     : "application/xml",
                    "server"       : getSettings("server"),
                    "username"     : getSettings("user"),
                    "password"     : getSettings("password"),
                    "proxyHost"    : getSettings("proxy"),
                    "proxyPort"    : getSettings("port")
                };
                $.ajax({
                    type           : "POST",
                    data           : dataString,
                    datatype       : "json",
                    url            : "/TestVerteilung/VerteilungServlet",
                    error    : function (response) {
                        try {
                            var r = jQuery.parseJSON(response.responseText);
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                        } catch(e)  {
                            var str = "FEHLER:\n";
                            str = str + e.toString() + "\n";
                            for ( var prop in e)
                                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                            alert(str + "\n" + response.responseText);
                        }
                    },
                    success        : function(data) {
                        if (data.success[0]){
                            if (dialog)
                                alert("Regeln erfolgreich übertragen!");
                        } else
                            alert("Fehler beim Übertragen der Regeln: " + data.result[0]);
                    }
                });
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
 * @param dialog    legt fest, ob ein Hinweisdialog angezeigt werden soll
 */
function getRules(rulesId, loadLocal, dialog) {
    try {
        if (isLocal()) {
            var ret;
            if (loadLocal) {
                var open = openFile("doc.xml");
                rulesEditor.getSession().setValue(open[0]);
                rulesEditor.getSession().foldAll(1);
            } else {
                ret = document.reader.getContent(rulesId, false, getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"), null);
                var json = jQuery.parseJSON(ret);
                if (json.success) {
                    rulesEditor.getSession().setValue(json.result);
                    rulesEditor.getSession().foldAll(1);
                    if (dialog)
                        alert("Regeln erfolgreich übertragen!");
                } else
                    alert("Fehler bei der Übertragung: " + json.result);
            }
        } else {
            var dataString = {
                "function"   : "getContent",
                "documentId" : rulesId,
                "extract"    : "false",
                "server"     : getSettings("server"),
                "username"   : getSettings("user"),
                "password"   : getSettings("password"),
                "proxyHost"  : getSettings("proxy"),
                "proxyPort"  : getSettings("port")
            };
            $.ajax({
                type         : "POST",
                data         : dataString,
                datatype     : "json",
                url          : "/TestVerteilung/VerteilungServlet",
                success      : function(data) {
                    if (data.success[0]) {
                        if (dialog)
                            alert("Regeln erfolgreich übertragen!");
                        rulesEditor.getSession().setValue(data.result[0].toString());
                        rulesEditor.getSession().foldAll(1);
                    } else
                        alert("Regeln konnten nicht Übertragen werden: " + data.result[0]);
                },
                error    : function (response) {
                    try {
                        var r = jQuery.parseJSON(response.responseText);
                        alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                    } catch(e)  {
                        var str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for ( var prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        alert(str + "\n" + response.responseText);
                    }
                }
            });
        }
        currentRules = "doc.xml";
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * Ãffnet die Regeln
 */
function openRules() {
    var id;
    try {
        if (rulesID != null) {
            id = rulesID.substring(rulesID.lastIndexOf('/') + 1);
            getRules(id, !alfrescoServerAvailable, false);
            document.getElementById('headerCenter').textContent = "Regeln (Server: doc.xml)";
        } else {
            if (isLocal()) {
                getRules("doc.xml", true, false);
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
 * @param name  der Name der Datei
 * @returns den Inhalt
 */
function openFile(name) {
    try {
        var contents = "";
        return document.reader.openFile(convertPath(name));
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
 * @param dialog       legt fest, ob ein Hinweis angezeigt werden soll
 * @returns {boolean}  true, wenn alles geklappt hat
 */
function save(file, text, dialog) {
    try {
        var ret = true;
        document.reader.save(file, text)
        if (dialog)
            alert(file + " erfolgreich gesichert!");
        return ret;
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
            alert("Failed to load file!");
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
            if (isLocal()) {
                var open = openFile("recognition.js");
                content = open[0];
                workDocument = open[1];
                eval(content);
                REC = new Recognition();
                REC.set(REC);
                removeMarkers(markers, textEditor);
                textEditor.getSession().setMode(new jsMode());
                textEditor.getSession().setValue(content);
                textEditor.setShowInvisibles(false);
                scriptMode = true;
                manageControls();
            } else {
                if (REC.exist(scriptID)) {
                    var dataString = {
                        "function": "getContent",
                        "documentId": scriptID,
                        "extract": "false",
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
                                alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                            } catch (e) {
                                var str = "FEHLER:\n";
                                str = str + e.toString() + "\n";
                                for (var prop in e)
                                    str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                                alert(str + "\n" + response.responseText);
                            }
                        },
                        success: function (data) {
                            if (data.success[0]) {
                                content = data.result[0].toString();
                                workDocument = "recognition.js";
                                eval(content);
                                REC = new Recognition();
                                REC.set(REC);
                                removeMarkers(markers, textEditor);
                                textEditor.getSession().setMode(new jsMode());
                                textEditor.getSession().setValue(content);
                                textEditor.setShowInvisibles(false);
                                scriptMode = true;
                                manageControls();
                            } else
                                alert("Script konnte nicht gefunden werden: " + data.result[0]);
                        }
                    });
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
        }
    } catch (e) {
        var str = "FEHLER:\n";
        str = str + e.toString() + "\n";
        for (var prop in e)
            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
        alert(str);
        verteilungLayout.sizePane("west", layoutState.west.size);
    }
}


/**
 * lädt ein geändertes Verteilungsscript in den Kontext der Anwendung, damit die Ãnderungen wirksam werden
 * @param dialog   legt fest, ob ein Hinweis gezeigt werden soll
 */
function reloadScript(dialog) {
    try {
        modifiedScript = textEditor.getSession().getValue();
        eval(modifiedScript);
        REC = new Recognition();
        REC.set(REC);
        if (dialog)
            alert("Script erfolgreich aktualisiert");
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * lädt das Verteilungsscript vom Server
 * @param dialog  legt fest, ob ein Hinweis gezeigt werden soll
 */
function getScript(dialog) {
    try {
        if (isLocal()) {
            var ret = document.reader.getContent(scriptID, false, getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"),
                getSettings("port"), null);
            var json = jQuery.parseJSON(ret);
            if (json.success) {
                save(workDocument, textEditor.getSession().getValue(), false);
                textEditor.getSession().setValue(json.result);
                if (dialog)
                    alert("Script erfolgreich heruntergeladen!");
            } else
                alert("Fehler bei der Übertragung: " + json.result);
        } else {
            var dataString = {
                "function": "getContent",
                "documentId": scriptID,
                "extract": "false",
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
                        alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                    } catch (e) {
                        var str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for (var prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        alert(str + "\n" + response.responseText);
                    }
                },
                success: function (data) {
                    if (data.success[0]) {
                        if (dialog)
                            alert("Script erfolgreich heruntergeladen!");
                        textEditor.getSession().setValue(data.result.toString());
                    } else
                        alert("Script konnte nicht geladen werden: " + data.result[0]);
                }
            });
        }
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * sendet das Script zum Server
 * @param dialog       legt fest, ob ein Hinweis gezeigt werden soll
 * @returns {boolean}  true, wenn alles geklappt hat
 */
function sendScript(dialog) {
    try {
        var erg = false;
        if (workDocument.endsWith("recognition.js")) {
            if (isLocal()) {
                document.reader.save(workDocument, textEditor.getSession().getValue());
                var ret = document.reader.updateDocument(scriptID, textEditor.getSession().getValue(),
                    "VerteilungsScript", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"), null);
                if (dialog) {
                    if (ret == 200) {
                        alert("Script erfolgreich übertragen!");
                        erg = true;
                    } else
                        alert("Fehler bei der Übertragung: " + ret);
                }
            } else {
                var dataString = {
                    "function"     : "updateDocument",
                    "documentId"   : scriptID,
                    "documentText" : textEditor.getSession().getValue(),
                    "description"  : "VerteilungsScript",
                    "server"       : getSettings("server"),
                    "username"     : getSettings("user"),
                    "password"     : getSettings("password"),
                    "proxyHost"    : getSettings("proxy"),
                    "proxyPort"    : getSettings("port")
                };
                $.ajax({
                    type           : "POST",
                    data           : dataString,
                    datatype       : "json",
                    url            : "/TestVerteilung/VerteilungServlet",
                    error    : function (response) {
                        try {
                            var r = jQuery.parseJSON(response.responseText);
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                        } catch(e)  {
                            var str = "FEHLER:\n";
                            str = str + e.toString() + "\n";
                            for ( var prop in e)
                                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                            alert(str + "\n" + response.responseText);
                        }
                    },
                    success        : function(data) {
                        if (data.success[0]){
                            if (dialog)
                                alert("Script erfolgreich übertragen!");
                        } else
                            alert("Script konnte nicht Übertragen werden: " + data.result[0]);
                    }
                });
            }
            return erg;
        }
    } catch (e) {
        errorHandler(e);
    }
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
 * initialisiert die Anwendung
 */
function init() {
    try {
        var cookie = $.cookie("settings");
        if (REC.exist(cookie)) {
            settings = $.parseJSON(cookie);
        } else {
            settings = {};
            settings.settings = [];
            if (!hasUrlParam())
                settings.settings = [
                    {"key": "server", "value": "http://192.168.178.100:9080"},
                    {"key": "user", "value": "admin"},
                    {"key": "password", "value": "admin"},
                    {"key": "proxy", "value": ""},
                    {"key": "port", "value": ""}
                ];
        }
        if (REC.exist(getSettings("server")))
            alfrescoServerAvailable = checkServerStatus(getSettings("server"), getSettings("proxy"), getSettings("port"));
        if (alfrescoServerAvailable) {
            var txt = [];
            if (isLocal()) {
                var json;
                var pattern = new RegExp("true", "ig");
                if (getUrlParam("runLocal") != null || pattern.test(getUrlParam("runLocal"))) {
                    runLocal = true;
                } else {
                    json = jQuery.parseJSON(document.reader.getNodeId("SELECT cmis:objectId from cmis:document where cmis:name='recognition.js'", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"),
                        getSettings("port")));
                    if (json.success)
                        scriptID = json.result;
                    else
                        txt.push("Script nicht gefunden! Fehler: " + json.result);
                    json = jQuery.parseJSON(document.reader.getNodeId("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port")
                    ));
                    if (json.success)
                        rulesID = json.result;
                    else
                        txt.push("Regeln nicht gefunden! Fehler: " + json.result);
                    json = jQuery.parseJSON(document.reader.getNodeId("SELECT cmis:objectId from cmis:folder where cmis:name='Inbox'", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port")
                    ));
                    if (json.success)
                        inboxID = json.result;
                    else
                        txt.push("Inbox nicht gefunden! Fehler: " + json.result);
                    json = jQuery.parseJSON(document.reader.getNodeId("SELECT * from cmis:folder where CONTAINS('PATH:\"//app:company_home/cm:Archiv\"')", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port")
                    ));
                    if (json.success)
                        rootID = json.result;
                    else
                        txt.push("Archiv nicht gefunden! Fehler: " + json.result);
                }
            } else {
                var dataString = {
                    "function": "getNodeId",
                    "cmisQuery": "SELECT cmis:objectId from cmis:document where cmis:name='recognition.js'",
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
                    async: false,
                    error: function (response) {
                        try {
                            var r = jQuery.parseJSON(response.responseText);
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                        } catch (e) {
                            var str = "FEHLER:\n";
                            str = str + e.toString() + "\n";
                            for (var prop in e)
                                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                            alert(str + "\n" + response.responseText);
                        }
                    },
                    success: function (data) {
                        if (data.success[0])
                            scriptID = data.result[0];
                        else {
                            txt.push("Script nicht gefunden! " + data.result[0]);
                        }
                    }
                });
                var dataString = {
                    "function": "getNodeId",
                    "cmisQuery": "SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'",
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
                    async: false,
                    error: function (response) {
                        try {
                            var r = jQuery.parseJSON(response.responseText);
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                        } catch (e) {
                            var str = "FEHLER:\n";
                            str = str + e.toString() + "\n";
                            for (var prop in e)
                                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                            alert(str + "\n" + response.responseText);
                        }
                    },
                    success: function (data) {
                        if (data.success[0])
                            rulesID = data.result[0];
                        else {
                            txt.push("Regeln nicht gefunden! " + data.result[0]);
                        }
                    }
                });
                var dataString = {
                    "function": "getNodeId",
                    "cmisQuery": "SELECT cmis:objectId from cmis:folder where cmis:name='Inbox'",
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
                    async: false,
                    error: function (response) {
                        try {
                            var r = jQuery.parseJSON(response.responseText);
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                        } catch (e) {
                            var str = "FEHLER:\n";
                            str = str + e.toString() + "\n";
                            for (var prop in e)
                                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                            alert(str + "\n" + response.responseText);
                        }
                    },
                    success: function (data) {
                        if (data.success[0])
                            inboxID = data.result[0];
                        else {
                            txt.push("Inbox nicht gefunden! " + data.result[0]);
                        }
                    }
                });
                var dataString = {
                    "function": "getNodeId",
                    "cmisQuery": "SELECT * from cmis:folder where CONTAINS('PATH:\"//app:company_home/cm:Archiv\"')",
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
                    async: false,
                    error: function (response) {
                        try {
                            var r = jQuery.parseJSON(response.responseText);
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                        } catch (e) {
                            var str = "FEHLER:\n";
                            str = str + e.toString() + "\n";
                            for (var prop in e)
                                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                            alert(str + "\n" + response.responseText);
                        }
                    },
                    success: function (data) {
                        if (data.success[0])
                            rootID = data.result[0];
                        else {
                            txt.push("Archiv nicht gefunden! " + data.result[0]);
                        }
                    }
                });
            }
            if (txt.length > 0)
                alert(txt.join("\n"));
            $( "#tabs" ).tabs( "option", "active", 0 );
        } else {
            $( "#tabs" ).tabs({ disabled: [ 0 ] });
        }
        openRules();
        manageControls();
    } catch (e) {
        errorHandler(e);
    }
}

var Range = require("ace/range").Range;
var markers = new Array();
var results = new Array();
var oldContent = null;
var modifiedScript = null;
var searchCont = false;
var searchRules = false;
