package thepaperpilot.farm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class PlanetScreen implements Screen{
    Planet planet;
    Stage ui;
    Label fps;
    Label stats;
    Label generating;
    ParticleEffect stars;

    public PlanetScreen() {
        Preferences prefs = Gdx.app.getPreferences("thepaperpilot.farm.planet1");
        planet = new Planet(getPlanet(prefs));

        ui = new Stage(new StretchViewport(320, 180));
        Table table = new Table(Main.skin);
        table.setFillParent(true);
        table.top();
        fps = new Label("", Main.skin);
        table.add(fps).left().row();
        stats = new Label("", Main.skin);
        stats.setWrap(true);
        stats.setAlignment(Align.top);
        table.add(stats).expand().fill().row();
        TextButton mutate = new TextButton("Mutate", Main.skin);
        mutate.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                planet.terminate();
                planet = new Planet(planet.mutate());
                save(Gdx.app.getPreferences("thepaperpilot.farm.planet1"), planet.prototype);
            }
        });
        table.add(mutate).row();
        TextButton randomize = new TextButton("Randomize", Main.skin);
        randomize.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                planet.terminate();
                planet = new Planet(Planet.random());
                save(Gdx.app.getPreferences("thepaperpilot.farm.planet1"), planet.prototype);
            }
        });
        table.add(randomize);
        final Slider slider = new Slider(16, 1024, 16, false, Main.skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Planet.TEXTURE_QUALITY = (int) slider.getValue();
                planet = new Planet(planet.prototype);
            }
        });
        slider.setValue(Planet.TEXTURE_QUALITY);
        table.add(slider).minWidth(10).expandX().fill();
        ui.addActor(table);

        stars = new ParticleEffect();
        stars.load(Gdx.files.internal("stars.p"), Gdx.files.internal(""));
        stars.scaleEffect(.2f);
        for (int i = 0; i < 100; i++) {
            stars.update(.1f);
        }

        ui.addActor(new ParticleEffectActor(stars, ui.getWidth() / 2, ui.getHeight() / 2));

        generating = new Label("Generating", Main.skin, "large");
        generating.setFillParent(true);
        generating.setAlignment(Align.center);
        generating.setTouchable(Touchable.disabled);
        ui.addActor(generating);
    }

    private static void save(Preferences prefs, Planet.PlanetPrototype prototype) {
        prefs.putFloat("lowR", prototype.low.r);
        prefs.putFloat("lowG", prototype.low.g);
        prefs.putFloat("lowB", prototype.low.b);
        prefs.putFloat("highR", prototype.high.r);
        prefs.putFloat("highG", prototype.high.g);
        prefs.putFloat("highB", prototype.high.b);
        prefs.putInteger("octave", prototype.octave);
        prefs.putFloat("frequency", prototype.frequency);
        prefs.putFloat("x1", prototype.x1);
        prefs.putFloat("y1", prototype.y1);
        prefs.putFloat("delta", prototype.delta);
        prefs.putFloat("cloudR", prototype.cloud.r);
        prefs.putFloat("cloudG", prototype.cloud.g);
        prefs.putFloat("cloudB", prototype.cloud.b);
        prefs.putInteger("cloudOctave", prototype.cloudOctave);
        prefs.putFloat("cloudFrequency", prototype.cloudFrequency);
        prefs.putFloat("cloudOpacity", prototype.cloudOpacity);
        prefs.putFloat("cloudx1", prototype.cloudx1);
        prefs.putFloat("cloudy1", prototype.cloudy1);
        prefs.putFloat("cloudDelta", prototype.clouddelta);
        prefs.flush();
    }

    public static Planet.PlanetPrototype getPlanet(Preferences prefs) {
        Planet.PlanetPrototype prototype = new Planet.PlanetPrototype();
        prototype.low = new Color(prefs.getFloat("lowR", 0), prefs.getFloat("lowG", 0), prefs.getFloat("lowB", 1), 1);
        prototype.high = new Color(prefs.getFloat("highR", 0), prefs.getFloat("highG", .5f), prefs.getFloat("highB", 0), 1);
        prototype.octave = prefs.getInteger("octave", 8);
        prototype.frequency = prefs.getFloat("frequency", 2);
        prototype.x1 = prefs.getFloat("x1", 0);
        prototype.y1 = prefs.getFloat("y1", 0);
        prototype.delta = prefs.getFloat("delta", 2);
        prototype.cloud = new Color(prefs.getFloat("cloudR", 1), prefs.getFloat("cloudG", 1), prefs.getFloat("cloudB", 1), 1);
        prototype.cloudOctave = prefs.getInteger("cloudOctave", 8);
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
        stats.setText(planet.toString());
        ui.act();
        ui.draw();
        // TODO make this render in a more reliable position/scale
        generating.setVisible(!planet.render(delta));
    }

    @Override
    public void resize(int width, int height) {
        planet.resize(width, height);
        ui.getViewport().update(width, height);
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
        planet.dispose();
    }
}
