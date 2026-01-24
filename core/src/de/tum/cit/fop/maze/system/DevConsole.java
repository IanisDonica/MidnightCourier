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

public class DevConsole {
    private final Table root;
    private final TextField inputField;
    private final Label outputLabel;
    private final ScrollPane scrollPane;
    private final List<String> outputLines = new ArrayList<>();
    private boolean visible = false;
    private de.tum.cit.fop.maze.entity.Player player;
    private boolean ignoreNextTyped = false;

    public DevConsole(MazeRunnerGame game) {
        // TODO add more commands, give items, set health, change level, open shop, spawn, god mode, no clip etc
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

    public void setPlayer(de.tum.cit.fop.maze.entity.Player player) {
        this.player = player;
    }

    public void addToStage(Stage stage) {
        stage.addActor(root);
    }

    public void toggle(Stage stage) {
        if (visible) {
            hide(stage);
        } else {
            show(stage);
        }
    }

    public void show(Stage stage) {
        visible = true;
        root.setVisible(true);
        inputField.setText("");
        ignoreNextTyped = true;
        stage.setKeyboardFocus(inputField);
    }

    public void hide(Stage stage) {
        visible = false;
        root.setVisible(false);
        stage.setKeyboardFocus(null);
    }

    public boolean isVisible() {
        return visible;
    }

    private void execute(String raw) {
        String command = raw == null ? "" : raw.trim();
        if (command.isEmpty()) {
            return;
        }
        appendLine("> " + command);
        String[] parts = command.split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "help" -> appendLine("Commands: help, tp <x> <y>, speed <multiplier>");
            case "tp" -> {
                if (parts.length < 3) {
                    appendLine("Usage: tp <x> <y>");
                } else {
                    try {
                        float x = Float.parseFloat(parts[1]), y = Float.parseFloat(parts[2]);
                        player.setPosition(x, y);
                        appendLine("tped to" + x + ", " + y);
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
                        player.setDebugSpeedMultiplier(multiplier);
                        appendLine("multiplier set to" + multiplier);
                    } catch (NumberFormatException ex) {
                        appendLine("Invalid command.");
                    }
                }
            }
            default -> appendLine("Unknown command. Try: help");
        }
        scrollPane.layout();
        scrollPane.setScrollPercentY(1f);
    }

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
}
