package com.cloud.erp.generate.service;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.converts.PostgreSqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.config.querys.PostgreSqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @Classname CodeGenerator
 * @Description TODO
 * @Date 2022-08-08 17:43
 * @Created by yl
 */
public class CodeGenerator {

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AutoGenerator generator = new AutoGenerator();
        // 全局变量配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir"); //当前项目(写死无需更改！！！)
        gc.setOutputDir(projectPath + "/erp-admin/src/main/java"); // 输出路径(写死无需更改！！！)
        gc.setFileOverride(true); // 默认 false ,是否覆盖已生成文件
        gc.setOpen(false); //默认true ,是否打开输出目录
        gc.setEnableCache(false); // 默认false,是否开启二级缓存
        gc.setAuthor("yl"); // 作者
        gc.setSwagger2(false); // 设置swagger 默认false
        gc.setBaseResultMap(true); // 生成BaseResultMap 默认false
        gc.setEntityName("%s");
        gc.setControllerName("%sController");
        gc.setServiceName("%sService");
        gc.setServiceImplName("%sServiceImpl");
        gc.setMapperName("%sMapper");
        gc.setXmlName("%sMapper");
        gc.setEntityName("%sEntity");
        gc.setIdType(IdType.ASSIGN_ID); // 指定生成的主键类型
        generator.setGlobalConfig(gc);

        // =============想要生成对应代码的数据表所在数据库配置===========

        DataSourceConfig dc = new DataSourceConfig();
        dc.setDbQuery(new PostgreSqlQuery()); // 数据库信息查询 //默认mysql
        dc.setDbType(DbType.POSTGRE_SQL);// 数据库类型
        dc.setTypeConvert(new PostgreSqlTypeConvert()); //类型转换 默认mysql
        dc.setUrl("jdbc:postgresql://172.16.105.216:5432/erp-sys?useSSL=false&serverTimezone=GMT%2B8");
        dc.setDriverName("org.postgresql.Driver");
        dc.setUsername("postgres");
        dc.setPassword("admin@viji");
        generator.setDataSource(dc);

        // 需要修改的地方！！！
        // ==================代码生成所在包的位置======================
        PackageConfig pc = new PackageConfig();
        // 你的包名在这里指定(生成的实体类等java代码保存的位置由这个决定)
        pc.setParent("com.cloud.erp.admin.modules.sys");//代码生成到哪个包下面
//        pc.setModuleName(""); //此处是所属模块名称
        generator.setPackageInfo(pc);
        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };

        /**
         * 生成mapper.xml
         * 生成到resource下面
         */
        String templatePath = "/templates/mapper.xml.vm"; // Velocity模板(写死无需更改！！！)
        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath +"/erp-admin"+"/src/main/resources/mapper/"
                         + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        generator.setCfg(cfg);
        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();

        templateConfig.setXml(null);
        generator.setTemplate(templateConfig);



        // ===============数据库表配置====================
        StrategyConfig sc = new StrategyConfig();
        sc.setCapitalMode(false); //是否大写命名 默认false
        sc.setSkipView(true); //是否跳过视图 默认false
        sc.setNaming(NamingStrategy.underline_to_camel);// 表映射 驼峰命名
        sc.setColumnNaming(NamingStrategy.underline_to_camel); // 字段映射 驼峰
        sc.setEntityLombokModel(true); //是否使用lombok 默认为false
        sc.setRestControllerStyle(true);  // 生成@RestController
        sc.setControllerMappingHyphenStyle(true); // @RequestMapping驼峰转连字符
        sc.setInclude(scanner("表名，多个英文逗号分割").split(",")); //表名，用逗号隔开
        sc.setEntityTableFieldAnnotationEnable(true); // 默认false 注释
        // sc.setTablePrefix("XX_");  // 去除表前缀
        sc.setLogicDeleteFieldName("delete_state"); // 逻辑删除字段名称


        // 自动填充配置
        TableFill gmtCreate = new TableFill("create_time", FieldFill.INSERT);
        TableFill gmtUpdate = new TableFill("update_time", FieldFill.INSERT_UPDATE);
        ArrayList<TableFill> tableFills = new ArrayList<>();
        tableFills.add(gmtCreate);
        tableFills.add(gmtUpdate);
        sc.setTableFillList(tableFills);
        sc.setSuperControllerClass("com.erp.common.controller.BaseController");

        generator.setStrategy(sc);



        // 模板引擎(写死无需更改！！！)
        generator.setTemplateEngine(new VelocityTemplateEngine());
        generator.execute();
    }
}
