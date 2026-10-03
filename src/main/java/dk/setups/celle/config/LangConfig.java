package dk.setups.celle.config;

import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import eu.okaeri.i18n.configs.LocaleConfig;
import eu.okaeri.platform.core.annotation.Messages;
import lombok.Getter;


@Getter
@SuppressWarnings("FieldMayBeFinal")
@Messages(path = "lang", defaultLocale = "en", suffix = ".yml", provider = YamlSnakeYamlConfigurer.class)
public class LangConfig extends LocaleConfig {

    /**
     * Global Commands
     */
    private String commandSystemUsageTemplate = "&eUsage for /{label}:\n{entries}";
    private String commandSystemUsageEntry = "&r - /{usage}";
    private String commandSystemUsageEntryDescription = "   &7{description}.";
    private String commandSystemPermissionsError = "&cMissing permission: {permission}!";
    private String commandSystemCommandError = "&cError: {message}";
    private String commandSystemUnknownError = "&cUnknown error! Reference ID: {id}";
    private String commandSystemConsoleOnlyError = "&cThis command can only be used from the console.";
    private String commandSystemPlayerOnlyError = "&cThis command can only be used by players.";

    /**
     * Cell Commands
     */
    private String commandCellLabel = "cell";
    private String commandCellAlias = "ce";
    private String commandCellDescription = "Commands for managing your cells.";

    private String commandCellUnrentAlias = "unrent ?";
    private String commandCellUnrentDescription = "Ends the rental of a cell.";
    private String commandCellUnrentUsage = "ce unrent [cell]";
    private String commandCellUnrentNotRented = "&cYou have not rented the cell &4{cell.name}&c.";
    private String commandCellUnrentSuccess = "&aYou have ended the rental of the cell &2{cell.name}&a.";

    private String commandCellMemberAddAlias = "add * ?";
    private String commandCellMemberAddDescription = "Adds a player to a cell.";
    private String commandCellMemberAddUsage = "ce add <player> [cell]";
    private String commandCellMemberAddSuccess = "&aPlayer &2{target} &ahas been added to the cell &2{cell.name}&a.";
    private String commandCellMemberAddAlreadyMember = "&2{target} &ais already a member of &2{cell.name}&a.";
    private String commandCellMemberAddNotOwner = "&cYou do not own &4{cell.name}&c.";
    private String commandCellMemberAddCannotBeMember = "&cPlayer &4{target} &ccannot be a member of the cell &4{cell.name}&c.";

    private String commandCellMemberRemoveAlias = "remove * ?";
    private String commandCellMemberRemoveDescription = "Removes a player from a cell.";
    private String commandCellMemberRemoveUsage = "ce remove <player> [cell]";
    private String commandCellMemberRemoveSuccess = "&aPlayer &2{target} &ahas been removed from the cell &2{cell.name}&a.";
    private String commandCellMemberRemoveNotMember = "&cPlayer &4{target} &cis not a member of &4{cell.name}&c.";
    private String commandCellMemberRemoveNotOwner = "&cYou do not own &4{cell.name}&c.";

    private String commandCellInfoAlias = "info ?";
    private String commandCellInfoDescription = "Shows information about a cell.";
    private String commandCellInfoUsage = "ce info [cell]";
    private String commandCellInfoOtherNoPermission = "&cYou do not own &4{cell.name}&c.";
    private String commandCellInfoNotRented =
            "&aGroup: &2{cell.group.name}" +
            "\n&aRegion: &2{cell.region.name}" +
            "\n&aTime remaining: &cNot rented." +
            "\n&aExpires: &cNot rented." +
            "\n&aMembers: &cNot rented";
    private String commandCellInfoHeader = "&aInformation about the cell &2{cell.name}&a:";
    private String commandCellInfoMemberFormat = "{member}, ";
    private String commandCellInfoMemberFormatLast = "{member}";
    private String commandCellInfoNoMembers = "&cNone...";
    private String commandCellInfoMessage = "&aOwner: &2{cell.owner.name}" +
            "\n&aGroup: &2{cell.group.name}" +
            "\n&aRegion: &2{cell.region.name}" +
            "\n&aTime remaining: &2{cell.time.left-long}" +
            "\n&aExpires: &2{cell.time.format-long}" +
            "\n&aMembers: &2{members}";

