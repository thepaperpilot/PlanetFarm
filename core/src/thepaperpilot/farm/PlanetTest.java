package thepaperpilot.farm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class PlanetTest implements Screen{
    private final PerspectiveCamera camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    private final ModelBatch batch = new ModelBatch();
    private final Model planet;
    private final ModelInstance instance;
    private final Environment environment = new Environment();

    Texture texture;

    public PlanetTest(Color ... colors) {
        camera.position.set(10f, 10f, 10f);
        camera.lookAt(0,0,0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        generate(pixmap, .5f, 20, colors);

        ModelBuilder modelBuilder = new ModelBuilder();
        planet = modelBuilder.createSphere(10, 10, 10, 15, 15,
                new Material(TextureAttribute.createDiffuse(texture = new Texture(pixmap))),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        instance = new ModelInstance(planet);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
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

    private static int oldDensity;
    private static float[] cellPoint;

    public static void generate(final Pixmap pixmap, float regularity, int density, Color[] colors){

        int width=pixmap.getWidth();
        int height=pixmap.getHeight();

        int r, g, b;
        int a=255;
        Vector3 distVect=new Vector3();
        float dist;
        float rand1;
        float rand2;

        //
        // Create random cell point, create random cache where necessary
        //
        if (oldDensity != density || cellPoint == null){
            cellPoint=new float[density * density * 2 + 4];
            for (int y=0; y < density; y++){
                for (int x=0; x < density; x++){
                    rand1= MathUtils.random();
                    rand2=MathUtils.random();
                    cellPoint[(x + y * density) * 2]=(x + 0.5f + (rand1 - 0.5f) * (1 - regularity)) / density - 1.f / width;
                    cellPoint[(x + y * density) * 2 + 1]=(y + 0.5f + (rand2 - 0.5f) * (1 - regularity)) / density - 1.f / height;
                }
            }
        }

        oldDensity=density;

        Color[][] cells = new Color[density][density];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j] = colors[MathUtils.random(colors.length - 1)];
            }
        }
        for (int y=0; y < height; y++){
            for (int x=0; x < width; x++){

                float pixelPosX=(float) x / width;
                float pixelPosY=(float) y / height;

                float minDist=10;
                float nextMinDist=minDist;
                int xo=x * density / width;
                int yo=y * density / height;

                for (int v=-1; v < 2; ++v){
                    int vo=((yo + density + v) % density) * density;
                    for (int u=-1; u < 2; ++u){
                        float cellPosX=cellPoint[((((xo + density + u)) % density) + vo) * 2];
                        float cellPosY=cellPoint[((((xo + density + u)) % density) + vo) * 2 + 1];

                        if (u == -1 && x * density < width){
                            cellPosX-=1;
                        }
                        if (v == -1 && y * density < height){
                            cellPosY-=1;
                        }
                        if (u == 1 && x * density >= width * (density - 1)){
                            cellPosX+=1;
                        }
                        if (v == 1 && y * density >= height * (density - 1)){
                            cellPosY+=1;
                        }

                        dist=distVect.set(pixelPosX, pixelPosY, 0).dst(cellPosX, cellPosY, 0);

                        if (dist < minDist){
                            nextMinDist=minDist / 2;
                            minDist=dist;
                        }
                        if (dist < nextMinDist){
                            nextMinDist=dist;
                        }
                    }
                }

                minDist=1 - minDist * density;

                if (minDist < 0){
                    minDist=0;
                }
                if (minDist > 1){
                    minDist=1;
                }

                //
                // Draw pixel
                //
                Color color = cells[xo][yo];
                r=(int) (minDist * color.r * 255);
                g=(int) (minDist * color.g * 255);
                b=(int) (minDist * color.b * 255);

                pixmap.drawPixel(x, y, (r << 24) | (g << 16) | (b << 8) | a);

            }
        }
    }
}
