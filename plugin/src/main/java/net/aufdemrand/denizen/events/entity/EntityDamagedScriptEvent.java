package net.aufdemrand.denizen.events.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

public class EntityDamagedScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[language]
    // @name Damage Cause
    // @group Events
    // @description
    // Possible damage causes
    // BLOCK_EXPLOSION, CONTACT, CUSTOM, DROWNING, ENTITY_ATTACK, ENTITY_EXPLOSION,
    // FALL, FALLING_BLOCK, FIRE, FIRE_TICK, LAVA, LIGHTNING, MAGIC, MELTING, POISON,
    // PROJECTILE, STARVATION, SUFFOCATION, SUICIDE, THORNS, VOID, WITHER.
    // -->

    // <--[event]
    // @Events
    // entity damaged (by <cause>) (in <area>)
    // <entity> damaged (by <cause>) (in <area>)
    // entity damages entity (in <area>)
    // entity damages <entity> (in <area>)
    // entity damaged by entity (in <area>)
    // entity damaged by <entity> (in <area>)
    // <entity> damages entity (in <area>)
    // <entity> damaged by entity (in <area>)
    // <entity> damaged by <entity> (in <area>)
    // <entity> damages <entity> (in <area>)
    //
    // @Regex ^on [^\s]+ ((damages [^\s]+)|damaged( by [^\s]+)?)( in ((notable (cuboid|ellipsoid))|([^\s]+)))?$
    //
    // @Switch with <item>
    //
    // @Cancellable true
    //
    // @Triggers when an entity is damaged.
    //
    // @Context
    // <context.entity> returns the dEntity that was damaged.
    // <context.damager> returns the dEntity damaging the other entity, if any.
    // <context.cause> returns the an Element of reason the entity was damaged - see <@link language damage cause> for causes.
    // <context.damage> returns an Element(Decimal) of the amount of damage dealt.
    // <context.final_damage> returns an Element(Decimal) of the amount of damage dealt, after armor is calculated.
    // <context.projectile> returns a dEntity of the projectile, if one caused the event.
    // <context.damage_TYPE> returns the damage dealt by a specific damage type where TYPE can be any of: BASE, HARD_HAT, BLOCKING, ARMOR, RESISTANCE, MAGIC, ABSORPTION.
    //
    // @Determine
    // Element(Decimal) to set the amount of damage the entity receives.
    //
    // @Player when the damager or damaged entity is a player. Cannot be both.
    //
    // @NPC when the damager or damaged entity is an NPC. Cannot be both.
    //
    // -->

    public EntityDamagedScriptEvent() {
        instance = this;
    }

    public static EntityDamagedScriptEvent instance;

    public dEntity entity;
    public Element cause;
    public Element damage;
    public Element final_damage;
    public Element critical;
    public dEntity damager;
    public dEntity projectile;
    public dItem held;
    public EntityDamageEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        return cmd.equals("damaged") || cmd.equals("damages");
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = CoreUtilities.toLowerCase(s);
        String cmd = CoreUtilities.getXthArg(1, lower);
        String attacker = cmd.equals("damages") ? CoreUtilities.getXthArg(0, lower) :
                CoreUtilities.getXthArg(2, lower).equals("by") ? CoreUtilities.getXthArg(3, lower) : "";
        String target = cmd.equals("damages") ? CoreUtilities.getXthArg(2, lower) : CoreUtilities.getXthArg(0, lower);

        if (!attacker.isEmpty()) {
            if (damager != null) {
                if (!cause.asString().equals(attacker) && !tryEntity(projectile, attacker) && !tryEntity(damager, attacker)) {
                    return false;
                }
            }
            else {
                if (!cause.asString().equals(attacker)) {
                    return false;
                }
            }
        }

        if (!tryEntity(entity, target)) {
            return false;
        }

        if (!runInCheck(scriptContainer, s, lower, entity.getLocation())) {
            return false;
        }

        if (!runWithCheck(scriptContainer, s, lower, held)) {
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "EntityDamaged";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        EntityDamageEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (aH.matchesDouble(determination)) {
            damage = new Element(aH.getDoubleFrom(determination));
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(damager != null && damager.isPlayer() ? damager.getDenizenPlayer() : entity.isPlayer() ? entity.getDenizenPlayer() : null,
                damager != null && damager.isCitizensNPC() ? damager.getDenizenNPC() : entity.isCitizensNPC() ? dEntity.getNPCFrom(event.getEntity()) : null);
    }

    @Override
    public dObject getContext(String name) {
        if (name.equals("entity")) {
            return entity.getDenizenObject();
        }
        else if (name.equals("damage")) {
            return damage;
        }
        else if (name.equals("final_damage")) {
            return final_damage;
        }
        else if (name.equals("cause")) {
            return cause;
        }
        else if (name.equals("damager") && damager != null) {
            return damager.getDenizenObject();
        }
        else if (name.equals("critical") && damager != null) {
            return critical;
        }
        else if (name.equals("projectile") && projectile != null) {
            return projectile.getDenizenObject();
        }
        else if (name.startsWith("damage_")) {
            for (EntityDamageEvent.DamageModifier dm : EntityDamageEvent.DamageModifier.values()) {
                if (name.equals("damage_" + CoreUtilities.toLowerCase(dm.name()))) {
                    return new Element(event.getDamage(dm));
                }
            }
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event) {
        entity = new dEntity(event.getEntity());
        damage = new Element(event.getDamage());
        final_damage = new Element(event.getFinalDamage());
        cause = new Element(CoreUtilities.toLowerCase(event.getCause().name()));
        damager = null;
        projectile = null;
        held = null;
        critical = null;
        if (event instanceof EntityDamageByEntityEvent) {
            damager = new dEntity(((EntityDamageByEntityEvent) event).getDamager());
            if (damager.isProjectile()) {
                projectile = damager;
                if (damager.hasShooter()) {
                    damager = damager.getShooter();
                }
            }
            if (damager != null) {
                held = damager.getItemInHand();

                Entity eDamager = ((EntityDamageByEntityEvent) event).getDamager();
                if(eDamager instanceof Player) {
                    Player player = (Player) eDamager;
                    if(player.getFallDistance() != 0.0F &&
                            !eDamager.isOnGround() &&
                            !player.isInsideVehicle() &&
                            !player.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                            player.getLocation().getBlock().getType() != Material.LADDER &&
                            player.getLocation().getBlock().getType() != Material.VINE &&
                            !player.isSprinting()) {
                        critical = new Element(true);
                    } else {
                        critical = new Element(false);
                    }
                }

                if (held != null) {
                    held.setAmount(1);
                }
            }
        }
        cancelled = event.isCancelled();
        this.event = event;
        fire();
        event.setCancelled(cancelled);
        event.setDamage(damage.asDouble());
    }
}
