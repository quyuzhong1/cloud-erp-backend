package com.common.business.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BaseDataMapper {

    /**
     * 【查询任意表某一字段值】根据指定查询字段的值
     *
     * @param tableName       表名
     * @param queryFieldName  查询字段名
     * @param queryValue      查询值
     * @param returnFieldName 返回字段名
     * @return
     */
	 @Select("select ${returnFieldName} from ${tableName} where ${queryFieldName} = #{queryValue} ${extendQuerySql} limit 1")
    List<Map<String, Object>> queryValueByValue(@Param("tableName") String tableName, @Param("queryFieldName") String queryFieldName,
                             @Param("queryValue") String queryValue, @Param("returnFieldName") String returnFieldName , @Param("extendQuerySql") String extendQuerySql);
	 
	 /**
	  * 【查询dict表某一字段值】根据指定查询字段的值
	  *
	  * @param tableName       表名
	  * @param queryFieldName  查询字段名
	  * @param queryValue      查询值
	  * @param returnFieldName 返回字段名
	  * @return
	  */
	 @Select("select type,${queryFieldName},${returnFieldName} from ${tableName} where type in (${queryTypeField})")
	 List<Map<String, Object>> queryValueByType(@Param("tableName") String tableName, @Param("queryFieldName") String queryFieldName,
			 @Param("returnFieldName") String returnFieldName , @Param("queryTypeField") String queryTypeField);

	 /**
	  * 按时间字段批量删除过期数据，返回实际删除行数
	  *
	  * @param tableName          表名
	  * @param timeField          时间字段
	  * @param retentionDay       保留天数
	  * @param limitCount         一次限制条数
	  * @param extSql 扩展sql语句
	  * @return 删除行数
	  */
	 @Delete("<script>"
			 + "DELETE FROM ${tableName} WHERE id IN ("
			 + "  SELECT id FROM ${tableName}"
			 + "  WHERE ${timeField} &lt; current_date - (${retentionDay} || ' day')::interval"
			 + "  <if test=\"extSql != null and extSql != ''\">"
			 + "    AND ${extSql}"
			 + "  </if>"
			 + "  LIMIT ${limitCount}"
			 + ")"
			 + "</script>")
	 int deleteArchiveData(@Param("tableName") String tableName,
						   @Param("timeField") String timeField,
						   @Param("retentionDay") int retentionDay,
						   @Param("limitCount") int limitCount,
						   @Param("extSql") String extSql);

}
