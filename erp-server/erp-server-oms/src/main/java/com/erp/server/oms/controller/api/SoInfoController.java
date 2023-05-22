package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.AddGroup;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

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


    /**
     * 获取 tab列表
     *
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<SoInfoDTO.TabListDTO>> tabList() {
        List<SoInfoDTO.TabListDTO> tabList = soDetailService.tabList();
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
            tableField = "create_user_id",
            menuCode = "oms:so:paging",
            tableAlias = "ci"
    )
    public ApiResult<PagingVO<SoInfoDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoInfoDTO.PagingParamDTO> dto) {
        PagingVO<SoInfoDTO.PagingViewDTO> pagingVO = soInfoService.paging(dto);
        return success(pagingVO);
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:add",
            serviceClass = SoInfoService.class,
            keyIdName = "id"
    )
    public ApiResult add(@RequestBody @Validated({AddGroup.class}) SoInfoDTO.AddDTO dto) {
        String id = soInfoService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 批量提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:submit",
            serviceClass = SoInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soInfoService.submit(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:addAndSubmit",
            serviceClass = SoInfoService.class,
            keyIdName = "id"
    )
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
            tableField = "create_user_id",
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
            tableField = "create_user_id",
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
            tableField = "create_user_id",
            menuCode = "oms:so:updateAndSubmit",
            serviceClass = SoInfoService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated SoInfoDTO.UpdateDTO dto) {
        Boolean result = soInfoService.updateAndSubmit(dto);
        return result ? success() : failure();
    }


    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
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
            tableField = "create_user_id",
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
            tableField = "create_user_id",
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
            tableField = "create_user_id",
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
            tableField = "create_user_id",
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
            tableField = "create_user_id",
            menuCode = "oms:so:exportSoContractPdf",
            serviceClass = SoInfoService.class,
            keyIdName = "id")
    public ApiResult<SoInfoDTO.ExportPdfDTO> exportSoContractPdf(@RequestParam("id") String id) {
        SoInfoDTO.ExportPdfDTO result = soInfoService.exportSoContractPdf(id);
        return success(result);
    }





    /**
     * 下推备货申请单数据显示
     * @author Will
     * @date: 2023/5/18 19:33
     * @param dto
     * @return ApiResult<List<AddDetailView>>
     */
    @PostMapping("/viewGenerateSalesDemand")
    public ApiResult<List<SoInfoDTO.ViewGenerateSalesDemandDTO>> viewGenerateSalesDemand(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SoInfoDTO.ViewGenerateSalesDemandDTO> list = soInfoService.viewGenerateSalesDemand(dto.getIds());
        return success(list);
    }


}
