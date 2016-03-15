/**
 * jQuery Theme Switcher plugin
 *
 * Copyright (c) 2011 Dave Hoff (davehoff.com)
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 *
 */

(function ($) {

    $.fn.themeswitcher = function (options) {
        var switcherDiv = this, switcherOptions = {};
        var settings = {
            loadtheme: "",
            height: 200,
            width: 175,
            rounded: true,
            imgpath: "./src/main/resource/images/",
            themepath: "https://ajax.googleapis.com/ajax/libs/jqueryui/",
            jqueryuiversion: "1.11.2",
            initialtext: "Switch Theme",
            buttonpretext: "Theme:",
            closeonselect: true,
            buttonheight: 14,
            cookiename: "jquery-ui-theme",
            themes: [],
            additionalthemes: [],
            onopen: null,
            onclose: null,
            onselect: null,
            cookieexpires: 365,
            cookiepath: '/'
        };

        if (options) {
            //lowercase all options passed in
            $.each(options, function (k, v) {
                switcherOptions[k.toLowerCase()] = v;
            });

            $.extend(settings, switcherOptions);
        }

        if (!settings.themes.length) {
            var themes = [
                {
                    title: "Black Tie",
                    name: "black-tie",
                    icon: "theme_90_black_tie.png",
                    url:  "./src/main/resource/themes/black-tie/jquery-ui.css"
                },
                {
                    title: "Blitzer",
                    name: "blitzer",
                    icon: "theme_90_blitzer.png",
                    url:  "./src/main/resource/themes/blitzer/jquery-ui.css"
                },
                {
                    title: "Cupertino",
                    name: "cupertino",
                    icon: "theme_90_cupertino.png",
                    url:  "./src/main/resource/themes/cupertino/jquery-ui.css"
                },
                {
                    title: "Dark Hive",
                    name: "dark-hive",
                    icon: "theme_90_dark_hive.png",
                    url:  "./src/main/resource/themes/dark-hive/jquery-ui.css"
                },
                {
                    title: "Dot Luv",
                    name: "dot-luv",
                    icon: "theme_90_dot_luv.png",
                    url:  "./src/main/resource/themes/dot-luv/jquery-ui.css"
                },
                {
                    title: "Eggplant",
                    name: "eggplant",
                    icon: "theme_90_eggplant.png",
                    url:  "./src/main/resource/themes/eggplant/jquery-ui.css"
                },
                {
                    title: "Excite Bike",
                    name: "excite-bike",
                    icon: "theme_90_excite_bike.png",
                    url:  "./src/main/resource/themes/excite-bike/jquery-ui.css"
                },
                {
                    title: "Flick",
                    name: "flick",
                    icon: "theme_90_flick.png",
                    url:  "./src/main/resource/themes/flick/jquery-ui.css"
                },
                {
                    title: "Hot Sneaks",
                    name: "hot-sneaks",
                    icon: "theme_90_hot_sneaks.png",
                    url:  "./src/main/resource/themes/hot-sneaks/jquery-ui.css"
                },
                {
                    title: "Humanity",
                    name: "humanity",
                    icon: "theme_90_humanity.png",
                    url:  "./src/main/resource/themes/humanity/jquery-ui.css"
                },
                {
                    title: "Le Frog",
                    name: "le-frog",
                    icon: "theme_90_le_frog.png",
                    url:  "./src/main/resource/themes/le-frog/jquery-ui.css"
                },
                {
                    title: "Mint Choc",
                    name: "mint-choc",
                    icon: "theme_90_mint_choco.png",
                    url:  "./src/main/resource/themes/mint-chock/jquery-ui.css"
                },
                {
                    title: "Overcast",
                    name: "overcast",
                    icon: "theme_90_overcast.png",
                    url:  "./src/main/resource/themes/overcast/jquery-ui.css"
                },
                {
                    title: "Pepper Grinder",
                    name: "pepper-grinder",
                    icon: "theme_90_pepper_grinder.png",
                    url:  "./src/main/resource/themes/pepper-grinder/jquery-ui.css"
                },
                {
                    title: "Redmond",
                    name: "redmond",
                    icon: "theme_90_windoze.png",
                    url:  "./src/main/resource/themes/redmond/jquery-ui.css"
                },
                {
                    title: "Smoothness",
                    name: "smoothness",
                    icon: "theme_90_smoothness.png",
                    url:  "./src/main/resource/themes/smoothness/jquery-ui.css"
                },
                {
                    title: "South Street",
                    name: "south-street",
                    icon: "theme_90_south_street.png",
                    url:  "./src/main/resource/themes/south-street/jquery-ui.css"
                },
                {
                    title: "Start",
                    name: "start",
                    icon: "theme_90_start_menu.png",
                    url:  "./src/main/resource/themes/start/jquery-ui.css"
                },
                {
                    title: "Sunny",
                    name: "sunny",
                    icon: "theme_90_sunny.png",
                    url:  "./src/main/resource/themes/sunny/jquery-ui.css"
                },
                {
                    title: "Swanky Purse",
                    name: "swanky-purse",
                    icon: "theme_90_swanky_purse.png",
                    url:  "./src/main/resource/themes/swanky-purse/jquery-ui.css"
                },
                {
                    title: "Trontastic",
                    name: "trontastic",
                    icon: "theme_90_trontastic.png",
                    url:  "./src/main/resource/themes/trontastic/jquery-ui.css"
                },
                {
                    title: "UI Darkness",
                    name: "ui-darkness",
                    icon: "theme_90_ui_dark.png",
                    url:  "./src/main/resource/themes/ui-darkness/jquery-ui.css"
                },
                {
                    title: "UI Lightness",
                    name: "ui-lightness",
                    icon: "theme_90_ui_light.png",
                    url:  "./src/main/resource/themes/ui-lightness/jquery-ui.css"
                },
                {
                    title: "Vader",
                    name: "vader",
                    icon: "theme_90_black_matte.png",
                    url:  "./src/main/resource/themes/vader/jquery-ui.css"
                }
            ]
        } else {
            var themes = settings.themes;
        }

        if (settings.additionalthemes.length) {
            $.extend(themes, settings.additionalthemes);
        }

        var allUrls = getAllLinkUrls();

        // Switcher link
        var switcherLinkStyle = {
            "cursor": "pointer",
            "font-family": "'Trebuchet MS', Verdana, sans-serif",
            "font-size": "11px",
            "color": "#666",
            "background-image": "url('./src/main/resource/images/themes.png')" ,
            "border": "1px solid #CCC",
            "text-decoration": "none",
            "display": "block",
            "outline": "0px",
            "width": "24px",
            "height": "24px"

        }

        if (settings.rounded) {
            switcherLinkStyle['border-radius'] = "6px";
            switcherLinkStyle['-moz-border-radius'] = "6px";
            switcherLinkStyle['-webkit-border-radius'] = "6px";
        }

        var switcherLink = $("<a/>")
            .addClass("jquery-ui-switcher-link")
            .css(switcherLinkStyle)
            .bind({
                mouseenter: function () {
                    $(this).css({
                        "background-image": "url('./src/main/resource/images/themes.png')"
                    })
                },
                mouseleave: function () {
                    if (!switcherDiv.find(".jquery-ui-switcher-list-hldr").is(":visible")) {
                        $(this).css({
                            "background-image": "url('./src/main/resource/images/themes.png')"
                        })
                    }
                },
                click: function () {
                    (!switcherDiv.find(".jquery-ui-switcher-list-hldr").is(":visible")) ? openSwitcher() : closeSwitcher();
                }
            });

        // Title & Icon for switcher link
        var switcherTitle = $("<span/>").addClass("jquery-ui-switcher-title").appendTo(switcherLink);
        $("<span/>").appendTo(switcherLink);

        // load the default theme or the theme stored in the cookie
        if ($.cookie(settings.cookiename)) {
            updateTheme(findTheme($.cookie(settings.cookiename)));

        } else if (settings.loadtheme.length) {
            updateTheme(findTheme(settings.loadtheme));

        } else {
            $("#switcher").attr("title", settings.initialtext);
        }

        var switcherListHldr = $("<div/>")
            .addClass("jquery-ui-switcher-list-hldr")
            .css({
                "width": eval(settings.width + 8) + "px",
                "background": "#000",
                "color": "#FFF",
                "font-family": "'Trebuchet MS', Verdana, sans-serif",
                "font-size": "12px",
                "border": "1px solid #CCC",
                "border-top": "none",
                "z-index": "999999",
                "position": "absolute",
                "top": "29px",
                "right": "0px",
                "padding": "3px 3px 3px 0",
                "display": "none"
            })
            .bind({
                mouseleave: function () {
                    closeSwitcher();
                }
            });

        if (settings.rounded) {
            switcherListHldr.css("border-radius", "0 0 6px 6px");
            switcherListHldr.css("-moz-border-radius", "0 0 6px 6px");
            switcherListHldr.css("-webkit-border-radius", "0 0 6px 6px");
        }

        var switcherList = $("<ul/>")
            .css({
                "list-style": "none",
                "margin": "0",
                "padding": "0",
                "overflow-y": "auto",
                "overflow-x": "hidden",
                "height": settings.height + "px"
            })
            .appendTo(switcherListHldr);

        // Iterate over themes and build links
        $.each(themes, function (k, v) {
            var listItem = $("<li>")
                .css("height", "90px")
                .appendTo(switcherList);

            var listLink = $("<a>")
                .css({
                    "display": "block",
                    "padding": "5px 3px 5px 5px",
                    "text-decoration": "none",
                    "float": "left",
                    "width": "100%",
                    "clear": "left"
                })
                .bind({
                    click: function (e) {
                        updateTheme($(this).data());
                        $('#switcher').data("ui-tooltip-title", $(this).data().title);
                        return false;
                    }
                })
                .attr("href", "#")
                .data(v)
                .appendTo(listItem);

            var linkImg = $("<img>")
                .attr("src", settings.imgpath + v.icon)
                .attr("title", v.title)
                .css({
                    "float": "left",
                    "width": "90px",
                    "height": "80px",
                    "margin-right": "5px",
                    "border": "1px solid #333"
                })
                .appendTo(listLink);

            var linkText = $("<span>")
                .css({
                    "float": "left",
                    "padding-top": "5px",
                    "color": "#AAA"
                })
                .text(v.title)
                .appendTo(listLink);
        });

        function getAllLinkUrls() {
            var urls = new Array();
            var urlPrefix = settings.themepath + settings.jqueryuiversion + "/themes/";
            $.each(themes, function (k, v) {
                if (themes[k].url)
                    // URL ist im Theme bereits angegben
                    urls.push(themes[k].url)
                else
                    // Themes vom jQuery Server
                    urls.push(urlPrefix + themes[k].name + "/jquery-ui.css");
            });
            if (settings.additionalthemes.length) {
                $.each(settings.additionalthemes, function (k, v) {
                    urls.push(v.url);
                });
            }
            return urls;
        };

        function findCurrentStyle() {
            var currentStyle = new Array();
            var links = $("head link[rel='stylesheet']");
            $.each(links, function (k, v) {
                if ($.inArray(v.attributes["href"].value, allUrls) != -1) {
                    currentStyle.push(v);
                    return false;
                }
            });
            return currentStyle;
        };

        function updateTheme(data) {
            if (settings.onselect !== null)
                settings.onselect();

            var currentStyle = [];
            var url = data.url;

            if (!url) {
                var urlPrefix = settings.themepath + settings.jqueryuiversion + "/themes/";
                url = urlPrefix + data.name + "/jquery-ui.css";
            }
            currentStyle = findCurrentStyle();

            if (currentStyle.length) {
                currentStyle[0].href = url;
            } else {
                var style = $("<link/>")
                    .attr("type", "text/css")
                    .attr("rel", "stylesheet")
                    .attr("href", url);
                var dest = $('link');
                if (typeof(dest) == "object" && dest.length > 0)
                    style.insertBefore(dest[0]);
                else
                    style.appendTo("head");
            }

            $.cookie(settings.cookiename, data.name,
                { expires: settings.cookieexpires, path: settings.cookiepath }
            );



            if (settings.closeonselect)
                closeSwitcher();
            $('#switcher').attr('title', data.title);
        }

        // Finds a theme[] based on a valid name or title
        function findTheme(theme) {
            var result = null;
            $.each(themes, function (k, v) {
                if (v.name.toLowerCase() === theme.toLowerCase() || v.title.toLowerCase() === theme.toLowerCase()) {
                    result = v;
                    return false;
                }
            });

            if (!result) {
                return themes[0];
            }
            return result;
        }

        function openSwitcher() {
            if (settings.onopen !== null)
                settings.onopen();
            $(document).tooltip({ disabled: true });

            switcherDiv.find(".jquery-ui-switcher-list-hldr").slideDown("fast");
        }

        function closeSwitcher() {
            if (settings.onclose !== null)
                settings.onclose();
            $(document).tooltip("enable");
            switcherDiv.find(".jquery-ui-switcher-list-hldr").slideUp("fast", function () {

            });
        }


        this.css("position", "relative");
        this.append(switcherLink);
        this.append(switcherListHldr);

        return this;
    }

})(jQuery);

