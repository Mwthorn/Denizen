package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.Poses;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PoseCommand extends AbstractCommand {

    // <--[command]
    // @Name Pose
    // @Syntax pose (add/remove/{assume}) [id:<name>] (player/{npc}) (<location>)
    // @Required 1
    // @Plugin Citizens
    // @Short Rotates the player or NPC to match a pose, or adds/removes an NPC's poses.
    // @Group npc
    //
    // @Description
    // Makes a player or NPC assume the position of a pose saved on an NPC, removes a
    // pose with a specified ID from the current linked NPC, or adds a pose to the NPC
    // with an ID and a location, although the only thing that matters in the location
    // is the pitch and yaw.
    //
    // @Tags
    // <n@npc.has_pose[<name>]>
    // <n@npc.pose[<name>]>
    //
    // @Usage
    // Make an NPC assume a pose.
    // - pose id:MyPose1
    //
    // @Usage
    // Add a pose to an NPC. (Note that only the last 2 numbers matter)
    // - pose add id:MyPose2 l@0,0,0,-2.3,5.4,world
    //
    // @Usage
    // Remove a pose from an NPC.
    // - pose remove id:MyPose1
    // -->

    private enum TargetType {NPC, PLAYER}

    private enum Action {ADD, REMOVE, ASSUME}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (arg.matches("add", "assume", "remove")) {
                scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));
            }
            else if (arg.matchesPrefix("id")) {
                scriptEntry.addObject("pose_id", arg.getValue());
            }
            else if (arg.matches("player")) {
                scriptEntry.addObject("target", TargetType.PLAYER);
            }
            else if (arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("pose_loc", arg.asType(dLocation.class));
            }

        }

        // Even if the target is a player, this command requires an NPC to get the pose from.
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("This command requires an NPC!");
        }

        // It also requires a pose ID
        if (!scriptEntry.hasObject("pose_id")) {
            throw new InvalidArgumentsException("No ID specified!");
        }

        // Set default objects
        scriptEntry.defaultObject("target", TargetType.NPC);
        scriptEntry.defaultObject("action", Action.ASSUME);

        // If the target is a player, it needs a player! However, you can't ADD/REMOVE poses
        // from players, so only allow ASSUME.
        if (scriptEntry.getObject("target") == TargetType.PLAYER) {
            if (scriptEntry.getObject("action") != Action.ASSUME) {
                throw new InvalidArgumentsException("You cannot add or remove poses from a player.");
            }
            else if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsException("This command requires a linked player!");
            }
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Get objects
        TargetType target = (TargetType) scriptEntry.getObject("target");
        dNPC npc = Utilities.getEntryNPC(scriptEntry);
        Action action = (Action) scriptEntry.getObject("action");
        String id = (String) scriptEntry.getObject("pose_id");
        dLocation pose_loc = (dLocation) scriptEntry.getObject("pose_loc");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    ArgumentHelper.debugObj("Target", target.toString())
                            + (target == TargetType.PLAYER ? Utilities.getEntryPlayer(scriptEntry).debug() : "")
                            + npc.debug()
                            + ArgumentHelper.debugObj("Action", action.toString())
                            + ArgumentHelper.debugObj("Id", id)
                            + (pose_loc != null ? pose_loc.debug() : ""));
        }

        if (!npc.getCitizen().hasTrait(Poses.class)) {
            npc.getCitizen().addTrait(Poses.class);
        }

        Poses poses = npc.getCitizen().getTrait(Poses.class);

        switch (action) {

            case ASSUME:
                if (!poses.hasPose(id)) {
                    dB.echoError("Pose \"" + id + "\" doesn't exist for " + npc.toString());
                }

                if (target.name().equals("NPC")) {
                    poses.assumePose(id);
                }
                else {
                    Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
                    Location location = player.getLocation();
                    location.setYaw(poses.getPose(id).getYaw());
                    location.setPitch(poses.getPose(id).getPitch());

                    // The only way to change a player's yaw and pitch in Bukkit
                    // is to use teleport on him/her
                    player.teleport(location);
                }
                break;

            case ADD:
                if (!poses.addPose(id, pose_loc)) {
                    dB.echoError(npc.toString() + " already has that pose!");
                }
                break;

            case REMOVE:
                if (!poses.removePose(id)) {
                    dB.echoError(npc.toString() + " does not have that pose!");
                }
                break;

        }

    }
}