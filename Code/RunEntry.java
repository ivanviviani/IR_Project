public class RunEntry
{
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