package com.erp.server.oms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.AddGroup;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.scm.dto.SkuCostProfitDTO;
import com.erp.server.oms.query.SoInfoQueryHandler;
import com.erp.server.oms.service.SoChangeService;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 销售管理-销售订单
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@LogSystemModule("销售订单")
@RequestMapping("/so")
@Slf4j
public class SoInfoController extends BaseController {

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SoChangeService soChangeService;
    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<SoInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<SoInfoDTO.TabListDTO> tabList = soDetailService.tabList(dto);
        return success(tabList);
    }

    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:paging",
            tableAlias = "si"
    )
    @WebAdvanceQuery(handler = SoInfoQueryHandler.class)
    public ApiResult<PagingVO<SoInfoDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoInfoDTO.PagingParamDTO> dto) {
        PagingVO<SoInfoDTO.PagingViewDTO> pagingVO = soInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表查询总数
     *
     * @param dto
     * @return
     */
    @PostMapping("/pagingTotal")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:paging",
            tableAlias = "si"
    )
    @WebAdvanceQuery(handler = SoInfoQueryHandler.class)
    public ApiResult<SoInfoDTO.PagingTotalDTO> pagingTotal(@RequestBody @Validated SoInfoDTO.PagingParamDTO dto) {
        SoInfoDTO.PagingTotalDTO viewDTO = soInfoService.pagingTotal(dto);
        return success(viewDTO);
    }


    /**
     * 暂存
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "暂存销售订单")
    @PostMapping("/draft")
    public ApiResult draft(@RequestBody @Validated SoInfoDTO.AddDTO dto) {
        String id = soInfoService.draft(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 获取到所有审核通过的销售订单
     *
     * @param
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<BaseIdDTO.CodeDTO>> list() {
        List<BaseIdDTO.CodeDTO> resultList = soInfoService.listSo();
        return success(resultList);
    }


    /**
     * 根据销售订单 id 获取客户信息
     *
     * @param
     * @return
     */
    @GetMapping("/soCustomer")
    public ApiResult<SoInfoDTO.CustomerDTO> getSoCustomer(@RequestParam("id") String id) {
        SoInfoDTO.CustomerDTO result = soInfoService.getSoCustomer(id);
        return success(result);
    }

    /**
     * 创建
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "创建销售订单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated({AddGroup.class}) SoInfoDTO.AddDTO dto) {
        String id = soInfoService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 批量提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "批量提交审核销售订单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:submit",
            serviceClass = SoInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soInfoService.submit(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 新增并提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交审核销售订单")
    @PostMapping("/addAndSubmit")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated({AddGroup.class}) SoInfoDTO.AddDTO dto) {
        Boolean result = soInfoService.addAndSubmit(dto);
        return result ? success() : failure();
    }


    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:view",
            serviceClass = SoInfoService.class,
            keyIdName = "id"
    )
    public ApiResult<SoInfoDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SoInfoDTO.ViewDTO view = soInfoService.view(dto.getId());
        return success(view);
    }
    /**
     * 打印拣货单
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/printPickingView")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:printPickingView",
            serviceClass = SoInfoService.class,
            keyIdName = "id"
    )
    public ApiResult<SoInfoDTO.ViewDTO> printPickingView(@RequestBody @Validated BaseIdDTO dto) {
        SoInfoDTO.ViewDTO view = soInfoService.printPickingView(dto.getId());
        return success(view);
    }

    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售订单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:update",
            serviceClass = SoInfoService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated SoInfoDTO.UpdateDTO dto) {
        String id = soInfoService.updateSo(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交销售订单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:update",
            serviceClass = SoInfoService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated SoInfoDTO.UpdateDTO dto) {
        Boolean result = soInfoService.updateAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 更新明细备注
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/7/19 14:58
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更新销售订单明细备注:ids={ids},备注={remark}")
    @PostMapping("/updateDetailRemark")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id,seller_id",
//            menuCode = "oms:so:update",
//            serviceClass = SoInfoService.class,
//            keyIdName = "ids")
    public ApiResult updateDetailRemark(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = soInfoService.updateDetailRemark(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 更新备注
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/7/19 14:58
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更新销售订单备注:ids={ids},备注={remark}")
    @PostMapping("/updateRemark")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id,seller_id",
//            menuCode = "oms:so:update",
//            serviceClass = SoInfoService.class,
//            keyIdName = "ids")
    public ApiResult updateRemark(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = soInfoService.updateRemark(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售订单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:approve",
            serviceClass = SoInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoInfoEntity> soInfoEntityList = soInfoService.listByIds(ids);
        for (String id : ids) {
            SoInfoEntity entity = soInfoEntityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售订单不存在"));
                continue;
            }
            try {
                resultDTOS.add(soInfoService.approve(dto, entity));
            }catch (Exception e){
                log.error("B2B销售订单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核销售订单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:disApprove",
            serviceClass = SoInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoInfoEntity> soInfoEntityList = soInfoService.listByIds(ids);
        List<SoChangeEntity> soChangeList = soChangeService.listBySoIds(ids);
        for (String id : ids) {
            SoInfoEntity entity = soInfoEntityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售订单不存在"));
                continue;
            }
            try {
                resultDTOS.add(soInfoService.disApprove(dto, entity,soChangeList ));
            }catch (Exception e){
                log.error("B2B销售订单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销销售订单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:cancelProcess",
            serviceClass = SoInfoService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soInfoService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 删除销售订单
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除销售订单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:delete",
            serviceClass = SoInfoService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = soInfoService.deleteByIds(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 作废
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/5/10 20:11
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废销售订单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:invalid",
            serviceClass = SoInfoService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = soInfoService.invalid(dto.getIds(), dto.getRemark());
        return result ? success() : failure();
    }

    /**
     * 导出
     * 数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出销售订单")
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid SoInfoDTO.ExportDTO dto) {
        Boolean result = soInfoService.exportExcel(dto);
        return result ? success() : failure();

    }

    /**
     * 添加详情按钮-列表查询
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.SoDetailDTO.AddDetailView>>
     * @Author Luo_WG
     * @Date 2023/5/16 18:43
     **/
    @PostMapping("/listAddDetailView")
    public ApiResult<List<SoDetailDTO.AddDetailView>> listAddDetailView(@RequestBody listAddDetailViewDTO dto) {
        List<SoDetailDTO.AddDetailView> addDetailViews = soDetailService.listAddDetailView(dto);
        return success(addDetailViews);
    }

    /**
     * 查询销售订单合同PDF数据
     * @author will
     * @date 2024/11/5 9:34
     * @param id
     * @return ApiResult<ExportPdfDTO>
     */
    @GetMapping("/listSoContractPdf")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:exportSoContractPdf",
            serviceClass = SoInfoService.class,
            keyIdName = "id")
    public ApiResult<SoInfoDTO.ExportPdfDTO> listSoContractPdf(@RequestParam("id") String id) {
        SoInfoDTO.ExportPdfDTO result = soInfoService.listSoContractPdf(id);
        return success(result);
    }

    /**
     * 导出销售订单合同PDF
     *
     * @return
     * @author yl
     * @date 2023-05-18 12:01
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出销售订单合同PDF")
    @PostMapping("/exportSoContractPdf")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:exportSoContractPdf",
            serviceClass = SoInfoService.class,
            keyIdName = "id")
    public void exportSoContractPdf(@RequestBody @Valid BaseIdDTO dto, HttpServletResponse response) {
         soInfoService.exportSoContractPdf(dto.getId(),response);
    }

    /**
     * 导出销售订单合同excel
     *
     * @return
     * @author yl
     * @date 2023-05-18 12:01
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出销售订单合同excel")
    @PostMapping("/exportSoContractExcel")
    public ApiResult<SoInfoDTO.ExportPdfDTO> exportSoContractExcel(@RequestBody @Valid BaseIdDTO dto, HttpServletResponse response) {
        Boolean result = soInfoService.exportSoContractExcel(dto.getId(), response);
        return result ? success() : failure();
    }

    /**
     * 导出销售订单的的发票信息
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.oms.dto.SoInfoDTO.ExportPdfDTO>
     * @author yl
     * @date 2023-07-04 14:44
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出销售订单的的发票信息")
    @PostMapping("/exportSoPI")
    public ApiResult exportSoPI(@RequestBody @Valid BaseIdDTO dto, HttpServletResponse response) {
        Boolean result = soInfoService.exportSoPI(dto.getId(), response);
        return result ? success() : failure();

    }

    /**
     * 导出销售订单国内PI
     *
     * @param dto
     * @param response
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-10-12 14:39
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出销售订单国内PI")
    @PostMapping("/exportSoDomesticPI")
    public ApiResult exportSoDomesticPI(@RequestBody @Valid BaseIdDTO dto, HttpServletResponse response) {
        Boolean result = soInfoService.exportSoDomesticPI(dto.getId(), response);
        return result ? success() : failure();

    }


    /**
     * 下推备货申请单数据显示
     *
     * @param dto
     * @return ApiResult<List < AddDetailView>>
     * @author Will
     * @date: 2023/5/18 19:33
     */
    @PostMapping("/viewGenerateSalesDemand")
    public ApiResult<List<SoInfoDTO.ViewGenerateSalesDemandDTO>> viewGenerateSalesDemand(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SoInfoDTO.ViewGenerateSalesDemandDTO> list = soInfoService.viewGenerateSalesDemand(dto.getIds());
        return success(list);
    }

    /**
     * 下推发货通知单\销售出库单-列表查询
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.SoInfoDTO.GenerateDeliveryView>>
     * @Author Luo_WG
     * @Date 2023/5/25 12:01
     **/
    @PostMapping("/generateDeliveryView")
    public ApiResult<List<SoInfoDTO.GenerateDeliveryView>> generateDeliveryView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SoInfoDTO.GenerateDeliveryView> list = soInfoService.generateDeliveryView(dto.getIds());
        return success(list);
    }

    /**
     * 下推销售退货订单-列表查询
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.SoInfoDTO.GenerateSoReturnView>>
     * @Author Luo_WG
     * @Date 2023/5/25 15:16
     **/
    @PostMapping("/generateSoReturnView")
    public ApiResult<List<SoInfoDTO.GenerateSoReturnView>> generateSoReturnView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SoInfoDTO.GenerateSoReturnView> list = soInfoService.generateSoReturnView(dto.getIds());
        return success(list);
    }

    /**
     * 下推销售退货订单-列表查询-计算退货金额
     * @param  calDTO
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.SoInfoDTO.GenerateSoReturnView>>
     * @Author jack
     * @Date 2024-11-25
     **/
    @PostMapping("/calReturnAmountByQty")
    public ApiResult<List<SoInfoDTO.GenerateSoReturnView>> calReturnAmountByQty(@RequestBody SoInfoDTO.CalDetailDTO calDTO) {
        List<SoInfoDTO.GenerateSoReturnView> list = soInfoService.calReturnAmountByQty(calDTO.getDetails());
        return success(list);
    }

    /**
     * 根据sku id和数量计算成本毛利
     *
     * @param costParam
     * @return
     */
    @PostMapping("/getSkuCostProfit")
    public ApiResult<SkuCostProfitDTO.SkuCostProfitResult> getSkuCostProfit(@RequestBody @Validated SkuCostProfitDTO.SkuCostProfitParam costParam) {
        return success(soInfoService.getSkuCostProfit(costParam));
    }

    /**
     * 补录销售订单毛利历史数据
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "补录销售订单毛利历史数据,开始时间={startDate},结束时间={endDate}")
    @GetMapping("/brushData")
    public ApiResult<Void> getSkuCostProfit(@RequestParam(value = "startDate") String startDate,
                                            @RequestParam(value = "endDate") String endDate) {
        soInfoService.brushCostData(LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return success();
    }

    /**
     * 补录销售订单毛利历史数据（根据id）
     *
     * @param id
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "补录销售订单毛利历史数据id={id}")
    @GetMapping("/brushById")
    public ApiResult<Void> brushById(@RequestParam(value = "id") String id) {
        soInfoService.brushCostData(id);
        return success();
    }

    /**
     * 打印
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.lang.Void>
     * @Author Luo_WG
     * @Date 2023/7/13 10:44
     **/
    @PostMapping("/print")
    public ApiResult<List<SoInfoDTO.PrintDTO>> print(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<SoInfoDTO.PrintDTO> printDTOList = soInfoService.print(dto.getIds());
        return success(printDTOList);
    }

    /**
     * 临时接口：添加折扣额 修复历史的数据销售额数据
     *
     * @param
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-10-13 9:06
     */
    @PostMapping("/temporaryUpdate")
    public ApiResult temporaryUpdate() {
        List<String> errorList = soInfoService.temporaryUpdate();
        if (CollectionUtils.isNotEmpty(errorList)) {
            return ApiResult.error(1,"以下订单出错: "+errorList.stream().collect(Collectors.joining(",")));
        }
        return ApiResult.success();
    }

    /**
     * 根据销售订单判断是否已经下推过有效发货通知单
     *
     * @param id
     * @return com.common.core.controller.vo.ApiResult<Boolean>
     * @author zhangchunlin
     * @date 2023-07-26 17:40
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "根据销售订单判断是否已经下推过有效发货通知单：id={id}")
    @PostMapping("/checkPushDownDeliveryNotice")
    public ApiResult<Boolean> checkPushDownDeliveryNotice(@RequestParam(value = "id") String id) {
        Boolean isPush = soInfoService.checkSoPushDeliveryNotice(id);
        return success(isPush);
    }

    /**
     * 获取销售成本毛利信息
     *
     * @param calCostProfitDTO
     * @return
     */
    @PostMapping("/calSkuCostProfit")
    public ApiResult<List<SoDetailDTO.CalDetailResultDTO>> calSkuCostProfit(@RequestBody @Validated SoInfoDTO.CalCostProfitDTO calCostProfitDTO) {
        return success(soInfoService.calSkuCostProfit(calCostProfitDTO));
    }

    /**
     * 下载销售订单导入模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载销售订单导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        soInfoService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入销售订单
     *
     * @param response
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-10-17 10:28
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入销售订单")
    @PostMapping("/importExcel")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = soInfoService.importExcel(excelFile, response);
        return result?success():failure();
    }

    /**
     * 下推加工单保存
     * @author Will
     * @date: 2023/12/6 11:04
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "下推加工单保存")
    @PostMapping(value = "/generateMachineInfo")
    public ApiResult generateMachineInfo(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result =  soInfoService.generateMachineInfo(dto.getIds());
        return result ? success():failure();
    }

    /**
     * 下推销售出库订单-列表查询 入参id为明细Id
     **/
    @PostMapping("/generateSoOutView")
    public ApiResult<List<SoInfoDTO.GenerateSoOutView>> generateSoOutView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SoInfoDTO.GenerateSoOutView> list = soInfoService.generateSoOutView(dto.getIds());
        return success(list);
    }

    /**
     * 下推销售出库订单
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "下推销售出库订单,id={soId}")
    @PostMapping("/generateSoOut")
    public ApiResult<List<BatchResultDTO>> generateSoOut(@RequestBody @Validated List<SoInfoDTO.GenerateSoOutView> generateSoOutViewList) {
        List<BatchResultDTO> resultDTOS = soInfoService.generateSoOut(generateSoOutViewList);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 单个锁定查询
     * @author will
     * @date 2024/7/15 11:11
     * @param id
     * @return ApiResult<SoInfoDTO.LockVirtualInventoryDTO>
     */
    @GetMapping("/viewLockVirtualInventory")
    public ApiResult<SoInfoDTO.LockVirtualInventoryDTO> viewLockVirtualInventory(@RequestParam(value = "id") String id) {
        return success(soInfoService.viewLockVirtualInventory(id));
    }

    /**
     * 批量锁定查询
     * @author will
     * @date 2024/7/15 15:03
     * @param dto
     * @return ApiResult<List<SoInfoDTO.BatchLockVirtualInventoryDTO>>
     */
    @PostMapping("/viewBatchLockVirtualInventory")
    public ApiResult<List<SoInfoDTO.BatchLockVirtualInventoryDTO>> viewBatchLockVirtualInventory(@RequestBody @Validated BaseIdsDTO.DetailIdListDTO dto) {
        return success(soInfoService.viewBatchLockVirtualInventory(dto.getDetailIdList()));
    }

    /**
     * 锁定库存保存
     * @author will
     * @date 2024/7/15 16:00
     * @param list
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "锁定库存",keyIdName = "detailId")
    @PostMapping("/saveLockVirtualInventory")
    public ApiResult<List<BatchResultDTO>> saveLockVirtualInventory(@RequestBody @Validated List<SoInfoDTO.LockVirtualInventorySaveDTO> list) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());
        for (SoInfoDTO.LockVirtualInventorySaveDTO saveDTO : list) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = soDetailService.saveLockVirtualInventory(saveDTO);
            }catch (Exception e){
                log.error("销售订单明细释放库存失败",e);
                SoDetailEntity entity = soDetailService.getById(saveDTO.getDetailId());
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(saveDTO.getDetailId(), saveDTO.getDetailId(), "销售订单明细不存在, 锁定库存失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                SoInfoEntity soInfoEntity = soInfoService.getById(entity.getMainId());
                if (ObjectUtil.isEmpty(soInfoEntity)) {
                    resultDTO = BatchResultDTO.fail(saveDTO.getDetailId(), saveDTO.getDetailId(), "销售订单不存在, 锁定库存失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(),  CharSequenceUtil.format("【{}】{}",soInfoEntity.getCode(),entity.getSkuNo()), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 单个释放锁定库存
     * @author will
     * @date 2024/7/15 17:24
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "释放库存")
    @PostMapping("/unLockVirtualInventory")
    public ApiResult unLockVirtualInventory(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = soInfoService.unLockVirtualInventory(dto.getId());
        return result ? success():failure();
    }

}
