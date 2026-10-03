package dk.setups.celle.gui.config;

import dk.setups.celle.gui.config.item.ConfigGUIItem;
import eu.okaeri.configs.OkaeriConfig;

import java.util.LinkedHashMap;

public abstract class GUIConfiguration extends OkaeriConfig {

    public abstract String getTitle();

    public abstract int getRows();

    public abstract LinkedHashMap<String, ConfigGUIItem> getItems();

}