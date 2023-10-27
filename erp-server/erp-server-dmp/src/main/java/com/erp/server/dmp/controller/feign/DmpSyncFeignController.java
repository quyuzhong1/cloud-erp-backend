package com.erp.server.dmp.controller.feign;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.erp.model.dmp.dto.DmpSyncKingdeeDTO;
import com.erp.server.dmp.service.DmpSyncFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 金蝶同步feign
 * @author Will
 * @date: 2023/10/17 18:15
 */
@Slf4j
@RestController
@RequestMapping("feign/dmp/")
public class DmpSyncFeignController {
    @Resource
    private DmpSyncFeignService dmpSyncFeignService;

    /**
     * 查询金蝶数据
     * @param paramDTO
     * @return
     */
    @PostMapping("/sync/listKingdeeData")
    public List<Map<String, Object>> listKingdeeData(@RequestBody @Valid DmpSyncKingdeeDTO.ParamDTO paramDTO){
        List<Map<String, Object>> list = dmpSyncFeignService.listKingdeeData(paramDTO);
        return CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;
    }
}
