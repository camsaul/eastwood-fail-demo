(ns etc.core)

(defprotocol ISQLDriver
  "Methods SQL-based drivers should implement in order to use `IDriverSQLDefaultsMixin`.
   Methods marked *OPTIONAL* have default implementations in `ISQLDriverDefaultsMixin`."

  (active-tables ^java.util.Set [this, ^java.sql.DatabaseMetaData metadata]
    "Return a set of maps containing information about the active tables/views, collections, or equivalent that currently exist in DATABASE.
     Each map should contain the key `:name`, which is the string name of the table. For databases that have a concept of schemas,
     this map should also include the string name of the table's `:schema`.")

  ;; The following apply-* methods define how the SQL Query Processor handles given query clauses. Each method is called when a matching clause is present
  ;; in QUERY, and should return an appropriately modified version of KORMA-QUERY. Most drivers can use the default implementations for all of these methods,
  ;; but some may need to override one or more (e.g. SQL Server needs to override the behavior of `apply-limit`, since T-SQL uses `TOP` instead of `LIMIT`).
  (apply-aggregation [this korma-query, ^Map query] "*OPTIONAL*.")
  (apply-breakout    [this korma-query, ^Map query] "*OPTIONAL*.")
  (apply-fields      [this korma-query, ^Map query] "*OPTIONAL*.")
  (apply-filter      [this korma-query, ^Map query] "*OPTIONAL*.")
  (apply-join-tables [this korma-query, ^Map query] "*OPTIONAL*.")
  (apply-limit       [this korma-query, ^Map query] "*OPTIONAL*.")
  (apply-order-by    [this korma-query, ^Map query] "*OPTIONAL*.")
  (apply-page        [this korma-query, ^Map query] "*OPTIONAL*.")

  (column->base-type ^clojure.lang.Keyword [this, ^clojure.lang.Keyword column-type]
    "Given a native DB column type, return the corresponding `Field` `base-type`.")

  (column->special-type ^clojure.lang.Keyword [this, ^String column-name, ^clojure.lang.Keyword column-type]
    "*OPTIONAL*. Attempt to determine the special-type of a field given the column name and native type.
     For example, the Postgres driver can mark Postgres JSON type columns as `:json` special type.")

  (connection-details->spec [this, ^Map details-map]
    "Given a `Database` DETAILS-MAP, return a JDBC connection spec.")

  (current-datetime-fn [this]
    "*OPTIONAL*. Korma form that should be used to get the current `DATETIME` (or equivalent). Defaults to `(k/sqlfn* :NOW)`.")

  (date [this, ^clojure.lang.Keyword unit, field-or-value]
    "Return a korma form for truncating a date or timestamp field or value to a given resolution, or extracting a date component.")

  (excluded-schemas ^java.util.Set [this]
    "*OPTIONAL*. Set of string names of schemas to skip syncing tables from.")

  (field->alias ^String [this, ^Field field]
    "*OPTIONAL*. Return the alias that should be used to for FIELD, i.e. in an `AS` clause. The default implementation calls `name`, which
     returns the *unqualified* name of `Field`.

     Return `nil` to prevent FIELD from being aliased.")

  (get-connection-for-sync ^java.sql.Connection [this details]
    "*OPTIONAL*. Get a connection used for a Sync step. By default, this returns a pooled connection.")

  (prepare-value [this, ^Value value]
    "*OPTIONAL*. Prepare a value (e.g. a `String` or `Integer`) that will be used in a korma form. By default, this returns VALUE's `:value` as-is, which
     is eventually passed as a parameter in a prepared statement. Drivers such as BigQuery that don't support prepared statements can skip this
     behavior by returning a korma `raw` form instead, or other drivers can perform custom type conversion as appropriate.")

  (set-timezone-sql ^String [this]
    "*OPTIONAL*. This should be a prepared JDBC SQL statement string to be used to set the timezone for the current transaction.

       \"SET @@session.timezone = ?;\"")

  (stddev-fn ^clojure.lang.Keyword [this]
    "*OPTIONAL*. Keyword name of the SQL function that should be used to do a standard deviation aggregation. Defaults to `:STDDEV`.")

  (string-length-fn ^clojure.lang.Keyword [this]
    "Keyword name of the SQL function that should be used to get the length of a string, e.g. `:LENGTH`.")

  (unix-timestamp->timestamp [this, field-or-value, ^clojure.lang.Keyword seconds-or-milliseconds]
    "Return a korma form appropriate for converting a Unix timestamp integer field or value to an proper SQL `Timestamp`.
     SECONDS-OR-MILLISECONDS refers to the resolution of the int in question and with be either `:seconds` or `:milliseconds`."))
