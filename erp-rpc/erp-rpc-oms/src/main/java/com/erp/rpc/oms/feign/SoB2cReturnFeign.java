package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.oms.dto.SoB2cReturnDetailDTO;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soB2cReturnFeign",configuration = {FeignErrorDecoder.class})
public interface SoB2cReturnFeign {
    /**
     * 根据主键ids查询销售退货单主表信息
     * @Author zdy
     * @Date 2023/5/15 18:17
     * @param ids ids
     * @return com.erp.model.oms.entity.SoReturnEntity
     **/
    @PostMapping("feign/soB2cReturn/listByIds")
    List<SoB2cReturnEntity> listByIds(@RequestBody List<String> ids);

    /**
     * 根据退货物流单号查询B2C销售退货单（可能多条，按创建时间倒序）
     * @param returnLogisticCode 退货物流单号
     * @return java.util.List<com.erp.model.oms.entity.SoB2cReturnEntity>
     **/
    @PostMapping("feign/soB2cReturn/listByReturnLogisticCode")
    List<SoB2cReturnEntity> listByReturnLogisticCode(@RequestBody String returnLogisticCode);

    /**
     * 按平台订单号/平台退货单号/销售单号/退货单号中任一字段匹配参考单号（一次查询，字段间为 OR 关系）
     * @param referenceNo 参考单号
     * @return 命中的退货单列表
     */
    @PostMapping("feign/soB2cReturn/listByAnyReferenceNo")
    List<SoB2cReturnEntity> listByAnyReferenceNo(@RequestBody String referenceNo);

    /**
     * 根据详情id查询详情表信息
     * @Author zdy
     * @Date 2023/5/15 18:17
     * @param detailIds ids
     * @return java.util.List<com.erp.model.oms.entity.SoReturnDetailEntity>
     **/
    @PostMapping("feign/soB2cReturn/listDetailByIds")
    List<SoB2cReturnDetailEntity> listDetailByIds(@RequestBody List<String> detailIds);


    /***
     * 根据主表ids查询详情表信息
     * @Author zdy
     * @Date 2023/5/17 15:55
     * @param mainIds
     * @return java.util.List<com.erp.model.oms.entity.SoReturnDetailEntity>
     **/
    @PostMapping("feign/soB2cReturn/listDetailByMainIds")
    List<SoB2cReturnDetailDTO.ViewDTO> listDetailByMainIds(@RequestBody List<String> mainIds);


    @PostMapping("feign/soB2cReturn/updateBatch")
    void updateBatch(@RequestBody List<SoB2cReturnEntity> list);
}
