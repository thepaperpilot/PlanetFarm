package thepaperpilot.farm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
    ParticleEffect stars;

    public PlanetScreen() {
        Planet.PlanetPrototype prototype = new Planet.PlanetPrototype();
        prototype.low = Color.BLUE;
        prototype.high = new Color(0, .5f, 0, 1);
        // Higher seems to make it run slower for higher quality
        prototype.octave = 8;
        // Higher makes smaller high spots
        prototype.frequency = 2;
        prototype.delta = 2;
        prototype.cloud = Color.WHITE;
        prototype.cloudOctave = 8;
        prototype.cloudFrequency = 8;
        prototype.cloudOpacity = .6f;
        prototype.clouddelta = 2;
        planet = new Planet(prototype);
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
        TextButton randomize = new TextButton("Randomize", Main.skin);
        randomize.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                planet = Planet.random();
            }
        });
        table.add(randomize);
        final Slider slider = new Slider(32, 1024, 16, false, Main.skin);
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
        planet.render(delta); // TODO make this render in a more reliable position/scale
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
