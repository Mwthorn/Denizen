package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class ResourcePackStatusScriptEvent extends BukkitScriptEvent {

    // <--[event]
    // @Events
    // resource pack status
    //
    // @Regex ^on resource pack status$
    //
    // @Triggers when a player accepts, denies, successfully loads, or fails to download a resource pack.
    //
    // @Context
    // <context.hash> returns an Element of the resource pack's hash, or null if one was not specified.
    // <context.status> returns an Element of the status. Can be: SUCCESSFULLY_LOADED, DECLINED, FAILED_DOWNLOAD, ACCEPTED.
    //
    // -->

    public ResourcePackStatusScriptEvent() {
        instance = this;
    }

    public static ResourcePackStatusScriptEvent instance;

    public Element hash;
    public Element status;
    public dPlayer player;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("resource pack status");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return true;
    }

    @Override
    public String getName() {
        return "ResourcePackStatus";
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("hash")) {
            return hash;
        }
        else if (name.equals("status")) {
            return status;
        }
        return super.getContext(name);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }
}