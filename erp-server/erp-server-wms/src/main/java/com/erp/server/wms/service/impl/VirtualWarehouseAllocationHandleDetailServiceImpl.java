package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.threadlocal.UserContext;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.server.wms.mapper.VirtualWarehouseAllocationHandleDetailMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseAllocationHandleDetailDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 分货单拆单明细表 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-07
 */
@Slf4j
@Service
public class VirtualWarehouseAllocationHandleDetailServiceImpl extends SuperServiceImpl<VirtualWarehouseAllocationHandleDetailMapper, VirtualWarehouseAllocationHandleDetailEntity> implements VirtualWarehouseAllocationHandleDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;
    @Resource
    private VirtualWarehouseAllocationHandleRelationService virtualWarehouseAllocationHandleRelationService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseAllocationHandleDetailDTO.AddDTO addDTO) {
        VirtualWarehouseAllocationHandleDetailEntity virtualWarehouseAllocationHandleDetailEntity = new VirtualWarehouseAllocationHandleDetailEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseAllocationHandleDetailEntity);

        // 数据处理
        handleData(virtualWarehouseAllocationHandleDetailEntity);

        log.info("开始新增分货单拆单明细单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        virtualWarehouseAllocationHandleDetailEntity.setCode(code);
        boolean save = super.save(virtualWarehouseAllocationHandleDetailEntity);
        if (!save) {
            throw new ServiceException("分货单拆单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "分货单拆单明细单", virtualWarehouseAllocationHandleDetailEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehouseAllocationHandleDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehouseAllocationHandleDetailEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseAllocationHandleDetailDTO.UpdateDTO updateDTO) {
        VirtualWarehouseAllocationHandleDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "分货单拆单明细单"));
        VirtualWarehouseAllocationHandleDetailEntity virtualWarehouseAllocationHandleDetailEntity = BeanMapperUtils.map(VirtualWarehouseAllocationHandleDetailEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseAllocationHandleDetailEntity);
        log.info("编辑 开始修改分货单拆单明细单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(virtualWarehouseAllocationHandleDetailEntity);
        if (!save) {
            throw new ServiceException("分货单拆单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录分货单拆单明细单日志数据，单号：【{}】", virtualWarehouseAllocationHandleDetailEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseAllocationHandleDetailEntity.getCode(), "分货单拆单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseAllocationHandleDetailEntity, null, virtualWarehouseAllocationHandleDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleDetail(VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehouseAllocationHandleEntity allocationHandleEntity) {
        //获取所有明细
        List<VirtualWarehouseAllocationDetailEntity> vmAllocationDetailList = virtualWarehouseAllocationDetailService.list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>()
                .eq(VirtualWarehouseAllocationDetailEntity::getMainId, allocationEntity.getId()));
        List<VirtualWarehouseAllocationHandleDetailEntity> handleDetailList = new ArrayList<>();
        List<VirtualWarehouseAllocationHandleRelationEntity> handleRelationList = new ArrayList<>();
        String type = allocationEntity.getType();
        switch (VirtualWarehouseAllocationTypeEnum.getEnum(type)) {
            case ALLOCATION:
                Map<String, List<VirtualWarehouseAllocationDetailEntity>> allocationMap = vmAllocationDetailList.stream().collect(Collectors.groupingBy(VirtualWarehouseAllocationDetailEntity::getToVirtualWarehouseId));
                allocationMap.forEach((toVmId, allocationDetailList) -> {
                    //获取
                    //保存拆单明细
                    VirtualWarehouseAllocationHandleDetailEntity handleDetailEntity = getHandleDetailEntity(allocationEntity);
                    handleDetailEntity.setToVirtualWarehouseId(toVmId);
                    this.save(handleDetailEntity);
                    handleDetailList.add(handleDetailEntity);
                    allocationDetailList.forEach(allocationDetail -> {
                        VirtualWarehouseAllocationHandleRelationEntity vmAllocationHandleRelationEntity = getHandleRelationEntity(allocationEntity, allocationHandleEntity, allocationDetail, handleDetailEntity);
                        virtualWarehouseAllocationHandleRelationService.save(vmAllocationHandleRelationEntity);
                    });
                });
                break;
            case CANCEL:
                Map<String, List<VirtualWarehouseAllocationDetailEntity>> map = vmAllocationDetailList.stream().collect(Collectors.groupingBy(VirtualWarehouseAllocationDetailEntity::getFromVirtualWarehouseId));
                map.forEach((fromVmId, allocationDetailList) -> {
                    //保存拆单明细
                    VirtualWarehouseAllocationHandleDetailEntity handleDetailEntity = getHandleDetailEntity(allocationEntity);
                    handleDetailEntity.setFromVirtualWarehouseId(fromVmId);
                    this.save(handleDetailEntity);
                    handleDetailList.add(handleDetailEntity);
                    allocationDetailList.forEach(allocationDetail -> {
                        VirtualWarehouseAllocationHandleRelationEntity vmAllocationHandleRelationEntity = getHandleRelationEntity(allocationEntity, allocationHandleEntity, allocationDetail, handleDetailEntity);
                        virtualWarehouseAllocationHandleRelationService.save(vmAllocationHandleRelationEntity);
                    });
                });
                break;
            case TRANSFER:
                //两种情况

                break;
        }
        //todo 推送中台任务
    }

    private static VirtualWarehouseAllocationHandleRelationEntity getHandleRelationEntity(VirtualWarehouseAllocationEntity allocationEntity, VirtualWarehouseAllocationHandleEntity allocationHandleEntity, VirtualWarehouseAllocationDetailEntity allocationDetail, VirtualWarehouseAllocationHandleDetailEntity handleDetailEntity) {
        VirtualWarehouseAllocationHandleRelationEntity vmAllocationHandleRelationEntity = new VirtualWarehouseAllocationHandleRelationEntity();
        vmAllocationHandleRelationEntity.setAllocationId(allocationEntity.getId());
        vmAllocationHandleRelationEntity.setAllocationDetailId(allocationDetail.getId());
        vmAllocationHandleRelationEntity.setHandleId(allocationHandleEntity.getId());
        vmAllocationHandleRelationEntity.setHandleDetailId(handleDetailEntity.getAllocationId());
        return vmAllocationHandleRelationEntity;
    }

    private static VirtualWarehouseAllocationHandleDetailEntity getHandleDetailEntity(VirtualWarehouseAllocationEntity allocationEntity) {
        VirtualWarehouseAllocationHandleDetailEntity handleDetailEntity = new VirtualWarehouseAllocationHandleDetailEntity();
        handleDetailEntity.setAllocationId(allocationEntity.getId());
        handleDetailEntity.setDirection(allocationEntity.getDirection());
        handleDetailEntity.setType(allocationEntity.getType());
        handleDetailEntity.setStatus(allocationEntity.getStatus());
//        handleDetailEntity.setToVirtualWarehouseId(allocationEntity.get);
//        handleDetailEntity.setFromVirtualWarehouseId(allocationEntity.getDirection());
        //
        return handleDetailEntity;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseAllocationHandleDetailEntity virtualWarehouseAllocationHandleDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
