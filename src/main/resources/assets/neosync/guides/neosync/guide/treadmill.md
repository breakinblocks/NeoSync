---
navigation:
  title: Treadmill
  icon: neosync:treadmill
  parent: index.md
  position: 5
item_ids:
  - neosync:treadmill
---

# <Color id="light_purple">Treadmill</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="treadmill" scale="2"/>
  Livestock powered industry.
</Column>

***
## <Color id="gold">Recipe</Color>

<RecipeFor id="treadmill"/>

***
## <Color id="gold">Placing It</Color>

The treadmill is two blocks long and places like a bed: it extends away from you, and the far block is the front. Put either half against a <ItemLink id="shell_constructor"/> or <ItemLink id="shell_storage"/>, or against any block that accepts Forge Energy.

***
## <Color id="gold">Runners</Color>

Lure an animal onto the front block. It locks onto the treadmill, runs in place and generates energy every tick. The energy goes into every FE receiver touching either half of the treadmill.

An animal stops counting as a runner while it is a baby, leashed, told to sit, taking damage, or standing in water. Players can run too, but a crouching player does not.

***
## <Color id="gold">Energy per Entity</Color>

Default values from `energyMap`. Server owners can add any entity to the list.

| Entity | FE/tick |
| --- | --- |
| Chicken | 2 |
| Pig | 16 |
| Player | 20 |
| Wolf | 22 |
| Villager | 25 |
| Creeper | 80 |
| Enderman | 160 |

With JEI installed, the Treadmill Energy Sources category lists the live values for the current server.
