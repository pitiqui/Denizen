package net.aufdemrand.denizen.npc.traits;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.Toggleable;

import net.citizensnpcs.util.NMS;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// <--[language]
// @name Invisible Trait
// @group NPC Traits
// @description
// The invisible trait will allow a NPC to remain invisible, even after a server restart. It permanently applies
// the invisible potion effect. Use '/trait invisible' or the 'invisible' command to toggle this trait.
//
// Note that player-type NPCs must have '/npc playerlist' toggled to be turned invisible. This does not apply
// to mob-type NPCs. Once invisible, the player-type NPCs can be taken off the playerlist.

// -->

public class InvisibleTrait extends Trait implements Listener, Toggleable {

    @Persist("")
    private boolean invisible = true;

    PotionEffect invis = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1);


    public InvisibleTrait() {
        super("invisible");
    }


    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
        if (invisible) setInvisible();
        else if (npc.isSpawned() && npc.getBukkitEntity() instanceof LivingEntity)
            if (((LivingEntity)npc.getBukkitEntity()).hasPotionEffect(PotionEffectType.INVISIBILITY))
                ((LivingEntity)npc.getBukkitEntity()).removePotionEffect(PotionEffectType.INVISIBILITY);
    }


    private void setInvisible() {
        if (npc.isSpawned() && npc.getBukkitEntity() instanceof LivingEntity) {
            // Apply NPC Playerlist if necessary
            if (npc.getBukkitEntity().getType() == EntityType.PLAYER) {
                npc.data().setPersistent("removefromplayerlist", false);
                NMS.addOrRemoveFromPlayerList(npc.getBukkitEntity(), false);
            }
            invis.apply((LivingEntity)npc.getBukkitEntity());
        }
    }


    @Override
    public void onSpawn() {
        if (invisible) setInvisible();
    }


    @Override
    public boolean toggle() {
        setInvisible(!invisible);
        return invisible;
    }


    @Override
    public void onRemove() {
        setInvisible(false);
    }


    @Override
    public void onAttach() {
        setInvisible(invisible);
    }
}
