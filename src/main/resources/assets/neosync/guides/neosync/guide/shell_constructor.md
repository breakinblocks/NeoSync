---
navigation:
  title: Shell Constructor
  icon: neosync:shell_constructor
  parent: index.md
  position: 3
item_ids:
  - neosync:shell_constructor
  - neosync:zero_point_shell_constructor
---

# <Color id="light_purple">Shell Constructor</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="shell_constructor" scale="2"/>
  Grows a new body from a sample of your own.
</Column>

***
## <Color id="gold">Recipe</Color>

<RecipeFor id="shell_constructor"/>

The constructor is two blocks tall. Place it with a free block above it.

***
## <Color id="gold">Taking a Sample</Color>

Right-click the constructor with an empty hand. It takes a genetic sample and starts building a shell that belongs to you.

<Color id="red">The sample costs 20 health, or 40 in hardcore.</Color> That is a full health bar, so with no preparation it kills you. Ways around it:

* Eat a golden apple or an enchanted golden apple for absorption first.
* Hold a totem of undying in either hand.
* Have the server enable `warnPlayerInsteadOfKilling`, which refuses the sample when you do not have enough health instead of killing you.

If you do die the shell still starts building. Only one shell can grow in a constructor at a time, and it stays there until you sync into it.

***
## <Color id="gold">Powering It</Color>

A shell needs 256000 FE by default (`shellConstructorCapacity`), and `shellConstructionSpeed` scales how fast that fills. Any Forge Energy source works: a <ItemLink id="treadmill"/> touching the constructor, an FE cable, or a generator from another mod.

A comparator against the constructor outputs the build progress. Jade shows the owner and a percentage.

In creative mode with `enableInstantShellConstruction` turned on the shell finishes instantly.

***
## <Color id="gold">Colors</Color>

Sneak and right-click the constructor with a dye to recolor its trim. Shells built inside take that color in the shell selector, which is handy for telling bodies apart.

***
## <Color id="gold">Zero Point Shell Constructor</Color>

<Row>
  <ItemImage id="zero_point_shell_constructor"/>
  ### <Color id="aqua">Zero Point Shell Constructor</Color>
</Row>

Upgrade a constructor with a nether star. It needs no power and finishes shells instantly. The sample still costs health.

<RecipeFor id="zero_point_shell_constructor"/>
