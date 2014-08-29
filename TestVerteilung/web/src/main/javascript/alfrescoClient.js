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
    tabelle = $('#tabelle').DataTable({
        "jQueryUI": true,
        "pagingType": "full_numbers",
        "data": [],
        "scrollX": "100%",
        "scrollXInner": "100%",
        "scrollY" : calcDataTableHeight(),
        "autoWidth": true,
        "lengthChange": false,
        "searching": false,
        "pageLength": Math.max(Math.floor((verteilungLayout.state.west.innerHeight - 24 - 26 - 20) / 29), 1),
        "columns": [
            {
                "class":          'details-control',
                "orderable":      false,
                "data":           null,
                "defaultContent": '',
                "width": "12px"
            },
            { "title": "Name", "type": "string", "class": "alignLeft"  },
            { "title": "Dokumenttyp", "type": "string", "class": "alignLeft" },
            { "title": "Ergebnis", "width": "102px", "class": "alignLeft" },
            { "title": "Id" },
            { "title": "Fehler" }
        ],
        "columnDefs": [
            { "targets": [1,2,3], "visible": true},
            { "targets": [3], "mRender": imageFieldFormatter, "sortable": false},
            { "targets": [4,5], "visible": false}
        ],
        "language": {
            "info": "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_"
        }
    });
    // Add event listener for opening and closing details
    $('#tabelle tbody').on('click', 'td.details-control', function () {
        var tr = $(this).closest('tr');
        var row = tabelle.row( tr );

        if ( row.child.isShown() ) {
            // This row is already open - close it
            row.child.hide();
            tr.removeClass('shown');
        }
        else {
            // Open this row
            row.child( formatDetails(row.data()) ).show();
            tr.addClass('shown');
        }
    } );
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
 * formatiert die Iconspalte in der Tabelle
 * @param data
 * @param type
 * @param full
 * @return {string}
 */
