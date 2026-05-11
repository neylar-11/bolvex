package com.mygame;

import com.jme3.app.SimpleApplication;

public class Main extends SimpleApplication {

    private Mapa1State    mapaState;
    private CamaraControl camaraControl;
    private ColisionMapa  colisionMapa;  // NUEVO
    private Jugador       jugador;       // NUEVO

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);

        // MAPA (render + colisiones hardcodeadas)
        mapaState = new Mapa1State();
        stateManager.attach(mapaState);

        // COLISIONES — lee los segmentos del mapa
        colisionMapa = new ColisionMapa(mapaState.getSegmentosColision());

        // CÁMARA (va antes que Coordenadas y Jugador)
        camaraControl = new CamaraControl(this, mapaState);

        // JUGADOR
        jugador = new Jugador(this, colisionMapa, camaraControl);

        // DEBUG de coordenadas (quitar en producción)
        new Coordenadas(this, camaraControl);

        // MENÚ
        stateManager.attach(new MenuState());
    }

    @Override
    public void simpleUpdate(float tpf) {
        // update de cámara: solo actúa si está en modo manual
        if (camaraControl != null) {
            camaraControl.update(tpf);
        }
        // update del jugador: mueve a Nyx y arrastra la cámara
        if (jugador != null) {
            jugador.update(tpf);
        }
    }

    // Getters por si otras clases los necesitan
    public ColisionMapa  getColisionMapa()  { return colisionMapa; }
    public CamaraControl getCamaraControl() { return camaraControl; }
    public Jugador       getJugador()       { return jugador; }
}