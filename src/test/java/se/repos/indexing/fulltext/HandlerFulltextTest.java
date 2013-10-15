/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.indexing.fulltext;

import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import se.repos.indexing.IndexingDoc;
import se.repos.indexing.item.IndexingItemProgress;
import se.simonsoft.cms.item.events.change.CmsChangesetItem;

public class HandlerFulltextTest {

	@Test
	public void testHandleFolder() {
		IndexingItemProgress p = mock(IndexingItemProgress.class);
		CmsChangesetItem item = mock(CmsChangesetItem.class);
		when(p.getItem()).thenReturn(item);
		when(item.isFolder()).thenReturn(true);
		
		HandlerFulltext handler = new HandlerFulltext();
		handler.handle(p);
		
		// shouldn't even ask for fields
	}

	@Test
	public void testHandleDelete() {
		IndexingItemProgress p = mock(IndexingItemProgress.class);
		CmsChangesetItem item = mock(CmsChangesetItem.class);
		when(p.getItem()).thenReturn(item);
		when(item.isDelete()).thenReturn(true);
		
		HandlerFulltext handler = new HandlerFulltext();
		handler.handle(p);
		
		// shouldn't even ask for fields
	}
	
	@Test
	public void testHandle() {
		IndexingItemProgress p = mock(IndexingItemProgress.class);
		CmsChangesetItem item = mock(CmsChangesetItem.class);
		when(p.getItem()).thenReturn(item);
		when(p.getContents()).thenReturn(new ByteArrayInputStream("textfile\n".getBytes()));
		IndexingDoc doc = mock(IndexingDoc.class);
		when(p.getFields()).thenReturn(doc);
		
		HandlerFulltext handler = new HandlerFulltext();
		handler.handle(p);
		verify(doc).setField("text", "textfile\n\n");
	}
	
}
