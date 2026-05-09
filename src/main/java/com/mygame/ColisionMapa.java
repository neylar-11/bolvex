package com.mygame;

import com.jme3.math.Vector2f;
import java.util.ArrayList;

/**
 * ColisionMapa
 * ============
 * Detecta colisiones entre un AABB (rectángulo del jugador)
 * y los segmentos de línea del mapa capturados con Coordenadas.
 *
 * USO:
 *   ColisionMapa colision = new ColisionMapa(coordenadas);
 *
 *   // En el update del jugador:
 *   ColisionMapa.Resultado r = colision.resolver(posX, posY, anchoJugador, altoJugador);
 *   posX = r.posX;
 *   posY = r.posY;
 *   if (r.enPiso)   velocidadY = 0;  // dejó de caer
 *   if (r.enTecho)  velocidadY = 0;  // rebotó arriba
 *   if (r.enPared)  velocidadX = 0;  // chocó pared
 */
public class ColisionMapa {

    // ============================================================
    // RESULTADO DE RESOLUCIÓN
    // ============================================================
    public static class Resultado {
        public float   posX;       // posición corregida X (esquina inferior izquierda)
        public float   posY;       // posición corregida Y (esquina inferior izquierda)
        public boolean enPiso;     // el jugador está parado sobre algo
        public boolean enTecho;    // el jugador tocó techo
        public boolean enParedIzq; // chocó pared a su izquierda
        public boolean enParedDer; // chocó pared a su derecha

        public Resultado(float x, float y) {
            posX = x;
            posY = y;
        }
    }

    // ============================================================
    // UMBRAL: ángulo para decidir si una línea es piso/techo o pared
    // Si |dy/len| > UMBRAL_HORIZONTAL → es pared
    // Si |dx/len| > UMBRAL_HORIZONTAL → es piso o techo
    // ============================================================
    private static final float UMBRAL_HORIZONTAL = 0.5f; // 45°

    // Margen de penetración mínima para considerar colisión real
    private static final float MARGEN = 2f;

    private ArrayList<ArrayList<Vector2f>> segmentos;

    public ColisionMapa(Coordenadas coordenadas) {
        this.segmentos = coordenadas.getSegmentos();
    }

    // Permite pasar los segmentos directamente si ya los tienes
    public ColisionMapa(ArrayList<ArrayList<Vector2f>> segmentos) {
        this.segmentos = segmentos;
    }

