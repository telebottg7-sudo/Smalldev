# Smalldev — One-by-One Fix Plan for Google AI Studio

## Purpose

This file is an implementation plan for Google AI Studio to turn Smalldev from a UI prototype into a real, working Android development environment.

**Important execution rule:** Do not fix all issues at once. Work through the tasks strictly in order. Complete exactly one task, run its tests, review the diff, and stop for approval before starting the next task.

Each task below contains:

- the problem;
- the files to inspect or modify;
- the required implementation;
- acceptance criteria;
- tests that must be added or run.

Do not redesign unrelated UI, rename the entire project, upgrade dependencies broadly, or implement multiple numbered tasks in one change.

---

## Current project status

The current terminal and project features are partly simulated:

- `apt`, `pip`, `npm`, `cargo`, `pkg`, and distro commands print placeholder output.
- Python, Node.js, C compilation, React, and PRoot commands do not run their real tools.
- AI agent commands print simulated analysis and patch results.
- Git clone creates fabricated files instead of cloning the requested repository.
- ZIP import and workspace file operations do not enforce safe path boundaries.
- The terminal can invoke Android `/system/bin/sh -c`, but it is not a real Ubuntu PRoot environment.

The goal is to replace simulated success with real execution, explicit failure reporting, and safe isolation.

---

# Phase 0 — Establish a safe baseline

## Task 0.1 — Make the repository buildable in AI Studio

**Files:**

- `gradlew`
- `.github/workflows/build-debug-apk.yml`
- `gradle/wrapper/gradle-wrapper.properties`
- optionally `README.md`

**Implementation:**

1. Commit the executable bit for `gradlew`.
2. Ensure the wrapper distribution URL is valid and reproducible.
3. Make CI validate the wrapper with `./gradlew --version` before building.
4. Ensure the AI Studio/CI environment configures an Android SDK and required SDK platforms.
5. Do not change application logic in this task.

**Acceptance criteria:**

- `./gradlew --version` works on Unix.
- `./gradlew testDebugUnitTest` starts without permission or SDK-location errors in the configured AI Studio environment.
- CI reports a clear failure if the SDK is unavailable.

**Tests:**

```bash
./gradlew --version
./gradlew testDebugUnitTest
```

**Stop after this task. Review the diff and build output before continuing.**

---

## Task 0.2 — Add a real baseline test structure

**Files:**

- `app/src/test/`
- `app/src/androidTest/`
- `app/build.gradle.kts`

**Implementation:**

Add test suites for the existing production boundaries without changing their behavior yet:

- `ShellExecutionService`
- `WorkspaceRepository`
- ZIP import
- path validation helpers once introduced
- Room database initialization

Remove or replace tests that only assert a hard-coded value equals itself. Keep tests small and deterministic.

**Acceptance criteria:**

- Tests fail when the relevant behavior is broken.
- Tests do not depend on network access.
- Tests run in CI.

**Stop after this task.**

---

# Phase 1 — Remove immediate security risks

## Task 1.1 — Remove and rotate exposed Telegram/session material

**Files:**

- `app/src/main/java/com/example/data/WorkspaceRepository.kt`
- `app/src/main/java/com/example/data/ShellExecutionService.kt`
- all Git history and generated project fixtures containing API/session values

**Implementation:**

1. Treat any real Telegram API hash, API ID, session string, or session database as compromised.
2. Rotate/revoke the values outside the repository before shipping.
3. Remove all credential-shaped values from source and Git history.
4. Replace the seeded sample with clearly fake placeholders that cannot authenticate.
5. Never print session strings automatically in terminal output.
6. Add secret scanning to CI.
7. Add redaction for known secret patterns in terminal output and logs.

**Acceptance criteria:**

- No real API hash, token, password, private key, or session string remains in the repository history or APK inputs.
- Running the sample project cannot output an account credential.
- CI fails when common credential patterns are introduced.

**Tests:**

- Repository-wide secret scan.
- Unit test that generated terminal output redacts sensitive values.

**Stop after this task.**

---

## Task 1.2 — Create one safe path-resolution helper

**Files:**

- Add a new repository utility, for example `app/src/main/java/com/example/data/SafePathResolver.kt`.
- Do not yet refactor every caller in this task.

**Implementation:**

Create a single tested function that:

1. accepts a project root and user/archive-relative path;
2. rejects null/blank paths where inappropriate;
3. rejects absolute paths;
4. normalizes separators;
5. rejects `..` traversal and invalid path components;
6. resolves the candidate using canonical paths;
7. verifies the result remains below the project root;
8. returns a safe `File` or a typed error.

Do not silently convert unsafe paths into another path.

**Acceptance criteria:**

The helper rejects:

- `../outside.txt`
- `../../../../data/file`
- `/absolute/path`
- `foo/../../outside`
- platform-specific separator traversal

It accepts normal paths such as:

- `README.md`
- `src/main.py`
- `.github/workflows/build.yml`

**Tests:**

Add table-driven tests for accepted and rejected paths.

**Stop after this task.**

---

## Task 1.3 — Fix ZIP import securely

**Files:**

- `app/src/main/java/com/example/data/WorkspaceRepository.kt`
- ZIP import tests

**Implementation:**

Refactor `extractZipStream()` to:

1. resolve every entry through the safe path helper;
2. reject traversal and absolute entries;
3. reject or safely handle symlink-like entries;
4. stream bytes instead of calling `readBytes()` for an unbounded entry;
5. enforce configurable limits:
   - maximum entries;
   - maximum uncompressed total bytes;
   - maximum bytes per file;
   - maximum directory depth;
6. extract into a temporary project directory;
7. only publish the project after extraction succeeds;
8. clean up the temporary directory on failure;
9. keep Room metadata consistent with physical files;
10. report a real import error instead of silently creating a fallback README after a security failure.

**Acceptance criteria:**

- A ZIP containing `../../outside.txt` is rejected.
- A ZIP containing an absolute path is rejected.
- A ZIP bomb or oversized archive is rejected before exhausting app storage/memory.
- Failed imports leave no partial project or orphaned database rows.
- Normal ZIP projects import successfully.

**Stop after this task.**

---

## Task 1.4 — Apply safe paths to create/save/rename/delete

**Files:**

- `app/src/main/java/com/example/data/WorkspaceRepository.kt`
- `app/src/main/java/com/example/viewmodel/IdeViewModel.kt`
- file manager UI only if input constraints are needed

**Implementation:**

Use the safe path helper for:

- `createFile()`;
- `saveFileContent()`;
- `renameFile()`;
- `deleteFile()`;
- `getFileContent()`;
- any future file operation.

Validate the project ID against a project record before accessing its directory. Treat names as a single path component; do not allow a file name to contain `/` or `\\`.

Make database and filesystem updates recoverable. Do not delete database records before confirming the physical operation can succeed.

**Acceptance criteria:**

- No file API can write, read, rename, or delete outside its project root.
- Rename failures do not silently lose database records.
- Traversal tests pass for every file operation.

**Stop after this task.**

---

## Task 1.5 — Define backup and privacy policy

**Files:**

- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`
- optional secure-storage implementation

**Implementation:**

Choose one explicit product policy:

- disable backup for private workspaces; or
- back up only user-approved project data while excluding credentials, caches, exports, temporary files, session databases, and shared runtime files.

Document the policy and test backup/restore behavior.

**Acceptance criteria:**

- Sensitive files cannot be backed up unintentionally.
- Cloud backup and device transfer rules are explicit.
- The policy is documented in the app/repository.

**Stop after this task.**

---

# Phase 2 — Make the terminal real and safe

## Task 2.1 — Choose the execution architecture before coding

**Files:**

- Add a design note under `docs/terminal-architecture.md`.
- Do not modify command behavior yet.

**Decision required:**

Choose one of these supported architectures:

### Option A: Real local runtime

Use a bundled or downloaded userspace runtime such as a properly managed PRoot/rootfs. Commands run inside that runtime, with a controlled workspace and environment.

### Option B: Safe command simulator

Keep simulation, but rename the UI and commands so users cannot mistake it for a real shell. Never report fake installs, builds, servers, or patches as successful.

### Option C: Android shell only

Expose Android shell functionality honestly, without calling it Ubuntu/PRoot, and restrict access to an approved workspace.

Do not implement a hybrid where some commands are fake and others are silently executed by Android shell.

**Acceptance criteria:**

- The architecture document states what runs where, what filesystem it can access, how packages are installed, how processes are stopped, and how errors are reported.
- Product/UI labels match the chosen architecture.

**Stop after this task.**

---

## Task 2.2 — Replace hard-coded command simulation with a command registry

**Files:**

- `app/src/main/java/com/example/data/ShellExecutionService.kt`
- add command model/registry classes
- update terminal tests

**Implementation:**

Create a typed command execution result containing:

- stdout;
- stderr;
- exit code;
- start/end timestamps;
- timed-out/cancelled state;
- process ID if applicable.

Remove fake success handlers one command family at a time. Do not keep a fake fallback for real commands.

For each command, choose explicitly:

- real executable with validated arguments;
- intentionally unsupported with a clear error;
- simulator command clearly labeled as simulated.

**Acceptance criteria:**

- The terminal never displays success unless the operation returned a successful verified result.
- Unsupported commands display an error, not a fabricated result.
- Exit code and stderr are visible.

**Stop after this task.**

---

## Task 2.3 — Implement process lifecycle controls

**Files:**

- `ShellExecutionService.kt`
- `IdeViewModel.kt`
- `TerminalView.kt`
- terminal model classes

**Implementation:**

1. Keep a reference to each running process/job.
2. Implement real cancellation for the Stop button.
3. Add timeout handling.
4. Kill child processes/process groups where supported.
5. Limit concurrent processes per terminal/workspace.
6. Limit stdout/stderr bytes and line count.
7. Report timeout, cancellation, and exit status explicitly.
8. Do not execute `^C` as a new shell command.

**Acceptance criteria:**

- A long-running command can be stopped.
- Stop changes the process state to cancelled.
- A hung process cannot keep the app busy indefinitely.
- Output limits prevent memory growth.

**Stop after this task.**

---

## Task 2.4 — Make working directories real

**Files:**

- `ShellExecutionService.kt`
- `IdeViewModel.kt`
- terminal session model

**Implementation:**

Store a canonical working directory per terminal session. Resolve `cd` safely within the allowed workspace. Pass that actual directory to each real process. Do not only change the displayed prompt.

**Acceptance criteria:**

```text
mkdir sub
cd sub
pwd
```

shows the actual workspace/subdirectory path, and a subsequent `ls` observes the same directory.

**Stop after this task.**

---

## Task 2.5 — Implement real package installation with verification

**Files:**

- `IdeViewModel.kt`
- `ShellExecutionService.kt`
- `PackageCatalog.kt`
- `WorkspaceRepository.kt`

**Implementation:**

For the selected runtime, invoke the real package manager inside that runtime. Do not mark a package installed after a delay. Mark it installed only if:

1. the command exits successfully;
2. the package manager confirms installation;
3. the installed version can be queried and recorded.

If the runtime is not available, show “runtime unavailable” rather than success.

**Acceptance criteria:**

- Failed installs do not add database records.
- Uninstall removes the actual package and only then updates UI state.
- Package versions shown in UI match the runtime.

**Stop after this task.**

---

# Phase 3 — Implement real project functionality

## Task 3.1 — Implement actual Git clone

**Files:**

- `WorkspaceRepository.kt`
- Git-related models/UI
- network/security tests

**Implementation:**

1. Validate repository URLs and allow only approved schemes (`https` and explicitly supported SSH handling).
2. Do not pass untrusted URLs into shell strings.
3. Clone into a temporary directory.
4. Verify clone success and checkout state.
5. Import/index the actual files into Room.
6. Keep `.git` consistent with the physical checkout.
7. Support cancellation and progress.
8. Clean up partial clones on failure.
9. Never create a fake `.git/config` or fabricated source files.

**Acceptance criteria:**

- A real public repository produces its actual files.
- Invalid URLs fail clearly.
- Network failures do not create a “successful” project.
- The UI displays the actual clone error.

**Stop after this task.**

---

## Task 3.2 — Make code execution real or clearly unsupported

**Files:**

- `ShellExecutionService.kt`
- `MainActivity.kt`
- `CodingAgentOption.kt`
- editor run actions

**Implementation:**

The Run button must either execute the selected file in the chosen real runtime or show an explicit unsupported-runtime error. Remove fake outputs such as “Hello from C!” and “Server listening” unless a real process produced them.

**Acceptance criteria:**

- Python code is executed by the configured Python runtime.
- Node code is executed by the configured Node runtime.
- C is compiled by a real compiler before reporting success.
- HTML preview is clearly separated from server execution.

**Stop after this task.**

---

## Task 3.3 — Implement AI calls honestly and safely

**Files:**

- `AiCodingService.kt`
- `IdeViewModel.kt`
- secret configuration
- AI panel models/tests

**Implementation:**

1. Use the Google AI Studio/Firebase AI integration configured for this app, or show a clear unavailable state.
2. Do not claim Claude, Aider, Kiro, Antigravity, or Gemini CLI execution unless their real binaries/API calls are available.
3. Separate generated suggestions from applied file changes.
4. Require user review before writing AI-generated changes.
5. Show API/network/model errors to the user.
6. Do not place API keys in source or APK constants.

**Acceptance criteria:**

- AI output identifies the actual model/provider used.
- Failed AI requests do not produce fabricated “patch applied” messages.
- Applying a patch shows a diff and supports undo.

**Stop after this task.**

---

# Phase 4 — Reliability and release hardening

## Task 4.1 — Replace destructive Room migration

**Files:**

- `AppDatabase.kt`
- add Room migration classes
- export Room schemas

**Implementation:**

Replace `fallbackToDestructiveMigration()` with explicit migrations. Add migration tests from every released schema version. Provide a user-visible recovery/export path for migration failure.

**Acceptance criteria:**

- Upgrading the app preserves projects and files.
- Migration tests pass.
- No normal app upgrade deletes user workspaces.

**Stop after this task.**

---

## Task 4.2 — Harden WebView preview

**Files:**

- `WebPreviewView.kt`
- `AndroidManifest.xml` if permissions change

**Implementation:**

1. Disable JavaScript unless a preview explicitly requires it.
2. Disable file/content access unless required.
3. Block or intercept external navigation.
4. Disable mixed content and unnecessary storage.
5. Do not add a JavaScript bridge without a separate security review.
6. Add a CSP to generated/local preview content where possible.

**Acceptance criteria:**

- Project HTML cannot navigate the app to arbitrary external pages without an explicit user action.
- Preview behavior is documented and tested.

**Stop after this task.**

---

## Task 4.3 — Harden release builds

**Files:**

- `app/build.gradle.kts`
- CI workflow
- ProGuard/R8 rules

**Implementation:**

1. Enable R8/minification after testing.
2. Fail release builds when signing credentials are missing.
3. Keep release keystores outside the repository.
4. Validate version/application identity.
5. Add dependency and secret scanning to CI.
6. Build and inspect the release APK in a clean environment.

**Acceptance criteria:**

- Release build succeeds only with intentional signing configuration.
- No secrets are included in the APK.
- CI runs unit tests, static checks, secret scanning, and release smoke checks.

**Stop after this task.**

---

# Required workflow for Google AI Studio

For every task:

1. Read only the task being implemented.
2. Inspect the current code before editing.
3. Make the smallest focused change.
4. Add or update tests for that task.
5. Run the task-specific tests.
6. Run formatting/static checks if available.
7. Review the diff for unrelated changes.
8. Update this file with a short completion note under the task.
9. Stop and wait for approval before starting the next numbered task.

Do not combine Task 1.3 with Task 2.2, Task 3.1 with Task 3.3, or any other tasks. Security fixes must be completed and tested before adding new runtime features.

## Completion log

- [ ] Task 0.1 — Buildable baseline
- [ ] Task 0.2 — Baseline tests
- [ ] Task 1.1 — Remove exposed credentials/session material
- [ ] Task 1.2 — Safe path resolver
- [ ] Task 1.3 — Secure ZIP import
- [ ] Task 1.4 — Safe workspace file operations
- [ ] Task 1.5 — Backup/privacy policy
- [ ] Task 2.1 — Terminal architecture decision
- [ ] Task 2.2 — Command registry and real results
- [ ] Task 2.3 — Process lifecycle controls
- [ ] Task 2.4 — Real working directories
- [ ] Task 2.5 — Real package installation
- [ ] Task 3.1 — Actual Git clone
- [ ] Task 3.2 — Real code execution
- [ ] Task 3.3 — Honest AI integration
- [ ] Task 4.1 — Room migrations
- [ ] Task 4.2 — WebView hardening
- [ ] Task 4.3 — Release hardening
