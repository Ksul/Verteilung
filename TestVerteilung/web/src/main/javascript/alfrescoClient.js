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
 * Eventhandler der für die Verarbeitung von fallen gelassen Dateien auf die Inbox zuständig ist
 * @param evt  das Event
 */
function handleDropInbox(evt) {
    evt.stopPropagation();
    evt.preventDefault();
    var files = evt.dataTransfer.files;
    for ( var i = 0; i < files.length; i++) {
        var f = files[i];
        if (f) {
            var reader = new FileReader();
            reader.onloadend = (function (f) {
                return function (evt) {
                    if (evt.target.readyState == FileReader.DONE) {
                        var content = evt.target.result;
                        var json = executeService("createDocument", [
                            {"name": "folder", "value":inboxID},
                            {"name": "fileName", "value": f.name},
                            {"name": "documentText", "value": btoa(content)},
                            {"name": "mimeType", "value": "application/pdf"},
                            {"name": "extraProperties", "value": "{}"},
                            {"name": "versionState", "value": "major"}

                        ], "Dokument konnten nicht im Alfresco angelegt werden!");
                    }

                }  })(f);
            blob = f.slice(0, f.size + 1);
            reader.readAsBinaryString(blob);
        } else {
            message("Fehler", "Failed to load file!");
        }
    }
}


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
                name: "alfrescoInnerCenterLayout",
                contentSelector: ".ui-widget-content",
                spacing_open: 8,
                spacing_closed: 12,
                north: {
                    paneSelector: "#alfrescoCenterInnerNorth",
                    size: .44,
                    onresize: function () {
                    },
                    children: {
                        name: "alfrescoInnerCenterNorthLayout",
                        contentSelector: ".ui-widget-content",
                        spacing_open: 8,
                        spacing_closed: 12,
                        north: {
                            paneSelector: "#alfrescoCenterInnerNorthNorth",
                            minHeight: 30,
                            resizable:			        false,
                            closable:		    		false
                        },
                        center: {
                            size: "auto",
                            paneSelector: "#alfrescoCenterInnerNorthCenter",
                            onresize: function () {
                            }
                        }
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
    try {

        $('#dtable2').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="alfrescoTabelle"></table>');
        alfrescoTabelle = $('#alfrescoTabelle').DataTable({
            "jQueryUI": true,
            "pagingType": "full_numbers",
            "data": [],
            "scrollX": "100%",
            "scrollXInner": "100%",
            // "sScrollY" : calcDataTableHeight(),
            "autoWidth": true,
            "lengthChange": false,
            "searching": false,
            // "iDisplayLength": Math.max(Math.floor((verteilungLayout.state.west.innerHeight - 24 - 26 - 20) / 29), 1),
            "columns": [
                {
                    "class": 'details-control',
                    "orderable": false,
                    "data": null,
                    "defaultContent": '',
                    "width": "12px"
                },
                {
                    "data": "contentStreamMimeType",
                    "title": "Typ",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignCenter",
                    "width": "35px"
                },
                {
                    "data": "title",
                    "title": "Titel",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": "documentDate",
                    "title": "Datum",
                    "defaultContent": '',
                    "type": "date",
                    "class": "alignLeft"
                },
                {
                    "data": "person",
                    "title": "Person",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": "amount",
                    "title": "Betrag",
                    "defaultContent": '',
                    "type": "numeric",
                    "class": "alignLeft"
                },
                {
                    "data": "idvalue",
                    "title": "Schlüssel",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": null,
                    "title": "Aktion",
                    "width": "102px",
                    "class": "alignLeft"
                },
                {
                    "data": "objectID"
                }
            ],
            "columnDefs": [
                {
                    "targets": [4, 5],
                    "visible": true
                },

                {   "targets": [8],
                    "visible": false
                },

                {
                    "targets": [1],
                    "render": function (data, type, row) {
                        if (exist(data) && data == "application/pdf") {

                            var image = document.createElement("span");
                            image.id = "alfrescoTable" + row.objectID;
                            image.className = "alfrescoTableEvent";
                            image.draggable = true;
                            image.href = "#";
                            image.title = "PDF Dokument";
                            image.style.backgroundImage = "url(src/main/resource/images/pdf.png)";
                            image.style.width = "16px";
                            image.style.height = "16px";
                            image.style.cursor = "pointer";
                            image.style.cssFloat = "left";
                            image.style.marginRight = "5px";
                            return image.outerHTML;
                        } else
                            return "";
                    },
                    "visible": true
                },
                {
                    "targets": [2],
                    "render": function (data, type, row) {
                        if (exist(data))
                            return data;
                        else if (exist(row.name))
                            return row.name;
                        else
                            return "";
                    },
                    "visible": true
                },
                {
                    "targets": [3],
                    "render": function (data, type, row) {
                        if (exist(data)) {
                            var datum;
                            try {
                                // editierte Datumwerte haben das falsche Format, deshalb werden sie erstmal wieder geparst
                                data = $.datepicker.parseDate("dd.mm.yy", data).getTime();
                            } catch(e){}
                            datum = $.datepicker.formatDate("dd.mm.yy", new Date(Number(data)));
                            return datum
                        }
                        else if (exist(row.creationDate))
                            return $.datepicker.formatDate("dd.mm.yy", new Date(Number(row.creationDate)));
                        else
                            return "";
                    },
                    "visible": true
                },
                {
                    "targets": [7],
                    "mRender": alfrescoAktionFieldFormatter,
                    "sortable": false
                }
            ],
            "language": {
                "info": "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_"
            }
        });
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * baut die Alfresco Folder Tabelle auf.
 */
function loadAlfrescoFolderTable() {
    try {
        $('#dtable3').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="alfrescoFolderTabelle"></table>');
        alfrescoFolderTabelle = $('#alfrescoFolderTabelle').DataTable({
            "jQueryUI": true,
            "pagingType": "full_numbers",
            "data": [],
            "scrollX": "100%",
            "scrollXInner": "100%",
            // "sScrollY" : calcDataTableHeight(),
            "autoWidth": true,
            "lengthChange": false,
            "searching": false,
            "rowCallback": function( row, data ) {
                try {
                    // Cell click
                    $('td', row).on('click', function () {
                        try {
                            if (this.cellIndex == 0) {
                                $("#tree").jstree('deselect_all', true);
                                switchAlfrescoDirectory(data);
                            }
                        } catch (e) {
                            errorHandler(e);
                        }
                    });
                    // Cursor
                    $('td', row).hover(function () {
                        if (this.cellIndex == 0)
                            $(this).css('cursor', 'pointer');
                    }, function () {
                        $(this).css('cursor', 'auto');

                    });
                } catch (e) {
                    errorHandler(e);
                }
            },
            // "iDisplayLength": Math.max(Math.floor((verteilungLayout.state.west.innerHeight - 24 - 26 - 20) / 29), 1),
            "columns": [
                {
                    "class": 'folder-control',
                    "orderable": false,
                    "data": null,
                    "defaultContent": '',
                    "width": "40px"
                },
                {
                    "data": "name",
                    "title": "Name",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "data": "description",
                    "title": "Beschreibung",
                    "defaultContent": '',
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "title": "Aktion",
                    "data": null,
                    "width": "120px",
                    "class": "alignLeft"
                }
            ],
            "columnDefs": [
                {
                    "targets": [0],
                    "sortable": false
                },
                {
                    "targets": [1, 2],
                    "visible": true
                },
                {
                    "targets": [3],
                    "mRender": alfrescoFolderAktionFieldFormatter,
                    "sortable": false
                }
            ],
            "language": {
                "info": "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_"
            }
        });
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * lädt die Tabelle für den Verteilungstab
 */
function loadVerteilungTable() {
    try {
        $('#dtable').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="tabelle"></table>');
        tabelle = $('#tabelle').DataTable({
            "jQueryUI": true,
            "pagingType": "full_numbers",
            "data": [],
            "scrollX": "100%",
            "scrollXInner": "100%",
            "scrollY": calcDataTableHeight(),
            "autoWidth": true,
            "lengthChange": false,
            "searching": false,
            "pageLength": Math.max(Math.floor((verteilungLayout.state.west.innerHeight - 24 - 26 - 20) / 29), 1),
            "columns": [
                {
                    "class": 'details-control',
                    "orderable": false,
                    "data": null,
                    "defaultContent": '',
                    "width": "35px"
                },
                {
                    "title": "Name",
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "title": "Dokumenttyp",
                    "type": "string",
                    "class": "alignLeft"
                },
                {
                    "title": "Ergebnis",
                    "width": "102px",
                    "class": "alignLeft"
                },
                {
                    "title": "Id"
                },
                {
                    "title": "Fehler"
                }
            ],
            "columnDefs": [
                {
                    "targets": [0],
                    "sortable": false
                },
                {
                    "targets": [1, 2, 3],
                    "visible": true
                },
                {
                    "targets": [3],
                    "mRender": imageFieldFormatter,
                    "sortable": false
                },
                {
                    "targets": [4, 5],
                    "visible": false
                }
            ],
            "language": {
                "info": "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_"
            }
        });
        // Add event listener for opening and closing details
        $('#tabelle tbody').on('click', 'td.details-control', function () {
            var tr = $(this).closest('tr');
            var row = tabelle.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
            }
            else {
                // Open this row
                row.child(formatDetails(row.data())).show();
                tr.addClass('shown');
            }
        });
    } catch (e) {
        errorHandler(e);
    }
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
    try {

        var container = document.createElement("div");
        var image = document.createElement("div");
        image.href = "#";
        image.className = "folderCreate";
        image.style.backgroundImage = "url(src/main/resource/images/folder_add.png)";
        image.title = "neuen Folder anlegen";
        image.style.cursor = "pointer";
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);
        image = document.createElement("div");
        image.href = "#";
        image.className = "folderRemove";
        image.style.backgroundImage = "url(src/main/resource/images/folder_remove.png)";
        image.title = "Folder löschen";
        image.style.cursor = "pointer";
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);
        image = document.createElement("div");
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
        image.className = "folderSwitch";
        image.title = "Ordner öffnen";
        image.style.backgroundImage = "url(src/main/resource/images/details_open.png)";
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);
        return container.outerHTML;
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert die Iconspalte in der AlfrescoTabelle
 * @param o
 * @returns {string}
 */
function alfrescoAktionFieldFormatter(data, type, full) {
    try {
        var container = document.createElement("div");
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
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert die Iconspalte in der Tabelle
 * @param data
 * @param type
 * @param full
 * @return {string}
 */
function imageFieldFormatter(data, type, full) {
    try {
        var container = document.createElement("div");
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
    } catch (e) {
        errorHandler(e);
    }
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
 * füllt die BreadCrumb Leiste
 * @param data          der aktuelle Folder

 */
function fillBreadCrumb(data) {
    var object;
    var id;
    var parentObj;
    var fill = true;
    var oldLi = $('#breadcrumblist');
    if (exist(oldLi))
        oldLi.remove();
    var container = $('#breadcrumb');
    var ul = document.createElement('ul');
    ul.id = 'breadcrumblist';
    do {
        if (!exist(data)) {
            object = ["Archiv"];
            id = "-1";
            parentObj = null;
            name = "Archiv";
        }
        else {
            object = data.path.split('/');
            id = data.objectId;
            parentObj = data.parentId;
            name = data.name;
        }
        var li = document.createElement('li');
        var jsonData = {
            'objectId': id,
            'path': object.join('/'),
            'name': name,
            'parentId': parentObj
        };
        li.data = jsonData;
        li.onclick = function () {
            $("#tree").jstree('deselect_all', false);
            switchAlfrescoDirectory(this.data);
        };
        var a = document.createElement('a');
        a.href = '#';
        a.text = name;
        li.appendChild(a);
        if (parentObj == null)
            fill = false;
        else
            data = $("#tree").jstree('get_json', $(document.getElementById(parentObj))).data;
        ul.insertBefore(li, ul.firstChild);
    } while (fill);
    container.append(ul);
    return;
}

/**
 * führt die Aktualisierungen für eine Verzeichniswechsel im Alfresco durch
 * @param data      das Datenobjekt des ausgewählten Folders
 */
function switchAlfrescoDirectory(data) {
    try {
        var objectId;
        if (exist(data))
            objectId = data.objectId;
        else
            objectId = "-1";
        var json = executeService("listFolder", [
            {"name": "filePath", "value": objectId},
            {"name": "withFolder", "value": -1}
        ], "Verzeichnis konnte nicht aus dem Server gelesen werden:");
        if (json.success) {
            alfrescoFolderTabelle.clear();
            alfrescoFolderTabelle.rows.add(json.result).draw();
            $.fn.dataTable.makeEditable( alfrescoFolderTabelle, {
                "fnShowError" : function(text, aktion){
                    message("Fehler", text);
                },
                "aoColumns": [ null,
                    {
                        placeholder: ""
                    },
                    {
                        placeholder: ""
                    },
                    null
                    ],
                sUpdateURL: function (value, settings) {
                    try {
                        var extraProperties;
                        var data = alfrescoFolderTabelle.row($(this).closest('tr')).data();
                        if (this.cellIndex == 1) {
                            // Name geändert
                            data.name = value
                        } else {
                            // Beschreibung geändert
                            data.description = value;
                        }
                        extraProperties = {
                            'cmis:folder': {
                                'cmis:objectTypeId': 'cmis:folder',
                                'cmis:name': data.name
                            },
                            'P:cm:titled': {
                                'cm:title': data.title,
                                'cm:description': data.description
                            }
                        };
                        erg = executeService("updateProperties", [
                            {"name": "documentId", "value": data.objectId},
                            {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                        ], null, true);
                        if (erg.success) {
                            var node = $(document.getElementById(data.objectId));
                            $("#tree").jstree('rename_node', node[0], value);
                            return(value);
                        }
                        else
                            return "Folder konnte nicht aktualisiert werden!" + "<br>" + erg.result;

                    } catch (e) {
                        errorHandler(e);
                    }
                }
            });
            fillBreadCrumb(data);

            $("#tree").jstree('select_node', objectId);
        }
        json = executeService("listFolder", [
            {"name": "filePath", "value": objectId},
            {"name": "withFolder", "value": 1}
        ], "Dokumente konnten nicht aus dem Server gelesen werden:");
        if (json.success) {
            alfrescoTabelle.clear();
            alfrescoTabelle.rows.add(json.result).draw();

            $.fn.dataTable.makeEditable( alfrescoTabelle, {
                "fnShowError" : function(text, aktion){
                    message("Fehler", text);
                },
                "aoColumns": [ null,
                               null,
                    {
                        placeholder: ""
                    },
                    {
                        placeholder: ""
                    },
                    {
                        placeholder: ""
                    },
                    {
                        placeholder: ""
                    },
                    {
                        placeholder: ""
                    },
                    null
                ],
                sUpdateURL: function (value, settings) {
                    try {
                        var extraProperties;
                        var data = alfrescoTabelle.row($(this).closest('tr')).data();
                        if (this.cellIndex == 2) {
                            // Titel geändert
                            data.title = value
                        } else if (this.cellIndex == 3) {
                            // Datum geändert
                            data.documentDate = $.datepicker.parseDate("dd.mm.yy", value).getTime();
                        } else if (this.cellIndex == 4) {
                            // Person geändert
                            data.person = value;
                        } else if (this.cellIndex == 5) {
                            // Betrag geändert
                            data.amount = value;
                        } else if (this.cellIndex == 6) {
                            // Id geändert
                            data.idvalue = value;
                        }
                        var extraProperties = {
                            'P:cm:titled': {'cm:title': data.title, 'cm:description': data.description},
                            'D:my:archivContent': {'my:documentDate':data.documentDate, 'my:person': data.person},
                            'P:my:amountable': {'my:amount': data.amount, "my:tax": data.tax},
                            'P:my:idable': {'my:idvalue': data.idvalue}
                        };
                        erg = executeService("updateProperties", [
                            {"name": "documentId", "value": data.objectId},
                            {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                        ], null, true);
                        if (erg.success) {
                            return(value);
                        }
                        else
                            return "Dokument konnte nicht aktualisiert werden!" + "<br>" + erg.result;
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            });
            $('.alfrescoTableEvent').off("dragstart");
            $('.alfrescoTableEvent').on("dragstart", function (event) {
                try {
                    var dt = event.originalEvent.dataTransfer;
                    var row = alfrescoTabelle.row($(this).closest(('tr')));
                    var data = row.data();
                    dt.setData('Id', data.objectID);
                    dt.setData('parentId', data.parentId);
                    dt.setData('rowIndex', row.index());
                    // die Zielbereiche für den Drag festlegen
                    populateEventHandlerForTreeIcons();
                    // Den aktuellen Zweig wieder rausnehmen, damit nicht in das aktuelle Verzeichnis verschoben werden kann
                    $(document.getElementById(data.parentId)).children('a').off("dragenter dragover drop");
                } catch (e) {
                    errorHandler(e);
                }
            });
            $('.alfrescoTableEvent').off("click");
            $('.alfrescoTableEvent').on("click", function (event) {
                try {
                    var dt = event.originalEvent.dataTransfer;
                    //wg alfresco Prefix
                    var data = alfrescoTabelle.row($(this).closest(('tr'))).data();
                    var url = getSettings("server") + "d/d/workspace/" + data.nodeRef.substr(12) + "/file.bin";
                    var obj = executeService("getTicket");
                    if (obj.success)
                        url = url + "?alf_ticket=" + obj.result.data.ticket;
                    window.open(url);
                } catch (e) {
                    errorHandler(e);
                }
            });
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * behandelt die Clicks auf die Icons in der AlfrescoFoldertabelle
 */
function handleAlfrescoFolderImageClicks() {
    $(document).on("click", ".folderSwitch", function () {
        try {
             var tr = $(this).closest('tr');
             var row = alfrescoFolderTabelle.row( tr).data();
             $("#tree").jstree('deselect_all', true);
             switchAlfrescoDirectory(row);
        } catch (e) {
            errorHandler(e);
        }
     });
    $(document).on("click", ".folderCreate", function () {
        try {
            var tr = $(this).closest('tr');
            startFolderDialog(alfrescoFolderTabelle.row(tr), "VIEW_WEB_CREATE");
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".folderRemove", function () {
        try {
            var tr = $(this).closest('tr');
            var id = alfrescoFolderTabelle.row(tr).data().objectId;
            var $dialog = $('<div></div>').html("Ausgewählten Folder " + alfrescoFolderTabelle.row(tr).data().name + " löschen?").dialog({
                autoOpen: true,
                title: "Folder löschen",
                modal: true,
                height: 100,
                width: 200,
                buttons: {
                    "Ok": function () {
                        try {
                            $(this).dialog("destroy");
                            erg = executeService("deleteFolder", [
                                {"name": "documentId", "value": id}
                            ], "Folder konnte nicht gelöscht werden!", false);
                            if (erg.success) {
                                var node = $(document.getElementById(id));
                                $("#tree").jstree('remove', node[0]);
                                alfrescoFolderTabelle.rows().invalidate();
                            }
                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                    "Abbrechen": function () {
                        $(this).dialog("destroy");
                    }
                }
            });
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".folderEdit", function () {
        try {
            var tr = $(this).closest('tr');
            startFolderDialog(alfrescoFolderTabelle.row(tr), "VIEW_WEB_EDIT");
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
            startDocumentDialog(alfrescoTabelle.row(tr));
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
        try {
            var tr = $(this).closest('tr');
            var row = tabelle.row(tr).data();
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
        }
        catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".glass", function () {
        try {
            var tr = $(this).closest('tr');
            var row = tabelle.row(tr).data();
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

        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".loeschen", function () {
        try {
            var answer = confirm("Eintrag löschen?");
            if (answer) {
                var tr = $(this).closest('tr');
                var row = tabelle.row(tr).data();
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
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".pdf", function (name) {
        try {
            var tr = $(this).closest('tr');
            var row = tabelle.row(tr).data();
            var name = row[1];
            if (typeof daten[name]["container"] != "undefined" && daten[name]["container"] != null) {
                openPDF(daten[name]["container"], true);
            } else {
                openPDF(daten[name]["file"]);

            }
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".moveToInbox", function () {
        try {
            var tr = $(this).closest('tr');
            var row = tabelle.row(tr).data();
            var name = row[1];
            var docId = "workspace:/SpacesStore/" + daten[name]["container"];
            var json = executeService("createDocument", [
                {"name": "folder", "value": "/Archiv/Inbox"},
                { "name": "fileName", "value": name},
                { "name": "documentContent", "value": daten[name].content, "type": "byte"},
                { "name": "documentType", "value": "application/pdf"},
                { "name": "extraCMSProperties", "value": ""},
                { "name": "versionState", "value": "major"}
            ], ["Dokument konnte nicht auf den Server geladen werden:", "Dokument " + name + " wurde erfolgreich in die Inbox verschoben!"]);
        } catch (e) {
            errorHandler(e);
        }
    });
}

/**
 * lädt und kobertiert die Daten für den Tree
 * @param aNode      der ausgeählte Knoten
 * @return obj      die Daten als jstree kompatible JSON Objekte
 */
function loadDataForTree(aNode) {
    var obj = {};
    var state = {"opened": true, "disabled": false, "selected": true};
    var state1 = {"opened": false, "disabled": false, "selected": false};
    try {
        // keine Parameter mit gegeben, also den Rooteintrag erzeugen
        if (!exist(aNode)) {
            return [
                {"icon": "", "text": "Archiv", "state": state1, "children": true}
            ];
        } else {
            if (alfrescoServerAvailable) {
                var json = executeService("listFolder", [
                    {"name": "filePath", "value": aNode.id != "#" ? aNode.id : "-1"},
                    {"name": "withFolder", "value": -1}
                ], "Verzeichnis konnte nicht aus dem Server gelesen werden:");
                if (json.success) {
                    var obj = [];
                    for (var index = 0; index < json.result.length; index++) {
                        var item = {};
                        var o = json.result[index];
                        if (o.baseTypeId == "cmis:folder") {
                            item["icon"] = "";
                            item["state"] = state1;
                        } else {
                            item["icon"] = "";
                            item["state"] = "";
                        }
                        item["id"] = o.objectId;
                        item["children"] = o.hasChildFolder;
                        item["text"] = o.name;
                        item["data"] = o;
                        item["a_attr"] = "'class': 'drop'";
                        obj.push(item);
                    }
                    if (aNode.id == "#")
                        return [
                            {"icon": "", "text": "Archiv", "state": state, "children": obj, "id": "-1"}
                        ];
                    else
                        return obj;
                }
                else {
                    message("Fehler", "Folder konnte nicht erfolgreich im Alfresco gelesen werden!");
                    return null;
                }
            }
        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * verschiebt ein Dokument
 * @param node
 * @param destination
 */
function moveDocument(nodeId, parentId, destinationId) {
    try {
        var json = executeService("moveDocument", [
            {"name": "documentId", "value": nodeId},
            {"name": "currentLocationId", "value": parentId},
            {"name": "destinationId", "value": destinationId}
        ], "Dokument konnte nicht verschoben werden:");
        return json.success;
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * trägt den EventHandler für die Tree-Icons ein.
 */
function populateEventHandlerForTreeIcons() {
    $('.jstree-anchor').off("dragenter dragover drop");
    $('.jstree-anchor').on("dragenter dragover drop", function (event) {
        try {
            event.preventDefault();
            if (event.type === 'drop') {
                var nodeId = event.originalEvent.dataTransfer.getData('id', $(this).attr('id'));
                var parentId = event.originalEvent.dataTransfer.getData('parentId', $(this).attr('id'));
                var rowIndex = event.originalEvent.dataTransfer.getData('rowIndex', $(this).attr('id'));
                var destinationId = this.parentElement.id;
                var erg = moveDocument(nodeId, parentId, destinationId);
                if (erg) {
                    alfrescoTabelle.row(rowIndex).remove();
                    alfrescoTabelle.draw();
                }
            }
        } catch (e) {
            errorHandler(e);
        }
    });
}

/**
 * lädt den Alfresco Tree
 */
function loadAlfrescoTree() {

    try {
        $("#tree").jstree('destroy');
        tree = $("#tree").jstree({
            'core': {
                    'check_callback': true,
                    'data': function (node, aFunction) {
                        try {
                            var obj = loadDataForTree(node);
                            // CallBack ausführen
                            if (exist(obj)) {
                                aFunction.call(this, obj);
                            }

                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                'themes' : {
                    'responsive' : false,
                    'variant' : 'big',
                    'stripes' : false,
                    'dots'    : true,
                    'icons'   : true

                }

            },
            'plugins': ["dnd", "types", "search"]
        }).on("select_node.jstree", function (event, data) {
            try {
                if (!exist(data.node.data))
                    switchAlfrescoDirectory(null);
                else {
                    if (data.node.data.baseTypeId == "cmis:folder") {
                        if (alfrescoServerAvailable) {
                            switchAlfrescoDirectory(data.node.data);
                            $("#tree").jstree('open_node', data.node.id);
                        }
                    }
                }
            } catch (e) {
                errorHandler(e);
            }
        }).on("loaded.jstree", function (event, data) {
            try {
                // Eventlistner für Drop in Inbox
                var zone = document.getElementById(inboxID);
                zone.addEventListener('dragover', handleDragOver, false);
                zone.addEventListener('drop', handleDropInbox, false);
            } catch (e) {
                errorHandler(e);
            }
        });



        // Initiales Lesen
        if (alfrescoServerAvailable)
            switchAlfrescoDirectory(null);

    } catch (e) {
        errorHandler(e);
    }
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
