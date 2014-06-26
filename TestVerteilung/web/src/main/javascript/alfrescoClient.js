function changeCss(className, classValue) {
// we need invisible container to store additional css definitions
    var cssMainContainer = $('#css-modifier-container');
    if (cssMainContainer.length == 0) {
        var cssMainContainer = $('<div id="css-modifier-container"></div>');
        cssMainContainer.hide();
        cssMainContainer.appendTo($('body'));
    }

// and we need one div for each class
    classContainer = cssMainContainer.find('div[data-class="' + className + '"]');
    if (classContainer.length == 0) {
        classContainer = $('<div data-class="' + className + '"></div>');
        classContainer.appendTo(cssMainContainer);
    }

// append additional style
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
        "aoColumns": [
            { "mDataProp": null, "sClass": "control center", "sWidth": "12px"},
            { "sTitle": "Name", "sType": "string", "sClass": "alignLeft"  },
            { "sTitle": "Datum", "sType": "date", "sClass": "alignLeft" },
            { "sTitle": "Person", "sType": "string", "sClass": "alignLeft" },
            { "sTitle": "Betrag", "sType": "numeric", "sClass": "alignLeft" },
            { "sTitle": "Id" }
        ],
        "aoColumnDefs": [
            // { "aTargets": [0], "fnRender": expandFieldFormatter, "bSortable": false},
            { "aTargets": [1, 2, 3, 4], "bVisible": true},
            { "aTargets": [5], "bVisible": false}
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
        "aoColumns": [
            { "mDataProp": null, "sClass": "control center", "sWidth": "12px"},
            { "sTitle": "Name", "sType": "string", "sClass": "alignLeft"  },
            { "sTitle": "Beschreibung", "sType": "string", "sClass": "alignLeft" },
            { "sTitle": "Aktion", "sWidth": "102px", "sClass": "alignLeft" },
            { "sTitle": "Id" }
        ],
        "aoColumnDefs": [
            // { "aTargets": [0], "fnRender": expandFieldFormatter, "bSortable": false},
            { "aTargets": [1, 2], "bVisible": true},
            { "aTargets": [3], "fnRender": aktionFieldFormatter, "bSortable": false},
            { "aTargets": [4], "bVisible": false}
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
        "aoColumns": [
            { "mDataProp": null,"sClass": "control center", "sWidth": "12px"},
            { "sTitle": "Name", "sType": "string", "sClass": "alignLeft"  },
            { "sTitle": "Dokumenttyp", "sType": "string", "sClass": "alignLeft" },
            { "sTitle": "Ergebnis", "sWidth": "102px", "sClass": "alignLeft" },
            { "sTitle": "Id" },
            { "sTitle": "Fehler" }
        ],
        "aoColumnDefs": [
            { "aTargets": [0], "fnRender": expandFieldFormatter, "bSortable": false},
            { "aTargets": [1,2,3], "bVisible": true},
            { "aTargets": [3], "fnRender": imageFieldFormatter, "bSortable": false},
            { "aTargets": [4,5], "bVisible": false}
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
 * formatiert die Iconspalte in der Tabelle
 * @param o
 * @returns {string}
 */
function aktionFieldFormatter(o) {
    if (o.iDataRow == 0) {
        //	o.cell.setStyle('width', '102px');
    }
    var container =  document.createElement("div");
    var image = document.createElement("div");
    image.href = "#";
    image.className = "edit";
    image.style.backgroundImage = "url(resource/file_edit.png)";
    image.title = "Details bearbeiten";
    image.style.cursor = "pointer";
    image.style.width = "16px";
    image.style.height = "16px";
    image.style.cssFloat = "left";
    image.style.marginRight = "5px";
    container.appendChild(image);
    image = document.createElement("div");
    image.href = "#";
    image.className = "open";
    image.title = "Ordner öffnen";
    image.style.backgroundImage = "url(resource/details_open.png)";
    image.style.width = "16px";
    image.style.height = "16px";
    image.style.cursor = "pointer";
    image.style.cssFloat = "left";
    image.style.marginRight = "5px";
    container.appendChild(image);
    container.appendChild(image);
    return container.outerHTML;
}

/**
 * führt die Aktualisierungen für eine Verzeichniswechsel im Alfresco durch
 * @param id  die Objectid des ausgewählten Folders
 */
function switchAlfrescoDirectory(id) {
    try {
        var json = executeService("listFolderAsJSON", [
            {"name": "filePath", "value": id},
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
            {"name": "filePath", "value": id},
            {"name": "withFolder", "value": 1}
        ], "Dokumente konnten nicht aus dem Server gelesen werden:");
        if (json.success) {
            alfrescoTabelle.fnClearTable();
            for (var index = 0; index < json.result.length; ++index) {
                var name = json.result[index].attr.title ? json.result[index].attr.title : json.result[index].attr.name ? json.result[index].attr.name : "";
                var datum = json.result[index].attr.documentDate ? json.result[index].attr.documentDate : json.result[index].attr.creationDate ? json.result[index].attr.creationDate : "";
                var date = parseDate(datum);
                var dateString = date ? REC.dateFormat(date, "dd.MM.YYYY") : "";
                var person = json.result[index].attr.person ? json.result[index].attr.person : "";
                var amount = json.result[index].attr.amount ? json.result[index].attr.amount : "";
                var id = json.result[index].attr.objectId ? json.result[index].attr.objectId : "";
                var row = [ null, name, dateString, person, amount, id];
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
                        alert("Folder konnte nicht erfolgreich im Alfresco gelesen werden!");
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
    $(document).on("click", ".open", function () {
        var aPos = alfrescoFolderTabelle.fnGetPosition(this.parentNode.parentNode);
        var row = alfrescoFolderTabelle.fnGetData(aPos[0]);
        switchAlfrescoDirectory(row[4]);
     });
}
