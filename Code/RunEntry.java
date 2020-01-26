public class RunEntry
{
    public static final int RUN_LEN = 1000;

    public String id;
    public double score;

    public RunEntry(String id, double score)
    {
        this.id = id;
        this.score = score;
    }

    public void increaseScore(double value)
    {
        score = score + value;
    }

    public void multiplyScore(double value)
    {
        score = score * value;
    }
}