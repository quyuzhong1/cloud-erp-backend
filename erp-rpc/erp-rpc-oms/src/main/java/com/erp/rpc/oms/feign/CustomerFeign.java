package com.erp.rpc.oms.feign;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.sys.dto.DictBasicDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    @PostMapping("feign/customer/ListCustomerAddressByIds")
    List<CustomerAddressEntity> ListCustomerAddressByIds(@RequestBody List<String> ids);

    /**
     * 根据ids查询客户信息
     * @Author Luo_WG
     * @Date 2023/6/1 15:42
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     **/
    @PostMapping("feign/customer/listCustomerByIds")
    List<CustomerInfoEntity> listCustomerByIds(@RequestBody List<String> ids);

    /**
     * 客户列表审核
     * @Author Luo_WG
     * @Date 2023/7/4 12:28
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/customer/approve")
    Boolean approve(@RequestBody BaseApproveParamDTO dto);

    /**
     * 售货员信息
     * @author yl
     * @date 2023-05-15 10:10
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SellerDTO.ViewDTO>
     */
    @PostMapping("feign/customer/listSellerByMainId")
    List<SellerDTO.ViewDTO> listSellerByMainId(@RequestBody String mainId);

    /**
     * 根据key 获取字典数据
     * @author yl
     * @date 2023-03-17 14:16
     * @param Key
     * @return java.util.List<com.erp.model.scm.dto.DictBasicDTO>
     */
    @PostMapping("feign/customer/getDictBasicByKey")
    List<DictBasicDTO.ViewDTO> getDictBasicByKey(@RequestBody String Key);

}