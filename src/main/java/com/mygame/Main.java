package com.mygame;

import com.jme3.app.SimpleApplication;

public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Configuraciones básicas de visualización
        setDisplayStatView(false);
        setDisplayFps(false);

        // Iniciamos directamente en el menú
        stateManager.attach(new MenuState());
    }
}