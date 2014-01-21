package de.schulte.testverteilung;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 07.01.14
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class NodeProperties {

    private String propertyTyp;

    private String propertyId;

    private String displayName;

    private String queryName;

    private String value;

    public NodeProperties(String propertyTyp, String propertyId, String displayName, String queryName, String value) {
        this.propertyTyp = propertyTyp;
        this.propertyId = propertyId;
        this.displayName = displayName;
        this.queryName = queryName;
        this.value = value;
    }

    public String getPropertyTyp() {
        return propertyTyp;
    }

    public void setPropertyTyp(String propertyTyp) {
        this.propertyTyp = propertyTyp;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "NodeProperties{" +
                "propertyTyp='" + propertyTyp + '\'' +
                ", propertyId='" + propertyId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", queryName='" + queryName + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
