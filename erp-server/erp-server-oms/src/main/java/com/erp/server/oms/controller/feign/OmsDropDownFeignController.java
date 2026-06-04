package com.erp.server.oms.controller.feign;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.server.oms.service.DictBasicService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 下拉列表
 */
@RestController
@RequestMapping("feign/drop/down")
public class OmsDropDownFeignController extends BaseController {

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 根据type返回树状结构
     *
     * @param key
     * @return
     */
    @GetMapping("/dict/tree")
    public List<BaseDropDownDTO.Tree> tree(@RequestParam("key") String key) {
        return dictBasicService.getTreeByKey(key);
    }

    /**
     * 根据类型和值获取到对应信息
     *
     * @param type
     * @param value
     * @return com.erp.model.oms.entity.DictBasicEntity
     */
    @GetMapping("/dict/getByTypeAndValue")
    public DictBasicEntity getByTypeAndValue(String type, String value) {
        return dictBasicService.getByTypeAndValue(type, value);
    }

    /**
     * 获取对应字典数据
     *  customerCompanyCategory  公司客户类别
     *  settleMode 客户结算方式
     *  collectionTerms 收款条件
     *  invoiceType 发票类型
     *  salesPlatform 销售平台
     *  soB2cBillStatus  b2c销售订单状态
     *  soB2cPayStatus  b2c销售订单付款状态
     *  soB2cAbnormalType  b2c销售订单异常信息
     *  soB2cLable  b2c销售订单标签
     *  logisticsMethod b2c销售订单物流方式
     *  shopAuthType 店铺授权类型
     *  invoiceTemplateType 发票模板类型
     *  invoiceType 发票类型
     *  invoiceStatus 发票状态
     *  uploadStatus 上传状态
     *  cfgInvoiceType 发票配置类型
     * @return
     */
    @GetMapping("/dict/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> list(@RequestParam("key") String key) {
        List<DictBasicEntity> list = dictBasicService.getByKey(key);
        //list 根据sort排序
        list = list.stream().sorted(Comparator.comparingInt(DictBasicEntity::getSort)).collect(Collectors.toList());
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getValue(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    @GetMapping("/dict/listInternalSalesPlatform")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listInternalSalesPlatform(@RequestParam("key") String key) {
        return success(dictBasicService.listInternalSalesPlatform(key));
    }
}
