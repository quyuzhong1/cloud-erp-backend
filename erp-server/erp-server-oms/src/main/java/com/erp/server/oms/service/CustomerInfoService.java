package com.erp.server.oms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.dto.CustomerDTO.CustomerBatchUpdateDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.vo.CustomerInfoVO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface CustomerInfoService extends SuperService<CustomerInfoEntity> {

    /**
     * 获取到分组的id 集合
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-11 18:10
     */
    List<String> listGroup();


    /**
     * 添加客户信息
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-12 10:30
     */
    String add(CustomerDTO.AddDTO dto);


    /**
     * 添加客户信息
     *
     * @param dto
     * @return java.lang.String
     */
    CustomerInfoEntity addOrGetCustom(CustomerDTO.AddDTO dto);


    /**
     * 提交
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-12 16:47
     */
    Boolean submit(List<String> ids);

    /**
     * 获取tab list
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.TabListDTO>
     * @author yl
     * @date 2023-05-12 17:01
     */
    List<CustomerDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 分页信息
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.CustomerDTO.PagingViewDTO>
     * @author yl
     * @date 2023-05-12 17:21
     */
    PagingVO<CustomerDTO.PagingViewDTO> paging(PagingDTO<CustomerDTO.PagingParamDTO> dto);


    /**
     * 新增并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 9:21
     */
    String addAndSubmit(CustomerDTO.AddDTO dto);

    /**
     * 客户详情
     *
     * @param id
     * @return com.erp.model.oms.dto.CustomerDTO.ViewDTO
     * @author yl
     * @date 2023-05-15 9:24
     */
    CustomerDTO.ViewDTO view(String id);

    /**
     * 修改客户信息
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-05-15 10:39
     */
    String updateCustomer(CustomerDTO.UpdateDTO dto);


    /**
     * 修改并提交
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:12
     */
    Boolean updateAndSubmit(CustomerDTO.UpdateDTO dto);

    /**
     * 审核
     *
     * @param dto
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:17
     */
    BatchResultDTO approve(BaseApproveParamDTO dto, CustomerInfoEntity entity);

    /**
     * @param dto
     * @param list
     * @return Boolean
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/4 11:31
     */
    Boolean approveEnd(BaseApproveParamDTO dto, List<CustomerInfoEntity> list);


    /**
     * 反审核
     *
     * @param entity
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:25
     */
    BatchResultDTO disApprove(CustomerInfoEntity entity);

    /**
     * 删除客户
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:30
     */
    List<BatchResultDTO>  deleteByIds(List<String> ids);


    /**
     * 导出 客户列表
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:53
     */
    Boolean exportExcel(CustomerDTO.ExportDTO dto);


    /**
     * 客户列表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.InfoDTO>
     * @author yl
     * @date 2023-05-15 15:24
     */
    List<CustomerDTO.InfoDTO> listCustomer();

    /**
     * 启用或者停用客户
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 15:30
     */
    Boolean updateStatus(CustomerBatchUpdateDTO dto);


    /**
     * 撤销流程
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 15:39
     */
    Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto);


    /**
     * 获取启用的列表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.InfoDTO>
     * @author yl
     * @date 2023-05-15 16:05
     */
    List<CustomerDTO.InfoDTO> listEnable(String permissionSql);


    /**
     * 获取客户的默认联系人
     *
     * @param customerId
     * @return com.erp.model.oms.dto.CustomerDTO.BaseDTO
     * @author yl
     * @date 2023-05-15 16:15
     */
    CustomerDTO.BaseDTO getBase(String customerId);


    /**
     * 引用客户
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-15 14:30
     */
    Boolean quoteCustomer(List<String> ids);

    /**
     * 获取客户等级信息
     *
     * @return
     */
    List<CustomerInfoVO> listCustomerByGroup();
    /**
     * 获取客户属性信息
     *
     * @return
     */
    List<CustomerInfoVO> listCustomerByProperty();

    /**
     * 修改金蝶同步信息
     *
     * @param id
     * @param syncKingdeeId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/5/25 10:43
     **/
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);


    /**
     * 处理平台类型历史数据
     *
     * @param
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-28 15:53
     */
    Boolean processData();

    /**
     * 导入客户信息（系统上线临时使用，后续移除）新增客户，如果存在了则不导入
     *
     * @param file
     */
    void importCustomer(MultipartFile file) throws IOException;

    /**
     * 根据金蝶id获取客户信息
     *
     * @param kingdeeCustomerIds
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     * @author yl
     * @date 2023-07-06 15:04
     */
    List<CustomerInfoEntity> listByKingdeeIdList(List<String> kingdeeCustomerIds);


    /**
     * 导入客户金蝶信息（系统上线临时使用，后续移除）
     *
     * @param file
     */
    void importCustomerKingdee(MultipartFile file) throws IOException;

    /**
     * @param countryIdList
     * @return List<CustomerInfoEntity>
     * @description: 根据国家ids查询客户信息
     * @author Will
     * @date: 2023/7/24 12:29
     */
    List<CustomerInfoEntity> listByCountryIdList(List<String> countryIdList);

    /**
     * 根据客户名称获取详情
     *
     * @param name
     * @return
     */
    CustomerInfoEntity getCustomerByName(String name);

    /**
     * 根据名称获取用户
     * @author yl
     * @date 2023-10-17 15:37
     * @param customerName
     * @return com.erp.model.oms.entity.CustomerInfoEntity
     */
    CustomerInfoEntity getByName(String customerName);
    /**
     * 根据客户id获取详情
     *
     * @param id
     * @return
     */
    CustomerInfoEntity getCustomerById(String id);
    CustomerInfoEntity getCustomerByCode(String code);

    /**
     * 根据客户名称获取信息
     * @author yl
     * @date 2023-10-26 15:18
     * @param customerNameList
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     */
    List<CustomerInfoEntity> listByNameList(List<String> customerNameList);

    /**
     * @description
     * @param name 客户名称
     * @return
     * @date 2024-03-22 16:01
     * @author Lambda
     */
    List<CustomerInfoEntity> listByName(String name);

    /**
     * 根据客户名称 获取到客户信息
     * @param name
     * @return
     */
    List<CustomerDTO.ReceiveInfoDTO> listReceiveByName(String name);

    /**
     * 根据客户名称list获取客户详情list
     */
    List<CustomerDTO.ReceiveInfoDTO> listDTOByNameList(List<String> customerNameList);

    /**
     * 根据客户id查询店铺负责人和部门
     * @Author Luo_WG
     * @Date 2024/4/1 15:20
     * @param codeList
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.SellerUserDeptDTO>
     **/
    List<CustomerDTO.SellerUserDeptDTO> listSellerUserDepByCodes(List<String> codeList);

    PagingVO<CustomerDTO.PageSelectDTO> pagingSelect(PagingDTO<CustomerDTO.SelectDTO> dto);

    PagingVO<CustomerDTO.PagingExportDTO> exportCustomer(PagingDTO<CustomerDTO.ExportDTO> dto);

    List<CustomerDTO.InfoDTO> listSimpleName(CustomerDTO.PageSelectDTO dto);

    List<cn.hutool.core.lang.Pair<Integer,List<?>>> exportCustomerPairList(PagingDTO<CustomerDTO.ExportDTO> dto);

    /**
     * 初始化处理客户销售部门数据
     */
    void initHistoryCustomerDeptId();

    VirtualWarehouseDTO.VwDTO getVirtualWarehouseByCustomerId(CustomerDTO.VirtualDTO dto);

    List<CustomerInfoEntity> listByCodes(List<String> list);

    void updateApproveStatus(CustomerInfoEntity entity);

    CustomerDTO.ThirdCustomerAccountDTO getThirdCustomerAccount(BaseIdDTO dto);

    Boolean isSyncDht(String customerId);
}
