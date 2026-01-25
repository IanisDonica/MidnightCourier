#!/usr/bin/env python3
import argparse
import glob
import os
import re


def read_file(path):
    with open(path, "r", encoding="utf-8") as f:
        return f.read()


def parse_map_size(tmx_text):
    m = re.search(r'<map[^>]*\bwidth="(\d+)"[^>]*\bheight="(\d+)"', tmx_text)
    if not m:
        raise ValueError("TMX map width/height not found")
    return int(m.group(1)), int(m.group(2))


def parse_csv_layer(tmx_text):
    m = re.search(r'<data\s+encoding="csv">\s*(.*?)\s*</data>', tmx_text, re.S)
    if not m:
        raise ValueError("TMX CSV data block not found")
    csv_text = m.group(1)
    parts = [p.strip() for p in csv_text.replace("\n", ",").split(",") if p.strip() != ""]
    return [int(p) for p in parts]


def load_existing_keys(path):
    return set()


def append_entries(path, entries):
    if not entries:
        return
    needs_newline = False
    if os.path.exists(path):
        with open(path, "rb") as f:
            if f.seek(0, os.SEEK_END) > 0:
                f.seek(-1, os.SEEK_END)
                needs_newline = f.read(1) not in (b"\n", b"\r")
    with open(path, "a", encoding="utf-8") as f:
        if needs_newline:
            f.write("\n")
        for line in entries:
            f.write(line)
            f.write("\n")


def build_property_entries(width, height, gids, existing_keys):
    entries = []
    for row in range(height):
        for col in range(width):
            idx = row * width + col
            gid = gids[idx]
            if gid <= 0:
                continue
            x = col
            y = height - 1 - row
            key = f"{x},{y}"
            entries.append(f"{key}={gid + 100}")
    return entries


def main():
    parser = argparse.ArgumentParser(description="Append TMX tiles into property files as value=gid+100.")
    parser.add_argument("--tmx", default="assets/Assets_Map/THE_MAP.tmx", help="Path to TMX file")
    parser.add_argument("--properties-glob", default="maps/*.properties", help="Glob for property files")
    args = parser.parse_args()

    tmx_text = read_file(args.tmx)
    width, height = parse_map_size(tmx_text)
    gids = parse_csv_layer(tmx_text)
    if len(gids) != width * height:
        raise ValueError(f"CSV tile count {len(gids)} != width*height {width*height}")

    for path in sorted(glob.glob(args.properties_glob)):
        existing_keys = load_existing_keys(path)
        entries = build_property_entries(width, height, gids, existing_keys)
        append_entries(path, entries)
        print(f"{path}: appended {len(entries)} entries")


if __name__ == "__main__":
    main()
