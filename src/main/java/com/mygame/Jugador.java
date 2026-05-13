package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

public class Jugador implements ActionListener {

    private SimpleApplication app;
    private ColisionMapa      colisionMapa;
    private CamaraControl     camaraControl;
    private Mapa1State        mapaState;

    private Node     nodoNyx;
    private Geometry geoNyx;
    private Material matNyx;

    private final float ANCHO = 40f;
    private final float ALTO  = 40f;

    public float posX = 148f;
    public float posY = 678f;

    private float velX = 0f;
    private float velY = 0f;

    private final float VELOCIDAD     = 350f;
    private final float FUERZA_SALTO  = 900f;
    private final float GRAVEDAD      = -2000f;
    private final float VEL_MAX_CAIDA = -1200f;
    private final int   SUBSTEPS      = 4;

    private final float GRAVEDAD_SALTO = 0.6f;
    private final float CORTE_SALTO    = 0.95f;

    private boolean saltandoPresionado = false;
    private boolean enPiso             = false;
    private boolean puedeSaltar        = false;

    private float anguloRotacion         = 0f;
    private final float GRADOS_POR_PIXEL = 0.35f;

    private boolean moverIzq = false;
    private boolean moverDer = false;

    // ── TRANSFORMACIÓN ──
    private boolean transformado = false;

    public Jugador(SimpleApplication app,
                   ColisionMapa colisionMapa,
                   CamaraControl camaraControl) {
        this(app, colisionMapa, camaraControl, null);
    }

    public Jugador(SimpleApplication app,
                   ColisionMapa colisionMapa,
                   CamaraControl camaraControl,
                   Mapa1State mapaState) {
        this.app          = app;
        this.colisionMapa  = colisionMapa;
        this.camaraControl = camaraControl;
        this.mapaState     = mapaState;
        crearVisual();
        registrarInput();
    }

