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
    function startDocumentDialog(id, name, titel, beschreibung, person, betrag, datum, schluessel, steuer) {
        try {
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
                        "titel": {
                            "type": "string",
                            "title": "Titel",
                            "required": true
                        },

                        "beschreibung": {
                            "type": "string",
                            "title": "Beschreibung",
                            "required": false
                        },
                        "person": {
                            "type": "string",
                            "title": "Person",
                            "enum":[
                                "Klaus",
                                "Katja",
                                "Till",
                                "Kilian"
                            ],
                            "required": true
                        },
                        "betrag": {
                            "type": "number",
                            "title": "Betrag",
                            "required": false
                        },
                        "datum": {
                            "type": "date",
                            "format":"date",
                            "title": "Datum",
                            "required": true
                        },
                        "schluessel": {
                            "type": "string",
                            "title": "Id",
                            "required": false
                        },
                        "steuer": {
                            "type": "boolean",
                            "title":"Steuern",
                            "required": false
                        }

                    }
                },

                "options": {
                    "renderForm": true,
                    "form":{
                        "buttons":{
                            "submit":{"value":"Sichern"},
                            "reset":{"value":"Abbrechen"}
                        }
                    },
                    "fields": {

                        "titel": {
                            "size": 30

                        },
                        "name": {
                            "size": 30,
                            "readonly": true
                        },
                        "beschreibung": {
                            "type": "textarea",
                            "size": 150
                        },
/*                        "betrag":{
                            "type": "currency",
                            "centsSeparator": ",",
                            "prefix": "",
                            "suffix": " €",
                            "thousandsSeparator": "."
                        },*/
                        "steuer": {
                            "rightLabel": "relevant"
                        },
                        "datum": {
                            "dateFormatRegex": "/(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[012])\.(19|20)\d\d$/",
                            "dateFormat":"dd.mm.yy"
                        }

                    }
                },
                "data": {
                    "name": exist(name)? name : "",
                    "titel": exist(titel)? titel : "",
                    "steuer": exist(steuer) ? steuer : false,
                    "datum": exist(datum) ? datum : $.datepicker.formatDate( "dd.mm.yy", new Date()),
                    "person": exist(person) ? person : "Klaus",
                    "betrag": exist(betrag) ? betrag : "",
                    "schluessel": exist(schluessel)? schluessel : "",
                    "beschreibung": exist(beschreibung) ? beschreibung : ""
                },
                "view": {
                    "parent": "VIEW_WEB_EDIT",
                    "layout": {
                        "template": "threeColumnGridLayout",
                        "bindings": {
                            "name":"column-1-1",
                            "titel":"column-1-1",

                            "beschreibung":"column-1-1",
                            "person":"column-1-2",
                            "datum":"column-2-2",
                            "betrag":"column-1-2",
                            "schluessel":"column-2-2",
                            "steuer":"column-1-2"

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
                "ui": "jquery-ui"  ,
                "postRender": function(renderedField) {
                    var form = renderedField.form;
                    if (form) {
                        form.registerSubmitHandler(function(e) {
                            if (form.isFormValid()) {
                                try {
                                    //TODO
                                    var titel = $("[name='titel']").val(),
                                        beschreibung = $("[name='beschreibung']").val(),
                                        person = $("[name='person']").val(),
                                        datum = $("[name='datum']").val(),
                                        betrag = $("[name='betrag']").val(),
                                        schluessel = $("[name='schluessel']").val(),
                                        steuer = $("[name='steuer']").val();

                                    var extraProperties = {
                                        'P:cm:titled':{'cm:title':titel,'cm:description':beschreibung},
                                        'D:my:archivContent':{'my:documentDate':$.datepicker.parseDate('dd.mm.yy', datum).toUTCString(),'my:person':person},
                                        'P:my:amountable':{'my:amount':betrag},
                                        'P:my:idable':{'my:idvalue':schluessel}
                                        };

                                    erg = executeService("updateProperties", [
                                        {"name": "documentId", "value": id},
                                        {"name": "extraProperties", "value": JSON.stringify(extraProperties)}
                                    ], null, false);
                                    $('#dialogBox').dialog("destroy");
                                    jQuery('#simpleGrid').remove();
                                } catch (e) {
                                    errorHandler(e);
                                }
                            }
                        });
                    }
                }

            } ;

            changeCss('.grid','max-width: 100%; min-width:100%');
            changeCss('input', 'width:100%');
            changeCss('.ui-widget textarea', 'width:100%');
            changeCss('select','max-width: 100%; min-width:10%');
            changeCss('.alpaca-controlfield-checkbox input', 'margin-top:-1px');
            changeCss("input[type='checkbox']", 'width:10px;float:left');
            changeCss('h2','background-color: transparent; background-image: url("./src/main/resource/images/alfresco.png"); background-repeat: no-repeat; background-position: left; height: 24px; border: 0; padding-left: 28px; padding-top: 4px');
            $('head').append('<link href="./src/main/resource/simplegrid.css" rel="stylesheet" id="simpleGrid" />');
            $('<div id="dialogBox">').append(Alpaca($('<div id="form" class="grid grid-pad">'), dialogSettings)).dialog({
                autoOpen: true,
                width: 420,
                height:'auto',
                modal: true,
                open: function(){
                    $(".alpaca-form-buttons-container").addClass("ui-dialog-buttonpane ui-widget-content");
                    $(".alpaca-form-button-submit").button();
                    $(".alpaca-form-button-reset").button().click(function(){  $('#dialogBox').dialog("close"); });
                    /*   $(".alpaca-form-button-submit").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only");
                     $(".alpaca-form-button-submit").css({padding: "0.4em 1em 0.4em 1em"});
                     $(".alpaca-form-button-reset").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only");
                     $(".alpaca-form-button-reset").css({padding: "0.4em 1em 0.4em 1em"});
                     $(".alpaca-form-button-reset").click(function(){  $('#dialogBox').dialog("close"); });
                     $(".alpaca-form-buttons-container").addClass("ui-dialog-buttonpane ui-widget-content");*/
                }
            });
        } catch (e) {
            errorHandler(e);
        }

}