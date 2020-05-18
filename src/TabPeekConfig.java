import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PersistentStateComponent keeps config values.
 */
@State(
        name = "TabPeekConfig",
        storages = {
                @Storage("TabPeekConfig.xml")}
)
public class TabPeekConfig implements PersistentStateComponent<TabPeekConfig> {

    static final int DEFAULT_SWITCH_DELAY = 333;
    static final int DEFAULT_RESTORE_DELAY = 500;

    private int switchDelay = DEFAULT_SWITCH_DELAY;
    private int restoreDelay = DEFAULT_RESTORE_DELAY;

    public int getSwitchDelay() {
        return switchDelay;
    }

    public int getRestoreDelay() {
        return restoreDelay;
    }

    @Nullable
    @Override
    public TabPeekConfig getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TabPeekConfig tabPeekConfig) {
        XmlSerializerUtil.copyBean(tabPeekConfig, this);
    }

    @Nullable
    public static TabPeekConfig getInstance() {
        return ServiceManager.getService(TabPeekConfig.class);
    }

    public void setSwitchDelay(int value) {
        this.switchDelay = value;
    }

    public void setRestoreDelay(int value) {
        this.restoreDelay = value;
    }

}
