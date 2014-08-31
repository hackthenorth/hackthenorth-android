package com.hackthenorth.android.framework;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.hackthenorth.android.framework.FuzzySearchIndexer.Tokened;

public class FuzzySearchIndexer<T extends Tokened & Comparable<T>> {
    private static final String TAG = "FuzzySearchIndexer";

    public interface Tokened {
        public ArrayList<String> getTokens();
    }

    private HashMap<String, HashSet<T>> mTokensMap;
    private ArrayList<T> mObjects;

    /**
     * Sorts the list passed in the constructor.
     * @param queriesString
     */
    public void query(String queriesString) {

        // Get all the query strings from the query string
        String[] queries;
        if ("".equals(queriesString)) {
            queries = new String[0];
        } else {
            queries = queriesString.split("\\s+");
        }

        String print = "";
        for (String query : queries) {
            print += query + ", ";
        }
        Log.i(TAG, "queries: " + print);
        if (queries.length == 0) {
            // Use the default sorting
            Collections.sort(mObjects);

        } else {

            ArrayList<HashMap<String, Double>> tokenCostsList =
                    new ArrayList<HashMap<String, Double>>(queries.length);

            for (String query : queries) {
                HashMap<String, Double> tokenCosts = getTokenCosts(query);
                tokenCostsList.add(tokenCosts);
            }

            // Rank the objects
            HashMap<T, Double> objectCosts = new HashMap<T, Double>(mObjects.size());
            for (T object : mObjects) {
                objectCosts.put(object, 0d);
            }

            for (Map.Entry<String, HashSet<T>> entry : mTokensMap.entrySet()) {
                for (T object : entry.getValue()) {
                    double cost = objectCosts.get(object);
                    for (HashMap<String, Double> tokenCosts : tokenCostsList) {
                        cost += tokenCosts.get(entry.getKey());
                    }
                    objectCosts.put(object, cost);
                }
            }

            final HashMap<T, Double> costs = objectCosts;
            Collections.sort(mObjects, new Comparator<T>() {
                @Override
                public int compare(T lhs, T rhs) {
                    double ls = costs.get(lhs);
                    double rs = costs.get(rhs);
                    return ls == rs ? 0 : (ls < rs ? 1 : -1);
                }
            });
        }
    }

    public HashMap<String, Double> getTokenCosts(String query) {
        HashMap<String, Double> costs = new HashMap<String, Double>();

        for (String token : mTokensMap.keySet()) {
            costs.put(token, Math.exp(-levenshtein(token, query)));
        }

        return costs;
    }

    public int levenshtein(String src, String target) {

        int[][] table = new int[src.length() + 1][target.length() + 1];

        for (int i = 0; i <= src.length(); i++) {
            table[i][0] = i;
        }

        for (int j = 1; j <= target.length(); j++) {
            table[0][j] = j;
        }

        for (int j = 1; j <= target.length(); j++) {
            for (int i = 1; i <= src.length(); i++) {
                if (src.charAt(i - 1) == target.charAt(j - 1)) {
                    table[i][j] = table[i-1][j-1];
                } else {
                    int del = table[i-1][j] + 1;   // deletion
                    int ins = table[i][j-1] + 1;   // insertion
                    int sub = table[i-1][j-1] + 1; // substitution

                    // use the min of those three operations
                    int smallest = del;
                    if (ins < del) smallest = ins;
                    if (sub < del) smallest = sub;

                    table[i][j] = smallest;
                }
            }
        }

        return table[src.length()][target.length()];
    }

    public void updateData(ArrayList<T> objects) {
        // Build a new map
        mTokensMap = new HashMap<String, HashSet<T>>();

        // Build the tokens map
        for (T object : objects) {
            for (String token : object.getTokens()) {
                token = token.toLowerCase();
                HashSet<T> references = mTokensMap.get(token);
                if (references == null) {
                    references = new HashSet<T>();
                    references.add(object);
                    mTokensMap.put(token, references);
                } else {
                    references.add(object);
                }
            }
        }
    }

    public FuzzySearchIndexer(ArrayList<T> objects) {
        if (objects == null) {
            throw new NullPointerException("List cannot be null");
        }

        mObjects = objects;
        updateData(objects);
    }
}
