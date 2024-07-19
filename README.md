# Tantivy search accuracy benchmark
This project evaluates [Tantivy](https://github.com/quickwit-oss/tantivy) retrieval quality using standard metrics such as ndcg, map, precision using [python implementation of trec_eval](https://github.com/cvangysel/pytrec_eval). Retrieval task is done in rust with Tantivy. Its result is exported as tsv file which is then loaded pytrec_eval for evaluation.

We use the same datasets available at [Beir](https://github.com/beir-cellar/beir) and compare tantivy results against that which has been published on [BEIR leaderboard](https://eval.ai/web/challenges/challenge-page/1897/leaderboard/4475).

# Prerequiste
Python 3.9
cargo 1.79.0 (ffa9cf99a 2024-06-03)

Download and unzip dataset into ```.\data``` folder. E.g. If you choose the Scifact dataset your folder should look like
```
data
    scifact
        corpus.jsonl
        queries.jsonl
        qrels
            test.tsv
            dev.tsv
```

# Results
