package de.schulte.testverteilung;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.Response.ResponseType;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class AlfrescoConnector {

    private static String CMIS = "cmis";

    private String server;

    private String username;

    private String password;

    private String proxyHost;

    private String proxyPort;

    private Credentials credentials;

    public AlfrescoConnector(String server, String username, String password, String proxyHost, String proxyPort,
                             Credentials credentials) {
        super();
        this.server = server;
        this.username = username;
        this.password = password;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.credentials = credentials;
    }

    public String getTicket() {

        {
            String _ticket = "";

            URL url;
            HttpURLConnection connection = null;
            try {

                String urlParameters = "{ \"username\" : \"" + this.username + "\", \"password\" : \"" + this.password + "\" }";

                // Create connection
                url = new URL(this.server + "/alfresco/service/api/login");
                if (proxyHost != null && proxyPort != null) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                    connection = (HttpURLConnection) url.openConnection(proxy);
                } else
                    connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                // Send request
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                // Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                String _jsonResponse = response.toString();

                JSONObject _jsonResponseObject = new JSONObject(_jsonResponse);
                JSONObject jsonDataObject = new JSONObject(_jsonResponseObject.get("data").toString());
                _ticket = jsonDataObject.get("ticket").toString();

            } catch (Exception e) {

                e.printStackTrace();
                return null;

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
            return _ticket;
        }
    }

    /**
     * liefert einen Knoten
     * @param   cmisQuery die Query als CMIS String
     * @return            der Knoten
     * @throws VerteilungException
     */
    public AlfrescoResponse getNode(final String cmisQuery) throws VerteilungException {

        try {

            final AbderaClient client = new AbderaClient();

            if (proxyHost != null && proxyPort != null)
                client.setProxy(proxyHost, Integer.parseInt(proxyPort));

            // Authentication header
            final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
            final RequestOptions options = new RequestOptions();
            options.setHeader("Authorization", "Basic " + encodedCredential);
            options.setContentType("application/cmisquery+xml");

            String uri = "";
            try {
                uri = server + "/alfresco/service/cmis/query?q=" + URLEncoder.encode(cmisQuery, "UTF-8") + "&searchAllVersions=false&skipCount=0&maxItems=1";
            } catch (UnsupportedEncodingException e) {
                System.out.println(e.getLocalizedMessage());
                e.printStackTrace();
                throw new VerteilungException(e.getLocalizedMessage());
            }
            final ClientResponse clientResponse = client.get(uri, options);

            final AlfrescoResponse alfResponse = parseResponse(clientResponse);

            if (ResponseType.SUCCESS == clientResponse.getType()) {
                final Document<Feed> document = clientResponse.getDocument();
                if (document != null) {
                    alfResponse.setDocument((Document<Feed>) document.clone());
                }
            } else {
                throw new VerteilungException(alfResponse.getStackTrace());
            }

            clientResponse.release();
            return alfResponse;
        } catch (RuntimeException e) {
            throw new VerteilungException(e.getLocalizedMessage());
        }
    }

    public AlfrescoResponse getContent(final String documentId) throws VerteilungException {

        final AbderaClient client = new AbderaClient();
        if (proxyHost != null && proxyPort != null)
            client.setProxy(proxyHost, Integer.parseInt(proxyPort));

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(this.username + ":" + this.password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = server + "/alfresco/service/cmis/i/" + documentId.substring(documentId.lastIndexOf('/') + 1) + "/content";

        final ClientResponse clientResponse = client.get(uri, options);
        AlfrescoResponse alfResponse = null;
        try {
            byte[] bytes = IOUtils.toByteArray(clientResponse.getInputStream());
            alfResponse = parseResponse(clientResponse);
            alfResponse.setContent(bytes);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
            throw new VerteilungException(e.getLocalizedMessage());
        }
        clientResponse.release();

        return alfResponse;

    }

    public String moveDocument(final String documentId, final String destinationId) throws VerteilungException {

        String ret = "";

        URL url;
        HttpURLConnection connection = null;
        try {

            // Create connection
            final String uri = server + "/alfresco/service/slingshot/doclib/action/move-to/node/workspace/SpacesStore/"
                    + destinationId.substring(destinationId.lastIndexOf('/') + 1);
            url = new URL(uri);
            if (proxyHost != null && proxyPort != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else
                connection = (HttpURLConnection) url.openConnection();

            final String encodedCredential = Base64Coder.encodeString(username + ":" + password);
            final String body = "{\"nodeRefs\":[\"" + documentId + "\"]}";
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", "" + body.length());
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setRequestProperty("Authorization", "BASIC " + encodedCredential);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();

            // Get Response
            if (connection.getResponseCode() == HttpServletResponse.SC_OK) {
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                String _jsonResponse = response.toString();

                JSONObject json = new JSONObject(_jsonResponse);
                ret = json.toString();
            } else
                throw new VerteilungException("Status: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
            throw new VerteilungException(e.getLocalizedMessage());
        } catch (JSONException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
            throw new VerteilungException(e.getLocalizedMessage());
            //    } catch (Exception e) {
            //      System.out.println(e.getLocalizedMessage());
            //      e.printStackTrace();
            //      JSONObject o = new JSONObject();
            //      try {
            //        o.put("error", e.getLocalizedMessage());
            //      } catch (JSONException e1) {
            //        // TODO Auto-generated catch block
            //        e1.printStackTrace();
            //      }
            //      return o.toString();

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
        return ret;
    }

    /**
     * STATUS 200 : SUCCESS STATUS 404 : CLIENT_ERROR - File not found
     *
     * @param checkedOutFileId
     * @param isMajorVersion
     * @param checkinComments
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse checkin(final String checkedOutFileId, final boolean isMajorVersion, String checkinComments)
            throws IOException {

        // Replace white spaces from the URI
        checkinComments = checkinComments.replace(" ", "%20");

        // Create an empty Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();

        final AbderaClient client = new AbderaClient();
        if (proxyHost != null && proxyPort != null)
            client.setProxy(proxyHost, Integer.parseInt(proxyPort));
        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(username + ":" + password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = server + "/alfresco/service/cmis/pwc/i/" + checkedOutFileId + "?checkin=true&major="
                + isMajorVersion + "&checkinComment=" + checkinComments;

        final ClientResponse clientResponse = client.put(uri, entry, options);
        final AlfrescoResponse alfResponse = parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 200 : SUCCESS STATUS 500 : Internal server error - May be caused by:
     * Duplicate child name not allowed (you are trying to use the original file
     * name instead of the checked-out file name) Example: original file name:
     * demo.txt checked-out file name: demo (Working copy).txt
     *
     * @param content
     * @param description
     * @param checkedOutFileId
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse updateContent(final String content, final String description, final String checkedOutFileId)
            throws IOException {

        // Build the input Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();

        entry.setSummary(description);
        entry.setContent(content);

        final ExtensibleElement objElement = (ExtensibleElement) entry.addExtension(CMISConstants.NS_CMIS_CORE, "object",
                CMIS);
        final ExtensibleElement propsElement = objElement.addExtension(CMISConstants.NS_CMIS_CORE, "properties", CMIS);
        final ExtensibleElement stringElement = propsElement.addExtension(CMISConstants.NS_CMIS_CORE, "propertyId", CMIS);
        stringElement.setAttributeValue("cmis:name", "ObjectTypeId");
        final Element valueElement = stringElement.addExtension(CMISConstants.NS_CMIS_CORE, "value", CMIS);
        valueElement.setText("document"); // This could be changed as an input
        // parameter

        final AbderaClient client = new AbderaClient();
        if (proxyHost != null && proxyPort != null)
            client.setProxy(proxyHost, Integer.parseInt(proxyPort));
        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(username + ":" + password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);
        options.setContentType("application/atom+xml");
        final String uri = server + "/alfresco/service/cmis/i/" + checkedOutFileId;

        final ClientResponse clientResponse = client.put(uri, entry, options);
        final AlfrescoResponse alfResponse = parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 200 : SUCCESS STATUS 500 : Internal server error - May be caused by:
     * Duplicate child name not allowed (you are trying to use the original file
     * name instead of the checked-out file name) Example: original file name:
     * demo.txt checked-out file name: demo (Working copy).txt
     *
     * @param uriString
     * @param description
     * @param mimeType
     * @param checkedOutFileId
     * @return AlfrescoResponse
     * @throws IOException
     * @throws URISyntaxException
     */
    public AlfrescoResponse updateCheckedOutFile(final String uriString, final String description, final String mimeType,
                                                 final String checkedOutFileId) throws IOException, URISyntaxException {

        final URI uri = new URI(uriString);

        final File fileToUpload = new File(uri);

        final InputStream is = new FileInputStream(fileToUpload);

        // final byte[] fileBytes = getBytesFromFile(fileToUpload);
        // char[] encodedFile = Base64Coder.encode(fileBytes); This will cause messy
        // code issue.
        // final String encodedFileString = new String(fileBytes);

        // Build the input Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();
        entry.setSummary(description);
        entry.setContent(is, mimeType);

        final ExtensibleElement objElement = (ExtensibleElement) entry.addExtension(CMISConstants.NS_CMIS_CORE, "object",
                CMIS);
        final ExtensibleElement propsElement = objElement.addExtension(CMISConstants.NS_CMIS_CORE, "properties", CMIS);
        final ExtensibleElement stringElement = propsElement.addExtension(CMISConstants.NS_CMIS_CORE, "propertyId", CMIS);
        stringElement.setAttributeValue("cmis:name", "ObjectTypeId");
        final Element valueElement = stringElement.addExtension(CMISConstants.NS_CMIS_CORE, "value", CMIS);
        valueElement.setText("document"); // This could be changed as an input
        // parameter

        final AbderaClient client = new AbderaClient();

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(username + ":" + password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String url = server + "/alfresco/s/cmis/i/" + checkedOutFileId;

        final ClientResponse clientResponse = client.put(url, entry, options);
        final AlfrescoResponse alfResponse = parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    public AlfrescoResponse checkout(final String fileId) throws IOException {

        // Build the input Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();

        final ExtensibleElement objElement = (ExtensibleElement) entry.addExtension(CMISConstants.NS_CMIS_RESTATOM,
                "object", CMISConstants.CMISRA_PREFIX);
        final ExtensibleElement propsElement = objElement.addExtension(CMISConstants.NS_CMIS_CORE, "properties", CMIS);
        final ExtensibleElement stringElement = propsElement.addExtension(CMISConstants.NS_CMIS_CORE, "propertyId", CMIS);
        stringElement.setAttributeValue("propertyDefinitionId", "cmis:objectId");
        final Element valueElement = stringElement.addExtension(CMISConstants.NS_CMIS_CORE, "value", CMIS);
        valueElement.setText("workspace://SpacesStore/" + fileId);

        // Post it
        final AbderaClient client = new AbderaClient();
        if (proxyHost != null && proxyPort != null)
            client.setProxy(proxyHost, Integer.parseInt(proxyPort));
        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(username + ":" + password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = server + "/alfresco/service/cmis/checkedout";

        final ClientResponse clientResponse = client.post(uri, entry, options);
        final AlfrescoResponse alfResponse = parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    public AlfrescoResponse downloadFileById(final String fileId, final String outputFileFolder,
                                             final String outputFileName) throws IOException {

        final AbderaClient client = new AbderaClient();
        if (proxyHost != null && proxyPort != null)
            client.setProxy(proxyHost, Integer.parseInt(proxyPort));
        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(username + ":" + password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = server + "/alfresco/service/cmis/i/" + fileId + "/content";

        final ClientResponse clientResponse = client.get(uri, options);
        final AlfrescoResponse alfResponse = parseResponseAsOutputFile(clientResponse, outputFileFolder, outputFileName);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * Uploads the file at the given path in Alfresco STATUS 201 : Created STATUS
     * 404 : Not found - May be caused by: Destination directory not found STATUS
     * 500 : Internal server error - May be caused by: File already exist
     *
     * @param fileAbsolutePath
     * @param description
     * @param mimeType
     * @param destinationFolder
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse uploadFileByPath(final String fileAbsolutePath, final String fileName,
                                             final String description, final String mimeType, final String destinationFolder) throws IOException {
        if ("text/plain".equalsIgnoreCase(mimeType)) {
            // Get file content
            final File fileToUpload = new File(fileAbsolutePath);
            final byte[] fileBytes = getBytesFromFile(fileToUpload);
            // Upload file
            return uploadFile(fileBytes, fileName, description, mimeType, destinationFolder);
        } else {
            // Upload stream
            final FileInputStream inputStream = new FileInputStream(fileAbsolutePath);
            return uploadFile(inputStream, fileName, description, mimeType, destinationFolder);
        }

    }

    /**
     * Uploads the given file bytes to an Alfresco file<br/>
     * STATUS 201 : Created STATUS 404 : Not found - May be caused by: Destination
     * directory not found STATUS 500 : Internal server error - May be caused by:
     * File already exist
     *
     * @param fileBytes
     * @param fileName
     * @param description
     * @param mimeType
     * @param destinationFolder
     * @return
     * @throws IOException
     */
    private AlfrescoResponse uploadFile(final byte[] fileBytes, final String fileName, final String description,
                                        final String mimeType, final String destinationFolder) throws IOException {
        // String encodedFileString = new String(fileBytes);
        String encodedFileString = null;
        if (fileBytes != null) {
            encodedFileString = new String(fileBytes);
        }
        // Build the input Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();
        entry.setTitle(fileName);
        entry.setSummary(description);
        entry.setContent(encodedFileString, mimeType);

        final ExtensibleElement objElement = (ExtensibleElement) entry.addExtension(CMISConstants.NS_CMIS_CORE, "object",
                CMIS);
        final ExtensibleElement propsElement = objElement.addExtension(CMISConstants.NS_CMIS_CORE, "properties", CMIS);
        final ExtensibleElement stringElement = propsElement.addExtension(CMISConstants.NS_CMIS_CORE, "propertyId", CMIS);
        stringElement.setAttributeValue("cmis:name", "ObjectTypeId");
        final Element valueElement = stringElement.addExtension(CMISConstants.NS_CMIS_CORE, "value", CMIS);
        valueElement.setText("document"); // This could be changed as an input
        // parameter

        final AbderaClient client = new AbderaClient();
        if (proxyHost != null && proxyPort != null)
            client.setProxy(proxyHost, Integer.parseInt(proxyPort));
        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(username + ":" + password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = server + "/alfresco/service/cmis/p/" + destinationFolder + "/children";

        final ClientResponse clientResponse = client.post(uri, entry, options);
        final AlfrescoResponse alfResponse = parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * upload non test/plain MimeType file
     *
     * @param inputStream
     * @param fileName
     * @param description
     * @param mimeType
     * @param destinationFolder
     * @return
     */
    private AlfrescoResponse uploadFile(final InputStream inputStream, final String fileName, final String description,
                                        final String mimeType, final String destinationFolder) {
        // Build the input Atom Entry
        final Abdera abdera = new Abdera();
        final Entry entry = abdera.newEntry();
        entry.setTitle(fileName);
        entry.setSummary(description);
        entry.setContent(inputStream, mimeType);

        final ExtensibleElement objElement = (ExtensibleElement) entry.addExtension(CMISConstants.NS_CMIS_CORE, "object",
                CMIS);
        final ExtensibleElement propsElement = objElement.addExtension(CMISConstants.NS_CMIS_CORE, "properties", CMIS);
        final ExtensibleElement stringElement = propsElement.addExtension(CMISConstants.NS_CMIS_CORE, "propertyId", CMIS);
        stringElement.setAttributeValue("cmis:name", "ObjectTypeId");
        final Element valueElement = stringElement.addExtension(CMISConstants.NS_CMIS_CORE, "value", CMIS);
        valueElement.setText("document"); // This could be changed as an input
        // parameter

        final AbderaClient client = new AbderaClient();
        if (proxyHost != null && proxyPort != null)
            client.setProxy(proxyHost, Integer.parseInt(proxyPort));

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(username + ":" + password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = server + "/alfresco/service/cmis/p/" + destinationFolder + "/children";

        final ClientResponse clientResponse = client.post(uri, entry, options);
        final AlfrescoResponse alfResponse = parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }

    /**
     * STATUS 200: SUCCESS
     *
     * @param folderPath
     * @param byPath
     * @return AlfrescoResponse
     * @throws IOException
     */
    public AlfrescoResponse listFolder(final String folderPath, final boolean byPath) throws IOException {
        final AbderaClient client = new AbderaClient();

        if (proxyHost != null && proxyPort != null)
            client.setProxy(proxyHost, Integer.parseInt(proxyPort));

        // Authentication header
        final String encodedCredential = Base64Coder.encodeString(username + ":" + password);
        final RequestOptions options = new RequestOptions();
        options.setHeader("Authorization", "Basic " + encodedCredential);

        final String uri = server + "/alfresco/s/cmis/" + (byPath ? "s/":"i/") + folderPath + "/children";

        final ClientResponse clientResponse = client.get(uri, options);
        final AlfrescoResponse alfResponse = parseResponseWithDocument(clientResponse);
        clientResponse.release();
        return alfResponse;
    }



    /**
     * Parse ClientResponse
     *
     * @param response
     * @return AlfrescoResponse
     */
    @SuppressWarnings("unchecked")
    private AlfrescoResponse parseResponseWithDocument(final ClientResponse response) {

        final AlfrescoResponse alfResponse = parseResponse(response);

        if (ResponseType.SUCCESS == response.getType()) {
            final Document<Element> document = response.getDocument();
            if (document != null) {
                alfResponse.setDocument((Document<Element>) document.clone());
            }
        }
        return alfResponse;
    }

    /**
     * Parse ClientResponse
     *
     * @param response
     * @return AlfrescoResponse
     */
    private AlfrescoResponse parseResponse(final ClientResponse response) {

        AlfrescoResponse alfResponse;

        String responseType = "";
        if (response.getType() != null) {
            responseType = response.getType().toString();
        }
        final String statusCode = String.valueOf(response.getStatus());
        final String statusText = response.getStatusText();

        alfResponse = new AlfrescoResponse(responseType, statusCode, statusText);

        if (ResponseType.SUCCESS != response.getType()) {
            // printStackTrace
            InputStream inputStream;
            try {
                inputStream = response.getInputStream();

                final char[] buffer = new char[0x10000];
                final StringBuilder stackTrace = new StringBuilder();
                final Reader in = new InputStreamReader(inputStream, "UTF-8");
                int read;
                do {
                    read = in.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        stackTrace.append(buffer, 0, read);
                    }
                } while (read >= 0);
                in.close();

                alfResponse.setStackTrace(stackTrace.toString());
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return alfResponse;
    }

    /**
     * Parse ClientResponse
     *
     * @param response
     * @return
     * @throws IOException
     */
    private AlfrescoResponse parseResponseAsOutputFile(final ClientResponse response, final String outputFileFolder,
                                                       final String outputFileName) throws IOException {

        final AlfrescoResponse alfResponse = parseResponse(response);

        if (ResponseType.SUCCESS == response.getType()) {
            if (response.getContentLength() > 0) {
                final InputStream inputStream = response.getInputStream();
                final File responseFile = new File(outputFileFolder + outputFileName);
                final OutputStream outputStream = new FileOutputStream(responseFile);
                final byte buf[] = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();
            }
        }
        return alfResponse;
    }

    private byte[] getBytesFromFile(final File file) throws IOException {
        final InputStream is = new FileInputStream(file);

        // Get the size of the file
        final long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
            throw new IOException("File too long");
        }

        // Create the byte array to hold the data
        final byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }



}
