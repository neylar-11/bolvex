package com.mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.ui.Picture;

import java.awt.Polygon;

public class NivelesState extends BaseAppState implements ActionListener {

    private SimpleApplication app;
    private Picture backgroundNiveles;

    // POLÍGONO NIVEL 1
    private Polygon nivel1;

    @Override
    protected void initialize(Application app) {

        this.app = (SimpleApplication) app;

        backgroundNiveles = new Picture("NivelesBackground");

        backgroundNiveles.setImage(
                app.getAssetManager(),
                "Interface/imagen niveles.jpeg",
                true
        );

        backgroundNiveles.setWidth(
                app.getContext().getSettings().getWidth()
        );

        backgroundNiveles.setHeight(
                app.getContext().getSettings().getHeight()
        );

        backgroundNiveles.setPosition(0, 0);

        // =========================
        // CREAR POLÍGONO NIVEL 1
        // =========================
        nivel1 = new Polygon();

        nivel1.addPoint(339, 834);
        nivel1.addPoint(362, 840);
        nivel1.addPoint(390, 840);
        nivel1.addPoint(415, 822);
        nivel1.addPoint(427, 804);
        nivel1.addPoint(434, 768);
        nivel1.addPoint(437, 752);
        nivel1.addPoint(449, 741);
        nivel1.addPoint(455, 724);
        nivel1.addPoint(454, 701);
        nivel1.addPoint(446, 687);
        nivel1.addPoint(431, 675);
        nivel1.addPoint(410, 674);
        nivel1.addPoint(392, 685);
        nivel1.addPoint(374, 689);
        nivel1.addPoint(354, 693);
        nivel1.addPoint(337, 702);
        nivel1.addPoint(323, 714);
        nivel1.addPoint(314, 730);
        nivel1.addPoint(306, 748);
        nivel1.addPoint(306, 771);
        nivel1.addPoint(307, 788);
        nivel1.addPoint(318, 806);
        nivel1.addPoint(328, 823);

        // CLICK
        this.app.getInputManager().addMapping(
                "ClickNivel",
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT)
        );

        this.app.getInputManager().addListener(
                this,
                "ClickNivel"
        );
    }

    @Override
    protected void onEnable() {

        app.getGuiNode().attachChild(backgroundNiveles);

        System.out.println(
                "Estás en la selección de niveles."
        );
    }

    @Override
    protected void onDisable() {

        backgroundNiveles.removeFromParent();
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    public void onAction(
            String name,
            boolean isPressed,
            float tpf
    ) {

        if (isPressed) {
            return;
        }

        if (name.equals("ClickNivel")) {

            Vector2f mouse =
                    app.getInputManager()
                            .getCursorPosition();

            // =========================
            // CLICK NIVEL 1
            // =========================
            if (nivel1.contains(mouse.x, mouse.y)) {

                System.out.println("CLICK NIVEL 1");

                getStateManager().detach(this);
                getStateManager().attach(new Mapa1State());
            }
                

                // AQUÍ CAMBIAS DE NIVEL
            
        }
    }
}