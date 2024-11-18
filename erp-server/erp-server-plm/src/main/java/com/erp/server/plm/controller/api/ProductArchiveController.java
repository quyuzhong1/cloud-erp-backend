package com.erp.server.plm.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.ProductArchiveDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.server.plm.service.ProductArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产品归档管理
 *
 * @Classname

 * @Date 2022-10-08 14:59
 * @Created by yl
 */
@RestController
@LogSystemModule("产品归档管理")
@RequestMapping("product/archive")
public class ProductArchiveController extends BaseController {
    @Autowired
    private ProductArchiveService productArchiveService;

    /**
     * 分页获取
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:product:archive:paging", tableAlias = "pt")
    public ApiResult<PagingVO<ProductArchiveDTO>> paging(@RequestBody @Validated PagingDTO<ProductSearchDTO.PagingParamDTO> dto) {
        PagingVO<ProductArchiveDTO> pagingVO = productArchiveService.paging(dto);

        return success(pagingVO);
    }

    /**
     * 重新激活
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "重新激活：id={productId}")
    @PostMapping("/activate")
    public ApiResult<Object> activate(String productId) {
        boolean flag = productArchiveService.activate(productId);
        return flag==true?success():failure("激活失败");
    }
}
