package de.schulte.testverteilung;

import java.io.InputStream;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;

/**
 * 
 * @author Jordi Anguela
 *
 */
public class AlfrescoResponse {

  private String responseType = "";
  private String statusCode   = "";
  private String statusText   = "";
  private Document document; // Entry or Feed
  private String stackTrace   = "";
  private byte[] content = null;

  public AlfrescoResponse() {
  }

  public AlfrescoResponse(String type, String code, String text) {
    responseType = type;
    statusCode = code;
    statusText = text;
  }

  public String getResponseType() {
    return responseType;
  }

  public void setResponseType(String responseType) {
    this.responseType = responseType;
  }

  public String getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(String statusCode) {
    this.statusCode = statusCode;
  }

  public String getStatusText() {
    return statusText;
  }

  public void setStatusText(String statusText) {
    this.statusText = statusText;
  }

  public Document getDocument() {
    return document;
  }

  public void setDocument(Document document) {
    this.document = document;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }

  @Override
  public String toString() {
    return "AlfrescoResponse [responseType=" + responseType + ", statusCode="
        + statusCode + ", statusText=" + statusText + ", document=" + document
        + ", stackTrace=" + stackTrace + "]";
  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

}
