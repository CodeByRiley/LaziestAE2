# Laziest AE2

Source code is licensed under the MIT license in LICENSE. <br>
Textures in assets/ are licensed by E. Geng (2020) under assets/LICENSE.

A Minecraft **1.7.10** backport of [Lazy AE2](https://github.com/phantamanta44/Lazy-AE2) by phantamanta44,
which targets 1.12.2. <br> Adds machines that automate the tedious parts of Applied Energistics 2.

Requires **Applied Energistics 2** rv3, or GTNH's [AE2 Unofficial](https://github.com/GTNewHorizons/Applied-Energistics-2-Unofficial) fork.
On stock rv3 the mod fills in the fork's [compressed crafting accelerators](#crafting-co-processing-units);
on the fork it defers to the ones already there.
### Supported (Optional) mods
- NEI
- MineTweaker
- WDMla/Waila


## Machines

| Machine | What it does |
| --- | --- |
| **Fluix Aggregator** | Combines three ingredients into one output; automates in-world fluix crafting. |
| **Pulse Centrifuge** | Purifies crystals without water or waiting. Also grinds sky stone, ender pearls and wheat. |
| **ME Circuit Etcher** | Prints processors without inscriber presses. Takes redstone, silicon and a core material. |
| **Crystal Energizer** | Charges certus quartz faster than a charger. Energy cost comes from the recipe. |
| **Preemptive Assembly Unit** | Holds patterns and pushes their ingredients into an adjacent crafter, a whole batch at a time. |
| **ME Level Maintainer** | Keeps items stocked in the network, with a target quantity and batch size per line. |
| **Mass Assembly Chamber** | Multiblock crafting provider that accepts jobs in bulk and works through them using network power. |

The four processing machines accept AE2 **acceleration cards** (up to 8), trading energy for speed,
and have per-face IO configuration plus an auto-export toggle.

Every machine except the chamber has **redstone control**: always active, active with a signal,
active without one, or never. A disabled machine keeps its progress rather than restarting, and
the two crafting providers report themselves busy so the network sends jobs elsewhere.

These controls live in the configuration tab hanging off the side of each GUI. Hover the gear to
peek at it, click to pin it open, and drag it to move the tab to another edge.

### Mass Assembly Chamber

Any face-connected group of assembler blocks forms a structure, up to 256 blocks. It needs
exactly one Controller plus at least one Frame, Pattern Provider, Crafting Coprocessor and IO Port.
Vents are decorative.

Every block carries a grid node, so an ME cable may attach anywhere on the structure; the whole
cluster counts as a single channel. Throughput scales with the number of coprocessors, at
proportionally worse energy efficiency.

Crafting Coprocessors come in five tiers — 1x, 4x, 16x, 64x and 256x — following AE2's own
storage ladder. Each tier fuses four of the one below with a speculation core, and counts as
that many coprocessors while taking a single block and a single grid node. The chamber can only
put so many to work before the queue runs dry; past that point the CPU bar in its GUI says how
many are actually being used.

It is a crafting *provider*, not a crafting CPU — the network still needs a real Crafting CPU to
plan jobs.

### Crafting Co-Processing Units

Compressed crafting accelerators for real AE2 crafting CPUs — 4x, 16x, 64x, 256x, 1024x and
4096x. One block does the work of that many accelerators, sparing the space and the grid nodes.
Each is four of the tier below on a crafting grid, shapeless.

**These only exist on stock AE2 rv3.** GTNH's fork already ships the same ladder, so the mod
detects it and stays out of the way rather than adding a second, parallel set. The recipes match
the fork's, so a pack can move between the two without relearning them.

### Preemptive Assembly Unit

Replaces an ME Interface in front of a machine that crafts from an item inventory:

```
Crafting CPU  ->  PAU (stages ingredients)  ->  adjacent crafter  ->  results piped back
                                                                  ->  PAU import buffer  ->  ME network
```

Put a crafter, such as a Molecular Assembler, against a face set to OUTPUT or OMNI. If ingredients
cannot be delivered anywhere, they are returned to the network so the job can be reissued.

## Integration

**NEI** — recipe and usage screens for the four processing machines. Hover a machine and press
R or U to see its recipes. Clicking the progress arrow in a machine GUI opens the same screens.

**WDMla / Waila** — in-world tooltips. Looking at a machine shows its progress, buffered AE and
installed acceleration cards; the assembly unit shows its pattern count and whether ingredients
are staged; the level maintainer shows how many rows it stocks; the Mass Assembly Chamber shows
structure status, queue depth and the job it is working on.

Asking for details adds the IO mode of the face being pointed at, the auto-export state, the
level maintainer's per-row stock, and the chamber's part breakdown — which is also what makes a
chamber part look up its controller, so the structure scan only runs on demand.

[WDMla](https://github.com/GTNewHorizons/WDMla), the GTNewHorizons successor to Waila, is the
preferred front end: it draws real progress bars and lets each provider be reordered or switched
off individually. Plain [Waila](https://github.com/GTNewHorizons/waila) is supported as a
fallback, with progress shown as a percentage and sneaking standing in for the details key.

WDMla answers to the `Waila` mod id as well, and is told to ignore the legacy registration so
nothing appears twice. If WDMla's `overrideWailaTooltips` option is turned off, both paths
register and lines are duplicated — that is the option doing exactly what it says.

**MineTweaker** — recipes can be added or removed from scripts:

```zenscript
mods.laziestae2.Aggregator.addRecipe(<threng:material>, <ore:dustCoal>, <ore:dustFluix>, <ore:ingotIron>);
mods.laziestae2.Aggregator.removeRecipe(<appliedenergistics2:material:7>);

mods.laziestae2.Centrifuge.addRecipe(<output>, <input>);
mods.laziestae2.Etcher.addRecipe(<output>, <middle>);                  // default redstone + silicon
mods.laziestae2.Etcher.addRecipe(<output>, <top>, <bottom>, <middle>); // full positional form
mods.laziestae2.Energizer.addRecipe(<output>, <input>, 12000);
```

Every machine also has a `removeRecipe(IIngredient)` method.

## Security

Machines connected to a network with an **ME Security Terminal** obey it: opening a GUI, or
changing face IO, auto-export or redstone mode, requires `BUILD` rights. A network without a
security terminal is unrestricted, as is a machine that is not attached to one.

## Configuration

`config/laziestae2.cfg` covers energy buffers, work rates, upgrade scaling, the job queue size, and:

- `general.networkTransferPerTick` — maximum AE a machine draws from the network per tick. **0 means no limit.**
- `machines.fast_crafter.preemptiveBatching` — whether the assembly unit pulls a whole batch of
  ingredients ahead of time. This reaches into AE2 internals; disable it if a future AE2 build changes them.

## Building

```bash
./gradlew build
```

Output lands in `build/libs`. Close any running client first — it locks the jar.

Uses [anatawa12's ForgeGradle 1.2 fork](https://github.com/anatawa12/ForgeGradle-1.2), so Gradle 4.4.1+
works. Dependency versions live in `gradle.properties`.

## Credits

Original mod, textures and design by **phantamanta44**. This is an independent backport; please
direct issues with the 1.12.2 version upstream.
