package com.example.projectapplication;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView upcomingGameDate, upcomingGameTeamOpponent;
    TextView recentGameOpponent, recentGameGiantsScore, recentGameOpponentScore;
    TableLayout battingTable, pitchingTable;

    Context context;

    FloatingActionButton fabRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        upcomingGameDate = (TextView) findViewById(R.id.upcoming_date);
        upcomingGameTeamOpponent = (TextView) findViewById(R.id.upcoming_team_opponent);

        pitchingTable = (TableLayout) findViewById(R.id.table_giantsPitching);

        fabRefresh = (FloatingActionButton) findViewById(R.id.fab_refresh);
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFabRefresh(view);
                jsonParse(dailyGamesURL());
            }
        });
    }

    // Get yesterday's date to retrieve most recent completed game
    // But...what if most recent game is today??
    public static String getDate() {

        // If I call yesterday's date, will the api send me the most recent game info?
        // Even if no game was played on that specific date.

        // Get yesterday's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);

        return dateFormat.format(calendar.getTime());

    }

    // Get the url type with the correct date inserted
    public String dailyGamesURL() {

        String getData = "https://api.mysportsfeeds.com/v2.1/pull/mlb/2019-regular/date//games.json?team=sf";

        // gameData = check yesterday's date, insert into url string:
        StringBuilder gameData = new StringBuilder();
        gameData.append(getData);
        gameData.insert(62, getDate());

        //return correct (date) url
        return gameData.toString();

    }

    private void jsonParse(String getUrl) {

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                getUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        // Where the magic happens
                        try {

                            setRecentGameScore(response);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley: ", "Error");
                    }
                }
        ) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = getString(R.string.api_credentials);
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(),Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);

    }

    private void setRecentGameScore(JSONObject response) throws JSONException {

        recentGameGiantsScore = findViewById(R.id.recentGame_giantsScore);
        recentGameOpponent = findViewById(R.id.recentGame_opponent);
        recentGameOpponentScore = findViewById(R.id.recentGame_opponentScore);

        String aTeam = "";
        String hTeam = "";

        JSONObject data = response.getJSONArray("games").getJSONObject(0);

        // find the awayScoreTotal
        Object awayScore = data.getJSONObject("score").get("awayScoreTotal");

        // find the homeScoreTotal
        Object homeScore = data.getJSONObject("score").get("homeScoreTotal");

        JSONObject isAway = data.getJSONObject("schedule").getJSONObject("awayTeam");

        // Determine if the Giants were 'away' or 'home'
        if (isAway.get("id").equals(136)) {
            recentGameGiantsScore.setText(awayScore.toString());
            recentGameOpponentScore.setText(homeScore.toString());
            recentGameOpponent.setText(data.getJSONObject("schedule").getJSONObject("homeTeam").get("abbreviation").toString());
        } else {
            recentGameGiantsScore.setText(homeScore.toString());
            recentGameOpponentScore.setText(awayScore.toString());
            recentGameOpponent.setText(data.getJSONObject("schedule").getJSONObject("awayTeam").get("abbreviation").toString());
        }

        hTeam = data.getJSONObject("schedule").getJSONObject("homeTeam").get("abbreviation").toString();
        aTeam = data.getJSONObject("schedule").getJSONObject("awayTeam").get("abbreviation").toString();

        createBattingTableRow(hTeam, aTeam);

    }

    public void getUpcomingGame(String url) {

        // Get the date/time/opponentName for next game

    }

    // The boxScore url type can only be called if I know the two teams who played, which comes
    // from the dailyGames url
    public String boxScoreURL(String hTeam, String aTeam) {

        String boxURL = "https://api.mysportsfeeds.com/v2.1/pull/mlb/2019-regular/games//boxscore.json?team=sf";

        // creates: -AT-HT
        StringBuilder boxscoreURL = new StringBuilder(getDate() + "-" + aTeam + "-" + hTeam);

        // take boxURL -> insert the above string + returned date (getDate()) into the correct url
        StringBuilder url = new StringBuilder(boxURL);
        url.insert(63, boxscoreURL);

        return url.toString();

    }

    // MainActivity is doing too much.
    // Get both Tables into their own fragments/other activities or something

    public void createBattingTableRow(final String homeTeam, final String awayTeam) throws JSONException {

        battingTable = (TableLayout) findViewById(R.id.table_giantsBatting);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                boxScoreURL(homeTeam, awayTeam),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        // Where the magic happens
                        try {

                            JSONObject stats = response.getJSONObject("stats");
                            JSONArray homePlayer = stats.getJSONObject("home").getJSONArray("players");
                            JSONArray awayPlayer = stats.getJSONObject("away").getJSONArray("players");

                            if (homeTeam.equals("SF")) {
                                for (int i = 0; i < homePlayer.length()+1; i++) {

                                    JSONObject pStats = homePlayer.getJSONObject(i+1).getJSONArray("playerStats").getJSONObject(0).getJSONObject("batting");

                                    Object lName = homePlayer.getJSONObject(i+1).getJSONObject("player").get("lastName");

                                    int ab = pStats.getInt("atBats");
                                    int run = pStats.getInt("runs");
                                    int h = pStats.getInt("hits");
                                    int b2 = pStats.getInt("secondBaseHits");
                                    int b3 = pStats.getInt("thirdBaseHits");
                                    int hr = pStats.getInt("homeruns");
                                    int rbis = pStats.getInt("runsBattedIn");
                                    int bb = pStats.getInt("batterWalks");
                                    int strikeouts = pStats.getInt("batterStrikeouts");

                                    if (ab > 0) {

                                        // add stats to the row as it's being added to the table
                                        TableRow row = new TableRow(context);
                                        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                                        row.setLayoutParams(layoutParams);

                                        TextView lastName = new TextView(context);
                                        lastName.setText(lName.toString());
                                        row.addView(lastName, 0);

                                        TextView atBats = new TextView(context);
                                        atBats.setText(String.valueOf(ab));
                                        row.addView(atBats, 1);

                                        TextView hits = new TextView(context);
                                        hits.setText(String.valueOf(h));
                                        row.addView(hits, 2);

                                        TextView homeruns = new TextView(context);
                                        homeruns.setText(String.valueOf(hr));
                                        row.addView(homeruns, 3);

                                        TextView rbi = new TextView(context);
                                        rbi.setText(String.valueOf(rbis));
                                        row.addView(rbi, 4);

                                        TextView runs = new TextView(context);
                                        runs.setText(String.valueOf(run));
                                        row.addView(runs, 5);

                                        TextView doubles = new TextView(context);
                                        doubles.setText(String.valueOf(b2));
                                        row.addView(doubles, 6);

                                        TextView triples = new TextView(context);
                                        triples.setText(String.valueOf(b3));
                                        row.addView(triples, 7);

                                        TextView strikeout = new TextView(context);
                                        strikeout.setText(String.valueOf(strikeouts));
                                        row.addView(strikeout, 8);

                                        TextView walks = new TextView(context);
                                        walks.setText(String.valueOf(bb));
                                        row.addView(walks, 9);

                                        battingTable.addView(row);

                                    }

                                    if (homePlayer.getJSONObject(i+1).getJSONObject("player").get("position").equals("P")) {

                                        JSONObject pitchingStats = homePlayer.getJSONObject(i+1).getJSONArray("playerStats").getJSONObject(0).getJSONObject("pitching");

                                        // int wL = figure out the w/l stat
                                        int wL = 0;
                                        int ip = pitchingStats.getInt("inningsPitched");
                                        int hitsAllowed = pitchingStats.getInt("hitsAllowed");
                                        int er = pitchingStats.getInt("earnedRunsAllowed");
                                        int walks = pitchingStats.getInt("pitcherWalks");
                                        int so = pitchingStats.getInt("pitcherStrikeouts");
                                        int totalPitches = pitchingStats.getInt("pitchesThrown");

                                        createPitchingTableRow(lName, wL, ip, er, hitsAllowed, so, walks, totalPitches);

                                    }
                                }

                            } else {
                                // search 'away' stats
                                for (int i = 0; i < awayPlayer.length()+1; i++) {

                                    JSONObject pStats = awayPlayer.getJSONObject(i+1).getJSONArray("playerStats").getJSONObject(0).getJSONObject("batting");

                                    Object lName = awayPlayer.getJSONObject(i+1).getJSONObject("player").get("lastName");

                                    int ab = pStats.getInt("atBats");
                                    int run = pStats.getInt("runs");
                                    int h = pStats.getInt("hits");
                                    int b2 = pStats.getInt("secondBaseHits");
                                    int b3 = pStats.getInt("thirdBaseHits");
                                    int hr = pStats.getInt("homeruns");
                                    int rbis = pStats.getInt("runsBattedIn");
                                    int bb = pStats.getInt("batterWalks");
                                    int strikeouts = pStats.getInt("batterStrikeouts");

                                    if (ab > 0) {

                                        // add stats to the row as it's being added to the table
                                        TableRow row = new TableRow(context);
                                        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                                        row.setLayoutParams(layoutParams);

                                        TextView lastName = new TextView(context);
                                        lastName.setText(lName.toString());
                                        row.addView(lastName, 0);

                                        TextView atBats = new TextView(context);
                                        atBats.setText(String.valueOf(ab));
                                        row.addView(atBats, 1);

                                        TextView hits = new TextView(context);
                                        hits.setText(String.valueOf(h));
                                        row.addView(hits, 2);

                                        TextView homeruns = new TextView(context);
                                        homeruns.setText(String.valueOf(hr));
                                        row.addView(homeruns, 3);

                                        TextView rbi = new TextView(context);
                                        rbi.setText(String.valueOf(rbis));
                                        row.addView(rbi, 4);

                                        TextView runs = new TextView(context);
                                        runs.setText(String.valueOf(run));
                                        row.addView(runs, 5);

                                        TextView doubles = new TextView(context);
                                        doubles.setText(String.valueOf(b2));
                                        row.addView(doubles, 6);

                                        TextView triples = new TextView(context);
                                        triples.setText(String.valueOf(b3));
                                        row.addView(triples, 7);

                                        TextView strikeout = new TextView(context);
                                        strikeout.setText(String.valueOf(strikeouts));
                                        row.addView(strikeout, 8);

                                        TextView walks = new TextView(context);
                                        walks.setText(String.valueOf(bb));
                                        row.addView(walks, 9);

                                        battingTable.addView(row);

                                    }

                                    if (awayPlayer.getJSONObject(i+1).getJSONObject("player").get("position").equals("P")) {

                                        JSONObject pitchingStats = awayPlayer.getJSONObject(i+1).getJSONArray("playerStats").getJSONObject(0).getJSONObject("pitching");

                                        // int wL = figure out the w/l stat
                                        int wL = 0;
                                        int ip = pitchingStats.getInt("inningsPitched");
                                        int hitsAllowed = pitchingStats.getInt("hitsAllowed");
                                        int er = pitchingStats.getInt("earnedRunsAllowed");
                                        int walks = pitchingStats.getInt("pitcherWalks");
                                        int so = pitchingStats.getInt("pitcherStrikeouts");
                                        int totalPitches = pitchingStats.getInt("pitchesThrown");

                                        createPitchingTableRow(lName, wL, ip, er, hitsAllowed, so, walks, totalPitches);
                                    }

                                }
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley: ", "Error");
                    }
                }
        ) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = getString(R.string.api_credentials);
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(),Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);

    }

    public void createPitchingTableRow(Object lName, int wL, int ip, int er, int hitsAllowed, int so, int walks, int totalPitches) {

        TableRow row = new TableRow(context);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(layoutParams);

        TextView lastName = new TextView(context);
        lastName.setText(lName.toString());
        row.addView(lastName, 0);

        TextView winLoss = new TextView(context);
        winLoss.setText(String.valueOf(wL));
        row.addView(winLoss, 1);

        TextView inningsP = new TextView(context);
        inningsP.setText(String.valueOf(ip));
        row.addView(inningsP, 2);

        TextView earnedRuns = new TextView(context);
        earnedRuns.setText(String.valueOf(er));
        row.addView(earnedRuns, 3);

        TextView hits = new TextView(context);
        hits.setText(String.valueOf(hitsAllowed));
        row.addView(hits, 4);

        TextView strikeouts = new TextView(context);
        strikeouts.setText(String.valueOf(so));
        row.addView(strikeouts, 5);

        TextView bb = new TextView(context);
        bb.setText(String.valueOf(walks));
        row.addView(bb, 6);

        TextView pitches = new TextView(context);
        pitches.setText(String.valueOf(totalPitches));

        pitchingTable.addView(row);

    }

    public void setFabRefresh(View v) {
        // Refresh button
    }

}
