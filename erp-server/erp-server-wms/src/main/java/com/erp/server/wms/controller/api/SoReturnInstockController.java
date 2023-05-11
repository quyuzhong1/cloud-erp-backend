package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.server.wms.service.SoReturnInstockService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 销售退货入库单
 * @author LUO_WG
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/soReturnInstock")
public class SoReturnInstockController extends BaseController {

    @Resource
    private SoReturnInstockService soReturnInstockService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnInstockDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "seller_id",
            menuCode = "wms:soReturnInstockService:paging",
            tableAlias = "srr"
    )
    public ApiResult<PagingVO<SoReturnInstockDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<SoReturnInstockDTO.PagingParam> dto) {
        PagingVO<SoReturnInstockDTO.PagingView> pagingVO = soReturnInstockService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoReturnInstockDTO.soDeliveryNoticeCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            menuCode = "wms:soReturnInstockService:listCount",
            tableAlias = "srr"
    )
    public ApiResult<List<SoReturnInstockDTO.StatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoReturnInstockDTO.StatusCountDTO> soDeliveryNoticeCountDTOS = soReturnInstockService.listCount(dto);
        return success(soDeliveryNoticeCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoReturnInstockDTO.Add dto) {
        String id = soReturnInstockService.add(dto);
        return StringUtils.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soReturnInstockService:update",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoReturnInstockDTO.Update dto) {
        Boolean flag = soReturnInstockService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoReturnInstockDTO.ViewDTO>
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "receive_user_id",
            menuCode = "wms:soReturnInstockService:view",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult<SoReturnInstockDTO.View> view(@RequestParam("id") String id) {
        SoReturnInstockDTO.View dto = soReturnInstockService.view(id);
        return success(dto);
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soReturnInstockService:submit",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnInstockService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soReturnInstockService:add",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SoReturnInstockDTO.Add dto) {
        Boolean flag = soReturnInstockService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soReturnInstockService:update",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoReturnInstockDTO.Update dto) {
        Boolean flag = soReturnInstockService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soReturnInstockService:approve",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = soReturnInstockService.approve(baseApproveParamDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soReturnInstockService:disApprove",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnInstockService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soReturnInstockService:cancelProcess",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnInstockService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soReturnInstockService:invalid",
            serviceClass = SoReturnInstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = soReturnInstockService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = soReturnInstockService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @param response response
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            menuCode = "wms:soReturnInstockService:paging",
            tableAlias = "srr"
    )
    public ApiResult exportExcel(@RequestBody SoReturnInstockDTO.PagingParam dto, HttpServletResponse response) {
        Boolean flag = soReturnInstockService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

    /**
     * 下推退货入库单-保存
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/generateSoReturnInstockSave")
    public ApiResult generateSoReturnInstockSave(@RequestBody SoReturnReceiveDTO.ListGenerateSoReturnInstockView dto) {
        Boolean flag = soReturnInstockService.generateSoReturnReceiveSave(dto.getList());
        return flag == true ? success() : failure();
    }
}
