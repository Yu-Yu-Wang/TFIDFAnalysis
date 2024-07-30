import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.TreeMap;  

public class TFIDFSearch {
    public static void main(String[] args) {
        String inputFileName = args[0] + ".ser"; // The serialized file

        // 反序列化 Indexer 对象
        try (FileInputStream fis = new FileInputStream(inputFileName);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            Indexer idx = (Indexer) ois.readObject();
            HashMap<Integer, HashMap<String, Integer>> fileWordCountData = idx.getFileWordCountData();
            List<Integer> fileWordAmount = idx.getfileWordAmount();

            List<String> keyWords = new ArrayList<>(); 
            try (BufferedReader reader = new BufferedReader(new FileReader(args[1]))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    keyWords.add(line);
                }
                
                TFIDFSort finalAnswer = new TFIDFSort();
                List<String> Answer = finalAnswer.queryAnalysis(keyWords, fileWordCountData, fileWordAmount);
                CreateFile.fileGenerator(Answer);
            }


            // 显示反序列化的内容
            /*for (Map.Entry<Integer, HashMap<String, Integer>> entry : fileWordCountData.entrySet()) {
                System.out.println("File " + entry.getKey() + ":");
                for (Map.Entry<String, Integer> wordEntry : entry.getValue().entrySet()) {
                    System.out.println("Word: " + wordEntry.getKey() + ", Count: " + wordEntry.getValue());
                }
            }*/
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class TFIDFSort {
    public List<String> queryAnalysis(List<String> keyWords, HashMap<Integer, HashMap<String, Integer>> fileWordCountData, List<Integer> fileWordAmount) {
        int requireNumber = Integer.parseInt(keyWords.get(0));
        String intersection = "AND";
        String union = "OR";
        List<String> Answer = new ArrayList<>(); 
        
        
        for(int i = 1 ; i < keyWords.size() ; i++) {
            List<Integer> targetFiles = new ArrayList<>();
            String require = keyWords.get(i);
            
            if(require.contains(union)) {
                require = require.replaceAll(union, ""); // 删除 "OR"
                String[] parts = require.split("\\s+"); // 拆分关键字
                int[] keywordAppearFileAmount = new int[parts.length]; // 记录每个关键字出现的文件数量
                Set<String> partsSet = new HashSet<>();
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        partsSet.add(part);
                        System.out.println(part);
                    }
                }
                
                for (int j = 0; j < fileWordCountData.size(); j++) {
                    HashMap<String, Integer> wordCounts = fileWordCountData.get(j);
                    boolean containsAnyKeyword = false; // 是否包含任何一个关键字的标志
                    for (int k = 0; k < parts.length; k++) {
                        if (wordCounts.containsKey(parts[k]) && wordCounts.get(parts[k]) > 0) {
                            containsAnyKeyword = true;
                            keywordAppearFileAmount[k]++; // 如果包含关键字，增加关键字出现的文件数量
                        }
                    }
                    if (containsAnyKeyword && !targetFiles.contains(j)) {
                        targetFiles.add(j); // 记录文件编号
                    }
                }

                TFIDFCalculate hashMapOfTFIDF = new TFIDFCalculate();
                HashMap<Integer, Double> TFIDFValue = hashMapOfTFIDF.calculate(targetFiles, fileWordCountData, fileWordAmount, parts, keywordAppearFileAmount);
                List<Integer> answer = hashMapOfTFIDF.queue(TFIDFValue, requireNumber);
                String result = answer.stream()
                .map(Object::toString) // 将整数转换为字符串
                .collect(Collectors.joining(" "));
                Answer.add(result);
                //System.out.println(result);
            }

            else {
                require = require.replaceAll(intersection, "");
    
                String[] parts = require.split("\\s+");
                int keywordAppearFileAmount[] = new int[parts.length]; // 记录每个关键字出现的文件数量
                Set<String> partsSet = new HashSet<>();
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        partsSet.add(part);
                        System.out.println(part);
                    }
                }

                for (int docId :fileWordCountData.keySet()) {
                    HashMap<String, Integer> wordCounts = fileWordCountData.get(docId);

                    boolean allValuesGreaterThanZero = true;
                    for (String part : partsSet) {
                        if (!wordCounts.containsKey(part) || wordCounts.get(part) <= 0) {
                            allValuesGreaterThanZero = false;
                            break;
                        }
                    }

                    if (allValuesGreaterThanZero && !targetFiles.contains(docId)) {
                        targetFiles.add(docId); // 記錄所有單詞出現過的檔案編號
                    }

                    for (int k = 0; k < parts.length; k++) {
                        if (wordCounts.containsKey(parts[k]) && wordCounts.get(parts[k]) > 0) {
                            keywordAppearFileAmount[k]++; // 計算每個關鍵詞出現的檔案數量
                        }
                    }
                    
                }
                TFIDFCalculate hashMapOfTFIDF = new TFIDFCalculate();
                HashMap<Integer, Double> TFIDFValue = hashMapOfTFIDF.calculate(targetFiles, fileWordCountData, fileWordAmount, parts, keywordAppearFileAmount);
                List<Integer> answer = hashMapOfTFIDF.queue(TFIDFValue, requireNumber);
                String result = answer.stream()
                .map(Object::toString) // 将整数转换为字符串
                .collect(Collectors.joining(" "));
                Answer.add(result);

                //System.out.println(result);
            }
            
        }
        return Answer;
    }
}

