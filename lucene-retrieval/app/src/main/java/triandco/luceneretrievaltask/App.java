package triandco.luceneretrievaltask;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.KeywordField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class App {
    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        if(args.length == 0) {
            System.out.println("Missing corpus arguments. If you are running gradle try: ./gradlew run --args=\"scifact\"");
            return;
        }
        String corpusName = args[0];
        String corpusPath = currentDir + "/../../data/"+corpusName+"/";
        List<CorpusItem> corpus = LoadCorpus(corpusPath + "corpus.jsonl");
        List<DatasetQuery> queries = LoadQuery(corpusPath + "queries.jsonl");
        String resultPath = corpusPath + "result_lucene.tsv";

        try {
            Path tmpDir = Files.createTempDirectory("lucene-index");
            Directory dir = FSDirectory.open(tmpDir);
            System.out.println("Loaded corpus and query. Begining indexing " + corpus.size() + " item(s).");
            Index(dir, corpus);

            System.out.println("Index corpus complete.\nNow retrieving " + queries.size() + " queries.");
            var result = Search(dir, queries);

            System.out.println("Retrieval complete.\nNow writing to tsv at" + resultPath);
            WriteResult(result, resultPath);
            
        } catch (IOException e){
            e.printStackTrace(System.out);
        }
    }

    public static String TEXT_FIELD = "text";
    public static String TITLE_FIELD = "content";
    public static String ID_FIELD = "id";


    static void Index(Directory dir, List<CorpusItem> corpus){
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        try (IndexWriter writer = new IndexWriter(dir, iwc)) {
            for (CorpusItem corpusItem : corpus) {
                Document doc = new Document();
                TextField titleField = new TextField(TEXT_FIELD, corpusItem.Title, Store.NO);
                TextField contentField = new TextField(TITLE_FIELD,corpusItem.Text, Store.NO);
                KeywordField idField = new KeywordField(ID_FIELD, corpusItem.Id, Store.YES);
                doc.add(contentField);
                doc.add(titleField);
                doc.add(idField);
                writer.addDocument(doc);
            }
            writer.commit();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    static List<QueryResult> Search(Directory dir, List<DatasetQuery> queries) {
        var results = new ArrayList<QueryResult>();
        try {
            var reader = DirectoryReader.open(dir);
            var searcher = new IndexSearcher(reader);
            var analyzer = new StandardAnalyzer();
            var storedField = searcher.storedFields();
            var parser = new MultiFieldQueryParser(new String[]{TITLE_FIELD, TEXT_FIELD}, analyzer);
            for (var q: queries){
                try {
                    var sanitisedQuery = SanitiseQuery(q.Text);
                    var luceneQuery = parser.parse(sanitisedQuery);
                    var docs = searcher.search(luceneQuery, 1000, Sort.RELEVANCE, true);
                    for (var scoreDoc: docs.scoreDocs){
                        var queryResult = new QueryResult();
                        queryResult.QueryId = q.Id;
                        queryResult.DocumentId = storedField.document(scoreDoc.doc).get(ID_FIELD);
                        queryResult.Score = scoreDoc.score;
                        results.add(queryResult);
                    }
                } catch (ParseException e) {
                    e.printStackTrace(System.out);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

        return results;
    }

    static String SanitiseQuery(String query) {
        String charsToRemove = "+-&|!(){}[]^\"~*?:\\/";
        String regex = "[" + Pattern.quote(charsToRemove) + "]";
        return query.replaceAll(regex, "");
    }

    static void WriteResult(List<QueryResult> queryResults, String stringPath) {
        var stringResults = queryResults.stream().map(x -> x.toString()).toList();
        System.out.println("Retrieval completed.");
        var resultString = String.join("\n", stringResults);
        var path = Paths.get(stringPath);
        try {
            Files.writeString(path, resultString, StandardCharsets.UTF_8);
        }catch(IOException e){
            e.printStackTrace(System.out);
        }
    }

    static List<CorpusItem> LoadCorpus(String filePath){
        var objects = new ArrayList<CorpusItem>();
        var mapper = new ObjectMapper();
        var reader = mapper.readerFor(CorpusItem.class);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                CorpusItem obj = reader.readValue(line);
                objects.add(obj);
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

        return objects;
    }
    
    static List<DatasetQuery> LoadQuery(String filePath){
        List<DatasetQuery> objects = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(DatasetQuery.class);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                DatasetQuery obj = reader.readValue(line);
                objects.add(obj);
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

        return objects;
    }
}

@JsonIgnoreProperties(ignoreUnknown=true)
class CorpusItem{
    @JsonProperty("_id")
    public String Id;
    
    @JsonProperty("title")
    public String Title;

    @JsonProperty("text")
    public String Text;

    @Override
    public String toString() {
        return "CorpusItem{" +
                "_id='" + Id + '\'' +
                ", text='" + Text + '\'' +
                ", title='" + Title + '\'' +
                '}';
    }
}

@JsonIgnoreProperties(ignoreUnknown=true)
class DatasetQuery {
    @JsonProperty("_id")
    public String Id;

    @JsonProperty("text")
    public String Text;

    @Override
    public String toString() {
        return "Query{" +
                "_id='" + Id + '\'' +
                ", text='" + Text + '\'' +
                '}';
    }
}

class QueryResult {
    String QueryId;
    String DocumentId;
    Float Score;

    @Override
    public String toString() {
        return QueryId+"\t"+DocumentId+"\t"+Score;
    }
}
