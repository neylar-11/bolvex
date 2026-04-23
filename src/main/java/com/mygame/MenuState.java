package com.mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.ui.Picture;

public class MenuState extends BaseAppState {
    private SimpleApplication app;
    private Picture background;
    private final Vector2f centroBotonPlay = new Vector2f(771f, 359f);
    private final float radioBoton = 125f;

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Click") && !isPressed) {
                Vector2f cursor = app.getInputManager().getCursorPosition();
                
                // Cálculo de distancia para colisión circular
                if (cursor.distance(centroBotonPlay) <= radioBoton) {
                    System.out.println("Cargando Niveles...");
                    irANiveles();
                }
            }
        }
    };

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        
        background = new Picture("MenuBackground");
        background.setImage(app.getAssetManager(), "Interface/imagen menú.jpeg", true);
        background.setWidth(app.getContext().getSettings().getWidth());
        background.setHeight(app.getContext().getSettings().getHeight());
        background.setPosition(0, 0);
    }

    @Override
    protected void onEnable() {
        app.getGuiNode().attachChild(background);
        app.getInputManager().setCursorVisible(true);
        app.getFlyByCamera().setEnabled(false);
        
        app.getInputManager().addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addListener(actionListener, "Click");
    }

    @Override
    protected void onDisable() {
        background.removeFromParent();
        app.getInputManager().removeListener(actionListener);
    }

    private void irANiveles() {
        // Quitamos el menú y ponemos la pantalla de niveles
        getStateManager().detach(this);
        getStateManager().attach(new NivelesState());
    }

    @Override protected void cleanup(Application app) {}
}