    private void crearVisual() {
        Quad quad = new Quad(ANCHO, ALTO);
        geoNyx = new Geometry("Nyx", quad);

        matNyx = new Material(app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = app.getAssetManager()
                .loadTexture("Interface/nyx2.png");
        tex.setWrap(Texture.WrapMode.EdgeClamp);
        matNyx.setTexture("ColorMap", tex);
        matNyx.getAdditionalRenderState()
              .setBlendMode(RenderState.BlendMode.Alpha);
        // FIX: evita que las esquinas transparentes tapen sprites detrás
        matNyx.getAdditionalRenderState()
              .setDepthWrite(false);

        geoNyx.setMaterial(matNyx);
        geoNyx.setQueueBucket(RenderQueue.Bucket.Transparent);
        geoNyx.setLocalTranslation(-ANCHO / 2f, -ALTO / 2f, 0f);

        nodoNyx = new Node("NodoNyx");
        nodoNyx.attachChild(geoNyx);
        nodoNyx.setLocalTranslation(posX + ANCHO / 2f, posY + ALTO / 2f, 2f);

        app.getRootNode().attachChild(nodoNyx);
    }

    // ── Cambia la textura del jugador al recoger el item ──
    public void transformar() {
        if (transformado) return;
        transformado = true;

        Texture tex = app.getAssetManager()
                .loadTexture("Interface/nyx2ladrillo.png");
        tex.setWrap(Texture.WrapMode.EdgeClamp);
        matNyx.setTexture("ColorMap", tex);

        System.out.println("¡Nyx se transformó con el ladrillo!");
    }

    public boolean isTransformado() { return transformado; }

    private void registrarInput() {
        app.getInputManager().addMapping("NyxIzq",
                new KeyTrigger(KeyInput.KEY_LEFT),
                new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addMapping("NyxDer",
                new KeyTrigger(KeyInput.KEY_RIGHT),
                new KeyTrigger(KeyInput.KEY_D));
        app.getInputManager().addMapping("NyxSalto",
                new KeyTrigger(KeyInput.KEY_SPACE));
        app.getInputManager().addListener(this, "NyxIzq", "NyxDer", "NyxSalto");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (camaraControl.isModoDebug()) {
            moverIzq           = false;
            moverDer           = false;
            saltandoPresionado = false;
            return;
        }

        if (name.equals("NyxIzq")) moverIzq = isPressed;
        if (name.equals("NyxDer")) moverDer = isPressed;

        if (name.equals("NyxSalto")) {
            if (isPressed && puedeSaltar) {
                velY               = FUERZA_SALTO;
                enPiso             = false;
                puedeSaltar        = false;
                saltandoPresionado = true;
            } else if (!isPressed && saltandoPresionado && velY > 0f) {
                velY              *= CORTE_SALTO;
                saltandoPresionado = false;
            }
        }
    }

    public void update(float tpf) {

        // ── 1. VELOCIDAD HORIZONTAL ──
        if (camaraControl.isModoDebug()) {
            velX = 0f;
        } else {
            velX = 0f;
            if (moverIzq) velX = -VELOCIDAD;
            if (moverDer) velX =  VELOCIDAD;
        }

        // ── 2. GRAVEDAD ──
        float gravedadFrame;
        if (saltandoPresionado && velY > 0f) {
            gravedadFrame = GRAVEDAD * GRAVEDAD_SALTO;
        } else {
            gravedadFrame      = GRAVEDAD;
            saltandoPresionado = false;
        }
        velY += gravedadFrame * tpf;
        if (velY < VEL_MAX_CAIDA) velY = VEL_MAX_CAIDA;

        // ── 3. SUB-PASOS DE FÍSICA + COLISIÓN ──
        float dtSub = tpf / SUBSTEPS;
        enPiso = false;

        for (int i = 0; i < SUBSTEPS; i++) {
            posX += velX * dtSub;
            posY += velY * dtSub;

            ColisionMapa.Resultado r =
                    colisionMapa.resolver(posX, posY, ANCHO, ALTO);
            posX = r.posX;
            posY = r.posY;

            if (mapaState != null) {
                mapaState.updateBloques(posX, posY, ANCHO, ALTO, velY, dtSub);
                mapaState.resolverColisionBloques(r, ANCHO, ALTO);

                // ── BLOQUES OCULTOS ──
                mapaState.updateBloquesOcultos(posX, posY, ANCHO, ALTO, velY, dtSub);
                mapaState.resolverColisionBloquesOcultos(r, ANCHO, ALTO);

                posX = r.posX;
                posY = r.posY;
            }

            if (r.enPiso) {
                velY               = 0f;
                enPiso             = true;
                puedeSaltar        = true;
                saltandoPresionado = false;
            }
            if (r.enTecho) {
                velY               = 0f;
                saltandoPresionado = false;
            }
            if (r.enParedIzq || r.enParedDer) velX = 0f;
        }

        if (!enPiso) puedeSaltar = false;

        // ── 4. ROTACIÓN ──
        if (velX != 0f) {
            anguloRotacion -= velX * GRADOS_POR_PIXEL * tpf;
        }

        // ── 5. ACTUALIZAR NODO ──
        float centroX = posX + ANCHO / 2f;
        float centroY = posY + ALTO  / 2f;

        Quaternion rot = new Quaternion();
        rot.fromAngleAxis(anguloRotacion * FastMath.DEG_TO_RAD, Vector3f.UNIT_Z);
        nodoNyx.setLocalRotation(rot);
        nodoNyx.setLocalTranslation(centroX, centroY, 2f);

        // ── 6. CÁMARA SIGUE A NYX ──
        camaraControl.seguirJugador(centroX);
    }

    public void destruir() {
        nodoNyx.removeFromParent();
        app.getInputManager().deleteMapping("NyxIzq");
        app.getInputManager().deleteMapping("NyxDer");
        app.getInputManager().deleteMapping("NyxSalto");
        app.getInputManager().removeListener(this);
    }

    public float getPosX()      { return posX;   }
    public float getPosY()      { return posY;   }
    public float getAncho()     { return ANCHO;  }
    public float getAlto()      { return ALTO;   }
    public boolean isEnPiso()   { return enPiso; }
}