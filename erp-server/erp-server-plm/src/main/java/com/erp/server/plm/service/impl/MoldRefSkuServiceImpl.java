package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.server.plm.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.MoldRefSkuEntity;
import com.erp.server.plm.mapper.MoldRefSkuMapper;
import com.erp.server.plm.service.MoldRefSkuService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.MoldRefSkuDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 模具关联sku 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-10
 */
@Slf4j
@Service
public class MoldRefSkuServiceImpl extends SuperServiceImpl<MoldRefSkuMapper, MoldRefSkuEntity> implements MoldRefSkuService {
    @Resource
    private OperateLogService sysLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(MoldRefSkuDTO.AddDTO addDTO) {
        MoldRefSkuEntity moldRefSkuEntity = new MoldRefSkuEntity();
        BeanMapperUtils.copy(addDTO, moldRefSkuEntity);

        // 数据处理
        handleData(moldRefSkuEntity);

        log.info("开始新增模具关联sku");
        boolean save = super.save(moldRefSkuEntity);
        if(!save) {
            throw new ServiceException("模具关联sku保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "模具关联sku" , moldRefSkuEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, moldRefSkuEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(moldRefSkuEntity.getId(), moldRefSkuEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(MoldRefSkuDTO.UpdateDTO addOrUpdateDTO) {
        MoldRefSkuEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "模具关联sku"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        MoldRefSkuEntity moldRefSkuEntity =  BeanMapperUtils.map(MoldRefSkuEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(moldRefSkuEntity);
        log.info("编辑 开始修改模具关联sku数据，id：【{}】", old.getId());
        boolean save = super.updateById(moldRefSkuEntity);
        if(!save) {
            throw new ServiceException("模具关联sku保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录模具关联sku日志数据，id：【{}】", moldRefSkuEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), moldRefSkuEntity.getId(), "模具关联sku");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, moldRefSkuEntity, null, moldRefSkuEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(MoldRefSkuEntity moldRefSkuEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
