package com.erp.server.sys.generator;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.PostgreSqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.PostgreSqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.keywords.PostgreSqlKeyWordsHandler;
import com.common.core.controller.BaseController;
import com.common.core.entity.BaseEntity;
import com.common.business.service.SuperService;
import com.common.business.service.SuperServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 代码生成器
 * @author Cloud
 */
@SpringBootTest
@Slf4j
public class MyBatisGeneratorRun {
    // 项目路径
    private static final String PROJECT_PATH = System.getProperty("user.dir").replace("\\erp-server-sys","");
    // 当前环境是否Windows
    private static final boolean IS_WINDOWS = System.getProperty("os.name").startsWith("Windows");
    // 数据库链接配置
    static String MODEL = "sys";
    private static String DB_URL = StrUtil.format("jdbc:postgresql://172.16.100.12:5432/erp-{}?useSSL=false&serverTimezone=GMT%2B8", MODEL);
    private static final String USER_NAME = "postgres";
    private static final String PASSWORD = "admin@viji";

    //指定server包名
    private static String BASE_PACKAGE_NAME = StrUtil.format("com.erp.server.{}", MODEL);
    //指定Model包名 com.erp.model.plm
    private static String BASE_PACKAGE_MODEL_NAME = StrUtil.format("com.erp.model.{}", MODEL);
    //模块名 如果有模块名，则需在模块名前加. 例：.log
    private static final String MODULE_NAME = StrUtil.format("erp-model-{}", MODEL);

    private static final String SERVER_NAME = StrUtil.format("erp-server-{}", MODEL);
    //作者名
    private static final String AUTHOR = "Lambda";
    // 输出路径(为空默认为项目路径)
    private static final String OUTPUT_DIR = "";

    @Test
    void run() {
        // 表前缀
        String prefix = "";

        String[] tables = new String[]{"dict_city","dict_country","dict_country","dict_global_area"};
        autoGenerator(prefix, tables);


    }

    @Test
    void runByScanner() {
        // 控制台输入
        String tableNameStr = scanner("表名(多表逗号分隔)");
        String[] tableNames = tableNameStr.split(",");
        String tablePrefix = scanner("表前缀(无前缀输入#)").replaceAll("#", "");
        autoGenerator(tablePrefix, tableNames);
    }