    private String commandCellListSelfAlias = "list;find";
    private String commandCellListSelfUsage = "ce list";
    private String commandCellListSelfDescription = "Lists the cells you can use.";
    private String commandCellListSelfNoCells = "&cYou have no cells.";
    private String commandCellListSelfHeader = "&aYour cells: ";
    private String commandCellListSelfCell = "&2{cell.group.name} &a- &2{cell.name} &a- &2{cell.region.name} &a- &2{cell.owner.name} &a- &2{cell.time.format-long}";

    private String commandCellListOtherAlias = "list *;find *";
    private String commandCellListOtherUsage = "ce list <player>";
    private String commandCellListOtherDescription = "Lists the cells a player owns.";
    private String commandCellListOtherNoCells = "&cThis player has no cells.";
    private String commandCellListOtherHeader = "&aPlayer's cells: ";
    private String commandCellListOtherCell = "&2{cell.group.name} &a- &2{cell.name} &a- &2{cell.region.name} &a- &2{cell.owner.name} &a- &2{cell.time.format-long}";

    private String commandCellTeleportAlias = "tp *;teleport *";
    private String commandCellTeleportDescription = "Teleports you to a cell.";
    private String commandCellTeleportUsage = "ce tp <cell>";
    private String commandCellTeleportNotMember = "&cYou are not a member of &4{cell.name}&c.";
    private String commandCellTeleportNoTeleport = "&cNo teleport location is set for &4{cell.name}&c.";
    /**
     * Cell Admin Commands
     */

    private String commandCeaLabel = "celladmin";
    private String commandCeaAlias = "cea";
    private String commandCeaDescription = "Commands for administering cells.";

    private String commandCeaCreateAutoAlias = "create auto * *";
    private String commandCeaCreateAutoDescription = "Creates a cell using the sign you are looking at.";
    private String commandCeaCreateAutoUsage = "cea create auto <group> <cell-name>";
    private String commandCeaCreateAutoNotLookingAtSign = "&cYou must look at a sign to create a cell.";
    private String commandCeaCreateAutoNoSelection = "&cYou have no WorldEdit selection.";
    private String commandCeaCreateAutoRegionAlreadyExists = "&cA region named &4{region} &calready exists.";

    private String commandCeaCreateCellAlias = "create cell * * *";
    private String commandCeaCreateCellDescription = "Creates a cell using a region and a name.";
    private String commandCeaCreateCellUsage = "cea create cell <group> <region> <cell-name>";
    private String commandCeaCreateCellAlreadyExists = "&cA cell named &4{name} &calready exists.";
    private String commandCeaCreateCellCreated = "&aCell &2{cell.name} &ahas been created in region &2{cell.region.name} &aand group &2{cell.group.name}&a.";

    private String commandCeaCellSetSignAlias = "cell set sign *";
    private String commandCeaCellSetSignDescription = "Sets the sign for a cell to the sign you are looking at.";
    private String commandCeaCellSetSignUsage = "cea cell set sign <cell>";
    private String commandCeaCellSetSignNotLookingAtSign = "&cYou must look at a sign.";
    private String commandCeaCellSetSignSuccess = "&aSet the sign for &2{cell.name}&a.";

    private String commandCeaCellDeleteSignAlias = "cell delete sign";
    private String commandCeaCellDeleteSignDescription = "Removes the cell sign you are looking at.";
    private String commandCeaCellDeleteSignUsage = "cea cell delete sign";
    private String commandCeaCellDeleteSignNotLookingAtSign = "&cYou must look at a sign.";
    private String commandCeaCellDeleteSignSuccess = "&aRemoved the sign.";
    private String commandCeaCellDeleteSignFailure = "&cThere is no cell sign at this location.";

