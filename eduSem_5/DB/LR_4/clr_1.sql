-- Создать, развернуть и протестировать 6 объектов SQL CLR:

select * from pg_language;
-- 1) Определяемую пользователем скалярную функцию CLR,
-- Функция возвращает максимальную длину танка.
create or replace function max_cost_clr()
returns varchar as
$$
return plpy.execute("select hull_length "
                    "from hull "
                    "order by hull_length desc "
                    "limit 1")[0]['hull_length']
$$
language plpythonu;

-- select * from max_cos_clr();
--
-- -- 2) Пользовательскую агрегатную функцию CLR,
-- -- Вывести сумму мест по всем детским садам.
--
-- create or replace function sum(old int, new int)
-- returns int
-- as $$
--     return old + new
-- $$ language plpython2u;
--
-- CREATE AGGREGATE sum_places(int)
-- (
--     sfunc = sum,
--     stype = int,
--     initcond = 0
-- );
--
-- select sum_places(max_num_of_seats) from institution;
--
-- select sum(max_num_of_seats) from institution;
--
-- -- 3) Определяемую пользователем табличную функцию CLR,
-- -- Вывести информацию о родителях которые должны заплатить больше 1000 за обучение
--
-- create or replace function parents_debts(num int)
-- returns table(full_name text, phone text, address text, service_cost numeric)
-- as
-- $$
--     query = plpy.prepare("select full_name, phone, address, service_cost "
--                         "from contract C join parent P on C.id_parent = P.id_parent "
--                         "where C.service_cost >= $1", ['int'])
--     return plpy.execute(query, [num])
-- $$ language plpython2u;
--
-- select * from parents_debts(1000);
--
-- -- 4) Хранимую процедуру CLR,
-- -- Добавить всем работникам 1 день стажа
--
-- select *
-- into temp employee_temp
-- from employee;
--
-- select * from employee_temp order by id_employee;
--
-- create or replace procedure inc_experience_clr()
-- as
-- $$
--     plpy.execute("update employee_temp "
--                  "set experience = experience + interval '1 day'")
-- $$
-- language plpython2u;
--
-- call inc_experience_clr();
--
-- -- 5) Триггер CLR,
--
-- create temp table if not exists parent_temp
-- (
--  	full_name varchar(50) not null,
--  	gender nchar(3) not null,
-- 	num_after_this int not null
-- );
--
-- create or replace function inc_num_clr()
-- returns trigger AS
-- $$
--     plpy.execute("update parent_temp "
-- 	             "set num_after_this = num_after_this + 1")
-- $$
-- language plpython2u;
--
-- create trigger trigger_after_insert_clr
-- after insert on parent_temp for each row
-- execute function inc_num_clr();
--
--
-- select * from parent_temp order by num_after_this desc;
--
--
-- insert into parent_temp (full_name, gender, num_after_this)
-- values ('1', 'муж', -1);
-- insert into parent_temp (full_name, gender, num_after_this)
-- values ('2', 'муж', -1);
-- insert into parent_temp (full_name, gender, num_after_this)
-- values ('3', 'жен', -1);
-- insert into parent_temp (full_name, gender, num_after_this)
-- values ('4', 'муж', -1);
-- insert into parent_temp (full_name, gender, num_after_this)
-- values ('5', 'жен', -1);
--
-- -- 6) Определяемый пользователем тип данных CLR.
-- select * from employee;
--
-- create type emp_info
-- as
-- (
--     full_name text,
--     education text
-- );
--
-- create or replace function get_emp_info()
-- returns emp_info as
-- $$
--     query = plpy.execute("select full_name, education "
--                          "from employee "
--                          "order by experience desc")
--     return (query[0]['full_name'], query[0]['education'])
-- $$
-- language plpython2u;
--
-- select get_emp_info();
--
-- select * from employee order by experience desc;