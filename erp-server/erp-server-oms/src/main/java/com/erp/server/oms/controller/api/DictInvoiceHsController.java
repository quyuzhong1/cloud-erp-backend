package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.DictInvoiceHsDTO;
import com.erp.model.tms.dto.DictHsCodeDTO;
import com.erp.rpc.tms.feign.DictHsCodeFeign;
import com.erp.server.oms.service.DictInvoiceHsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 发票海关编码
 *
 * @author will
 * @since 2025-04-07
 */
@Slf4j
@RestController
@LogSystemModule("发票海关编码")
@RequestMapping("/dictInvoiceHs")
public class DictInvoiceHsController extends BaseController {

    @Resource
    private DictInvoiceHsService dictInvoiceHsService;
    @Resource
    private DictHsCodeFeign dictHsCodeFeign;

   /**
    * 分页查询
    * @author will
    * @date 2025/4/7 18:02
    * @param dto 
    * @return DictInvoiceHsDTO.ListDTO
    */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<DictHsCodeDTO.ListBRDTO>> paging(@RequestBody @Validated PagingDTO<DictHsCodeDTO.PagingParamDTO> dto) {
        return success(dictHsCodeFeign.pagingByBR(dto));
    }
}
