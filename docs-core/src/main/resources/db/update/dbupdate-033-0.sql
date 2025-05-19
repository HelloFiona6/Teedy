-- -- DBUPDATE-033-0.SQL

-- -- Create table for document translations
-- create cached table T_DOCUMENT_TRANSLATION (
--   DTR_ID_C varchar(36) not null,
--   DOC_ID_C varchar(36) not null,
--   LANG_C varchar(10) not null,
--   TRANSLATED_TEXT_C clob not null,
--   USER_ID_C varchar(36),
--   CREATE_DATE_D datetime not null,
--   primary key (DTR_ID_C)
-- );

-- -- Update the database version
-- update T_CONFIG set CFG_VALUE_C = '33' where CFG_ID_C = 'DB_VERSION'; 