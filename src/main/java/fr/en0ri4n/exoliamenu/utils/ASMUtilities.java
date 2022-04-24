package fr.en0ri4n.exoliamenu.utils;

import fr.en0ri4n.exoliamenu.References;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public class ASMUtilities
{
    public static void patchFont(ResourceLocation location, TextureManager textureManager)
    {
        if(location.toString().equals("minecraft:textures/font/ascii.png"))
        {
            textureManager.bindTexture(References.FONT_LOCATION);
        }
        else if(location.toString().equals("minecraft:textures/font/ascii_sga.png"))
        {
            textureManager.bindTexture(References.SGA_FONT_LOCATION);
        }
    }
}
