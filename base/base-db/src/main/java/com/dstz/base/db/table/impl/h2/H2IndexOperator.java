package com.dstz.base.db.table.impl.h2;

import com.dstz.base.core.util.AppUtil;
import com.dstz.base.core.util.BeanUtils;
import com.dstz.base.db.api.table.ITableOperator;
import com.dstz.base.db.api.table.model.Index;
import com.dstz.base.db.table.BaseIndexOperator;
import com.dstz.base.db.table.model.DefaultIndex;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * H2 索引操作的实现
 *
 * <pre>
 * </pre>
 */
public class H2IndexOperator extends BaseIndexOperator {

    // 批量操作的
    protected int BATCH_SIZE = 100;
    private final String SQL_GET_ALL_INDEX = "" + "SELECT "
            + "A.TABLE_NAME  , " + // Table Name
            "A.INDEX_NAME  , " + // Index Name
            "A.NON_UNIQUE  , " + // Unique rule: D = Duplicates allowed; P =
            // Primary index; U = Unique entries only
            // allowed
            "A.COLUMN_NAME  , " + // column count
            "A.INDEX_TYPE_NAME  , " + // index type
            "A.REMARKS , " + "A.SQL " + // index comment
            "FROM " + "INFORMATION_SCHEMA.INDEXES  A " + "WHERE " + "1=1 ";


