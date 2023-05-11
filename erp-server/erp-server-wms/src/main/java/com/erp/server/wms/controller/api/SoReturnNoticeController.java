package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.server.wms.service.SoReturnNoticeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 退货通知单
 * @author LUO_WG
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/soReturnNotice")
public class SoReturnNoticeController extends BaseController {
    @Resource
    private SoReturnNoticeService soReturnNoticeService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnNoticeDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "seller_id",
            menuCode = "wms:soReturnNotice:paging",
            tableAlias = "sdn"
    )
    public ApiResult<PagingVO<SoReturnNoticeDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<SoReturnNoticeDTO.PagingParam> dto) {
        PagingVO<SoReturnNoticeDTO.PagingView> pagingVO = soReturnNoticeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoReturnNoticeDTO.soDeliveryNoticeCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            menuCode = "wms:soReturnNotice:listCount",
            tableAlias = "sdn"
    )
    public ApiResult<List<SoReturnNoticeDTO.StatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoReturnNoticeDTO.StatusCountDTO> soDeliveryNoticeCountDTOS = soReturnNoticeService.listCount(dto);
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
    public ApiResult add(@RequestBody @Validated SoReturnNoticeDTO.Add dto) {
        String id = soReturnNoticeService.add(dto);
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
            menuCode = "wms:soReturnNotice:update",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoReturnNoticeDTO.Update dto) {
        Boolean flag = soReturnNoticeService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoReturnNoticeDTO.ViewDTO>
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "receive_user_id",
            menuCode = "wms:soReturnNotice:view",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "id")
    public ApiResult<SoReturnNoticeDTO.View> view(@RequestParam("id") String id) {
        SoReturnNoticeDTO.View dto = soReturnNoticeService.view(id);
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
            menuCode = "wms:soReturnNotice:submit",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnNoticeService.submit(dto.getIds());
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
            menuCode = "wms:soReturnNotice:add",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SoReturnNoticeDTO.Add dto) {
        Boolean flag = soReturnNoticeService.addAndSubmit(dto);
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
            menuCode = "wms:soReturnNotice:update",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoReturnNoticeDTO.Update dto) {
        Boolean flag = soReturnNoticeService.updateAndSubmit(dto);
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
            menuCode = "wms:soReturnNotice:approve",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = soReturnNoticeService.approve(baseApproveParamDTO);
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
            menuCode = "wms:soReturnNotice:disApprove",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnNoticeService.disApprove(dto.getIds());
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
            menuCode = "wms:soReturnNotice:cancelProcess",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnNoticeService.cancelProcess(dto.getIds());
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
            menuCode = "wms:soReturnNotice:invalid",
            serviceClass = SoReturnNoticeService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = soReturnNoticeService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
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
        Boolean flag = soReturnNoticeService.delete(idsDTO.getIds());
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
            menuCode = "wms:soReturnNotice:paging",
            tableAlias = "sdn"
    )
    public ApiResult exportExcel(@RequestBody SoReturnNoticeDTO.PagingParam dto, HttpServletResponse response) {
        Boolean flag = soReturnNoticeService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

    /**
     * 下推退货通知单-保存
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/generateSoReturnNoticeSave")
    public ApiResult generateSoReturnNoticeSave(@RequestBody SoReturnDTO.ListGenerateSoReturnNoticeView dto) {
        Boolean flag = soReturnNoticeService.generateSoReturnNoticeSave(dto.getList());
        return flag == true ? success() : failure();
    }

    /**
     * 下推退货签收单-列表查询
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/generateSoDeliveryView")
    public ApiResult<List<SoReturnNoticeDTO.GenerateSoReturnReceiveView>> generateSoDeliveryView(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> generateSoDeliveryViews = soReturnNoticeService.generateSoDeliveryView(dto.getIds());
        return success(generateSoDeliveryViews);
    }
}
