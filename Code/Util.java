import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Util
{
    /**
     * Given a run, sort its element by descending score.
     *
     * @param run The run to elaborate.
     * @return The sorted run.
     */
    public static RunEntry[] sortRun(RunEntry[] run)
    {
        /*
         * Quicksort implementation
         */
        Random r = new Random();
        if (run.length < 2)
        {
            return run;
        }

        double pivot = run[r.nextInt(run.length)].score;
        ArrayList<RunEntry> greaterList = new ArrayList<>();
        ArrayList<RunEntry> lowerList = new ArrayList<>();
        ArrayList<RunEntry> equalList = new ArrayList<>();
        for (int i = 0; i < run.length; i++)
        {
            if (run[i].score > pivot)
            {
                greaterList.add(run[i]);
            }
            else if (run[i].score < pivot)
            {
                lowerList.add(run[i]);
            }
            else
            {
                equalList.add(run[i]);
            }
        }

        RunEntry[] greater = sortRun(greaterList.toArray(new RunEntry[0]));
        RunEntry[] lower = sortRun(lowerList.toArray(new RunEntry[0]));

        RunEntry[] newRun = new RunEntry[run.length];
        for (int i = 0; i < run.length; i++)
        {
            if (i < greater.length)
            {
                newRun[i] = greater[i];
            }
            else if (i < greater.length + equalList.size())
            {
                newRun[i] = equalList.get(i - greater.length);
            }
            else
            {
                newRun[i] = lower[i - greater.length - equalList.size()];
            }
        }

        return newRun;
    }

    /**
     * Given a run, sort its element usign as comparator the majority runoff vote.
     *
     * @param run       The run to elaborate.
     * @param reference The reference runs to compute the majority runoff.
     * @return The sorted run.
     */
    public static RunEntry[] sortRunMajRunoff(RunEntry[] run, RunEntry[][] reference)
    {
        /*
         * Quicksort implementation
         */
        Random r = new Random();
        if (run.length < 2)
        {
            return run;
        }

        RunEntry pivot = run[r.nextInt(run.length)];
        ArrayList<RunEntry> greaterList = new ArrayList<>();
        ArrayList<RunEntry> lowerList = new ArrayList<>();
        ArrayList<RunEntry> equalList = new ArrayList<>();
        for (int i = 0; i < run.length; i++)
        {
            int comparisonResult = compare(run[i], pivot, reference);
            if (comparisonResult > 0)
            {
                greaterList.add(run[i]);
            }
            else if (comparisonResult < 0)
            {
                lowerList.add(run[i]);
            }
            else
            {
                equalList.add(run[i]);
            }
        }

        RunEntry[] greater = sortRunMajRunoff(greaterList.toArray(new RunEntry[0]), reference);
        RunEntry[] lower = sortRunMajRunoff(lowerList.toArray(new RunEntry[0]), reference);

        RunEntry[] newRun = new RunEntry[run.length];
        for (int i = 0; i < run.length; i++)
        {
            if (i < greater.length)
            {
                newRun[i] = greater[i];
            }
            else if (i < greater.length + equalList.size())
            {
                newRun[i] = equalList.get(i - greater.length);
            }
            else
            {
                newRun[i] = lower[i - greater.length - equalList.size()];
            }
        }

        return newRun;
    }

    private static int compare(RunEntry a, RunEntry b, RunEntry[][] reference)
    {
        /*
         * Count the run with a before b and b before a, then return
         * the difference A-B ( A-B>0 iff A>B )
         */
        if (a.id.equals(b.id))
        {
            return 0;
        }

        int countA = 0;
        int countB = 0;

        for (int i = 0; i < reference.length; i++)
        {
            for (int j = 0; j < reference[i].length; j++)
            {
                // The first one I found, win one point
                if (reference[i][j].id.equals(a.id))
                {
                    countA++;
                    break;
                }
                if (reference[i][j].id.equals(b.id))
                {
                    countB++;
                    break;
                }
            }
        }
        return countA - countB;
    }

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
        double averagePrecision = 0;
        int recallBase = relevant.size();
        int relFound = 0;

        for (int i = 0; i < run.length; i++)
        {
            if (relevant.contains(run[i].id))
            {
                relFound++;
                averagePrecision += relFound / (i + 1.0);
            }
        }

        averagePrecision = averagePrecision / recallBase;

        /*/
        System.out.println("Average Precision: "+averagePrecision);
        /*/

        return averagePrecision;
    }
}
