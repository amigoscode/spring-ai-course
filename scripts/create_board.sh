#!/usr/bin/env bash
# Optional: put your task issues on a GitHub Projects board (kanban).
#
# Run "Set up my tasks" in the Actions tab first, so the issues exist. Then, on your machine:
#   gh auth refresh -s project
#   ./scripts/create_board.sh
set -euo pipefail

repo=$(gh repo view --json nameWithOwner -q .nameWithOwner)
owner=${repo%%/*}
title="Spring AI Course"

number=$(gh project list --owner "$owner" --format json -q ".projects[] | select(.title == \"$title\") | .number" | head -1)
if [[ -z "$number" ]]; then
  number=$(gh project create --owner "$owner" --title "$title" --format json -q .number)
  echo "Created project #$number"
fi
gh project link "$number" --owner "$owner" --repo "$repo" >/dev/null 2>&1 || true

existing=$(gh project item-list "$number" --owner "$owner" --limit 1000 --format json -q '.items[].content.url')
gh issue list --repo "$repo" --state all --limit 1000 --search "label:module-00,module-01,module-02,module-03,module-04,module-05,module-06,module-07,module-08,module-09,module-10,module-11,module-12,module-13" \
  --json url,title -q 'sort_by(.title) | .[].url' | while read -r url; do
  if ! grep -qx "$url" <<<"$existing"; then
    gh project item-add "$number" --owner "$owner" --url "$url" >/dev/null
    echo "Added $url"
  fi
done

echo
echo "Board ready: https://github.com/users/$owner/projects/$number"
echo "Tip: open it, click the view name and switch the layout to Board."
