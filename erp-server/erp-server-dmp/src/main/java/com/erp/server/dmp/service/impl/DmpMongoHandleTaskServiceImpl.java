package com.erp.server.dmp.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpMongoHandleTaskEntity;
import com.erp.sdk.oms.amz.spapi.dto.ReportFulfilledShipmentsMongoDTO;
import com.erp.server.dmp.mapper.DmpMongoHandleTaskMapper;
import com.erp.server.dmp.service.DmpMongoHandleTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    public <T> List<T> findMongoData(String lastId, Integer handleCount, String mongoTableName, Class<T> mongoDTOClass) {
        // 查询
        Query query = new Query();
        if (StringUtils.isNotBlank(lastId)) {
            query.addCriteria(Criteria.where("_id").gt(new ObjectId(lastId)));
        }
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
}
