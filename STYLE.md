# Code Style Guide

Java 8 source targeting Minecraft 1.7.10 / Forge. The rules below describe what
the codebase already does; if a change disagrees with the code, the code wins and
this file should be updated.

## Formatting

- **Indent**: 4 spaces, never tabs.
- **Line length**: no strict limit. Break when readability suffers, and indent the
  continuation to line up with what it continues.
- **Braces**: K&R. Opening brace on the same line, closing brace on its own line.
  Single-statement bodies drop the braces — see [Control flow](#control-flow).

```java
if (!isRedstoneActive()) {
    setWorking(false);
    return;
}

double extracted = energyGrid.extractAEPower(
        needed,
        Actionable.MODULATE,
        PowerMultiplier.CONFIG);
```

- **Casts**: no space after the closing parenthesis — `(TileMachine)tile`, `(float)work`.
- **Generics**: write the type arguments out; the diamond operator is not used.
  `new ArrayList<String>()`, not `new ArrayList<>()`.
- **Imports**: no wildcards. Occasionally a rarely-used type is written fully
  qualified inline rather than imported (`java.awt.Rectangle`); either is fine, but
  don't do both for the same type in one file.

## Naming

| Kind | Convention | Example |
| --- | --- | --- |
| Classes, enums, interfaces | PascalCase | `TileMachine`, `RedstoneMode` |
| Interfaces for capabilities | `I` prefix | `ISideConfigurable`, `IRedstoneConfigurable` |
| Methods | camelCase, verb-led | `isRedstoneActive()`, `writeSyncNBT()` |
| Fields and locals | camelCase | `autoExportTimer`, `gridNode` |
| Constants (`static final`) | UPPER_SNAKE_CASE | `MAX_UPGRADES`, `AUTO_EXPORT_SIZE` |
| Enum constants | UPPER_SNAKE_CASE | `OMNI`, `NEVER` |
| Packages | lowercase, no underscores | `tile.massassembler` |

Prefixes worth keeping consistent: `Tile*` for tile entities, `Block*` for blocks,
`Gui*`/`Container*` for the two halves of a screen, `Message*` for packets,
`Mod*` for registration holders.

Fields are `private final` wherever the value never changes after construction —
that is the default, not the exception.

## Functions

A method with no body is written on one line:

```java
public static void writeSomething() { }
```

That includes overrides that deliberately do nothing and hooks left for subclasses
to fill in — the one-line form makes it obvious the emptiness is intentional
rather than an unfinished edit.

## Comments

Javadoc `/** ... */` on types and on any method whose contract isn't obvious from
the signature. Line comments explain **why**, not what:

```java
// Grid nodes must only exist server-side, and only once the tile has a world
// and position, so creation is deferred to the first server tick.
private void ensureGridNode() {
    ...
}
```

Don't comment self-evident code. Do comment anything that would look like a bug
to someone who doesn't know the constraint — side-safety, AE2 quirks, packet
ordering, texture layouts.

Comments inside a method body stay as short as they can be, and always use `//`,
never `/** ... */` — including when they run to a second line. Block comments
belong above a declaration, never inside one.

## Control flow

Guard clauses over nesting. Validate and return early so the body runs unindented:

```java
public boolean hasPermission(EntityPlayer player, SecurityPermissions permission) {
    if (worldObj == null || worldObj.isRemote || player == null)
        return true;

    IGridNode node = getActionableNode();
    IGrid grid = node == null ? null : node.getGrid();
    if (grid == null)
        return true;

    ...
}
```

An `if` whose body is a single statement must not use braces. One that is likely
to grow, or that already does more than one thing, keeps them:

```java
public boolean hasPermission(EntityPlayer player, SecurityPermissions permission) {
    if (worldObj == null || worldObj.isRemote || player == null)
        return true;
    // ^ one statement, no braces

    IGridNode node = getActionableNode();
    IGrid grid = node == null ? null : node.getGrid();
    if (grid == null) {
        Logger.error("Grid is null");
        // we might do something to handle this
        return true;
    }
    // ^ more than one statement, so braces

    ...
}
```

Two cases where the braces stay regardless:

- **The body wraps onto a second line.** A braceless `if` whose statement spans
  lines reads as though the continuation is a separate statement, which is how
  the next person edits a bug into it.
- **Either half of an `if`/`else` needs braces.** Brace both or neither, so the
  two halves look alike.

## Sides

The server is the authority. Anything a client could lie about is re-checked there.

- Guard side-specific work with `worldObj.isRemote`.
- Code called from both sides that needs server-only state returns the permissive
  answer on the client and lets the server decide.
- Packet handlers validate the player themselves — reach, tile identity and
  permission — by routing through `isUseableByPlayer` rather than re-deriving it.

## NBT

- Keys are PascalCase strings: `"Work"`, `"AutoExport"`, `"RedstoneMode"`.
- `writeToNBT`/`readFromNBT` persist; `writeSyncNBT`/`readSyncNBT` describe the tile
  to clients. Only sync what the client actually renders.
- Absent means default. Don't write a tag whose value is the default — a missing
  key must read back correctly:

```java
if (redstoneMode != RedstoneMode.IGNORE)
    tag.setByte("RedstoneMode", (byte)redstoneMode.ordinal());
```

- Enum ordinals are persisted, so **declaration order is a save format**. Add new
  constants at the end, keep the default first, and give the enum an explicit
  field if some other order is needed for display or art.

After changing state, `markDirty()` to save and `markForUpdate()` to resync — the
setter does both, callers don't.

## Integration with other mods

Each optional mod gets its own package under `integration/`, and nothing outside
it may reference that mod's classes. A class that imports an absent mod must never
be loaded: either the mod's own discovery loads it (WDMla's annotation scan), or
`IntegrationManager` checks `Loader.isModLoaded` first and catches `Throwable`
around the registration.

