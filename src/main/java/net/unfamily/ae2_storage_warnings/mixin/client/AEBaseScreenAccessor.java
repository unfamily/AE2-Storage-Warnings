package net.unfamily.ae2_storage_warnings.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.components.Button;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.VerticalButtonBar;

@Mixin(AEBaseScreen.class)
public interface AEBaseScreenAccessor {
    @Invoker("addToLeftToolbar")
    <B extends Button> B ae2sn$addToLeftToolbar(B button);

    @Accessor("verticalToolbar")
    VerticalButtonBar ae2sn$getVerticalToolbar();
}
