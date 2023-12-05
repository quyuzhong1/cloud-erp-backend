package com.cloud.erp.gateway.component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.DispatcherType;
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
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

@WebFilter(filterName ="xssFilter",urlPatterns = "/*",dispatcherTypes = DispatcherType.REQUEST)
public class CrosXssFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(CrosXssFilter.class);
    private static final String xssErrorResponseMessage = "CrosXssFilter error.";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        //跨域设置
        if(servletResponse instanceof HttpServletResponse){
            HttpServletResponse httpServletResponse=(HttpServletResponse)servletResponse;
            //通过在响应 header 中设置 ‘*’ 来允许来自所有域的跨域请求访问。
            httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
            //通过对 Credentials 参数的设置，就可以保持跨域 Ajax 时的 Cookie
            //设置了Allow-Credentials，Allow-Origin就不能为*,需要指明具体的url域
            //httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
            //请求方式
            httpServletResponse.setHeader("Access-Control-Allow-Methods", "*");
            //(预检请求)的返回结果(即 Access-Control-Allow-Methods 和Access-Control-Allow-Headers 提供的信息) 可以被缓存多久
            httpServletResponse.setHeader("Access-Control-Max-Age", "86400");
            //首部字段用于预检请求的响应。其指明了实际请求中允许携带的首部字段
            httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
        }

        XssAndSqlHttpServletRequestWrapper xssRequest=new XssAndSqlHttpServletRequestWrapper(request);
        String method = ((HttpServletRequest) request).getMethod();

        String param = "";

        if ("POST".equalsIgnoreCase(method)) {
            param = this.getBodyString(xssRequest.getReader());
            if(StringUtils.isNotBlank(param)){
                if(xssRequest.checkXSSAndSql(param)){
                    servletResponse.setCharacterEncoding("UTF-8");
                    servletResponse.setContentType("application/json;charset=UTF-8");
                    PrintWriter out = servletResponse.getWriter();
                    out.write(xssErrorResponseMessage);
                    return;
                }
            }
        }

        if (xssRequest.checkParameter()) {
            servletResponse.setCharacterEncoding("UTF-8");
            servletResponse.setContentType("application/json;charset=UTF-8");
            PrintWriter out = servletResponse.getWriter();
            out.write(xssErrorResponseMessage);
            return;
        }

        filterChain.doFilter(xssRequest, servletResponse);

    }


    // 获取request请求body中参数
    public String getBodyString(BufferedReader br) {
        String inputLine;
        String str = "";
        try {
            while ((inputLine = br.readLine()) != null) {
                str += inputLine;
            }
            br.close();
        } catch (IOException e) {
            logger.error("IOException: " ,e);
        }
        return str;

    }


    @Override
    public void destroy() {

    }


    //内部类，网上的资料一般是独立写一个类，这里为了迁移代码方便，把这个类写在内部类中，这样整个迁移就只有一个CrosXssFilter.java了。
    private class XssAndSqlHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private final Logger logger = LoggerFactory.getLogger(XssAndSqlHttpServletRequestWrapper.class);

        private String key = "and|exec|insert|select|delete|update|count|*|%|chr|mid|master|truncate|char|declare|;|or|-|+";
        private Set<String> notAllowedKeyWords = new HashSet<String>(0);

        HttpServletRequest orgRequest = null;
        private Map<String, String[]> parameterMap;
        private final byte[] body; //用于保存读取body中数据

        public XssAndSqlHttpServletRequestWrapper(HttpServletRequest request) throws IOException{
            super(request);
            orgRequest = request;
            parameterMap = request.getParameterMap();
            body = StreamUtils.copyToByteArray(request.getInputStream());

            String keyStr[] = key.split("\\|");
            for (String str : keyStr) {
                notAllowedKeyWords.add(str);
            }

        }

        // 重写几个HttpServletRequestWrapper中的方法
        /**
         * 获取所有参数名
         *
         * @return 返回所有参数名
         */
        @Override
        public Enumeration<String> getParameterNames() {
            Vector<String> vector = new Vector<String>(parameterMap.keySet());
            return vector.elements();
        }

        /**
         * 覆盖getParameter方法，将参数名和参数值都做xss &amp; sql过滤。<br>
         * 如果需要获得原始的值，则通过super.getParameterValues(name)来获取<br>
         * getParameterNames,getParameterValues和getParameterMap也可能需要覆盖
         */
        @Override
        public String getParameter(String name) {
            String[] results = parameterMap.get(name);
            if (results == null || results.length == 0)
                return null;
            else {
                String value = results[0];
                if (value != null) {
                    value = xssEncode(value);
                }
                return value;
            }
        }

        /**
         * 获取指定参数名的所有值的数组，如：checkbox的所有数据 接收数组变量 ，如checkobx类型
         */
        @Override
        public String[] getParameterValues(String name) {
            String[] results = parameterMap.get(name);
            if (results == null || results.length == 0)
                return null;
            else {
                int length = results.length;
                for (int i = 0; i < length; i++) {
                    results[i] = xssEncode(results[i]);
                }
                return results;
            }
        }

        /**
         * 覆盖getHeader方法，将参数名和参数值都做xss &amp; sql过滤。<br>
         * 如果需要获得原始的值，则通过super.getHeaders(name)来获取<br>
         * getHeaderNames 也可能需要覆盖
         */
        @Override
        public String getHeader(String name) {

            String value = super.getHeader(xssEncode(name));
            if (value != null) {
                value = xssEncode(value);
            }
            return value;
        }

        /**
         * 将容易引起xss &amp; sql漏洞的半角字符直接替换成全角字符
         *
         * @param s
         * @return
         */
        private String xssEncode(String s) {
            if (s == null || s.isEmpty()) {
                return s;
            } else {
                s = stripXSSAndSql(s);
            }
            StringBuilder sb = new StringBuilder(s.length() + 16);
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '>':
                        sb.append("＞");// 转义大于号
                        break;
                    case '<':
                        sb.append("＜");// 转义小于号
                        break;
                    case '\'':
                        sb.append("＇");// 转义单引号
                        break;
                    case '\"':
                        sb.append("＂");// 转义双引号
                        break;
                    case '&':
                        sb.append("＆");// 转义&
                        break;
                    case '#':
                        sb.append("＃");// 转义#
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }


            return sb.toString();
        }

        /**
         * 获取最原始的request
         *
         * @return
         */
        public HttpServletRequest getOrgRequest() {
            return orgRequest;
        }

        /**
         * 获取最原始的request的静态方法
         *
         * @return
         */
        @SuppressWarnings("unused")
        public HttpServletRequest getOrgRequest(HttpServletRequest req) {
            if (req instanceof XssAndSqlHttpServletRequestWrapper) {
                return ((XssAndSqlHttpServletRequestWrapper) req).getOrgRequest();
            }

            return req;
        }

        /**
         *
         * 防止xss跨脚本攻击(替换，根据实际情况调整)
         */

        public String stripXSSAndSql(String value) {
            if (value != null) {
                // NOTE: It's highly recommended to use the ESAPI library and
                // uncomment the following line to
                // avoid encoded attacks.
                // value = ESAPI.encoder().canonicalize(value);
                // Avoid null characters
                /** value = value.replaceAll("", ""); ***/
                // Avoid anything between script tags
                Pattern scriptPattern = Pattern.compile(
                        "&lt;[\r\n| | ]*script[\r\n| | ]*&gt;(.*?)<!--[\r\n| | ]*script[\r\n| | ]*-->", Pattern.CASE_INSENSITIVE);
                value = scriptPattern.matcher(value).replaceAll("");
                // Avoid anything in a
                // src="http://www.yihaomen.com/article/java/..." type of
                // e-xpression
                scriptPattern = Pattern.compile("src[\r\n| | ]*=[\r\n| | ]*[\\\"|\\\'](.*?)[\\\"|\\\']",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                value = scriptPattern.matcher(value).replaceAll("");
                // Remove any lonesome  tag
                scriptPattern = Pattern.compile("<!--[\r\n| | ]*script[\r\n| | ]*-->", Pattern.CASE_INSENSITIVE);
                value = scriptPattern.matcher(value).replaceAll("");
                // Remove any lonesome <script ...> tag
                scriptPattern = Pattern.compile("<[\r\n| | ]*script(.*?)>",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                value = scriptPattern.matcher(value).replaceAll("");
                // Avoid eval(...) expressions
                scriptPattern = Pattern.compile("eval\\((.*?)\\)",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                value = scriptPattern.matcher(value).replaceAll("");
                // Avoid e-xpression(...) expressions
                scriptPattern = Pattern.compile("e-xpression\\((.*?)\\)",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                value = scriptPattern.matcher(value).replaceAll("");
                // Avoid javascript:... expressions
                scriptPattern = Pattern.compile("javascript[\r\n| | ]*:[\r\n| | ]*", Pattern.CASE_INSENSITIVE);
                value = scriptPattern.matcher(value).replaceAll("");
                // Avoid vbscript:... expressions
                scriptPattern = Pattern.compile("vbscript[\r\n| | ]*:[\r\n| | ]*", Pattern.CASE_INSENSITIVE);
                value = scriptPattern.matcher(value).replaceAll("");
                // Avoid onload= expressions
                scriptPattern = Pattern.compile("onload(.*?)=",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                value = scriptPattern.matcher(value).replaceAll("");
            }
            return value;
        }

        public boolean checkXSSAndSql(String value) {
            boolean flag = false;
            if (value != null) {
                // NOTE: It's highly recommended to use the ESAPI library and
                // uncomment the following line to
                // avoid encoded attacks.
                // value = ESAPI.encoder().canonicalize(value);
                // Avoid null characters
                /** value = value.replaceAll("", ""); ***/
                // Avoid anything between script tags
                Pattern scriptPattern = Pattern.compile(
                        "<[\r\n| | ]*script[\r\n| | ]*>(.*?)</[\r\n| | ]*script[\r\n| | ]*>", Pattern.CASE_INSENSITIVE);
                flag = scriptPattern.matcher(value).find();
                if (flag) {
                    return flag;
                }
                // Avoid anything in a
                // src="http://www.yihaomen.com/article/java/..." type of
                // e-xpression
                scriptPattern = Pattern.compile("src[\r\n| | ]*=[\r\n| | ]*[\\\"|\\\'](.*?)[\\\"|\\\']",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                flag = scriptPattern.matcher(value).find();
                if (flag) {
                    return flag;
                }
                // Remove any lonesome </script> tag
                scriptPattern = Pattern.compile("<!--[\r\n| | ]*script[\r\n| | ]*-->", Pattern.CASE_INSENSITIVE);
                flag = scriptPattern.matcher(value).find();
                if (flag) {
                    return flag;
                }
                // Remove any lonesome <script ...> tag
                scriptPattern = Pattern.compile("<[\r\n| | ]*script(.*?)>",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                flag = scriptPattern.matcher(value).find();
                if (flag) {
                    return flag;
                }
                // Avoid eval(...) expressions
                scriptPattern = Pattern.compile("eval\\((.*?)\\)",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                flag = scriptPattern.matcher(value).find();
                if (flag) {
                    return flag;
                }
                // Avoid e-xpression(...) expressions
                scriptPattern = Pattern.compile("e-xpression\\((.*?)\\)",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                flag = scriptPattern.matcher(value).find();
                if (flag) {
                    return flag;
                }
                // Avoid javascript:... expressions
                scriptPattern = Pattern.compile("javascript[\r\n| | ]*:[\r\n| | ]*", Pattern.CASE_INSENSITIVE);
                flag = scriptPattern.matcher(value).find();
                if (flag) {
                    return flag;
                }
                // Avoid vbscript:... expressions
                scriptPattern = Pattern.compile("vbscript[\r\n| | ]*:[\r\n| | ]*", Pattern.CASE_INSENSITIVE);
                flag = scriptPattern.matcher(value).find();
                if (flag) {
                    return flag;
                }
                // Avoid onload= expressions
                scriptPattern = Pattern.compile("onload(.*?)=",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
                flag = scriptPattern.matcher(value).find();
                if (flag) {
                    return flag;
                }

                flag=checkSqlKeyWords(value);
            }
            return flag;
        }

        public boolean checkSqlKeyWords(String value){
            String paramValue = value;
            for (String keyword : notAllowedKeyWords) {
                if (paramValue.length() > keyword.length() + 4
                        && (paramValue.contains(" "+keyword)||paramValue.contains(keyword+" ")||paramValue.contains(" "+keyword+" "))) {

                    logger.error(this.getRequestURI()+ "参数中包含不允许sql的关键词(" + keyword
                            + ")");
                    return true;
                }
            }
            return false;
        }

        public final boolean checkParameter() {

            @SuppressWarnings({ "unchecked", "rawtypes" })
            Map<String, String[]> submitParams = new HashMap(parameterMap);

            Set<String> submitNames = submitParams.keySet();
            for (String submitName : submitNames) {
                Object submitValues = submitParams.get(submitName);
                if ((submitValues instanceof String)) {
                    if (checkXSSAndSql((String) submitValues)) {
                        return true;
                    }
                } else if ((submitValues instanceof String[])) {
                    for (String submitValue : (String[])submitValues){
                        if (checkXSSAndSql(submitValue)) {
                            return true;
                        }
                    }
                }
            }
            return false;
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
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public boolean isReady() {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public void setReadListener(ReadListener arg0) {
                    // TODO Auto-generated method stub

                }
            };
        }

    }

}