package com.erp.rpc.oms.feign;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.vo.CustomerInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "customer")
public interface CustomerFeign {
    /**
     * 获取客户信息
     *
     * @return com.erp.model.oms.entity.SoInfoEntity
     * @Author Luo_WG
     * @Date 2023/5/15 18:18
     **/
    @PostMapping("feign/customer/listCustomer")
    List<CustomerInfoEntity> listCustomer();

    /**
     * 获取客户分组信息
     *
     * @return com.erp.model.oms.vo.CustomerInfoVO
     * @Author zdy
     * @Date 2023/10/08 18:18
     **/
    @PostMapping("feign/customer/listCustomerByGroup")
    List<CustomerInfoVO> listCustomerByGroup();

    /**
     * 获取客户属性信息
     *
     * @return com.erp.model.oms.vo.CustomerInfoVO
     * @Author zdy
     * @Date 2023/10/08 18:18
     **/
    @PostMapping("feign/customer/listCustomerByProperty")
    List<CustomerInfoVO> listCustomerByProperty();

    /**
     * 引用客户
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     * @author yl
     * @date 2023-05-17 18:35
     */
    @PostMapping("feign/customer/quoteCustomer")
    List<CustomerInfoEntity> quoteCustomer(@RequestBody List<String> ids);

    /**
     * 根据id查询收货地址
     *
     * @param ids
     * @return java.util.List<com.erp.model.oms.entity.CustomerAddressEntity>
     * @Author Luo_WG
     * @Date 2023/5/29 17:39
     **/
    @PostMapping("feign/customer/listCustomerAddressByIds")
    List<CustomerAddressEntity> listCustomerAddressByIds(@RequestBody List<String> ids);

    /**
     * 根据ids查询客户信息
     *
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     * @Author Luo_WG
     * @Date 2023/6/1 15:42
     **/
    @PostMapping("feign/customer/listCustomerByIds")
    List<CustomerInfoEntity> listCustomerByIds(@RequestBody List<String> ids);

    /**
     * 根据客户id查询店铺负责人和部门
     * @Author Luo_WG
     * @Date 2024/4/1 15:17
     * @param codeList
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     **/
    @PostMapping("feign/customer/listSellerUserDepByCodes")
    List<CustomerDTO.SellerUserDeptDTO> listSellerUserDepByCodes(@RequestBody List<String> codeList);

    /**
     * @param countryIdList
     * @return List<CustomerInfoEntity>
     * @description: 根据国家ids查询客户信息
     * @author Will
     * @date: 2023/7/24 12:28
     */
    @PostMapping("feign/customer/listByCountryIdList")
    List<CustomerInfoEntity> listByCountryIdList(@RequestBody List<String> countryIdList);

    /**
     * 客户列表审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/7/4 12:28
     **/
    @PostMapping("feign/customer/approve")
    ApiResult<List<BatchResultDTO>> approve(@RequestBody BaseApproveParamDTO dto);

    /**
     * 售货员信息
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SellerDTO.ViewDTO>
     * @author yl
     * @date 2023-05-15 10:10
     */
    @PostMapping("feign/customer/listSellerByMainId")
    List<SellerDTO.ViewDTO> listSellerByMainId(@RequestBody String mainId);

    /**
     * 根据key 获取字典数据
     *
     * @param Key
     * @return java.util.List<com.erp.model.scm.dto.DictBasicDTO>
     * @author yl
     * @date 2023-03-17 14:16
     */
    @PostMapping("feign/customer/getDictBasicByKey")
    List<DictBasicDTO.ViewDTO> getDictBasicByKey(@RequestBody String Key);


    /**
     * 根据客户名称获取客户详情
     *
     * @param customerName
     * @return
     */
    @PostMapping("feign/customer/getCustomerByName")
    CustomerInfoEntity getCustomerByName(@RequestBody String customerName);

    /**
     * 根据客户名称获取客户详情
     *
     * @param id
     * @return
     */
    @PostMapping("feign/customer/getCustomerById")
    CustomerInfoEntity getCustomerById(@RequestBody String id);

    /**
     * 根据客户名称list获取客户详情list
     *
     */
    @PostMapping("feign/customer/listDTOByNameList")
    List<CustomerDTO.ReceiveInfoDTO> listDTOByNameList(@RequestBody List<String> customerNameList);
    
    /**
     * 根据客户名称和编码获取客户详情
     *
     * @param customerName
     * @return
     */
    @PostMapping("feign/customer/getCustomerByCodeAndName")
    List<CustomerInfoEntity> getCustomerByCodeAndName(@RequestParam(value = "code") String code,@RequestParam(value = "name") String name);

    /**
     * 根据客户编码获取客户详情
     * @param list
     * @return
     */
    @PostMapping("feign/customer/listByCodes")
    List<CustomerInfoEntity> listByCodes(List<String> list);

    /**
     * 更新 客户信息审核状态
     * @param entity
     */
    @PostMapping("feign/customer/updateApproveStatus")
    void updateApproveStatus(CustomerInfoEntity entity);
}