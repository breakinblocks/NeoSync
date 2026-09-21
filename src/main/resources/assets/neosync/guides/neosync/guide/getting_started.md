---
navigation:
  title: Getting Started
  icon: minecraft:golden_apple
  parent: index.md
  position: 1
---

# <Color id="light_purple">Getting Started</Color>

This page walks through a first shell from nothing to your first sync.

***
## <Color id="gold">Step 1: The Sync Core</Color>

Craft a <ItemLink id="sync_core"/>. It is the key ingredient in every NeoSync block.

<RecipeFor id="sync_core"/>

***
## <Color id="gold">Step 2: The Shell Constructor</Color>

Craft a <ItemLink id="shell_constructor"/> and place it. It is two blocks tall, so leave room above it.

<RecipeFor id="shell_constructor"/>

Right-click the constructor with an empty hand to give it a genetic sample.

<Color id="red">Warning:</Color> with the default config the sample costs 20 health (40 in hardcore), which is your entire health bar. Eat a golden apple or enchanted golden apple first, hold a totem of undying, or ask the server owner to turn on `warnPlayerInsteadOfKilling` in the config. If you die, the shell still starts building.

***
## <Color id="gold">Step 3: Power the Constructor</Color>

Craft a <ItemLink id="treadmill"/> and place it so that either half touches the constructor. It places like a bed: two blocks long, extending away from you, with the far block as the front.

<RecipeFor id="treadmill"/>

Lure a pig or a wolf onto the front block. The animal runs in place and the constructor fills up. A comparator on the constructor reports build progress, and Jade shows it as a percentage.

Any Forge Energy cable or generator pointed at the constructor works as well. A full shell needs 256000 FE by default.

***
## <Color id="gold">Step 4: The Shell Storage</Color>

Craft a <ItemLink id="shell_storage"/> and place it somewhere you want to be able to travel to.

<RecipeFor id="shell_storage"/>

Give it a redstone signal or feed it FE. When it has power the doors open.

***
## <Color id="gold">Step 5: Sync</Color>

Walk into the open storage. The shell selector appears with every finished shell you own, grouped by world. Click a shell and your mind moves into it. Your old body stays in the storage with everything it was carrying.

See [Syncing](syncing.md) for what happens when you die, how cross dimensional travel works and how to rename shells.
