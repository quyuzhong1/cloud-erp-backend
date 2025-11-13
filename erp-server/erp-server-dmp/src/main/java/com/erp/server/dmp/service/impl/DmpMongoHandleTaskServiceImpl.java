package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
import com.erp.server.dmp.enums.DmpMongoHandleTypeEnum;
import com.erp.server.dmp.mapper.DmpMongoHandleTaskMapper;
import com.erp.server.dmp.service.DmpMongoHandleTaskService;
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 处理mongo业务数据任务 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-05-07
 */
@Slf4j
@Service
public class DmpMongoHandleTaskServiceImpl extends SuperServiceImpl<DmpMongoHandleTaskMapper, DmpMongoHandleTaskEntity> implements DmpMongoHandleTaskService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public <T> List<T> findMongoData(String lastId, Integer handleCount, String mongoTableName, Class<T> mongoDTOClass, Boolean queryIsAddOrUpdate) {
        // 查询
        Query query = new Query();
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(lastId)) {
            criteria = Criteria.where("_id").gt(new ObjectId(lastId));
        }
        if (null != queryIsAddOrUpdate){
           criteria.and("isAddOrUpdate").is(queryIsAddOrUpdate);
        }
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, "_id")).limit(handleCount);
        return mongoTemplate.find(query, mongoDTOClass, mongoTableName);
    }

    @Override
    public void updateMaxLastIdAndNextTime(DmpMongoHandleTaskEntity mongoHandleTaskEntity, String maxLastId) {
        mongoHandleTaskEntity.setLastId(maxLastId);
        mongoHandleTaskEntity.setNextTime(mongoHandleTaskEntity.getNextTime().plusSeconds(mongoHandleTaskEntity.getIntervalTime()));
        if (!this.updateById(mongoHandleTaskEntity)){
            throw new ServiceException("[dmp_mongo_handle_task] 更新lastId和NextTime失败");
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public Long clearHistory(DmpMongoHandleTypeEnum handleTypeEnum, LocalDateTime historyDateTime, Integer size) {
        // 构建查询条件
        Criteria criteria = Criteria.where("downloadTime").gt(historyDateTime);

        // 创建查询对象并设置条件
        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.ASC, "downloadTime"));

        // 设置删除操作的限制数量
        query.limit(size);

        // 执行删除操作
        DeleteResult deleteResult = mongoTemplate.remove(query, handleTypeEnum.getMongoTableName());
        return deleteResult.getDeletedCount();
    }
}