    private String commandCeaCellSetTeleportAlias = "cell set teleport *";
    private String commandCeaCellSetTeleportDescription = "Sets the teleport location of a cell to your current position.";
    private String commandCeaCellSetTeleportUsage = "cea cell set teleport <cell>";
    private String commandCeaCellSetTeleportSuccess = "Set the teleport location to {x}, {y}, {z}";

    private String commandCeaCreateGroupAlias = "create group *";
    private String commandCeaCreateGroupDescription = "Creates a cell group.";
    private String commandCeaCreateGroupUsage = "cea create group <name>";
    private String commandCeaCreateGroupAlreadyExists = "&cGroup &4{group.name} &calready exists.";
    private String commandCeaCreateGroupCreated = "&aGroup &2{group.name} &ahas been created.";

    private String commandCeaDeleteCellAlias = "delete cell *";
    private String commandCeaDeleteCellDescription = "Deletes a cell.";
    private String commandCeaDeleteCellUsage = "cea delete cell <name>";
    private String commandCeaDeleteCellSuccess = "&aCell &2{cell.name} &ahas been deleted.";

    private String commandCeaUnrentAlias = "unrent *";
    private String commandCeaUnrentDescription = "Ends the rental of a cell.";
    private String commandCeaUnrentUsage = "cea unrent <cell>";
    private String commandCeaUnrentSuccess = "&aThe rental of cell &2{cell.name} &ahas been ended.";

    private String commandCeaUnrentAllAlias = "unrent all *";
    private String commandCeaUnrentAllDescription = "Ends all rentals for a player.";
    private String commandCeaUnrentAllUsage = "cea unrent all <player>";
    private String commandCeaUnrentAllSuccess = "&aAll rentals for player &2{player} &ahave been ended.";

    private String commandCeaExtendCellAlias = "extend cell *";
    private String commandCeaExtendCellDescription = "Extends the rental of a cell.";
    private String commandCeaExtendCellUsage = "cea extend cell <cell>";
    private String commandCeaExtendCellNotRented = "&cCell &4{cell.name} &cis not rented.";
    private String commandCeaExtendCellSuccess = "&aThe rental of cell &2{cell.name} &ahas been extended.";

    private String commandCeaDeleteGroupAlias = "delete group * *";
    private String commandCeaDeleteGroupDescription = "Deletes a group and moves its cells to another group.";
    private String commandCeaDeleteGroupUsage = "cea delete group <name> <new-group>";
    private String commandCeaDeleteGroupFoundCellsInGroup = "&aFound &2{count} &acells in the group.";
    private String commandCeaDeleteGroupSuccess = "&aGroup &2{group.name} &ahas been deleted, and {count} cells have been moved to group &2{newGroup.name}&a.";

    private String commandCeaLogsPlayerAlias = "logs player *";
    private String commandCeaLogsPlayerDescription = "Shows the logs for a player.";
    private String commandCeaLogsPlayerUsage = "cea logs player <player>";

    private String commandCeaLogsCellAlias = "logs cell *";
    private String commandCeaLogsCellDescription = "Shows the logs for a cell.";
    private String commandCeaLogsCellUsage = "cea logs cell <cell>";

    private String commandCeaGroupSetSignLineAlias = "group set sign * * * *...";
    private String commandCeaGroupSetSignLineDescription = "Sets a line of the signs for a cell group.";
    private String commandCeaGroupSetSignLineUsage = "cea group set sign <group> <state> <line> <text>";
    private String commandCeaGroupSetSignLineNotCorrectLine = "&cThe line number must be between 1 and 4.";
    private String commandCeaGroupSetSignLineNotCorrectState = "&cValid states: &4unrented, rented-non-member, rented-member, rented-owner&c.";
    private String commandCeaGroupSetSignLineSuccess = "&aSign line &2{line} &afor group &2{group.name} &ahas been set to &2{text}&a.";

