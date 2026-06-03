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

public class ItemComida {

    private SimpleApplication app;
    private ColisionMapa      colisionMapa;

    // ── Referencias a bloques para colisión ──
    private ArrayList<CajaBloque>       bloques;
    private ArrayList<CajaBloqueOculto> bloquesOcultos;

    private Node     nodo;
    private Geometry geo;

    private final float TAMANO = 32f;

    private float posX;
    private float posY;

    public enum Fase { SALIENDO, ACTIVO, RECOGIDO }
    private Fase fase = Fase.SALIENDO;

    private float       yOrigen;
    private final float SUBIDA_PIXELS = 24f;
    private final float VEL_SUBIDA    = 80f;

    private float velX =  140f;
    private float velY =  0f;
    private final float GRAVEDAD        = -1200f;
    private final float VEL_MAX_CAIDA   = -800f;
    private final float IMPULSO_REBOTE  =  500f;

    private boolean vivo = true;

    // ── Constructor completo ──
    public ItemComida(SimpleApplication app,
                      ColisionMapa colisionMapa,
                      ArrayList<CajaBloque> bloques,
                      ArrayList<CajaBloqueOculto> bloquesOcultos,
                      float bloqueX, float bloqueY, float bloqueAncho) {
        this.app            = app;
        this.colisionMapa   = colisionMapa;
        this.bloques        = bloques;
        this.bloquesOcultos = bloquesOcultos;

        this.posX    = bloqueX + (bloqueAncho / 2f) - (TAMANO / 2f);
        this.posY    = bloqueY;
        this.yOrigen = bloqueY;

        crearVisual();
    }

    private void crearVisual() {
        Quad quad = new Quad(TAMANO, TAMANO);
        geo = new Geometry("ItemComida", quad);

        Material mat = new Material(app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = app.getAssetManager()
                .loadTexture("Interface/comidaladrillo.png");
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        mat.setTexture("ColorMap", tex);
        mat.getAdditionalRenderState()
           .setBlendMode(RenderState.BlendMode.Alpha);
        mat.getAdditionalRenderState()
           .setDepthWrite(false);

        geo.setMaterial(mat);
        geo.setQueueBucket(RenderQueue.Bucket.Transparent);

        nodo = new Node("NodoItemComida");
        nodo.attachChild(geo);
        nodo.setLocalTranslation(posX, posY, 3f);

        app.getRootNode().attachChild(nodo);
    }

    public void update(float tpf) {
        if (fase == Fase.RECOGIDO) return;

        // ── FASE SALIENDO: sube del bloque antes de caer ──
        if (fase == Fase.SALIENDO) {
            posY += VEL_SUBIDA * tpf;
            if (posY >= yOrigen + SUBIDA_PIXELS) {
                posY = yOrigen + SUBIDA_PIXELS;
                fase = Fase.ACTIVO;
            }
            nodo.setLocalTranslation(posX, posY, 3f);
            return;
        }

        // ── FASE ACTIVO ──

        // Gravedad
        velY += GRAVEDAD * tpf;
        if (velY < VEL_MAX_CAIDA) velY = VEL_MAX_CAIDA;

        posX += velX * tpf;
        posY += velY * tpf;

        // Colisión con el mapa (terreno)
        ColisionMapa.Resultado r =
                colisionMapa.resolver(posX, posY, TAMANO, TAMANO);
        posX = r.posX;
        posY = r.posY;

        if (r.enPiso)                      velY = 0f;
        if (r.enParedIzq || r.enParedDer)  velX = -velX;

        // ── Colisión con CajaBloque ──
        if (bloques != null) {
            for (CajaBloque bloque : bloques) {
                if (!bloque.colisionaConJugador(posX, posY, TAMANO, TAMANO)) continue;

                float itemCX = posX + TAMANO / 2f;
                float itemCY = posY + TAMANO / 2f;
                float bloqCX = bloque.getMundoX() + bloque.getTamano() / 2f;
                float bloqCY = bloque.getMundoY() + bloque.getTamano() / 2f;

                float overlapX = (TAMANO + bloque.getTamano()) / 2f - Math.abs(itemCX - bloqCX);
                float overlapY = (TAMANO + bloque.getTamano()) / 2f - Math.abs(itemCY - bloqCY);

                if (overlapX <= 0 || overlapY <= 0) continue;

                if (overlapY < overlapX) {
                    if (itemCY > bloqCY) {
                        // Item encima del bloque — lo sigue si está animando
                        posY = bloque.getMundoY() + bloque.getTamano() + bloque.getOffsetAnim();
                        velY = 0f;
                        // Si el bloque está rebotando, lanza el item hacia arriba
                        // y resetea la dirección horizontal a la derecha (adelante)
                        if (bloque.isAnimando()) {
                            velY = IMPULSO_REBOTE;
                            velX = Math.abs(velX); // ← siempre hacia la derecha al saltar
                        }
                    } else {
                        // Item debajo del bloque
                        posY = bloque.getMundoY() - TAMANO;
                        if (velY > 0) velY = 0f;
                    }
                } else {
                    // Colisión lateral → rebote horizontal
                    if (itemCX > bloqCX) posX = bloque.getMundoX() + bloque.getTamano();
                    else                  posX = bloque.getMundoX() - TAMANO;
                    velX = -velX;
                }
            }
        }

        // ── Colisión con CajaBloqueOculto (solo si ya fue revelado) ──
        if (bloquesOcultos != null) {
            for (CajaBloqueOculto b : bloquesOcultos) {
                if (!b.colisionaConJugador(posX, posY, TAMANO, TAMANO)) continue;
                resolverColisionAABB(b.getMundoX(), b.getMundoY(), b.getTamano());
            }
        }

        nodo.setLocalTranslation(posX, posY, 3f);
    }

    // ============================================================
    // Colisión AABB genérica (bloques ocultos)
    // ============================================================
    private void resolverColisionAABB(float bloqX, float bloqY, float bloqTam) {
        float itemCX = posX + TAMANO  / 2f;
        float itemCY = posY + TAMANO  / 2f;
        float bloqCX = bloqX + bloqTam / 2f;
        float bloqCY = bloqY + bloqTam / 2f;

        float overlapX = (TAMANO + bloqTam) / 2f - Math.abs(itemCX - bloqCX);
        float overlapY = (TAMANO + bloqTam) / 2f - Math.abs(itemCY - bloqCY);

        if (overlapX <= 0 || overlapY <= 0) return;

        if (overlapY < overlapX) {
            if (itemCY > bloqCY) {
                posY = bloqY + bloqTam;
                velY = 0f;
            } else {
                posY = bloqY - TAMANO;
                if (velY > 0) velY = 0f;
            }
        } else {
            if (itemCX > bloqCX) posX = bloqX + bloqTam;
            else                  posX = bloqX - TAMANO;
            velX = -velX;
        }
    }

    // ── Colisión con el jugador ──
    public boolean tocaJugador(float jugX, float jugY,
                               float jugAncho, float jugAlto) {
        if (fase != Fase.ACTIVO) return false;

        return (posX + TAMANO > jugX)            &&
               (posX          < jugX + jugAncho) &&
               (posY + TAMANO > jugY)            &&
               (posY          < jugY + jugAlto);
    }

    public void recoger() {
        fase = Fase.RECOGIDO;
        vivo = false;
        nodo.removeFromParent();
    }//

    public boolean isVivo()  { return vivo;  }
    public Fase    getFase() { return fase;  }
    public Node    getNode() { return nodo;  }
}
