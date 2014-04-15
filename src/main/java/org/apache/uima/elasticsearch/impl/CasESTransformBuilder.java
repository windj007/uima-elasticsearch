package org.apache.uima.elasticsearch.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.serialization.json.SimpleFileTransformBuilder;

public class CasESTransformBuilder extends SimpleFileTransformBuilder {
	private static final String	FORWARD_XSLT	= "transformations/XCasToES.xslt";
	private static final String	REVERSE_XSLT	= "transformations/ESToXCas.xslt";

	private Set<String>			annotsToIndex	= new HashSet<String>();

	public CasESTransformBuilder() {
		super(FORWARD_XSLT, REVERSE_XSLT);
	}

	public void indexAnnots(String... annotFullNames) {
		annotsToIndex.addAll(Arrays.asList(annotFullNames));
	}

	@Override
	protected Transformer loadTransformer(String resource)
		throws TransformerConfigurationException {
		Transformer trans = super.loadTransformer(resource);
		trans.setParameter("annotsToIndex", StringUtils.join(annotsToIndex, ','));
		return trans;
	}
}
