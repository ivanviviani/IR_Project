// import java.util.Arrays;
// import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

public class Fusion {

    /**
     * Compute a new run using combMNZ fusion method.
     * @param superRun Matrix [topic][system] of Runs for all topics and systems.
     * @param topic Number of the considered topic.
     * @param systems Array with the numbers of the considered systems.
     * @return The fused run.
     */
    public static Run combMNZ(Run[][] superRun, int topic, int[] systems) {

        /* 
         * For every ranked document, accumulate the sum of all the scores
         * it gets in the different runs and count the runs where it appears.
         * All documents, their cumulative score and their presence count
         * are stored in a hash map.
          */
        HashMap<String,RunEntry> collection = new HashMap<>();
        HashMap<String,Integer> counter = new HashMap<>();

        // Scan chosen systems (for the fixed topic) = Scan their Runs
        for(int i = 0; i < systems.length; i++) {
            
            // Now superRun[topic][systems[i]] = Run for [topic][system]
            Run currRun = superRun[topic][systems[i]];

            // Search inside the current Run
            for(RunEntry ds : currRun.list) {
                // Get document id and score
                String currID = ds.docID;
                double currScore = ds.docScore;
                // If already present in the hash map, update
                if(collection.containsKey(currID)) {
                    RunEntry currDS = collection.get(currID);
                    currDS.increaseScore(currScore);
                    counter.replace(currID, counter.get(currID) + 1);
                }
                else { // If missing, add
                    collection.put(currID, new RunEntry(currID,currScore));
                    counter.put(currID,1);
                }
            }
            
        }

        /*
         * Convert the hashmap to Run, then update the score of each document
         * multiplying it by the number of occurrences in the runs. The resulting
         * run is sorted by score and trimmed to the first 1000 results.
         */
        Run finalRun = new Run(collection.values());
        // Scan the run and update document scores
        for(RunEntry ds: finalRun.list) {
            // Current document id and score
            String currID = ds.docID;
            // Number of runs where the document appears
            Integer numRuns = counter.get(currID);
            // Update score
            ds.multiplyScore(numRuns);
        }

        // Sort the final run by document score
        Util.quickSort(finalRun, (x, y) -> (x.docScore-y.docScore>0)?(1):((x.docScore-y.docScore<0)?(-1):0));
        // Trim it to the first 1000 results
        finalRun = Util.extractTop(finalRun, 1000);

        // Return the final run
        return finalRun;
    }

    /**
     * Compute a new run using rCombMNZ fusion method.
     * @param superRun Matrix [topic][system] of Runs for all topics and systems.
     * @param topic Number of the considered topic.
     * @param systems Array with the numbers of the considered systems.
     * @return The fused run.
     */
    public static Run rCombMNZ(Run[][] superRun, int topic, int[]systems) {

        /*
         * For every ranked document, assign r - # doc points, then
         * accumulate the sum of all the points and consider it as the
         * score for the document, moreover it count the runs where each
         * document appears. All documents, their cumulative score and
         * their presence count are stored in an hashmap.
         */
        HashMap<String,RunEntry> collection = new HashMap<>();
        HashMap<String,Integer> counter = new HashMap<>();

        // Scan chosen systems (for the fixed topic) = Scan their Runs
        for(int i = 0; i < systems.length; i++) {
            
            // Now superRun[topic][systems[i]] = Run for [topic][system]
            Run currRun = superRun[topic][systems[i]];
            // Get the list and its length
            ArrayList<RunEntry> runList = currRun.list;
            int runLen = runList.size();
            // Search inside the current Run
            for(RunEntry ds : runList) {
                // Current document id and score
                String currID = ds.docID;
                // Current document position (index)
                int pos = runList.indexOf(ds);
                double currScore = runLen - 1 - pos; 
                // If already present in the hash map, update
                if(collection.containsKey(currID)) {
                    RunEntry currDS = collection.get(currID);
                    currDS.increaseScore(currScore);
                    counter.replace(currID, counter.get(currID) + 1);
                }
                else { // If missing, add
                    collection.put(currID, new RunEntry(currID,currScore));
                    counter.put(currID,1);
                }
            }
        }

        /*
         * Convert the hash map to array, then update the points of each document
         * multiplying it by the number of occurrences in the runs. The resulting
         * run is sorted by score and trimmed to the first 1000 results.
         */
        Run finalRun = new Run(collection.values());
        // Scan the run and update document scores
        for(RunEntry ds: finalRun.list) {
            // Current document id and score
            String currID = ds.docID;
            // Number of runs where the document appears
            Integer numRuns = counter.get(currID);
            // Update score
            ds.multiplyScore(numRuns);
        }

        // Sort the final run by document score
        Util.quickSort(finalRun, (x, y) -> (x.docScore-y.docScore>0)?(1):((x.docScore-y.docScore<0)?(-1):0));
        // Trim it to the first 1000 results
        finalRun = Util.extractTop(finalRun, 1000);

        // Return the final run
        return finalRun;
    }

