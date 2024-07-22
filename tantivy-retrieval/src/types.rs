use std::num::ParseFloatError;

use serde::Deserialize;


#[derive(Deserialize, Clone)]
pub struct CorpusItem {
    #[serde(rename = "_id")]
    pub id: String,
    pub title: String,
    pub text: String,
}

#[derive(Deserialize, Clone)]
pub struct Corpus {
    pub items: Vec<CorpusItem>,
}

#[derive(Deserialize, Clone)]
pub struct Query {
    #[serde(rename = "_id")]
    pub id: String,
    pub text: String
}


#[derive(Debug)]
pub enum LoadError {
    Io(std::io::Error),
    Json(serde_json::error::Error),
    Csv(csv::Error),
    ParseError(ParseFloatError),
}

#[derive(Deserialize, Clone)]
pub struct RetrievalResult {
    #[serde(rename = "_id")]
    pub qid: String,
    pub doc: String,
    pub score: f32
}
