![ArtMap](http://puu.sh/kRWAF/2c81256338.jpg)

### Supported Versions:
* Spigot/Paper 1.15.2, 1.14.4, 1.13.2 - Master Branch
* Spigot/Paper 1.8.8 - 1.12.2 - 1.12 Branch

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

### Features
* Custom easel entity
* Basic filter for artwork titles
* Asynchronous protocol handling
* List & preview system to view artworks

### Permissions Nodes
* artmap.artist - allows players to use artmap
* artmap.admin - grants administrative override/deletion priveleges
* artmap.artkit - Will give players access to all dyes when seated at an easel and the config option forceartkit is set to true.

## Move to Gitlab
I've moved this project to gitlab https://gitlab.com/BlockStack/ArtMap.  Mirroring has been setup so all commits should be available to fork on github.  But please submit issues on Gitlab.

### Attribution
This is not my original work and belongs to Fupery.  I continue maintenance of this plugin in his absence for my personal use.  As he had the code freely available I have continued that with my updates.

Bukkit plugin - allows players to draw directly onto minecraft maps. 
User guide at [ArtMap Wiki](https://gitlab.com/BlockStack/ArtMap/wikis/home).

### NOTE
Use a plugin manager to reload your server whenever you update ArtMap - the Spigot reload command will freak out and throw a bunch of exceptions.
