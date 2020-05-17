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

public class PeekStartupActivity implements StartupActivity, DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        MouseEventListener mouseEventListener = new MouseEventListener();
        Toolkit.getDefaultToolkit().addAWTEventListener(mouseEventListener, AWTEvent.MOUSE_EVENT_MASK);
    }
}

class MouseEventListener implements AWTEventListener {

    private final HashMap<JBEditorTabs, TabInfo> selectedTab;
    private final HashMap<JBEditorTabs, Boolean> tabsListeners;
    private final HashMap<JBEditorTabs, Boolean> tabsTrigger;

    MouseEventListener() {
        selectedTab = new HashMap<>();
        tabsListeners = new HashMap<>();
        tabsTrigger = new HashMap<>();
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
                        selectedTab.put(tabs, tabs.getSelectedInfo());
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
                                        selectedTab.put(tabs, newSelection);
                                    }
                                }
                            });
                            tabsListeners.put(tabs, true);
                        }

                        TabInfo info = label.getInfo();
                        tabsTrigger.put(tabs, true);
                        if (selectedTab.get(tabs) == null) {
                            selectedTab.put(tabs, tabs.getSelectedInfo());
                        }
                        tabs.select(info, false);
                        tabsTrigger.put(tabs, false);
                    }
                }
            }

            if (mouseEvent.getID() == MouseEvent.MOUSE_EXITED) {
                if (mouseEvent.getSource() instanceof TabLabel) {
                    TabLabel label = (TabLabel) mouseEvent.getSource();
                    Container parent = label.getParent();
                    if (parent instanceof JBEditorTabs) {
                        JBEditorTabs tabs = (JBEditorTabs) parent;
                        TabInfo info = label.getInfo();

                        TabInfo tabInfo = selectedTab.get(tabs);
                        if (tabInfo != null) {
                            tabsTrigger.put(tabs, true);
                            tabs.select(tabInfo, false);
                            tabsTrigger.put(tabs, false);
                        }
                    }
                }
            }
        }
    }
}
