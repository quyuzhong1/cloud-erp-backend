package com.erp.server.oms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerInfoService extends SuperService<CustomerInfoEntity> {

    /**
     * 获取到分组的id 集合
     * @author yl
     * @date 2023-05-11 18:10
     * @param
     * @return java.util.List<java.lang.String>
     */
    List<String> listGroup();

    
    /**
     * 添加客户信息
     * @author yl
     * @date 2023-05-12 10:30
     * @param dto
     * @return java.lang.String
     */
    String add(CustomerDTO.AddDTO dto);

    
    /**
     * 提交
     * @author yl
     * @date 2023-05-12 16:47
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    /**
     * 获取tab list
     * @author yl
     * @date 2023-05-12 17:01
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.TabListDTO>
     */
    List<CustomerDTO.TabListDTO> tabList();

    /**
     * 分页信息
     * @author yl
     * @date 2023-05-12 17:21
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.CustomerDTO.PagingViewDTO>
     */
    PagingVO<CustomerDTO.PagingViewDTO> paging(PagingDTO<CustomerDTO.PagingParamDTO> dto);

    
    /**
     * 新增并提交
     * @author yl
     * @date 2023-05-15 9:21
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(CustomerDTO.AddDTO dto);

    /**
     * 客户详情
     * @author yl
     * @date 2023-05-15 9:24
     * @param id
     * @return com.erp.model.oms.dto.CustomerDTO.ViewDTO
     */
    CustomerDTO.ViewDTO view(String id);

    /**
     * 修改客户信息
     * @author yl
     * @date 2023-05-15 10:39
     * @param dto
     * @return java.lang.String
     */
    String updateCustomer(CustomerDTO.UpdateDTO dto);

    
    /**
     * 修改并提交
     * @author yl
     * @date 2023-05-15 14:12
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(CustomerDTO.UpdateDTO dto);

    /**
     * 审核
     * @author yl
     * @date 2023-05-15 14:17
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean approve(BaseApproveParamDTO dto);

    
    /**
     * 反审核
     * @author yl
     * @date 2023-05-15 14:25
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean disApprove(List<String> ids);

    /**
     * 删除客户
     * @author yl
     * @date 2023-05-15 14:30
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);

    
    /**
     * 导出 客户列表
     * @author yl
     * @date 2023-05-15 14:53
     * @param dto
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportExcel(CustomerDTO.ExportDTO dto, HttpServletResponse response);

    
    /**
     * 客户列表
     * @author yl
     * @date 2023-05-15 15:24
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.InfoDTO>
     */
    List<CustomerDTO.InfoDTO> listCustomer();

    /**
     * 启用或者停用客户
     * @author yl
     * @date 2023-05-15 15:30
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateStatus(UpdateStateDTO.BatchUpdateDTO dto);

    
    /**
     * 撤销流程
     * @author yl
     * @date 2023-05-15 15:39
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    
    /**
     * 获取启用的列表
     * @author yl
     * @date 2023-05-15 16:05
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.InfoDTO>
     */
    List<CustomerDTO.InfoDTO> listEnable();

    
    /**
     * 获取客户的默认联系人
     * @author yl
     * @date 2023-05-15 16:15
     * @param customerId
     * @return com.erp.model.oms.dto.CustomerDTO.BaseDTO
     */
    CustomerDTO.BaseDTO getBase(String customerId);
}
