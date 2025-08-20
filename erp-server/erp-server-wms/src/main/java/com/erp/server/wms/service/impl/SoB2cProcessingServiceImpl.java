package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import com.erp.model.wms.dto.SoB2cProcessingDTO;
import com.erp.model.wms.entity.SoB2cProcessingEntity;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.model.wms.enums.OrderProcessingLableEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.SoB2cProcessingConverter;
import com.erp.server.wms.mapper.SoB2cProcessingMapper;
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

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_B2C_PROCESSING;

/**
 * <p>
 * B2C虚拟仓订单跟踪 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-12-18
 */
@Slf4j
@Service
public class SoB2cProcessingServiceImpl extends SuperServiceImpl<SoB2cProcessingMapper, SoB2cProcessingEntity> implements SoB2cProcessingService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private TransferInfoDetailService transferInfoDetailService;

    @Autowired
    private SoOutstockDetailService soOutstockDetailService;

    @Autowired
    private VirtualTransFlowService virtualTransFlowService;

    /**
    * 修改
    */
    @Override
    public Boolean addOrUpdate(List<SoB2cProcessingDTO.AddOrUpdateDTO> list,LocalDate startDate) {
        //删除多余b2c订单
        baseMapper.deleteB2cOrder(startDate);

        if (CollUtil.isEmpty(list)) {
            return Boolean.TRUE;
        }
        // 数据处理
        List<SoB2cProcessingEntity> soB2cProcessingList =  handleData(list);
        if (CollUtil.isEmpty(soB2cProcessingList)) {
            log.warn("B2C虚拟仓订单数据处理为空！");
            return Boolean.TRUE;
        }
        return super.saveBatch(soB2cProcessingList);
    }

    @Override
    public PagingVO<SoB2cProcessingDTO.ListDTO> paging(PagingDTO<SoB2cProcessingDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<SoB2cProcessingDTO.ListDTO> pageData = this.baseMapper.paging(dto.page(), dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(SoB2cProcessingDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("B2C虚拟仓列表信息", EXPORT_WMS_SO_B2C_PROCESSING.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public void autoUpdateSoB2cProcessing(LocalDate startDate) {
          List<SoB2cProcessingEntity> list = baseMapper.listSoB2cProcessing(startDate);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        log.warn("查询b2c订单跟踪数据，startDate = {}，size = {}",startDate,list.size());
        List<String> skuIdList = list.stream().map(SoB2cProcessingEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        Map<String, List<BomChildrenSkuDTO>> bomMap = bomChildrenSkuList.stream()
                .collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}_{}", obj.getParentSkuId(), obj.getType())));

        /**
         * 1、b2b非组合品订单中转走直接调拨单出库，非中转走销售出库单出库
         * 2、b2b组合品走加工单出库
         */
        //查询加工单数据
        List<String> deliveryIdList = list.stream().map(SoB2cProcessingEntity::getDeliveryId).distinct().collect(Collectors.toList());
        List<List<String>> sourceIdListPartition = Lists.partition(deliveryIdList, 50000);

        //查询发货单出库流水
        List<VirtualTransFlowEntity> virtualTransFlowList = new ArrayList<>();
        //直接调拨单数据
        List<SoB2bProcessingDTO.ResponseDTO> transferList = new ArrayList<>();
        //销售出库单数据
        List<SoB2bProcessingDTO.ResponseDTO> soOutstockList = new ArrayList<>();
        //分页查询数据
        for (List<String> sourceIdPartition : sourceIdListPartition) {
            List<VirtualTransFlowEntity> virtualTransFlowPageList = virtualTransFlowService.listBySourceIdList(sourceIdPartition);
            if (CollectionUtils.isNotEmpty(virtualTransFlowPageList)) {
                virtualTransFlowList.addAll(virtualTransFlowPageList);
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
        log.warn("查询b2c订单跟踪数据，virtualTransFlowList = {}，transferList = {}，soOutstockList = {}",virtualTransFlowList.size(),transferList.size(),soOutstockList.size());
        List<SoB2cProcessingDTO.AddOrUpdateDTO> addList = new ArrayList<>();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<List<SoB2cProcessingDTO.AddOrUpdateDTO>> result =   list.parallelStream().map(entity -> {
            //根据类型更新出库数据
            handleOutstockByType(virtualTransFlowList, transferList, soOutstockList, entity);
            log.warn("根据类型处理成功！code = {}",entity.getB2cSoCode());
            // 新增数据
            List<SoB2cProcessingDTO.AddOrUpdateDTO> localList = new ArrayList<>();
            addBomList(addList,entity,bomMap.get(CharSequenceUtil.format("{}_{}",entity.getSkuId(),BomTypeEnum.COMBINATION.getType())));
            log.warn("按bom处理数据成功！code = {}",entity.getB2cSoCode());
            return localList;
        }).collect(Collectors.toList());
        // 合并所有局部列表
        result.forEach(addList::addAll);
        stopWatch.stop();
        log.warn("数据处理成功，耗时，time = {}",stopWatch.prettyPrint());
        ApplicationContextUtils.getBean(SoB2cProcessingServiceImpl.class).addOrUpdate(addList,startDate);
        log.warn("数据更新成功!");
    }

    @Override
    public PagingVO<ReportProcessingDTO.ListDTO> b2cTotalPaging(PagingDTO<ReportProcessingDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        IPage<ReportProcessingDTO.ListDTO> pageData = this.baseMapper.b2cTotalPaging(dto.page(), dto.getParams());
        return new PagingVO<>(pageData);
    }

    /**
     * 添加bom数据
     * @author will
     * @date 2025/1/2 10:55
     * @param addList
     * @param entity
     * @param bomChildrenSkuList
     */
    private void addBomList (List<SoB2cProcessingDTO.AddOrUpdateDTO> addList, SoB2cProcessingEntity entity, List<BomChildrenSkuDTO> bomChildrenSkuList) {
            if (CollUtil.isEmpty(bomChildrenSkuList)) {
                SoB2cProcessingDTO.AddOrUpdateDTO addDTO = SoB2cProcessingConverter.INSTANCE.entityToAdd(entity);
                addDTO.setParentSkuId("");
                addDTO.setBomVersion("");
                addList.add(addDTO);
                return;
            }
            for (BomChildrenSkuDTO childrenSkuDTO : bomChildrenSkuList) {
                SoB2cProcessingDTO.AddOrUpdateDTO addDTO = SoB2cProcessingConverter.INSTANCE.entityToAdd(entity);
                addDTO.setSkuId(childrenSkuDTO.getSkuId());
                addDTO.setParentSkuId(childrenSkuDTO.getParentSkuId());
                addDTO.setOutstockQty(ObjectUtil.isEmpty(addDTO.getOutstockQty()) ? MathUtil.ZERO : addDTO.getOutstockQty() * childrenSkuDTO.getQuantity());
                addDTO.setFrozenQty(ObjectUtil.isEmpty(addDTO.getFrozenQty()) ? MathUtil.ZERO :addDTO.getFrozenQty() * childrenSkuDTO.getQuantity());
                addDTO.setDeliveryQty(ObjectUtil.isEmpty(addDTO.getDeliveryQty()) ? MathUtil.ZERO :addDTO.getDeliveryQty() * childrenSkuDTO.getQuantity());
                addDTO.setBomVersion(childrenSkuDTO.getBomVersion());
                addList.add(addDTO);
            }
    }

        /**
         * 根据类型赋值出库数据
         * @author will
         * @date 2024/12/31 14:59
         * @param virtualTransFlowList
         * @param transferList
         * @param soOutstockList
         * @param entity
         */
    private void handleOutstockByType ( List<VirtualTransFlowEntity> virtualTransFlowList,List<SoB2bProcessingDTO.ResponseDTO> transferList,
                                      List<SoB2bProcessingDTO.ResponseDTO> soOutstockList,SoB2cProcessingEntity entity) {
        //发货单出库流水
        long count = virtualTransFlowList.stream().filter(obj -> StrUtil.equals(obj.getSourceDetailId(), entity.getDeliveryDetailId())).count();
        if (count > 0) {
            entity.setOutstockOrderId(entity.getId());
            entity.setOutstockOrderStatus(entity.getDeliveryStatus());
            entity.setOutstockQty(entity.getDeliveryQty());
            entity.setOutstockOrderCode(entity.getDeliveryCode());
            entity.setOutstockOrderTime(entity.getDeliveryTime());
            entity.setOutstockOrderType(SourceTypeEnum.SO_B2C_DELIVERY.getCode());
            if (SoB2cDeliveryStatusEnum.SHIPPED.getStatus().equals(entity.getDeliveryStatus())) {
                entity.setFrozenQty(MathUtil.valueOfZero(entity.getFrozenQty()) - MathUtil.valueOfZero(entity.getDeliveryQty()));
            }
            return;
        }
        //直接调拨单
        List<SoB2bProcessingDTO.ResponseDTO> transferResponseDTOList = transferList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSourceId(), entity.getDeliveryId())
                                && CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())
                                && CharSequenceUtil.equals(obj.getWarehouseId(), entity.getWarehouseId())
                                &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getDeliveryDetailId()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(transferResponseDTOList)) {
            handleOutstock (entity,transferResponseDTOList, SourceTypeEnum.TRANSFER_INFO.getCode());
            return;
        }
        //销售出库单
        List<SoB2bProcessingDTO.ResponseDTO> soOutstockResponseDTOList = soOutstockList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSourceId(), entity.getDeliveryId())
                                && CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())
                                &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getDeliveryDetailId()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(soOutstockResponseDTOList)) {
            handleOutstock (entity,soOutstockResponseDTOList, SourceTypeEnum.SO_OUTSTOCK.getCode());
        }
    }


    /**
     * 出库字段赋值
     * @author will
     * @date 2024/12/19 20:56
     * @param entity
     * @param responseDTOList
     * @param sourceType
     */
    private void handleOutstock (SoB2cProcessingEntity entity, List<SoB2bProcessingDTO.ResponseDTO> responseDTOList,String sourceType) {
        SoB2bProcessingDTO.ResponseDTO responseDTO = responseDTOList.get(0);
        Integer totalQty = responseDTOList.stream().map(SoB2bProcessingDTO.ResponseDTO::getQty).reduce(MathUtil.ZERO, MathUtil::add);
        entity.setOutstockOrderId(responseDTO.getId());
        entity.setOutstockOrderStatus(responseDTO.getApproveStatus());
        entity.setOutstockQty(totalQty);
        entity.setOutstockOrderCode(responseDTO.getCode());
        entity.setOutstockOrderTime(responseDTO.getApproveTime());
        entity.setOutstockOrderType(sourceType);
        entity.setFrozenQty(MathUtil.valueOfZero(entity.getFrozenQty()) - MathUtil.valueOfZero(totalQty));
    }

    /**
    * 新增修改处理数据
    */
    private List<SoB2cProcessingEntity> handleData(List<SoB2cProcessingDTO.AddOrUpdateDTO> list) {
        List<SoB2cProcessingEntity> newList = new ArrayList<>();
        for (SoB2cProcessingDTO.AddOrUpdateDTO addOrUpdateDTO :list) {
            if (ObjectUtil.isEmpty(addOrUpdateDTO)) {
                continue;
            }
            SoB2cProcessingEntity entity = SoB2cProcessingConverter.INSTANCE.addToEntity(addOrUpdateDTO);
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
    private void fillPageData(List<SoB2cProcessingDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (SoB2cProcessingDTO.ListDTO listDTO : list) {
            listDTO.setDeliveryStatusName(SoB2cDeliveryStatusEnum.getName(listDTO.getDeliveryStatus()));
            listDTO.setOutstockOrderTypeName(SourceTypeEnum.getName(listDTO.getOutstockOrderType()));
            //冻结时长
            listDTO.setFrozenDays(ObjectUtil.isEmpty(listDTO.getFrozenTime()) ? null : (Math.toIntExact(LocalDate.now().toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay())+ 1));
            //标签
            List<String> labelList = new ArrayList<>();
            if (CharSequenceUtil.isNotBlank(listDTO.getOutstockOrderId())) {
                labelList.add(OrderProcessingLableEnum.OUTSTOCK.getCode());
                LocalDate localDate = ObjUtil.isEmpty(listDTO.getOutstockOrderTime()) ? LocalDate.now() : listDTO.getOutstockOrderTime().toLocalDate();
                //冻结时长
                listDTO.setFrozenDays(Math.toIntExact(localDate.toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay()) + 1);
            }
            if (MathUtil.compareTo(listDTO.getFrozenQty(),MathUtil.ZERO) != MathUtil.ZERO && CharSequenceUtil.isNotBlank(listDTO.getDeliveryId()) && CharSequenceUtil.isBlank(listDTO.getOutstockOrderId())) {
                labelList.add(OrderProcessingLableEnum.FROZEN.getCode());
                //冻结时长
                listDTO.setFrozenDays(Math.toIntExact(LocalDate.now().toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay()) + 1);
            }
            if (CharSequenceUtil.isBlank(listDTO.getOutstockOrderId()) && ObjectUtil.isNotEmpty(listDTO.getFrozenTime()) && (LocalDate.now().toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay() >= 7)) {
                labelList.add(OrderProcessingLableEnum.UN_SHIPPED.getCode());
                //冻结时长
                listDTO.setFrozenDays(Math.toIntExact(LocalDate.now().toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay()) + 1);
            }
            listDTO.setLabelList(labelList);
            //出库状态
            if (CharSequenceUtil.equals(listDTO.getOutstockOrderType(),SourceTypeEnum.TRANSFER_INFO.getCode())
                    || CharSequenceUtil.equals(listDTO.getOutstockOrderType(),SourceTypeEnum.SO_OUTSTOCK.getCode())) {
                listDTO.setOutstockOrderStatusName(ApproveStatusEnum.getName(listDTO.getOutstockOrderStatus()));
            }  else if (CharSequenceUtil.equals(listDTO.getOutstockOrderType(),SourceTypeEnum.SO_B2C_DELIVERY.getCode())) {
                listDTO.setOutstockOrderStatusName(SoB2cDeliveryStatusEnum.getName(listDTO.getOutstockOrderStatus()));
            }
        }
    }
}
