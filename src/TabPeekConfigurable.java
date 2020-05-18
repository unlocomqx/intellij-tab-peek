import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TabPeekConfigurable implements SearchableConfigurable {
    private TabPeekConfigurableForm mGUI;

    @NotNull
    @Override
    public String getId() {
        return "preferences.TabPeekConfigurable";
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Tab Peek Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mGUI = new TabPeekConfigurableForm();
        mGUI.createUI();
        return mGUI.rootPanel;
    }

    @Override
    public void disposeUIResources() {
        mGUI = null;
    }

    @Override
    public boolean isModified() {
        return mGUI.isModified();
    }

    @Override
    public void apply() {
        mGUI.apply();
    }

    @Override
    public void reset() {
        mGUI.reset();
    }
}
