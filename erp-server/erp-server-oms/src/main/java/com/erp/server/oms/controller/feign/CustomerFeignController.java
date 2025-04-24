package com.erp.server.oms.controller.feign;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.vo.CustomerInfoVO;
import com.erp.server.oms.service.CustomerAddressService;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.CustomerSellerService;
import com.erp.server.oms.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("feign/customer")
@Slf4j
public class CustomerFeignController extends BaseController {
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
     *
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     * @Author Luo_WG
     * @Date 2023/5/17 18:41
     **/
    @PostMapping("/listCustomer")
    public List<CustomerInfoEntity> listCustomer() {
        return customerInfoService.list();
    }

    /**
     * 获取所有客户等级信息
     *
     * @return java.util.List<com.erp.model.oms.vo.CustomerInfoVO>
     * @Author zdy
     * @Date 2023/10/08 18:41
     **/
    @PostMapping("/listCustomerByGroup")
    public List<CustomerInfoVO> listCustomerByGroup() {
        return customerInfoService.listCustomerByGroup();
    }

    /**
     * 获取客户属性信息
     *
     * @return com.erp.model.oms.vo.CustomerInfoVO
     * @Author zdy
     * @Date 2023/10/08 18:18
     **/
    @PostMapping("/listCustomerByProperty")
    public List<CustomerInfoVO> listCustomerByProperty() {
        return customerInfoService.listCustomerByProperty();
    }

    @PostMapping("/quoteCustomer")
    public Boolean quoteCustomer(@RequestBody List<String> ids) {
        return customerInfoService.quoteCustomer(ids);
    }

    @PostMapping("/listCustomerAddressByIds")
    public List<CustomerAddressEntity> listCustomerAddressByIds(@RequestBody List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return customerAddressService.listByIds(ids);
    }

    @PostMapping("/listCustomerByIds")
    public List<CustomerInfoEntity> listCustomerByIds(@RequestBody List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return customerInfoService.listByIds(ids);
    }

    /**
     * 根据客户id查询店铺负责人和部门
     * @Author Luo_WG
     * @Date 2024/4/1 15:17
     * @param codeList
     * @return java.util.List<com.erp.model.oms.entity.CustomerInfoEntity>
     **/
    @PostMapping("/listSellerUserDepByCodes")
    public List<CustomerDTO.SellerUserDeptDTO> listSellerUserDepByCodes(@RequestBody List<String> codeList) {
        return customerInfoService.listSellerUserDepByCodes(codeList);
    }

    /**
     * 根据国家ids查询客户信息
     *
     * @param ids
     * @return List<CustomerInfoEntity>
     * @author Will
     * @date: 2023/7/24 12:28
     */
    @PostMapping("/listByCountryIdList")
    List<CustomerInfoEntity> listByCountryIdList(@RequestBody List<String> ids) {
        return customerInfoService.listByCountryIdList(ids);
    }

    /**
     * 客户列表审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/7/4 12:28
     **/
    @PostMapping("/approve")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<CustomerInfoEntity> entityList = customerInfoService.listByIds(ids);
        for (String id : ids) {
            CustomerInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"客户信息不存在"));
                continue;
            }
            try {
                resultDTOS.add(customerInfoService.approve(dto, entity));
            }catch (Exception e){
                log.error("B2B客户审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 售货员信息
     *
     * @param mainId
     * @return java.util.List<com.erp.model.oms.dto.SellerDTO.ViewDTO>
     * @author yl
     * @date 2023-05-15 10:10
     */
    @PostMapping("/listSellerByMainId")
    public List<SellerDTO.ViewDTO> listSellerByMainId(@RequestBody String mainId) {
        return customerSellerService.listByMainId(mainId);
    }

    /**
     * 根据key 获取字典数据
     *
     * @param key
     * @return java.util.List<com.erp.model.scm.dto.DictBasicDTO>
     * @author yl
     * @date 2023-03-17 14:16
     */
    @PostMapping("/getDictBasicByKey")
    public List<DictBasicDTO.ViewDTO> getDictBasicByKey(@RequestBody String key) {
        return dictBasicService.getByKey(key);
    }

    /**
     * 根据名称获取用户详情
     *
     * @param customerName
     * @return
     */
    @PostMapping("/getCustomerByName")
    public CustomerInfoEntity getCustomerByName(@RequestBody String customerName) {
        return customerInfoService.getCustomerByName(customerName);
    }

    /**
     * 根据名称获取用户详情
     *
     * @param id
     * @return
     */
    @PostMapping("/getCustomerById")
    public CustomerInfoEntity getCustomerById(@RequestBody String id) {
        return customerInfoService.getCustomerById(id);
    }

    /**
     * 根据客户名称list获取客户详情list
     *
     */
    @PostMapping("/listDTOByNameList")
    public List<CustomerDTO.ReceiveInfoDTO> listDTOByNameList(@RequestBody List<String> customerNameList) {
        return customerInfoService.listDTOByNameList(customerNameList);
    }

    /**
     * 根据名称获取用户详情
     *
     * @param customerName
     * @return
     */
    @PostMapping("/getCustomerByCodeAndName")
    public List<CustomerInfoEntity> getCustomerByCodeAndName(@RequestParam(value = "code") String code,@RequestParam(value = "name") String name) {
        return customerInfoService.lambdaQuery().eq(CustomerInfoEntity::getCode, code).eq(CustomerInfoEntity::getName, name).list();
    }
}
