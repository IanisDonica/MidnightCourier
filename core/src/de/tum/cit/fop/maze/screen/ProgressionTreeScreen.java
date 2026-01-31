package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.system.AudioManager;
import de.tum.cit.fop.maze.system.ProgressionManager;
import de.tum.cit.fop.maze.system.progression.Upgrade;

import java.util.HashMap;
import java.util.Map;

/**
 * Screen that displays the progression upgrade tree.
 */
public class ProgressionTreeScreen implements Screen {
    /** Parent-child upgrade connections. */
    private static final String[][] CONNECTIONS = {
            {"root", "speed"},
            {"root", "health"},
            {"root", "stealth"},
            {"speed", "speed_2"},
            {"speed_2", "speed_3"},
            {"speed_2", "drink_speed_1"},
            {"drink_speed_1", "drink_speed_2"},
            {"health", "health_2"},
            {"health_2", "health_3"},
            {"health_2", "regen"},
            {"speed_3", "master"},
            {"health_3", "master"},
            {"new_glasses", "pothol_imunity"},
            {"stealth", "pothol_imunity"}
    };

    /** Game instance for navigation and resources. */
    private final MazeRunnerGame game;
    /** Stage hosting UI elements. */
    private final Stage stage;
    /** Root table for layout. */
    private final Table table;
    /** Back button to return to previous screen. */
    private final TextButton backButton;
    /** Renderer for drawing connection lines. */
    private final ShapeRenderer shapeRenderer;
    /** Background texture. */
    private final Texture backgroundTexture;
    /** Background image. */
    private final Image backgroundImage;
    /** Mega logo texture. */
    private final Texture megaTexture;
    /** Mega logo image. */
    private final Image megaImage;
    /** Mapping of upgrade names to buttons. */
    private final Map<String, TextButton> buttonsByName = new HashMap<>();
    /** Audio manager for UI sounds. */
    private final AudioManager audioManager;

    /**
     * Creates the progression tree screen.
     *
     * @param game game instance
     */
    public ProgressionTreeScreen(MazeRunnerGame game) {
        this.game = game;
        audioManager = game.getAudioManager();
        var graphicsManager = game.getGraphicsManager();
        Viewport viewport = new FitViewport(graphicsManager.getWidth(), graphicsManager.getHeight());
        stage = new Stage(viewport, game.getSpriteBatch());
        shapeRenderer = new ShapeRenderer();
        backgroundTexture = new Texture(Gdx.files.internal("Assets_Map/mega_inside.png"));
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);
        megaTexture = new Texture(Gdx.files.internal("Assets_Map/mega.png"));
        megaImage = new Image(megaTexture);
        table = new Table();
        table.setFillParent(true);
        table.top();
        table.padTop(20);
        stage.addActor(table);

