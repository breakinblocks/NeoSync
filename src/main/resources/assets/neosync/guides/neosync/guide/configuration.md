---
navigation:
  title: Configuration
  icon: minecraft:writable_book
  parent: index.md
  position: 8
---

# <Color id="light_purple">Configuration</Color>

The config file is `config/neosync-common.toml`. The options players ask about most:

| Key | Default | Effect |
| --- | --- | --- |
| `enableInstantShellConstruction` | `false` | Shells finish instantly for creative players |
| `warnPlayerInsteadOfKilling` | `false` | Refuse the sample instead of killing a low health player |
| `fingerstickDamage` | `20` | Health taken per sample |
| `hardcoreFingerstickDamage` | `40` | Health taken per sample in hardcore |
| `shellConstructorCapacity` | `256000` | FE needed for a full shell |
| `shellConstructionSpeed` | `2.5` | Build speed multiplier |
| `shellStorageCapacity` | `320` | FE buffer of a storage |
| `shellStorageConsumption` | `16` | FE drained per tick to keep a shell alive |
| `acceptsRedstone` | `true` | Storages accept a redstone signal as power |
| `maxUnpoweredLifespan` | `20` | Ticks a storage keeps its shell alive without power |
| `energyMap` | see below | FE per tick for each entity on a treadmill |
| `syncPriority` | `NATURAL` | Order shells are picked on death |
| `autoSyncOnDeath` | `true` | Sync into another shell when a shell dies |
| `wrench` | `minecraft:stick` | Item that toggles a container's comparator output |

The default `energyMap` is chicken 2, pig 16, player 20, wolf 22, villager 25, creeper 80 and enderman 160. Entries take the form `entity_id:energy_per_tick`, so `minecraft:horse:30` adds horses at 30 FE per tick.

`syncPriority` accepts `NATURAL`, `NEAREST` or any dye color name in capitals, such as `RED` or `LIGHT_BLUE`. See [Syncing](syncing.md) for how the order is used.
