package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.scripts.commands.entity.RenameCommand;
import com.denizenscript.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MirrorNameTrait extends Trait {

    @Persist("")
    public boolean mirror = true;

    public UUID mirroredUUID = null;

    public MirrorNameTrait() {
        super("mirrorname");
    }

    public void respawn() {
        if (!npc.isSpawned() || npc.getEntity().getType() != EntityType.PLAYER) {
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), () -> {
            if (npc.isSpawned()) {
                Location loc = npc.getEntity().getLocation();
                npc.despawn(DespawnReason.PENDING_RESPAWN);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), () -> {
                    npc.spawn(loc);
                });
            }
        });
    }

    public void mirrorOn() {
        if (!npc.isSpawned()) {
            return;
        }
        mirroredUUID = npc.getEntity().getUniqueId();
        RenameCommand.addDynamicRename(npc.getEntity(), null, Player::getName);
    }

    public void mirrorOff() {
        if (mirroredUUID == null) {
            return;
        }
    }

    public void enableMirror() {
        mirror = true;
        mirrorOn();
        if (npc.isSpawned() && npc.getEntity().getType() == EntityType.PLAYER) {
            respawn();
        }
    }

    public void disableMirror() {
        mirror = false;
        mirrorOff();
        if (RenameCommand.customNames.remove(mirroredUUID) != null && npc.isSpawned() && npc.getEntity().getType() == EntityType.PLAYER) {
            respawn();
        }
    }

    @Override
    public void onSpawn() {
        if (mirror) {
            mirrorOn();
        }
    }

    @Override
    public void onRemove() {
        if (mirror) {
            mirrorOff();
        }
    }

    @Override
    public void onAttach() {
        if (mirror) {
            mirrorOn();
        }
    }
}
