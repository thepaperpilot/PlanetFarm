package thepaperpilot.farm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class PlanetTest implements Screen{
    private final PerspectiveCamera camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    private final ModelBatch batch = new ModelBatch();
    private final Model planet;
    private final ModelInstance instance;
    private final Environment environment = new Environment();

    Texture texture;

    static int columns = 4;

    public PlanetTest(Color color) {
        camera.position.set(10f, 10f, 10f);
        camera.lookAt(0,0,0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        ModelBuilder modelBuilder = new ModelBuilder();
        planet = modelBuilder.createSphere(10, 10, 10, 15, 15,
                new Material(TextureAttribute.createDiffuse(texture = simplex(256, color))),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        instance = new ModelInstance(planet);

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }

    //The function that generates the simplex noise texture
    public static Texture simplex(int size, Color color) {
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

                    //Generate 6 octaves of noise
                    float gray = (float)(SimplexNoise.fbm(6, sx, sy, sz, 8) / 2 + 0.5);

                    //Set components of the current pixel
                    data[offset    ] = (byte) (color.r * (byte)(gray * 255));
                    data[offset + 1] = (byte) (color.g * (byte)(gray * 255));
                    data[offset + 2] = (byte) (color.b * (byte)(gray * 255));
                    data[offset + 3] = (byte) (color.a * (byte)(255));

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

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        instance.transform.rotate(0, 1, 0, 2);
        batch.begin(camera);
        batch.render(instance, environment);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        planet.dispose();
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {
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
}
