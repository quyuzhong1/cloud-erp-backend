package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
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

     /**
      * 根据用户id 集合获取用户信息 部门信息
      * @author yl
      * @date 2023-06-15 16:55
      * @param userIdList
      * @return java.util.List<com.erp.model.sys.dto.SysDepartmentUserNumberDTO>
      */
    @PostMapping("/listDeptUserByUserIdList")
    public List<SysDepartmentUserNumberDTO> listDeptUserByUserIdList(@RequestBody List<String> userIdList) {
        List<SysDepartmentUserNumberDTO> list = sysDepartmentUserService.listDeptUserByUserIdList(userIdList);
        return list;
    }

    /**
     * @description: 根据编码集合查询
     * @author Will
     * @date: 2023/7/5 18:16
     * @param codeList
     * @return List<SysDepartmentDTO>
     */
    @PostMapping("/listDeptByCodeList")
    public List<SysDepartmentDTO> listDeptByCodeList(@RequestBody List<String> codeList) {
        List<SysDepartmentDTO> list = departmentService.listDeptByCodeList(codeList);
        return list;
    }
}
