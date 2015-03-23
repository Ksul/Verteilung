/**
 * Created by klaus on 22.03.2015.
 */

CommentTest = TestCase("CommentTest");

CommentTest.prototype.setUp = function() {
    REC.init();
    var iBox = companyhome.childByNamePath("/Archiv/Inbox");
    REC.currentDocument = iBox.createNode("WebScriptTest", "my:archivContent");
    REC.currentDocument.setProperty("cm:title", "Test Title");
    REC.currentDocument.setProperty("my:person", "Klaus");
    REC.currentDocument.properties.content.write(new Content("Test"));
};

CommentTest.prototype.testAddComment = function() {
    var comments = new Comments();
    comments.addComment(REC.currentDocument, "Test");
    REC.currentDocument;
};