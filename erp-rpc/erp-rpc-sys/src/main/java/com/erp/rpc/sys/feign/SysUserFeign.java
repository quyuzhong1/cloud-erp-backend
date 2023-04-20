package com.erp.rpc.sys.feign;


import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @Classname 系统管理 服务
 * @Description TODO
 * @Date 2022-07-08 16:52
 * @Created by yl
 */
@FeignClient(name = "erp-sys")
public interface SysUserFeign {

    /**
     *  账号登录
     */
    @PostMapping("feign/user/accountLogin")
    ApiResult<SysUserDTO> accountLogin(@RequestBody AccountLoginDTO loginDTO);

    /**
     * 设置登录ip账号登录
     */
    @PostMapping("feign/user/setLoginIp")
    ApiResult<SysUserDTO> setLoginIp(@RequestBody SysLoginIpDTO loginDTO);

    /**
     * 扫码登录
     */
    @PostMapping("feign/user/scanCodeLogin")
    ApiResult<SysUserDTO> scanCodeLogin(@RequestBody SysUserThirdDTO loginDTO);


    /**
     * 获取用户列表
     */
    @PostMapping("feign/user/findList")
    ApiResult<List<FindUserDTO>> userList(@RequestBody BaseSearchDTO dto);

    /**
     * 获取用户权限
     * */
    @PostMapping("feign/user/getRequestPermissionsList")
    List<UserRequestPermissionsDTO> getRequestPermissionsList(@RequestBody String userId);

    /**
     * 获取用户列表
     */
    @GetMapping("feign/user/getUserList")
    List<FindUserDTO> getUserList();

    /**
     * 获取部门的用户
     */
    @GetMapping("feign/user/getDepUserList")
    List<String> getDepUserList(@RequestBody String userId);

    /**
     *  根据用户id 获取用户角色的id
     */
    @PostMapping("feign/user/getRoleIdList")
    List<String> getRoleIdList(@RequestBody String userId);

    /**
     *   根据第三方平台 以及union id 获取用户id
     */
    @PostMapping("feign/user/getUserIdByThird")
    String getUidByUnionId(@RequestBody FindUserByThirdDTO third);

    @PostMapping("feign/user/getThirdUnionId")
    List<ThirdUnionDTO> getThirdUnionId(@RequestBody String fsPlatform);

    /**
     *   根据userId查询用户
     */
    @PostMapping("feign/user/getUserByUserId")
    FindUserDTO getUserByUserId(@RequestBody String userId);

    /**
     *  根据用户名称查询用户
     */
    @GetMapping("feign/user/getUserByUserName")
    FindUserDTO getUserByUserName(@RequestBody String userName);

    /**
     *  根据userIds查询用户集合
     */
    @GetMapping("feign/user/getUserListByUserIds")
    List<FindUserDTO> getUserListByUserIds(@RequestBody List<String> userIds);

    /**
     * 查询sku编码
     */
    @PostMapping("feign/code/getSkuNo")
    String getSkuNo(@RequestBody SysCodeSkuDTO dto);

    /**
     *  查询spu编码
     */
    @PostMapping("feign/code/getSpuNo")
    String getSpuNo(@RequestBody SysCodeDTO dto);

    /**
     * 查询时间格式的业务编码
     */
    @PostMapping("feign/code/getBusinessNo")
    String getBusinessNo(SysCodeDTO dto);

    @PostMapping("feign/user/getUserDeptList")
    List<SysUserDeptDTO> getUserDeptList();

    /**
     *  根据部门id查询部门
     */
    @PostMapping("feign/user/getUserDeptById")
    SysDepartmentDTO getUserDeptById(@RequestBody String deptId);


    /**
     * 根据部门名称查询部门id  包括字部门
     */
    @PostMapping("feign/dept/getDeptIdList")
    List<String> getDeptIdsByName(@RequestBody String deptName);

    /**
     * 获取所有的部门信息
      */
    @GetMapping("feign/dept/getDeptList")
    List<SysDepartmentDTO> getDeptList();

    @PostMapping("feign/user/getSysUserById")
    SysUserDTO getSysUserById(@RequestBody String uid);
    /**
     * 根据用户id查询所有上级用户
     */
    @PostMapping("feign/user/listSuperiorByUserIds")
    List<UserSuperiorDTO> listSuperiorByUserIds(@RequestBody List<String> userIds);

    /**
     *  根据角色id查用户名称
     */
    @PostMapping("feign/user/listRoleByIds")
    List<String> listRoleByIds(@RequestBody List<String> roleIds);

    /**
     *  根据用户ids查询角色
     */
    @PostMapping("feign/user/listRoleByUserIds")
    List<SysRoleDTO> listRoleByUserIds(List<String> userIds);

    /**
     *  根据部门名称查询部门负责人
     */
    @PostMapping("feign/dept/getByDeptNames")
    List<SysUserDeptDTO> getByDeptNames(@RequestBody List<String> deptNames);

    /**
     * 查询日历列表
     * @param dto
     * @return
     */
    @PostMapping("feign/calendar/list")
    List<SysCalendarListVO> listCalendar(@RequestBody SysCalendarDTO.ListDTO dto);


    /**
     * 查询银行卡列表
     * @param ids
     * @return
     */
    @PostMapping("feign/bank/getByIds")
    List<BaseIdDTO> getBankList(@RequestBody List<String> ids);


     /**
      * 查询组织列表
      * @author yl
      * @date 2023-03-22 15:34
      * @param ids
      * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
      */
    @PostMapping("feign/accountingCompany/getByIds")
    List<BaseIdDTO> getAccountingCompanyList(@RequestBody List<String> ids);

    /**
     * @description: 查询所有已启用组织
     * @author Will
     * @date: 2023/3/22 16:35
     * @return List<BaseIdDTO>
     */
    @GetMapping("feign/accountingCompany/list")
    List<BaseIdDTO> listAccountingCompany();

    /**
     * 根据币种查询
     */
    @PostMapping("feign/currency/listByCurrency")
    List<CurrencyDTO.ViewDTO> listByCurrency(@RequestBody List<String> currencyList);

    /**
     * 根据主键id查询组织信息
     * @Author Luo_WG
     * @Date 2023/4/13 12:19
     * @param id id:组织id
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     **/
    @PostMapping("feign/accountingCompany/getCompanyById")
    SysAccountingCompanyEntity getCompanyById(@RequestBody String id);

    /**
     * 根据用户Id获取部门
     * @Author Luo_WG
     * @Date 2023/4/18 9:53
     * @param userId userId
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("feign/dept/getDeptByUserId")
    SysDepartmentUserNumberDTO getDeptByUserId(@RequestBody String userId);

    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/user/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, String> params);

    @PostMapping("feign/user/getThirdUnionIdByUserId")
    ThirdUnionDTO getThirdUnionIdByUserId(@RequestParam(value = "platform") String platform, @RequestParam(value = "userId") String userId);
}
