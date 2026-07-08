#!/usr/bin/env python3
"""Rebuild Cursor Agent sidebar index from local agent-transcripts (.jsonl).

After a Cursor reinstall/update, the UI history list can be empty even though
transcripts still exist under ~/.cursor/projects/<slug>/agent-transcripts/.

This script merges transcript folders back into:
  - globalStorage/state.vscdb -> composer.composerHeaders (allComposers)
  - workspaceStorage/<hash>/state.vscdb -> composer.composerData (allComposers)
  - globalStorage/state.vscdb -> composerHeaders table (when present)

Default mode is dry-run. Pass --apply to write changes (Cursor must be closed).
"""

from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import sqlite3
import sys
from datetime import datetime
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Set, Tuple
from urllib.parse import quote, unquote, urlparse


DEFAULT_WORKSPACE_PATH = r"D:\cloud-erp-backend"
CURSOR_PROCESS_NAMES = ("Cursor.exe", "cursor.exe")
TIMESTAMP_RE = re.compile(
    r"<timestamp>\s*(.*?)\s*</timestamp>", re.IGNORECASE | re.DOTALL
)
USER_QUERY_RE = re.compile(
    r"<user_query>\s*(.*?)\s*</user_query>", re.IGNORECASE | re.DOTALL
)


def eprint(*args: object) -> None:
    print(*args, file=sys.stderr)


def normalize_path(path: str) -> str:
    return os.path.normcase(os.path.normpath(os.path.abspath(path)))


def path_to_project_slug(workspace_path: str) -> str:
    drive, rest = os.path.splitdrive(normalize_path(workspace_path))
    drive_part = drive.replace(":", "").lower()
    rest_part = rest.replace("\\", "-").replace("/", "-").strip("-")
    if drive_part and rest_part:
        return f"{drive_part}-{rest_part}"
    return rest_part or drive_part


def default_cursor_user_dir() -> Path:
    appdata = os.environ.get("APPDATA")
    if not appdata:
        raise RuntimeError("APPDATA is not set; cannot locate Cursor user data.")
    return Path(appdata) / "Cursor" / "User"


def default_projects_dir() -> Path:
    return Path.home() / ".cursor" / "projects"


def cursor_is_running() -> bool:
    if os.name != "nt":
        return False
    try:
        import subprocess

        result = subprocess.run(
            [
                "tasklist",
                "/FI",
                "IMAGENAME eq Cursor.exe",
                "/FO",
                "CSV",
                "/NH",
            ],
            capture_output=True,
            text=True,
            check=False,
        )
        output = (result.stdout or "").strip()
        return "Cursor.exe" in output and "No tasks are running" not in output
    except Exception:
        return False


def build_workspace_identifier(workspace_hash: str, workspace_path: str) -> Dict[str, Any]:
    norm = normalize_path(workspace_path)
    drive, rest = os.path.splitdrive(norm)
    rest_win = rest.replace("/", "\\")
    fs_path = f"{drive.lower()}{rest_win}"
    external = "file:///" + quote(fs_path.replace("\\", "/"), safe="/:")
    path_uri = "/" + fs_path.replace("\\", "/")
    return {
        "id": workspace_hash,
        "uri": {
            "$mid": 1,
            "fsPath": fs_path,
            "_sep": 1,
            "external": external,
            "path": path_uri,
            "scheme": "file",
        },
    }


def uri_to_path(uri: str) -> str:
    if uri.startswith("file://"):
        parsed = urlparse(uri)
        path = unquote(parsed.path)
        if path.startswith("/") and len(path) > 2 and path[2] == ":":
            path = path[1:]
        return normalize_path(path)
    return normalize_path(uri)


