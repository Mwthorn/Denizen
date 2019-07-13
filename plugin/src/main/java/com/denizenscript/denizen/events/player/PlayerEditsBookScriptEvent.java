package com.denizenscript.denizen.events.player;

import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.scripts.containers.core.BookScriptContainer;
import com.denizenscript.denizen.utilities.MaterialCompat;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.dScript;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

// <--[event]
// @Events
// player edits book
// player signs book
//
// @Regex ^on player (edits|signs) book$
//
// @Triggers when a player edits or signs a book.
// @Context
// <context.title> returns the name of the book, if any.
// <context.pages> returns the number of pages in the book.
// <context.book> returns the book item being edited.
// <context.signing> returns whether the book is about to be signed.
//
// @Determine
// "NOT_SIGNING" to prevent the book from being signed.
// dScript to set the book information to set it to instead.
//
// -->

public class PlayerEditsBookScriptEvent extends BukkitScriptEvent implements Listener {

    PlayerEditsBookScriptEvent instance;
    PlayerEditBookEvent event;
    Element signing;
    Element title;
    Element pages;
    dItem book;
    dPlayer player;
    BookMeta bookMeta;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        return lower.startsWith("player edits book") || lower.startsWith("player signs book");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String action = path.eventArgLowerAt(1);
        if (action.equals("edits")) {
            return true;
        }
        if (action.equals("signs") && signing.asBoolean()) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "PlayerEditsBook";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (determination.toUpperCase().equals("NOT_SIGNING")) {
            signing = new Element(false);
        }
        else if (dScript.matches(determination)) {
            dScript script = dScript.valueOf(determination);
            if (script.getContainer() instanceof BookScriptContainer) {
                dItem dBook = ((BookScriptContainer) script.getContainer()).getBookFrom(player, null);
                bookMeta = (BookMeta) dBook.getItemStack().getItemMeta();
                if (dBook.getMaterial().getMaterial() == MaterialCompat.WRITABLE_BOOK) {
                    signing = new Element(false);
                }
            }
            else {
                dB.echoError("Script '" + determination + "' is valid, but not of type 'book'!");
            }
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(player, null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("signing")) {
            return signing;
        }
        if (name.equals("title")) {
            return title;
        }
        else if (name.equals("book")) {
            return book;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onPlayerEditsBook(PlayerEditBookEvent event) {
        player = dPlayer.mirrorBukkitPlayer(event.getPlayer());
        signing = new Element(event.isSigning());
        bookMeta = event.getNewBookMeta();
        pages = new Element(bookMeta.getPageCount());
        title = event.isSigning() ? new Element(bookMeta.getTitle()) : null;
        book = new dItem(event.getPlayer().getInventory().getItem(event.getSlot()));
        this.event = event;
        fire(event);
        event.setNewBookMeta(bookMeta);
        event.setSigning(signing.asBoolean());
    }
}