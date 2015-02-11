function XMLDoc(source, errFn) {
    this.topNode = null;
    // set up the properties and methods for this object
    this.errFn = errFn; // user defined error functions
    this.hasErrors = false; // were errors found during the parse?
    this.source = source; // the string source of the document
};

XMLDoc.prototype.createXMLNode = function (strXML) {
    return new XMLDoc(strXML, this.errFn).docNode;
};

XMLDoc.prototype.error = function (str) {
    this.hasErrors = true;
    if (this.errFn) {
        this.errFn("ERROR: " + str);
    } else if (this.onerror) {
        this.onerror("ERROR: " + str);
    }
    return 0;
};

XMLDoc.prototype.getTagNameParams = function (tag, obj) {
    var elm = -1, e, s = tag.indexOf('[');
    var attr = [];
    if (s >= 0) {
        e = tag.indexOf(']');
        if (e >= 0)
            elm = tag.substr(s + 1, (e - s) - 1);
        else
            obj.error('expected ] near ' + tag);
        tag = tag.substr(0, s);
        if (isNaN(elm) && elm != '*') {
            attr = elm.substr(1, elm.length - 1); // remove @
            attr = attr.split('=');
            if (attr[1]) { // remove "
                s = attr[1].indexOf('"');
                attr[1] = attr[1].substr(s + 1, attr[1].length - 1);
                e = attr[1].indexOf('"');
                if (e >= 0)
                    attr[1] = attr[1].substr(0, e);
                else
                    obj.error('expected " near ' + tag);
            }
            elm = -1;
        } else if (elm == '*')
            elm = -1;
    }
    return [tag, elm, attr[0], attr[1]];
};

XMLDoc.prototype.getUnderlyingXMLText = function () {
    var strRet = "";
    // for now, hardcode the xml version 1 information. When we handle Processing
    // Instructions later, this
    // should be looked at again
    strRet = strRet + "<?xml version=\"1.0\"?>";
    if (this.docNode == null) {
        return;
    }
    strRet = REC.displayElement(this.docNode, strRet);
    return strRet;
};

XMLDoc.prototype.handleNode = function (current) {
    if ((current.nodeType == 'COMMENT') && (this.topNode != null)) {
        return this.topNode.addElement(current);
    } else if ((current.nodeType == 'TEXT') || (current.nodeType == 'CDATA')) {
        // if the current node is a text node:
        // if the stack is empty, and this text node isn't just whitespace, we have
        // a problem (we're not in a document element)
        if (this.topNode == null) {
            if (REC.trim(current.content, true, false) == "") {
                return true;
            } else {
                return this.error("expected document node, found: " + current);
            }
        } else {
            // otherwise, append this as child to the element at the top of the stack
            return this.topNode.addElement(current);
        }
    } else if ((current.nodeType == 'OPEN') || (current.nodeType == 'SINGLE')) {
        // if we find an element tag (open or empty)
        var success = false;
        // if the stack is empty, this node becomes the document node
        if (this.topNode == null) {
            this.docNode = current;
            current.parent = null;
            success = true;
        } else {
            // otherwise, append this as child to the element at the top of the stack
            success = this.topNode.addElement(current);
        }

        if (success && (current.nodeType != 'SINGLE')) {
            this.topNode = current;
        }
        // rename it as an element node
        current.nodeType = "ELEMENT";
        return success;
    }
    // if it's a close tag, check the nesting
    else if (current.nodeType == 'CLOSE') {
        // if the stack is empty, it's certainly an error
        if (this.topNode == null) {
            return this.error("close tag without open: " + current.toString());
        } else {
            // otherwise, check that this node matches the one on the top of the stack
            if (current.tagName != this.topNode.tagName) {
                return this.error("expected closing " + this.topNode.tagName + ", found closing " + current.tagName);
            } else {
                // if it does, pop the element off the top of the stack
                this.topNode = this.topNode.getParent();
            }
        }
    }
    return true;
};

XMLDoc.prototype.insertNodeAfter = function (referenceNode, newNode) {
    var parentXMLText = this.getUnderlyingXMLText();
    var selectedNodeXMLText = referenceNode.getUnderlyingXMLText();
    var originalNodePos = parentXMLText.indexOf(selectedNodeXMLText) + selectedNodeXMLText.length;
    var newXML = parentXMLText.substr(0, originalNodePos);
    newXML += newNode.getUnderlyingXMLText();
    newXML += parentXMLText.substr(originalNodePos);
    var newDoc = new XMLDoc(newXML, this.errFn);
    return newDoc;
};

XMLDoc.prototype.insertNodeInto = function (referenceNode, insertNode) {
    var parentXMLText = this.getUnderlyingXMLText();
    var selectedNodeXMLText = referenceNode.getUnderlyingXMLText();
    var endFirstTag = selectedNodeXMLText.indexOf(">") + 1;
    var originalNodePos = parentXMLText.indexOf(selectedNodeXMLText) + endFirstTag;
    var newXML = parentXMLText.substr(0, originalNodePos);
    newXML += insertNode.getUnderlyingXMLText();
    newXML += parentXMLText.substr(originalNodePos);
    var newDoc = new XMLDoc(newXML, this.errFn);
    return newDoc;
};

XMLDoc.prototype.loadXML = function (sourceXML) {
    this.topNode = null;
    this.hasErrors = false;
    this.source = sourceXML;
};

XMLDoc.prototype.parse = function () {
    var pos = 0;
    // set up the arrays used to store positions of < and > characters
    err = false;
    while (!err) {
        var closing_tag_prefix = '';
        var chpos = this.source.indexOf('<', pos);
        var open;
        var close;
        if (chpos == -1) {
            break;
        }
        open = chpos;
        // create a text node
        var str = this.source.substring(pos, open);
        if (str.length != 0) {
            err = !this.handleNode(new XMLNode('TEXT', this, str));
        }
        // handle Programming Instructions - they can't reliably be handled as tags
        if (chpos == this.source.indexOf("<?", pos)) {
            pos = this.parsePI(this.source, pos + 2);
            if (pos == 0) {
                err = true;
            }
            continue;
        }
        // nobble the document type definition
        if (chpos == this.source.indexOf("<!DOCTYPE", pos)) {
            pos = this.parseDTD(this.source, chpos + 9);
            if (pos == 0) {
                err = true;
            }
            continue;
        }
        // if we found an open comment, we need to ignore angle brackets
        // until we find a close comment
        if (chpos == this.source.indexOf('<!--', pos)) {
            open_length = 4;
            closing_tag_prefix = '--';
        }
        // similarly, if we find an open CDATA, we need to ignore all angle
        // brackets until a close CDATA sequence is found
        if (chpos == this.source.indexOf('<![CDATA[', pos)) {
            open_length = 9;
            closing_tag_prefix = ']]';
        }
        // look for the closing sequence
        chpos = this.source.indexOf(closing_tag_prefix + '>', chpos);
        if (chpos == -1) {
            return this.error("expected closing tag sequence: " + closing_tag_prefix + '>');
        }
        close = chpos + closing_tag_prefix.length;
        // create a tag node
        str = this.source.substring(open + 1, close);
        var n = this.parseTag(str);
        if (n) {
            err = !this.handleNode(n);
        }
        pos = close + 1;
        // and loop
    }
    return !err;
};

XMLDoc.prototype.parseAttribute = function (src, pos, node) {
    // chew up the whitespace, if any
    while ((pos < src.length) && (whitespace.indexOf(src.charAt(pos)) != -1)) {
        pos++;
    }
    // if there's nothing else, we have no (more) attributes - just break out
    if (pos >= src.length) {
        return pos;
    }
    var p1 = pos;
    while ((pos < src.length) && (src.charAt(pos) != '=')) {
        pos++;
    }
    var msg = "attributes must have values";
    // parameters without values aren't allowed.
    if (pos >= src.length) {
        return this.error(msg);
    }
    // extract the parameter name
    var paramname = REC.trim(src.substring(p1, pos++), false, true);
    // chew up whitespace
    while ((pos < src.length) && (whitespace.indexOf(src.charAt(pos)) != -1)) {
        pos++;
    }
    // throw an error if we've run out of string
    if (pos >= src.length) {
        return this.error(msg);
    }
    msg = "attribute values must be in quotes";
    // check for a quote mark to identify the beginning of the attribute value
    var quote = src.charAt(pos++);
    // throw an error if we didn't find one
    if (quotes.indexOf(quote) == -1) {
        return this.error(msg);
    }
    p1 = pos;
    while ((pos < src.length) && (src.charAt(pos) != quote)) {
        pos++;
    }
    // throw an error if we found no closing quote
    if (pos >= src.length) {
        return this.error(msg);
    }
    // store the parameter
    if (!node.addAttribute(paramname, REC.trim(src.substring(p1, pos++), false, true))) {
        return 0;
    }
    return pos;
};

XMLDoc.prototype.parseDTD = function (str, pos) {
    // we're just going to discard the DTD
    var firstClose = str.indexOf('>', pos);
    if (firstClose == -1) {
        return this.error("error in DTD: expected '>'");
    }
    var closing_tag_prefix = '';
    var firstOpenSquare = str.indexOf('[', pos);
    if ((firstOpenSquare != -1) && (firstOpenSquare < firstClose)) {
        closing_tag_prefix = ']';
    }
    while (true) {
        var closepos = str.indexOf(closing_tag_prefix + '>', pos);
        if (closepos == -1) {
            return this.error("expected closing tag sequence: " + closing_tag_prefix + '>');
        }
        pos = closepos + closing_tag_prefix.length + 1;
        if (str.substring(closepos - 1, closepos + 2) != ']]>') {
            break;
        }
    }
    return pos;
};

XMLDoc.prototype.parsePI = function (str, pos) {
    // we just swallow them up
    var closepos = str.indexOf('?>', pos);
    return closepos + 2;
};

XMLDoc.prototype.parseTag = function (src) {
    // if it's a comment, strip off the packaging, mark it a comment node
    // and return it
    if (src.indexOf('!--') == 0) {
        return new XMLNode('COMMENT', this, src.substring(3, src.length - 2));
    }
    // if it's CDATA, do similar
    if (src.indexOf('![CDATA[') == 0) {
        return new XMLNode('CDATA', this, src.substring(8, src.length - 2));
    }
    var n = new XMLNode();
    n.doc = this;
    if (src.charAt(0) == '/') {
        n.nodeType = 'CLOSE';
        src = src.substring(1);
    } else {
        // otherwise it's an open tag (possibly an empty element)
        n.nodeType = 'OPEN';
    }
    // if the last character is a /, check it's not a CLOSE tag
    if (src.charAt(src.length - 1) == '/') {
        if (n.nodeType == 'CLOSE') {
            return this.error("singleton close tag");
        } else {
            n.nodeType = 'SINGLE';
        }
        // strip off the last character
        src = src.substring(0, src.length - 1);
    }
    // set up the properties as appropriate
    if (n.nodeType != 'CLOSE') {
        n.attributes = new Array();
    }
    if (n.nodeType == 'OPEN') {
        n.children = new Array();
    }
    // trim the whitespace off the remaining content
    src = REC.trim(src, true, true);
    // chuck out an error if there's nothing left
    if (src.length == 0) {
        return this.error("empty tag");
    }
    // scan forward until a space...
    var endOfName = REC.firstWhiteChar(src, 0);
    // if there is no space, this is just a name (e.g. (<tag>, <tag/> or </tag>
    if (endOfName == -1) {
        n.tagName = src;
        return n;
    }
    // otherwise, we should expect attributes - but store the tag name first
    n.tagName = src.substring(0, endOfName);
    // start from after the tag name
    var pos = endOfName;
    // now we loop:
    while (pos < src.length) {
        pos = this.parseAttribute(src, pos, n);
        if (this.pos == 0) {
            return null;
        }
        // and loop
    }
    return n;
};

XMLDoc.prototype.removeNodeFromTree = function (node) {
    var parentXMLText = this.getUnderlyingXMLText();
    var selectedNodeXMLText = node.getUnderlyingXMLText();
    var originalNodePos = parentXMLText.indexOf(selectedNodeXMLText);
    var newXML = parentXMLText.substr(0, originalNodePos);
    newXML += parentXMLText.substr(originalNodePos + selectedNodeXMLText.length);
    var newDoc = new XMLDoc(newXML, this.errFn);
    return newDoc;
};

XMLDoc.prototype.replaceNodeContents = function (referenceNode, newContents) {
    var newNode = this.createXMLNode("<X>" + newContents + "</X>");
    referenceNode.children = newNode.children;
    return this;
};

XMLDoc.prototype.selectNode = function (tagpath) {
    tagpath = REC.trim(tagpath, true, true);
    var srcnode, node, tag, params, elm, rg;
    var tags, attrName, attrValue, ok;
    srcnode = node = ((this.source) ? this.docNode : this);
    if (!tagpath)
        return node;
    if (tagpath.indexOf('/') == 0)
        tagpath = tagpath.substr(1);
    tagpath = tagpath.replace(tag, '');
    tags = tagpath.split('/');
    tag = tags[0];
    if (tag) {
        if (tagpath.indexOf('/') == 0)
            tagpath = tagpath.substr(1);
        tagpath = tagpath.replace(tag, '');
        params = this.getTagNameParams(tag, this);
        tag = params[0];
        elm = params[1];
        attrName = params[2];
        attrValue = params[3];
        node = (tag == '*') ? node.getElements() : node.getElements(tag);
        if (node.length) {
            if (elm < 0) {
                srcnode = node;
                var i = 0;
                while (i < srcnode.length) {
                    if (attrName) {
                        if (srcnode[i].getAttribute(attrName) != attrValue)
                            ok = false;
                        else
                            ok = true;
                    } else
                        ok = true;
                    if (ok) {
                        node = srcnode[i].selectNode(tagpath);
                        if (node)
                            return node;
                    }
                    i++;
                }
            } else if (elm < node.length) {
                node = node[elm].selectNode(tagpath);
                if (node)
                    return node;
            }
        }
    }
};