    // ============================================================
    // RESOLVER COLISIONES
    // posX, posY = esquina inferior izquierda del jugador
    // ancho, alto = tamaño del AABB del jugador
    // ============================================================
    public Resultado resolver(float posX, float posY,
                              float ancho, float alto) {

        Resultado res = new Resultado(posX, posY);

        // AABB del jugador
        float jIzq = posX;
        float jDer = posX + ancho;
        float jAbj = posY;          // Y crece hacia arriba en jME
        float jArr = posY + alto;

        for (ArrayList<Vector2f> segmento : segmentos) {
            if (segmento.size() < 2) continue;

            for (int i = 0; i < segmento.size() - 1; i++) {

                Vector2f p1 = segmento.get(i);
                Vector2f p2 = segmento.get(i + 1);

                // Bounding box del segmento (con margen)
                float sMinX = Math.min(p1.x, p2.x) - MARGEN;
                float sMaxX = Math.max(p1.x, p2.x) + MARGEN;
                float sMinY = Math.min(p1.y, p2.y) - MARGEN;
                float sMaxY = Math.max(p1.y, p2.y) + MARGEN;

                // Descarte rápido: si el AABB del jugador no toca
                // el bounding box del segmento, saltamos
                if (jDer < sMinX || jIzq > sMaxX ||
                    jArr < sMinY || jAbj > sMaxY) {
                    continue;
                }

                // ------------------------------------------------
                // Calcular dirección y normal del segmento
                // ------------------------------------------------
                float dx  = p2.x - p1.x;
                float dy  = p2.y - p1.y;
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len == 0) continue;

                float dirX = dx / len;
                float dirY = dy / len;

                // Normal perpendicular (apunta "hacia dentro" del mapa)
                // Normal = (-dy, dx) normalizada → ya está normalizada
                float normX = -dirY;
                float normY =  dirX;

                // Centro del AABB del jugador
                float cX = res.posX + ancho / 2f;
                float cY = res.posY + alto  / 2f;

                // ------------------------------------------------
                // Proyectar centro del jugador sobre la línea
                // para obtener distancia con signo a la línea
                // ------------------------------------------------
                // Vector desde p1 al centro
                float vX = cX - p1.x;
                float vY = cY - p1.y;

                // Distancia con signo a la línea (positivo = lado de la normal)
                float dist = vX * normX + vY * normY;

                // Radio del AABB proyectado sobre la normal
                float radioX = (ancho / 2f) * Math.abs(normX);
                float radioY = (alto  / 2f) * Math.abs(normY);
                float radio  = radioX + radioY;

                // Penetración: cuánto se metió el jugador en la línea
                float penetracion = radio - Math.abs(dist);

                if (penetracion <= 0) continue; // no hay colisión

                // ------------------------------------------------
                // Determinar tipo de superficie y empujar
                // ------------------------------------------------
                // Si la normal apunta más en Y → piso o techo
                // Si la normal apunta más en X → pared
                float signo = (dist >= 0) ? 1f : -1f;

                if (Math.abs(normY) >= UMBRAL_HORIZONTAL) {
                    // PISO o TECHO
                    float empuje = penetracion * signo;
                    res.posY += empuje;

                    if (signo > 0) {
                        // Normal apunta hacia arriba → jugador está SOBRE la línea
                        res.enPiso = true;
                    } else {
                        // Normal apunta hacia abajo → jugador tocó TECHO
                        res.enTecho = true;
                    }

                } else {
                    // PARED
                    float empuje = penetracion * signo;
                    res.posX += empuje;

                    if (signo > 0) {
                        res.enParedDer = true;
                    } else {
                        res.enParedIzq = true;
                    }
                }

                // Actualizar AABB con la posición corregida
                jIzq = res.posX;
                jDer = res.posX + ancho;
                jAbj = res.posY;
                jArr = res.posY + alto;
            }
        }

        return res;
    }

    // ============================================================
    // HELPER: ¿hay piso debajo del jugador?
    // Útil para saber si puede saltar
    // Lanza un rayo hacia abajo desde el centro del jugador
    // ============================================================
    public boolean hayPisoAbajo(float posX, float posY,
                                float ancho, float alto,
                                float distancia) {

        float rayX  = posX + ancho / 2f; // centro horizontal
        float rayY0 = posY;              // base del jugador
        float rayY1 = posY - distancia;  // hacia abajo

        for (ArrayList<Vector2f> segmento : segmentos) {
            if (segmento.size() < 2) continue;

            for (int i = 0; i < segmento.size() - 1; i++) {
                Vector2f p1 = segmento.get(i);
                Vector2f p2 = segmento.get(i + 1);

                // Solo líneas aproximadamente horizontales
                float dy = Math.abs(p2.y - p1.y);
                float dx = Math.abs(p2.x - p1.x);
                if (dy > dx) continue; // más vertical que horizontal, ignorar

                // El rayo es vertical: rayX, entre rayY1 y rayY0
                float minX = Math.min(p1.x, p2.x);
                float maxX = Math.max(p1.x, p2.x);
                if (rayX < minX || rayX > maxX) continue;

                // Y interpolada del segmento en rayX
                float t    = (rayX - p1.x) / (p2.x - p1.x + 0.0001f);
                float segY = p1.y + t * (p2.y - p1.y);

                if (segY <= rayY0 && segY >= rayY1) {
                    return true;
                }
            }
        }
        return false;
    }
}