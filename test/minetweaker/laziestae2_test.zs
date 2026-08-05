// Laziest AE2 - MineTweaker integration test script.
//
// Install: copy this file into <gamedir>/scripts/ (for the dev environment,
// run/scripts/) together with a MineTweaker3 jar in the mods folder, then start
// the client. Scripts apply during MineTweaker's post-init, which runs after
// LaziestAE2 registers its defaults in FMLInitializationEvent, so removals here
// always see the built-in recipes.
//
// Verify afterwards by reading the log for the [minetweaker] lines below, then
// in game: hover a machine in NEI and press R (recipes) or U (usage), and
// craft one of the added recipes to confirm the machine actually accepts it.
//
// Deliberately avoids Applied Energistics item ids: AE2 rv3 registers its
// materials under item.ItemMultiMaterial with metadata that shifts between
// builds. Everything here is vanilla, ore dictionary, or laziestae2:material.
//
// laziestae2:material metadata
//   0 fluix steel          5 resonating crystal    10 spec core 8
//   1 carbonic fluix       6 parallel processor    11 spec core 16
//   2 fluix plated iron    7 spec core             12 spec core 32
//   3 coal dust            8 spec core 2           13 spec core 64
//   4 fluix logic unit     9 spec core 4           14 speculative processor

print("[LazyAE2 test] applying MineTweaker test script");


// --- Aggregator -------------------------------------------------------------

// Three inputs, mixing an ore entry with plain stacks. Order is irrelevant to
// the aggregator; it matches ingredients in any arrangement.
mods.laziestae2.Aggregator.addRecipe(<laziestae2:material:2>, <ore:ingotIron>, <minecraft:redstone>, <minecraft:clay_ball>);

// Union ingredient: exercises MTHelper flattening an "or" into several stacks.
// Either coal or charcoal in the first slot should satisfy this.
mods.laziestae2.Aggregator.addRecipe(<laziestae2:material:3> * 2, <minecraft:coal> | <minecraft:coal:1>, <minecraft:flint>, <minecraft:flint>);

// Wildcard input: any wool colour matches.
mods.laziestae2.Aggregator.addRecipe(<laziestae2:material:4>, <minecraft:wool:*>, <minecraft:string>, <minecraft:string>);

// Removes the built-in fluix steel recipe (coal dust + fluix dust + iron).
mods.laziestae2.Aggregator.removeRecipe(<laziestae2:material:0>);


// --- Centrifuge -------------------------------------------------------------

mods.laziestae2.Centrifuge.addRecipe(<laziestae2:material:3> * 4, <minecraft:coal_block>);

// Ore-dictionary input.
mods.laziestae2.Centrifuge.addRecipe(<minecraft:gunpowder>, <ore:dustCoal>);

// Removes a recipe this script added a moment ago, not a built-in one; proves
// script-added recipes are removable within the same pass.
mods.laziestae2.Centrifuge.removeRecipe(<minecraft:gunpowder>);


// --- Etcher -----------------------------------------------------------------

// Short form: top and bottom default to dustRedstone and itemSilicon.
mods.laziestae2.Etcher.addRecipe(<laziestae2:material:4>, <ore:ingotIron>);

// Full positional form: top, bottom, middle are distinct slots. Feeding the
// machine in the wrong order must NOT produce this output.
mods.laziestae2.Etcher.addRecipe(<laziestae2:material:8>, <minecraft:gold_ingot>, <minecraft:iron_ingot>, <minecraft:diamond>);

// Removes the built-in parallel processor recipe (resonating crystal middle).
mods.laziestae2.Etcher.removeRecipe(<laziestae2:material:6>);


// --- Energizer --------------------------------------------------------------

mods.laziestae2.Energizer.addRecipe(<laziestae2:material:5>, <minecraft:diamond>, 5000);

// Large energy cost, to confirm the buffer/progress maths handles it.
mods.laziestae2.Energizer.addRecipe(<laziestae2:material:13>, <minecraft:nether_star>, 2000000);

mods.laziestae2.Energizer.removeRecipe(<laziestae2:material:13>);


// --- Wildcard removal -------------------------------------------------------
//
// Kept last because it clears every remaining aggregator recipe whose output is
// a laziestae2 material, including ones added above. Comment it out when
// testing the additions in game.
//
// mods.laziestae2.Aggregator.removeRecipe(<laziestae2:material:*>);


print("[LazyAE2 test] script applied - expected results:");
print("[LazyAE2 test]  aggregator: +plated iron, +coal dust x2 (coal OR charcoal), +logic unit (any wool), -fluix steel");
print("[LazyAE2 test]  centrifuge: +coal dust x4 from coal block; gunpowder recipe added then removed");
print("[LazyAE2 test]  etcher:     +logic unit (short form), +spec core 2 (positional), -parallel processor");
print("[LazyAE2 test]  energizer:  +resonating crystal @5000 AE; spec core 64 added then removed");
print("[LazyAE2 test] no [minetweaker] ERROR lines should appear from this file");
