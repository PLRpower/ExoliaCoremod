package fr.en0ri4n.exoliamenu.events;

import fr.en0ri4n.exoliamenu.gui.GuiMainMenuExolia;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreenServerList;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class GuiEvents
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGui(GuiOpenEvent event)
    {
        if(event.getGui() instanceof GuiMainMenu || event.getGui() instanceof GuiScreenServerList)
        {
            event.setGui(new GuiMainMenuExolia());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGui(GuiScreenEvent.InitGuiEvent event)
    {
        if(event.getGui() instanceof GuiIngameMenu)
        {
            event.getButtonList().removeIf(button -> button.id == 7 || button.id == 12);
            sortButtons(event);
        }
    }

    private static void sortButtons(GuiScreenEvent.InitGuiEvent event)
    {
        int width = event.getGui().width;
        int height = event.getGui().height;
        boolean doubleButton = false;

        event.getButtonList().forEach(button -> button.y = 0);

        int line = 0;
        for(int i = 0; i < event.getButtonList().size(); i++)
        {
            GuiButton button = event.getButtonList().get(i);

            if(button.id == 1 || !button.visible) continue;

            if(button.width > 100)
            {
                button.width = 200;
                button.y = getHeight(height, line);
                button.x = width / 2 - 100;
                line++;
            }
            else
            {
                button.width = 98;
                button.y = getHeight(height, line);

                if(!doubleButton)
                {
                    button.x = width / 2 - 100;
                    doubleButton = true;

                    if(i == event.getButtonList().size() - 1)
                    {
                        button.width = 200;
                        line++;
                    }
                }
                else
                {
                    button.x = width / 2 + 2;
                    doubleButton = false;
                    line++;
                }
            }
        }

        // Disconnect button
        int finalLine = line;
        event.getButtonList().stream().filter(button -> button.id == 1).findFirst().ifPresent(button -> button.y = getHeight(height, finalLine));
    }

    private static int getHeight(int height, int line)
    {
        return height / 4 + 24 + (24 * line) + -16;
    }
}
