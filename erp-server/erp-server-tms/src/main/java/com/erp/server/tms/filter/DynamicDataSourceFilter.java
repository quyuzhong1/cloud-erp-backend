package com.erp.server.tms.filter;
 
 
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.common.business.dto.DorisQuerySettingDTO;
import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.business.threadlocal.DynamicDataSourceThreadLocal;
import com.common.business.wrapper.FeignQuery;

import lombok.extern.slf4j.Slf4j;

@WebFilter("/*")
@Slf4j
public class DynamicDataSourceFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
    	DorisQuerySettingDTO dorisQuerySettingDTO = null;
    	try {
			String requestURI = ((HttpServletRequest) request).getRequestURI();
			dorisQuerySettingDTO = FeignQuery.invoke(DorisQuerySettingDTO.class, "com.erp.server.dmp.inout.utils.DmpHandlerCache", "getDorisQuerySettingDTO", Arrays.asList(requestURI));
		} catch (Throwable e) {
			log.error("获取动态数据源配置错误" , e);
		}
    	if(dorisQuerySettingDTO == null) {
    		chain.doFilter(request, response);
    	}else {
    		ServletRequest requestWrapper = null;
            if(request instanceof HttpServletRequest) {
                requestWrapper = new RequestReaderHttpServletRequestWrapper((HttpServletRequest) request);
            }
            //获取请求中的流如何，将取出来的字符串，再次转换成流，然后把它放入到新request对象中。
            // 在chain.doFiler方法中传递新的request对象
            if(requestWrapper == null) {
                chain.doFilter(request, response);
            } else {
            	DynamicDataSourceTypeEnum dynamicDataSourceType = null;
                try {
    				dynamicDataSourceType = getDynamicDataSourceType(dorisQuerySettingDTO , requestWrapper);
    			} catch (Throwable e) {
    				log.error("获取动态数据源类型错误" , e);
    			}
                if(dynamicDataSourceType == null || DynamicDataSourceTypeEnum.POSTGRES == dynamicDataSourceType) {
                	chain.doFilter(requestWrapper, response);
                }else {
                	try {
                		DynamicDataSourceThreadLocal.set(dynamicDataSourceType);
        	            DynamicDataSourceContextHolder.push(dynamicDataSourceType.getCode());
        	            chain.doFilter(requestWrapper, response);
                    } finally {
                        DynamicDataSourceContextHolder.poll();
                        DynamicDataSourceThreadLocal.remove();
                    }
                }
            }
    	}
    }
    
    private DynamicDataSourceTypeEnum getDynamicDataSourceType(DorisQuerySettingDTO dorisQuerySettingDTO , ServletRequest requestWrapper) {
    	StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = requestWrapper.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            log.error("DynamicDatasourceFilter获取参数错误" , e);
        }

        String requestBody = sb.toString();
        DynamicDataSourceTypeEnum dynamicDataSourceType = dorisQuerySettingDTO.getDynamicDataSourceType(requestBody);
        if(dynamicDataSourceType != null && DynamicDataSourceTypeEnum.POSTGRES != dynamicDataSourceType) {
        	Integer sleepMillis = dorisQuerySettingDTO.getSleepMillis();
        	if(sleepMillis != null && sleepMillis > 0) {
        		try {
					Thread.sleep(sleepMillis);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
        	}
        }
		return dynamicDataSourceType;
    }
 
    @Override
    public void init(FilterConfig arg0) throws ServletException {
 
    }
    
    @Override
    public void destroy() {
 
    }
    
    private static class RequestReaderHttpServletRequestWrapper extends HttpServletRequestWrapper{
   	 
        private final byte[] body;
     
        public RequestReaderHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            body = getBodyString(request).getBytes(Charset.forName("UTF-8"));
        }
     
        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }
     
        @Override
        public ServletInputStream getInputStream() throws IOException {
     
            final ByteArrayInputStream bais = new ByteArrayInputStream(body);
     
            return new ServletInputStream() {
     
                @Override
                public int read() throws IOException {
                    return bais.read();
                }
     
                @Override
                public boolean isFinished() {
                    return false;
                }
     
                @Override
                public boolean isReady() {
                    return false;
                }
     
                @Override
                public void setReadListener(ReadListener readListener) {
     
                }
            };
        }
        
        private static String getBodyString(HttpServletRequest request) throws IOException {
            StringBuilder sb = new StringBuilder();
            InputStream inputStream = null;
            BufferedReader reader = null;
            try {
                inputStream = request.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        }
    }
}