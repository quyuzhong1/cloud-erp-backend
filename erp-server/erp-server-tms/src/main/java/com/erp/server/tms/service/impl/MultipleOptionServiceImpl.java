package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.erp.model.tms.entity.MultipleOptionEntity;
import com.erp.server.tms.mapper.MultipleOptionMapper;
import com.erp.server.tms.service.MultipleOptionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.MultipleOptionDTO;
import java.util.*;

/**
 * <p>
 * 多选下拉存储表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
 */
@Slf4j
@Service
public class MultipleOptionServiceImpl extends SuperServiceImpl<MultipleOptionMapper, MultipleOptionEntity> implements MultipleOptionService {

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(MultipleOptionDTO.AddDTO addDTO) {
        //先删除原数据
        this.deleteByMainIds(Arrays.asList(addDTO.getMainId()));

        //组装数据
        List<MultipleOptionEntity> list = new ArrayList<>();
        for (String refId : addDTO.getRefIdList()) {
            MultipleOptionEntity multipleOptionEntity = new MultipleOptionEntity();
            multipleOptionEntity.setMainId(addDTO.getMainId());
            multipleOptionEntity.setType(addDTO.getType());
            multipleOptionEntity.setRefId(refId);
            list.add(multipleOptionEntity);
        }

        //批量新增
        log.info("开始新增多选下拉存储单");
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("多选下拉存储单保存失败");
        }
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        if (CollectionUtil.isEmpty(mainIds)) {
            return Boolean.FALSE;
        }

        return lambdaUpdate()
                .set(MultipleOptionEntity::getIsDeleted, Boolean.TRUE)
                .in(MultipleOptionEntity::getMainId, mainIds)
                .update();
    }

    @Override
    public List<MultipleOptionEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(MultipleOptionEntity::getMainId, mainIds).list();
    }
}
