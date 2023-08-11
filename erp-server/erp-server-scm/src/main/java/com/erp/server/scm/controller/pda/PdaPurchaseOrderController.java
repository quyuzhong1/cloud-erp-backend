package com.erp.server.scm.controller.pda;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.*;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import com.erp.server.scm.service.PurchaseOrderService;
import org.apache.ibatis.annotations.Param;
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
import java.util.List;

/**
 * 采购订单管理
 * @author will
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/pdaPurchaseOrder")
public class PdaPurchaseOrderController extends BaseController {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    /**
     * 查询数量
     * @author Will
     * @date: 2023/3/15 17:34
     * @return ApiResult
     */
    @GetMapping("/listPoBySkuNo")
    public ApiResult<List<PurchaseOrderDTO.PdaPurchaseOrder>> listPoBySkuNo(@RequestParam("skuNo") String skuNo) {
        List<PurchaseOrderDTO.PdaPurchaseOrder> list = purchaseOrderService.listPoBySkuNo(skuNo);
        return success(list);
    }

}
