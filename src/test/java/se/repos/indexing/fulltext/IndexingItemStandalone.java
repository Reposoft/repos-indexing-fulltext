/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.indexing.fulltext;

import java.io.InputStream;

import se.repos.indexing.IndexingDoc;
import se.repos.indexing.item.IndexingItemProgress;
import se.repos.indexing.twophases.IndexingDocIncrementalSolrj;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.events.change.CmsChangesetItem;
import se.simonsoft.cms.item.properties.CmsItemProperties;

/**
 * Used to run extraction on a standalone file from classpath, without actually adding to indexing.
 */
public class IndexingItemStandalone implements IndexingItemProgress {

	private ClassLoader loader;
	private String resource;
	private IndexingDoc fields;
	private CmsChangesetItem item;
	
	public IndexingItemStandalone(String classLoaderResource) {
		this.loader = getClass().getClassLoader();
		this.resource = classLoaderResource;
		this.fields = new Fields();
		this.item = new Item();
	}

	@Override
	public IndexingDoc getFields() {
		return fields;
	}
	
	@Override
	public InputStream getContents() {
		InputStream resourceAsStream = loader.getResourceAsStream(resource);
		if (resourceAsStream == null) {
			throw new IllegalArgumentException("Failed to load classpath resource: " + resource);
		}
		return resourceAsStream;
	}
	
	@Override
	public CmsItemProperties getProperties() {
		throw new UnsupportedOperationException("Not supported in standalone extraction, yet");
	}
	
	@Override
	public CmsRepository getRepository() {
		throw new UnsupportedOperationException("Not supported in standalone extraction");
	}

	@Override
	public RepoRevision getRevision() {
		throw new UnsupportedOperationException("Not supported in standalone extraction");
	}

	@Override
	public CmsChangesetItem getItem() {
		return item;
	}
	
	@SuppressWarnings("serial")
	class Fields extends IndexingDocIncrementalSolrj {

		@Override
		public IndexingDoc deepCopy() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}
		
	}
	
	private class Item implements CmsChangesetItem {

		@Override
		public boolean isFile() {
			return true;
		}

		@Override
		public boolean isFolder() {
			return false;
		}
		
		@Override
		public boolean isDelete() {
			return false;
		}

		@Override
		public boolean isCopy() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public boolean isAdd() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public boolean isReplace() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public boolean isMove() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public boolean isContentModified() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public boolean isContent() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public boolean isPropertiesModified() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public boolean isProperties() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public boolean isExplicit() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public boolean isDerived() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public boolean isOverwritten() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public CmsItemPath getPath() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public RepoRevision getRevision() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public CmsItemPath getCopyFromPath() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public RepoRevision getCopyFromRevision() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public CmsChangesetItem getPreviousChange() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}

		@Override
		public RepoRevision getRevisionObsoleted() {
			throw new UnsupportedOperationException("Not supported in standalone extraction");
		}
		
	}
	
}
