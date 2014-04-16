package org.apache.uima.elasticsearch;

import org.apache.uima.jcas.JCas;

public interface IKeyGetter {
	String getKey(JCas doc);
}
