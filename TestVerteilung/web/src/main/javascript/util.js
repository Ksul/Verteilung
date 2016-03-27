/**
 * Created by m500288 on 11.09.14.
 */

/**
 * such eine Key / Value Kombination aus einer Json Struktur
 * @param obj           das zu durchsuchende JSON Objekt
 * @param key           der Key
 * @param val           der Wert
 * @return {Array}      das Ergebnis
 */
function searchJson(obj, key, val) {
    var objects = [];
    for (var i in obj) {
        if (!obj.hasOwnProperty(i)) continue;
        if (typeof obj[i] == 'object') {
            objects = objects.concat(searchJson(obj[i], key, val));
        } else
//if key matches and value matches or if key matches and value is not passed (eliminating the case where key matches but passed value does not)
        if (i == key && obj[i] == val || i == key && val == '') { //
            objects.push(obj);
        } else if (obj[i] == val && key == ''){
//only add if the object is not already in the array
            if (objects.lastIndexOf(obj) == -1){
                objects.push(obj);
            }
        }
    }
    return objects;
}

/**
 * ändert das CSS für einen bestimmte Class
 * @param className   der Name der Class
 * @param classValue  der neue Wert
 */
function changeCss(className, classValue) {
    var cssMainContainer = $('#css-modifier-container');
    if (cssMainContainer.length == 0) {
        var cssMainContainer = $('<div id="css-modifier-container"></div>');
        cssMainContainer.hide();
        cssMainContainer.appendTo($('body'));
    }
    classContainer = cssMainContainer.find('div[data-class="' + className + '"]');
    if (classContainer.length == 0) {
        classContainer = $('<div data-class="' + className + '"></div>');
        classContainer.appendTo(cssMainContainer);
    }
    classContainer.html('<style>' + className + ' {' + classValue + '}</style>');
};

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
 * parst einen Alfresco Datums String
 * @param dateString   der Datumsstring
 */
function parseDate(dateString) {
    try {
        var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        var parts = dateString.split(" ");
        var month = parts[1];
        var day = parts [2];
        var year = parts[5];
        var timeString = parts[3];
        parts = timeString.split(":");
        var hours = parts[0];
        var minutes = parts[1];
        var seconds = parts[2];
        var date = new Date(year, months.indexOf(month), day, hours, minutes, seconds, 0);
        return date;
    } catch(e) {
        return null;
    }
}

/**
 * liefert das aktuelle Tagesdatum
 * @param formatString der String zum formatieren
 * @return {number}
 */
function getCurrentDate(formatString) {
    {
        return REC.dateFormat(new Date(), formatString)
    }
}

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
 * erstellt einen vollständigen Pfad zum übegebenen Dateipfad
 * @param filePath     der übergebene Dateipfad
 * @returns {String}   den kompletten Pfad zur Datei auf dem Serveer
 */
