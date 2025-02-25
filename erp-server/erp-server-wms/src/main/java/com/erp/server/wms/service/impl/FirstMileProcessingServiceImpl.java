package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.wms.dto.FirstMileProcessingDTO;
import com.erp.model.wms.dto.FirstMileProcessingDetailDTO;
import com.erp.model.wms.dto.SoB2bProcessingDTO;
import com.erp.model.wms.entity.FirstMileProcessingEntity;
import com.erp.model.wms.enums.OrderProcessingLableEnum;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.FirstMileProcessingConverter;
import com.erp.server.wms.mapper.FirstMileProcessingMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Resource
    private FirstMileProcessingDetailService firstMileProcessingDetailService;

    /**
    * 修改
    */
    @Override
    public Boolean addOrupdate(List<FirstMileProcessingDTO.AddOrUpdateDTO> list,LocalDate startDate) {

        //删除头程订单
        baseMapper.deleteFirstMileOrder(startDate);

        if (CollUtil.isEmpty(list)) {
            return Boolean.TRUE;
        }
        // 数据处理
        List<FirstMileProcessingEntity> firstMileProcessingList =  handleData(list);
        if (CollUtil.isEmpty(firstMileProcessingList)) {
            return Boolean.TRUE;
        }
        boolean save = super.saveBatch(firstMileProcessingList);
        if (!save) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        // 使用stream和flatMap将所有detailList合并成一个List
        List<FirstMileProcessingDetailDTO.AddOrUpdateDTO> allDetailList = list.stream()
                .filter(item -> item.getDetailList() != null)
                .flatMap(item -> item.getDetailList().stream())
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(allDetailList)) {
            return Boolean.TRUE;
        }
        firstMileProcessingDetailService.addFirstMileOrderDetail(allDetailList);
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
        log.warn("查询头程订单跟踪数据，startDate = {}，size = {}",startDate,list.size());
        List<String> skuIdList = list.stream().map(FirstMileProcessingEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        Map<String, List<BomChildrenSkuDTO>> bomMap = bomChildrenSkuList.stream()
                .collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}_{}", obj.getParentSkuId(), obj.getType())));
        /**
         * 1、头程非组合品订单中转走直接调拨单出库，非中转走销售出库单出库
         * 2、头程组合品走加工单出库
         */
        //查询加工单数据
        List<String> deliveryIdList = list.stream().map(FirstMileProcessingEntity::getFirstMileDeliveryId).distinct().collect(Collectors.toList());
        List<List<String>> sourceDetailIdListPartition = Lists.partition(deliveryIdList, 50000);

        //查询加工单数据
        List<SoB2bProcessingDTO.ResponseDTO> machineList = new ArrayList<>();
        //直接调拨单数据
        List<SoB2bProcessingDTO.ResponseDTO> transferList = new ArrayList<>();
        //销售出库单数据
        List<SoB2bProcessingDTO.ResponseDTO> soOutstockList = new ArrayList<>();
        //分页查询数据
        for (List<String> sourceDetailIdPartition : sourceDetailIdListPartition) {
            List<SoB2bProcessingDTO.ResponseDTO> machinePageList = machineDetailService.listMachineByRefIdList(sourceDetailIdPartition);
            if (CollectionUtils.isNotEmpty(machinePageList)) {
                machineList.addAll(machinePageList);
            }
            List<SoB2bProcessingDTO.ResponseDTO> transferPageList = transferInfoDetailService.listTransferBySourceIdList(sourceDetailIdPartition);
            if (CollectionUtils.isNotEmpty(transferPageList)) {
                transferList.addAll(transferPageList);
            }
            List<SoB2bProcessingDTO.ResponseDTO> soOutstockPageList = soOutstockDetailService.listSoOutstockBySourceIdList(sourceDetailIdPartition);
            if (CollectionUtils.isNotEmpty(soOutstockPageList)) {
                soOutstockList.addAll(soOutstockPageList);
            }
        }
        log.warn("查询头程订单跟踪数据，machineList = {}，transferList = {}，soOutstockList = {}",machineList.size(),transferList.size(),soOutstockList.size());
        List<FirstMileProcessingDTO.AddOrUpdateDTO> addList = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        /**
         * 1、第三方仓要货单的要货申请单只需要生成主表数据，数量计算在主表中
         * 2、fba的要货申请单会出现一对多个头程发货单的情景，所以发货出库数据存于明细表中
         */
        //根据要货申请生成主表数据
        Map<String, List<FirstMileProcessingEntity>> map = list.stream().collect(Collectors.groupingBy(FirstMileProcessingEntity::getRequisitionApplicationDetailId));
        for (Map.Entry<String, List<FirstMileProcessingEntity>> entry : map.entrySet()) {
            List<FirstMileProcessingEntity> value = entry.getValue();
            //主表数据处理
            List<List<FirstMileProcessingDTO.AddOrUpdateDTO>> mainList = generateAddOrUpdateData(bomMap,value, machineList, transferList, soOutstockList);
            // 合并所有局部列表
            mainList.forEach(addList::addAll);
        }
        stopWatch.stop();
        log.warn("数据处理成功，耗时，time = {}",stopWatch.prettyPrint());
        ApplicationContextUtils.getBean(FirstMileProcessingServiceImpl.class).addOrupdate(addList,startDate);
        log.warn("数据更新成功!");
    }

    /**
     * 生成新增修改数据
     * @author will
     * @date 2025/2/25 16:13
     * @param bomMap
     * @param list
     * @param machineList
     * @param transferList
     * @param soOutstockList
     * @return java.util.List<java.util.List<com.erp.model.wms.dto.FirstMileProcessingDTO.AddOrUpdateDTO>>
     */
    private List<List<FirstMileProcessingDTO.AddOrUpdateDTO>> generateAddOrUpdateData (Map<String, List<BomChildrenSkuDTO>> bomMap,List<FirstMileProcessingEntity> list, List<SoB2bProcessingDTO.ResponseDTO> machineList,
                                          List<SoB2bProcessingDTO.ResponseDTO> transferList,List<SoB2bProcessingDTO.ResponseDTO> soOutstockList) {
        //所有发货明细id集合
        List<String> deliveryDetailIdList = list.stream().map(FirstMileProcessingEntity::getFirstMileDeliveryDetailId).distinct().collect(Collectors.toList());
        //查询发货信息
        FirstMileProcessingEntity firstMileProcessingEntity = list.get(0);
        List<FirstMileProcessingDTO.AddOrUpdateDTO> mainList = handleBomData(null,deliveryDetailIdList, bomMap, firstMileProcessingEntity, machineList, transferList, soOutstockList);
        //fba的需要新增明细数据
        if (!CharSequenceUtil.equals("fba",firstMileProcessingEntity.getType())) {
            return Collections.singletonList(mainList);
        }
        return list.parallelStream().map(entity -> handleBomData(mainList,deliveryDetailIdList,bomMap,entity,machineList,transferList,soOutstockList)).collect(Collectors.toList());
    }

    /**
     * 添加bom数据
     * @author will
     * @date 2025/2/25 18:55
     * @param mainList
     * @param deliveryDetailIdList
     * @param bomMap
     * @param entity
     * @param machineList
     * @param transferList
     * @param soOutstockList
     * @return java.util.List<com.erp.model.wms.dto.FirstMileProcessingDTO.AddOrUpdateDTO>
     */
    private List<FirstMileProcessingDTO.AddOrUpdateDTO> handleBomData ( List<FirstMileProcessingDTO.AddOrUpdateDTO> mainList,List<String> deliveryDetailIdList,Map<String, List<BomChildrenSkuDTO>> bomMap,FirstMileProcessingEntity entity,List<SoB2bProcessingDTO.ResponseDTO> machineList,
                                                                       List<SoB2bProcessingDTO.ResponseDTO> transferList,List<SoB2bProcessingDTO.ResponseDTO> soOutstockList) {

        //根据类型更新出库数据
        handleOutstockByType(deliveryDetailIdList,machineList,transferList,soOutstockList,entity);
        log.warn("根据类型处理成功！code = {}",entity.getRequisitionApplicationCode());
        // 新增数据
        List<FirstMileProcessingDTO.AddOrUpdateDTO> localList = new ArrayList<>();
        addBomList(localList,entity,bomMap.get(CharSequenceUtil.format("{}_{}",entity.getSkuId(),BomTypeEnum.COMBINATION.getType())));
        log.warn("按bom处理数据成功！code = {}",entity.getRequisitionApplicationCode());
        //未传主表数据则返回
        if (CollUtil.isEmpty(mainList)) {
            return localList;
        }
        //给主表添加明细数据
        mainList.forEach(obj -> {
            List<FirstMileProcessingDTO.AddOrUpdateDTO> detailList = localList.stream().filter(o -> CharSequenceUtil.equals(obj.getSkuId(), o.getSkuId())).collect(Collectors.toList());
            List<FirstMileProcessingDetailDTO.AddOrUpdateDTO> oldDetailList = obj.getDetailList();
            //转明细对象
            List<FirstMileProcessingDetailDTO.AddOrUpdateDTO> processingDetailList = BeanUtil.copyToList(detailList, FirstMileProcessingDetailDTO.AddOrUpdateDTO.class);
            processingDetailList.forEach(o -> o.setMainId(obj.getId()));
            oldDetailList.addAll(processingDetailList);
            obj.setDetailList(oldDetailList);
        });
        return mainList;
    }

    @Override
    public Boolean deleteFirstMileProcessing(FirstMileProcessingDTO.DeleteDTO dto) {
        return baseMapper.deleteFirstMileProcessing(dto);
    }

    /**
     * 添加bom数据
     * @author will
     * @date 2025/1/2 10:55
     * @param addList
     * @param entity
     * @param bomChildrenSkuList
     */
    private void addBomList (List<FirstMileProcessingDTO.AddOrUpdateDTO> addList, FirstMileProcessingEntity entity, List<BomChildrenSkuDTO> bomChildrenSkuList) {
        if (CollUtil.isEmpty(bomChildrenSkuList)) {
            FirstMileProcessingDTO.AddOrUpdateDTO addDTO = FirstMileProcessingConverter.INSTANCE.entityToAdd(entity);
            addDTO.setId(IdWorker.getIdStr());
            addDTO.setParentSkuId("");
            addDTO.setBomVersion("");
            addList.add(addDTO);
            return;
        }
        for (BomChildrenSkuDTO childrenSkuDTO : bomChildrenSkuList) {
            FirstMileProcessingDTO.AddOrUpdateDTO addOrUpdateDTO = FirstMileProcessingConverter.INSTANCE.entityToAdd(entity);
            addOrUpdateDTO.setId(IdWorker.getIdStr());
            addOrUpdateDTO.setSkuId(childrenSkuDTO.getSkuId());
            addOrUpdateDTO.setParentSkuId(childrenSkuDTO.getParentSkuId());
            addOrUpdateDTO.setOutstockQty(ObjectUtil.isEmpty(addOrUpdateDTO.getOutstockQty()) ? MathUtil.ZERO : addOrUpdateDTO.getOutstockQty() * childrenSkuDTO.getQuantity());
            addOrUpdateDTO.setFrozenQty(ObjectUtil.isEmpty(addOrUpdateDTO.getFrozenQty()) ? MathUtil.ZERO :addOrUpdateDTO.getFrozenQty() * childrenSkuDTO.getQuantity());
            addOrUpdateDTO.setApproveQty(ObjectUtil.isEmpty(addOrUpdateDTO.getApproveQty()) ? MathUtil.ZERO :addOrUpdateDTO.getApproveQty() * childrenSkuDTO.getQuantity());
            addOrUpdateDTO.setDeliveryQty(ObjectUtil.isEmpty(addOrUpdateDTO.getDeliveryQty()) ? MathUtil.ZERO :addOrUpdateDTO.getDeliveryQty() * childrenSkuDTO.getQuantity());
            addOrUpdateDTO.setBomVersion(childrenSkuDTO.getBomVersion());
            addList.add(addOrUpdateDTO);
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
    private void handleOutstockByType (List<String> deliveryDetailIdList,List<SoB2bProcessingDTO.ResponseDTO> machineList, List<SoB2bProcessingDTO.ResponseDTO> transferList,
                                       List<SoB2bProcessingDTO.ResponseDTO> soOutstockList, FirstMileProcessingEntity entity) {
        //加工单
        SoB2bProcessingDTO.ResponseDTO machineResponseDTO = machineList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSourceId(), entity.getFirstMileDeliveryId())
                        && CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())
                        &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getFirstMileDeliveryDetailId()))
                .findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(machineResponseDTO)) {
            //出库数量
            Integer totalQty = machineList.stream().filter(obj ->
                            CharSequenceUtil.equals(obj.getSourceId(), entity.getFirstMileDeliveryId())
                            && CharSequenceUtil.equals(obj.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
                            && deliveryDetailIdList.contains(entity.getFirstMileDeliveryDetailId()))
                            .map(SoB2bProcessingDTO.ResponseDTO::getQty)
                            .reduce(MathUtil.ZERO, Integer::sum);
            handleOutstock (entity,machineResponseDTO, SourceTypeEnum.MACHINE_INFO.getCode(),totalQty);
            return;
        }
        //直接调拨单
        SoB2bProcessingDTO.ResponseDTO transferResponseDTO = transferList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSourceId(), entity.getFirstMileDeliveryId())
                                && CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())
                                && CharSequenceUtil.equals(obj.getWarehouseId(), entity.getWarehouseId())
                                &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getFirstMileDeliveryDetailId()))
                .findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(transferResponseDTO)) {
            //出库数量
            Integer totalQty = transferList.stream().filter(obj ->
                            CharSequenceUtil.equals(obj.getSourceId(), entity.getFirstMileDeliveryId())
                            && CharSequenceUtil.equals(obj.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
                            && CharSequenceUtil.equals(obj.getWarehouseId(), entity.getWarehouseId())
                            && CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getFirstMileDeliveryDetailId()))
                    .map(SoB2bProcessingDTO.ResponseDTO::getQty)
                    .reduce(MathUtil.ZERO, Integer::sum);
            handleOutstock (entity,transferResponseDTO, SourceTypeEnum.TRANSFER_INFO.getCode(),totalQty);
            return;
        }
        //销售出库单
        SoB2bProcessingDTO.ResponseDTO soOutstockResponseDTO = soOutstockList.stream().filter(obj ->
                        CharSequenceUtil.equals(obj.getSourceId(), entity.getFirstMileDeliveryId())
                                && CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())
                                &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getFirstMileDeliveryDetailId()))
                .findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(soOutstockResponseDTO)) {
            //出库数量
            Integer totalQty =  soOutstockList.stream().filter(obj ->
                            CharSequenceUtil.equals(obj.getSourceId(), entity.getFirstMileDeliveryId())
                            && CharSequenceUtil.equals(obj.getApproveStatus(),ApproveStatusEnum.APPROVE.getStatus())
                            &&  CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getFirstMileDeliveryDetailId()))
                    .map(SoB2bProcessingDTO.ResponseDTO::getQty)
                    .reduce(MathUtil.ZERO, Integer::sum);
            handleOutstock (entity,soOutstockResponseDTO, SourceTypeEnum.SO_OUTSTOCK.getCode(),totalQty);
        }
    }

    /**
     * 新增修改处理数据
     */
    private List<FirstMileProcessingEntity> handleData(List<FirstMileProcessingDTO.AddOrUpdateDTO> list) {

        List<FirstMileProcessingEntity> newList = new ArrayList<>();
        for (FirstMileProcessingDTO.AddOrUpdateDTO addOrUpdateDTO :list) {
            if (ObjectUtil.isEmpty(addOrUpdateDTO)) {
                continue;
            }
            FirstMileProcessingEntity entity = FirstMileProcessingConverter.INSTANCE.addToEntity(addOrUpdateDTO);
            newList.add(entity);
        }
        return newList;
    }

    /**
     * 出库字段赋值
     * @author will
     * @date 2024/12/19 20:56
     * @param entity
     * @param responseDTO
     * @param sourceType
     */
    private void handleOutstock (FirstMileProcessingEntity entity, SoB2bProcessingDTO.ResponseDTO responseDTO,String sourceType,Integer totalQty) {
        entity.setOutstockOrderId(responseDTO.getId());
        entity.setOutstockOrderStatus(responseDTO.getApproveStatus());
        entity.setOutstockQty(responseDTO.getQty());
        entity.setOutstockOrderCode(responseDTO.getCode());
        entity.setOutstockOrderTime(responseDTO.getApproveTime());
        entity.setOutstockOrderType(sourceType);
        entity.setFrozenQty(MathUtil.valueOfZero(entity.getFrozenQty()) - totalQty);
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
            listDTO.setFrozenDays(ObjectUtil.isEmpty(listDTO.getFrozenTime()) ? null : (Math.toIntExact(LocalDate.now().toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay()) + 1));

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
            if (MathUtil.compareTo(listDTO.getFrozenQty(),MathUtil.ZERO) != MathUtil.ZERO && CharSequenceUtil.isNotBlank(listDTO.getFirstMileDeliveryId()) && CharSequenceUtil.isBlank(listDTO.getOutstockOrderId())) {
                labelList.add(OrderProcessingLableEnum.FROZEN.getCode());
            }
            //七日未发
            if (CharSequenceUtil.isBlank(listDTO.getOutstockOrderId()) && ObjectUtil.isNotEmpty(listDTO.getFrozenTime()) && (LocalDate.now().toEpochDay() - listDTO.getFrozenTime().toLocalDate().toEpochDay() >= 7)) {
                labelList.add(OrderProcessingLableEnum.UN_SHIPPED.getCode());
            }
            //要货冻结
            if (CharSequenceUtil.isBlank(listDTO.getFirstMileDeliveryId())
                    && Arrays.asList(RequisitionApplicationStatusEnum.HANDLE_ING.getStatus(),RequisitionApplicationStatusEnum.HANDLE.getStatus()).contains(listDTO.getRequisitionApplicationStatus())
                    && MathUtil.compareTo(listDTO.getFrozenQty(),MathUtil.ZERO) > MathUtil.ZERO) {
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
