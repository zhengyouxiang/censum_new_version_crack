package me.geek01.javaagent;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created By Arthur Zhang at 2018-12-20
 */
public class MyClassFileTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException {
        if (className.equals("com/jclarity/censum/license/LicenseCheck")) {
            System.out.println("##########enter here: " + className);
            try {
                ClassReader cr = new ClassReader(classBytes);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                ClassVisitor cv = new MyClassVisitor(className, cw);
                cr.accept(cv, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                byte[] bytes = cw.toByteArray();
                String fileName = className.replaceAll("/", "_");
                writeByteArrayToFile(classBytes, new File(fileName + ".class"));
                writeByteArrayToFile(bytes, new File(fileName + "-modify.class"));
                System.out.println("##########write file done: " + className);
                return bytes;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return classBytes;
    }

    public static class MyClassVisitor extends ClassVisitor {
        private String className;

        public MyClassVisitor(String className, ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
            this.className = className;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            System.out.println("visiting: " + className + name);
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if (name.equals("getCanLoadState")) {
                return new MyMethodVisitor(mv, access, name, desc);
            }
            return mv;
        }
    }

    public static class MyMethodVisitor extends AdviceAdapter {

        protected MyMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
            super(Opcodes.ASM5, mv, access, name, desc);
        }

        @Override
        protected void onMethodEnter() {
            mv.visitFieldInsn(GETSTATIC, "com/jclarity/censum/license/CanLoadState", "SUCCESS", "Lcom/jclarity/censum/license/CanLoadState;");
            mv.visitInsn(ARETURN);
        }
    }

    private static void writeByteArrayToFile(byte[] bytes, File file) {

        OutputStream out = null;
        try {
            out = new FileOutputStream(file, false);
            out.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }
        }
    }
}
