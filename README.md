# bofa-portal — Online Banking Transfers (legacy)

Replica of the Bank of America public landing page and the authenticated
**Transfer Money** flow, built deliberately on an end-of-life stack so it can
serve as the "before" system in a legacy-to-modern migration demo.

| Layer | Technology | Status |
|---|---|---|
| Web framework | Apache Struts **1.3.10** (ActionForm / Action / struts-config.xml) | EOL Dec 2008 |
| View | JSP 2.1 + Struts taglibs + JSTL 1.2, scriptlets | legacy |
| Servlet container | Apache Tomcat **7.0.109**, Servlet 2.5 | EOL Mar 2021 |
| Language | Java compiled `-source 1.7 -target 1.7`, runs on JDK 8 | Java 7 EOL 2015 |
| Browser | jQuery **1.7.2**, IE7/IE8 compatibility CSS, table layouts | EOL |
| Data | HSQLDB **1.8.0.10** in-memory (dev mirror of a DB2 for z/OS schema), Commons DBCP 1.4, hand-written JDBC DAOs | legacy |
| Logging | log4j **1.2.17** | EOL Aug 2015 |
| Auth | Unsalted MD5 passwords, 3-strike lockout, 10 min session | legacy |

See [docs/RESEARCH.md](docs/RESEARCH.md) for why this stack was chosen.

## Run it

Prereqs: JDK 8, Maven 3, Tomcat 7.0.x unpacked somewhere (default `~/tools/apache-tomcat-7.0.109`).

```sh
./run.sh                 # builds target/olb.war, deploys as ROOT, starts Tomcat on :8080
# CATALINA_HOME=/path/to/tomcat7 PORT=9090 ./run.sh
```

Open http://localhost:8080/ and sign in with **`demo.user` / `Password1`**.

## What's in the app

* `/index.jsp` — public landing page: FDIC bar, product nav, login panel, credit-card hero, cookie notice.
* `/login.do` — Struts `LoginAction`; failed logins increment `OLB_CUST.FAIL_CNT`, 3 failures lock the ID.
* `/secure/transfer.do` — Transfer Money page: balances, recent activity, *Make a transfer* form, live transfer summary.
* `/secure/quote.do` — jQuery AJAX endpoint returning a hand-built JSON quote (fee, total, delivery date).
* `/secure/transferSubmit.do` → `/secure/transferConfirm.do` — submit + confirmation page.

Business rules live in `com.bankofamerica.olb.service.TransferService` and
`BusinessCalendar`, and in the `OLB_FEE_SCHED` / `OLB_BANK_HOL` tables
(`src/main/resources/sql/`). They cover relationship-tier pricing and limits,
same-day internal posting, next-business-day / 3-business-day external delivery
with an 8 pm ET cutoff and bank holidays, savings withdrawal limits, available
balance holds, and daily confirmation-number sequencing.

Seeded demo data: Jordan Reyes (Preferred Rewards Gold), checking `...1001`
$4,215.38, savings `...1002` $12,940.00, one linked external Chase account
`...4432`, plus prior transfer history.

## Layout

```
pom.xml                         WAR build (Maven), Struts 1.3.10, servlet 2.5
run.sh                          build + deploy to Tomcat 7
src/main/java/com/bankofamerica/olb/
  action/     Struts Actions (Login, TransferView, TransferQuote, TransferSubmit, TransferConfirm)
  form/       Struts ActionForms
  dao/        JDBC DAOs + ConnectionFactory (DBCP)
  service/    TransferService, BusinessCalendar, AuthService
  model/      Customer, Account, Transfer
  util/       Money (long cents)
  web/        StartupListener, AuthFilter
src/main/resources/sql/         schema.sql, seed.sql (DB2-style DDL)
src/main/webapp/
  index.jsp, WEB-INF/jsp/*.jsp, WEB-INF/struts-config.xml, WEB-INF/web.xml
  css/olb.css  js/olb.js (jQuery 1.7.2)  images/
```
