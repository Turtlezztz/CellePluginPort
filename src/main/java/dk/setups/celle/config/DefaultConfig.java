package dk.setups.celle.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.platform.core.annotation.Configuration;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@Configuration(path = "defaults.yml", provider = YamlSnakeYamlConfigurer.class)
@SuppressWarnings("FieldMayBeFinal")
@Header({
        "Configure the default settings for cell groups here.",
        "These defaults apply when a cell group has no explicit value for a setting.",
        "",
        "Use the in-game commands to change settings for a specific cell group."
})
public class DefaultConfig extends OkaeriConfig {

    @Getter
    private transient static DefaultConfig instance;

    public DefaultConfig() {
        instance = this;
    }

    private double rentPrice = 1000.0;
    private String rentPermission = "prisoner";
    private long cellRentMillis = 1000 * 60 * 60 * 24;
    private long maxRentMillis = 1000 * 60 * 60 * 24 * 10;
    private int maxCellsPerPlayer = 9999;

    private List<String> unrentedSignLines = Arrays.asList(
            "&2&lAVAILABLE",
            "&a{cell.name}",
            "&a${cell.group.price.format-short}",
            "&aClick to rent"
    );

    private List<String> rentedNonMemberSignLines = Arrays.asList(
            "&4&lRENTED",
            "&c{cell.name}",
            "&c{cell.owner.name}",
            "&c{cell.time.left-short}"
    );

    private List<String> rentedMemberSignLines = Arrays.asList(
            "&5&lMEMBER",
            "&d{cell.name}",
            "&d{cell.owner.name}",
            "&d{cell.time.left-short}"
    );

    private List<String> rentedOwnerSignLines = Arrays.asList(
            "&3&lOWNED",
            "&b{cell.name}",
            "&b{cell.owner.name}",
            "&b{cell.time.left-short}"
    );
}