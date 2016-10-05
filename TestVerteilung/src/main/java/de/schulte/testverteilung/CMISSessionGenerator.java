package de.schulte.testverteilung;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;


/**
 * Created with IntelliJ IDEA.
 * Klasse erzeugt eine CMIS Session
 * User: Klaus Schulte (m500288)
 * Date: 08.01.14
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public class CMISSessionGenerator {

    private static Logger logger = Logger.getLogger(CMISSessionGenerator.class.getName());
        private String user;
        private String password;
        private String atomPubURL;
        private String repositoryName;

    public CMISSessionGenerator(String user, String password, String atomPubURL, String repositoryName) {
        this.user = user;
        this.password = password;
        this.atomPubURL = atomPubURL;
        this.repositoryName = repositoryName;
    }

    public String getUser() {
            return user;
        }
        public void setUser(String user) {
            this.user = user;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getAtomPubURL() {
            return atomPubURL;
        }
        public void setAtomPubURL(String atomPubURL){
            this.atomPubURL = atomPubURL;
        }
        public String getRepositoryName() {
            return repositoryName;
        }
        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
        }

    /**
     * baut die Session auf
     * @return
     */
    public Session generateSession() {

        Session session;

        try {

            // From: http://chemistry.apache.org/java/examples/example-create-session.html
            // default factory implementation
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<String, String>();

            // user credentials
            parameter.put(SessionParameter.USER, this.user);
            parameter.put(SessionParameter.PASSWORD, this.password);

            // connection settings
            parameter.put(SessionParameter.ATOMPUB_URL, this.atomPubURL);
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
//		parameter.put(SessionParameter.REPOSITORY_ID, this.repositoryName);
//		Session session = factory.createSession(parameter);

            // Set the alfresco object factory
            //parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

            // create session

            List<Repository> repositories = factory.getRepositories(parameter);
            session = repositories.get(0).createSession();

        } catch(Exception e) {
            logger.severe("Session konnte nicht aufgebaut werden: " + e.getMessage());
            throw e;
        }
        return session;
    }

    }

