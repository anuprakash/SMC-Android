package rs.pedjaapps.smc.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import rs.pedjaapps.smc.MaryoGame;
import rs.pedjaapps.smc.audio.MusicManager;
import rs.pedjaapps.smc.audio.SoundManager;
import rs.pedjaapps.smc.object.GameObject;
import rs.pedjaapps.smc.object.World;
import rs.pedjaapps.smc.shader.Shader;
import rs.pedjaapps.smc.utility.Constants;
import rs.pedjaapps.smc.utility.GameSave;
import rs.pedjaapps.smc.utility.LevelLoader;
import rs.pedjaapps.smc.utility.NATypeConverter;
import rs.pedjaapps.smc.utility.PrefsManager;
import rs.pedjaapps.smc.utility.Utility;
import rs.pedjaapps.smc.view.Background;
import rs.pedjaapps.smc.view.ConfirmDialog;
import rs.pedjaapps.smc.view.SelectionAdapter;
import rs.pedjaapps.smc.view.SettingsDialog;

/**
 * Created by pedja on 2/17/14.
 */
public class MainMenuScreen extends AbstractScreen implements InputProcessor
{
    private Texture gameLogo;
    private TextureRegion play, musicOn, musicOff, soundOn, soundOff, settings;
    private Rectangle playR, musicR, soundR, settingsR;
    private OrthographicCamera drawCam, debugCam, hudCam;
    private SpriteBatch batch;
    public MaryoGame game;
	private Background background;
	private Background backgroundColor;
    private LevelLoader loader;
    private BitmapFont debugFont;
    private GlyphLayout debugGlyph;
    private boolean playT = false, musicT = false, soundT = false, settingsT = false;
    private static final String FPS_STRING = "FPS: ";
    private static final NATypeConverter<Integer> fpsCounter = new NATypeConverter<>();

    private int screenWidth = Gdx.graphics.getWidth();
    private int screenHeight = Gdx.graphics.getHeight();
    private Music music;
    private Sound audioOn;
    private World world;
    private ParticleEffect cloudsPEffect;

	private SelectionAdapter selectionAdapter;

	public boolean isSelection;

    private ConfirmDialog exitDialog;
    private SettingsDialog settingsDialog;

    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private TextureRegion marioFrame;

    public MainMenuScreen(MaryoGame game)
    {
		super(game);
        this.game = game;
        batch = new SpriteBatch();
        drawCam = new OrthographicCamera(Constants.MENU_CAMERA_WIDTH, Constants.MENU_CAMERA_HEIGHT);
        drawCam.position.set(Constants.MENU_CAMERA_WIDTH / 2 + (Constants.MENU_DRAW_WIDTH - Constants.MENU_CAMERA_WIDTH) / 2, Constants.MENU_CAMERA_HEIGHT / 2, 0);
        drawCam.update();
        debugCam = new OrthographicCamera(1280, 720);
        debugCam.position.set(1280 / 2, 720 / 2, 0);
        debugCam.update();
        hudCam = new OrthographicCamera(screenWidth, screenHeight);
        hudCam.position.set(screenWidth / 2, screenHeight / 2, 0);
        hudCam.update();

        loader = new LevelLoader("main_menu");
        debugFont = new BitmapFont();
        debugFont.setColor(Color.RED);
        debugFont.getData().setScale(1.3f);
        debugGlyph = new GlyphLayout();
        world = new World(this);

		selectionAdapter = new SelectionAdapter(loadSelectionItems(), this);
        exitDialog = new ConfirmDialog(this, hudCam);
        settingsDialog = new SettingsDialog(this, hudCam);
    }

	private Array<SelectionAdapter.Level> loadSelectionItems()
	{
		Array<SelectionAdapter.Level> items = new Array<SelectionAdapter.Level>();
		for (int i = 0; i < GameSave.LEVELS.size(); i++)
		{
			SelectionAdapter.Level level = new SelectionAdapter.Level();
            if(i < GameSave.LEVELS.size())level.levelId = GameSave.LEVELS.get(i);
            level.isUnlocked = i == 0 || GameSave.isUnlocked(level.levelId);
			items.add(level);
		}
		return items;
	}

    @Override
    public void show()
    {
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(this);
        music = world.screen.game.assets.manager.get(loader.level.music.first());
        music.setLooping(true);
        MusicManager.play(music);
    }

