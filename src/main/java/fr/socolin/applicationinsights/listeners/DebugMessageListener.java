package fr.socolin.applicationinsights.listeners;


import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessListener;
import org.jetbrains.annotations.NotNull;

public class DebugMessageListener implements DebugProcessListener {

    @Override
    public void processAttached(@NotNull DebugProcess process) {

    }
}
/*
import com.intellij.debugger.impl.DebuggerManagerListener;
import com.intellij.debugger.impl.DebuggerSession;
public class DebugMessageListener implements DebuggerManagerListener {
    @Override
    public void sessionAttached(DebuggerSession session) {
        System.out.println("hello");
    }
    //        XDebuggerManager.getInstance(session.getProject()).getDebugSessions()[0].getDebugProcess();
}
*/
