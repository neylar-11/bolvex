package com.mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.ui.Picture;

public class NivelesState extends BaseAppState {
    private SimpleApplication app;
    private Picture backgroundNiveles;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        
        backgroundNiveles = new Picture("NivelesBackground");
        backgroundNiveles.setImage(app.getAssetManager(), "Interface/imagen niveles.jpeg", true);
        backgroundNiveles.setWidth(app.getContext().getSettings().getWidth());
        backgroundNiveles.setHeight(app.getContext().getSettings().getHeight());
        backgroundNiveles.setPosition(0, 0);
    }

    @Override
    protected void onEnable() {
        app.getGuiNode().attachChild(backgroundNiveles);
        System.out.println("Estás en la selección de niveles.");
    }

    @Override
    protected void onDisable() {
        backgroundNiveles.removeFromParent();
    }

    @Override protected void cleanup(Application app) {}
}