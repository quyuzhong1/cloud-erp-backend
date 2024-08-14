package com.erp.server.wms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO;
import com.erp.server.wms.service.ReportFormsManageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 报表管理
 * @Author Luo_WG
 * @Date 2023/6/12 17:31
 **/
@RestController
@LogSystemModule("采购业务汇总表")
@RequestMapping("/reportFormsManage")
public class ReportFormsManageController extends BaseController {
    @Resource
    private ReportFormsManageService reportFormsManageService;

    /**
     * 采购业务汇总表列表查询
     * @Author Luo_WG
     * @Date 2023/6/12 18:24
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO.PagingViewDTO>>
     **/
    @PostMapping(value = "/purchaseBusinessGatherTablePaging")
    public ApiResult<PagingVO<List<PurchaseBusinessGatherTableDTO.PagingViewDTO>>> purchaseBusinessGatherTablePaging(@RequestBody PagingDTO<PurchaseBusinessGatherTableDTO.PagingParamDTO> dto) {
        PagingVO<List<PurchaseBusinessGatherTableDTO.PagingViewDTO>> listPagingVO = reportFormsManageService.purchaseBusinessGatherTablePaging(dto);
        return success(listPagingVO);
    }

    /**
     * 导出采购业务汇总表
     * @Author Luo_WG
     * @Date 2023/6/12 18:27
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出采购业务汇总表")
    @PostMapping(value = "/exportExcelPurchaseBusiness")
    public ApiResult exportExcelPurchaseBusiness(@RequestBody PurchaseBusinessGatherTableDTO.PagingParamDTO dto) {
        Boolean flag = reportFormsManageService.exportExcelPurchaseBusiness(dto);
        return flag == true ? success() : failure();
    }
}
