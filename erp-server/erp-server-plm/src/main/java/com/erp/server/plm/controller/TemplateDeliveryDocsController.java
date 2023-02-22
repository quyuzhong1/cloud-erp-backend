package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.server.plm.service.TemplateDeliveryDocsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板管理
 *
 * @author Will
 * @version 1.0
 * @date 2022/11/14 17:31
 */
@RestController
@RequestMapping("/plm/templateDeliveryDocs")
public class TemplateDeliveryDocsController extends BaseController {

    @Autowired
    private TemplateDeliveryDocsService templateDeliveryDocsService;

    /**
     * 模板详情-输出物-列表分页查询
     *
     * @author Will
     * @date: 2022/11/15 16:11
     * @param dto
     * @return ApiResult<PagingVO<List<TemplateDeliveryDocsShowDTO>>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<TemplateDeliveryDocsShowDTO>>> paging(@RequestBody PagingDTO<TemplateSearchDTO> dto) {
        PagingVO<List<TemplateDeliveryDocsShowDTO>> pagingVO = templateDeliveryDocsService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 模板详情-输出物-删除
     *
     * @author Will
     * @date: 2022/11/14 14:58
     * @param dto
     * @return ApiResult
     */
    @DeleteMapping("/delete")
    public ApiResult delete(@RequestBody TemplateDeliveryDocsDeleteDTO dto) {
        Boolean flag = templateDeliveryDocsService.deleteTemplateDeliveryDocs(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 模板详情-输出物-修改状态
     *
     * @author Will
     * @date: 2022/11/17 9:33
     * @param dto
     * @return ApiResult
     */
    @PutMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated TemplateDeliveryDocsUpdateStatusDTO dto) {
        Boolean flag = templateDeliveryDocsService.updateStatus(dto);
        return flag ? success() : failure();
    }

}
