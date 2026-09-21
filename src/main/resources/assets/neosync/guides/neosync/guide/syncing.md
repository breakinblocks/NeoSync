---
navigation:
  title: Syncing
  icon: minecraft:ender_eye
  parent: index.md
  position: 6
---

# <Color id="light_purple">Syncing</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="minecraft:ender_eye" scale="2"/>
  Moving your mind between bodies.
</Column>

***
## <Color id="gold">The Shell Selector</Color>

Walk into an open <ItemLink id="shell_storage"/> and the shell selector opens. It is a radial menu with one entry per shell you own.

* Shells are grouped by world. Use the up and down arrows to switch worlds, and left and right to page through a crowded world.
* Each entry shows the shell's coordinates and, for a shell still under construction, its progress.
* Right-click a shell to rename it. Enter saves, Esc cancels.
* Shells in a <ItemLink id="manual_shell_storage"/> are marked Manual.

Click a shell to sync. Your current body stays where it is with everything it carries, and you wake up in the shell you picked. Every shell keeps its own inventory, experience level and game mode.

Syncing works across dimensions, including custom ones. The Astral Projection advancement is yours the first time you do it.

***
## <Color id="gold">Dying in a Shell</Color>

Dying in a shell does not count towards your death counter. What happens next depends on where you are:

* <Color id="aqua">In a shell, with another finished shell available</Color>: you sync straight into the next body. The dead shell drops its inventory where it died, so grave mods pick it up.
* <Color id="aqua">In a shell, with nowhere to go</Color>: a normal death. You respawn as usual.
* <Color id="aqua">In your original body</Color>: a normal death. Shells are never picked for you when your first body dies.

Shells in a <ItemLink id="manual_shell_storage"/> are never chosen automatically.

A server can turn automatic syncing off entirely with `autoSyncOnDeath`. You then see the normal death screen and respawn as usual, with all your shells still available to walk into.

***
## <Color id="gold">Which Shell Gets Picked</Color>

The `syncPriority` config decides the order when more than one shell qualifies. Its default, `NATURAL`, prefers your original body. `NEAREST` prefers the closest shell, and any dye color name prefers shells stored in a storage of that color. Several values can be listed, and the first one that matches a shell wins.

***
## <Color id="gold">Ghost Shells</Color>

If a storage is destroyed or a chunk is rolled back while a shell is inside, the shell can linger in your selector with no body behind it. An operator can clean these up with `/neosync ghostshells`. See [Commands](commands.md).
