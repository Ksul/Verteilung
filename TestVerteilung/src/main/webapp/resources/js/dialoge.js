
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

    /**
     * ändert ein Dokument
     * @param input             die neu einzutragenden Daten
     * @param origData          die original Daten
     */
    function editDocument(input, origData) {
        try {
            var extraProperties = {
                'P:cm:titled': {
                    'cm:title': input.title,
                    'cm:description': input.description
                },
                'D:my:archivContent': {
                    'my:documentDate': input.documentDateDisplay,
                    'my:person': input.person
                },
                'P:my:amountable': {'my:amount': input.amount, "my:tax": input.tax},
                'P:my:idable': {'my:idvalue': input.idvalue}
            };

            var erg = executeService("updateProperties", [
                {"name": "documentId", "value": origData.objectId},
                {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
            ], "Dokument konnte nicht aktualisiert werden!", false);
            if (erg.success) {
                var data = $.parseJSON(erg.result);
                // Tabelle updaten
                var row = alfrescoTabelle.row('#' + data.objectID);
                if (row)
                    row.data(data).invalidate();
            }
        } catch (e) {
            errorHandler(e);
        }
    }

    /**
     * löscht ein Dokument
     */
    function deleteDocument() {
        try {
            var origData = $("#dialogBox").alpaca().data;
            var erg = executeService("deleteDocument", [
                {"name": "documentId", "value": origData.objectID}
            ], ["Dokument konnte nicht gelöscht werden!"]);
            if (erg.success) {
                alfrescoTabelle.row('#' + origData.objectID).remove().draw(false);
            }
        } catch (e) {
            errorHandler(e);
        }
    }


    try {
        // Konversion
        if (exist(data.documentDate)) {
            if (data.documentDate != "null")
                data.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date(Number(data.documentDate)));
            else
                data.documentDateDisplay = $.datepicker.formatDate("dd.mm.yy", new Date());
        }
        data.amountDisplay = $.format.number(data.amount, '#,##0.00');
        if (!exist(data.tax))
            data.tax = false;

        // Einstellungen für den Dokumentendialog
        var dialogDocumentDetailsSettings = { "id": "detailDialog",
            "schema": {
                "type": "object",
                "title": function() {
                    if (modus == "web-display")
                        return "Dokument löschen?";
                    else
                        return "Dokument Eigenschaften";
                },
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
                    "buttons": function () {
                        switch (true) {
                            case /display/.test(modus):
                                return {
                                    "delete": {
                                        "type": "button",
                                        "styles": " ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ",
                                        "value": "Löschen"
                                    },
                                    "reset": {"value": "Abbrechen"}
                                };
                            case /create/.test(modus):
                                return {
                                    "submit": {"value": "Erstellen"},
                                    "reset": {"value": "Abbrechen"}
                                };
                            default:
                                return {
                                    "submit": {"value": "Sichern"},
                                    "reset": {"value": "Abbrechen"}
                                };
                        }
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
                                if (modus == "web-edit") {
                                    // Konvertierung
                                    if (input.amountDisplay && typeof input.amountDisplay == "string")
                                        input.amount = parseFloat(input.amountDisplay.replace(/\./g, '').replace(/,/g, "."));
                                    if (input.documentDateDisplay && typeof input.documentDateDisplay == "string")
                                        input.documentDate = $.datepicker.parseDate("dd.mm.yy", input.documentDateDisplay).getTime();
                                    // Wurde was geändert?
                                    if ((input.title && origData.title != input.title) ||
                                        (input.description && origData.description != input.description) ||
                                        (input.person && origData.person != input.person) ||
                                        (input.documentDate && origData.documentDate != input.documentDate) ||
                                        (input.amount && origData.amount != input.amount) ||
                                        (input.tax != origData.tax))
                                        editDocument(input, origData);
                                } else if (modus == "web-display") {
                                    deleteDocument(origData);
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
        var additionalButton =[{"id":".alpaca-form-button-delete", "function": deleteDocument }];
        startDialog(dialogDocumentDetailsSettings, 450, additionalButton);
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

    /**
     * erzeugt einen neuen Ordner
     * @param input         die Daten des neuen Ordners
     * @param origData      die übergebenen Daten
     */
    function createFolder(input, origData) {
        try {
            var extraProperties = {
                'cmis:folder': {
                    'cmis:objectTypeId': 'cmis:folder',
                    'cmis:name': input.name
                },
                'P:cm:titled': {
                    'cm:title': input.title,
                    'cm:description': input.description
                }
            };
            var erg = executeService("createFolder", [
                {"name": "documentId", "value": origData.objectId},
                {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
            ], "Ordner konnte nicht erstellt werden!", false);
            if (erg.success) {
                var lastElement = $("#breadcrumblist").children().last();
                var newData = $.parseJSON(erg.result);
                var tree = $.jstree.reference('#tree');
                // Tree updaten
                var node = tree.get_node(newData.parentId);
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
        } catch (e) {
            errorHandler(e);
        }
    }

    /**
     * editiert einen Ordner
     * @param input         die neuen Daten des Ordners
     * @param origData      die originalen Daten des Ordners
     */
    function editFolder(input, origData) {
        try {
            var extraProperties = {
                'cmis:folder': {
                    'cmis:objectTypeId': 'cmis:folder',
                    'cmis:name': input.name
                },
                'P:cm:titled': {
                    'cm:title': input.title,
                    'cm:description': input.description
                }
            };
            var erg = executeService("updateProperties", [
                {"name": "documentId", "value": origData.objectId},
                {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
            ], "Ordner konnte nicht aktualisiert werden!", false);
            if (erg.success) {
                var lastElement = $("#breadcrumblist").children().last();
                var newData = $.parseJSON(erg.result);
                // Tree updaten
                var tree = $.jstree.reference('#tree');
                var node = tree.get_node(newData.objectID);
                if (node) {
                    tree.rename_node(node, newData.name);
                    node.data = newData;
                }
                // Tabelle updaten
                if (lastElement && lastElement.get(0).id == newData.parentId) {
                    var row = alfrescoFolderTabelle.row('#' + newData.objectID);
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
        } catch (e) {
            errorHandler(e);
        }
    }

    /**
     * löscht einen Ordner
     */
    function deleteFolder() {
        try {
            var origData = $("#dialogBox").alpaca().data;
            var erg = executeService("deleteFolder", [
                {"name": "documentId", "value": origData.objectID}
            ], "Ordner konnte nicht gelöscht werden!", false);
            if (erg.success) {
                var lastElement = $("#breadcrumblist").children().last();
                // Tree updaten
                var tree = $.jstree.reference('#tree');
                tree.delete_node(origData.objectID);
                // Tabelle updaten
                if (lastElement && lastElement.get(0).id == origData.parentId) {
                    var row = alfrescoFolderTabelle.row('#' + origData.objectID);
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
        } catch (e) {
            errorHandler(e);
        }
    }
 
 
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
                    "buttons": function () {
                        switch (true) {
                            case /display/.test(modus):
                                return {
                                    "delete": {
                                        "type": "button",
                                        "styles": "btn btn-primary",
                                        "value": "Löschen"
                                    },
                                    "reset": {"value": "Abbrechen"}
                                };
                            case /create/.test(modus):
                                return {
                                    "submit": {"value": "Erstellen"},
                                    "reset": {"value": "Abbrechen"}
                                };
                            default:
                                return {
                                    "submit": {"value": "Sichern"},
                                    "reset": {"value": "Abbrechen"}
                                };
                        }
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
                                var alpaca = $("#dialogBox").alpaca();
                                // Werte übertragen
                                var input = alpaca.getValue();
                                // die original Daten sichern.
                                var origData = alpaca.data;
                                if (modus == "web-create") {
                                    // ein neuer Ordner wird erstellt
                                    createFolder(input, origData);
                                }
                                else if (modus == "web-edit" && dataChanged) {
                                    // bestehender Ordner wird editiert
                                    if ((input.name && input.name != origData.name) ||
                                        (input.title && input.title != origData.title) ||
                                        (input.description && input.description != origData.description))
                                        editFolder(input, origData);
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
        var additionalButton =[{"id":".alpaca-form-button-delete", "function": deleteFolder }];
        startDialog(folderDialogSettings, 460, additionalButton);
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
 * startet den eigentlichen Dialog
 * @param dialogSettings            die Settings für den Dialog
 * @param width                     die Weite des Fensters
 * @param callbacks                 Array mit Callbacks für weitere Buttons
 */
function startDialog(dialogSettings, width, callbacks) {

    /**
     * schliesst den Dialog
     */
    function closeDialog() {
        var dialogBox = $('#dialogBox');
        if (dialogBox) {
            dialogBox.dialog("close");
            dialogBox.remove();
        }
    }

    $("<div>", {id: "dialogBox", class: "grid gridpad"}).appendTo("body");
    $('#dialogBox').alpaca(dialogSettings).dialog({
        "autoOpen": true,
        "width": width,
        "height": 'auto',
        "modal": true,
        "position": {
            "my": "top",
            "at": "center center-20%",
            "of": window,
            "collision": "fit",
            // Ensure the titlebar is always visible
            "using": function (pos) {
                var topOffset = $(this).css(pos).offset().top;
                if (topOffset < 0) {
                    $(this).css("top", pos.top - topOffset);
                }
            }
        },
        "open": function () {
            $(".alpaca-form-buttons-container").addClass("ui-dialog-buttonpane ui-widget-content");
            $(".alpaca-form-button-submit").button();
            $(".alpaca-form-button-reset").button().click(function () {
                closeDialog();
            });
            if (callbacks) {
                for (var i = 0; i < callbacks.length; i++) {
                    var obj = callbacks[i];
                    $(obj.id).button().click(function () {
                        obj.function();
                        closeDialog();
                    });
                }
            }
        }
    });
}