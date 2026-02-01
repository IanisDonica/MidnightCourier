# Midnight Courier

You play a as courier in the concrete jungle maze known as Bucharest, delivering for glovo in order to try and
make some extra money for a holiday abroad. In the city you have to battle with bad infastructure (Potholes that will kill you),
horible and unpredictible (BMW) drivers that will do nothing to avoid killing you directly or indirectly (through explosions from car crashes), and corupt police trying to get 
your paycheck as a bribe. The player cannot directly kill the enemies (but can get them to kill themselves), this is left for the player to figure out.

## Story
The story is delivered through high quality, oscar worthy cutscenes, play and find out (no spoilers).


## Rules
The game has 2 modes, Endless and Campain. In general the player must pick up a delivery (key), the arrow will point to it if not picked up, and then take it to a drop-off point (DropOff). In Campaign, the drop-off grants permission to leave (can-leave), and the ExitDoor ends the level once can-leave is active. In Endless, the loop is: key (delivery) → drop-off → new key (delivery), repeating forever. The arrow tracks the current target, in endless the player also has a small map on to show him the area where he must go (to make it less frustrating). 
During the gameplay loop, the player is targeted by policeman, bad infastructure (potholes) and BMW drivers that try to kill or bancrupt him, Potholes and BMW driver collisions result in an instant death, explostions will knock the player back and policemen will arrest the player if he runs out of bride money (HP).
In Endless mode, dificualty scales over time: spawns accelerate the longer you survive, so the map gradually fills with more threats. The delivery timer adapts to player skill, finishing a delivery quickly reduces the next time limit more, while slower completions reduce it less.

## Code Organization
- `MazeRunnerGame.java`: Central entry point and screen router; owns shared managers (audio, config, graphics, progression).
- `maze/screen/`: One class per game state (menu, settings, gameplay, cutscenes, victory/game over).
- `maze/screen/BaseEndScreen.java`: Shared parent for all victory/death screens (common background, fade, stats, and button layout).
- `entity`: Powerups, obstacles, players, enemies; They all share common collision code, common rendering code, and common protected atributes, this way each entity can be made with only a few lines of code that declare how its different from the others. For example the HealthPickup only states what it's animations frames are and that it should give the player HP and poitns upon pickup.
- `core/src/de/tum/cit/fop/maze/ai/`: A* alogorithm implementation (that is used by BMW drivers and Poliemen (enemies)), Also contains the state logic for the enemies. We decide to make each state an entity so that in the future it would be easier to implement different enemies who differences in their state change logic, but otherwise use the same states
- `core/src/de/tum/cit/fop/maze/system/`: HUD, input handling, save/load, achievements, audio, points, dev console, progression
- `core/src/de/tum/cit/fop/maze/system/progression`: All the upgrades for the ProgressionManager Class
- `core/src/de/tum/cit/fop/maze/entity/DeathCause.java`: Enum used to classify player death reasons (pothole, BMW, arrest, timeout, etc.), so screens and effects can react consistently.

### Map Value Table (properties files)
These values are interpreted by `MapLoader` when spawning entities and building layers:

| Value | Meaning |
|------:|---------|
| 1 | Entry point |
| 2 | Exit |
| 3 | Health pickup |
| 4 | Guard enemy |
| 5 | Key |
| 6 | Energy drink |
| 7 | Wall |
| 8 | Pothole trap |
| 9 | Drop-off |
| 10 | Shop |
| 11 | Road |
| 13 | BMW driver |

Notes:
- Values **over 100** are treated as **tile IDs** for the visual TMX layer. `MapLoader` subtracts 100 and writes them into the generated map CSV.
- A single coordinate can effectively carry **multiple meanings**: one value defines the rendered tile (>=100), while a separate entry can define gameplay (e.g., wall/road/collectible) for the same `(x,y)` in the properties file.

## UML Diagram

<img width="2204" height="1398" alt="AIBehaviour" src="https://github.com/user-attachments/assets/dd7f6fed-c458-474c-a093-478914633220" />
<img width="1952" height="1226" alt="Pathfinders" src="https://github.com/user-attachments/assets/00710ea1-be41-4c57-9f57-3834674e8a5b" />
<img width="5826" height="5143" alt="Entity" src="https://github.com/user-attachments/assets/10366b2b-b95e-4586-a6cd-953c12d00156" />
<img width="1130" height="1040" alt="MapLoader" src="https://github.com/user-attachments/assets/11d4a035-e58c-4c0a-b05b-df7c88d9a183" />
<img width="7706" height="4306" alt="screens" src="https://github.com/user-attachments/assets/7a2265f9-2012-44ad-b108-634075516a81" />
<img width="5368" height="2387" alt="managers2" src="https://github.com/user-attachments/assets/d72d2e5c-2961-4d4b-bac4-ba166cdd10c1" />
<img width="4252" height="2357" alt="managers" src="https://github.com/user-attachments/assets/862badb4-23a2-4bb4-8c48-1d36aae286d0" />

