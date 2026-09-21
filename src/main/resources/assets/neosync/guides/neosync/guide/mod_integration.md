---
navigation:
  title: Mod Integration
  icon: minecraft:comparator
  parent: index.md
  position: 9
---

# <Color id="light_purple">Mod Integration</Color>

All of these are optional. NeoSync runs the same without them.

***
## <Color id="gold">JEI</Color>

Every NeoSync block has an information page, and the Treadmill Energy Sources category lists each entity the treadmill accepts with its live FE per tick.

***
## <Color id="gold">Jade</Color>

Look at a constructor, storage or treadmill for the owner, build progress, trim color, powered state and energy level.

***
## <Color id="gold">Curios</Color>

Curio slots belong to the body. Each shell keeps its own curios, and they stay behind with the body when you sync out.

***
## <Color id="gold">Mekanism</Color>

Radiation belongs to the body too. A shell keeps the dose it absorbed while you were in it, a freshly built shell starts clean, and syncing does not carry radiation with you.

***
## <Color id="gold">Sable and Create Aeronautics</Color>

Shell storages, constructors and treadmills all work on a moving or rotated airship. Walking into a storage on deck opens the selector, syncing into a shell parked on another airship works, and animals on a treadmill run in place relative to the ship. Each shell remembers which airship it is on.

***
## <Color id="gold">Grave Mods</Color>

Dying in your original body is an ordinary death, so grave mods behave as they always do.

Dying in a shell while another shell is available is different: the death and drops events fire, so a grave is placed at the dead shell with its full inventory, but there is no respawn, so the respawn event never fires. For Simple Tombs that means the soulbound `KEEPPARTS` option does not carry across an automatic sync. Set `KEEPPARTS=NONE` if you want one consistent behaviour, and rely on the grave.
