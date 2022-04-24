package fr.en0ri4n.exoliamenu.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ExoliaClassTransformer implements IClassTransformer
{
    public static final Logger LOGGER = LogManager.getLogger("Exolia-CoreMod");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(name.equals("net.minecraftforge.fml.client.SplashProgress"))
        {
            LOGGER.info("Class to patch : " + name);
            return patchSplashProgress(name, basicClass);
        }
        else if(name.equals("bip") || name.equals("net.minecraft.client.gui.FontRenderer"))
        {
            LOGGER.info("About to patch : " + name);
            return patchFontRenderer(name, basicClass, name.equals("bip"));
        }

        return basicClass;
    }

    private byte[] patchFontRenderer(String name, byte[] basicClass, boolean obf)
    {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        MethodNode methodNode = classNode.methods.stream().filter(method -> method.name.equals("bindTexture")).findFirst().get();//ASMHelper.findMethod(classNode, "bindTexture", "(Lnet/minecraft/util/ResourceLocation;)V");

        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/FontRenderer", obf ? "field_78298_i" : "renderEngine", "Lnet/minecraft/client/renderer/texture/TextureManager;"));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "fr/en0ri4n/exoliamenu/utils/ASMUtilities", "patchFont", "(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/TextureManager;)V", false));
        instructions.add(new InsnNode(Opcodes.RETURN));
        methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), instructions);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);

        LOGGER.info("Patched : " + name);
        return cw.toByteArray();
    }

    private byte[] patchSplashProgress(String name, byte[] basicClass)
    {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        MethodNode startMethodNode = ASMHelper.findMethod(classNode, "start", "()V"); // On récupère le contenu de la méthode
        InsnList startMethodInstr = new InsnList();
        startMethodInstr.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "fr/en0ri4n/exoliamenu/gui/SplashProgressCustom", "start", "()V", false));
        startMethodInstr.add(new InsnNode(Opcodes.RETURN));
        if(startMethodNode != null)
            startMethodNode.instructions.insertBefore(startMethodNode.instructions.getFirst(), startMethodInstr);

        MethodNode finishMethodNode = ASMHelper.findMethod(classNode, "finish", "()V"); // On récupère le contenu de la méthode
        InsnList finishMethodInstr = new InsnList();
        finishMethodInstr.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "fr/en0ri4n/exoliamenu/gui/SplashProgressCustom", "finish", "()V", false));
        finishMethodInstr.add(new InsnNode(Opcodes.RETURN));
        if(finishMethodNode != null)
            finishMethodNode.instructions.insertBefore(finishMethodNode.instructions.getFirst(), finishMethodInstr);


        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);

        LOGGER.info("Patched : " + name);
        return cw.toByteArray();
    }
}
