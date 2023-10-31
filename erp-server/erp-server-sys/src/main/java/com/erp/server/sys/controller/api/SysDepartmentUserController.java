package com.erp.server.sys.controller.api;


import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.BatchSysDepartUserDTO;
import com.erp.model.sys.dto.DepartmentSearchDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.server.sys.service.SysDepartmentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * 部门员工管理
 * @Classname

 * @Date 2022-07-13 18:49
 * @Created by yl
 */
@RestController
@LogSystemModule("部门管理")
@RequestMapping("departmentUser")
public class SysDepartmentUserController extends BaseController {

    @Autowired
    private SysDepartmentUserService sysDepartmentUserService;


    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "批量保存部门人员:部门id={departmentId},用户ids={userIds}")
    @RequestMapping("/batchSave")
    public ApiResult batchSave(@RequestBody BatchSysDepartUserDTO dto) {
        boolean flag = sysDepartmentUserService.saveBatchDepartmentUser(dto);
        return flag == true ? success() : failure();
    }

    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除部门人员")
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

    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "设置部门领导:ids={ids},是否领导={state}(1=是,0=否)")
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

    
    /**
     * 根据部门id获取员工信息
     * @author yl
     * @date 2023-06-05 12:02
     * @param deptId
     * @return 
     */
    @GetMapping("/listDeptUserByDeptId")
    public ApiResult<List<FindUserDTO>> listDeptUserByDeptId(@RequestParam("deptId") String deptId) {
        List<FindUserDTO>  resultList = sysDepartmentUserService.listDeptUserByDeptId(deptId);
        return success(resultList);
    }



}
