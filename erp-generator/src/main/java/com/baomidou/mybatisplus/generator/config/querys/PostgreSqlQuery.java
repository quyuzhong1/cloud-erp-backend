/*
 * Copyright (c) 2011-2020, baomidou (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baomidou.mybatisplus.generator.config.querys;

import com.baomidou.mybatisplus.annotation.DbType;

/**
 * PostgreSql 表数据查询
 *
 * @author hubin
 * @since 2018-01-16
 */
public class PostgreSqlQuery extends AbstractDbQuery {


    @Override
    public DbType dbType() {
        return DbType.POSTGRE_SQL;
    }


    @Override
    public String tablesSql() {
        return "SELECT A.tablename, obj_description(oid, 'pg_class') AS comments FROM pg_tables A, pg_class B WHERE A.schemaname='%s' AND A.tablename = B.relname";
    }


    @Override
    public String tableFieldsSql() {
        return "SELECT A.attname AS name, format_type(A.atttypid, A.atttypmod) AS type,col_description(A.attrelid, A.attnum) AS comment, (CASE C.contype WHEN 'p' THEN 'PRI' ELSE '' END) AS key " +
            "FROM pg_attribute A LEFT JOIN pg_constraint C ON A.attnum = C.conkey[1] AND A.attrelid = C.conrelid " +
            "WHERE  A.attrelid = '%s.%s'::regclass AND A.attnum > 0 AND NOT A.attisdropped ORDER  BY A.attnum";
    }

    @Override
    public String tableFieldColumnsSql() {
        return "SELECT\n" +
                " A.attname AS \"name\",-- 字段名\n" +
                " t.typname as typename,    --字段类型\n" +
                " NULLIF(information_schema._pg_char_max_length(A.atttypid, A.atttypmod), -1) AS maxlen,  -- 字符串最大长度\n" +
                " col_description ( A.attrelid, A.attnum ) AS COMMENT,   -- 字段备注\n" +
                " format_type ( A.atttypid, A.atttypmod ) AS \"TYPE\",\n" +
                " case when A.attnotnull then 'NO' else 'YES' end AS \"NULL\" ,  -- 是否空\n" +
                " A.atthasdef ,  --是否存在默认值\n" +
                " A.atttypmod,\n" +
                " case\n" +
                "    when position('::' in col.column_default) > 0 then replace(substring(col.column_default from '.*::'), '::', '')\n" +
                "    else col.column_default\n" +
                " end as \"DEFAULT\"\n" +
                "FROM\n" +
                "\tpg_class AS C,\n" +
                "\tpg_attribute AS A ,\n" +
                "\tpg_type as T,\n" +
                "\tinformation_schema.\"columns\" col\n" +
                "WHERE\n" +
                "\tC.relname = '%s' \n" +
                "\tand col.table_name = c.relname\n" +
                "  and col.column_name = a.attname\n" +
                "\tAND A.attrelid = C.oid \n" +
                "\tAND A.atttypid= T.oid\n" +
                "\tAND A.attnum > 0\n" +
                "\tAND NOT A.attisdropped";
    }


    @Override
    public String tableName() {
        return "tablename";
    }


    @Override
    public String tableComment() {
        return "comments";
    }


    @Override
    public String fieldName() {
        return "name";
    }

    @Override
    public String fieldDefault() {
        return "DEFAULT";
    }


    @Override
    public String fieldType() {
        return "type";
    }


    @Override
    public String fieldComment() {
        return "comment";
    }


    @Override
    public String fieldKey() {
        return "key";
    }

}
