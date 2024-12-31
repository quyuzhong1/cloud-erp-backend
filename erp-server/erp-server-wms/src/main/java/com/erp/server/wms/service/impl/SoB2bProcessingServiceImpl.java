package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.entity.SoB2bProcessingEntity;
import com.erp.model.wms.enums.OrderProcessingLableEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.SoB2bProcessingMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_B2B_PROCESSING;

/**
 * <p>
 * B2B虚拟仓订单跟踪 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Slf4j
@Service
public class SoB2bProcessingServiceImpl extends SuperServiceImpl<SoB2bProcessingMapper, SoB2bProcessingEntity> implements SoB2bProcessingService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private TransferInfoDetailService transferInfoDetailService;

    @Autowired
    private MachineDetailService machineDetailService;

    @Autowired
    private SoOutstockDetailService soOutstockDetailService;


    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<SoB2bProcessingDTO.AddOrUpdateDTO> list) {
        if (CollUtil.isEmpty(list)) {
            lambdaUpdate().remove();
            return Boolean.TRUE;
        }
        // 数据处理
        List<SoB2bProcessingEntity> soB2bProcessingList =  handleData(list);
        List<SoB2bProcessingEntity> addList = soB2bProcessingList.stream().filter(obj -> CharSequenceUtil.isBlank(obj.getId())).collect(Collectors.toList());
        List<SoB2bProcessingEntity> updateList = soB2bProcessingList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(addList)) {
            super.saveBatch(addList);
        }
        if (CollUtil.isNotEmpty(updateList)) {
            super.updateBatchById(updateList);
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<SoB2bProcessingDTO.ListDTO> paging(PagingDTO<SoB2bProcessingDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<SoB2bProcessingDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(SoB2bProcessingDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("B2B虚拟仓列表信息", EXPORT_WMS_SO_B2B_PROCESSING.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public void autoUpdateSoB2bProcessing(LocalDate startDate) {
       List<SoB2bProcessingEntity> list = baseMapper.listSoB2bProcessing(startDate);
       if (CollUtil.isEmpty(list)) {
           return;
       }
        List<String> skuIdList = list.stream().map(SoB2bProcessingEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        /**
         * 1、b2b非组合品订单中转走直接调拨单出库，非中转走销售出库单出库
         * 2、b2b组合品走加工单出库
         */
        //查询加工单数据
        List<String> deliveryNoticeIdList = list.stream().map(SoB2bProcessingEntity::getDeliveryNoticeId).distinct().collect(Collectors.toList());
        List<SoB2bProcessingDTO.ResponseDTO> machineList = machineDetailService.listMachineBySourceIdList(deliveryNoticeIdList);

        //直接调拨单数据
        List<SoB2bProcessingDTO.ResponseDTO> transferList = transferInfoDetailService.listTransferBySourceIdList(deliveryNoticeIdList);

        //销售出库单数据
        List<SoB2bProcessingDTO.ResponseDTO> soOutstockList = soOutstockDetailService.listSoOutstockBySourceIdList(deliveryNoticeIdList);

        List<SoB2bProcessingDTO.AddOrUpdateDTO> addList = new ArrayList<>();
        for (SoB2bProcessingEntity entity :list) {
            //根据类型更新出库数据
            handleOutstockByType(machineList,transferList,soOutstockList,entity);

            //bom信息
            List<BomChildrenSkuDTO> childList = bomChildrenSkuList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getParentSkuId(), entity.getSkuId())
                    &&  CharSequenceUtil.equals(obj.getType(), BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
            if (CollUtil.isEmpty(childList)) {
                SoB2bProcessingDTO.AddOrUpdateDTO addDTO = new SoB2bProcessingDTO.AddOrUpdateDTO();
                BeanMapperUtils.copy(entity,addDTO);
                addList.add(addDTO);
                continue;
            }
            for (BomChildrenSkuDTO childrenSkuDTO : childList) {
                SoB2bProcessingDTO.AddOrUpdateDTO addDTO = new SoB2bProcessingDTO.AddOrUpdateDTO();
                BeanMapperUtils.copy(entity,addDTO);
                addDTO.setSkuId(childrenSkuDTO.getSkuId());
                addDTO.setParentSkuId(childrenSkuDTO.getParentSkuId());
                addDTO.setOutstockQty(ObjectUtil.isEmpty(addDTO.getOutstockQty()) ? MathUtil.ZERO : addDTO.getOutstockQty() * childrenSkuDTO.getQuantity());
                addDTO.setFrozenQty(ObjectUtil.isEmpty(addDTO.getFrozenQty()) ? MathUtil.ZERO :addDTO.getFrozenQty() * childrenSkuDTO.getQuantity());
                addDTO.setSoQty(ObjectUtil.isEmpty(addDTO.getSoQty()) ? MathUtil.ZERO :addDTO.getSoQty() * childrenSkuDTO.getQuantity());
                addDTO.setDeliveryQty(ObjectUtil.isEmpty(addDTO.getDeliveryQty()) ? MathUtil.ZERO :addDTO.getDeliveryQty() * childrenSkuDTO.getQuantity());
                addDTO.setBomVersion(childrenSkuDTO.getBomVersion());
                addList.add(addDTO);
            }
        }
        this.update(addList);
    }

    /**
     * 根据类型赋值出库数据
     * @author will
     * @date 2024/12/31 14:59
     * @param machineList
     * @param transferList
     * @param soOutstockList
     * @param entity
     */
    private void handleOutstockByType (List<SoB2bProcessingDTO.ResponseDTO> machineList, List<SoB2bProcessingDTO.ResponseDTO> transferList,
                                       List<SoB2bProcessingDTO.ResponseDTO> soOutstockList, SoB2bProcessingEntity entity) {

        //加工单
        SoB2bProcessingDTO.ResponseDTO machineResponseDTO = machineList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSourceId(), entity.getDeliveryNoticeId())
                        && CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())
                        &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getDeliveryNoticeDetailId()))
                .findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(machineResponseDTO)) {
            handleOutstock (entity,machineResponseDTO, SourceTypeEnum.MACHINE_INFO.getCode());
            return;
        }

        //直接调拨单
        SoB2bProcessingDTO.ResponseDTO transferResponseDTO = transferList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSourceId(), entity.getDeliveryNoticeId())
                                && CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())
                                && CharSequenceUtil.equals(obj.getWarehouseId(), entity.getWarehouseId())
                                &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getDeliveryNoticeDetailId()))
                .findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(transferResponseDTO)) {
            handleOutstock (entity,transferResponseDTO, SourceTypeEnum.TRANSFER_INFO.getCode());
            return;
        }

        //销售出库单
        SoB2bProcessingDTO.ResponseDTO soOutstockResponseDTO = soOutstockList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSourceId(), entity.getDeliveryNoticeId())
                                && CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())
                                &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getDeliveryNoticeDetailId()))
                .findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(soOutstockResponseDTO)) {
            handleOutstock (entity,soOutstockResponseDTO, SourceTypeEnum.SO_OUTSTOCK.getCode());
        }
    }

    /**
     * 出库字段赋值
     * @author will
     * @date 2024/12/19 20:56
     * @param entity
     * @param responseDTO
     * @param sourceType
     */
    private void handleOutstock (SoB2bProcessingEntity entity, SoB2bProcessingDTO.ResponseDTO responseDTO,String sourceType) {
        entity.setOutstockOrderId(responseDTO.getId());
        entity.setOutstockOrderStatus(responseDTO.getApproveStatus());
        entity.setOutstockQty(responseDTO.getQty());
        entity.setOutstockOrderCode(responseDTO.getCode());
        entity.setOutstockOrderTime(responseDTO.getApproveTime());
        entity.setOutstockOrderType(sourceType);
        if (ApproveStatusEnum.APPROVE.getStatus().equals(responseDTO.getApproveStatus())) {
            entity.setFrozenQty(MathUtil.valueOfZero(entity.getFrozenQty()) - MathUtil.valueOfZero(responseDTO.getQty()));
        }
    }

    /**
    * 新增修改处理数据
    */
    private List<SoB2bProcessingEntity> handleData(List<SoB2bProcessingDTO.AddOrUpdateDTO> list) {
        List<String> soDetailIdList = list.stream().map(SoB2bProcessingDTO.AddOrUpdateDTO::getSoDetailId).distinct().collect(Collectors.toList());
        List<SoB2bProcessingEntity> oldList = this.listBySoDetailIdList(soDetailIdList);
        List<SoB2bProcessingEntity> newList = new ArrayList<>();
        for (SoB2bProcessingDTO.AddOrUpdateDTO addOrUpdateDTO :list) {
            SoB2bProcessingEntity entity = new SoB2bProcessingEntity();
            BeanMapperUtils.copy(addOrUpdateDTO,entity);
            //旧数据
            SoB2bProcessingEntity old = oldList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getSoDetailId(), addOrUpdateDTO.getSoDetailId())
                    && CharSequenceUtil.equals(obj.getDeliveryNoticeDetailId(), addOrUpdateDTO.getDeliveryNoticeDetailId())
                    && CharSequenceUtil.equals(obj.getSkuId(),addOrUpdateDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(old)) {
                entity.setId(old.getId());
            }
            newList.add(entity);
        }
        List<String> deleteIds = getDeleteIds(newList, oldList);
        if (CollUtil.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        return newList;
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<SoB2bProcessingEntity> newList, List<SoB2bProcessingEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoB2bProcessingEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoB2bProcessingEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 根据发货通知单明细id查询
     * @author will
     * @date 2024/12/20 10:17
     * @param soDetailIdList
     * @return List<SoB2bProcessingEntity>
     */
    private List<SoB2bProcessingEntity> listBySoDetailIdList (List<String> soDetailIdList) {
        if (CollUtil.isEmpty(soDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(SoB2bProcessingEntity::getSoDetailId,soDetailIdList).list();
    }

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:24
     * @param list
     */
    private void fillPageData(List<SoB2bProcessingDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (SoB2bProcessingDTO.ListDTO listDTO : list) {
            //发货通知单审核状态名称
            listDTO.setApproveStatusName(ApproveStatusEnum.getName(listDTO.getDeliveryNoticeApproveStatus()));
            //销售订单审核状态名称
            listDTO.setSoApproveStatusName(ApproveStatusEnum.getName(listDTO.getSoApproveStatus()));
            listDTO.setOutstockOrderTypeName(SourceTypeEnum.getName(listDTO.getOutstockOrderType()));
            //冻结时长
            listDTO.setFrozenDays(ObjectUtil.isEmpty(listDTO.getFrozenTime()) ? null : (Math.toIntExact(LocalDate.now().toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay())));

            List<String> labelList = new ArrayList<>();
            if (CharSequenceUtil.isNotBlank(listDTO.getOutstockOrderId())) {
                labelList.add(OrderProcessingLableEnum.OUTSTOCK.getCode());
                LocalDate localDate = ObjUtil.isEmpty(listDTO.getOutstockOrderTime()) ? LocalDate.now() : listDTO.getOutstockOrderTime().toLocalDate();
                //冻结时长
                listDTO.setFrozenDays(Math.toIntExact(localDate.toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay()) + 1);
            }
            if (MathUtil.compareTo(listDTO.getFrozenQty(),MathUtil.ZERO) != MathUtil.ZERO && CharSequenceUtil.isNotBlank(listDTO.getDeliveryNoticeId()) && CharSequenceUtil.isBlank(listDTO.getOutstockOrderId())) {
                labelList.add(OrderProcessingLableEnum.FROZEN.getCode());
            }
            if (CharSequenceUtil.isBlank(listDTO.getOutstockOrderId()) && ObjectUtil.isNotEmpty(listDTO.getFrozenTime()) && (LocalDate.now().toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay() >= 7)) {
                labelList.add(OrderProcessingLableEnum.UN_SHIPPED.getCode());
            }
            //订单冻结
            if (CharSequenceUtil.isBlank(listDTO.getDeliveryNoticeId())
                    && ObjUtil.isNotNull(listDTO.getFrozenQty())) {
                labelList.add(OrderProcessingLableEnum.ORDER_FROZEN.getCode());
            }
            listDTO.setLabelList(labelList);
            //出库状态
            if (CharSequenceUtil.equals(listDTO.getOutstockOrderType(),SourceTypeEnum.TRANSFER_INFO.getCode())
                    || CharSequenceUtil.equals(listDTO.getOutstockOrderType(),SourceTypeEnum.MACHINE_INFO.getCode())
                    || CharSequenceUtil.equals(listDTO.getOutstockOrderType(),SourceTypeEnum.SO_OUTSTOCK.getCode())) {
                listDTO.setOutstockOrderStatusName(ApproveStatusEnum.getName(listDTO.getOutstockOrderStatus()));
            }
        }
    }
}
