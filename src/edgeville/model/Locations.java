package edgeville.model;

/**
 * Created by Sky on 25-6-2016.
 */
public enum Locations {
    EDGEVILLE(new Tile(3097, 3499)),
    VARROCK(new Tile(3210, 3424)),
    FALADOR(new Tile(2964, 3378)),
    LUMBRIDGE(new Tile(3222, 3218)),
    CAMELOT(new Tile(2757, 3477)),

    SPAWN_LOCATION(new Tile(3097, 3499)),
    RESPAWN_LOCATION(new Tile(3097, 3499));

    private Tile tile;

    Locations(Tile tile) {
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }
}
