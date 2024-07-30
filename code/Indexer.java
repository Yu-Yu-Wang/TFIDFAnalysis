import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Indexer implements Serializable {
    private static final long serialVersionUID = 1L;
    private HashMap<Integer, HashMap<String, Integer>> fileWordCountData;
    private List<Integer> fileWordAmount;
    public Indexer(HashMap<Integer, HashMap<String, Integer>> fileWordCounts, List<Integer>fileWordAmount) {
        this.fileWordCountData = fileWordCounts;
        this.fileWordAmount = fileWordAmount;
    }

    public HashMap<Integer, HashMap<String, Integer>> getFileWordCountData() {
        return fileWordCountData;
    }

    public List<Integer> getfileWordAmount() {
        return fileWordAmount;
    }
}