package ru.wilyfox.client.chat;

import java.util.List;

public final class ServerEmojiRegistry {
    private static final List<EmojiEntry> EMOJIS = List.of(
            new EmojiEntry("Ангел", "angel", "\u1000"),
            new EmojiEntry("Злость", "angry", "\u1017"),
            new EmojiEntry("Разбитое сердце", "heart_broken", "\u1027"),
            new EmojiEntry("Беспокойство", "worry", "\u1031"),
            new EmojiEntry("Недоумение", "confused", "\u1008"),
            new EmojiEntry("Шок", "wow", "\u1004"),
            new EmojiEntry("Болезнь", "ill", "\u1012"),
            new EmojiEntry("Плач", "cry", "\u1021"),
            new EmojiEntry("Демон", "demon", "\u1029"),
            new EmojiEntry("Дизлайк", "dislike", "\u1024"),
            new EmojiEntry("Элегантность", "elegant", "\u1019"),
            new EmojiEntry("Глазки", "eyes", "\u1030"),
            new EmojiEntry("Любовь", "love", "\u1010"),
            new EmojiEntry("Крутизна", "cool", "\u1006"),
            new EmojiEntry("Наслаждение", "joy", "\u1007"),
            new EmojiEntry("Обморожение", "frozen", "\u1022"),
            new EmojiEntry("GG", "gg", "\u1001"),
            new EmojiEntry("Красное сердце", "heart_red", "\u1026"),
            new EmojiEntry("Веселье", "funny", "\u1011"),
            new EmojiEntry("Счастье", "happy", "\u1003"),
            new EmojiEntry("Лайк", "like", "\u1025"),
            new EmojiEntry("Тошнота", "puke", "\u1009"),
            new EmojiEntry("Задрот", "nerd", "\u1013"),
            new EmojiEntry("Грусть", "sad", "\u1014"),
            new EmojiEntry("Обида", "offence", "\u1016"),
            new EmojiEntry("Крик", "scream", "\u1018"),
            new EmojiEntry("Череп", "skull", "\u1028"),
            new EmojiEntry("Сон", "sleep", "\u1020"),
            new EmojiEntry("Смущение", "embar", "\u1005"),
            new EmojiEntry("Рвота", "vomit", "\u1002"),
            new EmojiEntry("Подмигивание", "wink", "\u1023"),
            new EmojiEntry("Пофигизм", "pof", "\u1015"),
            new EmojiEntry("Умиление", "cute", "\u1032"),
            new EmojiEntry("Язык", "tongue", "\u1033"),
            new EmojiEntry("Амогус", "amongus", "\u1034"),
            new EmojiEntry("Фиолетовое сердце", "heart_purple", "\u1035"),
            new EmojiEntry("Синее сердце", "heart_blue", "\u1036"),
            new EmojiEntry("Желтое сердце", "heart_yellow", "\u1037"),
            new EmojiEntry("Пришелец", "alien", "\u1038"),
            new EmojiEntry("Франкенштейн", "frank", "\u1039"),
            new EmojiEntry("Вампир", "vampire", "\u1040"),
            new EmojiEntry("Зомби", "zombie", "\u1041"),
            new EmojiEntry("Мумия", "mummy", "\u1042"),
            new EmojiEntry("Тыква", "pumpkin", "\u1043"),
            new EmojiEntry("Ножик", "knife", "\u1044"),
            new EmojiEntry("Жуткий глаз", "eye", "\u1045"),
            new EmojiEntry("Призрак", "ghost", "\u1046"),
            new EmojiEntry("Леденец", "candy", "\u1047"),
            new EmojiEntry("Елка", "tree", "\u1048"),
            new EmojiEntry("Подарок", "present", "\u1049"),
            new EmojiEntry("Рудольф", "rudolf", "\u1050"),
            new EmojiEntry("Санта", "santa", "\u1051"),
            new EmojiEntry("Какао", "cacao", "\u1052"),
            new EmojiEntry("Снежинка", "snow", "\u1053"),
            new EmojiEntry("Пингвин", "penguin", "\u1054")
    );

    private ServerEmojiRegistry() {
    }

    public static List<EmojiEntry> all() {
        return EMOJIS;
    }

    public static String replaceSymbolsWithKeys(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;
        for (EmojiEntry emoji : EMOJIS) {
            result = result.replace(emoji.symbol(), emoji.chatKey());
        }
        return result;
    }

    public record EmojiEntry(String name, String id, String symbol) {
        public String chatKey() {
            return ":" + id + ":";
        }
    }
}
