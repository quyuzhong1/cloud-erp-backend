package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

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
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    FirstMileSkuCostRefService firstMileSkuCostRefService;
    @Resource
    private FirstMileCostAllocationService service;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
        if (!save) {
            throw new ServiceException("头程费用分摊保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程费用分摊", firstMileCostAllocationEntity.getId());
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
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "头程费用分摊"));
        FirstMileCostAllocationEntity firstMileCostAllocationEntity = BeanMapperUtils.map(FirstMileCostAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(firstMileCostAllocationEntity);
        log.info("编辑 开始修改头程费用分摊数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileCostAllocationEntity);
        if (!save) {
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
        if (ConfirmStatusEnum.CONFIRM.getCode().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), "头程费用分摊数据已确认不能删除");
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
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO calcAllocatedCost(FirstMileCostAllocationEntity entity, FirstMileDeliveryEntity firstMileDeliveryEntity, List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList) {
        if (ConfirmStatusEnum.CONFIRM.getCode().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), "费用分摊已确认，无法重新分摊");
        }
        if (StrUtil.isBlank(entity.getSourceId())) {
            return BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), "发货单不能为空");
        }
        if (CollectionUtils.isEmpty(firstMileDeliveryDetailEntityList)) {
            return BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), "发货单明细不能为空");
        }
        //处理分摊数据
        return processAllocationData(entity, firstMileDeliveryEntity, firstMileDeliveryDetailEntityList);
    }

    /**
     * 处理分摊数据
     *
     * @param firstMileDeliveryDetailEntityList 发货单明细
     * @param firstMileDeliveryEntity           发货单
     * @param entity                            费用分摊
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO processAllocationData(FirstMileCostAllocationEntity entity, FirstMileDeliveryEntity firstMileDeliveryEntity, List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList) {
        //取值发货单对应的目的仓关联组织
        if (StrUtil.isBlank(firstMileDeliveryEntity.getDestWarehouseId())) {
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(), firstMileDeliveryEntity.getCode(), "发货单目的仓不能为空");
        }
        List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(Collections.singletonList(firstMileDeliveryEntity.getDestWarehouseId()));
        if (CollectionUtils.isEmpty(updateDTOS)) {
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(), firstMileDeliveryEntity.getCode(), StrUtil.format("发货单目的仓记录【{}】不存在", firstMileDeliveryEntity.getDestWarehouseName()));
        }
        String orgId = updateDTOS.stream().map(WarehouseDTO.UpdateDTO::getOrgId).filter(StrUtil::isNotBlank).findFirst().orElse(null);
        if (StrUtil.isBlank(orgId)) {
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(), firstMileDeliveryEntity.getCode(), StrUtil.format("发货单目的仓【{}】未关联组织", firstMileDeliveryEntity.getDestWarehouseName()));
        } else {
            entity.setOrgId(orgId);
        }
        SysAccountingCompanyEntity company = sysUserFeign.getCompanyById(orgId);
        if (Objects.isNull(company)) {
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(), firstMileDeliveryEntity.getCode(), StrUtil.format("发货单目的仓关联组织【{}】不存在", orgId));
        } else {
            entity.setOrgName(company.getCompanyName());
        }
        //核算期间id
        String reportPeriodId = reportPeriodMonthService.createOrUpdatePeriod(company, entity);
        //重量分摊记录--费用状态为{未分摊，部分分摊}
        List<String> statusList = new ArrayList<>(2);
        statusList.add(CostAllocationStatusEnum.NOT.getCode());
        statusList.add(CostAllocationStatusEnum.PART.getCode());
        List<FirstMileWeightAllocationEntity> weightAllocationEntityList = firstMileWeightAllocationService.listBySourceIds(Collections.singletonList(firstMileDeliveryEntity.getId()), statusList);
        //物流单
        List<LogisticsBillEntity> logisticsBillEntityList = logisticsBillService.listByOutstockIdList(Collections.singletonList(firstMileDeliveryEntity.getId()));
        if (CollectionUtils.isEmpty(logisticsBillEntityList)) {
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(), firstMileDeliveryEntity.getCode(), "未下发物流单，无法分摊");
        }
        LogisticsBillEntity logisticsBillEntity = logisticsBillEntityList.stream().filter(e -> StrUtil.isNotBlank(e.getLogisticsSupplierId())).findFirst().orElse(null);
        LogisticsSupplierEntity supplierEntity = null;
        if (Objects.nonNull(logisticsBillEntity) && StrUtil.isNotBlank(logisticsBillEntity.getLogisticsSupplierId())) {
            supplierEntity = logisticsSupplierService.getById(logisticsBillEntity.getLogisticsSupplierId());
        }
        //对账单明细 [已审核记录]
        List<TmsFirstMileReconciliationDetailEntity> reconciliationDetailEntityList = tmsFirstMileReconciliationDetailService.listByBusinessCodes(Collections.singletonList(firstMileDeliveryEntity.getCode()), ApproveStatusEnum.APPROVE.getStatus());
        //暂估账单 [已确认]
        List<FirstMileEstimatedBillDTO.View> estimatedBillEntityList = null;
        try {
            estimatedBillEntityList = firstMileEstimatedBillService.listByLogisticsBillIds(logisticsBillEntityList.stream().map(LogisticsBillEntity::getId).distinct().collect(Collectors.toList()), ConfirmStatusEnum.CONFIRM.getCode());
        }catch (Exception e){
            log.error("processAllocationData: 暂估账单获取异常:{}", e.getMessage());
            return BatchResultDTO.fail(firstMileDeliveryEntity.getId(), firstMileDeliveryEntity.getCode(), "暂估账单获取异常" + e.getMessage());
        }
        //sku分摊记录
        List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList = null;
        if (StrUtil.isNotBlank(entity.getId())) {
            skuCostAllocationEntityList = firstMileSkuCostAllocationService.listByMainIds(Collections.singletonList(entity.getId()));
        }
        //期初分摊[已审核记录]
        List<InitFirstMileAllocationDetailEntity> initFirstMileAllocationDetailEntityList = initFirstMileAllocationDetailService.listBySourceIds(Collections.singletonList(firstMileDeliveryEntity.getId()), ApproveStatusEnum.APPROVE.getStatus());

        //创建-重算费用分摊主表 发货单与物流单=1：1
        entity.setReportPeriodId(reportPeriodId);
        entity.setBusinessCode(firstMileDeliveryEntity.getSourceCode());
        entity.setBusinessType(firstMileDeliveryEntity.getDemandType());
        entity.setSourceId(firstMileDeliveryEntity.getId());
        entity.setSourceCode(firstMileDeliveryEntity.getCode());
        if (Objects.nonNull(logisticsBillEntity)) {
            entity.setLogisticsBillId(logisticsBillEntity.getId());
            entity.setSupplierId(logisticsBillEntity.getLogisticsSupplierId());
            entity.setTransportNo(logisticsBillEntity.getTransportNo());
            entity.setShopId(logisticsBillEntity.getShopId());
            entity.setShopName(logisticsBillEntity.getShopName());
        }
        if (Objects.nonNull(supplierEntity)) {
            entity.setSupplierName(supplierEntity.getSupplierName());
        }
        entity.setFromWarehouseId(firstMileDeliveryEntity.getDeliveryWarehouseId());
        entity.setFromWarehouseName(firstMileDeliveryEntity.getDeliveryWarehouseName());
        entity.setToWarehouseId(firstMileDeliveryEntity.getDestWarehouseId());
        entity.setToWarehouseName(firstMileDeliveryEntity.getDestWarehouseName());
        entity.setReportPeriodId(reportPeriodId);
        //构建sku分摊记录
        return buildSkuAllocation(firstMileDeliveryDetailEntityList, entity, skuCostAllocationEntityList, initFirstMileAllocationDetailEntityList, weightAllocationEntityList, reconciliationDetailEntityList, estimatedBillEntityList);
    }

    /**
     * 构建sku分摊记录
     *
     * @param firstMileDeliveryDetailEntityList       发货单明细
     * @param entity                                  费用分摊主记录
     * @param skuCostAllocationEntityList             sku分摊记录
     * @param initFirstMileAllocationDetailEntityList 期初
     * @param weightAllocationEntityList              重量分摊
     * @param reconciliationDetailEntityList          对账单
     * @param estimatedBillEntityList                 暂估账单
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO buildSkuAllocation(List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList,
                                             FirstMileCostAllocationEntity entity, List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList,
                                             List<InitFirstMileAllocationDetailEntity> initFirstMileAllocationDetailEntityList,
                                             List<FirstMileWeightAllocationEntity> weightAllocationEntityList,
                                             List<TmsFirstMileReconciliationDetailEntity> reconciliationDetailEntityList,
                                             List<FirstMileEstimatedBillDTO.View> estimatedBillEntityList) {
        if (CollectionUtils.isEmpty(firstMileDeliveryDetailEntityList)) {
            return BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), StrUtil.format("发货单无发货单明细，无法进行费用分摊"));
        }
        //优先取实际账单 实际账单不存在时取暂估账单
        if (CollectionUtils.isEmpty(reconciliationDetailEntityList) && CollectionUtils.isEmpty(estimatedBillEntityList)) {
            return BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), StrUtil.format("对账单和暂估账单不能同时为空"));
        }
        //费用分摊配置查询
        CfgSettingEntity cfgSetting = cfgSettingService.getByKey(CfgSettingEnum.ALLOCATION_SETTING.getCode());
        if (Objects.isNull(cfgSetting) || Objects.isNull(cfgSetting.getDataJson())) {
            return BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), "系统费用分摊配置不存在");
        }
        CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO = JSONUtil.toBean(cfgSetting.getDataJson(), CfgSettingValueDTO.AllocationSettingDTO.class);

        if (!CollectionUtils.isEmpty(reconciliationDetailEntityList)) {
            // TODO 后面再进行考虑定时分摊计算，现在只考虑分摊重算
            TmsFirstMileReconciliationDetailEntity oldReconciliationDetailEntity = reconciliationDetailEntityList.stream().filter(e -> Objects.nonNull(entity.getReconciliationMonth()) && Objects.equals(e.getReconciliationMonth(), entity.getReconciliationMonth())).findFirst().orElse(null);
            if (Objects.nonNull(oldReconciliationDetailEntity)) {
                entity.setStatus(ConfirmStatusEnum.WAIT_CONFIRM.getCode());
                entity.setReconciliationId(oldReconciliationDetailEntity.getId());
                entity.setReconciliationMonth(oldReconciliationDetailEntity.getReconciliationMonth());
                //根据对账单分别记录费用分摊主表记录
                return buildSkuAllocationByBill(null, oldReconciliationDetailEntity, entity, firstMileDeliveryDetailEntityList, skuCostAllocationEntityList, initFirstMileAllocationDetailEntityList, weightAllocationEntityList, allocationSettingDTO);
            } else {
                for (TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity : reconciliationDetailEntityList) {
                    entity.setStatus(ConfirmStatusEnum.WAIT_CONFIRM.getCode());
                    entity.setReconciliationId(reconciliationDetailEntity.getId());
                    entity.setReconciliationMonth(reconciliationDetailEntity.getReconciliationMonth());
                    //根据对账单分别记录费用分摊主表记录
                    buildSkuAllocationByBill(null, reconciliationDetailEntity, entity, firstMileDeliveryDetailEntityList, skuCostAllocationEntityList, initFirstMileAllocationDetailEntityList, weightAllocationEntityList, allocationSettingDTO);
                }
            }
        } else {
            //一个发货单只会存在一条暂估账单
            FirstMileEstimatedBillDTO.View firstMileEstimatedBillEntity = estimatedBillEntityList.get(0);
            entity.setStatus(ConfirmStatusEnum.WAIT_CONFIRM.getCode());
            entity.setEstimatedBillId(firstMileEstimatedBillEntity.getId());
            entity.setReconciliationMonth(null);
            return buildSkuAllocationByBill(firstMileEstimatedBillEntity, null, entity, firstMileDeliveryDetailEntityList, skuCostAllocationEntityList, initFirstMileAllocationDetailEntityList, weightAllocationEntityList, allocationSettingDTO);
        }
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), "重新分摊完成");
    }


    /**
     * 根据实际账单计算对账月份下 sku分摊记录
     *
     * @param firstMileEstimatedBillEntity
     * @param reconciliationDetailEntity
     * @param entity
     * @param firstMileDeliveryDetailEntityList
     * @param skuCostAllocationEntityList
     * @param initFirstMileAllocationDetailEntityList
     * @param weightAllocationEntityList
     * @param allocationSettingDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO buildSkuAllocationByBill(FirstMileEstimatedBillDTO.View firstMileEstimatedBillEntity, TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity,
                                                   FirstMileCostAllocationEntity entity, List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList,
                                                   List<FirstMileSkuCostAllocationEntity> skuCostAllocationEntityList,
                                                   List<InitFirstMileAllocationDetailEntity> initFirstMileAllocationDetailEntityList,
                                                   List<FirstMileWeightAllocationEntity> weightAllocationEntityList, CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO) {


        List<String> skuIds = firstMileDeliveryDetailEntityList.stream().map(FirstMileDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //核算月份
        ReportPeriodMonthEntity reportPeriodMonth = reportPeriodMonthService.getById(entity.getReportPeriodId());
        if (Objects.nonNull(reconciliationDetailEntity)) {
            entity.setReconciliationId(reconciliationDetailEntity.getMainId());
            entity.setReportPeriodMonth(reportPeriodMonth.getMonth());
            entity.setReconciliationMonth(reconciliationDetailEntity.getReconciliationMonth());
        }
        //根据分摊记录查询费用分摊记录是否存在
        BatchResultDTO batchResultDTO1 = checkCostAllocationExist(entity);
        if (!batchResultDTO1.getSuccess()) {
            //为创建主表记录之前都可以返回异常消息，否则就要抛出异常回退
            return batchResultDTO1;
        }
        //费用分摊重算
        String id = entity.getId();
        if (StrUtil.isNotBlank(id)) {
            //删除之前的sku分摊记录和明细记录 防止存在1对多个对账月份情况
            firstMileSkuCostAllocationService.removeByMainId(id);
            //删除sku分摊记录-sku成本关联记录
            firstMileSkuCostRefService.removeBySkuCostAllocation(skuCostAllocationEntityList);
            //删除sku分摊明细记录
            firstMileSkuCostAllocationDetailService.removeByMainId(id);
        }
        //查询发货单费用分摊记录（不包含本记录分摊）
        List<FirstMileCostAllocationDTO.PagingVO> voList = baseMapper.listSkuBySourceCodes(Collections.singletonList(entity.getSourceCode()));
        //上期账单是实际账单 并且所有费用分类的期末在途费用为0则不进行费用分摊
        BatchResultDTO batchResultDTO = checkCostAllocation(voList, entity.getReportPeriodMonth(), entity.getReconciliationMonth());
        if (!batchResultDTO.getSuccess()) {
            //为创建主表记录之前都可以返回异常消息，否则就要抛出异常回退
            return batchResultDTO;
        }

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
        //保存分摊主表记录
        this.saveOrUpdate(entity);
        List<FirstMileSkuCostAllocationEntity> firstMileSkuCostAllocationEntityList = new ArrayList<>(firstMileDeliveryDetailEntityList.size());
        //遍历计算sku分摊
        for (FirstMileDeliveryDetailEntity deliveryDetailEntity : firstMileDeliveryDetailEntityList) {
            //查询是否存在发货单其他费用分摊账期
            FirstMileCostAllocationDTO.PagingVO oldAllocation = voList.stream().filter(e -> Objects.nonNull(e)
                    && StrUtil.isNotBlank(e.getSkuId()) && StrUtil.isNotBlank(deliveryDetailEntity.getSkuId()) && e.getSkuId().equals(deliveryDetailEntity.getSkuId())
                    && Objects.nonNull(e.getReconciliationMonth()) && Objects.nonNull(reportPeriodMonth.getMonth()) && e.getReconciliationMonth().isAfter(reportPeriodMonth.getMonth()))
                    .findFirst().orElse(null);
            FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity = null;
            if (!CollectionUtils.isEmpty(skuCostAllocationEntityList)){
                firstMileSkuCostAllocationEntity = skuCostAllocationEntityList.stream().filter(e -> Objects.nonNull(e)
                        && e.getSkuId().equals(deliveryDetailEntity.getSkuId())).findFirst().orElse(null);
            }
            if (Objects.isNull(firstMileSkuCostAllocationEntity)) {
                firstMileSkuCostAllocationEntity = new FirstMileSkuCostAllocationEntity();
            }
            //上面已经删除了记录，这里重置sku分摊id
            firstMileSkuCostAllocationEntity.setId(null);
            firstMileSkuCostAllocationEntity.setMainId(entity.getId());
            firstMileSkuCostAllocationEntity.setSourceDetailId(deliveryDetailEntity.getId());
            InitFirstMileAllocationDetailEntity initFirstMileAllocationDetailEntity = initFirstMileAllocationDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getSkuId().equals(deliveryDetailEntity.getSkuId()))
                    .findFirst().orElse(null);
            firstMileSkuCostAllocationEntity
                    .setSkuId(deliveryDetailEntity.getSkuId())
                    .setSkuNo(deliveryDetailEntity.getSkuNo())
                    .setPlatformSkuNo(deliveryDetailEntity.getPlatformSkuNo())
                    .setDeliveryQty(Objects.nonNull(deliveryDetailEntity.getDeliveryQty()) ? deliveryDetailEntity.getDeliveryQty() : MathUtil.ZERO);
            //分摊重量 若有期初值则取值期初，无期初值则
            //{分摊重量}取值【头程重量分摊】单据的分摊重量(KG)【按照业务单号+SKU累计统计】
            if (Objects.nonNull(initFirstMileAllocationDetailEntity)) {
                firstMileSkuCostAllocationEntity.setInitFirstMileDetailId(initFirstMileAllocationDetailEntity.getId());
                firstMileSkuCostAllocationEntity.setAllocatedWeight(initFirstMileAllocationDetailEntity.getWeightAllocation());
            } else {
                FirstMileWeightAllocationEntity firstMileWeightAllocationEntity = weightAllocationEntityList.stream().filter(e -> e.getSkuId().equals(deliveryDetailEntity.getSkuId())).findFirst().orElse(null);
                if (Objects.isNull(firstMileWeightAllocationEntity)) {
                    throw new ServiceException(StrUtil.format("发货单【{}】SKU【{}】期初和重量分摊记录不存在", entity.getSourceCode(), deliveryDetailEntity.getSkuNo()));
                }
                firstMileSkuCostAllocationEntity.setAllocatedWeight(firstMileWeightAllocationEntity.getAllocationWeight());
                firstMileSkuCostAllocationEntity.setWeightAllocationId(firstMileWeightAllocationEntity.getId());
            }
            List<String> skuCostDetailIds = new ArrayList<>();
            //单位成本 第一次取值期初值则取值期初
            //后续生成则取值分摊组织下的产品成本【已审核】
            if (Objects.isNull(oldAllocation)) {
                firstMileSkuCostAllocationEntity.setProductCost(Objects.nonNull(initFirstMileAllocationDetailEntity) ? initFirstMileAllocationDetailEntity.getProductCost() : BigDecimal.ZERO);
                firstMileSkuCostAllocationEntity.setInitReceiveQty(Objects.nonNull(initFirstMileAllocationDetailEntity) ? initFirstMileAllocationDetailEntity.getInitReceiveQty() : MathUtil.ZERO);
            } else {
                firstMileSkuCostAllocationEntity.setInitReceiveQty(MathUtil.ZERO);
                //先判断sku是否是组合品，是组合品则要汇总子sku产品成本
                List<BomChildrenSkuDTO> skuDTOList = bomChildrenSkuDTOS.stream().filter(e -> StrUtil.isNotEmpty(e.getParentSkuId())
                        && StrUtil.isNotEmpty(e.getParentSkuNo()) && e.getParentSkuId().equals(deliveryDetailEntity.getSkuId())).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(skuDTOList) && BomTypeEnum.COMBINATION.getType().equals(skuDTOList.get(0).getType())) {
                    //汇总sku成本
                    BigDecimal productCost = skuDTOList.stream().map(e -> {
                        InventorySkuCostDTO.PagingVO pagingVO = skuCostList.stream().filter(f -> f.getSkuId().equals(e.getSkuId())).findFirst().orElse(null);
                        if (Objects.isNull(pagingVO)) {
                            return BigDecimal.ZERO;
                        } else {
                            return MathUtil.multiply(MathUtil.multiply(new BigDecimal(pagingVO.getProductCost()),e.getQuantity()),pagingVO.getExchangeRate());
                        }
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
                    //汇总关联的sku成本记录
                    List<String> skuIds1 = skuDTOList.stream().map(BomChildrenSkuDTO::getSkuId).distinct().collect(Collectors.toList());
                    List<String> costDetailIds = skuCostList.stream().filter(e -> skuIds1.contains(e.getSkuId())).map(InventorySkuCostDTO.PagingVO::getDetailId).collect(Collectors.toList());
                    skuCostDetailIds.addAll(costDetailIds);
                    firstMileSkuCostAllocationEntity.setProductCost(productCost);
                } else {
                    InventorySkuCostDTO.PagingVO pagingVO = skuCostList.stream().filter(f -> f.getSkuId().equals(deliveryDetailEntity.getSkuId())).findFirst().orElse(null);
                    if (Objects.nonNull(pagingVO)) {
                        skuCostDetailIds.add(pagingVO.getDetailId());
                        firstMileSkuCostAllocationEntity.setProductCost(MathUtil.multiply(new BigDecimal(pagingVO.getProductCost()),pagingVO.getExchangeRate()));
                    } else {
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
            //费用来源
            if (Objects.nonNull(firstMileEstimatedBillEntity)) {
                firstMileSkuCostAllocationEntity.setBillSourceType(ReconciliationBillTypeEnum.ESTIMATED.getCode());
                firstMileSkuCostAllocationEntity.setEstimatedBillId(firstMileEstimatedBillEntity.getId());
            }
            //对账单明细
            if (Objects.nonNull(reconciliationDetailEntity)) {
                firstMileSkuCostAllocationEntity.setBillSourceType(ReconciliationBillTypeEnum.ACTUAL.getCode());
                firstMileSkuCostAllocationEntity.setReconciliationDetailId(reconciliationDetailEntity.getId());
            }
            //保存分摊sku记录
            firstMileSkuCostAllocationService.saveOrUpdate(firstMileSkuCostAllocationEntity);
            //保存sku成本使用记录
            if (!CollectionUtils.isEmpty(skuCostDetailIds)) {
                firstMileSkuCostRefService.saveOrUpdateRef(skuCostDetailIds, firstMileSkuCostAllocationEntity.getId());
            }
            firstMileSkuCostAllocationEntityList.add(firstMileSkuCostAllocationEntity);
        }
        //新增费用分摊主表  新增费用sku记录
        buildSkuAllocationDetail(entity, firstMileSkuCostAllocationEntityList, firstMileEstimatedBillEntity, reconciliationDetailEntity, allocationSettingDTO, weightAllocationEntityList, voList, initFirstMileAllocationDetailEntityList, receiveDTOS);
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), StrUtil.format("核算月份【{}】对账月份【{}】费用分摊成功", reportPeriodMonth.getMonth(), entity.getReconciliationMonth()));
    }

    /**
     * 校验费用分摊是否已存在
     * @param entity
     * @return
     */
    private BatchResultDTO checkCostAllocationExist(FirstMileCostAllocationEntity entity) {
        //已存在 则重置id 已核算则返回异常
        List<FirstMileCostAllocationEntity> list = this.lambdaQuery().eq(FirstMileCostAllocationEntity::getSourceId, entity.getSourceId())
                .eq(FirstMileCostAllocationEntity::getReportPeriodId, entity.getReportPeriodId())
                .eq(Objects.nonNull(entity.getReconciliationMonth()),FirstMileCostAllocationEntity::getReconciliationMonth, entity.getReconciliationMonth()).list();
        if (CollectionUtils.isEmpty(list)){
            return BatchResultDTO.success();
        }else {
            FirstMileCostAllocationEntity entity1 = list.stream().filter(e -> Objects.equals(ConfirmStatusEnum.WAIT_CONFIRM.getCode(), e.getStatus())).findFirst().orElse(null);
            if (Objects.nonNull(entity1)){
                entity.setId(entity1.getId());
                return BatchResultDTO.success();
            }else {
                return BatchResultDTO.fail(entity.getId(),entity.getSourceCode(),StrUtil.format("发货单【{}】核算期间【{}】对账期间【{}】已确认不能重新审核", entity.getSourceCode(),entity.getReportPeriodMonth(),entity.getReconciliationMonth()));
            }
        }
    }

    /**
     * 检查 上期账单是实际账单 并且所有费用分类的期末在途费用为0则不进行费用分摊
     *
     * @param voList              分摊记录
     * @param reportPeriodMonth   核算月份
     * @param reconciliationMonth 对账月份
     */
    private BatchResultDTO checkCostAllocation(List<FirstMileCostAllocationDTO.PagingVO> voList, LocalDate reportPeriodMonth, LocalDate reconciliationMonth) {
        if (CollectionUtils.isEmpty(voList) || Objects.isNull(reportPeriodMonth) || Objects.isNull(reconciliationMonth)) {
            return BatchResultDTO.success();
        }
        //获取发货单所有分摊记录(本核算之前的记录)
        List<FirstMileCostAllocationDTO.PagingVO> beforeList = voList.stream().filter(e -> Objects.nonNull(e) && e.getReconciliationMonth().isBefore(reportPeriodMonth)).collect(Collectors.toList());
        //存在对账单月份则获取对账前一个对账月份的分摊记录，不存在对账月份，则获取核算月份之前的记录(实际账单)
        FirstMileCostAllocationDTO.PagingVO beforeVO = beforeList.stream().filter(e -> Objects.equals(e.getReconciliationMonth(), reconciliationMonth)).max(Comparator.comparing(FirstMileCostAllocationDTO.PagingVO::getReportPeriodMonth)).orElse(null);
        if (Objects.nonNull(beforeVO) && ReconciliationBillTypeEnum.ACTUAL.getCode().equals(beforeVO.getBillSourceType())) {
            //查询对应核算月份期末在途数据是否为0
            List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntityList = firstMileSkuCostAllocationDetailService.listByMainIds(Collections.singletonList(beforeVO.getId()));
            BigDecimal endPeriodTransitCost = skuCostAllocationDetailEntityList.stream().map(FirstMileSkuCostAllocationDetailEntity::getEndPeriodTransitCost).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (BigDecimal.ZERO.equals(endPeriodTransitCost)) {
                return BatchResultDTO.fail(beforeVO.getId(), beforeVO.getSourceCode(), "上期为实际账单，且期末在途费用为0，不进行下期费用分摊");
            }
        }
        return BatchResultDTO.success();
    }

    /**
     * 根据sku进行费用分摊明细计算
     *
     * @param entity                                  头程分摊记录
     * @param firstMileSkuCostAllocationEntityList    头程sku分摊记录
     * @param firstMileEstimatedBillEntity            头程暂估账单
     * @param reconciliationDetailEntity              对账单
     * @param allocationSettingDTO                    系统配置
     * @param weightAllocationEntityList              重量分摊记录
     * @param voList                                  分摊记录
     * @param initFirstMileAllocationDetailEntityList 期初
     * @param receiveDTOS                             签收记录
     */
    private void buildSkuAllocationDetail(FirstMileCostAllocationEntity entity, List<FirstMileSkuCostAllocationEntity> firstMileSkuCostAllocationEntityList,
                                          FirstMileEstimatedBillDTO.View firstMileEstimatedBillEntity, TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity,
                                          CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO, List<FirstMileWeightAllocationEntity> weightAllocationEntityList,
                                          List<FirstMileCostAllocationDTO.PagingVO> voList, List<InitFirstMileAllocationDetailEntity> initFirstMileAllocationDetailEntityList,
                                          List<FirstMileDeliveryDTO.ReceiveDTO> receiveDTOS) {
        if (CollectionUtils.isEmpty(firstMileSkuCostAllocationEntityList)) {
            return;
        }
        //新增sku分摊记录
//        firstMileSkuCostAllocationService.saveBatch(firstMileSkuCostAllocationEntityList);
        //计算费用分摊明细
        List<FirstMileSkuCostAllocationDetailEntity> skuCostAllocationDetailEntityList = calcAllocatedSkuCostDetail(firstMileSkuCostAllocationEntityList, allocationSettingDTO, firstMileEstimatedBillEntity, reconciliationDetailEntity, weightAllocationEntityList);
        //同一个发货单-核算月份-对账月份内 计算期初冲期初
        LocalDate reconciliationMonth = entity.getReconciliationMonth();
        LocalDate reportPeriodMonth = entity.getReportPeriodMonth();
        //获取发货单所有分摊记录(本核算之前的记录)
        List<FirstMileCostAllocationDTO.PagingVO> beforeList = voList.stream().filter(e -> Objects.nonNull(e)
                && Objects.nonNull(e.getReconciliationMonth())&& Objects.nonNull(reportPeriodMonth.getMonth()) && e.getReconciliationMonth().isBefore(reportPeriodMonth)).collect(Collectors.toList());
        //存在对账单月份则获取对账前一个对账月份的分摊记录，不存在对账月份，则获取核算月份之前的记录(实际账单)
        FirstMileCostAllocationDTO.PagingVO beforeVO = null;
        if (Objects.isNull(reconciliationMonth) && !CollectionUtils.isEmpty(beforeList)) {
            beforeVO = Collections.max(beforeList, Comparator.comparing(FirstMileCostAllocationDTO.PagingVO::getReportPeriodMonth));
        } else if (Objects.nonNull(reconciliationMonth) && !CollectionUtils.isEmpty(beforeList)) {
            beforeVO = beforeList.stream().filter(e -> Objects.equals(e.getReconciliationMonth(), reconciliationMonth)).max(Comparator.comparing(FirstMileCostAllocationDTO.PagingVO::getReportPeriodMonth)).orElse(null);
        }
        List<FirstMileSkuCostAllocationDetailEntity> beforeSkuDetailList = null;
        //判断是否存在账单
        FirstMileCostAllocationDTO.JudgeReconciliationDTO judgeReconciliationDTO = judgeMonthReconciliationHasReconciliation(reportPeriodMonth, reconciliationMonth, voList, firstMileSkuCostAllocationEntityList);
        //如果上个费用分摊记录存在
        if (Objects.nonNull(beforeVO)) {
            beforeSkuDetailList = firstMileSkuCostAllocationDetailService.listByMainIds(Collections.singletonList(beforeVO.getId()));
        }
        //期初在途费用
        for (FirstMileSkuCostAllocationDetailEntity detailEntity : skuCostAllocationDetailEntityList) {
            //期初头程分摊记录
            InitFirstMileAllocationDetailEntity initEntity = null;
            if (!CollectionUtils.isEmpty(initFirstMileAllocationDetailEntityList)){
                initEntity = initFirstMileAllocationDetailEntityList.stream().filter(e -> e.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            }
            //sku分摊记录
            FirstMileSkuCostAllocationEntity skuCostAllocationEntity = firstMileSkuCostAllocationEntityList.stream()
                    .filter(e -> e.getId().equals(detailEntity.getCostMainId())).findFirst().orElse(null);
            //sku签收统计
            FirstMileDeliveryDTO.ReceiveDTO receiveDTO = receiveDTOS.stream().filter(e -> Objects.nonNull(e) && Objects.nonNull(skuCostAllocationEntity)
                    && e.getSkuId().equals(skuCostAllocationEntity.getSkuId())).findFirst().orElse(null);
            //本月签收数量
            Integer currentMonthReceiveQty = Objects.nonNull(receiveDTO) ? receiveDTO.getCurrentMonthReceiveQty() : MathUtil.ZERO;
            //截止本月签收数量
            Integer asCurrentMonthReceiveQty = Objects.nonNull(receiveDTO) ? receiveDTO.getAsCurrentMonthReceiveQty() : MathUtil.ZERO;
            //截止上月签收数量
            Integer asLastMonthReceiveQty = Objects.nonNull(receiveDTO) ? receiveDTO.getAsLastMonthReceiveQty() : MathUtil.ZERO;
            //期初签收数量
            Integer initReceiveQty = Objects.nonNull(skuCostAllocationEntity) ? skuCostAllocationEntity.getInitReceiveQty() : MathUtil.ZERO;
            //累计签收数量
            Integer receiveQty = asCurrentMonthReceiveQty + initReceiveQty;
            //单产品分摊
            BigDecimal productAllocatedAmount = Objects.nonNull(detailEntity.getProductAllocatedAmount()) ? detailEntity.getProductAllocatedAmount() : BigDecimal.ZERO;
            //sku发货量
            Integer deliveryQty = Objects.nonNull(skuCostAllocationEntity) ? skuCostAllocationEntity.getDeliveryQty() : MathUtil.ZERO;
            //头程分摊金额
            BigDecimal allocatedAmount = Objects.nonNull(detailEntity.getAllocatedAmount()) ? detailEntity.getAllocatedAmount() : BigDecimal.ZERO;
            if (Objects.nonNull(beforeVO) && !CollectionUtils.isEmpty(beforeSkuDetailList)) {
                //上期记录
                FirstMileSkuCostAllocationDetailEntity beforeDetailEntity = beforeSkuDetailList.stream().filter(e -> Objects.equals(e.getSkuId(), detailEntity.getSkuId())
                        && Objects.equals(detailEntity.getFeeType(), e.getFeeType())).findFirst().orElse(null);
                if (Objects.nonNull(beforeDetailEntity)) {
                    //期初在途
                    detailEntity.setInitTransitCost(beforeDetailEntity.getEndPeriodTransitCost());
                    detailEntity.setInitEstimatedCost(beforeDetailEntity.getEndPeriodEstimatedCost());
                } else {
                    //期初在途
                    detailEntity.setInitTransitCost(BigDecimal.ZERO);
                    detailEntity.setInitEstimatedCost(BigDecimal.ZERO);
                }
            } else {
                if (Objects.nonNull(initEntity) && detailEntity.getFeeType().equals(AllocationFeeTypeEnum.SHIPPING_COST.getCode())) {
                    //期初在途
                    detailEntity.setInitTransitCost(Objects.nonNull(initEntity.getInitTransitCost()) ? initEntity.getInitTransitCost() : BigDecimal.ZERO);
                    detailEntity.setInitEstimatedCost(Objects.nonNull(initEntity.getInitEstimatedCost()) ? initEntity.getInitEstimatedCost() : BigDecimal.ZERO);
                } else if (Objects.nonNull(initEntity) && detailEntity.getFeeType().equals(AllocationFeeTypeEnum.DECLARE_COST.getCode())) {
                    detailEntity.setInitTransitCost(Objects.nonNull(initEntity.getInitTransitTariff()) ? initEntity.getInitTransitTariff() : BigDecimal.ZERO);
                    detailEntity.setInitEstimatedCost(Objects.nonNull(initEntity.getInitEstimatedTariff()) ? initEntity.getInitEstimatedTariff() : BigDecimal.ZERO);
                } else {
                    //期初在途
                    detailEntity.setInitTransitCost(BigDecimal.ZERO);
                    detailEntity.setInitEstimatedCost(BigDecimal.ZERO);
                }
            }
            //冲期初在途费用 上月开始有账单
            if (judgeReconciliationDTO.isLastMonthReconciliation()) {
                //若累计签收数量<发货数量：本月签收数量*单产品分摊
                if (receiveQty <= deliveryQty) {
                    detailEntity.setMidPeriodTransitCost(MathUtil.multiply(productAllocatedAmount, currentMonthReceiveQty));
                } else {
                    //若累计签收数量>发货数量：(发货数量-截止上月累计签收数量)*单产品分摊
                    //[累计签收数量>发货数量小于0不计算]
                    int qty = deliveryQty - asLastMonthReceiveQty;
                    if (qty <= 0) {
                        detailEntity.setMidPeriodTransitCost(BigDecimal.ZERO);
                    } else {
                        detailEntity.setMidPeriodTransitCost(MathUtil.multiply(productAllocatedAmount, qty));
                    }
                }
            } else {
                detailEntity.setMidPeriodTransitCost(BigDecimal.ZERO);
            }
            //本期分摊费用 本月开始有账单
            if (judgeReconciliationDTO.isCurrencyMonthReconciliation()) {
                if (receiveQty <= deliveryQty) {
                    //实际账单
                    if (ReconciliationBillTypeEnum.ACTUAL.getCode().equals(skuCostAllocationEntity.getBillSourceType())) {
                        //若累计签收数量<发货数量：本月签收数量*单产品分摊
                        detailEntity.setCurrentPeriodAllocatedCost(MathUtil.multiply(productAllocatedAmount, currentMonthReceiveQty));
                    } else {
                        //若累计签收数量<发货数量：累计签收数量*单产品分摊
                        detailEntity.setCurrentPeriodAllocatedCost(MathUtil.multiply(productAllocatedAmount, receiveQty));
                    }
                } else {
                    //实际账单
                    if (ReconciliationBillTypeEnum.ACTUAL.getCode().equals(skuCostAllocationEntity.getBillSourceType())) {
                        //若累计签收数量>发货数量：(发货数量-截止上月累计签收数量)*单产品分摊
                        //[累计签收数量>发货数量小于0不计算]
                        int qty = deliveryQty - asLastMonthReceiveQty;
                        if (qty <= 0) {
                            detailEntity.setCurrentPeriodAllocatedCost(BigDecimal.ZERO);
                        } else {
                            detailEntity.setCurrentPeriodAllocatedCost(MathUtil.multiply(productAllocatedAmount, qty));
                        }
                    } else {
                        //若累计签收数量>发货数量：发货数量*单产品分摊
                        detailEntity.setCurrentPeriodAllocatedCost(MathUtil.multiply(productAllocatedAmount, deliveryQty));
                    }
                }
            } else {
                detailEntity.setCurrentPeriodAllocatedCost(BigDecimal.ZERO);
            }
            //期末在途费用 计算
            //冲期初在途费用
            BigDecimal midPeriodTransitCost = Objects.nonNull(detailEntity.getMidPeriodTransitCost()) ? detailEntity.getMidPeriodTransitCost() : BigDecimal.ZERO;
            //本期分摊费用
            BigDecimal currentPeriodAllocatedCost = Objects.nonNull(detailEntity.getCurrentPeriodAllocatedCost()) ? detailEntity.getCurrentPeriodAllocatedCost() : BigDecimal.ZERO;
            BigDecimal mid = MathUtil.add(midPeriodTransitCost, currentPeriodAllocatedCost);
            if (BigDecimal.ZERO.equals(detailEntity.getInitTransitCost())) {
                //期初=0时，期初在途费用(0)+头程分摊金额-冲期初-本期分摊费用
                detailEntity.setEndPeriodTransitCost(MathUtil.subtract(allocatedAmount, mid));
            } else {
                //期初不等于0时，期初在途费用-冲期初-本期分摊费用
                detailEntity.setEndPeriodTransitCost(MathUtil.subtract(detailEntity.getInitTransitCost(), mid));
            }
            //期末暂估费用 计算
            /**
             * 1.预估账单有签收：本月累计签收数量*单产品分摊
             * 2.无账单无签收：取值为0
             * 3.有实际账单：取值为0
             */
            if (ReconciliationBillTypeEnum.ESTIMATED.getCode().equals(skuCostAllocationEntity.getBillSourceType())) {
                detailEntity.setEndPeriodEstimatedCost(MathUtil.multiply(productAllocatedAmount, asCurrentMonthReceiveQty));
            } else {
                detailEntity.setEndPeriodEstimatedCost(BigDecimal.ZERO);
            }
        }
        //保存sku分摊明细记录
        firstMileSkuCostAllocationDetailService.saveBatch(skuCostAllocationDetailEntityList);
    }

    /**
     * 判断是否存在对账单
     *
     * @param reportPeriodMonth
     * @param reconciliationMonth
     * @param voList
     * @param firstMileSkuCostAllocationEntityList 本期
     */
    private FirstMileCostAllocationDTO.JudgeReconciliationDTO judgeMonthReconciliationHasReconciliation(LocalDate reportPeriodMonth, LocalDate reconciliationMonth,
                                                                                                        List<FirstMileCostAllocationDTO.PagingVO> voList,
                                                                                                        List<FirstMileSkuCostAllocationEntity> firstMileSkuCostAllocationEntityList) {
        FirstMileCostAllocationDTO.JudgeReconciliationDTO judgeReconciliationDTO = new FirstMileCostAllocationDTO.JudgeReconciliationDTO();
        if (Objects.isNull(reconciliationMonth)) {
            return judgeReconciliationDTO;
        }
        //本月有实际账单
        boolean currencyReconciliation = false;
        //上月有实际账单
        boolean lastReconciliation = false;
        //上上月有实际账单
        boolean beforeLastReconciliation = false;
        FirstMileSkuCostAllocationEntity entity = firstMileSkuCostAllocationEntityList.stream().filter(e -> Objects.nonNull(e)
                && Objects.equals(ReconciliationBillTypeEnum.ACTUAL.getCode(), e.getBillSourceType())).findFirst().orElse(null);
        if (Objects.nonNull(entity)) {
            currencyReconciliation = Boolean.TRUE;
        }
        //本月为实际账单 上月为预估账单 则本月开始有账单
        //上月时间
        LocalDate lastMonth = reportPeriodMonth.with(TemporalAdjusters.lastDayOfMonth()).withDayOfMonth(1);
        FirstMileCostAllocationDTO.PagingVO pagingVO = voList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(lastMonth, e.getReportPeriodMonth())
                && Objects.equals(ReconciliationBillTypeEnum.ACTUAL.getCode(), e.getBillSourceType())).findFirst().orElse(null);
        if (Objects.nonNull(pagingVO)) {
            lastReconciliation = true;
        }
        //本月为实际账单 上月为实际账单 上上个月为预估账单 则上月开始有账单
        //上上月时间
        LocalDate beforeLastMonth = reportPeriodMonth.minusMonths(2).withDayOfMonth(1);
        FirstMileCostAllocationDTO.PagingVO pagingVO1 = voList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(beforeLastMonth, e.getReportPeriodMonth())
                && Objects.equals(ReconciliationBillTypeEnum.ACTUAL.getCode(), e.getBillSourceType())).findFirst().orElse(null);
        if (Objects.nonNull(pagingVO1)) {
            beforeLastReconciliation = true;
        }
        if (!lastReconciliation && currencyReconciliation) {
            judgeReconciliationDTO.setCurrencyMonthReconciliation(Boolean.TRUE);
        }
        if (!beforeLastReconciliation && lastReconciliation && currencyReconciliation) {
            judgeReconciliationDTO.setLastMonthReconciliation(Boolean.TRUE);
        }
        return judgeReconciliationDTO;
    }

    @Override
    public List<FirstMileCostAllocationEntity> listBySourceIds(List<String> sourceIds, String reportPeriodId) {
        if (CollectionUtils.isEmpty(sourceIds) && StrUtil.isBlank(reportPeriodId)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(!CollectionUtils.isEmpty(sourceIds), FirstMileCostAllocationEntity::getSourceId, sourceIds)
                .eq(StrUtil.isNotBlank(reportPeriodId), FirstMileCostAllocationEntity::getReportPeriodId, reportPeriodId).list();
    }

    @Override
    public void autoGenerateFirstMileCostAllocation(LocalDate reportPeriodMonth) {
        if (null == reportPeriodMonth) {
            throw new ServiceException("核算期间时间为空");
        }
        List<String> statusList = new ArrayList<>(2);
        statusList.add(CostAllocationStatusEnum.NOT.getCode());
        statusList.add(CostAllocationStatusEnum.PART.getCode());
        // 头程重量分摊-费用状态为{未分摊，部分分摊}+本期账单数据 判断是否进入头程费用分摊表
        List<FirstMileWeightAllocationEntity> list = firstMileWeightAllocationService.listBySourceIds(null, statusList);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> deliveryIds = list.stream().map(FirstMileWeightAllocationEntity::getSourceId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deliveryIds)) {
            return;
        }
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(deliveryIds);
        if (CollectionUtils.isEmpty(firstMileDeliveryEntityList)){
            return;
        }
        List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList = wmsFirstMileDeliveryFeign.listDetailByMainIds(deliveryIds);
        if (CollectionUtils.isEmpty(deliveryDetailEntityList)){
            return;
        }
        //按照发货单进行费用分摊
        for (String id : deliveryIds) {
            FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryEntityList.stream().filter(e -> Objects.nonNull(e) && e.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(deliveryEntity)) {
                continue;
            }
            List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList1 = deliveryDetailEntityList.stream().filter(e -> Objects.nonNull(e) && e.getMainId().equals(id)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(deliveryDetailEntityList1)) {
                continue;
            }
            //构造数据
            FirstMileCostAllocationEntity entity = new FirstMileCostAllocationEntity()
                    .setSourceId(deliveryEntity.getId()).setSourceCode(deliveryEntity.getCode()).setReportPeriodMonth(reportPeriodMonth);
            service.calcAllocatedCost(entity, deliveryEntity, deliveryDetailEntityList1);
        }
    }

    /**
     * 计算sku费用分类中各个分摊金额
     *
     * @param firstMileSkuCostAllocationEntityList
     * @param allocationSettingDTO
     * @param firstMileEstimatedBillEntity
     * @param reconciliationDetailEntity
     * @param weightAllocationEntityList
     * @return
     */
    private List<FirstMileSkuCostAllocationDetailEntity> calcAllocatedSkuCostDetail(List<FirstMileSkuCostAllocationEntity> firstMileSkuCostAllocationEntityList,
                                                                                    CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO,
                                                                                    FirstMileEstimatedBillDTO.View firstMileEstimatedBillEntity,
                                                                                    TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity,
                                                                                    List<FirstMileWeightAllocationEntity> weightAllocationEntityList) {
        List<FirstMileSkuCostAllocationDetailEntity> firstMileSkuCostAllocationDetailEntityList = new ArrayList<>();
        //sku成本总金额
        BigDecimal skuTotalCost = firstMileSkuCostAllocationEntityList.stream().map(e -> e.getProductCost().multiply(new BigDecimal(e.getDeliveryQty()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        //sku总重量
        BigDecimal weightTotal = weightAllocationEntityList.stream().map(FirstMileWeightAllocationEntity::getAllocationWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
        //头程总金额
        for (FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity : firstMileSkuCostAllocationEntityList) {
            FirstMileWeightAllocationEntity weightAllocationEntity = weightAllocationEntityList.stream().filter(e -> firstMileSkuCostAllocationEntity.getSkuId().equals(e.getSkuId())).findFirst().orElse(null);
            //分摊重量
            BigDecimal allocationWeight = BigDecimal.ZERO;
            if (Objects.nonNull(weightAllocationEntity)) {
                allocationWeight = weightAllocationEntity.getAllocationWeight();
            }
            //分摊成本 单位成本* 数量
            BigDecimal productCost = Objects.nonNull(firstMileSkuCostAllocationEntity.getProductCost()) ? firstMileSkuCostAllocationEntity.getProductCost() : BigDecimal.ZERO;
            Integer deliveryQty = Objects.nonNull(firstMileSkuCostAllocationEntity.getDeliveryQty()) ? firstMileSkuCostAllocationEntity.getDeliveryQty() : MathUtil.ZERO;
            BigDecimal productTotalCost = MathUtil.multiply(productCost, deliveryQty);
            //构建费用分摊明细
            firstMileSkuCostAllocationDetailEntityList.add(buildSkuCostDetailByShippingCost(allocationSettingDTO, firstMileSkuCostAllocationEntity, firstMileEstimatedBillEntity, reconciliationDetailEntity, skuTotalCost, weightTotal, allocationWeight, productTotalCost));
            firstMileSkuCostAllocationDetailEntityList.add(buildSkuCostDetailByDeclareCost(allocationSettingDTO, firstMileSkuCostAllocationEntity, firstMileEstimatedBillEntity, reconciliationDetailEntity, skuTotalCost, weightTotal, allocationWeight, productTotalCost));
            firstMileSkuCostAllocationDetailEntityList.add(buildSkuCostDetailByOtherTaxFee(allocationSettingDTO, firstMileSkuCostAllocationEntity, firstMileEstimatedBillEntity, reconciliationDetailEntity, skuTotalCost, weightTotal, allocationWeight, productTotalCost));
            firstMileSkuCostAllocationDetailEntityList.add(buildSkuCostDetailByOtherCost(allocationSettingDTO, firstMileSkuCostAllocationEntity, firstMileEstimatedBillEntity, reconciliationDetailEntity, skuTotalCost, weightTotal, allocationWeight, productTotalCost));
        }
        //按照比例分摊后，除不尽的分摊金额放数量最大一个
        resetAllocationAmount(firstMileSkuCostAllocationDetailEntityList, firstMileSkuCostAllocationEntityList);
        return firstMileSkuCostAllocationDetailEntityList;
    }

    /**
     * 根据费用类型进行分类 然后 按照比例分摊后，除不尽的分摊金额放数量最大一个
     *
     * @param firstMileSkuCostAllocationDetailEntityList sku分摊明细
     * @param firstMileSkuCostAllocationEntityList       sku分摊列表
     */
    private void resetAllocationAmount(List<FirstMileSkuCostAllocationDetailEntity> firstMileSkuCostAllocationDetailEntityList, List<FirstMileSkuCostAllocationEntity> firstMileSkuCostAllocationEntityList) {
        //同一个发货单内不同sku使用同一个费用分类的头程金额
        FirstMileSkuCostAllocationDetailEntity maxShippingCost = firstMileSkuCostAllocationDetailEntityList.stream().filter(e -> AllocationFeeTypeEnum.SHIPPING_COST.getCode().equals(e.getAllocationType())).max(Comparator.comparing(FirstMileSkuCostAllocationDetailEntity::getAllocatedAmount)).orElse(null);
        FirstMileSkuCostAllocationDetailEntity maxDeclareCost = firstMileSkuCostAllocationDetailEntityList.stream().filter(e -> AllocationFeeTypeEnum.DECLARE_COST.getCode().equals(e.getAllocationType())).max(Comparator.comparing(FirstMileSkuCostAllocationDetailEntity::getAllocatedAmount)).orElse(null);
        FirstMileSkuCostAllocationDetailEntity maxOtherTaxFee = firstMileSkuCostAllocationDetailEntityList.stream().filter(e -> AllocationFeeTypeEnum.OTHER_TAX_FEE.getCode().equals(e.getAllocationType())).max(Comparator.comparing(FirstMileSkuCostAllocationDetailEntity::getAllocatedAmount)).orElse(null);
        FirstMileSkuCostAllocationDetailEntity maxOtherFee = firstMileSkuCostAllocationDetailEntityList.stream().filter(e -> AllocationFeeTypeEnum.OTHER_COST.getCode().equals(e.getAllocationType())).max(Comparator.comparing(FirstMileSkuCostAllocationDetailEntity::getAllocatedAmount)).orElse(null);
        //累加各个类型头程分摊金额
        BigDecimal shippingCost = firstMileSkuCostAllocationDetailEntityList.stream().filter(e -> AllocationFeeTypeEnum.SHIPPING_COST.getCode().equals(e.getAllocationType())).map(FirstMileSkuCostAllocationDetailEntity::getAllocatedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal declareCost = firstMileSkuCostAllocationDetailEntityList.stream().filter(e -> AllocationFeeTypeEnum.DECLARE_COST.getCode().equals(e.getAllocationType())).map(FirstMileSkuCostAllocationDetailEntity::getAllocatedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal otherTaxFee = firstMileSkuCostAllocationDetailEntityList.stream().filter(e -> AllocationFeeTypeEnum.OTHER_TAX_FEE.getCode().equals(e.getAllocationType())).map(FirstMileSkuCostAllocationDetailEntity::getAllocatedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal otherFee = firstMileSkuCostAllocationDetailEntityList.stream().filter(e -> AllocationFeeTypeEnum.OTHER_COST.getCode().equals(e.getAllocationType())).map(FirstMileSkuCostAllocationDetailEntity::getAllocatedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //根据id汇总发货数量
        Map<String, Integer> qtyMap = firstMileSkuCostAllocationEntityList.stream().collect(Collectors.toMap(FirstMileSkuCostAllocationEntity::getId, FirstMileSkuCostAllocationEntity::getDeliveryQty));
        //比较后重置
        firstMileSkuCostAllocationDetailEntityList.forEach(e -> {
            //运费
            if (Objects.nonNull(maxShippingCost) && !maxShippingCost.getAmount().equals(shippingCost) && maxShippingCost.getAllocationType().equals(e.getAllocationType())
                    && e.getAllocatedAmount().equals(maxShippingCost.getAllocatedAmount()) && e.getCostMainId().equals(maxShippingCost.getCostMainId())) {
                //（总金额-累加金额） + 最高分摊金额 = 重置后的分摊金额（若累加高于总金额，则未负数，低于则填补分摊金额）
                e.setAllocatedAmount(MathUtil.add(MathUtil.subtract(maxShippingCost.getAmount(), shippingCost), maxShippingCost.getAllocatedAmount()));
            }
            //报关
            if (Objects.nonNull(maxDeclareCost) && !maxDeclareCost.getAmount().equals(declareCost) && maxDeclareCost.getAllocationType().equals(e.getAllocationType())
                    && e.getAllocatedAmount().equals(maxDeclareCost.getAllocatedAmount()) && e.getCostMainId().equals(maxDeclareCost.getCostMainId())) {
                //（总金额-累加金额） + 最高分摊金额 = 重置后的分摊金额（若累加高于总金额，则未负数，低于则填补分摊金额）
                e.setAllocatedAmount(MathUtil.add(MathUtil.subtract(maxDeclareCost.getAmount(), declareCost), maxDeclareCost.getAllocatedAmount()));
            }
            //其他税费
            if (Objects.nonNull(maxOtherTaxFee) && !maxOtherTaxFee.getAmount().equals(otherTaxFee) && maxOtherTaxFee.getAllocationType().equals(e.getAllocationType())
                    && e.getAllocatedAmount().equals(maxOtherTaxFee.getAllocatedAmount()) && e.getCostMainId().equals(maxOtherTaxFee.getCostMainId())) {
                //（总金额-累加金额） + 最高分摊金额 = 重置后的分摊金额（若累加高于总金额，则未负数，低于则填补分摊金额）
                e.setAllocatedAmount(MathUtil.add(MathUtil.subtract(maxOtherTaxFee.getAmount(), otherTaxFee), maxOtherTaxFee.getAllocatedAmount()));
            }
            //其他费用
            if (Objects.nonNull(maxOtherFee) && !maxOtherFee.getAmount().equals(otherFee) && maxOtherFee.getAllocationType().equals(e.getAllocationType())
                    && e.getAllocatedAmount().equals(maxOtherFee.getAllocatedAmount()) && e.getCostMainId().equals(maxOtherFee.getCostMainId())) {
                //（总金额-累加金额） + 最高分摊金额 = 重置后的分摊金额（若累加高于总金额，则未负数，低于则填补分摊金额）
                e.setAllocatedAmount(MathUtil.add(MathUtil.subtract(maxOtherFee.getAmount(), otherFee), maxOtherFee.getAllocatedAmount()));
            }
            //单个产品费用分摊
            Integer qty = qtyMap.getOrDefault(e.getCostMainId(), MathUtil.ZERO);
            BigDecimal allocatedAmount = Objects.nonNull(e.getAllocatedAmount()) ? e.getAllocatedAmount() : BigDecimal.ZERO;
            e.setProductAllocatedAmount(MathUtil.divide(allocatedAmount, new BigDecimal(qty), 6));
        });
    }

    /**
     * 其他费用
     *
     * @param allocationSettingDTO
     * @param firstMileSkuCostAllocationEntity
     * @param firstMileEstimatedBillEntity
     * @param reconciliationDetailEntity
     * @param skuTotalCost
     * @param weightTotal
     * @param allocationWeight
     * @param productTotalCost
     * @return
     */
    private FirstMileSkuCostAllocationDetailEntity buildSkuCostDetailByOtherCost(CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO, FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity, FirstMileEstimatedBillDTO.View firstMileEstimatedBillEntity, TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity, BigDecimal skuTotalCost, BigDecimal weightTotal, BigDecimal allocationWeight, BigDecimal productTotalCost) {
        FirstMileSkuCostAllocationDetailEntity entity = new FirstMileSkuCostAllocationDetailEntity()
                .setMainId(firstMileSkuCostAllocationEntity.getMainId())
                .setCostMainId(firstMileSkuCostAllocationEntity.getId())
                .setFeeType(AllocationFeeTypeEnum.OTHER_COST.getCode())
                .setAllocationType(allocationSettingDTO.getFirstOtherFee());
        if (Objects.nonNull(reconciliationDetailEntity)) {
            entity.setAmount(reconciliationDetailEntity.getOtherCost());
        } else if (Objects.nonNull(firstMileEstimatedBillEntity)) {
            //暂估
            entity.setAmount(firstMileEstimatedBillEntity.getOtherCost());
        } else {
            entity.setAmount(BigDecimal.ZERO);
        }
        //头程分摊金额
        if (CostAllocationEnum.WEIGHT_ALLOCATION.getCode().equals(allocationSettingDTO.getFirstShippingCost())) {
            entity.setAllocatedAmount(MathUtil.multiply(MathUtil.divide(allocationWeight, weightTotal), entity.getAmount(), 4));
        } else if (CostAllocationEnum.COST_ALLOCATION.getCode().equals(allocationSettingDTO.getFirstShippingCost())) {
            entity.setAllocatedAmount(MathUtil.multiply(MathUtil.divide(productTotalCost, skuTotalCost), entity.getAmount(), 4));
        }
        return entity;
    }

    /**
     * 其他税费
     *
     * @param allocationSettingDTO
     * @param firstMileSkuCostAllocationEntity
     * @param firstMileEstimatedBillEntity
     * @param reconciliationDetailEntity
     * @param skuTotalCost
     * @param weightTotal
     * @param allocationWeight
     * @param productTotalCost
     * @return
     */
    private FirstMileSkuCostAllocationDetailEntity buildSkuCostDetailByOtherTaxFee(CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO, FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity, FirstMileEstimatedBillDTO.View firstMileEstimatedBillEntity, TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity, BigDecimal skuTotalCost, BigDecimal weightTotal, BigDecimal allocationWeight, BigDecimal productTotalCost) {
        FirstMileSkuCostAllocationDetailEntity entity = new FirstMileSkuCostAllocationDetailEntity()
                .setMainId(firstMileSkuCostAllocationEntity.getMainId())
                .setCostMainId(firstMileSkuCostAllocationEntity.getId())
                .setFeeType(AllocationFeeTypeEnum.OTHER_TAX_FEE.getCode())
                .setAllocationType(allocationSettingDTO.getFirstOtherTaxFee());
        if (Objects.nonNull(reconciliationDetailEntity)) {
            entity.setAmount(reconciliationDetailEntity.getOtherTaxCost());
        } else if (Objects.nonNull(firstMileEstimatedBillEntity)) {
            //暂估
            entity.setAmount(firstMileEstimatedBillEntity.getOtherTaxCost());
        } else {
            entity.setAmount(BigDecimal.ZERO);
        }
        //头程分摊金额
        if (CostAllocationEnum.WEIGHT_ALLOCATION.getCode().equals(allocationSettingDTO.getFirstShippingCost())) {
            entity.setAllocatedAmount(MathUtil.multiply(MathUtil.divide(allocationWeight, weightTotal), entity.getAmount(), 4));
        } else if (CostAllocationEnum.COST_ALLOCATION.getCode().equals(allocationSettingDTO.getFirstShippingCost())) {
            entity.setAllocatedAmount(MathUtil.multiply(MathUtil.divide(productTotalCost, skuTotalCost), entity.getAmount(), 4));
        }
        return entity;
    }

    /**
     * 关税
     *
     * @param allocationSettingDTO
     * @param firstMileSkuCostAllocationEntity
     * @param firstMileEstimatedBillEntity
     * @param reconciliationDetailEntity
     * @param skuTotalCost
     * @param weightTotal
     * @param allocationWeight
     * @param productTotalCost
     * @return
     */
    private FirstMileSkuCostAllocationDetailEntity buildSkuCostDetailByDeclareCost(CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO, FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity, FirstMileEstimatedBillDTO.View firstMileEstimatedBillEntity, TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity, BigDecimal skuTotalCost, BigDecimal weightTotal, BigDecimal allocationWeight, BigDecimal productTotalCost) {
        FirstMileSkuCostAllocationDetailEntity entity = new FirstMileSkuCostAllocationDetailEntity()
                .setMainId(firstMileSkuCostAllocationEntity.getMainId())
                .setCostMainId(firstMileSkuCostAllocationEntity.getId())
                .setFeeType(AllocationFeeTypeEnum.DECLARE_COST.getCode())
                .setAllocationType(allocationSettingDTO.getFirstTariffFee());
        if (Objects.nonNull(reconciliationDetailEntity)) {
            entity.setAmount(reconciliationDetailEntity.getDeclareCost());
        } else if (Objects.nonNull(firstMileEstimatedBillEntity)) {
            //暂估
            entity.setAmount(firstMileEstimatedBillEntity.getCustomsClearanceCost());
        } else {
            entity.setAmount(BigDecimal.ZERO);
        }
        //头程分摊金额
        if (CostAllocationEnum.WEIGHT_ALLOCATION.getCode().equals(allocationSettingDTO.getFirstShippingCost())) {
            entity.setAllocatedAmount(MathUtil.multiply(MathUtil.divide(allocationWeight, weightTotal), entity.getAmount(), 4));
        } else if (CostAllocationEnum.COST_ALLOCATION.getCode().equals(allocationSettingDTO.getFirstShippingCost())) {
            entity.setAllocatedAmount(MathUtil.multiply(MathUtil.divide(productTotalCost, skuTotalCost), entity.getAmount(), 4));
        }
        return entity;
    }

    /**
     * 构建运费分摊金额
     *
     * @param allocationSettingDTO
     * @param firstMileSkuCostAllocationEntity
     * @param firstMileEstimatedBillEntity
     * @param reconciliationDetailEntity
     * @param skuTotalCost                     总成本
     * @param weightTotal                      总重量
     * @param allocationWeight
     * @param productTotalCost
     * @return
     */
    private FirstMileSkuCostAllocationDetailEntity buildSkuCostDetailByShippingCost(CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO,
                                                                                    FirstMileSkuCostAllocationEntity firstMileSkuCostAllocationEntity,
                                                                                    FirstMileEstimatedBillDTO.View firstMileEstimatedBillEntity,
                                                                                    TmsFirstMileReconciliationDetailEntity reconciliationDetailEntity,
                                                                                    BigDecimal skuTotalCost, BigDecimal weightTotal,
                                                                                    BigDecimal allocationWeight, BigDecimal productTotalCost) {
        FirstMileSkuCostAllocationDetailEntity entity = new FirstMileSkuCostAllocationDetailEntity()
                .setMainId(firstMileSkuCostAllocationEntity.getMainId())
                .setCostMainId(firstMileSkuCostAllocationEntity.getId())
                .setFeeType(AllocationFeeTypeEnum.SHIPPING_COST.getCode())
                .setAllocationType(allocationSettingDTO.getFirstShippingCost());
        if (Objects.nonNull(reconciliationDetailEntity)) {
            BigDecimal shippingCost = Objects.nonNull(reconciliationDetailEntity.getShippingCost()) ? reconciliationDetailEntity.getShippingCost() : BigDecimal.ZERO;
            entity.setAmount(shippingCost);
        } else if (Objects.nonNull(firstMileEstimatedBillEntity)) {
            //暂估
            entity.setAmount(firstMileEstimatedBillEntity.getLogisticsCost());
        } else {
            entity.setAmount(BigDecimal.ZERO);
        }
        //头程分摊金额
        if (CostAllocationEnum.WEIGHT_ALLOCATION.getCode().equals(allocationSettingDTO.getFirstShippingCost())) {
            entity.setAllocatedAmount(MathUtil.multiply(MathUtil.divide(allocationWeight, weightTotal), entity.getAmount(), 4));
        } else if (CostAllocationEnum.COST_ALLOCATION.getCode().equals(allocationSettingDTO.getFirstShippingCost())) {
            entity.setAllocatedAmount(MathUtil.multiply(MathUtil.divide(productTotalCost, skuTotalCost), entity.getAmount(), 4));
        }
        return entity;
    }

    private void fillPagingDb(List<FirstMileCostAllocationDTO.PagingVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //添加分摊明细
        list.forEach(e -> {
            e.setStatusName(ConfirmStatusEnum.getName(e.getStatus()));
            e.setBillSourceTypeName(ReconciliationBillTypeEnum.getNameByCode(e.getBillSourceType()));
            e.setFeeTypeName(AllocationFeeTypeEnum.getName(e.getFeeType()));
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

    @Override
    public List<FirstMileCostAllocationDTO.LastedAllocMonthDTO> listLastedAllocationMonth(List<String> logisticsBillIds) {
        return baseMapper.listLastedAllocationMonth(logisticsBillIds);
    }
}
