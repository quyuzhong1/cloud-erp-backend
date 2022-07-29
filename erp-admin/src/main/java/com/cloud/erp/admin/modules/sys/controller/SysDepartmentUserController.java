package com.cloud.erp.admin.modules.sys.controller;

import com.cloud.erp.admin.modules.sys.dto.DepartmentSearchDTO;
import com.cloud.erp.admin.modules.sys.dto.UpdateUserStateDTO;
import com.cloud.erp.admin.modules.sys.entity.SysDepartmentUserEntity;
import com.cloud.erp.admin.modules.sys.service.SysDepartmentUserService;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.ApiResult;
import com.erp.common.dto.PagingDTO;
import com.erp.common.vo.PagingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * @Classname SysDepartmentUserContrller
 * @Description TODO
 * @Date 2022-07-13 18:49
 * @Created by yl
 */
@RestController
@RequestMapping("sys/departmentUser")
public class SysDepartmentUserController  extends BaseController {

    @Autowired
    private SysDepartmentUserService sysDepartmentUserService;


    @RequestMapping("/batchSave")
    public ApiResult batchSave(@RequestBody Set<SysDepartmentUserEntity> list){
        boolean flag= sysDepartmentUserService.saveBatchDepartmentUser(list);
        return flag==true?success():failure();
    }

    @RequestMapping("/remove")
    public ApiResult remove(@RequestBody List<String> ids){
        boolean flag= sysDepartmentUserService.removeByIds(ids);
        return flag==true?success():failure();
    }

    @RequestMapping("/list")
    public ApiResult list(@RequestBody @Validated PagingDTO<DepartmentSearchDTO> dto){
        PagingVO pagingVO = sysDepartmentUserService.findDepartmentUser(dto);
        return success(pagingVO);
    }
    @RequestMapping("/setLead")
    public ApiResult setLead(@RequestBody UpdateUserStateDTO dto){
         sysDepartmentUserService.setLead(dto);
        return success();
    }

}
