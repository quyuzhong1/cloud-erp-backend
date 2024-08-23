package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ConfirmStatusEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.tms.dto.FirstMileEstimatedBillDTO;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.CostAllocationEnum;
import com.erp.model.tms.enums.DictCostCategoryEnum;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.plm.feign.LogisticsProductFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.tms.mapper.FirstMileCostAllocationMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 头程费用分摊 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
@Slf4j
@Service
public class FirstMileCostAllocationServiceImpl extends SuperServiceImpl<FirstMileCostAllocationMapper, FirstMileCostAllocationEntity> implements FirstMileCostAllocationService {
    @Autowired
    private OperateLogService operateLogService;
    //sku分摊明细
    @Resource
    private FirstMileSkuCostAllocationDetailService firstMileSkuCostAllocationDetailService;
    //sku费用分摊
    @Resource
    private FirstMileSkuCostAllocationService firstMileSkuCostAllocationService;
    //物流单
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    //重量分摊
    @Lazy
    @Resource
    private FirstMileWeightAllocationService firstMileWeightAllocationService;
    //期初
    @Lazy
    @Resource
    private InitFirstMileAllocationService initFirstMileAllocationService;
    @Resource
    private InitFirstMileAllocationDetailService initFirstMileAllocationDetailService;
    //SKU成本
    @Lazy
    @Resource
    private InventorySkuCostService inventorySkuCostService;
    //发货单
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    //暂估
    @Lazy
    @Resource
    private FirstMileEstimatedBillService firstMileEstimatedBillService;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private LogisticsProductFeign logisticsProductFeign;
    @Resource
    private ReportPeriodMonthService reportPeriodMonthService;
    @Resource
    private TmsFirstMileReconciliationDetailService tmsFirstMileReconciliationDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileCostAllocationDTO.AddDTO addDTO) {
        FirstMileCostAllocationEntity firstMileCostAllocationEntity = new FirstMileCostAllocationEntity();
        BeanMapperUtils.copy(addDTO, firstMileCostAllocationEntity);

        // 数据处理
        handleData(firstMileCostAllocationEntity);

        log.info("开始新增头程费用分摊");
        boolean save = super.save(firstMileCostAllocationEntity);
        if(!save) {
            throw new ServiceException("头程费用分摊保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程费用分摊" , firstMileCostAllocationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, firstMileCostAllocationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(firstMileCostAllocationEntity.getId(), firstMileCostAllocationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileCostAllocationDTO.UpdateDTO updateDTO) {
        FirstMileCostAllocationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程费用分摊"));
        FirstMileCostAllocationEntity firstMileCostAllocationEntity =  BeanMapperUtils.map(FirstMileCostAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(firstMileCostAllocationEntity);
        log.info("编辑 开始修改头程费用分摊数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileCostAllocationEntity);
        if(!save) {
            throw new ServiceException("头程费用分摊保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录头程费用分摊日志数据，id：【{}】", firstMileCostAllocationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileCostAllocationEntity.getId(), "头程费用分摊");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, firstMileCostAllocationEntity, null, firstMileCostAllocationEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<FirstMileCostAllocationDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<FirstMileCostAllocationDTO.TabListDTO> list = baseMapper.tabList(dto.getPermissionSql());
        List<FirstMileCostAllocationDTO.TabListDTO> tabListDTOList = new ArrayList<>(2);
        tabListDTOList.add(FirstMileCostAllocationDTO.TabListDTO.builder().tabFlag(ConfirmStatusEnum.WAIT_CONFIRM.getCode()).tabFlagName(ConfirmStatusEnum.WAIT_CONFIRM.getName()).count(getTabCount(ConfirmStatusEnum.WAIT_CONFIRM.getCode(), list)).build());
        tabListDTOList.add(FirstMileCostAllocationDTO.TabListDTO.builder().tabFlag(ConfirmStatusEnum.CONFIRM.getCode()).tabFlagName(ConfirmStatusEnum.CONFIRM.getName()).count(getTabCount(ConfirmStatusEnum.CONFIRM.getCode(), list)).build());
        return tabListDTOList;
    }

    @Override
    public PagingVO<FirstMileCostAllocationDTO.PagingVO> paging(PagingDTO<FirstMileCostAllocationDTO.PagingParamDTO> dto) {
        FirstMileCostAllocationDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<FirstMileCostAllocationDTO.PagingVO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<FirstMileCostAllocationDTO.PagingVO> pageData = baseMapper.paging(query, params);
        List<FirstMileCostAllocationDTO.PagingVO> list = pageData.getRecords();
        fillPagingDb(list);
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(FirstMileCostAllocationEntity entity) {
        //已确认不能删除
        if (ConfirmStatusEnum.CONFIRM.getCode().equals(entity.getStatus())){
            return BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),"头程费用分摊数据已确认不能删除");
        }
        firstMileSkuCostAllocationService.removeByMainId(entity.getId());
        firstMileSkuCostAllocationDetailService.removeByMainId(entity.getId());
        this.lambdaUpdate().eq(FirstMileCostAllocationEntity::getId, entity.getId()).remove();
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), "删除记录操作成功");
    }

    @Override
    public void exportExcel(FirstMileCostAllocationDTO.PagingParamDTO params, HttpServletResponse response) {
        params.setPermissionSql(params.getPermissionSql());
        List<FirstMileCostAllocationDTO.PagingVO> list = baseMapper.exportList(params);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        // 填充字段值
        fillPagingDb(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/firstMileCostAllocationExport.xlsx";
        String name = "头程费用分摊导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public List<FirstMileCostAllocationEntity> getByInitFirstMileId(String firstMileId) {
        if (StrUtil.isBlank(firstMileId)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(FirstMileCostAllocationEntity::getInitFirstMileId,firstMileId).list();
    }

    @Override
    public List<FirstMileCostAllocationEntity> getBySkuCostId(String skuCostId) {
        if (StrUtil.isBlank(skuCostId)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(FirstMileCostAllocationEntity::getSkuCostId,skuCostId).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO calcAllocatedCost(FirstMileCostAllocationEntity entity, FirstMileDeliveryEntity firstMileDeliveryEntity,List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList) {
        if (ConfirmStatusEnum.CONFIRM.getCode().equals(entity.getStatus())){
            return BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),"费用分摊已确认，无法重新分摊");
        }
        if (StrUtil.isBlank(entity.getSourceId())){
            return BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),"发货单不能为空");
        }
        //处理分摊数据
        return processAllocationData(entity,entity.getReportPeriodId(), firstMileDeliveryEntity,firstMileDeliveryDetailEntityList);
    }

    /**
     * 处理分摊数据
     * @param firstMileDeliveryDetailEntityList 发货单明细
     * @param firstMileDeliveryEntity 发货单
     * @param reportPeriodId 核算期间
     * @param entity 费用分摊
     */
    private BatchResultDTO processAllocationData(FirstMileCostAllocationEntity entity, String reportPeriodId, FirstMileDeliveryEntity firstMileDeliveryEntity,List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList) {
        //取值发货单对应的目的仓关联组织
        if (StrUtil.isBlank(firstMileDeliveryEntity.getDestWarehouseId())){
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(),firstMileDeliveryEntity.getCode(),"发货单目的仓不能为空");
        }
        List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(Collections.singletonList(firstMileDeliveryEntity.getDestWarehouseId()));
        if (CollectionUtils.isEmpty(updateDTOS)){
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(),firstMileDeliveryEntity.getCode(),StrUtil.format("发货单目的仓记录【{}】不存在", firstMileDeliveryEntity.getDestWarehouseName()));
        }
        String orgId = updateDTOS.stream().map(WarehouseDTO.UpdateDTO::getOrgId).filter(StrUtil::isNotBlank).findFirst().orElse(null);
        if (StrUtil.isBlank(orgId)){
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(),firstMileDeliveryEntity.getCode(),StrUtil.format("发货单目的仓【{}】未关联组织", firstMileDeliveryEntity.getDestWarehouseName()));
        }
        SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(orgId);
        if (Objects.isNull(company)){
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(),firstMileDeliveryEntity.getCode(),StrUtil.format("发货单目的仓关联组织【{}】不存在", orgId));
        }
        //核算期间id
        reportPeriodId = reportPeriodMonthService.createOrUpdatePeriod(company,reportPeriodId);
        //重量分摊记录--费用状态为{未分摊，部分分摊}
        List<FirstMileWeightAllocationEntity> weightAllocationEntityList = firstMileWeightAllocationService.listBySourceIds(Collections.singletonList(firstMileDeliveryEntity.getId()), null);
        //物流单
        List<LogisticsBillEntity> logisticsBillEntityList = logisticsBillService.listByOutstockIdList(Collections.singletonList(firstMileDeliveryEntity.getId()));
        if (CollectionUtils.isEmpty(logisticsBillEntityList)){
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(),firstMileDeliveryEntity.getCode(),"未下发物流单，无法分摊");
        }
        LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.stream().filter(e -> StrUtil.isNotBlank(e.getLogisticsSupplierId())).findFirst().orElse(null);
        LogisticsSupplierEntity supplierEntity = null;
        if (Objects.nonNull(logisticsBillEntity) && StrUtil.isNotBlank(logisticsBillEntity.getLogisticsSupplierId())){
            supplierEntity = logisticsSupplierService.getById(logisticsBillEntity.getLogisticsSupplierId());
        }
        //对账单明细 [已审核记录]
        List<TmsFirstMileReconciliationDetailEntity> reconciliationDetailEntityList = tmsFirstMileReconciliationDetailService.listByBusinessCodes(Collections.singletonList(firstMileDeliveryEntity.getCode()), ApproveStatusEnum.APPROVE.getStatus());
        //暂估账单 [已确认]
        List<FirstMileEstimatedBillDTO.View> estimatedBillEntityList = firstMileEstimatedBillService.listByLogisticsBillIds(logisticsBillEntityList.stream().map(LogisticsBillEntity::getId).distinct().collect(Collectors.toList()));
        //sku分摊记录
        List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList = firstMileSkuCostAllocationService.listByMainIds(Collections.singletonList(entity.getId()));
        //sku分摊明细记录
        List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntityList = firstMileSkuCostAllocationDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        //期初分摊[已审核记录]
        List<InitFirstMileAllocationDetailEntity> initFirstMileAllocationDetailEntityList = initFirstMileAllocationDetailService.listBySourceIds(Collections.singletonList(firstMileDeliveryEntity.getId()), ApproveStatusEnum.APPROVE.getStatus());
        //SKU成本[已审核记录]
