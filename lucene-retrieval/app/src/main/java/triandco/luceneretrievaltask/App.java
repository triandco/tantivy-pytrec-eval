package triandco.luceneretrievaltask;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class App {
    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        List<CorpusItem> corpus = Loader.LoadCorpus(currentDir + "/../../data/scifact/corpus.jsonl");
        List<Query> item = Loader.LoadQuery(currentDir + "/../../data/scifact/query.jsonl");
        System.out.println(item.toString());
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
class Query {
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
    Double Score;
}

class Loader {
    public static List<CorpusItem> LoadCorpus(String filePath){
        List<CorpusItem> objects = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(CorpusItem.class);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                CorpusItem obj = reader.readValue(line);
                objects.add(obj);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return objects;
    }
    
    public static List<Query> LoadQuery(String filePath){
        List<Query> objects = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(Query.class);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Query obj = reader.readValue(line);
                objects.add(obj);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return objects;
    }
}

