from __future__ import annotations

import argparse
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

try:
    from fontTools import subset
    from fontTools.ttLib import TTFont
except ImportError as exc:
    raise SystemExit(
        "Missing dependency: fonttools. Install it with `python -m pip install fonttools`."
    ) from exc

PROJECT_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_SOURCE_FONT = PROJECT_ROOT / "scripts" / "font-source" / "NotoSansSC_VF.ttf"
DEFAULT_OUTPUT_FONT = (
    PROJECT_ROOT
    / "composeApp"
    / "src"
    / "wasmJsMain"
    / "composeResources"
    / "font"
    / "NotoSansSC_WasmSubset.ttf"
)
DEFAULT_STRING_FILES = (
    PROJECT_ROOT / "composeApp" / "src" / "commonMain" / "composeResources" / "values" / "strings.xml",
    PROJECT_ROOT / "composeApp" / "src" / "commonMain" / "composeResources" / "values-zh" / "strings.xml",
)
ASCII_PRINTABLE = "".join(chr(code_point) for code_point in range(0x20, 0x7F))


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate a wasm-only subset font from Compose string resources."
    )
    parser.add_argument(
        "--source-font",
        type=Path,
        default=DEFAULT_SOURCE_FONT,
        help="Path to the original font file.",
    )
    parser.add_argument(
        "--output-font",
        type=Path,
        default=DEFAULT_OUTPUT_FONT,
        help="Path of the generated subset font.",
    )
    parser.add_argument(
        "--strings",
        type=Path,
        nargs="+",
        default=list(DEFAULT_STRING_FILES),
        help="String resource XML files to scan.",
    )
    parser.add_argument(
        "--extra-chars",
        default="",
        help="Extra characters to force-include.",
    )
    return parser.parse_args()


def extract_strings(xml_file: Path) -> list[str]:
    tree = ET.parse(xml_file)
    root = tree.getroot()
    texts: list[str] = []
    for element in root.iter():
        if element.tag not in {"string", "item"}:
            continue
        if element.text:
            texts.append(element.text)
    return texts


def build_charset(string_files: list[Path], extra_chars: str) -> str:
    characters = set(ASCII_PRINTABLE)
    for string_file in string_files:
        for text in extract_strings(string_file):
            characters.update(text)
    characters.update(extra_chars)
    return "".join(sorted(characters))


def subset_font(source_font: Path, output_font: Path, text: str) -> None:
    options = subset.Options()
    options.ignore_missing_glyphs = True
    options.layout_features = ["*"]
    options.name_IDs = ["*"]
    options.name_languages = ["*"]
    options.notdef_outline = True
    options.recommended_glyphs = True
    options.symbol_cmap = True
    options.legacy_cmap = True
    options.hinting = False
    options.desubroutinize = True

    font = TTFont(source_font)
    subsetter = subset.Subsetter(options=options)
    subsetter.populate(text=text)
    subsetter.subset(font)

    output_font.parent.mkdir(parents=True, exist_ok=True)
    font.save(output_font)


def format_size(size: int) -> str:
    units = ("B", "KB", "MB", "GB")
    current = float(size)
    for unit in units:
        if current < 1024 or unit == units[-1]:
            return f"{current:.2f} {unit}"
        current /= 1024
    return f"{size} B"


def main() -> int:
    args = parse_args()
    source_font = args.source_font.resolve()
    output_font = args.output_font.resolve()
    string_files = [path.resolve() for path in args.strings]

    missing_files = [path for path in [source_font, *string_files] if not path.exists()]
    if missing_files:
        missing = "\n".join(f" - {path}" for path in missing_files)
        print(f"Missing required file(s):\n{missing}", file=sys.stderr)
        return 1

    charset = build_charset(string_files, args.extra_chars)
    subset_font(source_font, output_font, charset)

    source_size = source_font.stat().st_size
    output_size = output_font.stat().st_size
    reduction = 0.0 if source_size == 0 else (1 - output_size / source_size) * 100

    print(f"Source font : {source_font}")
    print(f"Output font : {output_font}")
    print(f"Glyph chars : {len(set(charset))}")
    print(f"Source size : {format_size(source_size)}")
    print(f"Output size : {format_size(output_size)}")
    print(f"Reduction   : {reduction:.2f}%")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
