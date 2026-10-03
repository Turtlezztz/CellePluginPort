package dk.setups.celle.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import dk.setups.celle.cell.log.CellLog;
import dk.setups.celle.cell.log.CellLogFilter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CellLogStore extends BaseStore<Integer, CellLog> {

    public CellLogStore(Dao<CellLog, Integer> dao, StoreManager stores, Logger logger) {
        super(dao, stores, logger);
    }

    public List<CellLog> getLogs(CellLogFilter filter, int limit, int skip) {
        try {
            QueryBuilder<CellLog, Integer> query = getDao().queryBuilder();
            if (filter.getUser() != null || filter.getTarget() != null || filter.getCell() != null || filter.getAction() != null) {
                Where<CellLog, Integer> where = query.where();
                int count = 0;
                if (filter.getUser() != null) {
                    where.eq("actor_id", filter.getUser().getId());
                    if (filter.isTargetOrUser()) { where.eq("target_id", filter.getUser().getId()); where.or(2); }
                    count++;
                }
                if (filter.getTarget() != null) {
                    where.eq("target_id", filter.getTarget().getId());
                    if (filter.isTargetOrUser()) { where.eq("actor_id", filter.getTarget().getId()); where.or(2); }
                    count++;
                }
                if (filter.getCell() != null) { where.eq("cell_id", filter.getCell().getId()); count++; }
                if (filter.getAction() != null) { where.eq("action", filter.getAction()); count++; }
                if (count > 1) where.and(count);
            }

            query.orderBy("id", false);
            query.offset((long) skip).limit((long) limit);

            return query.query();
        } catch(SQLException exception) {
            getLogger().log(Level.FINE, "Failed to get logs for filter: " + filter, exception);
            return new ArrayList<>();
        }
    }
}