package com.erp.server.oms.controller.feign;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.server.oms.service.CustomerAddressService;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.CustomerSellerService;
import com.erp.server.oms.service.DictBasicService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("feign/customer")
public class CustomerFeignController {
    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private CustomerSellerService customerSellerService;

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 获取所有客户信息
     * @Author Luo_WG
     * @Date 2023/5/17 18:41
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     **/
    @PostMapping("/listCustomer")
    public List<CustomerInfoEntity> listCustomer() {
        return customerInfoService.list();
    }

    @PostMapping("/quoteCustomer")
    public Boolean quoteCustomer(@RequestBody List<String> ids) {
        return customerInfoService.quoteCustomer(ids);
    }

    @PostMapping("/ListCustomerAddressByIds")
    List<CustomerAddressEntity> ListCustomerAddressByIds(@RequestBody List<String> ids) {
        return customerAddressService.listByIds(ids);
    }

    @PostMapping("/listCustomerByIds")
    List<CustomerInfoEntity> listCustomerByIds(@RequestBody List<String> ids) {
        return customerInfoService.listByIds(ids);
    }

    /**
     * 客户列表审核
     * @Author Luo_WG
     * @Date 2023/7/4 12:28
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/approve")
    public Boolean approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        return customerInfoService.approve(dto);
    }

    /**
     * 售货员信息
     * @author yl
     * @date 2023-05-15 10:10
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SellerDTO.ViewDTO>
     */
    @PostMapping("/listSellerByMainId")
    public List<SellerDTO.ViewDTO> listSellerByMainId(@RequestBody String mainId) {
        return customerSellerService.listByMainId(mainId);
    }

    /**
     * 根据key 获取字典数据
     * @author yl
     * @date 2023-03-17 14:16
     * @param Key
     * @return java.util.List<com.erp.model.scm.dto.DictBasicDTO>
     */
    @PostMapping("/getDictBasicByKey")
    public List<DictBasicDTO.ViewDTO> getDictBasicByKey(@RequestBody String Key) {
        return dictBasicService.getByKey(Key);
    }

}
