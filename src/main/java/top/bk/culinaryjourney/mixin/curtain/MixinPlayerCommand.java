package top.bk.culinaryjourney.mixin.curtain;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import dev.dubhe.curtain.commands.PlayerCommand;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.bk.culinaryjourney.integration.curtain.FakePlayerRejoinHelper;

import static net.minecraft.commands.Commands.literal;

/**
 * Curtain 兼容性修复: Curtain 基于 Fabric Carpet, GCA, PCA, Carpet TIS Addition 却没有实现 Carpet TIS Addition 的 player 指令 rejoin 参数.
 *
 * 通过 Mixin Inject 注入 {@code register} 方法的 RETURN 获取 CommandDispatcher 额外为 player 命令注册 rejoin 参数.
 */
@Mixin(value = PlayerCommand.class, remap = false)
public abstract class MixinPlayerCommand {

    /**
     * 原 spawn 指令, 用于生成 FakePlayer.
     *
     * @see PlayerCommand#spawn(CommandContext)
     * @param context 指令上下文
     * @return result 运行结果
     */
    @Shadow
    private static int spawn(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    /**
     * Mixin Inject {@link PlayerCommand#register(CommandDispatcher)} 为 player 指令额外添加 rejoin 参数.
     *
     * @param dispatcher 指令调度器
     * @param ci         注入回调
     */
    @Inject(method = "register", at = @At("RETURN"))
    private static void register(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
        CommandNode<CommandSourceStack> player = dispatcher.getRoot().getChild("player");
        if (player == null) return;

        CommandNode<CommandSourceStack> args = player.getChild("player");
        if (args == null) return;

        args.addChild(literal("rejoin").executes(MixinPlayerCommand::culinary_journey_1_20_1_forge$rejoin).build());
    }

    /**
     * rejoin 的指令方法, 设置 {@link FakePlayerRejoinHelper#isRejoin} 启用 {@link MixinEntityPlayerMPFake} 的 Mixin 逻辑.
     *
     * @param context 指令上下文
     * @return result 运行结果
     * @throws CommandSyntaxException brigadier executes 需要的 Exception
     */
    @Unique
    private static int culinary_journey_1_20_1_forge$rejoin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        // 设置线程变量
        FakePlayerRejoinHelper.isRejoin.set(true);

        try
        {
            return spawn(context);
        }
        finally
        {
            // 执行后删除该线程
            FakePlayerRejoinHelper.isRejoin.remove();
        }
    }

}
