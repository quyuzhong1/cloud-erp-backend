package com.erp.server.tms.controller.api;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsBillCostDTO.PushDTO;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDeadlineSettingDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.entity.TransferDeclareEntity;
import com.erp.model.tms.enums.TransferDeclareUploadStatusEnum;
import com.erp.server.tms.query.TmsTransferDeclareQueryHandler;
import com.erp.server.tms.service.TransferDeclareDetailService;
import com.erp.server.tms.service.TransferDeclareService;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 中转报关表
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("中转报关表")
@RequestMapping("/transferDeclare")
public class TransferDeclareController extends BaseController {

    @Resource
    private TransferDeclareService transferDeclareService;

    @Resource
    private TransferDeclareDetailService transferDeclareDetailService;

    /**
     * 分页列表查询
     * @Author Luo_WG
     * @Date 2024/1/20 14:50
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.tms.dto.TransferDeclareDTO.ListDTO>>
     **/
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = TmsTransferDeclareQueryHandler.class)
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:transferDeclare:paging",
            tableAlias = "td"
    )
    public ApiResult<PagingVO<TransferDeclareDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<TransferDeclareDTO.PagingParamDTO> dto) {
        PagingVO<TransferDeclareDTO.ListDTO> pagingVO = transferDeclareService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 分页列表tab页
     * @Author Luo_WG
     * @Date 2024/1/20 16:54
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.tms.dto.TransferDeclareDTO.TabListDTO>>
     **/
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:transferDeclare:paging",
            tableAlias = "td"
    )
    public ApiResult<List<TransferDeclareDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<TransferDeclareDTO.TabListDTO> tabList = transferDeclareService.tabList(dto);
        return success(tabList);
    }

    /**
    * 新增
    * @author Luo_WG
    * @date:  2024-01-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中转报关表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TransferDeclareDTO.AddDTO dto) {
        return success(transferDeclareService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2024-01-19
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中转报关表修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:transferDeclare:update",
            serviceClass = TransferDeclareService.class,
            keyIdName = "id")
    public ApiResult<Object>update(@RequestBody @Validated TransferDeclareDTO.UpdateDTO dto) {
        Boolean flag = transferDeclareService.update(dto);
        if (flag) {
            //如果明细有移除需要根据明细上传状态修改主表上传状态
            List<TransferDeclareDetailEntity> detailEntities = transferDeclareDetailService.listByMainIds(Arrays.asList(dto.getId()));
            List<String> orderUploadStatusList = detailEntities.stream().map(req -> req.getOrderUploadStatus()).distinct().collect(Collectors.toList());
            if (orderUploadStatusList.contains(TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode())) {
                transferDeclareService.updateUploadStatus(dto.getId(), TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode());
            } else if (orderUploadStatusList.contains(TransferDeclareUploadStatusEnum.WAIT_UPLOAD.getCode())) {
                transferDeclareService.updateUploadStatus(dto.getId(), TransferDeclareUploadStatusEnum.WAIT_UPLOAD.getCode());
            } else {
                transferDeclareService.updateUploadStatus(dto.getId(), TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode());
            }
        }
        return flag ? success() : failure();
    }

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2024/1/23 18:07
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.tms.dto.TransferDeclareDTO.ViewDTO>
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:transferDeclare:view",
            serviceClass = TransferDeclareService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<TransferDeclareDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(transferDeclareService.view(id));
    }


    /**
     * 详情明细高级查询
     * @Author Luo_WG
     * @Date 2024/1/24 10:11
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.tms.dto.TransferDeclareDetailDTO.ViewDTO>>
     **/
    @PostMapping("/viewDetailList")
    public ApiResult<List<TransferDeclareDetailDTO.ViewDTO>> viewDetailList(@RequestBody @Validated TransferDeclareDTO.ViewDetailParamDTO dto) {
        List<TransferDeclareDetailDTO.ViewDTO> result = transferDeclareService.viewDetailList(dto);
        return success(result);
    }

    /**
     * 报关设置
     * @Author Luo_WG
     * @Date 2024/1/24 15:39
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/forcastSetting")
    public ApiResult<Object>forcastSetting(@RequestBody @Validated ValidList<TransferDeclareGenerationSettingDTO.AddDTO> dto) {
        Boolean flag = transferDeclareService.forcastSetting(dto.getList());
        return flag ? success() : failure();
    }

    /**
     * 报关设置-详情（设置后第二次点击调用）
     * @Author Luo_WG
     * @Date 2024/1/24 17:31
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO.ViewDTO>>
     **/
    @GetMapping("/forcastSettingView")
    public ApiResult<List<TransferDeclareGenerationSettingDTO.ViewDTO>> forcastSettingView() {
        List<TransferDeclareGenerationSettingDTO.ViewDTO> viewDTOList = transferDeclareService.forcastSettingView();
        return success(viewDTOList);
    }

    /**
     * 截单设置
     * @Author Luo_WG
     * @Date 2024/1/24 17:54
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/deadlineSetting")
    public ApiResult<Object>deadlineSetting(@RequestBody @Validated ValidList<TransferDeclareDeadlineSettingDTO.AddDTO> dto) {
        Boolean flag = transferDeclareService.deadlineSetting(dto.getList());
        return flag ? success() : failure();
    }

    /**
     * 截单设置-详情（设置后第二次点击调用）
     * @Author Luo_WG
     * @Date 2024/1/24 17:54
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping("/deadlineSettingView")
    public ApiResult<List<TransferDeclareDeadlineSettingDTO.ViewDTO>> deadlineSettingView() {
        List<TransferDeclareDeadlineSettingDTO.ViewDTO> viewDTOS = transferDeclareService.deadlineSettingView();
        return success(viewDTOS);
    }

    /**
     * 删除
     * @Author Luo_WG
     * @Date 2024/1/24 18:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:transferDeclare:delete",
            serviceClass = TransferDeclareService.class,
            keyIdName = "ids")
    public ApiResult<Object>delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = transferDeclareService.delete(dto.getIds());
        return flag ? success() : failure();
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2024/1/24 18:43
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出中转报关单")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Object>exportExcel(@RequestBody @Validated TransferDeclareDTO.PagingParamDTO dto) {
        Boolean flag = transferDeclareService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 入库预报
     * @Author zdy
     * @Date 2024/2/26 9:54
     * @param dtos
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogViewService
    @PostMapping(value = "/instockForecast")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:transferDeclare:instockForecast",
            tableAlias = "td"
    )
    public ApiResult<List<BatchResultDTO>> instockForecast(@RequestBody @Validated List<BaseDTO.QtyDTO> dtos) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtos.size());
        for (BaseDTO.QtyDTO qtyDTO : dtos) {
            List<BatchResultDTO> result = new ArrayList<>();
            try {
                result = transferDeclareService.instockForecast(qtyDTO);
            } catch (Exception e) {
                log.error("入库预报失败", e);
                TransferDeclareEntity entity = transferDeclareService.getById(qtyDTO.getId());
                if (ObjectUtil.isEmpty(entity)) {
                    result.add(BatchResultDTO.fail(qtyDTO.getId(), qtyDTO.getId(), "报关单不存在, 入库预报失败"));
                    resultDTOS.addAll(result);
                    continue;
                }
                result.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
            resultDTOS.addAll(result);

        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    
    /**
     * 下推分摊
     * @author Will
     * @date:  2023-11-06
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/pushAllocation")
    @LogAction(value = LogActionEnum.INSERT, desc = "下推分摊")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
    tableField = "create_user_id",
    menuCode = "tms:transferDeclare:pushAllocation",
    serviceClass = TransferDeclareService.class,
    keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> pushAllocation(@RequestBody @Validated PushDTO dto) {
   	 List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = transferDeclareService.pushAllocation(id,dto.getReportDate());
            }catch (Exception e){
                log.error("中转报关 状态变更",e);
                TransferDeclareEntity entity = transferDeclareService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "中转报关不存在, 下推分摊失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
