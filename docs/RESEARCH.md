# Why this stack: BofA legacy technology research

Short summary of what is publicly known about Bank of America's legacy
estate, and how each finding maps to a choice in this replica. Nothing here
claims to describe BofA's actual online-banking source code; the goal is a
*plausible* legacy system for a modernization demo.

## Public findings

| Finding | Source (public) | What we did with it |
|---|---|---|
| Decades of M&A (NationsBank, FleetBoston, MBNA, Countrywide, Merrill Lynch) left BofA with many duplicate back-end systems; a multi-year "infrastructure transformation" program was launched to consolidate them. | BankTech / Network World coverage of BofA's infrastructure consolidation | Schema and comments reference a host system of record (`OLBP01` on DB2 for z/OS) and ticket IDs for long-deferred remediation (`OLB-2211` MD5 replacement). |
| Core deposit and card systems run on IBM mainframes (z/OS, COBOL/CICS, DB2). BofA is repeatedly cited among the largest mainframe shops in US banking. | Industry reporting on bank mainframe estates; BofA job postings for COBOL/CICS/DB2 | Money stored as BIGINT cents mirroring `COMP-3 S9(13)V99`; DB2-style table names (`OLB_CUST`, `OLB_ACCT`, `OLB_XFR`); status codes `P/S/R`; tier codes `00/10/20/30`. |
| Mid-2000s Java web tier: WebSphere Application Server, Struts 1.x, JSP, home-grown JDBC frameworks. Online banking of that era (bankofamerica.com `/myaccounts/*.go`, `*.do` URLs) is a classic Struts 1 MVC pattern. | Public URL patterns, job postings for WebSphere/Struts/JSP, Java EE adoption timeline in US banks | Apache Struts **1.3.10** with `*.do` mappings, `ActionForm`s, `struts-config.xml`, JSP scriptlets, Servlet 2.5. Tomcat 7 stands in for WebSphere 6/7 locally; `ConnectionFactory` notes the JNDI datasource it replaces. |
| Browser support for IE7/IE8 persisted in bank web apps far longer than elsewhere. | Historical bank browser-support matrices | `X-UA-Compatible` meta tag, table-based layout, vendor-prefixed CSS, jQuery **1.7.2** (last line supporting IE6-8 before 2.x). |
| log4j 1.x remained widespread in bank Java apps until the 2021 Log4Shell scramble forced inventories. | CISA / vendor advisories, 2021 | log4j **1.2.17** with `log4j.properties`. |
| Legacy password storage (unsalted MD5/SHA-1) is a recurring finding in bank modernization assessments. | OWASP, public breach post-mortems | `AuthService.md5Hex`, explicitly flagged in code as legacy debt. |

## EOL / support status of what is used here

| Component | Version | Status |
|---|---|---|
| Apache Struts 1 | 1.3.10 | End of life 5 Dec 2008 (CVE-2014-0114 unpatched upstream) |
| Apache Tomcat 7 | 7.0.109 | End of life 31 Mar 2021 |
| Java language level | 1.7 | Public updates ended Apr 2015 |
| Servlet / JSP | 2.5 / 2.1 | Superseded 2009 |
| jQuery | 1.7.2 | 1.x line EOL; known XSS CVEs (CVE-2012-6708, CVE-2015-9251) |
| HSQLDB | 1.8.0.10 | 1.8 line unmaintained since 2010 |
| Commons DBCP | 1.4 | Superseded by DBCP2 in 2014 |
| log4j | 1.2.17 | End of life Aug 2015 (CVE-2019-17571, CVE-2021-4104) |
| MD5 password hashing | – | Deprecated for credentials since ~2005 |

## What a modernization should be able to extract from this code

The app is small but carries real, testable behavior that lives only in code
and seed data — the kind of "tribal knowledge" a migration has to recover:

* `TransferService` — 10 numbered rules in the class comment, implemented in `quote()`/`submit()`.
* `BusinessCalendar` — ET cutoff hour, weekend/holiday rolling, business-day arithmetic.
* `OLB_FEE_SCHED` — fee + per-transfer + daily limit matrix by transfer type × relationship tier.
* `OLB_BANK_HOL` — 2026/2027 Federal Reserve holidays.
* `TransferDAO.nextConfirmationNumber` — `XFRyyMMdd-nnnnnn`, sequence resets daily.
* `CustomerDAO.recordLogin` — 3 failures → `STAT_CD='L'`.
* `AuthFilter` — unauthenticated `/secure/*` → `/index.jsp?expired=1`, no-cache headers.
* `TransferQuoteAction` — the ad-hoc JSON contract consumed by `js/olb.js`.
