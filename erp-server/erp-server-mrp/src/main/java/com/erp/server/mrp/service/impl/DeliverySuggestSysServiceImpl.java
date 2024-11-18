package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.DeliverySuggestSysDTO;
import com.erp.model.mrp.entity.DeliverySuggestSysEntity;
import com.erp.server.mrp.mapper.DeliverySuggestChangeMapper;
import com.erp.server.mrp.service.DeliverySuggestSysService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * <p>
 * 建议发货变更表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-10-21
 */
@Slf4j
@Service
public class DeliverySuggestSysServiceImpl extends SuperServiceImpl<DeliverySuggestChangeMapper, DeliverySuggestSysEntity> implements DeliverySuggestSysService {
    @Autowired
    private OperateLogService operateLogService;


    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(DeliverySuggestSysDTO.AddDTO updateDTO) {

        //采购建议数据
        DeliverySuggestSysEntity old = getBySource(updateDTO.getSourceId(), updateDTO.getSourceType());
        if (ObjectUtil.isNotEmpty(old)) {
            return Boolean.TRUE;
        }
        DeliverySuggestSysEntity deliverySuggestSysEntity =  BeanMapperUtils.map(DeliverySuggestSysEntity.class, updateDTO);
        // 数据处理
        handleData(deliverySuggestSysEntity);

        boolean save = super.saveOrUpdate(deliverySuggestSysEntity);
        if(!save) {
            throw new ServiceException("建议采购变更保存失败");
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DeliverySuggestSysEntity deliverySuggestSysEntity) {

    }


    /**
     * 根据来源查询
     * @author will
     * @date 2024/10/22 15:20
     * @param sourceId
     * @param sourceType
     */
    private DeliverySuggestSysEntity getBySource (String sourceId,String sourceType) {
        return lambdaQuery().eq(DeliverySuggestSysEntity::getSourceId,sourceId)
                .eq(DeliverySuggestSysEntity::getSourceType,sourceType)
                .last("limit 1")
                .one();
    }
}
