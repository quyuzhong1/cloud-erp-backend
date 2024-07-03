package com.erp.server.dmp.pull.mongo.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.common.business.dto.CleanBaseDTO;
import com.common.core.anno.ParamData;
import com.common.core.utils.MapUtil;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.utils.MongoUtil;
import com.google.common.collect.Lists;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

@Service("mongoService")
@Slf4j
public class MongoServiceImpl implements MongoService {

	@Autowired
	@Qualifier(value = "reportTemplate")
	private MongoTemplate orderTemplate;
	
	@Override
	public <T> void saveMongoDataMult(List<T> dataList, String table) {
		orderTemplate.insert(dataList, table);
	}

	@Override
	public <T> void saveMongoData(T data, String table) {
		orderTemplate.insert(data, table);
	}

	@Override
	public <T> T findMongoDataById(String taskid, String table, Class<T> clazz) throws Exception {
		return orderTemplate.findById(taskid, clazz, table);
	}

	@Override
	public <T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table,
			Class<T> clazz) {
		Criteria criteria = MongoUtil.mongoFilter_duplicateKey(obj);
		Query query = new Query(criteria);
		if(currentPage > 0 && pageSize> 0) {
			query.skip((long) (currentPage - 1) *pageSize).limit(pageSize);
		}
		List<T> list = orderTemplate.find(query, clazz, table);
		if(list == null || list.size() <= 0) {
			return null;
		}
		return list;
	}
	
	@Override
	public <T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table, Class<T> clazz,
                                     Direction direction, String... properties) throws Exception {
		Criteria criteria = MongoUtil.mongoFilter_duplicateKey(obj);
		Query query = new Query(criteria);
		if(currentPage > 0 && pageSize> 0) {
			query.skip((currentPage-1)*pageSize).limit(pageSize);
		}
		if(!ObjectUtils.isEmpty(direction) && properties != null && properties.length > 0) {
			query.with(Sort.by(direction, properties));
		}
		List<T> list = orderTemplate.find(query, clazz, table);
		if(list == null || list.size() <= 0) {
			return null;
		}
		return list;
	}

	@Override
	public <T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table, Class<T> clazz,
			Boolean hasId) throws Exception {
		Criteria criteria = MongoUtil.mongoFilter_duplicateKey(obj);
		Query query = new Query(criteria);
		if(currentPage > 0 && pageSize> 0) {
			query.skip((currentPage-1)*pageSize).limit(pageSize);
		}
		if(hasId != null && hasId==false) {
			query.fields().exclude("_id");
		}
		List<T> list = orderTemplate.find(query, clazz, table);
		if(list == null || list.size() <= 0) {
			return null;
		}
		return list;
	}

	@Override
	public <T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table, Class<T> clazz,
                                     Boolean hasId, Direction direction, String... properties) throws Exception {
		Criteria criteria = MongoUtil.mongoFilter_duplicateKey(obj);
		Query query = new Query(criteria);
		if(currentPage > 0 && pageSize> 0) {
			query.skip((currentPage-1)*pageSize).limit(pageSize);
		}
		if(hasId != null && hasId==false) {
			query.fields().exclude("_id");
		}
		if(!ObjectUtils.isEmpty(direction) && properties != null && properties.length > 0) {
			query.with(Sort.by(direction, properties));
		}
		List<T> list = orderTemplate.find(query, clazz, table);
		if(list == null || list.size() <= 0) {
			return null;
		}
		return list;
	}
	
	@Override
	public <T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table,
			Class<T> clazz, Boolean hasId, List<String> fileds) throws Exception {
		Criteria criteria = MongoUtil.mongoFilter_duplicateKey(obj);
		Query query = new Query(criteria);
		if(currentPage > 0 && pageSize> 0) {
			query.skip((currentPage-1)*pageSize).limit(pageSize);
		}
		if(fileds != null && fileds.size() > 0) {
			for(String filed:fileds) {
				query.fields().include(filed);
			}
		}
		if(hasId != null && hasId==false) {
			query.fields().exclude("_id");
		}
		List<T> list = orderTemplate.find(query, clazz, table);
		if(list == null || list.size() <= 0) {
			return null;
		}
		return list;
	}

	@Override
	public <T> List<T> findMongoData(Object obj, int currentPage, int pageSize, String table, Class<T> clazz,
                                     List<String> fileds, Boolean hasId, Direction direction, String... properties) throws Exception {
		Criteria criteria = MongoUtil.mongoFilter_duplicateKey(obj);
		Query query = new Query(criteria);
		if(currentPage > 0 && pageSize> 0) {
			query.skip((currentPage-1)*pageSize).limit(pageSize);
		}
		if(fileds != null && fileds.size() > 0) {
			for(String filed:fileds) {
				query.fields().include(filed);
			}
		}
		if(hasId != null && hasId==false) {
			query.fields().exclude("_id");
		}
		if(!ObjectUtils.isEmpty(direction) && properties != null && properties.length > 0) {
			query.with(Sort.by(direction, properties));
		}
		List<T> list = orderTemplate.find(query, clazz, table);
		if(list == null || list.size() <= 0) {
			return null;
		}
		return list;
	}

	@Override
	public <T> long findMongoCount(Object obj, String table, Class<T> clazz) throws Exception {
		Criteria criteria = MongoUtil.mongoFilter_duplicateKey(obj);
		Query query = new Query(criteria);
		return orderTemplate.count(query, clazz, table);
	}

	@Override
	public <T> void deleteMongoData(Object obj, String table, Class<T> clazz) {
		Criteria criteria = MongoUtil.mongoFilter(obj);
		Query query = new Query(criteria);
		orderTemplate.remove(query, clazz, table);
	}

	@Override
	public <T> void updateMongoData(Object obj, MapUtil data, String table, Class<T> clazz){
		Criteria criteria = MongoUtil.mongoFilter_duplicateKey(obj);
		Query query = new Query(criteria);
		Update update = new Update();
		if(data != null && data.size() > 0) {
			for(Map.Entry<String, Object> entry:data.entrySet()) {
				update.set(entry.getKey(), entry.getValue());
			}
		}
		orderTemplate.updateMulti(query, update, clazz, table);
	}

	@Override
	public <T> void deleteMongoDataByID(String id, String table, Class<T> clazz) {
		Criteria criteria = new Criteria();
		criteria.and("_id").is(id);
		Query query = new Query(criteria);
		orderTemplate.remove(query, clazz, table);
		
	}

	@Override
	public <T> void upsertMongoData(Object obj, MapUtil data, String table,Class<T> clazz) {
		Criteria criteria = MongoUtil.mongoFilter_duplicateKey(obj);
		Query query = new Query(criteria);
		Update update = new Update();
		if(data != null && data.size() > 0) {
			for(Map.Entry<String, Object> entry:data.entrySet()) {
				update.set(entry.getKey(), entry.getValue());
			}
		}
		orderTemplate.upsert(query, update, clazz, table);
	}

	@Override
	public <T> void upsertMongoDataBatch(List<Map<String,Object>> newData, String table) {
		ArrayList<WriteModel<Document>> writeModels = new ArrayList<>();
		if(!CollectionUtils.isEmpty(newData)){
			for (int i = 0; i < newData.size(); i++) {
				Map<String, Object> map = newData.get(i);
				Document document1 = new Document("_id",map.get("_id"));
				Document document2 = new Document("$set",map);
				UpdateOneModel<Document> documentUpdateOneModel = new UpdateOneModel<Document>(document1,document2,new UpdateOptions().upsert(true));
				writeModels.add(documentUpdateOneModel);
			}
			long l = System.currentTimeMillis();
			orderTemplate.getDb().getCollection(table).bulkWrite(writeModels);
			long l1 = System.currentTimeMillis();
			log.info("保存时间:"+(l1-l));
		}
	}

	@Override
	public void deleteMongoDataBatch(Object obj, String table) throws Exception {
		List<Query> queryList = new ArrayList<>();
		if (obj instanceof ArrayList<?>) {
			for (Object o : (List<?>) obj) {
				Criteria criteria = MongoUtil.mongoFilter(o);
				Query query = new Query(criteria);
				queryList.add(query);
			}
		}
		// BulkMode.UNORDERED:表示并行处理，遇到错误时能继续执行不影响其他操作；BulkMode.ORDERED：表示顺序执行，遇到错误时会停止所有执行
		BulkOperations bulkOperations = orderTemplate.bulkOps(BulkOperations.BulkMode.ORDERED,table);
		bulkOperations.remove(queryList);
		// 执行操作
		bulkOperations.execute();
	}

	@Override
	public <T extends CleanBaseDTO> void updateIsClearByUniqueIds(List<String> uniqueIds, Integer isClear, String tableName, Class<T> tClass) {
		// 按1000个分组
		List<List<String>> partition = Lists.partition(uniqueIds, 1000);

		for (List<String> curUniqueIds : partition) {
			// 构建查询条件，查找uniqueId在指定列表内的记录
			Query query = new Query(Criteria.where("uniqueId").in(curUniqueIds));

			// 构建更新操作，将isClear字段设置为1
			Update update = new Update().set("isClear", 1);

			// 执行更新操作
			orderTemplate.updateMulti(query, update, tClass, tableName);
		}
	}

	@Override
	public List<Map<String, Object>> findMongoData(List<ParamData> paramDataList, String table) {
		Criteria criteria = MongoUtil.mongoFilter_duplicateKey(paramDataList);
		Query query = new Query(criteria);
		List<Map> list = orderTemplate.find(query, Map.class, table);
		if(CollUtil.isEmpty(list)) {
			return new ArrayList<Map<String,Object>>();
		}else {
			List<Map<String, Object>> resultList = new ArrayList<>();
			for(Map l : list) {
				Set<Entry> entrySet = l.entrySet();
				Map<String, Object> map = new HashMap<>();
				for(Entry entry : entrySet) {
					map.put(entry.getKey().toString(), entry.getValue());
				}
				resultList.add(map);
			}
			return resultList;
		}
	}

}
