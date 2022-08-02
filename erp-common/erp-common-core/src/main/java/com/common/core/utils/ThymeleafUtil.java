package com.common.core.utils;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

/**
 * @Classname ThymeleafUtil
 * @Description TODO
 * @Date 2022-08-02 10:59
 * @Created by yl
 */
public class ThymeleafUtil {
    /**
     * 默认数据格式
     */
    private static final String DATA_KEY = "data";


    /**
     * 渲染模板、产生渲染后的HTML或字符
     *
     * @param template
     * @param data
     * @return
     */
    public static String generateTemplate(String template, Object data) {

        //使用文件的方式构造模板引擎
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        TemplateEngine templateEngine = new TemplateEngine();
        //构造模板引擎
        templateEngine.setTemplateResolver(resolver);
        Context context = new Context();
        //循环给模板增加数据
        if (data != null) {
            //将数据带入页面
            context.setVariable(DATA_KEY, data);
        }
        //模板文件名称
        return templateEngine.process(template, context);
    }
}
