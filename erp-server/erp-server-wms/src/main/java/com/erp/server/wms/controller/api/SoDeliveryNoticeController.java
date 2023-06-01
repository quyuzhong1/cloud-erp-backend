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
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.server.wms.service.SoDeliveryNoticeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 销售发货通知单
 * @author LUO_WG
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/soDeliveryNotice")
public class SoDeliveryNoticeController extends BaseController {
    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    /**
     * 列表查询
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO < com.erp.model.wms.dto.soDeliveryNoticeDTO.PagingViewDTO>>
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "seller_id",
            menuCode = "wms:soDeliveryNotice:paging",
            tableAlias = "sdn"
    )
    public ApiResult<PagingVO<SoDeliveryNoticeDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<SoDeliveryNoticeDTO.PagingParam> dto) {
        PagingVO<SoDeliveryNoticeDTO.PagingView> pagingVO = soDeliveryNoticeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.wms.dto.soDeliveryNoticeDTO.soDeliveryNoticeCountDTO>>
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "seller_id",
            menuCode = "wms:soDeliveryNotice:paging",
            tableAlias = "sdn"
    )
    public ApiResult<List<SoDeliveryNoticeDTO.StatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoDeliveryNoticeDTO.StatusCountDTO> soDeliveryNoticeCountDTOS = soDeliveryNoticeService.listCount(dto);
        return success(soDeliveryNoticeCountDTOS);
    }

    /**
     * 新增
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     **/
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoDeliveryNoticeDTO.Add dto) {
        String id = soDeliveryNoticeService.add(dto);
        return StringUtils.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     **/
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soDeliveryNotice:update",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoDeliveryNoticeDTO.Update dto) {
        Boolean flag = soDeliveryNoticeService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     *
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.soDeliveryNoticeDTO.ViewDTO>
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "receive_user_id",
            menuCode = "wms:soDeliveryNotice:view",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "id")
    public ApiResult<SoDeliveryNoticeDTO.View> view(@RequestParam("id") String id) {
        SoDeliveryNoticeDTO.View dto = soDeliveryNoticeService.view(id);
        return success(dto);
    }

    /**
     * 提交
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     **/
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soDeliveryNotice:submit",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soDeliveryNoticeService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     **/
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soDeliveryNotice:add",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SoDeliveryNoticeDTO.Add dto) {
        Boolean flag = soDeliveryNoticeService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     **/
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soDeliveryNotice:update",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoDeliveryNoticeDTO.Update dto) {
        Boolean flag = soDeliveryNoticeService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     *
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     **/
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soDeliveryNotice:approve",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = soDeliveryNoticeService.approve(baseApproveParamDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 批量反审核
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soDeliveryNotice:disApprove",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soDeliveryNoticeService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 取消流程
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     **/
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soDeliveryNotice:cancelProcess",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soDeliveryNoticeService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     *
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "wms:soDeliveryNotice:invalid",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = soDeliveryNoticeService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     *
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = soDeliveryNoticeService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 导出
     *
     * @param dto      dto
     * @param response response
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     **/
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody SoDeliveryNoticeDTO.PagingParam dto, HttpServletResponse response) {
        Boolean flag = soDeliveryNoticeService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

    /**
     * 下推销售出库单-保存
     *
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:5
     **/
    @PostMapping(value = "/generateSoDeliverySave")
    public ApiResult generateSoDeliverySave(@RequestBody BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = soDeliveryNoticeService.generateSoDeliverySave(idsDTO.getIds());
        return flag ? success() : failure();
    }

    /**
     * 下推发货通知单-保存
     * @Author Luo_WG
     * @Date 2023/5/25 12:30
     * @param validList validList
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/generateDeliverySave")
    public ApiResult generateDeliverySave(@RequestBody @Validated ValidList<SoInfoDTO.GenerateDeliveryView> validList) {
        Boolean flag = soDeliveryNoticeService.generateDeliverySave(validList.getList());
        return flag ? success() : failure();
    }

    /**
     * 销售单详情-单据关联-发货通知单
     * @Author Luo_WG
     * @Date 2023/5/25 16:36
     * @param soId
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoDeliveryNoticeDTO.PagingView>>
     **/
    @GetMapping("/listSoDeliveryNoticeBySourceId")
    public ApiResult<List<SoDeliveryNoticeDTO.PagingView>> listSoDeliveryNoticeBySourceId(@RequestParam("soId") String soId) {
        List<SoDeliveryNoticeDTO.PagingView> list = soDeliveryNoticeService.listSoReturnDetailBySourceId(soId);
        return success(list);
    }
}

