package dogapi;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches successful results from an underlying BreedFetcher.
 * Failed lookups (BreedNotFoundException) are NOT cached.
 */
public class CachingBreedFetcher implements BreedFetcher {
    private final BreedFetcher delegate;
    private final Map<String, List<String>> cache = new HashMap<>();
    private int callsMade = 0;

    public CachingBreedFetcher(BreedFetcher fetcher) {
        this.delegate = fetcher;
    }

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        String key = breed == null ? "null" : breed.toLowerCase();

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        callsMade++;
        try {
            List<String> result = delegate.getSubBreeds(breed);
            // Store an unmodifiable copy to prevent accidental mutation.
            List<String> frozen = Collections.unmodifiableList(result);
            cache.put(key, frozen);
            return frozen;
        } catch (BreedNotFoundException e) {
            // Do NOT cache failures.
            throw e;
        }
    }

    public int getCallsMade() {
        return callsMade;
    }
}