    @Override
    public void render(float delta)
    {
		Gdx.gl20.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		backgroundColor.render(drawCam, batch);
		background.render(drawCam, batch);

        batch.setProjectionMatrix(drawCam.combined);
        batch.begin();

        cloudsPEffect.draw(batch, delta);

        drawObjects(delta);

        Utility.draw(batch, gameLogo, 2f, 5f, 2f);

        batch.draw(marioFrame, 2, 4.609375f, 0.85f, 0.85f);

        batch.end();

        if (isSelection)
		{
			selectionAdapter.render(delta);
		}
		else
		{
			batch.setProjectionMatrix(hudCam.combined);
			batch.begin();

            if(playT)batch.setShader(Shader.GLOW_SHADER);
			batch.draw(play, playR.x, playR.y, playR.width, playR.height);
            batch.setShader(null);

            if(soundT)batch.setShader(Shader.GLOW_SHADER);
			batch.draw((PrefsManager.isPlaySounds() ? soundOn : soundOff), soundR.x, soundR.y, soundR.width, soundR.height);
            batch.setShader(null);

            if(musicT)batch.setShader(Shader.GLOW_SHADER);
			batch.draw((PrefsManager.isPlayMusic() ? musicOn : musicOff), musicR.x, musicR.y, musicR.width, musicR.height);
            batch.setShader(null);

            if(settingsT)batch.setShader(Shader.GLOW_SHADER);
			batch.draw(settings, settingsR.x, settingsR.y, settingsR.width, settingsR.height);
            batch.setShader(null);

			batch.end();
		}

        exitDialog.render(batch);
        settingsDialog.render(batch);
    }

    private void drawObjects(float deltaTime)
    {
        //noinspection ForLoopReplaceableByForEach
        for(int i = 0, size = loader.level.gameObjects.size(); i < size; i++)
        //for (GameObject gameObject : loader.level.gameObjects)
        {
            GameObject gameObject = loader.level.gameObjects.get(i);
			gameObject._update(deltaTime);
            gameObject._render(batch);
        }
    }

    @Override
    public void resize(int width, int height)
    {
        screenHeight = Gdx.graphics.getHeight();
        screenWidth = Gdx.graphics.getWidth();
        Constants.initCamera();
        drawCam = new OrthographicCamera(Constants.MENU_CAMERA_WIDTH, Constants.MENU_CAMERA_HEIGHT);
        drawCam.position.set(Constants.MENU_CAMERA_WIDTH / 2 + (Constants.MENU_DRAW_WIDTH - Constants.MENU_CAMERA_WIDTH) / 2, Constants.MENU_CAMERA_HEIGHT / 2, 0);
        drawCam.update();
        hudCam = new OrthographicCamera(screenWidth, screenHeight);
        hudCam.position.set(screenWidth / 2, screenHeight / 2, 0);
        hudCam.update();
        exitDialog.resize();
        settingsDialog.resize();
    }

    @Override
    public void hide()
    {
        music.stop();
    }

    @Override
    public void pause()
    {
        music.stop();
    }

    @Override
    public void resume()
    {
        MusicManager.play(music);
    }

    @Override
    public void dispose()
    {
        Gdx.input.setInputProcessor(null);
        game.assets.dispose();
        background.dispose();
        backgroundColor.dispose();
        batch.dispose();
        exitDialog.dispose();
        settingsDialog.dispose();
        music.stop();
    }

    @Override
    public void loadAssets()
    {
        loader.parseLevel(world);
		game.assets.manager.load("data/hud/hud.pack", TextureAtlas.class);
        game.assets.manager.load("data/hud/controls.pack", TextureAtlas.class);
        game.assets.manager.load("data/maryo/small.pack", TextureAtlas.class);
        game.assets.manager.load("data/game/logo/smc_big_1.png", Texture.class, game.assets.textureParameter);
        game.assets.manager.load("data/game/background/more_hills.png", Texture.class, game.assets.textureParameter);
        game.assets.manager.load("data/sounds/audio_on.mp3", Sound.class);
        cloudsPEffect = new ParticleEffect();
        cloudsPEffect.load(game.assets.resolver.resolve("data/animation/particles/clouds_emitter.p"), game.assets.resolver.resolve("data/clouds/default_1/"));
        cloudsPEffect.setPosition(Constants.MENU_CAMERA_WIDTH / 2, Constants.MENU_CAMERA_HEIGHT);
        cloudsPEffect.start();

        game.assets.manager.load("data/hud/lock.png", Texture.class, game.assets.textureParameter);
        exitDialog.loadAssets();
        settingsDialog.loadAssets();

    }

