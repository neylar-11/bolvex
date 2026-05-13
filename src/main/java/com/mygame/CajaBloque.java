package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import java.util.ArrayList;

public class CajaBloque {

    private SimpleApplication app;

    private float mundoX;
    private float mundoY;
    private float tamano;

    private Node     nodo;
    private Geometry geo;
    private Material mat;

    // ── ESTADO ──
    private boolean golpeado = false;
    private boolean animando = false;

    // ── Si este bloque spawna el item ──
    private boolean tieneItem    = false;
    private ItemComida itemSpawn = null;
    private ColisionMapa colisionMapaRef = null;

    // ── Listas de bloques para pasarle al item ──
    private ArrayList<CajaBloque>       bloquesRef        = null;
    private ArrayList<CajaBloqueOculto> bloquesOcultosRef = null;

    // ── ANIMACIÓN DE REBOTE ──
    private float       timerAnim       = 0f;
    private final float BOUNCE_DURACION = 0.12f;
    private final float BOUNCE_PIXELS   = 18f;
    private float       offsetAnim      = 0f;

    // ── CICLO DE TEXTURAS ──
    private static final String[] TEXTURAS = {
        "Interface/cajallaveroja.png",
        "Interface/cajallaveazul.png",
        "Interface/cajallaveamarilla.png",
        "Interface/cajallaveverde.png"
    };
    private int         texIndex      = 0;
    private float       timerTex      = 0f;
    private final float TEX_INTERVALO = 0.18f;

    // ── Constructor normal (sin item) ──
    public CajaBloque(SimpleApplication app,
                      float mundoX, float mundoY,
                      float tamano) {
        this.app    = app;
        this.mundoX = mundoX;
        this.mundoY = mundoY;
        this.tamano = tamano;
        crearVisual();
    }

    // ── Constructor con item ──
    public CajaBloque(SimpleApplication app,
                      float mundoX, float mundoY,
                      float tamano,
                      ColisionMapa colisionMapa,
                      ArrayList<CajaBloque> bloques,
                      ArrayList<CajaBloqueOculto> bloquesOcultos) {
        this.app               = app;
        this.mundoX            = mundoX;
        this.mundoY            = mundoY;
        this.tamano            = tamano;
        this.tieneItem         = true;
        this.colisionMapaRef   = colisionMapa;
        this.bloquesRef        = bloques;
        this.bloquesOcultosRef = bloquesOcultos;
        crearVisual();
    }

    private void crearVisual() {
        Quad quad = new Quad(tamano, tamano);
        geo = new Geometry("CajaBloque_" + mundoX + "_" + mundoY, quad);

        mat = new Material(app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState()
           .setBlendMode(RenderState.BlendMode.Alpha);
        mat.setFloat("AlphaDiscardThreshold", 0.01f);

        aplicarTextura(TEXTURAS[texIndex]);

        geo.setMaterial(mat);
        geo.setQueueBucket(RenderQueue.Bucket.Opaque);
        geo.setLocalTranslation(0f, 0f, 0f);

        nodo = new Node("NodoCaja_" + mundoX + "_" + mundoY);
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

        if (!golpeado) {
            timerTex += tpf;
            if (timerTex >= TEX_INTERVALO) {
                timerTex = 0f;
                texIndex = (texIndex + 1) % TEXTURAS.length;
                aplicarTextura(TEXTURAS[texIndex]);
            }
        }

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

        golpeado  = true;
        animando  = true;
        timerAnim = 0f;

        if (tieneItem && itemSpawn == null && colisionMapaRef != null) {
            itemSpawn = new ItemComida(
                    app,
                    colisionMapaRef,
                    bloquesRef,
                    bloquesOcultosRef,
                    mundoX, mundoY + tamano, tamano
            );
        }

        return true;
    }

    // ============================================================
    // COLISIÓN PASIVA
    // ============================================================
    public float getTechoY() { return mundoY; }

    public boolean colisionaConJugador(float posX, float posY,
                                       float ancho, float alto) {
        float jugDer = posX + ancho;
        float jugArr = posY + alto;

        float bloqIzq = mundoX;
        float bloqDer = mundoX + tamano;
        float bloqAbj = mundoY;
        float bloqArr = bloqAbj + tamano;

        return jugDer > bloqIzq && posX < bloqDer &&
               jugArr > bloqAbj && posY < bloqArr;
    }

    // ============================================================
    // LIMPIEZA
    // ============================================================
    public void destruir() { nodo.removeFromParent(); }

    // GETTERS
    public Node       getNode()        { return nodo;        }
    public boolean    fueGolpeado()    { return golpeado;    }
    public float      getMundoX()      { return mundoX;      }
    public float      getMundoY()      { return mundoY;      }
    public float      getTamano()      { return tamano;      }
    public float      getOffsetAnim()  { return offsetAnim;  }  // ── NUEVO
    public boolean    isAnimando()     { return animando;    }  // ── NUEVO
    public ItemComida getItemSpawn()   { return itemSpawn;   }
    public boolean    tieneItem()      { return tieneItem;   }
}