/**
 * Tuple (document, score) for a fixed system and topic.
 */
public class RunEntry {
    
    // Private variables
    public String docID;
    public double docScore;

    // Constructor
    public RunEntry(String docID, double docScore) {
        this.docID = docID;
        this.docScore = docScore;
    }

    // Set methods
    public void increaseScore(double value) {
        this.docScore += value;
    }
    public void multiplyScore(double value) {
        this.docScore *= value; 
    }
    
}