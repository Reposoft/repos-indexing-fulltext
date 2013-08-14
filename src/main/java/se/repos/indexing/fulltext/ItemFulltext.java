package se.repos.indexing.fulltext;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			logger.debug("Skipping folder {}", item);
			return;
		}
		
		useTika(progress.getContents());

	}

	@SuppressWarnings("serial")
	@Override
	public Set<Class<? extends IndexingItemHandler>> getDependencies() {
		return new HashSet<Class<? extends IndexingItemHandler>>() {{
			this.add(ItemPathinfo.class);
		}};
	}
	
	public void useTika(InputStream content) {
		Tika tika = new Tika();
		
		// TODO use tika-xmp instead
		Metadata metadata = new Metadata();
		
		String text;
		
		try {
			text = tika.parseToString(content, metadata);
		} catch (TikaException e) {
			throw new RuntimeException("not handled", e);
		} catch (IOException e) {
			throw new RuntimeException("not handled", e);
		}
		
		for (String n : metadata.names()) {
			System.out.print(n + ": ");
			if (metadata.isMultiValued(n)) {
				System.out.println("̣");
				for (String v : metadata.getValues(n)) {
					System.out.println("\t" + v);
				}
			} else {
				System.out.println(metadata.get(n));
				
			}
		}		
		
		System.out.println(text);
	}

}
