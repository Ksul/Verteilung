/**
 * Öffnet den Einstellungsdialog für die Alfresco Server Settings
 */
function startSettingsDialog() {
    try {
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
                "parent": "VIEW_WEB_EDIT",
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
                    "columnGridLayout": '<div class="filter-content">' + '{{if options.label}}<h2>${options.label}</h2><span></span>{{/if}}' + '{{if options.helper}}<p>${options.helper}</p>{{/if}}'
                        + '<div id="column-1-1" class="col-1-1"> </div>'
                        + '<div id="column-1-2" class="col-1-2"> </div> <div id="column-2-2" class="col-1-2"> </div>'
                        + '<div id="column-1-7_12" class="col-7-12"> </div> <div id="column-2-5_12" class="col-5-12"> </div>'
                        + '<div id="column-1-3" class="col-1-3"> </div> <div id="column-2-3" class="col-1-3"> </div> <div id="column-3-3" class="col-1-3"> </div>'
                        + '</div>'                }

            },
            "ui": "jquery-ui",
            "data": {
                "server": getSettings("server"),
                "binding": getSettings("binding"),
                "user": getSettings("user"),
                "password": getSettings("password")
            },
            "postRender": function (control) {
                control.on("validated", function (e) {
                    $("#btn-ok").button("option", "disabled", false);
                });
                control.on("invalidated", function (e) {
                    $("#btn-ok").button("option", "disabled", true);
                });
            }
        };

        changeCss('.grid', 'max-width: 100%; min-width:100%');
        changeCss('input', 'width:100%');
        changeCss('h2', 'background-color: transparent; background-image: url("./src/main/resource/images/alfresco.png"); background-repeat: no-repeat; background-position: left; height: 24px; border: 0; padding-left: 28px; padding-top: 4px');
        $('head').append('<link href="./src/main/resource/simplegrid.css" rel="stylesheet" id="simpleGrid" />');
        $('<div id="settingsDialog">').append(Alpaca($('<div id="form">'), dialogSettings)).dialog({
            autoOpen: true,
            modal: true,
            width: 480,
            height: 'auto',
            open: function () {
                $(".alpaca-form-buttons-container").addClass("ui-dialog-buttonpane ui-widget-content");
            },
            buttons: {
                "Save": {
                    "id": "btn-ok",
                    "text": "Save",
                    "click": function () {
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
                            fillMessageBox("Einstellungen gesichert");
                            $('#settingsDialog').dialog("destroy");
                            jQuery('#simpleGrid').remove();
                            init();
                            loadAlfrescoTree();
                        } catch (e) {
                            errorHandler(e);
                        }
                    }},

                "Cancel": {
                    "id": "btn-cancel",
                    "text": "Cancel",
                    "click": function () {
                        $(this).dialog("destroy");
                    }
                }
            }
        });

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
        var dialogSettings = { "id": "detailDialog",
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
                        "type": "date",
                        "format": "date",
                        "title": "Datum",
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
                        "dateFormatRegex": "/(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[012])\.(19|20)\d\d$/",
                        "dateFormat": "dd.mm.yy"
                    }

                }
            },
            "data": data,
            "view": {
                "parent": "VIEW_WEB_EDIT",
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
                    "threeColumnGridLayout": '<div class="filter-content">' + '{{if options.label}}<h2>${options.label}</h2><span></span>{{/if}}' + '{{if options.helper}}<p>${options.helper}</p>{{/if}}'
                        + '<div id="column-1-1" class="col-1-1"> </div>'
                        + '<div id="column-1-2" class="col-1-2"> </div> <div id="column-2-2" class="col-1-2"> </div>'
                        + '<div id="column-1-7_12" class="col-7-12"> </div> <div id="column-2-5_12" class="col-5-12"> </div>'
                        + '<div id="column-1-3" class="col-1-3"> </div> <div id="column-2-3" class="col-1-3"> </div> <div id="column-3-3" class="col-1-3"> </div>'
                        + '<div id="column-1-b" class="col-1-1"> </div>'
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
                                var title = $("[name='title']").val(),
                                    description = $("[name='description']").val(),
                                    person = $("[name='person']").val(),
                                    documentDate = $("[name='documentDate']").val(),
                                    amount = $("[name='amount']").val(),
                                    idvalue = $("[name='idvalue']").val(),
                                    tax = $("[name='tax']").val();
                                if (data.title != title || data.description != description || data.person != person || data.documentDate != documentDate
                                    || data.amount != amount || data.tax != tax) {
                                    data.title = title;
                                    data.description = description;
                                    data.person = person;
                                    data.documentDate = $.datepicker.parseDate("dd.mm.yy", documentDate).getTime();
                                    data.amount = amount;
                                    data.idvalue = idvalue;
                                    data.tax = tax;
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

                                }
                                alfrescoTabelle.rows().invalidate();
                                $('#dialogBox').dialog("destroy");
                                jQuery('#simpleGrid').remove();
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    });
                }
            }

        };
        startDialog(dialogSettings, 420);
    } catch (e) {
        errorHandler(e);
    }
}


