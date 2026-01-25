#!/usr/bin/env python3
import argparse
import os
import sys
import xml.etree.ElementTree as ET

FLIP_MASK = 0x1FFFFFFF

def read_road_tile_ids(tsx_path: str):
    tree = ET.parse(tsx_path)
    root = tree.getroot()
    ids = set()
    for tile in root.findall("tile"):
        tile_id = tile.get("id")
        if tile_id is None:
            continue
        props = tile.find("properties")
        if props is None:
            continue
        for prop in props.findall("property"):
            name = (prop.get("name") or "").strip().lower()
            value = (prop.get("value") or "").strip().lower()
            if name == "road" and value == "true":
                ids.add(int(tile_id))
                break
    return ids


def build_global_road_gids(tmx_path: str, tileset_dir: str):
    tree = ET.parse(tmx_path)
    root = tree.getroot()
    gids = set()

    for tileset in root.findall("tileset"):
        firstgid = tileset.get("firstgid")
        source = tileset.get("source")
        if firstgid is None:
            continue
        firstgid = int(firstgid)
        if not source:
            continue
        tsx_path = os.path.join(tileset_dir, source)
        if not os.path.isfile(tsx_path):
            raise FileNotFoundError(f"Missing tileset: {tsx_path}")
        local_ids = read_road_tile_ids(tsx_path)

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


def load_properties(path):
    values = {}
    if not os.path.isfile(path):
        return values
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or "=" not in line:
                continue
            key, value = line.split("=", 1)
            if "," not in key:
                continue
            x_str, y_str = key.split(",", 1)
            try:
                x = int(x_str)
                y = int(y_str)
                values[(x, y)] = int(value)
            except ValueError:
                continue
    return values


def update_road_properties(tmx_path, out_path, road_gids):
    tree = ET.parse(tmx_path)
    root = tree.getroot()
    width = int(root.get("width"))
    height = int(root.get("height"))

    values = load_properties(out_path)

    for layer in root.findall("layer"):
        rows = iter_layer_rows(layer)
        if len(rows) != height:
            raise ValueError("Layer row count does not match map height.")
        for row_idx, row in enumerate(rows):
            if len(row) != width:
                raise ValueError("Layer column count does not match map width.")
            y = height - 1 - row_idx
            for x, gid in enumerate(row):
                base_gid = gid & FLIP_MASK
                if base_gid != 0 and base_gid in road_gids:
                    if values.get((x, y), 0) == 0:
                        values[(x, y)] = 11

    with open(out_path, "w", encoding="utf-8") as f:
        for y in range(height):
            for x in range(width):
                f.write(f"{x},{y}={values.get((x, y), 0)}\n")


def main():
    parser = argparse.ArgumentParser(description="Mark road tiles as 11 in level properties based on road tilesets.")
    parser.add_argument("--tmx", default="assets/Assets_Map/THE_MAP.tmx", help="Path to TMX map")
    parser.add_argument("--tilesets", default="assets/Assets_Map", help="Directory containing TSX tilesets")
    parser.add_argument("--out", default="maps/level-1.properties", help="Output properties file")
    args = parser.parse_args()

    road_gids = build_global_road_gids(args.tmx, args.tilesets)
    update_road_properties(args.tmx, args.out, road_gids)
    return 0


if __name__ == "__main__":
    sys.exit(main())