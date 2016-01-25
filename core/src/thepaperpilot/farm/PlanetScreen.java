package thepaperpilot.farm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.ArrayList;

public class PlanetScreen implements Screen{
    private static final float COLUMNS = 4;

    ArrayList<Planet> planets = new ArrayList<Planet>();
    Stage ui;
    Label fps;
    ParticleEffect stars;
    private final PerspectiveCamera camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    public PlanetScreen() {
        Preferences prefs = Gdx.app.getPreferences("thepaperpilot.farm.planet1");
        planets.add(new Planet(getPlanet(prefs)));
        save(prefs, planets.get(0).prototype);
        int j = 2;
        while (true) {
            prefs = Gdx.app.getPreferences("thepaperpilot.farm.planet" + j);
            if (prefs.getBoolean("planet"))
                planets.add(new Planet(getPlanet(prefs)));
            else break;
            j++;
        }

        ui = new Stage(new StretchViewport(320, 180));
        Table table = new Table(Main.skin);
        table.setFillParent(true);
        table.top();
        final Slider slider = new Slider(32, 512, 16, false, Main.skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Planet.TEXTURE_QUALITY = (int) slider.getValue();
                for (int i = 0; i < planets.size(); i++) {
                    Planet planet = planets.get(i);
                    planet.terminate();
                    planets.remove(i);
                    planets.add(i, new Planet(planet.prototype));
                }
                updatePositions();
            }
        });
        slider.setValue(Planet.TEXTURE_QUALITY);
        table.add(slider).colspan(2).minWidth(10).expandX().fill().row();
        fps = new Label("", Main.skin);
        table.add(fps).left().top().expand().row();
        TextButton mutate = new TextButton("Mutate", Main.skin);
        mutate.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Planet oldPlanet = planets.get(planets.size() - 1);
                oldPlanet.terminate();
                planets.remove(oldPlanet);
                Planet newPlanet = new Planet(oldPlanet.mutate());
                planets.add(newPlanet);
                save(Gdx.app.getPreferences("thepaperpilot.farm.planet" + (planets.indexOf(newPlanet) + 1)), newPlanet.prototype);
                updatePositions();
            }
        });
        table.add(mutate).expandX().fill();
        TextButton randomize = new TextButton("Randomize", Main.skin);
        randomize.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Planet newPlanet = new Planet(Planet.random());
                planets.add(newPlanet);
                save(Gdx.app.getPreferences("thepaperpilot.farm.planet" + (planets.indexOf(newPlanet) + 1)), newPlanet.prototype);
                updatePositions();
            }
        });
        table.add(randomize).expandX().fill();
        ui.addActor(table);
        stars = new ParticleEffect();
        stars.load(Gdx.files.internal("stars.p"), Gdx.files.internal(""));
        for (int i = 0; i < 100; i++) {
            stars.update(.1f);
        }

        camera.near = 1f;
        camera.far = 300f;
        updatePositions();
    }

    private void updatePositions() {
        int rows = MathUtils.ceil(planets.size() / COLUMNS);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < COLUMNS && i * COLUMNS + j < planets.size(); j++) {
                planets.get((int) (i * COLUMNS + j)).position(2 * Planet.PLANET_SIZE * j, -2 * Planet.PLANET_SIZE * i);
            }
        }
        camera.position.set(Planet.PLANET_SIZE * (COLUMNS - 1), -Planet.PLANET_SIZE * (rows - 1), 8 * Planet.PLANET_SIZE);
        camera.lookAt(Planet.PLANET_SIZE * (COLUMNS - 1), -Planet.PLANET_SIZE * (rows - 1), 0);
        camera.update();
    }

    private static void save(Preferences prefs, Planet.PlanetPrototype prototype) {
        prefs.putFloat("lowR", prototype.low.r);
        prefs.putFloat("lowG", prototype.low.g);
        prefs.putFloat("lowB", prototype.low.b);
        prefs.putFloat("highR", prototype.high.r);
        prefs.putFloat("highG", prototype.high.g);
        prefs.putFloat("highB", prototype.high.b);
        prefs.putFloat("frequency", prototype.frequency);
        prefs.putFloat("x1", prototype.x1);
        prefs.putFloat("y1", prototype.y1);
        prefs.putFloat("delta", prototype.delta);
        prefs.putFloat("cloudR", prototype.cloud.r);
        prefs.putFloat("cloudG", prototype.cloud.g);
        prefs.putFloat("cloudB", prototype.cloud.b);
        prefs.putFloat("cloudFrequency", prototype.cloudFrequency);
        prefs.putFloat("cloudOpacity", prototype.cloudOpacity);
        prefs.putFloat("cloudx1", prototype.cloudx1);
        prefs.putFloat("cloudy1", prototype.cloudy1);
        prefs.putFloat("cloudDelta", prototype.clouddelta);
        prefs.putBoolean("planet", true);
        prefs.flush();
    }

    public static Planet.PlanetPrototype getPlanet(Preferences prefs) {
        Planet.PlanetPrototype prototype = new Planet.PlanetPrototype();
        prototype.low = new Color(prefs.getFloat("lowR", 0), prefs.getFloat("lowG", 0), prefs.getFloat("lowB", 1), 1);
        prototype.high = new Color(prefs.getFloat("highR", 0), prefs.getFloat("highG", .5f), prefs.getFloat("highB", 0), 1);
        prototype.frequency = prefs.getFloat("frequency", 2);
        prototype.x1 = prefs.getFloat("x1", 0);
        prototype.y1 = prefs.getFloat("y1", 0);
        prototype.delta = prefs.getFloat("delta", 2);
        prototype.cloud = new Color(prefs.getFloat("cloudR", 1), prefs.getFloat("cloudG", 1), prefs.getFloat("cloudB", 1), 1);
        prototype.cloudFrequency = prefs.getFloat("cloudFrequency", 8);
        prototype.cloudOpacity = prefs.getFloat("cloudOpacity", .8f);
        prototype.cloudx1 = prefs.getFloat("cloudx1", 0);
        prototype.cloudy1 = prefs.getFloat("cloudy1", 0);
        prototype.clouddelta = prefs.getFloat("cloudDelta", 2);
        return prototype;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(ui);
    }

    @Override
    public void render(float delta) {
        fps.setText("fps: " + Gdx.graphics.getFramesPerSecond());
        Planet.spriteBatch.setProjectionMatrix(camera.combined);
        Planet.spriteBatch.begin();
        Planet.modelBatch.begin(camera);
        stars.draw(Planet.spriteBatch, delta);
        for (Planet planet : planets) {
            planet.render(delta);
        }
        Planet.spriteBatch.end();
        Planet.modelBatch.end();
        ui.act();
        ui.draw();
    }

    @Override
    public void resize(int width, int height) {
        ui.getViewport().update(width, height);
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
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

    @Override
    public void dispose() {
        for (Planet planet : planets) {
            planet.dispose();
        }
    }
}
