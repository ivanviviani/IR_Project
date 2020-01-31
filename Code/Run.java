import java.util.*;
/**
 * Tuple (system, topic) for a fixed TREC version.
 */
public class Run {
    
    // Private variables
    public ArrayList<RunEntry> list;

    // Constructor
    public Run() {
        this.list = new ArrayList<>();
    }

    // Alternative constructor
    public Run(Collection<RunEntry> coll) {
        this.list = new ArrayList<>(coll);
    }

}