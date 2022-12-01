package com.erp.server.dmp.pull.mongo;

import com.common.core.utils.MapUtil;
import org.springframework.data.domain.Sort.Direction;

import java.util.List;
import java.util.Map;

public interface MongoService {
	/**
	 * 1单个保存数据
	 * @param <T>
	 * @param data:需要插入的数据 
	 * @param table:表名
	 * @throws Exception
	 */
	<T> void saveMongoData(T data, String table) throws Exception;
	/**
	 * 2批量保存数据
	 * @param <T>
	 * @param dataList:需要插入的数据
	 * @param table:表名
	 * @throws Exception
	 */
	<T> void saveMongoDataMult(List<T> dataList, String table) throws Exception;
	/**
	 * 3根据mongo里面的自增id查询数据
	 * @param <T>
	 * @param taskid:条件
	 * @param table:表名
	 * @param clazz:返回的对象
	 * @return
	 * @throws Exception
	 */
	<T> T findMongoDataById(String taskid, String table, Class<T> clazz) throws Exception;
	/**
	 * 4 查询mongo里面的数据,支持分页
	 * @param <T>
	 * @param obj:条件
	 * @param currentPage:当前页数
	 * @param pageSize:每页条数
	 * @param table:表名
	 * @param clazz:返回的对象
	 * @return
	 * @throws Exception
	 */
	<T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table, Class<T> clazz) throws Exception;
	/**
	 * 5 查询mongo里面的数据,支持分页
	 * @param <T>
	 * @param obj:条件
	 * @param currentPage:当前页数
	 * @param pageSize:每页条数
	 * @param table:表名
	 * @param clazz:返回的对象
	 * @param direction:正序(asc),倒序(desc)
	 * @param properties:排序的字段
	 * @return
	 * @throws Exception
	 */
	<T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table, Class<T> clazz, Direction direction, String... properties) throws Exception;
	/**
	 * 6 查询mongo里面的数据,支持分页
	 * @param <T>
	 * @param obj:条件
	 * @param currentPage:当前页数
	 * @param pageSize:每页条数
	 * @param table:表名
	 * @param clazz:返回的对象
	 * @param hasId:是否查询mongo的主键,默认返回，true返回，false不返回
	 * @return
	 * @throws Exception
	 */
	<T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table, Class<T> clazz, Boolean hasId) throws Exception;
	/**
	 * 7 查询mongo里面的数据,支持分页
	 * @param <T>
	 * @param obj:条件
	 * @param currentPage:当前页数
	 * @param pageSize:每页条数
	 * @param table:表名
	 * @param clazz:返回的对象
	 * @param hasId:是否查询mongo的主键,默认返回，true返回，false不返回
	 * @param direction:正序(asc),倒序(desc)
	 * @param properties:排序的字段
	 * @return
	 * @throws Exception
	 */
	<T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table, Class<T> clazz, Boolean hasId, Direction direction, String... properties) throws Exception;
	/**
	 * 8 查询mongo里面的数据,支持分页
	 * @param <T>
	 * @param obj:条件
	 * @param currentPage:当前页数
	 * @param pageSize:每页条数
	 * @param table:表名
	 * @param clazz:返回的对象
	 * @param fileds:指定查询的对象
	 * @param hasId:是否查询mongo的主键
	 * @return
	 * @throws Exception
	 */
	<T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table, Class<T> clazz, Boolean hasId, List<String> fileds) throws Exception;
	/**
	 * 9查询mongo里面的数据,支持分页
	 * @param <T>
	 * @param obj:条件
	 * @param currentPage:当前页数
	 * @param pageSize:每页条数
	 * @param table:表名
	 * @param clazz:返回的对象
	 * @param fileds:指定查询的对象
	 * @param hasId:是否查询mongo的主键
	 * @param direction:正序(asc),倒序(desc)
	 * @param properties:排序的字段
	 * @return
	 * @throws Exception
	 */
	<T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table, Class<T> clazz, List<String> fileds, Boolean hasId, Direction direction, String... properties) throws Exception;
	/**
	 * 10 查询总条数
	 * @param obj:条件
	 * @param table:表名
	 * @return
	 * @throws Exception
	 */
	<T> long findMongoCount(Object obj, String table, Class<T> clazz) throws Exception;
	/**
	 * 11删除数据
	 * @param obj:条件
	 * @param table:表名
	 * @throws Exception
	 */
	<T> void deleteMongoData(Object obj, String table, Class<T> clazz) throws Exception;
	/**
	 * 12更新数据
	 * @param obj:条件
	 * @param data:需要修改的数据
	 * @param table:表名
	 * @throws Exception
	 */
	<T> void updateMongoData(Object obj, MapUtil data, String table, Class<T> clazz) throws Exception;
	/**
	 * 13根据id删除数据
	 * @param id,:条件
	 * @param table:表名
	 * @throws Exception
	 */
	<T> void deleteMongoDataByID(String id, String table, Class<T> clazz) throws Exception;

	/**
	 * 12更新数据
	 * @param obj:条件
	 * @param data:需要修改的数据
	 * @param table:表名
	 * @throws Exception
	 */
	<T> void upsertMongoData(Object obj, MapUtil data, String table, Class<T> clazz);

	/**
	 * 13批量更新数据
	 * @param map:条件
	 * @throws Exception
	 */
	<T> void upsertMongoDataBatch(List<Map<String,Object>> map, String table, Class<T> clazz);

	/**
	 * 14 批量删除数据
	 * @param obj:条件
	 * @param table:表名
	 * @throws Exception
	 */
	void deleteMongoDataBatch(Object obj,String table)throws Exception;
}
