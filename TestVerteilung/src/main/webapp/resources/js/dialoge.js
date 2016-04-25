
/**
 * Öffnet den Einstellungsdialog für die Alfresco Server Settings
 */
function startSettingsDialog() {
    try {

        var data =  {
                "server": getSettings("server"),
                "binding": getSettings("binding"),
                "user": getSettings("user"),
                "password": getSettings("password"), 
                "store": getSettings("store")
        };
        if (!data.store)
            data.store = false;

        // Einstellungen für den Settings Dialog
        var dialogSettings = { "id": "settingsDialog",
            "schema": {
                "type": "object",
                "title": "Server Einstellungen",
                "properties": {

                    "user": {
                        "type": "string",
                        "title": "Benutzer",
                        "required": true
                    },
                    "password": {
                        "type": "string",
                        "title": "Password",
                        "required": true
                    },
                    "server": {
                        "type": "string",
                        "title": "Server",
                        "required": true,
                        "pattern": "^(ht|f)tp(s?)\:\/\/[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-‌​\.\?\,\'\/\\\+&amp;%\$#_]*)?$"
                    },
                    "binding": {
                        "type": "string",
                        "title": "Binding",
                        "required": true,
                        "pattern": "^(ht|f)tp(s?)\:\/\/[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-‌​\.\?\,\'\/\\\+&amp;%\$#_]*)?$"
                    },
                    "store": {
                        "type": "boolean",
                        "title": "Einstellungen sichern",
                        "required": false,
                        "default": false
                    }
                }
            },
                "options": {
                    "renderForm": true,
                    "form": {
                        "buttons": {
                            "submit": {"value": "Sichern"},
                            "reset": {"value": "Abbrechen"}
                        }
                    },
                "fields": {
                    "server": {
                        "size": 60,
                        "placeholder": "z.B.: http://[host]:[port]/alfresco/"
                    },
                    "binding": {
                        "size": 100,
                        "placeholder": "z.B.: http://[host]:[port]/alfresco/api/-default-/public/cmis/versions/1.0/atom"
                    },
                    "user": {
                        "size": 30
                    },
                    "password": {
                        "type": "password",
                        "size": 20
                    },
                    "store" : {

                    }
                }
            },

            "view": {
                "parent": "web-edit",
                "layout": {
                    "template": "columnGridLayout",
                    "bindings": {
                        "server": "server",
                        "binding": "binding",
                        "user": "user",
                        "password": "password",
                        "store": "store"
                    }
                },
                "templates": {
                    "columnGridLayout": '<div class="filter-content">' + '{{#if options.label}}<h2>{{options.label}}</h2><span></span>{{/if}}' + '{{#if options.helper}}<p>{{options.helper}}</p>{{/if}}'
                        + '<div id="server" class="col-1-1"> </div>'
                        + '<div id="binding" class="col-1-1"> </div>'
                        + '<div id="user" class="col-7-12"> </div><div id="password" class="col-5-12"> </div>'
                        + '<div id="store" class="col-1-1"> </div>'
                        + '</div>'                }

            },
            "data": data,
            "ui": "jquery-ui",

            "postRender": function (renderedField) {
                renderedField.on("validated", function () {
                    $("#btn-ok").button("option", "disabled", false);
                });
                renderedField.on("invalidated", function () {
                    $("#btn-ok").button("option", "disabled", true);
                });
                var form = renderedField.form;
                if (form) {
                    form.registerSubmitHandler(function () {
                        if (form.isFormValid()) {
                            try {
                                var input = $("#dialogBox").alpaca().getValue();
                                if (!input.server.endsWith("/"))
                                    input.server = input.server + "/";
                                settings = {"settings": [
                                    {"key": "server", "value": input.server},
                                    {"key": "user", "value": input.user},
                                    {"key": "password", "value": input.password},
                                    {"key": "binding", "value": input.binding},
                                    {"key": "store", "value": input.store}
                                ]};
                                if (store) {
                                    $.cookie("settings", JSON.stringify(settings), {expires: 9999});
                                    REC.log(INFORMATIONAL, "Einstellungen gesichert");
                                    fillMessageBox(true);
                                }
                                closeDialog();
                                init();
                                loadAlfrescoTree();
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    });
                }
            }
        };
        startDialog(dialogSettings, 480);
    } catch (e) {
        errorHandler(e);
    }
}



/**
 * startet den Detaildialog für Dokumente
 * @param data     die Daten welche bearbeitet werden sollen
 * @param modus    der Modus web-edit    Dokument editieren
 */
function startDocumentDialog(data, modus) {
    try {

        // Konversion
        if (exist(data.documentDate)) {
            if (data.documentDate != "null")
                data.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date(Number(data.documentDate)));
            else
                data.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date());
        }
        data.amountDisplay = $.format.number(parseFloat(data.amount), '#,##0.00');
        if (!exist(data.tax))
            data.tax = false;

        // Einstellungen für den Dokumentendialog
        var dialogDocumentDetailsSettings = { "id": "detailDialog",
            "schema": {
                "type": "object",
                "title": "Dokument Eigenschaften",
                "properties": {
                    "name": {
                        "type": "string",
                        "title": "Dateiname",
                        "required": false
                    },
                    "title": {
                        "type": "string",
                        "title": "Titel",
                        "required": true
                    },

                    "description": {
                        "type": "string",
                        "title": "Beschreibung",
                        "required": false
                    },
                    "person": {
                        "type": "string",
                        "title": "Person",
                        "enum": [
                            "Klaus",
                            "Katja",
                            "Till",
                            "Kilian"
                        ],
                        "required": true,
                        "default": "Klaus"
                    },
                    "amountDisplay": {
                        "type": "string",
                        "required": false,
                        "properties": {}
                    },
                    "documentDateDisplay": {
                        "type": "string",
                        "title": "Datum",
                        "format": "date",
                        "required": true
                    },
                    "idvalue": {
                        "type": "string",
                        "title": "Id",
                        "required": false
                    },
                    "tax": {
                        "type": "boolean",
                        "title": "Steuern",
                        "required": false
                    }

                }
            },

            "options": {
                "renderForm": true,
                "form": {
                    "buttons": {
                        "submit": {"value": "Sichern"},
                        "reset": {"value": "Abbrechen"}
                    }
                },
                "fields": {

                    "title": {
                        "size": 30

                    },
                    "name": {
                        "size": 30,
                        "readonly": true
                    },
                    "description": {
                        "type": "textarea",
                        "size": 60
                    },
                    "amountDisplay": {
                        "type": "currency",
                        "label": "Betrag",
                        "centsLimit": 2,
                        "centsSeparator": ",",
                        "prefix": "",
                        "round": "none",
                        "thousandsSeparator": ".",
                        "suffix": "",
                        "unmask": true,
                        "allowNegative" : true,
                        "helpers": [],
                        "validate": true,
                        "disabled": false,
                        "showMessages": true,
                        "renderButtons": true,
                        "data": {},
                        "attributes": {},
                        "allowOptionalEmpty": true,
                        "autocomplete": false,
                        "disallowEmptySpaces": false,
                        "disallowOnlyEmptySpaces": false,
                        "fields": {}
                    },
                    "tax": {
                        "rightLabel": "relevant"
                    },
                    "documentDateDisplay": {
                        "type": "date",
                        "label": "Dokumentdatum",
                        "helpers": [],
                        "validate": true,
                        "disabled": false,
                        "showMessages": true,
                        "renderButtons": true,
                        "allowOptionalEmpty": true,
                        "autocomplete": false,
                        "disallowEmptySpaces": false,
                        "disallowOnlyEmptySpaces": false,
                        "dateFormatRegex": "/(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[012])\.(19|20)\d\d$/",
                        "picker": {
                            "useCurrent": true,
                            "format": "DD.MM.YYYY",
                            "locale": "de_DE",
                            "dayViewHeaderFormat": "DD.MM.YYYY",
                            "extraFormats": []
                        },
                        "dateFormat": "DD.MM.YYYY",
                        "manualEntry": true
                    }
                }
            },
            "view": {
                "parent": modus,
                "layout": {
                    "template": "threeColumnGridLayout",
                    "bindings": {
                        "name": "column-1-1",
                        "title": "column-1-1",
                        "description": "column-1-1",
                        "person": "column-1-2",
                        "documentDateDisplay": "column-2-2",
                        "amountDisplay": "column-1-2",
                        "idvalue": "column-2-2",
                        "tax": "column-1-b"

                    }
                },
                "templates": {
                    "threeColumnGridLayout": '<div class="filter-content">' + '{{#if options.label}}<h2>{{options.label}}</h2><span></span>{{/if}}' + '{{#if options.helper}}<p>{{options.helper}}</p>{{/if}}'
                        + '<div id="column-1-1" class="col-1-1"> </div>'
                        + '<div id="column-1-2" class="col-1-2"> </div> <div id="column-2-2" class="col-1-2"> </div>'
                        + '<div id="column-1-7_12" class="col-7-12"> </div> <div id="column-2-5_12" class="col-5-12"> </div>'
                        + '<div id="column-1-3" class="col-1-3"> </div> <div id="column-2-3" class="col-1-3"> </div> <div id="column-3-3" class="col-1-3"> </div>'
                        + '<div id="column-1-b" class="col-1-1"> </div>'
                        + '</div>'
                }

            },
            "data": data,
            "ui": "jquery-ui",
            "postRender": function (renderedField) {
                var form = renderedField.form;
                if (form) {
                    form.registerSubmitHandler(function () {
                        if (form.isFormValid()) {
                            try {
                                var alpaca = $("#dialogBox").alpaca();
                                // Werte übertragen
                                var input = alpaca.getValue();
                                // die original Daten sichern.
                                var origData = alpaca.data;
                                if (origData.amountDisplay && typeof origData.amountDisplay == "string")
                                    origData.amountDisplay = origData.amountDisplay.replace(/\./g, '').replace(/,/g, ".");
                                // Wurde was geändert?
                                if ((input.title && origData.title != input.title) || 
                                    (input.description && origData.description != input.description) || 
                                    (input.person && origData.person != input.person) || 
                                    (input.documentDateDisplay && origData.documentDateDisplay != input.documentDateDisplay) || 
                                    (input.amountDisplay && origData.amountDisplay != input.amountDisplay) || 
                                    (origData.tax && origData.tax != origData.tax)) {

                                    var extraProperties = {
                                        'P:cm:titled': {'cm:title': input.title, 'cm:description': input.description},
                                        'D:my:archivContent': {'my:documentDate': $.datepicker.parseDate("dd.mm.yy", input.documentDateDisplay).getTime(), 'my:person': input.person},
                                        'P:my:amountable': {'my:amount': input.amountDisplay, "my:tax": input.tax},
                                        'P:my:idable': {'my:idvalue': input.idvalue}
                                    };

                                    erg = executeService("updateProperties", [
                                        {"name": "documentId", "value": origData.objectId},
                                        {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                                    ], "Dokument konnte nicht aktualisiert werden!", false);
                                    if (erg.success) {
                                        data = $.parseJSON(erg.result);
                                        // Tabelle updaten
                                         var row = alfrescoTabelle.row('#' + data.objectID);
                                         if (row) 
                                             row.data(data).invalidate();
                                    }
                                }
                                closeDialog();
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    });
                }
            }

        };
        startDialog(dialogDocumentDetailsSettings, 450);
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * startet den Detaildialog für Folder
 * @param data     die Daten welche bearbeitet werden sollen
 * @param modus    der Modus web-create     neuen Ordner erzeugen
 *                           web-edit       Ordner editieren
 *                           web-display    Ordner löschen
 */
function startFolderDialog(data, modus) {
    try {

        // Einstellungen für den Folder Dialog
        var folderDialogSettings = { "id": "detailDialog",
            "schema": {
                "type": "object",
                "title": function() {
                    if (modus == "web-display")
                        return "Ordner löschen?";
                    else
                        return "Ordner Eigenschaften";
                },
                "properties": {
                    "name": {
                        "type": "string",
                        "title": "Name",
                        "required": true
                    },
                    "title": {
                        "type": "string",
                        "title": "Titel",
                        "required": false
                    },
                    "description": {
                        "type": "string",
                        "title": "Beschreibung",
                        "required": false
                    }
                }
            },
            "options": {
                "renderForm": true,
                "form": {
                    "buttons": {
                        "submit": {
                            "value": function () {
                                switch(true) {
                                    case /display/.test(modus):
                                        return "Löschen";
                                    case /create/.test(modus):
                                        return "Erstellen";
                                    default:
                                        return "Sichern";    
                                }
                            }
                        },
                        "reset": {"value": "Abbrechen"}
                    }
                },
                "fields": {

                    "name": {
                        "size": 30
                    },
                    "title": {
                        "size": 30
                    },
                    "description": {
                        "type": "textarea",
                        "size": 150
                    }
                }
            },
            "data": data,
            "view": {
                "parent": modus,
                "layout": {
                    "template": "threeColumnGridLayout",
                    "bindings": {
                        "name": "column-1-1",
                        "title": "column-1-1",
                        "description": "column-1-1"

                    }
                },
                "templates": {
                    "threeColumnGridLayout": '<div class="filter-content">' + '{{#if options.label}}<h2>{{options.label}}</h2><span></span>{{/if}}' + '{{#if options.helper}}<p>{{options.helper}}</p>{{/if}}'
                        + '<div id="column-1-1" class="col-1-1"> </div>'
                        + '<div id="column-1-2" class="col-1-2"> </div> <div id="column-2-2" class="col-1-2"> </div>'
                        + '<div id="column-1-7_12" class="col-7-12"> </div> <div id="column-2-5_12" class="col-5-12"> </div>'
                        + '<div id="column-1-3" class="col-1-3"> </div> <div id="column-2-3" class="col-1-3"> </div> <div id="column-3-3" class="col-1-3"> </div>'
                        + '</div>'
                }

            },
            "ui": "jquery-ui",
            "postRender": function (renderedField) {
                var form = renderedField.form;
                if (form) {
                    form.registerSubmitHandler(function () {
                        if (form.isFormValid()) {
                            try {
                                var tree, row, node, lastElement, newData, dataChanged, extraProperties, erg;
                                dataChanged = false;
                                var alpaca = $("#dialogBox").alpaca();
                                // Werte übertragen
                                var input = alpaca.getValue();
                                // die original Daten sichern.
                                var origData = alpaca.data;
                                extraProperties = {
                                    'cmis:folder': {
                                        'cmis:objectTypeId': 'cmis:folder',
                                        'cmis:name': input.name
                                    },
                                    'P:cm:titled': {
                                        'cm:title': input.title,
                                        'cm:description': input.description
                                    }
                                };
                                if ((input.name && input.name != origData.name) || 
                                    (input.title && input.title != origData.title) || 
                                    (input.description && input.description != origData.description))
                                    dataChanged = true;
                                lastElement = $("#breadcrumblist").children().last();
                                if (modus == "web-create") {
                                    // ein neuer Ordner wird erstellt
                                    erg = executeService("createFolder", [
                                        {"name": "documentId", "value": origData.objectId},
                                        {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                                    ], "Dokument konnte nicht aktualisiert werden!", false);
                                    if (erg.success) {
                                        newData = $.parseJSON(erg.result);
                                        tree = $.jstree.reference('#tree');
                                        // Tree updaten
                                        node = tree.get_node(newData.parentId);
                                        if (node) {
                                            tree.create_node(node, buildObjectForTree(newData));
                                        }
                                        // Tabelle updaten
                                        if (lastElement && lastElement.get(0).id == newData.parentId) {
                                            alfrescoFolderTabelle.rows.add([newData]).draw();
                                        }
                                        // BreadCrumb aktualisieren
                                        if (lastElement)
                                            fillBreadCrumb(lastElement.data().data);
                                    }
                                }
                                else if (modus == "web-edit" && dataChanged) {
                                    // bestehender Ordner wird editiert
                                    erg = executeService("updateProperties", [
                                        {"name": "documentId", "value": origData.objectId},
                                        {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                                    ], "Ordner konnte nicht aktualisiert werden!", false);
                                    if (erg.success) {
                                        newData = $.parseJSON(erg.result);
                                        // Tree updaten
                                        tree = $.jstree.reference('#tree');
                                        node = tree.get_node(newData.objectID);
                                        if (node) {
                                            tree.rename_node(node, newData.name);
                                            node.data = newData;
                                        }
                                        // Tabelle updaten
                                        if (lastElement && lastElement.get(0).id == newData.parentId) {
                                            row = alfrescoFolderTabelle.row('#' + newData.objectID);
                                            if (row) {
                                                row.data(newData).invalidate();
                                            }
                                        }
                                        // BreadCrumb aktualisieren
                                        if (lastElement && lastElement.get(0).id == origData.objectID) {
                                            fillBreadCrumb(input);
                                        } else if (lastElement)
                                            fillBreadCrumb(lastElement.data().data);
                                    }
                                } else if (modus == "web-display") {
                                    // Ordner wird gelöscht
                                    erg = executeService("deleteFolder", [
                                        {"name": "documentId", "value": origData.objectID}
                                    ], "Ordner konnte nicht gelöscht werden!", false);
                                    if (erg.success) {
                                        // Tree updaten
                                        tree = $.jstree.reference('#tree');
                                        tree.delete_node(origData.objectID);
                                        // Tabelle updaten
                                        if (lastElement && lastElement.get(0).id == origData.parentId) {
                                            row = alfrescoFolderTabelle.row('#' + origData.objectID);
                                            if (row) {
                                                row.remove().draw();
                                            }
                                        }
                                        // der aktuelle Ordner ist der zu löschende
                                        if (lastElement && lastElement.get(0).id == origData.objectID) {
                                            tree.select_node(origData.parentId);
                                        } else {

                                            // BreadCrumb aktualisieren
                                            if (lastElement)
                                                fillBreadCrumb(lastElement.data().data);
                                        }
                                    }
                                }
                                closeDialog();
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    });
                }
            }
        };

        startDialog(folderDialogSettings, 460);
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * startet den Detaildialog für Kommentare
 */
function startCommentsDialog(comments) {
    try {
        var data = comments.items;

        $dialog = $('<div> <table cellpadding="0" cellspacing="0" border="0" class="display" id="custTabelle"></table> </div>').dialog({
            autoOpen: false,
            title: "Kommentare",
            modal: true,
            height:300,
            width:800,
            buttons: {
                "Ok": function () {
                    $(this).dialog("destroy");
                }
            }
        }).css({height:"300px", width:"800px", overflow:"auto"});

        $dialog.dialog('open');
        $('#custTabelle').DataTable({
            "jQueryUI": true,
            "paging": false,
            "data": data,
            "scrollX": "100%",
            "scrollXInner": "100%",
            // "sScrollY" : calcDataTableHeight(),
            "autoWidth": true,
            "lengthChange": false,
            "searching": false,
            "columns": [
                {
                    "data": "author.username",
                    "title": "User",
                    "defaultContent": '',
                    "type": "string",
                    "width": "120px",
                    "class": "alignLeft"
                },
                {
                    "data": "modifiedOn",
                    "title": "Datum",
                    "defaultContent": '',
                    "type": "string",
                    "width": "120px",
                    "class": "alignLeft"
                },
                {
                    "title": "Kommentar",
                    "data": "content",
                    "class": "alignLeft"
                }
            ],
            "columnDefs": [
                {
                    "targets": [0, 2],
                    "visible": true
                },
                {
                    "targets": [1],
                    "visible": true,
                    "render": function (data, type, row) {
                        return  $.formatDateTime('dd.mm.yy hh:ii:ss', new Date(Date.parse(row.modifiedOnISO)));
                    }
                }
            ],
            "info": false
        });
    } catch (e) {
        errorHandler(e);
    }
}
/**
 * schliesst den Dialog
 */
function closeDialog() {
    var dialogBox = $('#dialogBox');
    if (dialogBox) {
        dialogBox.dialog("close");
        dialogBox.remove();
    }
    $('#simpleGrid').remove();
}
/**
 * startet den eigentlichen Dialog
 * @param dialogSettings            die Settings für den Dialog
 * @param width                     die Weite des Fensters
 */
function startDialog(dialogSettings, width) {

    $('head').append('<link href="resources/css/simplegrid.css" rel="stylesheet" id="simpleGrid" />');
     $("<div>", {id: "dialogBox", class:"grid gridpad" }).appendTo("body");
    $('#dialogBox').alpaca( dialogSettings).dialog({
        autoOpen: true,
        width: width,
        height: 'auto',
        modal: true,
        position: {
            my: "top",
            at: "center center-20%",
            of: window,
            collision: "fit",
            // Ensure the titlebar is always visible
            using: function( pos ) {
                var topOffset = $( this ).css( pos ).offset().top;
                if ( topOffset < 0 ) {
                    $( this ).css( "top", pos.top - topOffset );
                }
            }
        },
        open: function () {
            $(".alpaca-form-buttons-container").addClass("ui-dialog-buttonpane ui-widget-content");
            $(".alpaca-form-button-submit").button();
            $(".alpaca-form-button-reset").button().click(function () {
                closeDialog();
            });
        }
    });

}