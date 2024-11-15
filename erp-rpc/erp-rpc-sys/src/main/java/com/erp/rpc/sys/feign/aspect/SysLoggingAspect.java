package com.erp.rpc.sys.feign.aspect;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.config.GlobalExceptionHandler;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.utils.MD5Util;
import com.common.business.utils.RedisUtil;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.enums.LogStatusEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.IpUtils;
import com.erp.model.sys.dto.SysLogRecordDTO;
import com.erp.model.sys.dto.SysLogRecordFieldDTO;
import com.erp.model.sys.dto.SysLogRecordFieldListDTO;
import com.erp.rpc.sys.feign.SysLogRecordFeign;
import com.erp.rpc.sys.feign.SysLogRecordFieldFeign;
import com.erp.rpc.sys.feign.bo.UpdateRecordItemBO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.ReflectionDiffBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.NamedThreadLocal;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 系统日志切面
 *
 * @author Jim
 * @since 2023-08-30
 */
@Slf4j
@Aspect
public class SysLoggingAspect {

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private SysLogRecordFeign sysLogRecordFeign;

    @Resource
    private SysLogRecordFieldFeign sysLogRecordFieldFeign;

    @Resource
    private GlobalExceptionHandler globalExceptionHandler;

    // 日志描述
    // 主数据修改描述
    private static final String MAIN_UPDATE_LOG_DEC = "主数据 修改 {} 字段，修改前:{}，修改后:{}\n";
    // 明细数据添加描述
    private static final String DETAIL_ADD_LOG_DEC = "明细数据 新增id为:{}\n";
    // 明细数据删除描述
    private static final String DETAIL_DELETE_LOG_DEC = "明细数据 删除id为:{}\n";
    // 明细数据修改描述
    private static final String DETAIL_UPDATE_LOG_DEC = "明细数据 id为:{}，修改 {} 字段，修改前:{}，修改后:{}\n";
    // 批量操作描述:（操作行为）了（系统模块）id为: 结果为:
    private static final String BATCH_OPERATION_LOG_DEC = "{}了{}\n id为:{}\n 结果为:{}\n";
    // 自定义描述格式(包含任意{})
    private static final String CUSTOM_MATCH_REGEX = ".*\\{.*\\}.*";

    /**
     * 数据缓存
     */
    private static final ThreadLocal<UpdateRecordItemBO> LOG_INFO_THREAD_LOCAL = new NamedThreadLocal<>("Log Info");

    /**
     * 系统日志切入点
     */
    @Pointcut("@annotation(com.common.core.anno.LogAction)")
    public void logPointcut() {
    }

    /**
     * 核心业务正常结束时执行
     * 说明：假如有after，先执行after,再执行returning
     */
    @AfterReturning(value = "logPointcut()", returning = "apiResult")
    public void doAfterReturning(JoinPoint joinPoint, Object apiResult) {
        try {
            log.debug("Sys Logging doAfterReturning()");
            if (null == apiResult) {
                return;
            }
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogAction logAction = method.getAnnotation(LogAction.class);
            // 登陆响应菜单记录过大忽略记录响应体
            if (LogActionEnum.LOGIN.equals(logAction.value())){
                return;
            }
            UpdateRecordItemBO bo = LOG_INFO_THREAD_LOCAL.get();
            if (null == bo) {
                return;
            }
            bo.setResponseParams(JSONUtil.toJsonStr(apiResult));
        } catch (Exception e) {
            log.error("[系统日志]添加系统日志-doAfterReturning-异常：{}", e.getMessage());
        }
    }

