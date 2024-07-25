# Introduction
This project evaluates [Tantivy](https://github.com/quickwit-oss/tantivy), [Elastic Search](https://github.com/elastic/elasticsearch) and [Apache Lucene](https://github.com/apache/lucene) retrieval quality using [NDCG](https://en.wikipedia.org/wiki/Discounted_cumulative_gain).

This project is based on [Beir](https://github.com/beir-cellar/beir). Index and retrieval task is performed by Tantivy and Lucene in their respective Tust and Java environment. 

## Retrieval task configuration
| Name | Engine | Tokeniser | BM25 settings | Query style |
| - | - | - | - | - |
| Tantivy default | Tantivy | Default | Default (K1=1.2, B=0.75) | Multifiled |
| Tantivy disjunction max | Tantivy | Default | Default (K1=1.2, B=0.75) | Disjunction max |
| Apache Lucene default | Apache lucene | Default | Default(K1=1.2, B=0.75) | Multifield |
| Elastic Search default | Elastic search | Default | Default(K1=1.2, B=0.75) | Disjunction max |

Retrieval results are exported as tsv file which are then scored with [pytrec_eval](https://github.com/cvangysel/pytrec_eval). This approach allows us to manually examine search output and ensure each engine's performance is scored by the same code base.

Evaluation datasets are available on [Beir github](https://github.com/beir-cellar/beir).

# NDCG@10 results
| Dataset | Tantivy multifield | Tantivy disjunction max | Apache Lucene default | [Beir BM25 Flat]((https://eval.ai/web/challenges/challenge-page/1897/leaderboard/4475)) | Elastic Search disjunction max |
| - | - | - | - | - | - |
| Scifact | 0.6110550406527024 | 0.6518504567299743 | 0.6105774540257333 | 0.679 | 0.6563018879997284 |
| NFCorpus | 0.20174488628325865 | 0.21048722765891772 | 0.2021653197430468 | 0.322 | 0.2116375800036891 |
| TREC-COVID | 0.03640657024103224 | 0.04495782186916706 | 0.03705072222267741 | 0.595 | 0.05433894833185797 |
| NQ | 0.30181710921729077 | 0.24489390409423747 | 0.301753090384626 | 0.306 | 0.310128528137924 |

# Running evaluation
## 1. Prerequiste
1. This project is built in a linux container as [pytrec_eval is not playing nicely with pip on windows](https://github.com/cvangysel/pytrec_eval/issues/32). If you prefer to run it on your local environment, make sure you have:
    * Python 3.9
    * cargo lastest
    * Java latest with OpenJDK and gradle
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

## 2. Running tantivy retrieval task

1. Run the following step to generate result for tantivy retrieval task. For instance, we are running retrieval for scifact corpus
```sh
cd tantivy-retrieval
cargo update
cargo run -- scifact
```

For retrieval task using disjunction max query
```sh
cd tantivy-retrieval
cargo update
cargo run -- scifact dismax
```

1. If ran successfully, a new file called ```result_tantivy.tsv``` will be created in the dataset folder

## 3. Runing lucene retrieval task
1. Run the following step to geenrate result for lucene retrieval task. For instance, we are running retrieval for scifact corpus
```sh
cd lucene-retrieval
./gralew run --args="scifact"
```
2. Result of the run will be added to the dataset folder with the name ``result_lucene.tsv``

## 4. Running elastic search retrieval task
1. [Download and install self-managed version of elastic search for your platform](https://www.elastic.co/guide/en/elasticsearch/reference/current/install-elasticsearch.html)
2. Update elastic search connection details in main.py
3. Setup environment
```sh
cd elasticsearch-retrieval
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirement.txt
```
4. Run the evaluation task. For instance, we are evaluate tantivy performance on the scifact corpus
```ssh
python3 main.py scifact
```

## 5. Running evaluation
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
