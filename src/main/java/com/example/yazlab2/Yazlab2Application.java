package com.example.yazlab2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.*;
import org.bson.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@Controller

public class Yazlab2Application {

    private List<String> texts = new ArrayList<>();
    String combinedText;
    Long elapsedTime;
    @GetMapping("/")
    public String showForm() {
        System.out.println("testdsad");
        return "index";
    }
    @GetMapping("/result")
    public String showForm1(Model model) {
        System.out.println("fsadastestdsad");
        model.addAttribute("time",Long.toString(elapsedTime)+" ms");
        model.addAttribute("combinedText",combinedText.trim());
        return "index";
    }

    @PostMapping("/api/text")
    @ResponseBody
    public String inputText(@RequestParam("text1") String text1,
                              Model model) {

        texts.add(text1);

        return "/index";
    }


    @PostMapping("/process-texts")
    @ResponseBody
    public void processTexts(Model model) {
        var client = MongoClients.create();
        var database = client.getDatabase("yazlab");
        var collection = database.getCollection("girdi");
        long startTime = System.currentTimeMillis();
        combinedText = combineTexts(texts);
        long endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        var document = new Document()

                .append("girdi", texts)
                .append("cikti", combinedText.trim())
                .append("sure",Long.toString(elapsedTime)+" ms");
        collection.insertOne(document);


        client.close();

        System.out.println("Combined text:");
        System.out.println(combinedText);

        texts.clear();



    }
    private static String combineTexts(List<String> texts) {


        List<List<String>> wordsList = new LinkedList<>();
        int textNumber = 0;
        for (String text : texts) {
            String[] words = text.split("\\s+");
            List<String> wordList = new ArrayList<>(Arrays.asList(words)); // Create a new ArrayList
            wordsList.add(wordList);
            textNumber+=1;

        }

        StringBuilder combinedTextBuilder = new StringBuilder();
        boolean done = false;
        int currentTextIndex = 0;
        int currentWordIndex = 0;
        int lastIndex[] = new int[textNumber];
        for(int i = 0;i<textNumber;i++){
            lastIndex[i] = 0;
        }
        boolean secondFound = false;
        while (!done) {
            List<String> currentWords = wordsList.get(currentTextIndex);
            if (currentWordIndex >= currentWords.size()) {

                done = true;
                break;
            }
            String currentWord = currentWords.get(currentWordIndex);
            combinedTextBuilder.append(currentWord).append(" ");




            int nextTextIndex = getNextTextIndex(wordsList, currentTextIndex, currentWord);
            if (nextTextIndex >= 0) {
                List<String> nextWords = wordsList.get(nextTextIndex);

                int commonIndex = nextWords.indexOf(currentWord);
                if(commonIndex<lastIndex[nextTextIndex]){
                    secondFound = false;
                    for (int i = lastIndex[nextTextIndex]+1; i < nextWords.size(); i++) {
                        if (nextWords.get(i).equals(currentWord)) {
                            lastIndex[currentTextIndex] = currentWordIndex;
                            currentTextIndex = nextTextIndex;

                            currentWordIndex = i >= 0 ? i + 1 : 0;
                            secondFound = true;

                            break;
                        }

                    }
                    if(!secondFound){
                        //lastIndex = currentWordIndex;
                        currentWordIndex+=1;
                    }
                }

                else{
                    lastIndex[currentTextIndex] = currentWordIndex;
                    currentTextIndex = nextTextIndex;
                    currentWordIndex = commonIndex >= 0 ? commonIndex + 1 : 0;
                }

            } else {
                currentWordIndex+=1;
            }


        }

        String combinedText = combinedTextBuilder.toString().trim();
        return combinedText;
    }

    private static int getNextTextIndex(List<List<String>> wordsList, int currentTextIndex, String currentWord) {
        for (int i = 0; i < wordsList.size(); i++) {
            if(i==currentTextIndex)
                continue;
            List<String> words = wordsList.get(i);
            if (words.contains(currentWord)) {
                return i;
            }
        }
        return -1;
    }
    public static void main(String[] args) {
        SpringApplication.run(Yazlab2Application.class, args);
    }
}
