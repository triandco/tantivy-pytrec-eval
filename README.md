# Introduction
This project evaluates [Tantivy](https://github.com/quickwit-oss/tantivy) retrieval quality using standard metrics such as ndcg@10, map@10, precision@10, and recall@10 with [python implementation of trec_eval](https://github.com/cvangysel/pytrec_eval). Index and retrieval task is performed by Tantivy. Retrieval result is exported as tsv file which is then loaded pytrec_eval for evaluation.

This project is based on [Beir](https://github.com/beir-cellar/beir). 

We use the same datasets available at [Beir](https://github.com/beir-cellar/beir) and compare tantivy results against that which has been published on [BEIR leaderboard](https://eval.ai/web/challenges/challenge-page/1897/leaderboard/4475).

# Result
| Dataset | ndcg@10 |
|-|-|
| Scifact | 0.6251573122952132 |
| NFCorpus | 0.20505084876906404 |


# Prerequiste
This project is built in a linux container as [pytrec_eval is not playing nice with pip on windows](https://github.com/cvangysel/pytrec_eval/issues/32). If you prefer to run it on your local environment, make sure you have:
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
python main.py ../data/scifact
```
