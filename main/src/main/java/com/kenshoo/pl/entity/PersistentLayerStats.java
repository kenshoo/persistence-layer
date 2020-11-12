package com.kenshoo.pl.entity;

import com.kenshoo.pl.data.AffectedRows;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class PersistentLayerStats {

    private final Map<String, AffectedRows> tableStats = new HashMap<>();
    private long fetchTimeMillis = 0;
    private long updateTimeMillis = 0;
    private long auditLogTimeMillis = 0;

    public void addAffectedRows(String tableName, AffectedRows affectedRows) {
        tableStats.put(tableName, tableStats.getOrDefault(tableName, AffectedRows.empty()).plus(affectedRows));
    }

    public void addFetchTime(long elapsed) {
        fetchTimeMillis += elapsed;
    }

    public void addUpdateTime(long elapsed) {
        updateTimeMillis += elapsed;
    }

    public void addAuditLogTime(long elapsed) {
        auditLogTimeMillis += elapsed;
    }

    public PersistentLayerStats combine(PersistentLayerStats other) {
        PersistentLayerStats result = new PersistentLayerStats();
        Set<String> allTables = Stream.concat(getTablesAffected().stream(), other.getTablesAffected().stream()).collect(toSet());
        for (String table : allTables) {
            result.addAffectedRows(table, getAffectedRowsOf(table).plus(other.getAffectedRowsOf(table)));
        }
        result.addFetchTime(getFetchTime(TimeUnit.MILLISECONDS) + other.getFetchTime(TimeUnit.MILLISECONDS));
        result.addUpdateTime(getUpdateTime(TimeUnit.MILLISECONDS) + other.getUpdateTime(TimeUnit.MILLISECONDS));
        return result;
    }

    public Collection<String> getTablesAffected() {
        return tableStats.keySet();
    }

    public AffectedRows getAffectedRowsOf(String tableName) {
        return tableStats.getOrDefault(tableName, AffectedRows.empty());
    }

    public long getFetchTime(TimeUnit timeUnit) {
        return TimeUnit.MILLISECONDS.convert(fetchTimeMillis, timeUnit);
    }

    public long getUpdateTime(TimeUnit timeUnit) {
        return TimeUnit.MILLISECONDS.convert(updateTimeMillis, timeUnit);
    }

    public long getAuditLogTime(TimeUnit timeUnit) {
        return TimeUnit.MILLISECONDS.convert(auditLogTimeMillis, timeUnit);
    }
}
