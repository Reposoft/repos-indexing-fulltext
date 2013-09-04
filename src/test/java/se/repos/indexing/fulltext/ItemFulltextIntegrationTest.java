/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.indexing.fulltext;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.After;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc2.SvnImport;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import se.repos.testing.indexing.SvnTestIndexing;
import se.repos.testing.indexing.TestIndexOptions;
import se.simonsoft.cms.testing.svn.CmsTestRepository;
import se.simonsoft.cms.testing.svn.SvnTestSetup;

/**
 * Test queries on files in an actual test repository.
 * 
 * This should just test that the combination of repository and indexing works,
 * for details on extraction and querying use instead
 * {@link ItemFulltextExtractionTest} and {@link ItemFulltextQueryTest}.
 */
public class ItemFulltextIntegrationTest {

	
	@After
	public void tearDown() {
		SvnTestSetup.getInstance().tearDown();
		SvnTestIndexing.getInstance().tearDown();
	}
	
	/**
	 * Test the index of the structure in Repos Search 1 test sets.
	 */
	@Test
	public void testHandleSearch1Docs() throws SVNException, SolrServerException {
		ItemFulltext handler = new ItemFulltext();
		TestIndexOptions options = new TestIndexOptions().itemDefaults().addHandler(handler);
		
		CmsTestRepository repo = SvnTestSetup.getInstance().getRepository();
		SolrServer solr = SvnTestIndexing.getInstance(options).enable(repo).getCore("repositem");
		
		File docs = new File("src/test/resources/repos-search-v1");
		assertTrue(docs.isDirectory());
		
		SvnOperationFactory svnkitOp = repo.getSvnkitOp();
		SvnImport imp = svnkitOp.createImport();
		imp.setSource(docs);
		imp.setSingleTarget(SvnTarget.fromURL(repo.getUrlSvnkit()));
		imp.run();
		
		QueryResponse all = solr.query(new SolrQuery("*:*"));
		assertEquals("Should have indexed all v1 documents (30), folders (9) and revisions (2)", 30 + 9 + 2, all.getResults().getNumFound());
		
		QueryResponse pdf = solr.query(new SolrQuery("pathext:pdf"));
		assertEquals(1, pdf.getResults().getNumFound());
		SolrDocument shortpdf = pdf.getResults().get(0);
		assertEquals("keywordinsaveaspdf someotherkeyword", shortpdf.getFieldValues("embd_dc:subject").iterator().next());
		// don't forget to avoid assertions of stuff that belong in the isolated tests, almost everything
		
		// TODO see https://wiki.apache.org/tika/MetadataRoadmap
		// We need typed values, particularily Date support. We might also want to avoid multi-value for most fields.
	}

}
