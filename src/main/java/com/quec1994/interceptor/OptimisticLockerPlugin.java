/**
 * Copyright 2009-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quec1994.interceptor;

import com.quec1994.util.PluginUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * 拦截默认PreparedStatement
 * <p>MyBatis乐观锁插件<br>
 *
 * @author vi
 * @version 0.0.2
 * @date 2018-04-01
 * @date 2020-03-02, quec1994, 加入了对Delete语句的支持
 * @since JDK1.8
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class OptimisticLockerPlugin implements Interceptor {

    private static final Log log = LogFactory.getLog(OptimisticLockerPlugin.class);
    /**
     * 数据库列名
     */
    private static String VERSION_COLUMN = "version";
    /**
     * 实体类字段名
     */
    private static String VERSION_FIELD = "version";
    /**
     * 拦截类型
     */
    private static final String METHOD_TYPE = "prepare";

    private static Properties props = null;

    @Override
    public Object intercept(Invocation invocation) throws Exception {
        String interceptMethod = invocation.getMethod().getName();
        if (!METHOD_TYPE.equals(interceptMethod)) {
            return invocation.proceed();
        }
        StatementHandler handler = (StatementHandler) PluginUtil.processTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(handler);
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        SqlCommandType sqlCmdType = ms.getSqlCommandType();
        if (sqlCmdType != SqlCommandType.UPDATE && sqlCmdType != SqlCommandType.DELETE) {
            return invocation.proceed();
        }
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        String versionFieldName = "delegate.boundSql.parameterObject." + VERSION_FIELD;
        if (!metaObject.hasGetter(versionFieldName)) {
            throw new BindingException("value of version-field[" + VERSION_FIELD + "]can not be empty");
        }
        Object originalVersion = metaObject.getValue(versionFieldName);
        if (Objects.isNull(originalVersion) || Long.parseLong(originalVersion.toString()) <= 0) {
            throw new BindingException("value of version-field[" + VERSION_FIELD + "]can not be empty");
        }
        String originalSql = boundSql.getSql();
        if (log.isDebugEnabled()) {
            log.debug("originalSql: " + originalSql);
        }
        originalSql = addVersionToSql(originalSql, VERSION_COLUMN, originalVersion);
        // 更新sql语句
        metaObject.setValue("delegate.boundSql.sql", originalSql);
        // 更新值对象的版本号
        metaObject.setValue(versionFieldName, (Long) originalVersion + 1);
        if (log.isDebugEnabled()) {
            log.debug("originalSql after add version: " + originalSql);
            log.debug("delegate.boundSql.parameterObject." + VERSION_FIELD + originalSql);
        }
        return invocation.proceed();
    }

    private String addVersionToSql(String originalSql, String versionColumnName, Object originalVersion) {
        try {
            Statement stmt = CCJSqlParserUtil.parse(originalSql);
            if (stmt instanceof Update) {
                Update update = (Update) stmt;
                buildVersionUpdate(versionColumnName, update);
                Expression where = update.getWhere();
                if (where != null) {
                    update.setWhere(new AndExpression(where, buildVersionEquals(versionColumnName, originalVersion)));
                } else {
                    update.setWhere(buildVersionEquals(versionColumnName, originalVersion));
                }
                originalSql = stmt.toString();
            } else if (stmt instanceof Delete) {
                Delete delete = (Delete) stmt;
                Expression where = delete.getWhere();
                if (where != null) {
                    delete.setWhere(new AndExpression(where, buildVersionEquals(versionColumnName, originalVersion)));
                } else {
                    delete.setWhere(buildVersionEquals(versionColumnName, originalVersion));
                }
                originalSql = stmt.toString();
            }
            return originalSql;
        } catch (Exception e) {
            log.error("addVersionToSql ERROR", e);
            return originalSql;
        }
    }

    /**
     * update 语句，添加更新 version
     *
     * @param versionColumnName version列名
     * @param update            update 语句
     * @return 语句中是否已添加了更新
     */
    private boolean buildVersionUpdate(String versionColumnName, Update update) {
        boolean hasVersionColumnName = false;
        List<Column> columns = update.getColumns();
        for (Column column : columns) {
            if (column.getColumnName().equalsIgnoreCase(versionColumnName)) {
                hasVersionColumnName = true;
                break;
            }
        }
        if (!hasVersionColumnName) {
            Column versionColumn = new Column();
            versionColumn.setColumnName(versionColumnName);
            columns.add(versionColumn);

            List<Expression> expressions = update.getExpressions();
            Addition add = new Addition();
            add.setLeftExpression(versionColumn);
            add.setRightExpression(new LongValue(1));
            expressions.add(add);
        }
        return hasVersionColumnName;
    }

    /**
     * 创建验证 version 的条件
     *
     * @param versionColumnName version列名
     * @param originalVersion   version原值、原乐观锁值
     * @return sql语句：相等条件
     */
    private Expression buildVersionEquals(String versionColumnName, Object originalVersion) {
        EqualsTo equal = new EqualsTo();
        Column column = new Column();
        column.setColumnName(versionColumnName);
        equal.setLeftExpression(column);
        LongValue val = new LongValue(originalVersion.toString());
        equal.setRightExpression(val);
        return equal;
    }


    private Class<?> getMapper(MappedStatement ms) {
        String namespace = getMapperNamespace(ms);
        Collection<Class<?>> mappers = ms.getConfiguration().getMapperRegistry().getMappers();
        for (Class<?> clazz : mappers) {
            if (clazz.getName().equals(namespace)) {
                return clazz;
            }
        }
        return null;
    }

    private String getMapperNamespace(MappedStatement ms) {
        String id = ms.getId();
        int pos = id.lastIndexOf(".");
        return id.substring(0, pos);
    }

    private String getMapperShortId(MappedStatement ms) {
        String id = ms.getId();
        int pos = id.lastIndexOf(".");
        return id.substring(pos + 1);
    }

    /**
     * 获取到拦截的对象，底层也是通过代理实现的，实际上是拿到一个目标代理对象
     *
     * @param target 拦截的对象
     * @return 目标代理对象
     **/
    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler || target instanceof ParameterHandler) {
            // 使用插件包装目标代理对象
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    /**
     * 获取设置的数据库列名、实体类字段名等参数
     *
     * @param properties 设置的参数
     **/
    @Override
    public void setProperties(Properties properties) {
        if (null != properties && !properties.isEmpty()) {
            props = properties;
        }
        if (props != null) {
            VERSION_COLUMN = props.getProperty("versionColumn", "version");
            VERSION_FIELD = props.getProperty("versionField", "version");
        }
    }
}