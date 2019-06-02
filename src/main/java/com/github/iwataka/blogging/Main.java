package com.github.iwataka.blogging;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Main {

    private static ClassPool classPool;

    private static String classPattern = ".*";

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        classPool = ClassPool.getDefault();
        if (agentArgs != null && !agentArgs.isEmpty()) {
            classPattern = agentArgs;
        }

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader,
                                    String className,
                                    Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws IllegalClassFormatException {
                try {
                    ByteArrayInputStream stream = new ByteArrayInputStream(classfileBuffer);
                    CtClass ctClass = classPool.makeClass(stream);

                    if (!ctClass.getName().matches(classPattern)) {
                        return null;
                    }

                    CtMethod[] methods = ctClass.getDeclaredMethods();
                    for (CtMethod method : methods) {
                        String name = method.getName();
                        if (!name.startsWith("set")) {
                            continue;
                        }
                        CtClass[] types = method.getParameterTypes();
                        if (types.length != 1) {
                            continue;
                        }
                        int modifiers = method.getModifiers();
                        if (!Modifier.isPublic(modifiers)) {
                            continue;
                        }
                        method.insertBefore("System.out.println(\"" + ctClass.getName() + "#" + name + " called with \" + $args[0]);");
                    }

                    return ctClass.toBytecode();
                } catch (Exception ex) {
                    IllegalClassFormatException e = new IllegalClassFormatException();
                    e.initCause(ex);
                    throw e;
                }

            }
        });
    }
}
