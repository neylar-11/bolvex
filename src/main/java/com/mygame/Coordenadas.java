package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Sphere;

import java.util.ArrayList;

public class Coordenadas implements ActionListener {

    private SimpleApplication app;

    // DEBUG ACTIVADO O DESACTIVADO
    private boolean modoDebug = false;

    // LISTA DE PUNTOS
    private ArrayList<Vector2f> puntos = new ArrayList<>();

    public Coordenadas(SimpleApplication app) {

        this.app = app;

        // TECLA C -> ACTIVAR DEBUG
        app.getInputManager().addMapping(
                "ModoDebug",
                new KeyTrigger(KeyInput.KEY_C)
        );

        // CLICK IZQUIERDO
        app.getInputManager().addMapping(
                "Click",
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT)
        );

        // LISTENER
        app.getInputManager().addListener(
                this,
                "ModoDebug",
                "Click"
        );
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

        // SOLO AL SOLTAR
        if (isPressed) {
            return;
        }

        // ACTIVAR / DESACTIVAR DEBUG
        if (name.equals("ModoDebug")) {

            modoDebug = !modoDebug;

            System.out.println(
                    "Modo Debug: " + modoDebug
            );
        }

        // CLICK SOLO SI DEBUG ESTÁ ACTIVO
        if (name.equals("Click") && modoDebug) {

            // POSICIÓN DEL MOUSE
            Vector2f mousePos =
                    app.getInputManager().getCursorPosition();

            // GUARDAR PUNTO
            puntos.add(mousePos);

            // MOSTRAR EN CONSOLA
            System.out.println(
                    "PUNTO " + puntos.size()
                    + " -> X: " + mousePos.x
                    + " | Y: " + mousePos.y
            );

            // DIBUJAR PUNTO
            dibujarPunto(mousePos);

            // SI YA HAY MÁS DE 1 PUNTO
            // DIBUJAR LÍNEA
            if (puntos.size() > 1) {

                Vector2f anterior =
                        puntos.get(puntos.size() - 2);

                dibujarLinea(anterior, mousePos);
            }
        }
    }

    // =========================
    // DIBUJAR PUNTO
    // =========================
    private void dibujarPunto(Vector2f pos) {

        Sphere esfera = new Sphere(8, 8, 6);

        Geometry punto =
                new Geometry("PuntoDebug", esfera);

        Material mat = new Material(
                app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );

        mat.setColor("Color", ColorRGBA.Red);

        punto.setMaterial(mat);

        // POSICIÓN
        punto.setLocalTranslation(
                new Vector3f(pos.x, pos.y, 5)
        );

        // AGREGAR AL GUI
        app.getGuiNode().attachChild(punto);
    }

    // =========================
    // DIBUJAR LÍNEA
    // =========================
    private void dibujarLinea(Vector2f p1, Vector2f p2) {

        // CREAR MESH MANUALMENTE
        Mesh mesh = new Mesh();
        mesh.setMode(Mesh.Mode.Lines);

        // BUFFER DE POSICIONES (2 vértices)
        mesh.setBuffer(
                VertexBuffer.Type.Position,
                3,
                new float[]{
                        p1.x, p1.y, 4f,
                        p2.x, p2.y, 4f
                }
        );

        // BUFFER DE ÍNDICES
        mesh.setBuffer(
                VertexBuffer.Type.Index,
                2,
                new short[]{ 0, 1 }
        );

        mesh.updateBound();
        mesh.updateCounts();

        Geometry geoLinea =
                new Geometry("LineaDebug", mesh);

        Material mat = new Material(
                app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );

        mat.setColor("Color", ColorRGBA.Green);

        // GROSOR EN EL RENDER STATE, NO EN EL MESH
        mat.getAdditionalRenderState().setLineWidth(3f);

        geoLinea.setMaterial(mat);

        // AGREGAR AL GUI
        app.getGuiNode().attachChild(geoLinea);
    }
}