def find_workspace_hash(cursor_user_dir: Path, workspace_path: str) -> str:
    target = normalize_path(workspace_path)
    storage = cursor_user_dir / "workspaceStorage"
    if not storage.is_dir():
        raise RuntimeError(f"workspaceStorage not found: {storage}")

    matches: List[Tuple[str, str]] = []
    for entry in storage.iterdir():
        if not entry.is_dir():
            continue
        workspace_json = entry / "workspace.json"
        if not workspace_json.is_file():
            continue
        try:
            data = json.loads(workspace_json.read_text(encoding="utf-8"))
        except Exception:
            continue
        folder = data.get("folder") or data.get("configPath") or ""
        folder_path = uri_to_path(folder)
        if folder_path == target:
            matches.append((entry.name, folder_path))

    if not matches:
        raise RuntimeError(
            f"No workspaceStorage entry found for {workspace_path}. "
            "Open the project once in Cursor, then rerun."
        )
    if len(matches) > 1:
        eprint("Warning: multiple workspaceStorage entries matched; using the first.")
    return matches[0][0]


def resolve_transcripts_dir(
    projects_dir: Path, workspace_path: str, override: Optional[str]
) -> Path:
    if override:
        path = Path(override)
        if not path.is_dir():
            raise RuntimeError(f"Transcripts dir not found: {path}")
        return path

    slug = path_to_project_slug(workspace_path)
    candidate = projects_dir / slug / "agent-transcripts"
    if candidate.is_dir():
        return candidate

    best: Optional[Tuple[int, Path]] = None
    if projects_dir.is_dir():
        for entry in projects_dir.iterdir():
            transcripts = entry / "agent-transcripts"
            if not transcripts.is_dir():
                continue
            count = sum(1 for p in transcripts.iterdir() if p.is_dir())
            if best is None or count > best[0]:
                best = (count, transcripts)

    if best and best[0] > 0:
        eprint(
            f"Warning: slug dir {candidate} not found; using {best[1]} ({best[0]} folders)."
        )
        return best[1]

    raise RuntimeError(
        f"Could not locate agent-transcripts for {workspace_path}. "
        f"Expected {candidate} or pass --transcripts-dir."
    )


def parse_jsonl_metadata(jsonl_path: Path) -> Tuple[str, str, int, int]:
    created_at = int(jsonl_path.stat().st_mtime * 1000)
    updated_at = created_at
    title = jsonl_path.parent.name[:8]
    subtitle = ""

    try:
        with jsonl_path.open("r", encoding="utf-8") as handle:
            for line in handle:
                line = line.strip()
                if not line:
                    continue
                try:
                    obj = json.loads(line)
                except json.JSONDecodeError:
                    continue
                role = obj.get("role")
                content = obj.get("message", {}).get("content", [])
                text_parts: List[str] = []
                for block in content:
                    if isinstance(block, dict) and block.get("type") == "text":
                        text_parts.append(block.get("text") or "")
                text = "\n".join(text_parts)
                if role == "user" and title == jsonl_path.parent.name[:8]:
                    ts_match = TIMESTAMP_RE.search(text)
                    if ts_match:
                        raw_ts = ts_match.group(1).strip()
                        for fmt in (
                            "%A, %b %d, %Y, %I:%M %p (UTC%z)",
                            "%A, %b %d, %Y, %I:%M %p (UTC+8)",
                        ):
                            try:
                                dt = datetime.strptime(raw_ts, fmt)
                                created_at = int(dt.timestamp() * 1000)
                                break
                            except ValueError:
                                pass
                    query_match = USER_QUERY_RE.search(text)
                    query = (
                        query_match.group(1).strip()
                        if query_match
                        else text.strip()
                    )
                    query = re.sub(r"\s+", " ", query)
                    if query:
                        title = query[:80]
                        subtitle = query[:160]
                if role == "assistant":
                    tool_names = []
                    for block in content:
                        if isinstance(block, dict) and block.get("type") == "tool_use":
                            name = block.get("name")
                            if name:
                                tool_names.append(str(name))
                    if tool_names:
                        subtitle = ", ".join(tool_names[:6])
                updated_at = max(updated_at, created_at)
    except OSError:
        pass

    updated_at = int(jsonl_path.stat().st_mtime * 1000)
    if not subtitle:
        subtitle = title[:160]
    return title, subtitle, created_at, updated_at


