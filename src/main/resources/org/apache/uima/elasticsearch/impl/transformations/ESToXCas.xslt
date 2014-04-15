<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy® -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" />

	<xsl:template name="childrenNode">
		<xsl:param name="curNode" select="." />
		<xsl:variable name="curNodeName" select="$curNode/type/text()" />
		<xsl:element name="{$curNodeName}">
			<xsl:for-each select="$curNode/attribs/*">
				<xsl:variable name="attrib_name" select="name()" />
				<xsl:attribute name="{$attrib_name}">
					<xsl:value-of select="." />
				</xsl:attribute>
			</xsl:for-each>
			<xsl:value-of select="$curNode/text/text()" />
			<xsl:for-each select="$curNode/children">
				<xsl:call-template name="childrenNode" />
			</xsl:for-each>
		</xsl:element>
	</xsl:template>

	<xsl:template match="/">
		<xsl:call-template name="childrenNode">
			<xsl:with-param name="curNode" select="document/originalCas/children[1]" />
		</xsl:call-template>
	</xsl:template>
</xsl:stylesheet>

