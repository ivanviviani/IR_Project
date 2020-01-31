import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

/*
 * IMPLEMENTATION OF RANDOM-SET EXPERIMENT
 * For every edition of TREC (3-5-9 edition), for every size of subset of systems (considered sizes: 2, 4, 6, 8, 10, 12),
 * for REP times (200 times) choose a subset of the system of fixed size, then for every topic read the related input
 * data, then compute the fusion methods, compute the AP, finally compute the average on topics obtaining MAP.
 */
public class RandomSetExperiment {

    // STANDARD PARAMETERS
    private static final String[] NAME = new String[]{"TREC-3", "TREC-5", "TREC-9"};
    private static final String[] RELEVANCE = new String[]{"T3-j.txt", "T5-j.txt", "T9-j.txt"};
    private static final int[] NUM_SYS = new int[]{40, 61, 104};
    private static final int[] TOPIC_L = new int[]{151,251,451};
    private static final int[] TOPIC_H = new int[]{201,301,501}; // +1
    private static final int[] DIM = new int[]{2, 4, 6, 8, 10, 12};
    private static final int REP = 200;

    public static void main(String[] args) {

        // Initialization of results
        // [trec][combMNZ-rCombMNZ-bordaFuse-condorcetFuse][subset size]
        double[][][] resultMAP = new double[NAME.length][4][DIM.length];
        // Fusion methods
        String[] fusionMethods = new String[]{"combMNZ","rCombMNZ","bordaFuse","condorcetFuse"};

        // Scan TREC versions (3,5,9)
        for(int i = 0; i < NAME.length; i++) {

            // Number of topics for current TREC edition
            int numTopics = TOPIC_H[i] - TOPIC_L[i];

            // Measure execution time
            long t = System.currentTimeMillis();
            System.err.println("Reading relevance judgements (relevant docs) for "+NAME[i]);
            // Create array of hash sets for every topic containing
            // their relevant documents. [topic]
            HashSet<String>[] relDocsByTopic = Reader.extractRelJudgements(numTopics, RELEVANCE[i]);
            System.err.println(" ("+(System.currentTimeMillis()-t)+" ms)");

            // Measure execution time
            t = System.currentTimeMillis();
            System.err.println("Reading runs for "+NAME[i]);
            // Create superRun = matrix [topic][system] of Runs
            Run[][] superRun = Reader.extractSuperRun(new int[]{TOPIC_L[i],TOPIC_H[i]}, NAME[i]);
            System.err.println(" ("+(System.currentTimeMillis()-t)+" ms)");

            // Measure execution time
            t = System.currentTimeMillis();
            System.err.println("Creating comparators hash maps");
            // Create array of hash maps for every topic. Each hash map associates
            // every document (for that topic) to an array of ranks for every system.
            HashMap<String,Integer[]>[] cmpHM = Reader.extractComparator(superRun);
            System.err.println(" ("+(System.currentTimeMillis()-t)+" ms)");

            // ITERATION MONITOR
            System.err.println("ED: "+NAME[i]);
            // Scan sizes of randoom subsets of systems to create (2,4,6,8,10,12)
            for(int j = 0; j < DIM.length; j++) {
                
                // ITERATION MONITOR
                t = System.currentTimeMillis();
                System.err.println("SAMPLE: "+DIM[j]);

                // for REP times
                for(int k = 0; k < REP; k++) {

                    // ITERATION MONITOR
                    System.err.print("*");

                    // Choose DIM[i_size] systems randomly
                    int[] chosenSys = Reader.reservoirSampling(NUM_SYS[i], DIM[j]);

                    // Fuse them together
                    // for each topic
                    for(int l = 0; l < numTopics; l++) {

                        // Compute the final runs (one for each fusion method)
                        Run combRun = Fusion.combMNZ(superRun, l, chosenSys);
                        Run rCombRun = Fusion.rCombMNZ(superRun, l, chosenSys);
                        Run bordaRun = Fusion.bordaFuse(superRun, l, chosenSys);
                        Run condoRun = Fusion.condorcetFuse(superRun, l, chosenSys, cmpHM[l]);

                        // Compute the avergae precisions (of each final run)
                        double APcombMNZ = Util.averagePrecision(combRun, relDocsByTopic[l]);
                        double APrCombMNZ = Util.averagePrecision(rCombRun, relDocsByTopic[l]);
                        double APbordaFuse = Util.averagePrecision(bordaRun, relDocsByTopic[l]);
                        double APcondorcetFuse = Util.averagePrecision(condoRun, relDocsByTopic[l]);

                        // Compute normalization factor
                        double normalize = REP * (numTopics);

                        // Fill results
                        resultMAP[i][0][j] += APcombMNZ / normalize;
                        resultMAP[i][1][j] += APrCombMNZ / normalize;
                        resultMAP[i][2][j] += APbordaFuse / normalize;
                        resultMAP[i][3][j] += APcondorcetFuse / normalize;
                    }
                }
                // ITERATION MONITOR
                System.err.println();
                System.err.println("SAMPLE: "+DIM[j]+" ("+(System.currentTimeMillis()-t)+" ms)");
            }
        }

        // Write results to file (one for each TREC edition 3,5,9)
        try {
            for(int i = 0; i < NAME.length; i++) {

                PrintWriter pw = new PrintWriter("Result-" + NAME[i] + ".csv");
                for(int j = 0; j < resultMAP[i].length; j++) {
                    pw.print(fusionMethods[j] + ";");
                    for(int k = 0; k < DIM.length; k++) {
                        pw.print(resultMAP[i][j][k] + ";");
                    }
                    pw.println();
                }
                pw.close();
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }
}