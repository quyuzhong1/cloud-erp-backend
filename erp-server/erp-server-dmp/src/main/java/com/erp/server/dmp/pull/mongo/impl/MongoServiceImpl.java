package com.erp.server.dmp.pull.mongo.impl;

import com.common.business.dto.CleanBaseDTO;
import com.common.core.utils.MapUtil;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.utils.MongoUtil;
import com.google.common.collect.Lists;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import lombok.extern.slf4j.Slf4j;
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
	public <T> void upsertMongoDataBatch(List<Map<String,Object>> newData, String table, Class<T> clazz) {
		ArrayList<WriteModel<Document>> writeModels = new ArrayList<>();
		if(!CollectionUtils.isEmpty(newData)){
			for (int i = 0; i < newData.size(); i++) {

				Document document1 = new Document("mainId",newData.get(i).get("mainId"));
				Document document2 = new Document("$set",newData.get(i).get("entity"));
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

}
