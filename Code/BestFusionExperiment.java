import java.util.HashSet;

public class BestFusionExperiment
{
    private static final String[] NAME = new String[]{"TREC-3", "TREC-5", "TREC-9"};
    private static final String[] RELEVANCE = new String[]{"T3-judgement.txt", "T5-judgement.txt", "T9-judgement.txt"};
    private static final int[] NUM_SYS = new int[]{40, 61, 104};
    private static final int[] TOPIC_L = new int[]{151,251,451};
    private static final int[] TOPIC_H = new int[]{201,301,501}; // +1

    public static void main(String[] args)
    {
        // for every trec
        //   get relevance judgement
        //   for every system
        //     compute map value
        for(int i=0;i< NAME.length;i++)
        {
            int topics = TOPIC_H[i]-TOPIC_L[i];
            double[] map = new double[NUM_SYS[i]];

            HashSet[] relevance = new HashSet[topics];
            for(int j=0;j<topics;j++)
            {
                relevance[j] = Reader.extractJudgement(j+TOPIC_L[i],RELEVANCE[i]);
            }

            for(int j=0;j<NUM_SYS.length;j++)
            {
                RunEntry[][] run = Reader.extractRunBySystem(j,new int[]{TOPIC_L[i],TOPIC_H[i]},NAME[i]);
                for(int k=0;k<topics;k++)
                {
                    map[j] += Util.averagePrecision(run[k],relevance[k])/topics;
                }
            }


        }
        /*TODO individuare i migliori sistemi*/

        /*TODO fare il merge sequenziale dei sistemi*/
    }


    private static int[] sortIndex(double[] value)
    {
        int[] index = new int[value.length];
        for(int i = 0;i<index.length;i++)
        {
            int max = 0;
            for(int j=1;j<value.length;j++)
            {
                if(value[max]<value[j])
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
