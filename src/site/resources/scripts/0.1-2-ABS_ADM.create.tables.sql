----------------------------------------------------------------
-- connecte en ABS_ADM
----------------------------------------------------------------

--==============================================================
-- Table: ABS_AGENT_RECUP_COUNT
--==============================================================
create sequence ABS_S_AGENT_RECUP_COUNT 
start with 1 
increment by 1 
nomaxvalue;

create public synonym ABS_S_AGENT_RECUP_COUNT for ABS_S_AGENT_RECUP_COUNT;
grant select on ABS_S_AGENT_RECUP_COUNT to R_ABS_USR;

create table ABS_AGENT_RECUP_COUNT
(
    ID_AGENT_RECUP_COUNT NUMBER(38,0) not null,
    ID_AGENT NUMBER(7,0) not null,
    DATE_MODIFICATION TIMESTAMP not null,
    TOTAL_MINUTES NUMBER(38,0) default 0 not null,
    VERSION NUMBER(38,0) default 0 not null,
	constraint PK_ABS_AGENT_RECUP_COUNT
    primary key (ID_AGENT_RECUP_COUNT)
)
TABLESPACE TS_ABS_DATA;

create public synonym ABS_AGENT_RECUP_COUNT for ABS_AGENT_RECUP_COUNT;
grant select, insert, update, delete on ABS_AGENT_RECUP_COUNT to R_ABS_USR;
grant select on ABS_AGENT_RECUP_COUNT to R_ABS_READ;

--==============================================================
-- Table: ABS_AGENT_WEEK_RECUP
--==============================================================
create sequence ABS_S_AGENT_WEEK_RECUP 
start with 1 
increment by 1 
nomaxvalue;

create public synonym ABS_S_AGENT_WEEK_RECUP for ABS_S_AGENT_WEEK_RECUP;
grant select on ABS_S_AGENT_WEEK_RECUP to R_ABS_USR;

create table ABS_AGENT_WEEK_RECUP
(
    ID_AGENT_WEEK_RECUP NUMBER(38,0) not null,
    ID_AGENT NUMBER(7,0) not null,
    DATE_MONDAY DATE not null,
	DATE_MODIFICATION TIMESTAMP not null,
    MINUTES_RECUP NUMBER(38,0) default 0 not null,
    VERSION NUMBER(38,0) default 0 not null,
    constraint PK_ABS_AGENT_WEEK_RECUP
    primary key (ID_AGENT_WEEK_RECUP)
)
TABLESPACE TS_ABS_DATA;

create public synonym ABS_AGENT_WEEK_RECUP for ABS_AGENT_WEEK_RECUP;
grant select, insert, update, delete on ABS_AGENT_WEEK_RECUP to R_ABS_USR;
grant select on ABS_AGENT_WEEK_RECUP to R_ABS_READ;