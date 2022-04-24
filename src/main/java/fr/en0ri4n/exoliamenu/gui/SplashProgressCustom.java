package fr.en0ri4n.exoliamenu.gui;

import fr.en0ri4n.exoliamenu.References;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.*;
import net.minecraft.crash.CrashReport;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.SharedDrawable;
import org.lwjgl.util.glu.GLU;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

public class SplashProgressCustom
{
    private static Drawable d;
    private static volatile boolean pause = false;
    private static volatile boolean done = false;
    private static Thread thread;
    private static volatile Throwable threadError;
    private static final Lock lock = new ReentrantLock(true);
    private static SplashProgressCustom.SplashFontRenderer fontRenderer;

    private static final IResourcePack mcPack = Minecraft.getMinecraft().defaultResourcePack;
    private static final IResourcePack fmlPack = createResourcePack(FMLSanityChecker.fmlLocation);
    private static IResourcePack miscPack;

    // New
    public static final ResourceLocation BACKGROUND = References.getLoc("textures/gui/loading_screen_background.png");
    private static Texture backgroundTexture;
    // New end

    private static SplashProgressCustom.Texture fontTexture;

    private static boolean enabled;
    private static int fontColor;
    private static int barBorderColor;
    private static int barColor;
    private static int barBackgroundColor;
    static boolean isDisplayVSyncForced = false;
    private static final int TIMING_FRAME_COUNT = 200;
    private static final int TIMING_FRAME_THRESHOLD = TIMING_FRAME_COUNT * 5 * 1000000; // 5 ms per frame, scaled to nanos

    static final Semaphore mutex = new Semaphore(1);

