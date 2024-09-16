package client.creator;

import client.inventory.Item;
import constants.id.ItemId;
import constants.id.MapId;
import database.character.CharacterSaver;

public class CharacterCreator {
    private final CharacterSaver chrSaver;

    public CharacterCreator(CharacterSaver chrSaver) {
        this.chrSaver = chrSaver;
    }

    public boolean createBeginner(NewCharacterSpec spec, int accountId, int worldId) {
        int mapId = getStartingMap(spec.type());
        Item guide = getStarterGuide(spec.type());
        // TODO, save:
        // - character
        // - skills
        // - equips
        // - starter item
        // - keymap
        // - quickslots
        return false;
    }

    private int getStartingMap(JobType type) {
        return switch (type) {
            case ADVENTURER -> MapId.MUSHROOM_TOWN;
            case KNIGHT_OF_CYGNUS -> MapId.ARAN_TUTORIAL_START;
            case ARAN -> MapId.STARTING_MAP_NOBLESSE;
        };
    }

    private Item getStarterGuide(JobType type) {
        int itemId = switch (type) {
            case ADVENTURER -> ItemId.BEGINNERS_GUIDE;
            case KNIGHT_OF_CYGNUS -> ItemId.NOBLESSE_GUIDE;
            case ARAN -> ItemId.LEGENDS_GUIDE;
        };
        return new Item(itemId, (short) 0, (short) 1);
    }
}
