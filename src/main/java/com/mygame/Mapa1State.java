package com.mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.renderer.Camera;
import com.jme3.ui.Picture;

import java.util.ArrayList;

public class Mapa1State extends BaseAppState {

    private SimpleApplication app;

    // =========================
    // MAPA PRINCIPAL
    // =========================
    private Picture fondoMapa;
    private final float IMG_ANCHO_ORIGINAL = 3584f;
    private final float IMG_ALTO_ORIGINAL  = 240f;

    // =========================
    // FONDO TILEADO
    // =========================
    private ArrayList<Picture> tilesFondo = new ArrayList<>();
    private final float BG_ANCHO_ORIGINAL = 512f;
    private final float BG_ALTO_ORIGINAL  = 240f;

    // =========================
    // ZOOM
    // =========================
    private final float ZOOM = .9f;

    // TAMAÑO FINAL
    private float anchoFinal;
    private float altoFinal;

    // X DEL JUGADOR
    private float jugadorX = 0f;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;

        Camera cam = this.app.getCamera();
        float pantallaAlto = cam.getHeight();

        float escalaBase = pantallaAlto / IMG_ALTO_ORIGINAL;

        altoFinal  = IMG_ALTO_ORIGINAL  * escalaBase * ZOOM;
        anchoFinal = IMG_ANCHO_ORIGINAL * escalaBase * ZOOM;

        // TAMAÑO DE CADA TILE DE FONDO
        // (misma escala y zoom que el mapa)
        float bgAncho = BG_ANCHO_ORIGINAL * escalaBase * ZOOM;
        float bgAlto  = BG_ALTO_ORIGINAL  * escalaBase * ZOOM;

        // =========================
        // CALCULAR CUÁNTOS TILES
        // SE NECESITAN PARA CUBRIR
        // TODO EL ANCHO DEL MAPA
        // =========================
        int cantidadTiles = (int) Math.ceil(anchoFinal / bgAncho) + 1;

        for (int i = 0; i < cantidadTiles; i++) {

            Picture tile = new Picture("FondoTile_" + i);
            tile.setImage(
                    app.getAssetManager(),
                    "Scenes/Fondo Mapa1.3.png",
                    true
            );
            tile.setWidth(bgAncho);
            tile.setHeight(bgAlto);

            // ACOMODAR EN FILA HORIZONTAL
            // CENTRADO VERTICAL igual que el mapa
            float offsetY = (bgAlto - pantallaAlto) / 2f;
            tile.setPosition(i * bgAncho, -offsetY);

            tilesFondo.add(tile);
        }

        // =========================
        // MAPA PRINCIPAL
        // =========================
        fondoMapa = new Picture("Mapa1");
        fondoMapa.setImage(
                app.getAssetManager(),
                "Interface/Mapa1.1.png",
                true
        );
        fondoMapa.setWidth(anchoFinal);
        fondoMapa.setHeight(altoFinal);
        fondoMapa.setPosition(0, 0);

        this.app.getFlyByCamera().setEnabled(false);

        System.out.println("Mapa cargado | Zoom: x" + ZOOM);
        System.out.println("Tiles de fondo: " + cantidadTiles);
    }

    public void actualizarCamara(float xJugador) {
        this.jugadorX = xJugador;
    }

    @Override
    public void update(float tpf) {

        Camera cam = app.getCamera();
        float pantallaAncho = cam.getWidth();
        float pantallaAlto  = cam.getHeight();

        // SCROLL DEL MAPA
        float offsetX = jugadorX - (pantallaAncho / 2f);
        offsetX = Math.max(0, offsetX);
        offsetX = Math.min(anchoFinal - pantallaAncho, offsetX);

        float offsetY = (altoFinal - pantallaAlto) / 2f;

        fondoMapa.setPosition(-offsetX, -offsetY);

        // EL FONDO NO SE MUEVE (posición fija)
    }

    @Override
    protected void onEnable() {

        // PRIMERO EL FONDO (queda detrás del mapa)
        for (Picture tile : tilesFondo) {
            app.getGuiNode().attachChild(tile);
        }

        // LUEGO EL MAPA (queda encima)
        app.getGuiNode().attachChild(fondoMapa);
    }

    @Override
    protected void onDisable() {
        fondoMapa.removeFromParent();
        for (Picture tile : tilesFondo) {
            tile.removeFromParent();
        }
    }

    @Override
    protected void cleanup(Application app) {
        tilesFondo.clear();
    }
}