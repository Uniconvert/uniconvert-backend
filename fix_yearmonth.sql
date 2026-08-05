UPDATE budget
SET `year_month` = CONCAT(SUBSTRING(`year_month`,1,4), '-', SUBSTRING(`year_month`,5,2))
WHERE `year_month` NOT LIKE '%-%';
