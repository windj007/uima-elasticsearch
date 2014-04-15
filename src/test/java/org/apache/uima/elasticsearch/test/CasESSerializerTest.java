package org.apache.uima.elasticsearch.test;

// import static org.junit.Assert.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.elasticsearch.impl.CasESTransformBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.serialization.CasSerializerMetaFactory;
import org.apache.uima.serialization.ICasSerializer;
import org.apache.uima.serialization.SerializedCasFileReader;
import org.apache.uima.serialization.SerializedCasFileWriter;
import org.apache.uima.serialization.exceptions.CasSerializationException;
import org.apache.uima.serialization.exceptions.SerializerInitializationException;
import org.apache.uima.serialization.exceptions.UnknownFactoryException;
import org.apache.uima.serialization.json.CasJsonSerializerFactory;
import org.apache.uima.serialization.json.test.TestAnnotator;
import org.apache.uima.serialization.json.test.type.First;
import org.apache.uima.serialization.json.test.type.Second;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.util.JCasUtil;

public class CasESSerializerTest {
	@Before
	public void regFactory() {
		CasESTransformBuilder builder = new CasESTransformBuilder();
		builder.indexAnnots(First.class.getName());
		CasSerializerMetaFactory.Instance().registerFactory(
			getSerializerFactory(),
			new CasJsonSerializerFactory(builder));
	}
	
	@Rule
	public TemporaryFolder	folder	= new TemporaryFolder();

	@Test
	public void simpleSerializationTest()
		throws ResourceInitializationException, IOException,
		AnalysisEngineProcessException, SerializerInitializationException,
		CasSerializationException, UnknownFactoryException {

		AnalysisEngine ae = AnalysisEngineFactory.createPrimitive(TestAnnotator.class);
		JCas doc = ae.newJCas();
		ae.process(doc);

		ICasSerializer serializer = CasSerializerMetaFactory.Instance().getFactory(
			getSerializerFactory()).createSerializer();
		String serialized = serializer.serialize(doc.getCas());

		JCas doc2 = ae.newJCas();
		serializer.deserialize(doc2.getCas(), serialized);

		checkAnnotations(doc);
		checkAnnotations(doc2);
	}

	@Test
	public void fileSerializationTest() throws ResourceInitializationException,
		AnalysisEngineProcessException, IOException, CasSerializationException,
		CollectionException, SerializerInitializationException {

		// ************************* prepare document *************************
		AnalysisEngine ae = AnalysisEngineFactory.createPrimitive(TestAnnotator.class);
		JCas doc = ae.newJCas();
		ae.process(doc);

		File dir = folder.newFolder();

		// ***************************** serialize ****************************
		AnalysisEngine writer = AnalysisEngineFactory.createPrimitive(
			SerializedCasFileWriter.class,
			SerializedCasFileWriter.PARAM_OUTPUT_DIRECTORY_NAME,
			dir,
			SerializedCasFileWriter.PARAM_SERIALIZER_FACTORY,
			getSerializerFactory());
		writer.process(doc);

		// **************************** deserialize ***************************
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
			SerializedCasFileReader.class,
			SerializedCasFileReader.PARAM_INPUT_DIRECTORY,
			dir,
			SerializedCasFileReader.PARAM_SERIALIZER_FACTORY,
			getSerializerFactory());

		JCas doc2 = ae.newJCas();
		reader.getNext(doc2.getCas());

		// ****************************** compare *****************************
		checkAnnotations(doc);
		checkAnnotations(doc2);
	}

	private void checkAnnotations(JCas doc) {
		Collection<Second> seconds = JCasUtil.select(doc, Second.class);
		Second s1 = null;
		Second s2 = null;
		assertTrue(seconds.size() == 2);
		for (Second s : seconds) {
			if (s.getBegin() == 6) {
				assertTrue(s.getEnd() == 10);
				s1 = s;
			}
			if (s.getBegin() == 5) {
				assertTrue(s.getEnd() == 9);
				s2 = s;
			}
		}

		DocumentAnnotation docAnnot = JCasUtil.selectSingle(
			doc,
			DocumentAnnotation.class);
		assertTrue(docAnnot != null);

		Collection<First> firsts = JCasUtil.select(doc, First.class);
		assertTrue(firsts.size() == 2);

		for (First f : firsts) {
			if (f.getBegin() == 1) {
				assertTrue(f.getEnd() == 3);
				assertTrue(f.getAnnot() == s1);
				assertTrue(f.getBool());
				assertFalse(f.getBoolArray(0));
				assertTrue(f.getBoolArray(2));
				assertTrue(f.getDocAnnot() == docAnnot);
				assertTrue(f.getFirst() == f);
				assertTrue(f.getInteger() == 123);
				assertTrue(f.getIntegerList().getNthElement(0) == 5467);
				assertTrue(f.getIntegerList().getNthElement(1) == 345);
				assertTrue(f.getRefToSofa() == doc.getSofa());
				assertTrue(f.getSecond() == s2);
				assertTrue(f.getString().equals("String value"));
			}
			if (f.getBegin() == 0) {
				assertTrue(f.getFirst() != null);
			}
		}
	}

	protected String getSerializerFactory() {
		return "CasES";
	}
}