## How to run the game

### Prerequisites
- Java 17 (required by the Gradle toolchain)


### Run (DesktopLauncher)
If you want to launch it from your IDE, run the main class:

```
de.tum.cit.fop.maze.DesktopLauncher
```

Make sure the working directory is set to the root dir so the game can find the files.

## Developer Console
Press tilda (`~`) during gameplay to open the console. Useful commands:
- `help` (list commands)
- `tp x y` (teleport)
- `speed <multiplier>`
- `sethp <hp>` / `setmaxhp <hp>`
- `setcredits <points>`
- `godmode on|off`
- `giveenergydrink` / `givekey`
- `spawn <enemy|trap|bmwdriver> x y`

## Key Bindings
Input is routed through `KeyHandler`, which reads bindings from a JSON file at startup and maps actions (move, sprint, pause, zoom, console, etc.) to key codes.
The bindings file is located in `config/keybindings.json`.
You can remap controls in the Settings -> Controls menu, which updates this file and persists across runs.

## Policemen and BMW Drivers
- Policemen (guards) are grid-based enemies that move only on walkable tiles. They use A* pathfinding to avoid walls and switch between patrol, chase, and retreat states depending on player proximity and global events.
- BMW drivers are fast-moving enemies that follow road tiles only. They continuously pick new road goals, pathfind along the road layer, and can trigger deadly collisions and explosions on contact.
- **Policemen state switching rules:**
  - **Patrol → Chase:** The player is within line of sight (same maze corridor, no wall in between) and within vision range (~20 tiles).
  - **Chase → Retreat:** `shouldRetreat(...)` triggers (e.g., after a chase duration or loss of sight), or when any guard hits the player (global retreat).
  - **Retreat → Patrol:** Retreat lasts a short duration (~4s) or reaches the retreat target and finishes the retreat wait.
  - **Patrol Wait / Retreat Wait:** When a target is reached and the guard is centered on a tile, it pauses briefly, then resumes the next patrol/retreat path.

## Bonus Additions
- **Fog of War:** Implemented as a post-processing shaderc (`shaders/combined.frag`) writen in OpenGL in both `GameScreen` (only appears after level 2) and `SurvivalScreen`. The world is rendered to an FBO, then a shader is applied around the player using `fogIntensity`. The “new glasses” upgrade raises the radius by increasing that value.
- **Noire Mode:** Uses the grayscale shader (`shaders/grayscale.frag`) on the same FBO pipeline. It auto-triggers when HP is low (`player.getHp() <= 1`) and can also be toggled via input for manual testing.
- **Webserver Leaderboards:** `PointManager` writes local scores to `assets/data/highscore.json` and sends a JSON payload via `Gdx.net` to `transprut.solutions` (need to manage the DNS). It’s fire-and-forget, so networking never blocks gameplay. The webserver is a simple Django webapp; [source code](https://github.com/IanisDonica/webserverTransprut)
- **Anti-Aliasing** 
- **Endless Minimap (Key Preview):** A second `FrameBuffer` (`keyPreviewFbo`) renders a zoomed-out view centered on the key/exit target. The HUD shows this preview window to reduce dead-end frustration in survival mode.
- **Drift Mechanics:** 

## Upgrades (Skill Tree)
Upgrades are purchased in the shop and applied in `GameScreen` and `SurvivalScreen` via the `ProgressionManager`.

- **Root:** Base unlock for the progression tree. Purchasing it triggers the first-upgrade achievement.
- **Speed I / II / III:** Each tier adds +20% movement speed (stacking).
- **Health I / II / III:** Each tier adds +1 max HP (base 3 + tiers).
- **Regeneration:** Enables periodic HP regen; if HP is full, it awards points instead.
- **Drink Speed I / II:** Increases energy drink duration (each tier +50% duration).
- **New Glasses:** Increases fog-of-war visibility radius (+2 once).
- **Pothole Immunity:** Makes the player immune to pothole traps.
- **Stealth:** Reduces policeman detection radius (guards spot you from ~25% less distance).
- **Mastery:** Final-tier unlock; purchasing it triggers the mastery achievement.
