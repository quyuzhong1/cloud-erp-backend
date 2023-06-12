package com.baomidou.mybatisplus;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.PostgreSqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * mybatisplus自动生成工具
 * @author zhangchunlin
 * @since 2023-05-28
 */
public class Generator {

    /**
       模块名（需要更改）
     */
    private static final String MODEL = "plm";
    /**
     * 作者（需要更改）
     */
    private static final String AUTHOR = "Luo_WG";
    /**
     * 项目路径
     */
    private static final String PROJECT_PATH = System.getProperty("user.dir");
    /**
     * 当前环境是否Windows
     */
    private static final boolean IS_WINDOWS = System.getProperty("os.name").trim().toLowerCase().contains("windows");
    /**
     * 指定server包名
     */
    private static String BASE_PACKAGE_NAME = StrUtil.format("com.erp.server.{}", MODEL);
    /**
     * 指定Model包名 如com.erp.model.wms
     */
    private static String BASE_PACKAGE_MODEL_NAME = StrUtil.format("com.erp.model.{}", MODEL);
    private static final String BASE_MODEl_PROJECT_NAME = "erp-model";
    /**
     * 模块名
     */
    private static final String MODULE_NAME = StrUtil.format("erp-model-{}", MODEL);
    /**
     * 服务名
     */
    private static final String SERVER_NAME = StrUtil.format("erp-server-{}", MODEL);
    /**
     * 输出路径(为空默认为项目路径)
     */
    private static final String OUTPUT_DIR = "";
    /**
     * Service接口是否带前缀I，设置成false -> UserService, 设置成true -> IUserService
     */
    private static final boolean serviceNameStartWithI = false;

    /**
     * 数据库链接
     */
    private static final String DB_URL = "jdbc:postgresql://172.16.100.12:5432/" + StrUtil.format( "erp-{}", MODEL) + "?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false";
    /**
     * 数据库用户名
     */
    private static final String DB_USER_NAME = "postgres";
    /**
     * 数据库密码
     */
    private static final String DB_PASSWORD = "admin@viji";

    public static void main(String[] args) {
        // 需要生成的表名（特别注意：请确保生成多个表时在同一个数据库，如果一次性生成多个，中间有异常不会中断后续生成）
        // 现设置的是文件不覆盖，即生成时如果已经存在该文件则不会生成导致覆盖，设置成true覆盖，如果需要覆盖请将全局配置fileOverride设置成true
        String[] tableNames = {"product_customs"};
        generateByTables(tableNames);
    }

