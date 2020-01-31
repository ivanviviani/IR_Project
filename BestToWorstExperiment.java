import java.util.HashSet;
import java.io.PrintWriter;
import java.util.HashMap;

/*
 * IMPLEMENTATION OF BEST-TO-WORST EXPERIMENT
 * For every edition of TREC (3-5-9 edition), consider the 20 best performing systems in terms of MAP score.
 * For each fusion method considered, perform 19 fusion experiments, in order: fuse the top two best systems, 
 * then the top three, and so on until the last fusion of all the top 20 systems. For each fusion compute the
 * final MAP score.
 */
public class BestToWorstExperiment {

    //* STANDARD PARAMETERS
    private static final String[] NAME = new String[]{"TREC-3", "TREC-5", "TREC-9"};
    private static final String[] RELEVANCE = new String[]{"T3-j.txt", "T5-j.txt", "T9-j.txt"};
    private static final int[] NUM_SYS = new int[]{40, 61, 104};
    private static final int[] TOPIC_L = new int[]{151, 251, 451};
    private static final int[] TOPIC_H = new int[]{201, 301, 501}; // +1
    private static final int DIM = 21; //last +1
    //*/

    public static void main(String[] args) {
        // Create results map
        // [trec][combMNZ-rCombMNZ-bordaFuse-condorcetFuse][size]
        double[][][] resultMAP = new double[NAME.length][4][DIM - 2];
        String[] fusionMethods = new String[]{"combMNZ", "rCombMNZ", "bordaFuse", "condorcetFuse"};

        // Scan TREC versions (3,5,9)
        for(int i = 0; i < NAME.length; i++) {

            // Number of topics for current TREC edition
            int numTopics = TOPIC_H[i] - TOPIC_L[i];
        
            long t = System.currentTimeMillis();
            System.err.print("Reading relevance judgements (relevant docs) for "+NAME[i]);
            // Extract relevance judgements [topic]
            HashSet<String>[] relDocsByTopic = Reader.extractRelJudgements(numTopics, RELEVANCE[i]);
            System.err.println(" (" + (System.currentTimeMillis() - t) + " ms)");

            t = System.currentTimeMillis();
            System.err.print("Reading runs for "+NAME[i]);
            // Create the super run [topic][sys][rank]
            Run[][] superRun = Reader.extractSuperRun(new int[]{TOPIC_L[i], TOPIC_H[i]}, NAME[i]);
            System.err.println(" (" + (System.currentTimeMillis() - t) + " ms)");

            t = System.currentTimeMillis();
            System.err.print("Creating comparators hash maps");
            // Create array of hash maps for every topic. Each hash map associates
            // every document (for that topic) to an array of ranks for every system. < doc, ranks >
            HashMap<String, Integer[]>[] cmpHM = Reader.extractComparator(superRun);
            System.err.println(" (" + (System.currentTimeMillis() - t) + " ms)");

            // System scores 
            double[] syScores = new double[NUM_SYS[i]];
            // Compute MAP for every system
            for(int s = 0; s < syScores.length; s++) { // Scan systems
                for(int l = 0; l < numTopics; l++) { // Scan topics
                    // Compute and add MAP contribution for each topic
                    syScores[s] += Util.averagePrecision(superRun[l][s], relDocsByTopic[l]) / numTopics;
                }
            }

            // Sort indexes of systems by their MAP score
            int[] bestSysIndexes = sortIndexesTop20(syScores);
            // for(int d : bestSysIndexes) System.err.println("  System " + d + " MAP : " + syScores[d]);
            
            // Best-to-Worst experiment: fuse the top two systems, then the top three, ..., finally the top 20
            // ITERATION MONITOR
            System.err.println("ED: "+NAME[i]);
            for(int j = 2; j < DIM; j++) { // Scan system groups sizes

                t = System.currentTimeMillis();

                // Selection of the best j systems
                int[] chosenSys = new int[j];
                for(int l = 0; l < chosenSys.length; l++) chosenSys[l] = bestSysIndexes[l];

                for(int l = 0; l < numTopics; l++) { // Scan topics

                    // Compute final runs for current group of systems (one for each fusion method)
                    Run combRun = Fusion.combMNZ(superRun, l, chosenSys);
                    Run rCombRun = Fusion.rCombMNZ(superRun, l, chosenSys);
                    Run bordaRun = Fusion.bordaFuse(superRun, l, chosenSys);
                    Run condoRun = Fusion.condorcetFuse(superRun, l, chosenSys, cmpHM[l]);

                    // Compute average precisions (MAP contributions) for current group of systems, current topic
                    double combMNZ = Util.averagePrecision(combRun, relDocsByTopic[l]);
                    double rCombMNZ = Util.averagePrecision(rCombRun, relDocsByTopic[l]);
                    double bordaFuse = Util.averagePrecision(bordaRun, relDocsByTopic[l]);
                    double condorcetFuse = Util.averagePrecision(condoRun, relDocsByTopic[l]);

                    // Add MAP contributions for current group of systems, current topic to the results map
                    resultMAP[i][0][j - 2] += combMNZ / numTopics;
                    resultMAP[i][1][j - 2] += rCombMNZ / numTopics;
                    resultMAP[i][2][j - 2] += bordaFuse / numTopics;
                    resultMAP[i][3][j - 2] += condorcetFuse / numTopics;
                }

                // ITERATION MONITOR
                System.err.println("  FUSION TOP " + j + " SYS (" + (System.currentTimeMillis() - t) + " ms)");
            }
        }

        // Write results to file (one for each TREC edition 3,5,9)
        try {
            for(int i = 0; i < NAME.length; i++) {

                PrintWriter pw = new PrintWriter("Result-BEST-" + NAME[i] + ".csv");
                for(int j = 0; j < resultMAP[i].length; j++) {
                    pw.print(fusionMethods[j] + ";");
                    for(int k = 0; k < DIM-2; k++) {
                        pw.print(resultMAP[i][j][k] + ";");
                    }
                    pw.println();
                }
                pw.close();
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Sort the indexes of an array according to the top 20 values (decreasing order)
     * @param arr Array of values.
     * @return Trimmed array of the top 20 indexes sorted by value (decreasing order)
     */
    private static int[] sortIndexesTop20(double[] arr) {

        // Initialization of the return value
        int[] indexes = new int[20];

        // Support copy of the original array
        double[] values = arr.clone();
        int i_max;
        // Fill the (sorted) indexes array with the index of the top 20 max values
        for(int i = 0; i < indexes.length; i++) { // Scan the (sorted) indexes array
            // Find the max value, save its index and reset it
            i_max = 0;
            for(int j = 1; j < values.length; j++) {
                if (values[i_max] < values[j]) 
                    i_max = j;
            }
            indexes[i] = i_max;
            values[i_max] = -1.0; // Reset
        }

        // Return the sorted array of indexes
        return indexes;
    }
}