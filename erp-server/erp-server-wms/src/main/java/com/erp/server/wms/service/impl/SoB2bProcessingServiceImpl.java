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
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.wms.dto.ReportProcessingDTO;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.entity.SoB2bProcessingEntity;
import com.erp.model.wms.enums.OrderProcessingLableEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.SoB2bProcessingConverter;
import com.erp.server.wms.mapper.SoB2bProcessingMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    @Override
    public Boolean addOrUpdate(List<SoB2bProcessingDTO.AddOrUpdateDTO> list,LocalDate startDate) {

        //删除多余b2b订单
        baseMapper.deleteB2bOrder(startDate);

        if (CollUtil.isEmpty(list)) {
            return Boolean.TRUE;
        }
        // 数据处理
        List<SoB2bProcessingEntity> soB2bProcessingList =  handleData(list);
        if (CollUtil.isEmpty(soB2bProcessingList)) {
            return Boolean.TRUE;
        }
        return super.saveBatch(soB2bProcessingList);
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
        log.warn("查询b2b订单跟踪数据，startDate = {}，size = {}",startDate,list.size());
        List<String> skuIdList = list.stream().map(SoB2bProcessingEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        Map<String, List<BomChildrenSkuDTO>> bomMap = bomChildrenSkuList.stream()
                .collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}_{}", obj.getParentSkuId(), obj.getType())));
        /**
         * 1、b2b非组合品订单中转走直接调拨单出库，非中转走销售出库单出库
         * 2、b2b组合品走加工单出库
         */
        List<String> deliveryNoticeIdList = list.stream().map(SoB2bProcessingEntity::getDeliveryNoticeId).distinct().collect(Collectors.toList());
        List<List<String>> sourceIdListPartition = Lists.partition(deliveryNoticeIdList, 30000);

        //查询加工单数据
        List<SoB2bProcessingDTO.ResponseDTO> machineList = new ArrayList<>();
        //直接调拨单数据
        List<SoB2bProcessingDTO.ResponseDTO> transferList = new ArrayList<>();
        //销售出库单数据
        List<SoB2bProcessingDTO.ResponseDTO> soOutstockList = new ArrayList<>();
        //分页查询数据
        for (List<String> sourceIdPartition : sourceIdListPartition) {
            //销售订单id集合
            List<String> soIdList = list.stream().filter(obj -> sourceIdPartition.contains(obj.getDeliveryNoticeId()))
                    .map(SoB2bProcessingEntity::getSoId).distinct().collect(Collectors.toList());
            soIdList.addAll(sourceIdPartition);
            List<SoB2bProcessingDTO.ResponseDTO> machinePageList = machineDetailService.listMachineBySourceIdList(soIdList);
            if (CollectionUtils.isNotEmpty(machinePageList)) {
                machineList.addAll(machinePageList);
            }
            List<SoB2bProcessingDTO.ResponseDTO> transferPageList = transferInfoDetailService.listTransferBySourceIdList(sourceIdPartition);
            if (CollectionUtils.isNotEmpty(transferPageList)) {
                transferList.addAll(transferPageList);
            }
            List<SoB2bProcessingDTO.ResponseDTO> soOutstockPageList = soOutstockDetailService.listSoOutstockBySourceIdList(sourceIdPartition);
            if (CollectionUtils.isNotEmpty(soOutstockPageList)) {
                soOutstockList.addAll(soOutstockPageList);
            }
        }
        log.warn("查询b2b订单跟踪数据，machineList = {}，transferList = {}，soOutstockList = {}",machineList.size(),transferList.size(),soOutstockList.size());
        List<SoB2bProcessingDTO.AddOrUpdateDTO> addList = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<List<SoB2bProcessingDTO.AddOrUpdateDTO>> result =  list.parallelStream().map(entity -> {
            //判断是否需要出库
            if (entity.getIsOutstock()) {
                //根据类型更新出库数据
                handleOutstockByType(machineList,transferList,soOutstockList,entity);
                log.warn("根据类型处理成功！code = {}",entity.getSoCode());
            }
            // 新增数据
            List<SoB2bProcessingDTO.AddOrUpdateDTO> localList = new ArrayList<>();
            addBomList(localList,entity,bomMap.get(CharSequenceUtil.format("{}_{}",entity.getSkuId(),BomTypeEnum.COMBINATION.getType())));
            log.warn("按bom处理数据成功！code = {}",entity.getSoCode());
            return localList;
        }).collect(Collectors.toList());
        // 合并所有局部列表
        result.forEach(addList::addAll);
        stopWatch.stop();
        log.warn("数据处理成功，耗时，time = {}",stopWatch.prettyPrint());
        ApplicationContextUtils.getBean(SoB2bProcessingServiceImpl.class).addOrUpdate(addList,startDate);
        log.warn("数据更新成功!");
    }

    @Override
    public Boolean deleteB2bProcessing(SoB2bProcessingDTO.DeleteDTO dto) {
        return baseMapper.deleteB2bProcessing(dto);
    }

    @Override
    public PagingVO<ReportProcessingDTO.ListDTO> b2bTotalPaging(PagingDTO<ReportProcessingDTO.PagingParamDTO> dto) {
        return null;
    }

    /**
     * 添加bom数据
     * @author will
     * @date 2025/1/2 10:55
     * @param addList
     * @param entity
     * @param bomChildrenSkuList
     */
    private void addBomList (List<SoB2bProcessingDTO.AddOrUpdateDTO> addList,SoB2bProcessingEntity entity,List<BomChildrenSkuDTO> bomChildrenSkuList) {
        if (CollUtil.isEmpty(bomChildrenSkuList)) {
            SoB2bProcessingDTO.AddOrUpdateDTO addDTO = SoB2bProcessingConverter.INSTANCE.entityToAdd(entity);
            addDTO.setParentSkuId("");
            addDTO.setBomVersion("");
            addList.add(addDTO);
            return;
        }
        for (BomChildrenSkuDTO childrenSkuDTO : bomChildrenSkuList) {
            SoB2bProcessingDTO.AddOrUpdateDTO addDTO = SoB2bProcessingConverter.INSTANCE.entityToAdd(entity);
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
                        (CharSequenceUtil.equals(SourceTypeEnum.SO_INFO.getCode(),obj.getSourceType())  && CharSequenceUtil.equals(obj.getSourceId(), entity.getSoId()) &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getSoDetailId())
                                || CharSequenceUtil.equals(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode(),obj.getSourceType())  && CharSequenceUtil.equals(obj.getSourceId(), entity.getDeliveryNoticeId()) &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getDeliveryNoticeDetailId()))
                                && CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus()))
                .findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(machineResponseDTO)) {
            handleOutstock (entity,machineResponseDTO, SourceTypeEnum.MACHINE_INFO.getCode());
            return;
        }

        //直接调拨单
        SoB2bProcessingDTO.ResponseDTO transferResponseDTO = transferList.stream().filter(obj ->
                                CharSequenceUtil.isNotBlank(entity.getDeliveryNoticeId())
                                && CharSequenceUtil.equals(obj.getSourceId(), entity.getDeliveryNoticeId())
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
                                CharSequenceUtil.isNotBlank(entity.getDeliveryNoticeId())
                                && CharSequenceUtil.equals(obj.getSourceId(), entity.getDeliveryNoticeId())
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
        entity.setFrozenQty(MathUtil.valueOfZero(entity.getFrozenQty()) - MathUtil.valueOfZero(responseDTO.getQty()));
    }

    /**
    * 新增修改处理数据
    */
    private List<SoB2bProcessingEntity> handleData(List<SoB2bProcessingDTO.AddOrUpdateDTO> list) {
        List<SoB2bProcessingEntity> newList = new ArrayList<>();
        for (SoB2bProcessingDTO.AddOrUpdateDTO addOrUpdateDTO :list) {
            if (ObjectUtil.isEmpty(addOrUpdateDTO)) {
                continue;
            }
            SoB2bProcessingEntity entity = SoB2bProcessingConverter.INSTANCE.addToEntity(addOrUpdateDTO);
            newList.add(entity);
        }
        return newList;
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
            listDTO.setFrozenDays(ObjectUtil.isEmpty(listDTO.getFrozenTime()) ? null : (Math.toIntExact(LocalDate.now().toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay())+ 1));

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
                    && MathUtil.compareTo(listDTO.getFrozenQty(),MathUtil.ZERO) != MathUtil.ZERO) {
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
