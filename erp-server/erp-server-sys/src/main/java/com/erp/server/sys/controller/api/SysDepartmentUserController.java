package com.erp.server.sys.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.BatchSysDepartUserDTO;
import com.erp.model.sys.dto.DepartmentSearchDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.server.sys.service.SysDepartmentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Classname SysDepartmentUserContrller
 * @Description TODO
 * @Date 2022-07-13 18:49
 * @Created by yl
 */
@RestController
@RequestMapping("departmentUser")
public class SysDepartmentUserController extends BaseController {

    @Autowired
    private SysDepartmentUserService sysDepartmentUserService;


    @RequestMapping("/batchSave")
    public ApiResult batchSave(@RequestBody BatchSysDepartUserDTO dto) {
        boolean flag = sysDepartmentUserService.saveBatchDepartmentUser(dto);
        return flag == true ? success() : failure();
    }

    @RequestMapping("/remove")
    public ApiResult remove(@RequestBody List<String> ids) {
        boolean flag = sysDepartmentUserService.removeByIds(ids);
        return flag == true ? success() : failure();
    }

    @RequestMapping("/list")
    public ApiResult list(@RequestBody @Validated PagingDTO<DepartmentSearchDTO> dto) {
        PagingVO pagingVO = sysDepartmentUserService.findDepartmentUser(dto);
        return success(pagingVO);
    }

    @RequestMapping("/setLead")
    public ApiResult setLead(@RequestBody UpdateUserStateDTO dto) {
        sysDepartmentUserService.setLead(dto);
        return success();
    }

    /**
     * 根据人员id查询部门
     * @author Will
     * @date: 2023/3/27 12:10
     * @param userId
     * @return ApiResult
     */
    @GetMapping("/getDeptByUserId")
    public ApiResult getDeptByUserId(@RequestParam("userId") String userId) {
        SysDepartmentUserNumberDTO dto = sysDepartmentUserService.getDeptByUserId(userId);
        return success(dto);
    }


}
