//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.baomidou.mybatisplus.generator.config.converts;

import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.ITypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;

public class PostgreSqlTypeConvert implements ITypeConvert {
    public static final PostgreSqlTypeConvert INSTANCE = new PostgreSqlTypeConvert();

    @Override
    public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
        String t = fieldType.toLowerCase();
        if(t.contains("char")) {
            return DbColumnType.STRING;
        } else if (t.contains("text")) {
            return DbColumnType.STRING;
        } else if (t.contains("json")) {
            return DbColumnType.STRING;
        } else if (t.contains("enum")) {
            return DbColumnType.STRING;
        } else if (t.contains("bigint")) {
            return DbColumnType.LONG;
        } else if (t.contains("int")) {
            return DbColumnType.INTEGER;
        } else if (t.contains("bit")) {
            return DbColumnType.BOOLEAN;
        } else if (t.contains("decimal")) {
            return DbColumnType.BIG_DECIMAL;
        } else if (t.contains("numeric")) {
            return DbColumnType.BIG_DECIMAL;
        } else if (t.contains("bytea")) {
            return DbColumnType.BYTE_ARRAY;
        } else if (t.contains("float")) {
            return DbColumnType.FLOAT;
        } else if (t.contains("double")) {
            return DbColumnType.DOUBLE;
        } else if (t.contains("boolean")) {
            return DbColumnType.BOOLEAN;
        } else if (t.contains("date")) {
            return toDateType(globalConfig, fieldType);
        } else if (t.contains("time")) {
            return toDateType(globalConfig, fieldType);
        }
        return DbColumnType.STRING;
    }

    public static IColumnType toDateType(GlobalConfig config, String type) {
        byte var3;
        switch(config.getDateType()) {
        case SQL_PACK:
            var3 = -1;
            switch(type.hashCode()) {
            case 3076014:
                if (type.equals("date")) {
                    var3 = 0;
                }
                break;
            case 3560141:
                if (type.equals("time")) {
                    var3 = 1;
                }
            }

            switch(var3) {
            case 0:
                return DbColumnType.DATE_SQL;
            case 1:
                return DbColumnType.TIME;
            default:
                return DbColumnType.TIMESTAMP;
            }
        case TIME_PACK:
            var3 = -1;
            switch(type.hashCode()) {
            case 3076014:
                if (type.equals("date")) {
                    var3 = 0;
                }
                break;
            case 3560141:
                if (type.equals("time")) {
                    var3 = 1;
                }
            }

            switch(var3) {
            case 0:
                return DbColumnType.LOCAL_DATE;
            case 1:
                return DbColumnType.LOCAL_TIME;
            default:
                return DbColumnType.LOCAL_DATE_TIME;
            }
        default:
            return DbColumnType.DATE;
        }
    }


}
