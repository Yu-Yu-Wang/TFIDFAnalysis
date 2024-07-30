import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BuildIndex {
    static void testa(){
        System.out.println("test");
    }
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide the input file name as a command line argument.");
            System.exit(1);
        }
        BuildIndex.testa();
        String inputFileName = args[0]; // Replace with your input file
        String outputFileName = args[0].replaceAll(".txt", ".ser");

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = ModelAdjust.processText(line);
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Paragraph paragraph = new Paragraph();
        List<String> paragraphSort = paragraph.processParagraphs(lines);
        List<Integer> fileWordAmount = new ArrayList<>();
        HashMap<Integer, HashMap<String, Integer>> fileWordCountData = new HashMap<>();
        for (int i = 0; i < paragraphSort.size(); i++) {
            String fileContent = paragraphSort.get(i);
            HashMap<String, Integer> wordCountMap = MapDataSort.countEachWordsInFile(fileContent);
            fileWordCountData.put(i, wordCountMap);
        }

        fileWordAmount = MapDataSort.countWordsInEachFile();
        // 创建 Indexer 对象
        Indexer idx = new Indexer(fileWordCountData, fileWordAmount);

        // 序列化 Indexer 对象
        try (FileOutputStream fos = new FileOutputStream(outputFileName);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(idx);
            System.out.println("Index serialized to " + outputFileName);
            oos.close();
	        fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MapDataSort {
    private static List<Integer> fileWordAmount = new ArrayList<>();
    public static HashMap<String, Integer> countEachWordsInFile(String fileContent) {
        HashMap<String, Integer> wordCountMap = new HashMap<>();
        String[] words = fileContent.split("\\s+");
        fileWordAmount.add(words.length);
        for (String word : words) {
            if (!word.isEmpty()) {
                wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
            }
        }
        return wordCountMap;
    }
    
    public static List<Integer> countWordsInEachFile(){
        return fileWordAmount;
    }
}

class ModelAdjust {
    public static String processText(String text) {
        return text.replaceAll("[^a-zA-Z]+", " ").toLowerCase().trim();
    }
}


class Paragraph {
    public List<String> processParagraphs(List<String> lines) {
        int rows = lines.size();
        List<String> paragraphSort = new ArrayList<>();
        String[] paragraphContent = new String[rows / 5];
        String[] lineContent = new String[5];
        int paragraphNumber = 0;
        int rows1 = 0;

        for (String line : lines) {
            lineContent[rows1] = ModelAdjust.processText(line);
            rows1 += 1;
            if (rows1 == 5) {
                paragraphContent[paragraphNumber] = String.join(" ", lineContent);
                paragraphSort.add(paragraphContent[paragraphNumber]);
                paragraphNumber++;
                rows1 = 0;
            }
        }
        return paragraphSort;
    }
}