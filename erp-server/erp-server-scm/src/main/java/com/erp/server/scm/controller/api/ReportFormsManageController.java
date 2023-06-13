package com.erp.server.scm.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 报表管理
 * @Author Luo_WG
 * @Date 2023/6/12 17:31
 **/
@RestController
@RequestMapping("/ReportFormsManage")
public class ReportFormsManageController extends BaseController {

    /**
     * 采购业务汇总表
     * @Author Luo_WG
     * @Date 2023/6/12 18:24
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO.PagingViewDTO>>
     **/
    @PostMapping(value = "/purchaseBusinessGatherTable")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:ReportFormsManage:purchaseBusinessGatherTable",
            tableAlias = "pod")
    public ApiResult<List<PurchaseBusinessGatherTableDTO.PagingViewDTO>> purchaseBusinessGatherTable(@RequestBody PurchaseBusinessGatherTableDTO.PagingParamDTO dto) {
        return null;
    }

    /**
     * 导出采购业务汇总表
     * @Author Luo_WG
     * @Date 2023/6/12 18:27
     * @param dto
     * @param response
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/exportExcelPurchaseBusiness")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "scm:ReportFormsManage:exportExcelPurchaseBusiness",
            tableAlias = "pod")
    public ApiResult exportExcelPurchaseBusiness(@RequestBody PurchaseBusinessGatherTableDTO.PagingParamDTO dto, HttpServletResponse response) {
        Boolean flag = Boolean.TRUE;
        return flag == true ? success() : failure();
    }
}
