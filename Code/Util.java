import java.util.Comparator;
import java.util.HashSet;

public class Util
{
    /**
     * Return the first k entry of the run.
     *
     * @param run The run to elaborate.
     * @param k   The number of entry to extract.
     * @return The trimmed run.
     */
    public static RunEntry[] extractTop(RunEntry[] run, int k)
    {
        if (run.length < k)
        {
            return run;
        }

        RunEntry[] newRun = new RunEntry[k];
        for (int i = 0; i < k; i++)
        {
            newRun[i] = run[i];
        }
        return newRun;
    }

    /**
     * Computes the average precision for the run.
     *
     * @param run      The run to evaluate.
     * @param relevant The relevant document.
     * @return The computed average precision.
     */
    public static double averagePrecision(RunEntry[] run, HashSet<String> relevant)
    {
        /*DEBUG*/
        for(int i = 0; i<run.length;i++)
        {
            System.out.println(i+"\t"+run[i].id+"\tr: "+(relevant.contains(run[i].id)));
        }
        System.out.println();
        /*DEBUG*/

        double averagePrecision = 0;
        int recallBase = relevant.size();
        int relFound = 0;

        /*DEBUG*/
        System.out.println("RB: "+recallBase);
        /*DEBUG*/

        for (int i = 0; i < run.length; i++)
        {
            if (relevant.contains(run[i].id))
            {
                relFound++;
                System.out.println(relFound+" / "+(i+1));
                averagePrecision += relFound / (i + 1.0);
            }
        }

        averagePrecision = averagePrecision / recallBase;

        /*DEBUG*/
        System.out.println("AP: "+averagePrecision);
        /*DEBUG*/

        return averagePrecision;
    }
}

class MajRunoffComparator implements Comparator<RunEntry>
{
    private RunEntry[][][] superRun;
    private int topic;
    private int[] sys;

    public MajRunoffComparator(RunEntry[][][] sR, int t, int[] s)
    {
        superRun = sR;
        topic = t;
        sys = s;
    }

    public int compare(RunEntry r1, RunEntry r2)
    {
        /*
         * Count the run with a before b and b before a, then return
         * the difference A-B ( A-B>0 iff A>B )
         */
        int count = 0;
        for (int i = 0; i < sys.length; i++)
            for (int j = 0; j < superRun[topic][sys[i]].length; j++)
            {
                // The first one I found, win one point
                if (superRun[topic][sys[i]][j].id.equals(r1.id))
                {
                    count++;
                    break;
                }
                if (superRun[topic][sys[i]][j].id.equals(r2.id))
                {
                    count++;
                    break;
                }
            }
        return count;
    }

    public static void parallelQuickSort(RunEntry[] run, Comparator cmp)
    {



    }
}
