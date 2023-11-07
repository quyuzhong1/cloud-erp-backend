package com.erp.server.wms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.FbaDeliveryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import com.erp.model.wms.entity.FbaDeliveryEntity;

/**
 * FBA发货单
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@RestController
@LogSystemModule("FBA发货单")
@RequestMapping("/fbaDelivery")
public class FbaDeliveryController extends BaseController {

    @Autowired
    private FbaDeliveryService fbaDeliveryService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "FBA发货单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FbaDeliveryDTO.AddDTO dto) {
        return success(fbaDeliveryService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
    tableField = "create_user_id",
    menuCode = "wms:fbaDelivery:update",
    serviceClass = FbaDeliveryService.class,
    keyIdName = "id")
    public ApiResult update(@RequestBody @Validated FbaDeliveryDTO.UpdateDTO dto) {
        fbaDeliveryService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:paging",
            tableAlias = ""
    )
    public ApiResult<List<FbaDeliveryDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(fbaDeliveryService.tabList(dto));
    }

    /**
    * 列表查询
    * @author Luo_WG
    * @date: 2023-10-30
    * @param dto
    * @return ApiResult<PagingVO<FbaDeliveryDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<FbaDeliveryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FbaDeliveryDTO.PagingParamDTO> dto) {
        return success(fbaDeliveryService.paging(dto));
    }

    /**
    * 新增并提交审核
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/addAndSubmit")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated FbaDeliveryDTO.AddDTO dto) {
        fbaDeliveryService.addAndSubmit(dto);
        return success();
    }

    /**
    * 修改并提交审核
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<Void>
    */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:updateAndSubmit",
            serviceClass = FbaDeliveryService.class,
            keyIdName = "id")
    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated FbaDeliveryDTO.UpdateDTO dto) {
        fbaDeliveryService.updateAndSubmit(dto);
        return success();
    }

    /**
    * 提交审核
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:submit",
            serviceClass = FbaDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "FBA发货单提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = fbaDeliveryService.submit(id);
            }catch (Exception e){
                log.error("FBA发货单 提交审核失败",e);
                FbaDeliveryEntity entity = fbaDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "FBA发货单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return success(resultDTOS);
    }

    /**
    * 审核
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:approve",
            serviceClass = FbaDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "FBA发货单审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = fbaDeliveryService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("FBA发货单审核失败",e);
                FbaDeliveryEntity entity = fbaDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "FBA发货单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return success(resultDTOS);
    }

    /**
    * 反审核
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:disApprove",
            serviceClass = FbaDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "FBA发货单反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = fbaDeliveryService.disApprove(id);
            }catch (Exception e){
                log.error("FBA发货单反审核失败",e);
                FbaDeliveryEntity entity = fbaDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "FBA发货单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return success(resultDTOS);
    }

    /**
    * 删除
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:delete",
            serviceClass = FbaDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "FBA发货单删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = fbaDeliveryService.delete(id);
            }catch (Exception e){
                log.error("FBA发货单删除失败",e);
                FbaDeliveryEntity entity = fbaDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "FBA发货单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return success(resultDTOS);
    }
    /**
    * 作废
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:invalid",
            serviceClass = FbaDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.INVALID, desc = "FBA发货单作废")
    public ApiResult<List<BatchResultDTO>> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO invalidResult;
            try {
                invalidResult = fbaDeliveryService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("FBA发货单作废失败",e);
                FbaDeliveryEntity entity = fbaDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    invalidResult = BatchResultDTO.fail(id, id, "FBA发货单不存在, 作废失败");
                    resultDTOS.add(invalidResult);
                    continue;
                }
                invalidResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(invalidResult);
        }
        return success(resultDTOS);
    }

    /**
    * 撤销
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:cancel",
            serviceClass = FbaDeliveryService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "FBA发货单撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = fbaDeliveryService.cancelProcess(id);
            }catch (Exception e){
                log.error("FBA发货单撤回流程失败",e);
                FbaDeliveryEntity entity = fbaDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "FBA发货单不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return success(resultDTOS);
    }

    /**
    * 详情
    * @author Luo_WG
    * @date:  2023-10-30
    * @param id
    * @return ApiResult<FbaDeliveryDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:view",
            serviceClass = FbaDeliveryService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<FbaDeliveryDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(fbaDeliveryService.view(id));
    }

    /**
    * 导出Excel数据
    * @author Luo_WG
    * @date:  2023-10-30
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaDelivery:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "FBA发货单导出Excel数据")
    public void exportList(@RequestBody @Validated FbaDeliveryDTO.ExportDTO dto, HttpServletResponse response) {
        fbaDeliveryService.exportList(dto, response);
    }

    /**
     * 下推加工单列表查询
     * @Author Luo_WG
     * @Date 2023/10/31 9:44
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.GenerateGenerateMachineView>>
     **/
    @PostMapping("/generateMachineView")
    public ApiResult<List<FbaDeliveryDTO.GenerateMachineView>> generateMachineView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<FbaDeliveryDTO.GenerateMachineView> result = fbaDeliveryService.generateMachineView(dto.getIds());
        return success(result);
    }

    /**
     * 下推加工单保存
     * @Author Luo_WG
     * @Date 2023/10/31 9:57
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/fbaDeliveryGenerateMachineSave")
    public ApiResult fbaDeliveryGenerateMachineSave(@RequestBody @Validated List<FbaDeliveryDTO.GenerateMachineView> list) {
        Boolean flag = fbaDeliveryService.fbaDeliveryGenerateMachineSave(list);
        return flag ? success() : failure();
    }

    /**
     * 下推加工单提交
     * @Author Luo_WG
     * @Date 2023/10/31 9:57
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/fbaDeliveryGenerateMachineSubmit")
    public ApiResult fbaDeliveryGenerateMachineSubmit(@RequestBody @Validated List<FbaDeliveryDTO.GenerateMachineView> list) {
        Boolean flag = fbaDeliveryService.fbaDeliveryGenerateMachineSubmit(list);
        return flag ? success() : failure();
    }

    /**
     * 下推加工单提交并审核
     * @Author Luo_WG
     * @Date 2023/10/31 9:57
     * @param list
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/fbaDeliveryGenerateMachineSubmitAndApprove")
    public ApiResult fbaDeliveryGenerateMachineSubmitAndApprove(@RequestBody @Validated List<FbaDeliveryDTO.GenerateMachineView> list) {
        Boolean flag = fbaDeliveryService.fbaDeliveryGenerateMachineSubmitAndApprove(list);
        return flag ? success() : failure();
    }

    /**
     * 打印子件明细查询
     * @Author Luo_WG
     * @Date 2023/10/31 10:12
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.PrintSonItem>>
     **/
    @PostMapping("/printSonItemDetail")
    public ApiResult<List<FbaDeliveryDTO.PrintSonItem>> printSonItemDetail(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<FbaDeliveryDTO.PrintSonItem> list = fbaDeliveryService.printSonItemDetail(dto.getIds());
        return success(list);
    }
}
