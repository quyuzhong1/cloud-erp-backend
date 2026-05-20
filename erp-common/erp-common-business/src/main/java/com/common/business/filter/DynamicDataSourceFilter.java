package com.common.business.filter;
 
 
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.DorisQuerySettingDTO;
import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.business.mapper.BaseDataMapper;
import com.common.business.threadlocal.DynamicDataSourceThreadLocal;

import lombok.extern.slf4j.Slf4j;

@WebFilter(urlPatterns = "/*", asyncSupported = true)
@Slf4j
public class DynamicDataSourceFilter implements Filter,CommandLineRunner{

	@Value("${fresh.cache.time:5}")
    private int freshCacheTime;
	
	@Value("${fresh.cache.switch:true}")
    private boolean freshCacheSwitch;
	
	@Autowired(required = false)
	private BaseDataMapper baseDataMapper;
	
	private Map<String , DorisQuerySettingDTO> dorisQueryCfgSettingMappingCache;
	
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (!BusinessCommonConstants.isDynamicEnabled()) {
            chain.doFilter(request, response);
            return;
        }
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        DorisQuerySettingDTO setting = getDorisQuerySettingDTO(httpRequest.getRequestURI());
        if (setting == null) {
            chain.doFilter(request, response);
            return;
        }
        // 命中路由配置：包装 request 让 body 可重复读取，再用 body 决定走 Doris 还是 Postgres
        RequestReaderHttpServletRequestWrapper wrapper = new RequestReaderHttpServletRequestWrapper(httpRequest);
        DynamicDataSourceTypeEnum dataSourceType = null;
        try {
            dataSourceType = getDynamicDataSourceType(setting, wrapper);
        } catch (Throwable e) {
            log.error("获取动态数据源类型错误", e);
        }
        if (dataSourceType == null || DynamicDataSourceTypeEnum.POSTGRES == dataSourceType) {
            chain.doFilter(wrapper, response);
            return;
        }
        try {
            DynamicDataSourceThreadLocal.set(dataSourceType);
            DynamicDataSourceContextHolder.push(dataSourceType.getCode());
            chain.doFilter(wrapper, response);
        } finally {
            DynamicDataSourceContextHolder.poll();
            DynamicDataSourceThreadLocal.remove();
        }
    }
    
	private synchronized void initDorisQueryCfgSetting() {
		LambdaQueryWrapper<CfgSettingEntity> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(CfgSettingEntity::getType, SettingEnum.DORIS_QUERY_CFG);
		queryWrapper.eq(CfgSettingEntity::getStatus, true);
		dorisQueryCfgSettingEntityCache = cfgSettingService.listMaps(queryWrapper);
		dorisQueryCfgSettingMappingCache = dorisQueryCfgSettingEntityCache.stream().collect(Collectors.toMap(c -> {
			String key = c.get("key").toString();
			if(!key.startsWith("/")) {
				key = "/" + key;
			}
			return key;
		}, c -> {
			DorisQuerySettingDTO d = new DorisQuerySettingDTO();
			String value = c.get("value").toString();
			if(StringUtils.isNotBlank(value)) {
				try {
					d = JSON.parseObject(value, DorisQuerySettingDTO.class);
				} catch (Exception e) {
					log.error("转换doris配置查询错误" , e);
				}
			}
			return d;
		} , (c1 , c2) -> c1));
	}
	
	private DorisQuerySettingDTO getDorisQuerySettingDTO(String requestURI) {
		if(dorisQueryCfgSettingMappingCache == null) {
			this.initDorisQueryCfgSetting();
		}
		if(!requestURI.startsWith("/")) {
			requestURI = "/" + requestURI;
		}
		return dorisQueryCfgSettingMappingCache.get(requestURI);
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
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
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
                    return bais.available() <= 0;
                }

                @Override
                public boolean isReady() {
                    // body 已驻留在内存 ByteArrayInputStream，永远可立即读取不阻塞
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // 同步路径下不会被调用；保留空实现以兼容偶发框架探测
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

	@Override
	public void run(String... args) throws Exception {
		if(BusinessCommonConstants.isDynamicEnabled()) {
			this.initCache(freshCacheSwitch);
		}
	}

	public void initCache(boolean isCreateTask) {
		this.initDorisQueryCfgSetting();
		if(isCreateTask) {
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
				LambdaQueryWrapper<CfgSettingEntity> updateQueryWrapper = new LambdaQueryWrapper<>();
				updateQueryWrapper.eq(CfgSettingEntity::getType, SettingEnum.DORIS_QUERY_CFG);
				updateQueryWrapper.gt(CfgSettingEntity::getUpdateTime, DateUtil.offsetSecond(new Date(), -(freshCacheTime + 1)));
				List<Map<String , Object>> cfgSettingEntityFreshList = cfgSettingService.listMaps(updateQueryWrapper);
				if(CollUtil.isNotEmpty(cfgSettingEntityFreshList)) {
					this.initDorisQueryCfgSetting();
				}
			}, 1, freshCacheTime, TimeUnit.SECONDS);
		}
	}
}