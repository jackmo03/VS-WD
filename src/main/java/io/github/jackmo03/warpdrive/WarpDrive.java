package io.github.jackmo03.warpdrive;

import com.mojang.logging.LogUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import io.github.jackmo03.warpdrive.item.ItemFCUDestroyer;
import io.github.jackmo03.warpdrive.item.ItemFCUFrigate;
import io.github.jackmo03.warpdrive.item.ItemFCUShuttle;
import io.github.jackmo03.warpdrive.item.ItemShipCore;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import io.github.jackmo03.warpdrive.block.BlockShipCore;
import dan200.computercraft.api.ForgeComputerCraftAPI;
import io.github.jackmo03.warpdrive.compat.cct.peripheral.PShipCore;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WarpDrive.MODID)
public class WarpDrive {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "warpdrive";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under
    // the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under
    // the "examplemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be
    // registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, MODID);

    // 注册方块
    public static final RegistryObject<Block> BLOCK_SHIP_CORE = BLOCKS.register("block_ship_core", BlockShipCore::new);

    // 注册对应的方块物品
    public static final RegistryObject<Item> ITEM_SHIP_CORE = ITEMS.register("item_ship_core",
            () -> new ItemShipCore(BLOCK_SHIP_CORE.get()));

    public static final RegistryObject<Item> ITEM_FCU_SHUTTLE = ITEMS.register("item_fcu_shuttle",
            () -> new ItemFCUShuttle(new Item.Properties()));

    public static final RegistryObject<Item> ITEM_FCU_FRIGATE = ITEMS.register("item_fcu_frigate",
            () -> new ItemFCUFrigate(new Item.Properties()));

    public static final RegistryObject<Item> ITEM_FCU_DESTROYER = ITEMS.register("item_fcu_destroyer",
            () -> new ItemFCUDestroyer(new Item.Properties()));

    // Creates a creative tab for WarpDrive
    public static final RegistryObject<CreativeModeTab> WARP_DRIVE_TAB = CREATIVE_MODE_TABS.register("warpdrive_tab",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.literal("Warp Drive"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ITEM_SHIP_CORE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ITEM_SHIP_CORE.get());
                    }).build());

    public WarpDrive(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so block get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so item get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the
        // config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // Register ComputerCraft peripheral provider
        ForgeComputerCraftAPI.registerPeripheralProvider(new ShipCorePeripheralProvider());

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods
    // in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    private static class ShipCorePeripheralProvider implements IPeripheralProvider {
        @Override
        public LazyOptional<IPeripheral> getPeripheral(Level world, BlockPos pos, Direction side) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof BlockShipCore) {
                return LazyOptional.of(() -> new PShipCore(world, pos));
            }
            return LazyOptional.empty();
        }
    }
}