    public static void start()
    {
        enabled = ((!FMLClientHandler.instance().hasOptifine()) || Launch.blackboard.containsKey("optifine.ForgeSplashCompatible"));
        fontColor = 0x000000;
        barBorderColor = 0xC0C0C0;
        barColor = 0x03c200;
        barBackgroundColor = 0xFFFFFF;

        final ResourceLocation fontLoc = References.getLoc("textures/font/ascii.png");

        File miscPackFile = new File(Minecraft.getMinecraft().gameDir, "resources");

        miscPack = createResourcePack(miscPackFile);

        if(!enabled) return;
        // getting debug info out of the way, while we still can
        FMLCommonHandler.instance().registerCrashCallable(new ICrashCallable()
        {
            @Override
            public String call()
            {
                return "' Vendor: '" + glGetString(GL_VENDOR) + "' Version: '" + glGetString(GL_VERSION) + "' Renderer: '" + glGetString(GL_RENDERER) + "'";
            }

            @Override
            public String getLabel()
            {
                return "GL info";
            }
        });

        CrashReport report = CrashReport.makeCrashReport(new Throwable(), "Loading screen debug info");
        StringBuilder systemDetailsBuilder = new StringBuilder();
        report.getCategory().appendToStringBuilder(systemDetailsBuilder);
        FMLLog.log.info(systemDetailsBuilder.toString());

        try
        {
            d = new SharedDrawable(Display.getDrawable());
            Display.getDrawable().releaseContext();
            d.makeCurrent();
        }
        catch(LWJGLException e)
        {
            FMLLog.log.error("Error starting SplashProgress:", e);
            disableSplash(e);
        }

        //Call this ASAP if splash is enabled so that threading doesn't cause issues later
        getMaxTextureSize();

        //Thread mainThread = Thread.currentThread();
        thread = new Thread(new Runnable()
        {
            private int barWidth = 100;
            private int barHeight = 30;
            private long updateTiming;
            private long framecount;

            @Override
            public void run()
            {
                setGL();
                fontTexture = new Texture(fontLoc, null);
                backgroundTexture = new Texture(BACKGROUND, null, false);
                glEnable(GL_TEXTURE_2D);
                fontRenderer = new SplashFontRenderer();
                glDisable(GL_TEXTURE_2D);
                while(!done)
                {
                    framecount++;

                    barWidth = Display.getWidth() / 10 * 3;

                    ProgressManager.ProgressBar first = null, penult = null, last = null;
                    Iterator<ProgressManager.ProgressBar> i = ProgressManager.barIterator();
                    while(i.hasNext())
                    {
                        if(first == null) first = i.next();
                        else
                        {
                            penult = last;
                            last = i.next();
                        }
                    }

                    glClear(GL_COLOR_BUFFER_BIT);

                    // matrix setup
                    int w = Display.getWidth();
                    int h = Display.getHeight();
                    glViewport(0, 0, w, h);
                    glMatrixMode(GL_PROJECTION);
                    glLoadIdentity();
                    glOrtho(0, w, h, 0, -1, 1);
                    glMatrixMode(GL_MODELVIEW);
                    glEnable(GL_TEXTURE_2D);
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                    glLoadIdentity();

                    glPushMatrix();
                    glEnable(GL_TEXTURE_2D);
                    backgroundTexture.bind();
                    float scaleX = 1.07F;
                    float scaleY = 1.9F;
                    glTranslatef(0, 0, 0);
                    glBegin(GL_QUADS);
                    glTexCoord2f(0, 0);
                    glVertex2f(0, 0);
                    glTexCoord2f(1, 0);
                    glVertex2f(w * scaleX, 0);
                    glTexCoord2f(1, 1);
                    glVertex2f(w * scaleX, h * scaleY);
                    glTexCoord2f(0, 1);
                    glVertex2f(0, h * scaleY);
                    glEnd();
                    glLoadIdentity();

                    // background end

                    glDisable(GL_TEXTURE_2D);
                    glEnd();
                    glPopMatrix();

                    // bars
                    if(first != null)
                    {
                        glPushMatrix();
                        glTranslatef((float) (w - barWidth) / 2 + 2, (h / 20 * 15), 0);
                        drawBar(first);
                        glPopMatrix();
                    }


                    // Exolia logo
                    glColor4f(1, 1, 1, 1);
                    glEnd();

                    // We use mutex to indicate safely to the main thread that we're taking the display global lock
                    // So the main thread can skip processing messages while we're updating.
                    // There are system setups where this call can pause for a while, because the GL implementation
                    // is trying to impose a framerate or other thing is occurring. Without the mutex, the main
                    // thread would delay waiting for the same global display lock
                    mutex.acquireUninterruptibly();
                    long updateStart = System.nanoTime();
                    Display.update();
                    // As soon as we're done, we release the mutex. The other thread can now ping the processmessages
                    // call as often as it wants until we get get back here again
                    long dur = System.nanoTime() - updateStart;
                    if(framecount < TIMING_FRAME_COUNT)
                    {
                        updateTiming += dur;
                    }
                    mutex.release();
                    if(pause)
                    {
                        clearGL();
                        setGL();
                    }
                    // Such a hack - if the time taken is greater than 10 milliseconds, we're gonna guess that we're on a
                    // system where vsync is forced through the swapBuffers call - so we have to force a sleep and let the
                    // loading thread have a turn - some badly designed mods access Keyboard and therefore GlobalLock.lock
                    // during splash screen, and mutex against the above Display.update call as a result.
                    // 4 milliseconds is a guess - but it should be enough to trigger in most circumstances. (Maybe if
                    // 240FPS is possible, this won't fire?)
                    if(framecount >= TIMING_FRAME_COUNT && updateTiming > TIMING_FRAME_THRESHOLD)
                    {
                        if(!isDisplayVSyncForced)
                        {
                            isDisplayVSyncForced = true;
                            FMLLog.log.info("Using alternative sync timing : {} frames of Display.update took {} nanos", TIMING_FRAME_COUNT, updateTiming);
                        }
                        try
                        {
                            Thread.sleep(16);
                        }
                        catch(InterruptedException ignored)
                        {
                        }
                    }
                    else
                    {
                        if(framecount == TIMING_FRAME_COUNT)
                        {
                            FMLLog.log.info("Using sync timing. {} frames of Display.update took {} nanos", TIMING_FRAME_COUNT, updateTiming);
                        }
                        Display.sync(100);
                    }
                }
                clearGL();
            }

            private void setColor(int color)
            {
                glColor3ub((byte) ((color >> 16) & 0xFF), (byte) ((color >> 8) & 0xFF), (byte) (color & 0xFF));
            }

            private void drawBox(int w, int h)
            {
                glBegin(GL_QUADS);
                glVertex2f(0, 0);
                glVertex2f(0, h);
                glVertex2f(w, h);
                glVertex2f(w, 0);
                glEnd();
            }

            private void drawBar(ProgressManager.ProgressBar b)
            {
                barHeight = Display.getHeight() / 18;

                glPushMatrix();
                // title - message
                setColor(fontColor);
                glScalef(1F, 1F, 1);
                glEnable(GL_TEXTURE_2D);
                //fontRenderer.drawString(b.getTitle() + " - " + b.getMessage(), 0, 0, 0x000000);
                glDisable(GL_TEXTURE_2D);
                glPopMatrix();

                // border
                glPushMatrix();
                int textHeight2 = 20;
                glTranslatef(0, textHeight2, 0);
                setColor(barBorderColor);
                drawBox(barWidth, barHeight);
                // interior
                setColor(barBackgroundColor);
                glTranslatef(1, 1, 0);
                drawBox(barWidth - 2, barHeight - 2);
                // slidy part
                setColor(barColor);
                drawBox((barWidth - 2) * (b.getStep() + 1) / (b.getSteps() + 1), barHeight - 2); // Step can sometimes be 0.
                // progress text
                String progress = "Minecraft is Loading...";//" + b.getStep() + "/" + b.getSteps();
                float scaleFactor = 2F;
                glTranslatef(((float) barWidth - 2) / 2 - fontRenderer.getStringWidth(progress), (float) barHeight / 2F - 13, 0);
                setColor(fontColor);
                glScalef(scaleFactor, scaleFactor, 1);
                glEnable(GL_TEXTURE_2D);
                fontRenderer.drawString(progress, 0, 3, 0x000000);
                glPopMatrix();
            }

            private void setGL()
            {
                lock.lock();
                try
                {
                    Display.getDrawable().makeCurrent();
                }
                catch(LWJGLException e)
                {
                    FMLLog.log.error("Error setting GL context:", e);
                    throw new RuntimeException(e);
                }
                int backgroundColor = Color.black.getRGB();
                glClearColor((float) ((backgroundColor >> 16) & 0xFF) / 0xFF, (float) ((backgroundColor >> 8) & 0xFF) / 0xFF, (float) (backgroundColor & 0xFF) / 0xFF, 1);
                glDisable(GL_LIGHTING);
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            }

            private void clearGL()
            {
                Minecraft mc = Minecraft.getMinecraft();
                mc.displayWidth = Display.getWidth();
                mc.displayHeight = Display.getHeight();
                mc.resize(mc.displayWidth, mc.displayHeight);
                glClearColor(1, 1, 1, 1);
                glEnable(GL_DEPTH_TEST);
                glDepthFunc(GL_LEQUAL);
                glEnable(GL_ALPHA_TEST);
                glAlphaFunc(GL_GREATER, .1f);
                try
                {
                    Display.getDrawable().releaseContext();
                }
                catch(LWJGLException e)
                {
                    FMLLog.log.error("Error releasing GL context:", e);
                    throw new RuntimeException(e);
                }
                finally
                {
                    lock.unlock();
                }
            }
        });
        thread.setUncaughtExceptionHandler((t, e) ->
        {
            FMLLog.log.error("Splash thread Exception", e);
            threadError = e;
        });
        thread.start();
        checkThreadState();
    }

