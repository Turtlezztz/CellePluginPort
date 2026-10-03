# Celler for Paper 26.2

Modernized from the archived CellePlugin by AbdisKiosk. This version uses English commands, messages, menus, signs, configuration comments and time/number formatting. It retains cell groups, rentals, members, logs, configurable inventories, the PlaceholderAPI expansion and Staxi importer.

## Requirements

- Paper **26.2** on **Java 25**. The build and server checks use Paper `26.2.build.129-stable`.
- WorldEdit **7.4.5**, WorldGuard **7.0.19**, and Vault **1.7.3**.
- A Paper-compatible economy plugin that registers an Economy service with Vault.
- Optional: the full **PlaceholderAPI 2.12.3** server plugin from its [official release](https://github.com/PlaceholderAPI/PlaceholderAPI/releases/tag/2.12.3). Its Maven API artifact is intended for compilation and is not a deployable plugin.

Paper's Java and setup requirements are documented in the [Paper installation guide](https://docs.papermc.io/paper/getting-started/). This artifact targets Paper 26.2; it does not target older Bukkit/Spigot versions or Folia.

## Build and install

Set `JAVA_HOME` to a JDK 25 installation, then run:

```sh
./gradlew --no-daemon build
```

Install `build/libs/celle-fr-26.2-port-2.0.0-combined.jar` in the server's `plugins` folder, alongside the dependencies above. Install only one Celler jar. The jar bundles SQLite, configuration, database and inventory libraries; Paper, WorldGuard, WorldEdit, Vault and PlaceholderAPI remain external dependencies.

Start the server normally. Configuration is generated in `plugins/Celler`: `config.yml`, `defaults.yml`, `lang/en.yml`, `guis/`, and `cell.db`. Administrators use `/celladmin` or `/cea`; players use `/cell` or `/ce`. Both labels and command patterns can be customized in `lang/en.yml`; restart after changing command patterns. `/cea reload` reloads configuration, messages, inventories and the sign/expiration timer. Reopen inventories to see their updated configuration. Use a full server restart to replace the jar.

## Upgrade an existing installation

1. Stop the server and back up the complete `plugins/Celler` folder, WorldGuard region files and worlds. If SQLite has `cell.db-wal` or `cell.db-shm` companions, preserve them with the database. Upgrade a copy first.
2. Upgrade the server and dependency plugins to the versions above using their own world/region migration instructions. This plugin port does not convert a Minecraft 1.8 world or a WorldGuard 6 region file.
3. Replace the old Celler jar. Keep `cell.db` and the existing YAML files in `plugins/Celler`. The cell, user, group, member, sign, teleport and log schema is preserved; leases and UUID memberships are retained.
4. Check the startup log and the multiplayer checklist below before moving the installation to your live server.

Legacy inventory material names and data colors such as `STAINED_GLASS_PANE` with `durability: 15` are translated to modern materials. Unsupported material names now fail with a configuration error instead of silently producing the wrong item. Inventory rows must be 1–6 and slots must fit the inventory.

Customized command aliases in `lang/en.yml` are preserved. Transfer any aliases you want to keep from the old locale file. New installations use `/cea group set price <group> <price>`; an old `commandCeaGroupSetRentPriceAlias` value of `group set permission * *` will retain that spelling until you edit it. Durations accept positive combinations of `d`, `h`/`t`, `m` and `s`, for example `1d2h30m`. The legacy `t` hour suffix remains supported.

The available-cell inventory supports paging using item keys `nextpage` and `prevpage` in `guis/cellsinregion.yml`. If an existing customized `items` map omits these keys, add two arrow items with unused slots using the same structure as the other items, or regenerate that one file after saving a copy. Fresh installations generate the buttons automatically.

The preserved database schema requires region names to be unique across worlds and stores teleport positions as integer block coordinates. The port rejects conflicting region assignments rather than overwriting an existing cell. Staxi import remains available through the migration command and the `cell.admin.migrate` permission.

## Switch an existing installation to English

The default locale is now `en`, and a new `lang/en.yml` is generated with English messages and command patterns. The player command is `/cell` (short alias `/ce`), and ending a rental uses `/ce unrent [cell]`. Staxi import uses `/cell-migrate <plugin-folder>`; `/celle-migrate` remains an alias. Fresh cell groups require the `prisoner` permission by default; existing groups keep their configured rental permission, including `fange`.

Existing YAML values and database records remain unchanged. To adopt all English defaults on an existing server:

1. Stop the server and back up `plugins/Celler`.
2. Move `lang/dk.yml` out of the `lang` directory so the old locale cannot serve Danish messages. Transfer any desired customizations to `lang/en.yml` after it is generated.
3. Move `config.yml`, `defaults.yml` and `guis/` into your backup so the plugin regenerates English versions on startup. Reapply your limits, rental prices, permissions, timings and inventory customizations afterward. Alternatively, translate those files in place.
4. Start the server with the new jar. Keep `cell.db` in place to preserve cells, rentals and memberships.

Groups with explicit sign text stored in the database keep that text. Translate those lines with `/cea group set sign <group> <state> <line> <text>` for each applicable state (`unrented`, `rented-non-member`, `rented-member`, `rented-owner`). Groups using the defaults display the regenerated English sign text automatically.

## Behavior changes

- Commands, payments, cell events, world access and inventory changes run on the server thread. Rental/membership changes use fresh transactional database records, and rejected rentals/extensions refund the payment.
- Expired leases are cleared even when their expiration occurred while the server was stopped. Ownership and membership changes update WorldGuard immediately.
- WorldGuard 7 queries protect building, containers and interactions. The existing `cell.build` and `cell.interact` permission groups remain supported.
- Iron doors use modern block data and update both halves. Standing, wall and hanging signs are supported, and periodic personalized sign updates cover both sides without changing the actual sign block.
- Admin teleport and group-price setters work; group limits, log filters, generated database IDs, stale cache entries, time formatting and inventory pagination are corrected.

## Validation

See [VALIDATION.md](VALIDATION.md) for the automated and real-server results. The checked build passes 21 tests and boots on Paper 26.2 with the real dependency plugins. A native server harness checks rentals, extension, members, WorldGuard queries, inventories, payment cancellation, placeholders and expiration after a restart.

Complete these client checks on a copy of your server, using an owner, a member and an unrelated player:

- Create a group/cell from a WorldEdit selection; attach standing, wall and hanging signs. Confirm both sides display the correct personalized text, right-click rents/extends only once and managed signs cannot be edited or broken without the intended admin access.
- Rent with your real economy provider, verify the balance, test insufficient funds and group/global limits, then extend, remove members, unrent and let a lease expire.
- Test building, chests, buttons and both halves of iron doors for all three players, including inherited/overlapping regions and your `cell.build`/`cell.interact` groups.
- Open each inventory, inspect the logs and page through more than 36 available cells. Test teleport, join and respawn behavior, and reloading customized YAML.
- Check an actual backup of your legacy database/configuration and a representative Staxi import. Automated fixtures cannot establish compatibility with every historic server customization.
