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
    private final PerspectiveCamera camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    private final ModelBatch batch = new ModelBatch();
    private final Model planet;
    private final ModelInstance instance;
    private final Model clouds;
    private final ModelInstance cloudsInstance;
    private final Environment environment = new Environment();

    Texture texture;

    static int columns = 1;

    public Planet(PlanetPrototype prototype) {
        camera.position.set(20f, 0, 0);
        camera.lookAt(0,0,0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        ModelBuilder modelBuilder = new ModelBuilder();
        planet = modelBuilder.createSphere(10, 10, 10, 25, 25,
                new Material(TextureAttribute.createDiffuse(texture = simplex(128, prototype.low, prototype.high, prototype.octave, prototype.frequency, 1))),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        instance = new ModelInstance(planet);
        clouds = modelBuilder.createSphere(10.2f, 10.2f, 10.2f, 25, 25,
                new Material(TextureAttribute.createDiffuse(texture = simplex(128, prototype.cloud, Color.CLEAR, prototype.cloudOctave, prototype.cloudFrequency, prototype.cloudOpacity))),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        clouds.materials.first().set(new BlendingAttribute(GL20.GL_BLEND_SRC_ALPHA, GL20.GL_BLEND_SRC_ALPHA));
        cloudsInstance = new ModelInstance(clouds);

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }

    //The function that generates the simplex noise texture
    public static Texture simplex(int size, Color low, Color high, int octave, double frequency, float modifier) {
        byte[] data = new byte[size * size * columns * 4];
        int offset = 0;
        for (int y = 0; y < size; y++) {
            for (int s = 0; s < columns; s++) {
                for (int x = 0; x < size; x++) {
                    //Scale x and y to [-1,1] range
                    double tx = ((double)x / (size - 1)) * 2 - 1;
                    double ty = 1 - ((double)y / (size - 1)) * 2;

                    //Determine point on cube in worldspace
                    double cx = 0, cy = 0, cz = 0;
                    if      (s == 0) { cx =   1; cy =  tx; cz =  ty; }
                    else if (s == 1) { cx = -tx; cy =   1; cz =  ty; }
                    else if (s == 2) { cx = - 1; cy = -tx; cz =  ty; }
                    else if (s == 3) { cx =  tx; cy = - 1; cz =  ty; }
                    else if (s == 4) { cx = -ty; cy =  tx; cz =   1; }
                    else if (s == 5) { cx =  ty; cy =  tx; cz = - 1; }

                    //Determine point on sphere in worldspace
                    double sx = cx * Math.sqrt(1 - cy*cy/2 - cz*cz/2 + cy*cy*cz*cz/3);
                    double sy = cy * Math.sqrt(1 - cz*cz/2 - cx*cx/2 + cz*cz*cx*cx/3);
                    double sz = cz * Math.sqrt(1 - cx*cx/2 - cy*cy/2 + cx*cx*cy*cy/3);

                    //Generate noise
                    float gray = (float) (SimplexNoise.fbm(octave, sx, sy, sz, frequency) / 2f + 0.5);
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
        }

        Pixmap pixmap = new Pixmap(columns * size, size, Pixmap.Format.RGBA8888);
        pixmap.getPixels().put(data).position(0);

        Texture texture = new Texture(pixmap, true);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        instance.transform.rotate(0, 1, 0, 1);
        cloudsInstance.transform.rotate(0, 1, 0, .5f);

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

    public static class PlanetPrototype {
        Color low;
        Color high;
        int octave;
        double frequency;
        Color cloud;
        int cloudOctave;
        double cloudFrequency;
        float cloudOpacity;
    }
}
