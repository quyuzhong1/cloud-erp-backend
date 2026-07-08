#!/usr/bin/env python3
"""Force Cursor to refresh its local model catalog cache.

GPT/Claude/Gemini availability is decided by Cursor servers (account + egress IP).
This script only clears stale local cache so Cursor re-fetches the catalog on next launch.
It cannot bypass regional restrictions by itself.
"""

from __future__ import annotations

import argparse
import json
import os
import shutil
import sqlite3
import sys
from datetime import datetime
from pathlib import Path
from typing import Any, Dict, List, Tuple


APP_USER_KEY = (
    "src.vs.platform.reactivestorage.browser.reactiveStorageServiceImpl."
    "persistentStorage.applicationUser"
)
CACHE_KEYS = [
    "cursorai/serverConfig",
    "cursorai/featureConfigCache",
    "cursor/initialModelState",
    "workbench.experiments.statsigBootstrap",
]


def eprint(*args: object) -> None:
    print(*args, file=sys.stderr)


def cursor_user_dir() -> Path:
    appdata = os.environ.get("APPDATA")
    if not appdata:
        raise RuntimeError("APPDATA is not set.")
    return Path(appdata) / "Cursor" / "User"


def cursor_is_running() -> bool:
    if os.name != "nt":
        return False
    import subprocess

    result = subprocess.run(
        ["tasklist", "/FI", "IMAGENAME eq Cursor.exe", "/FO", "CSV", "/NH"],
        capture_output=True,
        text=True,
        check=False,
    )
    output = (result.stdout or "").strip()
    return "Cursor.exe" in output and "No tasks are running" not in output


def load_json(conn: sqlite3.Connection, table: str, key: str) -> Any:
    cur = conn.cursor()
    cur.execute(f"SELECT value FROM {table} WHERE key = ?", (key,))
    row = cur.fetchone()
    if not row:
        return None
    raw = row[0]
    if isinstance(raw, (bytes, memoryview)):
        raw = bytes(raw).decode("utf-8", errors="replace")
    return json.loads(raw)


def save_json(conn: sqlite3.Connection, table: str, key: str, value: Any) -> None:
    payload = json.dumps(value, ensure_ascii=False, separators=(",", ":"))
    cur = conn.cursor()
    cur.execute(
        f"INSERT INTO {table}(key, value) VALUES(?, ?) "
        f"ON CONFLICT(key) DO UPDATE SET value = excluded.value",
        (key, payload),
    )


def delete_key(conn: sqlite3.Connection, table: str, key: str) -> bool:
    cur = conn.cursor()
    cur.execute(f"DELETE FROM {table} WHERE key = ?", (key,))
    return cur.rowcount > 0


def summarize_models(app_user: Dict[str, Any]) -> Tuple[int, List[str]]:
    models = app_user.get("availableDefaultModels2") or []
    names = [str(item.get("name", "")) for item in models if isinstance(item, dict)]
    gpt = [name for name in names if "gpt" in name.lower() or "codex" in name.lower()]
    return len(names), gpt


def backup_file(path: Path, backup_dir: Path) -> Path:
    backup_dir.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    target = backup_dir / f"{path.name}.{stamp}.bak"
    shutil.copy2(path, target)
    return target


def refresh(apply: bool) -> int:
    if apply and cursor_is_running():
        eprint("ERROR: Cursor is still running. Quit Cursor completely, then rerun with --apply.")
        return 2

    db_path = cursor_user_dir() / "globalStorage" / "state.vscdb"
    if not db_path.is_file():
        raise RuntimeError(f"state.vscdb not found: {db_path}")

    conn = sqlite3.connect(f"file:{db_path}?mode=ro", uri=True)
    app_user = load_json(conn, "ItemTable", APP_USER_KEY) or {}
    before_count, before_gpt = summarize_models(app_user)
    conn.close()

    print("Cursor model catalog refresh")
    print(f"  DB path           : {db_path}")
    print(f"  Membership        : {app_user.get('membershipType')}")
    print(f"  Subscription      : {app_user.get('subscriptionStatus')}")
    print(f"  useOpenAIKey      : {app_user.get('useOpenAIKey')}")
    print(f"  Cached models     : {before_count}")
    print(f"  Cached GPT/Codex  : {before_gpt or '(none)'}")
    print(f"  Mode              : {'APPLY' if apply else 'DRY-RUN'}")
    print("")
    print("Will clear local cache keys:")
    for key in CACHE_KEYS:
        print(f"  - {key}")
    print("Will reset applicationUser.availableDefaultModels2 to []")

    if not apply:
        print("")
        print("Dry-run only. Re-run with --apply after fully quitting Cursor.")
        print("")
        print("After apply:")
        print("  1. Ensure global proxy exit is outside CN (Settings already has http.proxy=127.0.0.1:7890)")
        print("  2. Launch Cursor and sign out / sign in once")
        print("  3. Cursor Settings -> Models, search 'gpt' and enable toggles")
        print("  4. If still no GPT, use BYOK: Settings -> Models -> OpenAI API Key + Override Base URL")
        return 0

    backup_dir = cursor_user_dir() / "backups" / "model-catalog-refresh"
    backup = backup_file(db_path, backup_dir)
    print("")
    print(f"Backup created: {backup}")

    conn = sqlite3.connect(str(db_path))
    try:
        app_user = load_json(conn, "ItemTable", APP_USER_KEY) or {}
        app_user["availableDefaultModels2"] = []
        app_user["hasResetModelOnce"] = False
        save_json(conn, "ItemTable", APP_USER_KEY, app_user)
        removed = []
        for key in CACHE_KEYS:
            if delete_key(conn, "ItemTable", key):
                removed.append(key)
        conn.commit()
    finally:
        conn.close()

    print("Removed cache keys:", ", ".join(removed) if removed else "(none)")
    print("")
    print("Refresh write completed.")
    print("Next steps:")
    print("  1. Launch Cursor (keep proxy enabled)")
    print("  2. Sign out, then sign in again")
    print("  3. Open Cursor Settings -> Models and search 'gpt'")
    print("  4. Turn off Auto in Agent and pick a GPT model if it appears")
    print("")
    print("If GPT still does not appear, your account catalog is region-filtered.")
    print("Use BYOK (OpenAI-compatible API key + base URL) to add custom GPT models.")
    return 0


def main() -> int:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(encoding="utf-8")

    parser = argparse.ArgumentParser(description="Refresh Cursor model catalog cache.")
    parser.add_argument("--apply", action="store_true", help="Write changes (Cursor must be closed)")
    args = parser.parse_args()
    try:
        return refresh(args.apply)
    except Exception as exc:
        eprint(f"ERROR: {exc}")
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
