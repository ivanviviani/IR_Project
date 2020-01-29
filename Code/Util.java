import java.util.*;

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
        /*DEBUG/
        for (int i = 0; i < run.length; i++)
        {
            System.out.println(i + "\t" + run[i].id + "\tr: " + (relevant.contains(run[i].id)));
        }
        System.out.println();
        /*DEBUG*/

        double averagePrecision = 0;
        int recallBase = relevant.size();
        int relFound = 0;

        /*DEBUG/
        System.out.println("RB: " + recallBase);
        /*DEBUG*/

        for (int i = 0; i < run.length; i++)
        {
            if (relevant.contains(run[i].id))
            {
                relFound++;
                /*DEBUG/
                System.out.println(relFound + " / " + (i + 1));
                /*DEBUG*/
                averagePrecision += relFound / (i + 1.0);
            }
        }

        averagePrecision = averagePrecision / recallBase;

        /*DEBUG/
        System.out.println("AP: " + averagePrecision);
        /DEBUG*/

        return averagePrecision;
    }

    public static void quickSort(RunEntry[] run, Comparator<RunEntry> cmp)
    {
        Random r = new Random();

        if (run.length < 2)
        {
            return;
        }

        RunEntry pivot = run[r.nextInt(run.length)];

        ArrayList<RunEntry> grtList = new ArrayList<>();
        ArrayList<RunEntry> eqlList = new ArrayList<>();
        ArrayList<RunEntry> lwrList = new ArrayList<>();

        for (RunEntry re : run)
        {
            int res = cmp.compare(pivot, re);

            if (res < 0)
            {
                grtList.add(re);
            }
            else if (res > 0)
            {
                lwrList.add(re);
            }
            else
            {
                eqlList.add(re);
            }
        }

        RunEntry[] grt = grtList.toArray(new RunEntry[0]);
        RunEntry[] eql = eqlList.toArray(new RunEntry[0]);
        RunEntry[] lwr = lwrList.toArray(new RunEntry[0]);

        quickSort(grt, cmp);
        quickSort(lwr, cmp);

        for(int i=0;i<run.length;i++)
        {
            if(i<grt.length)
            {
                run[i] = grt[i];
            }
            else if(i<grt.length+eql.length)
            {
                run[i] = eql[i-grt.length];
            }
            else
            {
                run[i] = lwr[i-grt.length-eql.length];
            }
        }
    }
}

class MajRunoffComparator implements Comparator<RunEntry>
{
    private int[] sys;
    private HashMap<String, Integer[]> ref;

    public MajRunoffComparator(int[] sys, HashMap<String, Integer[]> ref)
    {
        this.sys = sys;
        this.ref = ref;
    }

    public int compare(RunEntry r1, RunEntry r2)
    {
        /*
         * Count the run with a before b and b before a, then return
         * the difference A-B ( A-B>0 iff A>B )
         */
        int count = 0;
        Integer[] ref1 = ref.get(r1.id);
        Integer[] ref2 = ref.get(r2.id);

        for (int i = 0; i < sys.length; i++)
        {
            // first found AND ( second missing OR second has higher rank than first )
            if (ref1[i] != null && (ref2[i] == null || ref2[i] > ref1[i]))
            {
                count++;
            }
            // second found AND ( first missing OR first has higher rank than second )
            if (ref2[i] != null && (ref1[i] == null || ref1[i] > ref2[i]))
            {
                count--;
            }
        }
        return count;
    }
}