package se.repos.indexing.fulltext;

import static org.junit.Assert.*;

import org.junit.Test;

import se.repos.indexing.item.IndexingItemProgress;

/**
 * Tests what we can get out of various file formats by asserting on an index document, no actual use of Solr.
 */
public class ItemFulltextExtractionTest {

	@Test
	public void test() {
		ItemFulltext fulltext = new ItemFulltext();
		
		IndexingItemProgress item = new IndexingItemStandalone("repos-search-v1/docs/Excel format.xls");
		fulltext.handle(item);
		
		assertTrue("Should extract text", item.getFields().containsKey("text"));
	}

}