/*!
 * jQuery Cookie Plugin
 * https://github.com/carhartl/jquery-cookie
 *
 * Copyright 2011, Klaus Hartl
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.opensource.org/licenses/GPL-2.0
 */
(function ($) {
    $.cookie = function (key, value, options) {

        // key and at least value given, set cookie...
        if (arguments.length > 1 && (!/Object/.test(Object.prototype.toString.call(value)) || value === null || value === undefined)) {
            options = $.extend({}, options);

            if (value === null || value === undefined) {
                options.expires = -1;
            }

            if (typeof options.expires === 'number') {
                var days = options.expires, t = options.expires = new Date();
                t.setDate(t.getDate() + days);
            }

            value = String(value);

            return (document.cookie = [
                encodeURIComponent(key), '=', options.raw ? value : encodeURIComponent(value),
                options.expires ? '; expires=' + options.expires.toUTCString() : '', // use expires attribute, max-age is not supported by IE
                options.path ? '; path=' + options.path : '',
                options.domain ? '; domain=' + options.domain : '',
                options.secure ? '; secure' : ''
            ].join(''));
        }

        // key and possibly options given, get cookie...
        options = value || {};
        var decode = options.raw ? function (s) {
            return s;
        } : decodeURIComponent;

        var pairs = document.cookie.split('; ');
        for (var i = 0, pair; pair = pairs[i] && pairs[i].split('='); i++) {
            if (decode(pair[0]) === key) return decode(pair[1] || ''); // IE saves cookies with empty string as "c; ", e.g. without "=" as opposed to EOMB, thus pair[1] may be undefined
        }
        return null;
    };
})(jQuery);