    /**
     * @param tableNames 表名
     */
    private static void generateByTables(String... tableNames) {
        String filePath = PROJECT_PATH + "/" + "erp-server" + "/" + SERVER_NAME + "/src/main/java/";
        if (StringUtils.isNotEmpty(OUTPUT_DIR)) {
            filePath = OUTPUT_DIR;
        }
        if (IS_WINDOWS) {
            filePath = filePath.replaceAll("/+|\\\\+", "\\\\");
        } else {
            filePath = filePath.replaceAll("/+|\\\\+", "/");
        }

        GlobalConfig config = new GlobalConfig();

        config.setAuthor(AUTHOR)
                .setActiveRecord(true)
                .setOutputDir(filePath)
                // 设置成false如果存在相同文件则不会生成
                .setFileOverride(false)
                .setEnableCache(false)
                .setSwagger2(false)
                .setBaseResultMap(true)
                .setBaseColumnList(true)
                // 控制器数据权限注解生成需要用到
                .setModelName(MODEL)
                // 控制器是否生成数据权限注解信息（同时表包含approve_status和code字段时会生成权限注解信息）
                .setDataPermission(true);
        if (!serviceNameStartWithI) {
            config.setServiceName("%sService");
        }
        // 实体名后带Entity
        config.setEntityName("%sEntity");
        new AutoGenerator().setGlobalConfig(config)
                .setDataSource(dataSourceConfig())
                .setStrategy(strategyConfig(tableNames))
                .setPackageInfo(packageConfig())
                .setTemplateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    @SneakyThrows
    private static PackageConfig packageConfig() {
        String xmlPath = PROJECT_PATH + "/"  + "erp-server" + "/" + SERVER_NAME + "/src/main/resources/mapper/";
        String entityPath = PROJECT_PATH.replace("erp-server", "erp-model") + "/" +  BASE_MODEl_PROJECT_NAME + "/" + MODULE_NAME + "/src/main/java/com/erp/model/" + MODEL+"/entity";
        String controllerPath = PROJECT_PATH + "/"  + "erp-server" + "/" + SERVER_NAME + "/src/main/java/com/erp/server/" + MODEL + "/" + "controller/api";
        String servicePath = PROJECT_PATH + "/"  + "erp-server" + "/" + SERVER_NAME + "/src/main/java/com/erp/server/" + MODEL + "/" + "service";
        String serviceImplPath = PROJECT_PATH + "/"  + "erp-server" + "/" + SERVER_NAME + "/src/main/java/com/erp/server/" + MODEL + "/" + "service" + "/" + "impl";
        String mapperPath = PROJECT_PATH + "/"  + "erp-server" + "/" + SERVER_NAME + "/src/main/java/com/erp/server/" + MODEL + "/" + "mapper";
        String dtoPath = PROJECT_PATH.replace("erp-server", "erp-model") + "/" +  BASE_MODEl_PROJECT_NAME + "/" + MODULE_NAME + "/src/main/java/com/erp/model/" + MODEL+"/dto";
        if (IS_WINDOWS) {
            xmlPath = xmlPath.replaceAll("/+|\\\\+", "\\\\");
            entityPath = entityPath.replaceAll("/+|\\\\+", "\\\\");
        } else {
            xmlPath = xmlPath.replaceAll("/+|\\\\+", "/");
            entityPath = entityPath.replaceAll("/+|\\\\+", "/");
        }
        Map<String, String> pathInfo = new HashMap<>();
        // 自定义输出路径
        pathInfo.put(ConstVal.ENTITY_PATH, entityPath);
        pathInfo.put(ConstVal.XML_PATH, xmlPath);
        pathInfo.put(ConstVal.CONTROLLER_PATH, controllerPath);
        pathInfo.put(ConstVal.SERVICE_PATH, servicePath);
        pathInfo.put(ConstVal.SERVICE_IMPL_PATH, serviceImplPath);
        pathInfo.put(ConstVal.MAPPER_PATH, mapperPath);
        pathInfo.put(ConstVal.DTO_PATH, dtoPath);

        PackageConfig packageConfig = new PackageConfig()
                .setParent(BASE_PACKAGE_NAME)
                .setPathInfo(pathInfo)
                .setController("controller")
                .setEntity("entity")
                .setMapper("mapper")
                .setService("service")
                .setServiceImpl("service.impl")
                .setXml("mybatis.mappers")
                // DTO包
                .setDto("dto");


        // 自定义包名
        Map<String, String> packageInfo = packageConfig.getPackageInfo();
        Map<String, String> newPackageInfo = new HashMap<>();
        newPackageInfo.putAll(packageInfo);
        // 替换实体包名
        newPackageInfo.put(ConstVal.ENTITY, BASE_PACKAGE_MODEL_NAME + "." + "entity" );
        // 替换控制器包名
        String controllerPackage = packageInfo.get(ConstVal.CONTROLLER);
        newPackageInfo.put(ConstVal.CONTROLLER, controllerPackage + ".api");
        // 替换DTO实体包名（注意需把后面2位的to转换为大写）
        newPackageInfo.put(ConstVal.DTO, BASE_PACKAGE_MODEL_NAME + "." + "dto" );

        Field packageInfoField = packageConfig.getClass().getDeclaredField("packageInfo");
        packageInfoField.setAccessible(true);
        packageInfoField.set(packageConfig, newPackageInfo);
        return packageConfig;
    }

    private static DataSourceConfig dataSourceConfig() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl(DB_URL)
                .setUsername(DB_USER_NAME)
                .setPassword(DB_PASSWORD)
                .setDriverName("org.postgresql.Driver")
                // 类型转换
                .setTypeConvert(new PostgreSqlTypeConvert());
        return dataSourceConfig;

    }

    private static StrategyConfig strategyConfig(String... tableNames) {
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig
                .setCapitalMode(true)
                // 开启DTO实体验证
                .setDtoValidate(true)
                .setRestControllerStyle(true)
                .setEntityLombokModel(true)
                .setEntityTableFieldAnnotationEnable(true)
                .setEntityColumnConstant(true)
                .setEntityBuilderModel(true)
                .setNaming(NamingStrategy.underline_to_camel)
                .setVersionFieldName("version")
                .setLogicDeleteFieldName("isDeleted")
                .setEntitySerialVersionUID(false)
                .setSuperEntityClass("com.common.core.entity.BaseEntity")
                .setSuperServiceClass("com.common.business.service.SuperService")
                .setSuperServiceImplClass("com.common.business.service.SuperServiceImpl")
                .setSuperControllerClass("com.common.core.controller.BaseController")
                // 公共字段
                .setSuperEntityColumns("id","create_time","update_time","version","is_deleted","create_user_id","create_user_name","update_user_id","update_user_name")
                .setInclude(tableNames);
        return strategyConfig;
    }


}