package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class CamaraControl implements ActionListener {

    private SimpleApplication app;
    private Mapa1State mapaState;

    // VELOCIDAD DE MOVIMIENTO (píxeles por segundo)
    private final float VELOCIDAD = 300f;

    // TECLAS PRESIONADAS
    private boolean moverIzquierda = false;
    private boolean moverDerecha   = false;

    // POSICIÓN ACTUAL DE LA CÁMARA
    private float camaraX = 0f;

    public CamaraControl(SimpleApplication app, Mapa1State mapaState) {
        this.app       = app;
        this.mapaState = mapaState;

        // REGISTRAR TECLAS
        app.getInputManager().addMapping(
                "CamaraIzquierda",
                new KeyTrigger(KeyInput.KEY_LEFT),
                new KeyTrigger(KeyInput.KEY_A)
        );
        app.getInputManager().addMapping(
                "CamaraDerecha",
                new KeyTrigger(KeyInput.KEY_RIGHT),
                new KeyTrigger(KeyInput.KEY_D)
        );

        app.getInputManager().addListener(
                this,
                "CamaraIzquierda",
                "CamaraDerecha"
        );
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

        if (name.equals("CamaraIzquierda")) {
            moverIzquierda = isPressed;
        }
        if (name.equals("CamaraDerecha")) {
            moverDerecha = isPressed;
        }
    }

    // =========================
    // LLAMAR EN simpleUpdate
    // =========================
    public void update(float tpf) {

        if (moverIzquierda) {
            camaraX -= VELOCIDAD * tpf;
        }
        if (moverDerecha) {
            camaraX += VELOCIDAD * tpf;
        }

        // MOVER CÁMARA Y PARALLAX
        mapaState.moverCamara(camaraX);
    }

    public float getCamaraX() {
        return camaraX;
    }
}
