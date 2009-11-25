<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
	xmlns:mods="http://www.loc.gov/mods/v3" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:date="http://exslt.org/dates-and-times" 
	xmlns:marc="http://www.loc.gov/MARC21/slim" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-3.xsd" 
	exclude-result-prefixes="#all">

	<!-- REVISIONS made by Stanford University  (be sure to update variable $thisXSLT -->
	<!-- 1.0 Initial version per spec by NH; rnanders 2009-03-26 -->

	<xsl:import href="DLF_STANFORD_MARC2MODS3-3.xsl"/>

	<!-- Get the catkey from a Param or from the "id" attribute of the input root element -->
	<xsl:variable name="ckey">
			<xsl:if test="contains(marc:record/@id,'catkey_')">
				<xsl:value-of select="substring-after(marc:record/@id,'_a')"/>
			</xsl:if>
	</xsl:variable>

	<!-- Parameters needed for data not found in the input XML -->
	<xsl:param name="catkey" select="$ckey"/>
	<xsl:param name="barcode"/>
	<xsl:param name="druid"/>
	<xsl:param name="test"/>
	
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

	<!-- Output will contain this authorship information -->
	<xsl:variable name="thisXSLT">DOR_MARC2MODS3-3.xsl Revision 1.1</xsl:variable>
	
	<!-- Specifying COSIMO as location of the schema for validation purposes -->
	<xsl:variable name="schemaLocation">http://www.loc.gov/mods/v3 http://cosimo.stanford.edu/standards/mods/v3/mods-3-3.xsd</xsl:variable>

	<!-- get a sequence of 035 elements -->
	<xsl:variable name="field035s" select="/marc:record/marc:datafield[@tag='035']/marc:subfield[@code='a']"/>
	<!-- Select only the OCLC record that begins with OCoLC or OCoLC-M, but not 'OCoLC-I' -->
	<xsl:variable name="oclc">
		<xsl:for-each select="$field035s">
			<xsl:if test="contains(.,'OCoLC')">
				<xsl:if test="not(contains(.,'OCoLC-I'))">
					<xsl:value-of select="substring-after(.,')')"/>
				</xsl:if>
			</xsl:if>
		</xsl:for-each>		
	</xsl:variable>

	<!-- Create the MODS using the imported DLF_STANFORD_MARC2MODS3-3.xsl transform  -->
	<xsl:variable name="firstpass">
		<xsl:choose>
			<xsl:when test="//marc:collection">
				<mods:modsCollection xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" >
					<xsl:attribute name="schemaLocation" namespace="http://www.w3.org/2001/XMLSchema-instance">
						<xsl:value-of select="$schemaLocation"/>
					</xsl:attribute>
					<xsl:for-each select="//marc:collection/marc:record">
						<mods:mods version="3.3">
							<xsl:call-template name="marcRecord"/>
						</mods:mods>
					</xsl:for-each>
				</mods:modsCollection>
			</xsl:when>
			<xsl:otherwise>
				<mods:mods xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="3.3">
					<xsl:attribute name="schemaLocation" namespace="http://www.w3.org/2001/XMLSchema-instance">
						<xsl:value-of select="$schemaLocation"/>
					</xsl:attribute>
					<xsl:for-each select="//marc:record">
						<xsl:call-template name="marcRecord"/>
					</xsl:for-each>
				</mods:mods>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<!-- Add the DOR enhancements -->
	<xsl:template match="/">
		<xsl:apply-templates select="$firstpass" mode="dor"/>
	</xsl:template>
	
	<!-- generic identity transform catches anything not matched elsewhere -->
	<xsl:template match="*" mode="dor">
		<xsl:element name="mods:{local-name(.)}">
			<xsl:apply-templates select="@*|node()" mode="dor"/>
		</xsl:element>
	</xsl:template>

	<!-- Make sure all MODS elements have a "mods:" prefix and no extraneous attributes or namespace declarations -->
	<xsl:template match="mods:*" mode="dor">
		<xsl:element name="mods:{local-name(.)}" namespace="{namespace-uri(.)}">
			<xsl:apply-templates select="@*|node()" mode="dor"/>
		</xsl:element>
	</xsl:template>

	<!-- copy all attributes and text nodes as is -->
	<xsl:template match="@*|text()" mode="dor">
		<xsl:copy>
			<xsl:value-of select="normalize-space(.)"/>
		</xsl:copy>
	</xsl:template>

	<!-- Use the input's physicalDescription to trigger creation of  mods:relatedItem type="original" -->
	<xsl:template match="mods:physicalDescription" mode="dor">
		<mods:relatedItem type="original">
			<!-- Copy the physical description from first pass as is -->
			<mods:physicalDescription>
				<xsl:apply-templates mode="dor"/>
			</mods:physicalDescription>
			<!-- Copy the recordInfo from first pass, but add identifiers for catkey and oclc -->
			<mods:recordInfo>
				<xsl:for-each select="../mods:recordInfo/*[not(self::mods:recordIdentifier)]">
					<xsl:apply-templates select="self::node()" mode="dor"/>
				 </xsl:for-each>
				<xsl:if test="string($catkey)">
					<mods:recordIdentifier source="SUL catalog key">
						<xsl:value-of select="$catkey"/>
					</mods:recordIdentifier>
				</xsl:if>
				<xsl:if test="string($oclc)">
					<mods:recordIdentifier source="oclc">
						<xsl:value-of select="$oclc"/>
					</mods:recordIdentifier>
				</xsl:if>
			</mods:recordInfo>
		</mods:relatedItem>

		<!-- Add boiler plate for MIME formats that may be included -->
		<mods:physicalDescription>
			<mods:form authority="marcform">
				<xsl:text>electronic</xsl:text>
			</mods:form>
			<mods:reformattingQuality>
				<xsl:text>preservation</xsl:text>
			</mods:reformattingQuality>
			<mods:digitalOrigin>
				<xsl:text>reformatted digital</xsl:text>
			</mods:digitalOrigin>
		</mods:physicalDescription>

	</xsl:template>

  <!-- We capture the first pass recordInfo above, but using it again to trigger more custom additions -->
	<xsl:template match="mods:recordInfo" mode="dor">

		<mods:recordInfo>
			<mods:recordContentSource><xsl:value-of select="$thisXSLT"/></mods:recordContentSource>
			<xsl:if test="not(string($test))">
				<mods:recordCreationDate encoding="iso8601">
					<xsl:value-of select="date:date-time()"/>
				</mods:recordCreationDate>
			</xsl:if>			
			<xsl:if test="string($barcode)">
				<mods:recordIdentifier source="Data Provider Digital Object Identifier">
					<xsl:value-of select="$barcode"/>
				</mods:recordIdentifier>
			</xsl:if>
		</mods:recordInfo>

		<!-- 
		<mods:accessCondition type="useAndReproduction" displayLabel="Copyright Stanford University. Stanford, CA 94305. (650) 723-2300."
			>Stanford University Libraries and Academic Information Resources - Terms of Use SULAIR Web sites are subject to Stanford University's standard Terms of Use (See http://www.stanford.edu/home/atoz/terms.html) These terms include a limited personal, non-exclusive, non-transferable license to access and use the sites, and to download - where permitted - material for personal, non-commercial, non-display use only.   Please contact the University Librarian to request permission to use SULAIR Web sites and contents beyond the scope of the above license, including but not limited to republication to a group or republishing the Web site or parts of the Web site. SULAIR provides access to a variety of external databases and resources, which sites are governed by their own Terms of Use, as well as contractual access restrictions.   The Terms of Use on these external sites always govern the data available there. Please consult with library staff if you have questions about data access and availability.</mods:accessCondition>
 		-->
		<xsl:if test="string($druid)">
			<mods:identifier type="local" displayLabel="SUL Resource ID">
				<xsl:value-of select="$druid"/>
			</mods:identifier>
			<mods:location>
				<mods:physicalLocation>Stanford University Libraries</mods:physicalLocation>
				<mods:url>http://purl.stanford.edu/<xsl:value-of select="substring-after($druid,':')"/></mods:url>
			</mods:location>
		</xsl:if>

	</xsl:template>

</xsl:stylesheet>