class TFIDFCalculate {
    public HashMap<Integer, Double> calculate (List<Integer> targetFiles, HashMap<Integer, HashMap<String, Integer>> fileWordCountData, List<Integer> fileWordAmount, String[] parts, int[] keywordAppearFileAmount) {
        HashMap<Integer, Double> TFIDFValue = new HashMap<>();
        for (int number = 0 ; number < targetFiles.size() ; number++) {
            int targetFilesNumber = targetFiles.get(number);//取得目標檔案的ID
            //System.out.println("targetFilesNumber:"+targetFilesNumber);
            HashMap<String, Integer> targetFilesWordCounts = fileWordCountData.get(targetFilesNumber);//取得目標檔案的內容
            //System.out.println("targetFilesWordCounts:"+targetFilesWordCounts);
            //System.out.println("fileWordAmount:"+fileWordAmount);
            int wordsAmountInTargetFile = fileWordAmount.get(targetFilesNumber);//取得目標檔案的字數
            double sum = 0;
            for (int k = 0; k < parts.length; k++) {
                if (targetFilesWordCounts.get(parts[k]) == null) {
                    targetFilesWordCounts.put(parts[k], 0);
                }
                //System.out.println("targetFilesWordCounts:"+targetFilesWordCounts);
                double tf = (double) targetFilesWordCounts.get(parts[k]) / wordsAmountInTargetFile;
                //System.out.println(targetFilesWordCounts.get(parts[k]));
                //System.out.println("wordsAmountInTargetFile:"+wordsAmountInTargetFile);
                //System.out.println("tf:"+tf);
                double idf = 0;
                if(keywordAppearFileAmount[k]==0) idf = 0;
                else idf = Math.log((double) fileWordCountData.size() / keywordAppearFileAmount[k]);
                //System.out.println(fileWordCountData.size());
                //System.out.println(keywordAppearFileAmount[k]);
                //System.out.println("idf:"+idf);
                double tfidf = tf * idf;
                sum+=tfidf;
            }
            TFIDFValue.put(targetFilesNumber, sum);
        }
        //System.out.println("TFIDF:"+TFIDFValue);
        return TFIDFValue;
    }

    public List<Integer> queue(HashMap<Integer, Double> TFIDFValue, int requireNumber) {
        // 使用 TreeMap 將 TF-IDF 值排序
        TreeMap<Double, List<Integer>> sortedMap = new TreeMap<>(Collections.reverseOrder());
    
        // 將 TF-IDF 值與相應的文件編號放入 TreeMap 中
        for (Map.Entry<Integer, Double> entry : TFIDFValue.entrySet()) {
            if (!sortedMap.containsKey(entry.getValue())) {
                sortedMap.put(entry.getValue(), new ArrayList<>());
            }
            sortedMap.get(entry.getValue()).add(entry.getKey());
        }
    
        // 找出前 requireNumber 大的文件編號
        List<Integer> topFileNumbers = new ArrayList<>();
        int count = 0;
        for (Map.Entry<Double, List<Integer>> sortedEntry : sortedMap.entrySet()) {
            double tfidf = sortedEntry.getKey();
            List<Integer> files = sortedEntry.getValue();
            Collections.sort(files); // 对文件编号列表进行排序
            for (int file : files) {
                topFileNumbers.add(file);
                if (++count == requireNumber) {
                    // 如果需要打印前几大的 TF-IDF 值，可以在这里添加打印语句
                    return topFileNumbers;
                }
            }
        }
    
        // 如果找到的文件編號數不足，用 -1 補足
        while (topFileNumbers.size() < requireNumber) {
            topFileNumbers.add(-1);
        }
        
        return topFileNumbers;
    }    
}

class CreateFile {
    public static void fileGenerator(List<String> answer) {
        try (BufferedWriter outputWriter = new BufferedWriter(new FileWriter("output.txt"))) {
            for (String value : answer) {
                outputWriter.write(value+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
