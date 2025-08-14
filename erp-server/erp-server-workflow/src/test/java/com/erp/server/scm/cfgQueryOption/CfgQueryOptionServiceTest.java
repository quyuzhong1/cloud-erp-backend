package com.erp.server.scm.cfgQueryOption;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.server.workflow.service.CfgQueryOptionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import java.sql.*;
import java.util.*;

@Slf4j
@RunWith(SpringRunner.class)
@ImportAutoConfiguration(exclude = {
        org.camunda.bpm.spring.boot.starter.rest.CamundaBpmRestJerseyAutoConfiguration.class
})
@SpringBootTest(classes = {CfgQueryOptionServiceTest.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ComponentScan(basePackages = {"com.erp.*","com.common.*"})
@Profile("dev")
public class CfgQueryOptionServiceTest {

    @Autowired
    private CfgQueryOptionService cfgQueryOptionService;

    /**
     * 数据库用户名
     */
    private static final String DB_USER_NAME = "postgres";
    /**
     * 数据库密码
     */
    private static final String DB_PASSWORD = "admin@viji";


    public CfgQueryOptionServiceTest(){

    }

    /**
     * 测试 genBySql 方法
     */
    @Test
    @Transactional(rollbackFor =Exception.class)
    public void testGenBySql() {
        String sql = "SELECT obj_description(cls.oid) AS table_comment,cls.relname, col.attnum AS ordinal_position, col.attname AS COLUMN_NAME, format_type(col.atttypid, col.atttypmod) AS data_type, NOT col.attnotnull AS is_nullable, des.description AS column_comment FROM pg_attribute col JOIN pg_class cls ON col.attrelid = cls.OID JOIN pg_namespace ns ON cls.relnamespace = ns.OID LEFT JOIN pg_description des ON des.objoid = col.attrelid AND des.objsubid = col.attnum WHERE cls.relname = '{}' AND col.attnum > 0 and col.attname not in ('create_user_id','create_user_name','create_time','update_user_id','update_user_name','update_time','version','is_deleted') AND NOT col.attisdropped ORDER BY col.attnum;";

        String url = "jdbc:postgresql://172.16.100.60:32590/erp-{}?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false";

        // 准备测试数据
        CfgQueryOptionDTO.GenDTO dto = new CfgQueryOptionDTO.GenDTO();
        //系统
        dto.setModel("plm");
        //表名, 多个使用英文逗号隔开
        dto.setTableName("pilot_application,pilot_application_detail11");
        //表归属类型：main（主表）,detailList(明细)，自定义名称（根据实际单据）
        dto.setFieldBelongsType("main,detailList");
        //业务key，跟流程单据key保持一致（menu）
        dto.setBussinessKey("pilotApplication");
        //使用类型，CfgQueryOptionUseTypeEnum枚举
        dto.setUseType("testtttt");

        dto.setSql(sql);
        dto.setUrl(url);
        dto.setAccount(DB_USER_NAME);
        dto.setPassword(DB_PASSWORD);
        // 调用被测试的方法
        cfgQueryOptionService.genBySql(dto);
    }


}