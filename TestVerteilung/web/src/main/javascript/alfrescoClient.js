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
 * baut das Layout der Anwendung auf
 */
function loadLayout() {
    var clientPageLayoutSettings = {
        name:                       "clientLayoutSettings",
        center__paneSelector:       "#clientPage",
        resizable:		    		false
    };

    var pageLayoutSettings = {
        name:                       "pageLayout",
        fxName:					    "slide",		// none, slide, drop, scale
        fxSpeed_open:		    	800,
        fxSpeed_close:		    	1000,
        fxSettings_open:	    	{ easing: "easeInQuint" },
        fxSettings_close:  		    { easing: "easeOutQuint" },
        spacing_open:		        8,
        spacing_closed:	           	12,
        closable:		    		true,
        resizable:		    		false,
        north:  {
            paneSelector:		    "#contentNorth",
            minSize:                33
        },
        center: {
            paneSelector:		    "#tabs"
        }
    };
    var contentLayoutSettings = {
        name:						"contentLayout",
        spacing_open:		        0,
        spacing_closed:		        12,
        resizable:			        false,
        closable:		    		false,
        north:  {
            paneSelector:		    "#tabButtons"
        },
        center: {
            paneSelector:		    "#tabPanels",
            //	center panel contains a Tabs widget, with a layout inside 1 or more tab-panels
            onresize:			    $.layout.callbacks.resizeTabLayout
        }
        //activate:                   $.layout.callbacks.resizeTabLayout
    };

    var alfrescoLayoutSettings = {
        name:                      "alfrescoLayout" ,
        size:					    "auto",
        minSize:				    13,
        initClosed:				    false,
        resizerTip:				    "Resize This Pane",
        fxName:					    "slide",
        fxSpeed_open:		    	800,
        fxSpeed_close:		    	1000,
        fxSettings_open:	    	{ easing: "easeInQuint" },
        fxSettings_close:  		    { easing: "easeOutQuint" },
        closable:		    		true,
        resizable:		    		true,
        //slidable:				true,
        livePaneResizing:	    	true,
        spacing_open:		    	8,
        spacing_closed:			    12,
        initPanes:	    			false,
        resizeWithWindow:			false,
        contentSelector:			".ui-widget-content",
        west: {
            paneSelector:           "#alfrescoWest",
            size:				    .2,
            fxSettings_open:	    { easing: "easeOutBounce" },
            closable:			    true,
            resizable:			    true,
            slidable:			    true
        },
        center: {
            paneSelector:           "#alfrescoCenter",
            initHidden:             false,
            minHeight:              80,
            size:                   .8,
            initClosed:             false,
            children: {
                name: "alfrescoIinnerCenterLayout",
                contentSelector: ".ui-widget-content",
                spacing_open: 8,
                spacing_closed: 12,
                north: {
                    paneSelector: "#alfrescoCenterInnerNorth",
                    size: .44,
                    onresize: function () {
                    }
                },
                center: {
                    size: "auto",
                    paneSelector: "#alfrescoCenterInnerCenter",
                    onresize: function () {
                    }
                }
            }
        },
        south:{
            paneSelector:           "#alfrescoSouth",
            size:                   0.17,
            contentSelector:	    ".ui-widget-content",
            resizable:			    true,
            slidable:			    true,
            spacing_open:		    8,
            spacing_closed:		    12
        },
        //	enable state management
        stateManagement__enabled:	true,
        showDebugMessages:			true
    };


    var verteilungLayoutSettings = {
        name:                      "verteilungLayout" ,
        size:					    "auto",
        minSize:				    13,
        initClosed:				false,
        resizerTip:				"Resize This Pane",
        fxName:					"slide",
        fxSpeed_open:		    	800,
        fxSpeed_close:		    	1000,
        fxSettings_open:	    	{ easing: "easeInQuint" },
        fxSettings_close:  		{ easing: "easeOutQuint" },
        closable:		    		true,
        resizable:		    		true,
        //slidable:				true,
        livePaneResizing:	    	true,
        spacing_open:		    	8,
        spacing_closed:			12,
        initPanes:	    			false,
        resizeWithWindow:			false,
        contentSelector:			".ui-widget-content",

        //	reference only - these options are NOT required because 'true' is the default

        //	some pane-size settings
        west: {
            paneSelector:           "#verteilungWest",
            size:				    .6,
            fxSettings_open:	    { easing: "easeOutBounce" },
            closable:			    true,
            resizable:			    true,
            slidable:			    true,
            onresize:	            function () { textEditor.resize(); $('div.dataTables_scrollBody').css('height',calcDataTableHeight()); tabelle.fnSettings()._iDisplayLength = Math.max(Math.floor((verteilungLayout.state.west.innerHeight - 24 - 26 - 20) / 29), 1); tabelle.fnDraw(); tabelle.fnAdjustColumnSizing(); }
        },
        center: {
            paneSelector:           "#verteilungCenter",
            initHidden:             false,
            minHeight:              80,
            size:                   .4,
            initClosed:             false,
            onresize:	            function () { rulesEditor.resize(); }

        },
        south:{
            paneSelector:           "#verteilungSouth",
            size:                   0.17,
            contentSelector:	    ".ui-widget-content",
            resizable:			    true,
            slidable:			    true,
            children: {
                name:				"innerLayout",
                contentSelector:	".ui-widget-content",
                spacing_open:		8,
                spacing_closed:		12,
                west:{
                    paneSelector:	"#verteilungSouthInnerWest",
                    size:           .74,
                    onresize:	    function () { outputEditor.resize();}
                },
                center:{
                    size:           "auto",
                    paneSelector:	"#verteilungSouthInnerCenter",
                    onresize:	    function () { propsEditor.resize();}
                }
            }
        },
        //	enable state management
        stateManagement__enabled:	true,
        showDebugMessages:			true
    };


    // create the tabs before the page layout because tabs will change the height of the north-pane
    $("#tabs").tabs({
        // using callback addon
        activate: $.layout.callbacks.resizeTabLayout,
        active: 1

        /* OR with a custom callback
         activate: function (evt, ui) {
         $.layout.callbacks.resizeTabLayout( evt, ui );
         // other code...
         }
         */
    });

    $('#tabs').layout( contentLayoutSettings);

    $('body').layout(clientPageLayoutSettings);

    $('#clientPage').layout(pageLayoutSettings);

    verteilungLayout = $('#tab2').layout(verteilungLayoutSettings);
    alfrescoLayout = $('#tab1').layout(alfrescoLayoutSettings);

    // if there is no state-cookie, then DISABLE state management initially
    var cookieExists = !$.isEmptyObject( verteilungLayout.readCookie() );
    if (!cookieExists) toggleStateManagement( true, false );

}

