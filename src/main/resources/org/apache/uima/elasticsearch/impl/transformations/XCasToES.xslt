<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy® -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" indent="yes" />

	<xsl:param name="annotsToIndex" select="''" />
	<xsl:param name="sofaID" select="'_InitialView'" />

	<xsl:template name="ordinaryNode">
		<xsl:variable name="node_name" select="name()" />
		<children type="{$node_name}">
			<attribs>
				<xsl:for-each select="./@*">
					<xsl:variable name="attrib_name" select="name()" />
					<xsl:element name="{$attrib_name}">
						<xsl:value-of select="." />
					</xsl:element>
				</xsl:for-each>
			</attribs>
			<xsl:for-each select="./*">
				<xsl:call-template name="ordinaryNode" />
			</xsl:for-each>
			<text>
				<xsl:value-of select="text()" />
			</text>
		</children>

	</xsl:template>

	<xsl:template name="collectAnnotationsText">
		<xsl:param name="annotName" />
		<xsl:param name="elementName" select="$annotName" />
		<xsl:param name="sofaID" select="'_InitialView'" />

		<xsl:variable name="fullText"
			select="//uima.cas.Sofa[@sofaID=$sofaID]/@sofaString" />

		<xsl:variable name="annotNodes" select="//*[name() = $annotName]" />

		<xsl:if test="count($annotNodes) &gt; 0">
			<xsl:element name="{$elementName}">
				<xsl:for-each select="$annotNodes">
					<xsl:if test="@begin &gt; 0 and @end &gt; 0">
						<xsl:value-of
							select="concat(substring($fullText, @begin + 1, @end - @begin), ' ')" />
					</xsl:if>
				</xsl:for-each>
			</xsl:element>
		</xsl:if>


	</xsl:template>

	<xsl:template name="indexAnnotationsCoveredText">
		<xsl:param name="annotsNames" />
		<xsl:param name="sofaID" select="'_InitialView'" />

		<xsl:variable name="namesSep" select="','" />

		<xsl:choose>
			<xsl:when test="contains($annotsNames, $namesSep)">
				<xsl:variable name="curAnnotName"
					select="substring-before($annotsNames, $namesSep)" />

				<xsl:call-template name="collectAnnotationsText">
					<xsl:with-param name="annotName" select="$curAnnotName" />
					<xsl:with-param name="sofaID" select="$sofaID" />
				</xsl:call-template>

				<xsl:call-template name="indexAnnotationsCoveredText">
					<xsl:with-param name="annotsNames"
						select="substring-after($annotsNames, $namesSep)" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="collectAnnotationsText">
					<xsl:with-param name="annotName" select="$annotsNames" />
					<xsl:with-param name="sofaID" select="$sofaID" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="/CAS">
		<document>
			<indexed>
				<xsl:call-template name="indexAnnotationsCoveredText">
					<xsl:with-param name="annotsNames" select="$annotsToIndex" />
					<xsl:with-param name="sofaID" select="$sofaID" />
				</xsl:call-template>
			</indexed>
			<originalCas>
				<xsl:call-template name="ordinaryNode" />
			</originalCas>
		</document>
	</xsl:template>
</xsl:stylesheet>

