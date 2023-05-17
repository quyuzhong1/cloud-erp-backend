package com.erp.rpc.oms.feign;

import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soReturn")
public interface SoReturnFeign {

    /**
     * 根据主键id查询销售退货单主表信息
     * @param id id
     * @return com.erp.model.oms.entity.SoReturnEntity
     * @Author Luo_WG
     * @Date 2023/5/15 18:17
     **/
    @PostMapping("feign/soReturn/getSoReturnById")
    SoReturnEntity getSoReturnById(@RequestBody String id);

    /**
     * 根据主键ids查询销售退货单主表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:17
     * @param ids ids
     * @return com.erp.model.oms.entity.SoReturnEntity
     **/
    @PostMapping("feign/soReturn/listByIds")
    List<SoReturnEntity> listByIds(@RequestBody List<String> ids);

    /**
     * 根据来源id查询销售退货单详情表信息
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoReturnDetailEntity>
     * @Author Luo_WG
     * @Date 2023/5/15 18:17
     **/
    @PostMapping("feign/soReturn/listDetailBySourceId")
    List<SoReturnDetailEntity> listDetailBySourceId(@RequestBody List<String> ids);

    /**
     * 根据详情id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/5/15 18:17
     * @param ids ids
     * @return java.util.List<com.erp.model.oms.entity.SoReturnDetailEntity>
     **/
    @PostMapping("feign/soReturn/listDetailByIds")
    List<SoReturnDetailEntity> listDetailByIds(@RequestBody List<String> ids);

    /***
     * 根据主表id查询详情表信息
     * @Author Luo_WG
     * @Date 2023/5/17 15:55
     * @param mainId
     * @return java.util.List<com.erp.model.oms.entity.SoReturnDetailEntity>
     **/
    @PostMapping("feign/soReturn/listDetailByMainId")
    List<SoReturnDetailEntity> listDetailByMainId(@RequestBody String mainId);

}
