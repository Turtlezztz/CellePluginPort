# Paper 26.2 rewrite plan

Target: Paper 26.2, Java 25, WorldGuard 7.0.19 and WorldEdit 7.4.5.

Status: implementation complete. Build, 21 automated tests and isolated native Paper server checks pass. Deployment/client acceptance checks remain documented in the README.

1. Replace the Spigot 1.8.8 / Java 8 build with a pinned Paper API and Java 25 toolchain. Produce one shaded deployable jar; use Paper's Adventure API and bundle SQLite. Update CI and plugin metadata.
2. Rewrite WorldGuard and WorldEdit integration using their current region container, adapters, session and query APIs. Retain region names, UUID membership and permission groups. Handle missing worlds and managers explicitly.
3. Replace legacy material IDs, durability colors, MaterialData doors, reflective skull profiles and sign packets. Support standing, wall and hanging signs, both sign sides and both door halves. Read old GUI item names/data through a migration adapter.
4. Run command mutations, economy calls, GUI operations, world access and custom events on the server thread. Rewrite rental and expiration handling without unmanaged executor pools. Expire leases that elapsed while the server was offline.
5. Preserve the SQLite schema, configuration/message keys (with English defaults), commands, permissions, GUIs, logs, placeholders and Staxi migration. Use the plugin data folder and close resources on shutdown. Fix cache identity and persistence failure handling.
6. Test rental limits, membership, cache behavior, SQLite round trips, serialization and packaging. Compile against the exact target API. Document deployment and manual multiplayer checks.

## Validation boundary

Compilation and automated tests do not establish multiplayer behavior. [VALIDATION.md](VALIDATION.md) records the checks actually run, and the README lists the remaining client and production-data acceptance checks.
