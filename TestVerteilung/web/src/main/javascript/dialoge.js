
/**
 * Öffnet den Einstellungsdialog für die Alfresco Server Settings
 */
function startSettingsDialog() {
    try {

        var data =  {
                "server": getSettings("server"),
                "binding": getSettings("binding"),
                "user": getSettings("user"),
                "password": getSettings("password")
        };

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
                        "size": 60
                    },
                    "binding": {
                        "size": 100
                    },
                    "user": {
                        "size": 30
                    },
                    "password": {
                        "type": "password",
                        "size": 20
                    }
                }
            },

            "view": {
                "parent": "web-edit",
                "layout": {
                    "template": "columnGridLayout",
                    "bindings": {
                        "server": "column-1-1",
                        "binding": "column-1-1",
                        "user": "column-1-7_12",
                        "password": "column-2-5_12"
                    }
                },
                "templates": {
                    "columnGridLayout": '<div class="filter-content">' + '{{#if options.label}}<h2>{{options.label}}</h2><span></span>{{/if}}' + '{{#if options.helper}}<p>{{options.helper}}</p>{{/if}}'
                        + '<div id="column-1-1" class="col-1-1"> </div>'
                        + '<div id="column-1-2" class="col-1-2"> </div> <div id="column-2-2" class="col-1-2"> </div>'
                        + '<div id="column-1-7_12" class="col-7-12"> </div> <div id="column-2-5_12" class="col-5-12"> </div>'
                        + '<div id="column-1-3" class="col-1-3"> </div> <div id="column-2-3" class="col-1-3"> </div> <div id="column-3-3" class="col-1-3"> </div>'
                        + '</div>'                }

            },
            "data": data,
            "ui": "jquery-ui",

            "postRender": function (control) {
                control.on("validated", function (e) {
                    $("#btn-ok").button("option", "disabled", false);
                });
                control.on("invalidated", function (e) {
                    $("#btn-ok").button("option", "disabled", true);
                });
                var form = renderedField.form;
                if (form) {
                    form.registerSubmitHandler(function (e) {
                        if (form.isFormValid()) {
                            try {
                                var server = $("[name='server']").val(),
                                    binding = $("[name='binding']").val(),
                                    user = $("[name='user']").val(),
                                    password = $("[name='password']").val();
                                if (!server.endsWith("/"))
                                    server = server + "/";
                                settings = {"settings": [
                                    {"key": "server", "value": server},
                                    {"key": "user", "value": user},
                                    {"key": "password", "value": password},
                                    {"key": "binding", "value": binding}
                                ]};
                                $.cookie("settings", JSON.stringify(settings), { expires: 9999 });
                                REC.log(INFORMATIONAL, "Einstellungen gesichert");
                                fillMessageBox(true);
                                $('dialogBox').dialog().dialog('close');
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
 * startet den Detaildialog für Documente
 */
function startDocumentDialog(tableRow) {
    try {
        var data = tableRow.data();
        // Konversion
        if (exist(data.documentDate)) {
            if (data.documentDate != "null")
                data.documentDate = $.datepicker.formatDate("dd.mm.yy", new Date(Number(data.documentDate)));
            else
                data.documentDate = $.datepicker.formatDate("dd.mm.yy", new Date());
        }
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
                    "amount": {
                        "type": "number",
                        "title": "Betrag",
                        "required": false
                    },
                    "documentDate": {
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
                        "required": false,
                        "default": "false"
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
                    /*                        "amount":{
                     "type": "currency",
                     "centsSeparator": ",",
                     "prefix": "",
                     "suffix": " €",
                     "thousandsSeparator": "."
                     },*/
                    "tax": {
                        "rightLabel": "relevant"
                    },
                    "documentDate": {
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
                            "useCurrent": false,
                            "format": "DD.MM.YYYY",
                            "locale": "de_DE",
                            "dayViewHeaderFormat": "DD.MM.YYYY",
                            "extraFormats": []
                        },
                        "dateFormat": "dd.mm.yy",
                        "manualEntry": true
                    }
                }
            },
            "view": {
                "parent": "web-edit",
                "layout": {
                    "template": "threeColumnGridLayout",
                    "bindings": {
                        "name": "column-1-1",
                        "title": "column-1-1",
                        "description": "column-1-1",
                        "person": "column-1-2",
                        "documentDate": "column-2-2",
                        "amount": "column-1-2",
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
                    form.registerSubmitHandler(function (e) {
                        if (form.isFormValid()) {
                            try {
                                // Werte übertragen
                                var title = $("[name='title']").val(),
                                    description = $("[name='description']").val(),
                                    person = $("[name='person']").val(),
                                    documentDate = $("[name='documentDate']").val(),
                                    amount = $("[name='amount']").val(),
                                    idvalue = $("[name='idvalue']").val(),
                                    tax = $("[name='tax']").val();
                                // Wurde was geändert?
                                if (data.title != title || data.description != description || data.person != person || data.documentDate != documentDate
                                    || data.amount != amount || data.tax != tax) {

                                    var extraProperties = {
                                        'P:cm:titled': {'cm:title': title, 'cm:description': description},
                                        'D:my:archivContent': {'my:documentDate': $.datepicker.parseDate("dd.mm.yy", documentDate).getTime(), 'my:person': person},
                                        'P:my:amountable': {'my:amount': amount, "my:tax": tax},
                                        'P:my:idable': {'my:idvalue': idvalue}
                                    };

                                    erg = executeService("updateProperties", [
                                        {"name": "documentId", "value": data.objectId},
                                        {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                                    ], "Dokument konnte nicht aktualisiert werden!", false);
                                    if (erg.success) {
                                        // Daten in die Tabelle übertragen
                                        data.title = title;
                                        data.description = description;
                                        data.person = person;
                                        data.documentDate = $.datepicker.parseDate("dd.mm.yy", documentDate).getTime();
                                        data.amount = amount;
                                        data.idvalue = idvalue;
                                        data.tax = tax;
                                    }
                                }
                                alfrescoTabelle.rows().invalidate();
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
                                if (modus == "web-display") return "Löschen"; else return "Sichern"
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
                    form.registerSubmitHandler(function (e) {
                        if (form.isFormValid()) {
                            try {
                                var tree, row, node, lastElement, origData, newData, dataChanged, extraProperties, erg;
                                dataChanged = false;
                                // die originaldaten sichern. Das hier verwendete Verfahren soll besonders schnell sein
                                origData = JSON.parse(JSON.stringify(data));
                                data.name = $("[name='name']").val();
                                data.description = $("[name='description']").val();
                                data.title = $("[name='title']").val();

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
                                if (data.name != origData.name || data.title != origData.title || data.description != origData.description)
                                    dataChanged = true;
                                lastElement = $("#breadcrumblist").children().last();
                                if (modus == "web-create") {
                                    // ein neuer Ordner wird erstellt
                                    erg = executeService("createFolder", [
                                        {"name": "documentId", "value": data.objectId},
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
                                        {"name": "documentId", "value": data.objectId},
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
                                                var dat = row.data();
                                                dat.name = newData.name;
                                                dat.title = newData.title;
                                                dat.description = newData.description;
                                                row.invalidate();
                                            }
                                        }
                                        // BreadCrumb aktualisieren
                                        if (lastElement && lastElement.get(0).id == data.objectID) {
                                            fillBreadCrumb(data);
                                        } else if (lastElement)
                                            fillBreadCrumb(lastElement.data().data);
                                    }
                                } else if (modus == "web-display") {
                                    // Ordner wird gelöscht
                                    erg = executeService("deleteFolder", [
                                        {"name": "documentId", "value": data.objectID}
                                    ], "Ordner konnte nicht gelöscht werden!", false);
                                    if (erg.success) {
                                        // Tree updaten
                                        tree = $.jstree.reference('#tree');
                                        tree.delete_node(data.objectID);
                                        // Tabelle updaten
                                        if (lastElement && lastElement.get(0).id == data.parentId) {
                                            row = alfrescoFolderTabelle.row('#' + data.objectID);
                                            if (row) {
                                                row.remove().draw();
                                            }
                                        }
                                        // der aktuelle Ordner ist der zu löschende
                                        if (lastElement && lastElement.get(0).id == data.objectID) {
                                            tree.select_node(data.parentId);
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
 * @param dialogSettings            die Settings fü den Dialog
 * @param width                     die Weite für das Fenster
 */
function startDialog(dialogSettings, width) {

    $('head').append('<link href="./src/main/resource/css/simplegrid.css" rel="stylesheet" id="simpleGrid" />');
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