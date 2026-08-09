# Project purpose

## The problem

The owner plays tabletop RPGs and manages D&D characters in D&D Beyond, which works
well but only supports one game system. Creating a character for any other system
means leaving the tool. The obvious alternative, Roll20, is generic: it stores sheets
but knows nothing about the rules behind them, so everything is manual.

The gap is a tool that is both **system-aware** and **system-plural**.

## What this project is

A personal-scale web application where a player keeps characters from several game
systems in one vault, and where the application understands each system's rules well
enough to be useful during play — not just to store numbers.

## What "useful during play" means here

- Character creation explains each choice *before* it is made. Selecting a race shows
  what that race grants.
- Dice rolls happen inside the application, tied to the mechanic that triggered them,
  and are kept in a permanent history.
- Mechanics are active: casting a spell leads into rolling its damage, with the
  character's modifiers already applied.
- A character has an identity, not just a stat block: a portrait and a backstory.

## What this project is not

- Not a virtual tabletop. No maps, no tokens, no battle grid.
- Not a campaign manager or a note-taking tool for the group.
- Not multi-tenant SaaS. Scale target is one player and their friends.
- Not a complete rules database. Only the systems the owner actually plays get
  implemented, and only the parts of them that are used.

## Success criteria

The owner stops opening D&D Beyond to run a session, and the second game system is
added without rewriting anything built for the first.