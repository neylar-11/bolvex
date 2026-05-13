package com.mygame;

import com.jme3.app.SimpleApplication;

public class Main extends SimpleApplication {

    private Mapa1State    mapaState;
    private CamaraControl camaraControl;
    private ColisionMapa  colisionMapa;
    private Jugador       jugador;

    private boolean mapaListo     = false;
    private boolean juegoIniciado = false;

    public static void main(String[] args) {
        new Main().start();
    }

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);

        mapaState = new Mapa1State();
        stateManager.attach(mapaState);
        stateManager.attach(new MenuState(this));
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!mapaListo && mapaState.isInitialized()) {
            mapaListo = true;
        }

        if (juegoIniciado) {
            if (camaraControl != null) camaraControl.update(tpf);
            if (jugador       != null) jugador.update(tpf);
        }
    }

    public void iniciarJuego() {
        if (juegoIniciado) return;

        colisionMapa  = new ColisionMapa(mapaState.getSegmentosColision());
        camaraControl = new CamaraControl(this, mapaState);
        jugador       = new Jugador(this, colisionMapa, camaraControl, mapaState);
        new Coordenadas(this, camaraControl);

        // ── CONECTAR ITEM: esto activa el primer bloque y la detección de toque ──
        mapaState.setColisionMapa(colisionMapa);
        mapaState.setJugador(jugador);

        juegoIniciado = true;
        System.out.println("Juego iniciado");
    }

    public ColisionMapa  getColisionMapa()  { return colisionMapa;  }
    public CamaraControl getCamaraControl() { return camaraControl; }
    public Jugador       getJugador()       { return jugador;       }
}