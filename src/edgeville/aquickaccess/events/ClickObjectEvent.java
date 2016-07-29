package edgeville.aquickaccess.events;

import edgeville.aquickaccess.actions.ObjectClick1Action;
import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.model.map.MapObj;

/**
 * Created by Sky on 21-6-2016.
 */
public class ClickObjectEvent extends Event {
    private Player player;
    private MapObj mapObject;

    public ClickObjectEvent(Player player, MapObj mapObject) {
        this.player = player;
        this.mapObject = mapObject;
    }

    @Override
    public void execute(EventContainer container) {
    	if (player.getTile().distance(mapObject.tile()) <= 3) {
        //if (player.getTile().distance(targetTile) <= 3) {
            container.stop();
        }
    }

    @Override
    public void stop() {
        new ObjectClick1Action().handleObjectClick(player, mapObject);
    }
}
