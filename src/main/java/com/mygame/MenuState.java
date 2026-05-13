package com.mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.ui.Picture;

public class MenuState extends BaseAppState {

    private SimpleApplication app;
    private Main              main;   // ← referencia para llamar iniciarJuego()
    private Picture           background;

    private final Vector2f centroBotonPlay = new Vector2f(771f, 359f);
    private final float    radioBoton      = 125f;

    // Nombre único para no chocar con el "Click" de Coordenadas
    private static final String MAPPING_CLICK_MENU = "MenuClick";

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(MAPPING_CLICK_MENU) && !isPressed) {
                Vector2f cursor = app.getInputManager().getCursorPosition();
                if (cursor.distance(centroBotonPlay) <= radioBoton) {
                    System.out.println("Cargando Niveles...");
                    irANiveles();
                }
            }
        }
    };

    public MenuState(Main main) {
        this.main = main;
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;

        background = new Picture("MenuBackground");
        background.setImage(app.getAssetManager(),
                "Interface/imagen menú.jpeg", true);
        background.setWidth(app.getContext().getSettings().getWidth());
        background.setHeight(app.getContext().getSettings().getHeight());
        background.setPosition(0, 0);
    }

    @Override
    protected void onEnable() {
        app.getGuiNode().attachChild(background);
        app.getInputManager().setCursorVisible(true);
        app.getFlyByCamera().setEnabled(false);

        // Mapping con nombre único — no choca con Coordenadas
        app.getInputManager().addMapping(MAPPING_CLICK_MENU,
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addListener(actionListener, MAPPING_CLICK_MENU);
    }

    @Override
    protected void onDisable() {
        background.removeFromParent();
        app.getInputManager().removeListener(actionListener);
        // ← Bug 2 corregido: eliminar el mapping al salir
        if (app.getInputManager().hasMapping(MAPPING_CLICK_MENU)) {
            app.getInputManager().deleteMapping(MAPPING_CLICK_MENU);
        }
    }

    private void irANiveles() {
        getStateManager().detach(this);
        getStateManager().attach(new NivelesState(main));  // pasa main
    }

    @Override
    protected void cleanup(Application app) {}
}