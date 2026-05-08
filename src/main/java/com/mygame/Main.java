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

        // CONFIGURACIONES BÁSICAS
        setDisplayStatView(false);
        setDisplayFps(false);

        // DEBUG GLOBAL
        new Coordenadas(this);

        // MAPA
        mapaState = new Mapa1State();
        stateManager.attach(mapaState);

        // CONTROL DE CÁMARA
        // (se crea después del mapa)
        camaraControl = new CamaraControl(this, mapaState);

        // MENÚ
        stateManager.attach(new MenuState());
    }

    @Override
    public void simpleUpdate(float tpf) {

        // MOVER CÁMARA CON FLECHAS / WASD
        if (camaraControl != null) {
            camaraControl.update(tpf);
        }
    }
}