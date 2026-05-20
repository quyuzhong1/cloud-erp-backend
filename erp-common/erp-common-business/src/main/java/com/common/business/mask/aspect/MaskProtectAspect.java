package com.common.business.mask.aspect;

import com.common.business.mask.protect.MaskProtectInputProcessor;
import com.common.business.mask.protect.MaskProtectException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
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
        if (transactionTemplate != null) {
            return transactionTemplate;
        }
        if (transactionManager == null) {
            return null;
        }
        return new TransactionTemplate(transactionManager);
    }

    private static class MaskProtectProceedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private MaskProtectProceedException(Throwable cause) {
            super(cause);
        }
    }
}
