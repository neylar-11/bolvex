package com.mygame;

import com.jme3.app.SimpleApplication;

public class Main extends SimpleApplication {

    private Mapa1State    mapaState;
    private CamaraControl camaraControl;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        setDisplayStatView(false);
        setDisplayFps(false);

        // MAPA
        mapaState = new Mapa1State();
        stateManager.attach(mapaState);

        // CÁMARA (va antes que Coordenadas
        // porque Coordenadas la necesita)
        camaraControl = new CamaraControl(this, mapaState);

        // DEBUG: ahora recibe camaraControl
        // para que las coords sean del mundo
        new Coordenadas(this, camaraControl);

        // MENÚ
        stateManager.attach(new MenuState());
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (camaraControl != null) {
            camaraControl.update(tpf);
        }
    }
}