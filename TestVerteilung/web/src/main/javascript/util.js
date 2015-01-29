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
 * @param title Titel des Fensters
 * @param str        Meldungstext
 * @param autoClose  Wert für den Timeout beim automatischen Schliessen der Message
 */
function message(title, str, autoClose) {
    var dialogSettings = {
        autoOpen: false,
        title: title,
        modal: true,
        height:200,
        width:800
    };
    if (exist(autoClose)) {
       dialogSettings.open = function(event, ui){
           setTimeout("$('#messageBox').dialog('close')",autoClose);
       }
    } else {
        dialogSettings.buttons =  {
            "Ok": function () {
                $(this).dialog("destroy");
            }
        }
    }

    var $dialog = $('#messageBox').html(str).dialog(dialogSettings).css({height:"200px", width:"800px", overflow:"auto"});
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
                'alfresco-opencmis-extension-0.7.jar, ' +
                'chemistry-opencmis-client-api-0.10.0.jar, ' +
                'chemistry-opencmis-client-bindings-0.10.0.jar, ' +
                'chemistry-opencmis-client-impl-0.10.0.jar, ' +
                'chemistry-opencmis-commons-api-0.10.0.jar, ' +
                'chemistry-opencmis-commons-impl-0.10.0.jar');
        obj.setAttribute('code', 'de.schulte.testverteilung.VerteilungApplet.class');
        document.getElementById('appl').appendChild(obj);
        var app =  $('#reader').get(0);
        return (app.isActive != null && app.isActive());
    }
}

