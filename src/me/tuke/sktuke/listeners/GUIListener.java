package me.tuke.sktuke.listeners;

import ch.njol.skript.Skript;
import me.tuke.sktuke.TuSKe;
import me.tuke.sktuke.manager.gui.v2.GUIHandler;
import me.tuke.sktuke.manager.gui.v2.GUIInventory;
import me.tuke.sktuke.manager.gui.v2.SkriptGUIEvent;
import me.tuke.sktuke.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;

/**
 * @author Tuke_Nuke on 27/05/2017
 */
public abstract class GUIListener {
	private Inventory gui;
	private boolean isStarted = false;

	public GUIListener(Inventory gui) {
		this.gui = gui;
	}

	public abstract void onClick(InventoryClickEvent e, int slot);
	public abstract void onClose(InventoryCloseEvent e);
	public abstract void onDrag(InventoryDragEvent e, int slot);

	public void onEvent(Event event) {
		if (event instanceof InventoryClickEvent && !((InventoryClickEvent) event).isCancelled()) {
			InventoryClickEvent e = (InventoryClickEvent) event;
			if (isAllowedType(e.getClick())){
				Inventory click = InventoryUtils.getClickedInventory(e);
				if (click != null) {
					Inventory op = InventoryUtils.getOpositiveInventory(e.getView(), click);
					if (op == null || !click.equals(gui) && !op.equals(gui))
						return;
					int slot = e.getSlot();
					switch (e.getAction()) {
						case MOVE_TO_OTHER_INVENTORY:
							if (gui.equals(op)) {
								click = op;
								slot = InventoryUtils.getSlotTo(op, e.getCurrentItem());
							}
							break;
						case COLLECT_TO_CURSOR:
							click = gui;
							slot = InventoryUtils.getSlotTo(click, e.getCursor());
							break;
						case HOTBAR_SWAP:
						case HOTBAR_MOVE_AND_READD:
							if (gui.getType().equals(InventoryType.PLAYER)) {
								slot = e.getHotbarButton();
								click = gui;
							}
							break;

					}
					if (click.equals(gui)) {
						onClick(e, slot);
					}
				}
			}
		} else if (event instanceof InventoryCloseEvent) {
			InventoryCloseEvent e = (InventoryCloseEvent) event;
			if (e.getInventory().equals(gui)){
				if (e.getViewers().size() == 1) //Only stop listener when the last one close.
					Bukkit.getScheduler().runTask(TuSKe.getInstance(), this::stop);
				onClose(e);
				//	gui.clear();
			}

		} else if (event instanceof InventoryDragEvent) {
			if (((InventoryDragEvent) event).getInventory().equals(gui))
				for (int slot : ((InventoryDragEvent) event).getRawSlots())
					if (slot < ((InventoryDragEvent) event).getInventory().getSize()) {
						slot = ((InventoryDragEvent) event).getView().convertSlot(slot);
						onDrag((InventoryDragEvent) event, slot);
						if (((InventoryDragEvent) event).isCancelled())
							break;
					}
		}
	}
	public boolean isStarted() {
		return isStarted;
	}
	public void stop() {
		if (isStarted()) {
			SkriptGUIEvent.getInstance().unregister(this);
			isStarted = false;
		}
	}
	public void start() {
		if (!isStarted()) {
			isStarted = true;
			SkriptGUIEvent.getInstance().register(this);
		}
	}
	private boolean isAllowedType(ClickType ct){
		if (ct != null)
			switch(ct){
				case UNKNOWN:
				case WINDOW_BORDER_RIGHT:
				case WINDOW_BORDER_LEFT:
				case CREATIVE:
					return false;
				default:
					break;
			}
		return true;
	}

	public void finalize() {
		gui.clear();
	}
}
