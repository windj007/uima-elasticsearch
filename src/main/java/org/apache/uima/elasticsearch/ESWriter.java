package org.apache.uima.elasticsearch;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.serialization.CasSerializerMetaFactory;
import org.apache.uima.serialization.ICasSerializer;
import org.apache.uima.serialization.exceptions.CasSerializationException;
import org.apache.uima.serialization.exceptions.UimaSerializationBaseException;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;

public class ESWriter extends JCasAnnotator_ImplBase {
	public static final String	PARAM_SERIALIZER_FACTORY_NAME	= ConfigurationParameterFactory.createConfigurationParameterName(
																	ESWriter.class,
																	"serializerFactoryName");

	public static final String	PARAM_INDEX_NAME				= ConfigurationParameterFactory.createConfigurationParameterName(
																	ESWriter.class,
																	"indexName");

	public static final String	PARAM_DOC_TYPE					= ConfigurationParameterFactory.createConfigurationParameterName(
																	ESWriter.class,
																	"docType");

	@ConfigurationParameter(description = "Name of factory to create the serializer", mandatory = true)
	private String				serializerFactoryName;

	@ConfigurationParameter(description = "Name of ElasticSearch index to put document to", mandatory = true)
	private String				indexName;

	@ConfigurationParameter(description = "Type of documents to be indexed", mandatory = true)
	private String				docType;

	private ICasSerializer		serializer;

	private Node				node;
	private Client				client;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException {
		super.initialize(context);

		try {
			serializer = CasSerializerMetaFactory.Instance().getFactory(
				serializerFactoryName).createSerializer();
		} catch (UimaSerializationBaseException e) {
			throw new ResourceInitializationException(e);
		}

		node = NodeBuilder.nodeBuilder().node();
		client = node.client();
	}

	@Override
	public void process(JCas doc) throws AnalysisEngineProcessException {
		try {
			client.prepareIndex(indexName, docType)
				.setSource(serializer.serialize(doc.getCas()));
		} catch (CasSerializationException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	@Override
	public void collectionProcessComplete()
		throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		client.close();
	}

}
