# IR_Project
Replication of results presented in "Condorcet Fusion for Improved Retrieval".

DATASET
* Folders "TREC-3", "TREC-5", "TREC-9" contain the files of the runs.
* Files "T3-j.txt", "T5-j.txt", "T9-j.txt" contain relevance judgements for the respective TREC edition.

CODE
* Class "BestToWorstExperiment" contains best-to-worst fusion experiment.
* Class "RadomSetExperiment" contains random-sets experiment.
* Class "Reader" contains utility tools for reading from the dataset files, both input run and relevance judgements.
* Class "Fusion" contains static methods that perform the following fusion techniques: combMNZ, rCombMNZ, bordaFuse, condorcetFuse.
* Class "Util" contains utility subroutines related to fusion methods (eg. sorting, comparison, ...) and average precision metric computation.
* Class "Run" contains a list representation of a run for a fixed system and topic. A set of runs for every topic and system is represented by a matrix \[topic\]x\[system\] of Runs.
* Class "RunEntry" contains the representation of a document in a run as a pair (document id, document score).