package com.common.business.config;

import com.common.core.utils.StrUtils;
import com.google.common.collect.Lists;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * feign拦截器配置
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-09
 */
@Slf4j
@Configuration
public class FeignInterceptor implements RequestInterceptor {

    // 需要转发的请求头
    private static List<String> forwardHeaderNames = new ArrayList<String>() {
        {
            add("tokenuserinfo");// 用户信息
            add("user-agent");
            add("x-real-ip");
            add("authorization");// token信息
        }
    };

    // 请求头会自动转换成了小写，此处做映射
    private static Map<String, String>  HEADER_NAME_MAPPING = new HashMap<String, String>(){{
        put("tokenuserinfo", "tokenUserInfo");
        put("authorization", "Authorization");
    }};

    @Override
    public void apply(RequestTemplate requestTemplate) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return;
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        Enumeration<String> headerNames = request.getHeaderNames();
        List<String> headNameList = Lists.newArrayList();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                headNameList.add(name);
                if(forwardHeaderNames.contains(name)){
                    Enumeration<String> values = request.getHeaders(name);
                    while (values.hasMoreElements()) {
                        String value = values.nextElement();
                        if(HEADER_NAME_MAPPING.containsKey(name)) {
                            requestTemplate.header(HEADER_NAME_MAPPING.get(name), value);
                        } else {
                            requestTemplate.header(name, value);
                        }
                    }
                }
            }
        }

        // seata分布式事务XID，防止事务无法回滚
        String xid = RootContext.getXID();
        if(StrUtils.isNotEmpty(xid)) {
            log.info("分布式事务seata,feign传递的xid:{}",xid);
            requestTemplate.header(RootContext.KEY_XID,xid);
        }

    }
}
