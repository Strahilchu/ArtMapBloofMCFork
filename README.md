![ArtMap](http://puu.sh/kRWAF/2c81256338.jpg)


# Version 4
Version 4 is not compatible with version 3's artwork database.  Please export the artwork on version 3 and import it into version 4.

## Features
* Seperate world to store artwork
    - This keeps artwork seperate from maps used for treasures and world mapping.
    - Allows for more ids available for artwork.
    - Allows for easier restores as the world can be deleted and recreated easily 
* Maps database changes.
    - Allows for search terms to be added to artwork. (ex: landscape, windmill, large)
    - Allows artwork to be grouped for easier lookup and showing of larger paintings

## Release 4.0.0-SNAPSHOT
* Very early work and will change frequently!


# Version 3

## Release 3.3.11
* MarriageMaster integration - Prevent players from using gift wen in the Artkit.

## Release 3.3.10
* Prevent players in artkit from picking up items. Prevents loss of items thrown to a player while they are using artkit.

## Release 3.3.9
* Disable Map reuse as it might be causing map collisions and blank maps.
* Add some logging around map initialize to see if it is having problems.

## Release 3.3.8
* 1.15 support
* Fix compilation problem caused by protocol lib 4.4.0
* Update AnvilGui dependency for 1.15 support

## Release 3.3.7
* Fix cartography table integration

## Release 3.3.6
* Fix incorrect assumption that Denizen includes Depenizen classed causing a ClassNotFoundException on startup.

## Release 3.3.4
* Updated anvilgui - Brush for saving artwork will now work on 1.14.4

## Release 3.3.3
* Fixed an issue where a server with over 32768 maps would cause a short overflow and try and load negative map IDs which would fail.
* Removed initial artwork checks from startup as they were slow on large numbers of artwork
    - Those checks now run on map load so keep an eye on timings of MapInitializeEvent

## Release 3.3.2
* Fixed user disconnect on Dropper tool use.
* Removed NMS dependency which should make compiling a bit easier

## Release 3.3.1
* Artkit now saves hotbar during current login session.
    - This works across different easels.
    - Clears on logout or server restart in case something breaks.
* Eye Dropper now prints out base dye plus the byte code for easier shade matching on other eisels.
* Fixed mismatch by making Coarse Dirt -> Podzol.

## Release 3.3.0
### Major Changes
* Paint Bukkit is no longer craftable.  
    - Instead use a regular bukkit in the main hand the dye you want to fill with in the offhand.
    - There have been too many exploits with the crafting of paint bukkets and duplicating items this neatly removes that problem.
    - Now allows players in creative mode to more easily use paint buckets without leaving the easel to craft them.
* Added Eye Dropper Tool.
    - Using a sponge left click the colour you would like to pick up. Then right click to draw with the color.
    - Allows easy copy of shades.
    - Usable with the paint bucket to fill with a shade.
* Admin's can now right click dyes in the dye menu to receive a copy of the dye.
* Players can now obtain a copy of their own artwork by right clicking on it in the preview menu.  It cost them one empty map just like using a crafting table would.

### Minor Changes
* Lots of cleanup to the English Language file.
    - Try to make sure tooltips won't go off screen even on huge GUI configurations.
    - Made more text able to be changed via the language files.
    - If you are using a custom lang.yml I suggest comparing to the new lang.yml to pickup changes.
    - If anyone has updates to the other languages files please sumbit an issue and I will have the updated or added as soon as possible.
* Fixed help menu back buttons sometimes being invisible.
* Fixed a few duplication and stealing from artkit bugs.
* Add '/art break' if a player really wants to break and easel and reset the artwork.
    - Prevents accidental easel breaks.

## Move to Gitlab
I've moved this project to gitlab https://gitlab.com/BlockStack/ArtMap.  Mirroring has been setup so all commits should be available to fork on github.  But please submit issues on Gitlab.

### Attribution
This is not my original work and belongs to Fupery.  I continue maintenance of this plugin in his absence for my personal use.  As he had the code freely available I have continued that with my updates.  I ask that anyone that uses this plugin have either previously bought it from spigot.  Or please do so in the future if Fupery returns.

Bukkit plugin - allows players to draw directly onto minecraft maps. 
User guide at [ArtMap Wiki](https://gitlab.com/BlockStack/ArtMap/wikis/home).

### Features
* Custom easel entity
* Basic filter for artwork titles
* Asynchronous protocol handling
* List & preview system to view artworks

### Supported Versions:
* Spigot 1.13.2 - 1.15.2  - master 
* Spigot 1.13.2 - 1.15.2  - release-3.0.0 Branch
* Spigot 1.8.8 - 1.12.2   - 1.12 Branch

### Permissions Nodes
* artmap.artist - allows players to use artmap
* artmap.admin - grants administrative override/deletion priveleges
* artmap.artkit - Will give players access to all dyes when seated at an easel and the config option forceartkit is set to true.

### NOTE
Use a plugin manager to reload your server whenever you update ArtMap - the Spigot reload command will freak out and throw a bunch of exceptions.
