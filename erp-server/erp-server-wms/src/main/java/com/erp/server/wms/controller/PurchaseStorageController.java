package com.erp.server.wms.controller;


import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PurchaseStorageDTO;
import com.erp.server.wms.service.PurchaseStorageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 采购入库单 前端控制器
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@RestController
@RequestMapping("/purchaseStorage")
public class PurchaseStorageController extends BaseController {

    @Resource
    private PurchaseStorageService purchaseStorageService;


    /**
     * 仓库分页列表
     *
     * @param
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PurchaseStorageDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PurchaseStorageDTO.SearchParamDTO> dto) {
        return null;
    }

    /**
     * 添加仓库
     *
     * @param
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PurchaseStorageDTO.AddDTO dto) {
        return null;
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated PurchaseStorageDTO.AddDTO dto) {
        return null;
    }

    /**
     * 仓库提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return null;
    }
}
