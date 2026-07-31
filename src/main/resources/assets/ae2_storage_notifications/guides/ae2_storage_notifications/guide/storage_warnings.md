---
navigation:
  title: Storage Warnings
  icon: ae2:item_storage_cell_1k
  position: 0
---

# Storage Warnings

When you open an ME terminal, this mod checks the storage **cells** on that network.

If remaining capacity for a resource type (items, fluids, chemicals, …) is at or below the configured threshold, a **WARNING!** dialog appears before the terminal opens.

## What is checked

- Only cells in drives / ME chests (not external storage buses)
- Remaining **bytes** and remaining **types** (typed cells only)
- Types take priority over byte space in the message
- Typeless disks (e.g. AE2 Things) only use byte capacity

## Disable warnings per type

1. Open a terminal
2. On the left toolbar, click the warning triangle button (just under **Configure visible types**)
3. Uncheck a type to stop warnings for it

Your choices are saved per player.

Use the **Open Guide** button on that screen to return here.
