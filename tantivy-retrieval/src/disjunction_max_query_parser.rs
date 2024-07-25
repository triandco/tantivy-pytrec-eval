use std::collections::HashMap;

use tantivy::{
    query::{BooleanQuery, DisjunctionMaxQuery, Query}, schema::Field, tokenizer::TextAnalyzer, Index, Score, TantivyError, Term
};

pub struct DisjunctionMaxQueryParser {
    text_analyzer: HashMap<Field, TextAnalyzer>,
}

impl DisjunctionMaxQueryParser {
    pub fn new(
        index: &Index,
        fields: Vec<&str>,
    ) -> Result<DisjunctionMaxQueryParser, TantivyError> {
        let t = fields
            .clone()
            .into_iter()
            .map(|field_name| {
                let field = index.schema().get_field(field_name)?;
                let tokenizer = index.tokenizer_for_field(field.clone())?;
                Ok::<_, TantivyError>((field, tokenizer))
            })
            .collect::<Result<Vec<_>, _>>()?;

        Ok(DisjunctionMaxQueryParser {
            text_analyzer: t.into_iter().collect(),
        })
    }

    pub fn parse(&self, query: &str, tie_breaker: f32) -> DisjunctionMaxQuery {
        let field_query = self
            .text_analyzer
            .clone()
            .into_iter()
            .map(|(field, mut text_analyzer)| {
                let mut token_stream = text_analyzer.token_stream(query);
                let mut terms = vec![];
                token_stream.process(&mut |token| {
                    let term = Term::from_field_text(field, &token.text);
                    terms.push(term);
                });
                Box::new(BooleanQuery::new_multiterms_query(terms)) as Box<dyn Query>
            })
            .collect::<Vec<_>>();

        DisjunctionMaxQuery::with_tie_breaker(field_query, tie_breaker)
    }
}
