package dev.crabcritic.service;

import org.springframework.stereotype.Service;

/**
 * The mighty SVG crab characters with expressions on cringe score.
 *
 * This is intentionally a dumb template engine (do whatever you want with it, it has been made so you can fully modify it)
 * The crab has 6 states (positive + 5 cringe levels).
 * No AI slop, no image costing your left eyeball, no external calls. Just paths and ellipses (pain..)
 *
 * The SVG weighs ~3KB. It renders instantly. It works even on your crappy macintosh screen. (no offense).
 * It costs exactly zero API calls. That's the point. (pain again)
 */
@Service
public class SvgRenderer {

    // Crab color palette
    private static final String SHELL = "#E8734A";
    private static final String DARK = "#B85530";
    private static final String LIGHT = "#F5C4B3";
    private static final String WHITE = "#FFFFFF";
    private static final String INK = "#4A1B0C";

    // Happy crab colors
    private static final String GOOD_SHELL = "#3BA878";
    private static final String GOOD_DARK = "#1A7A52";
    private static final String GOOD_LIGHT = "#A8E6CF";

    /**
     * Render a crab SVG according to the emotional state.
     *
     * @param cringeScore 1-5, how distressed the crab looks
     * @param isPositive  true = happy crab (green), false = judging crab (orange)
     * @return complete SVG markup as a string
     */
    public String render(int cringeScore, boolean isPositive) {
        String sc = isPositive ? GOOD_SHELL : SHELL;
        String dc = isPositive ? GOOD_DARK : DARK;
        String lc = isPositive ? GOOD_LIGHT : LIGHT;

        int level = isPositive ? 0 : Math.clamp(cringeScore, 1, 5);

        String eyeStalks = eyeStalks(level, dc);
        String eyes = eyes(level, dc);
        String brows = brows(level, dc);
        String mouth = mouth(level, dc);
        String claws = claws(level, sc, dc);
        String extras = extras(level, dc, sc);
        String legs = legs(dc);
        String shell = shell(sc, dc, lc);

        return """
                <svg width="280" height="186" viewBox="0 0 280 186" xmlns="http://www.w3.org/2000/svg">
                %s
                %s
                %s
                %s
                %s
                %s
                %s
                %s
                </svg>""".formatted(extras, claws, legs, shell, eyeStalks, eyes, brows, mouth);
    } //this looks crazy I know

    private String eyeStalks(int level, String dc) {
        String attr = "stroke=\"%s\" stroke-width=\"3.5\" stroke-linecap=\"round\" fill=\"none\"".formatted(dc);
        return switch (level) {
            case 0 -> """
                    <path d="M118 78 Q112 46 100 36" %s/>
                    <path d="M162 78 Q168 46 180 36" %s/>""".formatted(attr, attr);
            case 1 -> """
                    <path d="M118 78 Q112 48 102 38" %s/>
                    <path d="M162 78 Q168 48 178 38" %s/>""".formatted(attr, attr);
            case 2 -> """
                    <path d="M118 78 Q108 46 96 38" %s/>
                    <path d="M162 78 Q172 46 184 38" %s/>""".formatted(attr, attr);
            case 3 -> """
                    <path d="M118 78 Q102 40 88 30" %s/>
                    <path d="M162 78 Q178 38 194 32" %s/>""".formatted(attr, attr);
            case 4 -> {
                String wide = attr.replace("3.5", "4");
                yield """
                    <path d="M118 78 Q96 32 80 22" %s/>
                    <path d="M162 78 Q184 32 200 22" %s/>""".formatted(wide, wide);
            }
            default -> {
                String panic = attr.replace("3.5", "4.5");
                yield """
                    <path d="M118 78 Q88 24 72 12" %s/>
                    <path d="M162 78 Q192 24 208 12" %s/>""".formatted(panic, panic);
            }
        };
    }

