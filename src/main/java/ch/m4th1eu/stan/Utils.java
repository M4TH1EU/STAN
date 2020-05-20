package ch.m4th1eu.stan;

import com.darkprograms.speech.synthesiser.SynthesiserV2;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    private static String path = "E:\\JAVA\\WORKSPACE\\IA\\stan\\src\\main\\resources\\";
    private static Gson gson = new Gson();

    public static void readFiles() {
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            JsonObject jsonObject = new JsonObject();
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(Path::toString).collect(Collectors.toList());

            //On parcourt tous les fichiers du dossier "resources"
            for (String fileName : result) {
                try {
                    jsonObject = gson.fromJson(new FileReader(Paths.get(fileName).toFile()), JsonObject.class);

                    //On affiche les infos relatives au fichier
                    System.out.println("\n");
                    System.out.println("File: " + new File(fileName).getName());
                    System.out.println("Content: " + jsonObject);

                } catch (FileNotFoundException e) {
                    System.out.println("Error while reading a file");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Error while listing files");
            e.printStackTrace();
        }
    }


    /**
     * Check in every resource file and return the good output if the sentence matches.
     *
     * @param string The sentence
     */
    public static void response(String string) {
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            JsonObject jsonObject;
            JsonArray jsonArray;
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(Path::toString).collect(Collectors.toList());

            //On parcourt tous les fichiers du dossier "resources"
            for (String filePath : result) {
                try {
                    jsonObject = gson.fromJson(new FileReader(Paths.get(filePath).toFile()), JsonObject.class);
                    jsonArray = jsonObject.getAsJsonArray("input");

                    for (int i = 0; i < jsonArray.size(); i++) {

                        if (jsonArray.get(i) != null) {
                            //Detection
                            String[] sentenceInput = string.toLowerCase().split(" ");
                            String[] sentenceInJson = jsonArray.get(i).toString().replaceAll("\"", "").toLowerCase().split(" ");
                            int min = 0;
                            int max = sentenceInJson.length;

                            for (int j = 0; j < sentenceInput.length; j++) {
                                for (int k = 0; k < sentenceInJson.length; k++) {
                                    if (sentenceInput[j].equalsIgnoreCase(sentenceInJson[k])) {
                                        min++;
                                    }
                                }
                            }

                            if (calculatePercentage(min, max) >= 80) {
                                speak(getOutput(filePath));
                                runAction(filePath);
                                return;
                            }
                        }
                    }

                } catch (FileNotFoundException e) {
                    System.out.println("Error while reading a file");
                    e.printStackTrace();
                }
            }

            speak("Je ne comprends pas la question.");

        } catch (IOException e) {
            System.out.println("Error while listing files");
            e.printStackTrace();
        }
    }

    /**
     * Get what to answer
     *
     * @param filePath
     * @return The output sentence
     */
    public static String getOutput(String filePath) {
        JsonObject jsonObject;
        JsonArray jsonArray;

        try {
            jsonObject = gson.fromJson(new FileReader(Paths.get(filePath).toFile()), JsonObject.class);
            jsonArray = jsonObject.getAsJsonArray("output");

            int randomNumber = new Random().nextInt(Math.max(jsonArray.size(), 1));

            if (jsonArray.size() >= 1 && jsonArray.get(randomNumber) != null) {
                if (jsonArray.get(randomNumber).toString().length() >= 1) {
                    return jsonArray.get(randomNumber).toString().replaceAll("\"", "");
                }
            } else {
                return "Je ne sais pas quoi répondre à cette question... Apprends-moi via le fichier .json de cette dernière.";
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error while getting output sentence");
            e.printStackTrace();
        }

        return "error";
    }

    /**
     * Run actions
     *
     * @param filePath
     * @return The output sentence
     */
    public static String runAction(String filePath) {
        JsonObject jsonObject;

        try {
            jsonObject = gson.fromJson(new FileReader(Paths.get(filePath).toFile()), JsonObject.class);

            if (!jsonObject.getAsJsonObject("action").get("cmd").toString().replaceAll("\"", "").isEmpty()) {
                try {
                    // Run the process
                    Process p = Runtime.getRuntime().exec("cmd /C " + jsonObject.getAsJsonObject("action").get("cmd").toString().replaceAll("\"", ""));
                    // Get the input stream
                    InputStream is = p.getInputStream();

                    // Read script execution results
                    int i = 0;
                    StringBuffer sb = new StringBuffer();
                    while ((i = is.read()) != -1)
                        sb.append((char) i);

                    System.out.println(sb.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (!jsonObject.getAsJsonObject("action").get("void").toString().replaceAll("\"", "").isEmpty()) {
                Functions functions = new Functions();
                Method m = Functions.class.getMethod(jsonObject.getAsJsonObject("action").get("void").toString().replaceAll("\"", ""));
                //m.invoke(functions);
                String returnVal = (String) m.invoke(functions);
                speak(returnVal);
            }


        } catch (Exception e) {
            System.out.println("Error while running actions");
            e.printStackTrace();
        }

        return "error";
    }

    /**
     * Calculate percentage
     *
     * @param obtained
     * @param total
     * @return
     */
    private static double calculatePercentage(double obtained, double total) {
        return obtained * 100 / total;
    }

    /**
     * Convert an url result to string
     *
     * @param url
     * @return url's content
     */
    public static String readUrl(URL url) {
        StringBuilder content = new StringBuilder();

        try {
            URLConnection conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            String inputLine;

            while ((inputLine = br.readLine()) != null) {
                content.append(inputLine);
            }
            br.close();


        } catch (IOException e) {
            e.printStackTrace();
            return "Une erreur est survenue";
        }

        return content.toString();
    }

    /**
     * Speak
     *
     * @param text
     */
    public static void speak(String text) {
        System.out.println(text);
        SynthesiserV2 synthesizer = new SynthesiserV2("AIzaSyAbCAuYS0RD0WLSSRamoeMlwa5nK2e9qaA");
        synthesizer.setLanguage("fr");

        Thread thread = new Thread(() -> {
            try {
                AdvancedPlayer player = new AdvancedPlayer(synthesizer.getMP3Data(text));
                player.play();
            } catch (IOException | JavaLayerException e) {
                e.printStackTrace();
            }
        });

        thread.setDaemon(false);
        thread.start();

    }
}
