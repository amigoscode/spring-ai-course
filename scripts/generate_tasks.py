#!/usr/bin/env python3
"""Generate the student (main) branch from the solution branch.

Usage: python3 scripts/generate_tasks.py <solution-dir> <output-dir>

Markers, written in the comment style of the file (//, # or <!-- -->):

    // SOLUTION-START task-3
    return chatClient.prompt().user(question).call().content();
    // SOLUTION-ELSE
    // TASK: // TODO task-3: send the question and return the answer
    // TASK: throw new UnsupportedOperationException("TODO task-3");
    // SOLUTION-END

Everything between SOLUTION-START and SOLUTION-ELSE (or SOLUTION-END) is removed.
"TASK:" lines are kept with the marker stripped, so the starter code compiles.
Paths listed in .solution-only are left out of the output entirely.
"""

import fnmatch
import re
import shutil
import sys
from pathlib import Path

SKIP_DIRS = {".git", "target", ".idea", ".vscode", "node_modules"}
SKIP_FILES = {".env", ".DS_Store", ".solution-only"}
TEXT_SUFFIXES = {".java", ".properties", ".yaml", ".yml", ".xml", ".md", ".st", ".sql", ".json", ".txt", ".http"}

COMMENT = r"(?://|#|<!--)"
START = re.compile(rf"^\s*{COMMENT}\s*SOLUTION-START\b")
ELSE = re.compile(rf"^\s*{COMMENT}\s*SOLUTION-ELSE\b")
END = re.compile(rf"^\s*{COMMENT}\s*SOLUTION-END\b")
TASK = re.compile(rf"^(\s*){COMMENT} ?TASK: ?(.*?)(\s*-->)?$")


def strip(text: str, path: Path) -> str:
    out, state = [], "code"
    for number, line in enumerate(text.splitlines(keepends=True), 1):
        where = f"{path}:{number}"
        if START.match(line):
            if state != "code":
                sys.exit(f"{where}: SOLUTION-START inside another block")
            state = "solution"
        elif ELSE.match(line):
            if state != "solution":
                sys.exit(f"{where}: SOLUTION-ELSE without SOLUTION-START")
            state = "task"
        elif END.match(line):
            if state == "code":
                sys.exit(f"{where}: SOLUTION-END without SOLUTION-START")
            state = "code"
        elif state == "code":
            out.append(line)
        elif state == "task":
            match = TASK.match(line)
            if not match:
                sys.exit(f"{where}: lines after SOLUTION-ELSE must start with a TASK: comment")
            out.append(match.group(1) + match.group(2) + "\n")
    if state != "code":
        sys.exit(f"{path}: block not closed with SOLUTION-END")
    return "".join(out)


def main() -> None:
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    src, dst = Path(sys.argv[1]).resolve(), Path(sys.argv[2]).resolve()

    only_file = src / ".solution-only"
    solution_only = []
    if only_file.exists():
        solution_only = [l.strip() for l in only_file.read_text().splitlines() if l.strip() and not l.startswith("#")]

    # Clear the output but keep its git history.
    dst.mkdir(parents=True, exist_ok=True)
    for child in dst.iterdir():
        if child.name == ".git":
            continue
        shutil.rmtree(child) if child.is_dir() and not child.is_symlink() else child.unlink()

    for path in sorted(src.rglob("*")):
        rel = path.relative_to(src)
        if any(part in SKIP_DIRS for part in rel.parts) or path.name in SKIP_FILES or path.is_dir():
            continue
        if any(fnmatch.fnmatch(rel.as_posix(), pattern) for pattern in solution_only):
            continue
        target = dst / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        if path.suffix in TEXT_SUFFIXES or path.name in {"mvnw", ".gitignore", ".gitattributes"}:
            target.write_text(strip(path.read_text(), rel))
        else:
            shutil.copy2(path, target)
        target.chmod(path.stat().st_mode)

    leftovers = [p for p in dst.rglob("*") if p.is_file() and ".git" not in p.parts
                 and p.suffix in TEXT_SUFFIXES and "SOLUTION-" in p.read_text()]
    # The generator itself and its docs mention the markers on purpose.
    leftovers = [p for p in leftovers if p.name not in {"generate_tasks.py", "CONTRIBUTING.md"}]
    if leftovers:
        sys.exit(f"Solution markers left in: {leftovers}")
    print(f"Generated tasks from {src} into {dst}")


if __name__ == "__main__":
    main()
