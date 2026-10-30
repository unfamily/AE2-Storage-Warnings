# AE2 Storage Warnings → Refined Storage port brief

Product inventory of what we built for AE2, so the same behavior can be recreated for Refined Storage.
This is about **what the mod does**, not about AE2 implementation details.

---

## Goal

When the player opens a storage terminal/grid, **interrupt** with a warning if networked storage media is critically low on remaining capacity, then let them continue with **Ignore Warning**.

---

## Trigger

- Fires when opening a **main storage UI** (wired terminal, wireless, portable/grid equivalent).
- Check runs **server-side**, **before** the real UI opens.
- Only shows if the scan finds at least one warning that is still **enabled** for that player.

**Does not fire for:**
- Nested / submenu opens
- Utility UIs that are not the main storage screen (craft confirm, priority, trash, network status, etc.)
- The immediate re-open after **Ignore Warning** (one-shot skip)

---

## What is scanned

**Included**
- Storage media inside **drives / chests** (or RS equivalents) on the **same network** as the UI being opened
- For portable / item-hosted UIs: the portable storage item itself

**Excluded**
- External / bus-like storage (AE2: storage buses → RS: external storage / similar)
- Empty slots and non-storage machines

---

## Capacity rules

Metrics (aggregated **per resource channel**, network-wide totals):

| Metric | When | Meaning |
|--------|------|---------|
| Remaining **bytes/space** % | Always when capacity exists | `floor(100 × free / total)` |
| Remaining **types** % | Only for typed media | `floor(100 × freeTypes / totalTypes)` |

Rules:
- Default threshold: **10%** remaining (configurable)
- Warn if remaining % **≤ threshold**
- For a given channel, **types beat bytes**: if types are low, show a types warning and **do not** also show bytes for that channel
- Typeless / bytes-only media → bytes warnings only
- Sum across all scanned disks of that channel (not “worst single disk”)
- If the media reports **full**, treat it as full even if raw counters disagree

---

## Warning dialog UX

| Element | Content |
|---------|---------|
| Title | `WARNING!` (red, centered) |
| Bytes line | `- {Channel}: available space is {N}%` |
| Types line | `- {Channel}: available types are low.` (no percentage) |
| Action | Single button: **Ignore Warning** |

Behavior:
- **Ignore Warning** → close dialog and open the deferred terminal/grid **once** (no permanent mute)
- Close / Esc without Ignore → dialog closes, **storage UI does not open**

No inventory slots; dedicated modal.

---

## Per-channel toggles

- From an open storage UI: left toolbar button (warning triangle)
- Sub-screen: checkbox per resource channel (**checked = warnings on**, default)
- Saved **per player** (persistent)
- Toggle goes to server; server syncs back (login + after each change)
- Optional in-game guide link if a guide mod is available

This is separate from Ignore: toggles permanently mute a channel; Ignore only bypasses the current gate.

---

## Config (COMMON) — for implementers, not marketing copy

| Key | Default | Purpose |
|-----|---------|---------|
| `thresholdPercent` | `10` (0–100) | Remaining % at or below which warnings fire |
| `logging.enabled` | `false` | Debug logs for scans / intercepts |

No global on/off beyond that.

---

## Multiplayer

- Scan + intercept: **server-authoritative**
- Preferences: per player, server-stored
- Sync disabled-channel list on login and after toggles
- Each player has their own mute set

---

## Edge cases to preserve

1. **Skip once** after Ignore so the real UI open is not re-intercepted
2. **Portable** storage counted even when no network is resolved; if both exist, both contribute
3. No network from host → only portable path may still warn
4. Utility / submenu opens must not spam warnings
5. Treat “any main storage UI” as a trigger, not a hard-coded allowlist of screens
6. Network totals can dilute one nearly-empty disk with free space elsewhere of the same channel
7. Types message has no `%`; bytes message includes it

---

## RS mapping (product level)

| What we did on AE2 | Preserve on Refined |
|--------------------|---------------------|
| Open ME terminal | Open RS grid / wireless / portable grid |
| Cells in drives / ME chests | Disks in disk drives (and similar), **not** external storage |
| Key types (item, fluid, chemicals, …) | RS storage channels (item, fluid, …) |
| Bytes + types | Disk capacity + type slots where RS has them |
| Ignore once | Same UX |
| Toolbar per-type toggles + persistence + sync | Same |
| `thresholdPercent` + logging | Same config surface |

---

## Suggested RS mod naming

- ID: `rs_storage_warnings` (or `refined_storage_warnings`)
- Display: **RS Storage Warnings** / **Refined Storage Warnings**

---

## Out of scope (what we deliberately did *not* do)

- Chat / toast spam while playing (only gate on open)
- Scanning external / bus storage
- Permanent “don’t show again” from the warning dialog (only per-channel settings + one-shot Ignore)
- Per-disk worst-case warnings (we use network aggregates)
- Publishing copy that documents config/license/authors (marketing text is feature-only)