    /*
     * (non-Javadoc)
     *
     * @see
     * com.dstz.base.db.table.BaseIndexOperator#createIndex(java.lang.String,
     * java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public void createIndex(Index index) throws SQLException {
        String sql = genIndexDDL(index);
        jdbcTemplate.execute(sql);
    }


    /**
     * 生成Index对象对应的DDL语句
     *
     * @param index
     * @return
     */
    private String genIndexDDL(Index index) {
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE ");
        sql.append("INDEX ");
        sql.append(index.getIndexName());
        sql.append(" ON ");
        sql.append(index.getTableName());
        sql.append("(");
        for (String field : index.getColumnList()) {
            sql.append(field);
            sql.append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        return sql.toString();
    }

    /*
     * (non-Javadoc) 刪除索引
     *
     * @see
     * com.dstz.base.db.table.BaseIndexOperator#dropIndex(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void dropIndex(String tableName, String indexName) {
        String sql = "DROP INDEX " + indexName;
        jdbcTemplate.execute(sql);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dstz.base.db.table.BaseIndexOperator#getIndex(java.lang.String,
     * java.lang.String)
     */
    @Override
    public Index getIndex(String tableName, String indexName)
            throws SQLException {
        String sql = SQL_GET_ALL_INDEX;
        sql += " AND A.INDEX_NAME = '" + indexName + "' ";
        List<Index> indexes = getIndexesBySql(sql);
        List<Index> indexList = mergeIndex(indexes);
        if (BeanUtils.isEmpty(indexList)) {
            return null;
        } else {
            Index index = indexList.get(0);
            return index;
        }
    }

    /**
     * 通过SQL获得索引
     *
     * @param sql
     * @return
     */
    private List<Index> getIndexesBySql(String sql) {
        List<Index> indexes = jdbcTemplate.query(sql, new RowMapper<Index>() {
            @Override
            public Index mapRow(ResultSet rs, int rowNum) throws SQLException {
                Index index = new DefaultIndex();
                index.setTableName(rs.getString("TABLE_NAME"));
                index.setTableType(Index.TABLE_TYPE_TABLE);
                index.setIndexName(rs.getString("INDEX_NAME"));
                // set unique type
                String non_unique = rs.getString("NON_UNIQUE").trim();
                String index_type_name = rs.getString("INDEX_TYPE_NAME").trim();
                if ("TRUE".equalsIgnoreCase(non_unique)) {
                    index.setUnique(true);
                }
                // set primary index
                if ("PRIMARY KEY".equalsIgnoreCase(index_type_name)) {
                    index.setPkIndex(true);
                }
                // set index type
                index.setIndexType(Index.INDEX_TYPE_BTREE);

                index.setIndexComment(rs.getString("REMARKS"));
                List<String> indexFields = new ArrayList<String>();
                indexFields.add(rs.getString("COLUMN_NAME"));
                index.setColumnList(indexFields);
                // set index ddl
                index.setIndexDdl(rs.getString("SQL"));
                return index;
            }
        });
        return indexes;

    }

    /**
     * indexes中，每一索引列，对就一元素。需要进行合并。
     *
     * @param indexes
     * @return
     */
    private List<Index> mergeIndex(List<Index> indexes) {
        List<Index> indexList = new ArrayList<Index>();
        for (Index index : indexes) {
            boolean found = false;
            for (Index index1 : indexList) {
                if (index.getIndexName().equals(index1.getIndexName())
                        && index.getTableName().equals(index1.getTableName())) {
                    index1.getColumnList().add(index.getColumnList().get(0));
                    found = true;
                    break;
                }
            }
            if (!found)
                indexList.add(index);
        }
        return indexList;
    }

    /**
     * 判断索引是否是主键索引。如果是，则将索引index的pkIndex属性设置为true。此方法是批量操作，主要是减少对数据库访问次数。
     *
     * @param indexList
     * @return
     * @throws SQLException
     */
    private List<Index> dedicatePKIndex(List<Index> indexList)
            throws SQLException {
        // 用于存放所有索引所包含的所有数据表名(去掉重复)
        List<String> tableNames = new ArrayList<String>();
        for (Index index : indexList) {
            // 将索引对应的表名放到tableNames中。
            if (!tableNames.contains(index.getTableName())) {
                tableNames.add(index.getTableName());
            }
        }
        Map<String, List<String>> tablePKColsMaps = getTablesPKColsByNames(tableNames);
        for (Index index : indexList) {
            if (isListEqual(index.getColumnList(),
                    tablePKColsMaps.get(index.getTableName()))) {
                index.setPkIndex(true);
            } else {
                index.setPkIndex(false);
            }
        }

        return indexList;
    }

    /**
     * 根据表名，取得对应表的主键列。此方法是指操作，主要是减少对数据库的访问次数。返回的Map中：key=表名；value=表名对应的主键列列表。
     *
     * @param tableNames
     * @return
     * @throws SQLException
     */
    private Map<String, List<String>> getTablesPKColsByNames(
            List<String> tableNames) throws SQLException {
        Map<String, List<String>> tableMaps = new HashMap<String, List<String>>();
        List<String> names = new ArrayList<String>();
        for (int i = 1; i <= tableNames.size(); i++) {
            names.add(tableNames.get(i - 1));
            if (i % BATCH_SIZE == 0 || i == tableNames.size()) {
                Map<String, List<String>> map;
                map = getPKColumns(names);
                tableMaps.putAll(map);
                names.clear();
            }
        }
        return tableMaps;
    }

    /**
     * 获取主键字段
     *
     * @param tableNames
     * @return
     * @throws SQLException
     */
    private Map<String, List<String>> getPKColumns(List<String> tableNames)
            throws SQLException {
        ITableOperator tableOperator = AppUtil.getBean(ITableOperator.class);
        return tableOperator.getPKColumns(tableNames);
    }

    /**
     * 获取主键字段
     *
     * @param tableName
     * @return
     * @throws SQLException
     */
    private List<String> getPKColumns(String tableName) throws SQLException {
        ITableOperator tableOperator = AppUtil.getBean(ITableOperator.class);
        return tableOperator.getPKColumns(tableName);
    }


    /**
     * 判断索引是否是主键索引。如果是，则将索引index的pkIndex属性设置为true。
     *
     * @param index
     * @return
     */
    @SuppressWarnings("unused")
    private Index dedicatePKIndex(Index index) {
        try {
            List<String> pkCols = getPKColumns(index.getIndexName());
            if (isListEqual(index.getColumnList(), pkCols))
                index.setPkIndex(true);
            else
                index.setPkIndex(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return index;
    }

    /**
     * 比较两个列表是否相等。在比较两个列表的元素时，比较的方式为(o==null ? e==null : o.equals(e)).
     *
     * @param list1
     * @param list2
     * @return
     */
    private boolean isListEqual(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null)// 2个都为null
            return true;
        if (list1 == null || list2 == null)// 2个有一个为null
            return false;
        if (list1.size() != list2.size())// 2个长度不相等
            return false;
        if (list1.containsAll(list2))
            return true;

        return false;
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * com.dstz.base.db.table.BaseIndexOperator#rebuildIndex(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void rebuildIndex(String tableName, String indexName) {
        //String sql =" REORG INDEXES ALL FOR TABLE "+tableName+" ALLOW WRITE ACCESS CLEANUP ONLY ALL ";
        //jdbcTemplate.execute(sql);
        throw new UnsupportedOperationException("h2 不支持通过JDBC进行索引重建！");
    }


    @Override
    public List<Index> getIndexByFuzzyMatch(String indexName)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public List<Index> getIndexsByTable(String tableName) throws SQLException {
        String sql = SQL_GET_ALL_INDEX;
        sql += " AND UPPER(A.TABLE_NAME) = UPPER('" + tableName + "')";
        List<Index> indexes = getIndexesBySql(sql);
        // indexes中，每一索引列，对应一元素。需要进行合并。
        List<Index> indexList = mergeIndex(indexes);
        for (Index index : indexList) {
            index.setIndexDdl(genIndexDDL(index));
        }
        return indexList;
    }

}