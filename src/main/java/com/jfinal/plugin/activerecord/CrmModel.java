package com.jfinal.plugin.activerecord;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Model.
 * <p>
 * A clever person solves a problem.
 * A wise person avoids it.
 * A stupid person makes it.
 * @author honglei.wan
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class CrmModel<M extends Model> extends Model<M> implements Serializable {

    private static final long serialVersionUID = -990334519496260592L;

    private static Logger logger = LoggerFactory.getLogger(CrmModel.class);

    static {
        logger.info("CrmModel 加载完成");
    }

    private Map<String, Object> attrs = super._getAttrs();

    /**
     * Set attribute to model.
     * @param attr the attribute name of the model
     * @param value the value of the attribute
     * @return this model
     * @throws ActiveRecordException if the attribute is not exists of the model
     */
    @Override
    public M set(String attr, Object value) {
        // table 为 null 时用于未启动 ActiveRecordPlugin 的场景
        Table table = super._getTable();
        if (table != null && !table.hasColumnLabel(attr)) {
            logger.warn("The attribute name does not exist: {}",attr);
        } else {
            attrs.put(attr, value);
            // Add modify flag, update() need this flag.
            _getModifyFlag().add(attr);
        }

        return (M)this;
    }


    /**
     * 根据列名获取对象（符合条件的第一个）
     * 单个列名
     * 单个列值
     * @param columnName
     * @param idValue
     * @return
     */
    public M findSingleByColumn(String columnName, Object idValue) {
        List<M> result = findListByColumns(new String[]{columnName}, new Object[]{idValue});
        return result.size() > 0 ? result.get(0) : null;
    }

    /**
     * 根据列名获取对象列表
     * 单个列名
     * 单个列值
     * @param columnName
     * @param idValue
     * @return
     */
    public List<M> findListByColumn(String columnName, Object idValue) {
        return findListByColumns(new String[]{columnName}, new Object[]{idValue});
    }

    /**
     * 根据列名获取对象列表
     * 多个列名
     * 多个列值
     * @param columnNames
     * @param idValues
     * @return
     */
    public List<M> findListByColumns(String[] columnNames, Object[] idValues) {
        Table table = _getTable();
        if (columnNames == null || idValues == null || columnNames.length != idValues.length) {
            throw new IllegalArgumentException("attrNames length should equals to idValues length");
        }

        Config config = _getConfig();
        String sql = forModelFindByColumnNames(table,columnNames);
        return find(config, sql, idValues);
    }

    /**
     * 根据列名的值列表获取model list
     * 单个列名
     * 多个列值
     * @param columnName
     * @param values
     * @return
     */
    public List<M> findListWithColValues(String columnName, Object... values) {
        Table table = _getTable();
        Config config = _getConfig();

        StringBuilder sql = new StringBuilder("select ");
        sql.append('*');
        sql.append(" from `");
        sql.append(table.getName());
        sql.append("` where ");
        sql.append('`').append(columnName).append("` in (");

        for (int i=0; i<values.length; i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }

        sql.append(") ");

        return find(config, sql.toString(), values);
    }

    /**
     * 根据列名的值列表获取model list
     * 单个列名
     * 列值为list
     * @param columnName
     * @param list
     * @return
     */
    public List<M> findListWithColValues(String columnName, List<?> list) {
        Table table = _getTable();
        Config config = _getConfig();

        StringBuilder sql = new StringBuilder("select ");
        sql.append('*');
        sql.append(" from `");
        sql.append(table.getName());
        sql.append("` where ");
        sql.append('`').append(columnName).append("` in (");

        for (int i=0; i<list.size(); i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }

        sql.append(") ");

        return find(config, sql.toString(), list.toArray());
    }

    /**
     * 根据columnName查询Model列表，返回指定返回的columns
     * @param columns 指定返回的Columns
     * @param columnName 查询条件的ColumnName
     * @param values 查询条件
     * @return
     */
    public List<M> findListByColumnLoadColumns(String[] columns, String columnName, Object... values) {
        if (values == null || values.length == 0 || StringUtils.isBlank(columnName)) {
            return Lists.newArrayList();
        }

        String columnStr = columns == null ? "*" : String.join(",", columns);

        Table table = _getTable();
        Config config = _getConfig();

        StringBuilder sql = new StringBuilder("select ");
        sql.append(columnStr);
        sql.append(" from `");
        sql.append(table.getName());
        sql.append("` where ");
        sql.append('`').append(columnName).append("` in (");

        for (int i=0; i<values.length; i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }

        sql.append(") ");

        return find(config, sql.toString(), values);
    }

    /**
     * 根据列名获取对象
     * @param table
     * @param columnNames
     * @return
     */
    private String forModelFindByColumnNames(Table table,String[] columnNames) {
        StringBuilder sql = new StringBuilder("select ");
        sql.append('*');
        sql.append(" from `");
        sql.append(table.getName());
        sql.append("` where ");
        for (int i=0; i<columnNames.length; i++) {
            if (i > 0) {
                sql.append(" and ");
            }
            sql.append('`').append(columnNames[i]).append("` = ?");
        }
        return sql.toString();
    }

    /**
     * 根据列名获取符合条件的对象的个数
     * 单个列名
     * 单个列值
     * @param columnName
     * @param idValue
     * @return
     */
    public int countByColumn(String columnName, Object idValue) {
        return findListByColumns(new String[]{columnName}, new Object[]{idValue}).size();
    }

}


