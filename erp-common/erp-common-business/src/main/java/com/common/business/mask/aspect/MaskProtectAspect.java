package com.common.business.mask.aspect;

import com.common.business.mask.protect.MaskProtectInputProcessor;
import com.common.business.mask.protect.MaskProtectException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 * 脱敏值回显保护入参切面。
 *
 * @author cloud-erp
 */
@Aspect
@Component
@Order(-50)
public class MaskProtectAspect {

    @Resource
    private MaskProtectInputProcessor inputProcessor;

    @Autowired(required = false)
    private TransactionTemplate transactionTemplate;

    @Autowired(required = false)
    private PlatformTransactionManager transactionManager;

    @Value("${mask.protect.transaction-timeout-seconds:8}")
    private Integer transactionTimeoutSeconds;

    @Pointcut("execution(public * com.erp.server.*.controller.api.*.*(..))")
    public void controllerApi() {
    }

    @Around("controllerApi()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        List<MaskProtectInputProcessor.FieldRestorePlan> plans = inputProcessor.collect(args);
        if (!inputProcessor.requiresDbCompare(plans)) {
            inputProcessor.validateAndApply(plans);
            return pjp.proceed(args);
        }

        // RESTORE_ORIGINAL 必须把锁读原值和旧业务更新放在同一个事务里，
        // 避免回填后、保存前被其他请求改掉同一行。
        TransactionTemplate template = resolveTransactionTemplate();
        if (template == null) {
            throw new MaskProtectException();
        }
        try {
            return template.execute(status -> {
                inputProcessor.validateAndApply(plans);
                try {
                    return pjp.proceed(args);
                } catch (Throwable e) {
                    status.setRollbackOnly();
                    throw new MaskProtectProceedException(e);
                }
            });
        } catch (MaskProtectProceedException e) {
            throw e.getCause();
        }
    }

    private TransactionTemplate resolveTransactionTemplate() {
        PlatformTransactionManager manager = transactionManager;
        if (manager == null && transactionTemplate != null) {
            manager = transactionTemplate.getTransactionManager();
        }
        if (manager == null) {
            return null;
        }
        TransactionTemplate template = transactionTemplate == null
                ? new TransactionTemplate(manager)
                : new TransactionTemplate(manager, transactionTemplate);

        // 每次创建独立模板，避免保护逻辑的超时时间污染全局 TransactionTemplate。
        int timeout = transactionTimeoutSeconds == null ? 8 : transactionTimeoutSeconds;
        if (timeout > 0) {
            template.setTimeout(timeout);
        } else {
            template.setTimeout(TransactionDefinition.TIMEOUT_DEFAULT);
        }
        return template;
    }

    private static class MaskProtectProceedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private MaskProtectProceedException(Throwable cause) {
            super(cause);
        }
    }
}
