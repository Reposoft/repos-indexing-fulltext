/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.indexing.fulltext;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.xmp.XMPMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.repos.indexing.IndexingDoc;
import se.repos.indexing.item.IndexingItemHandler;
import se.repos.indexing.item.IndexingItemProgress;
import se.repos.indexing.item.ItemPathinfo;
import se.simonsoft.cms.item.events.change.CmsChangesetItem;

public class ItemFulltext implements IndexingItemHandler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void handle(IndexingItemProgress progress) {
		
		CmsChangesetItem item = progress.getItem();
		
		if (item.isFolder()) {
			logger.trace("Skipping folder {}", item);
			return;
		}
		
		if (item.isDelete()) {
			logger.trace("Skipping deleted {}", item);
			return;
		}

		// TODO use tika-xmp instead?
		// TODO pre-load metadata with for example explicit content type from svn prop
		Metadata metadata = new Metadata();
		//XMPMetadata metadata = new XMPMetadata();
		
		useTika(progress.getContents(), metadata, progress.getFields());

	}

	@SuppressWarnings("serial")
	@Override
	public Set<Class<? extends IndexingItemHandler>> getDependencies() {
		return new HashSet<Class<? extends IndexingItemHandler>>() {{
			this.add(ItemPathinfo.class);
		}};
	}
	
	public void useTika(InputStream content, Metadata metadata, IndexingDoc indexingDoc) {
		Tika tika = new Tika();
		
		String text;
		
		try {
			text = tika.parseToString(content, metadata);
		} catch (TikaException e) {
			throw new RuntimeException("not handled", e);
		} catch (IOException e) {
			throw new RuntimeException("not handled", e);
		}
		
		indexingDoc.setField("text", text);
		
		for (String n : metadata.names()) {
			String fieldname = "embd_" + n.replace(' ', '_');
			if (n.indexOf(' ') >= 0) {
				logger.debug("Whitespace in metadata name, '{}' becomes {}", n, fieldname);
			}
			if (metadata.isMultiValued(n)) {
				StringBuffer concat = new StringBuffer();
				for (String v : metadata.getValues(n)) {
					indexingDoc.addField(fieldname, v);
				}
			} else {
				indexingDoc.addField(fieldname, metadata.get(n));
			}
		}		
	}

}