/**
 * baut die Alfresco Tabelle auf.
 */
function loadAlfrescoTable() {
    $('#dtable2').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="alfrescoTabelle"></table>' );
    alfrescoTabelle = $('#alfrescoTabelle').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aaData": [],
        "sScrollX": "100%",
        "sScrollXInner": "100%",
        // "sScrollY" : calcDataTableHeight(),
        "bAutoWidth": true,
        "bLengthChange": false,
        "bFilter": false,
        // "iDisplayLength": Math.max(Math.floor((verteilungLayout.state.west.innerHeight - 24 - 26 - 20) / 29), 1),
        "columns": [
            { "dataProp": null, "class": "control center", "width": "12px"},
            { "title": "Titel", "type": "string", "class": "alignLeft"  },
            { "title": "Datum", "type": "date", "class": "alignLeft" },
            { "title": "Person", "type": "string", "class": "alignLeft" },
            { "title": "Betrag", "type": "numeric", "class": "alignLeft" },
            { "title": "Schlüssel", "type": "string", "class": "alignLeft" },
            { "title": "Name", "type": "string", "class": "alignLeft" },
            { "title": "Beschreibung", "type": "string", "class": "alignLeft" },
            { "title": "Aktion", "width": "102px", "class": "alignLeft" },
            { "title": "Id" }
        ],
        "columnDefs": [
            // { "aTargets": [0], "fnRender": expandFieldFormatter, "bSortable": false},
            { "targets": [1, 2, 3, 4, 5], "visible": true},
            { "targets": [6, 7], "visible": false},
            { "targets": [8], "mRender": alfrescoAktionFieldFormatter, "sortable": false},
            { "targets": [9], "visible": false}
        ],
        "oLanguage": {
            "sInfo": "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_"
        }
    });
}

