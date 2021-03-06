package com.umn.cacheserver.policy;

import com.umn.cacheserver.model.Cache;
import com.umn.cacheserver.model.CacheEntry;
import com.umn.cacheserver.model.DatasetEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.*;

public class LearnedEviction extends EvictionPolicy {

    private RestTemplate restTemplate;
    private String flaskURL;

    public LearnedEviction(RestTemplate restTemplate, String flaskURL) {
        this.restTemplate = restTemplate;
        this.flaskURL = flaskURL;
    }

    /**
     * Returns the index
     */
    @Override
    public int evict() {
        List<Integer> cacheValues = new ArrayList<>();
        List<Integer> freqList = new ArrayList<>();
        Timestamp timeArray[] = new Timestamp[Cache.cacheSize];
        int index = 0;
        for (CacheEntry entry : Cache.cache) {
            timeArray[index] = entry.getTimestamp();
            freqList.add(entry.getFrequency());
            cacheValues.add(Integer.parseInt(entry.getValue()));
            index++;
        }
        DatasetEntry cacheSnapshot = new DatasetEntry(cacheValues, freqList, rankArr(timeArray));
        String url = String.format("http://%s/predict/eviction", flaskURL);

        HttpEntity<DatasetEntry> request = new HttpEntity<>(cacheSnapshot);
        String ind = restTemplate.postForObject(url, request, String.class);
        return Integer.parseInt(ind);
    }


    private List<Integer> rankArr(Timestamp[] input) {
        // Copy input array into newArray
        Timestamp[] newArray = Arrays.copyOfRange(input, 0, input.length);

        // Sort newArray[] in ascending order
        Arrays.sort(newArray);
        int i;

        // Map to store the rank of the array element
        Map<Timestamp, Integer> ranks = new HashMap<>();

        int rank = 1;

        for (Timestamp element : newArray) {

            // Update rank of element
            if (ranks.get(element) == null) {
                ranks.put(element, rank);
                rank++;
            }
        }

        // Assign ranks to elements
        List<Integer> rankedInput = new ArrayList<>();
        for (Timestamp element : input) {
            rankedInput.add(ranks.get(element));
        }

        return rankedInput;
    }
}
