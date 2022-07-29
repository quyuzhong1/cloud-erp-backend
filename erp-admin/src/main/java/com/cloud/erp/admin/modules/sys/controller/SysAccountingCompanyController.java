package com.cloud.erp.admin.modules.sys.controller;

import com.cloud.erp.admin.modules.sys.dto.SysAccountingCompanyDTO;
import com.cloud.erp.admin.modules.sys.service.SysAccountingCompanyService;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.*;
import com.erp.common.vo.PagingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Classname SysAccountingCompanyController
 * @Description TODO
 * @Date 2022-07-12 9:39
 * @Created by yl
 */
@RestController
@RequestMapping("sys/company")
public class SysAccountingCompanyController extends BaseController {

    @Autowired
    private SysAccountingCompanyService sysAccountingCompanyService;

    /**
     * 分页列表
     */
    @RequestMapping("/paging")
    public ApiResult list(@RequestBody @Validated PagingDTO<BasePagingSearchDTO> dto) {
        PagingVO pagingVO = sysAccountingCompanyService.paging(dto);
        return success(pagingVO);
    }

    //添加公司
    @RequestMapping("/save")
    public ApiResult save(@RequestBody @Validated SysAccountingCompanyDTO dto) {
        boolean flag = sysAccountingCompanyService.saveCompany(dto);
        return flag == true ? success() : failure();
    }

    //修改公司
    @RequestMapping("/update")
    public ApiResult update(@RequestBody @Validated SysAccountingCompanyDTO dto) {
        boolean flag = sysAccountingCompanyService.updateCompany(dto);
        return flag == true ? success() : failure();
    }

    //更改状态 禁用或者启用
    @RequestMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated StateDTO dto) {
        boolean flag = sysAccountingCompanyService.updateCompanyState(dto);
        return flag == true ? success() : failure();
    }


    //更改状态 禁用或者启用
    @RequestMapping("/batchUpdateState")
    public ApiResult batchUpdateState(@RequestBody @Validated BatchStateDTO  dto) {
        boolean flag = sysAccountingCompanyService.batchUpdateCompanyState(dto);
        return flag == true ? success() : failure();
    }


    //更改状态 禁用或者启用
    @RequestMapping("/delete")
    public ApiResult delete(@RequestBody @Validated List<String> ids) {
        sysAccountingCompanyService.removeByIds(ids);
        return success();
    }
}
