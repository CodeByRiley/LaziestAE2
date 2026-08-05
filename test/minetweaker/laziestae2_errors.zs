// Laziest AE2 - MineTweaker error-path test script.
//
// Every call below is INVALID on purpose. The mod must reject each one with a
// logged error and keep loading; nothing here may crash the game or leave a
// half-registered recipe behind. Delete this file once the paths are verified.
//
// Expected [minetweaker] ERROR lines, one per numbered case:
//   1. Aggregator: recipe needs an output and three inputs
//   2. Aggregator: recipe needs an output and three inputs
//   3. Centrifuge: recipe needs an output and an input
//   4. Etcher: recipe needs an output and three inputs
//   5. Etcher: recipe needs an output and an input
//   6. Energizer: energy cost must be positive
//   7. Energizer: energy cost must be positive
//   8. Energizer: recipe needs an output and an input
//
// Cases 9 and 10 are silent no-ops by design, not errors.
//
// Unknown ore names are used to produce empty ingredients. Do not swap them for
// unknown item ids such as <minecraft:notreal> - those fail at script parse
// time and never reach the mod's validation.

print("[LazyAE2 errors] applying error-path script; 8 errors expected");

// 1. Empty ore entry in an input slot.
mods.laziestae2.Aggregator.addRecipe(<laziestae2:material:0>, <ore:dustNonexistentium>, <minecraft:redstone>, <minecraft:redstone>);

// 2. Empty ore entry in the last slot.
mods.laziestae2.Aggregator.addRecipe(<laziestae2:material:0>, <minecraft:redstone>, <minecraft:redstone>, <ore:ingotUnobtainium>);

// 3. Centrifuge with an empty input.
mods.laziestae2.Centrifuge.addRecipe(<laziestae2:material:3>, <ore:dustNonexistentium>);

// 4. Etcher full form, empty middle.
mods.laziestae2.Etcher.addRecipe(<laziestae2:material:4>, <minecraft:redstone>, <minecraft:iron_ingot>, <ore:gemNonexistentium>);

// 5. Etcher short form, empty middle.
mods.laziestae2.Etcher.addRecipe(<laziestae2:material:4>, <ore:gemNonexistentium>);

// 6. Zero energy cost.
mods.laziestae2.Energizer.addRecipe(<laziestae2:material:5>, <minecraft:diamond>, 0);

// 7. Negative energy cost.
mods.laziestae2.Energizer.addRecipe(<laziestae2:material:5>, <minecraft:diamond>, -4000);

// 8. Empty input; validated before the energy check, so this reports the
//    input error rather than the energy one.
mods.laziestae2.Energizer.addRecipe(<laziestae2:material:5>, <ore:gemNonexistentium>, 0);

// 9. Removal with an empty ingredient: expands to zero stacks, so the loop body
//    never runs. Silent no-op, no error line.
mods.laziestae2.Centrifuge.removeRecipe(<ore:dustNonexistentium>);

// 10. Removal of an output no recipe produces. Also silent.
mods.laziestae2.Etcher.removeRecipe(<minecraft:cake>);

print("[LazyAE2 errors] error-path script done");
