# 🐾 ClarityPets

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Paper-26.3+-red.svg?style=for-the-badge)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-25-orange.svg?style=for-the-badge)](https://openjdk.org)
[![Cross-Play](https://img.shields.io/badge/Geyser%2FFloodgate-Compatible-brightgreen.svg?style=for-the-badge)](https://geysermc.org)
[![Translations](https://img.shields.io/badge/Languages-9%20Supported-purple.svg?style=for-the-badge)](#-multi-language-support)

**ClarityPets** is an advanced, high-performance survival companion and automated colosseum duel system custom-engineered for **Clarity Network** (Minecraft 1.21.4+ / Paper 26.3+, Java 25).

Built from the ground up to replace legacy pet systems, ClarityPets spawns **real vanilla mobs with custom native Paper AI goals (zero NMS hacks)**, features **85+ tameable companion mob types**, **23 trainable RPG skills across 3 skilltree archetypes**, an expandable **virtual pet backpack**, an automated **Colosseum arena duel engine**, and an interactive dashboard built 100% on **Paper's Native Dialog API**—completely free of chest GUI hacks or inventory glitches.

---

## ✨ Features

### 🧠 1. Real Vanilla Mobs with Pure Paper AI (Zero NMS Hacks)
* Spawns genuine Paper entities with custom AI goals via Paper's native `MobGoals` API (`Goal<Mob>`).
* **Physics & Visuals**: Fluid ground sprinting, smooth 3D airborne gliding and hovering for flying pets (e.g., Breeze, Allay, Phantom, Ghast), and natural underwater swimming for aquatic mobs.
* **Smart Following (`PetFollowOwnerGoal`)**: Catch-up acceleration when the owner sprints, soft follow distance, and async cross-world / extreme-distance teleportation.
* **Idle Head Tracking (`PetLookAtOwnerGoal`)**: Pets turn their heads naturally to maintain eye contact with their owner when nearby.
* **Buoyancy & Water Safety (`PetFloatGoal`)**: Automatic floating and surface breathing for non-aquatic mobs.
* **Combat & Protection (`PetAttackGoal`)**: Engages monsters attacking the owner or retaliates according to the selected Behavior Mode.

### 🐾 2. 85+ Tameable Vanilla Mob Types
ClarityPets supports 85+ distinct mob companions spanning 5 movement and ecological categories:
* **🏔️ LAND (44+ Mobs)**:
  * Wolf, Cat, Ocelot, Fox, Polar Bear, Panda, Iron Golem, Snow Golem, Warden, Ravager, Camel, Horse, Donkey, Mule, Skeleton Horse, Zombie Horse, Pig, Cow, Sheep, Chicken, Rabbit, Armadillo, Goat, Sniffer, Villager, Wandering Trader, Pillager, Vindicator, Evoker, Witch, Zombie, Husk, Drowned, Skeleton, Stray, Wither Skeleton, Piglin, Piglin Brute, Zombified Piglin, Hoglin, Zoglin, Creeper, Enderman, Spider, Cave Spider, Silverfish, Endermite, Shulker, and baby variants.
* **✈️ AIR (7 Mobs)**:
  * Breeze, Allay, Phantom, Ghast, Bat, Bee, Vex, Wither, and Ender Dragon.
* **🌊 AQUATIC (7 Mobs)**:
  * Dolphin, Glow Squid, Squid, Guardian, Elder Guardian, Cod, Salmon, Tropical Fish, Pufferfish, and Tadpole.
* **🦎 AMPHIBIOUS (3 Mobs)**:
  * Axolotl, Turtle, and Frog.
* **🛡️ Unique Passives & Active Abilities**:
  * Each mob comes with hand-tailored base stats (HP, ATK, DEF, SPD), passive perks (e.g., Cat Creeper Ward, Dolphin Grace, Iron Golem Knockback Resistance, Warden Sonic Pulse, Breeze Wind Burst), and distinct Colosseum active skills.

### 🌳 3. 23 Trainable RPG Skills & 3 Archetypes
Players can customize their companion's growth through **3 specialized skilltree archetypes**:
* ⚔️ **WARRIOR (Combat Specialist)**: Increases offensive firepower, critical strike rate, life leech, and combat crowd control.
* 🌾 **GATHERER (Resource & Utility Companion)**: Boosts crop harvesting, mining fortune, auto floor item pickup, tree felling, and extra fishing treasures.
* 🛡️ **SURVIVOR (Tank & Guardian Support)**: Enhances maximum HP, armor resistance, regeneration rate, environmental fire wards, and threat management.

#### Complete Skill Directory (5 Tiers Each):
| Skill | Archetype | Description |
| :--- | :--- | :--- |
| **Damage** | Warrior | Increases pet physical attack damage (+15% to +100%). |
| **Critical Strike** | Warrior | Grants chance to deal 1.75x critical damage (+6% to +30%). |
| **Life Steal** | Warrior | Converts damage dealt into health (+5% to +25%). |
| **Thorns** | Warrior | Reflects incoming damage back at the attacker (+10% to +50%). |
| **Taunt** | Warrior | Pulls aggro from nearby hostile monsters when owner is threatened. |
| **Ground Stomp** | Warrior | Periodic AoE shockwave dealing damage and knockback. |
| **Crop Harvester** | Gatherer | Automatically replants and duplicates broken crops (15% to 50% chance). |
| **Mining Luck** | Gatherer | Grants bonus ore drops when owner mines underground (10% to 40%). |
| **Lumberjack** | Gatherer | Bonus wood drops and chance to fell adjacent log blocks (15% to 60%). |
| **Treasure Hunter** | Gatherer | Chance to uncover rare gems and coins when digging gravel/sand. |
| **Fishing Buddy** | Gatherer | Doubles fish catches and reels in treasure items (12% to 45%). |
| **Item Pickup** | Gatherer | Pet vacuum-picks up nearby ground items directly into its backpack. |
| **Backpack** | Gatherer | Expands pet virtual storage capacity from 9 to 45 slots. |
| **Health Boost** | Survivor | Permanently raises maximum health (+20% to +150%). |
| **Rapid Regen** | Survivor | Accelerates out-of-combat HP recovery (+0.5 to +3.5 HP/sec). |
| **Defense** | Survivor | Reduces incoming physical and projectile damage (+10% to +50%). |
| **Swiftness** | Survivor | Grants bonus movement speed (+8% to +35%). |
| **Resistance Aura** | Survivor | Grants defensive buffs to the owner within a 6-block radius. |
| **Fire Ward** | Survivor | Grants complete immunity to fire and lava damage. |
| **Potion Aura** | Survivor | Periodically pulses beneficial potion effects to the owner. |
| **Feed Master** | Survivor | Increases EXP gains and stat boosts when feeding daily treats. |
| **Soul Bound** | Survivor | Pet instantly respawns without cooldown upon fatal encounters. |
| **Exp Booster** | Neutral | Boosts passive EXP gains from mob kills and owner survival activities. |

### 🎒 4. Virtual Pet Backpack (`/pet backpack`)
* Integrated virtual inventory with zero item loss and no chest GUI block dependencies.
* **Auto-Scaling Size**: Starts at 9 slots and expands up to 45 slots based on the `BACKPACK` skill tier.
* **Auto-Pickup Integration**: When paired with `PICKUP` skill, your pet searches for dropped floor items within range and stores them into the backpack automatically.
* **Database Persistence**: Serialized via Base64 and asynchronously autosaved to MySQL on `InventoryCloseEvent`.

### ⚔️ 5. Automated Colosseum Duel Arena
A fully automated, asynchronous 1v1 arena duel system:
* **Challenge System (`/pet duel <player>`)**: Invite players to pet combat; pending invitations expire automatically after 60 seconds.
* **Tactical Stances**: Choose between **Aggressive** (+20% ATK, -10% DEF), **Defensive** (+30% DEF, -15% ATK), or **Balanced** (+5% All Stats).
* **AI Duel Engine**: Spawns isolated duel clones inside the arena bounds with custom pathfinding, dynamic active skill procs, and stun/root CC mechanics.
* **Active Duel Skills**:
  * *Baby Zombie:* **Frenzy Strike** (Rapid double slash)
  * *Skeleton:* **Headshot** (1.5s stun crit)
  * *Piglin:* **Overpower Swing** (Heavy knockback strike)
  * *Wither Skeleton:* **Wither Curse** (Decay damage)
  * *Enderman:* **Shadow Ambush** (Teleport behind target with crit)
  * *Cave Spider:* **Web Trap** (Roots target for 2s)
  * *Stray:* **Frostbite Arrow** (Slowness debuff)
  * *Breeze:* **Wind Burst** (AoE pushback & pathing reset)
  * *Witch:* **Splash Salve** (Emergency heal)
  * *Baby Zombified Piglin:* **Golden Bastion** (Defense buff)
  * *Baby Drowned:* **Aqua Leach** (Vampiric strike)
* **ELO Rating & Leaderboards**: Complete tracking of wins, losses, draws, win streaks, and competitive rating.

### 🎭 6. Behavior Modes
Customize how your active companion reacts in the wild:
* **Passive**: Never attacks; ignores hostile threats and avoids provoking mobs.
* **Defensive (Default)**: Attacks monsters that attack the owner or the pet.
* **Aggressive**: Proactively hunts and engages hostile monsters within 10 blocks.
* **Sentry**: Guards a stationary location and holds position.

### 📜 7. 100% Native Paper Dialog API
* **Strictly No Chest GUIs**: All interaction menus, pet selection lists, skilltree upgrades, and duel challenges use Paper's client-side Dialog API.
* **High Contrast & Clean Typography**: Clean white text, crisp layout, color-coded primary buttons (Green for Summon/Confirm, Red for Release/Cancel, White for Navigation).
* **Paginated Pet Storage**: Smooth 8-item paginated carousel with Previous/Next buttons.
* **Interactive Guide & Tips**: In-game dialog guides explaining skill mechanics, daily treat leveling, and behavior modes.

### 🌐 8. Multi-Language Support
Deep integration with **ClarityLangCore** with standalone YAML bundle fallbacks:
* 🇬🇧 **English (`en_US`)**
* 🇮🇩 **Indonesian (`id_ID`)**
* 🇩🇪 **German (`de_DE`)**
* 🇪🇸 **Spanish (`es_ES`)**
* 🇫🇷 **French (`fr_FR`)**
* 🇯🇵 **Japanese (`ja_JP`)**
* 🇧🇷 **Portuguese (`pt_BR`)**
* 🇷🇺 **Russian (`ru_RU`)**
* 🇨🇳 **Chinese (`zh_CN`)**

---

## 💻 Commands & Permissions

### Player Commands (`claritypets.use`)
| Command | Aliases | Description |
| :--- | :--- | :--- |
| `/pet` | `/pets` | Opens the main Paper Dialog dashboard. |
| `/pet storage` | — | Opens paginated pet storage and companion selector. |
| `/pet colosseum` | — | Opens the Colosseum duel management menu. |
| `/pet guide` | `/pet tips` | Opens the companion guide & gameplay tips dialog. |
| `/pet skilltree` | `/pet skills` | Opens the skilltree view and point allocation menu. |
| `/pet backpack` | `/pet bag` | Opens the companion's virtual storage backpack. |
| `/pet summon <type>` | — | Quickly summons a specific pet from your stable. |
| `/pet despawn` | — | Safely dismisses your active companion. |
| `/pet stance <stance>`| — | Sets Colosseum stance (`AGGRESSIVE`, `DEFENSIVE`, `BALANCED`). |
| `/pet mode <mode>` | — | Sets wild behavior mode (`PASSIVE`, `DEFENSIVE`, `AGGRESSIVE`, `SENTRY`). |
| `/pet treat` | — | Feeds daily treats (Carrots, Apples, Cookies, Bread) for bonus EXP. |
| `/pet rename <name>` | — | Renames your active companion (max 24 characters). |
| `/pet release` | — | Releases active pet permanently (with safety confirmation dialog). |
| `/pet duel <player>` | — | Challenges a player to an automated Colosseum arena duel. |

### Admin Commands (`claritypets.admin`)
| Command | Aliases | Description |
| :--- | :--- | :--- |
| `/petadmin reload` | — | Reloads `config.yml` and language bundles. |
| `/petadmin give <player> <mob>` | — | Unlocks a pet companion for a player. |
| `/petadmin take <player> <mob>` | — | Removes a pet companion from a player's stable. |
| `/petadmin setlevel <player> <mob> <level>` | — | Sets a pet's level directly (1–50). |
| `/petadmin unlock <player> all` | — | Unlocks all 85+ mob types for a player. |
| `/petadmin resetdata <player>` | — | Clears all pet and duel data for a target player. |
| `/petadmin info <player>` | — | Displays pet statistics and duel summary of a player. |

---

## 📊 PlaceholderAPI Placeholders

| Placeholder | Description |
| :--- | :--- |
| `%claritypets_active_pet%` | Custom name or display name of the active companion. |
| `%claritypets_active_pet_type%` | Mob type name (e.g. Wolf, Warden, Breeze). |
| `%claritypets_active_pet_level%` | Current level of the active companion. |
| `%claritypets_active_pet_category%` | Ecological category (`LAND`, `AIR`, `AQUATIC`, etc.). |
| `%claritypets_active_pet_exp%` | Current experience points. |
| `%claritypets_active_pet_mode%` | Current behavior mode (`DEFENSIVE`, `PASSIVE`, etc.). |
| `%claritypets_active_pet_passive%` | Name of the pet's unique passive ability. |
| `%claritypets_active_pet_active_skill%`| Name of the pet's active Colosseum skill. |
| `%claritypets_duel_wins%` | Total Colosseum duel wins. |
| `%claritypets_duel_losses%` | Total Colosseum duel losses. |
| `%claritypets_duel_draws%` | Total Colosseum duel draws. |
| `%claritypets_duel_streak%` | Current win streak. |
| `%claritypets_duel_best_streak%` | Career highest win streak. |
| `%claritypets_duel_rating%` | Competitive ELO rating (default: 1000). |
| `%claritypets_duel_winrate%` | Formatted win percentage (e.g. `65.4%`). |
| `%claritypets_total_pets%` | Total number of unlocked companions in player's stable. |

---

## 🛠️ Building & Installation

### Requirements
* **Java 25** (JDK 25)
* **Paper 26.3+** (Build #226 or higher for Minecraft 1.21.4+)
* **MySQL 8.0+** or MariaDB (for HikariCP database storage)
* Optional: **ClarityLangCore** (for global server language switching), **PlaceholderAPI**

### Compiling from Source
```bash
git clone https://github.com/Harukiyu/ClarityPets.git
cd ClarityPets
./gradlew build
```
The compiled shadow JAR will be output to:
```text
build/libs/ClarityPets-1.0.0.jar
```

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0 (GPLv3)**.  
See the [LICENSE](LICENSE) file for details.

```text
Copyright (C) 2026 Harukiyu / Clarity Network
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```
