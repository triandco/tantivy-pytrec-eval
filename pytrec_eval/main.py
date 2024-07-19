import pytrec_eval
import json
import csv 


def load_qrel(file_path):
    """Load judgements into python dictionary.
       { "query_id": { "doc_id": int } }
    """
    result = {}
    with open(file_path, mode='r', newline='') as csvfile:
        reader = csv.reader(csvfile, delimiter='\t')
        next(reader) # skip header
        for row in reader:
            outer_key = row[0]
            inner_key = row[1]
            value = row[2]
            if outer_key not in result:
                result[outer_key] = {}
            result[outer_key][inner_key] = int(value)
    return result

def load_result(file_path):
    """Load result into python dictionary.
       { "query_id": { "doc_id": float } }
    """
    result = {}
    with open(file_path, mode='r', newline='') as csvfile:
        reader = csv.reader(csvfile, delimiter=',')
        for row in reader:
            outer_key = row[0]
            inner_key = row[1]
            value = row[2]
            if outer_key not in result:
                result[outer_key] = {}
            result[outer_key][inner_key] = float(value)
    return result

def main():
    qrel = load_qrel('../data/scifact/qrels/test.tsv')
    result = load_result('../data/scifact/result.csv') 

    evaluator = pytrec_eval.RelevanceEvaluator(
        qrel, {'map', 'ndcg'})

    evaluation_result = evaluator.evaluate(result)
    summation = {}
    for (run_id, measures) in evaluation_result.items():
        for (measurement, score) in measures.items():
            if measurement not in summation : summation[measurement] = score
            else: summation[measurement] += score

    average = {}
    for (measurement, total) in summation.items():
        if measurement not in average: average[measurement] = total / len(evaluation_result)

    print(json.dumps(average, indent=1))

if __name__ == '__main__':
    main()