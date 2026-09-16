package top.bk.culinaryjourney.mixin;

import net.creeperhost.ftbbackups.BackupHandler;
import net.creeperhost.ftbbackups.utils.TieredBackupTest;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * FTB Backups 2 易用性修复: 优化 FTB Backups 2 对于 {@code backupName} 参数的处理, 使其能够在备份文件中体现.
 *
 * FTB Backups 2 原逻辑为无论如何都使用时间戳命名 Backup Data, {@code backupName} 参数写入 Metadata.
 * 通过 Mixin ModifyVariable 更改 {@code createBackup(Lnet/minecraft/server/MinecraftServer;ZLjava/lang/String;)V} 方法中的第一次 STORE, 即局部变量 @{code backupName}.
 */
@Mixin(value = BackupHandler.class, remap = false)
public class MixinBackupHandler {

    /**
     * Mixin ModifyVariable {@link BackupHandler#createBackup(MinecraftServer, boolean, String)} 修改 {@code backupName} 局部变量.
     *
     * @param originalName    原 backupName 的赋值, 由 {@link TieredBackupTest#getBackupName()} 获取的时间戳
     * @param minecraftServer Minecraft Server 实例
     * @param protect         是否为受保护的 Backup (即不会被自动清理)
     * @param name            真正的 Backup Name 原本只被写入 Metadata, 更改为同时体现在 Backup Data
     */
    @ModifyVariable(method = "createBackup(Lnet/minecraft/server/MinecraftServer;ZLjava/lang/String;)V", at = @At("STORE"), index = 3)
    private static String modifyBackupName(String originalName, MinecraftServer minecraftServer, boolean protect, String name) {
        return name.isEmpty() ? originalName : name + "-" + originalName;
    }

}
