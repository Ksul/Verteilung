
/**
 * Öffnet den Einstellungsdialog für die Alfresco Server Settings
 */
function startSettingsDialog(){
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
                        "pattern": "@^(https?|ftp)://[^\s/$.?#].[^\s]*$@iS"
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
                    "server": {
                        "size": 60
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
                        "server":"column-1-1",
                        "user":"column-1-7_12",
                        "password":"column-2-5_12"
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
            "ui": "jquery-ui" ,
            "data": {
                "server":    getSettings("server"),
                "user":      getSettings("user"),
                "password":  getSettings("password")
            },
            "postRender": function(renderedField) {
                var form = renderedField.form;
                if (form) {
                    form.registerSubmitHandler(function(e) {
                        if (form.isFormValid()) {
                            try {
                                var server = $("[name='server']").val(),
                                    user = $("[name='user']").val(),
                                    password = $("[name='password']").val();
                                if (!server.endsWith("/"))
                                    server = server + "/";
                                settings = {"settings": [{"key":"server", "value":server},
                                    {"key":"user", "value":user},
                                    {"key":"password", "value":password}]};
                                $.cookie("settings", JSON.stringify(settings), { expires: 9999 });
                                fillMessageBox("Einstellungen gesichert");
                                $('#settingsDialog').dialog("close");
                                checkAndBuidAlfrescoEnvironment();
                                openRules();
                                manageControls();
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
        changeCss('h2','background-color: transparent; background-image: url("./src/main/resource/images/alfresco.png"); background-repeat: no-repeat; background-position: left; height: 24px; border: 0; padding-left: 28px; padding-top: 4px');
        $('<div id="settingsDialog">').append(Alpaca( $('<div id="form">'), dialogSettings)).dialog({
            autoOpen:   true,
            modal:      true,
            width:420,
            height: 'auto',
            open: function(){
                    $(".alpaca-form-button-submit").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only");
                    $(".alpaca-form-button-submit").css({padding: "0.4em 1em 0.4em 1em"});
                    $(".alpaca-form-button-reset").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only");
                    $(".alpaca-form-button-reset").css({padding: "0.4em 1em 0.4em 1em"});
                    $(".alpaca-form-button-reset").click(function(){  $('#settingsDialog').dialog("close"); });
                    $(".alpaca-form-buttons-container").addClass("ui-dialog-buttonpane ui-widget-content");
                }

        });

    } catch (e) {
        errorHandler(e);
    }
}