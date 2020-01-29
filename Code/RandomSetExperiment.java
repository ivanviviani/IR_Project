import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

/*
 * IMPLEMENTATION OF RANDOM-SET EXPERIMENT
 * For every edition of TREC (3 edition), for every size of subset of systems (condidered sizes: 2, 4, 6, 8, 10, 12),
 * for REP times (200 times) individuate a subset of the system of fixed size then for every topic read the related input
 * data, then compute the fusion method and compute the AP and then compute the average on topics obtaining MAP.
 */
public class RandomSetExperiment
{
    /* STANDARD PARAMETERS
    private static final String[] NAME = new String[]{"TREC-3", "TREC-5", "TREC-9"};
    private static final String[] RELEVANCE = new String[]{"T3-j.txt", "T5-j.txt", "T9-j.txt"};
    private static final int[] NUM_SYS = new int[]{40, 61, 104};
    private static final int[] TOPIC_L = new int[]{151,251,451};
    private static final int[] TOPIC_H = new int[]{201,301,501}; // +1
    private static final int[] DIM = new int[]{2, 4, 6, 8, 10, 12};
    private static final int REP = 200;
    //*/

    //* SMALL TEST
    private static final String[] NAME = new String[]{"TREC-9"};
    private static final String[] RELEVANCE = new String[]{"T9-j.txt"};
    private static final int[] NUM_SYS = new int[]{104};
    private static final int[] TOPIC_L = new int[]{451};
    private static final int[] TOPIC_H = new int[]{501}; // +1
    private static final int[] DIM = new int[]{2, 4, 6, 8, 10, 12};
    private static final int REP = 200;
    //*/

    /* DEBUG PARAMETERS
    private static final String[] NAME = new String[]{"TREC-3"};
    private static final String[] RELEVANCE = new String[]{"T3-judgement.txt"};
    private static final int[] NUM_SYS = new int[]{3};
    private static final int[] TOPIC_L = new int[]{1};
    private static final int[] TOPIC_H = new int[]{2}; // +1
    private static final int[] DIM = new int[]{2,3};
    private static final int REP = 1;
    //*/

    public static void main(String[] args)
    {
        // [trec][combMNZ-rCombMNZ-bordaFuse-condorcetFuse][size]
        double[][][] resultMAP = new double[NAME.length][4][DIM.length];
        String[] fm = new String[]{"combMNZ","rCombMNZ","bordaFuse","condorcetFuse"};

        for (int i = 0; i < NAME.length; i++)
        {
            int topics = TOPIC_H[i]-TOPIC_L[i];

            long t = System.currentTimeMillis();
            System.out.print("Reading judgement");
            // Store run and relevance judgement
            // [topic]
            HashSet<String>[] relevant = Reader.extractJudgement(topics, RELEVANCE[i]);
            System.out.println(" ("+(System.currentTimeMillis()-t)+" ms)");

            t = System.currentTimeMillis();
            System.out.print("Reading runs");
            // [topic][sys][rank]
            RunEntry[][][] superRun = Reader.extractSuperRun(new int[]{TOPIC_L[i],TOPIC_H[i]}, NAME[i]);
            System.out.println(" ("+(System.currentTimeMillis()-t)+" ms)");

            t = System.currentTimeMillis();
            System.out.print("Creating comparators hash maps");
            // [topic][sys][rank]
            HashMap<String,Integer[]>[] cmpHM = Reader.extractComparator(superRun);
            System.out.println(" ("+(System.currentTimeMillis()-t)+" ms)");

            // ITERATION MONITOR
            System.out.println("ED: "+NAME[i]);
            for (int j = 0; j < DIM.length; j++)
            {
                // ITERATION MONITOR
                t = System.currentTimeMillis();
                System.out.println("SAMPLE: "+DIM[j]);
                for (int k = 0; k < REP; k++)
                {
                    // ITERATION MONITOR
                    System.out.print("*");
                    int[] chosenSys = Reader.reservoirSampling(NUM_SYS[i], DIM[j]);

                    for (int l = 0; l < topics; l++)
                    {
                        double combMNZ = Util.averagePrecision(Fusion.combMNZ(superRun,l,chosenSys), relevant[l]);
                        double rCombMNZ = Util.averagePrecision(Fusion.rCombMNZ(superRun,l,chosenSys), relevant[l]);
                        double bordaFuse = Util.averagePrecision(Fusion.bordaFuse(superRun,l,chosenSys), relevant[l]);
                        double condorcetFuse = Util.averagePrecision(Fusion.condorcetFuse(superRun,l,chosenSys,cmpHM[l]), relevant[l]);

                        double normalize = REP * topics;

                        resultMAP[i][0][j] += combMNZ / normalize;
                        resultMAP[i][1][j] += rCombMNZ / normalize;
                        resultMAP[i][2][j] += bordaFuse / normalize;
                        resultMAP[i][3][j] += condorcetFuse / normalize;
                    }
                }
                // ITERATION MONITOR
                System.out.println();
                System.out.println("SAMPLE: "+DIM[j]+" ("+(System.currentTimeMillis()-t)+" ms)");
            }
        }

        try
        {
            for (int i = 0; i < NAME.length; i++)
            {
                PrintWriter pw = new PrintWriter("Result-" + NAME[i] + ".csv");
                for (int j = 0; j < resultMAP[i].length; j++)
                {
                    pw.print(fm[j] + ";");
                    for (int k = 0; k < DIM.length; k++)
                    {
                        pw.print(resultMAP[i][j][k] + ";");
                    }
                    pw.println();
                }
                pw.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}