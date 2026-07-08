#!/usr/bin/env python3
"""Patch @Digits annotations per precision upgrade rules."""
import re
import os
import sys

ROOT = os.path.join(os.path.dirname(__file__), "..", "erp-model")


def transform_digits(match: re.Match) -> str:
    old = match.group(0)
    i, fr = int(match.group(1)), int(match.group(2))
    rest = match.group(3)
    new_i = 18 if i <= 18 else i
    new_fr = 6 if fr < 6 else fr
    if new_i == i and new_fr == fr:
        return old
    new = f"@Digits(integer = {new_i}, fraction = {new_fr}{rest})"
    if "message" in rest:
        if new_i != i:
            new = re.sub(r"整数位不能超过\d+位", f"整数位不能超过{new_i}位", new)
        if new_fr != fr:
            new = re.sub(r"小数位不能超过\d+位", f"小数位不能超过{new_fr}位", new)
            new = re.sub(r"小数位不能大于\d+位", f"小数位不能大于{new_fr}位", new)
            new = re.sub(r"小数位不能大于\d+个字符", f"小数位不能大于{new_fr}个字符", new)
    return new


def main() -> int:
    pattern = re.compile(
        r"@Digits\s*\(\s*integer\s*=\s*(\d+)\s*,\s*fraction\s*=\s*(\d+)([^)]*)\)"
    )
    changed_files = 0
    changed_ann = 0
    for dirpath, _, files in os.walk(ROOT):
        for name in files:
            if not name.endswith(".java"):
                continue
            path = os.path.join(dirpath, name)
            with open(path, encoding="utf-8") as fh:
                text = fh.read()

            ann_before = len(pattern.findall(text))

            def counter(m: re.Match) -> str:
                nonlocal changed_ann
                result = transform_digits(m)
                if result != m.group(0):
                    changed_ann += 1
                return result

            new_text = pattern.sub(counter, text)
            if new_text != text:
                with open(path, "w", encoding="utf-8", newline="\n") as fh:
                    fh.write(new_text)
                changed_files += 1
    print(f"@Digits: {changed_ann} annotations updated in {changed_files} files")
    return 0


if __name__ == "__main__":
    sys.exit(main())
