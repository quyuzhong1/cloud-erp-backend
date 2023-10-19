package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.AddGroup;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.scm.dto.SkuCostProfitDTO;
import com.erp.server.oms.kingdee.SyncKingdeeSoService;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Map;

/**
 * 销售管理-销售订单
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/so")
public class SoInfoController extends BaseController {

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoDetailService soDetailService;
    @Resource
    private SyncKingdeeSoService syncKingdeeSoService;
    @Resource
    private MQProducerService mQProducerService;

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
     * 修改
     *
     * @param dto
     * @return
     */
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
    @PostMapping("/updateDetailRemark")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:update",
            serviceClass = SoInfoService.class,
            keyIdName = "ids")
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
    @PostMapping("/updateRemark")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:update",
            serviceClass = SoInfoService.class,
            keyIdName = "ids")
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
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:approve",
            serviceClass = SoInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = soInfoService.approve(dto);
        return result ? success() : failure();
    }

    /**
     * 反审核
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:disApprove",
            serviceClass = SoInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = soInfoService.disApprove(dto);
        return result ? success() : failure();
    }

    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
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
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid SoInfoDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = soInfoService.exportExcel(dto, response);
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
     * 导出销售订单合同PDF
     *
     * @return
     * @author yl
     * @date 2023-05-18 12:01
     */
    @GetMapping("/exportSoContractPdf")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:so:exportSoContractPdf",
            serviceClass = SoInfoService.class,
            keyIdName = "id")
    public ApiResult<SoInfoDTO.ExportPdfDTO> exportSoContractPdf(@RequestParam("id") String id) {
        SoInfoDTO.ExportPdfDTO result = soInfoService.exportSoContractPdf(id);
        return success(result);
    }

    /**
     * 导出销售订单合同excel
     *
     * @return
     * @author yl
     * @date 2023-05-18 12:01
     */
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
     * 订单监听测试方法
     *
     * @param id
     * @return
     */
    @PostMapping("/testOrderPush")
    public ApiResult testOrderPush(@RequestParam(value = "id") String id,@RequestParam(value = "operate") String operate) {
        SoInfoEntity soInfoEntity = soInfoService.getById(id);
//        soInfoService.syncOrderToDmp(soInfoEntity, SyncOperateEnum.OPERATE_APPROVE.getCode());
        String dmpPullTaskId = syncKingdeeSoService.syncOrderToDmp(soInfoEntity, operate);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", id);
        resultMap.put("dmpPullTaskId", dmpPullTaskId);
        resultMap.put("code", soInfoEntity.getCode());
        resultMap.put("operate", operate);
        SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SO_INFO_TAG.getName(),
                resultMap, id);
        if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
            return success();
        } else {
            return failure();
        }
    }

    /**
     * 下载销售订单导入模板
     *
     * @return
     */
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
    @PostMapping("/importExcel")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = soInfoService.importExcel(excelFile, response);
        return result?success():failure();
    }
}
