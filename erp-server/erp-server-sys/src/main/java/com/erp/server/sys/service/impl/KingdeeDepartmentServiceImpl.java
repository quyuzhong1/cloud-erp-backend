package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.sys.mapper.KingdeeDepartmentMapper;
import com.erp.server.sys.service.CommonService;
import com.erp.server.sys.service.KingdeeDepartmentService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class KingdeeDepartmentServiceImpl extends SuperServiceImpl<KingdeeDepartmentMapper, KingdeeDepartmentEntity> implements KingdeeDepartmentService {

    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KingdeeDepartmentDTO.AddDTO addDTO) {
        KingdeeDepartmentEntity kingdeeDepartmentEntity = new KingdeeDepartmentEntity();
        BeanMapperUtils.copy(addDTO, kingdeeDepartmentEntity);

        // 数据处理
        handleData(kingdeeDepartmentEntity);

        log.info("开始新增");
        boolean save = super.save(kingdeeDepartmentEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "" , kingdeeDepartmentEntity.getId());

        return new BaseResultDTO.AddDTO(kingdeeDepartmentEntity.getId(), kingdeeDepartmentEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KingdeeDepartmentDTO.UpdateDTO updateDTO) {
        KingdeeDepartmentEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        KingdeeDepartmentEntity kingdeeDepartmentEntity =  BeanMapperUtils.map(KingdeeDepartmentEntity.class, updateDTO);

        // 数据处理
        handleData(kingdeeDepartmentEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(kingdeeDepartmentEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean init() {
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_DEPARTMENT.getCode());
        //查询
        String fieldKeys = "FDEPTID,FNumber,FName,FUseOrgId.FNumber,FParentID.FNumber";

        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KingdeeDepartmentEntity kingdeeDepartmentEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
