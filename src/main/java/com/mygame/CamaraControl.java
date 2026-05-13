package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class CamaraControl implements ActionListener {

    private SimpleApplication app;
    private Mapa1State mapaState;

    private final float VELOCIDAD = 300f;

    private boolean moverIzquierda = false;
    private boolean moverDerecha   = false;

    private float camaraX = 0f;

    private boolean modoDebugCamara = false;

    public CamaraControl(SimpleApplication app, Mapa1State mapaState) {
        this.app       = app;
        this.mapaState = mapaState;

        app.getInputManager().addMapping("CamaraIzquierda",
                new KeyTrigger(KeyInput.KEY_J));
        app.getInputManager().addMapping("CamaraDerecha",
                new KeyTrigger(KeyInput.KEY_K));
        app.getInputManager().addMapping("ToggleDebugCamara",
                new KeyTrigger(KeyInput.KEY_L));

        app.getInputManager().addListener(this,
                "CamaraIzquierda", "CamaraDerecha", "ToggleDebugCamara");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

        if (name.equals("ToggleDebugCamara") && !isPressed) {
            modoDebugCamara = !modoDebugCamara;
            if (modoDebugCamara) {
                camaraX = mapaState.getOffsetX();
            }
            System.out.println("══ Cámara debug: "
                    + (modoDebugCamara
                        ? "ON  — flechas mueven cámara libre (L para salir)"
                        : "OFF — cámara sigue a Nyx") + " ══");
        }

        // Solo registra flechas si está en modo debug
        if (modoDebugCamara) {
            if (name.equals("CamaraIzquierda")) moverIzquierda = isPressed;
            if (name.equals("CamaraDerecha"))   moverDerecha   = isPressed;
        } else {
            // Al salir del modo debug limpia el estado para que no queden presionadas
            moverIzquierda = false;
            moverDerecha   = false;
        }
    }

    public void update(float tpf) {
        if (!modoDebugCamara) return;

        if (moverIzquierda) camaraX -= VELOCIDAD * tpf;
        if (moverDerecha)   camaraX += VELOCIDAD * tpf;

        mapaState.moverCamaraOffset(camaraX);
    }

    public void seguirJugador(float centroXJugador) {
        if (modoDebugCamara) return;
        mapaState.moverCamara(centroXJugador);
        camaraX = mapaState.getOffsetX();
    }

    public float getCamaraX() {
        return mapaState.getOffsetX();
    }

    // El jugador consulta esto para saber si debe ignorar su input
    public boolean isModoDebug() {
        return modoDebugCamara;
    }
}