/**
 * baut die Alfresco Folder Tabelle auf.
 */
function loadAlfrescoFolderTable() {
    $('#dtable3').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="alfrescoFolderTabelle"></table>' );
    alfrescoFolderTabelle = $('#alfrescoFolderTabelle').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aaData": [],
        "sScrollX": "100%",
        "sScrollXInner": "100%",
        // "sScrollY" : calcDataTableHeight(),
        "bAutoWidth": true,
        "bLengthChange": false,
        "bFilter": false,
        // "iDisplayLength": Math.max(Math.floor((verteilungLayout.state.west.innerHeight - 24 - 26 - 20) / 29), 1),
        "columns": [
            { "dataProp": null, "class": "control center", "width": "12px"},
            { "title": "Name", "type": "string", "class": "alignLeft"  },
            { "title": "Beschreibung", "type": "string", "class": "alignLeft" },
            { "title": "Aktion", "width": "102px", "class": "alignLeft" },
            { "title": "Id" }
        ],
        "columnDefs": [
            // { "aTargets": [0], "fnRender": expandFieldFormatter, "bSortable": false},
            { "targets": [1, 2], "visible": true},
            { "targets": [3], "mRender": alfrescoFolderAktionFieldFormatter, "sortable": false},
            { "targets": [4], "visible": false}
        ],
        "oLanguage": {
            "sInfo": "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_"
        } ,
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
            // Cell click
            $('td', nRow).on('click', function() {
               switchAlfrescoDirectory( aData[4]);

            });
        }
    });
}

/**
 * lädt die Tabelle für den Verteilungstab
 */
function loadVerteilungTable() {
    $('#dtable').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="tabelle"></table>' );
    tabelle = $('#tabelle').dataTable({
        "bJQueryUI": true,
        "sPaginationType": "full_numbers",
        "aaData": [],
        "sScrollX": "100%",
        "sScrollXInner": "100%",
        "sScrollY" : calcDataTableHeight(),
        "bAutoWidth": true,
        "bLengthChange": false,
        "bFilter": false,
        "iDisplayLength": Math.max(Math.floor((verteilungLayout.state.west.innerHeight - 24 - 26 - 20) / 29), 1),
        "columns": [
            { "dataProp": null,"class": "control center", "width": "12px"},
            { "title": "Name", "type": "string", "class": "alignLeft"  },
            { "title": "Dokumenttyp", "type": "string", "class": "alignLeft" },
            { "title": "Ergebnis", "width": "102px", "class": "alignLeft" },
            { "title": "Id" },
            { "title": "Fehler" }
        ],
        "columnDefs": [
            { "targets": [0], "fnRender": expandFieldFormatter, "sortable": false},
            { "targets": [1,2,3], "visible": true},
            { "targets": [3], "fnRender": imageFieldFormatter, "sortable": false},
            { "targets": [4,5], "visible": false}
        ],
        "oLanguage": {
            "sInfo": "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_"
        }
    });
    $(document).on('click', '#tabelle td.control', function () {
        var nTr = this.parentNode;
        var i = $.inArray(nTr, anOpen);
        if (i === -1) {
            $('img', this).attr('src', "./resource/Details_close.png");
            var nDetailsRow = tabelle.fnOpen(nTr, formatDetails(tabelle, nTr, 1), 'details');
            $('div.innerDetails', nDetailsRow).slideDown('fast', function () {
                $("div.dataTables_scrollBody").scrollTop(nTr.offsetTop);
            });
            anOpen.push(nTr);
        }
        else {
            $('img', this).attr('src', "./resource/Details_open.png");
            $('div.innerDetails', $(nTr).next()[0]).slideUp(function () {
                tabelle.fnClose(nTr);
                anOpen.splice(i, 1);
            });
        }
    });
}

/**
 * berechnet die Höhe für die Tabelle
 * @returns {string}
 */
function calcDataTableHeight()  {
    var h = verteilungLayout.state.west.innerHeight -68;
    return h + 'px';
}

/**
 * formatiert die Iconspalte in der AlfrescoFolderTabelle
 * @param o
 * @returns {string}
 */
