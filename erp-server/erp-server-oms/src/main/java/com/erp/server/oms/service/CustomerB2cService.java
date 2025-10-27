package com.erp.server.oms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerB2CDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.sys.entity.DictCountryEntity;
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
public interface CustomerB2cService extends SuperService<CustomerB2cEntity> {



    
    /**
     * 添加客户信息
     * @author yl
     * @date 2023-05-12 10:30
     * @param dto
     * @return java.lang.String
     */
    String add(CustomerB2CDTO.AddDTO dto);

    
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
     * @return java.util.List<com.erp.model.oms.dto.CustomerB2CDTO.TabListDTO>
     */
    List<CustomerB2CDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 分页信息
     * @author yl
     * @date 2023-05-12 17:21
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.CustomerB2CDTO.PagingViewDTO>
     */
    PagingVO<CustomerB2CDTO.PagingViewDTO> paging(PagingDTO<CustomerB2CDTO.PagingParamDTO> dto);

    
    /**
     * 新增并提交
     * @author yl
     * @date 2023-05-15 9:21
     * @param dto
     * @return java.lang.Boolean
     */
    String addAndSubmit(CustomerB2CDTO.AddDTO dto);

    /**
     * 客户详情
     * @author yl
     * @date 2023-05-15 9:24
     * @param id
     * @return com.erp.model.oms.dto.CustomerB2CDTO.ViewDTO
     */
    CustomerB2CDTO.ViewDTO view(String id);

    /**
     * 修改客户信息
     * @author yl
     * @date 2023-05-15 10:39
     * @param dto
     * @return java.lang.String
     */
    String updateCustomer(CustomerB2CDTO.UpdateDTO dto);

    
    /**
     * 修改并提交
     * @author yl
     * @date 2023-05-15 14:12
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(CustomerB2CDTO.UpdateDTO dto);

    /**
     * 审核
     * @author yl
     * @date 2023-05-15 14:17
     * @param dto
     * @return java.lang.Boolean
     */
    BatchResultDTO approve(BaseApproveParamDTO dto,CustomerB2cEntity entity);

    /**
     * @description: 结束审核
     * @author Will
     * @date: 2023/7/4 11:31
     * @param dto
     * @param list
     * @return Boolean
     */
    Boolean approveEnd (BaseApproveParamDTO dto,List<CustomerB2cEntity> list);

    
    /**
     * 反审核
     * @author yl
     * @date 2023-05-15 14:25
     * @param ids
     * @return java.lang.Boolean
     */
    BatchResultDTO disApprove(CustomerB2cEntity entity);

    /**
     * 删除客户
     * @author yl
     * @date 2023-05-15 14:30
     * @param ids
     * @return java.lang.Boolean
     */
    List<BatchResultDTO> deleteByIds(List<String> ids);

    
    /**
     * 导出 客户列表
     * @author yl
     * @date 2023-05-15 14:53
     * @param dto
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportExcel(CustomerB2CDTO.ExportDTO dto, HttpServletResponse response);

    
    /**
     * 客户列表
     * @author yl
     * @date 2023-05-15 15:24
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerB2CDTO.InfoDTO>
     */
    List<CustomerB2CDTO.InfoDTO> listCustomer();

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
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto);

    
    /**
     * 获取启用的列表
     * @author yl
     * @date 2023-05-15 16:05
     * @param
     * @return java.util.List<com.erp.model.oms.dto.CustomerB2CDTO.InfoDTO>
     */
    List<CustomerB2CDTO.InfoDTO> listEnable(String permissionSql);

    
    /**
     * 获取客户的默认联系人
     * @author yl
     * @date 2023-05-15 16:15
     * @param customerId
     * @return com.erp.model.oms.dto.CustomerB2CDTO.BaseDTO
     */
    CustomerB2CDTO.BaseDTO getBase(String customerId);


    /**
     * 引用客户
     * @author yl
     * @date 2023-05-15 14:30
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean quoteCustomer(List<String> ids);


    
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
     * @description: 根据国家ids查询客户信息
     * @author Will
     * @date: 2023/7/24 12:29
     * @param countryIdList
     * @return List<CustomerInfoEntity>
     */
    List<CustomerB2cEntity> listByCountryIdList(List<String> countryIdList);
    /**
     * @description: 根据客户id查询买家信息
     * @author Will
     * @date: 2023/9/5 19:30
     * @param id
     * @return ViewReceiveDataDTO
     */
    SoB2cDTO.ViewReceiveDataDTO viewReceiveData(String id);

    CustomerB2cEntity saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, SoB2cReceiverEntity receiverEntity, String dictCountryCode, List<DictCountryEntity> countryList ,boolean notUpdateAddress);

    CustomerB2cEntity getBySourceId(String mainId);

    /**
     * 根据条件查询
     * @param dictPlatform
     * @param name
     * @param sourceType
     * @return
     */
    CustomerB2cEntity findByPlatformAndName(String dictPlatform, String name, String sourceType);

    /**
     * 分页查询客户名称
     * @param pagingDTO
     * @return
     */
    CustomerB2CDTO.DropPagingDTO<CustomerB2CDTO.DropListDTO> customerDropDown(PagingDTO<CustomerB2CDTO.DropSearchDTO> pagingDTO);

    /**
     * @description: 根据id或名称查询
     * @author Will
     * @date: 2024/5/28 9:23
     * @param keyWord
     * @return CustomerB2cEntity
     */
    CustomerB2cEntity getByIdOrName (String keyWord);

    /**
     * 远程搜索
     * @param searchDTO
     * @return
     */
    PagingVO<CustomerB2CDTO.InfoDTO> pagingSelect(PagingDTO<CustomerB2CDTO.SelectDTO> searchDTO);

    List<CustomerB2CDTO.DropListDTO> customerListByName(List<String> customerNameList);
}
