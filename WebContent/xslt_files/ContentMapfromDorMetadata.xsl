<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="#all" version="2.0" 
	xmlns:METS="http://www.loc.gov/METS/" 
	xmlns:mods='http://www.loc.gov/mods/v3'
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:premis="info:lc/xmlns/premis-v2" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output encoding="UTF-8" indent="no" method="xml" version="1.0"/>

	<!-- druid -->
	<xsl:variable name="druid" select="/objectMD/@druid"/>

	<!-- barcode & catkey identifiers are stored in the dor datastream -->
	<xsl:variable name="barcode" select='/objectMD/dor/identifier[@name="barcode"]'/>
	<xsl:variable name="catkey" select='/objectMD/dor/identifier[@name="catkey"]'/>
		
		<xsl:variable name="modsRoot" select="/objectMD/mods:mods[1]"/>	
			
	<!-- Create root contentMap element then create children -->
	<xsl:template match="/">
		<xsl:element name="contentMap">
			<xsl:attribute name="id" select="$druid"/>
			<xsl:attribute name="type">pageMap</xsl:attribute>
			<description>
				<title><xsl:value-of select="$modsRoot/mods:titleInfo[1]/mods:title[1]"/></title>
				<author><xsl:value-of select="$modsRoot/mods:name[1]/mods:namePart[1]"/></author>
				<identifier>barcode:<xsl:value-of select="$barcode"/></identifier>
				<identifier>catkey:<xsl:value-of select="$catkey"/></identifier>
			</description>
			
			<xsl:apply-templates select="/objectMD/contentMetadata//METS:div[@TYPE='Page']"/>
		</xsl:element>
	</xsl:template>

	<!-- Create Page elements -->
	<xsl:template match="METS:div[@TYPE='Page']">
		<xsl:element name="page">
			<xsl:attribute name="sequence">
				<xsl:value-of select="@ORDER"/>
			</xsl:attribute>
			<!-- Create a PageType element for each Google page tag-->
			<xsl:for-each select="tokenize(@ADMID,'\s+')">
				<xsl:variable name="divType">
					<xsl:choose>
						<xsl:when test=".='TITLE'">TITLE_PAGE</xsl:when>
						<xsl:when test=".='TABLE_OF_CONTENTS'">TOC_PAGE</xsl:when>
						<xsl:when test=".='BLANK'">BLANK_PAGE</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="."/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:element name="PageType">
					<xsl:value-of select="$divType"/>
				</xsl:element>
			</xsl:for-each>
			<!-- Create Label element if a page number was found on the page -->
			<xsl:if test="@ORDERLABEL">
				<xsl:element name="Label">
					<xsl:value-of select="@ORDERLABEL"/>
				</xsl:element>
			</xsl:if>
			<!-- Create the File elements -->
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

	<!-- Create the File elements -->
	<xsl:template match="METS:fptr">
		<xsl:element name="file">
			<!-- Use the ID attribute to locate the METS <file> element pointed to -->
			<xsl:variable name="fileRef">
				<xsl:value-of select="@FILEID"/>
			</xsl:variable>
			<xsl:variable name="metsFileElement" select="/objectMD/contentMetadata//METS:file[@ID=$fileRef]"/>
			<!-- get the MIMETYPE of the file -->
			<xsl:variable name="mimetype" select="$metsFileElement/@MIMETYPE"/>
			<xsl:attribute name="datastream">
				<xsl:choose>
					<xsl:when test="$mimetype='text/plain' or $mimetype='text/html'">
						<xsl:text>text</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>image</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:attribute name="format" select="$mimetype"/>
			<xsl:attribute name="checksum" select="$metsFileElement/@CHECKSUM">
				
			</xsl:attribute>
			<!-- generate a URL for the file location -->
			<xsl:attribute name="url">
				<xsl:variable name="filename" select="$metsFileElement/METS:FLocat/@xlink:href"/>
				<xsl:value-of select="concat('file://localhost/dor/workspace/objects/',$filename)"/>
			</xsl:attribute>
		</xsl:element>
	</xsl:template>
	
	<!-- discard anything not explicitly matched -->
	<xsl:template match="@*|node()"/>
	
	
</xsl:stylesheet>
