package com.erp.server.wms.controller.feign;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.server.wms.service.PackingTaskService;
import com.erp.server.wms.service.WmsCartonSpecService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("装箱任务")
@RequestMapping("/feign/packingTask")
public class PackingTaskFeignController extends BaseController {

    @Resource
    private PackingTaskService packingTaskService;
    @Resource
    private WmsCartonSpecService wmsCartonSpecService;


    /**
     * 设备扫描称重
     */
    @PostMapping("/dimensionalWeight")
    public ApiResult<String> dimensionalWeight(@RequestBody @Validated DimensionalWeightDTO dto) {
        try {
            return packingTaskService.dimensionalWeight(dto);
        }catch (Exception e){
            log.error(CharSequenceUtil.format("大货称重异常,json:{}", JSONUtil.toJsonStr(dto)),e);
            return ApiResult.error(CharSequenceUtil.format("系统异常:{}", e.getMessage()));
        }
    }

    /**
     * 根据任务ID批量查询装箱信息
     * @param taskIds 任务ID集合
     */
    @PostMapping("/listCartonSpecByTaskIds")
    List<WmsCartonSpecDTO.WmsCartonSpecView> listCartonSpecByTaskIds(@RequestBody List<String> taskIds){
        List<WmsCartonSpecDTO.WmsCartonSpecView> resultList = new ArrayList<>(taskIds.size());
        List<PackingTaskEntity> packingTaskList = packingTaskService.listByIds(taskIds);
        for (PackingTaskEntity taskEntity : packingTaskList) {
            WmsCartonSpecDTO.WmsCartonSpecView cartonSpecView = wmsCartonSpecService.getCartonViewByPackingTaskId(taskEntity);
            resultList.add(cartonSpecView);
        }
        return resultList;
    }

    /**
     * 根据来源ID查询
     */
    @GetMapping("/getBySourceId")
    PackingTaskEntity getBySourceId(@RequestParam String sourceId){
        return packingTaskService.lambdaQuery().eq(PackingTaskEntity::getSourceId, sourceId).one();
    }

}
