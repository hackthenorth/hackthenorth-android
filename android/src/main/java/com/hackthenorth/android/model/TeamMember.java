package com.hackthenorth.android.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class TeamMember extends Model {
    private static final String TAG = "TeamMember";

    public String id;

    // Fields
    public String name;
    public String avatar;
    public ArrayList<String> role;
    public String Phone;
    public String email;
    public String twitter;

    public TeamMember() {
    }

    // This method takes a JSON string and returns an ArrayList of TeamMember from
    // the JSON data.
    public static ArrayList<TeamMember> loadTeamMemberArrayFromJSON(String json) {

        // Deserialize using GSON.
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, TeamMember>>(){}.getType();
        HashMap<String, TeamMember> teamMemberMap = gson.fromJson(json, type);

        // Build an ArrayList of teamMembers from the hashmap.
        ArrayList<TeamMember> teamMembers = new ArrayList<TeamMember>(teamMemberMap.size());
        for (Map.Entry<String, TeamMember> entry : teamMemberMap.entrySet()) {
            TeamMember teamMember = entry.getValue();
            teamMember.id = entry.getKey();
            teamMembers.add(teamMember);
        }

        // Sort the list
        Collections.sort(teamMembers, new Comparator<TeamMember>() {
            @Override
            public int compare(TeamMember lhs, TeamMember rhs) {
                // Sort them in descending order---that's why rhs is on the lhs
                // in this expression.
                return rhs.id.compareTo(lhs.id);
            }
        });

        return teamMembers;
    }
}