def discover_transcripts(transcripts_dir: Path) -> List[Dict[str, Any]]:
    rows: List[Dict[str, Any]] = []
    for entry in sorted(transcripts_dir.iterdir()):
        if not entry.is_dir():
            continue
        composer_id = entry.name
        if composer_id.startswith("empty-"):
            continue
        jsonl_path = entry / f"{composer_id}.jsonl"
        if not jsonl_path.is_file():
            continue
        title, subtitle, created_at, updated_at = parse_jsonl_metadata(jsonl_path)
        rows.append(
            {
                "composerId": composer_id,
                "name": title or composer_id[:8],
                "subtitle": subtitle,
                "createdAt": created_at,
                "lastUpdatedAt": updated_at,
            }
        )
    rows.sort(key=lambda item: item["lastUpdatedAt"], reverse=True)
    return rows


def load_json_value(conn: sqlite3.Connection, table: str, key: str) -> Any:
    cur = conn.cursor()
    cur.execute(f"SELECT value FROM {table} WHERE key = ?", (key,))
    row = cur.fetchone()
    if not row:
        return None
    raw = row[0]
    if isinstance(raw, (bytes, memoryview)):
        raw = bytes(raw).decode("utf-8", errors="replace")
    return json.loads(raw)


def save_json_value(conn: sqlite3.Connection, table: str, key: str, value: Any) -> None:
    payload = json.dumps(value, ensure_ascii=False, separators=(",", ":"))
    cur = conn.cursor()
    cur.execute(
        f"INSERT INTO {table}(key, value) VALUES(?, ?) "
        f"ON CONFLICT(key) DO UPDATE SET value = excluded.value",
        (key, payload),
    )


def table_exists(conn: sqlite3.Connection, name: str) -> bool:
    cur = conn.cursor()
    cur.execute(
        "SELECT 1 FROM sqlite_master WHERE type='table' AND name = ? LIMIT 1", (name,)
    )
    return cur.fetchone() is not None


def build_header(
    meta: Dict[str, Any],
    workspace_identifier: Dict[str, Any],
    template: Optional[Dict[str, Any]] = None,
) -> Dict[str, Any]:
    header = dict(template or {})
    header.update(
        {
            "type": "head",
            "composerId": meta["composerId"],
            "name": meta["name"],
            "subtitle": meta.get("subtitle") or meta["name"],
            "createdAt": meta["createdAt"],
            "lastUpdatedAt": meta["lastUpdatedAt"],
            "conversationCheckpointLastUpdatedAt": meta["lastUpdatedAt"],
            "unifiedMode": header.get("unifiedMode") or "agent",
            "forceMode": header.get("forceMode") or "edit",
            "hasUnreadMessages": False,
            "hasBlockingPendingActions": False,
            "hasPendingPlan": False,
            "isArchived": False,
            "isDraft": False,
            "isWorktree": False,
            "worktreeStartedReadOnly": False,
            "isSpec": False,
            "isProject": False,
            "isBestOfNSubcomposer": False,
            "numSubComposers": 0,
            "referencedPlans": [],
            "trackedGitRepos": [],
            "workspaceIdentifier": workspace_identifier,
        }
    )
    header.pop("workspace", None)
    return header


def build_workspace_entry(meta: Dict[str, Any]) -> Dict[str, Any]:
    return {
        "type": "head",
        "composerId": meta["composerId"],
        "createdAt": meta["createdAt"],
        "unifiedMode": "agent",
        "forceMode": "edit",
    }


def backup_file(path: Path, backup_dir: Path) -> Path:
    backup_dir.mkdir(parents=True, exist_ok=True)
    stamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    target = backup_dir / f"{path.name}.{stamp}.bak"
    shutil.copy2(path, target)
    return target


def merge_headers(
    existing: Iterable[Dict[str, Any]],
    discovered: Iterable[Dict[str, Any]],
    workspace_identifier: Dict[str, Any],
) -> Tuple[List[Dict[str, Any]], int]:
    by_id: Dict[str, Dict[str, Any]] = {}
    template: Optional[Dict[str, Any]] = None
    for item in existing:
        if not isinstance(item, dict):
            continue
        composer_id = item.get("composerId") or item.get("id")
        if not composer_id:
            continue
        by_id[str(composer_id)] = item
        if template is None and item.get("workspaceIdentifier"):
            template = item

    added = 0
    for meta in discovered:
        composer_id = meta["composerId"]
        if composer_id in by_id:
            current = by_id[composer_id]
            if not current.get("workspaceIdentifier"):
                current["workspaceIdentifier"] = workspace_identifier
            if not current.get("name"):
                current["name"] = meta["name"]
            continue
        by_id[composer_id] = build_header(meta, workspace_identifier, template)
        added += 1

    merged = list(by_id.values())
    merged.sort(
        key=lambda item: item.get("lastUpdatedAt") or item.get("createdAt") or 0,
        reverse=True,
    )
    return merged, added


