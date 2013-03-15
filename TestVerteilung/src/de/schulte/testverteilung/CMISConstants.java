package de.schulte.testverteilung;

import javax.xml.namespace.QName;

public class CMISConstants {

	public static String NS_CMIS_RESTATOM = "http://docs.oasis-open.org/ns/cmis/restatom/200908/";

	public static String NS_CMIS_CORE = "http://docs.oasis-open.org/ns/cmis/core/200908/";

	public static final String CMIS_PREFIX = "cmis";
	
  public static final String CMISRA_PREFIX = "cmisra";

	public static QName CMISName(String localPart) {
		return new QName(NS_CMIS_CORE, localPart, CMIS_PREFIX);
	}
	
	public static QName CMISAtomName(String localPart) {
		return new QName(NS_CMIS_RESTATOM, localPart, CMISRA_PREFIX);
	}

	/* CMIS Element names */

	public static final QName REPOSITORY_INFO = CMISName("repositoryInfo");

	public static final QName REPOSITORY_ID = CMISName("repositoryId");

	public static final QName REPOSITORY_NAME = CMISName("repositoryName");

	public static final QName REPOSITORY_RELATIONSHIP = CMISName("repositoryRelationship");

	public static final QName REPOSITORY_DESCRIPTION = CMISName("repositoryDescription");

	public static final QName VENDOR_NAME = CMISName("vendorName");

	public static final QName PRODUCT_NAME = CMISName("productName");

	public static final QName PRODUCT_VERSION = CMISName("productVersion");

	public static final QName ROOT_FOLDER_ID = CMISName("rootFolderId");

	public static final QName CAPABILITIES = CMISName("capabilities");

	public static final QName CAPABILITY_MULTIFILING = CMISName("capabilityMultifiling");

	public static final QName CAPABILITY_UNFILING = CMISName("capabilityUnfiling");

	public static final QName CAPABILITY_VERSION_SPECIFIC_FILING = CMISName("capabilityVersionSpecificFiling");

	public static final QName CAPABILITY_PWC_UPDATEABLE = CMISName("capabilityPWCUpdateable");

	public static final QName CAPABILITY_PWC_SEARCHABLE = CMISName("capabilityPWCSearchable");

	public static final QName CAPABILITY_ALL_VERSIONS_SEARCHABLE = CMISName("capabilityAllVersionsSearchable");

	public static final QName CAPABILITY_QUERY = CMISName("capabilityQuery");

	public static final QName CAPABILITY_JOIN = CMISName("capabilityJoin");

	public static final QName CAPABILITY_FULL_TEXT = CMISName("capabilityFullText");

	public static final QName VERSIONS_SUPPORTED = CMISName("cmisVersionsSupported");

	public static final QName REPOSITORY_SPECIFIC_INFORMATION = CMISName("repositorySpecificInformation");

	public static final QName COLLECTION_TYPE = CMISName("collectionType");

	public static final QName DOCUMENT_TYPE = CMISName("documentType");

	public static final QName TYPE_ID = CMISName("typeId");

	public static final QName QUERY_NAME = CMISName("queryName");

	public static final QName DISPLAY_NAME = CMISName("displayName");

	public static final QName BASE_TYPE = CMISName("baseType");

	public static final QName BASE_TYPE_QUERY_NAME = CMISName("baseTypeQueryName");

	public static final QName PARENT_ID = CMISName("parentId");

	public static final QName DESCRIPTION = CMISName("description");

	public static final QName CREATABLE = CMISName("creatable");

	public static final QName FILEABLE = CMISName("fileable");

	public static final QName QUERYABLE = CMISName("queryable");

	public static final QName CONTROLLABLE = CMISName("controllable");

	public static final QName VERSIONABLE = CMISName("versionable");

	public static final QName OBJECT = CMISName("object");
	
	public static final QName PROPERTIES = CMISName("properties");

	public static final QName PROPERTY_STRING = CMISName("propertyString");

	public static final QName PROPERTY_DECIMAL = CMISName("propertyDecimal");

	public static final QName PROPERTY_INTEGER = CMISName("propertyInteger");

	public static final QName PROPERTY_BOOLEAN = CMISName("propertyBoolean");

	public static final QName PROPERTY_DATETIME = CMISName("propertyDateTime");

	public static final QName PROPERTY_URI = CMISName("propertyUri");

	public static final QName PROPERTY_ID = CMISName("propertyId");

	public static final QName PROPERTY_XML = CMISName("propertyXml");

	public static final QName PROPERTY_HTML = CMISName("propertyHtml");

	public static final QName NAME = CMISName("name");

	public static final QName VALUE = CMISName("value");

	public static final QName ALLOWABLE_ACTIONS = CMISName("allowableActions");

	/* CMIS Collection Types */

	public static final String COL_ROOT_CHILDREN = "rootchildren";

	public static final String COL_ROOT_DESCENDANTS = "rootdescendants";

	public static final String COL_UNFILED = "unfiled";

	public static final String COL_CHECKED_OUT = "checkedout";

	public static final String COL_TYPES_CHILDREN = "typeschildren";

	public static final String COL_TYPES_DESCENDANTS = "typesdescendants";

	public static final String COL_QUERY = "query";

	/* CMIS Link Types */

	public static final String LINK_REPOSITORY = "repository";

	public static final String LINK_LATEST_VERSION = "latestversion";

	public static final String LINK_PARENT = "parent";

	public static final String LINK_SOURCE = "source";

	public static final String LINK_TARGET = "target";

	public static final String LINK_TYPE = "type";

	public static final String LINK_ALLOWABLE_ACTIONS = "allowableactions";

	public static final String LINK_STREAM = "stream";

	public static final String LINK_PARENTS = "parents";

	public static final String LINK_CHILDREN = "children";

	public static final String LINK_DESCENDANTS = "descendants";

	public static final String LINK_ALL_VERSIONS = "allversions";

	public static final String LINK_RELATIONSHIPS = "relationships";

	public static final String LINK_POLICIES = "policies";
	
	public static final QName ATOMOBJECT = CMISAtomName("object");
}
