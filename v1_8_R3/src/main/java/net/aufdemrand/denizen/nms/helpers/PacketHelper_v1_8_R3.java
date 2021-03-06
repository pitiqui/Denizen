package net.aufdemrand.denizen.nms.helpers;

import io.netty.buffer.Unpooled;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_8_R3;
import net.aufdemrand.denizen.nms.interfaces.PacketHelper;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.ListTag;
import net.aufdemrand.denizen.nms.util.jnbt.Tag;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_8_R3.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PacketHelper_v1_8_R3 implements PacketHelper {

    @Override
    public void setSlot(Player player, int slot, ItemStack itemStack, boolean playerOnly) {
        int windowId = playerOnly ? 0 : ((CraftPlayer) player).getHandle().activeContainer.windowId;
        sendPacket(player, new PacketPlayOutSetSlot(windowId, slot, CraftItemStack.asNMSCopy(itemStack)));
    }

    @Override
    public void setFieldOfView(Player player, float fov) {
        PacketPlayOutAbilities packet = new PacketPlayOutAbilities(((CraftPlayer) player).getHandle().abilities);
        if (!Float.isNaN(fov)) {
            packet.b(fov);
        }
        sendPacket(player, packet);
    }

    @Override
    public void showDemoScreen(Player player) {
        sendPacket(player, new PacketPlayOutGameStateChange(5, 0.0F));
    }

    @Override
    public void showBlockAction(Player player, Location location, int action, int state) {
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        Block block = ((CraftWorld) location.getWorld()).getHandle().getType(position).getBlock();
        sendPacket(player, new PacketPlayOutBlockAction(position, block, action, state));
    }

    @Override
    public void showBlockCrack(Player player, int id, Location location, int progress) {
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        sendPacket(player, new PacketPlayOutBlockBreakAnimation(id, position, progress));
    }

    @Override
    public void showTileEntityData(Player player, Location location, int action, CompoundTag compoundTag) {
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        sendPacket(player, new PacketPlayOutTileEntityData(position, action, ((CompoundTag_v1_8_R3) compoundTag).toNMSTag()));
    }

    @Override
    public void showBannerUpdate(Player player, Location location, DyeColor base, List<Pattern> patterns) {
        List<CompoundTag> nbtPatterns = new ArrayList<CompoundTag>();
        for (Pattern pattern : patterns) {
            nbtPatterns.add(NMSHandler.getInstance()
                    .createCompoundTag(new HashMap<String, Tag>())
                    .createBuilder()
                    .putInt("Color", pattern.getColor().getDyeData())
                    .putString("Pattern", pattern.getPattern().getIdentifier())
                    .build());
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getBlockHelper().getNbtData(location.getBlock())
                .createBuilder()
                .putInt("Base", base.getDyeData())
                .put("Patterns", new ListTag(CompoundTag.class, nbtPatterns))
                .build();
        showTileEntityData(player, location, 3, compoundTag);
    }

    @Override
    public void showTabListHeaderFooter(Player player, String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter(new ChatComponentText(header));
        ReflectionHelper.setFieldValue(packet.getClass(), "b", packet, new ChatComponentText(footer));
        sendPacket(player, packet);
    }

    @Override
    public void resetTabListHeaderFooter(Player player) {
        showTabListHeaderFooter(player, "", "");
    }

    @Override
    public void showTitle(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        sendPacket(player, new PacketPlayOutTitle(fadeInTicks, stayTicks, fadeOutTicks));
        if (title != null) {
            sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText(title)));
        }
        if (subtitle != null) {
            sendPacket(player, new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText(subtitle)));
        }
    }

    @Override
    public void sendActionBarMessage(Player player, String message) {
        sendPacket(player, new PacketPlayOutChat(new ChatComponentText(message), (byte) 2));
    }

    @Override
    public void showEquipment(Player player, LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        sendPacket(player, new PacketPlayOutEntityEquipment(entity.getEntityId(), CraftEquipmentSlot.getSlotIndex(equipmentSlot), CraftItemStack.asNMSCopy(itemStack)));
    }

    @Override
    public void resetEquipment(Player player, LivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        showEquipment(player, entity, EquipmentSlot.HAND, equipment.getItemInHand());
        showEquipment(player, entity, EquipmentSlot.HEAD, equipment.getHelmet());
        showEquipment(player, entity, EquipmentSlot.CHEST, equipment.getChestplate());
        showEquipment(player, entity, EquipmentSlot.LEGS, equipment.getLeggings());
        showEquipment(player, entity, EquipmentSlot.FEET, equipment.getBoots());
    }

    @Override
    public void openBook(Player player, EquipmentSlot hand) {
        sendPacket(player, new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(Unpooled.buffer())));
    }

    @Override
    public void showHealth(Player player, float health, int food, float saturation) {
        sendPacket(player, new PacketPlayOutUpdateHealth(health, food, saturation));
    }

    @Override
    public void resetHealth(Player player) {
        showHealth(player, (float) player.getHealth(), player.getFoodLevel(), player.getSaturation());
    }

    @Override
    public void showExperience(Player player, float experience, int level) {
        sendPacket(player, new PacketPlayOutExperience(experience, 0, level));
    }

    @Override
    public void resetExperience(Player player) {
        showExperience(player, player.getExp(), player.getLevel());
    }

    @Override
    public boolean showSignEditor(Player player, Location location) {
        TileEntity tileEntity = ((CraftWorld) location.getWorld()).getTileEntityAt(location.getBlockX(),
                location.getBlockY(), location.getBlockZ());
        if (tileEntity instanceof TileEntitySign) {
            TileEntitySign sign = (TileEntitySign) tileEntity;
            // Prevent client crashing by sending current state of the sign
            sendPacket(player, sign.getUpdatePacket());
            sign.isEditable = true;
            sign.a(((CraftPlayer) player).getHandle());
            sendPacket(player, new PacketPlayOutOpenSignEditor(sign.getPosition()));
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void forceSpectate(Player player, Entity entity) {
        sendPacket(player, new PacketPlayOutCamera(((CraftEntity) entity).getHandle()));
    }

    public static void sendPacket(Player player, Packet packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
