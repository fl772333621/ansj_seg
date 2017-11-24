package org.ansj.ansj_lucene_plug;

import org.ansj.lucene6.AnsjAnalyzer;
import org.ansj.lucene6.AnsjAnalyzer.TYPE;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.io.StringReader;

public class HeightLightTest2 {

	private static Directory directory = new RAMDirectory();

	private static Analyzer indexAnalyzer = new AnsjAnalyzer(TYPE.index_ansj);

	private static Analyzer queryAnalyzer = new AnsjAnalyzer(TYPE.index_ansj);

	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException {
		String content = "<span\",\n" +
				"\"rgb(123, 12, 0);\">。而4月4日即将公开的作为WINNER其中一首主打曲《really really》的mv。虽然画面公开只有短短的10秒钟，但抓耳的旋律一下就吸引了粉丝的目光。" ;


		System.out.println(IndexAnalysis.parse(content));

		String query = "text:\"really\"";

		// 建立内存索引对象
		index(indexAnalyzer, content);

		// 查询
		search(queryAnalyzer, new QueryParser("text", queryAnalyzer).parse(query));
	}

	private static void search(Analyzer analyzer, Query query) throws IOException {
		DirectoryReader directoryReader = DirectoryReader.open(directory);
		// 查询索引
		IndexSearcher isearcher = new IndexSearcher(directoryReader);
		TopDocs hits = isearcher.search(query, 5);
		for (int i = 0; i < hits.scoreDocs.length; i++) {
			int docId = hits.scoreDocs[i].doc;
			Document document = isearcher.doc(docId);
			System.out.println(toHighlighter(analyzer, query, document));
		}
	}

	/**
	 * 高亮设置
	 * 
	 * @param query
	 * @param doc
	 * @return
	 */
	private static String toHighlighter(Analyzer analyzer, Query query, Document doc) {
		String field = "text";
		try {
			SimpleHTMLFormatter simpleHtmlFormatter = new SimpleHTMLFormatter("<font color=\"red\">", "</font>");
			Highlighter highlighter = new Highlighter(simpleHtmlFormatter, new QueryScorer(query));
			TokenStream tokenStream1 = indexAnalyzer.tokenStream("text", new StringReader(doc.get(field)));
			String highlighterStr = highlighter.getBestFragment(tokenStream1, doc.get(field));
			return highlighterStr == null ? doc.get(field) : highlighterStr;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTokenOffsetsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static void index(Analyzer analysis, String content) throws CorruptIndexException, IOException {
		Document doc = new Document();
		IndexWriter iwriter = new IndexWriter(directory, new IndexWriterConfig(analysis));
		doc.add(new TextField("text", content, Field.Store.YES));
		iwriter.addDocument(doc);
		iwriter.commit();
		iwriter.close();
	}
}