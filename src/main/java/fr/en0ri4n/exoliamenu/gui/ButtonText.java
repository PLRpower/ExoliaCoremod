package fr.en0ri4n.exoliamenu.gui;

import fr.en0ri4n.exoliamenu.References;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ButtonText extends GuiButton
{
    static final Minecraft minecraft = Minecraft.getMinecraft();
    private static final ResourceLocation BUTTON_TEXTURE = References.getLoc("textures/gui/buttons/default_button.png");
    private final double scale;
    private final int color;

    public ButtonText(int buttonId, int x, int y, int width, int height, double scale, String buttonText, int color)
    {
        super(buttonId, x, y, width, height, buttonText);
        this.scale = scale;
        this.color = color;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        mc.getTextureManager().bindTexture(BUTTON_TEXTURE);

        int color = this.color;
        if(this.isButtonHovered(mouseX, mouseY))
        {
            //this.drawGradientRect(this.x, this.y, this.x + this.width, this.y + this.height, 0x80FFFFFF, 0x80FFFFFF);
            color = new Color(this.color).brighter().getRGB();
        }

        this.drawGradientRect(x, y, width, height, 0xFFFFFF, 0xFFFFFF);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1D);
        this.drawCenteredString(mc.fontRenderer, this.displayString, (int) ((double) (this.x + this.width / 2) / scale), (int) ((double) (this.y + this.height / 2 - mc.fontRenderer.FONT_HEIGHT / 2) / scale), color == -1 ? rainbowColor() : color);
        GlStateManager.popMatrix();
    }

    private boolean isButtonHovered(int mouseX, int mouseY)
    {
        return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    private int rainbowColor()
    {
        return new Color((Color.HSBtoRGB(System.currentTimeMillis() % 2000L / 2000.0f, 1.0f, 1.0f))).getRGB();
    }
}
