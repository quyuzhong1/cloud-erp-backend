package com.common.business.mapper;

import java.util.List;
import java.util.Map;

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
	  * 【查询任意表某一字段值】根据指定查询字段的值
	  *
	  * @param tableName       表名
	  * @param queryFieldName  查询字段名
	  * @param queryValue      查询值
	  * @param returnFieldName 返回字段名
	  * @return
	  */
	 @Select("select type,${returnFieldName} from ${tableName} where type in (${queryTypeField}) and ${queryFieldName} = #{queryValue} ${extendQuerySql} limit 1")
	 List<Map<String, Object>> queryValueByType(@Param("tableName") String tableName, @Param("queryFieldName") String queryFieldName,
			 @Param("queryValue") String queryValue, @Param("returnFieldName") String returnFieldName 
			 , @Param("queryTypeField") String queryTypeField , @Param("extendQuerySql") String extendQuerySql);

}
