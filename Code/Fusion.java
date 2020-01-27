import java.util.HashMap;
import java.util.HashSet;

public class Fusion
{
    /**
     * Compute a new run using combMNZ method.
     *
     * @param superRun The super run that contain all runs for all systems and topics.
     * @param topic The number of the considered topic.
     * @param sys The assay with the number of considered systems.
     * @return The fusion run.
     */
    public static RunEntry[] combMNZ(RunEntry[][][] superRun, int topic, int[] sys)
    {
        /*
         * For every ranked document, accumulate the sum of all the scores
         * it gets in the different runs and count the runs where it appears.
         * All documents, their cumulative score and their presence count
         * are stored in an hashmap.
         */
        HashMap<String, RunEntry> collection = new HashMap<>();
        HashMap<String, Integer> counter = new HashMap<>();

        for (int i = 0; i < sys.length; i++)
        {
            for (int j = 0; j < superRun[topic][sys[i]].length; j++)
            {
                // Get current entry property

                String currKey = superRun[topic][sys[i]][j].id;
                double currScore = superRun[topic][sys[i]][j].score;

                // If already present, update
                if (collection.containsKey(currKey))
                {
                    RunEntry currEntry = collection.get(currKey);
                    currEntry.increaseScore(currScore);

                    counter.replace(currKey,counter.get(currKey)+1);
                }
                // If missing, add
                else
                {
                    collection.put(currKey, new RunEntry(currKey, currScore));

                    counter.put(currKey,1);
                }
            }
        }

        /*
         * Convert the hashmap to array, then update the score of each document
         * multiplying it by the number of occurrences in the runs. The resulting
         * run is sorted by score and trimmed to the first 1000 results.
         */
        RunEntry[] finalRun = collection.values().toArray(new RunEntry[0]);
        for(int i=0;i<finalRun.length;i++)
        {
            finalRun[i].multiplyScore(counter.get(finalRun[i].id));
        }
        finalRun = Util.sortRun(finalRun);
        finalRun = Util.extractTop(finalRun, RunEntry.RUN_LEN);

        /*/
        System.out.println("CombMNZ");
        for(int i = 0;i<5;i++)
        {
            System.out.println(finalRun[i].id+"\t"+finalRun[i].score);
        }
        /*/

        return finalRun;
    }

    /**
     * Compute a new run using rCombMNZ method.
     *
     * @param superRun The super run that contain all runs for all systems and topics.
     * @param topic The number of the considered topic.
     * @param sys The assay with the number of considered systems.
     * @return The fusion run.
     */
    public static RunEntry[] rCombMNZ(RunEntry[][][] superRun, int topic, int[] sys)
    {
        /*
         * For every ranked document, assign r - # doc points, then
         * accumulate the sum of all the points and consider it as the
         * score for the document, moreover it count the runs where each
         * document appears. All documents, their cumulative score and
         * their presence count are stored in an hashmap.
         */
        HashMap<String, RunEntry> collection = new HashMap<>();
        HashMap<String, Integer> counter = new HashMap<>();

        for (int i = 0; i < sys.length; i++)
        {
            for (int j = 0; j < superRun[topic][sys[i]].length; j++)
            {
                // Get current entry property
                String currKey = superRun[topic][sys[i]][j].id;
                double currScore = superRun[topic][sys[i]].length-1-j;

                // If already present, update
                if (collection.containsKey(currKey))
                {
                    RunEntry currEntry = collection.get(currKey);
                    currEntry.increaseScore(currScore);

                    counter.replace(currKey,counter.get(currKey)+1);
                }
                // If missing, add
                else
                {
                    collection.put(currKey, new RunEntry(currKey, currScore));

                    counter.put(currKey,1);
                }
            }
        }

        /*
         * Convert the hashmap to array, then update the points of each document
         * multiplying it by the number of occurrences in the runs. The resulting
         * run is sorted by score and trimmed to the first 1000 results.
         */
        RunEntry[] finalRun = collection.values().toArray(new RunEntry[0]);
        for(int i=0;i<finalRun.length;i++)
        {
            finalRun[i].multiplyScore(counter.get(finalRun[i].id));
        }
        finalRun = Util.sortRun(finalRun);
        finalRun = Util.extractTop(finalRun, RunEntry.RUN_LEN);

        /*/
        System.out.println("CombMNZ");
        for(int i = 0;i<5;i++)
        {
            System.out.println(finalRun[i].id+"\t"+finalRun[i].score);
        }
        /*/

        return finalRun;
    }

