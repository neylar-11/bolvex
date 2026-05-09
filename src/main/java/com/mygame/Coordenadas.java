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
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Sphere;

import java.util.ArrayList;

public class Coordenadas implements ActionListener {

    private SimpleApplication app;
    private CamaraControl     camaraControl;

    private boolean modoDebug = false;
    private final float GROSOR = 4f;

    // COLORES PARA CADA SEGMENTO
    private final ColorRGBA[] COLORES_SEGMENTO = {
        ColorRGBA.Green,
        ColorRGBA.Cyan,
        ColorRGBA.Yellow,
        ColorRGBA.Magenta,
        ColorRGBA.Orange,
        ColorRGBA.White
    };

    // SEGMENTOS CERRADOS
    private ArrayList<ArrayList<Vector2f>> segmentos = new ArrayList<>();

    // SEGMENTO EN PROGRESO
    private ArrayList<Vector2f> segmentoActual = new ArrayList<>();

    // NODOS VISUALES: uno por segmento
    private ArrayList<Node> nodosSegmentos = new ArrayList<>();
    private Node nodoActual = new Node("SegmentoActual");

    public Coordenadas(SimpleApplication app, CamaraControl camaraControl) {
        this.app           = app;
        this.camaraControl = camaraControl;

        app.getRootNode().attachChild(nodoActual);

        // C      -> ACTIVAR/DESACTIVAR DEBUG
        // CLICK  -> AGREGAR PUNTO
        // ENTER  -> CERRAR SEGMENTO Y EMPEZAR NUEVO
        // Z      -> DESHACER ÚLTIMO PUNTO
        // ESC    -> CANCELAR SEGMENTO ACTUAL
        // P      -> IMPRIMIR TODOS LOS SEGMENTOS
        app.getInputManager().addMapping("ModoDebug",     new KeyTrigger(KeyInput.KEY_C));
        app.getInputManager().addMapping("Click",         new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addMapping("NuevoSegmento", new KeyTrigger(KeyInput.KEY_RETURN));
        app.getInputManager().addMapping("Deshacer",      new KeyTrigger(KeyInput.KEY_Z));
        app.getInputManager().addMapping("Cancelar",      new KeyTrigger(KeyInput.KEY_ESCAPE));
        app.getInputManager().addMapping("Imprimir",      new KeyTrigger(KeyInput.KEY_P));

        app.getInputManager().addListener(
                this,
                "ModoDebug", "Click", "NuevoSegmento",
                "Deshacer", "Cancelar", "Imprimir"
        );
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

        if (isPressed) return;

        // ---- C: ACTIVAR / DESACTIVAR ----
        if (name.equals("ModoDebug")) {
            modoDebug = !modoDebug;
            System.out.println("══ Modo Debug: " + (modoDebug ? "ON" : "OFF") + " ══");
            if (modoDebug) imprimirAyuda();
        }

        if (!modoDebug) return;

        // ---- CLICK: AGREGAR PUNTO ----
        if (name.equals("Click")) {

            Vector2f pantalla = app.getInputManager().getCursorPosition();

            // =================================================
            // FIX: getCamaraX() ahora devuelve el offsetX real
            // ya clampeado desde Mapa1State.getOffsetX()
            // mundoX = screenX + offsetX (borde izq. de cámara)
            // =================================================
            float offsetX = camaraControl.getCamaraX();

            Vector2f mundo = new Vector2f(
                    pantalla.x + offsetX,
                    pantalla.y
            );

            segmentoActual.add(mundo);

            System.out.println(
                    "  [Seg " + (segmentos.size() + 1) + "]"
                    + "  Punto " + segmentoActual.size()
                    + "  ->  X: " + (int) mundo.x
                    + "  |  Y: " + (int) mundo.y
            );

            ColorRGBA color = colorActual();
            dibujarPunto(mundo, color, nodoActual);

            if (segmentoActual.size() > 1) {
                dibujarLinea(
                        segmentoActual.get(segmentoActual.size() - 2),
                        mundo,
                        color,
                        nodoActual
                );
            }
        }

        // ---- ENTER: CERRAR SEGMENTO ----
        if (name.equals("NuevoSegmento")) {

            if (segmentoActual.isEmpty()) {
                System.out.println("  [!] El segmento está vacío");
                return;
            }

            segmentos.add(new ArrayList<>(segmentoActual));
            nodosSegmentos.add(nodoActual);

            System.out.println(
                    "  ✔ Segmento " + segmentos.size()
                    + " cerrado  (" + segmentoActual.size() + " puntos)"
            );

            segmentoActual.clear();
            nodoActual = new Node("Seg_" + (segmentos.size() + 1));
            app.getRootNode().attachChild(nodoActual);

            System.out.println("  → Nuevo segmento listo");
        }

        // ---- Z: DESHACER ÚLTIMO PUNTO ----
        if (name.equals("Deshacer")) {

            if (segmentoActual.isEmpty()) {
                System.out.println("  [!] No hay puntos que deshacer");
                return;
            }

            segmentoActual.remove(segmentoActual.size() - 1);

            // REDIBUJAR EL SEGMENTO ACTUAL DESDE CERO
            nodoActual.detachAllChildren();
            ColorRGBA color = colorActual();

            for (int i = 0; i < segmentoActual.size(); i++) {
                dibujarPunto(segmentoActual.get(i), color, nodoActual);
                if (i > 0) {
                    dibujarLinea(
                            segmentoActual.get(i - 1),
                            segmentoActual.get(i),
                            color,
                            nodoActual
                    );
                }
            }

            System.out.println(
                    "  ↩ Deshecho  (" + segmentoActual.size() + " puntos restantes)"
            );
        }

        // ---- ESC: CANCELAR SEGMENTO ACTUAL ----
        if (name.equals("Cancelar")) {

            if (segmentoActual.isEmpty()) {
                System.out.println("  [!] No hay segmento en progreso");
                return;
            }

            nodoActual.detachAllChildren();
            segmentoActual.clear();
            System.out.println("  ✖ Segmento cancelado");
        }

        // ---- P: IMPRIMIR TODOS LOS SEGMENTOS ----
        if (name.equals("Imprimir")) {
            imprimirTodo();
        }
    }

    // =========================
    // COLOR DEL SEGMENTO ACTUAL
    // =========================
    private ColorRGBA colorActual() {
        return COLORES_SEGMENTO[
            segmentos.size() % COLORES_SEGMENTO.length
        ];
    }

    // =========================
    // DIBUJAR PUNTO
    // Las coordenadas guardadas son del mundo (mundo.x, mundo.y),
    // por eso se posiciona directo — el rootNode está en world space.
    // =========================
    private void dibujarPunto(Vector2f pos, ColorRGBA color, Node nodo) {

        Sphere esfera = new Sphere(8, 8, 5);
        Geometry punto = new Geometry("Punto", esfera);

        Material mat = new Material(
                app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        mat.setColor("Color", ColorRGBA.Red);
        punto.setMaterial(mat);

        // pos.x y pos.y ya son coordenadas de mundo → posicionar directo
        punto.setLocalTranslation(pos.x, pos.y, 10f);

        nodo.attachChild(punto);
    }

    // =========================
    // DIBUJAR LÍNEA
    // Igual que el punto: p1 y p2 son world coords → directo.
    // =========================
    private void dibujarLinea(Vector2f p1, Vector2f p2,
                              ColorRGBA color, Node nodo) {

        float dx  = p2.x - p1.x;
        float dy  = p2.y - p1.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        if (len == 0) return;

        float px = -(dy / len) * (GROSOR / 2f);
        float py =  (dx / len) * (GROSOR / 2f);

        float[] vertices = {
            p1.x + px,  p1.y + py,  9f,
            p1.x - px,  p1.y - py,  9f,
            p2.x - px,  p2.y - py,  9f,
            p2.x + px,  p2.y + py,  9f
        };
        short[] indices = { 0, 1, 2,  0, 2, 3 };

        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
        mesh.setBuffer(VertexBuffer.Type.Index,    3, indices);
        mesh.updateBound();
        mesh.updateCounts();

        Geometry linea = new Geometry("Linea", mesh);

        Material mat = new Material(
                app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md"
        );
        mat.setColor("Color", color);
        linea.setMaterial(mat);

        nodo.attachChild(linea);
    }

    // =========================
    // IMPRIMIR AYUDA
    // =========================
    private void imprimirAyuda() {
        System.out.println("  CLICK  → agregar punto");
        System.out.println("  ENTER  → cerrar segmento e iniciar uno nuevo");
        System.out.println("  Z      → deshacer último punto");
        System.out.println("  ESC    → cancelar segmento actual");
        System.out.println("  P      → imprimir todos los segmentos");
    }

    // =========================
    // IMPRIMIR RESUMEN COMPLETO
    // =========================
    private void imprimirTodo() {

        System.out.println("══════════════════════════════");
        System.out.println("  SEGMENTOS GUARDADOS: " + segmentos.size());

        for (int i = 0; i < segmentos.size(); i++) {
            ArrayList<Vector2f> seg = segmentos.get(i);
            System.out.println("  Segmento " + (i + 1)
                    + "  (" + seg.size() + " puntos):");
            for (Vector2f p : seg) {
                System.out.println(
                        "    X: " + (int) p.x
                        + "  Y: " + (int) p.y
                );
            }
        }

        if (!segmentoActual.isEmpty()) {
            System.out.println("  Segmento en progreso ("
                    + segmentoActual.size() + " puntos):");
            for (Vector2f p : segmentoActual) {
                System.out.println(
                        "    X: " + (int) p.x
                        + "  Y: " + (int) p.y
                );
            }
        }

        System.out.println("══════════════════════════════");
    }

    // =========================
    // GETTERS PARA COLISIONES
    // =========================
    public ArrayList<ArrayList<Vector2f>> getSegmentos() {
        return segmentos;
    }

    public ArrayList<Vector2f> getSegmentoActual() {
        return segmentoActual;
    }
}