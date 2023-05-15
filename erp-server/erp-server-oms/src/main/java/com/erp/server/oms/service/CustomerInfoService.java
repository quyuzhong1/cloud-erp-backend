package com.erp.server.oms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;

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
}
