# NeoSync Changelog

## 1.5.3

### Added

- isActive() reports whether the current server-thread call stack is inside a NeoSync sync teleport. Mods that police dimension changes can use it to recognise and permit sync travel without guessing from timing. Covers both the direct sync path and syncs completed through death and respawn.

### Fixed

- A sync whose target world does not exist now logs a warning instead of failing with no trace.

## 1.5.2

### Fixed

- A sync whose teleport is refused (by another mod cancelling the dimension change) no longer half-completes. Previously the player's current body was stored into the shell container, complete with a copy of their inventory, and the target shell was consumed before the teleport was attempted; a refused teleport then left the player where they stood with their items, a duplicate of that inventory inside the stored shell, and the target shell gone. The teleport now runs first, the target shell is only consumed after it succeeds, and a failed sync rolls back the stored shell and shows the standard failure message. Applies to both live syncs and syncs completed through death and respawn.

## 1.5.1

### Fixed

- Shell Storage, Shell Constructor, and their Zero Point variants no longer drop two items when broken. The block is two blocks tall and both halves rolled the same loot table; loot is now gated to the lower half, matching how vanilla handles doors. This also stops the stray item drop when breaking the bottom half in creative mode.

## 1.5.0

- Shell rename support, retrimming, anchor and inventory fixes.
