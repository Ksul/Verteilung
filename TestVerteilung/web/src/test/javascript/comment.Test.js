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
    assertTrue(REC.currentDocument.hasAspect("fm:discussable"));
    var discussion = REC.currentDocument.childAssocs["fm:discussion"][0];
    assertNotNull(discussion);
    var topic = discussion.children[0];
    assertNotNull(topic);
    var comment = topic.children[0];
    assertNotNull(comment);
    assertEquals("Test", comment.content);
};

CommentTest.prototype.testRemoveComment = function() {
    var comments = new Comments();
    comments.addComment(REC.currentDocument, "<table border=\"1\"> <tr><td>Nummer</td><td>Fehler</td></tr> ");
    comments.removeComments(REC.currentDocument);
    assertTrue(REC.currentDocument.hasAspect("fm:discussable"));
    var discussion = REC.currentDocument.childAssocs["fm:discussion"][0];
    assertNotNull(discussion);
    var topic = discussion.children[0];
    assertNotNull(topic);
    var comment = topic.children[0];
    assertNull(comment);
};