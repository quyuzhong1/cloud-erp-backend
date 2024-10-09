package com.erp.rpc.wms.feign;

import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 退货入库单 feign
 */
@FeignClient(name = "erp-wms", contextId = "soReturnInstock")
public interface SoReturnInstockFeign {
    /**
     * 获取退货入库单
     *
     * @param id
     * @return
     */
    @PostMapping("feign/soReturnInstock/getSoReturnInstockById")
    SoReturnInstockEntity getSoReturnInstockEntityById(@RequestParam(value = "id") String id);

    /**
     * 获取退货入库单 明细
     *
     * @param mainId
     * @return
     */
    @PostMapping("feign/soReturnInstock/getSoReturnInstockByMainId")
    List<SoReturnInstockDetailEntity> getSoReturnInstockDetailByMainId(@RequestParam(value = "mainId") String mainId);

    @PostMapping("feign/soReturnInstock/listDetailBySoReturnDetailIds")
    List<SoReturnInstockDetailEntity> listDetailBySoReturnDetailIds(@RequestBody List<String> detailIds);

    /**
     * 获取退货入库单 明细
     * @return
     */
    @PostMapping("feign/soReturnInstock/getSoReturnInstockByReturnIds")
    List<SoReturnInstockDetailEntity> getSoReturnInstockByReturnIds(@RequestBody List<String> returnIds);

}
