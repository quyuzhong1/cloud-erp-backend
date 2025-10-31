package com.erp.rpc.sys.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.business.dto.UserSelectDto;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.*;
import com.erp.model.sys.vo.MsgChannelConfigDTO;
import com.erp.model.sys.vo.MsgConfigDTO;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Classname 系统管理 服务

 * @Date 2022-07-08 16:52
 * @Created by yl
 */
@FeignClient(name = "erp-sys", contextId = "sysUserFeign",configuration = {FeignErrorDecoder.class})
public interface SysUserFeign {

    /**
     * 账号登录
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
     * 远程搜索
     *
     * @param dto
     * @return ApiResult
     */
    @PostMapping("feign/user/pagingSelect")
    ApiResult<PagingVO<UserSelectDto.PageSelectDTO>> pagingSelect(@RequestBody PagingDTO<UserSelectDto.SelectDTO> dto);

        /**
         * 获取用户权限
         */
    @PostMapping("feign/user/getRequestPermissionsList")
    List<UserRequestPermissionsDTO> getRequestPermissionsList(@RequestBody String userId);
    /**
     * 根据菜单cdoe查询用户数据权限
     * @author hyj
     * @date 2024/5/9 10:43
     * @param menuCode 菜单编号
     * @return java.lang.String
     **/
    @GetMapping("feign/user/getUserDatePermissionByMenuCode")
    Boolean getUserDatePermissionByMenuCode(@RequestParam("menuCode") String menuCode);
    /**
     * 获取用户列表
     */
    @GetMapping("feign/user/getUserList")
    List<FindUserDTO> getUserList();

    /**
     * 更新用户管理更新时间
     */
    @PostMapping("feign/user/updateSysUserTime")
    void updateSysUserTime(@RequestBody List<String> userIdList);

    /**
     * 获取部门的用户
     */
    @GetMapping("feign/user/getDepUserList")
    List<String> getDepUserList(@RequestBody String userId);

    /**
     * 根据用户id 获取用户角色的id
     */
    @PostMapping("feign/user/getRoleIdList")
    List<String> getRoleIdList(@RequestBody String userId);

    /**
     * 根据用户ID获取用户完整登录信息（包含权限和菜单）
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 用户信息（包含permissionList和leftMenuList）
     */
    @PostMapping("feign/user/getUserLoginInfo")
    ApiResult<SysUserDTO> getUserLoginInfo(@RequestBody SysFeignDTO.UserLoginInfoDTO dto);

    /**
     * 根据第三方平台 以及union id 获取用户id
     */
    @PostMapping("feign/user/getUserIdByThird")
    String getUidByUnionId(@RequestBody FindUserByThirdDTO third);

    @PostMapping("feign/user/getThirdUnionId")
    List<ThirdUnionDTO> getThirdUnionId(@RequestBody String fsPlatform);

    /**
     * 根据userId查询用户
     */
    @PostMapping("feign/user/getUserByUserId")
    FindUserDTO getUserByUserId(@RequestBody String userId);


    /**
     * 根据用户名称查询用户
     */
    @GetMapping("feign/user/getUserByUserName")
    FindUserDTO getUserByUserName(@RequestParam("userName") String userName,@RequestParam("userType") String userType);
    /**
     * 根据用户名称查询用户
     */
    @GetMapping("feign/user/getUserByMobile")
    FindUserDTO getUserByMobile(@RequestParam("mobile") String mobile,@RequestParam("userType") String userType);

    /**
     * 根据用户名称集合查询用户
     */
    @GetMapping("feign/user/listUserByUserNames")
    List<FindUserDTO> listUserByUserNames(@RequestParam("userNames") List<String> userNames,@RequestParam("userType") String userType);

    /**
     * 根据userIds查询用户集合
     */
    @GetMapping("feign/user/getUserListByUserIds")
    List<FindUserDTO> getUserListByUserIds(@RequestBody List<String> userIds);