function alfrescoFolderAktionFieldFormatter(data, type, full) {
   // if (o.iDataRow == 0) {
        //	o.cell.setStyle('width', '102px');
  //  }
    var container =  document.createElement("div");
    var image = document.createElement("div");
    image.href = "#";
    image.className = "folderEdit";
    image.style.backgroundImage = "url(src/main/resource/images/file_edit.png)";
    image.title = "Details bearbeiten";
    image.style.cursor = "pointer";
    image.style.width = "16px";
    image.style.height = "16px";
    image.style.cssFloat = "left";
    image.style.marginRight = "5px";
    container.appendChild(image);
    image = document.createElement("div");
    image.href = "#";
    image.className = "folderOpen";
    image.title = "Ordner öffnen";
    image.style.backgroundImage = "url(src/main/resource/images/details_open.png)";
    image.style.width = "16px";
    image.style.height = "16px";
    image.style.cursor = "pointer";
    image.style.cssFloat = "left";
    image.style.marginRight = "5px";
    container.appendChild(image);
    return container.outerHTML;
}

/**
 * formatiert die Iconspalte in der AlfrescoTabelle
 * @param o
 * @returns {string}
 */
function alfrescoAktionFieldFormatter(data, type, full) {

    var container =  document.createElement("div");
    var image = document.createElement("div");
    image.href = "#";
    image.className = "detailEdit";
    image.style.backgroundImage = "url(src/main/resource/images/file_edit.png)";
    image.title = "Details bearbeiten";
    image.style.cursor = "pointer";
    image.style.width = "16px";
    image.style.height = "16px";
    image.style.cssFloat = "left";
    image.style.marginRight = "5px";
    container.appendChild(image);
    return container.outerHTML;
}

/**
 * führt die Aktualisierungen für eine Verzeichniswechsel im Alfresco durch
 * @param objectId  die Objectid des ausgewählten Folders
 */
function switchAlfrescoDirectory(objectId) {
    try {
        var json = executeService("listFolderAsJSON", [
            {"name": "filePath", "value": objectId},
            {"name": "withFolder", "value": -1}
        ], "Verzeichnis konnte nicht aus dem Server gelesen werden:");
        if (json.success) {
            alfrescoFolderTabelle.fnClearTable();
            for (var index = 0; index < json.result.length; ++index) {
                var name = json.result[index].attr.name ? json.result[index].attr.name : "";
                var description = json.result[index].attr.description ? json.result[index].attr.description : "";
                var id = json.result[index].attr.objectId ? json.result[index].attr.objectId : "";
                var row = [ null, name, description, null, id];
                alfrescoFolderTabelle.fnAddData(row);
            }
        }
        json = executeService("listFolderAsJSON", [
            {"name": "filePath", "value": objectId},
            {"name": "withFolder", "value": 1}
        ], "Dokumente konnten nicht aus dem Server gelesen werden:");
        if (json.success) {
            alfrescoTabelle.fnClearTable();
            for (var index = 0; index < json.result.length; ++index) {
                var titel = json.result[index].attr.title ? json.result[index].attr.title : json.result[index].attr.name ? json.result[index].attr.name : "";
                var datum = json.result[index].attr.documentDate ? json.result[index].attr.documentDate : json.result[index].attr.creationDate ? json.result[index].attr.creationDate : "";
                var date = parseDate(datum);
                var dateString = date ? REC.dateFormat(date, "dd.MM.YYYY") : "";
                var person = json.result[index].attr.person ? json.result[index].attr.person : "";
                var amount = json.result[index].attr.amount ? json.result[index].attr.amount : "";
                var schluessel = json.result[index].attr.idvalue ? json.result[index].attr.idvalue : "";
                var id = json.result[index].attr.objectId ? json.result[index].attr.objectId : "";
                var name = json.result[index].attr.name ? json.result[index].attr.name : "";
                var beschreibung = json.result[index].attr.description ? json.result[index].attr.description : "";
                var row = [ null, titel, dateString, person, amount, schluessel, name, beschreibung, null, id];
                alfrescoTabelle.fnAddData(row);
            }
        }

    } catch (e) {
        errorHandler(e);
    }
}
/**
 * lädt den Alfresco Tree
 */
