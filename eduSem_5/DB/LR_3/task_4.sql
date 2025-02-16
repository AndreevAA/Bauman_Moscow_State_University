-- А. Четыре функции

-- 4) Рекурсивная функция или функция с рекурсивным ОТВ
-- Рекурсивный вывод информации о танках по их id
create or replace function recursive_hull_information(int, int)
returns setof military_vehicles.public.hull as
    $$
    begin
        if $1 < $2 then return query
            (
            select * from recursive_hull_information($1 + 1, $2)
        );
        end if;
        return query
            (
            select * from hull where hull_id = $1
        );
    end;
    $$
language 'plpgsql';
