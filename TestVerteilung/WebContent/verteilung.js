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
	var results = regex.exec(window.parent.location.href);
	if (results == null)
		return null;
	else
		return results[1];
}

function isLocal() {
	return (window.parent.location.href.startsWith("file"));
}

function getServer() {
	if (paramServer == null)
		paramServer = getUrlParam("server");
	return paramServer;
}

function getUser(){
	if (paramUser == null)
		paramUser = getUrlParam("user");
	return paramUser;
}

function getPassword(){
	if (paramPass == null)
		paramPass = getUrlParam("password");
	return paramPass;
}

function manageControls() {
	if (window.parent.frames.cont.multiMode && !window.parent.scriptMode) {
		window.parent.frames.cont.document.getElementById('inTxt').style.display = 'none';
		window.parent.frames.cont.document.getElementById('dtable').style.display = 'block';
	} else {
		window.parent.frames['control'].document.getElementById('pdf').style.display = 'block';
		window.parent.frames.cont.document.getElementById('inTxt').style.display = 'block';
		window.parent.frames.cont.document.getElementById('dtable').style.display = 'none';
	}
	if (window.parent.frames.cont.textEditor.getSession().getValue().length == 0) {
		window.parent.frames.control.document.getElementById('searchCont').setAttribute("disabled", true);

	} else {
		window.parent.frames.control.document.getElementById('searchCont').removeAttribute("disabled");
	}
	if (window.parent.frames.cont.textEditor.getSession().getValue().length == 0 && !window.parent.frames.cont.multiMode) {
		window.parent.frames.control.document.getElementById('play').setAttribute("disabled", true);
	} else {
		window.parent.frames.control.document.getElementById('play').removeAttribute("disabled");
	}
	if (isLocal()) {
		window.parent.frames.control.document.getElementById('save').removeAttribute("disabled");
		window.parent.frames.control.document.getElementById('saveScript').removeAttribute("disabled");
	} else {
		window.parent.frames.control.document.getElementById('save').setAttribute("disabled", true);
		window.parent.frames.control.document.getElementById('saveScript').setAttribute("disabled", true);
	}
	if (!window.parent.frames.cont.multiMode && window.parent.frames.cont.currentPDF)
		window.parent.frames.control.document.getElementById('pdf').removeAttribute("disabled");
	else
		window.parent.frames.control.document.getElementById('pdf').setAttribute("disabled", true);
	if (window.parent.scriptMode) {
		window.parent.frames.control.document.getElementById('filesinput').style.display = 'none';
		window.parent.frames.control.document.getElementById('play').style.display = 'none';
		window.parent.frames.control.document.getElementById('test').style.display = 'none';
		window.parent.frames.control.document.getElementById('back').style.display = 'none';
		window.parent.frames.control.document.getElementById('pdf').style.display = 'none';
		window.parent.frames.control.document.getElementById('script').style.display = 'none';
		window.parent.frames.control.document.getElementById('close').style.display = 'block';
		window.parent.frames.control.document.getElementById('send').style.display = 'block';
		window.parent.frames.control.document.getElementById('get').style.display = 'block';
		window.parent.frames.control.document.getElementById('saveScript').style.display = 'block';
		window.parent.frames.control.document.getElementById('reloadScript').style.display = 'block';
		window.parent.frames.control.document.getElementById('beautifyScript').style.display = 'block';
	} else {
		window.parent.frames.control.document.getElementById('filesinput').style.display = 'block';
		window.parent.frames.control.document.getElementById('play').style.display = 'block';
		window.parent.frames.control.document.getElementById('test').style.display = 'block';
		if (window.parent.frames.cont.showMulti)
			window.parent.frames.control.document.getElementById('back').style.display = 'block';
		window.parent.frames.control.document.getElementById('pdf').style.display = 'block';
		window.parent.frames.control.document.getElementById('script').style.display = 'block';
		window.parent.frames.control.document.getElementById('close').style.display = 'none';
		window.parent.frames.control.document.getElementById('send').style.display = 'none';
		window.parent.frames.control.document.getElementById('get').style.display = 'none';
		window.parent.frames.control.document.getElementById('saveScript').style.display = 'none';
		window.parent.frames.control.document.getElementById('reloadScript').style.display = 'none';
		window.parent.frames.control.document.getElementById('beautifyScript').style.display = 'none';
	}
	if(window.parent.rulesID != null && window.parent.scriptID != null && !window.parent.scriptMode) {
		window.parent.frames.control.document.getElementById('docUnknown').removeAttribute("disabled");
		window.parent.frames.control.document.getElementById('docError').removeAttribute("disabled");
	} else {
		window.parent.frames.control.document.getElementById('docUnknown').setAttribute("disabled", true);
		window.parent.frames.control.document.getElementById('docError').setAttribute("disabled", true);
	}
  if (window.parent.runLocal) {
		window.parent.frames.control.document.getElementById('send').setAttribute("disabled", true);
		window.parent.frames.control.document.getElementById('get').setAttribute("disabled", true);
		window.parent.frames.control.document.getElementById('load').setAttribute("disabled", true);
		window.parent.frames.control.document.getElementById('upload').setAttribute("disabled", true);
  }
}

