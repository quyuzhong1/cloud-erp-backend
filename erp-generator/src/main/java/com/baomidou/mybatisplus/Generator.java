package com.baomidou.mybatisplus;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.ConstVal;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.converts.PostgreSqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.SneakyThrows;

/**
 * mybatisplus自动生成工具
 * @author zhangchunlin
 * @since 2023-05-28
 */
public class Generator {

    /**
       模块名（需要更改）新加控制台输入，无需改动代码
     */
    private static String MODEL = "tms";
    /**
     * 作者（需要更改）新加控制台输入，无需改动代码
     */
    private static String AUTHOR = "lrp";
    /**
     * 项目路径
     */
    private static String PROJECT_PATH;
    /**
     * 当前环境是否Windows
     */
    private static final boolean IS_WINDOWS = System.getProperty("os.name").trim().toLowerCase().contains("windows");
    /**
     * 指定server包名
     */
    private static String BASE_PACKAGE_NAME;
    /**
     * 指定Model包名 如com.erp.model.wms
     */
    private static String BASE_PACKAGE_MODEL_NAME;
    private static final String BASE_MODEl_PROJECT_NAME = "erp-model";
    /**
     * 模块名
     */
    private static String MODULE_NAME;
    /**
     * 服务名
     */
    private static String SERVER_NAME;
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
    private static String DB_URL;
    /**
     * 数据库用户名
     */
    private static final String DB_USER_NAME = "postgres";
    /**
     * 数据库密码
     */
    private static final String DB_PASSWORD = "admin@viji";

