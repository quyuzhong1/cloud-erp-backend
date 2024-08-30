package com.erp.server.scm.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.model.sys.vo.SupplierUserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-01-05
 */
public interface SupplierUserService {
    /**
     * 供应商协同用户分页查询
     *
     * @param dto
     * @return
     */
    PagingVO<SupplierUserVO> paging(PagingDTO<UserPagingSearchDTO> dto);

    /**
     * 新增供应商协同用户
     *
     * @param sysUserInfoDTO
     */
    void add(SysUserInfoDTO sysUserInfoDTO);

    /**
     * 更新供应商协同用户
     *
     * @param sysUserInfoDTO
     */

    void update(SysUserInfoDTO sysUserInfoDTO);

    /**
     * 查询用户详情
     *
     * @param uid
     * @return
     */
    SupplierUserInfoVO getById(String uid);

    /**
     * 删除供应商协同用户
     *
     * @param uids
     */
    ApiResult deleteById(String uid);

    /**
     * 批量启用 停用
     *
     * @param stateDTO
     */
    ApiResult updateState(UpdateUserStateDTO stateDTO);

    /**
     * 重置密码
     *
     * @param uid
     * @param pwd
     * @return
     */
    ApiResult changePassword(String uid, String pwd);

    /**
     * 下载导出协作用户模板
     *
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入供应商协作用户
     *
     * @param excelFile
     * @param response
     * @param isSuper 是否是管理员
     * @return
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 导出供应商协作用户列表
     *
     * @param dto
     */
    Boolean exportSupplierUser(UserPagingSearchDTO dto);

    /**
     * 根据条件获取供应商用户列表
     * @param dto
     * @return
     */
    List<SupplierUserVO> getSupplierUserList(UserPagingSearchDTO dto);

    PagingVO<SupplierUserVO> exportSupplierUser(PagingDTO<UserPagingSearchDTO> dto);
}
