package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.WarehouseLocationMoveDetailEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.server.wms.mapper.VirtualWarehouseAllocationDetailMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 虚拟仓分货单明细 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
@Slf4j
@Service
public class VirtualWarehouseAllocationDetailServiceImpl extends SuperServiceImpl<VirtualWarehouseAllocationDetailMapper, VirtualWarehouseAllocationDetailEntity> implements VirtualWarehouseAllocationDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;
    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO batchAdd(VirtualWarehouseAllocationDetailDTO.AddDTO addDTO) {
        VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity = new VirtualWarehouseAllocationDetailEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseAllocationDetailEntity);

        // 数据处理
        handleData(virtualWarehouseAllocationDetailEntity);

        log.info("开始新增分货单明细");
        boolean save = super.save(virtualWarehouseAllocationDetailEntity);
        if (!save) {
            throw new ServiceException("分货单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "分货单明细", virtualWarehouseAllocationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, virtualWarehouseAllocationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(virtualWarehouseAllocationDetailEntity.getId(), virtualWarehouseAllocationDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchUpdate(VirtualWarehouseAllocationDetailDTO.UpdateDTO updateDTO) {
        VirtualWarehouseAllocationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "分货单明细"));
        VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity = BeanMapperUtils.map(VirtualWarehouseAllocationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseAllocationDetailEntity);
        log.info("编辑 开始修改分货单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(virtualWarehouseAllocationDetailEntity);
        if (!save) {
            throw new ServiceException("分货单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录分货单明细日志数据，id：【{}】", virtualWarehouseAllocationDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseAllocationDetailEntity.getId(), "分货单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseAllocationDetailEntity, null, virtualWarehouseAllocationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdd(VirtualWarehouseAllocationDTO.AddDTO addDTO, String mainId) {
        List<VirtualWarehouseAllocationDetailEntity> detailEntityList = BeanMapperUtils.copyList(VirtualWarehouseAllocationDetailEntity.class, addDTO.getDetailList());
        handleData(detailEntityList, mainId);
        boolean save = super.saveBatch(detailEntityList);
        if (!save) {
            throw new ServiceException(ApiError.ERROR_VMALLOCATION_DETAIL_ADD);
        }
        //添加操作日志
        List<String> addList = detailEntityList.stream().map(VirtualWarehouseAllocationDetailEntity::getId).filter(StringUtils::isBlank).collect(Collectors.toList());
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<VirtualWarehouseAllocationDetailEntity> receiveDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = receiveDetailEntityList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个分货单明细【%s】", ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), addPairList, "新增操作");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdate(VirtualWarehouseAllocationDTO.UpdateDTO updateDTO, String mainId) {
        if (CollectionUtils.isEmpty(updateDTO.getDetailList())) {
            throw new ServiceException(ApiError.ERROR_1040, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getName());
        }
        List<VirtualWarehouseAllocationDetailEntity> oldList = this.listByIds(Collections.singletonList(mainId));
        List<String> deleteIds = getDeleteIds(updateDTO.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<VirtualWarehouseAllocationDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个分货单明细【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }

        List<VirtualWarehouseAllocationDetailEntity> list = BeanMapperUtils.copyList(VirtualWarehouseAllocationDetailEntity.class, updateDTO.getDetailList());
        // 数据处理
        handleData(list, mainId);
        //添加操作日志
        List<String> addList = list.stream().map(VirtualWarehouseAllocationDetailEntity::getId).filter(StringUtils::isBlank).collect(Collectors.toList());
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<VirtualWarehouseAllocationDetailEntity> receiveDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = receiveDetailEntityList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个分货单【%s】", ModuleTypeEnum.WAREHOUSE_RECEIVE.getCode(), addPairList, "编辑操作");
        }
        return this.saveOrUpdateBatch(list);
    }

    /**
     * 手动完结
     *
     * @param vmAllocationDetailEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO manualFinish(VirtualWarehouseAllocationDetailEntity vmAllocationDetailEntity, VirtualWarehouseAllocationEntity vmAllocationEntity, VirtualWarehouseAllocationDTO.ManualFinishDto dto) {
        String existStatus = vmAllocationDetailEntity.getSyncStatus();
        String code = VirtualWarehouseAllocationSyncStatusEnum.MANUAL_COMPLETION_SYNC.getCode();
        if (Objects.equals(existStatus, code)) {
            throw new ServiceException("存在相同的状态");
        }
        vmAllocationDetailEntity.setSyncStatus(code);
        vmAllocationDetailEntity.setFinishDescription(dto.getFinishDescription());
        vmAllocationDetailEntity.setThirdCode(dto.getThirdCode());
        this.updateById(vmAllocationDetailEntity);
        //todo 变更同步表状态
//        //变更明细同步状态
//        virtualWarehouseAllocationDetailService.updateByMainId(vmAllocationEntity.getId(), VirtualWarehouseAllocationSyncStatusEnum.MANUAL_COMPLETION_SYNC.getCode());
        //todo 手动完结中台任务
        return BatchResultDTO.success(vmAllocationDetailEntity.getId(), vmAllocationEntity.getCode(), OperationTypeEnum.DISABLED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(VirtualWarehouseAllocationEntity allocationEntity) {
        //查找所有明细
        List<VirtualWarehouseAllocationDetailEntity> detailList = list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>().eq(VirtualWarehouseAllocationDetailEntity::getMainId, allocationEntity.getId()));
        if (CollectionUtils.isNotEmpty(detailList)) {
            String type = allocationEntity.getType();
            switch (VirtualWarehouseAllocationTypeEnum.getEnum(type)) {
                case ALLOCATION:
                    VirtualInventoryStockDTO.StockParamDTO allocationDto = getAllocationDto(VirtualInventoryBusinessTypeEnum.IN_USABLE, detailList, allocationEntity);
                    virtualInventoryTransCoreService.approve(allocationDto);
                    break;
                case TRANSFER:
                    VirtualInventoryStockDTO.TransferParamDTO dto = getTransferDTO(allocationEntity, detailList);
                    virtualInventoryTransCoreService.approve(dto);
                    break;
                case CANCEL:
                    VirtualInventoryStockDTO.StockParamDTO cancelDto = getAllocationDto(VirtualInventoryBusinessTypeEnum.OUT_USABLE, detailList, allocationEntity);
                    virtualInventoryTransCoreService.approve(cancelDto);
                    break;
                default:
                    throw new ServiceException(ApiError.ERROR_400);
            }
        }
    }

    private static VirtualInventoryStockDTO.TransferParamDTO getTransferDTO(VirtualWarehouseAllocationEntity allocationEntity, List<VirtualWarehouseAllocationDetailEntity> detailList) {
        VirtualInventoryStockDTO.TransferParamDTO dto = new VirtualInventoryStockDTO.TransferParamDTO();
        dto.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_USABLE.getCode());
        List<VirtualInventoryStockDTO.TransferStockDTO> paramList = new ArrayList<>();

        detailList.forEach(detailDto -> {
            VirtualInventoryStockDTO.TransferStockDTO outInStockDTO = new VirtualInventoryStockDTO.TransferStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(allocationEntity.getId());
            outInStockDTO.setSourceCode(allocationEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION);
            outInStockDTO.setSourceDetailId(detailDto.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailDto.getSkuId());
            outInStockDTO.setSkuNo(detailDto.getSkuNo());
            outInStockDTO.setWarehouseId(detailDto.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(detailDto.getFromVirtualWarehouseId());
            outInStockDTO.setVirtualCurWarehouseId(detailDto.getFromVirtualWarehouseId());
            outInStockDTO.setVirtualTargetWarehouseId(detailDto.getToVirtualWarehouseId());
            outInStockDTO.setQty(detailDto.getQty());
            paramList.add(outInStockDTO);
        });
        dto.setParamList(paramList);
        return dto;
    }

    private static VirtualInventoryStockDTO.StockParamDTO getAllocationDto(VirtualInventoryBusinessTypeEnum inUsable, List<VirtualWarehouseAllocationDetailEntity> detailList, VirtualWarehouseAllocationEntity allocationEntity) {
        VirtualInventoryStockDTO.StockParamDTO allocationDto = new VirtualInventoryStockDTO.StockParamDTO();
        allocationDto.setBusinessType(inUsable.getCode());
        List<VirtualInventoryStockDTO.OutInStockDTO> allocationParamList = new ArrayList<>();
        detailList.forEach(detailDto -> {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(allocationEntity.getId());
            outInStockDTO.setSourceCode(allocationEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION);
            outInStockDTO.setSourceDetailId(detailDto.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailDto.getSkuId());
            outInStockDTO.setSkuNo(detailDto.getSkuNo());
            outInStockDTO.setWarehouseId(detailDto.getWarehouseId());
            outInStockDTO.setVirtualWarehouseId(detailDto.getFromVirtualWarehouseId());
            outInStockDTO.setQty(detailDto.getQty());
            allocationParamList.add(outInStockDTO);
        });
        allocationDto.setParamList(allocationParamList);
        return allocationDto;
    }

    @Override
    public void updateByMainId(String mainId, String syncStatus) {
        this.update(new LambdaUpdateWrapper<VirtualWarehouseAllocationDetailEntity>().eq(VirtualWarehouseAllocationDetailEntity::getMainId, mainId)
                .set(VirtualWarehouseAllocationDetailEntity::getSyncStatus, syncStatus));
    }

    @Override
    public BatchResultDTO sync(VirtualWarehouseAllocationDetailEntity vmAllocationDetailEntity, VirtualWarehouseAllocationEntity vmAllocationEntity) {
        //todo 同步中台数据
        //变更明细同步状态
        virtualWarehouseAllocationDetailService.updateByMainId(vmAllocationEntity.getId(), VirtualWarehouseAllocationSyncStatusEnum.IN_SYNC.getCode());
        return null;
    }

    private void handleData(List<VirtualWarehouseAllocationDetailEntity> detailEntityList, String mainId) {
        detailEntityList.forEach(detail -> detail.setMainId(mainId));
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<VirtualWarehouseAllocationDTO.DetailDto> newList, List<VirtualWarehouseAllocationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(VirtualWarehouseAllocationDTO.DetailDto::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(VirtualWarehouseAllocationDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseAllocationDetailEntity virtualWarehouseAllocationDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
