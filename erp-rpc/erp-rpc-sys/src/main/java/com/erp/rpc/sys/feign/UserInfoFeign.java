package com.erp.rpc.sys.feign;

import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.ForgotPasswordDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogViewService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.vo.SupplierUserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname UserInfoFeign
 * @Date 2023-06-19 19:52
 * @Created by yl
 */
@FeignClient(name = "erp-sys", contextId = "user")
public interface UserInfoFeign {

    /**
     * 获取第三方绑定的用户信息
     *
     * @param
     * @return java.util.List<com.common.business.dto.FindUserDTO>
     * @author yl
     * @date 2023-06-20 10:16
     */
    @GetMapping("feign/user/listThirdBindUser")
    List<FindUserDTO> listThirdBindUserInfo();

    /**
     * 根据用户ids获取用户信息
     *
     * @param userIds
     * @return
     */
    @GetMapping("feign/user/getUserListByUserIds")
    List<FindUserDTO> listByUserIds(@RequestBody List<String> userIds);

    /**
     * 根据金蝶code 获取用户信息
     *
     * @param kingdeeCodeList
     * @return
     */
    @GetMapping("feign/user/listUserByKingdeeCode")
    List<FindUserDTO> listUserByKingdeeCode(@RequestBody List<String> kingdeeCodeList);


    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    @PostMapping("feign/user/page")
    PagingVO<SupplierUserVO> page(@RequestBody @Validated PagingDTO<UserPagingSearchDTO> dto);

    /**
     * 查询
     *
     * @param dto
     * @return
     */
    @PostMapping("feign/user/list")
    List<SupplierUserVO> list(@RequestBody @Validated UserPagingSearchDTO dto);
    /**
     * 添加用户
     */
    @RequestMapping("feign/user/save")
    String save(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO);

    /**
     * 修改用户
     */
    @RequestMapping("feign/user/update")
    Boolean update(@RequestBody @Validated SysUserInfoDTO sysUserInfoDTO);


    /**
     * 用户信息
     */
    @RequestMapping("feign/user/info/{uid}")
    SysUserInfoEntity info(@PathVariable("uid") String uid);


    /**
     * 删除SRM用户
     */
    @RequestMapping("feign/user/deleteSrmUser")
    ApiResult deleteSrmUser(@RequestBody List<String> uids);

    /**
     * 批量启用/禁用
     *
     * @param stateDTO
     * @return
     */
    @RequestMapping("feign/user/updateStateSrm")
    ApiResult updateStateSrm(@RequestBody @Validated UpdateUserStateDTO stateDTO);

    /**
     * 重置密码
     *
     * @param
     * @return
     * @Author Luo_WG
     * @Date 2023/4/20 9:43
     **/
    @GetMapping("feign/user/resetPassword")
    ApiResult resetPassword(@RequestParam("uid") String uid, @RequestParam("pwd") String pwd);

    /**
     * 忘记密码
     *
     * @param dto dto
     * @return
     * @Author Luo_WG
     * @Date 2023/4/20 11:16
     **/
    @PostMapping("feign/user/forgotPassword")
    ApiResult forgotPassword(@RequestBody ForgotPasswordDTO dto);

    /**
     * 忘记密码-获取验证码
     *
     * @param userAccount userAccount
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/20 11:45
     **/
    @GetMapping("feign/user/forgotPasswordGetCode")
    ApiResult<Map<String, Object>> forgotPasswordGetCode(@RequestParam("userAccount") String userAccount);
}
