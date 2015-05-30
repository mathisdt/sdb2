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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zephyrsoft.sdb2.model.Song;

/**
 * An in-memory index based on Lucene.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class IndexerServiceImpl implements IndexerService<Song> {
	
	private static final Logger LOG = LoggerFactory.getLogger(IndexerServiceImpl.class);
	
	private static final String SIMPLIFY_INDEXING_REGEX = "[-\r\n\t.,;:_/+'\"!?()\\[\\]]++";
	private static final String TERM_SPLIT_REGEX = "[- .,;:_/+'\"!?()\\[\\]]++";
	
	private Map<String, Song> songByUuid = new HashMap<>();
	private Map<IndexType, Directory> indexes = new HashMap<>();
	
	@Override
	public void index(IndexType indexType, Collection<Song> songs) {
		Directory directory = indexes.get(indexType);
		if (directory == null) {
			directory = new RAMDirectory();
			indexes.put(indexType, directory);
		}
		Analyzer analyzer = new SimpleAnalyzer();
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
	}
	
	private Document createDocument(Song song) {
		Document document = new Document();
		document.add(new StringField(FieldName.UUID.name(), song.getUUID(), Store.YES));
		document.add(new TextField(FieldName.TITLE.name(), song.getTitle().replaceAll(SIMPLIFY_INDEXING_REGEX, " "),
			Store.NO));
		document.add(new TextField(FieldName.LYRICS.name(), song.getLyrics().replaceAll(SIMPLIFY_INDEXING_REGEX, " "),
			Store.NO));
		return document;
	}
	
	@Override
	public List<Song> search(IndexType indexType, String searchString, FieldName... fieldsToSearchIn) {
		try {
			Directory directory = indexes.get(indexType);
			IndexReader indexReader = DirectoryReader.open(directory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			
			QueryParser queryParser = new QueryParser(FieldName.LYRICS.name(),
				new StandardAnalyzer(new CharArraySet(0, true)));
			queryParser.setAllowLeadingWildcard(true);
			BooleanQuery outerBooleanQuery = new BooleanQuery();
			for (FieldName field : fieldsToSearchIn) {
				BooleanQuery innerBooleanQuery = new BooleanQuery();
				for (String searchTerm : searchString.split(TERM_SPLIT_REGEX)) {
					Query query = queryParser.parse(field.name() + ":*" + searchTerm + "*");
					innerBooleanQuery.add(query, Occur.MUST);
				}
				outerBooleanQuery.add(innerBooleanQuery, Occur.SHOULD);
			}
			TopDocs hits = indexSearcher.search(outerBooleanQuery, Integer.MAX_VALUE);
			
			LOG.debug("{} hits for filter {}", hits.totalHits, searchString);
			
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
	
	@Override
	public void empty(IndexType indexType) {
		indexes.remove(indexType);
	}
	
}
