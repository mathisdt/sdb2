/*
 * This file is part of the Song Database (SDB).
 *
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License 3.0 as published by
 * the Free Software Foundation.
 *
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License 3.0 for more details.
 *
 * You should have received a copy of the GNU General Public License 3.0
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.Song;

import com.google.common.base.Stopwatch;

/**
 * An in-memory index based on Lucene.
 */
public class IndexerService {
	
	private static final Logger LOG = LoggerFactory.getLogger(IndexerService.class);
	
	private static final String SIMPLIFY_INDEXING_REGEX = "[-\r\n\t.,;:_/+'\"!?()\\[\\]]++";
	public static final String TERM_SPLIT_REGEX = "[- .,;:_/+'\"!?()\\[\\]]++";
	
	private Map<String, Song> songByUuid = new HashMap<>();
	
	private Map<IndexType, Directory> indexes = new EnumMap<>(IndexType.class);
	private static final Object INDEXES_LOCK = new Object();
	
	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	
	private final ArrayList<OnIndexChangeListener> onIndexChangeListeners = new ArrayList<>();
	
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
	
	public void index(final IndexType indexType, final Collection<Song> songs) {
		executor.execute(() -> {
			Stopwatch stopwatch = Stopwatch.createStarted();
			
			Directory directory = new ByteBuffersDirectory();
			try (Analyzer analyzer = CustomAnalyzer.builder()
				.withTokenizer(StandardTokenizerFactory.class)
				.addTokenFilter(LowerCaseFilterFactory.class)
				.addTokenFilter(NGramFilterFactory.class, "minGramSize", "1", "maxGramSize", "25")
				.build();
				IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
				for (Song song : songs) {
					Document document = createDocument(song);
					writer.addDocument(document);
					songByUuid.put(song.getUUID(), song);
				}
			} catch (IOException e) {
				LOG.warn("couldn't create analyzer and index songs", e);
			} finally {
				putIndex(indexType, directory);
				stopwatch.stop();
				LOG.info("indexing songs in background thread took {}", stopwatch);
				onIndexChangeListeners.forEach(l -> l.onIndexChange());
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
	
	public List<Song> search(IndexType indexType, String searchString, FieldName... fieldsToSearchIn) {
		try {
			Directory directory = getIndex(indexType);
			IndexReader indexReader = DirectoryReader.open(directory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			
			BooleanQuery.Builder outerBooleanQueryBuilder = new BooleanQuery.Builder();
			for (FieldName field : fieldsToSearchIn) {
				PhraseQuery.Builder phraseQueryBuilder = new PhraseQuery.Builder();
				for (String searchTerm : searchString.toLowerCase().split(TERM_SPLIT_REGEX)) {
					phraseQueryBuilder.add(new Term(field.name(), searchTerm));
				}
				PhraseQuery phraseQuery = phraseQueryBuilder.build();
				outerBooleanQueryBuilder.add(phraseQuery, Occur.SHOULD);
			}
			BooleanQuery outerBooleanQuery = outerBooleanQueryBuilder.build();
			TopDocs hits = indexSearcher.search(outerBooleanQuery, Integer.MAX_VALUE);
			StoredFields storedFields = indexSearcher.storedFields();
			
			LOG.trace("{} hits for filter \"{}\"", hits.totalHits, outerBooleanQueryBuilder);
			
			List<Song> ret = new ArrayList<>();
			for (ScoreDoc scoreDocument : hits.scoreDocs) {
				Document document = storedFields.document(scoreDocument.doc);
				String uuid = document.get(FieldName.UUID.name());
				ret.add(songByUuid.get(uuid));
			}
			return ret;
		} catch (Exception e) {
			LOG.warn("problem while searching", e);
			return new ArrayList<>(0);
		}
	}
	
	public void onIndexChange(OnIndexChangeListener onIndexChangeListener) {
		onIndexChangeListeners.add(onIndexChangeListener);
	}
	
	public void removeOnIndexChange(OnIndexChangeListener onIndexChangeListener) {
		onIndexChangeListeners.remove(onIndexChangeListener);
	}
	
	public interface OnIndexChangeListener {
		void onIndexChange();
	}
	
}
