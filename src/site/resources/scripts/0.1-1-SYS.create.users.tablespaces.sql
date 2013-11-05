----------------------------------------------------------------
-- connecte en SYS
----------------------------------------------------------------
-- creation des roles et users

create role R_ABS_ADM;
create role R_ABS_USR;
create role R_ABS_READ;

grant connect, create session, create table, create sequence, create public synonym to R_ABS_ADM;
grant connect, create session to R_ABS_USR;
grant connect, create session to R_ABS_READ;

create user ABS_ADM identified by PASSWORD_SECRET_SIE;
create user ABS_USR identified by PASSWORD_SECRET_SIE_2;
create user ABS_READ identified by PASSWORD_DONNER_AU_SED;

grant R_ABS_ADM to ABS_ADM;
grant R_ABS_USR to ABS_USR;
grant R_ABS_READ to ABS_READ;

grant unlimited tablespace to ABS_ADM;

----------------------------------------------------------------
-- Creation des tablespaces
-- /!\ LES NOMS DE FICHIERS SONT A DEFINIR PAR LE SIE /!\
-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
-- ATTENTION, BIEN RENOMMER LE NOM DU DATAFILE AFIN
--  QU'IL SOIT COHERENT AVEC LA BASE (eg :
-- RECETTE : ORADEV_TS_DEV.dbf -> SIRHR_TS_PARAM.dbf
-- PROD : ORADEV_TS_DEV.dbf -> SIRHP_TS_PARAM.dbf
-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
----------------------------------------------------------------

-- petit, prevoir des extends de 20 Mo, initial 20 Mo
CREATE TABLESPACE TS_ABS_PARAM DATAFILE
'E:\oradata\ORADEV\dbfusers\ORADEV_ts_dev.dbf'
SIZE 20M AUTOEXTEND ON NEXT 20M MAXSIZE 100M
LOGGING
ONLINE
PERMANENT
EXTENT MANAGEMENT LOCAL UNIFORM SIZE 512K
BLOCKSIZE 8K
SEGMENT SPACE MANAGEMENT AUTO
FLASHBACK OFF;


-- prevoir des extends de 100 Mo, initial 50 Mo
CREATE TABLESPACE TS_ABS_DATA DATAFILE
'E:\oradata\ORADEV\dbfusers\ORADEV_ts_dev.dbf'
SIZE 50M AUTOEXTEND ON NEXT 100M MAXSIZE 2000M
LOGGING
ONLINE
PERMANENT
EXTENT MANAGEMENT LOCAL UNIFORM SIZE 512K
BLOCKSIZE 8K
SEGMENT SPACE MANAGEMENT AUTO
FLASHBACK OFF;

-- tablespace à part pour la table des pointages qui va beaucoup grossir indépendamment des autres
-- prevoir des extends de 100 Mo, initial 50 Mo
CREATE TABLESPACE TS_ABS_BIG_DATA DATAFILE
'E:\oradata\ORADEV\dbfusers\ORADEV_ts_dev.dbf'
SIZE 50M AUTOEXTEND ON NEXT 100M MAXSIZE 2000M
LOGGING
ONLINE
PERMANENT
EXTENT MANAGEMENT LOCAL UNIFORM SIZE 512K
BLOCKSIZE 8K
SEGMENT SPACE MANAGEMENT AUTO
FLASHBACK OFF;

-- moyen, prevoir des extends de 100 Mo, initial 20 Mo
CREATE TABLESPACE TS_ABS_INDEX DATAFILE
'E:\oradata\ORADEV\dbfusers\ORADEV_ts_dev.dbf'
SIZE 20M AUTOEXTEND ON NEXT 100M MAXSIZE 2000M
LOGGING
ONLINE
PERMANENT
EXTENT MANAGEMENT LOCAL UNIFORM SIZE 512K
BLOCKSIZE 8K
SEGMENT SPACE MANAGEMENT AUTO
FLASHBACK OFF;


-- le plus petit possible, pas d'extend, bloque
CREATE TABLESPACE TS_ABS_DEFAULT DATAFILE
'E:\oradata\ORADEV\dbfusers\ORADEV_ts_dev.dbf'
SIZE 10M AUTOEXTEND OFF MAXSIZE 2000M
LOGGING
ONLINE
PERMANENT
EXTENT MANAGEMENT LOCAL UNIFORM SIZE 512K
BLOCKSIZE 8K
SEGMENT SPACE MANAGEMENT AUTO
FLASHBACK OFF;

alter tablespace TS_ABS_DEFAULT read only;


-- on redirige par defaut sur le tablespace USERS pour flagger les mises en recette sauvages...
alter user ABS_ADM default tablespace TS_ABS_DEFAULT;
