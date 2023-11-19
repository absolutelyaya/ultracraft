## Major Changes
- Switched from Geckolib to Aurelib. **Geckolib is no longer required to run the mod**.
- Fixed ultra-speed gamerule not affecting walk speed
- Hell Observers now detect *ALL* Hostile entities and not just those added by ultracraft.
- Fixed ultra-Hivel Gamerule
- Fixed ultra-speed Gamerule
- Added Core Eject Damage Type
- Added Piercer Alt-Fire Damage Type
- Terminal improvements
  - Terminals are now automatically unfocused when out of reach
  - Graffiti cursor is now more accurate
  - Graffiti now spans the entire back of Terminals
    - This also means Graffitis texture size has been increased (32x32 -> 40x40)
    - Old Graffitis are resized automatically
  - Added Random initial Screensavers
    - Custom ones can be added using datapacks
  - Made Terminal Item NBT less volatile
  - Base Color can now be set to any Hex Color
  - Fixed Crash that occurs when opening the Weapons Tab without the Piercer recipe unlocked
- Fixed issue with Soap visually disappearing after picking it up
- Fixed Recipe Syncing from Server to Client
- Added Functionality to Sky Blocks
## Settings & Gamerules
## Commands
- Added `ultrabossbar <bossbar-id>` subcommand to `/ultracraft`
  - changes a chosen bossbars style to the ultracraft one
    - why are the bossbar styles hard coded into the `/bossbar` command Mojang, even if you're not planning on it, for the pure possibility that you (OR SOMEONE ELSE \*WINKS\*) ever want to add another style, they can without much trouble. Like,, bruh
## Tweaks
## Minor Changes
- Fixed logic error in auto aiming
  - Pets also don't get targetted by coins or sharpshooter shots anymore
- Fixed absorption not being displayed in ultraHUD anymore
- Malicious Face now takes actual `interrupt` type damage when interrupted (previously generic entity damage)
- Added Trails to Coins
- Added Flowers and Tall Flowers to fragile Block Tag
  - Nothing pure is allowed to exist in this cruel world :pensive:
- Fixed Machinesword Attack sounds for `Better Combat` not playing
## Resource Changes
## API Changes
