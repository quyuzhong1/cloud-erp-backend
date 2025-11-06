package com.erp.server.scm.cfgQueryOption;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.entity.CfgQueryOptionExtEntity;
import com.erp.model.workflow.enums.CfgQueryOptionUseTypeEnum;
import com.erp.server.workflow.service.CfgQueryOptionExtService;
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
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Autowired
    private CfgQueryOptionExtService cfgQueryOptionExtService;

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
    public void testGenBySql() {
        String sql = "SELECT obj_description(cls.oid) AS table_comment,cls.relname, col.attnum AS ordinal_position, col.attname AS COLUMN_NAME, format_type(col.atttypid, col.atttypmod) AS data_type, NOT col.attnotnull AS is_nullable, des.description AS column_comment FROM pg_attribute col JOIN pg_class cls ON col.attrelid = cls.OID JOIN pg_namespace ns ON cls.relnamespace = ns.OID LEFT JOIN pg_description des ON des.objoid = col.attrelid AND des.objsubid = col.attnum WHERE cls.relname = '{}' AND col.attnum > 0 and col.attname not in ('create_user_id','create_user_name','create_time','update_user_id','update_user_name','update_time','version','is_deleted') AND NOT col.attisdropped ORDER BY col.attnum;";

        String url = "jdbc:postgresql://172.16.100.60:32590/erp-{}?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false";

        // 准备测试数据
        CfgQueryOptionDTO.GenDTO dto = new CfgQueryOptionDTO.GenDTO();
        //系统
        dto.setModel("oms");
        //表名, 多个使用英文逗号隔开
        dto.setTableName("so_multi_channel,so_multi_channel_detail");
        //表归属类型：main（主表）,detailList(明细)，自定义名称（根据实际单据）
        dto.setFieldBelongsType("main,detailList");
        //业务key，跟流程单据key保持一致（menu）
        dto.setBussinessKey("soMultiChannel");
        //使用类型，CfgQueryOptionUseTypeEnum枚举
        dto.setUseType("cfgApproveSync");

        dto.setSql(sql);
        dto.setUrl(url);
        dto.setAccount(DB_USER_NAME);
        dto.setPassword(DB_PASSWORD);
        // 调用被测试的方法
        cfgQueryOptionService.genBySql(dto);
    }


    /**
     * 测试 genBySql 方法
     */
    @Test
    public void testGenBySql111() {
        List<String> list = Arrays.asList("1968526068663963650","1968526068714295299","1968527867730980865","1968527867772923907","1968531372357197827","1968531372357197831","1968566215250051075","1968566215250051079","1968566215250051084","1968566215296188417","1968566215296188420","1968566215296188423","1968566215296188427","1968566216386707458","1968582099003809794","1968582099003809798","1968582099054141443","1968582099054141450","1968582099054141451","1968582099372908552");

        List<CfgQueryOptionExtEntity> cfgQueryOptionExtEntities = cfgQueryOptionExtService.lambdaQuery().in(CfgQueryOptionExtEntity::getCfgQueryOptionId, list).list();
        Map<String, CfgQueryOptionExtEntity> cfgQueryOptionExtMap = cfgQueryOptionExtEntities.stream().collect(Collectors.toMap(CfgQueryOptionExtEntity::getCfgQueryOptionId, Function.identity()));

        List<CfgQueryOptionEntity> cfgQueryOptionEntities = cfgQueryOptionService.lambdaQuery().like(CfgQueryOptionEntity::getBussinessKey, "%sample%").list();
        for (CfgQueryOptionEntity cfgQueryOptionEntity : cfgQueryOptionEntities) {
            CfgQueryOptionExtEntity cfgQueryOptionExtEntity = cfgQueryOptionExtMap.get(cfgQueryOptionEntity.getId());

            String idStr = IdWorker.getIdStr();

            cfgQueryOptionEntity.setUseType(CfgQueryOptionUseTypeEnum.ALL_DATA.getCode());
            cfgQueryOptionEntity.setId(idStr);
            cfgQueryOptionService.save(cfgQueryOptionEntity);
            if(Objects.nonNull(cfgQueryOptionExtEntity)){
                cfgQueryOptionExtEntity.setId(IdWorker.getIdStr());
                cfgQueryOptionExtEntity.setCfgQueryOptionId(idStr);
                cfgQueryOptionExtService.insertBatch(cfgQueryOptionExtEntity);
            }


        }





    }
}