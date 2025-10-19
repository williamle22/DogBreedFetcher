package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public List<String> getSubBreeds(String breed) {
        if (breed == null || breed.isBlank()) {
            throw new BreedNotFoundException(String.valueOf(breed));
        }

        String url = "https://dog.ceo/api/breed/" + breed.toLowerCase() + "/list";
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                throw new BreedNotFoundException(breed);
            }
            String body = response.body().string();

            // API returns JSON like:
            // { "message": ["afghan","basset"], "status": "success" }
            // or on error:
            // { "status":"error", "message":"Breed not found (main breed does not exist)", "code":404 }
            JSONObject json = new JSONObject(body);
            String status = json.optString("status", "error");
            if (!"success".equalsIgnoreCase(status)) {
                throw new BreedNotFoundException(breed);
            }

            JSONArray message = json.getJSONArray("message");
            List<String> result = new ArrayList<>(message.length());
            for (int i = 0; i < message.length(); i++) {
                result.add(message.getString(i));
            }
            return result;
        } catch (IOException e) {
            // Treat IO errors as "not found" per assignment instructions
            throw new BreedNotFoundException(breed);
        }
    }
}