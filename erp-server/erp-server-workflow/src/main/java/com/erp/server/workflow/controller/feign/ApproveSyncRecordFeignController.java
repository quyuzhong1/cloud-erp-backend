package com.erp.server.workflow.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.common.core.controller.BaseController;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.ApproveSyncRecordEntity;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgQueryOptionExtendTypeEnum;
import com.erp.server.workflow.service.ApproveSyncRecordService;
import com.erp.server.workflow.service.CfgQueryOptionExtService;
import com.erp.server.workflow.service.CfgQueryOptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * approveSyncRecord Feign
 * @date 2025-11-17
 * @author jack
 */
@RestController
@RequestMapping("feign/approveSyncRecord")
@Slf4j
public class ApproveSyncRecordFeignController extends BaseController {

    @Resource
    private ApproveSyncRecordService approveSyncRecordService;

    /**
     */
    @PostMapping("/add")
    public void add(@RequestBody ApproveSyncRecordEntity approveSyncRecordEntity) {
       approveSyncRecordService.save(approveSyncRecordEntity);
    }

    /**
     */
    @PostMapping("/updateById")
    public void updateById(@RequestBody ApproveSyncRecordEntity approveSyncRecordEntity) {
       approveSyncRecordService.updateById(approveSyncRecordEntity);
    }


}
