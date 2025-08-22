package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.entity.PackingTaskDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 装箱任务明细Feign
 * @date 2024-08-31
 * @author tanmujin
 */
@FeignClient(name = "erp-wms", path = "/feign/packingTaskDetail", contextId = "packingTaskDetailFeign" ,configuration = {FeignErrorDecoder.class})
public interface PackingTaskDetailFeign {

    /**
     * 根据装箱任务ID查询明细
     */
    @GetMapping("/listByMainId")
    List<PackingTaskDetailEntity> listByMainId(@RequestParam String mainId);
}