    /**
     * Compute a new run using bordaFuse fusion method.
     * @param superRun Matrix [topic][system] of Runs for all topics and systems.
     * @param topic Number of the considered topic.
     * @param systems Array with the numbers of the considered systems.
     * @return The fused run.
     */
    public static Run bordaFuse(Run[][] superRun, int topic, int[] systems) {

        /*
         * For every ranked document, assign r - # doc points, then
         * accumulate the sum of all the points and consider it as the
         * score for the document. All documents and their
         * cumulative points are stored in a hash map.
         */
        HashMap<String,RunEntry> collection = new HashMap<>();

        // Scan chosen systems (for the fixed topic) = Scan their Runs
        for(int i = 0; i < systems.length; i++) {
            
            // Now superRun[topic][systems[i]] = Run for [topic][system]
            Run currRun = superRun[topic][systems[i]];
            // Get the list and its length
            ArrayList<RunEntry> runList = currRun.list;
            int runLen = runList.size();
            // Search inside the current Run
            for(RunEntry ds : runList) {
                // Current document id and score
                String currID = ds.docID;
                // Current document position (index)
                int pos = runList.indexOf(ds);
                double currScore = runLen - 1 - pos; 
                // If already present in the hash map, update
                if(collection.containsKey(currID)) {
                    RunEntry currDS = collection.get(currID);
                    currDS.increaseScore(currScore);
                }
                else { // If missing, add
                    collection.put(currID, new RunEntry(currID, currScore));
                }
            }
        }

        /*
         * Convert the hash map to array, then the resulting run is sorted
         * by score (sum of points) and trimmed to the first 1000 results.
         */
        Run finalRun = new Run(collection.values());

        // Sort the final run by document score
        Util.quickSort(finalRun, (x, y) -> (x.docScore-y.docScore>0)?(1):((x.docScore-y.docScore<0)?(-1):0));
        // Trim it to the first 1000 results
        finalRun = Util.extractTop(finalRun, 1000);

        // Return the final run 
        return finalRun;
    }

    /**
     * Compute a new run using condorcetFuse fusion method.
     * @param superRun Matrix [topic][system] of Runs for all topics and systems.
     * @param topic Number of the considered topic.
     * @param systems Array with the numbers of the considered systems.
     * @param support
     * @return The fused run.
     */
    public static Run condorcetFuse(Run[][] superRun, int topic, int[] systems, HashMap<String,Integer[]> support) {
        /*
         * Create a list containing all the documents in the runs,
         * putting them in a hashset.
         */
        HashSet<String> collection = new HashSet<>();

        // Scan chosen systems (for the fixed topic) = Scan their Runs
        for(int i = 0; i < systems.length; i++) {
            
            // Now superRun[topic][systems[i]] = Run for [topic][system]
            Run currRun = superRun[topic][systems[i]];

            // Search inside the current Run
            for(RunEntry ds : currRun.list) {
                // Current document id
                String currID = ds.docID;
                collection.add(currID);
            }
        }

        /*
         * The obtained list of documents is converted to a list of entries
         * and then sorted.
         */
        String[] ids = collection.toArray(new String[0]);
        Run finalRun = new Run();
        ArrayList<RunEntry> runList = finalRun.list;
        for(int i = 0; i < ids.length; i++) {
            runList.add(new RunEntry(ids[i], 1/(i+1.0))); // symbolic score
        }

        // Sort the final run by document score
        Util.quickSort(finalRun, new MajRunoffComparator(systems, support));
        // Trim it to the first 1000 results
        finalRun = Util.extractTop(finalRun, 1000);

        // Return the final run
        return finalRun;

    }

}