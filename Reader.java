import java.io.File;
import java.util.*;

public class Reader {

    /**
     * Create a matrix of Runs [topic][system]
     * A run for each topic and system is an ArrayList<RunEntry>.
     * @param numTopicsRange Range of topic numbers:
     *                    numTopicsRange[0] -> min topic number
     *                    numTopicsRange[1] -> max topic number + 1
     * @param folder Folder containing the input files.
     * @return Matrix of all the runs.
     */
    public static Run[][] extractSuperRun(int[] numTopicsRange, String folder) {

        // Prepare to read the input files
        File dir = new File(folder);
        File[] inputFiles = dir.listFiles();
        Scanner scan;
        // Number of systems
        int numSys = inputFiles.length;
        // Number of topics
        int numTopics = numTopicsRange[1] - numTopicsRange[0];
        // Initialization of the return value
        Run[][] superRun = new Run[numTopics][numSys];
        // Current system index (matrix rows)
        int i_sys;

        // Parse the input files (one for each system)
        for(i_sys = 0; i_sys < numSys; i_sys++) {

            // Current topic index
            int i_topic = 0;

            try {
                
                // Create current run (matrix entry)
                Run currRun = new Run();
                // Open Scanner
                scan = new Scanner(inputFiles[i_sys]);
                
                // Parse current input file
                while(scan.hasNextLine()) {

                    // Tokenize current line
                    String[] currLine = tokenize(scan.nextLine());
                    /* Now:
                        currLine[0] = topic number
                        currLine[1] = fixed field (Q0)
                        currLine[2] = document id (string)
                        currLine[3] = document rank (not considered)
                        currLine[4] = document score (normalized in [0,1])
                        currLine[5] = system id (string)
                     */

                     // Support variable for topic matching
                     int tempTopic = Integer.parseInt(currLine[0]) - numTopicsRange[0];

                     // Create run element (RunEntry)
                     RunEntry currRunElement = new RunEntry(currLine[2], Double.parseDouble(currLine[4]));

                     // If new topic number reached, update variables
                     if(tempTopic != i_topic) {
                         // Done with current topic, add its run for current system to the matrix
                         superRun[i_topic][i_sys] = currRun;
                         // Update variables for the next topic
                         i_topic++;
                         // Create run for the new topic
                         currRun = new Run(); 
                     }

                     // Here current line is still relative to current topic
                     // Add current run element to current run
                     currRun.list.add(currRunElement);

                }

                // Exit while loop at last line of last topic (i_topic = numTopics - 1)
                // Add last topic run for current system to the return value
                superRun[i_topic][i_sys] = currRun;
                // Close Scanner
                scan.close();

            }
            catch (Exception e) { e.printStackTrace(); }
        }

        // Return the super run (matrix)
        return superRun;
    }

    /**
     * Create an array of hash maps that for each topic associate a document 
     * to its ranks in all the systems.
     * For a single hash map: key = document, value = array of numSys ranks.
     * Rank is 0-based, while null means not found in the run. 
     * @param superRun Matrix [topic][system] of Runs.
     * @return Array of hash maps for every topic.
     */
    public static HashMap<String, Integer[]>[] extractComparator(Run[][] superRun) {
        // Not found = 0
        // Found = rank index 1-based
        // Number of topics and systems
        int numTopics = superRun.length;
        int numSys = superRun[0].length;
        // Current topic index
        int i_topic;
        // Initialization of the return value
        HashMap<String,Integer[]>[] cmp = new HashMap[numTopics];

        // Scan topics (rows)
        for(i_topic = 0; i_topic < numTopics; i_topic++) {

            // Create array element (hash map for topic i_topic)
            cmp[i_topic] = new HashMap<>();
            // Current system index
            int i_sys;

            // Scan systems (columns)
            for(i_sys = 0; i_sys < numSys; i_sys++) {

                // Now superRun[i_topic][i_sys] is a Run instance
                // NOTE: runs (lists of RunEntry) can have different lengths

                // Current run (list of RunEntry) 
                Run currRun = superRun[i_topic][i_sys];

                // For each RunEntry in the run, add its docID (key) to the hash
                // map if not present
                for(RunEntry ds : currRun.list) {
                    // Current document id
                    String currDocID = ds.docID;
                    // If not present, add it to the hash map for topic i_topic
                    if(!cmp[i_topic].containsKey(currDocID)) {
                         // (for now create an empty value (Integer[numSys]))
                        cmp[i_topic].put(currDocID, new Integer[numSys]);
                    }
                    // For currDocID (for i_topic), get its ranks array and
                    // put its rank for system i_sys.
                    // NOTE: rank = 1 + index of RunEntry in currRun.list (ArrayList<RunEntry>)
                    Integer[] ranks = cmp[i_topic].get(currDocID);
                    ranks[i_sys] = 1 + currRun.list.indexOf(ds);
                }
            }
        }
        // Return array of hash maps
        return cmp;
    }

