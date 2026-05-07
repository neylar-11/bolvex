package com.mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.renderer.Camera;
import com.jme3.ui.Picture;

public class Mapa1State extends BaseAppState {

    private SimpleApplication app;
    private Picture fondoMapa;

    // TAMAÑO ORIGINAL DE LA IMAGEN
    private final float IMG_ANCHO_ORIGINAL = 3584f;
    private final float IMG_ALTO_ORIGINAL  = 240f;

    // =========================
    // AJUSTA ESTE VALOR:
    //   1.0 = sin zoom
    //   2.0 = doble de tamaño
    //   3.0 = triple, etc.
    // =========================
    private final float ZOOM = .9f;

    // TAMAÑO FINAL CON ZOOM
    private float anchoFinal;
    private float altoFinal;

    // X DEL JUGADOR
    private float jugadorX = 0f;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;

        Camera cam = this.app.getCamera();
        float pantallaAlto = cam.getHeight();

        // =========================
        // PRIMERO SE ESCALA PARA
        // LLENAR EL ALTO DE PANTALLA
        // LUEGO SE APLICA EL ZOOM
        // =========================
        float escalaBase = pantallaAlto / IMG_ALTO_ORIGINAL;

        altoFinal  = IMG_ALTO_ORIGINAL * escalaBase * ZOOM;
        anchoFinal = IMG_ANCHO_ORIGINAL * escalaBase * ZOOM;

        // =========================
        // MAPA
        // =========================
        fondoMapa = new Picture("Mapa1");
        fondoMapa.setImage(
                app.getAssetManager(),
                "Interface/Mapa1.png",
                true
        );
        fondoMapa.setWidth(anchoFinal);
        fondoMapa.setHeight(altoFinal);
        fondoMapa.setPosition(0, 0);

        this.app.getFlyByCamera().setEnabled(false);

        System.out.println("Mapa cargado");
        System.out.println("Zoom aplicado: x" + ZOOM);
        System.out.println("Tamaño final: " + anchoFinal + " x " + altoFinal);
    }

    // =========================
    // LLAMAR CADA FRAME CON
    // LA X DEL JUGADOR
    // =========================
    public void actualizarCamara(float xJugador) {
        this.jugadorX = xJugador;
    }

    @Override
    public void update(float tpf) {

        Camera cam = app.getCamera();
        float pantallaAncho = cam.getWidth();
        float pantallaAlto  = cam.getHeight();

        // SCROLL HORIZONTAL centrado en el jugador
        float offsetX = jugadorX - (pantallaAncho / 2f);
        offsetX = Math.max(0, offsetX);
        offsetX = Math.min(anchoFinal - pantallaAncho, offsetX);

        // CENTRAR VERTICALMENTE el mapa en pantalla
        // para que no quede pegado abajo
        float offsetY = (altoFinal - pantallaAlto) / 2f;

        fondoMapa.setPosition(-offsetX, -offsetY);
    }

    @Override
    protected void onEnable() {
        app.getGuiNode().attachChild(fondoMapa);
    }

    @Override
    protected void onDisable() {
        fondoMapa.removeFromParent();
    }

    @Override
    protected void cleanup(Application app) {
    }
}