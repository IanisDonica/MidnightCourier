package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PointManager implements Serializable {
    private static final String FILE_PATH = "assets/data/highscore.json";
    private static final String ENDPOINT = "https://webservertransprut-production.up.railway.app/api/scores/";
    private int points = 0;
    private int timePoints = 100000;
    private float offset = 0;
    private float safetyTime = 5f; // the amount of time for which points won't go down
    private float elapsedTime = 0f;
    private boolean requestSent = false;

    public int getPoints() {
        return points + timePoints;
    }

    public int getTimePoints() {
        return timePoints;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setTimePoints(int timePoints) {
        this.timePoints = timePoints;
    }

    public void add(int amount) {
        if (amount < 0) throw new IllegalArgumentException("points need to be non-negative");
        points += amount;
    }

    /*
    At the start they decrease quickly, later on they decrease slower, this is to make it a
     challenge for good players to get a lot of points, yet at the same allow worse players
    to at least get some points.
    */
    public void decreasePoints() {
        int deduct = (int) (400 * Math.ceil(Math.log10(timePoints)));
        timePoints = Math.max(timePoints - deduct, 0);
    }

    public void act(float delta) {
        // This might cause the safety time to be slightly bigger than the amount specified,
        // but since it's going to be a very, very small difference, it's easier to leave it as such
        elapsedTime += delta;
        if (safetyTime > 0) {
            safetyTime -= delta;
        } else {
            offset += delta;
        }
        if (offset > 3) {
            decreasePoints();
            offset = offset % 3;
        }
    }

    public void saveScore(int playerHp) {
        //THis is just to make the JSON handaling easier
        ScoreRecord record = new ScoreRecord(getPoints(), elapsedTime, playerHp);

        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        // Get the file (we assume it will always be there)
        FileHandle file = Gdx.files.local(FILE_PATH);
        JsonReader reader = new JsonReader();
        JsonValue rootArray = reader.parse(file.readString("UTF-8"));


        //Add a new record to it and write it back
        String recordJson = json.toJson(record);
        JsonValue recordValue = new JsonReader().parse(recordJson);
        rootArray.addChild(recordValue);
        String payloadForFile = rootArray.toJson(JsonWriter.OutputType.json);
        file.writeString(payloadForFile, false, "UTF-8");

        // Send one record to the server as a fire and forget the HTTP request
        if (!requestSent) {
            requestSent = true;
            Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
            request.setUrl(ENDPOINT);
            request.setHeader("Content-Type", "application/json");
            request.setContent(recordJson);

            Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
                //Even tho it's a fire and forget request, it's still required to have a
                // listener and override the methods. I just left them blank
                @Override
                public void handleHttpResponse(Net.HttpResponse httpResponse) {}

                @Override
                public void failed(Throwable t) {}

                @Override
                public void cancelled() {}
            });
        }
    }

    private static final class ScoreRecord {
        public double score;
        public float time;
        public int playerHp;
        public String dateTime;

        public ScoreRecord(double score, float time, int playerHp) {
            this.score = score;
            this.time = time;
            this.playerHp = playerHp;
            // This is only for the local leaderboard; I'll make the just take the request time
            this.dateTime = LocalDateTime.now().toString();
        }
    }
}
