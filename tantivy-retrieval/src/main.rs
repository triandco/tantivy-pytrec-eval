mod types;

use crate::types::LoadError;
use serde_json;
use std::{
    env,
    fs::{self, File},
    io::{BufRead, BufReader},
    path::Path,
};
use tantivy::{
    collector::TopDocs,
    query::QueryParser,
    schema::{Schema, Value, STORED, TEXT},
    Index, ReloadPolicy, TantivyDocument,
};
use types::{Corpus, CorpusItem, Query, RetrievalResult};

fn load_jsonl_corpus(path: &Path) -> Result<Corpus, LoadError> {
    let file = File::open(path).map_err(|e| LoadError::Io(e))?;
    let reader = BufReader::new(file);
    reader
        .lines()
        .into_iter()
        .map(|l| {
            l.map_err(LoadError::Io).and_then(|t| {
                serde_json::from_str::<CorpusItem>(t.as_str()).map_err(LoadError::Json)
            })
        })
        .collect::<Result<Vec<_>, _>>()
        .map(|v| Corpus { items: v })
}

fn load_jsonl_queries(path: &Path) -> Result<Vec<Query>, LoadError> {
    let file = File::open(path).map_err(LoadError::Io)?;
    let reader = BufReader::new(file);
    reader
        .lines()
        .into_iter()
        .map(|l| {
            l.map_err(LoadError::Io)
                .and_then(|t| serde_json::from_str::<Query>(t.as_str()).map_err(LoadError::Json))
        })
        .collect::<Result<Vec<_>, _>>()
}

fn write_result_tsv(path: &Path, result: Vec<RetrievalResult>) {
    let tsv_string = result
        .into_iter()
        .map(|result| format!("{}\t{}\t{}", result.qid, result.doc, result.score))
        .collect::<Vec<_>>()
        .join("\n");

    fs::write(path, tsv_string).expect("Failed to write result to file")
}

fn index_corpus(tantivy_index: &Index, corpus: Corpus) {
    let mut index_writer = tantivy_index
        .writer(50_000_000)
        .expect("Create index writer failed");

    let schema = tantivy_index.schema();

    for corpus_item in corpus.items.into_iter() {
        let id_field = schema.get_field("id").expect("Failed to get id field");
        let title_field = schema
            .get_field("title")
            .expect("Failed to get title field");
        let text_field = schema
            .get_field("text")
            .expect("Failed to get text field");

        let mut document = TantivyDocument::default();
        document.add_text(id_field, &corpus_item.id);
        document.add_text(title_field, &corpus_item.title);
        document.add_text(text_field, &corpus_item.text);

        index_writer
            .add_document(document)
            .expect("Add document successfully");
    }

    index_writer.commit().expect("Commit successfully");
}

/// Remove special characters in queries
/// https://x.com/fulmicoton/status/1810589778825613442
fn santise_query(query: String) -> String {
    let special_characters = vec![
        '+', '-', '!', '(', ')', '{', '}', '[', ']', '^', '"', '~', '*', '?', ':', '\\', '<',
    ];
    query
        .chars()
        .filter(|x| !special_characters.contains(x))
        .collect()
}

fn retrieve(tantivy_index: &Index, queries: Vec<Query>) -> Vec<RetrievalResult> {
    let reader = tantivy_index
        .reader_builder()
        .reload_policy(ReloadPolicy::OnCommitWithDelay)
        .try_into()
        .expect("Failed to create tantivy reader");

    let searcher = reader.searcher();
    let schema = tantivy_index.schema();
    let query_parser = QueryParser::for_index(
        &tantivy_index,
        vec![schema
            .get_field("title")
            .expect("Get title field failed"),
            schema
            .get_field("text")
            .expect("Get text field failed")],
    );

    let id_field = schema.get_field("id").expect("Fail to get id field");

    queries
        .into_iter()
        .flat_map(|query| {
            let sanitised_query = santise_query(query.text);
            let tantivy_query = query_parser
                .parse_query(&sanitised_query)
                .expect("Failed to parse query");
            searcher
                .search(&tantivy_query, &TopDocs::with_limit(10))
                .expect("Failed to search")
                .into_iter()
                .map(|(score, doc_address)| {
                    let tantivy_doc = searcher
                        .doc::<TantivyDocument>(doc_address)
                        .expect("Failed to get doc by address");
                    RetrievalResult {
                        qid: query.id.clone(),
                        doc: tantivy_doc
                            .get_all(id_field)
                            .map(|value| value.as_str())
                            .filter(Option::is_some)
                            .map(Option::unwrap)
                            .fold(String::new(), |state, next| state + next),
                        score,
                    }
                })
                .collect::<Vec<_>>()
        })
        .collect::<Vec<_>>()
}

fn main() -> () {
    let args: Vec<String> = env::args().collect();
    let current_dir = env::current_dir().expect("Failed to get current directory");
    let dataset_path = current_dir.join(Path::new("../data"));
    let dataset_path = dataset_path.join(Path::new(&args[1]));
    let corpus_path = dataset_path.join(Path::new("corpus.jsonl"));
    let queries_path = dataset_path.join(Path::new("queries.jsonl"));
    let result_path = dataset_path.join(Path::new("result_tantivy.tsv"));
    let corpus = load_jsonl_corpus(corpus_path.as_path()).expect(&format!(
        "Failed to load corpus at {}",
        corpus_path.to_str().unwrap()
    ));
    let queries = load_jsonl_queries(queries_path.as_path()).expect("Failed to load queries");

    let mut schema_builder = Schema::builder();
    schema_builder.add_text_field("id", TEXT | STORED);
    schema_builder.add_text_field("title", TEXT);
    schema_builder.add_text_field("text", TEXT);
    let schema = schema_builder.build();
    let tantivy_index =
        tantivy::Index::create_from_tempdir(schema.clone()).expect("Create index failed");

    index_corpus(&tantivy_index, corpus);

    let retrieval_result = retrieve(&tantivy_index, queries);

    write_result_tsv(&result_path, retrieval_result)
}
