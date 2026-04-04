package ru.wilyfox.utils;

public final class BossLevel {
    private BossLevel() {}

    public static Integer getBossLevel(String text) {
        switch (text) {
            case "Кригер" -> {
                return  15;
            }
            case "Слизень" -> {
                return  20;
            }
            case "Крысиный Король" -> {
                return  25;
            }
            case "Кошмар" -> {
                return  30;
            }
            case "Вендиго" -> {
                return  35;
            }
            case "Ульдрик" -> {
                return  40;
            }
            case "Паучиха" -> {
                return  45;
            }
            case "Мерлок" -> {
                return  50;
            }
            case "Элементалист" -> {
                return  55;
            }
            case "Жнец" -> {
                return  60;
            }
            case "Наездник" -> {
                return  65;
            }
            case "Разбойник" -> {
                return  70;
            }
            case "Шаман" -> {
                return  75;
            }
            case "Варден" -> {
                return  80;
            }
            case "Королевская Жаба" -> {
                return  90;
            }
            case "Гигант" -> {
                return  100;
            }
            case "Бессмертный Легион" -> {
                return  105;
            }
            case "Безумный Алхимик" -> {
                return  110;
            }
            case "Некромант" -> {
                return  115;
            }
            case "Пожиратель Тьмы" -> {
                return  120;
            }
            case "Чудовище" -> {
                return  125;
            }
            case "Октопус" -> {
                return  130;
            }
            case "Кузнец" -> {
                return  140;
            }
            case "Повелитель Грома" -> {
                return  150;
            }
            case "Гаргулья" -> {
                return  160;
            }
            case "Всадник" -> {
                return  170;
            }
            case "Кобольд" -> {
                return  180;
            }
            case "Самурай" -> {
                return  190;
            }
            case "Повелитель Мёртвых" -> {
                return  200;
            }
            case "Рыцарь Света" -> {
                return 210;
            }
            case "Гигантская черепаха" -> {
                return  220;
            }
            case "Змеиная Жрица" -> {
                return  230;
            }
            case "Могущественный Шалкер" -> {
                return  240;
            }
            case "Снежный Монстр" -> {
                return  250;
            }
            case "Дух Леса" -> {
                return  260;
            }
            case "Спектральный Куб" -> {
                return  270;
            }
            case "Циклоп" -> {
                return  280;
            }
            case "Гидра" -> {
                return  300;
            }
            case "Магнус" -> {
                return  320;
            }
            case "Вестница Ада" -> {
                return  330;
            }

            case "Цербер" -> {
                return  340;
            }
            case "Король Ифритов" -> {
                return  345;
            }
            case "Бафомет" -> {
                return  350;
            }
            case "Лавовый Монстр" -> {
                return  360;
            }
            case "Королева Пиглинов" -> {
                return  370;
            }
            case "Дракайна" -> {
                return  380;
            }
            case "Верховный Бес" -> {
                return  390;
            }
            case "Брутальный Пиглин" -> {
                return  400;
            }
            case "Адский Слизень" -> {
                return  410;
            }
            case "Зоглин" -> {
                return  420;
            }
            case "Демонический Рыцарь" -> {
                return  430;
            }

            case "Синтия" -> {
                return  440;
            }
            case "Рыцарь Энда" -> {
                return  450;
            }
            case "Маг Пространства" -> {
                return  460;
            }
            case "Шалкеровый Страж" -> {
                return  470;
            }
            case "Эндер Голем" -> {
                return  480;
            }
            case "Королева Теней" -> {
                return  490;
            }
            case "Хранитель" -> {
                return  500;
            }
            case "Воид" -> {
                return  510;
            }
            case "Странник Измерений" -> {
                return  520;
            }
            default -> {
                return null;
            }
        }
    }