    private static int max_texture_size = -1;

    public static int getMaxTextureSize()
    {
        if(max_texture_size != -1) return max_texture_size;
        for(int i = 0x4000; i > 0; i >>= 1)
        {
            GlStateManager.glTexImage2D(GL_PROXY_TEXTURE_2D, 0, GL_RGBA, i, i, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
            if(GlStateManager.glGetTexLevelParameteri(GL_PROXY_TEXTURE_2D, 0, GL_TEXTURE_WIDTH) != 0)
            {
                max_texture_size = i;
                return i;
            }
        }
        return -1;
    }

    private static void checkThreadState()
    {
        if(thread.getState() == Thread.State.TERMINATED || threadError != null)
        {
            throw new IllegalStateException("Splash thread", threadError);
        }
    }

    /**
     * Call before you need to explicitly modify GL context state during loading.
     * Resource loading doesn't usually require this call.
     * Call {@link #resume()} when you're done.
     *
     * @deprecated not a stable API, will break, don't use this yet
     */
    @Deprecated
    public static void pause()
    {
        if(!enabled) return;
        checkThreadState();
        pause = true;
        lock.lock();
        try
        {
            d.releaseContext();
            Display.getDrawable().makeCurrent();
        }
        catch(LWJGLException e)
        {
            FMLLog.log.error("Error setting GL context:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @deprecated not a stable API, will break, don't use this yet
     */
    @Deprecated
    public static void resume()
    {
        if(!enabled) return;
        checkThreadState();
        pause = false;
        try
        {
            Display.getDrawable().releaseContext();
            d.makeCurrent();
        }
        catch(LWJGLException e)
        {
            FMLLog.log.error("Error releasing GL context:", e);
            throw new RuntimeException(e);
        }
        lock.unlock();
    }

    public static void finish()
    {
        if(!enabled) return;
        try
        {
            checkThreadState();
            done = true;
            thread.join();
            glFlush();        // process any remaining GL calls before releaseContext (prevents missing textures on mac)
            d.releaseContext();
            Display.getDrawable().makeCurrent();
            fontTexture.delete();
            backgroundTexture.delete();
        }
        catch(Exception e)
        {
            FMLLog.log.error("Error finishing SplashProgress:", e);
            disableSplash(e);
        }
    }

    private static void disableSplash(Exception e)
    {
        if(disableSplash())
        {
            throw new EnhancedRuntimeException(e)
            {
                @Override
                protected void printStackTrace(WrappedPrintStream stream)
                {
                    stream.println("SplashProgress has detected a error loading Minecraft.");
                    stream.println("This can sometimes be caused by bad video drivers.");
                    stream.println("We have automatically disabled the new Splash Screen in config/splash.properties.");
                    stream.println("Try reloading minecraft before reporting any errors.");
                }
            };
        }
        else
        {
            throw new EnhancedRuntimeException(e)
            {
                @Override
                protected void printStackTrace(WrappedPrintStream stream)
                {
                    stream.println("SplashProgress has detected a error loading Minecraft.");
                    stream.println("This can sometimes be caused by bad video drivers.");
                    stream.println("Please try disabling the new Splash Screen in config/splash.properties.");
                    stream.println("After doing so, try reloading minecraft before reporting any errors.");
                }
            };
        }
    }

    private static boolean disableSplash()
    {
        File configFile = new File(Minecraft.getMinecraft().gameDir, "config/splash.properties");
        File parent = configFile.getParentFile();
        if(!parent.exists()) parent.mkdirs();

        enabled = false;
        return true;
    }

    private static IResourcePack createResourcePack(File file)
    {
        if(file.isDirectory())
        {
            return new FolderResourcePack(file);
        }
        else
        {
            return new FileResourcePack(file);
        }
    }

    private static final IntBuffer buf = BufferUtils.createIntBuffer(4 * 1024 * 1024);

    @SuppressWarnings("unused")
    private static class Texture
    {
        private final ResourceLocation location;
        private final int name;
        private final int width;
        private final int height;
        private final int frames;
        private final int size;

        public Texture(ResourceLocation location, @Nullable ResourceLocation fallback)
        {
            this(location, fallback, true);
        }

        public Texture(ResourceLocation location, @Nullable ResourceLocation fallback, boolean allowRP)
        {
            InputStream s = null;
            try
            {
                this.location = location;
                s = open(location, fallback, allowRP);
                ImageInputStream stream = ImageIO.createImageInputStream(s);
                Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
                if(!readers.hasNext()) throw new IOException("No suitable reader found for image" + location);
                ImageReader reader = readers.next();
                reader.setInput(stream);
                int frames = reader.getNumImages(true);
                BufferedImage[] images = new BufferedImage[frames];
                for(int i = 0; i < frames; i++)
                {
                    images[i] = reader.read(i);
                }
                reader.dispose();
                width = images[0].getWidth();
                int height = images[0].getHeight();
                // Animation strip
                if(height > width && height % width == 0)
                {
                    frames = height / width;
                    BufferedImage original = images[0];
                    height = width;
                    images = new BufferedImage[frames];
                    for(int i = 0; i < frames; i++)
                    {
                        images[i] = original.getSubimage(0, i * height, width, height);
                    }
                }
                this.frames = frames;
                this.height = height;
                int size = 1;
                while((size / width) * (size / height) < frames) size *= 2;
                this.size = size;
                glEnable(GL_TEXTURE_2D);
                synchronized(SplashProgress.class)
                {
                    name = glGenTextures();
                    glBindTexture(GL_TEXTURE_2D, name);
                }
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size, size, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) null);
                checkGLError("Texture creation");
                for(int i = 0; i * (size / width) < frames; i++)
                {
                    for(int j = 0; i * (size / width) + j < frames && j < size / width; j++)
                    {
                        buf.clear();
                        BufferedImage image = images[i * (size / width) + j];
                        for(int k = 0; k < height; k++)
                        {
                            for(int l = 0; l < width; l++)
                            {
                                buf.put(image.getRGB(l, k));
                            }
                        }
                        buf.position(0).limit(width * height);
                        glTexSubImage2D(GL_TEXTURE_2D, 0, j * width, i * height, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buf);
                        checkGLError("Texture uploading");
                    }
                }
                glBindTexture(GL_TEXTURE_2D, 0);
                glDisable(GL_TEXTURE_2D);
            }
            catch(IOException e)
            {
                FMLLog.log.error("Error reading texture from file: {}", location, e);
                throw new RuntimeException(e);
            }
            finally
            {
                IOUtils.closeQuietly(s);
            }
        }

        public ResourceLocation getLocation()
        {
            return location;
        }

        public int getName()
        {
            return name;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }

        public int getFrames()
        {
            return frames;
        }

        public int getSize()
        {
            return size;
        }

        public void bind()
        {
            glBindTexture(GL_TEXTURE_2D, name);
        }

        public void delete()
        {
            glDeleteTextures(name);
        }

        public float getU(int frame, float u)
        {
            return width * (frame % (float) (size / width) + u) / size;
        }

        public float getV(int frame, float v)
        {
            return height * (frame / (float) (size / width) + v) / size;
        }

        public void texCoord(int frame, float u, float v)
        {
            glTexCoord2f(getU(frame, u), getV(frame, v));
        }
    }

    private static class SplashFontRenderer extends FontRenderer
    {
        public SplashFontRenderer()
        {
            super(Minecraft.getMinecraft().gameSettings, fontTexture.getLocation(), null, false);
            super.onResourceManagerReload(null);
        }

        @Override
        protected void bindTexture(@Nonnull ResourceLocation location)
        {
            if(location != locationFontTexture) throw new IllegalArgumentException();
            fontTexture.bind();
        }

        @Nonnull
        @Override
        protected IResource getResource(@Nonnull ResourceLocation location) throws IOException
        {
            DefaultResourcePack pack = Minecraft.getMinecraft().defaultResourcePack;
            return new SimpleResource(pack.getPackName(), location, pack.getInputStream(location), null, null);
        }
    }

    public static void drawVanillaScreen(TextureManager renderEngine) throws LWJGLException
    {
        if(!enabled)
        {
            Minecraft.getMinecraft().drawSplashScreen(renderEngine);
        }
    }

    public static void clearVanillaResources(TextureManager renderEngine, ResourceLocation mojangLogo)
    {
        if(!enabled)
        {
            renderEngine.deleteTexture(mojangLogo);
        }
    }

    public static void checkGLError(String where)
    {
        int err = glGetError();
        if(err != 0)
        {
            throw new IllegalStateException(where + ": " + GLU.gluErrorString(err));
        }
    }

    private static InputStream open(ResourceLocation loc, @Nullable ResourceLocation fallback, boolean allowResourcePack) throws IOException
    {
        if(!allowResourcePack) return mcPack.getInputStream(loc);

        if(miscPack.resourceExists(loc))
        {
            return miscPack.getInputStream(loc);
        }
        else if(fmlPack.resourceExists(loc))
        {
            return fmlPack.getInputStream(loc);
        }
        else if(!mcPack.resourceExists(loc) && fallback != null)
        {
            return open(fallback, null, true);
        }
        return mcPack.getInputStream(loc);
    }

    private static int bytesToMb(long bytes)
    {
        return (int) (bytes / 1024L / 1024L);
    }
}