def sync_composer_headers_table(
    conn: sqlite3.Connection,
    headers: List[Dict[str, Any]],
    workspace_hash: str,
) -> None:
    if not table_exists(conn, "composerHeaders"):
        return
    cur = conn.cursor()
    cur.execute("DELETE FROM composerHeaders")
    for index, header in enumerate(headers):
        composer_id = header.get("composerId")
        if not composer_id:
            continue
        cur.execute(
            """
            INSERT INTO composerHeaders(
              composerId, workspaceId, createdAt, lastUpdatedAt,
              isArchived, isSubagent, recency, checkpointAt, value
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                composer_id,
                workspace_hash,
                header.get("createdAt"),
                header.get("lastUpdatedAt"),
                1 if header.get("isArchived") else 0,
                0,
                index,
                header.get("conversationCheckpointLastUpdatedAt")
                or header.get("lastUpdatedAt"),
                json.dumps(header, ensure_ascii=False, separators=(",", ":")),
            ),
        )


def recover(
    workspace_path: str,
    transcripts_dir: Optional[str],
    apply: bool,
    reset_migration_flags: bool,
) -> int:
    if apply and cursor_is_running():
        eprint("ERROR: Cursor is still running. Quit Cursor completely, then rerun with --apply.")
        return 2

    cursor_user_dir = default_cursor_user_dir()
    projects_dir = default_projects_dir()
    workspace_hash = find_workspace_hash(cursor_user_dir, workspace_path)
    transcripts_path = resolve_transcripts_dir(
        projects_dir, workspace_path, transcripts_dir
    )
    workspace_identifier = build_workspace_identifier(workspace_hash, workspace_path)
    discovered = discover_transcripts(transcripts_path)

    global_db = cursor_user_dir / "globalStorage" / "state.vscdb"
    workspace_db = cursor_user_dir / "workspaceStorage" / workspace_hash / "state.vscdb"
    if not global_db.is_file():
        raise RuntimeError(f"Global state DB not found: {global_db}")
    if not workspace_db.is_file():
        raise RuntimeError(f"Workspace state DB not found: {workspace_db}")

    conn_global = sqlite3.connect(f"file:{global_db}?mode=ro", uri=True)
    existing_headers_payload = load_json_value(conn_global, "ItemTable", "composer.composerHeaders")
    existing_workspace_payload = None
    conn_global.close()

    conn_workspace = sqlite3.connect(f"file:{workspace_db}?mode=ro", uri=True)
    existing_workspace_payload = load_json_value(
        conn_workspace, "ItemTable", "composer.composerData"
    )
    conn_workspace.close()

    existing_headers: List[Dict[str, Any]] = []
    if isinstance(existing_headers_payload, dict):
        existing_headers = existing_headers_payload.get("allComposers") or []
    elif isinstance(existing_headers_payload, list):
        existing_headers = existing_headers_payload

    merged_headers, added = merge_headers(
        existing_headers, discovered, workspace_identifier
    )
    discovered_ids = {item["composerId"] for item in discovered}
    existing_ids = {
        str(item.get("composerId") or item.get("id"))
        for item in existing_headers
        if isinstance(item, dict)
    }

    workspace_data = dict(existing_workspace_payload or {})
    workspace_data["allComposers"] = [
        build_workspace_entry(meta)
        for meta in sorted(
            discovered,
            key=lambda item: item["lastUpdatedAt"],
            reverse=True,
        )
    ]
    if reset_migration_flags:
        workspace_data["hasMigratedComposerData"] = False
        workspace_data["hasMigratedMultipleComposers"] = False

    selected_ids = workspace_data.get("selectedComposerIds")
    if not isinstance(selected_ids, list):
        selected_ids = workspace_data.get("selectedComposerId")
        if isinstance(selected_ids, str):
            selected_ids = [selected_ids]
    if not isinstance(selected_ids, list):
        selected_ids = []
    selected_ids = [item for item in selected_ids if item in discovered_ids]
    if not selected_ids and discovered:
        selected_ids = [discovered[0]["composerId"]]
    workspace_data["selectedComposerIds"] = selected_ids[:8]
    workspace_data["lastFocusedComposerIds"] = list(workspace_data["selectedComposerIds"])

    print("Cursor Agent history recovery")
    print(f"  Workspace path     : {workspace_path}")
    print(f"  Workspace hash     : {workspace_hash}")
    print(f"  Transcripts dir    : {transcripts_path}")
    print(f"  Transcript folders : {len(discovered)}")
    print(f"  Existing headers   : {len(existing_ids)}")
    print(f"  Headers after merge: {len(merged_headers)}")
    print(f"  New headers added  : {added}")
    print(f"  Global DB          : {global_db}")
    print(f"  Workspace DB       : {workspace_db}")
    print(f"  Mode               : {'APPLY' if apply else 'DRY-RUN'}")
    print("")
    print("Recent conversations to index:")
    for meta in discovered[:10]:
        ts = datetime.fromtimestamp(meta["lastUpdatedAt"] / 1000).strftime(
            "%Y-%m-%d %H:%M"
        )
        print(f"  {ts}  {meta['composerId'][:8]}...  {meta['name'][:70]}")

    if not apply:
        print("")
        print("Dry-run only. Re-run with --apply after fully quitting Cursor.")
        return 0

    backup_dir = cursor_user_dir / "backups" / "agent-history-recovery"
    backup_global = backup_file(global_db, backup_dir)
    backup_workspace = backup_file(workspace_db, backup_dir)
    print("")
    print(f"Backup global DB   : {backup_global}")
    print(f"Backup workspace DB: {backup_workspace}")

    conn_global = sqlite3.connect(str(global_db))
    try:
        save_json_value(
            conn_global,
            "ItemTable",
            "composer.composerHeaders",
            {"allComposers": merged_headers},
        )
        sync_composer_headers_table(conn_global, merged_headers, workspace_hash)
        conn_global.commit()
    finally:
        conn_global.close()

    conn_workspace = sqlite3.connect(str(workspace_db))
    try:
        save_json_value(
            conn_workspace, "ItemTable", "composer.composerData", workspace_data
        )
        conn_workspace.commit()
    finally:
        conn_workspace.close()

    print("")
    print("Recovery write completed.")
    print("Next steps:")
    print("  1. Launch Cursor")
    print(f"  2. Open {workspace_path}")
    print("  3. Open Agents panel and check history sections (Today / Last 7 days / Archived)")
    print("If some threads open blank, the transcript exists but composer body may need a new chat resume.")
    return 0


def parse_args(argv: Optional[List[str]] = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Rebuild Cursor Agent sidebar history from local agent-transcripts."
    )
    parser.add_argument(
        "--workspace-path",
        default=os.environ.get("CURSOR_RECOVERY_WORKSPACE", DEFAULT_WORKSPACE_PATH),
        help=f"Project folder path (default: {DEFAULT_WORKSPACE_PATH})",
    )
    parser.add_argument(
        "--transcripts-dir",
        default=os.environ.get("CURSOR_RECOVERY_TRANSCRIPTS_DIR"),
        help="Override agent-transcripts directory",
    )
    parser.add_argument(
        "--apply",
        action="store_true",
        help="Write changes to state.vscdb (default: dry-run)",
    )
    parser.add_argument(
        "--reset-migration-flags",
        action="store_true",
        help="Set workspace hasMigratedComposerData/hasMigratedMultipleComposers to false",
    )
    return parser.parse_args(argv)


def main(argv: Optional[List[str]] = None) -> int:
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(encoding="utf-8")

    args = parse_args(argv)
    try:
        return recover(
            workspace_path=args.workspace_path,
            transcripts_dir=args.transcripts_dir,
            apply=args.apply,
            reset_migration_flags=args.reset_migration_flags,
        )
    except Exception as exc:
        eprint(f"ERROR: {exc}")
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
