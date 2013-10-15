/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.indexing.fulltext;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.repos.indexing.IndexingDoc;
import se.repos.indexing.IndexingItemHandler;
import se.repos.indexing.item.IndexingItemProgress;
import se.repos.indexing.item.HandlerPathinfo;
import se.simonsoft.cms.item.events.change.CmsChangesetItem;

public class HandlerFulltext implements IndexingItemHandler {

	/**
	 * To avoid multivalue fields for all metadata in Solr we concatenate with newline, and tokenize on that at indexing.
	 */
	private static final String METADATA_MULTIVALUE_SEPARATOR = "\n";
	
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
		//org.apache.tika.xmp.XMPMetadata metadata = new org.apache.tika.xmp.XMPMetadata();
		
		useTika(progress.getContents(), metadata, progress.getFields());

	}

	@SuppressWarnings("serial")
	@Override
	public Set<Class<? extends IndexingItemHandler>> getDependencies() {
		return new HashSet<Class<? extends IndexingItemHandler>>() {{
			this.add(HandlerPathinfo.class);
		}};
	}
	
	public void useTika(InputStream content, Metadata metadata, IndexingDoc indexingDoc) {
		Tika tika = new Tika();
		
		String text;
		
		try {
			text = tika.parseToString(content, metadata);
		} catch (TikaException e) {
			logger.warn("Content extraction error for {}: {}", indexingDoc.getFieldValue("id"), e.getMessage());
			StringWriter err = new StringWriter();
			e.printStackTrace(new PrintWriter(err));
			indexingDoc.setField("text_error", err.toString());
			return;
		} catch (IOException e) {
			throw new RuntimeException("not handled", e);
		}
		
		indexingDoc.setField("text", text);
		
		for (String n : metadata.names()) {
			String fieldname = "embd_" + n.replace(' ', '_');
			if (n.indexOf(' ') >= 0) {
				logger.trace("Whitespace in metadata name, '{}' becomes {}", n, fieldname);
			}
			if (metadata.isMultiValued(n)) {
				StringBuffer concat = new StringBuffer();
				for (String v : metadata.getValues(n)) {
					concat.append(METADATA_MULTIVALUE_SEPARATOR).append(v);
				}
				indexingDoc.addField(fieldname, concat.substring(1));
			} else {
				indexingDoc.addField(fieldname, metadata.get(n));
			}
		}		
	}

}