XMLDoc.prototype.selectNodeText = function (tagpath) {
    var node = this.selectNode(tagpath);
    if (node != null) {
        return node.getText();
    } else {
        return null;
    }
};

function XMLNode(nodeType, doc, str) {
    // the content of text (also CDATA and COMMENT) nodes
    if (nodeType == 'TEXT' || nodeType == 'CDATA' || nodeType == 'COMMENT') {
        this.content = str;
    } else {
        this.content = null;
    }

    this.attributes = null; // an array of attributes (used as a hash table)
    this.children = null; // an array (list) of the children of this node
    this.doc = doc; // a reference to the document
    this.nodeType = nodeType; // the type of the node
    this.parent = "";
    this.tagName = ""; // the name of the tag (if a tag node)

    // configure the methods
    this.selectNode = XMLDoc.selectNode;
    this.selectNodeText = XMLDoc.selectNodeText;
};

XMLNode.prototype.addAttribute = function (attributeName, attributeValue) {
    // if the name is found, the old value is overwritten by the new value
    this.attributes['_' + attributeName] = attributeValue;
    return true;
};

XMLNode.prototype.addElement = function (node) {
    node.parent = this;
    this.children[this.children.length] = node;
    return true;
};

XMLNode.prototype.getAttribute = function (name) {
    if (this.attributes == null) {
        return null;
    }
    return this.attributes['_' + name];
};

XMLNode.prototype.getAttributeNames = function () {
    if (this.attributes == null) {
        var ret = new Array();
        return ret;
    }
    var attlist = new Array();
    for (var a in this.attributes) {
        attlist[attlist.length] = a.substring(1);
    }
    return attlist;
};

XMLNode.prototype.getElementById = function (id) {
    var node = this;
    var ret;
    // alert("tag name=" + node.tagName);
    // alert("id=" + node.getAttribute("id"));
    if (node.getAttribute("id") == id) {
        return node;
    } else {
        var elements = node.getElements();
        // alert("length=" + rugrats.length);
        var intLoop = 0;
        // do NOT use a for loop here. For some reason
        // it kills some browsers!!!
        while (intLoop < elements.length) {
            // alert("intLoop=" + intLoop);
            var element = elements[intLoop];
            // alert("recursion");
            ret = element.getElementById(id);
            if (ret != null) {
                // alert("breaking");
                break;
            }
            intLoop++;
        }
    }
    return ret;
};

XMLNode.prototype.getElements = function (byName) {
    if (this.children == null) {
        var ret = new Array();
        return ret;
    }
    var elements = new Array();
    for (var i = 0; i < this.children.length; i++) {
        if ((this.children[i].nodeType == 'ELEMENT') && ((byName == null) || (this.children[i].tagName == byName))) {
            elements[elements.length] = this.children[i];
        }
    }
    return elements;
};

XMLNode.prototype.getText = function () {
    if (this.nodeType == 'ELEMENT') {
        if (this.children == null) {
            return null;
        }
        var str = "";
        for (var i = 0; i < this.children.length; i++) {
            var t = this.children[i].getText();
            str += (t == null ? "" : t);
        }
        return str;
    } else if (this.nodeType == 'TEXT') {
        return REC.convertEscapes(this.content);
    } else {
        return this.content;
    }
};

XMLNode.prototype.getParent = function () {
    return this.parent;
};

XMLNode.prototype.getUnderlyingXMLText = function () {
    var strRet = "";
    strRet = REC.displayElement(this, strRet);
    return strRet;
};

XMLNode.prototype.removeAttribute = function (attributeName) {
    if (attributeName == null) {
        return this.doc.error("You must pass an attribute name into the removeAttribute function");
    }
    // now remove the attribute from the list.
    // I want to keep the logic for adding attribtues in one place. I'm
    // going to get a temp array of attributes and values here and then
    // use the addAttribute function to re-add the attributes
    var attributes = this.getAttributeNames();
    var intCount = attributes.length;
    var tmpAttributeValues = new Array();
    for (var intLoop = 0; intLoop < intCount; intLoop++) {
        tmpAttributeValues[intLoop] = this.getAttribute(attributes[intLoop]);
    }
    // now blow away the old attribute list
    this.attributes = new Array();

    // now add the attributes back to the array - leaving out the one we're
    // removing
    for (var intLoop = 0; intLoop < intCount; intLoop++) {
        if (attributes[intLoop] != attributeName) {
            this.addAttribute(attributes[intLoop], tmpAttributeValues[intLoop]);
        }
    }
    return true;
};

XMLNode.prototype.toString = function () {
    return "" + this.nodeType + ":" + (this.nodeType == 'TEXT' || this.nodeType == 'CDATA' || this.nodeType == 'COMMENT' ? this.content : this.tagName);
};

