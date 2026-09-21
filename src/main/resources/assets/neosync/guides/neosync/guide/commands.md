---
navigation:
  title: Commands
  icon: minecraft:command_block
  parent: index.md
  position: 7
---

# <Color id="light_purple">Commands</Color>

Everything sits under `/neosync` and needs permission level 2. `anchor` and `ghostshells` also work for the host in single player.

***
## <Color id="gold">/neosync select [targets]</Color>

Opens the shell selector for the targets wherever they stand, with no storage needed. Players with no finished shell are skipped with a message.

Standing in an empty storage this works like a normal sync: the body stays in the storage. Away from a storage there is nowhere to leave the body, so the player is killed and moved into the chosen shell when they respawn. A player who is already on the death screen can pick a shell too and arrives in it instead of at their spawn point.

***
## <Color id="gold">/neosync anchor</Color>

An anchor is a respawn point that shows up in the selector like a shell, with no block behind it. Syncing to it creates a fresh body with full health and an empty inventory at those coordinates.

* `anchor set <targets> <dimension> <x y z> [temporary]` gives each target an anchor. Pass `true` for a single use anchor that vanishes the moment it is used.
* `anchor ensure <targets> <dimension> <x y z>` sets a single use anchor only for players who have nothing to sync into. Safe to run on a timer or a login hook.
* `anchor list <targets>` lists anchors and marks the single use ones.
* `anchor remove <targets> [dimension x y z]` removes one anchor, or all of them without coordinates.

***
## <Color id="gold">/neosync ghostshells</Color>

`ghostshells <sync|remove|repair> <targets> [x y z]` deals with shells that show in the selector but no longer exist in the world. Anchors are never touched.

* `sync` repairs what it can and deletes the rest.
* `repair` repairs what it can and reports the rest without deleting.
* `remove` deletes ghost shells without trying a repair.

Without coordinates the sweep covers every shell the player has, and also marks each one as fully built.

***
## <Color id="gold">/neosync purge</Color>

`purge <players> [dimension]` deletes a player's shells, both the player's entry and the copy in the block. Contained bodies drop their inventory and XP at the block. Leave out the dimension to wipe every shell.
