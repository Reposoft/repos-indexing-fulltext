/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.indexing.fulltext;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import se.repos.indexing.IndexingDoc;
import se.repos.indexing.item.IndexingItemProgress;
import se.repos.indexing.item.IndexingItemStandalone;

public class ItemFulltextGraphicInvalidTest {

	
	private void printFields(IndexingDoc fields) {

		for (@SuppressWarnings("unused") String f : fields.getFieldNames()) {
			System.out.println(f);
		}
	}

	
	@Test @Ignore
	public void testPngHeapsize() {
		HandlerFulltext fulltext = new HandlerFulltext();

		IndexingItemProgress item = new IndexingItemStandalone("se/repos/indexing/fulltext/datasets/graphics-invalid/XD000207.png");
		fulltext.handle(item);
		IndexingDoc fields = item.getFields();

		assertTrue("Should extract text", fields.containsKey("text"));

		printFields(fields);
		
		assertEquals("text", "", fields.getFieldValue("text"));
		// XD000206.png has "8 8 8 8", not sure about XD000207 since pngcheck throws error "invalid sBIT length".
		assertEquals("Bits per Sample", "8 8 8 8", fields.getFieldValue("xmp_tiff.BitsPerSample"));
		
	}


}