function ArchivTyp(srch) {
    var i;
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.name = srch.name;
    this.searchString = srch.searchString;
    this.type = srch.type;
    if (REC.exist(srch.unique))
        this.unique = REC.trim(srch.unique);
    else
        this.unique = "error";
    if (REC.exist(srch.removeBlanks))
        this.removeBlanks = srch.removeBlanks;
    this.completeWord = REC.stringToBoolean(srch.completeWord, false);
    this.caseSensitive = REC.stringToBoolean(srch.caseSensitive, false);
    var tmp = new Array();
    REC.log(TRACE, "Search Archivposition");
    if (REC.exist(srch.archivPosition)) {
        REC.log(TRACE, "Archivposition exist");
        for (i = 0; i < srch.archivPosition.length; i++)
            tmp.push(new ArchivPosition(srch.archivPosition[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Archivposition found");
            this.archivPosition = tmp;
        } else
            REC.log(WARN, "No valid Archivposition found");
    }
    tmp = new Array();
    REC.log(TRACE, "Search Archivziel");
    if (REC.exist(srch.archivZiel)) {
        REC.log(TRACE, "Archivziel exist");
        for (i = 0; i < srch.archivZiel.length; i++)
            tmp.push(new ArchivZiel(srch.archivZiel[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Archivziel found");
            this.archivZiel = tmp;
        } else
            REC.log(WARN, "No valid Archivziel found");
    }
    REC.log(TRACE, "Search Tags");
    tmp = new Array();
    if (REC.exist(srch.tags)) {
        REC.log(TRACE, "Tags exist");
        for (i = 0; i < srch.tags.length; i++)
            tmp.push(new Tags(srch.tags[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Tags found");
            this.tags = tmp;
        } else
            REC.log(WARN, "No valid Tags found");
    }
    tmp = new Array();
    REC.log(TRACE, "Search Category");
    if (REC.exist(srch.category)) {
        REC.log(TRACE, "Category exist");
        for (i = 0; i < srch.category.length; i++)
            tmp.push(new Category(srch.category[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Category found");
            this.category = tmp;
        } else
            REC.log(WARN, "No valid Category found");
    }
    tmp = new Array();
    REC.log(TRACE, "Search SearchItems");
    if (REC.exist(srch.searchItem)) {
        REC.log(TRACE, "SearchItems exist");
        for (i = 0; i < srch.searchItem.length; i++)
            tmp.push(new SearchItem(srch.searchItem[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " SearchItem found");
            this.searchItem = tmp;
        } else
            REC.log(WARN, "No valid SearchItem found");
    }
    tmp = new Array();
    REC.log(TRACE, "Search Archivtyp");
    if (REC.exist(srch.archivTyp)) {
        REC.log(TRACE, "Archivtyp exist");
        for (i = 0; i < srch.archivTyp.length; i++)
            tmp.push(new ArchivTyp(srch.archivTyp[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Archivtyp found");
            this.archivTyp = tmp;
        } else
            REC.log(WARN, "No valid Archivtyp found");
    }
};

ArchivTyp.prototype.toString = function (ident) {
    var i;
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    var txt = REC.getIdent(ident) + "ArchivTyp:\n";
    txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
    txt = txt + REC.getIdent(ident) + "name: " + this.name + "\n";
    txt = txt + REC.getIdent(ident) + "searchString: " + this.searchString + "\n";
    txt = txt + REC.getIdent(ident) + "type: " + this.name + "\n";
    txt = txt + REC.getIdent(ident) + "unique: " + this.unique + "\n";
    txt = txt + REC.getIdent(ident) + "removeBlanks: " + this.removeBlanks + "\n";
    txt = txt + REC.getIdent(ident) + "caseSensitive: " + this.caseSensitive + "\n";
    txt = txt + REC.getIdent(ident) + "completeWord: " + this.completeWord + "\n";
    if (REC.exist(this.archivPosition)) {
        for (i = 0; i < this.archivPosition.length; i++) {
            txt = txt + this.archivPosition[i].toString(ident);
        }
    }
    if (REC.exist(this.archivZiel)) {
        for (i = 0; i < this.archivZiel.length; i++) {
            txt = txt + this.archivZiel[i].toString(ident);
        }
    }
    if (REC.exist(this.tags)) {
        for (i = 0; i < this.tags.length; i++) {
            txt = txt + this.tags[i].toString(ident);
        }
    }
    if (REC.exist(this.category)) {
        for (i = 0; i < this.category.length; i++) {
            txt = txt + this.category[i].toString(ident);
        }
    }
    if (REC.exist(this.searchItem)) {
        for (i = 0; i < this.searchItem.length; i++) {
            txt = txt + this.searchItem[i].toString(ident);
        }
    }
    if (REC.exist(this.archivTyp)) {
        for (i = 0; i < this.archivTyp.length; i++) {
            txt = txt + this.archivTyp[i].toString(ident);
        }
    }
    return txt;
};

ArchivTyp.prototype.resolve = function () {
    var i;
    var found = false;
    var orgLevel = REC.debugLevel;
    if (REC.exist(this.debugLevel))
        REC.debugLevel = this.debugLevel;
    REC.log(DEBUG, "resolve ArchivTyp " + this.name);
    REC.log(TRACE, "ArchivTyp.resolve: settings are: \n" + this);
    var str = REC.exist(this.removeBlanks) ? REC.content.replace(/ /g, '') : REC.content;
    var pst = (this.completeWord ? "\\b" + this.searchString + "?\\b" : this.searchString);
    var pat = new RegExp(pst, (this.caseSensitive ? "" : "i"));
    if (REC.exist(str) && pat.test(str)) {
        found = true;
        if (this.name != "Fehler")
            REC.currXMLName.push(this.name);
        REC.log(INFORMATIONAL, "Regel gefunden " + this.name);
        if (REC.exist(REC.currentSearchItems)) {
            var tmp = REC.currentSearchItems.concat(this.searchItem);
            REC.currentSearchItems = tmp;
        } else
            REC.currentSearchItems = this.searchItem;
        if (REC.exist(this.archivZiel)) {
            for (i = 0; i < this.archivZiel.length; i++) {
                REC.log(TRACE, "ArchivTyp.resolve: call ArchivZiel.resolve with " + REC.currentDocument.toString());
                this.archivZiel[i].resolve(REC.currentDocument.displayPath.split("/").slice(2).join("/") + "/" + REC.currentDocument.name);
            }
        }
        if (REC.exist(this.archivTyp)) {
            for (i = 0; i < this.archivTyp.length; i++) {
                REC.log(TRACE, "ArchivTyp.resolve: call ArchivTyp.resolve ");
                if (this.archivTyp[i].resolve()) {
                    this.unique = this.archivTyp[i].unique;
                    break;
                }
            }
        }
        if (REC.exist(this.searchItem)) {
            for (i = 0; i < this.searchItem.length; i++) {
                REC.log(TRACE, "ArchivTyp.resolve: call SearchItem.resolve ");
                this.searchItem[i].resolve();
            }
        }
        if (REC.exist(this.tags)) {
            for (i = 0; i < this.tags.length; i++) {
                REC.log(TRACE, "ArchivTyp.resolve: call Tags.resolve with currentDocument");
                this.tags[i].resolve(REC.currentDocument);
            }
        }
        if (REC.exist(this.category)) {
            for (i = 0; i < this.category.length; i++) {
                REC.log(TRACE, "ArchivTyp.resolve: call Category.resolve with currentDocument");
                this.category[i].resolve(REC.currentDocument);
            }
        }
        var p = this.parent;
        while (REC.exist(p)) {
            p.unique = this.unique;
            p = p.parent;
        }
        if (REC.exist(this.archivPosition)) {
            var orgFolder = null;
            for (i = 0; i < this.archivPosition.length; i++) {
                REC.log(TRACE, "ArchivTyp.resolve: call ArchivPosition.resolve");
                tmp = this.archivPosition[i].resolve();
                if (tmp != null) {
                    REC.log(TRACE, "ArchivTyp.resolve: process archivPosition" + tmp);
                    if (REC.exist(REC.mandatoryElements) && this.name != REC.errorBox && this.name != REC.duplicateBox) {
                        for (var j = 0; j < REC.mandatoryElements.length; j++) {
                            if (!REC.exist(REC.currentDocument.properties[REC.mandatoryElements[j]])) {
                                REC.errors.push(REC.mandatoryElements[j] + " is missing!");
                            }
                        }
                        if (REC.errors.length > 0)
                            return found;
                    }
                    if (this.name != REC.errorBox && this.name != REC.duplicateBox) {
                        var COM = new Comments();
                        COM.removeComments(REC.currentDocument);
                    } else
                        this.unique = "copy";
                    if (this.archivPosition[i].link && REC.exist(orgFolder)) {
                        REC.log(INFORMATIONAL, "Document link to folder " + tmp);
                        REC.log(INFORMATIONAL, tmp + "/" + orgFolder.name);
                        REC.log(INFORMATIONAL, (REC.exist(companyhome.childByNamePath(tmp + "/" + orgFolder.name))));
                        if (REC.exist(companyhome.childByNamePath(tmp + "/" + orgFolder.name)))
                            REC.log(WARN, "Link already exists!");
                        else
                            companyhome.childByNamePath(tmp).addNode(orgFolder);
                    } else {
                        REC.log(INFORMATIONAL, "Document place to folder " + tmp);
                        REC.log(TRACE, "ArchivTyp.resolve: search Document: " + REC.currentDocument.name + " in " + tmp);
                        orgFolder = companyhome.childByNamePath(tmp);
                        var tmpDoc = orgFolder.childByNamePath(REC.currentDocument.name);
                        if (tmpDoc != null) {
                            if (REC.exist(this.unique) && this.unique == "newVersion") {
                                REC.log(WARN, "Document exists, a new Version will created");
                                REC.makeNewVersion(tmpDoc, REC.currentDocument);
                            } else if (this.unique == "ignore") {
                                REC.log(WARN, "Dokument existiert bereits, hochgeladenes Dokument wird gel\\u00F6scht!");
                                //REC.currentDocument.remove();
                                return found;
                            } else {
                                REC.errors.push("Dokument mit dem Dateinamen " + REC.currentDocument.name + " ist im Zielordner bereits vorhanden! ");
                                REC.fehlerBox = REC.duplicateBox;
                                return found;
                            }
                        } else {
                            var uni = false;
                            if (REC.exist(this.unique) && REC.exist(REC.results["title"])) {
                                REC.log(TRACE, "ArchivTyp.resolve: check for unique");
                                q = "+PATH:\"/" + orgFolder.qnamePath + "//*\" +@cm\\:title:\"" + REC.results["title"].val + "\"";
                                REC.log(TRACE, "ArchivTyp.resolve: search document with " + q);
                                var x = search.luceneSearch(q);
                                if (x.length > 0) {
                                    REC.log(TRACE, "ArchivTyp.resolve: search document found " + x.length + " documents");
                                    for (var k = 0; k < x.length; k++) {
                                        REC.log(TRACE, "ArchivTyp.resolve: compare with document " + x[k].name + "[" + x[k].properties['cm:title'] + "]...");
                                        if (x[k].properties["cm:title"] == REC.results["title"].val) {
                                            uni = true;
                                            if (this.unique == "error") {
                                                REC.errors.push("Dokument mit Titel [" + REC.results["title"].val + "] ist im Zielordner bereits vorhanden");
                                                REC.fehlerBox = REC.duplicateBox;
                                                return found;
                                            } else if (this.unique == "newVersion") {
                                                log(WARN, "Dokument mit diesem Titel bereits vorhanden! Erstelle neue Version...");
                                                if (!REC.makeNewVersion(x[k], REC.currentDocument))
                                                    return found;
                                            } else if (this.unique == "overWrite") {
                                                x[k].remove();
                                                if (!REC.currentDocument.move(orgFolder))
                                                    REC.errors.push("Dokument konnte nicht in den Zielordner verschoben werden " + tmp);
                                            } else if (this.unique != "ignore") {
                                                REC.log(WARN, "Dokument mit gleichem Titel existiert bereits, hochgeladenes Dokument wird gel\\u00F6scht!");
                                                //REC.currentDocument.remove();
                                                return found;
                                            }
                                            break;
                                        }
                                    }
                                } else
                                    REC.log(TRACE, "ArchivTyp.resolve: check for unique: no document with same title found");
                            }
                            if (!uni) {
                                REC.log(TRACE, "ArchivTyp.resolve: move document to folder");
                                if (!REC.currentDocument.move(orgFolder))
                                    REC.errors.push("Dokument konnte nicht in den Zielordner verschoben werden " + tmp);
                            }
                        }
                    }
                } else
                    REC.errors.push("kein Zielordner vorhanden!");
            }
        }
    }
    REC.debugLevel = orgLevel;
    return found;
};

function ArchivPosition(srch) {
    if (REC.exist(srch.debugLevel))
        REC.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.link = REC.stringToBoolean(srch.link, false);
    if (REC.exist(srch.folder))
        this.folder = srch.folder;
    var tmp = new Array();
    REC.log(TRACE, "Search Archivziel");
    if (REC.exist(srch.archivZiel)) {
        REC.log(TRACE, "Archivziel exist");
        for (var i = 0; i < srch.archivZiel.length; i++)
            tmp.push(new ArchivZiel(srch.archivZiel[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Archivziel found");
            this.archivZiel = tmp;
        } else
            REC.log(WARN, "No valid Archivziel found");
    }
};

ArchivPosition.prototype.toString = function (ident) {
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    var txt = REC.getIdent(ident) + "ArchivPosition:\n";
    txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
    txt = txt + REC.getIdent(ident) + "link: " + this.link + "\n";
    txt = txt + REC.getIdent(ident) + "folder: " + this.folder + "\n";
    if (REC.exist(this.archivZiel)) {
        for (var i = 0; i < this.archivZiel.length; i++) {
            txt = txt + this.archivZiel[i].toString(ident);
        }
    }
    return txt;
};

ArchivPosition.prototype.resolve = function () {
    var orgLevel = REC.debugLevel;
    if (REC.exist(this.debugLevel))
        REC.debugLevel = this.debugLevel;
    REC.log(DEBUG, "resolve ArchivPosition");
    REC.log(TRACE, "ArchivPosition.resolve: settings are: \n" + this);
    var tmp = (REC.exist(REC.archivRoot) ? REC.archivRoot : "");
    REC.log(TRACE, "ArchivPosition.resolve: result is " + tmp);
    if (REC.exist(this.folder)) {
        tmp = tmp + REC.replaceVar(this.folder);
        var exp = new RegExp("[*\"<>\?:|]|\\.$");
        if (tmp.match(exp)) {
            var m = exp.exec(tmp);
            var erg = "Ung\ufffdtige Zeichen f\ufffdr Foldernamen!\n";
            erg = erg + tmp + "\n";
            erg = erg + "Position " + m.index + ":\n";
            for (var i = 0; i < m.length; i++) {
                erg = erg + m[i] + "\n";
            }
            REC.errors.push(erg);
            return;
        }
    }
    REC.log(TRACE, "ArchivPosition.resolve: result is " + tmp);
    tmp = REC.buildFolder(tmp);
    REC.log(TRACE, "ArchivPosition.resolve: result is " + tmp);
    if (REC.exist(this.archivZiel)) {
        for (i = 0; i < this.archivZiel.length; i++) {
            REC.log(TRACE, "ArchivPosition.resolve: call ArchivZiel.resolve with " + tmp);
            this.archivZiel[i].resolve(tmp);
        }
    }
    REC.log(DEBUG, "ArchivPosition.resolve: return is " + tmp);
    REC.debugLevel = orgLevel;
    return tmp;
};

function Format(srch) {
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.formatString = srch.formatString;
};

Format.prototype.toString = function (ident) {
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    var txt = REC.getIdent(ident) + "Format:\n";
    txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
    txt = txt + REC.getIdent(ident) + "formatString: " + this.formatString + "\n";
    return txt;
};

Format.prototype.resolve = function (value) {
    var orgLevel = REC.debugLevel;
    if (REC.exist(this.debugLevel))
        REC.debugLevel = this.debugLevel;
    REC.log(DEBUG, "resolve Format with " + value);
    REC.log(TRACE, "Format.resolve: settings are: \n" + this);
    var erg = null;
    if (REC.isDate(value))
        erg = REC.dateFormat(value, this.formatString);
    if (typeof value == "number")
        erg = REC.numberFormat(value, this.formatString);
    REC.log(DEBUG, "Format.resolve: return " + erg);
    REC.debugLevel = orgLevel;
    return erg;
};

function ArchivZiel(srch) {
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    if (REC.exist(srch.aspect))
        this.aspect = srch.aspect;
    if (REC.exist(srch.type))
        this.type = srch.type;
};

ArchivZiel.prototype.toString = function (ident) {
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    var txt = REC.getIdent(ident) + "ArchivZiel:\n";
    txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
    txt = txt + REC.getIdent(ident) + "aspect: " + this.aspect + "\n";
    txt = txt + REC.getIdent(ident) + "type: " + this.type + "\n";
    return txt;
};

ArchivZiel.prototype.resolve = function (doc) {
    var orgLevel = REC.debugLevel;
    if (REC.exist(this.debugLevel))
        REC.debugLevel = this.debugLevel;
    REC.log(DEBUG, "resolve ArchivZiel");
    REC.log(TRACE, "ArchivZiel.resolve: settings are: \n" + this);
    if (REC.exist(this.aspect)) {
        REC.log(TRACE, "ArchivZiel.resolve: Aspect is " + this.aspect);
        if (!companyhome.childByNamePath(doc).hasAspect(this.aspect))
            companyhome.childByNamePath(doc).addAspect(this.aspect);
        REC.log(INFORMATIONAL, "add aspect " + this.aspect);
    }
    if (REC.exist(this.type)) {
        REC.log(TRACE, "ArchivZiel.resolve: Type is " + this.type);
        if (!companyhome.childByNamePath(doc).isSubType(this.type))
            companyhome.childByNamePath(doc).specializeType(this.type);
        REC.log(INFORMATIONAL, "specialize type " + this.type);
    }
    REC.debugLevel = orgLevel;
};

function Check(srch, parent) {
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    if (REC.exist(parent))
        this.parent = parent;
    else
        this.parent.objectTyp = "string";
    if (this.parent.objectTyp == "date") {
        this.lowerValue = (REC.exist(srch.lowerValue) ? new Date(REC.trim(srch.lowerValue)) : null);
        this.upperValue = (REC.exist(srch.upperValue) ? new Date(REC.trim(srch.upperValue)) : null);
    } else if (this.parent.objectTyp == "int") {
        this.lowerValue = (REC.exist(srch.lowerValue) ? parseInt(REC.trim(srch.lowerValue), 10) : null);
        this.upperValue = (REC.exist(srch.upperValue) ? parseInt(REC.trim(srch.upperValue), 10) : null);
    } else if (this.parent.objectTyp == "float") {
        this.lowerValue = (REC.exist(srch.lowerValue) ? parseFloat(REC.trim(srch.lowerValue)) : null);
        this.upperValue = (REC.exist(srch.upperValue) ? parseFloat(REC.trim(srch.upperValue)) : null);
    } else {
        this.upperValue = (REC.exist(srch.lowerValue) ? srch.upperValue : null);
        this.upperValue = (REC.exist(srch.lowerValue) ? srch.upperValue : null);
    }
};

Check.prototype.resolve = function (erg) {
    var orgLevel = REC.debugLevel;
    if (REC.exist(this.debugLevel))
        REC.debugLevel = this.debugLevel;
    REC.log(DEBUG, "resolve Check with " + erg + " and " + this.parent.name);
    REC.log(TRACE, "Check.resolve: settings are:\n" + this);
    for (var i = 0; i < erg.length; i++) {
        if (erg[i].check) {
            if (REC.exist(this.upperValue) && erg[i].getValue() > this.upperValue) {
                erg[i].check = false;
                erg[i].error = this.parent.name + " maybe wrong [" + erg[i].getValue() + "] is bigger " + this.upperValue;
            }
            if (REC.exist(this.lowerValue) && erg[i].getValue() < this.lowerValue) {
                erg[i].check = false;
                erg[i].error = this.parent.name + " maybe wrong [" + erg[i].getValue() + "] is smaller " + this.lowerValue;
            }
        }
    }
    REC.log(DEBUG, "Check.resolve: return for " + this.parent.name + " is " + erg.getResult().text);
    REC.debugLevel = orgLevel;
    return erg;
};

Check.prototype.toString = function (ident) {
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    var txt = REC.getIdent(ident) + "Check:\n";
    txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
    txt = txt + REC.getIdent(ident) + "lowerValue: " + this.lowerValue + "\n";
    txt = txt + REC.getIdent(ident) + "upperValue: " + this.upperValue + "\n";
    return txt;
};

function Delimitter(srch) {
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.typ = srch.typ;
    this.text = srch.text;
    this.count = Number(srch.count);
    if (REC.exist(srch.removeBlanks))
        this.removeBlanks = srch.removeBlanks;
};

Delimitter.prototype.toString = function (ident) {
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    var txt = REC.getIdent(ident) + "Delimitter:\n";
    txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
    txt = txt + REC.getIdent(ident) + "typ: " + this.typ + "\n";
    txt = txt + REC.getIdent(ident) + "text: " + this.text + "\n";
    txt = txt + REC.getIdent(ident) + "count: " + this.count + "\n";
    txt = txt + REC.getIdent(ident) + "removeBlanks: " + this.removeBlanks + "\n";
    return txt;
};

Delimitter.prototype.resolve = function (erg, direction) {
    jstestdriver.console.log("X"+this.text+"X");
    var orgLevel = REC.debugLevel;
    if (REC.exist(this.debugLevel))
        REC.debugLevel = this.debugLevel;
    REC.log(TRACE, "resolve Delimitter with " + erg);
    REC.log(TRACE, "Delimitter.resolve: settings are:\n" + this);
    if (REC.exist(this.removeBlanks) && this.removeBlanks == "before") {
        str = removeBlanks(erg);
    }
    for (var i = 0; i < erg.length; i++) {
        if (typeof erg[i].text == "string") {
            REC.log(DEBUG, "resolve Delimitter: current String is " + REC.printTrace(erg[i].text, direction));
            var txtSave = erg[i].text;
            var tmp = "erg[i].text = erg[i].text";
            var tmpPos = "txtSave";
            if (this.typ == "start") {
                if (this.count < 0) {
                    tmp = tmp + ".split(this.text).reverse().slice(0, Math.abs(this.count)).reverse().join(this.text)";
                    tmpPos = tmpPos + ".split(this.text).reverse().slice(Math.abs(this.count)).reverse().join(this.text).length + this.text.length";
                } else {
                    tmp = tmp + ".split(this.text).slice(Math.abs(this.count)).join(this.text)";
                    tmpPos = tmpPos + ".split(this.text).slice(0, Math.abs(this.count)).join(this.text).length + this.text.length";
                }
                tmpPos = "erg[i].start = erg[i].start + " + tmpPos;
                eval(tmpPos);
            }
            if (this.typ == "end") {
                if (this.count < 0) {
                    tmp = tmp + ".split(this.text).reverse().slice(Math.abs(this.count)).reverse().join(this.text)";
                    tmpPos = tmpPos + ".split(this.text).reverse().slice(0, Math.abs(this.count)).reverse().join(this.text).length";
                } else {
                    tmp = tmp + ".split(this.text).slice(0, Math.abs(this.count)).join(this.text)";
                    tmpPos = tmpPos + ".split(this.text).slice(Math.abs(this.count)).join(this.text).length - this.text.length";
                }
                tmpPos = "erg[i].end = erg[i].end - " + tmpPos;
                eval(tmpPos);
            }
            REC.log(TRACE, "Delimitter.resolve: eval with " + tmp + " and " + erg[i]);
            eval(tmp);
            REC.log(DEBUG, "Delimitter.resolve: result is " + REC.printTrace(erg[i].text, direction));
        }
    }
    if (REC.exist(this.removeBlanks) && this.removeBlanks == "after") {
        erg = removeBlanks(erg);
    }
    REC.log(TRACE, "Delimitter.resolve: return is  " + erg);
    REC.debugLevel = orgLevel;
    return erg;
};

function Category(srch) {
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.name = srch.name;
};

Category.prototype.toString = function (ident) {
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    var txt = REC.getIdent(ident) + "Category:\n";
    txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
    txt = txt + REC.getIdent(ident) + "name: " + this.name + "\n";
    return txt;
};

Category.prototype.resolve = function (document) {
    var orgLevel = REC.debugLevel;
    if (REC.exist(this.debugLevel))
        REC.debugLevel = this.debugLevel;
    REC.log(DEBUG, "resolve Category");
    REC.log(TRACE, "Category.resolve: settings are: \n" + this);
    if (REC.exist(this.name)) {
        REC.log(TRACE, "Category.resolve: Category is " + this.name);
        REC.log(INFORMATIONAL, "add Category " + this.name);
        var root = classification;
        if (root != null) {
            var top = root;
            var parents = this.name.split("/");
            for (var k = 0; k < parents.length; k++) {
                var current = parents[k];
                var nodes;
                if (top == root)
                    nodes = top.getRootCategories("cm:generalclassifiable");
                else
                    nodes = top.subCategories;
                var nodeExists = false;
                for (var i = 0; i < nodes.length; i++) {
                    var node = nodes[i];
                    if (node.name == current) {
                        REC.log(TRACE, "Category [" + current + "] found");
                        top = node;
                        nodeExists = true;
                        break;
                    }
                }
                if (!nodeExists) {
                    REC.log(INFORMATIONAL, "Category [" + current + "] not found! Create Category");
                    if (top == root) {
                        REC.log(TRACE, "Create Root Category...");
                        top = classification.createRootCategory("cm:generalclassifiable", current);
                        REC.log(TRACE, "Root Category created!");
                    } else {
                        REC.log(TRACE, top.name + ": Create Sub Category...");
                        top = top.createSubCategory(current);
                        REC.log(TRACE, "Sub Category created!");
                    }
                }
            }
            if (top != null) {
                REC.log(TRACE, "Add Aspect cm:generalclassifiable to document");
                document.addAspect("cm:generalclassifiable");
                var categories = new Array(1);
                categories.push(top);
                REC.log(INFORMATIONAL, "Add Category [" + this.name + "] to document");
                document.properties["cm:categories"] = categories;
                document.save();
                REC.log(INFORMATIONAL, "Document saved!");
            } else
                REC.errors.push("Category top not found!");
        } else
            REC.errors.push("Category root not found!");
    }
    REC.debugLevel = orgLevel;
};

function Tags(srch) {
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.name = srch.name;
};

Tags.prototype.toString = function (ident) {
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    var txt = REC.getIdent(ident) + "Tags:\n";
    txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
    txt = txt + REC.getIdent(ident) + "name: " + this.name + "\n";
    return txt;
};

Tags.prototype.resolve = function (doc) {
    var orgLevel = REC.debugLevel;
    if (REC.exist(this.debugLevel))
        REC.debugLevel = this.debugLevel;
    REC.log(DEBUG, "resolve Tags");
    REC.log(TRACE, "Tags.resolve: settings are: \n" + this);
    if (REC.exist(this.name)) {
        REC.log(TRACE, "Tags.resolve: Tag is " + this.name);
        doc.addTag(this.name);
        doc.save();
        REC.log(INFORMATIONAL, "Document saved!");
        REC.log(INFORMATIONAL, "add Tag " + this.name);
    }
    REC.debugLevel = orgLevel;
};

function SearchItem(srch) {
    var tmp;
    var i;
    this.resolved = false;
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.name = srch.name;
    this.readOverReturn = REC.stringToBoolean(srch.readOverReturn, false);
    this.required = REC.stringToBoolean(srch.required, true);
    if (REC.exist(srch.fix))
        this.fix = srch.fix;
    if (REC.exist(srch.kind)) {
        tmp = REC.trim(srch.kind).split(",");
        this.kind = [];
        this.kind.push(tmp[0]);
        if (tmp.length > 1)
            this.kind.push(parseInt(REC.trim(tmp[1]), 10));
        else
            this.kind.push(1);
    }
    if (REC.exist(srch.word)) {
        tmp = REC.trim(srch.word).split(",");
        this.word = [];
        for (i = 0; i < tmp.length; i++) {
            this.word.push(parseInt(REC.trim(tmp[i]), 10));
        }
        this.readOverReturn = true;
    }
    if (REC.exist(srch.eval))
        this.eval = srch.eval;
    if (REC.exist(srch.text))
        this.text = srch.text;
    if (REC.exist(srch.value))
        this.value = srch.value;
    if (REC.exist(srch.target))
        this.target = srch.target;
    if (REC.exist(srch.expected))
        this.expected = srch.expected;
    else
        this.expected = null;
    if (REC.exist(srch.objectTyp))
        this.objectTyp = srch.objectTyp;
    else {
        if (REC.exist(this.kind)) {
            if (this.kind[0] == "date")
                this.objectTyp = "date";
            if (this.kind[0] == "amount")
                this.objectTyp = "float";
        } else
            this.objectTyp = "string";
    }
    this.completeWord = REC.stringToBoolean(srch.completeWord, false);
    this.caseSensitive = REC.stringToBoolean(srch.caseSensitive, false);
    this.included = REC.stringToBoolean(srch.included, false);
    if (REC.exist(srch.removeBlanks))
        this.removeBlanks = srch.removeBlanks;
    if (REC.exist(srch.removeReturns))
        this.removeReturns = srch.removeReturns;
    this.backwards = REC.stringToBoolean(srch.backwards, false);
    this.left = (REC.exist(srch.direction) && REC.trim(srch.direction).toLowerCase() == "left");
    tmp = new Array();
    REC.log(TRACE, "Search Check");
    if (REC.exist(srch.check)) {
        REC.log(TRACE, "Check exist");
        for (i = 0; i < srch.check.length; i++)
            tmp.push(new Check(srch.check[i], this));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Check found");
            this.check = tmp;
        } else
            REC.log(WARN, "No valid Check found");
    }
    tmp = new Array();
    REC.log(TRACE, "Search Delimitter");
    if (REC.exist(srch.delimitter)) {
        REC.log(TRACE, "Delimitter exist");
        for (i = 0; i < srch.delimitter.length; i++)
            tmp.push(new Delimitter(srch.delimitter[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Delimitter found");
            this.delimitter = tmp;
        } else
            REC.log(WARN, "No valid Delimitter found");
    }
    tmp = new Array();
    REC.log(TRACE, "Search Archivziel");
    if (REC.exist(srch.archivZiel)) {
        REC.log(TRACE, "Archivziel exist");
        for (i = 0; i < srch.archivZiel.length; i++)
            tmp.push(new ArchivZiel(srch.archivZiel[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Archivziel found");
            this.archivZiel = tmp;
        } else
            REC.log(WARN, "No valid Archivziel found");
    }
    tmp = new Array();
    REC.log(TRACE, "Format Archivziel");
    if (REC.exist(srch.format)) {
        REC.log(TRACE, "Format exist");
        for (i = 0; i < srch.format.length; i++)
            tmp.push(new Format(srch.format[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Format found");
            this.format = tmp;
        } else
            REC.log(WARN, "No valid Format found");
    }
};

SearchItem.prototype.toString = function (ident) {
    var i;
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    var txt = REC.getIdent(ident) + "SearchItem:\n";
    txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
    txt = txt + REC.getIdent(ident) + "name: " + this.name + "\n";
    txt = txt + REC.getIdent(ident) + "readOverReturn: " + this.readOverReturn + "\n";
    txt = txt + REC.getIdent(ident) + "fix: " + this.fix + "\n";
    txt = txt + REC.getIdent(ident) + "kind: " + this.kind + "\n";
    txt = txt + REC.getIdent(ident) + "word: " + this.word + "\n";
    txt = txt + REC.getIdent(ident) + "eval: " + this.eval + "\n";
    txt = txt + REC.getIdent(ident) + "text: " + this.text + "\n";
    txt = txt + REC.getIdent(ident) + "value: " + this.value + "\n";
    txt = txt + REC.getIdent(ident) + "expected: " + this.expected + "\n";
    txt = txt + REC.getIdent(ident) + "target: " + this.target + "\n";
    txt = txt + REC.getIdent(ident) + "objectTyp: " + this.objectTyp + "\n";
    txt = txt + REC.getIdent(ident) + "required: " + this.required + "\n";
    txt = txt + REC.getIdent(ident) + "removeBlanks: " + this.removeBlanks + "\n";
    txt = txt + REC.getIdent(ident) + "removeReturns: " + this.removeReturns + "\n";
    txt = txt + REC.getIdent(ident) + "backwards: " + this.backwards + "\n";
    txt = txt + REC.getIdent(ident) + "left: " + this.left + "\n";
    txt = txt + REC.getIdent(ident) + "caseSensitive: " + this.caseSensitive + "\n";
    txt = txt + REC.getIdent(ident) + "completeWord: " + this.completeWord + "\n";
    txt = txt + REC.getIdent(ident) + "included: " + this.included + "\n";
    if (REC.exist(this.check)) {
        for (i = 0; i < this.check.length; i++) {
            txt = txt + this.check[i].toString(ident);
        }
    }
    if (REC.exist(this.delimitter)) {
        for (i = 0; i < this.delimitter.length; i++) {
            txt = txt + this.delimitter[i].toString(ident);
        }
    }
    if (REC.exist(this.archivZiel)) {
        for (i = 0; i < this.archivZiel.length; i++) {
            txt = txt + this.archivZiel[i].toString(ident);
        }
    }
    if (REC.exist(this.format)) {
        for (i = 0; i < this.format.length; i++) {
            txt = txt + this.format[i].toString(ident);
        }
    }
    return txt;
};

SearchItem.prototype.convert = function (erg) {
    var numberExp = new RegExp("([\\-][1-9]{1}[0-9]{1,}\\.[\\d]{1,})|([1-9]{1}[0-9]{1,}\\.[\\d]{1,})|([\\-][1-9]{1}[0-9]{1,})|([1-9]{1}[0-9]{1,})", "g");
    var numberDotExp = new RegExp("\\d{1}\\.{1}\\d{1}", "g");
    for (var i = 0; i < erg.length; i++) {
        REC.log(TRACE, "SearchItem.resolve: call convertValue " + erg[i].text + " and " + this.name);
        if (typeof erg[i].text == "string" && REC.exist(this.text)) {
            erg[i] = REC.makeTrim(erg[i]);
            erg[i].start = REC.removedCharPos.getStartPos(erg[i].start);
            erg[i].end = REC.removedCharPos.getEndPos(erg[i].start, erg[i].text);
        }
        if (typeof erg[i].text == "string")
            erg[i].val = REC.convertValue(erg[i].text, erg[i].typ);
        if (erg[i].typ == "date" && !REC.isDate(erg[i].val)) {
            erg[i].check = false;
            erg[i].val = null;
            erg[i].error = "Result for " + this.name + " [" + erg[i].text + "] is not date";
        }
        if ((erg[i].typ == "int" || erg[i].typ == "float")) {
            if (!REC.isNumeric(erg[i].val)) {
                erg[i].check = false;
                erg[i].val = null;
                erg[i].error = "Result for " + this.name + " [" + erg[i].text + "] is not a numeric value";
                if (REC.exist(this.text)) {
                    var add = (erg[i].text.match(numberDotExp) != null ? erg[i].text.match(numberDotExp).length : 0);
                    var pos = REC.mergeStr(erg[i], ".").replace(",", ".").search(numberExp);
                    if (pos != -1) {
                        erg[i].start = erg[i].start + pos;
                        erg[i].end = erg[i].start + REC.mergeStr(erg[i], ".").replace(",", ".").match(REC.numberExp)[0].length + add;
                    }
                }
            }
        }
    }
    return erg;
};

SearchItem.prototype.findSpecialType = function (text, kind, left, expected) {
    var ret = new Array();
    var erg = null;
    var exp = new Array();
    if (kind[0] == "date") {
        exp[0] = new RegExp("\\d{1,2}\\.?.[ ]{0,9}[A\\u00C4BCDEFGIJKLMNOPRSTUVYZa\\u00E4bcdefgijklmnoprstuvyz]+\\.?[ ]{0,9}(\\d{4}|\\d{2})|\\d{1,2}\\.\\d{1,2}\\.(\\d{4}|\\d{2})", "g");
    } else if (kind[0] == "amount") {
        exp[0] = new RegExp("((([0-9]{1,3}\\.)*[0-9]{1,3})|\\d+)(?:\\.|,(\\d{2}))?( EUR| Euro| \\u20AC)", "g");
    } else if (kind[0] == "float") {
        exp[0] = new RegExp("[\-0-9\.]+[\,]+[0-9]*");
    }
    // exp[0] = new
    // RegExp("(0[1-9]|[12][0-9]|3[01])\\s(J(anuar|uli)|M(a(erz|i)|?rz)|August|(Okto|Dezem)ber)\\s[1-9][0-9]{3}|(0[1-9]|[12][0-9]|30)\\s(April|Juni|(Sept|Nov)ember)\\s[1-9][0-9]{3}|(0[1-9]|1[0-9]|2[0-8])\\sFebruar\\s[1-9][0-9]{3}|29\\sFebruar\\s((0[48]|[2468][048]|[13579][26])00|[0-9]{2}(0[48]|[2468][048]|[13579][26]))");
    // exp[1] = new
    // RegExp("((0?[1-9]|[12][1-9]|3[01])\\.(0?[13578]|1[02])\\.20[0-9]{2}|(0?[1-9]|[12][1-9]|30)\\.(0?[13456789]|1[012])\\.20[0-9]{2}|(0?[1-9]|1[1-9]|2[0-8])\\.(0?[123456789]|1[012])\\.20[0-9]{2}|(0?[1-9]|[12][1-9])\\.(0?[123456789]|1[012])\\.20(00|04|08|12|16|20|24|28|32|36|40|44|48|52|56|60|64|68|72|76|80|84|88|92|96))");
    for (var i = 0; i < exp.length; i++) {
        var match = text.match(exp[i]);
        var typ = null;
        if (REC.exist(match)) {
            for (var k = 0; k < match.length; k++) {
                var result = exp[i].exec(text);
                if (kind[0] == "date")
                    typ = "date";
                else if (kind[0] == "amount" || kind[0] == "float")
                    typ = "float";
                erg = REC.convertValue(match[k], typ);
                if (REC.exist(erg)) {
                    var res = new SearchResult(match[k], erg, result.index, result.index + match[k].length, typ, expected);
                    ret.push(res);
                }
            }
        }
    }
    if (left)
        ret.reverse();
    return ret.slice(kind[1] - 1);
};

SearchItem.prototype.handleError = function () {
    REC.log(INFORMATIONAL, "SearchItemm.resolve: " + this.name + " has NO RESULT");
    this.resolved = true;
    REC.results[this.name] = null;
    return null;
};

SearchItem.prototype.find = function (txt, erg) {
    var pos = 0;
    var lastPos = 0;
    var count = 0;
    var pst = (this.completeWord ? "\\b" + this.text + "?\\b" : this.text);
    var pat = new RegExp(pst, (this.caseSensitive ? "g" : "gi"));
    var match = txt.match(pat);
    var foundPos = new Array();
    if (REC.exist(match)) {
        for (var k = 0; k < match.length; k++) {
            var result = pat.exec(txt);
            foundPos.push(result.index);
        }
    }
    if (this.backwards) {
        REC.log(TRACE, "SearchItem.resolve: start search backwards with " + this.text);
        foundPos.reverse();
        match.reverse();
    } else {
        REC.log(TRACE, "SearchItem.resolve: start search forwards with " + this.text);
    }
    for (var j = 0; j < foundPos.length; j++) {
        pos = foundPos[j];
        REC.log(TRACE, "SearchItem.resolve: search found at position " + pos);
        var str;
        if (this.left) {
            str = new SearchResult(txt.slice(lastPos, pos + (this.included ? match[j].length : 0)), null, lastPos, pos + (this.included ? match[j].length : 0), this.objectTyp,
                this.expected);
            REC.log(TRACE, "SearchItem.resolve: get result left from position  " + REC.printTrace(str.text, this.left));
        } else {
            str = new SearchResult(txt.substr(pos + (this.included ? 0 : match[j].length)), null, pos + (this.included ? 0 : match[j].length), txt.length, this.objectTyp, this.expected);
            REC.log(TRACE, "SearchItem.resolve: get result right from position  " + REC.printTrace(str.text, this.left));
        }
        if (REC.exist(str) && str.text.length > 0) {
            REC.log(TRACE, "SearchItem.resolve: possible result is " + REC.printTrace(str.text, this.left));
            erg.modifyResult(str, count++);
        }
        lastPos = this.left ? 0 : pos;
    }
    return erg;
};

SearchItem.prototype.findForWords = function (erg, word, left) {
    for (var i = 0; i < erg.length; i++) {
        if (typeof erg[i].text == "string") {
            erg[i].text = erg[i].text.replace(/\s/g, ' ');
            if (left)
                erg[i].text = erg[i].text.split("").reverse().join("");
            var start = word[0];
            var end = 1;
            if (word.length > 1)
                end = word[1];
            var tmp = erg[i].text.split('');
            var begin = 0;
            var ende = 0;
            var marker = false;
            for (var k = 0; k < tmp.length; k++) {
                if (tmp[k] == " ") {
                    marker = true;
                }
                if (marker && tmp[k] != " ") {
                    begin++;
                    marker = false;
                }
                if (begin == start) {
                    start = k;
                    break;
                }
            }
            marker = false;
            for (k = start; k <= tmp.length; k++) {
                if (k == tmp.length)
                    end = k;
                else {
                    if (tmp[k] != " ") {
                        marker = true;
                    }
                    if (marker && tmp[k] == " ") {
                        ende++;
                        marker = false;
                    }
                    if (ende == end) {
                        end = k;
                        break;
                    }
                }
            }
            if (left) {
                erg[i].text = tmp.slice(start, end).reverse().join("");
                erg[i].end = erg[i].end - tmp.slice(0, start).reverse().join("").length;
                erg[i].start = erg[i].start + tmp.slice(end).reverse().join("").length;
            } else {
                erg[i].text = tmp.slice(start, end).join("");
                erg[i].start = erg[i].start + tmp.slice(0, start).join("").length;
                erg[i].end = erg[i].end - tmp.slice(end).join("").length;
            }
        }
    }
    return erg;
};

SearchItem.prototype.resolveItem = function (name) {
    for (var i = 0; i < REC.currentSearchItems.length; i++) {
        if (REC.currentSearchItems[i].name == name) {
            REC.currentSearchItems[i].resolve();
            return REC.results[name];
        }
    }
    REC.errors.push("SearchItem " + name + " not found!");
    return null;
};

SearchItem.prototype.resolve = function () {
    var i;
    var orgLevel = REC.debugLevel;
    if (REC.exist(this.debugLevel))
        REC.debugLevel = this.debugLevel;
    REC.log(DEBUG, "resolve SearchItem");
    REC.log(TRACE, "SearchItem.resolve: settings are: \n" + this);
    if (this.resolved) {
        if (REC.results[this.name] != null)
            return REC.results[this.name].getValue();
        else
            return null;
    }
    var erg = new SearchResultContainer();
    if (REC.exist(this.text))
        this.text = REC.replaceVar(this.text);
    var txt = null;
    if (REC.exist(this.fix))
        erg.modifyResult(new SearchResult(null, REC.convertValue(REC.replaceVar(this.fix), this.objectTyp), 0, 0, this.objectTyp, this.expected), 0);
    else if (REC.exist(this.eval)) {
        var e = eval(REC.replaceVar(this.eval));
        erg.modifyResult(new SearchResult(e.toString(), e, 0, 0, null, this.expected), 0);
    } else {
        if (REC.exist(this.value)) {
            var e = this.resolveItem(this.value);
            if (REC.exist(e)) {
                e = new SearchResult(e.text, e.val, e.start, e.end, e.typ, e.expected);
                if (REC.exist(this.expected))
                    e.expected = this.expected;
                if (REC.exist(this.objectTyp))
                    e.typ = this.objectTyp;
                erg.addResult(e);
                txt = erg.getResult().text;
            } else
                return this.handleError();
        } else
            txt = REC.content;
        REC.removedCharPos = new RemovedChar();
        if (REC.exist(this.removeBlanks) && this.removeBlanks == "before") {
            txt = txt.replace(/ /g, '');
        }
        if (REC.exist(this.removeReturns) && this.removeReturns == "before") {
            txt = txt.replace(/\n/g, '').replace(/\n/g, '');
        }
        if (REC.exist(this.kind))
            erg.modifyResult(this.findSpecialType(txt, this.kind, this.left, this.expected), 0);
        else if (REC.exist(this.text))
            erg = this.find(txt, erg);
    }
    if (erg.length == 0) {
        REC.log(TRACE, "searchItem.resolve: no matching result found");
    } else {
        if (REC.exist(this.delimitter)) {
            for (i = 0; i < this.delimitter.length; i++) {
                REC.log(DEBUG, "SearchItem.resolve: call Delimitter.resolve with " + REC.printTrace(erg, this.left));
                erg = this.delimitter[i].resolve(erg, this.left);
            }
        }
        if (!this.readOverReturn && REC.exist(this.text)) {
            var exp = new RegExp("[\\n\\n]");
            for (i = 0; i < erg.length; i++) {
                if (typeof erg[i].text == "string") {
                    pos = erg[i].text.search(exp);
                    if (pos != -1) {
                        erg[i].text = erg[i].text.substr(0, pos);
                        erg[i].end = REC.removedCharPos.getEndPos(erg[i].start, erg[i].text);
                    }
                }
            }
            REC.log(DEBUG, "SearchItem.resolve: readOverReturn result is " + REC.printTrace(erg, this.left));
        }
        if (REC.exist(this.word)) {
            erg = this.findForWords(erg, this.word, this.left);
        }
        if (REC.exist(this.removeBlanks) && this.removeBlanks == "after") {
            erg = REC.removeBlanks(erg);
        }
        if (REC.exist(this.removeReturns) && this.removeReturns == "after") {
            erg = REC.removeReturns(erg);
        }
        erg = this.convert(erg);
        if (REC.exist(this.format)) {
            for (i = 0; i < this.format.length; i++) {
                REC.log(DEBUG, "SearchItem.resolve: call Format.resolve with " + erg.getResult().getValue());
                erg.getResult().val = this.format[i].resolve(erg.getResult().getValue());
            }
        }
        if (REC.exist(this.check)) {
            for (i = 0; i < this.check.length; i++) {
                REC.log(DEBUG, "SearchItem.resolve: call Check.resolve with " + erg);
                erg = this.check[i].resolve(erg);
            }
        }
        REC.positions.add(REC.convertPosition(REC.content, erg.getResult().start, erg.getResult().end, this.name, erg.getResult().check));

        if (REC.exist(this.archivZiel)) {
            for (i = 0; i < this.archivZiel.length; i++) {
                REC.log(DEBUG, "SearchItem.resolve: call ArchivZiel.resolve with " + REC.currentDocument.displayPath.split("/").slice(2).join("/") + "/" + REC.currentDocument.name);
                this.archivZiel[i].resolve(REC.currentDocument.displayPath.split("/").slice(2).join("/") + "/" + REC.currentDocument.name);
            }
        }
        if (REC.exist(this.target) && erg.isFound()) {
            REC.log(INFORMATIONAL, "currentDocument.properties[\"" + this.target + "\"] = \"" + erg.getResult().getValue() + "\";");
            REC.currentDocument.properties[this.target] = erg.getResult().getValue();
            REC.currentDocument.save();
            REC.log(INFORMATIONAL, "Document saved!");
        }
    }
    if (this.required && !erg.isFound()) {
        var e = "Required SearchItem " + this.name + " is missing";
        REC.errors.push(e);
        REC.errors.push(erg.getError());
    } else if (erg.isFound()) {
        REC.log(DEBUG, "SearchItem.resolve: return is  " + erg.getResult().getValue());
        REC.debugLevel = orgLevel;
        REC.results[this.name] = erg.getResult();
        REC.log(INFORMATIONAL, this.name + " is " + erg.getResult().getValue());
        this.resolved = true;
        return erg.getResult().getValue();
    } else
        return this.handleError();
};

function Position(startRow, startColumn, endRow, endColumn, type, desc) {
    this.startRow = startRow;
    this.startColumn = startColumn;
    this.endRow = endRow;
    this.endColumn = endColumn;
    this.type = type;
    this.desc = desc;
};

Position.prototype.print = function () {
    return "StartRow: " + this.startRow + " StartColumn: " + this.startColumn + " EndRow: " + this.endRow + " EndColumn: " + this.endColumn + " Description: " + this.desc;
};

function PositionContainer() {
};

PositionContainer.prototype = new Array();

PositionContainer.prototype.add = function (pos) {
    var found = false;
    if (!(pos.startRow == pos.endRow && pos.startColumn == pos.endColumn)) {
        for (var i = 0; i < this.length; i++) {
            if ((pos.startRow > this[i].startRow && pos.endRow < this[i].endRow) || (pos.startRow == this[i].startRow && pos.startColumn >= this[i].startColumn)
                && (pos.endRow == this[i].endRow && pos.endColumn <= this[i].endColumn)) {
                this[i] = pos;
                found = true;
                break;
            }
        }
        if (!found)
            this.push(pos);
    }
}

function SearchResultContainer() {
};

SearchResultContainer.prototype = new Array();

SearchResultContainer.prototype.addResult = function (result) {
    if (result instanceof Array) {
        for (var i = 0; i < result.length; i++)
            this.push(result[i]);
    } else
        this.push(result);
};

SearchResultContainer.prototype.getResult = function () {
    for (var i = 0; i < this.length; i++) {
        if (this[i].check)
            return this[i];
    }
    if (REC.exist(this[0]))
        return this[0];
    return null;
};

SearchResultContainer.prototype.removeResult = function (result) {
    var h = new Array();
    for (var i = 0; i < this.length; i++) {
        if (this[i] != result)
            h.push(this[i]);
    }
    this.prototype = h;
};

SearchResultContainer.prototype.modifyResult = function (result, pos) {
    if (!REC.exist(this[pos]))
        this.addResult(result);
    else {
        if (result instanceof Array) {
            this.modifyResult(result[0], pos);
            for (var i = 1; i < result.length; i++) {
                this.addResult(result[i]);
            }
        } else {
            this[pos].text = result.text;
            this[pos].val = result.val;
            this[pos].check = result.check;
            this[pos].error = result.error;
            this[pos].typ = result.typ;
            this[pos].expected = result.expected;
            this[pos].start = this[pos].start + result.start;
            this[pos].end = this[pos].start + result.end - result.start;
        }
    }
};

SearchResultContainer.prototype.isFound = function () {
    return (REC.exist(this.getResult()) && this.getResult().check);
};

SearchResultContainer.prototype.toString = function (ident) {
    var txt = "";
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    for (var i = 0; i < this.length; i++) {
        txt = txt + REC.getIdent(ident) + this[i].toString() + "\n";
    }
    return txt;
}

SearchResultContainer.prototype.getError = function () {
    var e = this.getResult();
    if (e != null)
        return e.error;
    else
        return null;
};

/**
 * speichert das Ergbenis einer Suche
 * @param  text     der Text mit der Fundstelle
 * @param  val      das Ergebnis als passender Objecttyp
 * @param  start    die Beginnposition des Ergebnis uim Text
 * @param  end      die Endeposition des Ergebnis uim Text
 * @param  typ      der Typ des Ergebnis
 * @param  expected für Testzwecke. Hier kann ein erwartetes Ergebnis hinterlegt werden
 */
function SearchResult(text, val, start, end, typ, expected) {
    this.text = text;
    this.start = start;
    this.end = end;
    this.check = true;
    this.error = null;
    this.val = val;
    this.expected = expected;
    if (!REC.exist(typ) && REC.exist(val)) {
        if (typeof val == "number")
            this.typ = "float";
        else if (val instanceof Date)
            this.typ = "date";
        else
            this.typ = "string";
    } else
        this.typ = typ;
}

SearchResult.prototype.getValue = function () {
    return this.val;
};

SearchResult.prototype.toString = function(ident) {
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    var txt = REC.getIdent(ident) + "SearchResult:\n";
    txt = txt + REC.getIdent(ident) + "text    : " + this.text + "\n";
    txt = txt + REC.getIdent(ident) + "start   : " + this.start + "\n";
    txt = txt + REC.getIdent(ident) + "end     : " + this.end + "\n";
    txt = txt + REC.getIdent(ident) + "val     : " + this.val + "\n";
    txt = txt + REC.getIdent(ident) + "typ     : " + this.typ + "\n";
    txt = txt + REC.getIdent(ident) + "expected: " + this.expected + "\n";
    return txt;
};


function RemovedChar() {
    this.removedChar = new Array();
}

RemovedChar.prototype.push = function (obj) {
    this.removedChar.push(obj);
}

RemovedChar.prototype.getStartPos = function (startPos) {
    this.removedChar.sort(function (a, b) {
        return a - b;
    });
    var finalPos = startPos;
    for (var k = 0; k < this.removedChar.length; k++) {
        if (this.removedChar[k] == finalPos)
            finalPos++;
        else
            break;
    }
    return finalPos;
};

RemovedChar.prototype.getEndPos = function (startPos, txt) {
    this.removedChar.sort(function (a, b) {
        return a - b;
    });
    var finalPos = startPos + txt.length;
    for (var k = 0; k < this.removedChar.length; k++) {
        if (this.removedChar[k] >= startPos) {
            if (this.removedChar[k] < finalPos)
                finalPos++;
            else
                break;
        }
    }
    return finalPos;
};

function XMLObject(ruleDocument) {
    var attributes = ruleDocument.getAttributeNames();
    var count = attributes.length;
    for (var i = 0; i < count; i++) {
        var attribute = attributes[i];
        if (attribute.indexOf(":") == -1)
            eval("this." + attribute + " = \"" + ruleDocument.getAttribute(attribute) + "\";");
    }
    var tmp = new Array();
    // for each(elem in rule.children()) {
    var elements = ruleDocument.getElements();
    for (var i = 0; i < elements.length; i++) {
        var elem = elements[i];
        if (typeof tmp[elem.tagName] == "undefined") {
            tmp[elem.tagName] = new Array();
            tmp[elem.tagName].push(new XMLObject(elem));
        } else
            tmp[elem.tagName].push(new XMLObject(elem));
    }

    for (nam in tmp)
        eval("this." + nam + " = tmp[\"" + nam + "\"];");
};

function DebugLevel(level, text) {
    this.level = level;
    this.text = text;
};

Recognition.prototype.getDebugLevel = function (level) {
    var ret = null;
    if (this.exist(level)) {
        if (typeof level == "string") {
            if (this.trim(level).toLowerCase() == "none")
                ret = NONE;
            else if (this.trim(level).toLowerCase() == "error")
                ret = ERROR;
            else if (this.trim(level).toLowerCase() == "warn")
                ret = WARN;
            else if (this.trim(level).toLowerCase() == "informational")
                ret = INFORMATIONAL;
            else if (this.trim(level).toLowerCase() == "debug")
                ret = DEBUG;
            else if (this.trim(level).toLowerCase() == "trace")
                ret = TRACE;
        } else {
            if (level == 0)
                ret = NONE;
            else if (level == 1)
                ret = ERROR;
            else if (level == 2)
                ret = WARN;
            else if (level == 3)
                ret = INFORMATIONAL;
            else if (level == 4)
                ret = DEBUG;
            else if (level == 5)
                ret = TRACE;
        }
    } else
        ret = ERROR;
    return ret;
};

Recognition.prototype.print = function (obj, maxDepth, prefix) {
    var result = '';
    if (!prefix)
        prefix = '';
    if (typeof obj == "object") {
        for (var key in obj) {
            if (typeof obj[key] == 'object') {
                if (maxDepth !== undefined && maxDepth <= 1) {
                    result += (prefix + key + '=object [max depth reached]\n');
                } else
                    result += print(obj[key], (maxDepth) ? maxDepth - 1 : maxDepth, prefix + key + '.');
            } else {
                if (typeof obj[key] != "function")
                    if (typeof obj[key] == "string" && obj[key].length > REC.maxDebugLength)
                        result += (prefix + key + '=' + obj[key].slice(0, REC.maxDebugLength) + '...\n');
                    else
                        result += (prefix + key + '=' + obj[key] + '\n');
            }
        }
    } else if (typeof obj == 'string' && obj.length > REC.maxDebugLength)
        result += obj.slice(0, REC.maxDebugLength) + '...\n';
    else
        result += obj + '\n';
    return result;
};

Recognition.prototype.printTrace = function (str, left) {
    var result = "";
    if (left)
        result = "..." + str.slice((str.length - REC.maxDebugLength > 0 ? str.length - REC.maxDebugLength : 0), str.length) + "\n";
    else
        result = str.slice(0, str.length < REC.maxDebugLength ? str.length : REC.maxDebugLength) + '...\n';
    return result;
};

Recognition.prototype.replaceVar = function (str) {
    var replaced = false;
    if (str.indexOf("{") != -1) {
        if (this.exist(this.currentSearchItems)) {
            for (var i = 0; i < this.currentSearchItems.length; i++) {
                if (str.indexOf("{" + this.currentSearchItems[i].name + "}") != -1) {
                    var erg = this.currentSearchItems[i].resolve();
                    if (this.exist(erg)) {
                        str = str.replace(new RegExp("{" + this.currentSearchItems[i].name + "}", 'g'), erg);
                        replaced = true;
                    } else
                        str = str.replace(new RegExp("{" + this.currentSearchItems[i].name + "}", 'g'), null);

                }
            }
        }
        if (!replaced)
            REC.errors.push("could not replace Placeholder " + str.match(/\{.+\}/g) + "!");
    }
    return str;
};

Recognition.prototype.buildDate = function (text) {
    var monate = new Array("Januar", "Februar", "M\u00e4rz", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember");
    var tmp;
    var txt;
    var i;
    var dat;
    if (!this.isDate(dat)) {
        txt = text.replace(/ /g, '');
        for (i = 0; i < monate.length; i++) {
            var pos = txt.indexOf(monate[i]);
            if (pos != -1) {
                var form = this.numberFormat(i + 1, "00") + ".";
                if (pos > 0 && txt.charAt(pos - 1) != ".")
                    form = "." + form;
                txt = txt.replace(monate[i], form);
                tmp = txt.split(".");
                while (tmp.length < 3)
                    tmp.unshift("01");
                for (var k = tmp.length; k > 0; k--) {
                    if (k > 3) {
                        REC.log(WARN, "Kein Datum " + text);
                        return null;
                    }
                    if (k == tmp.length && tmp[k - 1].length == 2)
                        tmp[k - 1] = "20" + tmp[k - 1];
                    if (k != tmp.length && tmp[k - 1].length == 1)
                        tmp[k - 1] = "0" + tmp[k - 1];
                }
                var k = tmp[0];
                tmp[0] = tmp[1];
                tmp[1] = k;
                txt = tmp.join("/");
                dat = new Date(txt);
                break;
            }
        }
    }
    if (!this.isDate(dat)) {
        var jahr = text.toString().substr(6);
        if (jahr.length == 2) {
            if (parseInt(jahr, 10) < 60)
                jahr = "20" + jahr;
            else
                jahr = "19" + jahr;
        }
        var mon = text.toString().slice(3, 5);
        var tag = text.toString().slice(0, 2);
        dat = new Date(jahr + "/" + mon + "/" + tag);
    }
    if (!this.isDate(dat)) {
        dat = new Date(text);
    }
    if (!this.isDate(dat)) {
        txt = text.toString().split("/")[0] + "/01/20" + text.toString().split("/")[1];
        dat = new Date(txt);
    }
    if (!this.isDate(dat)) {
        txt = this.formatNumber(this.getPosition(monate, text.toString().split(" ")[0]) + 1, 2) + "/01/" + text.toString().split(" ")[1];
        dat = new Date(txt);
    }
    return dat;
};

Recognition.prototype.dateFormat = function (formatDate, formatString) {
    if (formatDate instanceof Date) {
        var returnStr = '';

        var replaceChars = {
            shortMonths: ['Jan', 'Feb', 'Mar', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'],
            longMonths: ['Januar', 'Februar', 'M\u00e4rz', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'],
            shortDays: ['Son', 'Mon', 'Die', 'Mit', 'Don', 'Fre', 'Sam'],
            longDays: ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],

            // Day
            d: function () {
                return (formatDate.getDate() < 10 ? '0' : '') + formatDate.getDate();
            },
            D: function () {
                return this.replaceChars.shortDays[formatDate.getDay()];
            },
            j: function () {
                return formatDate.getDate();
            },
            l: function () {
                return this.replaceChars.longDays[formatDate.getDay()];
            },
            N: function () {
                return formatDate.getDay() + 1;
            },
            S: function () {
                return (formatDate.getDate() % 10 == 1 && formatDate.getDate() != 11 ? 'st' : (formatDate.getDate() % 10 == 2 && formatDate.getDate() != 12 ? 'nd'
                    : (formatDate.getDate() % 10 == 3 && formatDate.getDate() != 13 ? 'rd' : 'th')));
            },
            w: function () {
                return formatDate.getDay();
            },
            z: function () {
                var d = new Date(formatDate.getFullYear(), 0, 1);
                return Math.ceil((formatDate - d) / 86400000);
            },
            // Fixed now
            // Week
            W: function () {
                var d = new Date(formatDate.getFullYear(), 0, 1);
                return Math.ceil((((formatDate - d) / 86400000) + d.getDay() + 1) / 7);
            },
            // Fixed now
            // Month
            F: function () {
                return replaceChars.longMonths[formatDate.getMonth()];
            },
            m: function () {
                return (formatDate.getMonth() < 9 ? '0' : '') + (formatDate.getMonth() + 1);
            },
            M: function () {
                return replaceChars.shortMonths[formatDate.getMonth()];
            },
            n: function () {
                return formatDate.getMonth() + 1;
            },
            t: function () {
                var d = new Date();
                return new Date(d.getFullYear(), d.getMonth(), 0).getDate()
            },
            // Fixed now, gets #days of date
            // Year
            L: function () {
                var year = formatDate.getFullYear();
                return (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0));
            },
            // Fixed now
            o: function () {
                var d = new Date(formatDate.valueOf());
                d.setDate(d.getDate() - ((formatDate.getDay() + 6) % 7) + 3);
                return d.getFullYear();
            },
            // Fixed now
            Y: function () {
                return formatDate.getFullYear();
            },
            y: function () {
                return ('' + formatDate.getFullYear()).substr(2);
            },
            // Time
            a: function () {
                return formatDate.getHours() < 12 ? 'am' : 'pm';
            },
            A: function () {
                return formatDate.getHours() < 12 ? 'AM' : 'PM';
            },
            B: function () {
                return Math.floor((((formatDate.getUTCHours() + 1) % 24) + formatDate.getUTCMinutes() / 60 + formatDate.getUTCSeconds() / 3600) * 1000 / 24);
            },
            // Fixed now
            g: function () {
                return formatDate.getHours() % 12 || 12;
            },
            G: function () {
                return formatDate.getHours();
            },
            h: function () {
                return ((formatDate.getHours() % 12 || 12) < 10 ? '0' : '') + (formatDate.getHours() % 12 || 12);
            },
            H: function () {
                return (formatDate.getHours() < 10 ? '0' : '') + formatDate.getHours();
            },
            i: function () {
                return (formatDate.getMinutes() < 10 ? '0' : '') + formatDate.getMinutes();
            },
            s: function () {
                return (formatDate.getSeconds() < 10 ? '0' : '') + formatDate.getSeconds();
            },
            u: function () {
                var m = formatDate.getMilliseconds();
                return (m < 10 ? '00' : (m < 100 ? '0' : '')) + m;
            },
            // Timezone
            e: function () {
                return "Not Yet Supported";
            },
            I: function () {
                return "Not Yet Supported";
            },
            O: function () {
                return (-formatDate.getTimezoneOffset() < 0 ? '-' : '+') + (Math.abs(formatDate.getTimezoneOffset() / 60) < 10 ? '0' : '')
                    + (Math.abs(formatDate.getTimezoneOffset() / 60)) + '00';
            },
            P: function () {
                return (-formatDate.getTimezoneOffset() < 0 ? '-' : '+') + (Math.abs(formatDate.getTimezoneOffset() / 60) < 10 ? '0' : '')
                    + (Math.abs(formatDate.getTimezoneOffset() / 60)) + ':00';
            },
            // Fixed now
            T: function () {
                var m = formatDate.getMonth();
                formatDate.setMonth(0);
                var result = formatDate.toTimeString().replace(/^.+ \(?([^\)]+)\)?$/, '$1');
                formatDate.setMonth(m);
                return result;
            },
            Z: function () {
                return -formatDate.getTimezoneOffset() * 60;
            },
            // Full Date/Time
            c: function () {
                return formatDate.format("Y-m-d\\TH:i:sP");
            },
            // Fixed now
            r: function () {
                return formatDate.toString();
            },
            U: function () {
                return formatDate.getTime() / 1000;
            }
        };

        formatString = formatString.replace("MMMM", "F").replace("MMM", "M").replace("MM", "m").replace("YYYY", "Y").replace("YY", "y").replace("dd", "d");

        for (var i = 0; i < formatString.length; i++) {
            var curChar = formatString.charAt(i);
            if (i - 1 >= 0 && formatString.charAt(i - 1) == "\\") {
                returnStr += curChar;
            } else if (replaceChars[curChar]) {
                returnStr += replaceChars[curChar].call(this.formatDate);
            } else if (curChar != "\\") {
                returnStr += curChar;
            }
        }
        return returnStr;
    } else
        return "";
};

Recognition.prototype.numberFormat = function (formatNumber, formatString) {
    if (!formatString || isNaN(+formatNumber))
        return formatNumber;
    formatNumber = formatString.charAt(0) == "-" ? -formatNumber : +formatNumber, j = formatNumber < 0 ? formatNumber = -formatNumber : 0, e = formatString.match(/[^\d\-\+#]/g),
        h = e && e[e.length - 1] || ".", e = e && e[1] && e[0] || ",", formatString = formatString.split(h), formatNumber = formatNumber.toFixed(formatString[1]
    && formatString[1].length), formatNumber = +formatNumber + "", d = formatString[1] && formatString[1].lastIndexOf("0"), c = formatNumber.split(".");
    if (!c[1] || c[1] && c[1].length <= d)
        formatNumber = (+formatNumber).toFixed(d + 1);
    d = formatString[0].split(e);
    formatString[0] = d.join("");
    var f = formatString[0] && formatString[0].indexOf("0");
    if (f > -1)
        for (; c[0].length < formatString[0].length - f;)
            c[0] = "0" + c[0];
    else
        +c[0] == 0 && (c[0] = "");
    formatNumber = formatNumber.split(".");
    formatNumber[0] = c[0];
    if (c = d[1] && d[d.length - 1].length) {
        for (var d = formatNumber[0], f = "", k = d.length % c, g = 0, i = d.length; g < i; g++)
            f += d.charAt(g), !((g - k + 1) % c) && g < i - c && (f += e);
        formatNumber[0] = f;
    }
    formatNumber[1] = formatString[1] && formatNumber[1] ? h + formatNumber[1] : "";
    return (j ? "-" : "") + formatNumber[0] + formatNumber[1];
};

Recognition.prototype.convertValue = function (val, typ) {
    var erg = null;
    if (this.exist(typ)) {
        if (this.trim(typ.toString()).toLowerCase() == "string")
            erg = val;
        else if (this.trim(typ.toString()).toLowerCase() == "date")
            erg = isNaN(this.buildDate(val).getTime()) ? null : this.buildDate(val);
        else if (this.trim(typ.toString()).toLowerCase() == "int")
            erg = isNaN(parseInt(this.prepareNumber(val), 10)) ? null : parseInt(this.prepareNumber(val), 10);
        else if (this.trim(typ.toString()).toLowerCase() == "float")
            erg = isNaN(parseFloat(this.prepareNumber(val))) ? null : parseFloat(this.prepareNumber(val));
    } else
        erg = val;
    return erg;
};

Recognition.prototype.prepareNumber = function (val) {
    if (val.indexOf(',') == -1 && val.split(".").length - 1 == 1)
        val = val.replace(/\./g, ',');
    val = val.replace(/\./g, '').replace(/,/g, ".");
    return val;
};

Recognition.prototype.monatName = function (datum) {
    var monatZahl = datum.getMonth();
    return monat[monatZahl];
};

Recognition.prototype.fillValues = function (value, srch) {
    if (this.exist(value)) {
        if (this.exist(srch.archivZiel[0].aspect)) {
            if (!this.currentDocument.hasAspect(srch.archivZiel[0].aspect.toString()))
                this.currentDocument.addAspect(srch.archivZiel[0].aspect.toString());
            REC.log(INFORMATIONAL, "Document add aspect " + srch.archivZiel[0].aspect.toString());
        }
        this.log(DEBUG, srch.name + ": " + value);
        this.log(INFORMATIONAL, "Document.properties[\"" + srch.archivZiel[0].target + "\"] = \"" + value + "\"");
        this.currentDocument.properties[srch.archivZiel[0].target] = value;
    }
};

Recognition.prototype.buildFolder = function (direction) {
    this.log(TRACE, "buildFolder: entering with " + direction);
    var fol = null;
    var dir = direction;
    var top = companyhome.childByNamePath(direction);
    if (top == null) {
        this.log(TRACE, "buildFolder: folder " + direction + " not found");
        var parts = direction.split("/");
        dir = "";
        for (var i = 0; i < parts.length; i++) {
            var part = parts[i];
            dir = dir + (dir.length == 0 ? "" : "/") + part;
            this.log(TRACE, "buildFolder: search Folder " + dir);
            if (dir.length > 0)
                fol = companyhome.childByNamePath(dir);
            if (!this.exist(fol)) {
                this.log(INFORMATIONAL, "erstelle Folder " + dir);
                if (top == null) {
                    this.log(TRACE, "buildFolder: create Folder[" + part + "] at companyhome ");
                    top = companyhome.createFolder(part);
                } else {
                    this.log(TRACE, "buildFolder: create Folder[" + part + "] at " + top.name);
                    top = top.createFolder(part);
                }
                if (top == null) {
                    this.errors.push("Folder " + dir + " konnte nicht erstellt werden");
                    dir = null;
                    break;
                }
            } else {
                this.log(TRACE, "buildFolder: folder " + dir + " found");
                top = fol;
            }
        }
    }
    this.log(TRACE, "buildFolder result is " + dir);
    return dir;
};

Recognition.prototype.makeNewVersion = function (doc, newDoc) {
    if (doc.isLocked) {
        this.errors.push("Gelocktes Dokument kann nicht ver?ndert werden!");
        return false;
    } else {
        if (!doc.hasAspect("cm:workingcopy")) {
            doc.ensureVersioningEnabled(true, false);
            var workingCopy = doc.checkoutForUpload();
            workingCopy.properties.content.write(this.currentDocument.properties.content);
            workingCopy.checkin();
            newDoc.remove();
            this.log(INFORMATIONAL, "Neue Version des Dokumentes erstellt");
            return true;
        }
    }
};

Recognition.prototype.isEmpty = function (str) {
    return (str == null) || (str.length == 0);
};


Recognition.prototype.isDate = function (x) {
    return (null != x) && !isNaN(x) && ("undefined" !== typeof x.getDate);
};

Recognition.prototype.getAmountInEuro = function (text, date) {
    var x = parseFloat(text);
    if (date < new Date("August 01, 2001 00:00:00"))
        x = dmToEuro(x);
    return x;
};

Recognition.prototype.dmToEuro = function (x) {
    var k = (Math.round((x / 1.95583) * 100) / 100).toString();
    k += (k.indexOf('.') == -1) ? '.00' : '00';
    return parseFloat(k.substring(0, k.indexOf('.') + 3));
};

Recognition.prototype.isNumeric = function (n) {
    var n2 = n;
    n = parseFloat(n);
    return (!isNaN(n) && n2 == n);
};

Recognition.prototype.trim = function (str) {
    return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
};

Recognition.prototype.trim = function (trimString, leftTrim, rightTrim) {
    if (REC.isEmpty(trimString)) {
        return "";
    }
    if (leftTrim == null) {
        leftTrim = true;
    }
    if (rightTrim == null) {
        rightTrim = true;
    }
    var left = 0;
    var right = 0;
    var i = 0;
    var k = 0;
    if (leftTrim == true) {
        while ((i < trimString.length) && (whitespace.indexOf(trimString.charAt(i++)) != -1)) {
            left++;
        }
    }
    if (rightTrim == true) {
        k = trimString.length - 1;
        while ((k >= left) && (whitespace.indexOf(trimString.charAt(k--)) != -1)) {
            right++;
        }
    }
    return trimString.substring(left, trimString.length - right);
};

Recognition.prototype.mergeStr = function (erg, c) {
    var arg = new Array();
    for (var i = 0; i < erg.text.length; i++) {
        var part = erg.text.substr(i, 1);
        if (part != c)
            arg.push(part);
        else
            this.removedCharPos.push(erg.start + i);
    }
    return arg.join("");
};

Recognition.prototype.removeReturns = function (erg) {
    for (var i = 0; i < erg.length; i++) {
        if (typeof erg[i].text == "string") {
            this.log(TRACE, "Removing Returns from String...");
            erg[i].text = this.mergeStr(erg[i], '\n');
            erg[i].text = this.mergeStr(erg[i], '\n');
            erg[i].start = this.removedCharPos.getStartPos(erg[i].start);
            erg[i].end = this.removedCharPos.getEndPos(erg[i].start, erg[i].text);
        }
    }
    return erg;
};

Recognition.prototype.removeBlanks = function (erg) {
    for (var i = 0; i < erg.length; i++) {
        if (typeof erg[i].text == "string") {
            this.log(TRACE, "Removing Blanks from String...");
            erg[i].text = this.mergeStr(erg[i], ' ');
            erg[i].start = this.removedCharPos.getStartPos(erg[i].start);
            erg[i].end = this.removedCharPos.getEndPos(erg[i].start, erg[i].text);
        }
    }
    return erg;
};

Recognition.prototype.convertEscapes = function (str) {
    var escAmpRegEx = /&amp;/g;
    var escLtRegEx = /&lt;/g;
    var escGtRegEx = /&gt;/g;
    str = str.replace(escAmpRegEx, "&");
    str = str.replace(escLtRegEx, "<");
    str = str.replace(escGtRegEx, ">");
    return str;
};

Recognition.prototype.convertToEscapes = function (str) {
    var escAmpRegEx = /&/g;
    var escLtRegEx = /</g;
    var escGtRegEx = />/g;
    str = str.replace(escAmpRegEx, "&amp;");
    str = str.replace(escLtRegEx, "&lt;");
    str = str.replace(escGtRegEx, "&gt;");
    return str;
};

Recognition.prototype.firstWhiteChar = function (str, pos) {
    if (REC.isEmpty(str)) {
        return -1;
    }
    while (pos < str.length) {
        if (whitespace.indexOf(str.charAt(pos)) != -1) {
            return pos;
        } else {
            pos++;
        }
    }
    return str.length;
};

Recognition.prototype.displayElement = function (domElement, strRet) {
    if (domElement == null) {
        return;
    }
    if (!(domElement.nodeType == 'ELEMENT')) {
        return;
    }
    var tagName = domElement.tagName;
    var tagInfo = "";
    tagInfo = "<" + tagName;
    var attributeList = domElement.getAttributeNames();
    for (var intLoop = 0; intLoop < attributeList.length; intLoop++) {
        var attribute = attributeList[intLoop];
        tagInfo = tagInfo + " " + attribute + "=";
        tagInfo = tagInfo + "\"" + domElement.getAttribute(attribute) + "\"";
    }
    tagInfo = tagInfo + ">";
    strRet = strRet + tagInfo;
    if (domElement.children != null) {
        var domElements = domElement.children;
        for (var intLoop = 0; intLoop < domElements.length; intLoop++) {
            var childNode = domElements[intLoop];
            if (childNode.nodeType == 'COMMENT') {
                strRet = strRet + "<!--" + childNode.content + "-->";
            }
            else if (childNode.nodeType == 'TEXT') {
                var cont = REC.trim(childNode.content, true, true);
                strRet = strRet + childNode.content;
            }
            else if (childNode.nodeType == 'CDATA') {
                var cont = REC.trim(childNode.content, true, true);
                strRet = strRet + "<![CDATA[" + cont + "]]>";
            }
            else {
                strRet = REC.displayElement(childNode, strRet);
            }
        }
    }
    strRet = strRet + "</" + tagName + ">";
    return strRet;
};

Recognition.prototype.makeTrim = function (erg) {
    var startpos = 0;
    var endpos = erg.text.length;
    var pos = erg.text.search(/[^\s\s*]/);
    if (pos != -1)
        startpos = pos;
    pos = erg.text.search(/\s\s*$/);
    if (pos != -1)
        endpos = pos;
    for (var i = 0; i < erg.text.length; i++)
        if (i < startpos || i > endpos)
            this.removedCharPos.push(erg.start + i);
    erg.text = this.trim(erg.text);
    return erg;
};

Recognition.prototype.getMessage = function () {
    if (this.errors.length > 0) {
        for (var i = 0; i < this.errors.length; i++)
            this.log(ERROR, "Fehler: " + this.errors[i]);
    }
    if (this.showContent || this.debugLevel.level >= DEBUG.level)
        this.mess = this.mess + "\n=====>\n" + this.content + "\n<====\n";
    return this.mess;
};

Recognition.prototype.log = function (level, text) {
    if (this.debugLevel.level >= level.level) {
        this.mess = this.mess + this.dateFormat(new Date(), "G:i:s,u") + " " + level.text + " " + text + "\n";
    }
};

Recognition.prototype.exist = function (val) {
    return typeof val != "undefined" && val != null;
};

Recognition.prototype.searchArray = function (arr, value) {
    var arr2str = arr.toString();
    return arr2str.search(value);
};

Recognition.prototype.getPosition = function (arr, obj) {
    for (var i = 0; i < arr.length; i++) {
        if (arr[i] == obj)
            return i;
    }
    return -1;
};

Recognition.prototype.formatNumber = function (zahl, laenge) {
    erg = String(zahl);
    while (laenge > erg.length)
        erg = "0" + erg;
    return erg;
};


Recognition.prototype.stringToBoolean = function (string, defaultVal) {
    if (this.exist(string)) {
        switch (string.toLowerCase()) {
            case "true":
            case "yes":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
            case null:
                return false;
            default:
                return Boolean(string);
        }
    } else
        return defaultVal;
};

Recognition.prototype.getRules = function () {
    var rules = script.parent.childByNamePath("doc.xml");
    if (!this.exist(rules)) {
        throw "Regeln nicht gefunden";
    }
    this.log(INFORMATIONAL, "Regeln gefunden!");
    this.log(TRACE, "Lade XML...");
    XMLDOC.loadXML(rules.content + "");
    XMLDOC.parse();
    this.log(TRACE, "XML geladen");
    return new XMLObject(XMLDOC.docNode);
};

Recognition.prototype.getIdent = function (count) {
    var ret = "";
    for (var i = 0; i < count; i++)
        ret = ret + "\t";
    return ret;
};

function Comments() {
};

Comments.prototype.addComment = function (node, content) {
    // fetch the data required to create a comment
    var title = "";
    // fetch the parent to add the node to
    var commentsFolder = this.getOrCreateCommentsFolder(node);
    // get a unique name
    var name = this.getUniqueChildName(commentsFolder, "comment");
    // create the comment
    var commentNode = commentsFolder.createNode(name, "fm:post");
    commentNode.mimetype = "text/html";
    commentNode.properties.title = title;
    commentNode.content = content;
    commentNode.save();
};

Comments.prototype.removeComments = function (node) {
    var nodes = this.getComments(node);
    if (REC.exist(nodes)) {
        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i].content.indexOf("<table border=\"1\"> <tr><td>Nummer</td><td>Fehler</td></tr> ") != -1)
                nodes[i].remove();
        }
    }
};

Comments.prototype.getUniqueChildName = function (parentNode, prefix, date) {
    // we create a name looking like prefix-datetimestamp
    if (typeof date === 'undefined') {
        date = new Date();
    }
    name = prefix + "-" + date.getTime();
    // check that no child for the given name exists
    if (parentNode.childByNamePath(name) === null) {
        return name;
    }
    // if there is already a prefix-datetimestamp node then start looking for a
    // unique
    // name by appending random numbers - try a maximum of 100 times.
    var finalName = name + "_" + Math.floor(Math.random() * 1000);
    var count = 0;
    while (parentNode.childByNamePath(finalName) !== null && count < 100) {
        finalName = name + "_" + Math.floor(Math.random() * 1000);
        ++count;
    }
    return finalName;
};

Comments.prototype.getComments = function (node) {
    var commentsFolder = this.getCommentsFolder(node);
    if (commentsFolder !== null) {
        var elems = commentsFolder.childAssocs["cm:contains"];
        if (elems !== null) {
            return elems;
        }
    }
    // no comments found, return an empty array
    return [];
};

Comments.prototype.getCommentsFolder = function (node) {
    if (node.hasAspect("fm:discussable")) {
        var forumFolder = node.childAssocs["fm:discussion"][0];
        var topicFolder = forumFolder.childByNamePath(COMMENTS_TOPIC_NAME);
        return topicFolder;
    } else {
        return null;
    }
};

Comments.prototype.getOrCreateCommentsFolder = function (node) {
    var commentsFolder = this.getCommentsFolder(node);
    if (commentsFolder == null) {
        commentsFolder = commentService.createCommentsFolder(node);
    }
    return commentsFolder;
};

Comments.prototype.getCommentData = function (node) {
    var data = {};
    data.node = node;
    data.author = people.getPerson(node.properties["cm:creator"]);
    data.isUpdated = (node.properties["cm:modified"] - node.properties["cm:created"]) > 5000;
    return data;
};

Recognition.prototype.getContent = function (doc) {
    var erg;
    var trans = doc.transformDocument("text/plain");
    if (this.exist(trans)) {
        erg = trans.content + "";
        trans.remove();
        if (!this.exist(erg) || erg.length == 0) {
            throw "Dokumenteninhalt konnte nicht gefunden werden";
        }
    } else {
        throw "Dokumenteninhalt konnte nicht extrahiert werden";
    }
    return erg;
};

Recognition.prototype.convertPosition = function (text, start, end, desc, type) {
    var startRow = text.substring(0, start).split("\n").length - 1;
    var startCol = start - text.substring(0, start).lastIndexOf("\n") - 1;
    var endRow = text.substring(0, end).split("\n").length - 1;
    var endCol = end - text.substring(0, end).lastIndexOf("\n") - 1;
    return new Position(startRow, startCol, endRow, endCol, type, desc);
};

Recognition.prototype.handleUnexpected = function (box) {
    if (this.errors.length > 0) {
        var comment = "<table border=\"1\"> <tr><td>Nummer</td><td>Fehler</td></tr> ";
        for (var i = 0; i < this.errors.length; i++) {
            comment = comment + "<tr>";
            comment = comment + "<td>" + (i + 1) + "</td>";
            comment = comment + "<td>" + this.errors[i] + "</td>";
            comment = comment + "</tr>";
        }
        comment = comment + "</table>";
        this.log(TRACE, "adding Comment " + comment);
        var COM = new Comments();
        COM.addComment(this.currentDocument, comment);
        ({
            searchString: "",
            resolve: ArchivTyp.prototype.resolve,
            name: "Fehler",
            archivPosition: [{
                folder: box,
                link: false,
                resolve: ArchivPosition.prototype.resolve
            }]
        }).resolve("");
    }
};

Recognition.prototype.testRules = function (rules) {
    try {
        this.mess = "";
        this.currXMLName = new Array();
        XMLDOC.loadXML(rules);
        XMLDOC.parse();
        this.recognize(this.currentDocument, new XMLObject(XMLDOC.docNode));
    } catch (e) {
        for (var prop in e)
            this.log(ERROR, "property: " + prop + " value: [" + e[prop] + "]");
        this.errors.push("Fehler: " + e.toString());
    } finally {
        this.handleUnexpected(this.fehlerBox);
    }
    return;
};

Recognition.prototype.recognize = function (doc, rules, deb) {
    this.archivRoot = "";
    if (this.exist(rules.debugLevel))
        this.debugLevel = this.getDebugLevel(rules.debugLevel);
    this.log(INFORMATIONAL, "Debug Level is set to: " + this.print(this.debugLevel));
    if (this.exist(rules.maxDebugLength))
        this.maxDebugLength = parseInt(rules.maxDebugLength, 10);
    else
        this.maxDebugLength = 80;
    this.log(INFORMATIONAL, "Debug length is set to: " + this.maxDebugLength);
    this.currentDocument = doc;
    var docName = this.currentDocument.name;
    this.log(INFORMATIONAL, "Process Dokument " + docName);
    this.content = this.getContent(this.currentDocument);
    if (this.exist(rules.inBox))
        this.inbox = this.trim(rules.inBox);
    else
        this.inbox = "Inbox";
    this.log(INFORMATIONAL, "Inbox is located: " + this.inbox);
    if (this.exist(rules.errorBox))
        this.errorBox = this.trim(rules.errorBox);
    else
        this.errorBox = "Fehler";
    this.log(INFORMATIONAL, "ErrorBox is located: " + this.errorBox);
    if (this.exist(rules.duplicateBox))
        this.duplicateBox = this.trim(rules.duplicateBox);
    else
        this.duplicateBox = "Fehler/Doppelte";
    this.log(INFORMATIONAL, "DuplicateBox is located: " + this.duplicateBox);
    if (this.exist(rules.unknownBox))
        this.unknownBox = this.trim(rules.unknownBox);
    else
        this.unknownBox = "Unbekannt";
    this.log(INFORMATIONAL, "UnknownBox is located: " + this.unknownBox);
    if (this.exist(rules.archivRoot))
        this.archivRoot = this.trim(rules.archivRoot);
    else
        this.archivRoot = "Archiv/";
    this.log(INFORMATIONAL, "ArchivRoot is located: " + this.archivRoot);
    if (this.exist(rules.mandatory)) {
        var mnd = this.trim(rules.mandatory);
        this.mandatoryElements = mnd.split(",");
        this.log(INFORMATIONAL, "Mandatory Elements are: " + mnd);
    }
    this.fehlerBox = this.errorBox;
    var ruleFound = false;
    this.currentSearchItems = null;
    for (var i = 0; i < rules.archivTyp.length; i++) {
        ruleFound = new ArchivTyp(rules.archivTyp[i]).resolve();
        if (ruleFound)
            break;
    }
    if (!ruleFound) {
        this.errors.push("Unbekanntes Dokument, keine passende Regel gefunden!");
        this.fehlerBox = this.unknownBox;
    }
    this.log(INFORMATIONAL, "Process Dokument " + docName + " finished!");
    return;
};

Recognition.prototype.run = function () {
    if (typeof (space) != "undefined") {
        try {
            rules = this.getRules();
            this.recognize(document, rules, space.name != "Inbox");
        } catch (e) {
            for (var prop in e) {
                this.log(ERROR, "property: " + prop + " value: [" + e[prop] + "]");
            }
            this.errors.push("Fehler: " + e.toString());
        } finally {
            this.handleUnexpected(this.fehlerBox);
            logger.log(this.getMessage());
        }
    }
};

Recognition.prototype.set = function (rec) {
    REC = rec;
};

function Recognition() {
    if (typeof (currentDocument) == "undefined") {
        this.currentDocument = ({
            setContent: function (inhalt) {
                this.content = inhalt;
                this.properties = new Array();
            },
            name: 'WebScriptTest',
            childByNamePath: function () {
                return null;
            },
            hasAspect: function () {
                return false;
            },
            isSubType: function () {
                return true;
            },
            addAspect: function () {
            },
            addTag: function () {
            },
            checkout: function () {
                return this;
            },
            checkin: function () {
            },
            specializeType: function () {
            },
            createNode: function (name, typ) {
                return this;
            },
            displayPath: 'WebScriptTest/WebScriptTest',
            save: function () {
            },
            remove: function () {
            },
            properties: new Array(),
            transformDocument: function () {
                return this;
            },
            remove: function () {
            },
            move: function () {
                return true;
            }
        });
    }
    this.id = Math.random() * 100;
    this.debugLevel = INFORMATIONAL;
    this.mess = "";
    this.content = "";
    this.errors = new Array();
    this.results = new Array();
    this.archivRoot = "";
    this.inBox = "";
    this.duplicateBox = "";
    this.errorBox = "";
    this.unknownBox = "";
    this.fehlerBox = "";
    this.maxDebugLength = 0;
    this.mandatoryElements = new Array();
    //this.currentDocument = "";
    this.currentSearchItems = new Array();
    this.positions = new Array();
    this.currXMLName = new Array();
    // this.exp1 = new RegExp("[^\\s]");
    this.removedCharPos = new RemovedChar();
    this.showContent = false;
    this.rules = "";
    this.result = new Array();
    this.errors = new Array();
    this.results = new Array();
    this.positions = new PositionContainer();
};

const
    NONE = new DebugLevel(0, "NONE");
const
    ERROR = new DebugLevel(1, "ERROR");
const
    WARN = new DebugLevel(2, "WARN");
const
    INFORMATIONAL = new DebugLevel(3, "INFORMATIONAL");
const
    DEBUG = new DebugLevel(4, "DEBUG");
const
    TRACE = new DebugLevel(5, "TRACE");
const
    COMMENTS_TOPIC_NAME = "Comments";
const
    whitespace = "\n\n\t ";
const
    quotes = "\"'";
if (typeof (search) == "undefined") {
    var search = ({
        luceneSearch: function () {
            return new Array();
        }
    });
}
if (typeof (companyhome) == "undefined") {
    var companyhome = ({
        childByNamePath: function () {
            return window.parent.REC.currentDocument;
        }
    });
}
if (typeof (commentService) == "undefined") {
    var commentService = ({
        createCommentsFolder: function (node) {
            return window.parent.REC.currentDocument;
        }
    });
}
if (typeof (classification) == "undefined") {
    var classification = ({
        getRootCategories: function () {
            return this;
        },
        createRootCategory: function () {
            return this;
        },
        createSubCategory: function () {
            return this;
        },
        subCategories: this,
        name: "Testkategorie"
    });
}
var REC = new Recognition();
var XMLDOC = new XMLDoc();
REC.run();
