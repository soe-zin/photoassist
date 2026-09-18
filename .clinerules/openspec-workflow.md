# OpenSpec Development Workflow

## Source of Truth

Before starting any implementation work, or immediately upon session recovery/reconnection:

1. Read `openspec/requirements.md`.
2. Read `openspec/tasks.md`.
3. Check for `openspec/progress.md`:
   - If `openspec/progress.md` exists, read it immediately to verify recent activity and identify the exact next step.
   - If `openspec/progress.md` **does not exist**, create it in the `openspec/` directory using the specified template before proceeding.
4. Review recent conversation context.
5. Do not scan the entire project directory unless the active task explicitly requires it.

Treat these files as the primary source of project requirements, task tracking, and state continuity.

---

## Task & Progress Tracking

`openspec/tasks.md` is the authoritative task checklist using GitHub-style status checkboxes:

- `[ ]` Not started
- `[x]` Completed
- `[~]` In progress
- `[!]` Blocked

### Mandatory `openspec/progress.md` Management

- **Creation:** If `openspec/progress.md` does not exist when starting work or resuming execution, create `openspec/progress.md` immediately.
- **Real-Time Updates:** You MUST update `openspec/progress.md` in real-time whenever a task status changes (marked `[~]`, `[x]`, or `[!]`), or after executing significant tool actions/tests. Do not delay writing to `openspec/progress.md` until the end of the session.

#### Standard `openspec/progress.md` Format:
```markdown
# OpenSpec Progress Tracking

## Active Task
- Current task from `openspec/tasks.md` being executed.

## Status & Changes
- [~] Details on what was just completed, verified, or changed.
- Updated files or key implementation details.

## Next Step
- The exact next task or sub-step to pick up.

## Blockers / Notes (Optional)
- Any errors, missing dependencies, or constraints encountered.