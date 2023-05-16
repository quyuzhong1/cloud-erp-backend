package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.server.wms.service.TransferApplicationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 *  调拨申请单
 *
 * @author will
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/transferApplication")
public class TransferApplicationController extends BaseController {

    @Resource
    private TransferApplicationService transferApplicationService;


    /**
     * 列表查询
     * @author Will
     * @date: 2023/5/10 19:56
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:paging",
            tableAlias = "ta"
    )
    public ApiResult<PagingVO<TransferApplicationDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TransferApplicationDTO.SearchParamDTO> dto) {
        PagingVO<TransferApplicationDTO.ListDTO> pagingVO = transferApplicationService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表数量
     * @author Will
     * @date: 2023/5/10 20:08
     * @param dto
     * @return ApiResult<List<ListStatusCountDTO>>
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:paging",
            tableAlias = "ta"
    )
    public ApiResult<List<TransferApplicationDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<TransferApplicationDTO.ListStatusCountDTO> list = transferApplicationService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/5/10 19:58
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:add",
            serviceClass = TransferApplicationService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated TransferApplicationDTO.AddDTO dto) {
        String id = transferApplicationService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/5/10 19:59
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:add",
            serviceClass = TransferApplicationService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated TransferApplicationDTO.AddDTO dto) {
        String id = transferApplicationService.addAndSubmit(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:update",
            serviceClass = TransferApplicationService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated TransferApplicationDTO.UpdateDTO dto) {
        Boolean flag = transferApplicationService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:update",
            serviceClass = TransferApplicationService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated TransferApplicationDTO.UpdateDTO dto) {
        Boolean flag = transferApplicationService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/5/10 20:00
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:submit",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = transferApplicationService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/5/10 20:10
     * @param id
     * @return ApiResult
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:view",
            serviceClass = TransferApplicationService.class,
            keyIdName = "id")
    public ApiResult<TransferApplicationDTO.ViewDTO> view(@RequestParam("id") String id) {
        TransferApplicationDTO.ViewDTO dto = transferApplicationService.view(id);
        return success(dto);
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/5/10 20:09
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:delete",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = transferApplicationService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:invalid",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = transferApplicationService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/5/10 20:11
     * @param baseApproveParamDTO
     * @return ApiResult
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:approve",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        transferApplicationService.approve(baseApproveParamDTO);
        return success();
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/5/10 20:12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:disApprove",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = transferApplicationService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/5/10 20:24
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:cancelProcess",
            serviceClass = TransferApplicationService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = transferApplicationService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/5/10 20:25
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:paging",
            tableAlias = "ta"
    )
    public ApiResult exportExcel(@RequestBody TransferApplicationDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = transferApplicationService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

    /**
     * 下推直接调拨单数据显示
     * @author Will
     * @date: 2023/5/10 18:39
     * @param dto
     * @return ApiResult<List<ViewGenerateTransferInfoDTO>>
     */
    @PostMapping(value = "/viewGenerateTransferInfo")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:viewGenerateTransferInfo",
            tableAlias = "ta"
    )
    public ApiResult<List<TransferApplicationDTO.ViewGenerateTransferInfoDTO>> viewGenerateTransferInfo(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> list = transferApplicationService.viewGenerateTransferInfo(dto.getIds());
        return success(list);
    }

    /**
     * 下推直接调拨单保存
     * @author Will
     * @date: 2023/5/12 10:52
     * @param validList
     * @return ApiResult
     */
    @PostMapping("/generateTransferInfo")
    public ApiResult generateTransferInfo(@RequestBody @Validated ValidList<TransferApplicationDTO.GenerateTransferInfoDTO> validList) {
        Boolean flag = transferApplicationService.generateTransferInfo(validList);
        return flag == true ? success() : failure();
    }

    /**
     * 下推分布式调出数据显示
     * @author Will
     * @date: 2023/5/10 18:39
     * @param dto
     * @return ApiResult<List<ViewGenerateTransferInfoDTO>>
     */
    @PostMapping(value = "/viewGenerateTransferOut")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "apply_user_id",
            menuCode = "wms:transferApplication:viewGenerateTransferOut",
            tableAlias = "ta"
    )
    public ApiResult<List<TransferApplicationDTO.ViewGenerateTransferInfoDTO>> viewGenerateTransferOut(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> list = transferApplicationService.viewGenerateTransferOut(dto.getIds());
        return success(list);
    }

    /**
     * 下推分布式调出保存
     * @author Will
     * @date: 2023/5/12 10:52
     * @param validList
     * @return ApiResult
     */
    @PostMapping("/generateTransferOut")
    public ApiResult generateTransferOut(@RequestBody @Validated ValidList<TransferApplicationDTO.GenerateTransferInfoDTO> validList) {
        Boolean flag = transferApplicationService.generateTransferOut(validList);
        return flag == true ? success() : failure();
    }

    /**
     * 查询拣货明细
     * @author Will
     * @date: 2023/5/16 12:09
     * @param id
     * @return ApiResult<List<CommonDTO>>
     */
    @PostMapping("/listPickingDetail")
    public ApiResult<List<PickingDetailDTO.CommonDTO>> listPickingDetail(@RequestParam("id") String id) {
        List<PickingDetailDTO.CommonDTO> list = transferApplicationService.listPickingDetail(id);
        return success(list);
    }
}
