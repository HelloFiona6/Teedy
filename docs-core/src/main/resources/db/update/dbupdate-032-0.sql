-- DBUPDATE-032-0.SQL

-- Create table for new user registration requests
create cached table T_USER_REGISTRATION_REQUEST (
  URR_ID_C varchar(36) not null,
  URR_USERNAME_C varchar(50) not null,
  URR_PASSWORD_C varchar(255) not null,
  URR_EMAIL_C varchar(100) not null,
  URR_CREATEDATE_D datetime not null,
  URR_STATUS_C varchar(20) not null, -- pending/accepted/rejected
  URR_ADMIN_COMMENT_C varchar(255),
  primary key (URR_ID_C)
);

-- Update the database version
update T_CONFIG set CFG_VALUE_C = '32' where CFG_ID_C = 'DB_VERSION'; 