Content shared between two integrations goes in a mod-free package — see
`integration/tooltip/`, which both the WDMla and Waila front ends draw from.

## Localisation

Every player-visible string comes from `en_US.lang`. Key namespaces:

| Prefix | Used for |
| --- | --- |
| `tile.laziestae2.*` | block names and item tooltips |
| `container.laziestae2.*` | GUI titles and in-GUI labels |
| `gui.laziestae2.*` | widget labels and control states |
| `tooltip.laziestae2.*` | in-world tooltip lines (WDMla / Waila) |

Format strings keep their `%s`/`%d` order — translators must not reorder them
without positional arguments.

## GUI and assets

Sprite sheets are on a **16px grid** where possible, one frame per state, laid out
left to right. Document the layout in a comment directly above the constants that
sample it, so the sheet and the code can be checked against each other:

```java
// auto_export.png is 64x16, grouped by state with the hovered frame beside it:
// on (0), on hovered (16), off (32), off hovered (48).
```

Rules that have bitten us:

- **Draw and hit-test from the same numbers.** If a sprite is inset or centred, the
  click bounds get the same inset. Never let them drift apart.
- **Odd glyphs need odd boxes.** A 2px mark cannot centre in a 17px cell; match the
  parities or the art will always look 1px off.
- **Panels that resize are nine-patches**, not stretched single sprites — fixed
  corners, stretched edges, filled centre.
- **Never ship a control whose state can't be changed.** If a tile gates behaviour
  on a setting, its GUI must expose that setting, or the tile opts out of the
  feature entirely (`supportsRedstoneControl()` returning false).

Art sources (`.ase`, working files, backups) do not belong under
`src/main/resources` — everything there ships inside the jar.

## Configuration

Config lives in `LaziestConfig` as public static fields, read live so in-game edits
take effect without a reload. Every entry passes a default and a comment string to
`config.get`, and values are clamped at the point of use rather than on load.
