package fr.en0ri4n.exoliamenu.core;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;

public final class ASMHelper
{
    public static MethodNode findMethod(ClassNode cnode, String name, String desc)
    {
        for(MethodNode m : cnode.methods)
        {
            if(name.equals(m.name) && desc.equals(m.desc)) return m;
        }
        return null;
    }

    public static AbstractInsnNode getFirstInstrWithOpcode(MethodNode mn, int opcode)
    {
        Iterator<AbstractInsnNode> ite = mn.instructions.iterator();
        while(ite.hasNext())
        {
            AbstractInsnNode n = ite.next();
            if(n.getOpcode() == opcode) return n;
        }
        return null;
    }
}