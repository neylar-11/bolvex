package com.mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import java.util.ArrayList;

public class Mapa1State extends BaseAppState {

    private SimpleApplication app;

    // =========================
    // MAPA PRINCIPAL
    // =========================
    private Geometry geoMapa;
    private final float IMG_ANCHO_ORIGINAL = 3584f;
    private final float IMG_ALTO_ORIGINAL  = 240f;

    // =========================
    // FONDO TILEADO
    // =========================
    private ArrayList<Geometry> tilesFondo = new ArrayList<>();
    private final float BG_ANCHO_ORIGINAL = 512f;
    private final float BG_ALTO_ORIGINAL  = 240f;
    private float bgAncho;
    private float bgAlto;

    // QUÉ TAN LENTO SE MUEVE EL FONDO
    private final float PARALLAX = 0.3f;

    // ZOOM
    private final float ZOOM = .9f;

    // TAMAÑO FINAL DEL MAPA
    private float anchoFinal;
    private float altoFinal;

    // POSICIÓN X DE LA CÁMARA
    private float camaraX = 0f;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;

        Camera cam = this.app.getCamera();
        float pantallaAncho = cam.getWidth();
        float pantallaAlto  = cam.getHeight();

        float escalaBase = pantallaAlto / IMG_ALTO_ORIGINAL;

        altoFinal  = IMG_ALTO_ORIGINAL  * escalaBase * ZOOM;
        anchoFinal = IMG_ANCHO_ORIGINAL * escalaBase * ZOOM;
        bgAncho    = BG_ANCHO_ORIGINAL  * escalaBase * ZOOM;
        bgAlto     = BG_ALTO_ORIGINAL   * escalaBase * ZOOM;

        // =========================
        // CÁMARA ORTOGRÁFICA
        // =========================
        cam.setParallelProjection(true);
        cam.setFrustum(
                -1000f, 1000f,
                -pantallaAncho / 2f,
                 pantallaAncho / 2f,
                 pantallaAlto  / 2f,
                -pantallaAlto  / 2f
        );
        cam.setLocation(new Vector3f(
                pantallaAncho / 2f,
                pantallaAlto  / 2f,
                500f
        ));
        this.app.getFlyByCamera().setEnabled(false);

        // =========================
        // FONDO TILEADO  (Z = 0)
        // =========================
        int cantidadTiles = (int) Math.ceil(anchoFinal / bgAncho) + 2;
        float posYFondo   = (pantallaAlto - bgAlto) / 2f;

        for (int i = 0; i < cantidadTiles; i++) {

            Quad quad = new Quad(bgAncho, bgAlto);
            Geometry tile = new Geometry("FondoTile_" + i, quad);

            Material mat = new Material(
                    app.getAssetManager(),
                    "Common/MatDefs/Misc/Unshaded.j3md"
            );
            Texture tex = app.getAssetManager()
                    .loadTexture("Scenes/Fondo Mapa1.3.png");
            mat.setTexture("ColorMap", tex);

            tile.setMaterial(mat);
            tile.setLocalTranslation(i * bgAncho, posYFondo, 0f);

            tilesFondo.add(tile);
        }

        // =========================
        // MAPA PRINCIPAL  (Z = 1)
        // CON TRANSPARENCIA ALPHA
        // para que se vea el fondo
        // a través de las zonas
        // transparentes del PNG
        // =========================
        Quad quadMapa = new Quad(anchoFinal, altoFinal);
        geoMapa = new Geometry("Mapa1", quadMapa);

        Material matMapa = new Material(
                app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        Texture texMapa = app.getAssetManager()
                .loadTexture("Interface/Mapa1.1.png");
        matMapa.setTexture("ColorMap", texMapa);

        // ACTIVAR TRANSPARENCIA ALPHA
        matMapa.getAdditionalRenderState()
               .setBlendMode(RenderState.BlendMode.Alpha);

        geoMapa.setMaterial(matMapa);

        // BUCKET TRANSPARENTE para que JME
        // respete el orden de renderizado
        geoMapa.setQueueBucket(RenderQueue.Bucket.Transparent);

        float posYMapa = (pantallaAlto - altoFinal) / 2f;
        geoMapa.setLocalTranslation(0f, posYMapa, 1f);

        System.out.println("Mapa cargado | Zoom: x" + ZOOM);
        System.out.println("Tiles de fondo: " + cantidadTiles);
    }

    // =========================
    // MOVER LA CÁMARA
    // =========================
    public void moverCamara(float xJugador) {

        Camera cam = app.getCamera();
        float pantallaAncho = cam.getWidth();
        float pantallaAlto  = cam.getHeight();

        camaraX = xJugador - (pantallaAncho / 2f);
        camaraX = Math.max(0, camaraX);
        camaraX = Math.min(anchoFinal - pantallaAncho, camaraX);

        // MOVER CÁMARA
        cam.setLocation(new Vector3f(
                camaraX + pantallaAncho / 2f,
                pantallaAlto / 2f,
                500f
        ));

        // MOVER FONDO MÁS LENTO (parallax)
        float posYFondo = (pantallaAlto - bgAlto) / 2f;

        for (int i = 0; i < tilesFondo.size(); i++) {
            tilesFondo.get(i).setLocalTranslation(
                    (i * bgAncho) + (camaraX * PARALLAX),
                    posYFondo,
                    0f
            );
        }
    }

    @Override
    protected void onEnable() {
        for (Geometry tile : tilesFondo) {
            app.getRootNode().attachChild(tile);
        }
        app.getRootNode().attachChild(geoMapa);
    }

    @Override
    protected void onDisable() {
        geoMapa.removeFromParent();
        for (Geometry tile : tilesFondo) {
            tile.removeFromParent();
        }
    }

    @Override
    protected void cleanup(Application app) {
        tilesFondo.clear();
    }
}