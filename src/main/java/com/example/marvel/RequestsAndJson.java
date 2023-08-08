package com.example.marvel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/*
A class to request and process HttpRequests, JSON response
 */

public class RequestsAndJson {

    //    base api url
    private static final String baseUrl = "https://gateway.marvel.com:443/v1/public/";

    //    public user key, required to make api requests
    private static final String apiPublicKey = "&apikey=644af64d95d4824d247aa0115bfacaa2";

    //    md5 hash, required for HttpRequests
    private static final String mdHash = "&hash=e53155456033b84f33db9f8ab09edf99";

    //    A class constructor that initializes an HttpClient that;ll be used to make all HttpRequests
    public RequestsAndJson() {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /*
    A method to parse response json body and to save required data in a structured format.
    It takes in a JSON string as parameter and processes it to extract required fields.
    All information is saved in a variable of type Object array which is returned
     */
    public static Object[][] extractComicData(String response) throws Exception {

//        checking if response is not empty(it can happen if wrong information is requested)
        if (response == null || response.isEmpty()) {
            System.out.println("Empty response received.");
            return null;
        }

//      a json parser to parse the response
        JSONParser parser = new JSONParser();

//        create a JSONObject based on the given json String
        JSONObject jsonResponse = (JSONObject) parser.parse(response);

//        get "data" key
        JSONObject data = (JSONObject) jsonResponse.get("data");

//        from inside data, get "results"(number of comics, and related details) field(an array) and save as a JSON Array
        JSONArray results = (JSONArray) data.get("results");


        if (results != null && !results.isEmpty()) {

//            an object to store individual comics(nd information)
            Object[][] comicDataArray = new Object[results.size()][5];

//            iterate through results array
            for (int i = 0; i < results.size(); i++) {
//                for current item, create a new JSON Object
                JSONObject comic = (JSONObject) results.get(i);

//                from current item, get "comic title"
                String title = (String) comic.get("title");// comic title
//                get "comic description"
                String description = (String) comic.get("description");// comic description

                // extract thumbnail information
                JSONObject thumbnailObject = (JSONObject) comic.get("thumbnail");
                String thumbnailUrl = thumbnailObject.get("path") + "." + thumbnailObject.get("extension");// comic thumbnail url

                // extract characters as a list of strings
                Object charactersObj = comic.get("characters");

                List<String> charactersList = new ArrayList<>();// list to store characters name

                if (charactersObj instanceof JSONObject) {
                    JSONObject characters = (JSONObject) charactersObj;
                    JSONArray charactersArray = (JSONArray) characters.get("items");//get to the "items" fields which have details about comic characters

//                   iterate through the items array and from every item extract character name that appears in the comic
                    for (Object characterObj : charactersArray) {
                        JSONObject character = (JSONObject) characterObj;
                        String characterName = (String) character.get("name");
                        charactersList.add(characterName);// append all characters to the charactersList list
                    }
                }

//                iterate through the "urls" field, which has information about individual comic web page
                JSONArray urlsArray = (JSONArray) comic.get("urls");

                List<String> urls = new ArrayList<>();// list to store urls
                for (Object urlObj : urlsArray) {
                    JSONObject url = (JSONObject) urlObj;
                    String urlString = (String) url.get("url");// get current url
                    urls.add(urlString);// append it to the urls list
                }

//                create a new object(for a comic) and add it to the Object array that is to be returned
                comicDataArray[i] = new Object[]{
                        title,// append title
                        description,// append description
                        thumbnailUrl,// append image
                        charactersList,// append comic characters
                        urls// append comic urls
                };
            }

            return comicDataArray;
        }

        return null;
    }


    /*
    A method to return comic data related to a character.
    It takes in a marvel character as a parameter and calls getCharacterId() method to get character id which is required to make the comic request.
    Then with that id, a new Http request is made which returns comic data related to the character, such as comic title,
    comic thumbnail, characters in the comic etc.
     */
    public static String getComicData(String character) {
        try {
            String characterId = getCharacterId(character);// calling the getCharacterId method

            if (characterId != null) {
                String comicsUrl = baseUrl + "characters/" + characterId + "/comics?format=digital%20comic&formatType=comic&noVariants=true&hasDigitalIssue=true&orderBy=title" + "&ts=1" + apiPublicKey + mdHash;
                return makeHttpRequest(comicsUrl);// make comic request and return it
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    //    A helper method that takes in a marvel character as a parameter and makes an HttpRequest to get character id. From the
//    response body it extracts the character id ad returns it. This id is later used to get comic details
//    related to that character
    private static String getCharacterId(String character) throws Exception {

//        url for the Http request
        String encodedCharacter = URLEncoder.encode(character, "UTF-8"); // Encode the character name
        String characterUrl = baseUrl + "characters?name=" + encodedCharacter + "&ts=1" + apiPublicKey + mdHash;

//        get HttpRequest response and save it as a String
        String response = makeHttpRequest(characterUrl);

        // Parse the response to extract character id
        try {
//            initializing the parser
            JSONParser parser = new JSONParser();
//            saving the parsed data in jsonResponse
            JSONObject jsonResponse = (JSONObject) parser.parse(response);
//            get the data key from response(it contains the id)
            JSONObject data = (JSONObject) jsonResponse.get("data");
//            from data get results array
            JSONArray results = (JSONArray) data.get("results");

//            check if the result is not null and is not empty
            if (results != null && !results.isEmpty()) {
                JSONObject characterData = (JSONObject) results.get(0);
                return characterData.get("id").toString();// return the id
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while extracting the id");
        }

        return null;
    }


    private static String makeHttpRequest(String urlString) throws IOException, InterruptedException, URISyntaxException {
        // Create an HttpClient instance
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Create an HttpRequest instance with the GET method
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(urlString))
                .GET()
                .build();

        // Send the request and receive the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Return the response body
        return response.body();
    }


    public static void main(String[] args) throws Exception {
        RequestsAndJson r = new RequestsAndJson();

        String characterComic = getComicData("Captain America");

        Object[][] comics = extractComicData(characterComic);

        for (Object[] comic : comics) {
            String title = (String) comic[0];
            String description = (String) comic[1];
            String thumbnailUrl = (String) comic[2];
            List<String> characters = (List<String>) comic[3];
            List<String> urls = (List<String>) comic[4];

            System.out.println("Title: " + title);
            System.out.println("Description: " + description);
            System.out.println("Thumbnail URL: " + thumbnailUrl);

            System.out.println("Characters:");
            for (String character : characters) {
                System.out.println("- " + character);
            }

            System.out.println("URLs:");
            for (String url : urls) {
                System.out.println("- " + url);
            }

            System.out.println();
        }
    }
}
//yahoy86570@royalka.com