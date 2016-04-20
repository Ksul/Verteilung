/**
 * ändert das CSS für einen bestimmte Class
 * @param className   der Name der Class
 * @param classValue  der neue Wert
 */
function changeCss(className, classValue) {
    var cssMainContainer = $('#css-modifier-container');
    if (cssMainContainer.length == 0) {
        cssMainContainer = $('<div id="css-modifier-container"></div>');
        cssMainContainer.hide();
        cssMainContainer.appendTo($('body'));
    }
    classContainer = cssMainContainer.find('div[data-class="' + className + '"]');
    if (classContainer.length == 0) {
        classContainer = $('<div data-class="' + className + '"></div>');
        classContainer.appendTo(cssMainContainer);
    }
    classContainer.html('<style>' + className + ' {' + classValue + '}</style>');
}

function getAlfrescoTicket() {
    if (!exist(alfrescoTicket)) {
        var obj = executeService("getTicket");
        if (obj.success)
            alfrescoTicket = obj.result.data.ticket;
    }
    return alfrescoTicket;
}

/**
 * startet den normalen Alfresco View
 */
function showAlfrescoNormalView(){
    viewMenu.children('li:first').superfish('hide');
    alfrescoLayout.children.center.alfrescoCenterInnerLayout.children.center.alfrescoCenterCenterInnerLayout.show("north");
    alfrescoTabelle.column(0).visible(true);
    alfrescoTabelle.column(1).visible(true);
    alfrescoTabelle.column(2).visible(false);
    calculateTableHeight("alfrescoCenterCenterCenter", alfrescoTabelle, "dtable2", "alfrescoTabelle", "alfrescoTabelleHeader", "alfrescoTableFooter");
}

/**
 * startet den normalen Alfresco SearchView
 */