function loadAlfrescoTree() {
    tree = $("#tree").jstree({
        "json_data": {
            "data": function (aNode, aFunction) {
                if (alfrescoServerAvailable) {
                    var json = executeService("listFolderAsJSON", [
                        {"name": "filePath", "value": aNode.attr ? aNode.attr("objectId") : "-1"},
                        {"name": "withFolder", "value": -1}
                    ], "Verzeichnis konnte nicht aus dem Server gelesen werden:");
                    if (json.success)
                        aFunction(json.result);
                    else
                        message("Fehler", "Folder konnte nicht erfolgreich im Alfresco gelesen werden!");
                }
            }
        },
        "plugins": [ "themes", "json_data", "ui", "crrm", "dnd", "search", "hotkeys", "themeroller"]
    }).bind("select_node.jstree",function (event, data) {
        if (data.rslt.obj.attr("rel") == "folder") {
            if (alfrescoServerAvailable) {
                switchAlfrescoDirectory(data.rslt.obj.attr("objectId"));
            }
        }
    }).delegate("a", "click", function (event, data) {
        event.preventDefault();
    });
}

function handleAlfrescoFolderImageClicks() {
    $(document).on("click", ".folderPpen", function () {
        try {
            var aPos = alfrescoFolderTabelle.fnGetPosition(this.parentNode.parentNode);
            var row = alfrescoFolderTabelle.fnGetData(aPos[0]);
            switchAlfrescoDirectory(row[4]);
        } catch (e) {
            errorHandler(e);
        }
     });
}


function handleAlfrescoImageClicks() {
    $(document).on("click", ".detailEdit", function () {
        try {
            var aPos = alfrescoTabelle.fnGetPosition(this.parentNode.parentNode);
            var row = alfrescoTabelle.fnGetData(aPos[0]);
            startDocumentDialog(row[9], row[6], row[1], row[7], row[3], row[4], row[2], row[5], null);
        } catch (e) {
            errorHandler(e);
        }
    });
}

/**
 * startet die Anwendung
 */
function start() {
    try {
        var erg = loadApplet();
        if (erg != null && !erg) {
            throw new Error("Applet konnte nicht geladen werden!");
        }
        document.getElementById('filesinput').addEventListener('change', readMultiFile, false);

        propsEditor = ace.edit("inProps");
        propsEditor.setReadOnly(true);
        propsEditor.renderer.setShowGutter(false);
        propsEditor.setShowPrintMargin(false);

        outputEditor = ace.edit("inOutput");
        outputEditor.setReadOnly(true);
        outputEditor.setShowPrintMargin(false);

        var zoneRules = document.getElementById('inRules');
        zoneRules.addEventListener('dragover', handleDragOver, false);
        zoneRules.addEventListener('drop', handleRulesSelect, false);

        rulesEditor = ace.edit("inRules");
        //rulesEditor.setTheme("ace/theme/eclipse");
        var xmlMode = require("ace/mode/xml").Mode;
        rulesEditor.getSession().setMode(new xmlMode());
        rulesEditor.setShowPrintMargin(false);
        rulesEditor.setDisplayIndentGuides(true);
        rulesEditor.commands.addCommand({
            name: "save",
            bindKey: {
                win: "Ctrl-Shift-S",
                mac: "Command-s"
            },
            exec: save
        });
        rulesEditor.commands.addCommand({
            name: "format",
            bindKey: {
                win: "Ctrl-Shift-F",
                mac: "Command-f"
            },
            exec: format
        });

        textEditor = ace.edit("inTxt");
        textEditor.setTheme("ace/theme/chrome");
        textEditor.setShowInvisibles(true);
        textEditor.setShowPrintMargin(false);
        jsMode = require("ace/mode/javascript").Mode;
        txtMode = require("ace/mode/text").Mode;
        textEditor.getSession().setMode(new txtMode());
        var zone = document.getElementById('inTxt');
        zone.addEventListener('dragover', handleDragOver, false);
        zone.addEventListener('drop', handleFileSelect, false);

        loadAlfrescoTable();
        loadAlfrescoFolderTable();
        var anOpen = [];
        loadVerteilungTable();

        init();
        // Eventhandler für die Image Clicks
        handleVerteilungImageClicks();
        handleAlfrescoFolderImageClicks();
        handleAlfrescoImageClicks();
        loadAlfrescoTree();
        switchAlfrescoDirectory("-1");
    } catch(e) {
        errorHandler(e);
    }
}
