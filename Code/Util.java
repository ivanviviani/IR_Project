import java.util.*;

public class Util {

    /**
     * Get the first k entries (RunEntry) of a Run.
     * @param run Run to elaborate.
     * @param k Number of entries (RunEntry) to extract.
     * @return Trimmed run.
     */
    public static Run extractTop(Run run, int k) {

        // Check length of the run
        if(run.list.size() < k) return run;
        // Initialization of the return value
        Run trimmedRun = new Run();
        // Fill the new list
        for(int i = 0; i < k; i++) {
            trimmedRun.list.add(run.list.get(i));
        }
        // Return trimmed run
        return trimmedRun;
    }

    /**
     * Compute the avergae precision for the run.
     * @param run Run to evaluate.
     * @param topicRelDocs Hash set of relevant documents for a topic.
     * @return Average precision of the run.
     */
    public static double averagePrecision(Run run, HashSet<String> topicRelDocs) {

        // Initialization of the return value
        double averagePrecision = 0;
        // Recall base
        int recallBase = Math.max(topicRelDocs.size(), 1);
        // Number of relevant documents retrieved so far
        int relFound = 0;

        // Get the list of RunEntry
        ArrayList<RunEntry> runList = run.list;
        // Scan the run
        for(int i = 0; i < runList.size(); i++) {
            // Check document relevance
            if(topicRelDocs.contains(runList.get(i).docID)) {
                // Relevant document retrieved found
                relFound++;
                // Update average precision
                averagePrecision += relFound / (i + 1.0);
            }
        }
        // Divide by the recall base
        averagePrecision = averagePrecision / recallBase;

        // Return average precision
        return averagePrecision;
    }

    /**
     * Sort the list field of a run by decreasing score field of RunEntry.
     * @param run Run to sort.
     * @param cmp Comparator to use in the sorting process.
     */
    public static void quickSort(Run run, Comparator<RunEntry> cmp) {

        Random r = new Random();
        // Check run length
        if(run.list.size() < 2) return;

        // Get the list
        ArrayList<RunEntry> runList = run.list;
        // Choose pivot
        RunEntry pivot = runList.get(r.nextInt(runList.size()));
        // Initialize greater-equal-lower runs
        Run grtRun = new Run();
        Run eqlRun = new Run();
        Run lwrRun = new Run();
        // Scan run list and fill the three runs (their lists)
        for(RunEntry ds : runList) {
            // Get comparison result
            int result = cmp.compare(pivot, ds);
            // Add current element to the appropriate list
            if(result < 0) grtRun.list.add(ds);
            else if(result > 0) lwrRun.list.add(ds);
                 else eqlRun.list.add(ds);
        }
        // Recursively sort the greater and lower runs
        quickSort(grtRun, cmp);
        quickSort(lwrRun, cmp);
        // Get the three lists as arrays
        RunEntry[] grt = grtRun.list.toArray(new RunEntry[0]);
        RunEntry[] eql = eqlRun.list.toArray(new RunEntry[0]);
        RunEntry[] lwr = lwrRun.list.toArray(new RunEntry[0]);
        // Merge them
        for(int i = 0; i < runList.size(); i++) {
            
            if(i < grt.length) 
                runList.set(i, grt[i]);
            else if(i < grt.length + eql.length) 
                runList.set(i, eql[i - grt.length]);
            else 
                runList.set(i, lwr[i - grt.length - eql.length]);
        }

    }
}

// Class Majority Runoff Comparator
class MajRunoffComparator implements Comparator<RunEntry> {

    // Private variables
    // Array of numbers of the considered systems
    private int[] systems;
    // Hash map (for a given topic) that associates to each document
    // an array of its ranks for every system
    private HashMap<String,Integer[]> docRanksBySystem; 

    // Constructor
    public MajRunoffComparator(int[] systems, HashMap<String,Integer[]> docRanksBySystem) {
        this.systems = systems;
        this.docRanksBySystem = docRanksBySystem;
    }

    // Compare method
    public int compare(RunEntry d1, RunEntry d2) {

        /*
         * Count the runs with d1 before d2 and d2 before d1, then return
         * the difference D1-D2 ( D1-D2>0 iff D1>D2 )
         */
        // Initialization of the return value
        int count = 0;

        // Get the arrays of ranks by system for the two documents
        Integer[] d1Ranks = docRanksBySystem.get(d1.docID);
        Integer[] d2Ranks = docRanksBySystem.get(d2.docID);
        // Scan the two arrays (both of length equal to systems.length)
        for(int i = 0; i < systems.length; i++) {

            // If d1 found AND ( d2 missing OR d2 has higher rank than d1 )
            if(d1Ranks[i] != null && (d2Ranks[i] == null || d2Ranks[i] > d1Ranks[i])) {
                count++;
            }
            // If d2 found AND ( d1 missing OR d1 has higher rank than d2 )
            if(d2Ranks[i] != null && (d1Ranks[i] == null || d1Ranks[i] > d2Ranks[i])) {
                count--;
            }
        }

        return count;
    }
}