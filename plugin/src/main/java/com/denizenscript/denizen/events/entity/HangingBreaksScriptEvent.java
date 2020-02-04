package com.denizenscript.denizen.events.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

public class HangingBreaksScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // hanging breaks (because <cause>)
    // <hanging> breaks (because <cause>)
    //
    // @Regex ^on [^\s]+ breaks( because [^\s]+)$
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Cancellable true
    //
    // @Triggers when a hanging entity (painting, item_frame, or leash_hitch) is broken.
    //
    // @Context
    // <context.cause> returns the cause of the entity breaking. Causes: ENTITY, EXPLOSION, OBSTRUCTION, PHYSICS, and DEFAULT.
    // <context.entity> returns the EntityTag that broke the hanging entity, if any.
    // <context.hanging> returns the EntityTag of the hanging.
    // -->

    public HangingBreaksScriptEvent() {
        instance = this;
    }

    public static HangingBreaksScriptEvent instance;
    public ElementTag cause;
    public EntityTag entity;
    public EntityTag hanging;
    public HangingBreakEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventArgLowerAt(1).equals("breaks")
                && !path.eventArgLowerAt(2).equals("hanging")
                && !path.eventArgLowerAt(0).equals("player");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String hangCheck = path.eventArgLowerAt(0);

        if (!tryEntity(hanging, hangCheck)) {
            return false;
        }

        if (path.eventArgLowerAt(2).equals("because") && !path.eventArgLowerAt(3).equals(CoreUtilities.toLowerCase(cause.asString()))) {
            return false;
        }

        if (!runInCheck(path, hanging.getLocation())) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public String getName() {
        return "HangingBreaks";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(entity);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("cause")) {
            return cause;
        }
        else if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("hanging")) {
            return hanging;
        }
        else if (name.equals("location")) {
            Deprecations.hangingBreaksEventContext.warn();
            return hanging.getLocation();
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onHangingBreaks(HangingBreakEvent event) {
        hanging = new EntityTag(event.getEntity());
        cause = new ElementTag(event.getCause().name());
        if (event instanceof HangingBreakByEntityEvent) {
            entity = new EntityTag(((HangingBreakByEntityEvent) event).getRemover());
        }
        else {
            entity = null;
        }
        this.event = event;
        fire(event);
    }
}
