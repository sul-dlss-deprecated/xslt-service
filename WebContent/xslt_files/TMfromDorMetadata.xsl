<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns="http://www.loc.gov/METS/"
	xmlns:mets="http://www.loc.gov/METS/"
	xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:premisObject="info:lc/xmlns/premis-v2"
	xmlns:premisRights="info:lc/xmlns/premis-v2"
	xmlns:premisAgent="info:lc/xmlns/premis-v2"
	xmlns:premis="info:lc/xmlns/premis-v2"
	xmlns:appMD="http://cosimo.stanford.edu/appmd"
	xmlns:gbs="http://books.google.com/gbs/"
	xmlns:mix="http://www.loc.gov/mix/v10"
	xmlns:textMD="info:lc/xmlns/textMD-v3"
	>
	<xsl:output indent="yes" exclude-result-prefixes="#all"/>
	
	<xsl:variable name="schemaLocation" >
		<xsl:text>
			http://www.loc.gov/METS/
			  http://cosimo.stanford.edu/standards/mets/v1/mets-1-7.xsd
			http://www.loc.gov/mods/v3
			  http://cosimo.stanford.edu/standards/mods/v3/mods-3-3.xsd
			info:lc/xmlns/textMD-v3
			  http://cosimo.stanford.edu/standards/textmd/v3/textMD-v3.0.xsd
			http://cosimo.stanford.edu/appmd
			  http://cosimo.stanford.edu/standards/appmd/v0/appmd-0.1.xsd
			info:lc/xmlns/premis-v2
			  http://cosimo.stanford.edu/standards/premis/v2/premis-2.xsd
		</xsl:text>
	</xsl:variable>
	
	<!-- druid -->
	<xsl:variable name="druid" select="/objectMD/@druid"/>
	<xsl:variable name="normalized-druid" select="replace($druid,'dr:','dr_')"/>
	
	<!-- major subsections of the DOR metadata extract-->
	<xsl:variable name="DOR" select="/objectMD/dor[1]"/>
	<xsl:variable name="MODS" select="/objectMD/mods:mods[1]"/>
	<xsl:variable name="googleMETS" select="/objectMD/mets:mets[1]"/>
	<xsl:variable name="fileAdminMetadata" select="/objectMD/fileAdminMetadata[1]"/>
	<xsl:variable name="contentMetadata" select="/objectMD/contentMetadata[1]"/>
	
	<!-- barcode & catkey identifiers are stored in the dor datastream -->
	<xsl:variable name="barcode" select="$DOR/identifier[@name=&quot;barcode&quot;]/@value "/>
	<xsl:variable name="catkey" select="$DOR/identifier[@name=&quot;catkey&quot;]/@value "/>
	<xsl:variable name="gmADMID" select="concat('AMD_FILE_STANFORD_',$barcode,'.xml')"/>

	<xsl:variable name="metsLABEL">
		<xsl:text>GoogleBooks_PublicDomain_</xsl:text>
		<xsl:value-of select="$barcode"/>
	</xsl:variable>

	<!-- assemble the TM with warts and all -->
	<xsl:variable name="firstpass">
		<mets>
			<!-- 
			<xsl:if test="$objectUUID">
				<xsl:attribute name="OBJID" select="$objectUUID"/>
			</xsl:if>
			-->
			<xsl:attribute name="LABEL" select="$metsLABEL"/>
			<xsl:attribute name="TYPE" select="'SUL_SDR_transferManifest'"/>
			<xsl:attribute name="xsi:schemaLocation" select="normalize-space($schemaLocation)"/>
			<xsl:call-template name="metsHdr"/>
			<xsl:call-template name="dmdSecMODS"/>
			<xsl:call-template name="amdSecRights"/>
			<xsl:call-template name="googleAMD"/>
			<xsl:copy-of select="$fileAdminMetadata/*"/>
			<xsl:copy-of select="$contentMetadata/*"/>
		</mets>
	</xsl:variable>
	
	<!-- tidy up the output -->
	<xsl:template match="/">
		<xsl:apply-templates select="$firstpass"  mode="tidy"/>
	</xsl:template>

	<!-- metsHdr -->
	<xsl:template name="metsHdr" exclude-result-prefixes="#all">
		<xsl:variable name="metsRECORDSTATUS" select="'TM'"/>
		<!-- begin metsHdr -->
		<mets:metsHdr>
			<xsl:attribute name="CREATEDATE" select="current-dateTime()"/>
			<xsl:attribute name="RECORDSTATUS" select="$metsRECORDSTATUS"/>
			<mets:agent ROLE="CREATOR" TYPE="ORGANIZATION">
				<mets:name>Google</mets:name>
			</mets:agent>
			<mets:agent OTHERTYPE="SOFTWARE" ROLE="CREATOR" TYPE="OTHER">
				<mets:name>DOR_V0.1</mets:name>
			</mets:agent>
			<xsl:if test="$barcode">
				<mets:altRecordID TYPE="SUL-Barcode">
					<xsl:value-of select="$barcode"/>
				</mets:altRecordID>
			</xsl:if>
			<mets:altRecordID TYPE="SUL_OBJ_DRUID">
				<xsl:value-of select="$druid"/>
			</mets:altRecordID>
		</mets:metsHdr>
	</xsl:template>
	
	<!-- MODS dmdSec -->
	<xsl:template name="dmdSecMODS">
		<xsl:variable name="modsLABEL">GoogleBook, Stanford Public Domain Google Books</xsl:variable>
		<!-- begin dmdSec -->
		<mets:dmdSec>
			<xsl:attribute name="ID" select="concat('DMD_',$normalized-druid,'_MODS')"/>	
			<mets:mdWrap>
				<xsl:attribute name="MDTYPE" select="'MODS'"/>
				<xsl:attribute name="LABEL" select="$modsLABEL"/>
				<mets:xmlData>
					<xsl:copy-of select="$MODS"/>
				</mets:xmlData>
			</mets:mdWrap>
		</mets:dmdSec>
	</xsl:template>
	
	<!-- Rights amdSec -->
	<xsl:template name="amdSecRights">
		<mets:amdSec ID="AMD_ObjectLevel_01">
			<mets:rightsMD>
				<xsl:attribute name="ID" select="concat('RMD_',$normalized-druid)"/>
				<xsl:attribute name="CREATED" select="current-dateTime()"/>
				<xsl:attribute name="STATUS" select="'DRAFT'"/>				
				<mets:mdWrap>
					<xsl:attribute name="LABEL" select="$metsLABEL"/>
					<xsl:attribute name="MDTYPE" select="'PREMIS'"/>				
					<mets:xmlData>
						<premisRights:premisRights >
							<premisRights:rightsExtension>
							</premisRights:rightsExtension>
							<premisRights:rightsStatement>
								<premisRights:rightsStatementIdentifier>
									<premisRights:rightsStatementIdentifierType>SDR_Access_Phase_2</premisRights:rightsStatementIdentifierType>
									<premisRights:rightsStatementIdentifierValue>SDR-ServiceStatement-GoogleBooks-V01</premisRights:rightsStatementIdentifierValue>
								</premisRights:rightsStatementIdentifier>
								<premisRights:rightsBasis>copyright</premisRights:rightsBasis>
								<premisRights:copyrightInformation>
									<premisRights:copyrightStatus>Public Domain</premisRights:copyrightStatus>
									<premisRights:copyrightJurisdiction>us</premisRights:copyrightJurisdiction>
									<premisRights:copyrightStatusDeterminationDate>20090202</premisRights:copyrightStatusDeterminationDate>
									<premisRights:copyrightNote>Copyright determined by algorithmic examination of format type, and publication information from pertinent MARC21 data fields (format, 008, 542) found in the library catalog bibliographic record for this barcode and catalog key.</premisRights:copyrightNote>
								</premisRights:copyrightInformation>
								<premisRights:rightsGranted>
									<premisRights:act>replicate</premisRights:act>
									<premisRights:restriction>For SDR preservation service operations</premisRights:restriction>
									<premisRights:termOfGrant>
										<premisRights:startDate>20081008</premisRights:startDate>
										<premisRights:endDate>20131008</premisRights:endDate>
									</premisRights:termOfGrant>
									<premisRights:rightsGrantedNote>Automatic renewal under the same terms for subsequent 5 year periods.  Changes to the agreement can be made at the time of renewal or any time with 90 days prior notice.</premisRights:rightsGrantedNote>
								</premisRights:rightsGranted>
								<premisRights:rightsGranted>
									<premisRights:act>disseminate</premisRights:act>
									<premisRights:restriction>Third party access must be authorized by Owner prior to dissemination</premisRights:restriction>
									<premisRights:termOfGrant>
										<premisRights:startDate>20081008</premisRights:startDate>
										<premisRights:endDate>20121008</premisRights:endDate>
									</premisRights:termOfGrant>
									<premisRights:rightsGrantedNote>Automatic renewal under the same terms for subsequent 5 year periods.  Changes to the agreement can be made at the time of renewal or any time with 90 days prior notice.</premisRights:rightsGrantedNote>
								</premisRights:rightsGranted>
								<premisRights:linkingObjectIdentifier>
									<premisRights:linkingObjectIdentifierType>SDR_UUID</premisRights:linkingObjectIdentifierType>
									<premisRights:linkingObjectIdentifierValue>library_stanford_edu_621dbe959a3a11ddbb9639e00e4b1d36</premisRights:linkingObjectIdentifierValue>
								</premisRights:linkingObjectIdentifier>
								<premisRights:linkingAgentIdentifier>
									<premisRights:linkingAgentIdentifierType>SDR_Data_Owner</premisRights:linkingAgentIdentifierType>
									<premisRights:linkingAgentIdentifierValue>The Board of Trustees of the Leland Stanford Junior University</premisRights:linkingAgentIdentifierValue>
								</premisRights:linkingAgentIdentifier>       
							</premisRights:rightsStatement>
						</premisRights:premisRights>
					</mets:xmlData>
				</mets:mdWrap>
			</mets:rightsMD>
		</mets:amdSec>	
	</xsl:template>
	
	<!-- ######################################################### -->
	<!-- Filter the amdSec from the google METS file               -->
	<!-- ######################################################### -->
	
	<xsl:template name="googleAMD">
			<mets:amdSec>
				<xsl:attribute name="ID" select="concat('AMD_Google_',$barcode)"/>
				<xsl:apply-templates select="$googleMETS/mets:amdSec/*" mode="googleMETS"/>
			</mets:amdSec>
	</xsl:template>
	
	<!-- The identity transform copies everything from input tree to output tree except nodes that match criteria specified in other templates -->
	<xsl:template match="@*|node()" mode="googleMETS">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="googleMETS" exclude-result-prefixes="#all"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- Add Google qualifier in front of  attributes relating to calibration targets -->
	<xsl:template match="mets:techMD/mets:mdRef[@LABEL='calibration target']" mode="googleMETS">
		<xsl:copy exclude-result-prefixes="#all">
			<xsl:for-each select="@*">
				<xsl:choose>
					<xsl:when test=".[name()='LABEL']">
						<xsl:attribute name="LABEL">
							<xsl:value-of select="concat('Google ',.)"/>
						</xsl:attribute>
					</xsl:when>
					<xsl:when test=".[name()='OTHERLOCTYPE']">
						<xsl:attribute name="OTHERLOCTYPE">
							<xsl:value-of select="concat('GOOGLE_' ,.)"/>
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	
	<!-- Add namespace info to PREMIS elements that lack it -->
	<xsl:template match="mets:digiprovMD/mets:mdWrap[@MDTYPE='PREMIS']/mets:xmlData" mode="googleMETS">
		<xsl:copy exclude-result-prefixes="#all">
			<xsl:apply-templates mode="premis"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*" mode="premis">
		<xsl:element  name="premis:{local-name(.)}" namespace="info:lc/xmlns/premis-v2">
			<xsl:apply-templates mode="premis"/>	
		</xsl:element>
	</xsl:template>
	
	<!-- Remove all techMD elements that relate to "page tags" -->
	<xsl:template match="mets:techMD[child::mets:mdWrap[@LABEL='page tags']]"  mode="googleMETS"/>
	
	<!-- ######################################################### -->
	<!-- Tidy up prefixes, namespace declarations, schemaLocations -->
	<!-- ######################################################### -->
	
	<!-- for METS root element, be sure to copy all namespace declarations and attributes -->

	<xsl:template match="mets:mets" mode="tidy">
		<xsl:copy > 
			<xsl:for-each select="@*">
				<xsl:copy/>
			</xsl:for-each>
			<!-- Then use other rules for the children -->
			<xsl:apply-templates  mode="tidy"/>
		 </xsl:copy > 
	</xsl:template>
	
	<!-- Default is to copy all elements and retain existing namespace prefixes -->
	<xsl:template match="*"  mode="tidy">
		<xsl:element name="{name(.)}"	namespace="{namespace-uri(.)}">
			<xsl:apply-templates select="@*|node()"  mode="tidy"/>
		</xsl:element>
	</xsl:template>
	
	<!-- Strip out any  schemaLocation attributes not at the root level -->
	<xsl:template match="@xsi:schemaLocation" mode="tidy"/>
	
	<!-- Make sure all METS elements have no prefix (mets is the default URI) -->
	<xsl:template match="mets:*"  mode="tidy">
		<xsl:element
			name="{local-name(.)}"
			namespace="{namespace-uri(.)}">
			<xsl:apply-templates select="@*|node()"  mode="tidy"/>
		</xsl:element>
	</xsl:template>
	
	<!-- Make sure all MODS elements have a "mods:" prefix and no extraneous attributes or namespace declarations -->
	<xsl:template match="mods:*"  mode="tidy">
		<xsl:element
			name="mods:{local-name(.)}"
			namespace="{namespace-uri(.)}">
			<xsl:apply-templates select="@*|node()"  mode="tidy"/>
		</xsl:element>
	</xsl:template>
	
	<!-- Copy any other attributes verbatum -->
	<xsl:template match="@*|text()"  mode="tidy">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"  mode="tidy"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- Auxiliary files are missing.  remove these elements -->
	<xsl:template match="mets:amdSec[@ID=$gmADMID]" mode="tidy"/>
	<xsl:template match="mets:fptr[parent::mets:div[@ID='Top01']]" mode="tidy"/>
	
	<xsl:template match="mets:FLocat" mode="tidy">
		<xsl:variable name="filename" select="replace(@xlink:href,':','_')"/>
		<FLocat>
			<xsl:for-each select="@*">
				<xsl:choose>
					<xsl:when test="name(.)='xlink:href'" >
						<xsl:attribute name="href" namespace="http://www.w3.org/1999/xlink">
							<xsl:value-of select="$filename"/>
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</FLocat>
	</xsl:template>
	
	<xsl:template match="@ID" mode="tidy">
		<xsl:choose>
			<xsl:when test="contains(.,':')">
					<xsl:variable name="newID" select="replace(.,':','_')"/>
					<xsl:attribute name="ID" select="$newID"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
		
	<xsl:template match="@DMDID" mode="tidy">
		<xsl:choose>
			<xsl:when test="contains(.,'original_object_id')">
				<xsl:variable name="newID" select="concat('DMD_',$normalized-druid,'_MODS')"/>
				<xsl:attribute name="DMDID" select="$newID"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="@ADMID" mode="tidy">
		<xsl:choose>
			<xsl:when test="contains(.,':')">
				<xsl:variable name="newID" select="replace(.,':','_')"/>
				<xsl:attribute name="ADMID" select="$newID"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="@FILEID" mode="tidy">
		<xsl:choose>
			<xsl:when test="contains(.,':')">
				<xsl:variable name="newID" select="replace(.,':','_')"/>
				<xsl:attribute name="FILEID" select="$newID"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="mets:mdWrap[@MDTYPE='PREMIS']" mode="tidy">
		<xsl:variable name="filename" select="replace(@LABEL,':','_')"/>
		<mdWrap>
			<xsl:attribute name="MDTYPE" select="'PREMIS'"/>
			<xsl:attribute name="LABEL" select="$filename"/>
			<xsl:apply-templates select="node()" mode="tidy"/>
		</mdWrap>
	</xsl:template>
	
	<xsl:template match="premisObject:originalName" mode="tidy">
		<xsl:variable name="filename" select="replace(.,':','_')"/>
		<xsl:copy>
			<xsl:value-of select="$filename"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="premisObject:objectIdentifierValue" mode="tidy">
		<xsl:variable name="filename" select="replace(.,':','_')"/>
		<xsl:copy>
			<xsl:value-of select="$filename"/>
		</xsl:copy>
	</xsl:template>
	
	
</xsl:stylesheet>