    private String commandCeaGroupSetRentPriceAlias = "group set price * *";
    private String commandCeaGroupSetRentPriceDescription = "Sets the rental price for cells in a group.";
    private String commandCeaGroupSetRentPriceUsage = "cea group set price <group> <price>";
    private String commandCeaGroupSetRentPriceSuccess = "&aThe rental price for cells in group &2{group.name} &ahas been set to &2{price}&a.";

    private String commandCeaGroupSetMaxRentTimeAlias = "group set maxrenttime * *";
    private String commandCeaGroupSetMaxRentTimeDescription = "Sets the maximum rental duration for cells in a group.";
    private String commandCeaGroupSetMaxRentTimeUsage = "cea group set maxrenttime <group> <duration, e.g. 2d>";
    private String commandCeaGroupSetMaxRentTimeSuccess = "&aThe maximum rental duration for cells in group &2{group.name} &ahas been set to &2{time}&a.";

    private String commandCeaGroupSetRentTimeAlias = "group set renttime * *";
    private String commandCeaGroupSetRentTimeDescription = "Sets the rental duration for cells in a group.";
    private String commandCeaGroupSetRentTimeUsage = "cea group set renttime <group> <duration, e.g. 2d>";
    private String commandCeaGroupSetRentTimeSuccess = "&aThe rental duration for cells in group &2{group.name} &ahas been set to &2{time}&a.";

    private String commandCeaGroupSetMaxRentedCellsAlias = "group set maxrentedcells * *";
    private String commandCeaGroupSetMaxRentedCellsDescription = "Sets the maximum number of cells a player can rent in a group.";
    private String commandCeaGroupSetMaxRentedCellsUsage = "cea group set maxrentedcells <group> <count>";
    private String commandCeaGroupSetMaxRentedCellsSuccess = "&aThe maximum number of cells a player can rent in group &2{group.name} &ahas been set to &2{count}&a.";

    private String commandCeaGroupInfoAlias = "group info *";
    private String commandCeaGroupInfoDescription = "Shows information about a cell group.";
    private String commandCeaGroupInfoUsage = "cea group info <group>";
    private String commandCeaGroupInfoMessage = "&aInformation about group &2{group.name}&a:\n" +
            "&aPrice: &2{group.rentPrice}\n" +
            "&aPermission: &2{group.permission}\n" +
            "&aRental duration: &2{group.renttime}\n" +
            "&aMaximum rental duration: &2{group.maxrenttime}\n" +
            "&aMaximum cells: &2{group.maxrentedcells}\n" +
            "&aSign lines:\n" +
            "&2Unrented: \n" +
            "  &a- &r{group.unrentedSignLines-1}\n" +
            "  &a- &r{group.unrentedSignLines-2}\n" +
            "  &a- &r{group.unrentedSignLines-3}\n" +
            "  &a- &r{group.unrentedSignLines-4}\n" +
            "&2Rented non-member: \n" +
            "  &a- &r{group.rentedNonMemberSignLines-1}\n" +
            "  &a- &r{group.rentedNonMemberSignLines-2}\n" +
            "  &a- &r{group.rentedNonMemberSignLines-3}\n" +
            "  &a- &r{group.rentedNonMemberSignLines-4}\n" +
            "&2Rented member: \n" +
            "  &a- &r{group.rentedMemberSignLines-1}\n" +
            "  &a- &r{group.rentedMemberSignLines-2}\n" +
            "  &a- &r{group.rentedMemberSignLines-3}\n" +
            "  &a- &r{group.rentedMemberSignLines-4}\n" +
            "&2Rented owner: \n" +
            "  &a- &r{group.rentedOwnerSignLines-1}\n" +
            "  &a- &r{group.rentedOwnerSignLines-2}\n" +
            "  &a- &r{group.rentedOwnerSignLines-3}\n" +
            "  &a- &r{group.rentedOwnerSignLines-4}";

