import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TabPeekConfigurable implements SearchableConfigurable {
    private TabPeekConfigurableForm myGUI;

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
        myGUI = new TabPeekConfigurableForm();
        return myGUI.rootPanel;
    }

    @Override
    public void disposeUIResources() {
        myGUI = null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }
}
