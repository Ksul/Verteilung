/**
 * Created by m500288 on 11.09.14.
 */

/**
 * Prüft, ob ein Alfresco Server antwortet
 * @param url         URL des Servers
 * @returns {boolean} true, wenn der Server verfügbar ist
 */
function checkServerStatus(url) {

    var obj = executeService("isURLAvailable", null, [{"name":"server", "value":url}, {"name":"timeout", "value":"5000"}], null, true);
    return obj.data.toString() == "true";
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
        else {
            if (!settings)
                settings = {settings:[]};
            settings.settings.push({"key": key, "value": urlPar});
        }
    }
    return settings.settings.filter(function (o) {
        return o.key.indexOf(key) >= 0;
    })[0].value;
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
    returnString += time.join(":");
    return returnString;
}


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
        return decodeURIComponent(results[1]);
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
 * setzt die Alfresco Parameter im Applet
 * @return       true   Operation war erfolgreich
 *               false  Operation war nicht erfolgreich
 */
function setAppletParameter(){
    var json = executeService("setParameter", null, [
        {"name": "server", "value": server},
        {"name": "binding", "value": binding},
        {"name": "user", "value": user},
        {"name": "password", "value": password}
    ], "Alfresco Parameter konnten nicht im Applet gesetzt werden:");
    return json.success;
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
 * @return {Array}   der String als Bytearray
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
 * @return  {string}
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





