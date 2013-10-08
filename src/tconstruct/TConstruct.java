package tconstruct;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.*;
import cpw.mods.fml.common.registry.*;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.crash.CallableMinecraftVersion;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import tconstruct.client.event.EventCloakRender;
import tconstruct.common.*;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.*;
import tconstruct.library.util.TabTools;
import tconstruct.util.*;
import tconstruct.util.config.*;
import tconstruct.util.landmine.behavior.Behavior;
import tconstruct.util.landmine.behavior.stackCombo.SpecialStackHandler;
import tconstruct.util.player.TPlayerHandler;
import tconstruct.worldgen.*;
import tconstruct.worldgen.village.*;

/** TConstruct, the tool mod.
 * Craft your tools with style, then modify until the original is gone!
 * @author: mDiyo
 * @dependencies: IC2 API, MFR API
 */

@Mod(modid = "TConstruct", name = "TConstruct", version = "1.6.X_1.5.0d", dependencies = "required-after:Forge@[8.9,)")
@NetworkMod(serverSideRequired = false, clientSideRequired = true, channels = { "TConstruct" }, packetHandler = tconstruct.util.network.TPacketHandler.class)
public class TConstruct
{
    /** The value of one ingot in millibuckets */
    public static final int ingotLiquidValue = 144;
    public static final int liquidUpdateAmount = 6;

    /* Instance of this mod, used for grabbing prototype fields */
    @Instance("TConstruct")
    public static TConstruct instance;
    /* Proxies for sides, used for graphics processing */
    @SidedProxy(clientSide = "tconstruct.client.TProxyClient", serverSide = "tconstruct.common.TProxyCommon")
    public static TProxyCommon proxy;

    public TConstruct()
    {
        if (Loader.isModLoaded("Natura"))
        {
            System.out.println("[TConstruct] Natura, what are we going to do tomorrow night?");
            if (Loader.isModLoaded("ChaoticBastion"))
            {
                System.out.println("[Natura] TConstruct, we're going to...");
                System.out.println("[ChaoticBastion] All your base are belong to us!");

            }
            else
                System.out.println("[Natura] TConstruct, we're going to take over the world!");
        }
        else
        {
            if (Loader.isModLoaded("ChaoticBastion"))
            {
                System.out.println("[TConstruct] Preparing to...");
                System.out.println("[ChaoticBastion] I'MA FIRING MY LAZOR!");
            }
            else
                System.out.println("[TConstruct] Preparing to take over the world");
        }
    }

    @EventHandler
    public void preInit (FMLPreInitializationEvent event)
    {

        PHConstruct.initProps(event.getModConfigurationDirectory());
        TConstructRegistry.materialTab = new TabTools("TConstructMaterials");
        TConstructRegistry.toolTab = new TabTools("TConstructTools");
        TConstructRegistry.blockTab = new TabTools("TConstructBlocks");

        tableCasting = new LiquidCasting();
        basinCasting = new LiquidCasting();
        chiselDetailing = new Detailing();

        content = new TContent();

        events = new TEventHandler();
        events.unfuxOreDictionary();
        MinecraftForge.EVENT_BUS.register(events);
        content.oreRegistry();

        proxy.registerRenderer();
        proxy.registerTickHandler();
        proxy.addNames();
        proxy.readManuals();
        proxy.registerKeys();

        GameRegistry.registerWorldGenerator(new TBaseWorldGenerator());
        MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainGenEventHandler());
        GameRegistry.registerFuelHandler(content);
        GameRegistry.registerCraftingHandler(new TCraftingHandler());
        NetworkRegistry.instance().registerGuiHandler(instance, proxy);

        VillagerRegistry.instance().registerVillageTradeHandler(78943, new TVillageTrades());
        if (PHConstruct.addToVillages)
        {
            VillagerRegistry.instance().registerVillageCreationHandler(new VillageToolStationHandler());
            VillagerRegistry.instance().registerVillageCreationHandler(new VillageSmelteryHandler());
            try
            {
                if (new CallableMinecraftVersion(null).minecraftVersion().equals("1.6.4"))
                {
                    MapGenStructureIO.func_143031_a(ComponentToolWorkshop.class, "TConstruct:ToolWorkshopStructure");
                    MapGenStructureIO.func_143031_a(ComponentSmeltery.class, "TConstruct:SmelteryStructure");
                }
            }
            catch (Throwable e)
            {

            }
        }

        playerTracker = new TPlayerHandler();
        GameRegistry.registerPlayerTracker(playerTracker);
        MinecraftForge.EVENT_BUS.register(playerTracker);
    }

    @EventHandler
    public void init (FMLInitializationEvent event)
    {
        if (event.getSide() == Side.CLIENT)
        {
            MinecraftForge.EVENT_BUS.register(new EventCloakRender());
        }

        content.intermodCommunication();
        TwilightForestConfig.initProps(PHConstruct.cfglocation);
        BOPConfig.initProps(PHConstruct.cfglocation);
        DimensionBlacklist.getbaddimensions();
        GameRegistry.registerWorldGenerator(new SlimeIslandGen(TContent.slimePool.blockID, 0));
    }

    @EventHandler
    public void postInit (FMLPostInitializationEvent evt)
    {
    	proxy.postInit();
        Behavior.registerBuiltInBehaviors();
        SpecialStackHandler.registerBuiltInStackHandlers();
        content.modIntegration();
        TContent.modRecipes();
        content.createEntities();
        content.modRecipes();
    }

    public static LiquidCasting getTableCasting ()
    {
        return tableCasting;
    }

    public static LiquidCasting getBasinCasting ()
    {
        return basinCasting;
    }

    public static Detailing getChiselDetailing ()
    {
        return chiselDetailing;
    }

    public static TContent content;
    public static TEventHandler events;
    public static TPlayerHandler playerTracker;
    public static LiquidCasting tableCasting;
    public static LiquidCasting basinCasting;
    public static Detailing chiselDetailing;
}
