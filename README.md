# Introduction
This project evaluates [Tantivy](https://github.com/quickwit-oss/tantivy), [Elastic Search](https://github.com/elastic/elasticsearch) and [Apache Lucene](https://github.com/apache/lucene) retrieval quality using standard ndcg@10 metric.

This project is based on [Beir](https://github.com/beir-cellar/beir). Index and retrieval task is performed by Tantivy and Lucene in their respective Tust and Java environment. 

For all candidate, we use the default settings for Tokenisation and BM25 settings. We use the default QueryParser for both Lucene and Tantivy which require query sanitisation since queries in multiple dataset contains special characters. Our sanitisation method was the removal of special character for each implementation. Meanwhile, for ElasticSearch, we use non-sanitised query string with a "type='best_field'" settings.

Retrieval results are exported as tsv file which are then scored with [pytrec_eval](https://github.com/cvangysel/pytrec_eval). This approach allows us to manually examine search output and ensure both Tantivy's and Lucene's performance is scored by the same code base.

Evaluation datasets are available on [Beir github](https://github.com/beir-cellar/beir).

# Results
| Dataset | Tantivy ndcg@10 | Apache Lucene ndcg@10 | [Beir BM25 Flat ndcg@10]((https://eval.ai/web/challenges/challenge-page/1897/leaderboard/4475)) | Elastic Search |
| - | - | - | - |
| Scifact | 0.6251573122952132 | 0.632431156289918 | 0.679 | 0.6563018879997284 |
| NFCorpus | 0.20505084876906404 | 0.20712280950112716 | 0.322 | 0.2116375800036891 |
| TREC-COVID | 0.0362915780899568 | 0.035369826134136535 | 0.595 | 0.05433894833185797 |
| NQ | 0.2637953053727399 | 0.2803606345656689 | 0.306 | 0.310128528137924 |

# Running evaluation
## 1. Prerequiste
This project is built in a linux container as [pytrec_eval is not playing nicely with pip on windows](https://github.com/cvangysel/pytrec_eval/issues/32). If you prefer to run it on your local environment, make sure you have:
* Python 3.9
* cargo lastest
* Java latest with OpenJDK and gradle

## 2. Running tantivy retrieval task
1. Download and unzip a [dataset](https://github.com/beir-cellar/beir) into ```.\data``` folder. For instance, if you choose the Scifact dataset your folder should look like
```
data
    scifact
        corpus.jsonl
        queries.jsonl
        qrels
            test.tsv
            dev.tsv
```
1. Run the following step to generate result for tantivy retrieval task. For instance, we are running retrieval for scifact corpus
```sh
cd tantivy-retrieval
cargo update
cargo run -- scifact
```
1. If ran successfully, a new file called ```result_tantivy.tsv``` will be created in the dataset folder

## 3. Runing lucene retrieval task
1. Run the following step to geenrate result for lucene retrieval task. For instance, we are running retrieval for scifact corpus
```sh
cd lucene-retrieval
./gralew run --args="scifact"
```
2. Result of the run will be added to the dataset folder with the name ``result_lucene.tsv``

## 4. Running evaluation
1. Run the following step to create virtualenv for python and install the necessary packages
```sh
cd evaluation
python3 -m .venv
source .venv/bin/activate
pip install -r requirement.txt
```
1. Run the evaluation script. For instance, we are evaluate tantivy performance on the scifact corpus
```sh
python main.py scifact tantivy
```
