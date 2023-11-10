## Major Changes
- Fixed FPS tanking when rendering Player Wings (thanks for helping me figure this one out BunnyHaver)
- Fixed Crash when using Terminal redstone buttons without having `better combat` installed
- Fixed Nail damage not referencing the Nails Owner properly
- Fixed Knuckleblast death message
- Fixed "hitscan" damagetype Tag not working
  - This means, `core ejects` and `interruptable charges` now explode again when shot
- Fixed primary fire being usable while charging alt fire
- Fixed bug that let you continue firing a weapon after death
- Fixed Pump shotgun keeping Pumps after switching variants
- Added Dispenser Behavior to Projectiles and Soul//Blood Orbs
  - Yes, "Projectiles" includes Soap
- Fixed Cancer Bullet Item spawning a normal Hell Bullet on use
- Improved cancer bullet behavior
- Fixed Cancerous Rodents with Size > 0 taking knockback
  - also made them solid
- magnets now break from damagetypes in the tag `ultracraft:break_magnet`
- Vent Covers can now be oriented vertically
- Hell Observers can now be oriented vertically
- Added Recipe for Vent Covers
- Cerberus is now guaranteed to drop a golden Apple if a Cerberus Ball was parried back at it
- Skewer Entities (harpoon & magnet) cannot be parried anymore while stuck in an entity
- Fixed Hell Observer Area Offset nbt block picking
- Harpoons don't replenish Durability anymore when picked up
- Skewer Entities can now be punched to be broken
  - Harpoons return to their owner when broken
- Harpoons now drop as an item instead of disappearing when broken/discarded
## Settings & Gamerules
## Commands
- `/ultracraft progression` subcommands now support multiple targets
  - only exception is `list`
- added `/ultracraft progression grant-all`
  - grants all progression entries to a given list (shocker)
## Tweaks
- Buffed V2 (Max Health 40 -> 80)
- Nerfed Nails (Damage 0.4 -> 0.35)
- Added Harpoon Damage to `ultracraft:unboosted` damagetype Tag
  - This means harpoon damage doesn't get multiplied by 2.5 when applied to non-mod entities
- Buffed Stamina regen (1 -> 1.5 per tick)
## Minor Changes
- Fixed the missing Translation Entry for the Arm Cycle Hotkey
- Added a few splash texts to the non-essential resourcepack
- Fixed the typo in "SRIMP"
- Made Vent Cover Slits transparent
- Glass is no longer breakable by slams per default
- Blood Fluid is no longer pushable with pistons
- Fixed Z-Fighting on Cerberus Block
- Beam Projectiles get discarded more quickly now
- Fixed Chargeable Weapons (like pierce Revolver) still shaking after switching variants
- Fixed Knuckle blasting while sliding breaking the slide animation
- Hivel State is now saved in client settings
## Resource Changes
## API Changes
