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
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

public class Jugador implements ActionListener {

    private SimpleApplication app;
    private ColisionMapa      colisionMapa;
    private CamaraControl     camaraControl;

    // =========================
    // GEOMETRÍA
    // =========================
    private Geometry geoNyx;

    // Tamaño visual y AABB de colisión
    private final float ANCHO = 60f;
    private final float ALTO  = 60f;

    // =========================
    // POSICIÓN (esquina inf-izq del AABB)
    // =========================
    public float posX = 200f;
    public float posY = 700f;

    // =========================
    // FÍSICA
    // =========================
    private float velX = 0f;
    private float velY = 0f;

    private final float VELOCIDAD      = 350f;  // píxeles/seg horizontal
    private final float FUERZA_SALTO   = 900f;  // impulso vertical
    private final float GRAVEDAD       = -2000f; // aceleración hacia abajo
    private final float VEL_MAX_CAIDA  = -1200f; // velocidad máxima de caída

    private boolean enPiso    = false;
    private boolean puedeSaltar = false;

    // =========================
    // ROTACIÓN (bolita)
    // =========================
    private float anguloRotacion = 0f;
    // Grados por píxel recorrido (ajusta a gusto)
    private final float GRADOS_POR_PIXEL = 0.4f;

    // =========================
    // INPUT
    // =========================
    private boolean moverIzq = false;
    private boolean moverDer = false;

    // =========================
    // CONSTRUCTOR
    // =========================
    public Jugador(SimpleApplication app,
                   ColisionMapa colisionMapa,
                   CamaraControl camaraControl) {

        this.app           = app;
        this.colisionMapa  = colisionMapa;
        this.camaraControl = camaraControl;

        crearVisual();
        registrarInput();
    }

    // =========================
    // CREAR QUAD CON TEXTURA
    // =========================
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

        // Pivote en el centro del quad para que rote bien
        // jME rota desde el origen (0,0) del quad,
        // así que movemos el pivot al centro con la traslación del nodo
        geoNyx.setLocalTranslation(posX, posY, 2f);

        app.getRootNode().attachChild(geoNyx);
    }

    // =========================
    // INPUT
    // =========================
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

        if (name.equals("NyxIzq"))  moverIzq = isPressed;
        if (name.equals("NyxDer"))  moverDer = isPressed;

        // SALTO: solo al soltar la tecla si está en piso
        if (name.equals("NyxSalto") && !isPressed && puedeSaltar) {
            velY = FUERZA_SALTO;
            enPiso     = false;
            puedeSaltar = false;
        }
    }

    // =========================
    // UPDATE — llamar desde Main.simpleUpdate
    // =========================
    public void update(float tpf) {

        // ── 1. MOVIMIENTO HORIZONTAL ──
        velX = 0f;
        if (moverIzq) velX = -VELOCIDAD;
        if (moverDer) velX =  VELOCIDAD;

        // ── 2. GRAVEDAD ──
        velY += GRAVEDAD * tpf;
        if (velY < VEL_MAX_CAIDA) velY = VEL_MAX_CAIDA;

        // ── 3. MOVER POSICIÓN ──
        posX += velX * tpf;
        posY += velY * tpf;

        // ── 4. COLISIONES ──
        ColisionMapa.Resultado r = colisionMapa.resolver(posX, posY, ANCHO, ALTO);
        posX = r.posX;
        posY = r.posY;

        if (r.enPiso) {
            velY        = 0f;
            enPiso      = true;
            puedeSaltar = true;
        } else {
            enPiso      = false;
            puedeSaltar = false;
        }

        if (r.enTecho)                      velY = 0f;
        if (r.enParedIzq || r.enParedDer)   velX = 0f;

        // ── 5. ROTACIÓN (bolita rueda según movimiento) ──
        // Positivo = rueda a la derecha, negativo = a la izquierda
        if (velX != 0f) {
            anguloRotacion -= velX * GRADOS_POR_PIXEL * tpf;
        }

        // ── 6. ACTUALIZAR VISUAL ──
        // Centro del quad para rotar desde el medio
        float centroX = posX + ANCHO / 2f;
        float centroY = posY + ALTO  / 2f;

        Quaternion rot = new Quaternion();
        rot.fromAngleAxis(anguloRotacion * FastMath.DEG_TO_RAD,
                Vector3f.UNIT_Z);
        geoNyx.setLocalRotation(rot);

        // Posición: compensamos el pivote para que el quad
        // rote sobre su centro y no sobre su esquina
        geoNyx.setLocalTranslation(
                centroX - ANCHO / 2f,
                centroY - ALTO  / 2f,
                2f
        );

        // ── 7. CÁMARA SIGUE A NYX ──
        camaraControl.seguirJugador(centroX);
    }

    // =========================
    // QUITAR DEL ESCENARIO
    // =========================
    public void destruir() {
        geoNyx.removeFromParent();
        app.getInputManager().deleteMapping("NyxIzq");
        app.getInputManager().deleteMapping("NyxDer");
        app.getInputManager().deleteMapping("NyxSalto");
        app.getInputManager().removeListener(this);
    }

    // Getters útiles para más adelante
    public float getPosX()   { return posX; }
    public float getPosY()   { return posY; }
    public float getAncho()  { return ANCHO; }
    public float getAlto()   { return ALTO; }
    public boolean isEnPiso(){ return enPiso; }
}