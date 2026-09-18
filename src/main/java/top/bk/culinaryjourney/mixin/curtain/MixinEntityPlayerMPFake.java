package top.bk.culinaryjourney.mixin.curtain;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.dubhe.curtain.features.player.patches.EntityPlayerMPFake;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.bk.culinaryjourney.integration.curtain.FakePlayerRejoinHelper;

/**
 * Curtain 兼容性修复: Curtain 基于 Fabric Carpet, GCA, PCA, Carpet TIS Addition 却没有实现 Carpet TIS Addition 的 player 指令 rejoin 参数.
 *
 * 通过 Mixin 取消 spawn 的自动 {@code teleportTo} {@code fixStartingPosition} 在 {@link PlayerList#placeNewPlayer(Connection, ServerPlayer)} 后立刻设置维度.
 */
@Mixin(EntityPlayerMPFake.class)
public class MixinEntityPlayerMPFake {

    /**
     * Mixin Inject {@link EntityPlayerMPFake#createFakePlayer(String, MinecraftServer, double, double, double, double, double, ResourceKey, GameType, boolean)} 在 {@link PlayerList#placeNewPlayer(Connection, ServerPlayer)} 后立刻设置 FakePlayer 上次存在的维度.
     *
     * @param cir         注入回调
     * @param instance    FakePlayer 实例
     * @param server      MinecraftServer 实例
     * @param dimensionId 当前维度的 ResourceKey
     * @param worldIn     当前世界实例
     */
    @Inject(
            method = "createFakePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;m_11261_(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;)V",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private static void adjustDimension(
            CallbackInfoReturnable<EntityPlayerMPFake> cir,
            @Local EntityPlayerMPFake instance,
            @Local(argsOnly = true) MinecraftServer server,
            @Local(argsOnly = true) LocalRef<ResourceKey<Level>> dimensionId,
            @Local LocalRef<ServerLevel> worldIn
    ) {
        if (FakePlayerRejoinHelper.isRejoin.get()) {
            ResourceKey<Level> dimension = instance.level().dimension();
            dimensionId.set(dimension);
            worldIn.set(server.getLevel(dimension));
        }
    }

    /**
     * 根据 {@code isRejoin} 跳过 {@code fixStartingPosition} 的赋值操作.
     *
     * @param instance FakePlayer 实例
     * @param newValue Field 的 newValue
     * @return cancel  是否跳过
     */
    @WrapWithCondition(
            method = "createFakePlayer",
            at = @At(
                    value = "FIELD",
                    target = "Ldev/dubhe/curtain/features/player/patches/EntityPlayerMPFake;fixStartingPosition:Ljava/lang/Runnable;",
                    remap = false
            ),
            remap = false
    )
    private static boolean disableCarpetsLocationFixerOnRejoin(
            EntityPlayerMPFake instance, Runnable newValue) {
        return !FakePlayerRejoinHelper.isRejoin.get();
    }

    /**
     * 根据 {@code isRejoin} 跳过 {@code teleportTo} 的赋值操作.
     *
     * @param instance    FakePlayer 实例
     * @param serverWorld 当前世界实例
     * @param x           x 坐标
     * @param y           y 坐标
     * @param z           z 坐标
     * @param yaw         yaw 方向
     * @param pitch       pitch 方向
     * @return cancel     是否跳过
     */
    @WrapWithCondition(
            method = "createFakePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/dubhe/curtain/features/player/patches/EntityPlayerMPFake;m_8999_(Lnet/minecraft/server/level/ServerLevel;DDDFF)V"
            ),
            remap = false
    )
    private static boolean dontRequestTeleport(
            EntityPlayerMPFake instance, ServerLevel serverWorld,
            double x, double y, double z, float yaw, float pitch) {
        return !FakePlayerRejoinHelper.isRejoin.get();
    }
}
