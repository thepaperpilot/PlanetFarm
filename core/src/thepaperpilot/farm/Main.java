package thepaperpilot.farm;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class Main extends Game implements Screen {
	public static final Main instance = new Main();
	public static final AssetManager manager = new AssetManager();
	public static final float CAMERA_SPEED = 200;

	public static Skin skin;

	private Stage loadingStage;

	public static void changeScreen(Screen screen) {
		if (screen == null)
			return;
		instance.setScreen(screen);
	}

	@Override
	public void create() {
		// start loading all our assets
		manager.load("skin.json", Skin.class);

		// show this screen while it loads
		setScreen(this);
	}

	@Override
	public void show() {
		// show a basic loading screen
		loadingStage = new Stage(new ExtendViewport(200, 200));

		Label loadingLabel = new Label("Loading...", new Skin(Gdx.files.internal("skin.json")));
		loadingLabel.setFillParent(true);
		loadingLabel.setAlignment(Align.center);
		loadingStage.addActor(loadingLabel);

		// basically a sanity check? loadingStage shouldn't have any input listeners
		// but I guess this'll help if the inputprocessor gets set to something it shouldn't
		Gdx.input.setInputProcessor(loadingStage);
	}

	@Override
	public void render(float delta) {
		// render the loading screen
		// act shouldn't do anything, but putting it here is good practice, I guess?
		loadingStage.act();
		loadingStage.draw();

		// continue loading. If complete, do shit
		if (manager.update()) {
			// set some stuff we need universally, now that their assets are loaded
			skin = manager.get("skin.json", Skin.class);
			skin.getFont("large").getData().setScale(.5f);
			skin.getFont("font").getData().setScale(.25f);

			Planet.modelBatch = new ModelBatch();
			Planet.spriteBatch = new SpriteBatch();

			// go to the menu screen
			setScreen(new PlanetScreen());
		}
	}

	@Override
	public void hide() {
		/// we're a good garbage collector
		loadingStage.dispose();
	}

	@Override
	public void pause() {
		// we're a passthrough!
		if (getScreen() == this || getScreen() == this) return;
		super.pause();
	}

	@Override
	public void resume() {
		// we're a passthrough!
		if (getScreen() == this || getScreen() == this) return;
		super.pause();
	}

	@Override
	public void resize(int width, int height) {
		// we're a passthrough!
		if (getScreen() == this) return;
		if (getScreen() != null) {
			getScreen().resize(width, height);
		}
	}

	@Override
	public void dispose() {
		// we're a passthrough!
		if (getScreen() != null && getScreen() != this) {
			getScreen().dispose();
		}
		// also clean up our shit
		manager.dispose();
		if (skin != null) skin.dispose();
		if (Planet.modelBatch != null) Planet.modelBatch.dispose();
		if (Planet.spriteBatch != null) Planet.spriteBatch.dispose();
	}

	@Override
	public void render() {
		// we're a passthrough!
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		getScreen().render(Math.min(.1f, Gdx.graphics.getDeltaTime()));
	}
}
