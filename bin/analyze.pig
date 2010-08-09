
rows = LOAD 'cassandra://Summit/Demo' USING CassandraStorage()
     AS (key:bytearray, cols:bag{col:tuple(name:bytearray, value:bytearray)});
cols = FOREACH rows GENERATE key, flatten(cols) AS (name, value);
regnumcols = FILTER cols BY name == 'regnum';

-- dump counts of regnums
regnums = GROUP regnumcols BY value;
regnumcounts = FOREACH regnums GENERATE group, COUNT(regnumcols);
DUMP regnumcounts;

-- store sorted regnums
regnums = FOREACH regnumcols GENERATE value, key;
orderedregnums = ORDER regnumcols BY value;
STORE orderedregnums INTO 'summarized';