    public static String scanner(String tip) throws Exception {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (org.apache.commons.lang.StringUtils.isNotBlank(ipt)) {
                return ipt;
            }
        }
        throw new Exception("请输入正确的" + tip + "！");
    }
    
    public static void main(String[] args) throws Exception{
        // 需要生成的表名（特别注意：请确保生成多个表时在同一个数据库，如果一次性生成多个，中间有异常不会中断后续生成）
        // 现设置的是文件不覆盖，即生成时如果已经存在该文件则不会生成导致覆盖，设置成true覆盖，如果需要覆盖请将全局配置fileOverride设置成true
    	PROJECT_PATH = ClassLoader.getSystemResource("").getPath().split("erp-generator")[0];
    	MODEL = scanner("模块名");
        AUTHOR = scanner("作者");
        String tableName = scanner("表名，多个英文逗号分割");
//        String[] tableNames = {"logistics_channel_constraint"};
        String[] tableNames = tableName.split(",");
        if(tableNames.length == 1) {
        	tableNames = tableName.split("，");
        }

        BASE_PACKAGE_NAME = StrUtil.format("com.erp.server.{}", MODEL);
        BASE_PACKAGE_MODEL_NAME = StrUtil.format("com.erp.model.{}", MODEL);
        MODULE_NAME = StrUtil.format("erp-model-{}", MODEL);
        SERVER_NAME = StrUtil.format("erp-server-{}", MODEL);
        DB_URL = "jdbc:postgresql://172.16.100.12:5432/" + StrUtil.format( "erp-{}", MODEL) + "?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false";

        generateByTables(tableNames);
        
        System.out.println("==========================准备处理枚举...================================");
        dealEnum(tableNames);
        System.out.println("\u001B[32m" + "==========================枚举生成完成！！！==========================" + "\u001B[0m");
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
                .setFileOverride(true)
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
                .setSuperServiceImplClass("com.common.business.service.impl.SuperServiceImpl")
                .setSuperControllerClass("com.common.core.controller.BaseController")
                // 公共字段
                .setSuperEntityColumns("id","create_time","update_time","version","is_deleted","create_user_id","create_user_name","update_user_id","update_user_name")
                .setInclude(tableNames);
        return strategyConfig;
    }

    private static void dealEnum(String[] tableNames) throws Exception{
    	String date = DateUtil.now();
    	String path = PROJECT_PATH.replace("erp-server", "erp-model") + "/" +  BASE_MODEl_PROJECT_NAME + "/" + MODULE_NAME + "/src/main/java/com/erp/model/" + MODEL+"/";
    	for(String tableName : tableNames) {
    		List<EnumDto> enumDtoList = new ArrayList<>();
    		String name = convertToCamel(tableName);
    		String description = "";
    		LineIterator lineIterator = FileUtils.lineIterator(new File(path + "entity/" + upperCaseFirst(name) + "Entity" +".java"));
    		String next = "";
    		while (lineIterator.hasNext()) {
    			next = lineIterator.next();
    			EnumDto enumDto = EnumDto.getEnumDto(next);
    			if(enumDto != null) {
    				next = lineIterator.next();
    				next = lineIterator.next();
    				next = lineIterator.next();
    				String[] split = next.trim().split(" ");
    				enumDto.setFiled(split[split.length - 1].replace(";", ""));
    				enumDtoList.add(enumDto);
    			}
    			if(org.apache.commons.lang.StringUtils.isBlank(description) && " * <p>".equals(next)) {
    				description = lineIterator.next().replace(" * ", "");
    			}
    		}
    		lineIterator.close();
    		
    		if(CollUtil.isEmpty(enumDtoList)) {
    			continue;
    		}
    		FileOutputStream fs = null;
    		for(EnumDto e : enumDtoList) {
    			File dirFile = new File(path + "enums/");
    			if(!dirFile.exists()) {
    				dirFile.mkdir();
    			}
    			String className = upperCaseFirst(name) + upperCaseFirst(e.getFiled()) + "Enum";
    			e.setClassName(className);
    			fs = new FileOutputStream(new File(path + "enums/"+ className + ".java"));
        		lineIterator = FileUtils.lineIterator(new File(PROJECT_PATH + "/erp-generator/src/main/resources/templates/Enum.ftl"));
        		fs.write("package com.erp.model.".getBytes());
        		fs.write(MODEL.getBytes());
        		fs.write(".enums".getBytes());
        		fs.write(";".getBytes());
        		while (lineIterator.hasNext()) {
        			next = lineIterator.next();
        			fs.write(replaceKeyWord(next , className , description + " " + e.getFiledName(), date).getBytes());
        			fs.write("\n".getBytes());
        			if(next.contains("public enum ${name} implements EnumMessage {")) {
        				Map<String, String> enumNameMaps = e.getEnumNameMap();
        				for(Map.Entry<String, String> enumNameMap : enumNameMaps.entrySet()) {
        					fs.write(("	" + enumNameMap.getKey().toUpperCase() + "(\"" + enumNameMap.getKey() +"\", \"" + enumNameMap.getValue() +"\"),").getBytes());
        					fs.write("\n".getBytes());
        				}
        			}
        		}
        		fs.close();
        		lineIterator.close();
    		}
    		
    		File mapperXmlFile = new File(path + "entity/" + upperCaseFirst(name) + "Entity" +".java");
    		File mapperXmlTempFile = new File(path + "entity/" + upperCaseFirst(name) + "Entity" +".java.temp");
    		fs = new FileOutputStream(mapperXmlTempFile);
    		lineIterator = FileUtils.lineIterator(mapperXmlFile);
    		while (lineIterator.hasNext()) {
    			next = lineIterator.next();
    			boolean filedFlag = false;
    			for(EnumDto e : enumDtoList) {
    				if(next.contains(e.getFiledCode())) {
    					fs.write(next.getBytes());
    					fs.write("  枚举：".getBytes());
    					fs.write(e.getClassName().getBytes());
    	    			fs.write("\n".getBytes());
    	    			
    	    			filedFlag = true;
    	    			break;
    				}
    			}
    			
    			if(filedFlag) {
    				continue;
    			}
    			fs.write(next.getBytes());
    			fs.write("\n".getBytes());
    		}
    		fs.close();
    		lineIterator.close();
    		mapperXmlFile.delete();
    		mapperXmlFile = new File(path + "entity/" + upperCaseFirst(name) + "Entity" +".java");
    		mapperXmlTempFile.renameTo(mapperXmlFile);
    		mapperXmlTempFile.delete();
    	}
    
    }
    
    private static String replaceKeyWord(String line , String name , String description , String date) {
    	line = line.replace("${name}", name);
    	line = line.replace("${description}", description);
    	line = line.replace("${author}", AUTHOR);
    	line = line.replace("${date}", date);
    	return line;
    }
    
    /**
     * 定义下划线
     */
    private static final char UNDERLINE = '_';

    // 将陀峰命名中的大写字母（首字母除外）转成“_小写字母”
    public static String camelToUnderline(String param) {
        if (org.apache.commons.lang.StringUtils.isNotBlank(param)) {
            int len = param.length();
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                char c = param.charAt(i);
                if (Character.isUpperCase(c)) {
                    if (i != 0) {
                        sb.append(UNDERLINE);
                    }
                    sb.append(Character.toLowerCase(c));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    
    /**
     * 下划线转驼峰
     *
     * @param underlineName
     * @return
     */
    public static String convertToCamel(String underlineName) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;

        for (int i = 0; i < underlineName.length(); i++) {
            char currentChar = underlineName.charAt(i);

            if (currentChar == UNDERLINE) {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(currentChar));
                    capitalizeNext = false;
                } else {
                    sb.append(Character.toLowerCase(currentChar));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 首字母大写
     *
     * @param name
     * @return
     */
    public static String upperCaseFirst(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }
    
    @Data
    static class EnumDto{
    	private String filed;
    	private String filedName;
    	private Map<String, String> enumNameMap;
    	private String className;
    	private String filedCode;
    	
    	public static EnumDto getEnumDto(String line) {
    		if(org.apache.commons.lang.StringUtils.isBlank(line)) {
    			return null;
    		}
    		String filedDes = line.trim().replace("* ", "");
    		String[] split = filedDes.split(":");
    		if(split.length == 1) {
    			split = filedDes.split("：");
    		}
    		if(split.length == 1) {
    			return null;
    		}
    		EnumDto enumDto = new EnumDto();
    		enumDto.setFiledCode(line);
    		enumDto.setFiledName(split[0]);
    		String[] split2 = split[1].split("，");
    		if(split2.length == 1) {
    			split2 = split[1].split(",");
    		}
    		if(split2.length == 1) {
    			return null;
    		}
    		Map<String, String> enumNameMap = new LinkedHashMap<>();
    		for(String s : split2) {
    			String[] split3 = s.split("=");
    			if(split3.length > 1) {
    				if(org.apache.commons.lang.StringUtils.isNotBlank(split3[0]) 
    						&& org.apache.commons.lang.StringUtils.isNotBlank(split3[1])) {
    					enumNameMap.put(split3[0].trim(), split3[1].trim());
    				}
    			}
    		}
    		if(enumNameMap.size() == 0) {
    			return null;
    		}
    		enumDto.setEnumNameMap(enumNameMap);
    		return enumDto;
    	}
    }
}