package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentTreeDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.dto.SysUserDeptDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
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


    @PostMapping("/listDepartByIds")
    public List<SysDepartmentEntity> getMarketingCenterDeptIds(@RequestBody List<String> deptIdList) {
        return departmentService.listByIdList(deptIdList);
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
     *
     * @param userIdList
     * @return java.util.List<com.erp.model.sys.dto.SysDepartmentUserNumberDTO>
     * @author yl
     * @date 2023-06-15 16:55
     */
    @PostMapping("/listDeptUserByUserIdList")
    public List<SysDepartmentUserNumberDTO> listDeptUserByUserIdList(@RequestBody List<String> userIdList) {
        List<SysDepartmentUserNumberDTO> list = sysDepartmentUserService.listDeptUserByUserIdList(userIdList);
        return list;
    }
    
    /**
     * 根据部门id集合查询部门用户关系
     * @author Will
     * @date: 2024/1/31 17:33
     * @param deptIdList 
     * @return List<SysDepartmentUserNumberDTO> 
     */
    @PostMapping("/listDeptUserByDeptIdList")
    public List<SysDepartmentUserNumberDTO> listDeptUserByDeptIdList(@RequestBody List<String> deptIdList) {
        List<SysDepartmentUserNumberDTO> list = sysDepartmentUserService.listDeptUserByDeptIdList(deptIdList);
        return list;
    }

    /**
     * @param codeList
     * @return List<SysDepartmentDTO>
     * @description: 根据编码集合查询
     * @author Will
     * @date: 2023/7/5 18:16
     */
    @PostMapping("/listDeptByCodeList")
    public List<SysDepartmentDTO> listDeptByCodeList(@RequestBody List<String> codeList) {
        List<SysDepartmentDTO> list = departmentService.listDeptByCodeList(codeList);
        return list;
    }

    /**
     * 根据用户id 获取到部门负责人
     *
     * @param userIdList
     * @return
     */
    @PostMapping("/listLeadByUserIdList")
    public List<String> listLeadByUserIdList(@RequestBody List<String> userIdList) {
        List<String> list = departmentService.listLeadByUserIdList(userIdList);
        return list;
    }


    /**
     * 根据部门名称查询最高级别部门及下级
     *
     * @param deptNameList
     * @return List<String>
     * @author Will
     * @date: 2023/9/20 18:54
     */
    @PostMapping("/listSameLevelDeptIdList")
    public List<SysDepartmentDTO> listSameLevelDeptIdList(@RequestBody List<String> deptNameList) {
        List<SysDepartmentDTO> list = departmentService.listSameLevelDeptIdList(deptNameList);
        return list;
    }

    /**
     * 根据父级获取全量子集
     *
     * @param deptId
     * @return
     */
    @GetMapping("/getDeptByParentId")
    public List<SysDepartmentTreeDTO> getDeptByParentId(@RequestParam("deptId") String deptId) {
        return departmentService.getDeptByParentId(deptId);
    }

    /**
     * 根据父级获取全量子集
     *
     * @return
     */
    @PostMapping("/getDeptByNames")
    public List<SysDepartmentEntity> getDeptByNames(@RequestBody List<String> deptNameList) {
        return departmentService.getDeptByNames(deptNameList);
    }
}
