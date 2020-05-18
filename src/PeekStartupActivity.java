import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
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

    private final TabPeekConfig mConfig;

    private final HashMap<JBEditorTabs, TabInfo> originalTab;
    private final HashMap<JBEditorTabs, Boolean> tabsListeners;
    private final HashMap<JBEditorTabs, Boolean> tabsTrigger;

    private final HashMap<JBEditorTabs, Timer> switchTimers;
    private final HashMap<JBEditorTabs, TimerTask> switchTimerTasks;

    private final HashMap<JBEditorTabs, Timer> restoreTimers;
    private final HashMap<JBEditorTabs, TimerTask> restoreTimerTasks;

    MouseEventListener() {
        mConfig = TabPeekConfig.getInstance();
        originalTab = new HashMap<>();
        tabsListeners = new HashMap<>();
        tabsTrigger = new HashMap<>();
        switchTimers = new HashMap<>();
        switchTimerTasks = new HashMap<>();
        restoreTimers = new HashMap<>();
        restoreTimerTasks = new HashMap<>();
    }

    private void scheduleSwitchTask(JBEditorTabs tabs, TabInfo info) {
        cancelSwitchTask(tabs);

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ApplicationManager.getApplication().invokeLater(() -> selectTab(tabs, info));
            }
        };

        switchTimers.put(tabs, timer);
        switchTimerTasks.put(tabs, timerTask);
        timer.schedule(timerTask, mConfig.getSwitchDelay());
    }

    private void cancelSwitchTask(JBEditorTabs tabs) {
        Timer timer = switchTimers.get(tabs);
        TimerTask timerTask = switchTimerTasks.get(tabs);

        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    private void scheduleRestoreTask(JBEditorTabs tabs, TabInfo info) {
        cancelRestoreTask(tabs);

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ApplicationManager.getApplication().invokeLater(() -> selectTab(tabs, info, true));
            }
        };
        restoreTimers.put(tabs, timer);
        restoreTimerTasks.put(tabs, timerTask);
        timer.schedule(timerTask, mConfig.getRestoreDelay());
    }

    private void cancelRestoreTask(JBEditorTabs tabs) {
        Timer timer = restoreTimers.get(tabs);
        TimerTask timerTask = restoreTimerTasks.get(tabs);

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
                        saveCurrentTab(tabs);
                    }
                }

                if (mouseEvent.getSource() instanceof EditorComponentImpl) {
                    // if the editor is clicked, persist current tab
                    EditorComponentImpl editor = (EditorComponentImpl) mouseEvent.getSource();
                    JBEditorTabs tabs = getEditorTabbedContainer(editor);
                    if (tabs != null) {
                        cancelRestoreTask(tabs);
                        cancelSwitchTask(tabs);
                        saveCurrentTab(tabs);
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
                            addTabsListener(tabs);
                        }

                        if (originalTab.get(tabs) == null) {
                            saveCurrentTab(tabs);
                        }

                        cancelRestoreTask(tabs);
                        cancelSwitchTask(tabs);

                        TabInfo info = label.getInfo();
                        scheduleSwitchTask(tabs, info);
                    }
                }
            }

            if (mouseEvent.getID() == MouseEvent.MOUSE_EXITED) {
                if (mouseEvent.getSource() instanceof TabLabel) {
                    TabLabel label = (TabLabel) mouseEvent.getSource();
                    Container parent = label.getParent();
                    if (parent instanceof JBEditorTabs) {
                        JBEditorTabs tabs = (JBEditorTabs) parent;

                        cancelSwitchTask(tabs);

                        TabInfo originalTabInfo = originalTab.get(tabs);
                        if (originalTabInfo != null) {
                            scheduleRestoreTask(tabs, originalTabInfo);
                        }
                    }
                }
            }
        }
    }

    private void saveCurrentTab(JBEditorTabs tabs) {
        saveSelectedTab(tabs, tabs.getSelectedInfo());
    }

    private JBEditorTabs getEditorTabbedContainer(EditorComponentImpl editor) {
        Container parent = editor;

        do {
            parent = parent.getParent();
        } while (parent != null && !(parent instanceof JBEditorTabs));

        return parent != null ? (JBEditorTabs) parent : null;
    }

    private void saveSelectedTab(JBEditorTabs tabs, TabInfo tabInfo) {
        originalTab.put(tabs, tabInfo);
    }

    private void addTabsListener(JBEditorTabs tabs) {
        tabs.addListener(new TabsListener() {
            @Override
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
                if (tabsTrigger.get(tabs) == null || !tabsTrigger.get(tabs)) {
                    saveSelectedTab(tabs, newSelection);
                }
            }
        });
        tabsListeners.put(tabs, true);
    }

    private void selectTab(JBEditorTabs tabs, TabInfo info) {
        tabsTrigger.put(tabs, true);
        tabs.select(info, false);
        tabsTrigger.put(tabs, false);
    }

    private void selectTab(JBEditorTabs tabs, TabInfo info, Boolean requestFocus) {
        tabsTrigger.put(tabs, true);
        tabs.select(info, requestFocus);
        tabsTrigger.put(tabs, false);
    }
}
