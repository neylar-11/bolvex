package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

public class CajaBloqueOculto {

    private SimpleApplication app;

    private float mundoX;
    private float mundoY;
    private float tamano;

    private Node     nodo;
    private Geometry geo;
    private Material mat;

    private boolean golpeado = false;
    private boolean animando = false;

    private float       timerAnim       = 0f;
    private final float BOUNCE_DURACION = 0.12f;
    private final float BOUNCE_PIXELS   = 18f;
    private float       offsetAnim      = 0f;

    private static final String[] TEXTURAS = {
        "Interface/cajaroja.png",
        "Interface/cajaazul.png",
        "Interface/cajaamarilla.png",
        "Interface/cajaverde.png"
    };
    private int texIndex = 0;

    public CajaBloqueOculto(SimpleApplication app,
                             float mundoX, float mundoY,
                             float tamano) {
        this.app    = app;
        this.mundoX = mundoX;
        this.mundoY = mundoY;
        this.tamano = tamano;
        crearVisual();
    }

    private void crearVisual() {
        Quad quad = new Quad(tamano, tamano);
        geo = new Geometry("CajaBloqueOculto_" + mundoX + "_" + mundoY, quad);

        mat = new Material(app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState()
           .setBlendMode(RenderState.BlendMode.Alpha);

        mat.getAdditionalRenderState().setColorWrite(false);
        mat.getAdditionalRenderState().setDepthWrite(false);

        geo.setMaterial(mat);
        geo.setQueueBucket(RenderQueue.Bucket.Transparent);
        geo.setLocalTranslation(0f, 0f, 0f);

        nodo = new Node("NodoOculto_" + mundoX + "_" + mundoY);
        nodo.attachChild(geo);
        actualizarPosicion();
    }

    private void aplicarTextura(String ruta) {
        Texture tex = app.getAssetManager().loadTexture(ruta);
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        mat.setTexture("ColorMap", tex);
    }

    private void actualizarPosicion() {
        nodo.setLocalTranslation(mundoX, mundoY + offsetAnim, 2f);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    public void update(float tpf) {
        if (!animando) return;

        timerAnim += tpf;
        float t = timerAnim / BOUNCE_DURACION;

        if (t >= 1f) {
            offsetAnim = 0f;
            animando   = false;
            timerAnim  = 0f;
        } else {
            offsetAnim = (float)(Math.sin(t * Math.PI) * BOUNCE_PIXELS);
        }

        actualizarPosicion();
    }

    // ============================================================
    // VERIFICAR GOLPE DESDE ABAJO
    // ============================================================
    public boolean verificarGolpe(float posX, float posY,
                                  float ancho, float alto,
                                  float velY) {
        if (golpeado) return false;
        if (velY <= 0) return false;

        float jugDer = posX + ancho;
        float jugArr = posY + alto;

        float bloqIzq = mundoX;
        float bloqDer = mundoX + tamano;
        float bloqAbj = mundoY;

        if (jugDer < bloqIzq || posX > bloqDer) return false;

        float margen = 12f;
        if (jugArr < bloqAbj - margen || jugArr > bloqAbj + margen) return false;

        texIndex = (int)(Math.random() * TEXTURAS.length);
        aplicarTextura(TEXTURAS[texIndex]);
        mat.getAdditionalRenderState().setColorWrite(true);
        mat.getAdditionalRenderState().setDepthWrite(true);
        geo.setQueueBucket(RenderQueue.Bucket.Opaque);

        golpeado  = true;
        animando  = true;
        timerAnim = 0f;
        return true;
    }

    // ============================================================
    // COLISIÓN PASIVA — solo activa si el bloque ya fue revelado
    // ============================================================
    public boolean colisionaConJugador(float posX, float posY,
                                       float ancho, float alto) {
        // Mientras no haya sido golpeado, el bloque no existe para el jugador
        if (!golpeado) return false;

        float jugDer = posX + ancho;
        float jugArr = posY + alto;

        float bloqIzq = mundoX;
        float bloqDer = mundoX + tamano;
        float bloqAbj = mundoY;
        float bloqArr = bloqAbj + tamano;

        return jugDer > bloqIzq && posX < bloqDer &&
               jugArr > bloqAbj && posY < bloqArr;
    }

    public void destruir() { nodo.removeFromParent(); }

    public Node  getNode()   { return nodo;   }
    public float getMundoX() { return mundoX; }
    public float getMundoY() { return mundoY; }
    public float getTamano() { return tamano; }
    public boolean fueGolpeado() { return golpeado; }
}