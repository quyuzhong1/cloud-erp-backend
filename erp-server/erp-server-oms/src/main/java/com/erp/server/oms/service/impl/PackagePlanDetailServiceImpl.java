package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.PackagePlanDetailDTO;
import com.erp.model.oms.entity.PackagePlanDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.PackagePlanDetailMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.PackagePlanDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 组包计划明细 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-10-09
 */
@Slf4j
@Service
public class PackagePlanDetailServiceImpl extends SuperServiceImpl<PackagePlanDetailMapper, PackagePlanDetailEntity> implements PackagePlanDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PackagePlanDetailDTO.AddDTO addDTO) {
        PackagePlanDetailEntity packagePlanDetailEntity = new PackagePlanDetailEntity();
        BeanMapperUtils.copy(addDTO, packagePlanDetailEntity);

        // 数据处理
        handleData(packagePlanDetailEntity);

        log.info("开始新增组包计划明细");
        boolean save = super.save(packagePlanDetailEntity);
        if(!save) {
            throw new ServiceException("组包计划明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "组包计划明细" , packagePlanDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKAGE_PLAN.getCode(), packagePlanDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(packagePlanDetailEntity.getId(), packagePlanDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PackagePlanDetailDTO.UpdateDTO addOrUpdateDTO) {
        PackagePlanDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "组包计划明细"));
        PackagePlanDetailEntity packagePlanDetailEntity =  BeanMapperUtils.map(PackagePlanDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(packagePlanDetailEntity);
        log.info("编辑 开始修改组包计划明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(packagePlanDetailEntity);
        if(!save) {
            throw new ServiceException("组包计划明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录组包计划明细日志数据，id：【{}】", packagePlanDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), packagePlanDetailEntity.getId(), "组包计划明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, packagePlanDetailEntity, null, packagePlanDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<PackagePlanDetailEntity> getBySoId(String soId) {
        if (CharSequenceUtil.isBlank(soId)){
            return null;
        }
        return this.lambdaQuery().select(PackagePlanDetailEntity::getId,PackagePlanDetailEntity::getMainId,PackagePlanDetailEntity::getSoId).eq(PackagePlanDetailEntity::getSoId, soId).list();
    }

    @Override
    public void updateBarcodeBySoId(String packagePlanId, String soId, String barcode) {
        if (CharSequenceUtil.isBlank(packagePlanId) || CharSequenceUtil.isBlank(soId) || CharSequenceUtil.isBlank(barcode)){
            return;
        }
        this.lambdaUpdate().set(PackagePlanDetailEntity::getBarcode, barcode).eq(PackagePlanDetailEntity::getMainId, packagePlanId).eq(PackagePlanDetailEntity::getSoId, soId).update();
    }

    @Override
    public List<PackagePlanDetailEntity> getByMainIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)){
            return null;
        }
        return this.lambdaQuery().in(PackagePlanDetailEntity::getMainId, ids).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PackagePlanDetailEntity packagePlanDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
