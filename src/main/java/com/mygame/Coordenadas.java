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
 
    // GROSOR DE LA LÍNEA EN PÍXELES
    private final float GROSOR = 3f;
 
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
        if (isPressed) return;
 
        // ACTIVAR / DESACTIVAR DEBUG
        if (name.equals("ModoDebug")) {
            modoDebug = !modoDebug;
            System.out.println("Modo Debug: " + modoDebug);
        }
 
        // CLICK SOLO SI DEBUG ESTÁ ACTIVO
        if (name.equals("Click") && modoDebug) {
 
            Vector2f mousePos =
                    app.getInputManager().getCursorPosition();
 
            puntos.add(mousePos);
 
            System.out.println(
                    "PUNTO " + puntos.size()
                    + " -> X: " + mousePos.x
                    + " | Y: " + mousePos.y
            );
 
            dibujarPunto(mousePos);
 
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
        Geometry punto = new Geometry("PuntoDebug", esfera);
 
        Material mat = new Material(
                app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        mat.setColor("Color", ColorRGBA.Red);
        punto.setMaterial(mat);
        punto.setLocalTranslation(new Vector3f(pos.x, pos.y, 5));
 
        app.getGuiNode().attachChild(punto);
    }
 
    // =========================
    // DIBUJAR LÍNEA COMO QUAD
    // Las líneas nativas de OpenGL
    // no funcionan en core profile,
    // así que usamos dos triángulos
    // =========================
    private void dibujarLinea(Vector2f p1, Vector2f p2) {
 
        // VECTOR DIRECCIÓN
        float dx  = p2.x - p1.x;
        float dy  = p2.y - p1.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
 
        if (len == 0) return;
 
        // NORMALIZAR
        float nx = dx / len;
        float ny = dy / len;
 
        // PERPENDICULAR (para el grosor)
        float px = -ny * (GROSOR / 2f);
        float py =  nx * (GROSOR / 2f);
 
        // 4 VÉRTICES DEL QUAD
        //
        //  v0 -------- v3
        //  |            |
        //  v1 -------- v2
        //
        float[] vertices = {
            p1.x + px,  p1.y + py,  4f,   // v0
            p1.x - px,  p1.y - py,  4f,   // v1
            p2.x - px,  p2.y - py,  4f,   // v2
            p2.x + px,  p2.y + py,  4f    // v3
        };
 
        // DOS TRIÁNGULOS
        short[] indices = { 0, 1, 2,  0, 2, 3 };
 
        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
        mesh.setBuffer(VertexBuffer.Type.Index,    3, indices);
        mesh.updateBound();
        mesh.updateCounts();
 
        Geometry geoLinea = new Geometry("LineaDebug", mesh);
 
        Material mat = new Material(
                app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        mat.setColor("Color", ColorRGBA.Green);
        geoLinea.setMaterial(mat);
 
        app.getGuiNode().attachChild(geoLinea);
    }
}