function imageFieldFormatter(data, type, full) {

    var container =  document.createElement("div");
    var image = document.createElement("div");
    image.href = "#";
    image.className = "run";
    if (full[3].error) {
        image.style.backgroundImage = "url(src/main/resource/images/error.png)";
        image.title = "Verteilung fehlerhaft";
    } else {
        image.style.backgroundImage = "url(src/main/resource/images/ok.png)";
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
    image.style.backgroundImage = "url(src/main/resource/images/glass.png)";
    image.style.width = "16px";
    image.style.height = "16px";
    image.style.cursor = "pointer";
    image.style.cssFloat = "left";
    image.style.marginRight = "5px";
    container.appendChild(image);
    image = document.createElement("div");
    image.href = "#";
    image.className = "loeschen";
    image.title = "Ergebnis löschen";
    if (daten[full[1]]["notDeleteable"] != "true") {
        image.style.backgroundImage = "url(src/main/resource/images/delete.png)";
        image.style.cursor = "pointer";
    }
    else {
        image.style.backgroundImage = "url(src/main/resource/images/delete-bw.png)";
        image.style.cursor = "not-allowed";
    }
    image.style.width = "16px";
    image.style.height = "16px";
    image.style.cssFloat = "left";
    image.style.marginRight = "5px";
    container.appendChild(image);
    image = document.createElement("div");
    image.className = "pdf";
    if (full[1].toLowerCase().endsWith(".pdf")) {
        image.style.backgroundImage = "url(src/main/resource/images/pdf.png)";
        image.style.cursor = "pointer";
    } else {
        image.style.backgroundImage = "url(src/main/resource/images/pdf-bw.png)";
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
    image.style.backgroundImage = "url(src/main/resource/images/move-file.png)";
    image.style.cursor = "pointer";
    image.style.cssFloat = "left";
    image.style.width = "16px";
    image.style.height = "16px";
    // image.style.marginRight = "5px";
    image.title = "Zur Inbox verschieben";
    container.appendChild(image);
    return container.outerHTML;
}

/**
 * formatiert die Fehlerdetails in der zusätzlichen Zeile(n) der Tabelle
 * @param data         Das Data Object der Zeile
 * @returns {string}   HTML für die extra Zeile
 */
function formatDetails(data) {
    var sOut = '<div class="innerDetails" style="overflow: auto; width: 100%; " ><table>' +
        '<tr><tr style="height: 0px;" > '+
        '<th style="width: 100px; padding-top: 0px; padding-bottom: 0px; border-top-width: 0px; border-bottom-width: 0px; height: 0px; font-size: 12px"' +
        'colspan="1" rowspan="1" tabindex="0" class="control center">Fehler</th>' +
        '<th style="width: auto; padding-left: 10px; padding-top: 0px; padding-bottom: 0px; border-top-width: 0px; border-bottom-width: 0px; height: 0px; font-size: 12px"' +
        'colspan="1" rowspan="1" tabindex="0" class="alignLeft">Beschreibung</th></tr><td>';
    var txt = "<tr>";
    for ( var i = 0; i < data[5].length; i++) {
        txt = txt + "<td class='alignCenter' style='font-size: 11px; padding-top: 0px; padding-bottom: 0px'>" + (i+1) + "</td><td style='font-size: 11px; padding-top: 0px; padding-bottom: 0px'>" + data[5][i] + "</td>";
        txt = txt + "</tr>";
    }
    sOut = sOut + txt;
    sOut += '</table></div>';
    return sOut;
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

    // Initiales Lesen
    if (alfrescoServerAvailable)
       switchAlfrescoDirectory("-1");
}

/**
 * behandelt die Clicks auf die Icons in der AlfrescoFoldertabelle
 */
function handleAlfrescoFolderImageClicks() {
    $(document).on("click", ".folderOpen", function () {
        try {
            var tr = $(this).closest('tr');
            var row = tabelle.row( tr).data();
            switchAlfrescoDirectory(row[4]);
        } catch (e) {
            errorHandler(e);
        }
     });
}

/**
 * behandelt die Clicks auf die Icons in der Alfrescotabelle
 */
function handleAlfrescoImageClicks() {
    $(document).on("click", ".detailEdit", function () {
        try {
            var tr = $(this).closest('tr');
            var row = tabelle.row( tr).data();
            startDocumentDialog(row[9], row[6], row[1], row[7], row[3], row[4], row[2], row[5], null);
        } catch (e) {
            errorHandler(e);
        }
    });
}

/**
 * behandelt die Clicks auf die Icons in der Verteilungstabelle
 */
function handleVerteilungImageClicks() {
    $(document).on("click", ".run", function () {
        var tr = $(this).closest('tr');
        var row = tabelle.row( tr).data();
        var name = row[1];
        REC.currentDocument.setContent(daten[name]["text"]);
        REC.testRules(rulesEditor.getSession().getValue());
        daten[name].log = REC.mess;
        daten[name].result = results;
        daten[name].position = REC.positions;
        daten[name].xml = REC.currXMLName;
        daten[name].error = REC.errors;
        var ergebnis = [];
        ergebnis["error"] = REC.errors.length > 0;
        row[2] = REC.currXMLName.join(" : ");
        row[3] = ergebnis;
        row[5] = REC.errors;
        if (tabelle.fnUpdate(row, aPos[0]) > 0)
            message("Fehler", "Tabelle konnte nicht aktualisiert werden!");
    });
    $(document).on("click", ".glass", function () {
        var tr = $(this).closest('tr');
        var row = tabelle.row( tr).data();
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
            var tr = $(this).closest('tr');
            var row = tabelle.row( tr).data();
            var name = row[1];
            try {
                netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
            } catch (e) {
                message("Fehler", "Permission to delete file was denied.");
            }
            currentFile = daten[name]["file"];
            textEditor.getSession().setValue("");
            propsEditor.getSession().setValue("");
            clearMessageBox();
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
        var tr = $(this).closest('tr');
        var row = tabelle.row( tr).data();
        var name = row[1];
        if (typeof daten[name]["container"] != "undefined" && daten[name]["container"] != null) {
            openPDF(daten[name]["container"], true);
        } else {
            openPDF(daten[name]["file"]);
        }
    });
    $(document).on("click", ".moveToInbox", function () {
        var tr = $(this).closest('tr');
        var row = tabelle.row( tr).data();
        var name = row[1];
        var docId = "workspace:/SpacesStore/" + daten[name]["container"];
        var json = executeService("createDocument", [
            {"name": "folder", "value": "/Archiv/Inbox"},
            { "name": "fileName", "value": name},
            { "name": "documentContent", "value": daten[name].content, "type": "byte"},
            { "name": "documentType", "value": "application/pdf"},
            { "name": "extraCMSProperties", "value": ""},
            { "name": "versionState", "value": "none"}
        ], ["Dokument konnte nicht auf den Server geladen werden:", "Dokument " + name + " wurde erfolgreich in die Inbox verschoben!"]);
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
        loadVerteilungTable();

        init();
        // Eventhandler für die Image Clicks
        handleVerteilungImageClicks();
        handleAlfrescoFolderImageClicks();
        handleAlfrescoImageClicks();
        loadAlfrescoTree();

    } catch(e) {
        errorHandler(e);
    }
}