function showAlfrescoSearchNormalView(){
    viewMenu.children('li:first').superfish('hide');
    alfrescoSearchTabelle.column(0).visible(true);
    alfrescoSearchTabelle.column(1).visible(true);
    alfrescoSearchTabelle.column(2).visible(false);
    calculateTableHeight("searchCenter", alfrescoSearchTabelle, "dtable4", "alfrescoSearchTabelle", "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
}

/**
 * startet den Alfresco Icon View
 */
function showAlfrescoIconView(){
    viewMenu.children('li:first').superfish('hide');
    alfrescoLayout.children.center.alfrescoCenterInnerLayout.children.center.alfrescoCenterCenterInnerLayout.hide("north");
    alfrescoTabelle.column(0).visible(false);
    alfrescoTabelle.column(1).visible(false);
    alfrescoTabelle.column(2).visible(true);
    calculateTableHeight("alfrescoCenterCenterCenter", alfrescoTabelle, "dtable2", "alfrescoTabelle", "alfrescoTabelleHeader", "alfrescoTableFooter");
}

/**
 * startet den Alfresco Search Icon View
 */
function showAlfrescoSearchIconView() {
    viewMenu.children('li:first').superfish('hide');
    alfrescoSearchTabelle.column(0).visible(false);
    alfrescoSearchTabelle.column(1).visible(false);
    alfrescoSearchTabelle.column(2).visible(true);
    calculateTableHeight("searchCenter", alfrescoSearchTabelle, "dtable4", "alfrescoSearchTabelle", "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
}

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
                            {"name": "documentId", "value":inboxFolderId},
                            {"name": "fileName", "value": f.name},
                            {"name": "documentText", "value": base64EncArr(strToUTF8Arr(content))},
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
 * berechnet die passende Tabellengröße
 * @param panel         das Layoutpanel, welches die Tabelle enthält
 * @param tabelle       die Tabelle
 * @param divId         die Id des DIV's welches die Tabelle enthält
 * @param tabelleId     die Id der Tabelle
 * @param headerId      die Id des headers
 * @param footerId      die Id des Footers
 */
function calculateTableHeight(panel, tabelle, divId, tabelleId,headerId, footerId) {
    var div = $('#'+divId);
    var table = $('#'+tabelleId);
    var completePanel = $('#'+panel).height();
    var topPanel = div.children().children()[0].offsetHeight;
    var downPanel = div.children().children()[2].offsetHeight;
    var columnPanel = div.children().children()[1].children[0].offsetHeight;
    var headerPanel = $('#'+headerId).height();
    var footerPanel = $('#'+footerId).height();
    while (((completePanel - topPanel - headerPanel - columnPanel - downPanel - footerPanel) > table.height()) && tabelle.page.len() < 50) {
        tabelle.page.len(tabelle.page.len() + 1).draw();
    }
    while (((completePanel - topPanel - headerPanel - columnPanel - downPanel - footerPanel) < table.height()) && tabelle.page.len() > 2) {
        tabelle.page.len(tabelle.page.len() - 1).draw();
    }
}


function asumeCountOfTableEntries(panel,  divId, tabelleId,headerId, footerId) {
    var div = $('#'+divId);
    var completePanel = $('#' + panel).height();
    var topPanel = div.children().children()[0].offsetHeight;
    var downPanel = div.children().children()[2].offsetHeight;
    var columnPanel = div.children().children()[1].children[0].offsetHeight;
    var headerPanel = $('#' + headerId).height();
    var footerPanel = $('#' + footerId).height();
    var rowHeight = $('.odd') + 2;
    return Math.floor((completePanel - topPanel - headerPanel - columnPanel - downPanel - footerPanel) / rowHeight);
}

/**
 * baut das Layout der Anwendung auf
 */
function loadLayout() {
    try {
        var clientPageLayoutSettings = {
            name: "clientLayoutSettings",
            center__paneSelector: "#clientPage",
            resizable: false
        };
        // Seitenlayout
        var pageLayoutSettings = {
            name: "pageLayout",
            fxName: "slide",		// none, slide, drop, scale
            fxSpeed_open: 800,
            fxSpeed_close: 1000,
            fxSettings_open: {easing: "easeInQuint"},
            fxSettings_close: {easing: "easeOutQuint"},
            spacing_open: 8,
            spacing_closed: 12,
            closable: true,
            resizable: false,
            center: {
                paneSelector: "#tabs",
                resizable: true,
                slidable: true,
                size: "auto"
            },
            south: {
                paneSelector: "#contentSouth",
                contentSelector: ".ui-widget-content",
                size: 0.1,
                resizable: true,
                slidable: true,
                initHidden: false,
                livePaneResizing: true,
                spacing_open: 8,
                spacing_closed: 12,
                resizeWithWindow: true,
                onresize: function () {
                    if (exist(outputEditor))
                        outputEditor.resize();
                }
            }
        };

        var contentLayoutSettings = {
            name: "contentLayout",
            spacing_open: 0,
            spacing_closed: 12,
            resizable: false,
            closable: false,
            initPanes: true,
            showDebugMessages:			true,
            contentSelector: ".ui-widget-content",
            north: {
                paneSelector: "#tabButtons"
            },
            center: {
                paneSelector: "#tabPanels",
                resizable: true,
                slidable: true,
                size: "auto",
                //	center panel contains a Tabs widget, with a layout inside 1 or more tab-panels
                onresize: $.layout.callbacks.resizeTabLayout
            },
            activate: $.layout.callbacks.resizeTabLayout
        };

        // SearchTab
        var searchLayoutSettings = {
            name: "searchLayout",
            size: "auto",
            minSize: 13,
            initClosed: false,
            initHidden: false,
            resizerTip: "Resize This Pane",
            fxName: "slide",
            fxSpeed_open: 800,
            fxSpeed_close: 1000,
            fxSettings_open: {easing: "easeInQuint"},
            fxSettings_close: {easing: "easeOutQuint"},
            closable: true,
            resizable: true,
            //slidable:				true,
            livePaneResizing: true,
            spacing_open: 8,
            spacing_closed: 12,
            initPanes: true,
            resizeWithWindow: false,
            contentSelector: ".ui-widget-content",
                north: {
                    paneSelector: "#searchNorth",
                    name: "searchNorthLayout",
                    size: 85,
                    fxSettings_open: {easing: "easeOutBounce"},
                    closable: false,
                    resizable: false,
                    slidable: false
                },
                center: {
                    paneSelector: "#searchCenter",
                    name: "searchCenterLayout",
                    minHeight: 80,
                    size: .8,
                    resizable: true,
                slidable: true
                        }
        };
        //AlfrescoTab
        var alfrescoLayoutSettings = {
            name: "alfrescoLayout",
            size: "auto",
            minSize: 13,
            initClosed: false,
            initHidden: false,
            resizerTip: "Resize This Pane",
            fxName: "slide",
            fxSpeed_open: 800,
            fxSpeed_close: 1000,
            fxSettings_open: {easing: "easeInQuint"},
            fxSettings_close: {easing: "easeOutQuint"},
            closable: true,
            resizable: true,
            //slidable:				true,
            livePaneResizing: true,
            spacing_open: 8,
            spacing_closed: 12,
            initPanes: true,
            resizeWithWindow: false,
            contentSelector: ".ui-widget-content",
            west: {
                paneSelector: "#alfrescoWest",
                name: "alfrescoWestLayout",
                size: .2,
                fxSettings_open: {easing: "easeOutBounce"},
                closable: true,
                resizable: true,
                slidable: true
            },
            center: {
                paneSelector: "#alfrescoCenter",
                name: "alfrescoCenterLayout",
                minHeight: 80,
                size: .8,
                children: {
                    name: "alfrescoCenterInnerLayout",
                    spacing_open: 8,
                    spacing_closed: 12,
                    north: {
                        paneSelector: "#alfrescoCenterNorth",
                        name: "alfrescoCenterNorthLayout",
                        minSize: 25,
                        maxSize: 25,
                        resizable: false,
                        closable: false,
                        children: {
                            name: "alfrescoCenterNorthInnerLayout",
                            center: {
                                size: "auto",
                                name: "alfrescoCenterNorthCenterLayout",
                                paneSelector: "#alfrescoCenterNorthCenter"
                            },
                            east: {
                                size: 90,
                                name: "alfrescoCenterNorthEastLayout",
                                paneSelector: "#alfrescoCenterNorthEast"
                            }
                        }
                    },
                    center: {
                        size: "auto",
                        name: "alfrescoCenterCenterLayout",
                        paneSelector: "#alfrescoCenterCenter",

                        children: {
                            resizable: true,
                            closable: false,
                            slidable: true,
                            spacing_open: 8,
                            spacing_closed: 12,
                            name: "alfrescoCenterCenterInnerLayout",
                            north: {
                                size: 225,
                                paneSelector: "#alfrescoCenterCenterNorth",
                                name: "alfrescoCenterCenterNorthLayout",
                                onresize: function () {
                                    try {
                                        calculateTableHeight("alfrescoCenterCenterNorth", alfrescoFolderTabelle, "dtable3", "alfrescoFolderTabelle", "alfrescoFolderTabelleHeader", "alfrescoFolderTableFooter");
                                    } catch (e) {
                                        errorHandler(e);
                                    }
                                }
                            },
                            center: {
                                size: "auto",
                                paneSelector: "#alfrescoCenterCenterCenter",
                                name: "alfrescoCenterCenterCenterLayout",
                                onresize: function () {
                                    try {
                                        calculateTableHeight("alfrescoCenterCenterCenter", alfrescoTabelle, "dtable2", "alfrescoTabelle", "alfrescoTabelleHeader", "alfrescoTableFooter");
                                      } catch (e) {
                                        errorHandler(e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        //VerteilungTab
        var verteilungLayoutSettings = {
            name: "verteilungLayout",
            size: "auto",
            minSize: 13,
            initClosed: false,
            resizerTip: "Resize This Pane",
            fxName: "slide",
            fxSpeed_open: 800,
            fxSpeed_close: 1000,
            fxSettings_open: {easing: "easeInQuint"},
            fxSettings_close: {easing: "easeOutQuint"},
            closable: true,
            resizable: true,
            //slidable:				true,
            livePaneResizing: true,
            spacing_open: 8,
            spacing_closed: 12,
            initPanes: true,
            resizeWithWindow: false,
            contentSelector: ".ui-widget-content",
            north: {
                paneSelector: "#verteilungNorth",
                minSize: 33
            },
            west: {
                paneSelector: "#verteilungWest",
                size: .4,
                fxSettings_open: {easing: "easeOutBounce"},
                closable: true,
                resizable: true,
                slidable: true,
                onresize: function () {
                    textEditor.resize();
                    $('div.dataTables_scrollBody').css('height', calcDataTableHeight());
                    tabelle.fnSettings()._iDisplayLength = Math.max(Math.floor((verteilungLayout.state.west.innerHeight - 24 - 26 - 20) / 29), 1);
                    tabelle.fnDraw();
                    tabelle.fnAdjustColumnSizing();
                }
            },
            center: {
                paneSelector: "#verteilungCenter",
                initHidden: false,
                minHeight: 80,
                size: .45,
                initClosed: false,
                onresize: function () {
                    rulesEditor.resize();
                }

            },
            east: {
                size:.15,
                paneSelector: "#verteilungEast",
                onresize: function () {
                    propsEditor.resize();
                }
            },
            //	enable state management
            stateManagement__enabled: false,
            showDebugMessages: true
        };


        // create the tabs before the page layout because tabs will change the height of the north-pane
        tabLayout = $("#tabs").tabs({
            // using callback addon
            activate: $.layout.callbacks.resizeTabLayout,
            beforeActivate: function (event, ui) {
                if (ui.newPanel.attr('id') == "tab2")
                    $('#alfrescoSearch').focus().select();
            },
            active : 1

            /* OR with a custom callback
             activate: function (evt, ui) {
             $.layout.callbacks.resizeTabLayout( evt, ui );
             // other code...
             }
             */
        });



        globalLayout = $('body').layout(clientPageLayoutSettings);

        $('#clientPage').layout(pageLayoutSettings);
        $('#tabs').layout(contentLayoutSettings);
        verteilungLayout = $('#tab3').layout(verteilungLayoutSettings);
        searchLayout = $('#tab2').layout(searchLayoutSettings);
        alfrescoLayout = $('#tab1').layout(alfrescoLayoutSettings);

        globalLayout.deleteCookie();
        globalLayout.options.stateManagement.autoSave = false;
        // if there is no state-cookie, then DISABLE state management initially
        var cookieExists = !$.isEmptyObject(verteilungLayout.readCookie());
        if (!cookieExists) toggleStateManagement(true, false);
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * baut die Alfresco Tabelle auf.
 */
function loadAlfrescoTable() {

    /**
     * zeigt das Dokument in einem neuen Tab an
     * @param obj     das Objekt
     * @param event   das Event
     */
    function openDocument(obj, event) {
        try {
            event.preventDefault();
            event.stopImmediatePropagation();
            var data = alfrescoTabelle.row($(obj).closest(('tr'))).data();
            var server = getSettings("server");
            if (!server.endsWith('/'))
                server = server + '/';
            var url = server + "service/api/node/content/workspace/" + data.nodeRef.substr(12) + "/" + data.contentStreamFileName;
            var erg = executeService("getTicket");
            if (erg.success)
                url = url + "?alf_ticket=" + erg.result.data.ticket;
            window.open(url);
        } catch (e) {
            errorHandler(e);
        }
    }

    try {
        $.fn.dataTable.moment('DD.MM.YYYY');
        alfrescoTabelle = $('#alfrescoTabelle').DataTable({
            jQueryUI: true,
            pagingType: "paging_with_jqui_icons",
            data: [],
            scrollX: "100%",
            scrollXInner: "100%",
            autoWidth: true,
            deferRender: true,
            processing: true,
            lengthChange: false,
            searching: false,
            order: [[3, 'desc']],
            select: {
                style: 'os'
            },
            columns: [
                {
                    class: 'details-control',
                    orderable: false,
                    data: null,
                    defaultContent: '',
                    width: "12px"
                },
                {
                    data: "contentStreamMimeType",
                    title: "Typ",
                    defaultContent: '',
                    type: "string",
                    class: "alignCenter alfrescoTableDragable",
                    width: "43px"
                },
                {
                    data: null,
                    title: "Vorschau",
                    orderable: false,
                    defaultContent: '',
                    type: "string",
                    class: "alignCenter",
                    width: "120px"
                },
                {
                    data: "title",
                    title: "Titel",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft alfrescoTableDragable"
                },
                {
                    data: "documentDate",
                    title: "Datum",
                    defaultContent: '',
                    //type: "date",
                    class: "alignLeft"
                },
                {
                    data: "person",
                    title: "Person",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: "amount",
                    title: "Betrag",
                    defaultContent: '',
                    type: "numeric",
                    class: "alignLeft"
                },
                {
                    data: "idvalue",
                    title: "Schlüssel",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: null,
                    title: "Aktion",
                    width: "102px",
                    class: "alignLeft"
                },
                {
                    data: "objectID"
                }
            ],
            columnDefs: [
                {
                    targets: [5, 6],
                    visible: true
                },

                {   targets: [9],
                    visible: false
                },

                {
                    targets: [1],
                    render: function (data, type, row) {
                        try {
                                if (exist(data) && data == "application/pdf") {
                                    var span = document.createElement("span");
                                    var url = location.href.substr(0, location.href.lastIndexOf('/')) + "/resources/images/pdf.png";
                                    var image = document.createElement('img');
                                    image.id = "alfrescoTableIcon" + row.objectID;
                                    image.className = "alfrescoTableIconEvent alfrescoTableDragable treeDropable";
                                    image.title = "PDF Dokument";
                                    image.draggable = true;
                                    image.style.cursor = "pointer";
                                    image.src =url;
                                    $('#alfrescoTabelle tbody').on( 'click', '#' + image.id, function (event) {
                                        openDocument(this, event);
                                    });
                                    span.appendChild(image);
                                    return span.outerHTML;
                                } else
                                    return "";
                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                    visible: true
                },
                {
                    targets: [2],
                    render: function (data, type, row) {
                        try {
                            if (exist(data)) {
                                var span = document.createElement("span");

                                span.href = "#";
                                span.style.width = "100px";
                                span.style.height = "100px";
                                var server = getSettings("server");
                                if (!server.endsWith('/'))
                                    server = server + '/';
                                var url = server + "service/api/node/workspace/" + row.nodeRef.substr(12) + "/content/thumbnails/doclib?c=queue&ph=true&alf_ticket=" + getAlfrescoTicket();
                                var image = document.createElement('img');
                                image.id = "alfrescoTableThumbnail" + row.objectID;
                                image.className = "alfrescoTableThumbnailEvent alfrescoTableDragable treeDropable";
                                image.draggable = true;
                                image.style.cursor = "pointer";
                                image.src =url;
                                $('#alfrescoTabelle tbody').on( 'click', '#' + image.id, function (event) {
                                    openDocument(this, event);
                                });
                                span.appendChild(image);
                                return span.outerHTML;
                            } else
                                return "";
                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                    visible: false
                },
                {
                    targets: [3],
                    render: function (data, type, row) {
                        if (exist(data))
                            return data;
                        else if (exist(row.name))
                            return row.name;
                        else
                            return "";
                    },
                    visible: true
                },
                {
                    targets: [4],
                    render: function (data, type, row) {
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
                    visible: true
                },
                {
                    targets: [6],
                    render: function (data, type, row) {
                        if (data) {
                            if (typeof data == "string" && data.indexOf(',') != -1)
                                return data;
                            else
                                return $.format.number(parseFloat(data), '#,##0.00');
                        }
                    },
                    visible: true
                },
                {
                    targets: [8],
                    render: function(data, types, row) {
                        return alfrescoAktionFieldFormatter(data, types, row).outerHTML;
                    },
                    orderable: false
                }
            ],
            language: {
                info: "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_",
                emptyTable: " ",
                paginate: {
                    first: "Erste ",
                    last:  "Letzte ",
                    next:  "Nächste ",
                    previous: "Vorherige "
                }
            }
        });

        $("#alfrescoTabelle_info").detach().appendTo('#alfrescoTableFooter');
        $("#alfrescoTabelle_paginate").detach().appendTo('#alfrescoTableFooter');

        // Add event listener for opening and closing details
        $('#dtable2 tbody').on('click', 'td.details-control', function () {
            var tr = $(this).closest('tr');
            var row = alfrescoTabelle.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
                calculateTableHeight("alfrescoCenterCenterCenter", alfrescoTabelle, "dtable2", "alfrescoTabelle", "alfrescoTabelleHeader", "alfrescoTableFooter");
            }
            else {
                // Open this row
                row.child(formatAlfrescoTabelleDetailRow(row.data())).show();
                tr.addClass('shown');
                calculateTableHeight("alfrescoCenterCenterCenter", alfrescoTabelle, "dtable2", "alfrescoTabelle", "alfrescoTabelleHeader", "alfrescoTableFooter");
            }
        });

        // Drag aus Tabelle
        $(document)
            .on('mousedown', '.alfrescoTableDragable', function (event) {
                try {
                    var nodes = [];
                    var selected = alfrescoTabelle.rows( {selected:true} ).data().toArray();
                    //prüfen, ob überhaupt etwas selektiert worden ist
                    if (!selected.length) {
                        var row = alfrescoTabelle.row($(this).closest(('tr')));
                        if (row)
                         selected.push(row.data());
                    }

                    for ( var index = 0; index < selected.length; ++index) {
                        if (selected[index])
                            nodes.push(selected[index].objectID)
                    }
                    if (nodes.length) {
                        var title = (selected.length > 1 ? (selected.length + " Dokumente") : exist(selected[0].title) ? selected[0].title : selected[0].name);
                        return $.vakata.dnd.start(event, {
                            'jstree': false,
                            'table': "alfrescoTabelle",
                            'obj': $(this),
                            'nodes': nodes
                        }, '<div id="jstree-dnd" class="jstree-default"><i class="jstree-icon jstree-er"></i>' + title + '</div>');
                    }
                } catch (e) {
                    errorHandler(e);
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
        alfrescoFolderTabelle = $('#alfrescoFolderTabelle').DataTable({
            jQueryUI: true,
            pagingType: "paging_with_jqui_icons",
            data: [],
            scrollX: "100%",
            scrollXInner: "100%",
            // "sScrollY" : calcDataTableHeight(),
            autoWidth: true,
            lengthChange: false,
            searching: false,
            select: {
                style: 'os'
            },
            rowCallback: function( row, data ) {
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
            order: [[2, 'desc']],
            columns: [
                {
                    class: 'folder-control treeDropable',
                    orderable: false,
                    data: null,
                    defaultContent: '',
                    width: "40px"
                },
                {
                    data: "name",
                    title: "Name",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft alfrescoFolderTableDragable treeDropable"
                },
                {
                    data: "description",
                    title: "Beschreibung",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft alfrescoFolderTableDragable treeDropable"
                },
                {
                    title: "Aktion",
                    data: null,
                    width: "120px",
                    class: "alignLeft",
                    orderable: false
                }
            ],
            columnDefs: [
                {
                    targets: [0],
                    orderable: false
                },
                {
                    targets: [1, 2],
                    visible: true
                },
                {
                    targets: [3],
                    render: function(data, type, row) {
                        return alfrescoFolderAktionFieldFormatter(data, type, row).outerHTML;
                    },
                    orderable: false
                }
            ],
            language: {
                info: "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_" ,
                emptyTable: " "
            }
        });

        // Drag aus Tabelle
        $(document)
            .on('mousedown', '.alfrescoFolderTableDragable', function (event) {
                try {
                    var nodes = [];
                    var selected = alfrescoFolderTabelle.rows( {selected:true} ).data().toArray();
                    //prüfen, ob überhaupt etwas selektiert worden ist
                    if (!selected.length) {
                        var row = alfrescoFolderTabelle.row($(this).closest(('tr')));
                        if (row)
                            selected.push(row.data());
                    }

                    for( var index = 0; index < selected.length; ++index) {
                        if (selected[index])
                            nodes.push(selected[index].objectID)
                    }
                    if (nodes.length) {
                        var title = (selected.length > 1 ? (selected.length + " Ordner") : exist(selected[0].title) ? selected[0].title : selected[0].name);
                        return $.vakata.dnd.start(event, {
                            'jstree': false,
                            'table': "alfrescoFolderTabelle",
                            'obj': $(this),
                            'nodes': nodes
                        }, '<div id="jstree-dnd" class="jstree-default"><i class="jstree-icon jstree-er"></i>' + title + '</div>');
                    }
                } catch (e) {
                    errorHandler(e);
                }
            });

    } catch (e) {
        errorHandler(e);
    }
    $("#alfrescoFolderTabelle_info").detach().appendTo('#alfrescoFolderTableFooter');
    $("#alfrescoFolderTabelle_paginate").detach().appendTo('#alfrescoFolderTableFooter');
}

/**
 * baut die die Tabelle für die Suchergebnisse auf.
 */
function loadAlfrescoSearchTable() {

    /**
     * öffnet ein Dokument
     * @param obj
     * @param event
     */
    function openDocument(obj, event) {
        try {
            event.preventDefault();
            event.stopImmediatePropagation();
            var data = alfrescoSearchTabelle.row($(obj).closest(('tr'))).data();
            var server = getSettings("server");
            if (!server.endsWith('/'))
                server = server + '/';
            var url = server + "service/api/node/content/workspace/" + data.nodeRef.substr(12) + "/" + data.contentStreamFileName;
            var result = executeService("getTicket");
            if (result.success)
                url = url + "?alf_ticket=" + result.result.data.ticket;
            window.open(url);
        } catch (e) {
            errorHandler(e);
        }
    }


    try {
        $.fn.dataTable.moment('DD.MM.YYYY');
        alfrescoSearchTabelle = $('#alfrescoSearchTabelle').DataTable({
            jQueryUI: true,
            pagingType: "paging_with_jqui_icons",
            data: [],
            scrollX: "100%",
            scrollXInner: "100%",
            autoWidth: true,
            lengthChange: false,
            searching: false,
            order: [[3, 'desc']],
            columns: [
                {
                    class: 'details-control',
                    orderable: false,
                    data: null,
                    defaultContent: '',
                    width: "12px"
                },
                {
                    data: "contentStreamMimeType",
                    title: "Typ",
                    defaultContent: '',
                    type: "string",
                    class: "alignCenter",
                    width: "43px"
                },
                {
                    data: null,
                    title: "Vorschau",
                    orderable: false,
                    defaultContent: '',
                    type: "string",
                    class: "alignCenter",
                    width: "120px"
                },
                {
                    data: "title",
                    title: "Titel",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: "documentDate",
                    title: "Datum",
                    defaultContent: '',
                    //type: "date",
                    class: "alignLeft"
                },
                {
                    data: "person",
                    title: "Person",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: "amount",
                    title: "Betrag",
                    defaultContent: '',
                    type: "numeric",
                    class: "alignLeft"
                },
                {
                    data: "idvalue",
                    title: "Schlüssel",
                    defaultContent: '',
                    type: "string",
                    class: "alignLeft"
                },
                {
                    data: null,
                    title: "Aktion",
                    width: "102px",
                    class: "alignLeft"
                },
                {
                    data: "objectID"
                }
            ],
            columnDefs: [
                {
                    targets: [5, 6],
                    visible: true
                },

                {   targets: [9],
                    visible: false
                },

                {
                    targets: [1],
                    render: function (data, type, row) {
                        try {
                            if (exist(data) && data == "application/pdf") {
                                var span = document.createElement("span");
                                var url = location.href.substr(0, location.href.lastIndexOf('/')) + "/resources/images/pdf.png";
                                var image = document.createElement('img');
                                image.id = "alfrescoSearchTableIcon" + row.objectID;
                                image.className = "alfrescoSearchTableIconEvent";
                                image.title = "PDF Dokument";
                                image.draggable = true;
                                image.style.cursor = "pointer";
                                image.src =url;
                                $('#alfrescoSearchTabelle tbody').on( 'click', '#' + image.id, function (event) {
                                    openDocument(this, event);
                                });
                                span.appendChild(image);
                                return span.outerHTML;
                            } else
                                return "";
                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                    visible: true
                },
                {
                    targets: [2],
                    render: function (data, type, row) {
                        try {
                            if (exist(data)) {
                                var span = document.createElement("span");

                                span.href = "#";
                                span.style.width = "100px";
                                span.style.height = "100px";
                                var server = getSettings("server");
                                if (!server.endsWith('/'))
                                    server = server + '/';
                                var url = server + "service/api/node/workspace/" + row.nodeRef.substr(12) + "/content/thumbnails/doclib?c=queue&ph=true&alf_ticket=" + getAlfrescoTicket();
                                var image = document.createElement('img');
                                image.id = "alfrescoSearchTableThumbnail" + row.objectID;
                                image.className = "alfrescoSearchTableThumbnailEvent";
                                image.draggable = true;
                                image.style.cursor = "pointer";
                                image.src =url;
                                $('#alfrescoSearchTabelle tbody').on( 'click', '#' + image.id, function (event) {
                                    openDocument(this, event);
                                });
                                span.appendChild(image);
                                return span.outerHTML;
                            } else
                                return "";
                        } catch (e) {
                            errorHandler(e);
                        }
                    },
                    visible: false
                },
                {
                    targets: [3],
                    render: function (data, type, row) {
                        if (exist(data))
                            return data;
                        else if (exist(row.name))
                            return row.name;
                        else
                            return "";
                    },
                    visible: true
                },
                {
                    targets: [4],
                    render: function (data, type, row) {
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
                    visible: true
                },
                {
                    targets: [6],
                    render: function (data, type, row) {
                        if (data) {
                            if (typeof data == "string" && data.indexOf(',') != -1)
                                return data;
                            else
                                return $.format.number(parseFloat(data), '#,##0.00');
                        }
                    },
                    visible: true
                },
                {
                    targets: [8],
                    render: function(data, type, row) {
                        
                        var container = alfrescoAktionFieldFormatter(data, type, row);
                        var image = document.createElement("div");
                        image.href = "#";
                        image.className = "detailAim";
                        image.style.backgroundImage = "url(resources/images/ziel.png)";
                        image.title = "Dokument im Ordner anzeigen";
                        image.style.cursor = "pointer";
                        image.style.width = "16px";
                        image.style.height = "16px";
                        image.style.cssFloat = "left";
                        image.style.marginRight = "5px";
                        container.appendChild(image);
                        return container.outerHTML;
                    },
                    orderable: false
                }
            ],
            language: {
                info: "Zeigt Einträge _START_ bis _END_ von insgesamt _TOTAL_",
                emptyTable: "Keine Ergebnisse gefunden",
                paginate: {
                    first: "Erste ",
                    last:  "Letzte ",
                    next:  "Nächste ",
                    previous: "Vorherige "
                }
            }
        });

        $("#alfrescoSearchTabelle_info").detach().appendTo('#alfrescoSearchTableFooter');
        $("#alfrescoSearchTabelle_paginate").detach().appendTo('#alfrescoSearchTableFooter');

        // Add event listener for opening and closing details
        $('#dtable4 tbody').on('click', 'td.details-control', function () {
            var tr = $(this).closest('tr');
            var row = alfrescoSearchTabelle.row(tr);

            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
                calculateTableHeight("searchCenter", alfrescoSearchTabelle, "dtable4", "alfrescoSearchTabelle", "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
            }
            else {
                // Open this row
                row.child(formatAlfrescoTabelleDetailRow(row.data())).show();
                tr.addClass('shown');
                calculateTableHeight("searchCenter", alfrescoSearchTabelle, "dtable4", "alfrescoSearchTabelle", "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
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
                    "render": function(data,types, row) { 
                        return imageFieldFormatter(data, types, row).outerHTML;},
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
                row.child(formatVerteilungTabelleDetailRow(row.data())).show();
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
 * @param data
 * @param type
 * @param full
 * @return {string}
 */
function alfrescoFolderAktionFieldFormatter(data, type, full) {
    try {

        var container = document.createElement("div");
        var image;

        // Ordner bearbeiten
        image = document.createElement("div");
        image.href = "#";
        image.className = "folderEdit";
        image.style.backgroundImage = "url(resources/images/beautify16.png)";
        image.title = "Ordner Details bearbeiten";
        image.style.cursor = "pointer";
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);
        
        // neuen Ordner im ausgewhlten Ordner anlegen
        if (data.objectID != alfrescoRootFolderId &&
            data.objectID != archivFolderId &&
            data.objectID != fehlerFolderId &&
            data.objectID != unknownFolderId &&
            data.objectID != doubleFolderId &&
            data.objectID != inboxFolderId) {
            image = document.createElement("div");
            image.href = "#";
            image.className = "folderCreate";
            image.style.backgroundImage = "url(resources/images/folder_add.png)";
            image.title = "neuen Ordner anlegen";
            image.style.cursor = "pointer";
            image.style.width = "16px";
            image.style.height = "16px";
            image.style.cssFloat = "left";
            image.style.marginRight = "5px";
            container.appendChild(image);
        }
        
        // ausgewählten Ordner löschen
        if (data.objectID != alfrescoRootFolderId &&
            data.objectID != archivFolderId &&
            data.objectID != fehlerFolderId &&
            data.objectID != unknownFolderId &&
            data.objectID != doubleFolderId &&
            data.objectID != inboxFolderId &&
            data.objectID != documentFolderId) {
            image = document.createElement("div");
            image.href = "#";
            image.className = "folderRemove";
            image.style.backgroundImage = "url(resources/images/folder_remove.png)";
            image.title = "Ordner löschen";
            image.style.cursor = "pointer";
            image.style.width = "16px";
            image.style.height = "16px";
            image.style.cssFloat = "left";
            image.style.marginRight = "5px";
            container.appendChild(image);
        }

        return container;
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert die Iconspalte in der AlfrescoTabelle
 * @param data
 * @param type
 * @param full
 * @returns {string}
 */
function alfrescoAktionFieldFormatter(data, type, full) {
    try {
        var container = document.createElement("div");
        var image = document.createElement("div");
        image.href = "#";
        image.className = "detailEdit";
        image.style.backgroundImage = "url(resources/images/beautify16.png)";
        image.title = "Details bearbeiten";
        image.style.cursor = "pointer";
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        image = document.createElement("div");
        image.href = "#";

        if (data.commentCount > 0) {
            image.style.backgroundImage = "url(resources/images/forum-16.gif)";
            image.style.cursor = "pointer";
            image.className = "showComments";
        }
        else {
            image.style.backgroundImage = "url(resources/images/forum-16-bw.gif)";
            image.style.cursor = "none";
        }
        image.title = "Kommentare";
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        image = document.createElement("div");
        image.href = "#";
        image.className = "deleteDocument";
        image.style.backgroundImage = "url(resources/images/deleteDocument.gif)";
        image.title = "Dokument löschen";
        image.style.cursor = "pointer";
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        image = document.createElement("div");
        image.href = "#";
        image.className = "rulesDocument";
        image.style.backgroundImage = "url(resources/images/rules.png)";
        image.title = "Dokument Regel erstellen";
        image.style.cursor = "pointer";
        image.style.width = "16px";
        image.style.height = "16px";
        image.style.cssFloat = "left";
        image.style.marginRight = "5px";
        container.appendChild(image);

        return container;
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
            image.style.backgroundImage = "url(resources/images/error.png)";
            image.title = "Verteilung fehlerhaft";
        } else {
            image.style.backgroundImage = "url(resources/images/ok.png)";
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
        image.style.backgroundImage = "url(resources/images/glass.png)";
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
            image.style.backgroundImage = "url(resources/images/delete.png)";
            image.style.cursor = "pointer";
        }
        else {
            image.style.backgroundImage = "url(resources/images/delete-bw.png)";
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
            image.style.backgroundImage = "url(resources/images/pdf.png)";
            image.style.cursor = "pointer";
        } else {
            image.style.backgroundImage = "url(resources/images/pdf-bw.png)";
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
        image.style.backgroundImage = "url(resources/images/move-file.png)";
        image.style.cursor = "pointer";
        image.style.cssFloat = "left";
        image.style.width = "16px";
        image.style.height = "16px";
        // image.style.marginRight = "5px";
        image.title = "Zur Inbox verschieben";
        container.appendChild(image);
        return container;
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * formatiert die Fehlerdetails in der zusätzlichen Zeile(n) der VerteilungsTabelle
 * @param data         Das Data Object der Zeile
 * @returns {string}   HTML für die extra Zeile
 */
function formatVerteilungTabelleDetailRow(data) {
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
 * formatiert die zusätzlichen Zeile(n) der AlfrescoTabelle
 * @param data         Das Data Object der Zeile
 * @returns {string}   HTML für die extra Zeile
 */
function formatAlfrescoTabelleDetailRow(data) {
    return 'Name: ' + data.name + ' erstellt am: ' + $.formatDateTime('dd.mm.yy hh:ii:ss', new Date(Number(data.creationDate))) + ' von: ' + data.createdBy + (data.lastModificationDate == data.creationDate ? '' : ' modifiziert am: ' + $.formatDateTime('dd.mm.yy hh:ii:ss', new Date(Number(data.lastModificationDate))) + ' von: ' + data.lastModifiedBy) + ' Version: ' + data.versionLabel + ' ' + (exist(data.checkinComment) ? data.checkinComment : '');
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
    var tree = $("#tree").jstree(true);
    var oldLi = $('#breadcrumblist');
    if (exist(oldLi))
        oldLi.remove();
    var container = $('#breadcrumb');
    var ul = document.createElement('ul');
    ul.id = 'breadcrumblist';
    do {
        if (!exist(data)) {
            object = ["Archiv"];
            id = archivFolderId;
            parentObj = null;
            name = "Archiv";
        }
        else {
            object = data.path.split('/');
            id = data.objectID;
            parentObj = data.parentId;
            name = data.name;
        }
        var li = document.createElement('li');
        li.data  = {
            'objectID': id,
            'path': object.join('/'),
            'name': name,
            'parentId': parentObj
        };
        li.id = id;
        li.onclick = function () {
            tree.deselect_all(false);
            switchAlfrescoDirectory(this.data);
        };
        $.data( li, "data", data);
        var a = document.createElement('a');
        a.href = '#';
        a.text = name;
        li.appendChild(a);
        if (parentObj == null)
            fill = false;
        else
            data = tree.get_node(parentObj).data;
        ul.insertBefore(li, ul.firstChild);
    } while (fill);
    container.append(ul);
}

/**
 * führt die Aktualisierungen für eine Verzeichniswechsel im Alfresco durch
 * @param data      das Datenobjekt des ausgewählten Folders
 */
function switchAlfrescoDirectory(data) {
    try {
        var objectID;
        var times = [];
        if (exist(data))
            objectID = data.objectID;
        else
            objectID = "-1";
        times.push(new Date().getTime());
        var json = executeService("listFolder", [
            {"name": "filePath", "value": objectID},
            {"name": "withFolder", "value": -1}
        ], "Verzeichnis konnte nicht aus dem Server gelesen werden:");
        if (json.success) {
            alfrescoFolderTabelle.clear();
            alfrescoFolderTabelle.rows.add(json.result).draw();
            calculateTableHeight("alfrescoCenterCenterNorth", alfrescoFolderTabelle, "dtable3", "alfrescoFolderTabelle", "alfrescoFolderTabelleHeader", "alfrescoFolderTableFooter");
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
                            {"name": "documentId", "value": data.objectID},
                            {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                        ], null, true);
                        if (erg.success) {
                            var node = $(document.getElementById(data.objectID));
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
            //$("#tree").jstree(true).refresh_node(objectID);
            $("#tree").jstree('select_node', objectID);
        }
        json = executeService("listFolder", [
            {"name": "filePath", "value": objectID},
            {"name": "withFolder", "value": "1"}
        ], "Dokumente konnten nicht aus dem Server gelesen werden:");
        if (json.success) {
            alfrescoTabelle.clear();
            alfrescoTabelle.rows.add(json.result).draw();
            times.push(new Date().getTime());
            REC.log(DEBUG, "SwitchDirectory: " + (times[1] -times[0]) + " ms");
            fillMessageBox(true);
            calculateTableHeight("alfrescoCenterCenterCenter", alfrescoTabelle, "dtable2", "alfrescoTabelle", "alfrescoTabelleHeader", "alfrescoTableFooter");
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
                        placeholder: "",
                        type: 'datepicker',
                        datepicker: {
                            "dateFormat": "dd.mm.yy" 
                        }
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
                //sSuccessResponse: "IGNORE", // keine Meldungen nach dem Editieren
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
                            value = data.documentDate;
                        } else if (this.cellIndex == 4) {
                            // Person geändert
                            data.person = value;
                        } else if (this.cellIndex == 5) {
                            // Betrag geändert
                            if (value.indexOf(',') != -1) {
                                data.amount = value.replace(/\./g, '').replace(/,/g, ".");  	
                                //value = data.amount;
                            }
                            else 
                                data.amount = value;
                        } else if (this.cellIndex == 6) {
                            // Id geändert
                            data.idvalue = value;
                        }
                        extraProperties = {
                            'P:cm:titled': {'cm:title': data.title, 'cm:description': data.description},
                            'D:my:archivContent': {'my:documentDate':data.documentDate, 'my:person': data.person},
                            'P:my:amountable': {'my:amount': data.amount, "my:tax": data.tax},
                            'P:my:idable': {'my:idvalue': data.idvalue}
                        };
                        var erg = executeService("updateProperties", [
                            {"name": "documentId", "value": data.objectID},
                            {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                        ], null, true);
                        if (erg.success) {
                            data = $.parseJSON(erg.result);
                            alfrescoTabelle.row($(this).closest('tr')).data(data);
                            return(value);
                        }
                        else
                            return "Dokument konnte nicht aktualisiert werden!" + "<br>" + erg.result;
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            });

        }
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * startet eine Suche im Alfresco Repository
 * @param searchText    der zu suchende Text
 */
function startSearch(searchText) {
    try {
        var sql = "SELECT * FROM cmis:document WHERE IN_TREE('" + archivFolderId + "') AND CONTAINS('" + searchText + "')";
        var json = executeService("findDocument", [
            {"name": "cmisQuery", "value": sql}
        ], null, true);
        if (json.success) {
            if (json.result.length == 0)
                message("Suche", "Keine Dokumente gefunden", 4000, 100, 300);
            alfrescoSearchTabelle.clear();
            alfrescoSearchTabelle.rows.add(json.result).draw();
            calculateTableHeight("searchCenter", alfrescoSearchTabelle, "dtable4", "alfrescoSearchTabelle", "alfrescoSearchTabelleHeader", "alfrescoSearchTableFooter");
            $.fn.dataTable.makeEditable(alfrescoSearchTabelle, {
                "fnShowError": function (text, aktion) {
                    message("Fehler", text);
                },
                "aoColumns": [null,
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
                        var data = alfrescoSearchTabelle.row($(this).closest('tr')).data();
                        if (this.cellIndex == 2) {
                            // Titel geändert
                            data.title = value
                        } else if (this.cellIndex == 3) {
                            // Datum geändert
                            data.documentDate = $.datepicker.parseDate("dd.mm.yy", value).getTime();
                            value = data.documentDate;
                        } else if (this.cellIndex == 4) {
                            // Person geändert
                            data.person = value;
                        } else if (this.cellIndex == 5) {
                            // Betrag geändert
                            if (value.indexOf(',' != -1)) {
                                data.amount = value.replace(/\./g, '').replace(/,/g, ".");
                                value = data.amount;
                            }
                        } else if (this.cellIndex == 6) {
                            // Id geändert
                            data.idvalue = value;
                        }
                        extraProperties = {
                            'P:cm:titled': {'cm:title': data.title, 'cm:description': data.description},
                            'D:my:archivContent': {'my:documentDate': data.documentDate, 'my:person': data.person},
                            'P:my:amountable': {'my:amount': data.amount, "my:tax": data.tax},
                            'P:my:idable': {'my:idvalue': data.idvalue}
                        };
                        erg = executeService("updateProperties", [
                            {"name": "documentId", "value": data.objectID},
                            {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                        ], null, true);
                        if (erg.success) {
                            data = $.parseJSON(erg.result);
                            alfrescoSearchTabelle.row($(this).closest('tr')).data(data);
                            return(value);
                        }
                        else
                            return "Dokument konnte nicht aktualisiert werden!" + "<br>" + erg.result;
                    } catch (e) {
                        errorHandler(e);
                    }
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
            startFolderDialog(alfrescoFolderTabelle.row(tr).data(), "web-create");
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".folderRemove", function () {
        try {
            var tr = $(this).closest('tr');
            startFolderDialog(alfrescoFolderTabelle.row(tr).data(), "web-display");
        } catch (e) {
            errorHandler(e);
        }
    });
    $(document).on("click", ".folderEdit", function () {
        try {
            var tr = $(this).closest('tr');
            startFolderDialog(alfrescoFolderTabelle.row(tr).data(), "web-edit");
        } catch (e) {
            errorHandler(e);
        }
    });
}

/**
 * behandelt die Clicks auf die Icons in der Alfrescotabelle
 */
function handleAlfrescoImageClicks() {
    // Details bearbeiten
    $(document).on("click", ".detailEdit", function () {
        try {
            var tr = $(this).closest('tr');
            startDocumentDialog($('#' + tr[0].parentElement.parentElement.id).DataTable().row(tr).data(), "web-edit");
        } catch (e) {
            errorHandler(e);
        }
    });
    // Kommentare lesen
    $(document).on("click", ".showComments", function () {
        try {
            var tr = $(this).closest('tr');
            // Kommentare lesen
            var obj = executeService("getTicket");
            if (obj.success) {
                var json = executeService("getComments", [
                    {
                        "name": "documentId",
                        "value": $('#' + tr[0].parentElement.parentElement.id).DataTable().row(tr).data().objectID
                    },
                    {"name": "ticket", "value": obj.result.data.ticket}
                ], ["Kommentare konnten nicht gelesen werden!"]);
                if (json.success) {
                    startCommentsDialog(json.result);
                }
            }
        } catch (e) {
            errorHandler(e);
        }
    });
    // Dokument löschen
    $(document).on("click", ".deleteDocument", function () {
        try {
            var tr = $(this).closest('tr');
            var tabelle = $('#' + tr[0].parentElement.parentElement.id).DataTable();
            var id = tabelle.row(tr).data().objectID;
            var $dialog = $('<div></div>').html("Ausgewähltes Dokument " + tabelle.row(tr).data().name + " löschen?").dialog({
                autoOpen: true,
                title: "Dokument löschen",
                modal: true,
                height: 150,
                width: 400,
                buttons: {
                    "Ok": function () {
                        try {
                            $(this).dialog("destroy");
                            var erg = executeService("deleteDocument", [
                                {"name": "documentId", "value": tabelle.row(tr).data().objectID}
                            ], ["Dokument konnte nicht gelöscht werden!"]);
                            if (erg.success) {
                                tabelle.rows().invalidate();
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
    // Regeln
    $(document).on("click", ".rulesDocument", function () {
        try {
            var tr = $(this).closest('tr');
            var tabelle = $('#' + tr[0].parentElement.parentElement.id).DataTable();
            var id = tabelle.row(tr).data().objectID;
            var json = executeService("getDocumentContent", [
                {"name": "documentId", "value": tabelle.row(tr).data().objectID},
                {"name": "extract", "value": "true"}
            ], ["Dokument konnten nicht gelesen werden!"]);
            if (json.success) {
                loadText(json.result, json.result, tabelle.row(tr).data().name, tabelle.row(tr).data().contentStreamMimeType, null);
                tabLayout.tabs("option", "active", 2);
            }
        } catch (e) {
            errorHandler(e);
        }

    });
    // Ziel im Ordner suchen
    $(document).on("click", ".detailAim", function () {
        try {
            var results = [];
            var tree = $('#tree').jstree(true);
            var tr = $(this).closest('tr');
            var tabelle = $('#' + tr[0].parentElement.parentElement.id).DataTable();
            var data = tabelle.row(tr).data();
            var id = data.objectID;
            if (data && data.parents) {
                var node = tree.get_node(data.parents[0]);
                while (!node) {
                    var json = executeService("getNodeById", [
                        {"name": "documentId", "value": data.parents[0]}
                    ], ["Dokument konnten nicht gelesen werden!"]);
                    if (json.success) {
                        data = json.result;
                        results.push(data);
                        if (data && data.parents )
                            node = tree.get_node(data.parents[0]);
                    } else {
                        break;
                    }
                }
            }
            if (node) {
                if (!results.length) {
                    tree.deselect_all(true);
                    tree.select_node(node, true);
                    tree.open_node(node, function(){
                        switchAlfrescoDirectory(node.data);
                        var row = alfrescoTabelle.row('#' + id);
                        if (row) {
                            row.draw().show().draw(false);
                            row.select();
                        }    
                    });
            
                }
                else {
                    results.push(node.data);
                    results.reverse();
                    // Hier muss mit einem Deferred Object gearbeitet werden, denn der open im Tree
                    // bewirkt einen asynchronen Aufruf, so das die nachfolgenden Operationen sonst nicht
                    // die notwendigen Daten haben.
                    var deffereds = $.Deferred(function (def) {
                        def.resolve();
                    });

                    for(var index = 0; index < results.length; index++) {
                        deffereds = (function(name, last, id, deferreds) {
                            return deferreds.then(function () {
                                return $.Deferred(function(def) {
                                    var node = tree.get_node(name.objectID);
                                    tree.open_node(node, function (last) {
                                        def.resolve();
                                        if (last){
                          
                                            node = tree.get_node(results[results.length - 1].objectID);
                                            tree.deselect_all(true);
                                            tree.select_node(node, true);
                                            tree.open_node(node, function(){
                                                switchAlfrescoDirectory(node.data);
                                                var row = alfrescoTabelle.row('#' + id);
                                                if (row) {
                                                    row.draw().show().draw(false);
                                                    row.select();
                                                }
                                            });
                                        }
                                           
                                    });
                                });
                            });
                        })(results[index], index == results.length -1, id, deffereds);
                    }
       
                }
                tabLayout.tabs("option", "active", 0);
            }

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
            //TODO das muss anders gemacht werden
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
                {"name": "documentId", "value": inboxFolderId},
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
 * baut ein Tree kompatibles Objekt aus den übergebenen Daten auf
 * @param data    das übergebene Objekt
 * @return {{}}   das Tree kompatible Objekt
 */
function buildObjectForTree(data) {
    var item = {};
    if (data.baseTypeId == "cmis:folder") {
        // Eintrag ist vom Typ Folder
        item["icon"] = "";
        item["state"] = {"opened": false, "disabled": false, "selected": false};
        // Typen definieren
        item["type"] = "documentFolderStandard";
        if (data.objectID == alfrescoRootFolderId) {
            // Alfresco Root Folder
            item["type"] = "alfrescoRootFolderStandard";
        }
        if (data.objectID == archivFolderId) {
            // Archiv Folder
            item["type"] = "archivRootStandard";
        }
        if (data.objectID == inboxFolderId ||
            data.objectID == unknownFolderId) {
            // Die Standard Folder
            item["type"] = "archivFolderStandard";
        }
        if (data.objectID == fehlerFolderId) {
            // Fehler Folder
            item["type"] = "archivFehlerFolderStandard";
        }
        if (data.objectID == doubleFolderId) {
            // Fehler Folder
            item["type"] = "archivDoubleFolderStandard";
        }
        if (data.objectID == documentFolderId) {
            // Fehler Folder
            item["type"] = "archivDocumentFolderStandard";
        }
    } else {
        // Eintrag ist vom Typ Document
        item["icon"] = "";
        item["state"] = "";
    }
    item["id"] = data.objectID;
    item["children"] = data.hasChildFolder;
    item["text"] = data.name;
    item["data"] = data;
    item["a_attr"] = "'class': 'drop'";
    return item;
}


/**
 * Funktion wird beim Knotenwechsel aufgerufen.
 * Entweder ist ein Knoten angeben und der Inhalt des dazugehörigen Folder wird gelesen,
 * oder falls nicht dann wird der Inhalt des Root Folders gelesen.
 * Die Gefundenen Objekte (also die entsprechenden Subfolder) werden in jstree kompatible JSON Obekte konvertiert
 * @param aNode      der ausgewählte Knoten
 * @return obj       die Daten als jstree kompatible JSON Objekte
 */
function loadAndConvertDataForTree(aNode) {
    var obj = {};

    try {
        // keine Parameter mit gegeben, also den Rooteintrag erzeugen
        if (!exist(aNode)) {
            return [
                {
                    "icon": "",
                    "id": archivFolderId,
                    "text": "Archiv",
                    "state": {"opened": false, "disabled": false, "selected": false},
                    "children": true,
                    "type": "archivRootStandard"
                }
            ]
        } else {
            if (alfrescoServerAvailable) {
                // den Folder einlesen
                var json = executeService("listFolder", [
                    {"name": "filePath", "value": aNode.id != "#" ? aNode.id : archivFolderId},
                    {"name": "withFolder", "value": -1}
                ], "Verzeichnis konnte nicht aus dem Server gelesen werden:");
                if (json.success) {
                    obj = [];
                    for (var index = 0; index < json.result.length; index++) {
                        obj.push(buildObjectForTree(json.result[index]));
                    }
                    if (aNode.id == "#")
                        return [
                            {
                                "icon": "",
                                "id": archivFolderId,
                                "text": "Archiv",
                                "state": {"opened": true, "disabled": false, "selected": true},
                                "children": obj,
                                "type": "archivRootStandard"
                            }
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
 * lädt den Alfresco Tree
 */
function loadAlfrescoTree() {

    /**
     * prüft, ob das gedragte Objekt auf das Ziel Objekt gezogen werden kann
     * @param data       die Daten des Events
     * @return {boolean} true, der Knoten ist gültig
     */
    function checkMove(data) {
        var sourceData, targetData;
        var erg = false;
        var t = $(data.event.target);
        // prüfen, ob das Element entweder auf eine passende Tabellenzeile (treeDropable)
        // oder auf einen Tree Knoten (jstree-anchor) gezogen werden soll.
        if (t.closest('.treeDropable').length || t.closest('.jstree-anchor').length) {
            for(var index = 0; index < data.data.nodes.length; ++index) {
                erg = false;
                if (data.data.jstree) {
                    // Quelle ist der Tree
                    sourceData = $.jstree.reference('#tree').get_node(data.data.nodes[index]).data;
                } else {
                    // Quelle ist die Tabelle
                    // die Daten aus der Tabellenzeile, also der Folder, in den verschoben werden soll
                    if (data.data.table == "alfrescoFolderTabelle") {
                        sourceData = alfrescoFolderTabelle.row('#' + data.data.nodes[index]).data();
                    } else if (data.data.table == "alfrescoTabelle") {
                        sourceData = alfrescoTabelle.row('#' + data.data.nodes[index]).data();
                    }
                }
                if (data.event.target.className.indexOf("jstree-anchor") != -1) {
                    // Ziel ist der Tree
                    // der Zielknoten
                    targetData = $.jstree.reference('#tree').get_node(t).data;
                } else if (data.event.target.className.indexOf("treeDropable") != -1 && data.event.target.className.indexOf("alfrescoTableDragable") == -1) {
                    // Ziel ist die Tabelle
                    // die Ziel Zeile
                    var row = alfrescoFolderTabelle.row(t.closest('.treeDropable')[0].parentElement);
                    if (row)
                        targetData = row.data();
                }
                // Object darf nicht in die Standard Ordner geschoben werden und die Standard Ordner dürfen generell nicht verschoben werden
                if (sourceData &&
                        // der zu verschiebene Ordner darf nicht der ArchivRootFolder sein
                    sourceData.objectID != archivFolderId &&
                        // der zu verschiebene Ordner darf nicht der Dokumenten Folder sein
                    sourceData.objectID != documentFolderId &&
                        // der zu verschiebene Ordner darf nicht derFehler Folder sein
                    sourceData.objectID != fehlerFolderId &&
                        // der zu verschiebene Ordner darf nicht der Inbox Folder sein
                    sourceData.objectID != inboxFolderId &&
                        // der zu verschiebene Ordner darf nicht der Unknown Folder sein
                    sourceData.objectID != unknownFolderId &&
                        // der zu verschiebene Ordner darf nicht der Doppelte Folder sein
                    sourceData.objectID != doubleFolderId &&
                    targetData &&
                        // Dokumente dürfen nicht direkt in den Dokumenten Ordner
                    (sourceData.baseTypeId == "cmis:folder" || (sourceData.baseTypeId == "cmis:document" && targetData.objectID != documentFolderId)) &&
                        // Ziel für Objekte darf nicht AlfrescoRootFolder sein
                    targetData.objectID != alfrescoRootFolderId &&
                        // Ziel für den Objekte darf nicht der ArchivRootFolder sein
                    targetData.objectID != archivFolderId &&
                        // Ziel für Ordner darf nicht Fehler Folder sein
                    (sourceData.baseTypeId == "cmis:document" || (sourceData.baseTypeId == "cmis:folder" && targetData.objectID != fehlerFolderId)) &&
                        // Ziel für den Ordner darf nicht der Folder für die Doppelten sein
                    (sourceData.baseTypeId == "cmis:document" || (sourceData.baseTypeId == "cmis:folder" && targetData.objectID != doubleFolderId)) &&
                        // Ziel für den Ordner darf nicht  Unbekannten Folder sein
                    (sourceData.baseTypeId == "cmis:document" || (sourceData.baseTypeId == "cmis:folder" && targetData.objectID != unknownFolderId)) &&
                        // Ziel für den Ordner darf nicht der Inbox Folder sein
                    (sourceData.baseTypeId == "cmis:document" || (sourceData.baseTypeId == "cmis:folder" && targetData.objectID != inboxFolderId)) &&
                        // Ziel für den Ordner darf nicht Parent Folder sein, da ist er nämlich schon drin
                    targetData.objectID != sourceData.parentId &&
                        // Ziel für den Ordner darf nicht derselbe oder ein eigener Child Folder sein, denn sonst würde der Knoten "entwurzelt"
                    getAllChildrenIds($.jstree.reference('#tree'), sourceData.objectID).indexOf(targetData.objectID) == -1) {
                    erg = true;
                }
                if (!erg)
                    break;
            }
        }
        return erg;
    }

    /**
     * sammelt alle Ids des Knoten und seiner Children
     * die Methode dient zum Prüfen ob ein Knoten in einen anderen verschoben werden kann.
     * Das geht nämlich nicht wenn der Ziel Knoten ein Children des zu verschiebenden Knotens ist
     * @param treeObj        die Referenz auf den Tree
     * @param nodeId         die Id des zu verschiebenden Knoten
     * @return {Array}       ein Array mit den Ids der Childknoten
     */
    function getAllChildrenIds(treeObj, nodeId) {
        var result = [];
        var node = treeObj.get_node(nodeId);
        result.push(node.id);
        if (node.children) {
            for(var i = 0; i < node.children.length; i++) {
                result = result.concat(getAllChildrenIds(treeObj, node.children[i]));
            }
        }
        return result;
    }

    function customMenu(node) {
        var tree = $("#tree").jstree(true);
        var items = {
            "create": {
                "separator_before": false,
                "separator_after": false,
                "label": "Erstellen",
                "icon" : "resources/images/details_open.png",
                "action": function (obj) {
                    try {
                        startFolderDialog($.jstree.reference('#tree').get_node(obj.reference[0]).data, "web-create");
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            },
            "rename": {
                "separator_before": false,
                "separator_after": false,
                "label": "Ändern",
                "icon" : "resources/images/beautify16.png",
                "action": function (obj) {
                    try {
                        startFolderDialog($.jstree.reference('#tree').get_node(obj.reference[0]).data, "web-edit");
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            },
            "delete": {
                "separator_before": false,
                "separator_after": false,
                "label": "Löschen",
                "icon" : "resources/images/deleteDocument.gif",
                "action": function (obj) {
                    try {
                        startFolderDialog($.jstree.reference('#tree').get_node(obj.reference[0]).data, "web-display");
                    } catch (e) {
                        errorHandler(e);
                    }
                }
            }
        };
        // Archivroot hat kein Kontextmenü
        if (tree.get_type(node) == "archivRootStandard")
            return false;
        // Im Fehlerordner kein Delete und Create
        if (tree.get_type(node) == "archivFehlerFolderStandard") {
            delete items.delete;
            delete items.create;
        }
        // In Standardordnern kein Delete und Create
        if (tree.get_type(node) == "archivFolderStandard") {
            delete items.delete;
            delete items.create;
        }
        // Im Ordner für die Dokumente kein Delete
        if (tree.get_type(node) == "archivDocumentFolderStandard") {
            delete items.delete;
        }
        return items;
    }


    try {
        $("#tree").jstree('destroy');
    } catch (e) {
    }


    try {
        tree = $("#tree").jstree({
            'core': {
                'data': function (node, aFunction) {
                    try {
                        // relevante Knoten im Alfresco suchen
                        var obj = loadAndConvertDataForTree(node);
                        // CallBack ausführen
                        if (exist(obj)) {
                            aFunction.call(this, obj);
                        }
                    } catch (e) {
                        errorHandler(e);
                    }
                },
                error : function (err) {  
                    REC.log(DEBUG, err.reason);
                    fillMessageBox(true);
                },
                'check_callback': function (op, node, par, pos, more) {
                    try {
                        var erg = false;
                        // Umbenannt werden darf alles
                        if (op === "rename_node")
                            return true;
                        // Keine Verzeichnisse in die Archiv Standardordner verschieben (ausser Ordner Dokumente)
                        if ((op === "move_node" ||
                             op === "copy_node" ||
                             op === "create_node" ||
                             op === "delete_node") &&
                             node.data &&
                             node.data.objectID != alfrescoRootFolderId &&
                             node.data.objectID != archivFolderId &&
                             node.data.objectID != inboxFolderId &&
                             node.data.objectID != fehlerFolderId &&
                             node.data.objectID != unknownFolderId &&
                             node.data.objectID != doubleFolderId &&
                             node.data.objectID != documentFolderId &&
                             node.data.baseTypeId == "cmis:folder" &&
                             par &&
                             par.id &&
                             par.id != alfrescoRootFolderId &&
                             par.id != archivFolderId &&
                             par.id != inboxFolderId &&
                             par.id != fehlerFolderId &&
                             par.id != unknownFolderId &&
                             par.id != doubleFolderId) {
                            erg = true;
                        }
                        // Dokumente nicht in die Root Verzeichenisse Archiv und Dokumente und nicht in das Verzeichnis wo es gerade ist.
                        if ((op === "move_node" ||
                             op === "copy_node") &&
                             node.data &&
                             node.data.baseTypeId == "cmis:document" &&
                             par &&
                             par.id &&
                             par.id != alfrescoRootFolderId &&
                             par.id != archivFolderId &&
                             par.id != documentFolderId &&
                             par.id != node.data.parentId ) {
                            erg = true;
                        }
                        return erg;
                    } catch (e) {
                        errorHandler(e);
                    }
                },
                'themes': {
                    'responsive': false,
                    'variant': 'big',
                    'stripes': false,
                    'dots': true,
                    'icons': true
                }
            },
            'types' : {
                '#' : {
                    "max_children" : 1
                },
                'archivRootStandard' : {
                    "valid_children" : ["archivFolderStandard", "archivDocumentFolderStandard", "archivFehlerFolderStandard"]
                },
                'archivFolderStandard' : {
                    "valid_children" : []
                },
                'archivDoubleFolderStandard' : {
                    "valid_children" : []
                },
                'archivFehlerFolderStandard' : {
                    "valid_children" : ["archivDoubleFolderStandard"]
                },
                'archivDocumentFolderStandard' : {
                    "valid_children" : ["documentFolderStandard"]
                },
                'documentFolderStandard' : {
                    "valid_children" : -1
                }
            },
            "contextmenu": {
                "items": customMenu,
                "select_node" : false
            },
            'plugins': ["dnd", "types", "contextmenu"]
        }).on("changed.jstree",  function (event, data){
            try {
                var tree =  $("#tree").jstree(true);
                var evt =  window.event || event;
                var button = evt.which || evt.button;

                // Select Node nicht bei rechter Maustaste
                if( button != 1 && ( typeof button != "undefined")){
                    return false;
                }
                if (!data.node || !data.node.data)
                    switchAlfrescoDirectory(null);
                else {
                    if (data.node.data.baseTypeId == "cmis:folder") {
                        if (alfrescoServerAvailable) {
                            switchAlfrescoDirectory(data.node.data);
                            tree.open_node(data.node.id);
                        }
                    }
                }
            } catch (e) {
                errorHandler(e);
            }
        }).on("loaded.jstree", function (event, data) {
            try {
                // Eventlistner für Drop in Inbox
                var zone = document.getElementById(inboxFolderId);
                zone.addEventListener('dragover', handleDragOver, false);
                zone.addEventListener('drop', handleDropInbox, false);
            } catch (e) {
                errorHandler(e);
            }
        }).on('move_node.jstree', function (event, data) {
            // Knoten innerhalb des Trees per Drag and Drop verschieben
            try {
                var nodeId = data.node.data.objectID;
                var parentId = data.node.data.parentId;
                var destinationId = data.parent;
                var json = executeService("moveNode", [
                    {"name": "documentId", "value": nodeId},
                    {"name": "currentLocationId", "value": parentId},
                    {"name": "destinationId", "value": destinationId}
                ], "Ordner konnte nicht verschoben werden:");
                if (json.success) {
                    var newData = $.parseJSON(json.result);
                    var source = $.parseJSON(json.source);
                    var target = $.parseJSON(json.target);
                    REC.log(INFORMATIONAL, "Ordner " + data.node.data.name + " von " + source.path + " nach " + target.path + " verschoben");
                    fillMessageBox(true);
                    // Bei Bedarf den Ordner aus der Tabelle entfernen
                    var row = alfrescoFolderTabelle.row('#' + nodeId);
                    if (row) {
                        row.remove();
                        alfrescoFolderTabelle.draw();
                    }
                    // Bei Bedarf den neuen Ordner in die Tabelle einfügen
                    if (jQuery("#breadcrumblist").children().last().get(0).id == newData.parentId)
                        alfrescoFolderTabelle.rows.add([newData]).draw();
                    // Das Objekt im Tree mit dem geänderten Knoten aktualisieren
                    data.node.data = newData;
                }

            } catch (e) {
                errorHandler(e);
            }
        });

        // Drag and Drop für Verschieben von Ordnern aus dem Tree in die Alfresco Folder Tabelle
        $(document)
            .on('dnd_move.vakata', function (e, data) {
                // Hier wird geprüft ob der Ordner, auf den das Element gezogen werden soll ein zulässiger Ordner ist
                // es wird hier aber nicht verhindert, dass das verschieben trotzdem geht sondern nur das Icon beim gezogenen
                // Objekt gesetzt.
                try {
                    var erg = checkMove(data);
                    if (erg) {
                        // Das Icon an dem gezogenen Objekt auf Ok setzen
                        data.helper.find('.jstree-icon').removeClass('jstree-er').addClass('jstree-ok');
                    } else {
                        // Das Icon an dem gezogenen Objekt auf Error setzen
                        data.helper.find('.jstree-icon').removeClass('jstree-ok').addClass('jstree-er');
                    }
                } catch (e) {
                    errorHandler(e);
                }
            }).on('dnd_stop.vakata', function (e, data) {
            // das eigentliche verschieben per Drag and Drop
            try {
                // nochmal die Zulääsigkeit des Drop prüfen!!
                if (checkMove(data)) {
                    var sourceData, targetData;
                    var t = $(data.event.target);
                    if (t.closest('.treeDropable').length || t.closest('.jstree-anchor').length) {

                        for(var index = 0; index < data.data.nodes.length; ++index) {

                            if (data.data.jstree) {
                                // Quelle ist der Tree
                                sourceData = $.jstree.reference('#tree').get_node(data.data.nodes[index]).data;
                            } else {
                                // Quelle ist die Tabelle
                                // die Daten aus der Tabellenzeile, also der Folder, in den verschoben werden soll
                                if (data.data.table == "alfrescoFolderTabelle") {
                                    sourceData = alfrescoFolderTabelle.row('#' + data.data.nodes[index]).data();
                                } else if (data.data.table == "alfrescoTabelle") {
                                    var row = alfrescoTabelle.row('#' + data.data.nodes[index]);
                                    if (row)
                                        sourceData = row.data();
                                }
                            }
                            if (data.event.target.className.indexOf("jstree-anchor") != -1) {
                                // Ziel ist der Tree
                                // der Zielknoten
                                targetData = $.jstree.reference('#tree').get_node(t).data;
                            } else if (data.event.target.className.indexOf("treeDropable") != -1) {
                                // Ziel ist die Tabelle
                                // die Ziel Zeile
                                targetData = alfrescoFolderTabelle.row(t.closest('.treeDropable')[0].parentElement).data();
                            }
                            if (sourceData.baseTypeId == "cmis:folder") {
                                // Tree updaten
                                var sourceNode = $.jstree.reference('#tree').get_node(sourceData.objectID);
                                var targetNode = $.jstree.reference('#tree').get_node(targetData.objectID);
                                if (sourceNode && targetNode)
                                    $.jstree.reference('#tree').move_node(sourceNode, targetNode);
                                // Der Rest passiert im move_node Handler!!
                            } else {
                                //Dokument wurde verschoben
                                var json = executeService("moveNode", [
                                    {"name": "documentId", "value": sourceData.objectID},
                                    {"name": "currentLocationId", "value": sourceData.parentId},
                                    {"name": "destinationId", "value": targetData.objectID}
                                ], "Dokument konnte nicht verschoben werden:");
                                if (json.success) {
                                    var newData = $.parseJSON(json.result);
                                    var source = $.parseJSON(json.source);
                                    var target = $.parseJSON(json.target);
                                    REC.log(INFORMATIONAL, "Dokument " + sourceData.name + " von " + source.path + " nach " + target.path + " verschoben");
                                    fillMessageBox(true);
                                    row.remove();
                                    alfrescoTabelle.draw();
                                }
                            }
                        }
                    }
                }
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
 * lädt die Buttons
 */
function loadButtons() {
    $("button:first").button({
        icons: {
            secondary: "ui-icon-triangle-1-s"
        },
        text: true
    })
        .click(function (event) {
            event.preventDefault();
            $( "#menu" ).menu();
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
        loadLayout();
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

        $.format.locale({
            number: {
                groupingSeparator: '.',
                decimalSeparator: ','
            }
        });

        loadAlfrescoTable();
        loadAlfrescoFolderTable();
        loadAlfrescoSearchTable();
        loadVerteilungTable();

        init();
        // Eventhandler für die Image Clicks
        handleVerteilungImageClicks();
        handleAlfrescoFolderImageClicks();
        handleAlfrescoImageClicks();
        loadAlfrescoTree();
        //loadButtons();
        // Icon Buttons
        $("#alfrescoSearchButton").button({
            icons: {
                primary: 'ui-icon-search'
            }
        });
        var countryList = ["Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria", "Burkina Faso", "Burma", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo, Democratic Republic", "Congo, Republic of the", "Costa Rica", "Cote d'Ivoire", "Croatia", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Fiji", "Finland", "France", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Greece", "Greenland", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, North", "Korea, South", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macedonia", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova", "Mongolia", "Morocco", "Monaco", "Mozambique", "Namibia", "Nauru", "Nepal", "Netherlands", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Norway", "Oman", "Pakistan", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia", "Rwanda", "Samoa", "San Marino", " Sao Tome", "Saudi Arabia", "Senegal", "Serbia and Montenegro", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "Spain", "Sri Lanka", "Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe"];
        $("#alfrescoSearch").autocomplete({
            source: countryList
        });
        $('#alfrescoSearch').on('keypress', function (event) {
            if(event.which === 13){
                startSearch($(this).val());
            }
        });
        viewMenu = $('#menu-1').superfish();
        viewMenu = $('#menu-2').superfish();
        fillMessageBox(true);
    } catch(e) {
        errorHandler(e);
    }
}

