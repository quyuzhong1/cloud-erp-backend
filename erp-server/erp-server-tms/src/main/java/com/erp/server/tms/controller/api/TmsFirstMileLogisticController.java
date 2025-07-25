package com.erp.server.tms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.TmsFirstMileReconciliationEntity;
import com.erp.model.tms.enums.SupplierTypeEnum;
import com.erp.model.tms.enums.ReconciliationTypeEnum;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.tms.query.TmsFirstMileLogisticQueryHandler;
import com.erp.server.tms.service.LogisticsBillService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 头程物流单
 * @author lrp
 * @since 2024-03-19
 */
@Slf4j
@RestController
@LogSystemModule("头程物流单")
@RequestMapping("/tmsFirstMileLogistic")
public class TmsFirstMileLogisticController extends BaseController {

    @Resource
    private TmsFirstMileLogisticService tmsFirstMileLogisticService;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;
    @Resource
    private SupplierFeign supplierFeign;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    /**
     * 导入
     * @author lrp
     * @date:  2024-03-19

    /**
     * tabList
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "lb.shop_id",
            warehouseTableField = "fmd.delivery_warehouse_id,fmd.dest_warehouse_id",
            menuCode = "tms:tmsFirstMileLogistic:paging",
            tableAlias = "lb"
    )
    public ApiResult<List<TmsFirstMileLogisticDTO.TabListDTO>> tabList(@RequestBody TmsFirstMileLogisticDTO.PagingParamDTO dto) {
        return success(tmsFirstMileLogisticService.tabList(dto));
    }

    /**
     * 分页列表
     * @author lrp
     * @date:  2024-03-19
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = TmsFirstMileLogisticQueryHandler.class)
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "lb.shop_id",
            warehouseTableField = "fmd.delivery_warehouse_id,fmd.dest_warehouse_id",
            menuCode = "tms:tmsFirstMileLogistic:paging",
            tableAlias = "lb"
    )
    public ApiResult<PagingVO<TmsFirstMileLogisticDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<TmsFirstMileLogisticDTO.PagingParamDTO> dto) {
        PagingVO<TmsFirstMileLogisticDTO.PagingVO> pagingVO = tmsFirstMileLogisticService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表统计
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/statistics")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileLogistic:paging",
            tableAlias = "lb"
    )
    public ApiResult<TmsFirstMileLogisticDTO.StatisticsVO> statistics(TmsFirstMileLogisticDTO.PagingParamDTO dto) {
        return success(tmsFirstMileLogisticService.statistics(dto));
    }

    /**
     * 新增
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileLogistic:add",
            serviceClass = TmsFirstMileLogisticService.class,
            keyIdName = "id")
    @LogAction(value = LogActionEnum.INSERT, desc = "头程物流单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Valid TmsFirstMileLogisticDTO.AddDTO dto) {
        return success(tmsFirstMileLogisticService.addFirstMileLogistics(dto));
    }

    /**
     * 编辑
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileLogistic:update",
            serviceClass = TmsFirstMileLogisticService.class,
            keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单更新")
    public ApiResult<Boolean> update(@RequestBody @Valid TmsFirstMileLogisticDTO.UpdateDTO dto) {
        return success(tmsFirstMileLogisticService.update(dto));
    }

    /**
     * 详情
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<TmsFirstMileLogisticDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(tmsFirstMileLogisticService.view(id));
    }

    /**
     * 更新物流状态
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/updateLogisticsStatus")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单更新物流状态")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileLogistic:update",
            serviceClass = TmsFirstMileLogisticService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> updateLogisticsStatus(@RequestBody @Valid TmsFirstMileLogisticDTO.UpdateLogisticsStatusDTO dto) {
        List<BatchResultDTO> resultDTOS = tmsFirstMileLogisticService.updateLogisticsStatus(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新备注
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/updateRemark")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单更新备注")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileLogistic:update",
            serviceClass = TmsFirstMileLogisticService.class,
            keyIdName = "id")
    public ApiResult<Boolean> updateRemark(@RequestBody @Valid TmsFirstMileLogisticDTO.UpdateRemarkDTO dto) {
        return success(tmsFirstMileLogisticService.updateRemark(dto));
    }

    /**
     * 更新发票状态
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/updateInvoicesStatus")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单更新发票状态")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileLogistic:update",
            serviceClass = TmsFirstMileLogisticService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> updateInvoicesStatus(@RequestBody @Valid TmsFirstMileLogisticDTO.UpdateInvoicesStatusDTO dto) {
        List<BatchResultDTO> resultDTOS = tmsFirstMileLogisticService.updateInvoicesStatus(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 发票导出
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/exportInvoices")
    @LogAction(value = LogActionEnum.EXPORT, desc = "头程物流单发票导出")
    public ApiResult<Boolean> exportInvoices(@RequestBody @Valid BaseIdsDTO.IdsDTO dto, HttpServletResponse response) {
        tmsFirstMileLogisticService.exportInvoices(dto.getIds(),response);
        return ApiResult.success();
    }
    /**
     * 更新渠道
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/updateChannel")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单更新渠道")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileLogistic:update",
            serviceClass = TmsFirstMileLogisticService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> updateChannel(@RequestBody @Valid TmsFirstMileLogisticDTO.UpdateChannelDTO dto) {
        List<BatchResultDTO> resultDTOS = tmsFirstMileLogisticService.updateChannel(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 批量更新渠道
     * @author zdy
     * @date:  2025-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/batchUpdateChannel")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单批量更新渠道")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileLogistic:update",
            serviceClass = TmsFirstMileLogisticService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> batchUpdateChannel(@RequestBody @Valid List<TmsFirstMileLogisticDTO.UpdateChannelDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = tmsFirstMileLogisticService.batchUpdateChannel(dtoList);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 生成对账单
     * @author lrp
     * @date:  2024-03-19
     * @return ApiResult<String>
     */
    @PostMapping("/generateReconciliation")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程物流单生成对账单")
    public ApiResult<List<BatchResultDTO>> generateReconciliation(@RequestBody @Valid TmsFirstMileLogisticDTO.GenerateReconciliationDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<LogisticsBillEntity> logisticsBillEntityList = tmsFirstMileLogisticService.listByIds(ids);
        String reconciliationType = dto.getSupplierType();
        if (SupplierTypeEnum.WAREHOUSE.getCode().equals(reconciliationType)){
            List<String> deliveryIds = logisticsBillEntityList.stream().map(LogisticsBillEntity::getOutstockId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<FirstMileDeliveryEntity> deliveryEntityList = wmsFirstMileDeliveryFeign.listByIds(deliveryIds);
            List<String> warehouseIds = deliveryEntityList.stream().map(FirstMileDeliveryEntity::getDestWarehouseId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            //获取三方仓关联的服务商
            List<OverseasProviderWarehouseDTO.ViewDTO> viewDTOS = wmsOverseasWarehouseFeign.listByWarehouseIdList(warehouseIds);
            Map<String, List<OverseasProviderWarehouseDTO.ViewDTO>> mainIdMap = viewDTOS.stream().collect(Collectors.groupingBy(OverseasProviderWarehouseDTO.ViewDTO::getMainId));
            for(Map.Entry<String, List<OverseasProviderWarehouseDTO.ViewDTO>> provideList : mainIdMap.entrySet()) {
                String supplierName = provideList.getValue().get(0).getProviderName();
                String supplierId = provideList.getValue().get(0).getMainId();
                List<String> warehouseIds2 = provideList.getValue().stream().map(OverseasProviderWarehouseDTO.ViewDTO::getWarehouseId).collect(Collectors.toList());
                List<String> deliveryIds2 = deliveryEntityList.stream().filter(e -> warehouseIds2.contains(e.getDestWarehouseId())).map(FirstMileDeliveryEntity::getId).collect(Collectors.toList());
                ids = logisticsBillEntityList.stream().filter(e -> deliveryIds2.contains(e.getOutstockId())).map(LogisticsBillEntity::getId).distinct().collect(Collectors.toList());
                // 当前添加的主账单记录
                Map<String, TmsFirstMileReconciliationEntity> currentMainEntityMap = new HashMap<>();
                for (String id : ids) {
                    BatchResultDTO updateResult;
                    LogisticsBillEntity entity = logisticsBillEntityList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(id, e.getId())).findFirst().orElse(null);
                    if (Objects.isNull(entity)){
                        updateResult = BatchResultDTO.fail(id, id, "物流单记录不存在");
                        resultDTOS.add(updateResult);
                        continue;
                    }
                    try {
                        updateResult = tmsFirstMileLogisticService.singleGenerateReconciliation(id, dto.getReconciliationId(), dto.getDateList(), currentMainEntityMap, ReconciliationTypeEnum.ACTUAL.getCode(),dto.getSupplierType(),supplierId,supplierName);
                    } catch (Exception e) {
                        log.error("头程对账生成失败", e);
                        if (ObjectUtil.isEmpty(entity)) {
                            updateResult = BatchResultDTO.fail(id, id, "B物流单不存在, 头程对账生成失败");
                            resultDTOS.add(updateResult);
                            continue;
                        }
                        updateResult = BatchResultDTO.fail(id, entity.getTransportNo(), e.getMessage());
                    }
                    resultDTOS.add(updateResult);
                }
            }
        }else if (SupplierTypeEnum.CUSTOM.getCode().equals(reconciliationType)){
            String supplierId = dto.getLogisticsSupplierId();
            if (CharSequenceUtil.isBlank(supplierId)){
                return failure("自定义物流商不能为空");
            }
            SupplierEntity supplier = supplierFeign.getSupplierById(supplierId);
            if (Objects.isNull(supplier)){
                return failure("自定义物流商不存在");
            }
            String supplierName = supplier.getName();
            // 当前添加的主账单记录
            Map<String, TmsFirstMileReconciliationEntity> currentMainEntityMap = new HashMap<>();
            for (String id : ids) {
                BatchResultDTO updateResult;
                LogisticsBillEntity entity = logisticsBillEntityList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(id, e.getId())).findFirst().orElse(null);
                if (Objects.isNull(entity)){
                    updateResult = BatchResultDTO.fail(id, id, "物流单记录不存在");
                    resultDTOS.add(updateResult);
                    continue;
                }
                try {
                    updateResult = tmsFirstMileLogisticService.singleGenerateReconciliation(id, dto.getReconciliationId(), dto.getDateList(), currentMainEntityMap, ReconciliationTypeEnum.ACTUAL.getCode(),dto.getSupplierType(),supplierId,supplierName);
                } catch (Exception e) {
                    log.error("头程对账生成失败", e);
                    if (ObjectUtil.isEmpty(entity)) {
                        updateResult = BatchResultDTO.fail(id, id, "B物流单不存在, 头程对账生成失败");
                        resultDTOS.add(updateResult);
                        continue;
                    }
                    updateResult = BatchResultDTO.fail(id, entity.getTransportNo(), e.getMessage());
                }
                resultDTOS.add(updateResult);
            }
        }else {
            Map<String, List<LogisticsBillEntity>> supplierIdMaps = logisticsBillEntityList.stream().collect(Collectors.groupingBy(LogisticsBillEntity::getLogisticsSupplierId));
            Set<String> supplierIds = supplierIdMaps.keySet();
            List<LogisticsSupplierEntity> logisticsSupplierEntityList = logisticsSupplierService.listByIds(supplierIds);
            Map<String, String> supplierNameMap = logisticsSupplierEntityList.stream().collect(Collectors.toMap(LogisticsSupplierEntity::getSupplierId, LogisticsSupplierEntity::getSupplierName));
            for(Map.Entry<String, List<LogisticsBillEntity>> supplierIdMap : supplierIdMaps.entrySet()) {
                ids = supplierIdMap.getValue().stream().map(LogisticsBillEntity::getId).collect(Collectors.toList());
                // 当前添加的主账单记录
                Map<String, TmsFirstMileReconciliationEntity> currentMainEntityMap = new HashMap<>();
                for (String id : ids) {
                    BatchResultDTO updateResult;
                    LogisticsBillEntity entity = logisticsBillEntityList.stream().filter(e -> Objects.nonNull(e) && Objects.equals(id, e.getId())).findFirst().orElse(null);
                    if (Objects.isNull(entity)){
                        updateResult = BatchResultDTO.fail(id, id, "物流单记录不存在");
                        resultDTOS.add(updateResult);
                        continue;
                    }
                    try {
                        updateResult = tmsFirstMileLogisticService.singleGenerateReconciliation(id, dto.getReconciliationId(), dto.getDateList(), currentMainEntityMap, ReconciliationTypeEnum.ACTUAL.getCode(),dto.getSupplierType(), entity.getLogisticsSupplierId(), supplierNameMap.getOrDefault(entity.getLogisticsSupplierId(), ""));
                    } catch (Exception e) {
                        log.error("头程对账生成失败", e);
                        if (ObjectUtil.isEmpty(entity)) {
                            updateResult = BatchResultDTO.fail(id, id, "B物流单不存在, 头程对账生成失败");
                            resultDTOS.add(updateResult);
                            continue;
                        }
                        updateResult = BatchResultDTO.fail(id, entity.getTransportNo(), e.getMessage());
                    }
                    resultDTOS.add(updateResult);
                }
            }
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下载模板
     */
    @GetMapping("/exportTemplate")
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载头程物流单模板")
    public ApiResult<Object>exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "excel/fmLogistics.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
        return success();
    }

    /**
     * 头程物流单导入
     */
    @PostMapping("/import")
    @LogAction(value = LogActionEnum.IMPORT, desc = "头程物流单导入")
    public ApiResult<Boolean> importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) throws Exception {
        return success(tmsFirstMileLogisticService.importExcel(excelFile,response));
    }