//        List<InventorySkuCostEntity> inventorySkuCostEntityList = inventorySkuCostService.listBy
        //创建-重算费用分摊主表 发货单与物流单=1：1
        entity.setReportPeriodId(reportPeriodId);
        entity.setBusinessCode(firstMileDeliveryEntity.getSourceCode());
        entity.setBusinessType(firstMileDeliveryEntity.getDemandType());
        entity.setSourceId(firstMileDeliveryEntity.getId());
        entity.setSourceCode(firstMileDeliveryEntity.getCode());
        if (Objects.nonNull(logisticsBillEntity)){
            entity.setSupplierId(logisticsBillEntity.getLogisticsSupplierId());
            entity.setTransportNo(logisticsBillEntity.getTransportNo());
            entity.setShopId(logisticsBillEntity.getShopId());
            entity.setShopName(logisticsBillEntity.getShopName());
        }
        if (Objects.nonNull(supplierEntity)){
            entity.setSupplierName(supplierEntity.getSupplierName());
        }
        entity.setFromWarehouseId(firstMileDeliveryEntity.getDeliveryWarehouseId());
        entity.setFromWarehouseName(firstMileDeliveryEntity.getDeliveryWarehouseName());
        entity.setReportPeriodId(reportPeriodId);
        entity.setReportPeriodId(reportPeriodId);
        this.saveOrUpdate(entity);
        //构建sku分摊记录
        buildSkuAllocation(firstMileDeliveryDetailEntityList,entity, skuCostAllocationEntityList,skuCostAllocationDetailEntityList,initFirstMileAllocationDetailEntityList);
        return BatchResultDTO.success(entity.getId(),entity.getSourceCode(),"重新分摊完成");
    }

    /**
     * @param firstMileDeliveryDetailEntityList       发货单明细
     * @param entity
     * @param skuCostAllocationEntityList             sku分摊记录
     * @param skuCostAllocationDetailEntityList       sku分摊明细
     * @param initFirstMileAllocationDetailEntityList  期初
     */
    private void buildSkuAllocation(List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList,
                                    FirstMileCostAllocationEntity entity, List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList,
                                    List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntityList,
                                    List<InitFirstMileAllocationDetailEntity> initFirstMileAllocationDetailEntityList) {
        if (CollectionUtils.isEmpty(firstMileDeliveryDetailEntityList)){
            return;
        }
        List<String> skuIds = firstMileDeliveryDetailEntityList.stream().map(FirstMileDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //核算月份
        ReportPeriodMonthEntity reportPeriodMonth = reportPeriodMonthService.getById(entity.getReportPeriodId());
        //根据sku获取签收数量汇总
        List<FirstMileDeliveryDTO.ReceiveDTO> receiveDTOS = wmsFirstMileDeliveryFeign.countReceiveQtyByParams(
                FirstMileDeliveryDTO.RequestReceiveDTO.builder()
                        .businessCodes(Collections.singletonList(entity.getBusinessCode()))
                        .month(reportPeriodMonth.getMonth())
                        .build());
        //子sku列表
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //合并 子sku和父级sku获取 全量sku明细
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(bomChildrenSkuDTOS)) {
            skuIds = Stream.concat(skuIds.stream(), bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getSkuId).filter(StrUtil::isNotEmpty))
                    .collect(Collectors.toList());
        }
        //sku成本
        List<InventorySkuCostDTO.PagingVO> skuCostList = inventorySkuCostService.listDetailByOrgIdAndSkuIds(reportPeriodMonth.getOrgId(), skuIds, ApproveStatusEnum.APPROVE.getStatus());
        //查询发货单费用分摊记录
        List<FirstMileCostAllocationDTO.PagingVO> voList = baseMapper.listSkuBySourceCodes(Collections.singletonList(entity.getSourceCode()));
        //遍历计算sku分摊
        for (FirstMileDeliveryDetailEntity deliveryDetailEntity : firstMileDeliveryDetailEntityList){
            //查询是否存在发货单其他费用分摊账期
            FirstMileCostAllocationDTO.PagingVO oldAllocation = voList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(deliveryDetailEntity.getSkuId()) &&
                    e.getReconciliationMonth().isAfter(reportPeriodMonth.getMonth())).findFirst().orElse(null);
            FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity = skuCostAllocationEntityList.stream().filter(e -> Objects.nonNull(e)
                    &&e.getSkuId().equals(deliveryDetailEntity.getSkuId())).findFirst().orElse(null);
            if (Objects.isNull(firstMileSkuCostAllocationEntity)){
                firstMileSkuCostAllocationEntity = new FirstMileSkuCostAllocationEntity();
            }
            InitFirstMileAllocationDetailEntity initFirstMileAllocationDetailEntity = initFirstMileAllocationDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(deliveryDetailEntity.getSkuId()))
                    .findFirst().orElse(null);
            firstMileSkuCostAllocationEntity
                    .setSkuId(deliveryDetailEntity.getSkuId())
                    .setSkuNo(deliveryDetailEntity.getSkuNo())
                    .setPlatformSkuNo(deliveryDetailEntity.getPlatformSkuNo())
                    .setDeliveryQty(deliveryDetailEntity.getDeliveryQty());
            //分摊重量 若有期初值则取值期初，无期初值则
            //{分摊重量}取值【头程重量分摊】单据的分摊重量(KG)【按照业务单号+SKU累计统计】
            if (Objects.nonNull(initFirstMileAllocationDetailEntity)){
                firstMileSkuCostAllocationEntity.setAllocatedWeight(initFirstMileAllocationDetailEntity.getWeightAllocation());

            }
            //单位成本 第一次取值期初值则取值期初
            //后续生成则取值分摊组织下的产品成本【已审核】
            if (Objects.isNull(oldAllocation) && Objects.nonNull(initFirstMileAllocationDetailEntity)){
                firstMileSkuCostAllocationEntity.setProductCost(initFirstMileAllocationDetailEntity.getProductCost());
                firstMileSkuCostAllocationEntity.setInitReceiveQty(initFirstMileAllocationDetailEntity.getInitReceiveQty());
            }else {
                //先判断sku是否是组合品，是组合品则要汇总子sku产品成本
                List<BomChildrenSkuDTO> skuDTOList = bomChildrenSkuDTOS.stream().filter(e -> StrUtil.isNotEmpty(e.getParentSkuId())
                                && StrUtil.isNotEmpty(e.getParentSkuNo()) && e.getParentSkuId().equals(deliveryDetailEntity.getSkuId())).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(skuDTOList) && BomTypeEnum.COMBINATION.getType().equals(skuDTOList.get(0).getType())){
                    BigDecimal productCost = skuDTOList.stream().map(e ->{
                        InventorySkuCostDTO.PagingVO pagingVO = skuCostList.stream().filter(f -> f.getSkuId().equals(e.getSkuId())).findFirst().orElse(null);
                        if (Objects.isNull(pagingVO)){
                            return BigDecimal.ZERO;
                        }else {
                            return pagingVO.getProductCost().multiply(BigDecimal.valueOf(e.getQuantity()));
                        }
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
                    firstMileSkuCostAllocationEntity.setProductCost(productCost);
                }else {
                    InventorySkuCostDTO.PagingVO pagingVO = skuCostList.stream().filter(f -> f.getSkuId().equals(deliveryDetailEntity.getSkuId())).findFirst().orElse(null);
                    if (Objects.nonNull(pagingVO)){
                        firstMileSkuCostAllocationEntity.setProductCost(pagingVO.getProductCost());
                    }else {
                        firstMileSkuCostAllocationEntity.setProductCost(BigDecimal.ZERO);
                    }
                }
            }
            //总成本
            firstMileSkuCostAllocationEntity.setProductTotalCost(firstMileSkuCostAllocationEntity.getProductCost().multiply(BigDecimal.valueOf(firstMileSkuCostAllocationEntity.getDeliveryQty())));
            //签收数量
            firstMileSkuCostAllocationEntity.setCurrentMonthReceiveQty(receiveDTOS.stream().filter(e -> e.getSkuId().equals(deliveryDetailEntity.getSkuId())).map(FirstMileDeliveryDTO.ReceiveDTO::getCurrentMonthReceiveQty).reduce(MathUtil.ZERO, Integer::sum));
            firstMileSkuCostAllocationEntity.setLastMonthReceiveQty(receiveDTOS.stream().filter(e -> e.getSkuId().equals(deliveryDetailEntity.getSkuId())).map(FirstMileDeliveryDTO.ReceiveDTO::getLastMonthReceiveQty).reduce(MathUtil.ZERO, Integer::sum));
            firstMileSkuCostAllocationEntity.setAsLastMonthReceiveQty(receiveDTOS.stream().filter(e -> e.getSkuId().equals(deliveryDetailEntity.getSkuId())).map(FirstMileDeliveryDTO.ReceiveDTO::getAsLastMonthReceiveQty).reduce(MathUtil.ZERO, Integer::sum));
            firstMileSkuCostAllocationEntity.setAsCurrentMonthReceiveQty(receiveDTOS.stream().filter(e -> e.getSkuId().equals(deliveryDetailEntity.getSkuId())).map(FirstMileDeliveryDTO.ReceiveDTO::getAsCurrentMonthReceiveQty).reduce(MathUtil.ZERO, Integer::sum));

        }
    }

    private void fillPagingDb(List<FirstMileCostAllocationDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //添加分摊明细
        list.forEach(e -> {
            e.setStatusName(ConfirmStatusEnum.getName(e.getStatus()));
            e.setFeeTypeName(DictCostCategoryEnum.getName(e.getFeeType()));
            e.setAllocationTypeName(CostAllocationEnum.getName(e.getAllocationType()));
        });
    }
    /**
     * 根据状态获取分页统计数量
     *
     * @param status
     * @param list
     * @return
     */
    private Integer getTabCount(String status, List<FirstMileCostAllocationDTO.TabListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return MathUtil.ZERO;
        }
        FirstMileCostAllocationDTO.TabListDTO tabListDTO = list.stream().filter(e -> Objects.nonNull(e) && status.equals(e.getTabFlag())).findFirst().orElse(null);
        if (Objects.nonNull(tabListDTO)) {
            return tabListDTO.getCount();
        } else {
            return MathUtil.ZERO;
        }
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileCostAllocationEntity firstMileCostAllocationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
