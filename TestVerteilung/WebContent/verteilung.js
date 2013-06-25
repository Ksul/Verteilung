String.prototype.endsWith = function(str) {
	return (this.match(str+"$")==str);
}

String.prototype.startsWith = function(str) {
	return (this.match("^"+str)==str);
}

function getUrlParam(name) {
	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	var regexS = "[\\?&]" + name + "=([^&#]*)";
	var regex = new RegExp(regexS);
	var results = regex.exec(this.location.href);
	if (results == null)
		return null;
	else
		return results[1];
}

function isLocal() {
	return (this.location.href.startsWith("file"));
}

function getSettings(key){
    var ret;
    if (settings.settings.filter(function(o) {return o.key.indexOf(key) >= 0;}).length == 0){
        var urlPar = getUrlParam(key);
        if (urlPar == null)
            return null;
        else
            settings.settings.push({"key":key, "value":urlPar});
    }
    return settings.settings.filter(function(o) {return o.key.indexOf(key) >= 0;})[0].value;
}

function showProgress() {
	$(function() {
		var progressbar = $("#progressbar"), progressLabel = $(".progress-label");
		progressbar.progressbar({
			value : 0,
			change : function() {
				progressLabel.text(Math.round(progressbar.progressbar("value")) + "%");
			},
			complete : function() {
				setTimeout(function(){progressLabel.text("");progressbar.progressbar("destroy");}, 3000);				
			}
		});
	});
}

function manageControls() {



	if (this.multiMode && !this.scriptMode) {
		document.getElementById('inTxt').style.display = 'none';
		document.getElementById('dtable').style.display = 'block';
	} else {

        if (this.alfrescoMode) {
            document.getElementById('tree').style.display = 'block';
            document.getElementById('dtable').style.display = 'none';
            document.getElementById('inTxt').style.display = 'none';
            document.getElementById('closeAlfresco').style.display = 'block';
            document.getElementById('docAlfresco').setAttribute("disabled", true);
        }
        else {
            document.getElementById('tree').style.display = 'none';
            document.getElementById('inTxt').style.display = 'block';
            document.getElementById('dtable').style.display = 'none';
            document.getElementById('closeAlfresco').style.display = 'none';
            document.getElementById('docAlfresco').removeAttribute("disabled");
        }

		document.getElementById('pdf').style.display = 'block';

	}

	if (this.textEditor.getSession().getValue().length == 0) {
		document.getElementById('searchCont').setAttribute("disabled", true);

	} else {
		document.getElementById('searchCont').removeAttribute("disabled");
	}
	if (this.textEditor.getSession().getValue().length == 0 && !this.multiMode) {
		document.getElementById('play').setAttribute("disabled", true);
	} else {
		document.getElementById('play').removeAttribute("disabled");
	}
	if (isLocal()) {
		document.getElementById('save').removeAttribute("disabled");
		document.getElementById('saveScript').removeAttribute("disabled");
	} else {
		document.getElementById('save').setAttribute("disabled", true);
		document.getElementById('saveScript').setAttribute("disabled", true);
	}
	if (!this.multiMode && this.currentPDF)
		document.getElementById('pdf').removeAttribute("disabled");
	else
		document.getElementById('pdf').setAttribute("disabled", true);
	if (this.scriptMode) {
		document.getElementById('filesinput').style.display = 'none';
		document.getElementById('play').style.display = 'none';
		document.getElementById('test').style.display = 'none';
		document.getElementById('back').style.display = 'none';
		document.getElementById('pdf').style.display = 'none';
		document.getElementById('script').style.display = 'none';
		document.getElementById('close').style.display = 'block';
		document.getElementById('sendScript').style.display = 'block';
		document.getElementById('getScript').style.display = 'block';
		document.getElementById('saveScript').style.display = 'block';
		document.getElementById('reloadScript').style.display = 'block';
		document.getElementById('beautifyScript').style.display = 'block';
	} else {
		document.getElementById('filesinput').style.display = 'block';
		document.getElementById('play').style.display = 'block';
		document.getElementById('test').style.display = 'block';
		if (this.showMulti)
			document.getElementById('back').style.display = 'block';
		document.getElementById('pdf').style.display = 'block';
		document.getElementById('script').style.display = 'block';
		document.getElementById('close').style.display = 'none';
		document.getElementById('sendScript').style.display = 'none';
		document.getElementById('getScript').style.display = 'none';
		document.getElementById('saveScript').style.display = 'none';
		document.getElementById('reloadScript').style.display = 'none';
		document.getElementById('beautifyScript').style.display = 'none';
	}

  if (this.runLocal || (this.scriptID == null && this.rulesID == null)) {
		document.getElementById('sendScript').setAttribute("disabled", true);
		document.getElementById('getScript').setAttribute("disabled", true);
		document.getElementById('getRules').setAttribute("disabled", true);
		document.getElementById('sendRules').setAttribute("disabled", true);
  }
}

