package net.ludocrypt.corners.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel;
import net.ludocrypt.corners.TheCorners;
import net.ludocrypt.corners.client.model.SpecialModelPartsHolder;
import net.ludocrypt.corners.client.model.SpecialModelSource;
import net.ludocrypt.corners.client.render.CornerRenderTypes;
import net.ludocrypt.corners.mixin.MultiPartBakedModelAccessor;
import net.ludocrypt.corners.mixin.WeightedBakedModelAccessor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

public final class TheCornersModelPlugin {

    private static final String MODELS_PATH = "models";
    private static final String MODEL_PATH_SUFFIX = ".json";
    private static final String SPECIAL_MODELS_KEY = "specialmodels";

    public static void init() {
        PreparableModelLoadingPlugin.register(TheCornersModelPlugin::loadSpecialModelIds, (specialModelIds, context) -> {
            context.addModels(specialModelIds);
            CornerRenderTypes.clearSpecialModelRenderTypes();
            context.modifyModelAfterBake().register((model, bakeContext) -> {
                if (model == null || !(bakeContext.sourceModel() instanceof SpecialModelSource specialModelSource)) {
                    return model;
                }

                Map<ResourceLocation, ResourceLocation> specialModels = specialModelSource.corners$getSpecialModels();

                if (specialModels.isEmpty()) {
                    return model;
                }

                if (!(model instanceof SpecialModelPartsHolder specialModelPartsHolder)) {
                    return model;
                }

                specialModelPartsHolder.corners$setSpecialModelParts(bakeSpecialModelParts(specialModels, bakeContext.baker(), bakeContext.settings()));
                return model;
            });
        });
    }

    private static CompletableFuture<Set<ResourceLocation>> loadSpecialModelIds(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> scanSpecialModelIds(resourceManager), executor);
    }

    private static Set<ResourceLocation> scanSpecialModelIds(ResourceManager resourceManager) {
        Set<ResourceLocation> specialModelIds = new LinkedHashSet<>();
        Map<ResourceLocation, Resource> modelResources = resourceManager.listResources(
            MODELS_PATH,
            id -> id.getPath().endsWith(MODEL_PATH_SUFFIX));

        for (Map.Entry<ResourceLocation, Resource> entry : modelResources.entrySet()) {
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonElement root = JsonParser.parseReader(reader);

                if (!root.isJsonObject()) {
                    continue;
                }

                JsonObject rootObject = root.getAsJsonObject();
                JsonObject specialModels = GsonHelper.getAsJsonObject(rootObject, SPECIAL_MODELS_KEY, null);

                if (specialModels == null) {
                    continue;
                }

                for (Map.Entry<String, JsonElement> specialModel : specialModels.entrySet()) {
                    specialModelIds.add(ResourceLocation.parse(GsonHelper.convertToString(specialModel.getValue(), SPECIAL_MODELS_KEY)));
                }
            } catch (Exception exception) {
                TheCorners.LOGGER.warn("[specialmodels] failed to scan model resource {} for specialmodels", entry.getKey(), exception);
            }
        }

        return Set.copyOf(specialModelIds);
    }

    public static List<SpecialModelPart> getSpecialModelParts(BakedModel model, BlockState state, long seed) {
        if (model instanceof SpecialModelPartsHolder specialModelPartsHolder
            && !(model instanceof MultiPartBakedModel)
            && !(model instanceof WeightedBakedModel)
            && !(model instanceof WrapperBakedModel)) {
            return specialModelPartsHolder.corners$getSpecialModelParts();
        }

        List<SpecialModelPart> parts = new ArrayList<>();
        collectSpecialModelParts(model, state, seed, parts, newIdentitySet());
        return parts;
    }

    private static void collectSpecialModelParts(BakedModel model, BlockState state, long seed, List<SpecialModelPart> parts, Set<BakedModel> seen) {
        if (!seen.add(model)) {
            return;
        }

        if (model instanceof SpecialModelPartsHolder specialModelPartsHolder) {
            parts.addAll(specialModelPartsHolder.corners$getSpecialModelParts());
        }

        if (model instanceof MultiPartBakedModel multiPartModel) {
            long childSeed = RandomSource.create(seed).nextLong();

            for (Pair<Predicate<BlockState>, BakedModel> selector : ((MultiPartBakedModelAccessor) multiPartModel).corners$getSelectors()) {
                if (selector.getLeft().test(state)) {
                    collectSpecialModelParts(selector.getRight(), state, childSeed, parts, seen);
                }
            }

            return;
        }

        if (model instanceof WeightedBakedModel weightedModel) {
            collectWeightedModelParts(weightedModel, state, seed, parts, seen);
            return;
        }

        if (model instanceof WrapperBakedModel wrapper) {
            BakedModel wrappedModel = wrapper.getWrappedModel();

            if (wrappedModel != null && wrappedModel != model) {
                collectSpecialModelParts(wrappedModel, state, seed, parts, seen);
            }
        }
    }

    private static List<SpecialModelPart> bakeSpecialModelParts(Map<ResourceLocation, ResourceLocation> specialModels, ModelBaker baker, ModelState settings) {
        List<SpecialModelPart> parts = new ArrayList<>(specialModels.size());

        for (Map.Entry<ResourceLocation, ResourceLocation> entry : specialModels.entrySet()) {
            RenderType renderType = CornerRenderTypes.getOrCreateSpecialModelRenderType(entry.getKey());

            if (renderType == null) {
                continue;
            }

            BakedModel specialModel = baker.bake(entry.getValue(), settings);

            if (specialModel != null) {
                parts.add(new SpecialModelPart(renderType, specialModel));
            }
        }

        return List.copyOf(parts);
    }

    private static void collectWeightedModelParts(WeightedBakedModel weightedModel, BlockState state, long seed, List<SpecialModelPart> parts, Set<BakedModel> seen) {
        WeightedBakedModelAccessor accessor = (WeightedBakedModelAccessor) weightedModel;
        List<WeightedEntry.Wrapper<BakedModel>> weightedModels = accessor.corners$getList();
        int totalWeight = accessor.corners$getTotalWeight();

        if (weightedModels.isEmpty() || totalWeight <= 0) {
            return;
        }

        RandomSource random = RandomSource.create(seed);
        int weightIndex = Math.abs((int) random.nextLong()) % totalWeight;
        WeightedRandom.getWeightedItem(weightedModels, weightIndex)
            .map(WeightedEntry.Wrapper::data)
            .ifPresent(model -> collectSpecialModelParts(model, state, seed, parts, seen));
    }

    private static Set<BakedModel> newIdentitySet() {
        return java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    }

    public record SpecialModelPart(RenderType renderType, BakedModel model) {
    }

    private TheCornersModelPlugin() {
    }
}
