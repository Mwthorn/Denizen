package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;

public class DragonPhaseChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // dragon changes phase
    // <entity> changes phase
    //
    // @Regex ^on [^\s]+ changes phase$
    // @Switch in <area>
    // @Switch from <phase>
    // @Switch to <phase>
    //
    // @Cancellable true
    //
    // @Triggers when a dragon's combat phase changes.
    //
    // @Context
    // <context.entity> returns the dEntity of the dragon.
    // <context.new_phase> returns an Element of the dragon's new phase. Phases: <@see link https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EnderDragonChangePhaseEvent.html>
    // <context.old_phase> returns an Element of the dragon's old phase. Can be any phase or 'null' in some cases.
    //
    // @Determine
    // Element to change the dragon's new phase.
    //
    // -->

    public DragonPhaseChangeScriptEvent() {
        instance = this;
    }

    public static DragonPhaseChangeScriptEvent instance;
    public dEntity entity;
    public EnderDragonChangePhaseEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return (CoreUtilities.toLowerCase(s).contains("changes phase"));
    }

    @Override
    public boolean matches(ScriptPath path) {
        String target = path.eventArgLowerAt(0);

        if (!tryEntity(entity, target)) {
            return false;
        }

        if (!runInCheck(path, entity.getLocation())) {
            return false;
        }

        if (!runGenericSwitchCheck(path, "from", event.getCurrentPhase() == null ? "null" : event.getCurrentPhase().name())) {
            return false;
        }

        if (!runGenericSwitchCheck(path, "to", event.getNewPhase().name())) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "DragonPhaseChanged";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (!isDefaultDetermination(determination)) {
            EnderDragon.Phase phase = EnderDragon.Phase.valueOf(determination.toUpperCase());
            event.setNewPhase(phase);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("old_phase")) {
            return new Element(event.getCurrentPhase() == null ? "null" : event.getCurrentPhase().name());
        }
        else if (name.equals("new_phase")) {
            return new Element(event.getNewPhase().name());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEnderDragonChangePhase(EnderDragonChangePhaseEvent event) {
        entity = new dEntity(event.getEntity());
        this.event = event;
        fire(event);
    }
}