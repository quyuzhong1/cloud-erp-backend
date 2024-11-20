package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.ReportOrderDataEntity;
import com.erp.model.wms.entity.ReportOrderDemandDetailEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.enums.CfgSettingOrderTypeEnum;
import com.erp.model.wms.enums.DeliveryStatusEnum;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.ReportOrderDataMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单报表信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-09-24
 */
@Slf4j
@Service
public class ReportOrderDataServiceImpl extends SuperServiceImpl<ReportOrderDataMapper, ReportOrderDataEntity> implements ReportOrderDataService {
    @Resource
    private ReportOrderDemandDetailService reportOrderDemandDetailService;

    @Resource
    private CfgSettingVirtualService cfgSettingVirtualService;

    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private VirtualInventoryService virtualInventoryService;

    @Resource
    private ReportOrderDemandService reportOrderDemandService;

    @Resource
    private ReportOrderSalesService reportOrderSalesService;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private WmsDeliveryPlanDetailService wmsDeliveryPlanDetailService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchAddOrUpdate(List<ReportOrderDataDTO.UpdateDTO> addOrUpdateList) {
        List<ReportOrderDataEntity> list =  BeanMapperUtils.copyList(ReportOrderDataEntity.class, addOrUpdateList);
        // 数据处理
        List<ReportOrderDataEntity> resultList =  handleData(list);
        //删除多余的订单数据
        this.removeByNotIds(null);
        //新增或修改有变更数据
        boolean save = super.saveOrUpdateBatch(resultList);
        if(!save) {
            throw new ServiceException("订单报表信息保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public void generateVirtualReport(String time,Boolean isAuto) {
        //查询配置
        CfgSettingVirtualDTO.ViewDTO viewDTO = cfgSettingVirtualService.viewVirtual();
        //规则配置
        CfgSettingVirtualValueDTO.VirtualRuleDTO virtualRuleDTO = viewDTO.getVirtualRuleDTO();
        if (ObjectUtil.isEmpty(virtualRuleDTO) || CollectionUtils.isEmpty(virtualRuleDTO.getExecTimeList())) {
            return;
        }
        if (isAuto) {
            LocalTime now = CharSequenceUtil.isBlank(time) ? LocalTime.now() : LocalTime.parse(time,DateTimeFormatter.ofPattern("HH:mm"));
            boolean isGenerate = virtualRuleDTO.getExecTimeList().contains(LocalTime.parse(now.format(DateTimeFormatter.ofPattern("HH:mm")), DateTimeFormatter.ofPattern("HH:mm")));
            if (!isGenerate) {
                return;
            }
        }
        //是否拆分
        boolean isSplit = ObjectUtil.isEmpty(viewDTO.getVirtualRuleDTO()) ? false : viewDTO.getVirtualRuleDTO().getIsSplit();
        //生成源数据
        generateReportOrderData();

        //查询源数据
        List<ReportOrderDataEntity> reportOrderDataList = baseMapper.listReportOrderData();
        if (CollectionUtils.isEmpty(reportOrderDataList)) {
            return;
        }
        //bom信息
        List<String> skuIdList = reportOrderDataList.stream().map(ReportOrderDataEntity::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        //取销售套装bom
        if (CollectionUtils.isNotEmpty(bomChildrenSkuList)) {
            bomChildrenSkuList = bomChildrenSkuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
        }

        //子级sku
        List<String> childSkuIdList = bomChildrenSkuList.stream().map(BomChildrenSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childSkuIdList)) {
            skuIdList.addAll(childSkuIdList);
        }
        //虚拟仓可用
        List<String> warehouseIdList = reportOrderDataList.stream().map(ReportOrderDataEntity::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> virtualWarehouseIdList = reportOrderDataList.stream().map(ReportOrderDataEntity::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setSkuIdList(skuIdList);
        paramDTO.setWarehouseIdList(warehouseIdList);
        paramDTO.setVirtualWarehouseIdList(virtualWarehouseIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList = virtualInventoryService.listInventoryQty(paramDTO);

        //生成订单需求明细数据
        generateReportOrderDemandDetail(reportOrderDataList,bomChildrenSkuList,virtualInventoryList,isSplit);

        //生成缺货统计数据
        generateReportOrderDemand(bomChildrenSkuList,isSplit);

        //生成销售看板数据
        generateReportOrderSales(reportOrderDataList,bomChildrenSkuList,virtualInventoryList,isSplit,viewDTO.getSalesDashboardDTO());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateReportOrderData() {
        List<ReportOrderDataDTO.ViewDTO> resultList = new ArrayList<>();

        //b2b销售订单
        List<ReportOrderDataDTO.ViewDTO> soDetailList = soInfoFeign.listAllVirtualSoDetail();
        if (CollectionUtils.isNotEmpty(soDetailList)) {
            resultList.addAll(soDetailList);
        }
        //b2c销售订单
        List<ReportOrderDataDTO.ViewDTO> soB2cDetailList = soB2cFeign.listAllVirtualSoB2cDetail();
        if (CollectionUtils.isNotEmpty(soB2cDetailList)) {
            resultList.addAll(soB2cDetailList);
        }
        //要货申请
        List<ReportOrderDataDTO.ViewDTO> requisitionApplicationDetailList = requisitionApplicationDetailService.listAllVirtualRequisitionApplicationDetail();
        if (CollectionUtils.isNotEmpty(requisitionApplicationDetailList)) {
            resultList.addAll(requisitionApplicationDetailList);
        }

        //无数据则删除所有并且返回
        if (CollectionUtils.isEmpty(resultList)) {
            lambdaUpdate().remove();
            return;
        }
        List<ReportOrderDataDTO.UpdateDTO> addOrUpdateList = BeanMapperUtils.copyList(ReportOrderDataDTO.UpdateDTO.class, resultList);
        this.batchAddOrUpdate(addOrUpdateList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateReportOrderDemandDetail(List<ReportOrderDataEntity> reportOrderDataList,List<BomChildrenSkuDTO> bomChildrenSkuList,List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList,Boolean isSplit) {
        //订单数据
        if (CollectionUtils.isEmpty(reportOrderDataList)) {
            return;
        }
        /**
         * B2B,单据状态：待提交、审核中、已审核（排除：审核不通过） && 作废状态：未作废 && 发货状态：未发货、部分发货（排除：已发货）
         * B2C,审核状态：待提交、审核中、已审核；（排除：审核不通过）&& 作废状态：未作废 && 订单状态：待配货、配货中、待发货、冻结中（排除：已发货）
         * 要货申请，单据状态：待提交、待处理 && 作废状态：未作废
         */
        //审核状态
        List<String> approveStatusList = Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus(), ApproveStatusEnum.APPROVE.getStatus());
        //发货状态
        List<String> deliveryStatusList = Arrays.asList(DeliveryStatusEnum.UN_SHIPPED.getCode(), DeliveryStatusEnum.PARTIAL_SHIPMENT.getCode());
        //b2c订单状态
        List<String> billStatusList = Arrays.asList(SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode(), SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode(), SoB2cBillStatusEnum.ENUM_IN_DISTRIBUTION.getCode(),
                SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode(), SoB2cBillStatusEnum.ENUM_PARTIAL_SHIPPED.getCode(), SoB2cBillStatusEnum.ENUM_FROZEN.getCode());
        //要货申请
        List<String> statusList = Arrays.asList(RequisitionApplicationStatusEnum.WAIT_SUBMIT.getCode(), RequisitionApplicationStatusEnum.WAIT_HANDLE.getCode());

        //需要添加订单需求明细数据
        List<ReportOrderDataEntity> list = reportOrderDataList.stream().filter(obj ->
                (CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.SO_INFO.getCode()) && approveStatusList.contains(obj.getApproveStatus()) && !obj.getInvalidStatus() && deliveryStatusList.contains(obj.getStatus()))
                        || (CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.SO_B2C.getCode()) && approveStatusList.contains(obj.getApproveStatus()) && !obj.getInvalidStatus() && billStatusList.contains(obj.getStatus()))
                        || (CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.REQUISITION_APPLICATION.getCode()) && statusList.contains(obj.getStatus()) && !obj.getInvalidStatus())
        ).collect(Collectors.toList());
        //符合条件数据为空则返回
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        List<ReportOrderDemandDetailDTO.AddDTO> addList = new ArrayList<>();
        for (ReportOrderDataEntity entity : list) {
            //bom信息
            List<BomChildrenSkuDTO> skuList = bomChildrenSkuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentSkuId(), entity.getSkuId())).collect(Collectors.toList());
            //拆分
            List<ReportOrderDemandDetailDTO.AddDTO> splitAddList = handleReportOrderDemandDetail(entity, skuList, virtualInventoryList, isSplit);
            addList.addAll(splitAddList);
        }
        if (CollectionUtils.isEmpty(addList)) {
            return;
        }
        reportOrderDemandDetailService.batchAddOrUpdate(addList);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateReportOrderDemand (List<BomChildrenSkuDTO> bomChildrenSkuList,Boolean isSplit) {
        List<ReportOrderDemandDetailEntity> list = reportOrderDemandDetailService.list();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, List<ReportOrderDemandDetailEntity>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getWarehouseId()).concat(obj.getVirtualWarehouseId())));

        List<ReportOrderDemandDTO.AddDTO> addList = new ArrayList<>();
        for (Map.Entry<String, List<ReportOrderDemandDetailEntity>> entry : map.entrySet()) {
            List<ReportOrderDemandDetailEntity> value = entry.getValue();
            ReportOrderDemandDTO.AddDTO addDTO = new ReportOrderDemandDTO.AddDTO();
            BeanMapperUtils.copy(value.get(0),addDTO);

            //b2b需求数
            Integer soQty = value.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.SO_INFO.getCode())).map(ReportOrderDemandDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setSoQty(soQty);
            //b2c需求数
            Integer b2cQty = value.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.SO_B2C.getCode())).map(ReportOrderDemandDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setB2cSoQty(b2cQty);
            //头程需求数
            Integer firstMileQty = value.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceType(), SourceTypeEnum.REQUISITION_APPLICATION.getCode())).map(ReportOrderDemandDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setFirstMileQty(firstMileQty);
            //剩余需求总数
            Integer totalQty = soQty + b2cQty + firstMileQty;
            addDTO.setTotalQty(totalQty);

            //虚拟仓可用
            Integer virtualUsableQty = value.stream().map(ReportOrderDemandDetailEntity::getVirtualUsableQty).findFirst().get();
            addDTO.setVirtualUsableQty(virtualUsableQty);
            //是否缺货
            Boolean isVirtualScarce = addDTO.getTotalQty() > virtualUsableQty ? Boolean.TRUE : Boolean.FALSE;
            addDTO.setIsVirtualScarce(isVirtualScarce);
            //缺货数量
            addDTO.setVirtualScarceQty(isVirtualScarce ? totalQty - virtualUsableQty : MathUtil.ZERO);
            addList.add(addDTO);
        }
        reportOrderDemandService.batchAddOrUpdate(addList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateReportOrderSales (List<ReportOrderDataEntity> reportOrderDataList,List<BomChildrenSkuDTO> bomChildrenSkuList,List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList ,
                                          Boolean isSplit,CfgSettingVirtualValueDTO.SalesDashboardDTO salesDashboardDTO) {
        if (CollectionUtils.isEmpty(reportOrderDataList) || ObjectUtil.isEmpty(salesDashboardDTO)) {
            return;
        }
        List<ReportOrderDataEntity> resultList = new ArrayList<>();

        List<String> orderTypeList = salesDashboardDTO.getOrderTypeList();
        //b2b销售订单数据格式化
        boolean containsB2b = orderTypeList.contains(CfgSettingOrderTypeEnum.B2B.getCode());
        if (containsB2b) {
            List<ReportOrderDataEntity> b2bList =  handleB2bSales(reportOrderDataList, salesDashboardDTO.getB2bStatusDTO(),bomChildrenSkuList,isSplit);
            if (CollectionUtils.isNotEmpty(b2bList)) {
                resultList.addAll(b2bList);
            }
        }
        //b2c销售订单数据格式化
        boolean containsB2c = orderTypeList.contains(CfgSettingOrderTypeEnum.B2C.getCode());
        if (containsB2c) {
            List<ReportOrderDataEntity> b2cList =  handleB2cSales(reportOrderDataList, salesDashboardDTO.getB2cStatusDTO(),bomChildrenSkuList,isSplit);
            if (CollectionUtils.isNotEmpty(b2cList)) {
                resultList.addAll(b2cList);
            }
        }
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        List<ReportOrderSalesDTO.AddDTO> addOrUpdateList = new ArrayList<>();
        Map<String, List<ReportOrderDataEntity>> map = resultList.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getWarehouseId()).concat(obj.getVirtualWarehouseId())));
        for (Map.Entry<String, List<ReportOrderDataEntity>> entry : map.entrySet()) {
            List<ReportOrderDataEntity> value = entry.getValue();
            ReportOrderDataEntity entity = value.get(0);
            ReportOrderSalesDTO.AddDTO addDTO = new ReportOrderSalesDTO.AddDTO();
            addDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            addDTO.setWarehouseId(entity.getWarehouseId());
            addDTO.setSkuId(entity.getSkuId());

            //需求总数量
            Integer totalQty = value.stream().map(ReportOrderDataEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setTotalQty(totalQty);
            //虚拟仓可用库存
            Integer virtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), entity.getSkuId())
                    && CharSequenceUtil.equals(obj.getWarehouseId(), entity.getWarehouseId())
                    && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), entity.getVirtualWarehouseId())
                    && CharSequenceUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode())
            ).map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setVirtualUsableQty(virtualUsableQty);

            //虚拟仓冻结库存
            Integer virtualFrozenQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), entity.getSkuId())
                    && CharSequenceUtil.equals(obj.getWarehouseId(), entity.getWarehouseId())
                    && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), entity.getVirtualWarehouseId())
                    && CharSequenceUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.FROZEN.getCode())
            ).map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setVirtualFrozenQty(virtualFrozenQty);
            //虚拟仓库存
            addDTO.setVirtualTotalQty(addDTO.getVirtualUsableQty() + addDTO.getVirtualFrozenQty());

            //是否缺货
            Boolean isVirtualScarce = totalQty > virtualUsableQty ? Boolean.TRUE : Boolean.FALSE;
            addDTO.setIsVirtualScarce(isVirtualScarce);
            //缺货数量
            Integer virtualScarceQty = isVirtualScarce ? totalQty - virtualUsableQty : MathUtil.ZERO;
            addDTO.setVirtualScarceQty(virtualScarceQty);

            //今天销量
            Integer todaySalesQty = value.stream().filter(obj -> obj.getDate().isEqual(LocalDate.now())).map(ReportOrderDataEntity::getOrderQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setTodaySalesQty(todaySalesQty);
            //昨日销量
            Integer yesterdaySalesQty = value.stream().filter(obj -> obj.getDate().isEqual(LocalDate.now().minusDays(1L))).map(ReportOrderDataEntity::getOrderQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setYesterdaySalesQty(yesterdaySalesQty);
            //近3日销量
            Integer threeDaysSalesQty = value.stream().filter(obj -> obj.getDate().isEqual(LocalDate.now().minusDays(2L)) || obj.getDate().isAfter(LocalDate.now().minusDays(2L))).map(ReportOrderDataEntity::getOrderQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setThreeDaysSalesQty(threeDaysSalesQty);
            //近7日销量
            Integer sevenDaysSalesQty = value.stream().filter(obj -> obj.getDate().isEqual(LocalDate.now().minusDays(6L)) || obj.getDate().isAfter(LocalDate.now().minusDays(6L))).map(ReportOrderDataEntity::getOrderQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setSevenDaysSalesQty(sevenDaysSalesQty);
            //近14日销量
            Integer fourteenDaysSalesQty = value.stream().filter(obj -> obj.getDate().isEqual(LocalDate.now().minusDays(14L)) || obj.getDate().isAfter(LocalDate.now().minusDays(14L))).map(ReportOrderDataEntity::getOrderQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setFourteenDaysSalesQty(fourteenDaysSalesQty);
            //近30日销量
            Integer thirtyDaysSalesQty = value.stream().filter(obj -> obj.getDate().isEqual(LocalDate.now().minusDays(30L)) || obj.getDate().isAfter(LocalDate.now().minusDays(30L))).map(ReportOrderDataEntity::getOrderQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setThirtyDaysSalesQty(thirtyDaysSalesQty);
            //近60日销量
            Integer sixtyDaysSalesQty = value.stream().filter(obj -> obj.getDate().isEqual(LocalDate.now().minusDays(60L)) || obj.getDate().isAfter(LocalDate.now().minusDays(60L))).map(ReportOrderDataEntity::getOrderQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setSixtyDaysSalesQty(sixtyDaysSalesQty);
            //近90日销量
            Integer ninetyDaysSalesQty = value.stream().filter(obj -> obj.getDate().isEqual(LocalDate.now().minusDays(90L)) || obj.getDate().isAfter(LocalDate.now().minusDays(90L))).map(ReportOrderDataEntity::getOrderQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setNinetyDaysSalesQty(ninetyDaysSalesQty);
            addOrUpdateList.add(addDTO);
        }
        reportOrderSalesService.batchAddOrUpdate(addOrUpdateList);
    }

    /**
     * 符合条件的b2c数据
     * @author will
     * @date 2024/9/27 14:26
     * @param reportOrderDataList
     * @param statusDTO
     * @param bomChildrenSkuList
     */
    private List<ReportOrderDataEntity> handleB2cSales (List<ReportOrderDataEntity> reportOrderDataList, CfgSettingVirtualValueDTO.StatusDTO statusDTO,
                                 List<BomChildrenSkuDTO> bomChildrenSkuList,Boolean isSplit) {
        if (ObjectUtil.isEmpty(statusDTO)) {
            return Collections.emptyList();
        }
        //订单状态
        List<String> statusList =  CollectionUtils.isEmpty(statusDTO.getStatusList()) ? new ArrayList<>() : statusDTO.getStatusList();
        //审核状态
        List<String> approveStatusList =  CollectionUtils.isEmpty(statusDTO.getApproveStatusList()) ? new ArrayList<>() : statusDTO.getApproveStatusList();
        //作废状态
        List<Boolean> invalidStatusList =  CollectionUtils.isEmpty(statusDTO.getInvalidStatusList()) ? new ArrayList<>() : statusDTO.getInvalidStatusList();
        List<ReportOrderDataEntity> list = reportOrderDataList.stream().filter(obj ->
                SourceTypeEnum.SO_B2C.getCode().equals(obj.getSourceType())
                        && statusList.contains(obj.getStatus())
                        && approveStatusList.contains(obj.getApproveStatus())
                        && invalidStatusList.contains(obj.getInvalidStatus())
        ).collect(Collectors.toList());
        //无数据直接返回 或 无拆分直接返回
        if (CollectionUtils.isEmpty(list) || !isSplit) {
            return list;
        }
        List<ReportOrderDataEntity> resultList = splitBom(list, bomChildrenSkuList);
        return resultList;
    }


    /**
     * 符合条件的b2b数据
     * @author will
     * @date 2024/9/27 14:26
     * @param reportOrderDataList
     * @param statusDTO
     */
    private List<ReportOrderDataEntity> handleB2bSales (List<ReportOrderDataEntity> reportOrderDataList, CfgSettingVirtualValueDTO.StatusDTO statusDTO,
                                 List<BomChildrenSkuDTO> bomChildrenSkuList,Boolean isSplit) {
        if (ObjectUtil.isEmpty(statusDTO)) {
            return Collections.emptyList();
        }
        //发货状态
        List<String> statusList =  CollectionUtils.isEmpty(statusDTO.getStatusList()) ? new ArrayList<>() : statusDTO.getStatusList();
        //审核状态
        List<String> approveStatusList =  CollectionUtils.isEmpty(statusDTO.getApproveStatusList()) ? new ArrayList<>() : statusDTO.getApproveStatusList();
        //作废状态
        List<Boolean> invalidStatusList =  CollectionUtils.isEmpty(statusDTO.getInvalidStatusList()) ? new ArrayList<>() : statusDTO.getInvalidStatusList();
        List<ReportOrderDataEntity> list = reportOrderDataList.stream().filter(obj ->
                SourceTypeEnum.SO_INFO.getCode().equals(obj.getSourceType())
                        && statusList.contains(obj.getStatus())
                        && approveStatusList.contains(obj.getApproveStatus())
                        && invalidStatusList.contains(obj.getInvalidStatus())
        ).collect(Collectors.toList());
        //无数据直接返回 或 无拆分直接返回
        if (CollectionUtils.isEmpty(list) || !isSplit) {
            return list;
        }
        List<ReportOrderDataEntity> resultList = splitBom(list, bomChildrenSkuList);
        return resultList;
    }


    /**
     * 拆分bom信息
     * @author will
     * @date 2024/9/29 16:53
     * @param list
     * @param bomChildrenSkuList
     * @return List<ReportOrderDataEntity>
     */
    private List<ReportOrderDataEntity> splitBom ( List<ReportOrderDataEntity> list,List<BomChildrenSkuDTO> bomChildrenSkuList) {
        List<ReportOrderDataEntity> resultList = new ArrayList<>();
        for (ReportOrderDataEntity orderDataEntity : list) {
            //bom信息
            List<BomChildrenSkuDTO> bomList = bomChildrenSkuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentSkuId(), orderDataEntity.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(bomList)) {
                resultList.add(orderDataEntity);
                continue;
            }
            //拆分
            for (BomChildrenSkuDTO skuDTO : bomList) {
                ReportOrderDataEntity newEntity = new ReportOrderDataEntity();
                BeanMapperUtils.copy(orderDataEntity,newEntity);
                newEntity.setSkuId(skuDTO.getSkuId());
                newEntity.setQty(orderDataEntity.getQty() * skuDTO.getQuantity());
                newEntity.setDeliveryNoticeQty(orderDataEntity.getDeliveryNoticeQty() *  skuDTO.getQuantity());
                newEntity.setFrozenQty(orderDataEntity.getFrozenQty() *  skuDTO.getQuantity());
                resultList.add(newEntity);
            }
        }
        return resultList;
    }


    /**
     * 格式化订单需求明细
     * @author will
     * @date 2024/9/27 10:52
     * @param entity
     * @param skuList
     * @param virtualInventoryList
     * @param isSplit
     * @return List<AddDTO>
     */
    private List<ReportOrderDemandDetailDTO.AddDTO> handleReportOrderDemandDetail(ReportOrderDataEntity entity,List<BomChildrenSkuDTO> skuList,
                                                                                  List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList,Boolean isSplit) {
        List<ReportOrderDemandDetailDTO.AddDTO> addList = new ArrayList<>();
        ReportOrderDemandDetailDTO.AddDTO addDTO = new ReportOrderDemandDetailDTO.AddDTO();
        BeanMapperUtils.copy(entity,addDTO);
        if (CollectionUtils.isNotEmpty(skuList) && isSplit) {
            //添加bom信息
            List<ReportOrderDemandDetailDTO.BomJsonDTO> bomJsonList = BeanUtil.copyToList(skuList, ReportOrderDemandDetailDTO.BomJsonDTO.class);
            JSONArray bomJson = JSONUtil.parseArray(bomJsonList);
            //拆分
            for (BomChildrenSkuDTO bomChildrenSkuDTO : skuList) {
                //添加bom数据
                ReportOrderDemandDetailDTO.AddDTO bomAddDTO = new ReportOrderDemandDetailDTO.AddDTO();
                BeanMapperUtils.copy(addDTO,bomAddDTO);
                //子级虚拟仓可用
                Integer virtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), bomChildrenSkuDTO.getSkuId())
                                && CharSequenceUtil.equals(obj.getWarehouseId(), entity.getWarehouseId())
                                && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), entity.getVirtualWarehouseId())
                                && CharSequenceUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode()))
                        .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty).reduce(MathUtil.ZERO, Integer::sum);
                bomAddDTO.setSkuId(bomChildrenSkuDTO.getSkuId());

                //订单需求数量
                bomAddDTO.setQty(bomAddDTO.getQty() * bomChildrenSkuDTO.getQuantity());
                //订单数量
                bomAddDTO.setOrderQty(bomAddDTO.getOrderQty() * bomChildrenSkuDTO.getQuantity());
                //发货通知单数量
                bomAddDTO.setDeliveryNoticeQty(bomAddDTO.getDeliveryNoticeQty() * bomChildrenSkuDTO.getQuantity());
                //冻结数量
                bomAddDTO.setFrozenQty(bomAddDTO.getFrozenQty() * bomChildrenSkuDTO.getQuantity());

                //订单需求数量，订单数量 - 发货通知单数量 - 冻结数量
                bomAddDTO.setQty(bomAddDTO.getOrderQty() - bomAddDTO.getDeliveryNoticeQty() - bomAddDTO.getFrozenQty());
                bomAddDTO.setVirtualUsableQty(virtualUsableQty);
                bomAddDTO.setBomJson(bomJson);
                bomAddDTO.setIsSplit(Boolean.TRUE);
                addList.add(bomAddDTO);
            }
        } else {
            Integer virtualUsableQty = virtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), entity.getSkuId())
                            && CharSequenceUtil.equals(obj.getWarehouseId(), entity.getWarehouseId())
                            && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), entity.getVirtualWarehouseId())
                            && CharSequenceUtil.equals(obj.getDictInventoryStatus(), InventoryStatusEnum.USABLE.getCode())
                    )
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty).reduce(MathUtil.ZERO, Integer::sum);
            addDTO.setVirtualUsableQty(virtualUsableQty);
            addList.add(addDTO);
        }
        return addList;
    }

    /**
     * 删除不存在id集合中的数据
     * @author will
     * @date 2024/9/26 19:09
     * @param notIdList
     */
    private void removeByNotIds (List<String> notIdList) {
        baseMapper.removeByNotIds(notIdList);
    }


    /**
     * 根据来源明细id集合查询
     * @author will
     * @date 2024/9/26 18:28
     * @param sourceDetailIdList
     * @return List<ReportOrderDataEntity>
     */
    private List<ReportOrderDataEntity> listBySourceDetailIdList(List<String> sourceDetailIdList){
        if (CollectionUtils.isEmpty(sourceDetailIdList)) {
            return Collections.emptyList();
        }
       return lambdaQuery().in(ReportOrderDataEntity::getSourceDetailId,sourceDetailIdList).list();
    }

    /**
    * 新增修改处理数据
    */
    private  List<ReportOrderDataEntity> handleData(List<ReportOrderDataEntity> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return resultList;
        }
        List<String> sourceDetailIdList = resultList.stream().filter(obj -> CharSequenceUtil.isNotEmpty(obj.getSourceDetailId())).map(ReportOrderDataEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<List<String>> sourceDetailIdListPartition = Lists.partition(sourceDetailIdList, 50000);

        //发货通知单
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailList = new ArrayList<>();
        for (List<String> sourceDetailIdPartition : sourceDetailIdListPartition) {
            List<SoDeliveryNoticeDetailEntity> list = soDeliveryNoticeDetailService.listDetailBySourceDetailIds(sourceDetailIdPartition);
            if (CollectionUtils.isNotEmpty(list)) {
                soDeliveryNoticeDetailList.addAll(list);
            }
        }
        List<ReportOrderDataEntity> addOrUpdateList = new ArrayList<>();
        for (ReportOrderDataEntity entity : resultList) {

            //发货通知单数量（仅b2b）
            Integer deliveryNoticeQty = soDeliveryNoticeDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), entity.getSourceDetailId())).map(SoDeliveryNoticeDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            entity.setDeliveryNoticeQty(deliveryNoticeQty);
            //冻结数量（仅b2b）
            Integer frozenQty = ObjectUtil.isEmpty(entity.getFrozenQty()) ? MathUtil.ZERO : entity.getFrozenQty();
            //需求数量,订单数量 - 发货通知单数量 - 冻结数量
            entity.setQty(entity.getOrderQty() - deliveryNoticeQty - frozenQty);

            addOrUpdateList.add(entity);
        }
        return addOrUpdateList;
    }
}
