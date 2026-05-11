package com.mygame;

import com.jme3.math.Vector2f;
import java.util.ArrayList;

public class ColisionMapa {

    public static class Resultado {
        public float   posX;
        public float   posY;
        public boolean enPiso;
        public boolean enTecho;
        public boolean enParedIzq;
        public boolean enParedDer;

        public Resultado(float x, float y) {
            posX = x;
            posY = y;
        }
    }

    // Margen sólo para el raycast de "¿hay piso cerca?"
    // Las resoluciones de colisión NO usan margen — solo penetración real.
    private static final float MARGEN_RAY = 6f;

    private ArrayList<ArrayList<Vector2f>> segmentos;

    public ColisionMapa(Coordenadas coordenadas) {
        this.segmentos = coordenadas.getSegmentos();
    }

    public ColisionMapa(ArrayList<ArrayList<Vector2f>> segmentos) {
        this.segmentos = segmentos;
    }

    // ============================================================
    // RESOLVER — dos pasadas:
    //   1) Pisos y techos
    //   2) Paredes
    // ============================================================
    public Resultado resolver(float posX, float posY,
                              float ancho, float alto) {

        Resultado res = new Resultado(posX, posY);

        // ── PASADA 1: PISOS Y TECHOS ──
        for (ArrayList<Vector2f> segmento : segmentos) {
            if (segmento.size() < 2) continue;
            for (int i = 0; i < segmento.size() - 1; i++) {
                Vector2f p1 = segmento.get(i);
                Vector2f p2 = segmento.get(i + 1);
                float dx = Math.abs(p2.x - p1.x);
                float dy = Math.abs(p2.y - p1.y);
                if (dx <= dy) continue;
                resolverPisoTecho(res, p1, p2, ancho, alto);
            }
        }

        // ── PASADA 2: PAREDES ──
        for (ArrayList<Vector2f> segmento : segmentos) {
            if (segmento.size() < 2) continue;
            for (int i = 0; i < segmento.size() - 1; i++) {
                Vector2f p1 = segmento.get(i);
                Vector2f p2 = segmento.get(i + 1);
                float dx = Math.abs(p2.x - p1.x);
                float dy = Math.abs(p2.y - p1.y);
                if (dy <= dx) continue;
                resolverPared(res, p1, p2, ancho, alto);
            }
        }

        return res;
    }

    // ============================================================
    // PISO / TECHO
    //
    // Cambios clave vs versión anterior:
    //  • Sin MARGEN en la detección — solo resuelve si hay penetración real.
    //  • enPiso se activa solo cuando la base del jugador cruzó el segmento
    //    (pAbj < segY), no cuando está "cerca".
    //  • enTecho igual: solo si la cabeza cruzó (pArr > segY).
    //  • El jugador debe solapar en X con el segmento (sin margen extra).
    // ============================================================
    private void resolverPisoTecho(Resultado res,
                                   Vector2f p1, Vector2f p2,
                                   float ancho, float alto) {

        float pIzq = res.posX;
        float pDer = res.posX + ancho;
        float pAbj = res.posY;          // base del jugador
        float pArr = res.posY + alto;   // cabeza del jugador
        float pCX  = res.posX + ancho / 2f;

        float sMinX = Math.min(p1.x, p2.x);
        float sMaxX = Math.max(p1.x, p2.x);

        // El AABB del jugador debe solapar en X con el segmento
        if (pDer <= sMinX || pIzq >= sMaxX) return;

        // Interpolamos Y en el centro del jugador (o en el extremo más cercano)
        float cx = Math.max(sMinX, Math.min(sMaxX, pCX));
        float segY = interpolarY(p1, p2, cx);

        // ── PISO: el jugador viene de arriba y su base penetró ──
        // segY está dentro de la franja vertical del jugador
        if (segY >= pAbj && segY <= pArr) {
            // ¿El centro del jugador está por encima del segmento? → piso
            float pCY = res.posY + alto / 2f;
            if (pCY >= segY) {
                res.posY   = segY;
                res.enPiso = true;
            } else {
                // Centro por debajo → techo
                res.posY    = segY - alto;
                res.enTecho = true;
            }
        }
    }

    // ============================================================
    // PARED
    //
    // Cambios clave:
    //  • Sin MARGEN — solo resuelve penetración real.
    //  • El solapamiento en Y se comprueba sin margen.
    //  • enParedIzq / enParedDer solo si segX está dentro del AABB.
    // ============================================================
    private void resolverPared(Resultado res,
                               Vector2f p1, Vector2f p2,
                               float ancho, float alto) {

        float pIzq = res.posX;
        float pDer = res.posX + ancho;
        float pAbj = res.posY;
        float pArr = res.posY + alto;
        float pCX  = res.posX + ancho / 2f;
        float pCY  = res.posY + alto  / 2f;

        float sMinY = Math.min(p1.y, p2.y);
        float sMaxY = Math.max(p1.y, p2.y);

        // El AABB del jugador debe solapar en Y con el segmento
        if (pArr <= sMinY || pAbj >= sMaxY) return;

        float cy   = Math.max(sMinY, Math.min(sMaxY, pCY));
        float segX = interpolarX(p1, p2, cy);

        // segX debe estar dentro del AABB horizontal del jugador
        if (segX < pIzq || segX > pDer) return;

        if (pCX >= segX) {
            // Pared a la izquierda del jugador → empujar hacia la derecha
            res.posX       = segX;
            res.enParedIzq = true;
        } else {
            // Pared a la derecha del jugador → empujar hacia la izquierda
            res.posX       = segX - ancho;
            res.enParedDer = true;
        }
    }

    // ============================================================
    // HELPERS DE INTERPOLACIÓN
    // ============================================================
    private float interpolarY(Vector2f p1, Vector2f p2, float x) {
        float dx = p2.x - p1.x;
        if (Math.abs(dx) < 0.001f) return (p1.y + p2.y) / 2f;
        float t = (x - p1.x) / dx;
        t = Math.max(0f, Math.min(1f, t));
        return p1.y + t * (p2.y - p1.y);
    }

    private float interpolarX(Vector2f p1, Vector2f p2, float y) {
        float dy = p2.y - p1.y;
        if (Math.abs(dy) < 0.001f) return (p1.x + p2.x) / 2f;
        float t = (y - p1.y) / dy;
        t = Math.max(0f, Math.min(1f, t));
        return p1.x + t * (p2.x - p1.x);
    }

    // ============================================================
    // HELPER: ¿hay piso debajo del jugador dentro de `distancia`?
    // Esto SÍ usa margen porque es un raycast predictivo.
    // ============================================================
    public boolean hayPisoAbajo(float posX, float posY,
                                float ancho, float alto,
                                float distancia) {
        float rayX  = posX + ancho / 2f;
        float rayY0 = posY;
        float rayY1 = posY - distancia;

        for (ArrayList<Vector2f> segmento : segmentos) {
            if (segmento.size() < 2) continue;
            for (int i = 0; i < segmento.size() - 1; i++) {
                Vector2f p1 = segmento.get(i);
                Vector2f p2 = segmento.get(i + 1);
                if (Math.abs(p2.x - p1.x) <= Math.abs(p2.y - p1.y)) continue;

                float minX = Math.min(p1.x, p2.x);
                float maxX = Math.max(p1.x, p2.x);
                if (rayX < minX || rayX > maxX) continue;

                float segY = interpolarY(p1, p2, rayX);
                if (segY <= rayY0 + MARGEN_RAY && segY >= rayY1) return true;
            }
        }
        return false;
    }
}