package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.DocsShowDTO;
import com.erp.model.plm.dto.StateDTO;
import com.erp.server.plm.service.SysDocsService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import java.util.List;
import java.util.Map;

/**
 *产品系统通用设置
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/sys/docs")
public class SysDocsController extends BaseController {

    @Autowired
    private SysDocsService sysDocsService;


    /**
     *输出文档列表
     * @author yl
     * @date 2022-10-09 10:31
     * @param dto
     * @return com.erp.common.dto.base.ApiResult
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<DocsShowDTO>> paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto){
        PagingVO<DocsShowDTO> pagingVO=sysDocsService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新建输出文档 或者修改输出文档
     * @author yl
     * @date 2022-10-09 10:33
     * @param dto
     * @return com.erp.common.dto.base.ApiResult
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody DocsDTO dto){
         sysDocsService.saveOrUpdateDocs(dto);
         return success();
    }


    /**
     * 输出文档列表 修改模板状态
     * @author yl
     * @date 2022-10-09 10:33
     * @param dto
     * @return com.erp.common.dto.base.ApiResult
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated StateDTO dto){
       Boolean flag= sysDocsService.updateState(dto);
       return flag==true?success():failure();
    }

    /**
     * 获取 设置目标交付文档列表
     * @author yl
     * @date 2022-10-09 10:33

     * @return com.erp.common.dto.base.ApiResult
     */
    @GetMapping("/list")
    public ApiResult sysDocsNames(){
       List<Map<String,Object>> list= sysDocsService.sysDocsNames();
        return success(list);
    }

}

