package com.mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;

import java.util.ArrayList;

public class Mapa1State extends BaseAppState {

    private SimpleApplication app;

    private Geometry geoMapa;
    private final float IMG_ANCHO_ORIGINAL = 3584f;
    private final float IMG_ALTO_ORIGINAL  = 240f;

    private ArrayList<Geometry> tilesFondo = new ArrayList<>();
    private final float BG_ANCHO_ORIGINAL = 512f;
    private final float BG_ALTO_ORIGINAL  = 240f;
    private float bgAncho;
    private float bgAlto;

    private final float PARALLAX = 0.3f;
    private final float ZOOM     = .9f;

    private float anchoFinal;
    private float altoFinal;
    private float escalaZoom;

    private float offsetX = 0f;

    private ArrayList<ArrayList<Vector2f>> segmentosColision = new ArrayList<>();

    // ── BLOQUES ──
    private ArrayList<CajaBloque>       bloques        = new ArrayList<>();
    private ArrayList<CajaBloqueOculto> bloquesOcultos = new ArrayList<>();

    // ── COLISION MAPA (referencia para el item) ──
    private ColisionMapa colisionMapaRef;

    // ── JUGADOR (referencia para detectar colisión con item) ──
    private Jugador jugadorRef;

    public void setJugador(Jugador jugador) {
        this.jugadorRef = jugador;
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;

        Camera cam = this.app.getCamera();
        float pantallaAncho = cam.getWidth();
        float pantallaAlto  = cam.getHeight();

        float escalaBase = pantallaAlto / IMG_ALTO_ORIGINAL;
        escalaZoom = escalaBase * ZOOM;

        altoFinal  = IMG_ALTO_ORIGINAL  * escalaZoom;
        anchoFinal = IMG_ANCHO_ORIGINAL * escalaZoom;
        bgAncho    = BG_ANCHO_ORIGINAL  * escalaZoom;
        bgAlto     = BG_ALTO_ORIGINAL   * escalaZoom;

        cam.setParallelProjection(true);
        cam.setFrustum(
                -1000f, 1000f,
                -pantallaAncho / 2f,
                 pantallaAncho / 2f,
                 pantallaAlto  / 2f,
                -pantallaAlto  / 2f
        );
        cam.setLocation(new Vector3f(
                pantallaAncho / 2f,
                pantallaAlto  / 2f,
                500f
        ));
        this.app.getFlyByCamera().setEnabled(false);

        int cantidadTiles = (int) Math.ceil(anchoFinal / bgAncho) + 2;
        float posYFondo   = (pantallaAlto - bgAlto) / 2f;

        for (int i = 0; i < cantidadTiles; i++) {
            Quad quad = new Quad(bgAncho, bgAlto);
            Geometry tile = new Geometry("FondoTile_" + i, quad);
            Material mat = new Material(app.getAssetManager(),
                    "Common/MatDefs/Misc/Unshaded.j3md");
            Texture tex = app.getAssetManager()
                    .loadTexture("Scenes/Fondo Mapa1.3.png");
            mat.setTexture("ColorMap", tex);
            tile.setMaterial(mat);
            tile.setLocalTranslation(i * bgAncho, posYFondo, 0f);
            tilesFondo.add(tile);
        }

        Quad quadMapa = new Quad(anchoFinal, altoFinal);
        geoMapa = new Geometry("Mapa1", quadMapa);
        Material matMapa = new Material(app.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        Texture texMapa = app.getAssetManager()
                .loadTexture("Interface/Mapa1.1.png");
        matMapa.setTexture("ColorMap", texMapa);
        matMapa.getAdditionalRenderState()
               .setBlendMode(RenderState.BlendMode.Alpha);
        geoMapa.setMaterial(matMapa);
        geoMapa.setQueueBucket(RenderQueue.Bucket.Transparent);
        geoMapa.setLocalTranslation(0f, (pantallaAlto - altoFinal) / 2f, 1f);

        cargarColisiones();
        crearBloques();

        System.out.println("Mapa cargado | Zoom: x" + ZOOM);
        System.out.println("Tiles de fondo: " + cantidadTiles);
        System.out.println("Segmentos de colision: " + segmentosColision.size());
        System.out.println("Bloques: " + bloques.size());
        System.out.println("Bloques ocultos: " + bloquesOcultos.size());
    }

    // ============================================================
    // BLOQUES NORMALES
    // ============================================================
    private void crearBloques() {
        float tam = 16f * escalaZoom;

        // índice 0 — tiene item; se reemplaza en setColisionMapa() con referencias completas
        bloques.add(new CajaBloque(this.app, 65f,    768f, tam));
        bloques.add(new CajaBloque(this.app, 1945f,  635f, tam));
        bloques.add(new CajaBloque(this.app, 2398f,  635f, tam));
        bloques.add(new CajaBloque(this.app, 2782f,  634f, tam));
        bloques.add(new CajaBloque(this.app, 6286f,  570f, tam));
        bloques.add(new CajaBloque(this.app, 7062f,  636f, tam));
        bloques.add(new CajaBloque(this.app, 7841f,  440f, tam));
        bloques.add(new CajaBloque(this.app, 8876f,  441f, tam));
        bloques.add(new CajaBloque(this.app, 8996f,  442f, tam));
        bloques.add(new CajaBloque(this.app, 9116f,  442f, tam));
        bloques.add(new CajaBloque(this.app, 9236f,  442f, tam));
        bloques.add(new CajaBloque(this.app, 9356f,  442f, tam));
        bloques.add(new CajaBloque(this.app, 9476f,  442f, tam));
        bloques.add(new CajaBloque(this.app, 9596f,  442f, tam));
        bloques.add(new CajaBloque(this.app, 9716f,  442f, tam));
        bloques.add(new CajaBloque(this.app, 9836f,  442f, tam));
        bloques.add(new CajaBloque(this.app, 9956f,  442f, tam));
        bloques.add(new CajaBloque(this.app, 10076f, 442f, tam));

        // ── BLOQUES OCULTOS ──
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 581f,  765f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 646f,  765f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 711f,  765f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 776f,  765f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 841f,  765f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 906f,  765f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 971f,  765f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 1036f, 765f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 1101f, 765f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 1167f, 765f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 4860f, 569f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 4925f, 569f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 4990f, 569f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 5055f, 569f, tam));
        bloquesOcultos.add(new CajaBloqueOculto(this.app, 8034f, 442f, tam));
    }

    // ============================================================
    // Llamar antes del primer update, una vez que ColisionMapa esté listo.
    // Reemplaza el bloque 0 con uno que tiene item y todas las referencias.
    // ============================================================
    public void setColisionMapa(ColisionMapa colisionMapa) {
        this.colisionMapaRef = colisionMapa;

        float tam = bloques.get(0).getTamano();
        float bx  = bloques.get(0).getMundoX();
        float by  = bloques.get(0).getMundoY();

        bloques.get(0).getNode().removeFromParent();

        // Constructor completo: pasa colisionMapa + ambas listas de bloques
        CajaBloque primerBloque = new CajaBloque(
                app, bx, by, tam,
                colisionMapa,
                bloques,
                bloquesOcultos);

        bloques.set(0, primerBloque);

        if (isEnabled()) {
            app.getRootNode().attachChild(primerBloque.getNode());
        }
    }

    // ============================================================
    // UPDATE BLOQUES NORMALES
    // ============================================================
    public boolean updateBloques(float jugX, float jugY,
                                 float jugAncho, float jugAlto,
                                 float velY, float tpf) {
        boolean algunGolpeado = false;
        for (CajaBloque bloque : bloques) {
            bloque.update(tpf);
            if (bloque.verificarGolpe(jugX, jugY, jugAncho, jugAlto, velY)) {
                algunGolpeado = true;
                System.out.println("¡Caja golpeada en ("
                        + (int) bloque.getMundoX() + ", "
                        + (int) bloque.getMundoY() + ")!");
            }

            // ── UPDATE Y COLISIÓN DEL ITEM si existe ──
            ItemComida item = bloque.getItemSpawn();
            if (item != null && item.isVivo()) {
                item.update(tpf);

                if (jugadorRef != null &&
                    item.tocaJugador(jugadorRef.getPosX(), jugadorRef.getPosY(),
                                     jugadorRef.getAncho(), jugadorRef.getAlto())) {
                    item.recoger();
                    jugadorRef.transformar();
                    System.out.println("¡Item recogido! Nyx se transforma.");
                }
            }
        }
        return algunGolpeado;
    }

    public void resolverColisionBloques(ColisionMapa.Resultado res,
                                        float ancho, float alto) {
        for (CajaBloque bloque : bloques) {
            if (!bloque.colisionaConJugador(res.posX, res.posY, ancho, alto))
                continue;

            float jugCX  = res.posX + ancho / 2f;
            float jugCY  = res.posY + alto  / 2f;
            float bloqCX = bloque.getMundoX() + bloque.getTamano() / 2f;
            float bloqCY = bloque.getMundoY() + bloque.getTamano() / 2f;

            float overlapX = (ancho  + bloque.getTamano()) / 2f - Math.abs(jugCX - bloqCX);
            float overlapY = (alto   + bloque.getTamano()) / 2f - Math.abs(jugCY - bloqCY);

            if (overlapX > 0 && overlapY > 0) {
                if (overlapY < overlapX) {
                    if (jugCY > bloqCY) {
                        res.posY   = bloque.getMundoY() + bloque.getTamano();
                        res.enPiso = true;
                    } else {
                        res.posY    = bloque.getMundoY() - alto;
                        res.enTecho = true;
                    }
                } else {
                    if (jugCX > bloqCX) {
                        res.posX       = bloque.getMundoX() + bloque.getTamano();
                        res.enParedIzq = true;
                    } else {
                        res.posX       = bloque.getMundoX() - ancho;
                        res.enParedDer = true;
                    }
                }
            }
        }
    }

    // ============================================================
    // UPDATE BLOQUES OCULTOS
    // ============================================================
    public boolean updateBloquesOcultos(float jugX, float jugY,
                                         float jugAncho, float jugAlto,
                                         float velY, float tpf) {
        boolean algunGolpeado = false;
        for (CajaBloqueOculto b : bloquesOcultos) {
            b.update(tpf);
            if (b.verificarGolpe(jugX, jugY, jugAncho, jugAlto, velY)) {
                algunGolpeado = true;
                System.out.println("¡Bloque oculto revelado en ("
                        + (int) b.getMundoX() + ", "
                        + (int) b.getMundoY() + ")!");
            }
        }
        return algunGolpeado;
    }

    public void resolverColisionBloquesOcultos(ColisionMapa.Resultado res,
                                                float ancho, float alto) {
        for (CajaBloqueOculto b : bloquesOcultos) {
            if (!b.colisionaConJugador(res.posX, res.posY, ancho, alto)) continue;

            float jugCX  = res.posX + ancho / 2f;
            float jugCY  = res.posY + alto  / 2f;
            float bloqCX = b.getMundoX() + b.getTamano() / 2f;
            float bloqCY = b.getMundoY() + b.getTamano() / 2f;

            float overlapX = (ancho + b.getTamano()) / 2f - Math.abs(jugCX - bloqCX);
            float overlapY = (alto  + b.getTamano()) / 2f - Math.abs(jugCY - bloqCY);

            if (overlapX > 0 && overlapY > 0) {
                if (overlapY < overlapX) {
                    if (jugCY > bloqCY) {
                        res.posY   = b.getMundoY() + b.getTamano();
                        res.enPiso = true;
                    } else {
                        res.posY    = b.getMundoY() - alto;
                        res.enTecho = true;
                    }
                } else {
                    if (jugCX > bloqCX) {
                        res.posX       = b.getMundoX() + b.getTamano();
                        res.enParedIzq = true;
                    } else {
                        res.posX       = b.getMundoX() - ancho;
                        res.enParedDer = true;
                    }
                }
            }
        }
    }

    public ArrayList<CajaBloque>       getBloques()        { return bloques;        }
    public ArrayList<CajaBloqueOculto> getBloquesOcultos() { return bloquesOcultos; }

    // ============================================================
    // CÁMARA
    // ============================================================
    public void moverCamara(float xJugador) {
        Camera cam = app.getCamera();
        float pantallaAncho = cam.getWidth();
        moverCamaraOffset(xJugador - (pantallaAncho / 2f));
    }

    public void moverCamaraOffset(float borde) {
        Camera cam = app.getCamera();
        float pantallaAncho = cam.getWidth();
        float pantallaAlto  = cam.getHeight();

        offsetX = Math.max(0, borde);
        offsetX = Math.min(anchoFinal - pantallaAncho, offsetX);

        cam.setLocation(new Vector3f(
                offsetX + pantallaAncho / 2f,
                pantallaAlto / 2f,
                500f
        ));

        float posYFondo = (pantallaAlto - bgAlto) / 2f;
        for (int i = 0; i < tilesFondo.size(); i++) {
            tilesFondo.get(i).setLocalTranslation(
                    (i * bgAncho) + (offsetX * PARALLAX),
                    posYFondo, 0f
            );
        }
    }

    public float getOffsetX() { return offsetX; }

    public ArrayList<ArrayList<Vector2f>> getSegmentosColision() {
        return segmentosColision;
    }

    // ============================================================
    // COLISIONES
    // ============================================================
    private static Vector2f v(float x, float y) { return new Vector2f(x, y); }

    private static ArrayList<Vector2f> seg(Vector2f... puntos) {
        ArrayList<Vector2f> s = new ArrayList<>();
        for (Vector2f p : puntos) s.add(p);
        return s;
    }

    private void cargarColisiones() {

        // ── S1 ──
        segmentosColision.add(seg(v(66,569), v(393,569), v(791,569), v(1476,569), v(1490,569)));
        // ── S2 ──
        segmentosColision.add(seg(v(1583,439), v(1554,439), v(1725,439), v(2145,439), v(2845,437), v(2852,438)));
        // ── S3 ──
        segmentosColision.add(seg(v(2929,630), v(2916,634), v(3082,634), v(3652,634), v(4177,634), v(4842,634), v(4853,634)));
        // ── S4 ──
        segmentosColision.add(seg(v(4860,441), v(5109,441)));
        // ── S5 ──
        segmentosColision.add(seg(v(5119,247), v(5767,247), v(5770,247), v(5113,248)));
        // ── S6 ──
        segmentosColision.add(seg(v(5773,375), v(5766,375), v(6144,375), v(6479,375), v(6482,375)));
        // ── S7 ──
        segmentosColision.add(seg(v(6479,441), v(6679,441), v(7169,441), v(7191,441)));
        // ── S8 ──
        segmentosColision.add(seg(v(7192,377), v(7428,377), v(7698,376), v(7709,376)));
        // ── S9 ──
        segmentosColision.add(seg(v(7320,570), v(7355,570), v(7382,569), v(7385,569)));
        // ── S10 ──
        segmentosColision.add(seg(v(7384,505), v(7609,506), v(7838,506)));
        // ── S11 ──
        segmentosColision.add(seg(v(7836,311), v(8216,311), v(9206,311), v(9801,311), v(10091,311), v(10106,313)));
        // ── S12 ──
        segmentosColision.add(seg(v(10191,507), v(10172,507), v(10241,504), v(10290,506), v(10587,505), v(11097,505), v(11752,505), v(12023,505), v(12358,505), v(12375,507)));
        // ── S13 ──
        segmentosColision.add(seg(v(12571,440), v(12621,440), v(12801,440), v(12821,440), v(12828,440)));
        // ── S14 ──
        segmentosColision.add(seg(v(13024,506), v(13103,506), v(13150,505)));
        // ── S15 ──
        segmentosColision.add(seg(v(13349,570), v(13431,570), v(13507,570), v(13604,570)));
        // ── S16 ──
        segmentosColision.add(seg(v(13799,765), v(13747,766), v(13737,767)));
        // ── S17 ──
        segmentosColision.add(seg(v(13800,376), v(13926,375), v(14121,377)));
        // ── S18 ──
        segmentosColision.add(seg(v(14127,441), v(14189,443)));
        // ── S19 ──
        segmentosColision.add(seg(v(14192,506), v(14252,506)));
        // ── S20 ──
        segmentosColision.add(seg(v(14192,375), v(14316,375), v(14514,378)));
        // ── S21 ──
        segmentosColision.add(seg(v(10434,766), v(10487,766), v(10497,766)));
        // ── S22 ──
        segmentosColision.add(seg(v(10494,635), v(10584,636), v(10691,635)));
        // ── S23 ──
        segmentosColision.add(seg(v(10691,764), v(10721,766), v(10754,765)));
        // ── S24 ──
        segmentosColision.add(seg(v(8874,506), v(8759,507), v(8605,506), v(8332,505), v(8100,507)));
        // ── S25 ──
        segmentosColision.add(seg(v(7905,506), v(7970,507)));
        // ── S26 ──
        segmentosColision.add(seg(v(7979,570), v(8001,570), v(8032,570)));
        // ── S27 ──
        segmentosColision.add(seg(v(7127,701), v(7157,702), v(7192,700)));
        // ── S28 ──
        segmentosColision.add(seg(v(7191,765), v(7223,766), v(7256,766)));
        // ── S29 ──
        segmentosColision.add(seg(v(7060,701), v(6936,702), v(6656,702), v(6445,702), v(6425,702), v(6415,702)));
        // ── S30 ──
        segmentosColision.add(seg(v(6351,637), v(6379,637), v(6415,637)));
        // ── S31 ──
        segmentosColision.add(seg(v(6283,635), v(6228,635), v(6063,635), v(5848,635), v(5598,635), v(5293,635), v(5128,635), v(5120,635)));
        // ── S32 ──
        segmentosColision.add(seg(v(2783,699), v(2733,699), v(2668,699), v(2563,699), v(2528,699)));
        // ── S33 ──
        segmentosColision.add(seg(v(2396,700), v(2336,700), v(2216,700), v(2096,700), v(2074,700)));
        // ── S34 ──
        segmentosColision.add(seg(v(1941,700), v(1903,701), v(1878,701), v(1798,701), v(1748,701)));
        // ── S35 ──
        segmentosColision.add(seg(v(1231,830), v(1341,830), v(1461,830), v(1601,830), v(1725,830), v(1747,830)));
        // ── S36 ──
        segmentosColision.add(seg(v(582,830), v(463,830), v(295,830), v(158,830), v(131,830)));
        // ── S37 ──
        segmentosColision.add(seg(v(61,830), v(9,831), v(0,831)));

        // ── T1 ──
        segmentosColision.add(seg(v(131,771), v(170,771), v(330,771), v(440,771), v(530,771), v(570,771), v(579,771)));
        // ── T2 ──
        segmentosColision.add(seg(v(1232,771), v(1257,771), v(1352,771), v(1442,771), v(1526,771), v(1601,771), v(1667,771), v(1687,771)));
        // ── T3 ──
        segmentosColision.add(seg(v(1686,642), v(1760,642), v(1861,642), v(1911,642), v(1940,642)));
        // ── T4 ──
        segmentosColision.add(seg(v(2075,642), v(2275,642), v(2335,642), v(2390,642), v(2394,642)));
        // ── T5 ──
        segmentosColision.add(seg(v(2529,642), v(2559,642), v(2639,642), v(2718,642), v(2759,642), v(2783,642)));
        // ── T6 ──
        segmentosColision.add(seg(v(3436,705), v(3476,705), v(3581,705), v(3676,705), v(3688,705)));
        // ── T7 ──
        segmentosColision.add(seg(v(4214,706), v(4249,706), v(4404,706), v(4434,706), v(4467,706)));
        // ── T8 ──
        segmentosColision.add(seg(v(5121,578), v(5201,578), v(5401,578), v(5656,578), v(5886,578), v(6081,578), v(6221,578), v(6251,578), v(6281,578)));
        // ── T9 ──
        segmentosColision.add(seg(v(6351,578), v(6431,578), v(6471,578), v(6475,578)));
        // ── T10 ──
        segmentosColision.add(seg(v(6478,643), v(6543,643), v(6658,643), v(6748,643), v(6848,643), v(7018,643), v(7043,643), v(7057,643)));
        // ── T11 ──
        segmentosColision.add(seg(v(7129,644), v(7164,644), v(7233,644), v(7252,644)));
        // ── T12 ──
        segmentosColision.add(seg(v(7518,577), v(7573,577), v(7618,577), v(7643,577)));
        // ── T12.5 ──
        segmentosColision.add(seg(v(7642,707), v(7656,707), v(7712,707), v(7778,707)));
        // ── T13 ──
        segmentosColision.add(seg(v(7778,641), v(7818,641), v(7883,641), v(7899,642)));
        // ── T14 ──
        segmentosColision.add(seg(v(7908,448), v(7948,448), v(8028,448), v(8030,448)));
        // ── T15 ──
        segmentosColision.add(seg(v(8102,448), v(8192,448), v(8362,448), v(8662,448), v(8862,448), v(8873,448)));
        // ── T16 ──
        segmentosColision.add(seg(v(10564,707), v(10614,707), v(10621,707)));
        // ── T17 ──
        segmentosColision.add(seg(v(10435,576), v(10485,576), v(10610,576), v(10696,576), v(10741,576), v(10750,577)));
        // ── T18 ──
        segmentosColision.add(seg(v(12574,707), v(12814,707), v(13054,707), v(13297,707), v(13539,707), v(13599,707)));
        // ── T19 ──
        segmentosColision.add(seg(v(13871,707), v(13925,707)));
        // ── T20 ──
        segmentosColision.add(seg(v(14194,446), v(14220,445), v(14250,447)));
        // ── T21 ──
        segmentosColision.add(seg(
            v(14514,901), v(14456,901), v(14316,901), v(14166,901),
            v(13981,901), v(13786,901), v(13616,901), v(13451,901),
            v(13266,901), v(13041,901), v(12782,901), v(12762,901),
            v(12601,901), v(12472,901), v(12336,901), v(12101,901),
            v(11836,901), v(11671,901), v(11521,901), v(11351,901),
            v(11177,901), v(10981,901), v(10806,901), v(10637,901),
            v(10482,901), v(10317,901), v(10142,901), v(9987,901),
            v(9837,901),  v(9687,901),  v(9537,901),  v(9382,901),
            v(9217,901),  v(9012,901),  v(8827,901),  v(8617,901),
            v(8372,901),  v(8128,901),  v(7902,901),  v(7733,901),
            v(7523,901),  v(7333,901),  v(7083,901),  v(6863,901),
            v(6623,901),  v(6333,901),  v(5978,901),  v(5823,901),
            v(5553,901),  v(5418,901),  v(5228,901),  v(5068,901),
            v(4868,901),  v(4669,901),  v(4474,901),  v(4284,901),
            v(4119,901),  v(3949,901),  v(3759,901),  v(3564,901),
            v(3384,901),  v(3179,901),  v(3004,901),  v(2809,901),
            v(2624,901),  v(2444,901),  v(2264,901),  v(2089,901),
            v(1930,901),  v(1744,901),  v(1684,901),  v(1505,902),
            v(864,902),   v(178,901),   v(0,901)
        ));
        // ── T22 ──
        segmentosColision.add(seg(v(13605,511), v(13517,511), v(13412,511), v(13367,511), v(13351,511)));
        // ── T23 ──
        segmentosColision.add(seg(v(13150,447), v(13091,447), v(13025,448)));
        // ── T24 ──
        segmentosColision.add(seg(v(12826,384), v(12731,384), v(12646,384), v(12606,384), v(12576,384)));
        // ── T25 ──
        segmentosColision.add(seg(v(7776,446), v(7681,446), v(7532,446), v(7422,446), v(7342,446), v(7326,447)));

        // ── P1 ──
        segmentosColision.add(seg(v(133,826), v(133,801), v(133,774)));
        // ── P2 ──
        segmentosColision.add(seg(v(62,831), v(60,776), v(62,744), v(61,673), v(61,572)));
        // ── P3 ──
        segmentosColision.add(seg(v(580,830), v(580,797), v(580,771)));
        // ── P4 ──
        segmentosColision.add(seg(v(1232,830), v(1232,796), v(1232,771)));
        // ── P5 ──
        segmentosColision.add(seg(v(1487,567), v(1487,506), v(1487,440), v(1487,336), v(1487,217), v(1487,119), v(1487,56)));
        // ── P6 ──
        segmentosColision.add(seg(v(1556,436), v(1555,408), v(1557,376), v(1557,330), v(1559,238), v(1556,162), v(1556,56)));
        // ── P7 ──
        segmentosColision.add(seg(v(1686,769), v(1686,731), v(1685,688), v(1685,642)));
        // ── P8 ──
        segmentosColision.add(seg(v(1748,827), v(1746,790), v(1747,754), v(1747,704)));
        // ── P9 ──
        segmentosColision.add(seg(v(1937,698), v(1937,668), v(1937,643)));
        // ── P10 ──
        segmentosColision.add(seg(v(2078,698), v(2078,668), v(2078,643)));
        // ── P11 ──
        segmentosColision.add(seg(v(2390,698), v(2390,668), v(2390,643)));
        // ── P12 ──
        segmentosColision.add(seg(v(2527,698), v(2527,668), v(2527,643)));
        // ── P13 ──
        segmentosColision.add(seg(v(2783,698), v(2783,668), v(2782,643)));
        // ── P14 ──
        segmentosColision.add(seg(v(2847,437), v(2847,402), v(2847,358), v(2847,197), v(2847,55)));
        // ── P15 ──
        segmentosColision.add(seg(v(2923,56), v(2923,129), v(2923,262), v(2923,391), v(2923,532), v(2923,617), v(2923,633)));
        // ── P16 ──
        segmentosColision.add(seg(v(3435,898), v(3435,850), v(3434,760), v(3434,709)));
        // ── P17 ──
        segmentosColision.add(seg(v(3689,708), v(3689,731), v(3689,788), v(3689,853), v(3689,897)));
        // ── P18 ──
        segmentosColision.add(seg(v(4215,897), v(4214,859), v(4214,814), v(4214,762), v(4214,731), v(4213,709)));
        // ── P19 ──
        segmentosColision.add(seg(v(4467,710), v(4467,748), v(4467,807), v(4467,861), v(4467,900)));
        // ── P20 ──
        segmentosColision.add(seg(v(4855,442), v(4853,509), v(4854,565), v(4854,613), v(4854,632)));
        // ── P21 ──
        segmentosColision.add(seg(v(5121,578), v(5119,628), v(5118,633)));
        // ── P22 ──
        segmentosColision.add(seg(v(5114,248), v(5113,283), v(5113,365), v(5113,413), v(5112,438)));
        // ── P23 ──
        segmentosColision.add(seg(v(5770,251), v(5770,317), v(5770,373)));
        // ── P24 ──
        segmentosColision.add(seg(v(6479,382), v(6480,411), v(6480,439)));
        // ── P25 ──
        segmentosColision.add(seg(v(6282,635), v(6282,609), v(6281,576)));
        // ── P26 ──
        segmentosColision.add(seg(v(6351,637), v(6352,603), v(6351,577)));
        // ── P27 ──
        segmentosColision.add(seg(v(6415,640), v(6415,672), v(6415,697)));
        // ── P28 ──
        segmentosColision.add(seg(v(6476,640), v(6477,604), v(6477,576)));
        // ── P29 ──
        segmentosColision.add(seg(v(7060,699), v(7059,672), v(7058,642)));
        // ── P30 ──
        segmentosColision.add(seg(v(7129,701), v(7129,671), v(7128,641)));
        // ── P31 ──
        segmentosColision.add(seg(v(7195,704), v(7195,730), v(7194,762)));
        // ── P32 ──
        segmentosColision.add(seg(v(7254,764), v(7255,728), v(7254,695), v(7253,642)));
        // ── P33 ──
        segmentosColision.add(seg(v(7187,381), v(7188,410), v(7188,438)));
        // ── P34 ──
        segmentosColision.add(seg(v(7323,568), v(7323,517), v(7323,454)));
        // ── P34.2 ──
        segmentosColision.add(seg(v(7379,562), v(7379,508)));
        // ── P35 ──
        segmentosColision.add(seg(v(7706,374), v(7706,335), v(7705,264), v(7704,182), v(7704,127), v(7706,58)));
        // ── P36 ──
        segmentosColision.add(seg(v(7777,56), v(7777,115), v(7777,165), v(7778,258), v(7777,347), v(7778,445)));
        // ── P37 ──
        segmentosColision.add(seg(v(7519,899), v(7519,849), v(7519,738), v(7517,648), v(7517,582)));
        // ── P38 ──
        segmentosColision.add(seg(v(7645,701), v(7645,676), v(7645,640), v(7644,583)));
        // ── P39 ──
        segmentosColision.add(seg(v(7779,702), v(7779,677), v(7779,644)));
        // ── P40 ──
        segmentosColision.add(seg(v(7904,899), v(7903,843), v(7902,767), v(7901,691), v(7901,644)));
        // ── P41 ──
        segmentosColision.add(seg(v(7837,505), v(7837,457), v(7837,387), v(7837,312)));
        // ── P42 ──
        segmentosColision.add(seg(v(7906,506), v(7906,473), v(7906,449)));
        // ── P42.2 ──
        segmentosColision.add(seg(v(7976,510), v(7976,525), v(7976,563)));
        // ── P43 ──
        segmentosColision.add(seg(v(8031,570), v(8031,525), v(8030,449)));
        // ── P44 ──
        segmentosColision.add(seg(v(8102,506), v(8102,488), v(8102,449)));
        // ── P45 ──
        segmentosColision.add(seg(v(8874,505), v(8873,481), v(8873,450)));
        // ── P46 ──
        segmentosColision.add(seg(v(10104,311), v(10104,271), v(10104,174), v(10103,57)));
        // ── P47 ──
        segmentosColision.add(seg(v(10175,55), v(10176,94), v(10176,167), v(10176,312), v(10176,402), v(10175,495), v(10173,501)));
        // ── P48 ──
        segmentosColision.add(seg(v(10434,760), v(10434,713), v(10433,625), v(10433,582)));
        // ── P49 ──
        segmentosColision.add(seg(v(10494,762), v(10492,704), v(10493,639)));
        // ── P50 ──
        segmentosColision.add(seg(v(10694,762), v(10694,737), v(10694,663), v(10694,639)));
        // ── P51 ──
        segmentosColision.add(seg(v(10564,898), v(10564,845), v(10564,742), v(10563,713)));
        // ── P52 ──
        segmentosColision.add(seg(v(10625,900), v(10626,824), v(10625,747), v(10625,713)));
        // ── P53 ──
        segmentosColision.add(seg(v(10754,762), v(10754,720), v(10754,645), v(10754,591), v(10752,582)));
        // ── P54 ──
        segmentosColision.add(seg(v(12374,504), v(12374,453), v(12373,373), v(12374,270), v(12372,159), v(12372,82), v(12373,57)));
        // ── P55 ──
        segmentosColision.add(seg(v(12572,898), v(12572,824), v(12573,735), v(12571,713)));
        // ── P56 ──
        segmentosColision.add(seg(v(12572,442), v(12571,382)));
        // ── P57 ──
        segmentosColision.add(seg(v(12827,442), v(12827,415), v(12827,383)));
        // ── P58 ──
        segmentosColision.add(seg(v(13026,506), v(13026,483), v(13024,448)));
        // ── P59 ──
        segmentosColision.add(seg(v(13152,505), v(13152,470), v(13151,449)));
        // ── P60 ──
        segmentosColision.add(seg(v(13350,571), v(13350,538), v(13350,513)));
        // ── P61 ──
        segmentosColision.add(seg(v(13604,568), v(13604,550), v(13605,531), v(13605,512)));
        // ── P62 ──
        segmentosColision.add(seg(v(13604,899), v(13604,856), v(13605,813), v(13603,775), v(13603,718)));
        // ── P63 ──
        segmentosColision.add(seg(v(13739,757), v(13740,608), v(13738,495), v(13738,338), v(13739,249), v(13739,157), v(13739,93), v(13737,55)));
        // ── P64 ──
        segmentosColision.add(seg(v(13801,765), v(13800,682), v(13801,532), v(13800,357), v(13799,288), v(13799,175), v(13801,69), v(13800,56)));
        // ── P65 ──
        segmentosColision.add(seg(v(13868,898), v(13869,824), v(13869,754), v(13868,713)));
        // ── P66 ──
        segmentosColision.add(seg(v(13930,900), v(13929,833), v(13930,758), v(13930,713)));
        // ── P67 ──
        segmentosColision.add(seg(v(14125,382), v(14125,408), v(14126,438)));
        // ── P68 ──
        segmentosColision.add(seg(v(14190,448), v(14190,503)));
        // ── P69 ──
        segmentosColision.add(seg(v(14251,504), v(14251,453)));
        // ── P70 ──
        segmentosColision.add(seg(v(14190,380), v(14189,443)));
        // ── P71 ──
        segmentosColision.add(seg(v(14514,379), v(14514,489), v(14514,623), v(14514,896)));
    }

    @Override
    protected void onEnable() {
        for (Geometry tile : tilesFondo)
            app.getRootNode().attachChild(tile);
        app.getRootNode().attachChild(geoMapa);
        for (CajaBloque b : bloques)
            app.getRootNode().attachChild(b.getNode());
        for (CajaBloqueOculto b : bloquesOcultos)
            app.getRootNode().attachChild(b.getNode());
    }

    @Override
    protected void onDisable() {
        geoMapa.removeFromParent();
        for (Geometry tile : tilesFondo)
            tile.removeFromParent();
        for (CajaBloque b : bloques)
            b.getNode().removeFromParent();
        for (CajaBloqueOculto b : bloquesOcultos)
            b.getNode().removeFromParent();
    }

    @Override
    protected void cleanup(Application app) {
        for (CajaBloque b : bloques)
            b.destruir();
        for (CajaBloqueOculto b : bloquesOcultos)
            b.destruir();
        tilesFondo.clear();
        segmentosColision.clear();
        bloques.clear();
        bloquesOcultos.clear();
    }
}