    /**
     * Read all the relevance judgements from a text file, for a given
     * number of topics. Used for each edition of TREC-X (X=3,5,9).
     * In particular, get the relevant documents for each topic.
     * @param numTopics Number of topics.
     * @param filename Text file containing the relevance judgements.
     * @return Array of hash sets with relevant documents (strings) per topic.
     */
    public static HashSet<String>[] extractRelJudgements(int numTopics, String filename) {
        
        // Initialization of the return value
        HashSet<String>[] relDocsByTopic = new HashSet[numTopics];
        // Index over the return value, from 0 to numTopics
        int i_topic = 0;
        // Scanner for reading the text file
        Scanner scan;
    
        // Parse the text file
        try {

            // Open Scanner
            scan = new Scanner(new File(filename));
            // Current topic (before initialization)
            int currTopic = -1;
            // Create hash set for current topic's relevant documents
            HashSet<String> currTopicRelDocs = new HashSet<>();

            // Scan the file 
            while(scan.hasNextLine()) {

                // Tokenize the current line
                String[] currLine = tokenize(scan.nextLine());
                /* Now:
                    currLine[0] = topic number
                    currLine[1] = fixed field (0)
                    currLine[2] = document id (string)
                    currLine[3] = document relevance judgement (0 or 1) 
                 */

                // Initialization of currTopic (only once)
                if(currTopic == -1) currTopic = Integer.parseInt(currLine[0]);

                // If new topic number reached, update variables
                if(Integer.parseInt(currLine[0]) != currTopic) {
                    // Done with currTopic, add its hash set of relevant documents
                    // to the return value
                    relDocsByTopic[i_topic] = currTopicRelDocs;
                    // Update variables for the next topic
                    i_topic++;
                    currTopic++;
                    // Create hash set for the new topic
                    currTopicRelDocs = new HashSet<>();
                }
                
                // Here current line is still relative to current topic
                // If relevant document, add it to the current topic hash set 
                if(Integer.parseInt(currLine[3]) > 0) currTopicRelDocs.add(currLine[2]);

            }

            // Exit while loop at last line of last topic (i_topic = numTopics - 1)
            // Add last topic hash set of relevant documents to the return value
            relDocsByTopic[i_topic] = currTopicRelDocs;
            // Close Scanner
            scan.close();

        }
        catch (Exception e) { e.printStackTrace(); }

        // Return relevant documents by topic
        return relDocsByTopic;
    }

    /**
     * Perform reservoir sampling.
     * @param streamSize Size of the stream.
     * @param numSamples Number of samples required.
     * @return Array with sampled positions.
     */
    public static int[] reservoirSampling(int streamSize, int numSamples) {

        Random r = new Random();
        int[] sampledPos = new int[numSamples];

        for(int i = 0; i < numSamples; i++) sampledPos[i] = i;

        for(int i = numSamples; i < streamSize; i++) {
            int t = r.nextInt(i);
            if(t < numSamples) sampledPos[t] = i;
        }
        
        return sampledPos;
    }

    /**
     * String tokenizer for a line of a TREC relevance judgements file,
     * in the standard TREC format.
     * @param line Line to tokenize.
     * @return ArrayList of line tokens.
     */
    private static String[] tokenize(String line) {
        // Initialization
        ArrayList<String> tokens = new ArrayList<>();
        // Open Scanner for reading the line
        Scanner scan = new Scanner(line);
        // Parse the line
        while(scan.hasNext()) tokens.add(scan.next());
        // Close Scanner
        scan.close();
        // Return line tokens
        return tokens.toArray(new String[0]);
    }

}