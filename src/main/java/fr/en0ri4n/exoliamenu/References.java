package fr.en0ri4n.exoliamenu;

import com.sun.jna.Function;
import net.minecraft.util.ResourceLocation;

public class References
{
    public static final String MOD_ID = "exoliamenu";
    public static final String NAME = "Exolia Menu";
    public static final String VERSION = "1.0";
    public static final String CLIENT = "fr.en0ri4n.exoliamenu.proxy.ClientProxy";
    public static final String SERVER = "fr.en0ri4n.exoliamenu.proxy.ServerProxy";

    public static final ResourceLocation FONT_LOCATION = getLoc("textures/font/ascii.png");
    public static final ResourceLocation SGA_FONT_LOCATION = getLoc("textures/font/ascii_sga.png");

    public static ResourceLocation getLoc(String path)
    {
        return new ResourceLocation(MOD_ID, path);
    }
}
