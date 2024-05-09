package com.erp.server.sys.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.KingdeeOperatorTypeDTO;
import com.erp.model.sys.entity.KingdeeOperatorTypeEntity;
import com.erp.server.sys.mapper.KingdeeOperatorTypeMapper;
import com.erp.server.sys.service.KingdeeOperatorTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@Service
public class KingdeeOperatorTypeServiceImpl extends SuperServiceImpl<KingdeeOperatorTypeMapper, KingdeeOperatorTypeEntity> implements KingdeeOperatorTypeService {



    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(KingdeeOperatorTypeDTO.AddDTO addDTO) {
        KingdeeOperatorTypeEntity kingdeeOperatorTypeEntity = new KingdeeOperatorTypeEntity();
        BeanMapperUtils.copy(addDTO, kingdeeOperatorTypeEntity);
        // 数据处理
        handleData(kingdeeOperatorTypeEntity);
        boolean save = super.save(kingdeeOperatorTypeEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        return save;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeeOperatorTypeDTO.UpdateDTO updateDTO) {
        KingdeeOperatorTypeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        KingdeeOperatorTypeEntity kingdeeOperatorTypeEntity =  BeanMapperUtils.map(KingdeeOperatorTypeEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeeOperatorTypeEntity);
        boolean save = super.updateById(kingdeeOperatorTypeEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KingdeeOperatorTypeEntity entity) {
    long count=this.lambdaQuery().
            eq(KingdeeOperatorTypeEntity::getCode, entity.getCode()).
            ne(StringUtils.isNotBlank(entity.getId()),KingdeeOperatorTypeEntity::getId, entity.getId()).
            count();
        if (count > 0) {
            throw new ServiceException("编码已存在");
        }
    }
}