        backButton = new TextButton("<", game.getSkin());
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                audioManager.playSound("Click.wav", 1);
                game.goBackFromProgressionTree();
            }
        });
    }

    /**
     * Sets input processing and rebuilds the tree.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.addListener(game.getKeyHandler());
        rebuildTable();
    }

    /**
     * Renders the screen and upgrade connections.
     *
     * @param delta frame delta time
     */
    @Override
    public void render(float delta) {
        if (!game.shouldRenderMenuBackground()) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
        drawConnections();
    }

    /**
     * Updates viewport on resize.
     *
     * @param width new width
     * @param height new height
     */
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    /**
     * Disposes stage, renderer, and textures.
     */
    @Override
    public void dispose() {
        stage.dispose();
        shapeRenderer.dispose();
        backgroundTexture.dispose();
        megaTexture.dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    /**
     * Rebuilds the progression tree layout.
     */
    private void rebuildTable() {
        table.clear();
        buttonsByName.clear();

        Table header = new Table();
        header.add(backButton).size(60, 60).left().padLeft(10);
        Table titleRow = new Table();
        Label titleLabel = new Label("Welcome to", game.getSkin(), "title");
        titleRow.add(titleLabel).padRight(20);
        titleRow.add(megaImage).height(60).width(180);
        Table titleBlock = new Table();
        titleBlock.add(titleRow).row();
        int credits = game.getProgressionManager().getPoints();
        Label creditsLabel = new Label("You have [#FFD54F]" + credits + "[] lei to spend", game.getSkin());
        creditsLabel.getStyle().font.getData().markupEnabled = true;
        creditsLabel.setFontScale(1.5f);
        titleBlock.add(creditsLabel).padTop(8);
        header.add(titleBlock).expandX().center();
        header.add().size(60, 60);
        table.add(header).expandX().fillX().padBottom(40).row();

        Table treeTable = new Table();
        treeTable.top();


        Table rowZero = new Table();
        rowZero.add().width(260).height(120).pad(20);
        rowZero.add(createUpgradeButton("root")).width(260).height(120).pad(20);
        rowZero.add().width(260).height(120).pad(20);
        rowZero.add(createUpgradeButton("new_glasses")).width(260).height(120).pad(20);
        treeTable.add(rowZero).row();


        Table rowOne = new Table();
        rowOne.add(createUpgradeButton("speed")).width(260).height(120).pad(20);
        rowOne.add(createUpgradeButton("health")).width(260).height(120).pad(20);
        rowOne.add(createUpgradeButton("stealth")).width(260).height(120).pad(20);
        rowOne.add().width(260).height(120).pad(20);
        treeTable.add(rowOne).row();

        Table rowTwo = new Table();
        rowTwo.add(createUpgradeButton("speed_2")).width(260).height(120).pad(20);
        rowTwo.add().width(260).height(120).pad(20); // (260 - (40)) / 2; padding left + right = 40
        rowTwo.add(createUpgradeButton("health_2")).width(260).height(120).pad(20);
        rowTwo.add(createUpgradeButton("pothol_imunity")).width(260).height(120).pad(20);
        treeTable.add(rowTwo).row();

        Table rowThree = new Table();
        rowThree.add(createUpgradeButton("drink_speed_1")).width(260).height(120).pad(20);
        rowThree.add(createUpgradeButton("speed_3")).width(260).height(120).pad(20);
        rowThree.add(createUpgradeButton("health_3")).width(260).height(120).pad(20);
        rowThree.add(createUpgradeButton("regen")).width(260).height(120).pad(20);
        rowThree.add().width(260).height(120).pad(20);
        treeTable.add(rowThree).row();

        Table rowFive = new Table();
        rowFive.add(createUpgradeButton("drink_speed_2")).width(260).height(120).pad(20);
        rowFive.add().width(130).height(120).pad(20);
        rowFive.add(createUpgradeButton("master")).width(260).height(120).pad(20);
        rowFive.add().width(390).height(120).pad(20);
        rowFive.add().width(260).height(120).pad(20);
        treeTable.add(rowFive).row();

        Table treeContainer = new Table();
        treeContainer.setBackground(game.getSkin().getDrawable("cell"));
        treeContainer.add(treeTable).padLeft(100).width(1300);
        table.add(treeContainer).expand().left().padTop(-20).row();
    }

    /**
     * Creates a button for a given upgrade name.
     *
     * @param upgradeName upgrade identifier
     * @return configured upgrade button
     */
    private TextButton createUpgradeButton(String upgradeName) {
        ProgressionManager progressionManager = game.getProgressionManager();
        Upgrade upgrade = progressionManager.getUpgrade(upgradeName);
        String title = upgrade == null ? upgradeName : upgrade.getTitle();
        int cost = upgrade == null ? 0 : upgrade.getCost();
        TextButton button = new TextButton(title + "\n[#FFD54F]" + cost + " lei[]", game.getSkin());
        button.getLabel().getStyle().font.getData().markupEnabled = true;
        boolean lockGlasses = "new_glasses".equals(upgradeName)
                && (game.getCurrentLevelNumber() == 1 || game.getCurrentLevelNumber() == 2);
        boolean canPurchase = !lockGlasses && progressionManager.canPurchase(upgradeName);
        boolean owned = progressionManager.hasUpgrade(upgradeName);
        if (lockGlasses && !owned) {
            button.setDisabled(true);
            button.setTouchable(Touchable.disabled);
            button.getLabel().setColor(Color.GRAY);
        } else if (!canPurchase || owned) {
            button.setDisabled(true);
            button.setTouchable(Touchable.disabled);
            if (owned) {
                button.getLabel().setColor(Color.GREEN);
            }
        } else {
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (progressionManager.buyUpgrade(upgradeName)) {
                        audioManager.playSound("Click.wav", 1);
                        rebuildTable();
                    }
                }
            });
        }
        buttonsByName.put(upgradeName, button);
        return button;
    }

    /**
     * Draws lines between connected upgrade buttons.
     */
    private void drawConnections() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        for (String[] connection : CONNECTIONS) {
            TextButton parent = buttonsByName.get(connection[0]);
            TextButton child = buttonsByName.get(connection[1]);
            if (parent == null || child == null) {
                continue;
            }
            Vector2 start = new Vector2(parent.getWidth() / 2f, 0);
            Vector2 end = new Vector2(child.getWidth() / 2f, child.getHeight());
            parent.localToStageCoordinates(start);
            child.localToStageCoordinates(end);
            shapeRenderer.line(start.x, start.y, end.x, end.y);
        }
        shapeRenderer.end();
    }
}
