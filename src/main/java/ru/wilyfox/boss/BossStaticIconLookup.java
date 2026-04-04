package ru.wilyfox.boss;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BossStaticIconLookup {
    private static final Map<Integer, BossIconInfo> BY_LEVEL = new LinkedHashMap<>();

    static {
        register(15, "paper", 106);
        register(20, "paper", 386);
        register(25, "paper", 788);
        register(30, "paper", 662);
        register(35, "paper", 582);
        register(40, "paper", 730);
        register(45, "paper", 663);
        register(50, "paper", 387);
        register(55, "paper", 789);
        register(60, "paper", 790);
        register(65, "paper", 664);
        register(70, "iron_axe", 0);
        register(75, "paper", 732);
        register(80, "paper", 61);
        register(90, "paper", 731);
        register(100, "mossy_cobblestone", 0);
        register(105, "paper", 665);
        register(110, "paper", 330);
        register(115, "end_crystal", 0);
        register(120, "soul_sand", 0);
        register(125, "paper", 701);
        register(130, "paper", 64);
        register(140, "paper", 328);
        register(150, "paper", 755);
        register(160, "paper", 698);
        register(170, "paper", 666);
        register(180, "paper", 60);
        register(190, "paper", 667);
        register(200, "bone", 0);
        register(210, "paper", 583);
        register(220, "paper", 66);
        register(230, "paper", 791);
        register(240, "shulker_box", 0);
        register(250, "paper", 668);
        register(260, "paper", 329);
        register(270, "magenta_stained_glass", 0);
        register(280, "paper", 669);
        register(300, "paper", 699);
        register(320, "paper", 63);
        register(330, "paper", 670);
        register(340, "paper", 69);
        register(345, "paper", 383);
        register(350, "paper", 65);
        register(360, "paper", 380);
        register(370, "paper", 62);
        register(380, "paper", 381);
        register(390, "paper", 792);
        register(400, "paper", 756);
        register(410, "paper", 382);
        register(420, "paper", 700);
        register(430, "paper", 10331);
        register(440, "paper", 38);
        register(450, "paper", 503);
        register(460, "paper", 707);
        register(470, "paper", 504);
        register(480, "paper", 506);
        register(490, "paper", 10332);
        register(500, "paper", 507);
        register(510, "paper", 509);
        register(520, "paper", 508);
    }

    private BossStaticIconLookup() {
    }

    public static BossIconInfo find(BossInfo boss) {
        if (boss == null || boss.getLevel() <= 0) {
            return null;
        }

        return BY_LEVEL.get(boss.getLevel());
    }

    private static void register(int level, String material, int customModelData) {
        BY_LEVEL.put(level, new BossIconInfo(material, customModelData));
    }
}