    /**
     * 导出
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "lb.shop_id",
            warehouseTableField = "fmd.delivery_warehouse_id,fmd.dest_warehouse_id",
            menuCode = "tms:tmsFirstMileLogistic:paging",
            tableAlias = "lb"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出头程物流单")
    @WebAdvanceQuery(handler = TmsFirstMileLogisticQueryHandler.class)
    public ApiResult<Object>export(@RequestBody @Valid TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        tmsFirstMileLogisticService.export(pagingParamDTO,response);
        return success();
    }

    /**
     * 导出费用明细
     */
    @PostMapping("/exportFeeDetail")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出头程物流单费用明细")
    @WebAdvanceQuery(handler = TmsFirstMileLogisticQueryHandler.class)
    public ApiResult<Object>exportFeeDetail(@RequestBody @Valid TmsFirstMileLogisticDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        tmsFirstMileLogisticService.exportFeeDetail(pagingParamDTO,response);
        return success();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除头程物流单")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsFirstMileLogistic:delete",
            serviceClass = TmsFirstMileLogisticService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<LogisticsBillEntity> entityList = tmsFirstMileLogisticService.listByIds(ids);
        for (String id : ids) {
            LogisticsBillEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"头程物流单不存在"));
                continue;
            }
            try {
                resultDTOS.add(tmsFirstMileLogisticService.delete(entity));
            }catch (Exception e){
                log.error("头程物流单删除失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCounterNo(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 查询符合生成条件的发货单
     */
    @PostMapping("/getCanGenerateDeliveryOrder")
    public ApiResult<List<TmsFirstMileLogisticDTO.DeliveryDTO>> getCanGenerateDeliveryOrder(@RequestBody TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto) {
        return success(tmsFirstMileLogisticService.getCanGenerateDeliveryOrder(dto));
    }

    /**
     * 选择渠道后返回对应数据
     */
    @PostMapping("getLogisticsAndShipping")
    public ApiResult<TmsFirstMileLogisticDTO.LogisticsDTO> getLogisticsAndShipping(@RequestBody TmsFirstMileLogisticDTO.CanGenerateDeliveryDTO dto){
        return success(tmsFirstMileLogisticService.getLogisticsAndShipping(dto));
    }


    /**
     * 计算运费
     */
    @PostMapping("calculateShippingCost")
    public ApiResult<BigDecimal> calculateShippingCost(@RequestBody @Valid TmsFirstMileLogisticDTO.CalculateShippingCostDTO dto){
        return success(tmsFirstMileLogisticService.calculateShippingCost(dto));
    }

    /**
     * 物流单对应待提交的对账单列表
     */
    @PostMapping("/waitSubmitReconciliation")
    public ApiResult<List<TmsFirstMileLogisticDTO.WaitSubmitListDTO>> waitSubmitReconciliation(@RequestBody @Valid BaseIdsDTO.IdsDTO dto){
        return success(tmsFirstMileLogisticService.waitSubmitReconciliation(dto.getIds()));
    }
    /**
     * 获取物流单状态（code,value）
     */
    @GetMapping("/getTrackStatusList")
    public ApiResult<List<Map<String,Object>>> getTrackStatusList() {
        return success(tmsFirstMileLogisticService.getTrackStatusList());
    }

    /**
     * 下推重量分摊
     */
    @PostMapping("/pushWeightAllocation")
    public ApiResult<List<BatchResultDTO>> pushWeightAllocation(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) throws InterruptedException {
        List<String> ids = dto.getIds().stream().filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsBillEntity> logisticsBillEntityList = logisticsBillService.listByIds(ids);
        List<BatchResultDTO> resultList = new ArrayList<>(ids.size());
        for (String id : ids) {
            LogisticsBillEntity entity = logisticsBillEntityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultList.add(BatchResultDTO.fail(id,id,"物流单不存在"));
                continue;
            }
            BatchResultDTO resultDTO = tmsFirstMileLogisticService.pushWeightAllocation(entity);
            resultList.add(resultDTO);
        }
        return resultList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultList) : failure(resultList);
    }

    /**
     * 下推物流单
     **/
    @PostMapping("/generateLogisticsBill")
    public ApiResult<List<BatchResultDTO>> generateLogisticsBill(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> entityList = wmsFirstMileDeliveryFeign.listByIds(ids);
        List<BatchResultDTO> result = new ArrayList<>();
        for (String id : ids){
            FirstMileDeliveryEntity firstMileDeliveryEntity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(firstMileDeliveryEntity)){
                result.add(BatchResultDTO.fail(id,id,"发货单为空"));
                continue;
            }
            try {
                result.add(tmsFirstMileLogisticService.generateLogisticsBill(firstMileDeliveryEntity));
            }catch (Exception e){
                log.error("头程发货单下推装箱任务失败>>>>>", e);
                result.add(BatchResultDTO.fail(firstMileDeliveryEntity.getId(),firstMileDeliveryEntity.getCode(),e.getMessage()));
            }
        }
        return result.stream().allMatch(BatchResultDTO::getSuccess) ? success(result) : failure(result);
    }
    /**
     * 获取物流轨迹明细
     * @param logisticsBillId 物流单id
     * @return
     */
    @GetMapping("/getTrackInfo")
    public ApiResult<LogisticsTrackDTO.ViewDTO> listTrack(@RequestParam(value = "logisticsBillId") String logisticsBillId){
        return success(tmsFirstMileLogisticService.listTrack(logisticsBillId));
    }
}