    public static String getBossNameByLevel(int level) {
        switch (level) {
            case 15 -> {
                return "РљСЂРёРіРµСЂ";
            }
            case 20 -> {
                return "РЎР»РёР·РµРЅСЊ";
            }
            case 25 -> {
                return "РљСЂС‹СЃРёРЅС‹Р№ РљРѕСЂРѕР»СЊ";
            }
            case 30 -> {
                return "РљРѕС€РјР°СЂ";
            }
            case 35 -> {
                return "Р’РµРЅРґРёРіРѕ";
            }
            case 40 -> {
                return "РЈР»СЊРґСЂРёРє";
            }
            case 45 -> {
                return "РџР°СѓС‡РёС…Р°";
            }
            case 50 -> {
                return "РњРµСЂР»РѕРє";
            }
            case 55 -> {
                return "Р­Р»РµРјРµРЅС‚Р°Р»РёСЃС‚";
            }
            case 60 -> {
                return "Р–РЅРµС†";
            }
            case 65 -> {
                return "РќР°РµР·РґРЅРёРє";
            }
            case 70 -> {
                return "Р Р°Р·Р±РѕР№РЅРёРє";
            }
            case 75 -> {
                return "РЁР°РјР°РЅ";
            }
            case 80 -> {
                return "Р’Р°СЂРґРµРЅ";
            }
            case 90 -> {
                return "РљРѕСЂРѕР»РµРІСЃРєР°СЏ Р–Р°Р±Р°";
            }
            case 100 -> {
                return "Р“РёРіР°РЅС‚";
            }
            case 105 -> {
                return "Р‘РµСЃСЃРјРµСЂС‚РЅС‹Р№ Р›РµРіРёРѕРЅ";
            }
            case 110 -> {
                return "Р‘РµР·СѓРјРЅС‹Р№ РђР»С…РёРјРёРє";
            }
            case 115 -> {
                return "РќРµРєСЂРѕРјР°РЅС‚";
            }
            case 120 -> {
                return "РџРѕР¶РёСЂР°С‚РµР»СЊ РўСЊРјС‹";
            }
            case 125 -> {
                return "Р§СѓРґРѕРІРёС‰Рµ";
            }
            case 130 -> {
                return "РћРєС‚РѕРїСѓСЃ";
            }
            case 140 -> {
                return "РљСѓР·РЅРµС†";
            }
            case 150 -> {
                return "РџРѕРІРµР»РёС‚РµР»СЊ Р“СЂРѕРјР°";
            }
            case 160 -> {
                return "Р“Р°СЂРіСѓР»СЊСЏ";
            }
            case 170 -> {
                return "Р’СЃР°РґРЅРёРє";
            }
            case 180 -> {
                return "РљРѕР±РѕР»СЊРґ";
            }
            case 190 -> {
                return "РЎР°РјСѓСЂР°Р№";
            }
            case 200 -> {
                return "РџРѕРІРµР»РёС‚РµР»СЊ РњС‘СЂС‚РІС‹С…";
            }
            case 210 -> {
                return "Р С‹С†Р°СЂСЊ РЎРІРµС‚Р°";
            }
            case 220 -> {
                return "Р“РёРіР°РЅС‚СЃРєР°СЏ С‡РµСЂРµРїР°С…Р°";
            }
            case 230 -> {
                return "Р—РјРµРёРЅР°СЏ Р–СЂРёС†Р°";
            }
            case 240 -> {
                return "РњРѕРіСѓС‰РµСЃС‚РІРµРЅРЅС‹Р№ РЁР°Р»РєРµСЂ";
            }
            case 250 -> {
                return "РЎРЅРµР¶РЅС‹Р№ РњРѕРЅСЃС‚СЂ";
            }
            case 260 -> {
                return "Р”СѓС… Р›РµСЃР°";
            }
            case 270 -> {
                return "РЎРїРµРєС‚СЂР°Р»СЊРЅС‹Р№ РљСѓР±";
            }
            case 280 -> {
                return "Р¦РёРєР»РѕРї";
            }
            case 300 -> {
                return "Р“РёРґСЂР°";
            }
            case 320 -> {
                return "РњР°РіРЅСѓСЃ";
            }
            case 330 -> {
                return "Р’РµСЃС‚РЅРёС†Р° РђРґР°";
            }
            case 340 -> {
                return "Р¦РµСЂР±РµСЂ";
            }
            case 345 -> {
                return "РљРѕСЂРѕР»СЊ РС„СЂРёС‚РѕРІ";
            }
            case 350 -> {
                return "Р‘Р°С„РѕРјРµС‚";
            }
            case 360 -> {
                return "Р›Р°РІРѕРІС‹Р№ РњРѕРЅСЃС‚СЂ";
            }
            case 370 -> {
                return "РљРѕСЂРѕР»РµРІР° РџРёРіР»РёРЅРѕРІ";
            }
            case 380 -> {
                return "Р”СЂР°РєР°Р№РЅР°";
            }
            case 390 -> {
                return "Р’РµСЂС…РѕРІРЅС‹Р№ Р‘РµСЃ";
            }
            case 400 -> {
                return "Р‘СЂСѓС‚Р°Р»СЊРЅС‹Р№ РџРёРіР»РёРЅ";
            }
            case 410 -> {
                return "РђРґСЃРєРёР№ РЎР»РёР·РµРЅСЊ";
            }
            case 420 -> {
                return "Р—РѕРіР»РёРЅ";
            }
            case 430 -> {
                return "Р”РµРјРѕРЅРёС‡РµСЃРєРёР№ Р С‹С†Р°СЂСЊ";
            }
            case 440 -> {
                return "РЎРёРЅС‚РёСЏ";
            }
            case 450 -> {
                return "Р С‹С†Р°СЂСЊ Р­РЅРґР°";
            }
            case 460 -> {
                return "РњР°Рі РџСЂРѕСЃС‚СЂР°РЅСЃС‚РІР°";
            }
            case 470 -> {
                return "РЁР°Р»РєРµСЂРѕРІС‹Р№ РЎС‚СЂР°Р¶";
            }
            case 480 -> {
                return "Р­РЅРґРµСЂ Р“РѕР»РµРј";
            }
            case 490 -> {
                return "РљРѕСЂРѕР»РµРІР° РўРµРЅРµР№";
            }
            case 500 -> {
                return "РҐСЂР°РЅРёС‚РµР»СЊ";
            }
            case 510 -> {
                return "Р’РѕРёРґ";
            }
            case 520 -> {
                return "РЎС‚СЂР°РЅРЅРёРє РР·РјРµСЂРµРЅРёР№";
            }
            default -> {
                return null;
            }
        }
    }
}
