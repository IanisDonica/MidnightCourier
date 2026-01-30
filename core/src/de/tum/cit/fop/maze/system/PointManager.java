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

/**
 * Manages points, timers, and score persistence/submit.
 */
public class PointManager implements Serializable {
    /** Local highscore file path. */
    private static final String FILE_PATH = "assets/data/highscore.json";
    /** Endpoint for remote score submission. */
    private static final String ENDPOINT = "https://webservertransprut-production.up.railway.app/api/scores/";
    /** Base points earned. */
    private int points = 100;
    /** Time-based points for campaign. */
    private int timePoints = 100000;
    /** Time-based points for survival. */
    private int timePointsSurvival = 0;
    /** Accumulator for point decay ticks. */
    private float offset = 0;
    /** Grace period before points start decreasing. */
    private float safetyTime = 5f; // the amount of time for which points won't go down
    /** Elapsed time since start. */
    private float elapsedTime = 0f;
    /** Whether the score submission request was sent. */
    private boolean requestSent = false;
    /** Level number for this manager. */
    private final int level;

    /**
     * Returns total points for the current mode.
     *
     * @return total points
     */
    public int getPoints() {
        if(level != 0) return points + timePoints;
        return points + timePointsSurvival;
    }

    /**
     * Returns the level number.
     *
     * @return level number
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns time-based points.
     *
     * @return time points
     */
    public int getTimePoints() {
        return timePoints;
    }

    /**
     * Sets base points.
     *
     * @param points new points value
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * Sets time-based points.
     *
     * @param timePoints new time points value
     */
    public void setTimePoints(int timePoints) {
        this.timePoints = timePoints;
    }

    /**
     * Adds points to the base score.
     *
     * @param amount non-negative amount to add
     */
    public void add(int amount) {
        if (amount < 0) throw new IllegalArgumentException("points need to be non-negative");
        points += amount;
    }

    /**
     * Creates a point manager for the given level.
     *
     * @param level level number
     */
    public PointManager(int level) {
        this.level = level;
    }

    /*
    At the start they decrease quickly, later on they decrease slower, this is to make it a
     challenge for good players to get a lot of points, yet at the same allow worse players
    to at least get some points.
    */
    /**
     * Decreases time points and increases survival time points.
     */
    public void decreasePoints() {
        int deduct = (int) (400 * Math.ceil(Math.log10(timePoints)));
        timePoints = Math.max(timePoints - deduct, 0);
        int adder = (int) (400 * Math.ceil(Math.log10(timePoints)));
        timePointsSurvival = Math.max(timePointsSurvival + adder, 0);
    }

    /**
     * Increases survival points based on time points.
     */
    public void increasePoints() {
        int adder = (int) (400 * Math.ceil(Math.log10(timePoints)));
        timePointsSurvival = Math.max(timePoints - adder, 0);
    }

    /**
     * Advances timers and applies time-based point changes.
     *
     * @param delta time step in seconds
     */
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

    /**
     * Returns elapsed time in seconds.
     *
     * @return elapsed time
     */
    public float getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Saves the score locally and submits it to the server once.
     *
     * @param playerHp player HP to include in the record
     */
    public void saveScore(int playerHp) {
        //THis is just to make the JSON handaling easier
        ScoreRecord record = new ScoreRecord(getPoints(), elapsedTime, playerHp, level);

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

    /**
     * Serializable score record sent to the server.
     */
    private static final class ScoreRecord {
        /** Total score. */
        public double score;
        /** Time played. */
        public float time;
        /** Player HP at end. */
        public int playerHp;
        /** Timestamp of the record. */
        public String dateTime;
        /** Level number. */
        public int level;

        /**
         * Creates a score record.
         *
         * @param score total score
         * @param time elapsed time
         * @param playerHp player HP at end
         * @param level level number
         */
        public ScoreRecord(double score, float time, int playerHp, int level) {
            this.score = score;
            this.time = time;
            this.playerHp = playerHp;
            this.level = level;
            this.dateTime = LocalDateTime.now().toString();
        }
    }
}
