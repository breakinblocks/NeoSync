# NeoSync Changelog

## 1.5.1

### Fixed

- Shell Storage, Shell Constructor, and their Zero Point variants no longer drop two items when broken. The block is two blocks tall and both halves rolled the same loot table; loot is now gated to the lower half, matching how vanilla handles doors. This also stops the stray item drop when breaking the bottom half in creative mode.
- Breaking the bottom half of a shell container no longer leaves the top half floating; the two halves are linked again after the 26.1 port dropped that handling.

## 1.5.0

- Shell rename support, retrimming, anchor and inventory fixes.