    /**
     * 根据编码集合查询
     */
    @GetMapping("feign/user/listUserByCodeList")
    List<FindUserDTO> listUserByCodeList(List<String> codeList);


    /**
     * 查询sku编码
     */
    @PostMapping("feign/code/getSkuNo")
    String getSkuNo(@RequestBody SysCodeSkuDTO dto);

    /**
     * 根据名称列表批量查询示例用户
     */
    @PostMapping("feign/sampleUseUser/getListByNameList")
    ApiResult<List<SampleUseUserDTO.ViewDTO>> getSampleUseUserListByNameList(@RequestBody List<String> nameList);

    /**
     * 查询spu编码
     */
    @PostMapping("feign/code/getSpuNo")
    String getSpuNo(@RequestBody SysCodeDTO dto);

    /**
     * 查询时间格式的业务编码
     */
//    @PostMapping("feign/code/getBusinessNo")
//    String getBusinessNo(SysCodeDTO dto);

    @PostMapping("feign/user/getUserDeptList")
    List<SysUserDeptDTO> getUserDeptList();

    /**
     * 根据部门id查询部门
     */
    @PostMapping("feign/user/getUserDeptById")
    SysDepartmentDTO getUserDeptById(@RequestBody String deptId);

    /**
     * 根据部门id查询部门
     */
    @PostMapping("feign/dept/listDepartByIds")
    List<SysDepartmentEntity> listDeptByIds(@RequestBody List<String> deptIdList);

    /**
     * 根据部门金蝶Code查询部门
     */
    @PostMapping("feign/user/getUserDeptByCode")
    SysDepartmentDTO getUserDeptByCode(@RequestBody String code);


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
     * 根据用户id查询所有上级部门
     */
    @PostMapping("feign/user/listDeptByUserIds")
    List<UserSuperiorDTO> listDeptByUserIds(@RequestBody List<String> userIds);
    /**
     * 根据角色id查用户名称
     */
    @PostMapping("feign/user/listRoleByIds")
    List<String> listRoleByIds(@RequestBody List<String> roleIds);

    /**
     * 根据用户ids查询角色
     */
    @PostMapping("feign/user/listRoleByUserIds")
    List<SysRoleDTO> listRoleByUserIds(List<String> userIds);

    /**
     * 根据部门名称查询部门负责人
     */
    @PostMapping("feign/dept/getByDeptNames")
    List<SysUserDeptDTO> getByDeptNames(@RequestBody List<String> deptNames);

    /**
     * 查询日历列表
     *
     * @param dto
     * @return
     */
    @PostMapping("feign/calendar/list")
    List<SysCalendarListVO> listCalendar(@RequestBody SysCalendarDTO.ListDTO dto);


    /**
     * 查询银行卡列表
     *
     * @param ids
     * @return
     */
    @PostMapping("feign/bank/getByIds")
    List<BaseIdDTO> getBankList(@RequestBody List<String> ids);


    /**
     * 查询组织列表
     *
     * @param ids
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     * @author yl
     * @date 2023-03-22 15:34
     */
    @PostMapping("feign/accountingCompany/getByIds")
    List<BaseIdDTO.CodeDTO> getAccountingCompanyList(@RequestBody List<String> ids);

    /**
     * @param codes
     * @return List<CodeDTO>
     * @description: 查询组织列表
     * @author Will
     * @date: 2023/6/29 12:27
     */
    @PostMapping("feign/accountingCompany/listByCodes")
    List<BaseIdDTO.CodeDTO> listAccountingCompanyByCodeList(@RequestBody List<String> codes);


    /**
     * @return List<BaseIdDTO>
     * @description: 查询所有已启用组织
     * @author Will
     * @date: 2023/3/22 16:35
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
     *
     * @param id id:组织id
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     * @Author Luo_WG
     * @Date 2023/4/13 12:19
     **/
    @PostMapping("feign/accountingCompany/getCompanyById")
    SysAccountingCompanyEntity getCompanyById(@RequestBody String id);