    @Override
    public void onAssetsLoaded()
    {
        TextureAtlas hud = game.assets.manager.get("data/hud/hud.pack");
		play = hud.findRegion("play");
        playR = new Rectangle(screenWidth / 2f - (screenWidth / 9f) / 2,
							  screenHeight / 2f - (screenWidth / 9f) / 2, screenWidth / 9f, screenWidth / 9f);

        musicOn = hud.findRegion("music");
        musicOff = hud.findRegion("music_off");
        musicR = new Rectangle(screenWidth - (screenWidth / 18f) * 1.25f,
							   (screenWidth / 18f) / 4, screenWidth / 18f, screenWidth / 18f);

        soundOn = hud.findRegion("sound");
        soundOff = hud.findRegion("sound_off");
        soundR = new Rectangle(screenWidth - (screenWidth / 18f) * 2.5f,
							   (screenWidth / 18f) / 4, screenWidth / 18f, screenWidth / 18f);

        settings = hud.findRegion("settings");
        settingsR = new Rectangle(screenWidth - (screenWidth / 18f) * 3.75f,
							   (screenWidth / 18f) / 4, screenWidth / 18f, screenWidth / 18f);

        background = new Background(new Vector2(0, 0), new Vector2(), "data/game/background/more_hills.png", Constants.MENU_CAMERA_WIDTH, Constants.MENU_CAMERA_HEIGHT, Constants.MENU_CAMERA_WIDTH * 2, Constants.MENU_CAMERA_HEIGHT, Background.BG_IMG_BOTTOM);
        background.onAssetsLoaded(drawCam, game.assets);

        backgroundColor = new Background(Background.BG_GR_VER);
        backgroundColor.setColors(new Color(.117f, 0.705f, .05f, 0f), new Color(0f, 0.392f, 0.039f, 0f));//color is 0-1 range where 1 = 255

        gameLogo = game.assets.manager.get("data/game/logo/smc_big_1.png");
        gameLogo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        //gdxLogo = Assets.manager.get("/game/logo/libgdx.png");
        //gdxLogo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        world.level = loader.level;

        TextureAtlas atlas = game.assets.manager.get("data/maryo/small.pack");
        marioFrame = atlas.findRegion(GameObject.TKey.stand_right.toString());

        audioOn = game.assets.manager.get("data/sounds/audio_on.mp3", Sound.class);

		selectionAdapter.initAssets();
        exitDialog.initAssets();
        settingsDialog.initAssets();

        for(GameObject go : loader.level.gameObjects)
            go.initAssets();
    }

    @Override
    public boolean keyDown(int keycode)
    {
        if (keycode == Input.Keys.ENTER)
        {
            //game.setScreen(new LoadingScreen(new GameScreen(game), false));
			isSelection = true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode)
    {
        if(keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE)
        {
            if(exitDialog.visible)exitDialog.hide();
            else exitDialog.show();
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character)
    {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        float x = screenX;//screenX / (screenWidth / Constants.CAMERA_WIDTH);
        float y = screenHeight - screenY;

        if(exitDialog.visible)
        {
            exitDialog.touchDown(x, y);
            return true;
        }

        if(settingsDialog.visible)
        {
            settingsDialog.touchDown(x, y);
            return true;
        }

		if (isSelection)
		{
			selectionAdapter.touchDown(x, y);
		}
		else
		{
			if (playR.contains(x, y))
			{
				playT = true;
			}
			if (musicR.contains(x, y))
			{
				musicT = true;
			}
			if (soundR.contains(x, y))
			{
				soundT = true;
			}
			if (settingsR.contains(x, y))
			{
				settingsT = true;
			}
		}
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        float x = screenX;// / (screenWidth / Constants.CAMERA_WIDTH);
        float y = screenHeight - screenY;

        if(exitDialog.visible)
        {
            exitDialog.touchUp(x, y);
            return true;
        }

        if(settingsDialog.visible)
        {
            settingsDialog.touchUp(x, y);
            return true;
        }

        if (isSelection)
        {
            selectionAdapter.touchUp(x, y);
        }
        else
        {
            if (playR.contains(x, y))
            {
                playT = false;
                //music.stop();
                //game.setScreen(new LoadingScreen(new GameScreen(game), false));
                isSelection = true;
            }
            if (musicR.contains(x, y))
            {
                musicT = false;
                if (Utility.toggleMusic())
                {
                    MusicManager.play(music);
                }
                else
                {
                    music.pause();
                }
            }
            if (soundR.contains(x, y))
            {
                soundT = false;
                if (Utility.toggleSound())
                {
                    SoundManager.play(audioOn);
                }
            }

            if (settingsR.contains(x, y))
            {
                settingsT = false;
                settingsDialog.show();
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        float x = screenX;// / (screenWidth / Constants.CAMERA_WIDTH);
        float y = screenHeight - screenY;

        if(exitDialog.visible)
        {
            exitDialog.touchDragged(x, y);
            return true;
        }

        if(settingsDialog.visible)
        {
            settingsDialog.touchDragged(x, y);
            return true;
        }

        if (isSelection)
        {
            selectionAdapter.touchDragged(x, y);
        }
        else
        {
            playT = playR.contains(x, y);
            musicT = musicR.contains(x, y);
            soundT = soundR.contains(x, y);
            settingsT = settingsR.contains(x, y);
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY)
    {
        return false;
    }

    @Override
    public boolean scrolled(int amount)
    {
        return false;
    }
}
