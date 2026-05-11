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

    private Node     nodoNyx;
    private Geometry geoNyx;

    private final float ANCHO = 60f;
    private final float ALTO  = 60f;

    public float posX = 148f;
    public float posY = 678f;

    private float velX = 0f;
    private float velY = 0f;

    private final float VELOCIDAD     = 350f;
    private final float FUERZA_SALTO  = 900f;
    private final float GRAVEDAD      = -2000f;
    private final float VEL_MAX_CAIDA = -1200f;
    private final int   SUBSTEPS      = 4;

    // =========================
    // SALTO VARIABLE
    // Mientras mantienes espacio
    // y vas subiendo, la gravedad
    // se reduce para llegar más alto
    //
    // GRAVEDAD_SALTO: multiplicador
    //   1.0 = gravedad normal (salto corto)
    //   0.0 = sin gravedad (sube infinito)
    //   0.4 = equilibrio para ~4.5 tiles
    //
    // CORTE_SALTO: al soltar espacio
    // antes de llegar al tope, la vel
    // se multiplica por esto (0.5 = corte)
    // =========================
    private final float GRAVEDAD_SALTO = 0.6f;
    private final float CORTE_SALTO    = 0.95f;

    private boolean saltandoPresionado = false;

    private boolean enPiso      = false;
    private boolean puedeSaltar = false;

    private float anguloRotacion     = 0f;
    private final float GRADOS_POR_PIXEL = 0.35f;

    private boolean moverIzq = false;
    private boolean moverDer = false;

    public Jugador(SimpleApplication app,
                   ColisionMapa colisionMapa,
                   CamaraControl camaraControl) {

        this.app           = app;
        this.colisionMapa  = colisionMapa;
        this.camaraControl = camaraControl;

        crearVisual();
        registrarInput();
    }

    private void crearVisual() {

        Quad quad = new Quad(ANCHO, ALTO);
        geoNyx = new Geometry("Nyx", quad);

        Material mat = new Material(
                app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = app.getAssetManager()
                .loadTexture("Interface/nyx.png");
        tex.setWrap(Texture.WrapMode.EdgeClamp);
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState()
           .setBlendMode(RenderState.BlendMode.Alpha);

        geoNyx.setMaterial(mat);
        geoNyx.setQueueBucket(RenderQueue.Bucket.Transparent);
        geoNyx.setLocalTranslation(-ANCHO / 2f, -ALTO / 2f, 0f);

        nodoNyx = new Node("NodoNyx");
        nodoNyx.attachChild(geoNyx);
        nodoNyx.setLocalTranslation(
                posX + ANCHO / 2f,
                posY + ALTO  / 2f,
                2f
        );

        app.getRootNode().attachChild(nodoNyx);
    }

    private void registrarInput() {
        app.getInputManager().addMapping("NyxIzq",
                new KeyTrigger(KeyInput.KEY_LEFT),
                new KeyTrigger(KeyInput.KEY_A));
        app.getInputManager().addMapping("NyxDer",
                new KeyTrigger(KeyInput.KEY_RIGHT),
                new KeyTrigger(KeyInput.KEY_D));
        app.getInputManager().addMapping("NyxSalto",
                new KeyTrigger(KeyInput.KEY_SPACE));

        app.getInputManager().addListener(this,
                "NyxIzq", "NyxDer", "NyxSalto");
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
                // INICIO DEL SALTO
                velY               = FUERZA_SALTO;
                enPiso             = false;
                puedeSaltar        = false;
                saltandoPresionado = true;

            } else if (!isPressed && saltandoPresionado && velY > 0f) {
                // SOLTÓ ESPACIO ANTES DEL TOPE
                // → cortar velocidad para salto corto
                velY               *= CORTE_SALTO;
                saltandoPresionado  = false;
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
        // Si el jugador va subiendo Y mantiene espacio:
        // aplicar gravedad reducida → sube más alto
        // En cualquier otro caso: gravedad normal
        float gravedadFrame;

        if (saltandoPresionado && velY > 0f) {
            gravedadFrame = GRAVEDAD * GRAVEDAD_SALTO;
        } else {
            gravedadFrame      = GRAVEDAD;
            saltandoPresionado = false; // ya no sube, desactivar
        }

        velY += gravedadFrame * tpf;
        if (velY < VEL_MAX_CAIDA) velY = VEL_MAX_CAIDA;

        // ── 3. SUB-PASOS DE FÍSICA + COLISIÓN ──
        float dtSub = tpf / SUBSTEPS;
        enPiso      = false;

        for (int i = 0; i < SUBSTEPS; i++) {

            posX += velX * dtSub;
            posY += velY * dtSub;

            ColisionMapa.Resultado r =
                    colisionMapa.resolver(posX, posY, ANCHO, ALTO);
            posX = r.posX;
            posY = r.posY;

            if (r.enPiso) {
                velY               = 0f;
                enPiso             = true;
                puedeSaltar        = true;
                saltandoPresionado = false;
            }
            if (r.enTecho) {
                velY               = 0f;
                saltandoPresionado = false; // techo corta el salto
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
        rot.fromAngleAxis(
                anguloRotacion * FastMath.DEG_TO_RAD,
                Vector3f.UNIT_Z
        );
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

    public float getPosX()    { return posX; }
    public float getPosY()    { return posY; }
    public float getAncho()   { return ANCHO; }
    public float getAlto()    { return ALTO; }
    public boolean isEnPiso() { return enPiso; }
}