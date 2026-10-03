# Paper 26.2 validation

## Build and automated tests

Validated with Temurin Java 25, Gradle 9.1.0 and the pinned Paper 26.2 build 129 API. `./gradlew --no-daemon build` creates the shaded `combined` jar and runs 21 JUnit tests:

| Area | Tests | Coverage |
| --- | ---: | --- |
| SQLite stores | 9 | Generated IDs, cache publication, lease conflicts, restart persistence, member cleanup, rollback, stale cache refresh, log filtering, legacy unrented records |
| Item serialization | 5 | Legacy materials/colors, metadata/enchantments/damage, unknown materials, runtime ItemStack subclasses, public skull textures |
| Cell leases | 3 | Expiration, extension limits, sign-line indexing |
| Duration parsing | 2 | Positive compound durations, invalid/overflow input |
| Time formatting | 2 | Zero/negative durations and date substitutions |

The test report is generated at `build/reports/tests/test/index.html`.

## Native Paper server checks

A disposable local server used the actual Paper 26.2 build 129, WorldEdit 7.4.5, WorldGuard 7.0.19, Vault 1.7.3 and full PlaceholderAPI 2.12.3 plugin jars. It was bound to localhost, used a temporary flat world and SQLite database, and shut down automatically after the checks. No production server or database was modified.

The native harness confirmed:

- Celler enables and shuts down with and without optional PlaceholderAPI.
- Rent, extend, add/remove member and unrent events persist their database changes and update WorldGuard ownership immediately.
- Real WorldGuard queries allow the owner to build and reject an outsider's building, container access and button/door use.
- Rental/member logs persist. Console configuration reload and the duration setter execute successfully.
- Admin, log and available-cell inventories construct using Paper's native inventory and item APIs.
- A cancelled rental withdraws once, refunds once and leaves the cell available.
- `%celler_available_smoke%` resolves through the registered PlaceholderAPI expansion.
- A persisted expired lease is cleared after a server restart, and persisted WorldGuard ownership is revoked.

The harness and server logs are retained locally in `build/reports/paper-smoke/` (ignored build artifacts). The economy provider and player objects were test doubles; dependency plugins, server, SQLite, WorldGuard queries and inventories were real.

## Remaining manual checks

No Minecraft clients were connected. Packet rendering, actual inventory clicks, sign/door interactions, permissions from your permission plugin and balances from your economy plugin require the multiplayer checks in [README.md](README.md). A real legacy production backup and an external Staxi installation were not available; test those on a server copy before deployment.