    private String eyes(int level, String dc) {
        return switch (level) {
            case 0 -> """
                    <circle cx="96" cy="32" r="13" fill="%s" stroke="%s" stroke-width="1.5"/>
                    <circle cx="184" cy="32" r="13" fill="%s" stroke="%s" stroke-width="1.5"/>
                    <circle cx="99" cy="30" r="5.5" fill="%s"/><circle cx="101" cy="28" r="2.2" fill="%s"/>
                    <circle cx="187" cy="30" r="5.5" fill="%s"/><circle cx="189" cy="28" r="2.2" fill="%s"/>
                    """.formatted(WHITE, dc, WHITE, dc, dc, WHITE, dc, WHITE);
            case 1 -> """
                    <circle cx="98" cy="34" r="12" fill="%s" stroke="%s" stroke-width="1.5"/>
                    <circle cx="182" cy="34" r="12" fill="%s" stroke="%s" stroke-width="1.5"/>
                    <circle cx="100" cy="33" r="5" fill="%s"/><circle cx="102" cy="31" r="2" fill="%s"/>
                    <circle cx="184" cy="33" r="5" fill="%s"/><circle cx="186" cy="31" r="2" fill="%s"/>
                    """.formatted(WHITE, dc, WHITE, dc, INK, WHITE, INK, WHITE);
            case 2 -> """
                    <circle cx="92" cy="34" r="12" fill="%s" stroke="%s" stroke-width="1.5"/>
                    <circle cx="188" cy="34" r="12" fill="%s" stroke="%s" stroke-width="1.5"/>
                    <circle cx="95" cy="34" r="5" fill="%s"/><circle cx="97" cy="32" r="2" fill="%s"/>
                    <circle cx="191" cy="34" r="5" fill="%s"/><circle cx="193" cy="32" r="2" fill="%s"/>
                    """.formatted(WHITE, dc, WHITE, dc, INK, WHITE, INK, WHITE);
            case 3 -> """
                    <circle cx="84" cy="26" r="13" fill="%s" stroke="%s" stroke-width="1.5"/>
                    <circle cx="198" cy="28" r="14" fill="%s" stroke="%s" stroke-width="1.5"/>
                    <circle cx="88" cy="26" r="5" fill="%s"/><circle cx="90" cy="24" r="2" fill="%s"/>
                    <circle cx="202" cy="27" r="5.5" fill="%s"/><circle cx="204" cy="25" r="2.2" fill="%s"/>
                    """.formatted(WHITE, dc, WHITE, dc, INK, WHITE, INK, WHITE);
            case 4 -> """
                    <circle cx="76" cy="18" r="14" fill="%s" stroke="%s" stroke-width="1.8"/>
                    <circle cx="204" cy="18" r="14" fill="%s" stroke="%s" stroke-width="1.8"/>
                    <circle cx="72" cy="18" r="6" fill="%s"/><circle cx="70" cy="16" r="2.2" fill="%s"/>
                    <circle cx="208" cy="18" r="6" fill="%s"/><circle cx="210" cy="16" r="2.2" fill="%s"/>
                    """.formatted(WHITE, dc, WHITE, dc, INK, WHITE, INK, WHITE);
            default -> // X eyes — the crab has seen too much
                    """
                    <circle cx="68" cy="8" r="15" fill="%s" stroke="%s" stroke-width="2"/>
                    <circle cx="212" cy="8" r="15" fill="%s" stroke="%s" stroke-width="2"/>
                    <line x1="60" y1="2" x2="76" y2="14" stroke="%s" stroke-width="3.5" stroke-linecap="round"/>
                    <line x1="76" y1="2" x2="60" y2="14" stroke="%s" stroke-width="3.5" stroke-linecap="round"/>
                    <line x1="204" y1="2" x2="220" y2="14" stroke="%s" stroke-width="3.5" stroke-linecap="round"/>
                    <line x1="220" y1="2" x2="204" y2="14" stroke="%s" stroke-width="3.5" stroke-linecap="round"/>
                    """.formatted(WHITE, dc, WHITE, dc, INK, INK, INK, INK);
        };
    }

