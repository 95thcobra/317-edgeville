package edgeville.model.entity.player;

import java.util.HashMap;
import java.util.Map;

import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.*;
import edgeville.util.SettingsBuilder;

/**
 * @author Simon on 8/23/2014.
 */
public class Interfaces {

	public static final int PANE_FIXED = 548;
	public static final int PANE_RESIZABLE = 161;
	// 164 horizontal bar

	public static final int MAIN_COMPONENT_FIXED = 18;
	// public static final int MAIN_COMPONENT_RESIZABLE = 7;
	public static final int MAIN_COMPONENT_RESIZABLE = 11;

	private Player player;
	private Map<Integer, Integer> visible = new HashMap<>();
	private int activeRoot;
	private boolean resizable;

	public Interfaces(Player player) {
		this.player = player;
	}

	public void resizable(boolean b) {
		resizable = b;
	}

	public boolean resizable() {
		return resizable;
	}

	public void send() {

	}

	public void closeAll() {

	}

	public int whereIs(int id) {
		return visible.entrySet().stream().filter(e -> e.getValue() == id).map(Map.Entry::getKey).findAny().orElse(-1);
	}

	public void setQuickPrayers(boolean enabled) {
		//send(enabled ? 77 : 271, player.interfaces().activeRoot(), resizable() ? 65 : 67, false);
	}

	public void sendFixed() {
		//sendRoot(PANE_FIXED);
	}

	public void enableXPDrops() {
		
	}

	public void disableXPDrops() {
		
	}

	public void setBountyInterface(boolean enabled) {
		
	}

	public void showSkull(boolean enabled) {
		
	}

	public void sendResizable() {
		
	}

	private void setInterfaceSettings() {
		
	}

	public void setting(int i, int c, int start, int end, SettingsBuilder b) {
		//player.write(new InterfaceSettings(i, c, start, end, b));
	}

	public void setting(int i, int c, int start, int end, int s) {
		//player.write(new InterfaceSettings(i, c, start, end, s));
	}

	public void sendMain(int id) {
		//sendMain(id, false);
	}

	public void sendMain(int id, boolean clickthrough) {
		//send(id, activeRoot, mainComponent(), clickthrough);
	}

	public void send(int id, int target, int targetChild, boolean clickthrough) {
		//player.write(new OpenInterface(id, target, targetChild, clickthrough));
		//visible.put((target << 16) | targetChild, id);
		}

	public void closeMain() {
		//close(activeRoot, mainComponent());
	}

	public void closeChatDialogue() {
		//close(162, 546);
	}

	public void close(int target, int targetChild) {
		//close((target << 16) | targetChild);
	}

	public void close(int hash) {
		//player.write(new CloseInterface(hash));
		//visible.remove(hash);
	}

	public int closeById(int id) {
	
		return 0;
	}

	public boolean visible(int id) {
		return activeRoot == id || visible.containsValue(id);
	}

	public boolean visible(int root, int sub) {
		return visible.containsKey(root << 16 | sub);
	}

	public void sendRoot(int id) {
		player.write(new SetRootPane(id));

		activeRoot = id;
	}

	public int activeRoot() {
		return activeRoot;
	}

	public int mainComponent() {
		// return resizable ? 9 : 18;
		return resizable ? MAIN_COMPONENT_RESIZABLE : MAIN_COMPONENT_FIXED; // interfaces
																			// move
																			// with
																			// resizing
																			// on
																			// 7
	}

	public void sendInterfaceString(int interfaceId, int stringId, String text) {
		//player.write(new InterfaceText(interfaceId, stringId, text));
	}
}