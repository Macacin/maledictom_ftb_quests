package dev.ftb.mods.ftbquests.api;

import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * Note: FTB Quests API for 1.20 is subject to change! More API will be added, but don't count on classes and methods
 * outside the dev.ftb.mods.ftbquests.api package to remain unchanged. Every effort will be made to maintain the
 * stability of classes/interfaces/methods within the dev.ftb.mods.ftbquests.api package.
 */
public class FTBQuestsAPI {
    public static final String MOD_ID = "ftbquests";
    public static final String MOD_NAME = "FTB Quests";

    private static API instance;

    /**
     * Retrieve the public API instance.
     *
     * @return the API handler
     * @throws NullPointerException if called before initialised
     */
    public static FTBQuestsAPI.API api() {
       return Objects.requireNonNull(instance);
    }

    /**
     * Convenience method to get a resource location in the FTB Quests namespace
     *
     * @param path the resource location path component
     * @return a new resource location
     */
    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    /**
     * Do not call this method yourself!
     * @param instance the API instance
     */
    @ApiStatus.Internal
    public static void _init(API instance) {
        if (FTBQuestsAPI.instance != null) {
            throw new IllegalStateException("can't init more than once!");
        }
        FTBQuestsAPI.instance = instance;
    }

    public interface API {
        BaseQuestFile getQuestFile(boolean isClient);

        void registerFilterAdapter(ItemFilterAdapter adapter);
    }
}
