package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.PurchaseSuggestSysDTO;
import com.erp.model.mrp.entity.PurchaseSuggestSysEntity;
import com.erp.server.mrp.mapper.PurchaseSuggestChangeMapper;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.PurchaseSuggestSysService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * <p>
 * 建议采购变更 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-10-21
 */
@Slf4j
@Service
public class PurchaseSuggestSysServiceImpl extends SuperServiceImpl<PurchaseSuggestChangeMapper, PurchaseSuggestSysEntity> implements PurchaseSuggestSysService {
    @Autowired
    private OperateLogService operateLogService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(PurchaseSuggestSysDTO.AddDTO updateDTO) {

        //采购建议数据
        PurchaseSuggestSysEntity old = getBySource(updateDTO.getSourceId(), updateDTO.getSourceType());
        if (ObjectUtil.isNotEmpty(old)) {
            return Boolean.TRUE;
        }
        PurchaseSuggestSysEntity purchaseSuggestSysEntity =  BeanMapperUtils.map(PurchaseSuggestSysEntity.class, updateDTO);
        // 数据处理
        handleData(purchaseSuggestSysEntity);

        boolean save = super.saveOrUpdate(purchaseSuggestSysEntity);
        if(!save) {
            throw new ServiceException("建议采购变更保存失败");
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PurchaseSuggestSysEntity purchaseSuggestSysEntity) {

    }

    /**
     * 根据来源查询
     * @author will
     * @date 2024/10/22 15:20
     * @param sourceId
     * @param sourceType
     */
    private PurchaseSuggestSysEntity getBySource (String sourceId,String sourceType) {
       return lambdaQuery().eq(PurchaseSuggestSysEntity::getSourceId,sourceId)
                .eq(PurchaseSuggestSysEntity::getSourceType,sourceType)
                .last("limit 1")
                .one();
    }
}
