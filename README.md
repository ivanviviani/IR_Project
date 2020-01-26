# IR_Project
Replication of results presented in "Condorcet Fusion for Improved Retrieval"
DATASET
Folders "TREC-3", "TREC-5", "TREC-9" contain files that contain runs.
Files "T3-j.txt", "T5-j.txt", "T9-j.txt" contain relevance judgement for the respective TREC edition.
CODE
Class "BestFusionExperiment" contains best-to-worst fusion experiment.
Class "RadomSetExperiment" contains random-sets experiment.
Class "Reader" contains utility tools for reading from the dataset files, both input run and relevance judgement.
Class "Fusion" contains static methods that perform many fusion techniques.
Class "Util" contains utility subroutines related to fusion methods (eg. sorting, comparison...) and compute the average precision metric.
Class "RunEntry" contains a basic representation of a document in a run; a run is represented by an array of "RunEntry", a set of runs is represented by a matrix of "RunEntry".