    private String brows(int level, String dc) {
        return switch (level) {
            case 0 -> """
                    <path d="M84 20 Q96 14 108 22" fill="none" stroke="%s" stroke-width="1.8" stroke-linecap="round"/>
                    <path d="M172 22 Q184 14 196 20" fill="none" stroke="%s" stroke-width="1.8" stroke-linecap="round"/>
                    """.formatted(dc, dc);
            case 1 -> """
                    <path d="M86 24 Q98 20 110 26" fill="none" stroke="%s" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M170 26 Q182 20 194 24" fill="none" stroke="%s" stroke-width="1.5" stroke-linecap="round"/>
                    """.formatted(dc, dc);
            case 2 -> """
                    <path d="M80 24 Q92 20 104 26" fill="none" stroke="%s" stroke-width="1.8" stroke-linecap="round"/>
                    <path d="M176 26 Q188 20 200 24" fill="none" stroke="%s" stroke-width="1.8" stroke-linecap="round"/>
                    """.formatted(dc, dc);
            case 3 -> """
                    <path d="M70 16 Q82 10 98 20" fill="none" stroke="%s" stroke-width="2" stroke-linecap="round"/>
                    <path d="M210 18 Q198 10 184 20" fill="none" stroke="%s" stroke-width="2" stroke-linecap="round"/>
                    """.formatted(dc, dc);
            case 4 -> """
                    <path d="M62 6 Q74 -2 92 10" fill="none" stroke="%s" stroke-width="2.5" stroke-linecap="round"/>
                    <path d="M218 6 Q206 -2 188 10" fill="none" stroke="%s" stroke-width="2.5" stroke-linecap="round"/>
                    """.formatted(dc, dc);
            default -> ""; // At level 5, brows have left the chat
        };
    }

    private String mouth(int level, String dc) {
        return switch (level) {
            case 0 -> """
                    <path d="M124 114 Q140 128 156 114" fill="none" stroke="%s" stroke-width="2.5" stroke-linecap="round"/>
                    """.formatted(dc);
            case 1 -> """
                    <path d="M128 112 Q140 120 152 112" fill="none" stroke="%s" stroke-width="2" stroke-linecap="round"/>
                    """.formatted(INK);
            case 2 -> """
                    <line x1="126" y1="112" x2="154" y2="112" stroke="%s" stroke-width="2.5" stroke-linecap="round"/>
                    """.formatted(INK);
            case 3 -> """
                    <path d="M128 112 Q134 106 140 112 Q146 106 152 112" fill="none" stroke="%s" stroke-width="2.2" stroke-linecap="round"/>
                    """.formatted(INK);
            case 4 -> """
                    <path d="M124 114 Q140 104 156 114" fill="none" stroke="%s" stroke-width="2.5" stroke-linecap="round"/>
                    """.formatted(INK);
            default -> """
                    <path d="M120 112 Q126 102 132 112 Q138 102 144 112 Q150 102 156 112 Q162 102 168 112" fill="none" stroke="%s" stroke-width="3" stroke-linecap="round"/>
                    """.formatted(INK);
        };
    }

    private String claws(int level, String sc, String dc) {
        return switch (level) {
            case 0 -> pincer(36, 72, -15, 10, sc, dc) + pincer(244, 66, 15, 10, sc, dc);
            case 1 -> pincer(34, 76, -5, 10, sc, dc) + pincer(246, 76, 5, 10, sc, dc);
            case 2 -> pincer(32, 70, -10, 10, sc, dc) + pincer(248, 70, 10, 10, sc, dc);
            case 3 -> pincer(40, 56, -30, 14, sc, dc) + pincer(244, 68, 12, 12, sc, dc);
            case 4 -> pincer(58, 28, -55, 18, sc, dc) + pincer(240, 60, 18, 16, sc, dc);
            default -> pincer(52, 14, -75, 22, sc, dc) + pincer(228, 14, 75, 22, sc, dc);
        };
    }

    /**
     * A single crab pincer
     */
    private String pincer(int x, int y, int rot, int opening, String sc, String dc) {
        return """
                <g transform="translate(%d,%d) rotate(%d)">
                  <ellipse cx="0" cy="0" rx="26" ry="16" fill="%s" stroke="%s" stroke-width="2"/>
                  <path d="M-8 -2 Q-2 -8 6 -3" fill="none" stroke="%s" stroke-width="0.8" opacity="0.4"/>
                  <path d="M-5 4 Q2 -1 8 3" fill="none" stroke="%s" stroke-width="0.8" opacity="0.35"/>
                  <ellipse cx="12" cy="-%d" rx="20" ry="9" fill="%s" stroke="%s" stroke-width="1.5"/>
                  <path d="M26 -%d L32 -%d" fill="none" stroke="%s" stroke-width="1.2" stroke-linecap="round"/>
                  <ellipse cx="12" cy="%d" rx="20" ry="9" fill="%s" stroke="%s" stroke-width="1.5"/>
                  <path d="M26 %d L32 %d" fill="none" stroke="%s" stroke-width="1.2" stroke-linecap="round"/>
                </g>
                """.formatted(
                x, y, rot,
                sc, dc,       // palm
                dc, dc,       // ridges
                opening, sc, dc,  // top finger
                opening - 3, opening, dc,  // top finger tip
                opening, sc, dc,  // bottom finger
                opening + 3, opening, dc   // bottom finger tip
        );
    }

