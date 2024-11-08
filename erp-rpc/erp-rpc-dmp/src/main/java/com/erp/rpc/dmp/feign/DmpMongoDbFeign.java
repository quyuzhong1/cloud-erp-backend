package com.erp.rpc.dmp.feign;


import com.erp.model.dmp.DmpPullOtherOutStockDTO;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * DMP远程调用mongodb
 *
 * @author Jim
 * @since 2023-11-08
 */
@FeignClient(value = "erp-dmp", path = "feign/mongodb", contextId = "DmpMongodbFeign")
public interface DmpMongoDbFeign {

    /**
     * 更新mongodb数据状态
     *
     * @Author Jim
     * @since 2023-10-10
     **/
    @PostMapping("/updateMongoDbData")
    void updateMongoDbData(@RequestBody MongoDBUpdateDTO dto);


    /**
     * 查询mongodb是否有销售出库单
     *
     * @Author Jim
     * @since 2024-01-04
     **/
    @GetMapping("/checkHasDeliveryDetail")
    Boolean checkHasDeliveryDetail(@RequestParam(value = "platformCode") String platformCode, @RequestParam(value = "platform") String platform);

    /**
     * 查询mongodb是否有销售出库单
     *
     * @Author Jim
     * @since 2024-02-14
     **/
    @PostMapping("/checkSoOutStock")
    Boolean checkSoOutStock(@RequestBody DmpPullSoOutStockDTO resultDTO);

    /**
     * 查询mongodb是生成其他出库单
     *
     * @Author Jim
     * @since 2024-02-14
     **/
    @PostMapping("/checkOtherOutStock")
    Boolean checkOtherOutStock(@RequestBody DmpPullOtherOutStockDTO resultDTO);

    /**
     * 获取mongo DB中track123数据
     * @param trackNoList
     * @return
     */
    @PostMapping("/listMongoTractDataByTrackNoList")
    String listMongoTractDataByTrackNoList(@RequestBody List<String> trackNoList);
}