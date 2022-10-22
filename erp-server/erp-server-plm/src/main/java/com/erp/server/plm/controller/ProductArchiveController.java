package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.ProductArchiveDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.server.plm.service.ProductArchiveService;
import com.erp.server.plm.service.ProductInfoService;
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
 * @Description TODO
 * @Date 2022-10-08 14:59
 * @Created by yl
 */
@RestController
@RequestMapping("plm/product/archive")
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
    //@RequestPermissions("plm:product:archive:paging")
    public ApiResult<PagingVO<List<ProductArchiveDTO>>> paging(@RequestBody @Validated PagingDTO<ProductSearchDTO> dto) {
        PagingVO<List<ProductArchiveDTO>> pagingVO = productArchiveService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 重新激活
     */
    @PostMapping("/activate")
    //@RequestPermissions("plm:product:archive:activate")
    public ApiResult activate(String productId) {
        boolean flag = productArchiveService.activate(productId);
        return flag==true?success():failure("激活失败");
    }
}
