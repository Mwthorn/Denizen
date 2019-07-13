package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dWorld;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.SpawnChangeEvent;

public class SpawnChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // spawn changes
    //
    // @Regex ^on spawn changes$
    //
    // @Triggers when the world's spawn point changes.
    //
    // @Context
    // <context.world> returns the dWorld that the spawn point changed in.
    // <context.old_location> returns the dLocation of the old spawn point.
    // <context.new_location> returns the dLocation of the new spawn point.
    //
    // -->

    public SpawnChangeScriptEvent() {
        instance = this;
    }

    public static SpawnChangeScriptEvent instance;
    public dWorld world;
    public dLocation old_location;
    public dLocation new_location;
    public SpawnChangeEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("spawn changes");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return true;
    }

    @Override
    public String getName() {
        return "SpawnChange";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("world")) {
            return world;
        }
        else if (name.equals("old_location")) {
            return old_location;
        }
        else if (name.equals("new_location")) {
            return new_location;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onSpawnChange(SpawnChangeEvent event) {
        world = new dWorld(event.getWorld());
        old_location = new dLocation(event.getPreviousLocation());
        new_location = new dLocation(event.getWorld().getSpawnLocation());
        this.event = event;
        fire(event);
    }
}