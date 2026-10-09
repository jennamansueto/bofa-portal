-- ============================================================
--  OLB-XFR  DDL  (dev mirror of DB2 for z/OS schema OLBP01)
--  Column naming follows corporate data dictionary std 2.3
--  All money columns are BIGINT cents (COMP-3 S9(13)V99 on host)
-- ============================================================

CREATE TABLE OLB_CUST (
    CUST_ID        INTEGER      NOT NULL PRIMARY KEY,
    OLB_USER_ID    VARCHAR(32)  NOT NULL,
    PSWD_HASH      CHAR(32)     NOT NULL,
    FIRST_NM       VARCHAR(40)  NOT NULL,
    LAST_NM        VARCHAR(40)  NOT NULL,
    REL_TIER_CD    CHAR(2)      DEFAULT '00' NOT NULL,
    LAST_LOGIN_TS  TIMESTAMP,
    FAIL_CNT       SMALLINT     DEFAULT 0 NOT NULL,
    STAT_CD        CHAR(1)      DEFAULT 'A' NOT NULL
);
CREATE UNIQUE INDEX OLB_CUST_UX1 ON OLB_CUST (OLB_USER_ID);

CREATE TABLE OLB_ACCT (
    ACCT_ID        VARCHAR(12)  NOT NULL PRIMARY KEY,
    CUST_ID        INTEGER      NOT NULL,
    ACCT_TYP_CD    CHAR(3)      NOT NULL,
    PROD_NM        VARCHAR(60)  NOT NULL,
    ACCT_NBR_LAST4 CHAR(4)      NOT NULL,
    CUR_BAL_CENTS  BIGINT       DEFAULT 0 NOT NULL,
    AVL_BAL_CENTS  BIGINT       DEFAULT 0 NOT NULL,
    EXT_BANK_NM    VARCHAR(60),
    SEQ_NO         SMALLINT     DEFAULT 0 NOT NULL,
    STAT_CD        CHAR(1)      DEFAULT 'A' NOT NULL
);

CREATE TABLE OLB_XFR (
    XFR_ID         INTEGER      IDENTITY,
    CONF_NBR       CHAR(16)     NOT NULL,
    CUST_ID        INTEGER      NOT NULL,
    FROM_ACCT_ID   VARCHAR(12)  NOT NULL,
    TO_ACCT_ID     VARCHAR(12)  NOT NULL,
    AMT_CENTS      BIGINT       NOT NULL,
    FEE_CENTS      BIGINT       DEFAULT 0 NOT NULL,
    XFR_TYP_CD     CHAR(3)      NOT NULL,
    REL_TIER_CD    CHAR(2)      NOT NULL,
    FREQ_CD        CHAR(1)      DEFAULT 'O' NOT NULL,
    SCHED_DT       DATE         NOT NULL,
    POST_DT        DATE         NOT NULL,
    STAT_CD        CHAR(1)      NOT NULL,
    MEMO           VARCHAR(60),
    CRT_TS         TIMESTAMP    NOT NULL,
    CRT_CHNL_CD    CHAR(3)      DEFAULT 'OLB' NOT NULL
);

-- Fee / limit matrix.  Maintained by Deposits Product via CICS screen OLBM04.
CREATE TABLE OLB_FEE_SCHED (
    XFR_TYP_CD       CHAR(3)   NOT NULL,
    REL_TIER_CD      CHAR(2)   NOT NULL,
    FEE_CENTS        BIGINT    NOT NULL,
    DAILY_LIM_CENTS  BIGINT    NOT NULL,
    PER_TXN_LIM_CENTS BIGINT   NOT NULL,
    EFF_DT           DATE      NOT NULL,
    PRIMARY KEY (XFR_TYP_CD, REL_TIER_CD)
);

CREATE TABLE OLB_BANK_HOL (
    HOL_DT   DATE        NOT NULL PRIMARY KEY,
    HOL_NM   VARCHAR(40) NOT NULL
);

CREATE TABLE OLB_CONF_SEQ (
    SEQ_DT   DATE    NOT NULL PRIMARY KEY,
    LAST_SEQ INTEGER NOT NULL
);
