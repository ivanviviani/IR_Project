import java.io.PrintWriter;
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
    */

    // SMALL TEST
    private static final String[] NAME = new String[]{"TREC-3"};
    private static final String[] RELEVANCE = new String[]{"T3-j.txt"};
    private static final int[] NUM_SYS = new int[]{40};
    private static final int[] TOPIC_L = new int[]{151};
    private static final int[] TOPIC_H = new int[]{201}; // +1

    private static final int[] DIM = new int[]{2, 4, 6, 8, 10, 12};

    private static final int REP = 10;//200;

    /* DEBUG PARAMETERS
    private static final String[] NAME = new String[]{"TREC-3"};
    private static final String[] RELEVANCE = new String[]{"T3-judgement.txt"};
    private static final int[] NUM_SYS = new int[]{3};
    private static final int[] TOPIC_L = new int[]{1};
    private static final int[] TOPIC_H = new int[]{2}; // +1
    private static final int[] DIM = new int[]{2,3};
    private static final int REP = 1;
    */

    public static void main(String[] args)
    {
        // [trec][combMNZ-rCombMNZ-bordaFuse-condorcetFuse][size]
        double[][][] resultMAP = new double[NAME.length][4][DIM.length];
        String[] fm = new String[]{"combMNZ","rCombMNZ","bordaFuse","condorcetFuse"};

        for (int i = 0; i < NAME.length; i++)
        {
            int topics = TOPIC_H[i]-TOPIC_L[i];

            // Store run and relevance judgement
            // [topic]
            HashSet[] relevant = new HashSet[topics];
            for(int j=0;j<topics;j++)
            {
                relevant[j] = Reader.extractJudgement(j, RELEVANCE[i]);
            }
            // [topic][sys][rank]
            RunEntry[][][] superRun = Reader.extractSuperRun(new int[]{TOPIC_L[i],TOPIC_H[i]}, NAME[i]);

            // ITERATION MONITOR
            System.out.println("ED: "+NAME[i]);
            for (int j = 0; j < DIM.length; j++)
            {
                // ITERATION MONITOR
                System.out.println("SAMPLE: "+DIM[j]);
                for (int k = 0; k < REP; k++)
                {
                    // ITERATION MONITOR
                    System.out.print("*");
                    int[] chosenSys = Reader.reservoirSampling(NUM_SYS[i], DIM[j]);

                    for (int l = TOPIC_L[i]; l < TOPIC_H[i]; l++)
                    {
                        RunEntry[][] run = Reader.extractRunFromSuperRun(l,chosenSys,superRun);

                        double combMNZ = Util.averagePrecision(Fusion.combMNZ(run), relevant[l]);
                        double rCombMNZ = Util.averagePrecision(Fusion.rCombMNZ(run), relevant[l]);
                        double bordaFuse = Util.averagePrecision(Fusion.bordaFuse(run), relevant[l]);
                        double condorcetFuse = Util.averagePrecision(Fusion.condorcetFuse(run), relevant[l]);

                        double normalize = REP * (topics);

                        resultMAP[i][0][j] += combMNZ / normalize;
                        resultMAP[i][1][j] += rCombMNZ / normalize;
                        resultMAP[i][2][j] += bordaFuse / normalize;
                        resultMAP[i][3][j] += condorcetFuse / normalize;
                    }
                    // ITERATION MONITOR
                    System.out.println();
                }
            }
        }

        try
        {
            for (int i = 0; i < NAME.length; i++)
            {
                PrintWriter pw = new PrintWriter("Result-" + NAME[i] + ".txt");
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