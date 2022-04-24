package fr.en0ri4n.exoliamenu.proxy;

import fr.en0ri4n.exoliamenu.ExoliaMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ClientProxy implements IProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        setWindowInfos();
    }

    @Override
    public void init(FMLInitializationEvent event)
    {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {

    }

    public void setWindowInfos()
    {
        setWindowIcon();

        Display.setTitle("Exolia - " + Minecraft.getMinecraft().getSession().getUsername());
    }

    public void setWindowIcon()
    {
        Util.EnumOS osType = Util.getOSType();

        if (osType != Util.EnumOS.OSX)
        {
            InputStream icon16 = null;
            InputStream icon32 = null;

            try
            {
                icon16 = getResource("icons/icon_16.png");
                icon32 = getResource("icons/icon_32.png");

                if (icon16 != null && icon32 != null)
                {
                    Display.setIcon(new ByteBuffer[] {this.readImageToBuffer(icon16), this.readImageToBuffer(icon32)});
                }
            }
            catch (IOException ioexception)
            {
                ioexception.printStackTrace();
            }
            finally
            {
                IOUtils.closeQuietly(icon16);
                IOUtils.closeQuietly(icon32);
            }
        }
    }

    private static InputStream getResource(String path)
    {
        return ExoliaMenu.class.getResourceAsStream("/" + path);
    }

    private ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException
    {
        BufferedImage bufferedimage = ImageIO.read(imageStream);
        int[] aint = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * aint.length);

        for (int i : aint)
        {
            bytebuffer.putInt(i << 8 | i >> 24 & 255);
        }

        bytebuffer.flip();
        return bytebuffer;
    }
}