/**
 * startet den Detaildialog für Folder
 */
function startFolderDialog(tableRow, modus) {
    try {
        var data = tableRow.data();
        var dialogSettings = { "id": "detailDialog",
            "schema": {
                "type": "object",
                "title": "Folder Eigenschaften",
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
                        "submit": {"value": "Sichern"},
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
                    "threeColumnGridLayout": '<div class="filter-content">' + '{{if options.label}}<h2>${options.label}</h2><span></span>{{/if}}' + '{{if options.helper}}<p>${options.helper}</p>{{/if}}'
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
                                var name = $("[name='name']").val(),
                                    description = $("[name='description']").val(),
                                    title = $("[name='title']").val();

                                if (data.name != name || data.title != title || data.description != description) {
                                    data.name = name;
                                    data.title = title;
                                    data.description = description;

                                    var extraProperties = {
                                        'cmis:folder': {
                                            'cmis:objectTypeId': 'cmis:folder',
                                            'cmis:name': name
                                        },
                                        'P:cm:titled': {
                                            'cm:title': title,
                                            'cm:description': description
                                        }
                                    };
                                    if (modus == "VIEW_WEB_CREATE")
                                        erg = executeService("createFolder", [
                                            {"name": "documentId", "value": data.objectId},
                                            {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                                        ], "Dokument konnte nicht aktualisiert werden!", false);
                                    if (erg.success) {
                                        var newFolder = $.parseJSON(erg.result);
                                        $("#tree").jstree('open_node', newFolder.parentId);
                                        switchAlfrescoDirectory(newFolder.parentId);
                                        $("#tree").jstree('select_node', newFolder.parentId);
                                    }
                                    else {
                                        erg = executeService("updateProperties", [
                                            {"name": "documentId", "value": data.objectId},
                                            {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                                        ], "Dokument konnte nicht aktualisiert werden!", false);
                                        if (erg.success) {
                                            alfrescoFolderTabelle.rows().invalidate();
                                            var node = $(document.getElementById(data.objectId));
                                            $("#tree").jstree('rename_node', node[0], name);
                                        }
                                    }
                                }
                                $('#dialogBox').dialog("destroy");
                                jQuery('#simpleGrid').remove();
                            } catch (e) {
                                errorHandler(e);
                            }
                        }
                    });
                }
            }

        };
        startDialog(dialogSettings, 460);
    } catch (e) {
        errorHandler(e);
    }
}

/**
 * startet den eigentlichen Dialog
 * @param dialogSettings            die Settings fü den Dialog
 * @param width                     die Weite für das Fenster
 */
function startDialog(dialogSettings, width) {

    $('head').append('<link href="./src/main/resource/simplegrid.css" rel="stylesheet" id="simpleGrid" />');
    $('<div id="dialogBox">').append(Alpaca($('<div id="form" class="grid grid-pad">'), dialogSettings)).dialog({
        autoOpen: true,
        width: width,
        height: 'auto',
        modal: true,
        position: {
            my: "top",
            at: "center center-25%",
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
                $('#dialogBox').dialog("close");
                $('#dialogBox').remove();
            });
        }
    });

}
