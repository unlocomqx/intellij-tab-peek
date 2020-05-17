import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.JBEditorTabs;
import com.intellij.ui.tabs.impl.TabLabel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class PeekStartupActivity implements StartupActivity, DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        MouseEventListener mouseEventListener = new MouseEventListener();
        Toolkit.getDefaultToolkit().addAWTEventListener(mouseEventListener, AWTEvent.MOUSE_EVENT_MASK);
    }
}

class MouseEventListener implements AWTEventListener {

    private final HashMap<JBEditorTabs, TabInfo> originalTab;
    private final HashMap<JBEditorTabs, Boolean> tabsListeners;
    private final HashMap<JBEditorTabs, Boolean> tabsTrigger;
    private final HashMap<JBEditorTabs, Timer> timers;
    private final HashMap<JBEditorTabs, TimerTask> timerTasks;

    MouseEventListener() {
        originalTab = new HashMap<>();
        tabsListeners = new HashMap<>();
        tabsTrigger = new HashMap<>();
        timers = new HashMap<>();
        timerTasks = new HashMap<>();
    }

    private void scheduleRestoreTask(JBEditorTabs tabs, TabInfo info) {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ApplicationManager.getApplication().invokeLater(() -> selectTab(tabs, info));
            }
        };
        timers.put(tabs, timer);
        timerTasks.put(tabs, timerTask);
        timer.schedule(timerTask, 1000);
    }

    private void cancelRestoreTask(JBEditorTabs tabs) {
        Timer timer = timers.get(tabs);
        TimerTask timerTask = timerTasks.get(tabs);

        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    @Override
    public void eventDispatched(AWTEvent event) {
        if (event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) event;
            if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED) {
                if (mouseEvent.getSource() instanceof TabLabel) {
                    TabLabel label = (TabLabel) mouseEvent.getSource();
                    Container parent = label.getParent();
                    if (parent instanceof JBEditorTabs) {
                        JBEditorTabs tabs = (JBEditorTabs) parent;
                        originalTab.put(tabs, tabs.getSelectedInfo());
                    }
                }
            }

            if (mouseEvent.getID() == MouseEvent.MOUSE_ENTERED) {
                if (mouseEvent.getSource() instanceof TabLabel) {
                    TabLabel label = (TabLabel) mouseEvent.getSource();
                    Container parent = label.getParent();
                    if (parent instanceof JBEditorTabs) {
                        JBEditorTabs tabs = (JBEditorTabs) parent;
                        if (tabsListeners.get(tabs) == null) {
                            tabs.addListener(new TabsListener() {
                                @Override
                                public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                                    if (tabsTrigger.get(tabs) == null || !tabsTrigger.get(tabs)) {
                                        originalTab.put(tabs, newSelection);
                                    }
                                }
                            });
                            tabsListeners.put(tabs, true);
                        }

                        TabInfo info = label.getInfo();
                        tabsTrigger.put(tabs, true);
                        if (originalTab.get(tabs) == null) {
                            originalTab.put(tabs, tabs.getSelectedInfo());
                        }

                        cancelRestoreTask(tabs);

                        selectTab(tabs, info);
                    }
                }
            }

            if (mouseEvent.getID() == MouseEvent.MOUSE_EXITED) {
                if (mouseEvent.getSource() instanceof TabLabel) {
                    TabLabel label = (TabLabel) mouseEvent.getSource();
                    Container parent = label.getParent();
                    if (parent instanceof JBEditorTabs) {
                        JBEditorTabs tabs = (JBEditorTabs) parent;
                        TabInfo originalTabInfo = originalTab.get(tabs);
                        if (originalTabInfo != null) {
                            scheduleRestoreTask(tabs, originalTabInfo);
                        }
                    }
                }
            }
        }
    }

    private void selectTab(JBEditorTabs tabs, TabInfo info) {
        tabsTrigger.put(tabs, true);
        tabs.select(info, false);
        tabsTrigger.put(tabs, false);
    }
}