    private String commandCeaSignGUICreateAlias = "sign create gui *";
    private String commandCeaSignGUICreateDescription = "Creates a sign that opens an inventory of available cells.";
    private String commandCeaSignGUICreateUsage = "cea sign create gui <region>";
    private String commandCeaSignGuiCreateNotLookingAtSign = "&cYou must look at a sign.";
    private String commandCeaSignGuiCreateSignAlreadyExists = "&cA managed sign already exists at this location.";
    private String commandCeaSignGuiCreateRegionNotFound = "&cNo region named &4{region} &cexists.";
    private String commandCeaSignGuiCreateSuccess = "&aThe sign has been created.";

    private String commandCeaSignGUIDeleteAlias = "sign delete gui";
    private String commandCeaSignGUIDeleteDescription = "Deletes a sign that opens an inventory of available cells.";
    private String commandCeaSignGUIDeleteUsage = "cea sign delete gui";
    private String commandCeaSignGuiDeleteNotLookingAtSign = "&cYou must look at a sign.";
    private String commandCeaSignGuiDeleteSignNotFound = "&cThere is no managed sign at this location.";
    private String commandCeaSignGuiDeleteSuccess = "&aThe sign has been deleted.";

    private String commandCeaReloadAlias = "reload";
    private String commandCeaReloadDescription = "Reloads the plugin.";
    private String commandCeaReloadUsage = "cea reload";
    private String commandCeaReloadSuccess = "&aThe plugin has been reloaded in &2{elapsed}ms&a.\n&aSome changes require a server restart.";

    private String commandCeaCommandEachAlias = "commandeach *...";
    private String commandCeaCommandEachDescription = "Runs a command for every cell.";
    private String commandCeaCommandEachUsage = "cea commandeach <command>";
    private String commandCeaCommandEachSentCommand = "&aExecuted &2{command}";

    private String commandCeaTeleportOtherAlias = "teleport * *";
    private String commandCeaTeleportOtherDescription = "Teleports a player to a cell.";
    private String commandCeaTeleportOtherUsage = "cea teleport <player> <cell>";
    private String commandCeaTeleportOtherNoTeleport = "&cNo teleport location is set for this cell.";
    private String commandCeaTeleportOtherSuccess = "&aTeleported &2{target.name} &ato &2{cell.name}&a.";

    /**
     * Cell Click Events
     */
    private String clickCellInfoHeader = "&aInformation about the cell &2{cell.name}&a:";
    private String clickCellInfoMemberFormat = "{member}, ";
    private String clickCellInfoMemberFormatLast = "{member}";
    private String clickCellInfoNoMembers = "&cNone...";
    private String clickCellInfoMessage = "&aOwner: &2{cell.owner.name}" +
                                        "\n&aGroup: &2{cell.group.name}" +
                                        "\n&aRegion: &2{cell.region.name}" +
                                        "\n&aTime remaining: &2{cell.time.left-long}" +
                                        "\n&aExpires: &2{cell.time.format-long}" +
                                        "\n&aMembers: &2{members}";

    private String cellAttemptRentAlreadyRented = "&cCell &4{cell.name} &cis already rented.";
    private String cellAttemptRentMaxRented = "&cYou have already rented the maximum number of cells in group &4{group}&c.";
    private String cellAttemptRentNoPermission = "&cYou do not have permission to rent cell &4{cell.name} &cin group &4{group}&c.";
    private String cellAttemptRentNotEnoughMoney = "&cYou do not have enough money to rent &4{cell.name}&c.";
    private String cellAttemptRentSuccess = "&aYou have rented cell &2{cell.name}&a.";

    private String cellAttemptExtendNotOwned = "&cYou do not own cell &4{cell.name}&c.";
    private String cellAttemptExtendNoPermission = "&cYou do not have permission to extend the rental of &4{cell.name}&c.";
    private String cellAttemptExtendFullyExtended = "&cThe rental of &4{cell.name} &chas reached its maximum duration.";
    private String cellAttemptExtendNotEnoughMoney = "&cYou do not have enough money to extend the rental of &4{cell.name}&c.";
    private String cellAttemptExtendSuccess = "&aYou have extended the rental of &2{cell.name}&a.";
}
