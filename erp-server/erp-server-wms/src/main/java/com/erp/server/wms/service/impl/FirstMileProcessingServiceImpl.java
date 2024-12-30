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
import com.erp.model.wms.dto.FirstMileProcessingDTO;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.entity.FirstMileProcessingEntity;
import com.erp.model.wms.enums.OrderProcessingLableEnum;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.FirstMileProcessingMapper;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FIRST_MILE_PROCESSING;

/**
 * <p>
 * 头程虚拟仓订单跟踪 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Slf4j
@Service
public class FirstMileProcessingServiceImpl extends SuperServiceImpl<FirstMileProcessingMapper, FirstMileProcessingEntity> implements FirstMileProcessingService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private MachineDetailService machineDetailService;

    @Resource
    private TransferInfoDetailService transferInfoDetailService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;


    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<FirstMileProcessingDTO.AddOrUpdateDTO> list) {
        if (CollUtil.isEmpty(list)) {
            lambdaUpdate().remove();
            return Boolean.TRUE;
        }
        // 数据处理
        List<FirstMileProcessingEntity> firstMileProcessingList =  handleData(list);
        List<FirstMileProcessingEntity> addList = firstMileProcessingList.stream().filter(obj -> CharSequenceUtil.isBlank(obj.getId())).collect(Collectors.toList());
        List<FirstMileProcessingEntity> updateList = firstMileProcessingList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(addList)) {
            super.saveBatch(addList);
        }
        if (CollUtil.isNotEmpty(updateList)) {
            super.updateBatchById(updateList);
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<FirstMileProcessingDTO.ListDTO> paging(PagingDTO<FirstMileProcessingDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<FirstMileProcessingDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(FirstMileProcessingDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("头程虚拟仓列表信息", EXPORT_WMS_FIRST_MILE_PROCESSING.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public void autoUpdateFirstMileProcessing(LocalDate startDate) {
        List<FirstMileProcessingEntity> list = baseMapper.listFirstMileProcessing(startDate);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<String> skuIdList = list.stream().map(FirstMileProcessingEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        /**
         * 1、头程非组合品订单中转走直接调拨单出库，非中转走销售出库单出库
         * 2、头程组合品走加工单出库
         */
        //查询加工单数据
        List<String> deliveryIdList = list.stream().map(FirstMileProcessingEntity::getFirstMileDeliveryId).distinct().collect(Collectors.toList());
        List<SoB2bProcessingDTO.ResponseDTO> machineList = machineDetailService.listMachineBySourceIdList(deliveryIdList);

        //直接调拨单数据
        List<SoB2bProcessingDTO.ResponseDTO> transferList = transferInfoDetailService.listTransferBySourceIdList(deliveryIdList);

        //销售出库单数据
        List<SoB2bProcessingDTO.ResponseDTO> soOutstockList = soOutstockDetailService.listSoOutstockBySourceIdList(deliveryIdList);

        List<FirstMileProcessingDTO.AddOrUpdateDTO> addList = new ArrayList<>();
        for (FirstMileProcessingEntity entity :list) {

            //加工单
            SoB2bProcessingDTO.ResponseDTO machineResponseDTO = machineList.stream().filter(obj ->
                            CharSequenceUtil.equals(obj.getSourceId(), entity.getFirstMileDeliveryId())
                            &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getFirstMileDeliveryDetailId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(machineResponseDTO)) {
                handleOutstock (entity,machineResponseDTO, SourceTypeEnum.MACHINE_INFO.getCode());
            }
            //直接调拨单
            SoB2bProcessingDTO.ResponseDTO transferResponseDTO = transferList.stream().filter(obj ->
                            CharSequenceUtil.equals(obj.getSourceId(), entity.getFirstMileDeliveryId())
                            &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getFirstMileDeliveryDetailId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(transferResponseDTO)) {
                handleOutstock (entity,transferResponseDTO, SourceTypeEnum.TRANSFER_INFO.getCode());
            }
            //销售出库单
            SoB2bProcessingDTO.ResponseDTO soOutstockResponseDTO = soOutstockList.stream().filter(obj ->
                            CharSequenceUtil.equals(obj.getSourceId(), entity.getFirstMileDeliveryId())
                            &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getFirstMileDeliveryDetailId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soOutstockResponseDTO)) {
                handleOutstock (entity,soOutstockResponseDTO, SourceTypeEnum.SO_OUTSTOCK.getCode());
            }
            //bom信息
            List<BomChildrenSkuDTO> childList = bomChildrenSkuList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getParentSkuId(), entity.getSkuId())
                    &&  CharSequenceUtil.equals(obj.getType(), BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
            if (CollUtil.isEmpty(childList)) {
                FirstMileProcessingDTO.AddOrUpdateDTO addDTO = new FirstMileProcessingDTO.AddOrUpdateDTO();
                BeanMapperUtils.copy(entity,addDTO);
                addList.add(addDTO);
                continue;
            }
            for (BomChildrenSkuDTO childrenSkuDTO : childList) {
                FirstMileProcessingDTO.AddOrUpdateDTO addOrUpdateDTO = new FirstMileProcessingDTO.AddOrUpdateDTO();
                BeanMapperUtils.copy(entity,addOrUpdateDTO);
                addOrUpdateDTO.setSkuId(childrenSkuDTO.getSkuId());
                addOrUpdateDTO.setParentSkuId(childrenSkuDTO.getParentSkuId());
                addOrUpdateDTO.setOutstockQty(ObjectUtil.isEmpty(addOrUpdateDTO.getOutstockQty()) ? MathUtil.ZERO : addOrUpdateDTO.getOutstockQty() * childrenSkuDTO.getQuantity());
                addOrUpdateDTO.setFrozenQty(ObjectUtil.isEmpty(addOrUpdateDTO.getFrozenQty()) ? MathUtil.ZERO :addOrUpdateDTO.getFrozenQty() * childrenSkuDTO.getQuantity());
                addOrUpdateDTO.setApproveQty(ObjectUtil.isEmpty(addOrUpdateDTO.getDeliveryQty()) ? MathUtil.ZERO :addOrUpdateDTO.getApproveQty() * childrenSkuDTO.getQuantity());
                addOrUpdateDTO.setDeliveryQty(ObjectUtil.isEmpty(addOrUpdateDTO.getDeliveryQty()) ? MathUtil.ZERO :addOrUpdateDTO.getDeliveryQty() * childrenSkuDTO.getQuantity());
                addOrUpdateDTO.setBomVersion(childrenSkuDTO.getBomVersion());
                addList.add(addOrUpdateDTO);
            }
        }
        this.update(addList);
    }


    /**
     * 新增修改处理数据
     */
    private List<FirstMileProcessingEntity> handleData(List<FirstMileProcessingDTO.AddOrUpdateDTO> list) {
        List<String> detailIdList = list.stream().map(FirstMileProcessingDTO.AddOrUpdateDTO::getRequisitionApplicationDetailId).distinct().collect(Collectors.toList());
        List<FirstMileProcessingEntity> oldList = this.listByApplicationDetailIdList(detailIdList);
        List<FirstMileProcessingEntity> newList = new ArrayList<>();
        for (FirstMileProcessingDTO.AddOrUpdateDTO addOrUpdateDTO :list) {
            FirstMileProcessingEntity entity = new FirstMileProcessingEntity();
            BeanMapperUtils.copy(addOrUpdateDTO,entity);
            //旧数据
            FirstMileProcessingEntity old = oldList.stream().filter(obj ->
                    CharSequenceUtil.equals(obj.getRequisitionApplicationId(), addOrUpdateDTO.getRequisitionApplicationId())
                    && CharSequenceUtil.equals(obj.getRequisitionApplicationDetailId(), addOrUpdateDTO.getRequisitionApplicationDetailId())
                    && CharSequenceUtil.equals(obj.getFirstMileDeliveryDetailId(), addOrUpdateDTO.getFirstMileDeliveryDetailId())
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
    private List<String> getDeleteIds(List<FirstMileProcessingEntity> newList, List<FirstMileProcessingEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(FirstMileProcessingEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(FirstMileProcessingEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 出库字段赋值
     * @author will
     * @date 2024/12/19 20:56
     * @param entity
     * @param responseDTO
     * @param sourceType
     */
    private void handleOutstock (FirstMileProcessingEntity entity, SoB2bProcessingDTO.ResponseDTO responseDTO,String sourceType) {
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
     * 根据发货通知单明细id查询
     * @author will
     * @date 2024/12/20 10:17
     * @param applicationDetailIdList
     * @return List<FirstMileProcessingEntity>
     */
    private List<FirstMileProcessingEntity> listByApplicationDetailIdList (List<String> applicationDetailIdList) {
        if (CollUtil.isEmpty(applicationDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(FirstMileProcessingEntity::getRequisitionApplicationDetailId,applicationDetailIdList).list();
    }

    /**
     * 分页查询
     * @author will
     * @date 2024/12/18 11:24
     * @param list
     */
    private void fillPageData(List<FirstMileProcessingDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (FirstMileProcessingDTO.ListDTO listDTO : list) {
            //发货单审核状态名称
            listDTO.setDeliveryApproveStatusName(ApproveStatusEnum.getName(listDTO.getDeliveryApproveStatus()));
            //要货申请状态名称
            listDTO.setRequisitionApplicationStatusName(RequisitionApplicationStatusEnum.getName(listDTO.getRequisitionApplicationStatus()));
            listDTO.setOutstockOrderTypeName(SourceTypeEnum.getName(listDTO.getOutstockOrderType()));
            //冻结时长
            listDTO.setFrozenDays(ObjectUtil.isEmpty(listDTO.getFrozenTime()) ? null : (Math.toIntExact(LocalDate.now().toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay())));

            //标签
            List<String> labelList = new ArrayList<>();
            //已出
            if (CharSequenceUtil.isNotBlank(listDTO.getOutstockOrderId())) {
                labelList.add(OrderProcessingLableEnum.OUTSTOCK.getCode());
                LocalDate localDate = ObjUtil.isEmpty(listDTO.getOutstockOrderTime()) ? LocalDate.now() : listDTO.getOutstockOrderTime().toLocalDate();
                //冻结时长
                listDTO.setFrozenDays(Math.toIntExact(localDate.toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay()) + 1);
            }
            //发货冻结
            if (MathUtil.compareTo(listDTO.getFrozenQty(),MathUtil.ZERO) != MathUtil.ZERO && CharSequenceUtil.isNotBlank(listDTO.getFirstMileDeliveryId())) {
                labelList.add(OrderProcessingLableEnum.FROZEN.getCode());
            }
            //七日未发
            if (CharSequenceUtil.isBlank(listDTO.getOutstockOrderId()) && ObjectUtil.isNotEmpty(listDTO.getFrozenTime()) && (LocalDate.now().toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay() >= 7)) {
                labelList.add(OrderProcessingLableEnum.UN_SHIPPED.getCode());
            }
            //要货冻结
            if (CharSequenceUtil.isBlank(listDTO.getFirstMileDeliveryId())
                    && Arrays.asList(RequisitionApplicationStatusEnum.HANDLE_ING.getStatus(),RequisitionApplicationStatusEnum.HANDLE.getStatus()).contains(listDTO.getRequisitionApplicationStatus())) {
                labelList.add(OrderProcessingLableEnum.REQUISITION_FROZEN.getCode());
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
