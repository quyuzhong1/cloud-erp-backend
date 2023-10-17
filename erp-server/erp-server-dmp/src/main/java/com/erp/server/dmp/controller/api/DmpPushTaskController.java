package com.erp.server.dmp.controller.api;


import com.common.core.controller.BaseController;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.service.DmpOrderItemService;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 中台同步任务表
 *
 * @author Cloud
 * @since 2023-09-06
 */
@Slf4j
@RestController
@RequestMapping("/dmpPushTask")
public class DmpPushTaskController extends BaseController {

    @Autowired
    private DmpPushTaskService dmpPushTaskService;

    @Autowired
    private DmpOrderItemService dmpOrderItemService;



    @PostMapping("/test")
    public void test(){
        List<DmpOrderItemEntity> itemEntityList = dmpOrderItemService.listByIds(Arrays.asList("1679163638713159686"));
        List<DmpOrderItemEntity> itemEntityList1 = dmpOrderItemService.splitOrderItem(itemEntityList, PlatformEnum.MABANG.getDesc());
        System.out.println(itemEntityList1);
    }


}
