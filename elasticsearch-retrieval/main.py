from ssl import create_default_context
from elasticsearch import Elasticsearch as ElasticSearch
from elasticsearch.helpers import bulk

import json
import csv
import os
import sys

class CorpusItem:
    id: str
    text: str
    title: str

    def __init__(self, _id, _text, _title): 
        self.id = _id
        self.text = _text 
        self.title = _title

    def __str__(self) -> str:
        return f"CorpusItem({self.id}\t{self.title}\t{self.text})"

class DatasetQuery:
    id: str
    text: str

    def __init__(self, _id:str, _text:str):
        self.id = _id
        self.text = _text

    def __str__(self) -> str:
        return f"Query({self.id}: {self.text})"

class QueryResult: 
    qid: str
    doc_id: str
    score: float

    def __init__(self, _qid:str, _doc_id:str, _score:float):
        self.qid = _qid
        self.doc_id = _doc_id
        self.score = _score

    def __str__(self) -> str:
        return f"QueryResult({self.qid}\t{self.doc_id}\t{self.score})"


def main():

    dataset = sys.argv[1]
    script_dir = os.path.dirname(os.path.abspath(sys.argv[0]))
    data_path = script_dir+"/../data/"+dataset

    elastic_search = ElasticSearch(
        ['https://localhost:9200'],
        basic_auth=('elastic', 'TpOVaMXdOntY7LNIoRUz'),
        ssl_context=create_default_context(cafile=script_dir+"/elasticsearch/config/certs/http_ca.crt")
    )

    corpus = load_corpus(data_path+"/corpus.jsonl")
    print(f"Corpus loaded with {len(corpus)} item(s)")

    queries = load_queries(data_path+"/queries.jsonl")
    print(f"Queries loaded with {len(queries)} item(s)")

    index(elastic_search, dataset, corpus)

    results = search(elastic_search, dataset, queries)

    print(f"A total of {len(results)} result(s) collected")
    with open(data_path+"/result_elasticsearch.tsv", mode='w') as file:
        tsv_writer = csv.writer(file, delimiter='\t')
        for result in results:
                tsv_writer.writerow((result.qid, result.doc_id, result.score))

def load_queries(path) -> list[DatasetQuery]:
    queries = []
    with open(path) as file:
        for line in file:
            json_obj = json.loads(line)
            queries.append(DatasetQuery(json_obj.get("_id"), json_obj.get("text")))

    return queries

def load_corpus(path) -> list[CorpusItem]:
    corpus = []
    with open(path) as file:
        for line in file:
            json_obj = json.loads(line)
            corpus.append(CorpusItem(json_obj.get("_id"), json_obj.get("title"), json_obj.get("text")))
    return corpus

def search(es: ElasticSearch, index_name: str, queries: list[DatasetQuery]) -> list[QueryResult]:
    output = []
    for query in queries: 
        req_body = {"query" : {"multi_match": {
                "query": query.text, 
                "type": "best_fields",
                "fields": ["text", "title"],
                "tie_breaker": 0.5
                }},
                "size": 1000}
        
        results = es.search(
            index = index_name, 
            body = req_body
        )
        for hit in results["hits"]["hits"]:
            output.append(QueryResult(query.id, hit["_id"], hit['_score']))
    
    return output
        
def split_array(arr: list, n:int) -> list[list]:
    k, m = divmod(len(arr), n)
    return [arr[i * k + min(i, m):(i + 1) * k + min(i + 1, m)] for i in range(n)]
    
def index(es: ElasticSearch, index_name: str, corpus: list[CorpusItem]):
    if es.indices.exists(index=index_name):
        es.indices.delete(index=index_name)

    mapping = {
        "mappings" : {
            "properties" : {
                "title": {"type": "text", "analyzer": "english"},
                "text": {"type": "text", "analyzer": "english"}
            }}}
    
    es.indices.create(index=index_name, body=mapping)

    operations = []
    for doc in corpus: 
        operations.append({
            '_index': index_name,
            '_op_type': 'create',
            "_id": doc.id,
            "_source": {
                "text": doc.text, 
                "title": doc.title
            }
        })

    bulk(es, operations, refresh="true")
        
if __name__ == '__main__':
    main()