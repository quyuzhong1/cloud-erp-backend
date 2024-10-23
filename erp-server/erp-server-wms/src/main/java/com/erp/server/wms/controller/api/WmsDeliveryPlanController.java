package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.scm.dto.ExcelImportDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.WmsDeliveryPlanDTO;
import com.erp.model.wms.entity.WmsDeliveryPlanEntity;
import com.erp.server.wms.query.WmsDeliveryPlanQueryHandler;
import com.erp.server.wms.service.WmsDeliveryPlanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 发货计划
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("发货计划")
@RequestMapping("/overseasDeliveryPlan")
public class WmsDeliveryPlanController extends BaseController {

    @Resource
    private WmsDeliveryPlanService wmsDeliveryPlanService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发货计划新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated WmsDeliveryPlanDTO.AddDTO dto) {
        return success(wmsDeliveryPlanService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "发货计划修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:overseasDeliveryPlan:update",
        serviceClass = WmsDeliveryPlanService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated WmsDeliveryPlanDTO.UpdateDTO dto) {
        wmsDeliveryPlanService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:paging",
            tableAlias = "odp"
    )
    public ApiResult<List<WmsDeliveryPlanDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(wmsDeliveryPlanService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return ApiResult<PagingVO<OverseasDeliveryPlanDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:paging",
            tableAlias = "odp"
    )
    @WebAdvanceQuery(handler = WmsDeliveryPlanQueryHandler.class)
    public ApiResult<PagingVO<WmsDeliveryPlanDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<WmsDeliveryPlanDTO.PagingParamDTO> dto) {
        return success(wmsDeliveryPlanService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交发货计划")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:add",
            serviceClass = WmsDeliveryPlanService.class,
            keyIdName = "id")
    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated WmsDeliveryPlanDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = wmsDeliveryPlanService.addAndSubmit(dto);
        return success(result);
    }

    /**
    * 修改并提交审核
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<Void>
    */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交发货计划")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:updateAndSubmit",
            serviceClass = WmsDeliveryPlanService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated WmsDeliveryPlanDTO.UpdateDTO dto) {
        wmsDeliveryPlanService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:submit",
            serviceClass = WmsDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "发货计划提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = wmsDeliveryPlanService.submit(id);
            }catch (Exception e){
                log.error("发货计划 提交审核失败",e);
                WmsDeliveryPlanEntity entity = wmsDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "发货计划不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 审核
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:approve",
            serviceClass = WmsDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "发货计划审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = wmsDeliveryPlanService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("发货计划审核失败",e);
                WmsDeliveryPlanEntity entity = wmsDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "发货计划不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 反审核
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:disApprove",
            serviceClass = WmsDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "发货计划反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = wmsDeliveryPlanService.disApprove(id);
            }catch (Exception e){
                log.error("发货计划反审核失败",e);
                WmsDeliveryPlanEntity entity = wmsDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "发货计划不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 删除
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:delete",
            serviceClass = WmsDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "发货计划删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = wmsDeliveryPlanService.delete(id);
            }catch (Exception e){
                log.error("发货计划删除失败",e);
                WmsDeliveryPlanEntity entity = wmsDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "发货计划不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 作废
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:invalid",
            serviceClass = WmsDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "发货计划作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = wmsDeliveryPlanService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("发货计划作废失败",e);
                WmsDeliveryPlanEntity entity = wmsDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "发货计划不存在, 作废失败");
                    resultDTOS.add(invalidResult);
                    continue;
                }
                invalidResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(invalidResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 撤销
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:cancelProcess",
            serviceClass = WmsDeliveryPlanService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "发货计划撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = wmsDeliveryPlanService.cancelProcess(id);
            }catch (Exception e){
                log.error("发货计划撤回流程失败",e);
                WmsDeliveryPlanEntity entity = wmsDeliveryPlanService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "发货计划不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 详情
    * @author Luo_WG
    * @date:  2023-11-16
    * @param id
    * @return ApiResult<OverseasDeliveryPlanDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasDeliveryPlan:view",
            serviceClass = WmsDeliveryPlanService.class,
            keyIdName = "id")
    public ApiResult<WmsDeliveryPlanDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(wmsDeliveryPlanService.view(id));
    }

    /**
    * 导出Excel数据
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return
    */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "发货计划导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated WmsDeliveryPlanDTO.PagingParamDTO dto) {
        wmsDeliveryPlanService.exportList(dto);
        return success(true);
    }

    /**
     * 查询发货记录
     * @Author Luo_WG
     * @Date 2023/11/16 17:21
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<OverseasDeliveryPlanDTO.DeliverRecordDTO>>
     **/
    @GetMapping("/listDeliverRecord")
    public ApiResult<List<FirstMileDeliveryDTO.DeliverRecordView>> listDeliverRecord(@RequestParam("id") String id) {
        List<FirstMileDeliveryDTO.DeliverRecordView> result = wmsDeliveryPlanService.listDeliverRecord(id);
        return success(result);
    }

    /**
     * 下推要货申请列表查询
     * @Author Luo_WG
     * @Date 2023/11/16 18:07
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>>
     **/
    @PostMapping("/generateRequisitionApplicationView")
    public ApiResult<List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO>> generateRequisitionApplicationView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> result = wmsDeliveryPlanService.generateRequisitionApplicationView(dto.getIds());
        return success(result);
    }

    /**
     * 发货计划下推要货申请保存
     * @Author Luo_WG
     * @Date 2023/11/16 18:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateRequisitionApplicationSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "发货计划下推要货申请保存")
    public ApiResult generateRequisitionApplicationSave(@RequestBody @Validated ValidList<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> dto) {
        Boolean flag = wmsDeliveryPlanService.generateRequisitionApplicationSave(dto.getList());
        return flag ? success() : failure();
    }

    /**
     * 发货计划下推要货申请保存并提交
     * @Author Luo_WG
     * @Date 2023/11/16 18:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateRequisitionApplicationSaveAndSubmit")
    @LogAction(value = LogActionEnum.INSERT, desc = "发货计划下推要货申请保存并提交")
    public ApiResult generateRequisitionApplicationSaveAndSubmit(@RequestBody @Validated ValidList<WmsDeliveryPlanDTO.GenerateRequisitionApplicationViewDTO> dto) {
        Boolean flag = wmsDeliveryPlanService.generateRequisitionApplicationSaveAndSubmit(dto.getList());
        return flag ? success() : failure();
    }

    /**
     * 下推发货单列表查询
     * @Author Luo_WG
     * @Date 2023/11/16 18:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.OverseasDeliveryPlanDTO.GenerateDeliverViewDTO>>
     **/
    @PostMapping("/generateDeliverView")
    public ApiResult<List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO>> generateDeliverView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> result = wmsDeliveryPlanService.generateDeliverView(dto.getIds());
        return success(result);
    }

    /**
     * 下推发货单保存
     * @Author Luo_WG
     * @Date 2023/11/16 18:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateDeliverSave")
    @LogAction(value = LogActionEnum.INSERT, desc = "下推发货单保存")
    public ApiResult generateDeliverSave(@RequestBody @Validated ValidList<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> dto) {
        Boolean flag = wmsDeliveryPlanService.generateDeliverSave(dto.getList());
        return flag ? success() : failure();
    }

    /**
     * 下推发货单保存并提交
     * @Author Luo_WG
     * @Date 2023/11/16 18:06
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateDeliverSaveAndSubmit")
    @LogAction(value = LogActionEnum.INSERT, desc = "下推发货单保存并提交")
    public ApiResult generateDeliverSaveAndSubmit(@RequestBody @Validated ValidList<WmsDeliveryPlanDTO.GenerateDeliverViewDTO> dto) {
        Boolean flag = wmsDeliveryPlanService.generateDeliverSaveAndSubmit(dto.getList());
        return flag ? success() : failure();
    }

    /**
     * 导入详情信息
     * @Author Luo_WG
     * @Date 2023/11/23 14:17
     * @param excelImportDTO
     * @param response
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.OverseasDeliveryPlanDetailDTO.ImportDTO>
     **/
    @PostMapping("/importDetailFile")
    public ApiResult<ListingInfoDTO.ImportDTO> importFile(@ModelAttribute @Validated ExcelImportDTO.CommonDTO excelImportDTO, HttpServletResponse response) {
        ListingInfoDTO.ImportDTO list = wmsDeliveryPlanService.importFile(excelImportDTO.getExcelFile(), excelImportDTO.getThirdSkuNoList(),excelImportDTO.getWarehouseId(),excelImportDTO.getShopId() , response);
        return success(list);
    }

    /**
     * 下载模板
     * @author Will
     * @date: 22023/3/15 18:22
     * @param request
     * @param response
     */
    @GetMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/deliveryPlanDetailTemplate.xlsx";
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
        return success();
    }


    /**
     * 发货计划显示
     * @author will
     * @date 2024/10/23 14:43
     * @param dto
     * @return ApiResult<WmsDeliveryPlanDTO.DeliverPlanViewDTO>
     */
    @PostMapping("/deliverPlanView")
    public ApiResult<WmsDeliveryPlanDTO.DeliverPlanViewDTO> deliverPlanView(@RequestBody @Validated BaseIdDTO dto) {
        WmsDeliveryPlanDTO.DeliverPlanViewDTO result = wmsDeliveryPlanService.deliverPlanView(dto.getId());
        return success(result);
    }
}
