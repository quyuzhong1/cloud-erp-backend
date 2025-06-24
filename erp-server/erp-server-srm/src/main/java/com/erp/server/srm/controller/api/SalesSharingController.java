package com.erp.server.srm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.srm.service.SalesSharingService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.srm.dto.SalesSharingDTO;

/**
 * 销量共享表
 *
 * @author jack
 * @since 2025-06-18
 */
@Slf4j
@RestController
@LogSystemModule("销量共享表")
@RequestMapping("/salesSharing")
public class SalesSharingController extends BaseController {

    @Resource
    private SalesSharingService salesSharingService;

    /**
     * 列表查询
     * @author jack
     * @date: 2025-06-13
     * @param pagingParamDTO
     * @return ApiResult<PagingVO<SalesSharingDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<SalesSharingDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SalesSharingDTO.PagingParamDTO> pagingParamDTO) {
        return success(salesSharingService.paging(pagingParamDTO));
    }

    /**
     * 获取通知内容
     * @author jack
     * @date: 2025-06-13
     * @return ApiResult<String>
     */
    @GetMapping("/getNoticeContent")
    public ApiResult<String> getNoticeContent() {
        return success(salesSharingService.getNoticeContent());
    }

    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-06-13
     * @param pagingParamDTO
     * @param response
     * @return
     */
    @PostMapping("/export")
    @WebAdvanceQuery
    public ApiResult<Object> exportList(@RequestBody @Validated SalesSharingDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        String msg = salesSharingService.exportList(pagingParamDTO, response);
        if(StringUtils.isBlank(msg)){
            return success();
        }else {
            return failure(msg);
        }
    }

    /**
     * 根据销量设置进行统计销量共享数据
     * @author jack
     * @date: 2025-06-24
     * @return ApiResult<String>
     */
    @GetMapping("/calSalesSharing")
    public ApiResult<String> calSalesSharing() {
        salesSharingService.calSalesSharing();
        return success();
    }


}
