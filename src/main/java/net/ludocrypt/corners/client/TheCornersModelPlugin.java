package net.ludocrypt.corners.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.util.Map;

public class TheCornersModelPlugin {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();

    public static void init() {

//        PreparableModelLoadingPlugin.register((resourceManager, executor) -> CompletableFuture.supplyAsync(() -> resourceManager.listResources("models", a -> true).entrySet().stream().map(SpecialModelData::toPair).filter(Objects::nonNull).collect(Pair.toMap()), executor), (resourceLocations, context) -> {
//            context.resolveModel().register(new ModelResolver() {
//                @Override
//                public @Nullable UnbakedModel resolveModel(Context context) {
//                    if(resourceLocations.containsKey(context.id())) {
//                        System.out.println();
//                    }
//                    return null;
//                }
//            });
//        });
//
    }

    private static UnbakedModel onLoad(UnbakedModel unbakedModel, ModelModifier.OnLoad.Context context) {
        if(!(unbakedModel instanceof BlockModel blockModel)) return unbakedModel;

        return null;
    }

    public static class SpecialModelData {
        private static final TypeToken<Data> dataToken = new TypeToken<>() {};

        public record Data(Map<ResourceLocation, ResourceLocation> specialmodels) {}

        private static Pair<ResourceLocation, Data> toPair(Map.Entry<ResourceLocation, Resource> entry) {
            try {
                var reader = entry.getValue().openAsReader();

                var data = GsonHelper.fromNullableJson(GSON, reader, dataToken, false);

                if(data != null && data.specialmodels() != null) {
                    return new Pair<>(entry.getKey(), data);
                } else {
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }
    }
}
