package com.erp.server.scm.controller;


import com.common.business.dto.base.BaseAuditParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.ScmSalesDemandDTO;
import com.erp.model.scm.dto.ScmSalesDemandViewDTO;
import com.erp.model.scm.dto.ScmSalesDemandSearchDTO;
import com.erp.server.scm.service.ScmSalesDemandService;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售需求主表 前端控制器
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@RestController
@RequestMapping("/salesDemand")
public class ScmSalesDemandController extends BaseController {

    @Resource
   private ScmSalesDemandService scmSalesDemandService;

   /**
    * 分页查询
    * @author Will
    * @date: 2023/3/15 16:47
    * @param dto
    * @return ApiResult<PagingVO<List<ScmSalesDemandDTO>>>
    */
   @PostMapping("/paging")
    public ApiResult<PagingVO<List<ScmSalesDemandViewDTO>>> queryByPage(@RequestBody @Validated PagingDTO<ScmSalesDemandSearchDTO> dto) {
        PagingVO<List<ScmSalesDemandViewDTO>> pagingVO = scmSalesDemandService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增或修改
     * @author Will
     * @date: 2023/3/15 17:34
     * @param scmSalesDemandDTO
     * @return ApiResult
     */
    @PostMapping("/addOrUpdate")
    public ApiResult addOrUpdate(@RequestBody @Validated ScmSalesDemandDTO scmSalesDemandDTO) {
        Boolean flag = scmSalesDemandService.addOrUpdateScmSalesDemand(scmSalesDemandDTO);
        return flag == true ? success() : failure();
    }


    /**
     * 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ApiResult<ScmSalesDemandDTO>
     */
    @GetMapping("/view")
    public ApiResult<ScmSalesDemandDTO> view(@Param("id") String id) {
        ScmSalesDemandDTO scmSalesDemandDTO = scmSalesDemandService.viewScmSalesDemand(id);
        return success(scmSalesDemandDTO);
    }

    /**
     * 删除
     * @author Will
     * @date: 2023/3/15 17:47
     * @param id
     * @return ApiResult
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestParam("id") String id) {
        Boolean flag = scmSalesDemandService.removeById(id);
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @author Will
     * @date: 2023/3/15 17:50
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/invalid")
    public ApiResult invalid(@RequestParam("ids") List<String> ids) {
        Boolean flag = scmSalesDemandService.invalid(ids);
        return flag == true ? success() : failure();
    }

    /**
     * 审核
     * @author Will
     * @date: 2023/3/15 17:54
     * @param baseAuditParamDTO
     * @return ApiResult
     */
    @PostMapping("/audit")
    public ApiResult audit(@RequestBody @Validated BaseAuditParamDTO baseAuditParamDTO) {
        scmSalesDemandService.audit(baseAuditParamDTO);
        return success();
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/3/15 17:50
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/unAudit")
    public ApiResult unAudit(@RequestParam("ids") List<String> ids) {
        Boolean flag = scmSalesDemandService.unAudit(ids);
        return flag == true ? success() : failure();
    }


    /**
     * 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param id
     * @return ApiResult
     */
    @PostMapping("/cancelProcess")
    public ApiResult cancelProcess(@RequestParam("id") String id) {
        Boolean result = scmSalesDemandService.cancelProcess(id);
        return result == true ? success() : failure();
    }


    /**
     * 导出
     * @author Will
     * @date: 2023/3/15 18:01
     * @param scmSalesDemandSearchDTO
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody ScmSalesDemandSearchDTO scmSalesDemandSearchDTO, HttpServletResponse response) {
        Boolean flag = scmSalesDemandService.exportExcel(scmSalesDemandSearchDTO, response);
        return flag == true ? success() : failure();
    }


}
