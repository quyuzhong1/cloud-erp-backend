package com.erp.server.oms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    List<CustomerDTO.TabListDTO> tabList(PermissionsDTO dto);

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
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/4 11:31
     * @param dto
     * @param list
     * @return Boolean
     */
    Boolean approveEnd (BaseApproveParamDTO dto,List<CustomerInfoEntity> list);

    
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
    List<CustomerDTO.InfoDTO> listEnable(String permissionSql);

    
    /**
     * 获取客户的默认联系人
     * @author yl
     * @date 2023-05-15 16:15
     * @param customerId
     * @return com.erp.model.oms.dto.CustomerDTO.BaseDTO
     */
    CustomerDTO.BaseDTO getBase(String customerId);


    /**
     * 引用客户
     * @author yl
     * @date 2023-05-15 14:30
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean quoteCustomer(List<String> ids);

    /**
     * 修改金蝶同步信息
     * @Author Luo_WG
     * @Date 2023/5/25 10:43
     * @param id
     * @param syncKingdeeStatus
     * @param syncKingdeeId
     * @param syncOperate
     * @return java.lang.Boolean
     **/
    Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus,String syncKingdeeId,String syncOperate);

    
    /**
     * 处理平台类型历史数据
     * @author yl
     * @date 2023-06-28 15:53
     * @param
     * @return java.lang.Boolean
     */
    Boolean processData();

    /**
     * 导入客户信息（系统上线临时使用，后续移除）新增客户，如果存在了则不导入
     * @param file
     */
    void importCustomer(MultipartFile file) throws IOException;

    /**
     * 根据金蝶id获取客户信息
     * @author yl
     * @date 2023-07-06 15:04
     * @param kingdeeCustomerIds
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     */
    List<CustomerInfoEntity> listByKingdeeIdList(List<String> kingdeeCustomerIds);


    /**
     * 导入客户金蝶信息（系统上线临时使用，后续移除）
     * @param file
     */
    void importCustomerKingdee(MultipartFile file) throws IOException;
    /**
     * @description: 根据国家ids查询客户信息
     * @author Will
     * @date: 2023/7/24 12:29
     * @param countryIdList
     * @return List<CustomerInfoEntity>
     */
    List<CustomerInfoEntity> listByCountryIdList(List<String> countryIdList);

    /**
     * 根据名称获取用户
     * @author yl
     * @date 2023-10-17 15:37
     * @param customerName
     * @return com.erp.model.oms.entity.CustomerInfoEntity
     */
    CustomerInfoEntity getByName(String customerName);
}
