---
name: cloud-erp-java-review
description: >-
  Strict, evidence-based Java backend code review for the Cloud ERP Maven
  multi-module project. Use when Codex is asked to review an MR, commit, staged
  diff, or code change in this repository, especially Spring Boot/Spring
  Cloud/MyBatis-Plus backend changes, security fixes, config changes,
  database/SQL changes, Feign/RocketMQ/transaction changes, or requests that
  require the exact Chinese Markdown review report format.
---

# Cloud ERP Java Review

## Purpose

Perform a strict, practical code review of **only the current diff**. Prioritize defects that can block merge: security, data permission, SQL injection, transaction boundaries, compile/runtime regressions, module boundary violations, and missing verification.

Use this skill together with **karpathy-guidelines**: be evidence-based, keep scope surgical, and avoid inventing issues outside the submitted diff.

## Review Scope

- Review only changed files and changed behavior in the diff.
- Do not scan the whole repository unless needed to understand a changed symbol, annotation pattern, or local convention.
- Do not report pre-existing unrelated problems unless the diff makes them worse or depends on them.
- Use concrete file paths and line numbers from the changed files.
- If MR metadata fields are placeholders or absent, proceed from the available diff.

## Collect Context

Prefer these commands depending on what is available:

```bash
git status --short
git diff --stat
git diff --cached --stat
git diff HEAD~1..HEAD --stat
git show --stat --oneline HEAD
```

For review detail:

```bash
git diff -- <path>
git diff --cached -- <path>
git show --format=fuller -- <path>
```

Read only the changed files or immediately adjacent code needed to verify a finding. Use `rg` for targeted lookups.

Branch vs target:

```bash
git diff origin/{target_branch}...HEAD
git diff --name-only origin/{target_branch}...HEAD
```

For Code Review GPT (`http://172.16.100.13:3000/reviews/{id}`), fetch `GET http://172.16.100.13:8001/api/webhook/reviews/{id}/`, then cross-check with the actual git diff.

## Workflow

1. Obtain diff only (commands above).
2. Read changed files — enough surrounding context for imports, signatures, and local patterns.
3. Walk the [High-Signal Checklist](reference.md); skip dimensions with no applicable changes.
4. Report findings using the mandatory output format below.
5. Score and conclude (merge recommendation).

## Project Rules To Apply

- Java 8, Maven, Spring Boot/Spring Cloud, Nacos, Seata, RocketMQ, PostgreSQL, Redis/Redisson, XXL-JOB, MyBatis/MyBatis-Plus, Lombok, FastJSON/FastJSON2, MapStruct, EasyExcel.
- `erp-model-*`: Entity/DTO/VO/Enums only; no business logic or Spring Bean injection.
- `erp-rpc-*`: Feign/RPC contracts.
- `erp-server-*`: business implementation.
- `erp-common-*`: reusable common capabilities, not single-domain private logic.
- Controller should inherit `BaseController` and return `ApiResult<T>` where applicable.
- Entity is data-layer object, DTO is input, VO is output; cross-system transfer uses VO.
- Business exceptions use `ServiceException`; Feign exceptions use `FeignServiceException`.
- CRUD services typically extend `SuperService<Entity>` and `SuperServiceImpl<Mapper, Entity>`.
- Mapper CRUD typically extends MyBatis-Plus `BaseMapper<Entity>`.
- Prefer MyBatis-Plus wrappers for simple queries; put complex dynamic SQL in mapper XML.
- Avoid new response structures, exception systems, ORM styles, or unapproved frameworks.

Full checklist: [reference.md](reference.md).

## Severity And Score

| Level | When to use |
|-------|-------------|
| **严重** | Security exploit, data leak, auth/data-permission bypass, SQL injection, data corruption, broken startup/compile, irreversible destructive behavior |
| **高** | Significant behavior regression, missing transaction/idempotency for critical writes, high-probability runtime failure, major performance issue |
| **中** | Correctness, maintainability, incomplete validation, weaker error handling, smaller performance issue |
| **低** | Local cleanup, minor compatibility, naming, logging clarity, small maintainability issue |

### 低风险默认不处理（Skip Low Severity By Default）

- 生成审查报告时，默认**不输出**「低」风险条目；问题列表只保留 严重 / 高 / 中。
- 处理外部 review（如 Code Review GPT）时，默认**不修复**「低」风险条目，也不补"误报"注释；修复范围聚焦 严重 / 高 / 中。
- 例外：用户在当次会话中**明确要求**「低风险也处理 / 全部处理 / 修干净」时，再纳入「低」风险项；这种例外仅对当次任务生效，不更新本规则。
- 「低」风险项不计入"合并前必须修复"，也不影响合并建议。

> 与 `code-review-gpt` 规则中"低优先级问题也应一并修复"的措辞冲突时，以本规则为准（更新优先级）。

| Score | Meaning |
|-------|---------|
| 90–100 | Excellent, no blocking issue |
| 80–89 | Good, minor non-blocking issues |
| 60–79 | Needs improvement, at least one meaningful fix |
| Below 60 | Obvious merge risk |

Severity heavily penalizes score; a diff with zero issues and good patterns typically scores 85+.

## Mandatory Output Format

Always output **exactly** this Markdown structure unless the user asks for another format:

```markdown
# 代码审查报告

## 概述

- **变更摘要**：用 1-3 句话概括本次改动内容与影响范围
- **风险等级**：高 / 中 / 低（取问题中最高等级）
- **总分**：xx / 100

---

## 问题列表（按严重程度排序）

### 严重

**1. 问题标题（简明扼要）**
- 文件：`path/to/File.java:123`
- 问题：具体描述问题现象、原因及可能后果
- 修复建议：可执行的修改方案，必要时附代码片段

### 高

### 中

### 低

若无问题：

> 未发现需要修复的问题。

---

## 优点

- 列出本次变更做得好的地方；若无明显优点，写「无明显亮点」。

---

## 结论

- 是否建议合并：是 / 否 / 有条件合并
- 合并前必须修复的问题：列出编号或「无」
- 一句话总结
```

### Formatting Rules

- Order findings by severity (严重 → 高 → 中 → 低).
- Number findings consecutively across all groups.
- Do not nest bullets under 文件 / 问题 / 修复建议.
- Do not invent issues for pure formatting changes.
- Do not repeat the change summary outside 概述.
- Do not output content unrelated to the review.
- If no issues are found, say so clearly and mention residual verification risk if tests could not run.

### Merge Recommendation

| Recommendation | Condition |
|----------------|-----------|
| **是** | No 严重/高 issues; 中 issues acceptable or documented |
| **有条件合并** | Only 中/低 issues; no 严重; 高 issues have agreed workaround |
| **否** | Any 严重 issue, or multiple 高 issues affecting correctness/security |

Merge blockers: all **严重** and **高** issues listed under 合并前必须修复.
