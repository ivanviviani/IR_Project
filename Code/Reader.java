import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

public class Reader
{
    /**
     * Create a super run containing the run of all topics, all systems. In particular create a
     * mulidimensional array: [topic][system][rank in run]
     *
     * @param topicsRange The range of topic number: tR[0] -> min topic number, tR[1] -> max topic number +1
     * @param folder The folder containin all the files.
     * @return The super run multidimensional array.
     */
    public static RunEntry[][][] extractSuperRun(int[] topicsRange, String folder)
    {
        File dir = new File(folder);
        File[] input = dir.listFiles();
        RunEntry[][][] superRun = new RunEntry[topicsRange[1]-topicsRange[0]][input.length][RunEntry.RUN_LEN];

        /*
         * For each of the sampled system, for each topic create the run related to the topic.
         */
        for (int i = 0; i < input.length; i++)
        {
            try
            {
                Scanner scan = new Scanner(input[i]);
                int currTopic = 0;
                ArrayList<RunEntry> currRun = new ArrayList<>();
                while (scan.hasNextLine())
                {
                    String[] line = tokenize(scan.nextLine());
                    int topic = Integer.parseInt(line[0])-topicsRange[0];
                    RunEntry currEntry = new RunEntry(line[2], Double.parseDouble(line[4]));

                    if(topic!=currTopic)
                    {
                        superRun[currTopic][i] = currRun.toArray(new RunEntry[0]);

                        currTopic++;
                        currRun = new ArrayList<>();
                    }

                    currRun.add(currEntry);
                }
                superRun[currTopic][i] = currRun.toArray(new RunEntry[0]);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        /*/
        for(int topic = 0; topic<superRun.length;topic++)
        {
            for(int sys = 0;sys<superRun[topic].length;sys++)
            {
                for(int rank = 0;rank<superRun[topic][sys].length;rank++)
                {
                    if(superRun[topic][sys][rank]==null)
                    {
                        System.out.println("NULL: "+topic+" "+sys+" "+rank);
                    }

                }
            }
        }
        /*/
        return superRun;
    }

    /**
     * Reads all the relevance judgements.
     *
     * @param topics The non normalized number of topic.
     * @param filename The name of the file containing the data.
     * @return The array of hash set with the relevant documents per topic.
     */
    public static HashSet<String>[] extractJudgement(int topics, String filename)
    {
        /*
         * Initialize the hashset that will contain the relevant documents.
         */
        HashSet<String>[] relevant = new HashSet[topics];
        int i = 0;
        /*
         * For the requested topic, save all the document that are relevant.
         */
        try
        {
            Scanner scan = new Scanner(new File(filename));
            int currTopic = -1;
            HashSet<String> currJudge = new HashSet<>();
            while (scan.hasNextLine())
            {
                String[] line = tokenize(scan.nextLine());
                // Initialize currTopic (once)
                if(currTopic==-1)
                {
                    currTopic = Integer.parseInt(line[0]);
                }

                if(Integer.parseInt(line[0]) != currTopic)
                {
                    relevant[i++] = currJudge;
                    currJudge = new HashSet<>();
                    currTopic++;
                }

                if (Integer.parseInt(line[3]) == 1)
                {
                    currJudge.add(line[2]);
                }
            }
            relevant[i] = currJudge;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return relevant;
    }

    /**
     * Performs a reservoir sampling.
     *
     * @param limit The size of the stream.
     * @param num The number of sample required.
     * @return The array with the sampled positions.
     */
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
