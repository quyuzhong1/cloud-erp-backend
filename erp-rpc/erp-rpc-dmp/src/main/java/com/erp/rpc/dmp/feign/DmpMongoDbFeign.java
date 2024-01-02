package com.erp.rpc.dmp.feign;


import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}