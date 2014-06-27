/**
 * Öffnet den Einstellungsdialog für die Alfresco Server Settings
 */
function startSettingsDialog(){
    try {
        var dialogSettingsLayout;

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
                        "required": true,
                        "pattern": "^[a-zA-Z0-9_]+$"
                    },
                    "server": {
                        "type": "string",
                        "title": "Server",
                        "required": true
                    }
                }
            },
            "options": {

                "fields": {
                    "server": {
                        "size": 57
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
                    "template": "threeColumnGridLayout",
                    "bindings": {
                        "server": "column-1",
                        "user": "column-1",
                        "password": "column-2"

                    }
                },
                "templates": {
                    "threeColumnGridLayout": '<div class="filter-content">' + '{{if options.label}}<h2>${options.label}</h2><span></span>{{/if}}' + '{{if options.helper}}<p>${options.helper}</p>{{/if}}' + '<div id="column-1" class="grid_6"> </div>' + '<div id="column-2" class="grid_6"> </div>' + '<div id="column-3" class="grid_12"> </div>' + '<div class="clear"></div>' + '</div>'
                }

            },
            "ui": "jquery-ui" ,
            "data": {
                "server":    getSettings("server"),
                "user":      getSettings("user"),
                "password":  getSettings("password")
            }
        } ;

        changeCss('.grid_6','width: 200px');
        changeCss('h2','background-color: transparent; background-image: url("./src/main/resource/images/alfresco.png"); background-repeat: no-repeat; background-position: left; height: 24px; border: 0; padding-left: 28px; padding-top: 4px');
        $('<div id="settingsDialog">').append(Alpaca( $('<div id="form">'), dialogSettings)).dialog({
            autoOpen:   true,
            modal:      true,
            width:440,
            height: 'auto',
            buttons: {
                "Save": function() {
                    var bValid = true;
                    var reg = "@^(https?|ftp)://[^\s/$.?#].[^\s]*$@iS";
                    allFields.removeClass( "ui-state-error" );
                    // bValid = bValid && checkRegexp( server, /(http|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?/, "Server Adresse ist ungültig" );
                    bValid = bValid && checkLength(user, "User", 1, -1);
                    bValid = bValid && checkLength(password, "Passwort", 1, -1);
                    settings = {"settings": [{"key":"server", "value":server.val()},
                        {"key":"user", "value":user.val()},
                        {"key":"password", "value":password.val()}]};
                    if ( bValid ) {
                        $.cookie("settings", JSON.stringify(settings), { expires: 9999 });
                        alert("Einstellungen gesichert");
                        init();
                        $( this ).dialog( "close" );
                    }
                },
                Cancel: function() {
                    $( this ).dialog( "close" );
                }
            },
            close: function() {
                allFields.val( "" ).removeClass( "ui-state-error" );
            }
        });

    } catch (e) {
        errorHandler(e);
    }
}