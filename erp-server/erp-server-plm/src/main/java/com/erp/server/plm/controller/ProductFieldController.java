package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductFieldDTO;
import com.erp.model.plm.dto.StateDTO;
import com.erp.model.plm.dto.SysProductFieldPagingDTO;
import com.erp.server.plm.service.ProductFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品开发管理
 * @Classname ProductFieldController
 * @Description TODO
 * @Date 2022-10-10 18:37
 * @Created by yl
 */
@RestController
@RequestMapping("/field")
public class ProductFieldController  extends BaseController {

    @Autowired
    private ProductFieldService productFieldService;

    /**
     * 设置-字段配置 列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SysProductFieldPagingDTO>> paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<SysProductFieldPagingDTO> pagingVO = productFieldService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 修改字段状态
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated StateDTO dto) {
        Boolean flag = productFieldService.updateState(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 设置-字段配置 新增加字段
     * @param dto
     * @return
     */
    @PostMapping("/save")
    public ApiResult  save(@RequestBody @Validated ProductFieldDTO dto) {
        Boolean flag= productFieldService.saveField(dto);
        return flag==true?success():failure();
    }
}
