package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
    List<SoB2cReturnDetailEntity> listDetailByMainIds(@RequestBody List<String> mainIds);
}
