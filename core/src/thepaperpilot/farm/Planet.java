package thepaperpilot.farm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Planet{
    public static int TEXTURE_QUALITY = 64;

    private final PerspectiveCamera camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    private final ModelBatch batch = new ModelBatch();
    private final Model planet;
    private final ModelInstance instance;
    private final Model clouds;
    private final ModelInstance cloudsInstance;
    private final Environment environment = new Environment();
    public final PlanetPrototype prototype;

    public Planet(PlanetPrototype prototype) {
        this.prototype = prototype;
        camera.position.set(15, -4, 0);
        camera.lookAt(0,0,0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        ModelBuilder modelBuilder = new ModelBuilder();
        planet = modelBuilder.createSphere(10, 10, 10, 25, 25,
                new Material(TextureAttribute.createDiffuse(simplex(TEXTURE_QUALITY, prototype.low, prototype.high, prototype.octave, prototype.frequency, 1, prototype.x1, prototype.y1, prototype.delta))),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        instance = new ModelInstance(planet);
        clouds = modelBuilder.createSphere(10.2f, 10.2f, 10.2f, 25, 25,
                new Material(TextureAttribute.createDiffuse(simplex(TEXTURE_QUALITY, prototype.cloud, Color.CLEAR, prototype.cloudOctave, prototype.cloudFrequency, prototype.cloudOpacity, prototype.cloudx1, prototype.cloudy1, prototype.clouddelta))),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        clouds.materials.first().set(new BlendingAttribute(GL20.GL_BLEND_SRC_ALPHA, GL20.GL_BLEND_SRC_ALPHA));
        cloudsInstance = new ModelInstance(clouds);

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, 0.1f, 1f));
        environment.add(new DirectionalLight().set(0.4f, 0.4f, 0.4f, -1f, -0.8f, -0.2f));
    }

    //The function that generates the simplex noise texture
    public static Texture simplex(int size, Color low, Color high, int octave, double frequency, float modifier, double x1, double y1, double delta) {
        byte[] data = new byte[size * size * 4];
        int offset = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                //Scale x and y to [-1,1] range
                double tx = ((double)x / (size - 1)) * 2 - 1;
                double ty = 1 - ((double)y / (size - 1)) * 2;

                //Determine point on sphere in worldspace
                double sx = x1 + MathUtils.cos((float) (2 * tx * Math.PI)) * delta / (2 * Math.PI);
                double sy = y1 + MathUtils.cos((float) (2 * ty * Math.PI)) * delta / (2 * Math.PI);
                double sz = x1 + MathUtils.sin((float) (2 * tx * Math.PI)) * delta / (2 * Math.PI);
                double sw = y1 + MathUtils.sin((float) (2 * ty * Math.PI)) * delta / (2 * Math.PI);

                //Generate noise
                float gray = (float) (SimplexNoise.fbm(octave, sx, sy, sz, sw, frequency) / 2f + 0.5);
                gray *= modifier;
                float ogray = (1 - gray);
                gray *= 255;
                ogray *= 255;

                //Set components of the current pixel
                data[offset    ] = (byte) (low.r * gray + high.r * ogray);
                data[offset + 1] = (byte) (low.g * gray + high.g * ogray);
                data[offset + 2] = (byte) (low.b * gray + high.b * ogray);
                data[offset + 3] = (byte) (low.a * gray + high.a * ogray);

                //Move to the next pixel
                offset += 4;
            }
        }

        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.getPixels().put(data).position(0);

        Texture texture = new Texture(pixmap, true);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    public void render(float delta) {
        instance.transform.rotate(0, 1, 0, delta * 10);
        cloudsInstance.transform.rotate(0, 1, 0, delta * 5);

        batch.begin(camera);
        batch.render(instance, environment);
        batch.render(cloudsInstance, environment);
        batch.end();
    }

    public void dispose() {
        batch.dispose();
        planet.dispose();
        clouds.dispose();
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public String toString() {
        return "PlanetPrototype{" +
                "low=" + prototype.low +
                ", high=" + prototype.high +
                ", octave=" + prototype.octave +
                ", frequency=" + prototype.frequency +
                ", x1=" + prototype.x1 +
                ", y1=" + prototype.y1 +
                ", delta=" + prototype.delta +
                ", cloud=" + prototype.cloud +
                ", cloudOctave=" + prototype.cloudOctave +
                ", cloudFrequency=" + prototype.cloudFrequency +
                ", cloudOpacity=" + prototype.cloudOpacity +
                ", cloudx1=" + prototype.cloudx1 +
                ", cloudy1=" + prototype.cloudy1 +
                ", clouddelta=" + prototype.clouddelta +
                '}';
    }

    public static Planet random() {
        PlanetPrototype prototype = new PlanetPrototype();
        prototype.low = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
        prototype.high = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
        prototype.octave = 8;
        prototype.frequency = MathUtils.random(6f) + 1;
        prototype.x1 = MathUtils.random(100) - 50;
        prototype.y1 = MathUtils.random(100) - 50;
        prototype.delta = MathUtils.random(6f) + 1;
        prototype.cloud = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1);
        prototype.cloudOctave = 8;
        prototype.cloudFrequency = MathUtils.random(6) + 1;
        prototype.cloudOpacity = MathUtils.random() / 2f + .5f;
        prototype.cloudx1 = MathUtils.random(100) - 50;
        prototype.cloudy1 = MathUtils.random(100) - 50;
        prototype.clouddelta = MathUtils.random(6f) + 1;
        return new Planet(prototype);
    }

    public static class PlanetPrototype {
        Color low;
        Color high;
        int octave;
        double frequency;
        double x1;
        double y1;
        double delta;

        Color cloud;
        int cloudOctave;
        double cloudFrequency;
        float cloudOpacity;
        double cloudx1;
        double cloudy1;
        double clouddelta;
    }
}