    /**
     * 根据金蝶id查询组织信息
     *
     **/
    @PostMapping("feign/accountingCompany/getCompanyByKindgeeId")
    SysAccountingCompanyEntity getCompanyByKindgeeId(@RequestBody String KindgeeId);

    /**
     * 根据公司名称查询公司信息
     *
     * @param orgName 公司名称
     * @return SysAccountingCompanyEntity
     * @Author Luo_WG
     * @Date 2023/4/13 12:19
     **/
    @PostMapping("feign/accountingCompany/getCompanyByName")
    SysAccountingCompanyEntity getCompanyByName(@RequestBody String orgName);

    /**
     * 根据主键id查询组织信息
     *
     * @param id id:组织id
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO>
     * @Author Luo_WG
     * @Date 2023/4/13 12:19
     **/
    @PostMapping("feign/accountingCompany/listCompanyById")
    List<SysAccountingCompanyEntity> listCompanyById(@RequestBody List<String> id);

    /**
     * 根据用户Id获取部门
     *
     * @param userId userId
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/18 9:53
     **/
    @PostMapping("feign/dept/getDeptByUserId")
    SysDepartmentUserNumberDTO getDeptByUserId(@RequestBody String userId);

    @PostMapping("feign/dept/listDeptUserByUserIdList")
    List<SysDepartmentUserNumberDTO> listDeptUserByUserIdList(@RequestBody List<String> userIdList);

