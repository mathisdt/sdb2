/*
 * This file is part of the Song Database (SDB).
 * 
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.Song;

import com.google.common.base.Stopwatch;

/**
 * An in-memory index based on Lucene.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class IndexerServiceImpl implements IndexerService<Song> {
	
	private static final Logger LOG = LoggerFactory.getLogger(IndexerServiceImpl.class);
	
	private static final String SIMPLIFY_INDEXING_REGEX = "[-\r\n\t.,;:_/+'\"!?()\\[\\]]++";
	public static final String TERM_SPLIT_REGEX = "[- .,;:_/+'\"!?()\\[\\]]++";
	
	private Map<String, Song> songByUuid = new HashMap<>();
	
	private Map<IndexType, Directory> indexes = new HashMap<>();
	private static final Object INDEXES_LOCK = new Object();
	
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private Directory getIndex(IndexType indexType) {
		synchronized (INDEXES_LOCK) {
			return indexes.get(indexType);
		}
	}
	
	private void putIndex(IndexType indexType, Directory directory) {
		synchronized (INDEXES_LOCK) {
			indexes.put(indexType, directory);
		}
	}
	
	@Override
	public void index(final IndexType indexType, final Collection<Song> songs) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Stopwatch stopwatch = Stopwatch.createStarted();
				
				Directory directory = new RAMDirectory();
				try {
					LOG.debug("available tokenizers: {}", TokenizerFactory.availableTokenizers());
					LOG.debug("available token filters: {}", TokenFilterFactory.availableTokenFilters());
					Analyzer analyzer = CustomAnalyzer.builder()
						.withTokenizer("standard")
						.addTokenFilter("lowercase")
						.addTokenFilter("ngram", "minGramSize", "1", "maxGramSize", "25")
						.build();
					IndexWriterConfig config = new IndexWriterConfig(analyzer);
					try (IndexWriter writer = new IndexWriter(directory, config)) {
						for (Song song : songs) {
							Document document = createDocument(song);
							writer.addDocument(document);
							songByUuid.put(song.getUUID(), song);
						}
					} catch (IOException e) {
						LOG.warn("couldn't index songs", e);
					}
				} catch (IOException e1) {
					LOG.warn("couldn't create analyzer", e1);
				} finally {
					putIndex(indexType, directory);
					stopwatch.stop();
					LOG.info("indexing songs in background thread took {}", stopwatch.toString());
				}
			}
		});
	}
	
	private Document createDocument(Song song) {
		Document document = new Document();
		document.add(new StringField(FieldName.UUID.name(), song.getUUID(), Store.YES));
		document.add(new TextField(FieldName.TITLE.name(), simplify(song.getTitle()), Store.NO));
		document.add(new TextField(FieldName.LYRICS.name(), simplify(song.getLyrics()), Store.NO));
		return document;
	}
	
	private String simplify(String content) {
		return content == null ? "" : content.replaceAll(SIMPLIFY_INDEXING_REGEX, " ");
	}
	
	@Override
	public List<Song> search(IndexType indexType, String searchString, FieldName... fieldsToSearchIn) {
		try {
			Directory directory = getIndex(indexType);
			IndexReader indexReader = DirectoryReader.open(directory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			
			BooleanQuery outerBooleanQuery = new BooleanQuery();
			for (FieldName field : fieldsToSearchIn) {
				PhraseQuery query = new PhraseQuery();
				for (String searchTerm : searchString.toLowerCase().split(TERM_SPLIT_REGEX)) {
					query.add(new Term(field.name(), searchTerm));
				}
				query.setBoost(field.getBoost());
				outerBooleanQuery.add(query, Occur.SHOULD);
			}
			TopDocs hits = indexSearcher.search(outerBooleanQuery, Integer.MAX_VALUE);
			
			LOG.debug("{} hits for filter \"{}\"", hits.totalHits, outerBooleanQuery);
			
			List<Song> ret = new LinkedList<>();
			for (ScoreDoc scoreDocument : hits.scoreDocs) {
				Document document;
				document = indexSearcher.doc(scoreDocument.doc);
				String uuid = document.get(FieldName.UUID.name());
				ret.add(songByUuid.get(uuid));
			}
			return ret;
		} catch (Exception e) {
			LOG.warn("problem while searching", e);
			return new ArrayList<>(0);
		}
	}
	
}
