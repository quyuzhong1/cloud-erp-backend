package com.erp.server.wms.controller.feign;

import com.erp.model.wms.entity.PackingTaskDetailEntity;
import com.erp.server.wms.service.PackingTaskDetailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 装箱任务明细Feign接口
 * @date 2024-08-31
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/packingTaskDetail")
public class PackingTaskDetailFeignController {

    @Resource
    private PackingTaskDetailService packingTaskDetailService;

    /**
     * 根据装箱任务ID查询明细
     */
    @GetMapping("/listByMainId")
    List<PackingTaskDetailEntity> listByMainId(@RequestParam String mainId){
        return packingTaskDetailService.lambdaQuery().eq(PackingTaskDetailEntity::getMainId, mainId).list();
    }
}