    /**
     * 根据部门id查询部门下人员
     */
    @PostMapping("feign/dept/listDeptUserByDeptIdList")
    List<SysDepartmentUserNumberDTO> listDeptUserByDeptIdList(@RequestBody List<String> deptIdList);

    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/user/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params);

    /**
     * 批量根据用户id获取第三方平台账号信息
     *
     * @param platform
     * @param userIds
     * @return
     */
    @PostMapping("feign/user/getThirdUnionIdsByUserIds")
    List<ThirdUnionDTO> getThirdUnionIdsByUserIds(@RequestParam(value = "platform") String platform, @RequestParam(value = "userIds") List<String> userIds);

    /**
     * 批量根据用户id获取第三方平台账号信息
     * @param platform
     * @param userIds
     * @return
     */
    @PostMapping("feign/user/getThirdByUserIds")
    List<ThirdUnionDTO> getThirdByUserIds(@RequestParam(value = "platform") String platform, @RequestParam(value = "userIds") List<String> userIds);

    /**
     * @param platform
     * @param thirdId
     * @return
     */
    @PostMapping("feign/user/getUserByThird")
    SysUserThirdEntity getUserByThird(@RequestParam(value = "platform") String platform, @RequestParam(value = "thirdId")String thirdId);

    /**
     * 根据主键获取消息配置信息
     *
     * @param id
     * @return
     */
    @GetMapping("feign/msgConfig/getById")
    MsgConfigDTO getMsgConfigById(@RequestParam(value = "id") String id);

    /**
     * 根据主键获取消息配置信息
     *
     * @param msgConfigId
     * @return
     */
    @GetMapping("feign/msgChannelConfig/findByMsgConfigId")
    List<MsgChannelConfigDTO> findByMsgConfigId(@RequestParam(value = "msgConfigId") String msgConfigId);

    /**
     * 批量获取用户基本信息，如手机号码，名字，邮箱（过滤掉禁用的用户）
     *
     * @return
     */
    @PostMapping("feign/user/getUserSimpleInfoByIds")
    List<SysUserSimpleDTO> getUserSimpleInfoByIds(@RequestParam(value = "userIds") List<String> userIds);

    /**
     * 根据角色id查询用户列表
     *
     * @param dto
     * @return
     */
    @PostMapping("feign/user/getUserListByRoleIds")
    List<FindUserDTO> getUserListByRoleIds(@RequestBody @Valid SysFeignDTO.ListByRoleIdsDTO dto);


    /**
     * 根据node key 获取到接收信息
     *
     * @param nodeKey 节点key
     * @return
     */
    @PostMapping("feign/notice/listNoticeReceiver")
    List<NoticeReceiverDTO.InfoDTO> listNoticeReceiverByNodeKey(@RequestBody String nodeKey);


    /**
     * 根据国家id获取到地区信息
     *
     * @param countryIds
     * @return java.util.List<com.erp.model.sys.dto.DictGlobalAreaDTO.InfoDTO>
     * @author yl
     * @date 2023-05-15 11:44
     */
    @PostMapping("feign/dict/listGlobalAreaByCountryIds")
    List<DictGlobalAreaDTO.InfoDTO> listGlobalAreaByCountryIds(@RequestBody List<String> countryIds);

    /**
     * 根据id获取国家信息
     *
     * @param id
     * @return java.util.List<com.erp.model.sys.entity.DictCountryEntity>
     * @Author Luo_WG
     * @Date 2023/5/26 10:45
     **/
    @PostMapping("feign/dictCountry/getCountryById")
    DictCountryEntity getCountryById(@RequestBody String id);

    /**
     * 根据id查询区域
     */
    @PostMapping("feign/globalArea/getById")
    DictGlobalAreaEntity getGlobalAreaById(@RequestBody String id);
    /**
     * 根据ids查询区域
     */
    @PostMapping("feign/globalArea/listGlobalAreaByIds")
    List<DictGlobalAreaEntity> listGlobalAreaByIds(@RequestBody List<String> ids);

    /**
     * 根据id查询省/市
     */
    @PostMapping("feign/city/getById")
    DictCityEntity getCityById(@RequestBody String id);

    /**
     * 根据ids查询省/市
     */
    @PostMapping("feign/city/listByIds")
    List<DictCityEntity> listCityByIds(@RequestBody List<String> ids);

    /**
     * 根据names查询市
     */
    @PostMapping("feign/city/listCityByNames")
    List<DictCityEntity> listCityByNames(@RequestBody List<String> names);

    /**
     * @param userIds
     * @return List<UserKingdeePostInfoDTO>
     * @description: 根据用戶ids 获取金蝶的对应岗位code
     * @author Will
     * @date: 2023/6/6 10:44
     */
    @PostMapping("feign/user/listUserKingdeePostByUserIds")
    List<KingdeePostDTO.UserKingdeePostInfoDTO> listUserKingdeePostByUserIds(@RequestBody List<String> userIds);

    /**
     * 获取用户区间配置
     *
     * @param type
     * @return
     */
    @GetMapping("feign/cfgUserRange/getByType")
    List<CfgUserRangeDTO.UserRangeDataDTO> getUserRangeByType(@RequestParam(value = "type") String type,
                                                              @RequestParam(value = "addLast", required = false, defaultValue = "true") Boolean addLast);

    /**
     * @param kingdeePostCodes
     * @return List<UserKingdeePostInfoDTO>
     * @description: 根据金蝶的对应岗位code获取信息
     * @author Will
     * @date: 2023/6/6 10:44
     */
    @PostMapping("feign/user/listUserKingdeePostByKingdeePostCodes")
    List<KingdeePostDTO.UserKingdeePostInfoDTO> listUserKingdeePostByKingdeePostCodes(@RequestBody List<String> kingdeePostCodes);

    /**
     * @param deptCodeList
     * @return List<SysDepartmentDTO>
     * @description: 根据部门编码查询
     * @author Will
     * @date: 2023/7/5 18:14
     */
    @PostMapping("feign/dept/listDeptByCodeList")
    List<SysDepartmentDTO> listDeptByCodeList(List<String> deptCodeList);

    /**
     * @param currCodeList
     * @return List<CurrencyDTO>
     * @description: 根据金蝶编码查询币别
     * @author Will
     * @date: 2023/7/5 18:37
     */
    @PostMapping("feign/currency/listCurrencyByKingdeeCodeList")
    List<CurrencyDTO.ViewDTO> listCurrencyByKingdeeCodeList(List<String> currCodeList);

    /**
     * 获取国家列表
     *
     * @param
     * @return
     */
    @GetMapping("/feign/dictCountry/list")
    List<DictCountryDTO.ListDTO> countryList();

    /**
     * 获取省份城市列表
     *
     * @param countryCode
     * @return
     */
    @GetMapping("/feign/city/getProvincesByCountryCode")
    List<DictCityDTO.ListDTO> getProvincesByCountryCode(@RequestParam("countryCode") String countryCode);

    /**
     * 获取所有币别
     */
    @GetMapping("/feign/currency/list")
    List<DictCurrencyEntity> currencyList();


    /**
     * 根据用户 获取到部门的负责人
     *
     * @param userIdList
     * @return
     */
    @PostMapping("/feign/dept/listLeadByUserIdList")
    List<String> listLeadByUserIdList(List<String> userIdList);

    /**
     * 根据角色id获取角色菜单
     *
     * @param roleIdList
     * @return
     */
    @PostMapping("/feign/user/getMenuRefRoleByRoleIds")
    List<SysRoleMenuEntity> getMenuRefRoleByRoleIds(@RequestBody List<String> roleIdList);

    /**
     * 根据部门名称查询用户
     *
     * @param deptName
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.common.business.dto.FindUserDTO>>
     * @Author Luo_WG
     * @Date 2023/7/20 15:49
     **/
    @PostMapping("feign/user/listUserByDept")
    List<SysUserInfoEntity> listUserByDept(@RequestBody String deptName);

    /**
     * @param deptNameList
     * @return List<SysDepartmentDTO>
     * @description: 根据部门名称查询最高级别部门及下级
     * @author Will
     * @date: 2023/9/20 18:52
     */
    @PostMapping("feign/dept/listSameLevelDeptIdList")
    List<SysDepartmentDTO> listSameLevelDeptIdList(@RequestBody List<String> deptNameList);

    /**
     * 根据父级获取全量子集
     *
     * @param deptId
     * @return
     */
    @GetMapping("feign/dept/getDeptByParentId")
    List<SysDepartmentTreeDTO> getDeptByParentId(@RequestParam("deptId") String deptId);
    /**
     * @description: 查询数据发送同步任务
     * @author Will
     * @date: 2023/10/30 11:41
     * @param syncParamDTO
     */
    @PostMapping("/feign/sysSyncTask/findDataSendSyncTask")
    void findDataSendSyncTask(@RequestBody DmpSyncMqDTO.SyncParamDTO syncParamDTO);

    @PostMapping("feign/dept/getDeptByNames")
    List<SysDepartmentEntity> getDeptByNames(@RequestBody List<String> deptNameList);

    @PostMapping("feign/dept/getDeptByIds")
    List<SysDepartmentEntity> getDeptByIds(@RequestBody List<String> ids);


    /**
     * 获取所有的部门信息
     */
    @GetMapping("feign/dept/getDeptEntityList")
    List<SysDepartmentEntity> getDeptEntityList();

    /**
     * @param platform
     * @param thirdIds
     * @return
     */
    @PostMapping("feign/user/getUserByThirdIdList")
    List<SysUserThirdEntity>  getUserByThirdIdList(@RequestParam(value = "platform") String platform, @RequestParam(value = "thirdIds") ArrayList<String> thirdIds);

    /**
     * 根据币种三字码获取币种符号
     * @param num
     * @return
     */
    @GetMapping("feign/currency/getCurrencyByNum")
    DictCurrencyEntity getCurrencyByNum(@RequestParam(value = "num") String num);
    /**
     * 通过App-Id获取飞书用户UnionId
     * 通过App-Id从sys_referer_config表获取配置信息，然后调用FsService获取用户unionId
     *
     * @param appId 应用ID
     * @param dto   查找第三方用户DTO
     * @return 用户UnionId
     */
    @PostMapping("feign/user/getFsUserUnionIdByAppId")
    ApiResult<String> getFsUserUnionIdByAppId(@RequestParam("appId") String appId, @RequestBody FindThirdUserDTO dto);

}
