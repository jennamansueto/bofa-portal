# Prompt: modernize the BofA Online Banking Transfers portal

> Paste everything below the line into a new Devin session.

---

I want you to modernize a legacy online-banking application end to end and produce a demo-ready result. Work autonomously; don't ask me for permission to start child sessions — use them wherever the plan below calls for them, and use the `managing-child-sessions` skill.

## The legacy system

Repo: https://github.com/jennamansueto/bofa-portal.git (branch `main` once merged; otherwise the open PR branch)

It is a Bank of America–style portal — public landing page with login, then an authenticated **Transfer Money** flow — built on an end-of-life stack: Apache Struts 1.3.10, JSP 2.1 / Servlet 2.5, Java 1.7 language level on JDK 8, Tomcat 7, jQuery 1.7.2 with IE7/IE8 CSS, HSQLDB 1.8 standing in for a DB2 for z/OS schema, hand-written JDBC DAOs, log4j 1.2, unsalted MD5 passwords. `README.md` explains how to run it (`./run.sh`, demo login `demo.user` / `Password1`); `docs/RESEARCH.md` explains the stack.

The business rules (tier-based fees and limits, same-day vs. next-business-day delivery, cutoff times, bank holidays, savings withdrawal limits, balance holds, confirmation numbers, login lockout, session expiry) live only in Java code and SQL seed data. Nobody has a written spec. Recovering that behavior and proving the new system preserves it is the point of this exercise.

## Target

Rebuild the same product on a modern, supported stack. Default to **Java 21 + Spring Boot 3 (REST) + PostgreSQL (Testcontainers/Docker for tests, docker-compose for local run)** on the back end and **React 18 + TypeScript + Vite** on the front end, with **Playwright** for acceptance tests. If the legacy analysis gives you a strong reason to deviate, do so and say why.

Put the new system in `<MODERN_REPO_URL>` (if I didn't fill this in, create a `modern/` directory on a feature branch of the legacy repo and tell me). Never push to `main`; use feature branches and open PRs for me to review.

The new UI must keep the Bank of America look and feel from the legacy JSPs (navy/red palette, FDIC bar, product nav, blue gradient hero, white panels) but implemented properly: responsive, accessible, no tables-for-layout. Polished, not gimmicky — no giant green checkmarks or "PASS" badges in the product UI itself.

## Plan — this is the shape I want to see

### Phase 0 — you (parent), before any child starts
1. Clone the legacy repo, run it with `./run.sh`, and **record screen recording #1: the legacy system running.** Walk through: landing page → login → Transfer Money page → change tier/amount/accounts and watch the summary update → submit an internal transfer → confirmation page with updated balances → start an external transfer to the Chase account and show the fee/delivery date for a Standard-tier customer → trigger at least one validation error (e.g. same From/To account). Narrate briefly with annotations. Keep it under 3 minutes.
2. Write a short `docs/00-plan.md` describing the child-session fan-out below, and attach it to the session.

### Phase 1 — three child sessions **in parallel** (analysis; read-only against the legacy repo)
Start all three at once. Each gets the legacy repo URL and a precise brief, and each must attach its markdown deliverable to its own session *and* return it to you.

* **Child A — Business-rules analyst.** Read `TransferService`, `BusinessCalendar`, `TransferDAO`, `CustomerDAO`, `AuthFilter`, `schema.sql`, `seed.sql`. Produce `docs/01-acceptance-criteria.md`: a numbered list (`AC-01`, `AC-02`, …) of Given/When/Then acceptance criteria covering **every** rule in the legacy code, each with a pointer to the legacy source line(s) it was derived from and the concrete seeded values that make it testable (accounts, balances, tiers, fee amounts, limits, holiday dates, cutoff hour, confirmation-number format, lockout count, session timeout, redirect targets). Include the JSON contract of `/secure/quote.do`. Aim for completeness over brevity — I expect 25–40 criteria.
* **Child B — UI/UX analyst.** Read the JSPs, `olb.css`, `olb.js`, and the images. Produce `docs/02-ui-spec.md`: page inventory, component inventory, design tokens (colors, type, spacing, radii), every user-visible string, every form field with its validation/behaviour, the AJAX summary behaviour, and the accessibility problems in the legacy markup that the rebuild must fix. Include screenshots of the running legacy app.
* **Child C — Data & platform analyst.** Read `schema.sql`, `seed.sql`, the DAOs, `pom.xml`, `web.xml`. Produce `docs/03-data-and-platform.md`: target PostgreSQL schema (proper types, money as `NUMERIC(15,2)` or integer cents — justify), the seed-data migration, the list of EOL components with their CVEs/EOL dates and what replaces each, and the security debt to retire (MD5, session handling, CSRF, no-cache headers) with the modern equivalent.

Wait for all three. Review them yourself for gaps (cross-check Child A's criteria against the legacy class comment in `TransferService` and the `OLB_FEE_SCHED` rows), fix anything missing, and attach the final three documents to this session.

### Phase 2 — child sessions **in sequence** (build; each depends on the previous)
Each child gets the three Phase 1 documents plus the output of the previous child.

1. **Child D — Backend.** Spring Boot service implementing every `AC-xx` from `docs/01-acceptance-criteria.md`: auth (bcrypt, lockout), accounts, transfer quote, transfer submit, transfer history, business calendar with the same ET cutoff and holiday table, fee schedule table. Unit + integration tests named after the AC IDs they cover. OpenAPI spec published. Opens a PR.
2. **Child E — Frontend.** Starts only after D's PR is open and its API is runnable. React/TypeScript app implementing `docs/02-ui-spec.md` against D's OpenAPI contract: landing/login, Transfer Money, confirmation, error states. Opens a PR.
3. **Child F — Acceptance tests.** Starts only after E is runnable. Playwright suite where **each test is titled with its AC ID and criterion text** (e.g. `AC-07 Standard-tier next-business-day external transfer charges $3.00`), driving the real UI against the real backend and database. Also produces `docs/04-traceability.md`: AC ID → legacy source → modern test → status. Opens a PR.

### Phase 3 — you (parent), the finale
1. Bring the modern system up (docker-compose or equivalent) with the seeded demo data.
2. **Record screen recording #2: the new system running, tested against the acceptance criteria pulled from the old system.** Structure it as:
   * open `docs/01-acceptance-criteria.md` briefly so the viewer sees where the criteria came from (legacy file/line references visible);
   * run the Playwright suite in headed mode so AC-titled tests are visible executing against the new UI — show the full pass/fail summary at the end;
   * then manually drive the same flow as recording #1 in the new UI (login → transfer → confirmation → external quote → validation error) so the two recordings are directly comparable.
   Keep it under 5 minutes.
3. Attach to this session: both recordings, the four `docs/*.md` files, and a one-page `docs/05-summary.md` with: before/after stack table, number of acceptance criteria recovered and passing, which child session did what (with session links), and anything in the legacy behavior you deliberately chose *not* to preserve (e.g. MD5, 10-minute session) and why.
4. Reply to me with: links to every PR, links to every child session, the two recordings, and a 5-line summary. Be succinct.

## Ground rules
* Don't merge anything to `main`.
* Attach every markdown artifact to the session so I can read it rendered in the webapp; tell children to do the same.
* If the legacy app's behaviour is ambiguous, the legacy code is the source of truth — preserve it and note it in the traceability doc rather than "fixing" it silently (except for the security items Child C flags).
* Keep me posted with short non-blocking updates at each phase boundary; only block if you need a real decision from me.
