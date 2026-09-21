---
navigation:
  title: Shell Storage
  icon: neosync:shell_storage
  parent: index.md
  position: 4
item_ids:
  - neosync:shell_storage
  - neosync:manual_shell_storage
  - neosync:zero_point_shell_storage
---

# <Color id="light_purple">Shell Storage</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="shell_storage" scale="2"/>
  Keeps a body safe while your mind is elsewhere.
</Column>

***
## <Color id="gold">Recipe</Color>

<RecipeFor id="shell_storage"/>

Like the constructor, the storage is two blocks tall.

***
## <Color id="gold">Power and Doors</Color>

The storage needs power to keep its shell alive. It accepts a redstone signal (unless `shellStorageAcceptsRedstone` is off) or Forge Energy. Its buffer holds 320 FE and it drains 16 FE per tick by default.

When it has power the doors open. Walk into an open storage to bring up the shell selector, or step into an empty one to leave your current body there.

A storage that loses power keeps its shell for `shellStorageMaxUnpoweredLifespan` ticks (20 by default), then the shell inside dies.

***
## <Color id="gold">Useful Tricks</Color>

* Sneak and right-click with a dye to color the trim and the shell inside. Colors show in the shell selector and can drive `syncPriority`.
* Hoppers pointed into a storage can equip or unequip armor and tools on the stored shell.
* A comparator reads either build progress or inventory fullness. Right-click the storage with the wrench item (a stick by default, set by `wrench` in the config) to switch between the two.
* Mining a storage or constructor with any pickaxe drops the block. No silk touch needed.

***
## <Color id="gold">Manual Shell Storage</Color>

<Row>
  <ItemImage id="manual_shell_storage"/>
  ### <Color id="aqua">Manual Shell Storage</Color>
</Row>

A storage whose shell is never picked for you when you die. Use it for bodies built for one job, such as a mining body loaded with tools or a body parked at a far outpost. Walk in to sync into it by hand like any other storage.

<RecipeFor id="manual_shell_storage"/>

***
## <Color id="gold">Zero Point Shell Storage</Color>

<Row>
  <ItemImage id="zero_point_shell_storage"/>
  ### <Color id="aqua">Zero Point Shell Storage</Color>
</Row>

Upgrade a storage with a nether star. It needs no redstone or FE, the doors open on their own as you approach, and the shell inside never decays.

<RecipeFor id="zero_point_shell_storage"/>