    /**
     * Compute a new run using Borda fuse method.
     *
     * @param superRun The super run that contain all runs for all systems and topics.
     * @param topic The number of the considered topic.
     * @param sys The assay with the number of considered systems.
     * @return The fusion run.
     */
    public static RunEntry[] bordaFuse(RunEntry[][][] superRun, int topic, int[] sys)
    {
        /*
         * For every ranked document, assign r - # doc points, then
         * accumulate the sum of all the points and consider it as the
         * score for the document. All documents and their
         * cumulative points are stored in an hashmap.
         */
        HashMap<String, RunEntry> collection = new HashMap<>();

        for (int i = 0; i < sys.length; i++)
        {
            for (int j = 0; j < superRun[topic][sys[i]].length; j++)
            {
                // Get current entry property
                String currKey = superRun[topic][sys[i]][j].id;
                double currScore = superRun[topic][sys[i]].length-1-j;

                // If already present, update
                if (collection.containsKey(currKey))
                {
                    RunEntry currEntry = collection.get(currKey);
                    currEntry.increaseScore(currScore);
                }
                // If missing, add
                else
                {
                    collection.put(currKey, new RunEntry(currKey, currScore));
                }
            }
        }

        /*
         * Convert the hashmap to array, then the resulting run is sorted
         * by score (sum of points) and trimmed to the first 1000 results.
         */
        RunEntry[] finalRun = collection.values().toArray(new RunEntry[0]);
        finalRun = Util.sortRun(finalRun);
        finalRun = Util.extractTop(finalRun, RunEntry.RUN_LEN);

        /*/
        System.out.println("bordaFuse");
        for(int i = 0;i<5;i++)
        {
            System.out.println(finalRun[i].id+"\t"+finalRun[i].score);
        }
        /*/

        return finalRun;
    }

    /**
     * Compute a new run using condorcet-fuse method.
     *
     * @param superRun The super run that contain all runs for all systems and topics.
     * @param topic The number of the considered topic.
     * @param sys The assay with the number of considered systems.
     * @return The fusion run.
     */
    public static RunEntry[] condorcetFuse(RunEntry[][][] superRun, int topic, int[] sys)
    {
        /*
         * Create a list containing all the documents in the runs,
         * putting them in a hashset.
         */
        HashSet<String> collection = new HashSet<>();

        for (int i = 0; i < sys.length; i++)
        {
            for (int j = 0; j < superRun[topic][sys[i]].length; j++)
            {
                String currKey = superRun[topic][sys[i]][j].id;

                if (!collection.contains(currKey))
                {
                    collection.add(currKey);
                }
            }
        }

        /*
         * The obtained list of document is converted to a list of entry
         * and then sorted.
         */
        String[] ids = collection.toArray(new String[0]);
        RunEntry[] finalRun = new RunEntry[ids.length];
        for(int i=0;i<finalRun.length;i++)
        {
            finalRun[i] = new RunEntry(ids[i],1/(i+1.0)); // symbolic score
        }

        finalRun = Util.sortRunMajRunoff(finalRun, superRun, topic, sys);

        /*/
        System.out.println("CondorcetFuse");
        for(int i = 0;i<5;i++)
        {
            System.out.println(finalRun[i].id+"\t"+finalRun[i].score);
        }
        /*/

        return finalRun;
    }
}