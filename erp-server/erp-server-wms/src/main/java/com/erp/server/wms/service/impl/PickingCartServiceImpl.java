package com.erp.server.wms.service.impl;


import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.PickingCartDTO;
import com.erp.model.wms.entity.PickingCartEntity;
import com.erp.server.wms.mapper.PickingCartMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.PickingCartService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 拣货车管理 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@Service
public class PickingCartServiceImpl extends SuperServiceImpl<PickingCartMapper, PickingCartEntity> implements PickingCartService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PickingCartDTO.AddDTO addDTO) {
        PickingCartEntity pickingCartEntity = new PickingCartEntity();
        BeanMapperUtils.copy(addDTO, pickingCartEntity);

        // 数据处理
        handleData(pickingCartEntity);

        log.info("开始新增拣货车管理");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JHC);
        pickingCartEntity.setCode(code);
        boolean save = super.save(pickingCartEntity);
        if(!save) {
            throw new ServiceException("拣货车管理保存失败");
        }
        return new BaseResultDTO.AddDTO(pickingCartEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PickingCartDTO.UpdateDTO updateDTO) {
        PickingCartEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "拣货车管理"));
        PickingCartEntity pickingCartEntity =  BeanMapperUtils.map(PickingCartEntity.class, updateDTO);

        // 数据处理
        handleData(pickingCartEntity);
        log.info("编辑 开始修改拣货车管理数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(pickingCartEntity);
        if(!save) {
            throw new ServiceException("拣货车管理保存失败");
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PickingCartEntity pickingCartEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
