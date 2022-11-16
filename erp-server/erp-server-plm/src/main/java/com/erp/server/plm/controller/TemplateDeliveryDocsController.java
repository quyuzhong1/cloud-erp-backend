package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.TemplateDeliveryDocsDTO;
import com.erp.model.plm.dto.TemplateDeliveryDocsDeleteDTO;
import com.erp.model.plm.dto.TemplateDeliveryDocsShowDTO;
import com.erp.server.plm.service.TemplateDeliveryDocsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板输出物
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
     * 输出物列表查询
     *
     * @author Will
     * @date: 2022/11/15 16:11
     * @param dto
     * @return ApiResult<PagingVO<List<TemplateDeliveryDocsShowDTO>>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<List<TemplateDeliveryDocsShowDTO>>> paging(@RequestBody PagingDTO<BaseSearchDTO> dto) {
        PagingVO<List<TemplateDeliveryDocsShowDTO>> pagingVO = templateDeliveryDocsService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增或者修改
     *
     * @author Will
     * @date: 2022/11/15 16:11
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated TemplateDeliveryDocsDTO dto) {
        Boolean flag = templateDeliveryDocsService.saveOrUpdate(dto);
        return flag ? success() : failure();
    }

    /**
     * 删除输出物
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

}
