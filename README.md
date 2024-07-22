# Introduction
This project evaluates [Tantivy](https://github.com/quickwit-oss/tantivy) and [Apache Lucene](https://github.com/apache/lucene) retrieval quality using standard ndcg@10 metric.

This project is based on [Beir](https://github.com/beir-cellar/beir). Index and retrieval task is performed by Tantivy and Lucene in their respective Tust and Java environment. 

Search task is perform on a single field document which concat the title and the content of each document. For both Tantivy and Lucene, we use the default Tokeniser and BM25 scoring with no further tinkering. While Lucene does not require query sanitisation, Tantivy does. To ensure minimal inteference with the implementaiton, we followed the [same santisation method that was mentioned by tantivy maintainer]().

Retrieval results are exported as tsv file which are then scored with [pytrec_eval](https://github.com/cvangysel/pytrec_eval). This approach allows us to manually examin search output and ensure both tantivy and apache is scored by the same code base.

Evaluation datasets are available on [Beir github](https://github.com/beir-cellar/beir).

# Result
Apache lucene match closely with Beir published leaderboard for BM25 flat.
Tantivy score lower that both Lucene and Beir published result.

| Dataset | Tantivy ndcg@10 | Lucene ndcg@10 | Beir BM25 Flat ndcg@10 |
| - | - | - | - |
| Scifact | 0.6251573122952132 | - | - |
| NFCorpus | 0.20505084876906404 | - | - |
| TREC-COVID | 0.0362915780899568 | - | - |

[BEIR leaderboard](https://eval.ai/web/challenges/challenge-page/1897/leaderboard/4475).

# Query explanation
Both tantivy and lucene provide explanation for BM25 score. We picked the first query result from the TREC-COVID corpus where the scores are highly disproportioned for comparision in the following table.


| Tantivy | Lucene |
| - | - |


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
cd retrieval
cargo update
cargo run -- scifact
```
1. If ran successfully, a new file called ```result.tsv``` will be created in the dataset folder

## 3. Runing lucene retrieval task
1. Ensure you have pull the 

## 4. Running evaluation
1. Run the following step to create virtualenv for python and install the necessary packages
```sh
cd evaluation
python3 -m .venv
source .venv/bin/activate
pip install -r requirement.txt
```
1. Run the evaluation script. For instance, we are running it for the scifact corpus
```sh
python main.py scifact
```