    private static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入 " + tip + " : ");
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StrUtil.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的 " + tip + ". ");
    }

    /**
     * 根据表自动生成
     */
    private static void autoGenerator(String tableNamePrefix, String... tableNames) {
        //配置数据源
        new AutoGenerator(dataSourceConfig().build())
            // 全局变量配置
            .global(globalConfig())
            // 包名配置
            .packageInfo(packageConfig())
            // 策略配置
            .strategy(strategyConfig(tableNamePrefix, tableNames))
            // 自定义模板配置
            .template(templateConfig())
            // 注入自定义配置
            .injection(getInjectionConfig())
            // 指定模板引擎 默认是VelocityTemplateEngine ，需要引入相关引擎依赖
            .execute(new FreemarkerTemplateEngine());
    }

    /**
     * 配置数据源
     */
    private static DataSourceConfig.Builder dataSourceConfig() {
        return new DataSourceConfig.Builder(DB_URL, USER_NAME, PASSWORD)
            // 数据库类型
            .dbQuery(new PostgreSqlQuery())
            // 数据库类型转换器
            .typeConvert(new PostgreSqlTypeConvert())
            // 数据库关键字处理器
            .keyWordsHandler(new PostgreSqlKeyWordsHandler())
            ;
    }

    /**
     * 设置包名
     */
    private static PackageConfig packageConfig() {
        String xmlPath = PROJECT_PATH + "/" + SERVER_NAME + "/src/main/resources/mapper/";
//        String entityPath = PROJECT_PATH.replace("erp-server", "erp-model") + "/" + MODULE_NAME + "/src/main/java/com/erp/model/" + MODEL+"/entity";
        if (StringUtils.isNotBlank(OUTPUT_DIR)) {
            xmlPath = OUTPUT_DIR;
        }
        if (IS_WINDOWS) {
            xmlPath = xmlPath.replaceAll("/+|\\\\+", "\\\\");
//            entityPath = entityPath.replaceAll("/+|\\\\+", "\\\\");
        } else {
            xmlPath = xmlPath.replaceAll("/+|\\\\+", "/");
//            entityPath = entityPath.replaceAll("/+|\\\\+", "/");
        }
        Map<OutputFile, String> pathInfo = new HashMap<>();
        // 自定义XML输出路径
//        pathInfo.put(OutputFile.entity, entityPath);
        pathInfo.put(OutputFile.mapperXml, xmlPath);
        PackageConfig build = new PackageConfig.Builder()
                .parent(BASE_PACKAGE_NAME)
                .moduleName("")
                .controller("controller")
//            .entity("entity")
                .mapper("mapper")
//            .xml("mapper.impl")
                .service("service")
                .serviceImpl("service.impl")
                .pathInfo(pathInfo)
                .build();
        return build;
    }

    /**
     * 全局配置
     */
    private static GlobalConfig globalConfig() {
        String filePath = PROJECT_PATH + "/" + SERVER_NAME + "/src/main/java/";
        if (StringUtils.isNotBlank(OUTPUT_DIR)) {
            filePath = OUTPUT_DIR;
        }
        if (IS_WINDOWS) {
            filePath = filePath.replaceAll("/+|\\\\+", "\\\\");
        } else {
            filePath = filePath.replaceAll("/+|\\\\+", "/");
        }

        return new GlobalConfig.Builder()
            //设置输出路径
            .outputDir(filePath)
            // 关闭-打开输出目录
            .disableOpenDir()
            // 开启-覆盖已有文件
            .fileOverride()
            // 开启 swagger2 模式
//            .enableSwagger()
            // 时间类型对应策略
            .dateType(DateType.ONLY_DATE)
            // 开发人员
            .author(AUTHOR)
            .build();
    }

    /**
     * 策略配置
     */
    private static StrategyConfig strategyConfig(String tableNamePrefix, String... tableNames) {
        StrategyConfig.Builder builder = new StrategyConfig.Builder()
            // 开启全局大写命名 ORACLE 注意
//            .enableCapitalMode()
            // 表前缀
            .addTablePrefix(tableNamePrefix)
            // 需要生成的的表名，多个表名传数组
            .addInclude(tableNames);

        // 实体策略配置
        builder.entityBuilder()
            // 数据库表映射到实体的命名策略, 默认下划线转驼峰命名
//            .naming(NamingStrategy.underline_to_camel)
            // 数据库表字段映射到实体的命名策略, 未指定按照 naming 执行
//            .columnNaming(NamingStrategy.underline_to_camel)
            // 【父类】Entity
            .superClass(BaseEntity.class)
            // 开启链式模型
            .enableChainModel()
            // 开启 lombok 模型
            .enableLombok()
            // 开启 ActiveRecord 模型
            .enableActiveRecord()
            // 开启 Boolean 类型字段移除 is 前缀
//            .enableRemoveIsPrefix()
            // 开启生成实体时生成字段注解
            .enableTableFieldAnnotation()
            // 开启生成字段常量
            .enableColumnConstant()
            // 禁用生成 serialVersionUID
            .disableSerialVersionUID()
            // 乐观锁字段名(数据库)
            .versionColumnName("version")
            // 乐观锁属性名(实体)
            .versionPropertyName("version")
            // 逻辑删除字段名(数据库)
            .logicDeleteColumnName("is_deleted")
            // 逻辑删除属性名(实体)
            .logicDeletePropertyName("isDeleted")
            // 【父类】Entity中的公共字段
            .addSuperEntityColumns("id", "create_time", "update_time", "version", "is_deleted","deleted_user_id", "deleted_time")
            // 全局主键类型
            .idType(IdType.ASSIGN_ID)
            .formatFileName("%sEntity");

        // controller 策略配置
        builder.controllerBuilder()
            // 设置父类
            .superClass(BaseController.class)
            // 开启驼峰转连字符
            .enableHyphenStyle()
            // 开启生成 @RestController 控制器
            .enableRestStyle()
            .formatFileName("%sController");

        // service 策略配置
        builder.serviceBuilder()
            // 设置 service 接口父类
            .superServiceClass(SuperService.class)
            // 设置 service 实现类父类
            .superServiceImplClass(SuperServiceImpl.class)
            .formatServiceFileName("%sService")
            .formatServiceImplFileName("%sServiceImpl");

        // mapper 策略配置
        builder.mapperBuilder()
            // 设置父类
//            .superClass()
            // 启用 BaseResultMap 生成
            .enableBaseResultMap()
            // 启用 BaseColumnList 生成
//            .enableBaseColumnList()
            // 设置缓存实现类
//            .cache()
            .formatMapperFileName("%sMapper")
            .formatXmlFileName("%sMapper");

        return builder.build();
    }

    /**
     * 注入自定义配置
     */
    private static InjectionConfig getInjectionConfig() {
        return  new InjectionConfig.Builder().build();
    }

    /**
     * 自定义模板配置
     */
    private static TemplateConfig templateConfig() {
        return new TemplateConfig.Builder()
//            .setController("/templates-generator/controller.java.vm")
//            .setService("/templates-generator/service.java.vm")
//            .setServiceImpl("/templates-generator/serviceImpl.java.vm")
//            .setEntity("/templates-generator/entity.java.vm")
//            .setMapper("/templates-generator/mapper.java.vm")
//            .setXml("/templates-generator/mapper.xml.vm")
            .build();
    }
}