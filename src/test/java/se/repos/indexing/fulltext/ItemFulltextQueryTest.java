/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.indexing.fulltext;

import static org.junit.Assert.*;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.repos.testing.indexing.ReposTestIndexing;
import se.repos.testing.indexing.TestIndexOptions;

/**
 * Tests field rules such as analysis and copy fields, with actual solr queries.
 * 
 * There should be no schema features that lack examples in tests,
 * or we'll end up with lots of copy-pasted stuff from solr examples that we don't know what it is for.
 */
public class ItemFulltextQueryTest {

	private ReposTestIndexing indexing = null;
	private SolrClient repositem = null;
	
	@Before
	public void setUp() {
		// run indexing without handlers or repository
		TestIndexOptions options = new TestIndexOptions().itemDefaults();
		indexing = ReposTestIndexing.getInstance(options);
		repositem = indexing.getCore("repositem");
	}
	
	@After
	public void tearDown() {
		indexing.tearDown(); // TODO make static and set up + tear down only once?
	}	
	
	@Test
	public void testLanguageDetection() {
		// TODO can we support language detection as in Stanbol? d.setField("language", "en-US");
	}
	
	@Test
	public void testTextSimple() throws Exception {
		SolrInputDocument d = new SolrInputDocument();
		d.setField("id", "1");
		d.setField("text", "A reasonably Short text with a ProductName and car model 93X or maybe 9-5X");
		repositem.add(d);
		SolrInputDocument e = new SolrInputDocument();
		e.setField("id", "dontmatch");
		e.setField("text", "distractions, car but no model,"); // append word sequences that should not match in assertions below
		repositem.add(e);
		repositem.commit();
		assertEquals(1, repositem.query(new SolrQuery("text:reasonably")).getResults().getNumFound());
		assertEquals("Should ignore case", 1,
				repositem.query(new SolrQuery("text:short")).getResults().getNumFound());
		assertEquals("Should ignore case in query", 1,
				repositem.query(new SolrQuery("text:shorT")).getResults().getNumFound());
		assertEquals("Should match multiple words", 1,
				repositem.query(new SolrQuery("text:\"car model\"")).getResults().getNumFound());
		// Default config no longer splits camel case.
		/*
		assertEquals("Should split camel case, such as product names", 1,
				repositem.query(new SolrQuery("text:\"Product Name\"")).getResults().getNumFound());
		*/
		assertEquals("Should match product name with mixed alpha and numerics", 1,
				repositem.query(new SolrQuery("text:93X")).getResults().getNumFound());
	}
	
	
	

}
