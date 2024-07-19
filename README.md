# Introduction
This project evaluates [Tantivy](https://github.com/quickwit-oss/tantivy) retrieval quality using standard ndcg@10 metric.

This project is based on [Beir](https://github.com/beir-cellar/beir). Index and retrieval task is performed by Tantivy. Retrieval result is exported as tsv file which is then scored with [pytrec_eval](https://github.com/cvangysel/pytrec_eval).

Evaluation datasets are available on [Beir github](https://github.com/beir-cellar/beir).

# Result thus far
| Dataset | tantivy ndcg@10 | Beir BM25flat ndcg@10|
|-|-|-|
| Scifact | 0.6251573122952132 | 0.679 |
| NFCorpus | 0.20505084876906404 | 0.322 | 
| TREC-COVID | 0.0362915780899568 | 0.595 |

[BEIR leaderboard](https://eval.ai/web/challenges/challenge-page/1897/leaderboard/4475).


# Prerequiste
This project is built in a linux container as [pytrec_eval is not playing nicely with pip on windows](https://github.com/cvangysel/pytrec_eval/issues/32). If you prefer to run it on your local environment, make sure you have:
* Python 3.9
* cargo lastest

# Running evaluation
## 1. Running tantivy retrieval task
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

## 2. Running evaluation
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
