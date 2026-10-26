#!/usr/bin/env python3
"""Create one GitHub issue per task, one milestone per module, and module labels.

Reads every */TASKS.md in the repo. Safe to run more than once: issues, milestones
and labels that already exist are skipped.

Needs the GitHub CLI (gh). In GitHub Actions it uses the workflow token.
Locally: gh auth login, then python3 scripts/setup_tasks.py
"""

import json
import os
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
REPO = os.environ.get("GITHUB_REPOSITORY") or subprocess.run(
    ["gh", "repo", "view", "--json", "nameWithOwner", "-q", ".nameWithOwner"],
    capture_output=True, text=True, check=True).stdout.strip()
BRANCH = os.environ.get("TASKS_BRANCH", "main")


def gh(*args, input=None):
    result = subprocess.run(["gh", *args], capture_output=True, text=True, input=input)
    if result.returncode != 0:
        sys.exit(f"gh {' '.join(args)} failed:\n{result.stderr}")
    return result.stdout


def parse(tasks_file: Path):
    text = tasks_file.read_text()
    module = tasks_file.parent.name
    heading = re.search(r"^# (.+)$", text, re.M).group(1).strip()
    sections = re.split(r"^## ", text, flags=re.M)[1:]
    tasks = []
    for section in sections:
        title, _, body = section.partition("\n")
        match = re.match(r"Task (\d+): (.+)", title.strip())
        if match:
            tasks.append((int(match.group(1)), match.group(2).strip(), body.strip()))
    return module, heading, tasks


def main():
    files = sorted(ROOT.glob("[0-9][0-9]-*/TASKS.md"))
    if not files:
        sys.exit("No */TASKS.md files found")

    labels = {l["name"] for l in json.loads(gh("label", "list", "--repo", REPO, "--limit", "200", "--json", "name"))}
    milestones = {m["title"]: m["number"] for m in json.loads(
        gh("api", f"repos/{REPO}/milestones?state=all&per_page=100"))}
    issues = {i["title"] for i in json.loads(
        gh("issue", "list", "--repo", REPO, "--state", "all", "--limit", "1000", "--json", "title"))}

    created = 0
    for tasks_file in files:
        module, heading, tasks = parse(tasks_file)
        label = f"module-{module[:2]}"
        if label not in labels:
            gh("label", "create", label, "--repo", REPO, "--color", "6E56CF", "--description", heading)
            labels.add(label)
        if heading not in milestones:
            milestone = json.loads(gh("api", f"repos/{REPO}/milestones", "-f", f"title={heading}",
                                      "-f", f"description=Tasks for {module}. See {module}/TASKS.md"))
            milestones[heading] = milestone["number"]

        for number, title, body in tasks:
            issue_title = f"{module[:2]} · Task {number}: {title}"
            if issue_title in issues:
                continue
            link = f"https://github.com/{REPO}/blob/{BRANCH}/{module}/TASKS.md"
            full_body = (f"{body}\n\n---\n📄 Module: [{module}/TASKS.md]({link}) · "
                         f"Read the module [README](https://github.com/{REPO}/blob/{BRANCH}/{module}/README.md) first.\n\n"
                         f"Close this issue when the task is done, or put `closes #<this issue>` in your commit message.")
            gh("issue", "create", "--repo", REPO, "--title", issue_title, "--label", label,
               "--milestone", heading, "--body-file", "-", input=full_body)
            issues.add(issue_title)
            created += 1
            print(f"Created: {issue_title}")

    print(f"Done. {created} new issues in {REPO}.")


if __name__ == "__main__":
    main()
