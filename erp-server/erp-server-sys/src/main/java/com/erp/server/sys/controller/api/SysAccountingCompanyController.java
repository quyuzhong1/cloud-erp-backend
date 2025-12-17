package com.erp.server.sys.controller.api;


import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.CompanyPagingSearchDTO;
import com.erp.model.sys.dto.SysAccountingCompanyDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.server.sys.service.SysAccountingCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织管理
 * @author Administrator
 * @Classname SysAccountingCompanyController

 * @Date 2022-07-12 9:39
 * @Created by yl
 */
@RestController
@LogSystemModule("核算公司")
@RequestMapping("company")
public class SysAccountingCompanyController extends BaseController {

    @Autowired
    private SysAccountingCompanyService sysAccountingCompanyService;

    /**
     * 分页列表
     */
    @RequestMapping("/paging")
    public ApiResult<PagingVO<SysAccountingCompanyEntity>> list(@RequestBody @Validated PagingDTO<CompanyPagingSearchDTO> dto) {
        PagingVO<SysAccountingCompanyEntity> pagingVO = sysAccountingCompanyService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加公司
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加公司")
    @RequestMapping("/save")
    public ApiResult save(@RequestBody @Validated SysAccountingCompanyDTO dto) {
        boolean flag = sysAccountingCompanyService.saveCompany(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改公司
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改公司")
    @RequestMapping("/update")
    public ApiResult update(@RequestBody @Validated SysAccountingCompanyDTO dto) {
        boolean flag = sysAccountingCompanyService.updateCompany(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 模块详情
     */
    @LogViewService
    @PostMapping("/view")
    public ApiResult<SysAccountingCompanyEntity> view(@RequestBody @Validated BaseIdDTO dto) {
        SysAccountingCompanyEntity result = sysAccountingCompanyService.view(dto.getId());
        return success(result);
    }


    /**
     * 更改状态 禁用或者启用
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更改禁用或者启用公司:id={id},状态值={state}(true=禁用,false=启用)")
    @RequestMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated UpdateStateDTO dto) {
        boolean flag = sysAccountingCompanyService.updateCompanyState(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 更改状态 禁用或者启用
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "批量更改禁用或者启用公司:ids={ids},状态值={state}(true=禁用,false=启用)")
    @RequestMapping("/batchUpdateState")
    public ApiResult batchUpdateState(@RequestBody @Validated BatchStateDTO dto) {
        boolean flag = sysAccountingCompanyService.batchUpdateCompanyState(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 更改状态 禁用或者启用
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除公司")
    @RequestMapping("/delete")
    public ApiResult delete(@RequestBody @Validated List<String> ids) {
        List<BatchResultDTO> resultDTOList=sysAccountingCompanyService.delete(ids);
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }

    /**
     * 获取组织列表
     *
     * @return
     * @author yl
     * @date 2023-03-21 17:3
     */
    @GetMapping("/list")
    public ApiResult<List<SysAccountingCompanyDTO.ListDTO>> list() {
        List<SysAccountingCompanyDTO.ListDTO> list = sysAccountingCompanyService.getList();
        return success(list);

    }

}