function openPDF(name, fromServer) {
	try {
  	if (fromServer) {
  		if (isLocal()){
  			var ticket = window.parent.frames.control.document.reader.getTicket(getServer(), getUser(), getPassword(), getUrlParam("proxy"), getUrlParam("port"), null);
  			window.open(name + "?alf_ticket=" + ticket);
  		}
  		else {
				var dataString = {
						"function"  : "getTicket",
						"server"    : getServer(),
						"username"  : getUser(),
						"password"  : getPassword(),
						"proxyHost" : getUrlParam("proxy"),
						"proxyPort" : getUrlParam("port")
					};
					$.ajax({
						type        : "POST",
						data        : dataString,
						datatype    : "json",
						url         : "VerteilungServlet",
    			  error       : function (response) {
                            var r = jQuery.parseJSON(response.responseText);
                            alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                          },
						success     : function(data) {
													   window.open(name + "?alf_ticket=" + data.result.toString());
						              }
					});			
			}
  	 }
			else {
				if (isLocal())
	  			window.parent.frames.control.document.reader.openPDF(name);
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
	window.parent.frames.cont.multiMode = false;
	window.parent.frames.cont.currentFile = name;
	window.parent.frames.cont.currentContainer = container;
	removeMarkers(markers, window.parent.frames['cont'].textEditor);
	window.parent.frames.cont.textEditor.getSession().setValue(txt);
	fillMessageBox("", false);
	window.parent.frames.props.propsEditor.getSession().setValue("");
	manageControls();
}

function loadMultiText(txt, name, typ,  notDeleteable, alfContainer, container) {
	try {
		window.parent.frames.cont.multiMode = true;
		var dat = new Array();
		window.parent.REC.currentDocument.setContent(txt);
		window.parent.REC.testRules(window.parent.frames['rules'].rulesEditor.getSession().getValue());
		dat["text"] = txt;
		dat["file"] = name;
		dat["log"] = window.parent.REC.getMessage();
		dat["result"] = window.parent.REC.results;
		dat["position"] = window.parent.REC.positions;
		dat["xml"] = window.parent.REC.currXMLName;
		dat["typ"] = typ;
		dat["error"] = window.parent.REC.errors;
		dat["container"] = container;
		dat["notDeleteable"] = notDeleteable;
		dat["alfContainer"] = alfContainer;
		daten[name] = dat;
		var row = new Array();
		row["id"] = uuid();
		row["feld"] = name;
		row["xml"] = window.parent.REC.currXMLName.join(" : ");
		row["error"] = window.parent.REC.errors;
		var ergebnis = new Array();
		ergebnis["error"] = window.parent.REC.errors.length > 0;
		row["result"] = ergebnis;
		tableData.push(row);
		window.parent.frames.cont.dtable.get("data").add(tableData, {
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
	window.parent.frames.cont.multiMode = false;
	window.parent.frames.cont.currentPDF = false;
	var files = evt.target.files;
	readFiles(files);
}

function readFiles(files) {
	try {
		if (!window.parent.currentRules.endsWith("doc.xml")) {
			var open = openFile("doc.xml");
			window.parent.currentRules = open[1];
			window.parent.frames.rules.rulesEditor.getSession().setValue(open[0]);
			window.parent.frames.rules.rulesEditor.getSession().foldAll(1);
		}
		window.parent.frames.cont.textEditor.getSession().setValue("");
		fillMessageBox("", false);
		window.parent.frames.cont.dtable.get("data").reset(null, {
			silent : true
		});
		daten = new Array();
		tableData = new Array();
		var count = files.length;
		var maxLen = 1000000;
		var first = true;
        var reader;
        var blob;
		for ( var i = 0; i < files.length; i++) {
			var f = files[i];
			if (f) {

				if (f.name.toLowerCase().endsWith(".pdf")) {
					window.parent.frames.cont.currentPDF = true;
					reader = new FileReader();
					reader.onloadend = (function(theFile, clear) {
						return function(evt) {
							if (evt.target.readyState == FileReader.DONE) {// DONE == 2
								var str = btoa(evt.target.result);
								if (isLocal()) {
									for ( var k = 0; k < Math.ceil(str.length / maxLen); k++)
										window.parent.frames.control.document.reader.getData(str.substr(k * maxLen, maxLen), k == 0);
									window.parent.frames.control.document.reader.extract(theFile.name, files.length > 1, theFile.type);
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
										url            : "VerteilungServlet",
                                        error          : function (response) {
                                                           var r = jQuery.parseJSON(response.responseText);
                                                           alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                                                         },
										success        : function(data) {
											                 if (data.success[0]) {
											                	 if (files.length == 1)
											                		 loadText(data.result[0].toString(), theFile.name, theFile.type, null);
											                	 else
											                		 loadMultiText(data.result[0].toString(), theFile.name, theFile.type, "false", "false", null);
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
										window.parent.frames.control.document.reader.getData(str.substr(k * maxLen, maxLen), k == 0);
									count = count + window.parent.frames.control.document.reader.extractZIP(theFile.name) - 1;
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
			      			  error          : function (response) {
                                       var r = jQuery.parseJSON(response.responseText);
                                       alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
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
				window.parent.frames['cont'].textEditor.getSession().setValue(window.parent.frames['cont'].textEditor.getSession().getValue() + " Failed to load file!\n");
			}
			first = false;
		}

		window.parent.frames.cont.dtable.render("#dtable");
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
				var aCount = window.parent.frames.control.daten[a.getAttrs().feld]["error"].length;
				var bCount = window.parent.frames.control.daten[b.getAttrs().feld]["error"].length;
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
			currentDocument.setContent(window.parent.frames.control.daten[name]["text"]);
			testRules(window.parent.frames['rules'].rulesEditor.getSession().getValue());
			window.parent.frames.control.daten[name].log = mess;
			window.parent.frames.control.daten[name].result = results;
			window.parent.frames.control.daten[name].position = positions;
			window.parent.frames.control.daten[name].xml = currXMLName;
			window.parent.frames.control.daten[name].error = errors;
			var row = null;
			for ( var i = 0; i < window.parent.frames.control.tableData.length; i++) {
				var r = window.parent.frames.control.tableData[i];
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
			window.parent.frames.cont.dtable.modifyRow(rowNumber, row);
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
			window.parent.frames.cont.multiMode = false;
			window.parent.frames.cont.showMulti = true;
			window.parent.frames.cont.currentFile = window.parent.frames.control.daten[name]["file"];
			setXMLPosition(window.parent.frames.control.daten[name]["xml"]);
			removeMarkers(markers, window.parent.frames['cont'].textEditor);
			markers = setMarkers(window.parent.frames.control.daten[name]["position"], window.parent.frames['cont'].textEditor);
			window.parent.frames.cont.textEditor.getSession().setValue(window.parent.frames.control.daten[name]["text"]);
			window.parent.frames.props.propsEditor.getSession().setValue(printResults(window.parent.frames.control.daten[name]["result"]));
			fillMessageBox(window.parent.frames.control.daten[name]["log"], true);
			manageControls();
		})(o.data.feld);
	};
	o.cell.appendChild(image);
	image = document.createElement("div");
	image.href = "#";
	image.title = "Ergebnis löschen";
	if (window.parent.frames.control.daten[o.data.feld]["notDeleteable"] != "true") {
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
				window.parent.frames.cont.currentFile = window.parent.frames.control.daten[name]["file"];
				window.parent.frames.cont.textEditor.getSession().setValue("");
				window.parent.frames.props.propsEditor.getSession().setValue("");
				fillMessageBox("", false);
				window.parent.frames.rules.rulesEditor.getSession().foldAll(1);
				if (window.parent.frames.cont.currentFile.length > 0) {
					var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
					file.initWithPath(window.parent.frames.cont.currentFile);
					if (file.exists() == true)
						file.remove(false);
				}
				window.parent.frames.cont.dtable.removeRow(rowNumber);
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
			if (typeof window.parent.frames.control.daten[name]["container"] != "undefined" && window.parent.frames.control.daten[name]["container"] != null) {
				openPDF(window.parent.frames.control.daten[name]["container"], true);
			} else {
				openPDF(window.parent.frames.control.daten[name]["file"]);
			}
		})(o.data.feld);
	};
	o.cell.appendChild(image);
		image = document.createElement("div");
	if (window.parent.frames.control.daten[o.data.feld]["alfContainer"] == "true") {
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
			var docId = "workspace:/SpacesStore/" + window.parent.frames.control.daten[name]["container"];
			if (isLocal()) {
				var json = jQuery.parseJSON(window.parent.frames.control.document.reader.moveDocument(docId, window.parent.inboxID, getServer(), getUser(), getPassword(), getUrlParam("proxy"), getUrlParam("port"), null));
				if (!json.success)
					alert("Dokument nicht verschoben: " + json.result);
			}
			else {
				var dataString = {
					"function"			: "moveDocument",
					"documentId"		: docId,
					"destinationId"	: window.parent.inboxID,
					"server"				: getServer(),
					"username"			: getUser(),
					"password"			: getPassword(),
					"proxyHost"			: getUrlParam("proxy"),
					"proxyPort"			: getUrlParam("port")
				};
				$.ajax({
					type						: "POST",
					data						: dataString,
					datatype				: "json",
					url							: "VerteilungServlet",
					async						: false,
  			  error           : function (response) {
                              var r = jQuery.parseJSON(response.responseText);
                              alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
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
		window.parent.frames.cont.textEditor.getSession().setValue("");
		fillMessageBox("", false);
		for ( var i = 0; i < tableData.length; i++) {
			var name = window.parent.frames.cont.dtable.get("data").getByClientId(window.parent.frames.cont.dtable.getRow(i).getData()["yui3-record"]).get("feld");
			window.parent.REC.currentDocument.setContent(window.parent.frames.control.daten[name].text);
			window.parent.REC.testRules(window.parent.frames['rules'].rulesEditor.getSession().getValue());
			window.parent.frames.control.daten[name].log = window.parent.REC.mess;
			window.parent.frames.control.daten[name].result = window.parent.REC.results;
			window.parent.frames.control.daten[name].position = window.parent.REC.positions;
			window.parent.frames.control.daten[name].xml = window.parent.REC.currXMLName;
			window.parent.frames.control.daten[name].error = window.parent.REC.errors;
			var row = tableData[i];
			row["xml"] = window.parent.REC.currXMLName.join(" : ");
			row["error"] = window.parent.REC.errors;
			var ergebnis = new Array();
			ergebnis["error"] = window.parent.REC.errors.length > 0;
			row["result"] = ergebnis;
			tableData[i] = row;
			window.parent.frames.cont.dtable.modifyRow(i, row);
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
	if (window.parent.REC.exist(positions)) {
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
	window.parent.frames['rules'].rulesEditor.getSession().foldAll(1);
	var text = window.parent.frames['rules'].rulesEditor.getSession().getValue();
	var pos = 0;
	for ( var i = 0; i < position.length; i++)
		pos = text.indexOf("<archivTyp name=\"" + position[i] + "\"", pos);
	if (pos != -1) {
		pos1 = text.indexOf("</archivTyp>", pos);
		if (pos1 != -1) {
			var p = window.parent.REC.convertPosition(text, pos, pos1 + 12, "");
			window.parent.frames['rules'].rulesEditor.getSession().unfold(p.startRow + 1, true);
			window.parent.frames['rules'].rulesEditor.gotoLine(p.startRow + 1);
			window.parent.frames['rules'].rulesEditor.selection.setSelectionRange(new Range(p.startRow, p.startColumn, p.endRow, p.endColumn));
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
		if (window.parent.REC.exist(results[key])) {
			ret = ret + key + blanks.substr(0, maxLength - key.length) + ": " + results[key].getValue();
			if (window.parent.REC.exist(results[key].expected)) {
				var tmp = eval(results[key].expected);
				if (window.parent.REC.exist(results[key].getValue()) && tmp.valueOf() == results[key].getValue().valueOf())
					ret = ret + " [OK]";
				else
					ret = ret + " [FALSE] " + tmp.valueOf();
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
	window.parent.frames.output.outputEditor.getSession().setValue(text);
}

function doBack() {
	window.parent.frames.cont.multiMode = true;
	window.parent.frames.cont.showMulti = false;
	window.parent.frames.cont.document.getElementById('inTxt').style.display = 'none';
	window.parent.frames.cont.document.getElementById('dtable').style.display = 'block';
	window.parent.frames.control.document.getElementById('back').style.display = 'none';
	window.parent.frames.cont.textEditor.getSession().setValue("");
	fillMessageBox("", false);
	window.parent.frames.props.propsEditor.getSession().setValue("");
	window.parent.frames.rules.rulesEditor.getSession().foldAll(1);
	manageControls();
}

function doTest() {
	try {
		if (isLocal()) {
			var open = openFile("test.txt");
			var content = open[0];
			window.parent.REC.currentDocument.setContent(content);
			removeMarkers(markers, window.parent.frames['cont'].textEditor);
			window.parent.frames.cont.textEditor.getSession().setValue(content);
			if (!window.parent.currentRules.endsWith("test.xml")) {
				open = openFile("test.xml");
				window.parent.currentRules = open[1];
				window.parent.frames.rules.rulesEditor.getSession().setValue(open[0]);
			}
			window.parent.REC.testRules(window.parent.frames.rules.rulesEditor.getSession().getValue());
			setXMLPosition(window.parent.REC.currXMLName);
			markers = setMarkers(window.parent.REC.positions, window.parent.frames.cont.textEditor);
			window.parent.frames.props.propsEditor.getSession().setValue(printResults(window.parent.REC.results));
			fillMessageBox(window.parent.REC.getMessage(), true);
			window.parent.frames.cont.document.getElementById('inTxt').style.display = 'block';
			window.parent.frames.cont.document.getElementById('dtable').style.display = 'none';
		} else {
			var dataString = {
				"function" : "doTest",
				"fileName" : "test.txt",
				"filePath" : "test.xml"
			}
  		$.ajax({
	  		type       : "POST",
		  	data       : dataString,
			  datatype   : "json",
		  	url        : "VerteilungServlet",
		    error      : function (response) {
                       var r = jQuery.parseJSON(response.responseText);
                       alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                     },
			  success    : function(data) {
			  	             if (data.success[0]){
				                 window.parent.REC.currentDocument.setContent(data.result[0].text.toString());
				                 removeMarkers(markers, window.parent.frames['cont'].textEditor);
			                   window.parent.frames.cont.textEditor.getSession().setValue(data.result[0].text.toString());
				                 window.parent.currentRules = "test.xml";
				                 window.parent.frames.rules.rulesEditor.getSession().setValue(data.result[0].xml.toString());
				                 window.parent.REC.testRules(window.parent.frames.rules.rulesEditor.getSession().getValue());
			                   setXMLPosition(window.parent.REC.currXMLName);
				                 markers = setMarkers(window.parent.REC.positions, window.parent.frames.cont.textEditor);
				                 window.parent.frames.props.propsEditor.getSession().setValue(printResults(window.parent.REC.results));
				                 fillMessageBox(window.parent.REC.getMessage(), true);
				                 window.parent.frames.cont.document.getElementById('inTxt').style.display = 'block';
				                 window.parent.frames.cont.document.getElementById('dtable').style.display = 'none';
			  	             } else
			  	            	 alert("Fehler: " + data.result[0]);
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

function work() {
	var selectMode = false;
	if (window.parent.frames.cont.multiMode)
		doReRunAll()
	else {
		var range = window.parent.frames.rules.rulesEditor.getSelectionRange();
		var sel = window.parent.frames.rules.rulesEditor.getSession().getTextRange(range);
		if (sel.length > 0) {
			if (!sel.startsWith("<")) {
				var start = window.parent.frames.rules.rulesEditor.find('<', {
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
				var end = window.parent.frames.rules.rulesEditor.find('>', {
					backwards : false,
					wrap : false,
					caseSensitive : false,
					wholeWord : false,
					start : new Range(range.end.row, range.end.column, range.end.row, range.end.column),
					regExp : false
				});
				range.setEnd(end.end);
			}
			sel = window.parent.frames.rules.rulesEditor.getSession().getTextRange(range);
			if (!sel.endsWith("/>")) {
				var tmp = sel.substring(1, sel.indexOf(" "));
				tmp = "</" + tmp + ">";
				end = window.parent.frames.rules.rulesEditor.find(tmp, {
					backwards : false,
					wrap : false,
					caseSensitive : false,
					wholeWord : false,
					start : new Range(range.end.row, range.end.column, range.end.row, range.end.column),
					regExp : false
				});
				range.setEnd(end.end);
			}
			window.parent.frames['rules'].rulesEditor.selection.setSelectionRange(range);
			sel = window.parent.frames.rules.rulesEditor.getSession().getTextRange(range);
			if (!sel.startsWith("<tags") && !sel.startsWith("<category") && !sel.startsWith("<archivPosition")) {
				selectMode = true;
				if (!sel.startsWith("<searchItem ")) {
					start = window.parent.frames.rules.rulesEditor.find('<searchItem', {
						backwards : true,
						wrap : false,
						caseSensitive : false,
						wholeWord : false,
						start : range,
						regExp : false
					});
					range.setStart(start.start);
					end = window.parent.frames.rules.rulesEditor.find('</searchItem>', {
						backwards : false,
						wrap : false,
						caseSensitive : false,
						wholeWord : false,
						start : new Range(range.end.row, range.end.column, range.end.row, range.end.column),
						regExp : false
					});
					range.setEnd(end.end);
					window.parent.frames['rules'].rulesEditor.selection.setSelectionRange(range);
					sel = window.parent.frames.rules.rulesEditor.getSession().getTextRange(range);
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
				sel = window.parent.frames.rules.rulesEditor.getSession().getValue();
		} else
			sel = window.parent.frames.rules.rulesEditor.getSession().getValue();
		window.parent.REC.currentDocument.setContent(window.parent.frames['cont'].textEditor.getSession().getValue());
		removeMarkers(markers, window.parent.frames.cont.textEditor);
		window.parent.REC.testRules(sel);
		if (!selectMode)
			setXMLPosition(window.parent.REC.currXMLName);
		markers = setMarkers(window.parent.REC.positions, window.parent.frames.cont.textEditor);
		fillMessageBox(window.parent.REC.getMessage(), true);
		window.parent.frames.props.propsEditor.getSession().setValue(printResults(window.parent.REC.results));
		window.parent.frames.cont.document.getElementById('inTxt').style.display = 'block';
		window.parent.frames.cont.document.getElementById('dtable').style.display = 'none';
	}
}

function upload(dialog) {
	try {
		var erg = false;
		if (window.parent.currentRules.endsWith("doc.xml")) {
			vkbeautify.xml(window.parent.frames.rules.rulesEditor.getSession().getValue());
			if (isLocal()) {
				var ret = window.parent.frames.control.document.reader.updateDocumentByFile(window.parent.rulesID, window.parent.currentRules,
						"XML-Beschreibung der Dokumente", "application/xml", getServer(), getUser(), getPassword(), getUrlParam("proxy"), getUrlParam("port"), null);
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
				"documentText" : window.parent.rules.rulesEditor.getSession().getValue(),
				"description"  : "XML-Beschreibung der Dokumente",
				"documentId"   : window.parent.rulesID,
			  "mimeType"     : "application/xml",
				"server"       : getServer(),
				"username"     : getUser(),
				"password"     : getPassword(),
				"proxyHost"    : getUrlParam("proxy"),
				"proxyPort"    : getUrlParam("port")
			};
			$.ajax({
				type           : "POST",
				data           : dataString,
				datatype       : "json",
				url            : "VerteilungServlet",
			  error          : function (response) {
                           var r = jQuery.parseJSON(response.responseText);
                           alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
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

function loadAlfrescoFolder(folderName) {
	try {
    window.parent.frames.cont.dtable.get("data").reset(null, {
      silent : true
    });
    var ret;
    daten = new Array();
    tableData = new Array();
		if (isLocal()) {
			ret = window.parent.frames.control.document.reader.listFolder(folderName, "false", getServer(), getUser(), getPassword(), getUrlParam("proxy"), getUrlParam("port"));
			var json = jQuery.parseJSON(ret);
			var ergebnis = json.result;
			for ( var i = 0; i < ergebnis.length; i++) {
				var erg = ergebnis[i];
				ret = window.parent.frames.control.document.reader.getContent(erg.id, true, getServer(), getUser(), getPassword(),
						getUrlParam("proxy"), getUrlParam("port"), null);
				json = jQuery.parseJSON(ret);
				if (json.success)
				  loadMultiText(json.result, erg.name, erg.typ, "true", "true", getServer() + "/alfresco/s/cmis/s/workspace:SpacesStore/i/" + erg.id.substring(erg.id.lastIndexOf('/') + 1)
						  + "/content.pdf");
				else
					alert("Fehler beim Holen des Inhalts: " + json.result);
			}
			window.parent.frames.cont.dtable.render("#dtable");
		} else {
			var dataString = {
				"function"  : "listFolder",
				"filePath"  : folderName,
				"server"    : getServer(),
				"username"  : getUser(),
				"password"  : getPassword(),
				"proxyHost" : getUrlParam("proxy"),
				"proxyPort" : getUrlParam("port")
			};
			$.ajax({
				type     : "POST",
				data     : dataString,
				datatype : "json",
				url      : "VerteilungServlet",
			    error    : function (response) {
                             var r = jQuery.parseJSON(response.responseText);
                             alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                           },
				success  : function(data) {
                             if(data.success[0]) {
					            var ergebnis = data.result[0];
					            for ( var i = 0; i < ergebnis.length; i++) {
					              var erg = ergebnis[i];
						            var dataString = {
						          		"function"   : "getContent",
						          		"documentId" : erg.id,
						          		"extract"    : "true",
						          		"server"     : getServer(),
						          		"username"   : getUser(),
						          		"password"   : getPassword(),
						          		"proxyHost"  : getUrlParam("proxy"),
						          		"proxyPort"  : getUrlParam("port")
						            };
						            $.ajax({
						          	  type         : "POST",
						          	  data         : dataString,
						          	  datatype     : "json",
						          	  url          : "VerteilungServlet",
                                      async        : false,
						      		  error        : function (response) {
						                               var r = jQuery.parseJSON(response.responseText);
						                               alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
						                             },
						          	  success      : function(data) {
                                                       if (data.sucess[0])
                                                           loadMultiText(data.result.toString(), erg.name, erg.typ, "true", "true", getServer() + "/alfresco/s/cmis/s/workspace:SpacesStore/i/"
									            	             + erg.id.substring(erg.id.lastIndexOf('/') + 1) + "/content.pdf");
                                                       else
                                                           alert("Fehler beim Holen des Inhalts " + data.result[0]);
							                           }
						              });	          
					             }	
					             window.parent.frames.cont.dtable.render("#dtable");
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

function loadXML(rDoc, loadLocal, dialog) {
	try {
		if (isLocal()) {
			var ret;
			if (loadLocal) {
				var open = openFile(rDoc);
				window.parent.frames.rules.rulesEditor.getSession().setValue(open[0]);
				window.parent.frames.rules.rulesEditor.getSession().foldAll(1);
			} else {
				ret = window.parent.frames.control.document.reader.getContent(rDoc, false, getServer(), getUser(), getPassword(), getUrlParam("proxy"), getUrlParam("port"), null);
				var json = jQuery.parseJSON(ret);
				if (json.success) {
					window.parent.frames.rules.rulesEditor.getSession().setValue(json.result);
					window.parent.frames.rules.rulesEditor.getSession().foldAll(1);
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
				"server"     : getServer(),
				"username"   : getUser(),
				"password"   : getPassword(),
				"proxyHost"  : getUrlParam("proxy"),
				"proxyPort"  : getUrlParam("port")
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
				                  	window.parent.frames.rules.rulesEditor.getSession().setValue(data.result[0].toString());
					                window.parent.frames.rules.rulesEditor.getSession().foldAll(1);
										  	 } else
												   alert("Regeln konnten nicht übertragen werden: " + data.result[0]);
				               },
			  error        : function (response) {
                         var r = jQuery.parseJSON(response.responseText);
                         alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                       }
			});
		}
		window.parent.currentRules = "doc.xml";
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
		var xml = window.parent.frames.rules.rulesEditor.getSession().getValue();
		xml = vkbeautify.xml(xml);
		window.parent.frames.rules.rulesEditor.getSession().setValue(xml);
		// window.parent.frames.rules.rulesEditor.getSession().foldAll(1);
		if (typeof currXMLName != "undefined" && currXMLName != null) {
			setXMLPosition(currXMLName);
			markers = setMarkers(positions, window.parent.frames['cont'].textEditor);
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
		var txt = window.parent.frames.cont.textEditor.getSession().getValue();
		txt = js_beautify(txt);
		window.parent.frames.cont.textEditor.getSession().setValue(txt);
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
		window.parent.frames.control.document.reader.save(file, text)
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
				window.parent.frames['rules'].rulesEditor.getSession().setValue(contents);
				window.parent.frames['rules'].rulesEditor.getSession().foldAll(1);
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
		contents = window.parent.frames.control.document.reader.openFile(datafile);
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
		var fs = window.parent.document.getElementById('frameset2');
		if (fs) {
			fs.cols = "90%,10%";
		}
		oldContent = window.parent.frames.cont.textEditor.getSession().getValue();
		var content;
		if (window.parent.REC.exist(modifiedScript) && modifiedScript - length > 0) {
			content = modifiedScript;
		} else {
			if (isLocal()) {
				var open = openFile("recognition.js");
				content = open[0];
				window.parent.workDocument = open[1];
				eval(content);
				window.parent.REC = new Recognition();
				window.parent.REC.set(window.parent.REC);
				removeMarkers(markers, window.parent.frames['cont'].textEditor);
				window.parent.frames.cont.textEditor.getSession().setMode(new window.parent.frames.cont.jsMode());
				window.parent.frames.cont.textEditor.getSession().setValue(content);
				window.parent.frames.cont.textEditor.setShowInvisibles(false);
				window.parent.scriptMode = true;
				manageControls();
			} else {
				var dataString = {
					"function"   : "getContent",
					"documentId" : window.parent.scriptID,
					"extract"    : "false",
					"server"     : getServer(),
					"username"   : getUser(),
					"password"   : getPassword(),
					"proxyHost"  : getUrlParam("proxy"),
					"proxyPort"  : getUrlParam("port")
				};
				$.ajax({
					type         : "POST",
					data         : dataString,
					datatype     : "json",
					url          : "VerteilungServlet",
  			  error        : function (response) {
                           var r = jQuery.parseJSON(response.responseText);
                           alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                         },
					success      : function(data) {
						               if (data.success[0]){
                						 content = data.result[0].toString();
		                				 window.parent.workDocument = "recognition.js";
		                				 eval(content);
		                 				 window.parent.REC = new Recognition();
		                 				 window.parent.REC.set(window.parent.REC);
		                				 removeMarkers(markers, window.parent.frames['cont'].textEditor);
			                			 window.parent.frames.cont.textEditor.getSession().setMode(new window.parent.frames.cont.jsMode());
		                				 window.parent.frames.cont.textEditor.getSession().setValue(content);
			                 			 window.parent.frames.cont.textEditor.setShowInvisibles(false);
						                 window.parent.scriptMode = true;
					                	 manageControls();
						               } else
						              	 alert("Script konnte nicht gefunden werden: " + data.result[0]);
					               }
				});
			}
		}
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
		var fs = window.parent.document.getElementById('frameset2');
		if (fs) {
			fs.cols = "40%,60%";
		}
	}
}

function reloadScript(dialog) {
	try {
		modifiedScript = window.parent.frames.cont.textEditor.getSession().getValue();
		eval(modifiedScript);
		window.parent.REC = new Recognition();
		window.parent.REC.set(window.parent.REC);
		alert("Script erfolgreich aktualisiert");
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function get(dialog) {
	try {
		if (isLocal()) {
			var ret = window.parent.frames.control.document.reader.getContent( window.parent.scriptID, false, getServer(), getUser(), getPassword(), getUrlParam("proxy"),
					getUrlParam("port"), null);
			var json = jQuery.parseJSON(ret);
			if (json.success) {
				save(window.parent.workDocument, window.parent.frames.cont.textEditor.getSession().getValue(), false);
				window.parent.frames.cont.textEditor.getSession().setValue(json.result);
				if (dialog)
					alert("Script erfolgreich heruntergeladen!");
			} else
				alert("Fehler bei der Übertragung: " + json.result);
		} else {
			var dataString = {
				"function"   : "getContent",
				"documentId" : window.parent.scriptID,
				"extract"    : "false",
				"server"     : getServer(),
				"username"   : getUser(),
				"password"   : getPassword(),
				"proxyHost"  : getUrlParam("proxy"),
				"proxyPort"  : getUrlParam("port")
			};
			$.ajax({
				type         : "POST",
				data         : dataString,
				datatype     : "json",
				url          : "VerteilungServlet",
			  error        : function (response) {
                         var r = jQuery.parseJSON(response.responseText);
                         alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                       },
				success      : function(data) {
					               if (data.success[0]) {
					                 if (dialog)
						                 alert("Script erfolgreich heruntergeladen!");
					                 window.parent.frames.cont.textEditor.getSession().setValue(data.result.toString());
				                 } else
				                	 alert("Script konnte nicht geladen werden: " + data.result[0]);
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

function send(dialog) {
	try {
		var erg = false;
		if (window.parent.workDocument.endsWith("recognition.js")) {
			if (isLocal()) {
				window.parent.frames.control.document.reader.save(window.parent.workDocument, window.parent.frames.cont.textEditor.getSession().getValue());
				var ret = window.parent.frames.control.document.reader.updateDocument(window.parent.scriptID, window.parent.frames.cont.textEditor.getSession().getValue(),
						"VerteilungsScript", getServer(), getUser(), getPassword(), getUrlParam("proxy"), getUrlParam("port"), null);
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
					"documentId"   : window.parent.scriptID,
					"documentText" : window.parent.frames.cont.textEditor.getSession().getValue(),
					"description"  : "VerteilungsScript",
					"server"       : getServer(),
					"username"     : getUser(),
					"password"     : getPassword(),
					"proxyHost"    : getUrlParam("proxy"),
					"proxyPort"    : getUrlParam("port")
				};
				$.ajax({
					type           : "POST",
					data           : dataString,
					datatype       : "json",
					url            : "VerteilungServlet",
  			  error          : function (response) {
                             var r = jQuery.parseJSON(response.responseText);
                             alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
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
		var fs = window.parent.document.getElementById('frameset2');
		if (fs) {
			fs.cols = "40%,60%";
		}
		window.parent.frames.cont.textEditor.getSession().setMode(new window.parent.frames.cont.txtMode());
		if (window.parent.REC.exist(oldContent) && oldContent.length > 0)
			window.parent.frames.cont.textEditor.getSession().setValue(oldContent);
		else
			window.parent.frames.cont.textEditor.getSession().setValue("");
		window.parent.frames.cont.textEditor.setShowInvisibles(true);
		window.parent.scriptMode = false;
		manageControls();
	} catch (e) {
		var str = "FEHLER:\n";
		str = str + e.toString() + "\n";
		for ( var prop in e)
			str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
		alert(str);
	}
}

function show() {
	var printWin = window.parent.frames['test'];
	YUI({
		win : printWin
	}).use("datatable", function(Y) {

		// A table from data with keys that work fine as column names
		var simple = new Y.DataTable({
			columns : [ {
				key : "id",
				label : "ID"
			}, {
				key : "name",
				label : "Name"
			}, {
				key : "price",
				label : "Preis"
			} ],
			data : [ {
				id : "ga_3475",
				name : "gadget",
				price : "$6.99"
			}, {
				id : "sp_9980",
				name : "sprocket",
				price : "$3.75"
			}, {
				id : "wi_0650",
				name : "widget",
				price : "$4.25"
			} ],
			summary : "Price sheet for inventory parts",
			caption : "Example table with simple columns"
		});

		simple.render("#simple");
	});
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
	if (isLocal()) {
		var json;
		var txt = [];
		var pattern = new RegExp("true", "ig");
		if (getUrlParam("local") == null || pattern.test(getUrlParam("local"))) {
			window.parent.runLocal = true;
		} else {
			json = jQuery.parseJSON(window.parent.frames.control.document.reader.getNodeId("recognition.js", "false", getServer(), getUser(), getPassword(), getUrlParam("proxy"),
					getUrlParam("port"), null));
			if (json.success)
				window.parent.scriptID = json.result;
			else
				txt.push("Script nicht gefunden! Fehler: " + json.result);
			json = jQuery.parseJSON(window.parent.frames.control.document.reader.getNodeId("doc.xml", "false", getServer(), getUser(), getPassword(), getUrlParam("proxy"), getUrlParam("port"),
					null));
			if (json.success)
				window.parent.rulesID = json.result;
			else
				txt.push("Regeln nicht gefunden! Fehler: " + json.result);
			json = jQuery.parseJSON(window.parent.frames.control.document.reader.getNodeId("Inbox", "true", getServer(), getUser(), getPassword(), getUrlParam("proxy"), getUrlParam("port"),
					null));
			if (json.success)
				window.parent.inboxID = json.result;
			else
				txt.push("Inbox nicht gefunden! Fehler: " + json.result);
			if (txt.length > 0)
				alert(txt.join("\n"));
		}
	} else {
		var dataString = {
				"function"     : "getNodeId",
				"fileName"     : "recognition.js",
				"searchFolder" : "false",
				"server"       : getServer(),
				"username"     : getUser(),
				"password"     : getPassword(),
				"proxyHost"    : getUrlParam("proxy"),
				"proxyPort"    : getUrlParam("port")
			};
			$.ajax({
				type           : "POST",
				data           : dataString,
				datatype       : "json",
				url            : "VerteilungServlet",
				async          : false,
			  error          : function (response) {
                           var r = jQuery.parseJSON(response.responseText);
                           alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                         },
				success        : function(data) {
				                   if (data.success[0])
				                      window.parent.scriptID = data.result[0];
				                    else{
				                      alert("Script nicht gefunden! " + data.result[0]);
				                    }
				                 }
			});
		var dataString = {
				"function"     : "getNodeId",
				"fileName"     : "doc.xml",
				"searchFolder" : "false",
				"server"       : getServer(),
				"username"     : getUser(),
				"password"     : getPassword(),
				"proxyHost"    : getUrlParam("proxy"),
				"proxyPort"    : getUrlParam("port")
				};
				$.ajax({
					type         : "POST",
					data         : dataString,
					datatype     : "json",
					url          : "VerteilungServlet",
					async        : false,
  			        error        : function (response) {
                                     var r = jQuery.parseJSON(response.responseText);
                                     alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                                  },
					success      : function(data) {
                                     if (data.success[0])
                                       window.parent.rulesID = data.result[0];
                                     else{
                                       alert("Regeln nicht gefunden! " + data.result[0]);
                                    }
					              }
				});
			var dataString = {
				"function"     : "getNodeId",
				"fileName"     : "Inbox",
				"searchFolder" : "true",
				"server"       : getServer(),
				"username"     : getUser(),
				"password"     : getPassword(),
				"proxyHost"    : getUrlParam("proxy"),
				"proxyPort"    : getUrlParam("port")
				};
				$.ajax({
					type         : "POST",
					data         : dataString,
					datatype     : "json",
					url          : "VerteilungServlet",
					async        : false,
  			        error        : function (response) {
                                     var r = jQuery.parseJSON(response.responseText);
                                     alert("Fehler: " + r.Message + "\nStackTrace: " + r.StackTrace + "\nExceptionType: " + r.ExceptionType);
                                   },
					success      : function(data) {
					                 if (data.success[0])
					                   window.parent.inboxID = data.result[0];
					                 else{
					                   alert("Inbox nicht gefunden! " + data.result[0]);
					                 }   					             	 
					               }
				});
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
var paramServer = null;
var paramProxy = null;
var paramPort = null;
var paramUser = null;
var paramPass = null;