function openPDF(name, fromServer) {
	try {
  	if (fromServer) {
  		if (isLocal()){
  			var ticket = document.reader.getTicket(getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"), null);
  			window.open(name + "?alf_ticket=" + ticket);
  		}
  		else {
				var dataString = {
						"function"  : "getTicket",
						"server"    : getSettings("server"),
						"username"  : getSettings("user"),
						"password"  : getSettings("password"),
						"proxyHost" : getSettings("proxy"),
						"proxyPort" : getSettings("port")
					};
					$.ajax({
						type        : "POST",
						data        : dataString,
						datatype    : "json",
						url         : "VerteilungServlet",
                        error    : function (response) {
                            try {
                                var r = jQuery.parseJSON(response.responseText);
                                alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                            } catch(e)  {
                                var str = "FEHLER:\n";
                                str = str + e.toString() + "\n";
                                for ( var prop in e)
                                    str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                                alert(str + "\n" + response.responseText);
                            }
                        },
						success     : function(data) {
													   window.open(name + "?alf_ticket=" + data.result.toString());
						              }
					});			
			}
  	 }
			else {
				if (isLocal())
	  			document.reader.openPDF(name);
	  		else 
			  window.open("/TestVerteilung/VerteilungServlet?function=openPDF&fileName=" + name, "_blank");
		}
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function loadText(txt, name, typ, container) {
	this.multiMode = false;
	this.currentFile = name;
	this.currentContainer = container;
	removeMarkers(markers, this.textEditor);
	this.textEditor.getSession().setValue(txt);
    document.getElementById("headerWest").firstChild.nodeValue = name;
	fillMessageBox("", false);
	this.propsEditor.getSession().setValue("");
	manageControls();
}

function loadMultiText(txt, name, typ,  notDeleteable, alfContainer, container) {
	try {
		this.multiMode = true;
		var dat = new Array();
		this.REC.currentDocument.setContent(txt);
		this.REC.testRules(this.rulesEditor.getSession().getValue());
		dat["text"] = txt;
		dat["file"] = name;
		dat["log"] = this.REC.getMessage();
		dat["result"] = this.REC.results;
		dat["position"] = this.REC.positions;
		dat["xml"] = this.REC.currXMLName;
		dat["typ"] = typ;
		dat["error"] = this.REC.errors;
		dat["container"] = container;
		dat["notDeleteable"] = notDeleteable;
		dat["alfContainer"] = alfContainer;
		daten[name] = dat;
		var row = new Array();
		row["id"] = uuid();
		row["feld"] = name;
		row["xml"] = this.REC.currXMLName.join(" : ");
		row["error"] = this.REC.errors;
		var ergebnis = new Array();
		ergebnis["error"] = this.REC.errors.length > 0;
		row["result"] = ergebnis;
		tableData.push(row);
		this.dtable.get("data").add(tableData, {
			silent : false
		});
		manageControls();
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		fillMessageBox(str, false);
	}
}

function handleFileSelect(evt) {
	evt.stopPropagation();
	evt.preventDefault();
	var files = evt.dataTransfer.files;
	readFiles(files);
}

function handleDragOver(evt) {
	evt.stopPropagation();
	evt.preventDefault();
	evt.dataTransfer.dropEffect = 'copy';
}

function readMultiFile(evt) {
	this.multiMode = false;
	this.currentPDF = false;
	var files = evt.target.files;
	readFiles(files);
}

function readFiles(files) {
	try {
		if (!this.currentRules.endsWith("doc.xml")) {
			var open = openFile("doc.xml");
			this.currentRules = open[1];
			this.rulesEditor.getSession().setValue(open[0]);
			this.rulesEditor.getSession().foldAll(1);
		}
		this.textEditor.getSession().setValue("");
		fillMessageBox("", false);
		this.dtable.get("data").reset(null, {
			silent : true
		});
		daten = new Array();
		tableData = new Array();
		var count = files.length;
		var maxLen = 1000000;
		var first = true;
    var reader;
    var blob;
		for ( var i = 0; i < count; i++) {
			var f = files[i];
			if (f) {

				if (f.name.toLowerCase().endsWith(".pdf")) {
					this.currentPDF = true;
					reader = new FileReader();
					reader.onloadend = (function(theFile, clear) {
						return function(evt) {
							if (evt.target.readyState == FileReader.DONE) {// DONE == 2
								var str = btoa(evt.target.result);
								if (isLocal()) {
									for ( var k = 0; k < Math.ceil(str.length / maxLen); k++)
										document.reader.getData(str.substr(k * maxLen, maxLen), k == 0);
									document.reader.extract(theFile.name, files.length > 1, theFile.type);
								} else {
									var dataString = {
										"function"     : "extract",
										"documentText" : str,
										"fileName"     : theFile.name,
										"clear"        : clear
									};
									$.ajax({
										type           : "POST",
										data           : dataString,
										datatype       : "json",
										async          : false,
										url            : "VerteilungServlet",
                                        error    : function (response) {
                                            try {
                                                var r = jQuery.parseJSON(response.responseText);
                                                alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                                            } catch(e)  {
                                                var str = "FEHLER:\n";
                                                str = str + e.toString() + "\n";
                                                for ( var prop in e)
                                                    str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                                                alert(str + "\n" + response.responseText);
                                            }
                                        },
										success        : function(data) {
											                 if (data.success[0]) {
											                	 if (count == 1)
											                		 loadText(data.result[0].toString(), theFile.name, theFile.type, null);
											                	 else {
											                		 loadMultiText(data.result[0].toString(), theFile.name, theFile.type, "false", "false", null);
											                	 }
											                 } else
											                		 alert ("Fehler beim Lesen des PDF: " + data.result[0]);
											                 }
									});
								}
							}
						};
					})(f, first);
					blob = f.slice(0, f.size + 1);
					reader.readAsBinaryString(blob);
				}

				if (f.name.toLowerCase().endsWith(".zip")) {
					reader = new FileReader();
					reader.onloadend = (function(theFile) {
						return function(evt) {
							if (evt.target.readyState == FileReader.DONE) {// DONE == 2
								var str = btoa(evt.target.result);
								if (isLocal()) {
									for ( var k = 0; k < Math.ceil(str.length / maxLen); k++)
										document.reader.getData(str.substr(k * maxLen, maxLen), k == 0);
									count = count + document.reader.extractZIP(theFile.name) - 1;
								} else {
									var dataString = {
										"function"     : "extractZIP",
										"documentText" : str
									};
									$.ajax({
										type           : "POST",
										data           : dataString,
										datatype       : "json",
										url            : "VerteilungServlet",
                                        error    : function (response) {
                                            try {
                                                var r = jQuery.parseJSON(response.responseText);
                                                alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                                            } catch(e)  {
                                                var str = "FEHLER:\n";
                                                str = str + e.toString() + "\n";
                                                for ( var prop in e)
                                                    str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                                                alert(str + "\n" + response.responseText);
                                            }
                                        },
										success        : function(data) {
																			 if (data.success[0]) {
											                   if (data.result[0].entry.length == 1)
												                   loadText(data.result[0].entry[0].result.toString(), data.result[0].entry[0].entryFileName.toString(), "application/zip", null);
											                   else {
												                   for ( var index = 0; index < data.result[0].entry.length; index++) {
												                     var array_element = data.result[0].entry[index];
													                   loadMultiText(array_element.result.toString(), array_element.entryFileName.toString(), "application/zip", "true", "false", null);
												                   }
											                   }
																			 } else
																				 alert("ZIP Datei konnte nicht entpackt werden: " + data.result[0]);
										                  }
									});
								}
							}
						};
					})(f);
					blob = f.slice(0, f.size + 1);
					reader.readAsBinaryString(blob);
				}

				if (f.type == "text/plain") {
					var r = new FileReader();
					if (files.length == 1) {
						r.onload = (function(theFile) {
							return function(e) {
								loadText(e.target.result, theFile.name, theFile.mozFullPath, theFile.type);
							};
						})(f);
					} else {
						r.onload = (function(theFile) {
							return function(e) {
								loadMultiText(e.target.result, theFile.name, theFile.mozFullPath, theFile.type, "false", "false", null);
							};
						})(f);
					}
					r.readAsText(f);
				}
			} else {
				this.textEditor.getSession().setValue(this.textEditor.getSession().getValue() + " Failed to load file!\n");
			}
			first = false;
		}

		this.dtable.render("#dtable");
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function callBack(Y) {
	dtable = new Y.DataTable({
		data : tableData,
		columns : [ {
			key : Y.Plugin.DataTableRowExpansion.column_key,
			label : '&nbsp;',
			formatter : function(o) {
				if (o.rowIndex == 0  && document.getElementById(o.column.id) != null) {
					document.getElementById(o.column.id).style.setProperty("width", "5px", "");
					document.getElementById(o.column.id).style.setProperty("padding", "4px 1px", "");
				}
			}
		}, {
			key : 'feld',
			label : 'Name',
			sortable : true
		}, {
			key : 'xml',
			label : 'Dokumenttyp',
			sortable : true

		}, {
			key : 'result',
			label : '&nbsp;',
			nodeFormatter : imageFieldFormatter,
			sortable : true,
			sortFn : function(a, b, desc) {
				var aCount = daten[a.getAttrs().feld]["error"].length;
				var bCount = daten[b.getAttrs().feld]["error"].length;
				order = (aCount > bCount) ? 1 : (aCount < bCount) ? -1 : 0;
				return desc ? -order : order;
			}
		} ],
		width : '100%'
	});
	dtable.plug(Y.Plugin.DataTableRowExpansion, {
		uniqueIdKey : 'id',
		template : function(data) {
			var err = data.error;
			if (err == "undefined" || err == null || err.length == 0)
				return "Kein Fehler";
			else {
				var txt = "";
				for ( var i = 0; i < err.length; i++) {
					if (txt.length > 0)
						txt = txt + '<br>';
					txt = txt + err[i];
				}
			}
			return txt;
		}
	});
}

function imageFieldFormatter(o) {
	if (o.rowIndex == 0) {
		o.cell.setStyle('width', '102px');
	}
	var image = document.createElement("div");
	image.href = "#";
	if (o.value["error"]) {
		image.style.backgroundImage = "url(ressource/error.png)";
		image.title = "Verteilung fehlerhaft";
	} else {
		image.style.backgroundImage = "url(ressource/ok.png)";
		image.title = "Verteilung erfolgreich";
	}
	image.style.cursor = "pointer";
	image.style.width = "16px";
	image.style.height = "16px";
	image.style.cssFloat = "left";
	image.style.marginRight = "5px";
	image.onclick = function() {
		return (function(name, rowNumber) {
			currentDocument.setContent(this.daten[name]["text"]);
			testRules(this.rulesEditor.getSession().getValue());
			daten[name].log = mess;
			daten[name].result = results;
			daten[name].position = positions;
			daten[name].xml = currXMLName;
			daten[name].error = errors;
			var row = null;
			for ( var i = 0; i < this.tableData.length; i++) {
				var r = this.tableData[i];
				if (r.feld == name) {
					row = r;
					break;
				}
			}
			row["xml"] = currXMLName.join(" : ");
			row["error"] = errors;
			var ergebnis = new Array();
			ergebnis["error"] = errors.length > 0;
			row["result"] = ergebnis;
			tableData[rowNumber] = row;
			this.dtable.modifyRow(rowNumber, row);
		})(o.data.feld, o.rowIndex);
	};
	o.cell.appendChild(image);
	image = document.createElement("div");
	image.href = "#";
	image.title = "Ergebnis anzeigen";
	image.style.backgroundImage = "url(ressource/glass.png)";
	image.style.width = "16px";
	image.style.height = "16px";
	image.style.cursor = "pointer";
	image.style.cssFloat = "left";
	image.style.marginRight = "5px";
	image.onclick = function() {
		return (function(name) {
			this.multiMode = false;
			this.showMulti = true;
			this.currentFile = this.daten[name]["file"];
			setXMLPosition(this.daten[name]["xml"]);
			removeMarkers(markers, this.textEditor);
			markers = setMarkers(this.daten[name]["position"], this.textEditor);
			this.textEditor.getSession().setValue(this.daten[name]["text"]);
			this.propsEditor.getSession().setValue(printResults(this.daten[name]["result"]));
			fillMessageBox(this.daten[name]["log"], true);
			manageControls();
		})(o.data.feld);
	};
	o.cell.appendChild(image);
	image = document.createElement("div");
	image.href = "#";
	image.title = "Ergebnis löschen";
	if (daten[o.data.feld]["notDeleteable"] != "true") {
	  image.style.backgroundImage = "url(ressource/delete.png)";
	  image.style.cursor = "pointer";
	}
	else {
		 image.style.backgroundImage = "url(ressource/delete-bw.png)";
		 image.style.cursor = "not-allowed";
	}
	image.style.width = "16px";
	image.style.height = "16px";
	image.style.cssFloat = "left";
	image.style.marginRight = "5px";
	image.onclick = function() {
		return (function(name, rowNumber) {
			var answer = confirm("Eintrag löschen?");
			if (answer) {
				try {
					netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
				} catch (e) {
					alert("Permission to delete file was denied.");
				}
				this.currentFile = daten[name]["file"];
				this.textEditor.getSession().setValue("");
				this.propsEditor.getSession().setValue("");
				fillMessageBox("", false);
				this.rulesEditor.getSession().foldAll(1);
				if (this.currentFile.length > 0) {
					var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
					file.initWithPath(this.currentFile);
					if (file.exists() == true)
						file.remove(false);
				}
				this.dtable.removeRow(rowNumber);
			}
		})(o.data.feld, o.rowIndex);
	};
	o.cell.appendChild(image);
	image = document.createElement("div");
	if (o.data.feld.toLowerCase().endsWith(".pdf")) {
		image.style.backgroundImage = "url(ressource/pdf.png)";
		image.style.cursor = "pointer";
	} else {
		image.style.backgroundImage = "url(ressource/pdf-bw.png)";
		image.style.cursor = "not-allowed";
	}
	image.style.cssFloat = "left";
	image.style.width = "16px";
	image.style.height = "16px";
	image.style.marginRight = "5px";
	image.title = "PDF anzeigen";
	image.onclick = function() {
		return (function(name) {
			if (typeof daten[name]["container"] != "undefined" && daten[name]["container"] != null) {
				openPDF(daten[name]["container"], true);
			} else {
				openPDF(daten[name]["file"]);
			}
		})(o.data.feld);
	};
	o.cell.appendChild(image);
		image = document.createElement("div");
	if (daten[o.data.feld]["alfContainer"] == "true") {
		image.style.backgroundImage = "url(ressource/move-file.png)";
		image.style.cursor = "pointer";
	} else {
		image.style.backgroundImage = "url(ressource/move-file-bw.png)";
		image.style.cursor = "not-allowed";
	}
	image.style.cssFloat = "left";
	image.style.width = "16px";
	image.style.height = "16px";
	// image.style.marginRight = "5px";
	image.title = "Zur Inbox verschieben";
	image.onclick = function() {
		return (function(name) {
			var docId = "workspace:/SpacesStore/" + this.daten[name]["container"];
			if (isLocal()) {
				var json = jQuery.parseJSON(document.reader.moveDocument(docId, this.inboxID, getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"), null));
				if (!json.success)
					alert("Dokument nicht verschoben: " + json.result);
			}
			else {
				var dataString = {
					"function"			: "moveDocument",
					"documentId"		: docId,
					"destinationId"	    : this.inboxID,
					"server"			: getSettings("server"),
					"username"			: getSettings("user"),
					"password"			: getSettings("password"),
					"proxyHost"			: getSettings("proxy"),
					"proxyPort"			: getSettings("port")
				};
				$.ajax({
					type						: "POST",
					data						: dataString,
					datatype				: "json",
					url							: "VerteilungServlet",
					async						: false,
                    error    : function (response) {
                        try {
                            var r = jQuery.parseJSON(response.responseText);
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                        } catch(e)  {
                            var str = "FEHLER:\n";
                            str = str + e.toString() + "\n";
                            for ( var prop in e)
                                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                            alert(str + "\n" + response.responseText);
                        }
                    },
					success					: function(data) {
						                  if (data.success[0])
						                  	alert("Dokument verschoben!")
						                  else
						                  	alert("Dokument nicht verschoben: " + data.result[0]);
														}
				});
			}
		})(o.data.feld);
	};
	o.cell.appendChild(image);
	return false;
}

function uuid() {
	var chars = '0123456789abcdef'.split('');
	var uuid = [], rnd = Math.random, r;
	uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
	uuid[14] = '4';
	// version 4
	for ( var i = 0; i < 36; i++) {
		if (!uuid[i]) {
			r = 0 | rnd() * 16;
			uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r & 0xf];
		}
	}

	return uuid.join('');
}

function doReRunAll() {
	try {
		this.textEditor.getSession().setValue("");
		fillMessageBox("", false);
		for ( var i = 0; i < tableData.length; i++) {
			var name = this.dtable.get("data").getByClientId(this.dtable.getRow(i).getData()["yui3-record"]).get("feld");
			this.REC.currentDocument.setContent(this.daten[name].text);
			this.REC.testRules(this.rulesEditor.getSession().getValue());
			this.daten[name].log = this.REC.mess;
			this.daten[name].result = this.REC.results;
			this.daten[name].position = this.REC.positions;
			this.daten[name].xml = this.REC.currXMLName;
			this.daten[name].error = this.REC.errors;
			var row = tableData[i];
			row["xml"] = this.REC.currXMLName.join(" : ");
			row["error"] = this.REC.errors;
			var ergebnis = new Array();
			ergebnis["error"] = this.REC.errors.length > 0;
			row["result"] = ergebnis;
			tableData[i] = row;
			this.dtable.modifyRow(i, row);
		}
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function setMarkers(positions, editor) {
	var markers = new Array();
	if (this.REC.exist(positions)) {
		/*
		 * var cssText = ".ace-chrome .ace_marker-layer .ace_step1 {background:
		 * rgb(252, 0, 0);}"; var cssClass = "ace-chrome"; var dom =
		 * require("ace/lib/dom"); dom.importCssString(cssText, cssClass);
		 */
		var markerId;
		for ( var i = 0; i < positions.length; i++) {
			var pos = positions[i];
			var r = new Range(pos.startRow, pos.startColumn, pos.endRow, pos.endColumn);
			if (pos.type)
				markerId = editor.getSession().addMarker(r, "ace_selection", pos.desc, false);
			else
				markerId = editor.getSession().addMarker(r, "ace_step", pos.desc, false);
			markers.push(markerId);
		}
	}
	return markers;
}

function removeMarkers(markers, editor) {
	for ( var i = 0; i < markers.length; i++) {
		editor.getSession().removeMarker(markers[i]);
	}
}

function setXMLPosition(position) {
	this.rulesEditor.getSession().foldAll(1);
	var text = this.rulesEditor.getSession().getValue();
	var pos = 0;
	for ( var i = 0; i < position.length; i++)
		pos = text.indexOf("<archivTyp name=\"" + position[i] + "\"", pos);
	if (pos != -1) {
		pos1 = text.indexOf("</archivTyp>", pos);
		if (pos1 != -1) {
			var p = this.REC.convertPosition(text, pos, pos1 + 12, "");
			this.rulesEditor.getSession().unfold(p.startRow + 1, true);
			this.rulesEditor.gotoLine(p.startRow + 1);
			this.rulesEditor.selection.setSelectionRange(new Range(p.startRow, p.startColumn, p.endRow, p.endColumn));
		}
	}
}

function printResults(results) {
	var ret = "";
	var blanks = "                                               ";
	var maxLength = 0;
	for (key in results) {
		if (key.length > maxLength)
			maxLength = key.length;
	}
	maxLength++;
	for (key in results) {
		if (this.REC.exist(results[key])) {
			ret = ret + key + blanks.substr(0, maxLength - key.length) + ": " + results[key].getValue();
			if (this.REC.exist(results[key].expected)) {
				var tmp = eval(results[key].expected);
				if (this.REC.exist(results[key].getValue()) && tmp.valueOf() == results[key].getValue().valueOf())
					ret = ret + " [OK]";
				else
					ret = ret + " [FALSE] " + tmp;
			}
			ret = ret + "\n";
		}
	}
	return ret;
}

function fillMessageBox(text, reverse) {
	if (reverse) {
		text = text.split('\n').reverse().join('\n');
		if (text.startsWith("\n"))
			text = text.substr(1);
	}
	this.outputEditor.getSession().setValue(text);
}

function doBack() {
	this.multiMode = true;
	this.showMulti = false;
	document.getElementById('inTxt').style.display = 'none';
	document.getElementById('dtable').style.display = 'block';
	document.getElementById('back').style.display = 'none';
	this.textEditor.getSession().setValue("");
	fillMessageBox("", false);
	this.propsEditor.getSession().setValue("");
	this.rulesEditor.getSession().foldAll(1);
	manageControls();
}

function doTest() {
    try {
        if (isLocal()) {
            var open = openFile("test.txt");
            var content = open[0];
            this.REC.currentDocument.setContent(content);
            removeMarkers(markers, this.textEditor);
            this.textEditor.getSession().setValue(content);
            if (!this.currentRules.endsWith("test.xml")) {
                open = openFile("test.xml");
                this.currentRules = open[1];
                this.rulesEditor.getSession().setValue(open[0]);
            }
            this.REC.testRules(this.rulesEditor.getSession().getValue());
            setXMLPosition(this.REC.currXMLName);
            markers = setMarkers(this.REC.positions, this.textEditor);
            this.propsEditor.getSession().setValue(printResults(this.REC.results));
            fillMessageBox(this.REC.getMessage(), true);
            document.getElementById('inTxt').style.display = 'block';
            document.getElementById('dtable').style.display = 'none';
            manageControls();
        } else {
            var dataString = {
                "function": "doTest",
                "fileName": "test.txt",
                "filePath": "test.xml"
            }
            $.ajax({
                type: "POST",
                data: dataString,
                datatype: "json",
                url: "VerteilungServlet",
                error: function (response) {
                    try {
                        var r = jQuery.parseJSON(response.responseText);
                        alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                    } catch (e) {
                        var str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for (var prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        alert(str + "\n" + response.responseText);
                    }
                },
                success: function (data) {
                    if (data.success[0]) {
                        this.REC.currentDocument.setContent(data.result[0].text.toString());
                        removeMarkers(markers, this.textEditor);
                        this.textEditor.getSession().setValue(data.result[0].text.toString());
                        this.currentRules = "test.xml";
                        this.rulesEditor.getSession().setValue(data.result[0].xml.toString());
                        this.REC.testRules(this.rulesEditor.getSession().getValue());
                        setXMLPosition(this.REC.currXMLName);
                        markers = setMarkers(this.REC.positions, this.textEditor);
                        this.propsEditor.getSession().setValue(printResults(this.REC.results));
                        fillMessageBox(this.REC.getMessage(), true);
                        document.getElementById('inTxt').style.display = 'block';
                        document.getElementById('dtable').style.display = 'none';
                        manageControls();
                    } else
                        alert("Fehler: " + data.result[0]);
                }
            });
        }
    } catch (e) {
        var str = "FEHLER:\n";
        str = str + e.toString() + "\n";
        for (var prop in e)
            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
        alert(str);
    }
}

function work() {
	var selectMode = false;
	if (this.multiMode)
		doReRunAll()
	else {
		var range = this.rulesEditor.getSelectionRange();
		var sel = this.rulesEditor.getSession().getTextRange(range);
		if (sel.length > 0) {
			if (!sel.startsWith("<")) {
				var start = this.rulesEditor.find('<', {
					backwards : true,
					wrap : false,
					caseSensitive : false,
					wholeWord : false,
					start : range,
					regExp : false
				});
				range.setStart(start.start);
			}
			if (!sel.endsWith("/>")) {
				var end = this.rulesEditor.find('>', {
					backwards : false,
					wrap : false,
					caseSensitive : false,
					wholeWord : false,
					start : new Range(range.end.row, range.end.column, range.end.row, range.end.column),
					regExp : false
				});
				range.setEnd(end.end);
			}
			sel = this.rulesEditor.getSession().getTextRange(range);
			if (!sel.endsWith("/>")) {
				var tmp = sel.substring(1, sel.indexOf(" "));
				tmp = "</" + tmp + ">";
				end = this.rulesEditor.find(tmp, {
					backwards : false,
					wrap : false,
					caseSensitive : false,
					wholeWord : false,
					start : new Range(range.end.row, range.end.column, range.end.row, range.end.column),
					regExp : false
				});
				range.setEnd(end.end);
			}
			this.rulesEditor.selection.setSelectionRange(range);
			sel = this.rulesEditor.getSession().getTextRange(range);
			if (!sel.startsWith("<tags") && !sel.startsWith("<category") && !sel.startsWith("<archivPosition")) {
				selectMode = true;
				if (!sel.startsWith("<searchItem ")) {
					start = this.rulesEditor.find('<searchItem', {
						backwards : true,
						wrap : false,
						caseSensitive : false,
						wholeWord : false,
						start : range,
						regExp : false
					});
					range.setStart(start.start);
					end = this.rulesEditor.find('</searchItem>', {
						backwards : false,
						wrap : false,
						caseSensitive : false,
						wholeWord : false,
						start : new Range(range.end.row, range.end.column, range.end.row, range.end.column),
						regExp : false
					});
					range.setEnd(end.end);
					this.rulesEditor.selection.setSelectionRange(range);
					sel = this.rulesEditor.getSession().getTextRange(range);
				}
				if (!sel.startsWith("<archivTyp "))
					sel = "<archivTyp name='' searchString=''>" + sel;
				if (!sel.endsWith("</archivTyp>"))
					sel = sel + "</archivTyp>";
				if (!sel.startsWith("<documentTypes "))
					sel = "<documentTypes>" + sel;
				if (!sel.endsWith("</documentTypes>"))
					sel = sel + "</documentTypes>";
			} else
				sel = this.rulesEditor.getSession().getValue();
		} else
			sel = this.rulesEditor.getSession().getValue();
		this.REC.currentDocument.setContent(this.textEditor.getSession().getValue());
		removeMarkers(markers, this.textEditor);
		this.REC.testRules(sel);
		if (!selectMode)
			setXMLPosition(this.REC.currXMLName);
		markers = setMarkers(this.REC.positions, this.textEditor);
		fillMessageBox(this.REC.getMessage(), true);
		this.propsEditor.getSession().setValue(printResults(this.REC.results));
		document.getElementById('inTxt').style.display = 'block';
		document.getElementById('dtable').style.display = 'none';
	}
}

function sendRules(dialog) {
	try {
		var erg = false;
		if (this.currentRules.endsWith("doc.xml")) {
			vkbeautify.xml(this.rulesEditor.getSession().getValue());
			if (isLocal()) {
				var ret = document.reader.updateDocumentByFile(this.rulesID, this.currentRules,
						"XML-Beschreibung der Dokumente", "application/xml", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"), null);
				if (dialog) {
					if (ret == 200) {
						alert("Regeln erfolgreich übertragen!");
						erg = true;
					} else
						alert("Fehler bei der Übertragung: " + ret);
				}
			} else {
			var dataString = {
				"function"     : "updateDocument",
				"documentText" : this.rulesEditor.getSession().getValue(),
				"description"  : "XML-Beschreibung der Dokumente",
				"documentId"   : this.rulesID,
			  "mimeType"     : "application/xml",
				"server"       : getSettings("server"),
				"username"     : getSettings("user"),
				"password"     : getSettings("password"),
				"proxyHost"    : getSettings("proxy"),
				"proxyPort"    : getSettings("port")
			};
			$.ajax({
				type           : "POST",
				data           : dataString,
				datatype       : "json",
				url            : "VerteilungServlet",
                error    : function (response) {
                    try {
                        var r = jQuery.parseJSON(response.responseText);
                        alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                    } catch(e)  {
                        var str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for ( var prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        alert(str + "\n" + response.responseText);
                    }
                },
				success        : function(data) {
					                 if (data.success[0]){
					                   if (dialog)
						                   alert("Regeln erfolgreich übertragen!");
					                 } else
					                	 alert("Fehler beim Übertragen der Regeln: " + data.result[0]);
				                 }
			});
		}
		return erg;
		}
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function loadAlfresco(){
    this.alfrescoMode = true;

    manageControls();
}

function closeAlfresco(){
    this.alfrescoMode = false;
    manageControls();
}

function openSettings(){
    var serverInput = document.getElementById('server');
    serverInput.value = getSettings("server");
    var userInput = document.getElementById('user');
    userInput.value = getSettings("user");
    var passInput = document.getElementById('password');
    passInput.value = getSettings("password");
    var proxyInput = document.getElementById('proxy');
    proxyInput.value =getSettings("proxy");
    var portInput = document.getElementById('port');
    portInput.value = getSettings("port");
    this.$( "#dialog-form" ).dialog( "open" );
}

function loadAlfrescoFolder(folderName) {
	try {
		showProgress();
	   this.dtable.get("data").reset(null, {
      silent : true
    });
    var ret;
    daten = new Array();
    tableData = new Array();
		if (isLocal()) {
			ret = document.reader.listFolder(folderName, "false", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"));
			var json = jQuery.parseJSON(ret);
			var ergebnis = json.result;
			for ( var i = 0; i < ergebnis.length; i++) {
				var erg = ergebnis[i];
				ret = document.reader.getContent(erg.id, true, getSettings("server"), getSettings("user"), getSettings("password"),
						getSettings("proxy"), getSettings("port"), null);
				json = jQuery.parseJSON(ret);
				if (json.success)
				  loadMultiText(json.result, erg.name, erg.typ, "true", "true", getSettings("server") + "/alfresco/s/cmis/s/workspace:SpacesStore/i/" + erg.id
						  + "/content.pdf");
				else
					alert("Fehler beim Holen des Inhalts: " + json.result);
			}
			this.dtable.render("#dtable");
		} else {
			var dataString = {
				"function"  : "listFolder",
				"filePath"  : folderName,
                "withFolder": "false",
				"server"    : getSettings("server"),
				"username"  : getSettings("user"),
				"password"  : getSettings("password"),
				"proxyHost" : getSettings("proxy"),
				"proxyPort" : getSettings("port")
			};
			$.ajax({
				type     : "POST",
				data     : dataString,
				datatype : "json",
				url      : "VerteilungServlet",
                error    : function (response) {
                             try {
                               var r = jQuery.parseJSON(response.responseText);
                               alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                             } catch(e)  {
                               var str = "FEHLER:\n";
                               str = str + e.toString() + "\n";
                               for ( var prop in e)
                                 str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                               alert(str + "\n" + response.responseText);
                             }
                },
				success  : function(data) {
                    if(data.success[0]) {
					            var ergebnis = data.result[0];
					            var anzahl = ergebnis.length;
					            var count = 1;
					            for ( var i = 0; i < anzahl; i++) {
					            	$('#progressbar').progressbar('value', (count++ / (anzahl *2)) * 100);
					            	var erg = ergebnis[i];
						            var dataString = {
						          		"function"   : "getContent",
						          		"documentId" : erg.id,
						          		"extract"    : "true",
						          		"server"     : getSettings("server"),
						          		"username"   : getSettings("user"),
						          		"password"   : getSettings("password"),
						          		"proxyHost"  : getSettings("proxy"),
						          		"proxyPort"  : getSettings("port")
						            };
						            $.ajax({
						          	  type         : "POST",
						          	  data         : dataString,
						          	  datatype     : "json",
						          	  url          : "VerteilungServlet",
                                      async        : false,
                                        error    : function (response) {
                                            try {
                                                var r = jQuery.parseJSON(response.responseText);
                                                alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                                            } catch(e)  {
                                                var str = "FEHLER:\n";
                                                str = str + e.toString() + "\n";
                                                for ( var prop in e)
                                                    str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                                                alert(str + "\n" + response.responseText);
                                            }
                                        },
						          	  success      : function(data) {
                                           if (data.success[0]) {
                                          	 $('#progressbar').progressbar('value', (count++ / (anzahl *2)) * 100);
                                             loadMultiText(data.result.toString(), erg.name, erg.typ, "true", "true", getSettings("server") + "/alfresco/s/cmis/s/workspace:SpacesStore/i/"
									            	             + erg.id + "/content.pdf");
                                           }
                                          else
                                             alert("Fehler beim Holen des Inhalts " + data.result[0]);
							                           }
						              });	          
					             }	
					             this.dtable.render("#dtable");
					           } else
					          	 alert("Fehler beim Lesen des Verzeichnisses: " + data.result[0]);
				}
			});
		}
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function getRules(rDoc, loadLocal, dialog) {
	try {
		if (isLocal()) {
			var ret;
			if (loadLocal) {
				var open = openFile(rDoc);
				this.rulesEditor.getSession().setValue(open[0]);
				this.rulesEditor.getSession().foldAll(1);
			} else {
				ret = document.reader.getContent(rDoc, false, getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"), null);
				var json = jQuery.parseJSON(ret);
				if (json.success) {
					this.rulesEditor.getSession().setValue(json.result);
					this.rulesEditor.getSession().foldAll(1);
					if (dialog)
						alert("Regeln erfolgreich übertragen!");
				} else
					alert("Fehler bei der Übertragung: " + json.result);
			}
		} else {
			var dataString = {
				"function"   : "getContent",
				"documentId" : rDoc,
				"extract"    : "false",
				"server"     : getSettings("server"),
				"username"   : getSettings("user"),
				"password"   : getSettings("password"),
				"proxyHost"  : getSettings("proxy"),
				"proxyPort"  : getSettings("port")
			};
			$.ajax({
				type         : "POST",
				data         : dataString,
				datatype     : "json",
				url          : "VerteilungServlet",
				success      : function(data) {
											   if (data.success[0]) {
					                 if (dialog)
						                 alert("Regeln erfolgreich übertragen!");
				                  	this.rulesEditor.getSession().setValue(data.result[0].toString());
					                this.rulesEditor.getSession().foldAll(1);
										  	 } else
												   alert("Regeln konnten nicht übertragen werden: " + data.result[0]);
				               },
                error    : function (response) {
                    try {
                        var r = jQuery.parseJSON(response.responseText);
                        alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                    } catch(e)  {
                        var str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for ( var prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        alert(str + "\n" + response.responseText);
                    }
                }
			});
		}
		this.currentRules = "doc.xml";
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function format() {
	try {
		var xml = this.rulesEditor.getSession().getValue();
		xml = vkbeautify.xml(xml);
		this.rulesEditor.getSession().setValue(xml);
		// window.parent.frames.rules.rulesEditor.getSession().foldAll(1);
		if (typeof currXMLName != "undefined" && currXMLName != null) {
			setXMLPosition(currXMLName);
			markers = setMarkers(positions, this.textEditor);
		}
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function formatScript() {
	try {
		var txt = this.textEditor.getSession().getValue();
		txt = js_beautify(txt);
		this.textEditor.getSession().setValue(txt);
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function save(file, text, dialog) {
	try {
		var ret = true;
		document.reader.save(file, text)
		if (dialog)
			alert(file + " erfolgreich gesichert!");
		return ret;
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function handleRulesSelect(evt) {
	evt.stopPropagation();
	evt.preventDefault();
	var files = evt.dataTransfer.files;
	for ( var i = 0; i < files.length; i++) {
		var f = files[i];
		if (f) {
			var r = new FileReader();
			r.onload = function(e) {
				var contents = e.target.result;
				this.rulesEditor.getSession().setValue(contents);
				this.rulesEditor.getSession().foldAll(1);
			};
			r.readAsText(f);
		} else {
			alert("Failed to load file!");
		}
	}
}

function openFile(name) {
	try {
		var contents = "";
		var datafile = "file://" + window.location.pathname.substring(0, window.location.pathname.lastIndexOf("/") + 1) + name;
		contents = document.reader.openFile(datafile);
		return [ contents, datafile ];
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function loadScript() {
    try {
        layoutState = myLayout.state;
        myLayout.sizePane("west", "100%");
        oldContent = this.textEditor.getSession().getValue();
        var content;
        if (this.REC.exist(modifiedScript) && modifiedScript - length > 0) {
            content = modifiedScript;
        } else {
            if (isLocal()) {
                var open = openFile("recognition.js");
                content = open[0];
                this.workDocument = open[1];
                eval(content);
                this.REC = new Recognition();
                this.REC.set(this.REC);
                removeMarkers(markers, this.textEditor);
                this.textEditor.getSession().setMode(new jsMode());
                this.textEditor.getSession().setValue(content);
                this.textEditor.setShowInvisibles(false);
                this.scriptMode = true;
                manageControls();
            } else {
                if (this.REC.exist(this.scriptID)) {
                    var dataString = {
                        "function": "getContent",
                        "documentId": this.scriptID,
                        "extract": "false",
                        "server": getSettings("server"),
                        "username": getSettings("user"),
                        "password": getSettings("password"),
                        "proxyHost": getSettings("proxy"),
                        "proxyPort": getSettings("port")
                    };
                    $.ajax({
                        type: "POST",
                        data: dataString,
                        datatype: "json",
                        url: "VerteilungServlet",
                        error: function (response) {
                            try {
                                var r = jQuery.parseJSON(response.responseText);
                                alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                            } catch (e) {
                                var str = "FEHLER:\n";
                                str = str + e.toString() + "\n";
                                for (var prop in e)
                                    str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                                alert(str + "\n" + response.responseText);
                            }
                        },
                        success: function (data) {
                            if (data.success[0]) {
                                content = data.result[0].toString();
                                this.workDocument = "recognition.js";
                                eval(content);
                                this.REC = new Recognition();
                                this.REC.set(this.REC);
                                removeMarkers(markers, this.textEditor);
                                this.textEditor.getSession().setMode(new jsMode());
                                this.textEditor.getSession().setValue(content);
                                this.textEditor.setShowInvisibles(false);
                                this.scriptMode = true;
                                manageControls();
                            } else
                                alert("Script konnte nicht gefunden werden: " + data.result[0]);
                        }
                    });
                }
                else {
                    $.get('recognition.js', function (msg) {
                        this.textEditor.getSession().setMode(new jsMode());
                        this.textEditor.getSession().setValue(msg);
                        this.textEditor.setShowInvisibles(false);
                        this.scriptMode = true;
                        manageControls();
                    });
                }
            }
        }
    } catch (e) {
        var str = "FEHLER:\n";
        str = str + e.toString() + "\n";
        for (var prop in e)
            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
        alert(str);
        myLayout.sizePane("west", layoutState.west.size);
    }
}

function reloadScript(dialog) {
	try {
		modifiedScript = this.textEditor.getSession().getValue();
		eval(modifiedScript);
		this.REC = new Recognition();
		this.REC.set(this.REC);
		alert("Script erfolgreich aktualisiert");
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function getScript(dialog) {
    try {
        if (isLocal()) {
            var ret = document.reader.getContent(this.scriptID, false, getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"),
                getSettings("port"), null);
            var json = jQuery.parseJSON(ret);
            if (json.success) {
                save(this.workDocument, this.textEditor.getSession().getValue(), false);
                this.textEditor.getSession().setValue(json.result);
                if (dialog)
                    alert("Script erfolgreich heruntergeladen!");
            } else
                alert("Fehler bei der Übertragung: " + json.result);
        } else {
            var dataString = {
                "function": "getContent",
                "documentId": this.scriptID,
                "extract": "false",
                "server": getSettings("server"),
                "username": getSettings("user"),
                "password": getSettings("password"),
                "proxyHost": getSettings("proxy"),
                "proxyPort": getSettings("port")
            };
            $.ajax({
                type: "POST",
                data: dataString,
                datatype: "json",
                url: "VerteilungServlet",
                error: function (response) {
                    try {
                        var r = jQuery.parseJSON(response.responseText);
                        alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                    } catch (e) {
                        var str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for (var prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        alert(str + "\n" + response.responseText);
                    }
                },
                success: function (data) {
                    if (data.success[0]) {
                        if (dialog)
                            alert("Script erfolgreich heruntergeladen!");
                        this.textEditor.getSession().setValue(data.result.toString());
                    } else
                        alert("Script konnte nicht geladen werden: " + data.result[0]);
                }
            });
        }
    } catch (e) {
        var str = "FEHLER:\n";
        str = str + e.toString() + "\n";
        for (var prop in e)
            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
        alert(str);
    }
}

function sendScript(dialog) {
	try {
		var erg = false;
		if (this.workDocument.endsWith("recognition.js")) {
			if (isLocal()) {
				document.reader.save(this.workDocument, this.textEditor.getSession().getValue());
				var ret = document.reader.updateDocument(this.scriptID, this.textEditor.getSession().getValue(),
						"VerteilungsScript", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"), null);
				if (dialog) {
					if (ret == 200) {
						alert("Script erfolgreich übertragen!");
						erg = true;
					} else
						alert("Fehler bei der Übertragung: " + ret);
				}
			} else {
				var dataString = {
					"function"     : "updateDocument",
					"documentId"   : this.scriptID,
					"documentText" : this.textEditor.getSession().getValue(),
					"description"  : "VerteilungsScript",
					"server"       : getSettings("server"),
					"username"     : getSettings("user"),
					"password"     : getSettings("password"),
					"proxyHost"    : getSettings("proxy"),
					"proxyPort"    : getSettings("port")
				};
				$.ajax({
					type           : "POST",
					data           : dataString,
					datatype       : "json",
					url            : "VerteilungServlet",
                    error    : function (response) {
                        try {
                            var r = jQuery.parseJSON(response.responseText);
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                        } catch(e)  {
                            var str = "FEHLER:\n";
                            str = str + e.toString() + "\n";
                            for ( var prop in e)
                                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                            alert(str + "\n" + response.responseText);
                        }
                    },
					success        : function(data) {
						                 if (data.success[0]){
						                   if (dialog)
							                   alert("Script erfolgreich übertragen!");
						                 } else
						                	 alert("Script konnte nicht übertragen werden: " + data.result[0]);
					                 }
				});
			}
			return erg;
		}
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function closeScript() {
	try {
        myLayout.sizePane("west", layoutState.west.size);
		this.textEditor.getSession().setMode(new txtMode());
		if (this.REC.exist(oldContent) && oldContent.length > 0)
			this.textEditor.getSession().setValue(oldContent);
		else
			this.textEditor.getSession().setValue("");
		this.textEditor.setShowInvisibles(true);
		this.scriptMode = false;
		manageControls();
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function stringToBytes(str) {
	var ch, st, re = [], j = 0;
	for ( var i = 0; i < str.length; i++) {
		ch = str.charCodeAt(i);
		if (ch < 127) {
			re[j++] = ch & 0xFF;
		} else {
			st = [];
			// clear stack
			do {
				st.push(ch & 0xFF);
				// push byte to stack
				ch = ch >> 8;
				// shift value down by 1 byte
			} while (ch);
			// add stack contents to result
			// done because chars have "wrong" endianness
			st = st.reverse();
			for ( var k = 0; k < st.length; ++k)
				re[j++] = st[k];
		}
	}
	// return an array of bytes
	return re;
}



function init() {
    var cookie = $.cookie("settings");
    if (this.REC.exist(cookie)) {
        settings = $.parseJSON(cookie);
    } else {
        settings = {};
        settings.settings = [];
    }
    if (this.REC.exist(getSettings("server"))) {
	if (isLocal()) {
		var json;
		var txt = [];
		var pattern = new RegExp("true", "ig");
		if (getUrlParam("local") == null || pattern.test(getUrlParam("local"))) {
			this.runLocal = true;
		} else {
			json = jQuery.parseJSON(document.reader.getNodeId("SELECT cmis:objectId from cmis:document where cmis:name='recognition.js'", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"),
					getSettings("port"), null));
			if (json.success)
				this.scriptID = json.result;
			else
				txt.push("Script nicht gefunden! Fehler: " + json.result);
			json = jQuery.parseJSON(document.reader.getNodeId("SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"),
					null));
			if (json.success)
				this.rulesID = json.result;
			else
				txt.push("Regeln nicht gefunden! Fehler: " + json.result);
			json = jQuery.parseJSON(document.reader.getNodeId("SELECT cmis:objectId from cmis:folder where cmis:name='Inbox'", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"),
					null));
			if (json.success)
				this.inboxID = json.result;
			else
				txt.push("Inbox nicht gefunden! Fehler: " + json.result);
            json = jQuery.parseJSON(document.reader.getNodeId("SELECT * from cmis:folder where CONTAINS('PATH:\"//app:company_home/cm:Archiv\"')", getSettings("server"), getSettings("user"), getSettings("password"), getSettings("proxy"), getSettings("port"),
                null));
            if (json.success)
                this.rootID = json.result;
            else
                txt.push("Archiv nicht gefunden! Fehler: " + json.result);

			if (txt.length > 0)
				alert(txt.join("\n"));
		}
	} else {
     	var dataString = {
				"function"     : "getNodeId",
				"cmisQuery"    : "SELECT cmis:objectId from cmis:document where cmis:name='recognition.js'",
				"server"       : getSettings("server"),
				"username"     : getSettings("user"),
				"password"     : getSettings("password"),
				"proxyHost"    : getSettings("proxy"),
				"proxyPort"    : getSettings("port")
			};
			$.ajax({
				type           : "POST",
				data           : dataString,
				datatype       : "json",
				url            : "VerteilungServlet",
				async          : false,
                error    : function (response) {
                    try {
                        var r = jQuery.parseJSON(response.responseText);
                        alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                    } catch(e)  {
                        var str = "FEHLER:\n";
                        str = str + e.toString() + "\n";
                        for ( var prop in e)
                            str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                        alert(str + "\n" + response.responseText);
                    }
                },
				success        : function(data) {
				                   if (data.success[0])
				                      this.scriptID = data.result[0];
				                    else{
				                      alert("Script nicht gefunden! " + data.result[0]);
				                    }
				                 }
			});
		var dataString = {
				"function"     : "getNodeId",
				"cmisQuery"    : "SELECT cmis:objectId from cmis:document where cmis:name='doc.xml'",
				"server"       : getSettings("server"),
				"username"     : getSettings("user"),
				"password"     : getSettings("password"),
				"proxyHost"    : getSettings("proxy"),
				"proxyPort"    : getSettings("port")
				};
				$.ajax({
					type         : "POST",
					data         : dataString,
					datatype     : "json",
					url          : "VerteilungServlet",
					async        : false,
                    error    : function (response) {
                        try {
                            var r = jQuery.parseJSON(response.responseText);
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                        } catch(e)  {
                            var str = "FEHLER:\n";
                            str = str + e.toString() + "\n";
                            for ( var prop in e)
                                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                            alert(str + "\n" + response.responseText);
                        }
                    },
					success      : function(data) {
                                     if (data.success[0])
                                       this.rulesID = data.result[0];
                                     else{
                                       alert("Regeln nicht gefunden! " + data.result[0]);
                                    }
					              }
				});
			var dataString = {
				"function"     : "getNodeId",
				"cmisQuery"     : "SELECT cmis:objectId from cmis:folder where cmis:name='Inbox'",
				"server"       : getSettings("server"),
				"username"     : getSettings("user"),
				"password"     : getSettings("password"),
				"proxyHost"    : getSettings("proxy"),
				"proxyPort"    : getSettings("port")
				};
				$.ajax({
					type         : "POST",
					data         : dataString,
					datatype     : "json",
					url          : "VerteilungServlet",
					async        : false,
                    error    : function (response) {
                        try {
                            var r = jQuery.parseJSON(response.responseText);
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                        } catch(e)  {
                            var str = "FEHLER:\n";
                            str = str + e.toString() + "\n";
                            for ( var prop in e)
                                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                            alert(str + "\n" + response.responseText);
                        }
                    },
					success      : function(data) {
					                 if (data.success[0])
					                   this.inboxID = data.result[0];
					                 else{
					                   alert("Inbox nicht gefunden! " + data.result[0]);
					                 }   					             	 
					               }
				});
        var dataString = {
            "function"     : "getNodeId",
            "cmisQuery"    : "SELECT * from cmis:folder where CONTAINS('PATH:\"//app:company_home/cm:Archiv\"')",
            "server"       : getSettings("server"),
            "username"     : getSettings("user"),
            "password"     : getSettings("password"),
            "proxyHost"    : getSettings("proxy"),
            "proxyPort"    : getSettings("port")
        };
        $.ajax({
            type         : "POST",
            data         : dataString,
            datatype     : "json",
            url          : "VerteilungServlet",
            async        : false,
            error    : function (response) {
                try {
                    var r = jQuery.parseJSON(response.responseText);
                    alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                } catch(e)  {
                    var str = "FEHLER:\n";
                    str = str + e.toString() + "\n";
                    for ( var prop in e)
                        str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                    alert(str + "\n" + response.responseText);
                }
            },
            success      : function(data) {
                if (data.success[0])
                    this.rootID = data.result[0];
                else{
                    alert("Archiv nicht gefunden! " + data.result[0]);
                }
            }
        });
		}
    }
	manageControls();
}

var Range = require("ace/range").Range;
var markers = new Array();
var results = new Array();
var tableData = new Array();
var oldContent = null;
var modifiedScript = null;
var searchCont = false;
var searchRules = false;
