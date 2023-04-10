package com.erp.server.scm;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.workflow.dto.AuditorHandleDTO;
import com.erp.server.workflow.service.ProcessTaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author Lambda
 * @Classname ErpServerScmApplicationTests
 * @Description TODO
 * @Date 2023-04-03 9:21
 * @Created by yl
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWorkApplicationTests.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerWorkApplicationTests {

    @Autowired
    private ProcessTaskService processTaskService;


    @Test
    public void test() {
        List<AuditorHandleDTO> resultList =    processTaskService.getHistoryTaskByProcessId("fd226ba4-d76a-11ed-901e-2ed55b4d95a2");

        System.out.println(JSONObject.toJSON(resultList));
    }



}
