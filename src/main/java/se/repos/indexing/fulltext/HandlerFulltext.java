/**
 * Copyright (C) 2004-2012 Repos Mjukvara AB
 */
package se.repos.indexing.fulltext;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.xmp.XMPMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.repos.indexing.IndexingDoc;
import se.repos.indexing.IndexingItemHandler;
import se.repos.indexing.item.HandlerPathinfo;
import se.repos.indexing.item.IndexingItemProgress;
import se.simonsoft.cms.item.events.change.CmsChangesetItem;

public class HandlerFulltext implements IndexingItemHandler {

	/**
	 * To avoid multivalue fields for all metadata in Solr we concatenate with newline, and tokenize on that at indexing.
	 */
	private static final String METADATA_MULTIVALUE_SEPARATOR = "\n";
	
	private String[] XMP_FIELDS_DEFAULT = {"dc:description", "dc:subject", "dc:title", "cp:revision", "tiff:Orientation"};
	private Set<String> xmpFields = new HashSet<String>(Arrays.asList(XMP_FIELDS_DEFAULT));
	
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

		// Indexing both traditional Metadata and XMPMetadata (tika-xmp).
		// TODO pre-load metadata with for example explicit content type from svn prop
		Metadata metadata = new Metadata();
		
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

		// Extract the classic embedded metadata.
		for (String n : metadata.names()) {
			this.addField("embd_", n, metadata, indexingDoc);
		}

		// Extract the metadata after mapping to XMP.
		try {
			XMPMetadata xmpMetadata = new org.apache.tika.xmp.XMPMetadata(metadata);

			// Can be used for deeper examination of XMP tree. Complex stuff.
			/*
			XMPIterator it = xmpMetadata.getXMPData().iterator();
			while (it.hasNext()) { 
				XMPPropertyInfo xmp = (XMPPropertyInfo) it.next();
				// https://code.google.com/p/metadata-extractor/source/browse/Libraries/XMPCore/src/com/adobe/xmp/options/PropertyOptions.java
				PropertyOptions xmpOptions = xmp.getOptions();
				
				System.out.println(xmp.getPath());
				logger.trace("XMP node: {} ({}): {}", xmp.getPath(), xmpOptions.getOptionsString(), xmp.getValue());
			}
			*/
				
			for (String n: this.xmpFields) {
				String[] values = xmpMetadata.getValues(n);
				if (values != null && values[0] != null) {
					logger.debug("XMP field {} exists.", n);
					this.addField("xmp_", n, metadata, indexingDoc);
				} else {
					logger.debug("XMP field {} does NOT exist.", n);
					//System.out.println("XMP field " + n + " does NOT exist.");
				}
				
			}
			
		} catch (Exception e) {
			logger.warn("XMP extraction error for {}: {}", indexingDoc.getFieldValue("id"), e.getMessage());
			StringWriter err = new StringWriter();
			e.printStackTrace(new PrintWriter(err));
			indexingDoc.setField("text_error", err.toString());
			return;
		}
	}

	private void addField(String prefix, String n, Metadata metadata, IndexingDoc indexingDoc) {
		
		String fieldname = prefix + n.replace(' ', '_').replace(':', '.');
		if (n.indexOf(' ') >= 0) {
			logger.trace("Whitespace in metadata name, '{}' becomes {}", n, fieldname);
		}
		if (n.indexOf(':') >= 0) {
			logger.trace("Colon in metadata name, '{}' becomes {}", n, fieldname);
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