    private String extras(int level, String dc, String sc) {
        return switch (level) {
            case 0 -> """
                    <circle cx="88" cy="18" r="3" fill="%s" opacity="0.5"/>
                    <circle cx="198" cy="20" r="2.5" fill="%s" opacity="0.4"/>
                    """.formatted(sc, sc);
            case 3 -> """
                    <circle cx="70" cy="48" r="3.5" fill="%s" opacity="0.35"/>
                    <circle cx="66" cy="60" r="2.5" fill="%s" opacity="0.25"/>
                    """.formatted(dc, dc);
            case 4 -> """
                    <circle cx="60" cy="52" r="4" fill="%s" opacity="0.4"/>
                    <circle cx="54" cy="66" r="3" fill="%s" opacity="0.3"/>
                    <circle cx="218" cy="46" r="3.5" fill="%s" opacity="0.35"/>
                    """.formatted(dc, dc, dc);
            case 5 -> """
                    <circle cx="50" cy="50" r="4.5" fill="%s" opacity="0.45"/>
                    <circle cx="42" cy="66" r="3.5" fill="%s" opacity="0.35"/>
                    <circle cx="228" cy="48" r="4" fill="%s" opacity="0.4"/>
                    <circle cx="236" cy="62" r="3" fill="%s" opacity="0.3"/>
                    <path d="M40 38 L30 26" stroke="%s" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
                    """.formatted(dc, dc, dc, dc, dc);
            default -> "";
        };
    }

    private String legs(String dc) {
        return """
                <path d="M108 134 Q90 148 78 164" fill="none" stroke="%1$s" stroke-width="2.5" stroke-linecap="round"/>
                <path d="M102 138 Q82 156 66 166" fill="none" stroke="%1$s" stroke-width="2.5" stroke-linecap="round"/>
                <path d="M96 142 Q78 164 62 172" fill="none" stroke="%1$s" stroke-width="2.5" stroke-linecap="round"/>
                <path d="M172 134 Q190 148 202 164" fill="none" stroke="%1$s" stroke-width="2.5" stroke-linecap="round"/>
                <path d="M178 138 Q198 156 214 166" fill="none" stroke="%1$s" stroke-width="2.5" stroke-linecap="round"/>
                <path d="M184 142 Q202 164 218 172" fill="none" stroke="%1$s" stroke-width="2.5" stroke-linecap="round"/>
                <circle cx="78" cy="166" r="2" fill="%1$s" opacity="0.5"/>
                <circle cx="66" cy="168" r="2" fill="%1$s" opacity="0.5"/>
                <circle cx="62" cy="174" r="2" fill="%1$s" opacity="0.5"/>
                <circle cx="202" cy="166" r="2" fill="%1$s" opacity="0.5"/>
                <circle cx="214" cy="168" r="2" fill="%1$s" opacity="0.5"/>
                <circle cx="218" cy="174" r="2" fill="%1$s" opacity="0.5"/>
                """.formatted(dc);
    }

    private String shell(String sc, String dc, String lc) {
        return """
                <ellipse cx="140" cy="104" rx="66" ry="44" fill="%s" stroke="%s" stroke-width="2.5"/>
                <ellipse cx="140" cy="98" rx="54" ry="32" fill="%s" opacity="0.35"/>
                <path d="M98 88 Q140 78 182 88" fill="none" stroke="%s" stroke-width="1" opacity="0.3"/>
                <path d="M92 100 Q140 92 188 100" fill="none" stroke="%s" stroke-width="1" opacity="0.25"/>
                <path d="M94 112 Q140 106 186 112" fill="none" stroke="%s" stroke-width="1" opacity="0.2"/>
                <circle cx="120" cy="90" r="3" fill="%s" opacity="0.4"/>
                <circle cx="158" cy="88" r="2.5" fill="%s" opacity="0.35"/>
                """.formatted(sc, dc, lc, dc, dc, dc, lc, lc);
    }
}
