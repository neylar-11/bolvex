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

    private float offsetX = 0f;

    // =========================
    // COLISIONES
    // =========================
    private ArrayList<ArrayList<Vector2f>> segmentosColision = new ArrayList<>();

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;

        Camera cam = this.app.getCamera();
        float pantallaAncho = cam.getWidth();
        float pantallaAlto  = cam.getHeight();

        float escalaBase = pantallaAlto / IMG_ALTO_ORIGINAL;

        altoFinal  = IMG_ALTO_ORIGINAL  * escalaBase * ZOOM;
        anchoFinal = IMG_ANCHO_ORIGINAL * escalaBase * ZOOM;
        bgAncho    = BG_ANCHO_ORIGINAL  * escalaBase * ZOOM;
        bgAlto     = BG_ALTO_ORIGINAL   * escalaBase * ZOOM;

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

            Material mat = new Material(
                    app.getAssetManager(),
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

        Material matMapa = new Material(
                app.getAssetManager(),
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

        System.out.println("Mapa cargado | Zoom: x" + ZOOM);
        System.out.println("Tiles de fondo: " + cantidadTiles);
        System.out.println("Segmentos de colision: " + segmentosColision.size());
    }

    // =========================
    // MOVER CÁMARA POR CENTRO
    // =========================
    public void moverCamara(float xJugador) {
        Camera cam = app.getCamera();
        float pantallaAncho = cam.getWidth();
        moverCamaraOffset(xJugador - (pantallaAncho / 2f));
    }

    // =========================
    // MOVER CÁMARA POR BORDE IZQUIERDO
    // =========================
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
                    posYFondo,
                    0f
            );
        }
    }

    public float getOffsetX() { return offsetX; }

    public ArrayList<ArrayList<Vector2f>> getSegmentosColision() {
        return segmentosColision;
    }

    // =========================
    // HELPERS
    // =========================
    private static Vector2f v(float x, float y) {
        return new Vector2f(x, y);
    }

    private static ArrayList<Vector2f> seg(Vector2f... puntos) {
        ArrayList<Vector2f> s = new ArrayList<>();
        for (Vector2f p : puntos) s.add(p);
        return s;
    }

    // =========================
    // COLISIONES DEL MAPA 1
    // 137 segmentos capturados con Coordenadas
    // =========================
    private void cargarColisiones() {

        // ── Seg 1 ──
        segmentosColision.add(seg(
            v(66,570), v(204,570), v(468,570), v(688,571),
            v(981,571), v(1461,572), v(1481,572), v(1485,567), v(1486,564)
        ));
        // ── Seg 2 ──
        segmentosColision.add(seg(
            v(1486,564), v(1487,514), v(1487,476), v(1486,336),
            v(1488,194), v(1487,66), v(1487,56)
        ));
        // ── Seg 3 ──
        segmentosColision.add(seg(
            v(1,824), v(6,830), v(14,832), v(61,832)
        ));
        // ── Seg 4 ──
        segmentosColision.add(seg(
            v(61,832), v(63,796), v(62,737), v(62,638), v(63,573)
        ));
        // ── Seg 5 ──
        segmentosColision.add(seg(
            v(130,832), v(186,833), v(274,831), v(348,832),
            v(411,832), v(482,833), v(550,832), v(581,831)
        ));
        // ── Seg 6 ──
        segmentosColision.add(seg(
            v(581,831), v(581,808), v(579,771)
        ));
        // ── Seg 7 ──
        segmentosColision.add(seg(
            v(578,771), v(529,769), v(477,770),
            v(388,770), v(194,770), v(131,771)
        ));
        // ── Seg 8 ──
        segmentosColision.add(seg(
            v(131,771), v(130,835)
        ));
        // ── Seg 9 ──
        segmentosColision.add(seg(
            v(1230,832), v(1376,832), v(1658,833),
            v(1738,832), v(1742,828), v(1748,824)
        ));
        // ── Seg 10 ──
        segmentosColision.add(seg(
            v(1231,821), v(1231,794), v(1231,771), v(1229,832)
        ));
        // ── Seg 11 ──
        segmentosColision.add(seg(
            v(1231,771), v(1300,770), v(1392,768),
            v(1499,771), v(1601,769), v(1685,769)
        ));
        // ── Seg 12 ──
        segmentosColision.add(seg(
            v(1685,769), v(1685,726), v(1686,641)
        ));
        // ── Seg 13 ──
        segmentosColision.add(seg(
            v(1554,55), v(1554,110), v(1554,241),
            v(1556,368), v(1554,413), v(1554,433)
        ));
        // ── Seg 14 ──
        segmentosColision.add(seg(
            v(1554,433), v(1560,441), v(1567,443), v(1578,442),
            v(1598,443), v(1668,442), v(2059,443), v(2424,441),
            v(2644,442), v(2775,444), v(2833,443), v(2843,441), v(2845,436)
        ));
        // ── Seg 15 ──
        segmentosColision.add(seg(
            v(2847,435), v(2847,422), v(2847,367),
            v(2847,277), v(2846,178), v(2848,90), v(2846,59)
        ));
        // ── Seg 16 ──
        segmentosColision.add(seg(
            v(1749,822), v(1749,798), v(1748,770),
            v(1747,739), v(1747,704)
        ));
        // ── Seg 17 ──
        segmentosColision.add(seg(
            v(1747,702), v(1811,703), v(1941,702)
        ));
        // ── Seg 18 ──
        segmentosColision.add(seg(
            v(1941,702), v(1940,678), v(1941,640)
        ));
        // ── Seg 19 ──
        segmentosColision.add(seg(
            v(1941,640), v(1908,640), v(1686,643)
        ));
        // ── Seg 20 ──
        segmentosColision.add(seg(
            v(2073,702), v(2395,703)
        ));
        // ── Seg 21 ──
        segmentosColision.add(seg(
            v(2395,703), v(2397,642)
        ));
        // ── Seg 22 ──
        segmentosColision.add(seg(
            v(2394,641), v(2075,641)
        ));
        // ── Seg 23 ──
        segmentosColision.add(seg(
            v(2075,641), v(2073,701)
        ));
        // ── Seg 24 ──
        segmentosColision.add(seg(
            v(2527,701), v(2784,702)
        ));
        // ── Seg 25 ──
        segmentosColision.add(seg(
            v(2784,702), v(2783,641)
        ));
        // ── Seg 26 ──
        segmentosColision.add(seg(
            v(2783,641), v(2529,643)
        ));
        // ── Seg 27 ──
        segmentosColision.add(seg(
            v(2529,643), v(2528,700)
        ));
        // ── Seg 28 ──
        segmentosColision.add(seg(
            v(2915,57), v(2917,204), v(2918,289), v(2918,366),
            v(2917,433), v(2918,519), v(2917,606), v(2917,628),
            v(2923,635), v(2924,638)
        ));
        // ── Seg 29 ──
        segmentosColision.add(seg(
            v(2924,638), v(3062,636), v(3782,638),
            v(4462,638), v(4850,637), v(4855,631), v(4858,628)
        ));
        // ── Seg 30 ──
        segmentosColision.add(seg(
            v(4858,627), v(4856,444)
        ));
        // ── Seg 31 ──
        segmentosColision.add(seg(
            v(4856,444), v(5108,444), v(5112,440),
            v(5117,435), v(5117,434), v(5118,382), v(5117,251)
        ));
        // ── Seg 32 ──
        segmentosColision.add(seg(
            v(5113,249), v(5766,250)
        ));
        // ── Seg 33 ──
        segmentosColision.add(seg(
            v(5766,250), v(5766,370), v(5771,375), v(5774,376)
        ));
        // ── Seg 34 ──
        segmentosColision.add(seg(
            v(5117,577), v(5118,630), v(5125,635),
            v(5129,638), v(5217,638), v(5788,638), v(6284,636)
        ));
        // ── Seg 35 ──
        segmentosColision.add(seg(
            v(6284,636), v(6283,577)
        ));
        // ── Seg 36 ──
        segmentosColision.add(seg(
            v(6283,577), v(5786,576), v(5119,576)
        ));
        // ── Seg 37 ──
        segmentosColision.add(seg(
            v(5774,379), v(6479,380)
        ));
        // ── Seg 38 ──
        segmentosColision.add(seg(
            v(6479,380), v(6479,435), v(6485,440), v(6490,443)
        ));
        // ── Seg 39 ──
        segmentosColision.add(seg(
            v(6490,443), v(7183,443), v(7188,437), v(7190,433)
        ));
        // ── Seg 40 ──
        segmentosColision.add(seg(
            v(7190,433), v(7191,381)
        ));
        // ── Seg 41 ──
        segmentosColision.add(seg(
            v(6478,637), v(6476,576)
        ));
        // ── Seg 42 ──
        segmentosColision.add(seg(
            v(6476,576), v(6350,577)
        ));
        // ── Seg 43 ──
        segmentosColision.add(seg(
            v(6350,637), v(6349,577)
        ));
        // ── Seg 44 ──
        segmentosColision.add(seg(
            v(6350,639), v(6413,639)
        ));
        // ── Seg 45 ──
        segmentosColision.add(seg(
            v(6413,639), v(6414,693), v(6421,699), v(6422,704)
        ));
        // ── Seg 46 ──
        segmentosColision.add(seg(
            v(6422,704), v(7061,703)
        ));
        // ── Seg 47 ──
        segmentosColision.add(seg(
            v(7061,703), v(7060,640)
        ));
        // ── Seg 48 ──
        segmentosColision.add(seg(
            v(7060,639), v(6482,640), v(6478,640)
        ));
        // ── Seg 49 ──
        segmentosColision.add(seg(
            v(7191,379), v(7700,379), v(7705,372), v(7708,369)
        ));
        // ── Seg 50 ──
        segmentosColision.add(seg(
            v(7708,369), v(7709,57)
        ));
        // ── Seg 51 ──
        segmentosColision.add(seg(
            v(7128,703), v(7127,641)
        ));
        // ── Seg 52 ──
        segmentosColision.add(seg(
            v(7255,640), v(7129,643)
        ));
        // ── Seg 53 ──
        segmentosColision.add(seg(
            v(7128,702), v(7194,704)
        ));
        // ── Seg 54 ──
        segmentosColision.add(seg(
            v(7191,703), v(7191,757), v(7195,763), v(7201,768)
        ));
        // ── Seg 55 ──
        segmentosColision.add(seg(
            v(7201,768), v(7255,768)
        ));
        // ── Seg 56 ──
        segmentosColision.add(seg(
            v(7258,767), v(7254,641)
        ));
        // ── Seg 57 ──
        segmentosColision.add(seg(
            v(7777,56), v(7776,443)
        ));
        // ── Seg 58 ──
        segmentosColision.add(seg(
            v(7776,443), v(7331,443), v(7326,449), v(7322,450)
        ));
        // ── Seg 59 ──
        segmentosColision.add(seg(
            v(7322,450), v(7321,564), v(7326,568), v(7332,573)
        ));
        // ── Seg 60 ──
        segmentosColision.add(seg(
            v(7332,573), v(7385,572)
        ));
        // ── Seg 61 ──
        segmentosColision.add(seg(
            v(7385,572), v(7385,510)
        ));
        // ── Seg 62 ──
        segmentosColision.add(seg(
            v(7385,510), v(7838,508)
        ));
        // ── Seg 63 ──
        segmentosColision.add(seg(
            v(7838,508), v(7838,314)
        ));
        // ── Seg 64 ──
        segmentosColision.add(seg(
            v(7838,314), v(8737,313), v(9582,314),
            v(10099,313), v(10103,308), v(10107,304)
        ));
        // ── Seg 65 ──
        segmentosColision.add(seg(
            v(10107,304), v(10107,55)
        ));
        // ── Seg 66 ──
        segmentosColision.add(seg(
            v(7906,509), v(7969,509)
        ));
        // ── Seg 67 ──
        segmentosColision.add(seg(
            v(7969,509), v(7970,563), v(7976,570), v(7977,575)
        ));
        // ── Seg 68 ──
        segmentosColision.add(seg(
            v(8033,573), v(7979,574)
        ));
        // ── Seg 69 ──
        segmentosColision.add(seg(
            v(8033,570), v(8031,447)
        ));
        // ── Seg 70 ──
        segmentosColision.add(seg(
            v(8031,447), v(7907,449)
        ));
        // ── Seg 71 ──
        segmentosColision.add(seg(
            v(7907,449), v(7904,508)
        ));
        // ── Seg 72 ──
        segmentosColision.add(seg(
            v(8100,508), v(8874,507)
        ));
        // ── Seg 73 ──
        segmentosColision.add(seg(
            v(8874,507), v(8875,446)
        ));
        // ── Seg 74 ──
        segmentosColision.add(seg(
            v(8875,446), v(8100,446)
        ));
        // ── Seg 75 ──
        segmentosColision.add(seg(
            v(8100,446), v(8100,507)
        ));
        // ── Seg 76 ──
        segmentosColision.add(seg(
            v(10172,56), v(10172,498), v(10177,502), v(10180,508)
        ));
        // ── Seg 77 ──
        segmentosColision.add(seg(
            v(10180,508), v(10961,508), v(11807,508),
            v(12364,507), v(12369,503), v(12373,500)
        ));
        // ── Seg 78 ──
        segmentosColision.add(seg(
            v(12373,500), v(12374,55)
        ));
        // ── Seg 79 ──
        segmentosColision.add(seg(
            v(10433,584), v(10432,758), v(10436,764), v(10439,768)
        ));
        // ── Seg 80 ──
        segmentosColision.add(seg(
            v(10439,768), v(10487,765), v(10487,765),
            v(10491,762), v(10496,758), v(10495,637)
        ));
        // ── Seg 81 ──
        segmentosColision.add(seg(
            v(10495,637), v(10691,639)
        ));
        // ── Seg 82 ──
        segmentosColision.add(seg(
            v(10691,639), v(10692,757), v(10696,764), v(10698,767)
        ));
        // ── Seg 83 ──
        segmentosColision.add(seg(
            v(10696,767), v(10746,766), v(10750,764),
            v(10754,761), v(10754,761), v(10755,583)
        ));
        // ── Seg 84 ──
        segmentosColision.add(seg(
            v(10755,583), v(10748,576), v(10678,576),
            v(10442,575), v(10435,580)
        ));
        // ── Seg 85 ──
        segmentosColision.add(seg(
            v(12572,443), v(12828,442)
        ));
        // ── Seg 86 ──
        segmentosColision.add(seg(
            v(12828,440), v(12829,382)
        ));
        // ── Seg 87 ──
        segmentosColision.add(seg(
            v(12829,382), v(12572,382)
        ));
        // ── Seg 88 ──
        segmentosColision.add(seg(
            v(12572,382), v(12571,442)
        ));
        // ── Seg 89 ──
        segmentosColision.add(seg(
            v(13024,508), v(13152,508)
        ));
        // ── Seg 90 ──
        segmentosColision.add(seg(
            v(13152,508), v(13152,446)
        ));
        // ── Seg 91 ──
        segmentosColision.add(seg(
            v(13152,446), v(13026,447)
        ));
        // ── Seg 92 ──
        segmentosColision.add(seg(
            v(13026,447), v(13025,510)
        ));
        // ── Seg 93 ──
        segmentosColision.add(seg(
            v(13347,572), v(13605,573)
        ));
        // ── Seg 94 ──
        segmentosColision.add(seg(
            v(13606,574), v(13604,511)
        ));
        // ── Seg 95 ──
        segmentosColision.add(seg(
            v(13604,511), v(13349,511)
        ));
        // ── Seg 96 ──
        segmentosColision.add(seg(
            v(13348,572), v(13347,512)
        ));
        // ── Seg 97 ──
        segmentosColision.add(seg(
            v(13737,57), v(13737,758)
        ));
        // ── Seg 98 ──
        segmentosColision.add(seg(
            v(13737,758), v(13741,763), v(13746,768), v(13800,768)
        ));
        // ── Seg 99 ──
        segmentosColision.add(seg(
            v(13800,768), v(13803,377)
        ));
        // ── Seg 100 ──
        segmentosColision.add(seg(
            v(14124,378), v(13803,381)
        ));
        // ── Seg 101 ──
        segmentosColision.add(seg(
            v(14125,379), v(14125,437)
        ));
        // ── Seg 102 ──
        segmentosColision.add(seg(
            v(14125,445), v(14188,445)
        ));
        // ── Seg 103 ──
        segmentosColision.add(seg(
            v(14188,445), v(14188,507)
        ));
        // ── Seg 104 ──
        segmentosColision.add(seg(
            v(14188,508), v(14253,509)
        ));
        // ── Seg 105 ──
        segmentosColision.add(seg(
            v(14257,509), v(14256,444)
        ));
        // ── Seg 106 ──
        segmentosColision.add(seg(
            v(14256,444), v(14193,445)
        ));
        // ── Seg 107 ──
        segmentosColision.add(seg(
            v(14190,441), v(14190,381)
        ));
        // ── Seg 108 ──
        segmentosColision.add(seg(
            v(14190,381), v(14514,380)
        ));
        // ── Seg 109 ──
        segmentosColision.add(seg(
            v(14514,895), v(13931,899)
        ));
        // ── Seg 110 ──
        segmentosColision.add(seg(
            v(13931,899), v(13930,712), v(13926,707), v(13922,703)
        ));
        // ── Seg 111 ──
        segmentosColision.add(seg(
            v(13922,703), v(13877,703), v(13871,706), v(13868,709)
        ));
        // ── Seg 112 ──
        segmentosColision.add(seg(
            v(13868,709), v(13866,899)
        ));
        // ── Seg 113 ──
        segmentosColision.add(seg(
            v(13866,899), v(13606,896)
        ));
        // ── Seg 114 ──
        segmentosColision.add(seg(
            v(13606,896), v(13604,712), v(13598,704)
        ));
        // ── Seg 115 ──
        segmentosColision.add(seg(
            v(13598,704), v(13370,705), v(12581,704), v(12572,712)
        ));
        // ── Seg 116 ──
        segmentosColision.add(seg(
            v(12572,712), v(12570,898)
        ));
        // ── Seg 117 ──
        segmentosColision.add(seg(
            v(12570,898), v(12029,898), v(11639,898),
            v(11333,898), v(10854,898), v(10626,898)
        ));
        // ── Seg 118 ──
        segmentosColision.add(seg(
            v(10625,897), v(10624,714), v(10617,702)
        ));
        // ── Seg 119 ──
        segmentosColision.add(seg(
            v(10615,703), v(10570,706), v(10560,712)
        ));
        // ── Seg 120 ──
        segmentosColision.add(seg(
            v(10560,712), v(10563,898)
        ));
        // ── Seg 121 ──
        segmentosColision.add(seg(
            v(10563,898), v(10223,898), v(9704,898), v(9114,898),
            v(8619,898), v(8143,898), v(7905,898), v(7905,898)
        ));
        // ── Seg 122 ──
        segmentosColision.add(seg(
            v(7905,898), v(7904,648), v(7895,640)
        ));
        // ── Seg 123 ──
        segmentosColision.add(seg(
            v(7895,640), v(7784,640), v(7775,647)
        ));
        // ── Seg 124 ──
        segmentosColision.add(seg(
            v(7775,647), v(7775,700)
        ));
        // ── Seg 125 ──
        segmentosColision.add(seg(
            v(7775,700), v(7645,704)
        ));
        // ── Seg 126 ──
        segmentosColision.add(seg(
            v(7645,704), v(7643,583), v(7637,574)
        ));
        // ── Seg 127 ──
        segmentosColision.add(seg(
            v(7637,574), v(7525,575), v(7516,584)
        ));
        // ── Seg 128 ──
        segmentosColision.add(seg(
            v(7517,899), v(7515,587)
        ));
        // ── Seg 129 ──
        segmentosColision.add(seg(
            v(7518,898), v(7410,899), v(6974,899), v(6379,899),
            v(5629,899), v(4959,899), v(4469,900)
        ));
        // ── Seg 130 ──
        segmentosColision.add(seg(
            v(4469,900), v(4468,714), v(4462,705)
        ));
        // ── Seg 131 ──
        segmentosColision.add(seg(
            v(4462,705), v(4222,705), v(4213,712)
        ));
        // ── Seg 132 ──
        segmentosColision.add(seg(
            v(4213,712), v(4212,899)
        ));
        // ── Seg 133 ──
        segmentosColision.add(seg(
            v(4212,899), v(4027,898), v(3692,899)
        ));
        // ── Seg 134 ──
        segmentosColision.add(seg(
            v(3692,899), v(3691,712), v(3682,704)
        ));
        // ── Seg 135 ──
        segmentosColision.add(seg(
            v(3682,704), v(3443,706), v(3434,711)
        ));
        // ── Seg 136 ──
        segmentosColision.add(seg(
            v(3434,711), v(3436,899)
        ));
        // ── Seg 137 ──
        segmentosColision.add(seg(
            v(3436,899), v(3131,899), v(2805,899), v(2305,899),
            v(1776,899), v(1191,899), v(547,899), v(191,899), v(1,898)
        ));
    }

    @Override
    protected void onEnable() {
        for (Geometry tile : tilesFondo)
            app.getRootNode().attachChild(tile);
        app.getRootNode().attachChild(geoMapa);
    }

    @Override
    protected void onDisable() {
        geoMapa.removeFromParent();
        for (Geometry tile : tilesFondo)
            tile.removeFromParent();
    }

    @Override
    protected void cleanup(Application app) {
        tilesFondo.clear();
        segmentosColision.clear();
    }
}