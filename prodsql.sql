
SELECT * from users where  email in ('hgomez@sagiss.com',
'rmerriman@sagiss.com',
'cmccorkle@sagiss.com',
'Victoria.Worley@vmghealth.com',
'ipryseski@sagiss.com',
'dingram@mapf.com',
'shaun.confer@jpi.com',
'jrichards@sw-construction.com',
'jferreri@sagiss.com',
'jloudermilk@sagiss.com',
'bfang@sagiss.com',
'landon.campbell@jpi.com',
'lisa.paddack@jpi.com',
'procureit@sagiss.com');

SELECT id from tenant_user where  email in ('hgomez@sagiss.com',
'rmerriman@sagiss.com',
'cmccorkle@sagiss.com',
'Victoria.Worley@vmghealth.com',
'ipryseski@sagiss.com',
'dingram@mapf.com',
'shaun.confer@jpi.com',
'jrichards@sw-construction.com',
'jferreri@sagiss.com',
'jloudermilk@sagiss.com',
'bfang@sagiss.com',
'landon.campbell@jpi.com',
'lisa.paddack@jpi.com',
'procureit@sagiss.com');

SELECT * from tenant_teams_user where teams_user_id in  (SELECT teams_user_id from tenant_user_mapping WHERE tenant_user_id in (SELECT id from tenant_user where  email in ('hgomez@sagiss.com',
'rmerriman@sagiss.com',
'cmccorkle@sagiss.com',
'Victoria.Worley@vmghealth.com',
'ipryseski@sagiss.com',
'dingram@mapf.com',
'shaun.confer@jpi.com',
'jrichards@sw-construction.com',
'jferreri@sagiss.com',
'jloudermilk@sagiss.com',
'bfang@sagiss.com',
'landon.campbell@jpi.com',
'lisa.paddack@jpi.com',
'procureit@sagiss.com'))
);

SELECT * from tenant_user_mapping WHERE tenant_user_id in (SELECT id from tenant_user where  email in ('hgomez@sagiss.com',
'rmerriman@sagiss.com',
'cmccorkle@sagiss.com',
'Victoria.Worley@vmghealth.com',
'ipryseski@sagiss.com',
'dingram@mapf.com',
'shaun.confer@jpi.com',
'jrichards@sw-construction.com',
'jferreri@sagiss.com',
'jloudermilk@sagiss.com',
'bfang@sagiss.com',
'landon.campbell@jpi.com',
'lisa.paddack@jpi.com',
'procureit@sagiss.com'));


