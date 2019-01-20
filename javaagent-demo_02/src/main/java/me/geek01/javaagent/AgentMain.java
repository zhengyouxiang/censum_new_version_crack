package me.geek01.javaagent;

import java.lang.instrument.Instrumentation;

/**
 * Created By Arthur Zhang at 2018-12-18
 */
public class AgentMain {

    public static void premain(String agentArgument, Instrumentation instrumentation) throws Exception {
        instrumentation.addTransformer(new MyClassFileTransformer(), true);
    }
}
