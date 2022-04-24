package fr.en0ri4n.exoliamenu.gui;

import fr.en0ri4n.exoliamenu.References;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiModList;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class GuiMainMenuExolia extends GuiScreen
{
    private static final String SKIN_DATABASE_URL = "https://exolia.site/skins/%s.png";
    private static final ResourceLocation BACKGROUND = References.getLoc("textures/gui/background.png");
    private ResourceLocation playerSkinLocation;
    private final Random random = new Random();
    private Color randomColor;

    public GuiMainMenuExolia()
    {
        this.playerSkinLocation = new ResourceLocation("textures/entity/steve.png");
    }

    @Override
    public void initGui()
    {
        this.setSkinTextureWhenAvailable(mc.getSession().getUsername());
        this.randomColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)).brighter();

        int centerX = this.width / 6 + ((this.width / 5 * 2) - this.width / 6) / 2;
        this.addButton(new ButtonText(10, centerX - 60 / 2, this.height - 130, 60, 20, 1.5D, TextFormatting.UNDERLINE + "Exolia", randomColor.getRGB()));
        this.addButton(new ButtonText(11, centerX - 70 / 2, this.height - 90, 70, 20, 1.5D, I18n.format("menu.options"), Color.GRAY.getRGB()));
        this.addButton(new ButtonText(12, centerX - 75 / 2, this.height - 50, 75, 20, 1.5D, I18n.format("menu.quit"), Color.RED.darker().getRGB()));

        // this.addButton(new GuiButton(3, 10, 10, 30, 30, "DEBUG-SOLO")); // Debug To Test in Solo
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        int textWidth = mc.fontRenderer.getStringWidth(I18n.format("menu.quit"));

        mc.getTextureManager().bindTexture(BACKGROUND);
        GuiScreen.drawScaledCustomSizeModalRect(0, 0, 0, 0, 1920, 1080, this.width, this.height, 1920, 1080);
        this.drawGradientRect(this.width / 6 - (textWidth / 6), 0, this.width / 5 * 2 + (textWidth / 6), this.height, 0x80000000, 0x80000000);

        mc.getTextureManager().bindTexture(References.getLoc("textures/gui/logo.png"));
        int size = (this.width / 5 * 2 - this.width / 6) - 10;
        GuiScreen.drawScaledCustomSizeModalRect(this.width / 6 + 5, 5, 0, 0, 1920, 1920, size, size, 1920, 1920);

        int nameBoxX = this.width / 24 * 17 - 40;
        int nameBoxY = this.height / 5 * 2 - 70;
        int nameBoxWidth = (this.width / 24 * 17 + 40) - (this.width / 24 * 17 - 40);
        int nameBoxHeight = (this.height / 5 * 2 - 70) - (this.height / 5 * 2 - 50);
        this.drawGradientRect(this.width / 24 * 17 - 40, this.height / 5 * 2 - 70, this.width / 24 * 17 + 40, this.height / 5 * 2 - 50, 0x80000000, 0x80000000);
        this.drawCenteredString(mc.fontRenderer, mc.getSession().getUsername(), nameBoxX + nameBoxWidth / 2, nameBoxY - nameBoxHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2, new Color((Color.HSBtoRGB(System.currentTimeMillis() % 10000L / 10000.0f, 1.0f, 1.0f))).getRGB());
        this.drawEntityOnScreen(this.width / 24 * 17, this.height / 5 * 2, 80);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        switch(button.id)
        {
            case 3: // Debug Case
                mc.displayGuiScreen(new GuiWorldSelection(this));
                break;
            case 10:
                mc.displayGuiScreen(new GuiConnecting(this, mc, new ServerData("Exolia", "exolia.serveminecraft.net", false)));
                break;
            case 11:
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                break;
            case 12:
                mc.shutdown();
                break;
        }
    }

    private void drawEntityOnScreen(int posX, int posY, int scale)
    {
        mc.getTextureManager().bindTexture(playerSkinLocation);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
        GlStateManager.rotate((System.currentTimeMillis() % 6000L / 6000.0f) * 360, 0.0F, 360.0F, 0.0F);
        ModelPlayer modelplayer = new ModelPlayer(0.0F, false);
        GlStateManager.enableDepth();
        //GlStateManager.enableAlpha();
        modelplayer.bipedHead.render(0.0625F);
        modelplayer.bipedHeadwear.render(0.0625F);
        modelplayer.bipedBody.render(0.0625F);
        modelplayer.bipedBodyWear.render(0.0625F);
        modelplayer.bipedRightArm.render(0.0625F);
        // modelplayer.bipedRightArmwear.render(0.0625F); // Not displaying correctly, idk why
        modelplayer.bipedLeftArm.render(0.0625F);
        // modelplayer.bipedLeftArmwear.render(0.0625F);
        modelplayer.bipedRightLeg.render(0.0625F);
        modelplayer.bipedRightLegwear.render(0.0625F);
        modelplayer.bipedLeftLeg.render(0.0625F);
        modelplayer.bipedLeftLegwear.render(0.0625F);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public void setSkinTextureWhenAvailable(String username)
    {
        try
        {
            URL url = new URL(String.format(SKIN_DATABASE_URL, username));
            BufferedImage image = ImageIO.read(url);
            DynamicTexture texture = new DynamicTexture(image);
            this.playerSkinLocation = mc.getTextureManager().getDynamicTextureLocation("main_menu_skin", texture);
        }
        catch(IOException ignored)
        {
        }
    }
}
