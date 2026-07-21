package org.dimdev.corners.compat.cloth_config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import org.dimdev.corners.TheCorners;
import org.dimdev.corners.config.CornerConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ClothConfigCompat {

    public static Screen create(Screen screen) {
        CornerConfig config = TheCorners.getConfig();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(screen)
                .setTitle(Component.translatable("text.autoconfig.the_corners.title"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("text.autoconfig.the_corners.title"));
        general.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("text.autoconfig.the_corners.option.delayMusicWithRadio"), config.delayMusicWithRadio)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.delayMusicWithRadio = value)
                .build());
        general.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("text.autoconfig.the_corners.option.disableStrongShaders"), config.disableStrongShaders)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.disableStrongShaders = value)
                .build());

        ConfigCategory christmas = builder
                .getOrCreateCategory(Component.translatable("text.autoconfig.the_corners.option.christmas"));
        christmas.addEntry(entryBuilder
                .startBooleanToggle(
                        Component.translatable("text.autoconfig.the_corners.option.christmas.christmas"),
                        config.christmas.christmas)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.christmas.christmas = value)
                .build());
        christmas.addEntry(entryBuilder
                .startStrList(
                        Component.translatable("text.autoconfig.the_corners.option.christmas.leftColors"),
                        config.christmas.leftColors)
                .setDefaultValue(List.of("#30FF99", "#FE515C", "#FFFFFF"))
                .setSaveConsumer(value -> config.christmas.leftColors = new ArrayList<>(value))
                .build());
        christmas.addEntry(entryBuilder
                .startStrList(
                        Component.translatable("text.autoconfig.the_corners.option.christmas.rightColors"),
                        config.christmas.rightColors)
                .setDefaultValue(List.of("#FE515C", "#FFFFFF", "#30FF99"))
                .setSaveConsumer(value -> config.christmas.rightColors = new ArrayList<>(value))
                .build());

        return builder.build();
    }
}
