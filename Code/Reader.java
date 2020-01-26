import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

public class Reader
{
    public static RunEntry[][] extractRunByTopic(int topic, int[] sys, String folder)
    {
        /*
         * Initialize utilities as file location and array with sampling the systems.
         */
        File dir = new File(folder);
        File[] input = dir.listFiles();

        /*
         * For each of the sampled system, create the run related to the topic.
         */
        RunEntry[][] run = new RunEntry[sys.length][5];
        for (int i = 0; i < sys.length; i++)
        {
            try
            {
                Scanner scan = new Scanner(input[sys[i]]);
                int j = 0;
                while (scan.hasNextLine())
                {
                    String[] line = tokenize(scan.nextLine());
                    if (Integer.parseInt(line[0]) == topic && j < 5)
                    {
                        run[i][j++] = new RunEntry(line[2], Double.parseDouble(line[4]));
                    }
                    else if (Integer.parseInt(line[0]) > topic || j >= 5)
                    {
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return run;
    }

    public static RunEntry[][] extractRunBySystem(int sys, int[] topicRange, String folder)
    {
        /*
         * Initialize utilities as file location and array with sampling the systems.
         */
        File dir = new File(folder);
        File[] input = dir.listFiles();
        File f = input[sys];

        /*
         * For each topic of the system, create a run.
         */
        RunEntry[][] run = new RunEntry[topicRange[1] - topicRange[0]][5];
        try
        {
            Scanner scan = new Scanner(f);
            while (scan.hasNextLine())
            {
                String[] line = tokenize(scan.nextLine());
                int topic = Integer.parseInt(line[0]) - topicRange[0];
                int rank = Integer.parseInt(line[3]) - 1;
                run[topic][rank] = new RunEntry(line[2], Double.parseDouble(line[4]));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return run;
    }

    public static HashSet<String> extractJudgement(int topic, String filename)
    {
        /*
         * Initialize the hashset that will contain the relevant documents.
         */
        HashSet<String> relevant = new HashSet<>();

        /*
         * For the requested topic, save all the document that are relevant.
         */
        try
        {
            Scanner scan = new Scanner(new File(filename));
            while (scan.hasNextLine())
            {
                String[] line = tokenize(scan.nextLine());
                if (Integer.parseInt(line[0]) == topic && Integer.parseInt(line[3]) == 1)
                {
                    relevant.add(line[2]);
                }
                else if (Integer.parseInt(line[0]) > topic)
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return relevant;
    }

    public static int[] reservoirSampling(int limit, int num)
    {
        /*
         * Reservoir sampling
         */
        Random r = new Random();
        int[] rnd = new int[num];

        for (int i = 0; i < num; i++)
        {
            rnd[i] = i;
        }

        for (int i = num; i < limit; i++)
        {
            int t = r.nextInt(i);
            if (t < num)
            {
                rnd[t] = i;
            }
        }

        return rnd;
    }

    private static String[] tokenize(String str)
    {
        ArrayList<String> token = new ArrayList<>();
        Scanner scan = new Scanner(str);
        while(scan.hasNext())
        {
            token.add(scan.next());
        }
        return token.toArray(new String[0]);
    }
}