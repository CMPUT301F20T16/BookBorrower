package com.example.vivlio.Controllers;

import com.google.gson.stream.JsonReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class fetches information from the google books api.
 * At the moment it only retrieves a book's title and author, which
 * can be accessed with {@code getTitle()} and {@code getAuthor()}.
 */
public class BookDetailFetcher {
    private URL url;
    private HttpURLConnection urlConnection;
    private String title;
    private String authors;
    private Boolean found;
    private Boolean titleFilled;
    private Boolean subtitleFilled;
    private Boolean authorFilled;

    public BookDetailFetcher() {
        title = null;
        authors = null;
        found = false;
        titleFilled = false;
        subtitleFilled = false;
        authorFilled = false;
    }

    /**
     * This method sends a HTTP request to the google books api,
     * then calls {@code readStream(InputStream)} to read and parse the
     * sequence of bytes.
     * @param isbn The ISBN code used as a search term
     */
    public void request(String isbn) {

        try {
            url = new URL("https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn);
            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            readStream(in);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            urlConnection.getErrorStream();
        } finally {
            urlConnection.disconnect();
        }
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return authors;
    }

    public Boolean isFound() {return found;}

    /**
     * Turns the input stream into a JSON object and reads/parses for the book's
     * title and authors. Saves them under their respective class attributes.
     * @param inStream The stream of bytes returned from the HTTP request
     * @throws IOException If an I/O error occurs in JsonReader methods
     */
    private void readStream(InputStream inStream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(inStream, "UTF-8"));

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("items")) {

                reader.beginArray();
                while (reader.hasNext()) {
                    readItemsArray(reader);
                }
                reader.endArray();
            } else if (name.equals("totalItems")) {
                int totalItems = reader.nextInt();

                // if the api doesn't find the book
                if (totalItems == 0) {
                    found = false;
                } else {
                    found = true;
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        reader.close();
    }

    /**
     * Reads items array in the JSON object
     * @param reader JsonReader object from {@code readStream(InputStream)}
     * @return Returns a list filled with the book's title and author
     * @throws IOException If an I/O error occurs in {@code JsonReader} methods
     */
    private void readItemsArray(JsonReader reader) throws IOException {

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("volumeInfo")) {
                readVolumeInfo(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    /**
     * Reads volume info within the items array in JSON
     * @param reader JsonReader object from {@code readItemsArray(JsonReader)}
     * @return Returns a list filled with the book's title and author
     * @throws IOException If an I/O error occurs in {@code JsonReader} methods
     */
    private void readVolumeInfo(JsonReader reader) throws IOException {
        reader.beginObject();

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("title") && !titleFilled) {
                title = reader.nextString();
                titleFilled = true;
            } else if (name.equals("subtitle") && !subtitleFilled) {
                String mainTitle = title;
                String subtitle = reader.nextString();
                title = titleConcat(mainTitle, subtitle);
                subtitleFilled  = true;
            } else if (name.equals("authors") && !authorFilled) {
                reader.beginArray();
                while (reader.hasNext()) {
                    if (authors == null) {
                        authors = reader.nextString();
                    } else {
                        String additionalAuthor = reader.nextString();
                        authors = authorConcat(authors, additionalAuthor);
                    }
                }
                reader.endArray();
                authorFilled = true;
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    /**
     * Concatenates main title and subtitle in one string
     * @param title String
     * @param subtitle String
     * @return Concatenated title String
     */
    private String titleConcat(String title, String subtitle) {
        String combinedTitle = title + ": " + subtitle;
        return combinedTitle;
    }

    /**
     * Concatenates two author strings into one string
     * @param concated Previously combined author strings
     * @param current Author string to be concatenated
     * @return Combined author string
     */
    private String authorConcat(String concated, String current) {
        String combinedAuthor = concated + ", " + current;
        return combinedAuthor;
    }
}
