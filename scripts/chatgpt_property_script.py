#!/usr/bin/env python3
import argparse
import os
import sys
import xml.etree.ElementTree as ET

FLIP_MASK = 0x1FFFFFFF
COLLIDE_KEYS = {"colide", "collide", "collision"}


def parse_tileset_collisions(tsx_path):
    """Return a set of local tile IDs that have a collision/colide property set true."""
    tree = ET.parse(tsx_path)
    root = tree.getroot()
    collidable = set()
    for tile in root.findall("tile"):
        tile_id = tile.get("id")
        if tile_id is None:
            continue
        props = tile.find("properties")
        if props is None:
            continue
        for prop in props.findall("property"):
            name = prop.get("name", "").strip().lower()
            if name not in COLLIDE_KEYS:
                continue
            value = prop.get("value", "").strip().lower()
            if value == "true":
                collidable.add(int(tile_id))
                break
    return collidable


def build_global_collidable_gids(tmx_path, tileset_dir):
    """Return a set of global gids that are collidable in the TMX tilesets."""
    tree = ET.parse(tmx_path)
    root = tree.getroot()
    gids = set()

    for tileset in root.findall("tileset"):
        firstgid = tileset.get("firstgid")
        source = tileset.get("source")
        if firstgid is None or source is None:
            continue
        firstgid = int(firstgid)
        tsx_path = os.path.join(tileset_dir, source)
        if not os.path.isfile(tsx_path):
            raise FileNotFoundError(f"Missing tileset: {tsx_path}")
        local_ids = parse_tileset_collisions(tsx_path)
        for local_id in local_ids:
            gids.add(firstgid + local_id)

    return gids


def iter_layer_rows(layer):
    data = layer.find("data")
    if data is None or data.get("encoding") != "csv":
        raise ValueError("Only CSV-encoded layer data is supported.")
    raw = data.text or ""
    rows = []
    for line in raw.strip().splitlines():
        line = line.strip()
        if not line:
            continue
        rows.append([int(v) for v in line.split(",") if v.strip() != ""])
    return rows


def build_collision_grid(tmx_path, collidable_gids):
    tree = ET.parse(tmx_path)
    root = tree.getroot()
    width = int(root.get("width"))
    height = int(root.get("height"))

    collision = [[False for _ in range(width)] for _ in range(height)]

    for layer in root.findall("layer"):
        rows = iter_layer_rows(layer)
        if len(rows) != height:
            raise ValueError("Layer row count does not match map height.")
        for row_idx, row in enumerate(rows):
            if len(row) != width:
                raise ValueError("Layer column count does not match map width.")
            y = height - 1 - row_idx  # TMX rows are top-to-bottom
            for x, gid in enumerate(row):
                base_gid = gid & FLIP_MASK
                if base_gid != 0 and base_gid in collidable_gids:
                    collision[y][x] = True

    return width, height, collision


def write_properties(path, width, height, collision):
    with open(path, "w", encoding="utf-8") as f:
        for y in range(height):
            for x in range(width):
                value = 7 if collision[y][x] else 0
                f.write(f"{x},{y}={value}\n")


def main():
    parser = argparse.ArgumentParser(description="Update level-1.properties from THE_MAP.tmx collisions.")
    parser.add_argument("--tmx", default="assets/Assets_Map/THE_MAP.tmx", help="Path to TMX map")
    parser.add_argument("--tilesets", default="assets/Assets_Map", help="Directory containing TSX tilesets")
    parser.add_argument("--out", default="maps/level-1.properties", help="Output properties file")
    args = parser.parse_args()

    tmx_path = args.tmx
    tileset_dir = args.tilesets
    out_path = args.out

    collidable_gids = build_global_collidable_gids(tmx_path, tileset_dir)
    width, height, collision = build_collision_grid(tmx_path, collidable_gids)
    write_properties(out_path, width, height, collision)

    return 0


if __name__ == "__main__":
    sys.exit(main())
