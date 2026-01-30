package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import de.tum.cit.fop.maze.MazeRunnerGame;

import java.util.ArrayList;
import java.util.List;

/**
 * In-game developer console for debugging and spawning entities.
 */
public class DevConsole {
    /** Root table containing console UI elements. */
    private final Table root;
    /** Input field for console commands. */
    private final TextField inputField;
    /** Output label showing command history. */
    private final Label outputLabel;
    /** Scroll container for output text. */
    private final ScrollPane scrollPane;
    /** Lines of output displayed in the console. */
    private final List<String> outputLines = new ArrayList<>();
    /** Game instance for navigation and managers. */
    private final MazeRunnerGame game;
    /** Whether the console is currently visible. */
    private boolean visible = false;
    /** Player instance for commands that affect the player. */
    private de.tum.cit.fop.maze.entity.Player player;
    /** Collision layer used for enemy spawning. */
    private com.badlogic.gdx.maps.tiled.TiledMapTileLayer collisionLayer;
    /** Road layer used for BMW enemy spawning. */
    private com.badlogic.gdx.maps.tiled.TiledMapTileLayer roadLayer;
    /** Whether the next typed character should be ignored. */
    private boolean ignoreNextTyped = false;

    /**
     * Creates a new developer console.
     *
     * @param game game instance used for UI skin and navigation
     */
    public DevConsole(MazeRunnerGame game) {
        this.game = game;
        root = new Table();
        root.setFillParent(true);
        root.setVisible(false);

        outputLabel = new Label("", game.getSkin());
        outputLabel.setWrap(true);

        scrollPane = new ScrollPane(outputLabel, game.getSkin());
        scrollPane.setFadeScrollBars(false);

        inputField = new TextField("", game.getSkin());
        inputField.setMessageText("Type a command...");
        inputField.setTextFieldListener((textField, c) -> {
            if (c == '\n' || c == '\r') {
                execute(textField.getText());
                textField.setText("");
            }
        });
        inputField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.GRAVE) {
                    toggle(event.getStage());
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyTyped(InputEvent event, char character) {
                if (ignoreNextTyped) {
                    ignoreNextTyped = false;
                    inputField.setText("");
                    return true;
                }
                return character == '`' || character == '~';
            }
        });

        root.addListener(new InputListener() {
            @Override
            public boolean keyTyped(InputEvent event, char character) {
                if (ignoreNextTyped) {
                    ignoreNextTyped = false;
                    inputField.setText("");
                    return true;
                }
                return character == '`' || character == '~';
            }
        });

        root.top().left().pad(20);
        root.add(scrollPane).width(800).height(300).row();
        root.add(inputField).width(800).padTop(10);
    }

    /**
     * Sets the player used for player-related commands.
     *
     * @param player player instance
     */
    public void setPlayer(de.tum.cit.fop.maze.entity.Player player) {
        this.player = player;
    }

    /**
     * Sets layers used for spawning entities.
     *
     * @param collisionLayer collision layer for enemy spawns
     * @param roadLayer road layer for BMW spawns
     */
    public void setSpawnLayers(com.badlogic.gdx.maps.tiled.TiledMapTileLayer collisionLayer,
                               com.badlogic.gdx.maps.tiled.TiledMapTileLayer roadLayer) {
        this.collisionLayer = collisionLayer;
        this.roadLayer = roadLayer;
    }

    /**
     * Adds the console UI to the stage.
     *
     * @param stage stage to attach to
     */
    public void addToStage(Stage stage) {
        stage.addActor(root);
    }

    /**
     * Toggles console visibility.
     *
     * @param stage stage to manage focus on
     */
    public void toggle(Stage stage) {
        if (visible) {
            hide(stage);
        } else {
            show(stage);
        }
    }

    /**
     * Shows the console and focuses the input field.
     *
     * @param stage stage to manage focus on
     */
    public void show(Stage stage) {
        visible = true;
        root.setVisible(true);
        inputField.setText("");
        ignoreNextTyped = true;
        stage.setKeyboardFocus(inputField);
    }

    /**
     * Hides the console and clears focus.
     *
     * @param stage stage to manage focus on
     */
    public void hide(Stage stage) {
        visible = false;
        root.setVisible(false);
        stage.setKeyboardFocus(null);
    }

    /**
     * Returns whether the console is currently visible.
     *
     * @return {@code true} if visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Executes a console command string.
     *
     * @param raw raw command input
     */
    private void execute(String raw) {
        String command = raw == null ? "" : raw.trim();
        if (command.isEmpty()) {
            return;
        }
        appendLine("> " + command);
        String[] parts = command.split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "help" -> appendLine("Commands: help, tp <x> <y>, speed <multiplier>, sethp <hp>, setmaxhp <hp>, setcredits <points>, openshop, godmode [on|off], giveenergydrink, givekey, spawn <enemy|trap|bmwdriver> <x> <y>, spawnbmws <amount>, spawnenemies <amount>, whereami");
            case "tp" -> {
                if (parts.length < 3) {
                    appendLine("Usage: tp <x> <y>");
                } else {
                    try {
                        float x = Float.parseFloat(parts[1]), y = Float.parseFloat(parts[2]);
                        player.setPosition(x, y);
                        appendLine("tped to " + x + ", " + y);
                    } catch (NumberFormatException ex) {
                        appendLine("Invalid command.");
                    }
                }
            }
            case "speed" -> {
                if (parts.length < 2) {
                    appendLine("Usage: speed <multiplier>");
                } else {
                    try {
                        float multiplier = Float.parseFloat(parts[1]);
                        player.setSpeedMultiplier(multiplier);
                        appendLine("multiplier set to " + multiplier);
                    } catch (NumberFormatException ex) {
                        appendLine("Invalid command.");
                    }
                }
            }
            case "sethp" -> {
                if (parts.length < 2) {
                    appendLine("Usage: sethp <hp>");
                } else {
                    try {
                        int hp = Integer.parseInt(parts[1]);
                        player.setHp(hp);
                        appendLine("hp set to " + player.getHp());
                    } catch (NumberFormatException ex) {
                        appendLine("Invalid command.");
                    }
                }
            }
            case "setmaxhp" -> {
                if (parts.length < 2) {
                    appendLine("Usage: setmaxhp <hp>");
                } else {
                    try {
                        int hp = Integer.parseInt(parts[1]);
                        player.setMaxHp(hp);
                        player.setHp(player.getHp());
                        appendLine("max hp set to " + player.getMaxHp());
                    } catch (NumberFormatException ex) {
                        appendLine("Invalid command.");
                    }
                }
            }
            case "setcredits" -> {
                if (parts.length < 2) {
                    appendLine("Usage: setcredits <points>");
                } else {
                    try {
                        int points = Integer.parseInt(parts[1]);
                        game.getProgressionManager().setPoints(points);
                        appendLine("credits set to " + game.getProgressionManager().getPoints());
                    } catch (NumberFormatException ex) {
                        appendLine("Invalid command.");
                    }
                }
            }
            case "openshop" -> {
                game.goToProgressionTreeScreenFromGame();
                appendLine("opened shop");
            }
            case "godmode" -> {
                if (parts.length < 2) {
                    player.setGodMode(!player.isGodMode());
                } else {
                    String arg = parts[1].toLowerCase();
                    player.setGodMode(arg.equals("on") || arg.equals("true") || arg.equals("1"));
                }
                appendLine("godmode " + (player.isGodMode() ? "on" : "off"));
            }
            case "giveenergydrink" -> {
                player.drinkEnergyDrink();
                appendLine("energy drink applied");
            }
            case "givekey" -> {
                player.pickupKey();
                appendLine("key granted");
            }
            case "spawn" -> {
                if (parts.length < 4) {
                    appendLine("Usage: spawn <enemy|trap|bmwdriver> <x> <y>");
                } else {
                    String type = parts[1].toLowerCase();
                    try {
                        float x = Float.parseFloat(parts[2]);
                        float y = Float.parseFloat(parts[3]);
                        spawnEntity(type, x, y);
                    } catch (NumberFormatException ex) {
                        appendLine("Invalid command.");
                    }
                }
            }
            case "spawnbmws" -> {
                if (parts.length < 2) {
                    appendLine("Usage: spawnbmws <amount>");
                } else {
                    try {
                        int amount = Integer.parseInt(parts[1]);
                        spawnBmws(amount);
                    } catch (NumberFormatException ex) {
                        appendLine("Invalid command.");
                    }
                }
            }
            case "spawnenemies" -> {
                if (parts.length < 2) {
                    appendLine("Usage: spawnenemies <amount>");
                } else {
                    try {
                        int amount = Integer.parseInt(parts[1]);
                        spawnEnemies(amount);
                    } catch (NumberFormatException ex) {
                        appendLine("Invalid command.");
                    }
                }
            }
            case "whereami" -> appendLine("player at " + player.getX() + ", " + player.getY());
            default -> appendLine("Unknown command. Try: help");
        }
        scrollPane.layout();
        scrollPane.setScrollPercentY(1f);
    }

    /**
     * Appends a line to the console output.
     *
     * @param line text to append
     */
    private void appendLine(String line) {
        outputLines.add(line);
        if (outputLines.size() > 100) {
            outputLines.remove(0);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < outputLines.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(outputLines.get(i));
        }
        outputLabel.setText(sb.toString());
    }

    /**
     * Spawns an entity of the given type at the provided coordinates.
     *
     * @param type entity type string
     * @param x world x position
     * @param y world y position
     */
    private void spawnEntity(String type, float x, float y) {
        if (player == null || player.getStage() == null) {
            appendLine("No stage available.");
            return;
        }
        switch (type) {
            case "enemy" -> {
                if (collisionLayer == null) {
                    appendLine("Missing collision layer.");
                    return;
                }
                de.tum.cit.fop.maze.entity.obstacle.Enemy enemy =
                        new de.tum.cit.fop.maze.entity.obstacle.Enemy(collisionLayer, x, y);
                player.getStage().addActor(enemy);
                appendLine("spawned enemy at " + x + ", " + y);
            }
            case "trap" -> {
                de.tum.cit.fop.maze.entity.obstacle.Trap trap =
                        new de.tum.cit.fop.maze.entity.obstacle.Trap(x, y);
                player.getStage().addActor(trap);
                appendLine("spawned trap at " + x + ", " + y);
            }
            case "bmwdriver", "bmw" -> {
                if (roadLayer == null) {
                    appendLine("Missing road layer.");
                    return;
                }
                de.tum.cit.fop.maze.entity.obstacle.BmwEnemy bmw =
                        new de.tum.cit.fop.maze.entity.obstacle.BmwEnemy(roadLayer, x, y);
                player.getStage().addActor(bmw);
                appendLine("spawned bmwdriver at " + x + ", " + y);
            }
            default -> appendLine("Unknown spawn type.");
        }
    }

    /**
     * Spawns a number of BMW enemies at random road positions.
     *
     * @param amount number of BMW enemies to spawn
     */
    private void spawnBmws(int amount) {
        if (player == null || player.getStage() == null) {
            appendLine("No stage available.");
            return;
        }
        if (roadLayer == null) {
            appendLine("Missing road layer.");
            return;
        }
        de.tum.cit.fop.maze.entity.obstacle.BmwEnemy.spawnRandomBmws(player, player.getStage(), amount);
        appendLine("spawned " + amount + " bmws");
    }

    /**
     * Spawns a number of standard enemies at random positions.
     *
     * @param amount number of enemies to spawn
     */
    private void spawnEnemies(int amount) {
        if (player == null || player.getStage() == null) {
            appendLine("No stage available.");
            return;
        }
        if (collisionLayer == null) {
            appendLine("Missing collision layer.");
            return;
        }
        de.tum.cit.fop.maze.entity.obstacle.Enemy.spawnRandomEnemies(player, player.getStage(), collisionLayer, amount);
        appendLine("spawned " + amount + " enemies");
    }
}
