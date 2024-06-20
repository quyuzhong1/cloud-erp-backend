package com.erp.server.wms.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.PickingCartTypeDTO;
import com.erp.model.wms.entity.PickingCartTypeEntity;
import com.erp.server.wms.mapper.PickingCartTypeMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.PickingCartTypeService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 拣货车类型 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@Service
public class PickingCartTypeServiceImpl extends SuperServiceImpl<PickingCartTypeMapper, PickingCartTypeEntity> implements PickingCartTypeService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PickingCartTypeDTO.AddDTO addDTO) {
        PickingCartTypeEntity pickingCartTypeEntity = new PickingCartTypeEntity();
        BeanMapperUtils.copy(addDTO, pickingCartTypeEntity);

        // 数据处理
        handleData(pickingCartTypeEntity);

        log.info("开始新增拣货车类型");
        boolean save = super.save(pickingCartTypeEntity);
        if(!save) {
            throw new ServiceException("拣货车类型保存失败");
        }
        return new BaseResultDTO.AddDTO(pickingCartTypeEntity.getId(), pickingCartTypeEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PickingCartTypeDTO.UpdateDTO updateDTO) {
        PickingCartTypeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "拣货车类型"));
        PickingCartTypeEntity pickingCartTypeEntity =  BeanMapperUtils.map(PickingCartTypeEntity.class, updateDTO);

        // 数据处理
        handleData(pickingCartTypeEntity);
        log.info("编辑 开始修改拣货车类型数据，id：【{}】", old.getId());
        boolean save = super.updateById(pickingCartTypeEntity);
        if(!save) {
            throw new ServiceException("拣货车类型保存失败");
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PickingCartTypeEntity pickingCartTypeEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
