package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.server.sys.service.SysDepartmentService;
import com.erp.server.sys.service.SysDepartmentUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Classname SysDeptFeignController
 * @Description TODO
 * @Date 2022-12-28 15:18
 * @Created by yl
 */
@RestController
@RequestMapping("feign/dept")
public class SysDeptFeignController extends BaseController {

    @Resource
    private SysDepartmentService departmentService;

    @Resource
    private SysDepartmentUserService sysDepartmentUserService;


    @PostMapping("/getDeptIdList")
    public List<String> getMarketingCenterDeptIds(@RequestBody String deptName) {
        return departmentService.getDeptIds(deptName);
    }

    @GetMapping("/getDeptList")
    public List<SysDepartmentDTO> getDeptList() {
        return departmentService.getDeptList();
    }


    @PostMapping("/getByDeptNames")
    public List<SysUserDeptDTO> getByDeptNames(@RequestBody List<String> deptNames) {
        return departmentService.getByDeptNames(deptNames);
    }

    @PostMapping("/getDeptByUserId")
    public SysDepartmentUserNumberDTO getDeptByUserId(@RequestBody String userId) {
        SysDepartmentUserNumberDTO dto = sysDepartmentUserService.getDeptByUserId(userId);
        return dto;
    }

}