    /**
     * 核心业务出现异常时执行
     * 说明：假如有after，先执行after,再执行Throwing
     */
    @AfterThrowing(value = "logPointcut()", throwing = "e")
    public void doAfterThrowing(Exception e) {
        try {
            log.debug("Sys Logging doAfterThrowing()");
            if (null == e) {
                return;
            }
            UpdateRecordItemBO bo = LOG_INFO_THREAD_LOCAL.get();
            if (null == bo) {
                return;
            }
            bo.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 1000));
        } catch (Exception ex) {
            log.error("[系统日志]添加系统日志-doAfterThrowing-异常：{}", e.getMessage());
        }
    }

    /**
     * 环切处理
     */
    @Around("logPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) {
        //接口重复提交校验
        String idempotentKey = getIdempotentKey(joinPoint);
        if (redisUtil.hasKey(idempotentKey)) {
            throw new ServiceException(ApiError.ERROR_1014);
        }else {
            //如果没有表示不是重复提交并设置key存活的缓存时间
            redisUtil.set(idempotentKey, "", 3);
        }
        // 处理请求
        Object obj = null;
        try {
            String actionPath = "/";
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (null != requestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                actionPath = request.getRequestURI();
            }
            log.warn("Sys Logging doAround.before: actionPath = {}", actionPath);
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogAction logAction = method.getAnnotation(LogAction.class);
            // 解析当前出当前实体类名
            String classPath = parseEntityClassName(joinPoint);
            // id信息
            Object id = null;
            Object firstObjArg = Arrays.stream(joinPoint.getArgs()).findFirst().orElse(null);
            if (null != firstObjArg) {
                String idKeyName = StringUtils.isBlank(logAction.keyIdName()) ? "id" : logAction.keyIdName();
                id = ReflectUtil.getFieldValue(firstObjArg, idKeyName);
            }

            // 初始化数据缓存
            UpdateRecordItemBO bo = UpdateRecordItemBO.init(classPath);
            LOG_INFO_THREAD_LOCAL.set(bo);

            // 处理前操作
            Object originalObj = beforeFindObj(joinPoint, logAction, id);
            // 更新使用view的DTO作为classPath
            if (logAction.value().hasCompare() && null != originalObj) {
                classPath = String.valueOf(originalObj.getClass());
                bo.setClassPath(classPath);
            }

            obj = proceedJoinPoint(joinPoint);
            // 处理后操作
            Object newObject = afterFindObj(joinPoint, logAction, id);
            // 生成对比描述或单记录自定义描述
            String description = generateSingleDesc(originalObj, newObject, logAction, classPath, joinPoint);

            // 添加日志到mq队列
            handleLog(joinPoint, logAction, description);
            log.debug("Sys Logging doAround.after:");
            return checkAndResolveException(obj, null);
        } catch (Throwable e) {
            log.error("[系统日志]添加系统日志-doAround-异常：{}", e.getMessage());
            return checkAndResolveException(obj, e);
        } finally {
            UpdateRecordItemBO bo = LOG_INFO_THREAD_LOCAL.get();
            if (null != bo) {
                // 清除当前缓存
                LOG_INFO_THREAD_LOCAL.remove();
            }
            //接口处理完成，清理已添加缓存key
            redisUtil.del(idempotentKey);
        }
    }
    private Object proceedJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            log.debug("Sys Logging proceed error:{}", e.getMessage());
            return e;
        }
    }
    private String getIdempotentKey(ProceedingJoinPoint proceedingJoinPoint){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }
        String params = argsToString(proceedingJoinPoint.getArgs());
        String url = null;
        String token = null;
        if (request != null) {
            // 请求地址（作为存放cache的key值）
            url = request.getRequestURI();
            // 用户的唯一标识
            token = request.getHeader("Authorization");
        }
        // 唯一标识（url +  token  + params）
        String submitKey = "Idempotent:" + MD5Util.toMD5(url + "_" + token + ":" + params);
        return submitKey;
    }
    /**
     * 参数拼装
     */
    private String argsToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (!ObjectUtils.isEmpty(o) && !isFilterObject(o)) {
                    try {
                        params.append(JSON.toJSONString(o)).append(" ");
                    } catch (Exception e) {
                        log.error("日志记录中转换传参异常：{}",e.getMessage());
                    }
                }
            }
        }
        return params.toString().trim();
    }
    /**
     * 判断是否是需要过滤的对象
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection<?> collection = (Collection<?>) o;
            return collection.stream().anyMatch(MultipartFile.class::isInstance);
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map<?, ?> map = (Map<?, ?>) o;
            return map.values().stream().anyMatch(MultipartFile.class::isInstance);
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }

    /**
     * 生成描述
     *
     * @return 空=logAction的desc
     */
    private String generateSingleDesc(Object originalObj, Object newObject, LogAction logAction, String classPath, ProceedingJoinPoint joinPoint) {
        // 1:生成自定义描述(包含{任意字段})
        if (Pattern.matches(CUSTOM_MATCH_REGEX, logAction.desc())) {
            // 获取请求参数转Map
            Object[] args = joinPoint.getArgs();
            if (0 == args.length) {
                return "";
            }
            Object paramsObj = args[0];
            // 文件上传处理
            if (paramsObj instanceof MultipartFile[]){
                return "";
            }
            // 兼容接口product/plan/uploadImageUrl
            if (paramsObj instanceof MultipartFile){
                MultipartFile file = (MultipartFile) paramsObj;
                return logAction.desc().replace("{name}", Objects.requireNonNull(file.getOriginalFilename()));
            }
            // 1:非数组请求参数处理
            if (!(paramsObj instanceof Collection)) {
                Map<String, Object> paramsMap = BeanUtil.beanToMap(paramsObj);
                return CharSequenceUtil.format(logAction.desc(), paramsMap);
            }
            return "";
        }
        // 3:生成对比描述
        if (logAction.value().hasCompare()) {
            return compareDataDesc(originalObj, newObject, logAction, classPath);
        }
        return "";
    }


    /**
     * 添加日志到mq队列
     */
    protected void handleLog(final JoinPoint joinPoint, LogAction logAction, String description) {
        // 创建DTO
        List<SysLogRecordDTO.AddDTO> dtoList = createLogDto(joinPoint, logAction, description);
        // 添加到mq
        sysLogRecordFeign.addSendMq(dtoList);
    }

    /**
     * 组合日志推动DTO
     */
    private static List<SysLogRecordDTO.AddDTO> createLogDto(JoinPoint joinPoint,
                                                             LogAction logAction,
                                                             String description) {
        // 菜单系统模块名称
        String currentSysName = parseSystemModule(joinPoint);

        // 当前请求
        HttpServletRequest request = null;
        // 请求路径
        String actionPath = "/";
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            request = ((ServletRequestAttributes) requestAttributes).getRequest();
            actionPath = request.getRequestURI();
        }

        // 记录请求参数
        String requestParams = "{}";
        if (joinPoint.getArgs().length > 0) {
            Object requestParamObj = Arrays.stream(joinPoint.getArgs()).findFirst().orElse(new JSONObject());
            requestParams = JSONUtil.toJsonStr(requestParamObj);
        }
        // 请求参数
        Object[] paramsArrays = joinPoint.getArgs();

        // 组合添加的DTO列表
        List<SysLogRecordDTO.AddDTO> dtoList;
        if (logAction.value().checkIsBatchOperation(logAction.isBatchOperationStr())) {
            // 批量处理创建
            dtoList = constructBatchByIds(logAction, request, actionPath, requestParams, currentSysName, description);
        } else if (0 != paramsArrays.length && ((paramsArrays[0] instanceof Collection) || (paramsArrays[0] instanceof MultipartFile[])) ) {
            // 数组请求参数批量处理创建
            dtoList = constructBatchByParams(logAction, request, actionPath, requestParams, currentSysName, joinPoint);
        } else {
            // 其他单条处理创建
            dtoList = Collections.singletonList(initDto(logAction, request, actionPath, requestParams, currentSysName, description));
        }
        return dtoList;
    }


    /**
     * 构建批量添加DTO
     */
    private static List<SysLogRecordDTO.AddDTO> constructBatchByIds(LogAction logAction,
                                                                    HttpServletRequest request,
                                                                    String actionPath,
                                                                    String requestParams,
                                                                    String currentSysName,
                                                                    String description
    ) {
        // ids的DTO列表
        List<SysLogRecordDTO.AddDTO> dtoList = new ArrayList<>();
        // 获取IDS信息
        List<String> ids = parseIds(logAction, requestParams);
        // 组合
        ids.forEach(id -> {
            // 初始化
            SysLogRecordDTO.AddDTO dto = initDto(logAction, request, actionPath, requestParams, currentSysName, description);
            // 处理数据
            handleData(id, logAction, dto);
            dtoList.add(dto);
        });
        return dtoList;
    }

    /**
     * 构建批量添加DTO
     */
    private static List<SysLogRecordDTO.AddDTO> constructBatchByParams(LogAction logAction,
                                                                       HttpServletRequest request,
                                                                       String actionPath,
                                                                       String requestParams,
                                                                       String currentSysName,
                                                                       JoinPoint joinPoint
    ) {
        Object[] args = joinPoint.getArgs();
        if (args[0] instanceof Collection){
            // 数组请求参数处理
            return ((Collection<?>) args[0]).stream()
                    .map(e -> {
                        Map<String, Object> paramsMap = BeanUtil.beanToMap(e);
                        String currentDesc = CharSequenceUtil.format(logAction.desc(), paramsMap);
                        // 初始化
                        SysLogRecordDTO.AddDTO dto = initDto(logAction, request, actionPath, requestParams, currentSysName, currentDesc);
                        // 设置记录ID
                        Object idObj = paramsMap.get(logAction.keyIdName());
                        dto.setRecordId(null != idObj ? idObj.toString() : "");
                        return dto;
                    })
                    .collect(Collectors.toList());
        } else if (args[0] instanceof MultipartFile[]){
            MultipartFile[] files = (MultipartFile[]) args[0];
            // 数组请求参数处理
            return Arrays.stream(files)
                    .map(e -> {
                        String currentDesc = logAction.desc().replace("{name}", Objects.requireNonNull(e.getOriginalFilename()));
                        // 初始化
                        return initDto(logAction, request, actionPath, requestParams, currentSysName, currentDesc);
                    })
                    .collect(Collectors.toList());
        } else {
            throw new ServiceException("未找到能解析的Collection");
        }


    }


    /**
     * 批量操作数据处理
     */
    private static void handleData(String id, LogAction logAction, SysLogRecordDTO.AddDTO dto) {
        // 设置记录ID
        dto.setRecordId(id);
        if (StringUtils.isBlank(dto.getResponseParams()) || StringUtils.isNotBlank(dto.getErrorMsg())) {
            // 批量操作描述:（操作行为）了（系统模块） id为: 结果为:
            String lastResult = CharSequenceUtil.format(BATCH_OPERATION_LOG_DEC, logAction.value().getName(), dto.getSystemModule(), id, "异常");
            // 设置当前描述
            dto.setDescription(lastResult);
            return;
        }
        String data = new JSONObject(dto.getResponseParams()).getStr("data");
        if (null == data || !(JSONUtil.isTypeJSONArray(data))) {
            // 设置当前描述
            // 批量操作描述:（操作行为）了（系统模块） id为: 结果为:
            String lastResult = CharSequenceUtil.format(BATCH_OPERATION_LOG_DEC, logAction.value().getName(), dto.getSystemModule(), id, "操作成功");
            // 设置当前描述
            dto.setDescription(lastResult);
            return;
        }
        List<BatchResultDTO> list = JSONUtil.toList(data, BatchResultDTO.class);
        BatchResultDTO currentResult = list.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
        if (null == currentResult) {
            return;
        }
        // 批量操作描述:（操作行为）了（系统模块） id为: 结果为:
        String lastResult = CharSequenceUtil.format(BATCH_OPERATION_LOG_DEC, logAction.value().getName(), dto.getSystemModule(), id, currentResult.getMsg());
        // 设置当前描述
        dto.setDescription(lastResult);
        // 设置记录Code
        dto.setRecordCode(currentResult.getCode());
    }

    /**
     * 系统日志DTO初始化
     */
    private static SysLogRecordDTO.AddDTO initDto(LogAction logAction,
                                                  HttpServletRequest request,
                                                  String actionPath,
                                                  String requestParams,
                                                  String currentSysName,
                                                  String description) {
        SysLogRecordDTO.AddDTO dto = new SysLogRecordDTO.AddDTO();
        dto.setSystemModule(currentSysName);
        dto.setAction(logAction.value().getCode());
        // 记录当前请求ip
        dto.setIp(null == request ? "0.0.0.0" : IpUtils.getIpAddress(request));
        dto.setPath(actionPath);
        dto.setRequestParams(requestParams);
        if (StringUtils.isNotBlank(description)) {
            dto.setDescription(description);
        } else {
            dto.setDescription(logAction.desc());
        }
        // 缓存获取其他信息
        UpdateRecordItemBO bo = LOG_INFO_THREAD_LOCAL.get();
        BeanUtil.copyProperties(bo, dto);
        if (null != bo) {
            dto.setStatus(StringUtils.isBlank(bo.getErrorMsg()) ? LogStatusEnum.SUCCESS.getName() : LogStatusEnum.ERROR.getName());
        }
        return dto;
    }


    /**
     * 获取批量处理IDS
     */
    private static List<String> parseIds(LogAction logAction, String requestParams) {
        // 兼容直接List<String>数组提交
        if (JSONUtil.isTypeJSONArray(requestParams)){
            JSONArray jsonArray = new JSONArray(requestParams);
            if (!jsonArray.isEmpty() && (jsonArray.get(0) instanceof String)){
                return jsonArray.toList(String.class);
            }
        }

        String idsKey = logAction.value().checkAndGetKeyIdName(logAction.keyIdName());
        if (StringUtils.isBlank(idsKey)) {
            throw new ServiceException("未找到批量查询字段:" + idsKey);
        }
        // 兼容非json参数
        if (!JSONUtil.isTypeJSON(requestParams)){
            return Collections.singletonList(requestParams);
        }
        Object idsValueObj = new JSONObject(requestParams).get(idsKey);
        if (null == idsValueObj) {
            // 兼容旧单删除(单id删除)
            Object idValueObj = new JSONObject(requestParams).get("id");
            if (null != idValueObj){
                return Collections.singletonList(idValueObj.toString());
            }
            String msg = CharSequenceUtil.format("未找到批量查询字段内容,key={}", idsKey);
            throw new ServiceException(msg);
        }
        // 兼容非List
        if (!(idsValueObj instanceof List<?>)) {
            return Collections.singletonList(idsValueObj.toString());
        }
        List<String> idsResult = new LinkedList<>();
        for (Object o : (List<?>) idsValueObj) {
            idsResult.add((String) o);
        }
        return idsResult;
    }

    /**
     * 解析系统名称
     */
    private static String parseSystemModule(JoinPoint joinPoint) {
        // 模块注解:优先级：方法上->类上->默认系统名称
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogSystemModule systemModule = method.getAnnotation(LogSystemModule.class);
        if (null == systemModule) {
            systemModule = joinPoint.getTarget().getClass().getAnnotation(LogSystemModule.class);
        }
        // 菜单系统模块名称
        String currentSysName = null != systemModule ? systemModule.value() : "";
        if (StringUtils.isBlank(currentSysName)) {
            currentSysName = SpringUtil.getApplicationName();
        }
        return currentSysName;
    }


    /**
     * 请求前处理和查询更新后的对象
     *
     * @return 更新后的对象
     */
    private Object afterFindObj(ProceedingJoinPoint joinPoint, LogAction controllerLog, Object id) {
        if (!LogActionEnum.UPDATE.equals(controllerLog.value()) && !LogActionEnum.UPDATE_AND_SUBMIT.equals(controllerLog.value())) {
            return null;
        }
        if (null == id){
            return null;
        }
        // 查询更新后的信息
        return invokeViewMethod(joinPoint.getTarget(), id);
    }

    /**
     * 请求前处理和查询更新前的对象
     *
     * @return 更新前的对象
     */
    public Object beforeFindObj(JoinPoint joinPoint, LogAction controllerLog, Object id) {
        if (!controllerLog.value().hasCompare()) {
            return null;
        }
        if (null == id){
            return null;
        }
        // 记录更新前后信息
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            return null;
        }
        Object originalData = invokeViewMethod(joinPoint.getTarget(), id.toString());
        // code信息
        String code = parseCodeByObj(originalData);
        UpdateRecordItemBO bo = LOG_INFO_THREAD_LOCAL.get();
        bo.setRecordId(id.toString());
        bo.setRecordCode(StringUtils.isNotBlank(code) ? code : "");
        LOG_INFO_THREAD_LOCAL.set(bo);
        return originalData;
    }

    /**
     * 查询view相关信息
     * 带{@link LogViewService} 优先查询
     * 二次查询controller中的view方法
     *
     * @param target 当前controller对象
     * @param idObj  查询id
     * @return 查询结果
     */
    private Object invokeViewMethod(Object target, Object idObj) {
        try {
            // 实际调用方法
            Method targetMethod = null;
            // 带LogViewService注解的方法
            Method annoViewMethod = null;
            // 名为view的方法
            Method nameViewMethod = null;
            // 调用结果
            Object invokeResult = null;
            Result result = getResult(target, nameViewMethod, annoViewMethod);
            // 启用带LogViewService注解的方法
            if (null != result.annoViewMethod) {
                targetMethod = result.annoViewMethod;
            }
            //  启用名为view的方法
            if (null != result.nameViewMethod && null == targetMethod) {
                targetMethod = result.nameViewMethod;
            }
            if (null != targetMethod) {
                invokeResult = getObject(target, idObj, targetMethod, invokeResult);
                // 解析结果
                if (invokeResult instanceof ApiResult) {
                    return ((ApiResult<?>) invokeResult).getData();
                }
            }
            log.error("[系统日志]未找到查看的view方式或view注解或查询异常:[{}]", JSONUtil.toJsonStr(invokeResult));
            return null;
        } catch (InvocationTargetException e) {
            log.error("[系统日志]未找到查询view异常:[{}]", e.getTargetException().getMessage());
        } catch (Exception e) {
            log.error("[系统日志]未找到查询view异常:[{}]", e.getMessage());
        }
        return null;
    }

    private static Object getObject(Object target, Object idObj, Method targetMethod, Object invokeResult) throws IllegalAccessException, InvocationTargetException {
        if (targetMethod.isAnnotationPresent(PostMapping.class)) {
            // view方法为Post
            BaseIdDTO dto = new BaseIdDTO();
            dto.setId(idObj.toString());
            invokeResult = targetMethod.invoke(target, dto);
        } else if (targetMethod.isAnnotationPresent(GetMapping.class)) {
            // view方法为Get
            invokeResult = targetMethod.invoke(target, idObj);
        } else if (targetMethod.isAnnotationPresent(RequestMapping.class)){
            // 兼容旧接口
            invokeResult = targetMethod.invoke(target, idObj);
        }
        return invokeResult;
    }

    private static Result getResult(Object target, Method nameViewMethod, Method annoViewMethod) {
        for (Method method : target.getClass().getDeclaredMethods()) {
            // 记录view方法
            if ("view".equals(method.getName())) {
                nameViewMethod = method;
            }
            if (method.isAnnotationPresent(LogViewService.class)) {
                annoViewMethod = method;
                break;
            }
        }
        Result result = new Result(annoViewMethod, nameViewMethod);
        return result;
    }

    private static class Result {
        public final Method annoViewMethod;
        public final Method nameViewMethod;

        public Result(Method annoViewMethod, Method nameViewMethod) {
            this.annoViewMethod = annoViewMethod;
            this.nameViewMethod = nameViewMethod;
        }
    }

    /**
     * 从view数据查询code
     */
    private String parseCodeByObj(Object object) {
        String code = new JSONObject(object).getStr("code");
        if (StringUtils.isBlank(code)) {
            // 此处兼容其他code字段
        }
        return code;
    }


    /**
     * 默认解析controller的主实体做为类名
     */
    private String parseEntityClassName(JoinPoint joinPoint) {
        String controllerClassName = joinPoint.getTarget().getClass().getName();
        // class com.erp.model.sys.entity.BiDataSourceCustomEntity
        return "class ".concat(controllerClassName
                .replace("controller.api", "entity")
                .replace("server", "model")
                .replace("Controller", "Entity"));
    }

    /**
     * 生成对比描述
     */
    private String compareDataDesc(Object original, Object updated, LogAction logAction, String classPath) {
        if (!logAction.value().hasCompare()) {
            return "";
        }
        if (null == original || null == updated){
            throw new ServiceException("未找到对比请求详情前对象或请求详情后对象:请检查提交的keyIdName是否是id");
        }

        // 查询需要记录修改的字段
        List<SysLogRecordFieldListDTO> fieldList = sysLogRecordFieldFeign.list(new SysLogRecordFieldDTO.ListDTO(Collections.singletonList(classPath)));
        if (CollectionUtils.isEmpty(fieldList)) {
            return "";
        }
        log.debug("修改前对象: \n{}", JSONUtil.toJsonStr(original));
        log.debug("修改后对象: \n{}", JSONUtil.toJsonStr(updated));
        // 对比
        ReflectionDiffBuilder<?> diffBuilder = new ReflectionDiffBuilder<>(original, updated, ToStringStyle.JSON_STYLE);
        List<Diff<?>> diffList = diffBuilder.build().getDiffs();
        if (CollectionUtils.isEmpty(diffList) || CollectionUtils.isEmpty(fieldList)) {
            return "";
        }
        // Map<字段名, Diff对象>
        Map<String, Diff<?>> diffMap = diffList.stream()
                .collect(Collectors.toMap(Diff::getFieldName, Function.identity()));

        // 主字段和子字段分组: true=子字段, false= 主字段
        Map<Boolean, List<SysLogRecordFieldListDTO>> listMap = fieldList.stream()
                .collect(Collectors.groupingBy(e -> e.getField().contains(".")));

        // 修改内容Builder
        StringBuilder stringBuilder = new StringBuilder();
        // 主数据对比记录
        appendByMain(listMap.get(false), diffMap, stringBuilder);
        // 明细数据对比记录
        appendByDetail(listMap.get(true), diffMap, stringBuilder);

        return stringBuilder.toString();
    }

    /**
     * 生成日志明细对比
     */
    private void appendByDetail(List<SysLogRecordFieldListDTO> detailFieldList, Map<String, Diff<?>> diffMap, StringBuilder stringBuilder) {
        if (CollectionUtils.isEmpty(detailFieldList)) {
            return;
        }
        String field = detailFieldList.get(0).getField();
        // 明细类名
        String subField = CharSequenceUtil.subBefore(field, ".", false);
        if (StringUtils.isBlank(subField)) {
            return;
        }
        Diff<?> diff = diffMap.get(subField);
        if (null == diff) {
            return;
        }
        Object oldSubObj = diff.getLeft();
        Object newSubObj = diff.getRight();
        if (!(oldSubObj instanceof List) || !(newSubObj instanceof List)) {
            throw new ServiceException("不支持非数组的明细对象");
        }
        // 转化成Map<明细ID, 明细对象>
        Map<String, ?> oldDetailMap = ((List<?>) diff.getLeft())
                .stream()
                .collect(Collectors.toMap(e -> ReflectUtil.getFieldValue(e, "id").toString(), Function.identity()));

        Map<String, ?> newDetailMap = ((List<?>) diff.getRight())
                .stream()
                .collect(Collectors.toMap(e -> ReflectUtil.getFieldValue(e, "id").toString(), Function.identity()));
        // 删除的明细
        List<Object> deleteList = new ArrayList<>();

        // 更新的明细
        List<Tuple> udpateList = new LinkedList<>();

        oldDetailMap.forEach((key, obj) -> {
            Object newObj = newDetailMap.get(key);
            // 已删除的
            if (null == newObj) {
                deleteList.add(obj);
            } else {
                udpateList.add(new Tuple(obj, newObj));
                newDetailMap.remove(key);
            }
        });
        // 新增的明细
        List<Object> addList = new ArrayList<>(newDetailMap.values());

        // 删除描述
        checkAndCreateLogDesc(stringBuilder, deleteList, DETAIL_DELETE_LOG_DEC);

        // 添加描述
        checkAndCreateLogDesc(stringBuilder, addList, DETAIL_ADD_LOG_DEC);

        // 更新记录
        if (CollectionUtils.isEmpty(udpateList)) {
            return;
        }
        Map<String, SysLogRecordFieldListDTO> detailFieldMap = detailFieldList.stream()
                .collect(Collectors.toMap(e -> CharSequenceUtil.subAfter(e.getField(), ".", true), Function.identity()));

        // 遍历更新的明细
        udpateList.forEach(tuple -> {
            ReflectionDiffBuilder<?> diffBuilder = new ReflectionDiffBuilder<>(tuple.get(0), tuple.get(1), ToStringStyle.JSON_STYLE);
            DiffResult<?> diffResult = diffBuilder.build();
            if (null == diffResult) {
                return;
            }
            List<Diff<?>> diffList = diffResult.getDiffs();
            diffList.forEach(diffObj -> {
                // 是否配置
                SysLogRecordFieldListDTO fieldDto = detailFieldMap.get(diffObj.getFieldName());
                if (null == fieldDto) {
                    return;
                }

                // 组合日志描述
                String logDesc = CharSequenceUtil.format(DETAIL_UPDATE_LOG_DEC,
                        fieldDto.getFieldName(),
                        fieldDto.parseDescByType(diffObj.getLeft()),
                        fieldDto.parseDescByType(diffObj.getRight()));
                stringBuilder.append(logDesc);
            });
        });
    }

    /**
     * 检查和生成日志描述
     */
    private void checkAndCreateLogDesc(StringBuilder stringBuilder, List<Object> addList, String detailAddLogDec) {
        if (CollectionUtils.isEmpty(addList)) {
            return;
        }
        addList.forEach(obj -> {
            Object idObj = ReflectUtil.getFieldValue(obj, "id");
            if (null == idObj) {
                throw new ServiceException("找不到实体的ID, class=".concat(obj.getClass().toString()));
            }
            String deleteLogDesc = CharSequenceUtil.format(detailAddLogDec, idObj.toString());
            stringBuilder.append(deleteLogDesc);
        });
    }

    /**
     * 拼接主数据修改日志
     */
    private void appendByMain(List<SysLogRecordFieldListDTO> mainfieldList, Map<String, Diff<?>> diffMap, StringBuilder stringBuilder) {
        if (CollectionUtils.isEmpty(mainfieldList)) {
            return;
        }
        mainfieldList.forEach(fieldDto -> {
            Diff<?> diff = diffMap.get(fieldDto.getField());
            if (null == diff) {
                return;
            }
            // 组合日志描述
            String logDesc = CharSequenceUtil.format(MAIN_UPDATE_LOG_DEC,
                    fieldDto.getFieldName(),
                    fieldDto.parseDescByType(diff.getLeft()),
                    fieldDto.parseDescByType(diff.getRight()));
            stringBuilder.append(logDesc);
        });
    }

    /**
     * 检查和处理响应
     */
    private Object checkAndResolveException(Object obj, Throwable otherException) {
        // 打印其他日志
        if (null != otherException) {
            log.error("");
        }
        // 非异常正常响应
        if (!(obj instanceof Throwable)) {
            return obj;
        }
        return reflectResolveException(obj);
    }

    /**
     * 反射解析处理的异常
     */
    private Object reflectResolveException(Object obj) {
        // 当前异常class
        Class<?> exceptionClass = obj.getClass();
        // 异常处理拥有的方法
        Method[] methods = ReflectUtil.getMethods(globalExceptionHandler.getClass());
        for (Method method : methods) {
            ExceptionHandler annotation = method.getAnnotation(ExceptionHandler.class);
            if (null == annotation) {
                continue;
            }
            Class<? extends Throwable> currentClass = Arrays.stream(annotation.value()).findFirst().orElse(null);
            if (null != currentClass && exceptionClass == currentClass) {
                try {
                    return method.invoke(globalExceptionHandler, obj);
                } catch (Exception e) {
                    // 调用globalExceptionHandler失败：全局异常解析失败
                    ApiResult<?> result = new ApiResult<>();
                    result.setCode(ApiError.GLOBAL_EXCEPTION_HANDLER_METHOD_ERROR.code);
                    result.setMsg(CharSequenceUtil.format(ApiError.GLOBAL_EXCEPTION_HANDLER_METHOD_ERROR.msg, e.getMessage()));
                    return result;
                }
            }
        }
        // 找不到globalExceptionHandler异常, 默认提示未知异常
        ApiResult<?> result = new ApiResult<>();
        result.setCode(ApiError.GLOBAL_EXCEPTION_UN_KNOW.code);
        result.setMsg(CharSequenceUtil.format(ApiError.GLOBAL_EXCEPTION_UN_KNOW.msg, JSONUtil.toJsonStr(obj)));
        return result;
    }


}