package com.erp.server.dmp.pull.schedule;

import com.erp.server.dmp.ErpServerDmpApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class PullGoodCangJobTest {

    @Resource
    private PullShopeeJob pullShopeeJob;
    @Test
    public void execute() {
        pullShopeeJob.execute();
    }

}