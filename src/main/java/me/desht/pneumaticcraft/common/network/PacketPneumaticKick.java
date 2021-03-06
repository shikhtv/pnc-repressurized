package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when the kick hotkey is pressed
 */
public class PacketPneumaticKick {
    public PacketPneumaticKick() {
    }

    public PacketPneumaticKick(@SuppressWarnings("unused") PacketBuffer buffer) {
        // empty
    }

    public void toBytes(@SuppressWarnings("unused") PacketBuffer buf) {
        // empty
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (handler.upgradeUsable(ArmorUpgradeRegistry.getInstance().kickHandler, false)) {
                int upgrades = handler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.DISPENSER);
                if (upgrades > 0) {
                    handleKick(player, Math.min(PneumaticValues.PNEUMATIC_KICK_MAX_UPGRADES, upgrades));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void handleKick(PlayerEntity player, int upgrades) {
        Vector3d lookVec = new Vector3d(player.getLookVec().x, Math.max(0, player.getLookVec().y), player.getLookVec().z).normalize();

        double playerFootY = player.getPosY() - player.getHeight() / 2;
        AxisAlignedBB box = new AxisAlignedBB(player.getPosX(), playerFootY, player.getPosZ(), player.getPosX(), playerFootY, player.getPosZ())
                .grow(1.5, 1.5, 1.5).offset(lookVec);
        List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player, box);
        if (entities.isEmpty()) return;
        entities.sort(Comparator.comparingDouble(o -> o.getDistanceSq(player)));

        Entity target = entities.get(0);
        if (!target.hitByEntity(player)) {
            if (target instanceof LivingEntity) {
                target.attackEntityFrom(DamageSource.causePlayerDamage(player), 3.0f + upgrades * 0.5f);
                ((LivingEntity) target).setJumping(true);
            }
            target.setOnGround(false);
            target.collidedHorizontally = false;
            target.collidedVertically = false;
            target.setMotion(target.getMotion().add(lookVec.scale(1.0 + upgrades * 0.5)).add(0, upgrades * 0.1, 0));
        }
        player.world.playSound(null, target.getPosX(), target.getPosY(), target.getPosZ(), ModSounds.PUNCH.get(), SoundCategory.PLAYERS, 1f, 1f);
        NetworkHandler.sendToAllTracking(new PacketSetEntityMotion(target, target.getMotion()), target);
        NetworkHandler.sendToAllTracking(new PacketSpawnParticle(ParticleTypes.EXPLOSION, target.getPosX(), target.getPosY(), target.getPosZ(), 1.0D, 0.0D, 0.0D), target);
        CommonArmorHandler.getHandlerForPlayer(player).addAir(EquipmentSlotType.FEET, -PneumaticValues.PNEUMATIC_KICK_AIR_USAGE * upgrades);
    }
}
