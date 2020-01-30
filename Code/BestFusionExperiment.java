import java.util.HashSet;
import java.io.PrintWriter;
import java.util.HashMap;

public class BestFusionExperiment
{
    //* STANDARD PARAMETERS
    private static final String[] NAME = new String[]{"TREC-3", "TREC-5", "TREC-9"};
    private static final String[] RELEVANCE = new String[]{"T3-j.txt", "T5-j.txt", "T9-j.txt"};
    private static final int[] NUM_SYS = new int[]{40, 61, 104};
    private static final int[] TOPIC_L = new int[]{151, 251, 451};
    private static final int[] TOPIC_H = new int[]{201, 301, 501}; // +1
    private static final int DIM = 21; //last +1
    //*/

    /* SMALL TEST
    private static final String[] NAME = new String[]{"TREC-9"};
    private static final String[] RELEVANCE = new String[]{"T9-j.txt"};
    private static final int[] NUM_SYS = new int[]{104};
    private static final int[] TOPIC_L = new int[]{451};
    private static final int[] TOPIC_H = new int[]{501}; // +1
    private static final int DIM = 21; //last +1
    //*/

    public static void main(String[] args)
    {
        // [trec][combMNZ-rCombMNZ-bordaFuse-condorcetFuse][size]
        double[][][] resultMAP = new double[NAME.length][4][DIM - 2];
        String[] fm = new String[]{"combMNZ", "rCombMNZ", "bordaFuse", "condorcetFuse"};

        for (int i = 0; i < NAME.length; i++)
        {
            int topics = TOPIC_H[i] - TOPIC_L[i];

            long t = System.currentTimeMillis();
            System.out.print("Reading judgment");
            // [topic]
            HashSet<String>[] relevant = Reader.extractJudgement(topics, RELEVANCE[i]);
            System.out.println(" (" + (System.currentTimeMillis() - t) + " ms)");

            t = System.currentTimeMillis();
            System.out.print("Reading runs");
            // [topic][sys][rank]
            RunEntry[][][] superRun = Reader.extractSuperRun(new int[]{TOPIC_L[i], TOPIC_H[i]}, NAME[i]);
            System.out.println(" (" + (System.currentTimeMillis() - t) + " ms)");

            t = System.currentTimeMillis();
            System.out.print("Creating comparators hash maps");
            // < doc, ranks >
            HashMap<String, Integer[]>[] cmpHM = Reader.extractComparator(superRun);
            System.out.println(" (" + (System.currentTimeMillis() - t) + " ms)");

            // Compute MAP for every system
            double[] systemScore = new double[NUM_SYS[i]];
            for (int s = 0; s < systemScore.length; s++)
            {
                for (int l = 0; l < topics; l++)
                {
                    systemScore[s] += Util.averagePrecision(superRun[l][s], relevant[l]) / topics;
                }
            }

            // Sort index of systems by their MAP score
            int[] bestSystemIndex = sortIndex(systemScore);
            for (int d : bestSystemIndex)
            {
                System.out.println("Sistema " + d + " " + systemScore[d]);
            }

            for (int j = 2; j < DIM; j++)
            {
                t = System.currentTimeMillis();

                // Selection of the best j systems
                int[] chosenSys = new int[j];
                for (int l = 0; l < chosenSys.length; l++)
                {
                    chosenSys[l] = bestSystemIndex[l];
                }

                for (int l = 0; l < topics; l++)
                {
                    double combMNZ = Util.averagePrecision(Fusion.combMNZ(superRun, l, chosenSys), relevant[l]);
                    double rCombMNZ = Util.averagePrecision(Fusion.rCombMNZ(superRun, l, chosenSys), relevant[l]);
                    double bordaFuse = Util.averagePrecision(Fusion.bordaFuse(superRun, l, chosenSys), relevant[l]);
                    double condorcetFuse = Util.averagePrecision(Fusion.condorcetFuse(superRun, l, chosenSys, cmpHM[l]), relevant[l]);

                    resultMAP[i][0][j - 2] += combMNZ / topics;
                    resultMAP[i][1][j - 2] += rCombMNZ / topics;
                    resultMAP[i][2][j - 2] += bordaFuse / topics;
                    resultMAP[i][3][j - 2] += condorcetFuse / topics;
                }

                // ITERATION MONITOR
                System.out.println("SAMPLE: " + j + " (" + (System.currentTimeMillis() - t) + " ms)");
            }
        }

        try
        {
            for (int i = 0; i < NAME.length; i++)
            {
                PrintWriter pw = new PrintWriter("Result-BEST-" + NAME[i] + ".csv");
                for (int j = 0; j < resultMAP[i].length; j++)
                {
                    pw.print(fm[j] + ";");
                    for (int k = 0; k < DIM - 2; k++)
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

    private static int[] sortIndex(double[] val)
    {
        double[] value = val.clone();
        int[] index = new int[value.length];
        for (int i = 0; i < index.length; i++)
        {
            int max = 0;
            for (int j = 1; j < value.length; j++)
            {
                if (value[max] < value[j])
                {
                    max = j;
                }
            }
            index[i] = max;
            value[max] = -1.0;
        }
        return index;
    }
}