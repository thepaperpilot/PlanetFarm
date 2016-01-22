package thepaperpilot.farm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class PlanetScreen implements Screen{
    Planet planet;
    Stage ui;
    Label fps;
    Label stats;

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
        TextButton randomize = new TextButton("Randomize", Main.skin);
        randomize.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                planet = Planet.random();
            }
        });
        stats = new Label("", Main.skin);
        stats.setWrap(true);
        stats.setAlignment(Align.top);
        table.add(stats).expand().fill().row();
        table.add(randomize);
        ui.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(ui);
    }

    @Override
    public void render(float delta) {
        planet.render(delta); // TODO make this render in a more reliable position/scale
        fps.setText("fps: " + Gdx.graphics.getFramesPerSecond());
        stats.setText(planet.toString());
        ui.act();
        ui.draw();
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
