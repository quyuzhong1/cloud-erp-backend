package com.erp.server.scm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.workflow.dto.AuditorHandleDTO;
import com.erp.server.workflow.service.ProcessTaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lambda
 * @Classname ErpServerScmApplicationTests

 * @Date 2023-04-03 9:21
 * @Created by yl
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWorkApplicationTests.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerWorkApplicationTests {

    @Resource
    private ProcessTaskService processTaskService;


    @Test
    public void testAuditorHandleDTO() {
        List<AuditorHandleDTO> resultList =  processTaskService.getHistoryTaskByProcessId("fd226ba4-d76a-11ed-901e-2ed55b4d95a2");
        log.warn(JSON.toJSONString(resultList));
    }



}