function createPathToFile(filePath) {
    var file = document.URL;
    var parts = file.split("/").reverse();
    parts.splice(0,1);
    if (!filePath.startsWith("/"))
        filePath = "/" + filePath;
    return parts.reverse().join("/") + filePath;
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
 * prüft, ob eine Variable vorhanden ist
 * @param val   die zu prüfende Variable
 * @returns {boolean}    true, wenn sie vorhanden ist
 */
function exist(val) {
    return typeof val != "undefined" && val != null;
}

/**
 * Globale Fehlerroutine
 * @param e             der auslösende Fehler
 * @param description   optionale Fehlermeldung
 */
function errorHandler(e, description) {
    var str;
    if (exist(description))
        str = description + "<br>FEHLER:<br>";
    else
        str = "FEHLER:<br>";
    str = str + e.toString() + "<br>";
    for (var prop in e)
        str = str + "property: " + prop + " value: [" + e[prop] + "]<br>";
    str = str + "Stacktrace: <br>" + e.stack;
    message("Fehler", str);
}

/**
 * zeigt eine Meldung
 * @param title      Titel des Fensters
 * @param str        Meldungstext
 * @param autoClose  Wert für den Timeout beim automatischen Schliessen der Message
 * @param height     Höhe des Gensters
 * @param width      Breite des Fensters
 * TODO Message für einfachen Dialog mit Ja/Nein oder Ok/Cancel aufbohren
 */
function message(title, str, autoClose, height, width) {
    if (!exist(height))
        height = 200;
    if (!exist(width))
        width = 800;
    var dialogSettings = {
        autoOpen: false,
        title: title,
        modal: true,
        height: height,
        width: width
    };
    var div = $("<div></div>");
    if (exist(autoClose)) {
       dialogSettings.open = function(event, ui){
           setTimeout("$('#messageBox').dialog('close')",autoClose);
       }
    } else {
        dialogSettings.buttons =  {
            "Ok": function () {
                $(this).dialog("destroy");
                div.remove();
            }
        }
    }

    var $dialog = div.html(str).dialog(dialogSettings).css({height:height+"px", width:width+"px", overflow:"auto"});
    $dialog.dialog('open');
}

/**
 * lädt das Applet
 * @param level         der Level für die Log Ausgaben
 * @param server        der Alfresco Server
 * @param bindingUrl    die Binding Url für den Alfresco Server (optional)
 * @param user          der User für den Alfresco Server (optional)
 * @param password      das Password für den Alfresco Server (optional)
 * @returns             true, wenn das Applet geladen werden konnte, ansonsten false
 */
function loadApplet(level, server, bindingUrl, user, password) {
    if (isLocal()) {
        var obj = document.createElement('applet');
        var param;
        obj.setAttribute('name', 'reader');
        obj.setAttribute('id', 'reader');
        obj.setAttribute('width', '1');
        obj.setAttribute('height', '1');
        obj.setAttribute('codebase', './WEB-INF/lib');
        if (typeof level != "undefined" && level != null ){
            param = document.createElement( "param" );
            param.setAttribute('name', 'debug');
            param.setAttribute('value', level);
            obj.appendChild(param);
        }
        if (typeof server != "undefined" && server != null ){
            param = document.createElement( "param" );
            param.setAttribute('name', 'server');
            param.setAttribute('value', server);
            obj.appendChild(param);
        }
        if (typeof bindingUrl != "undefined" && bindingUrl != null ){
            param = document.createElement( "param" );
            param.setAttribute('name', 'url');
            param.setAttribute('value', bindingUrl);
            obj.appendChild(param);
        }
        if (typeof user != "undefined" && user != null ){
            param = document.createElement( "param" );
            param.setAttribute('name', 'user');
            param.setAttribute('value', user);
            obj.appendChild(param);
        }
        if (typeof password != "undefined" && password != null ){
            param = document.createElement( "param" );
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
                'commons-jcs-core-2.0-beta-1.jar, ' +
                'alfresco-opencmis-extension-1.0.jar, ' +
                'chemistry-opencmis-client-api-0.13.0.jar, ' +
                'chemistry-opencmis-client-bindings-0.13.0.jar, ' +
                'chemistry-opencmis-client-impl-0.13.0.jar, ' +
                'chemistry-opencmis-commons-api-0.13.0.jar, ' +
                'chemistry-opencmis-commons-impl-0.13.0.jar, ' +
                'stax2-api-3.1.4.jar, ' +
                'woodstox-core-asl-4.4.0.jar');
        obj.setAttribute('code', 'de.schulte.testverteilung.VerteilungApplet.class');
        document.getElementById('appl').appendChild(obj);
        var app =  $('#reader').get(0);
        return (app.isActive != null && app.isActive());
    }
}

/**
 * konvertiert Bytes zu einem String
 * @param hexx          die zu konvertierenden Bytes
 * @return {string}     der Ergebnis String
 */
function hex2String(hexx) {
    var hex = hexx.toString(); //force conversion
    var str = '';
    for (var i = 0; i < hex.length; i += 2)
        str += String.fromCharCode(parseInt(hex.substr(i, 2), 16));
    return str;
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
                for ( var index = 0; index < params.length; ++index) {
                    // falls Baytecode übertragen werden soll, dann Umwandlung damit es nicht zu Konvertierungsproblemen kommt
                    if (exist(params[index].type) && params[index].type == "byte")
                       // params[index].value = base64EncArr(strToUTF8Arr(params[index].value));
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
                for (var index = 0; index < params.length; ++index) {
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
            if (exist(json.error)) {
                errorString = errorString + "<br>" + json.error;
                REC.log(ERROR, json.error);
            }
            REC.log(ERROR, json.result);
            fillMessageBox(true);
            throw new Error(errorString);
        } else {
            if (exist(successMessage)) {
                REC.log(INFORMATIONAL, successMessage);
                fillMessageBox(true);
            }
        }
        return json;
    } catch (e) {
        var p = "Service: " + service + "<br>";
        if (exist(params)) {
            for (var index = 0; index < params.length; ++index) {
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


