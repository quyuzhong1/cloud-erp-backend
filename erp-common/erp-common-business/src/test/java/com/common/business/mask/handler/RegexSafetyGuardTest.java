package com.common.business.mask.handler;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link RegexSafetyGuard} 单元测试
 *
 * <p>覆盖 3 类已知 ReDoS 模式族 + 边界条件，确保黑名单不漏放危险正则、也不误杀正常业务正则。</p>
 *
 * @author cloud-erp
 */
public class RegexSafetyGuardTest {

    // ============ 安全（应允许） ============

    @Test
    public void shouldAllowEmptyOrNull() {
        assertNull("null 视为不需要正则脱敏，允许", RegexSafetyGuard.check(null));
        assertNull("空串视为不需要正则脱敏，允许", RegexSafetyGuard.check(""));
    }

    @Test
    public void shouldAllowCommonBusinessRegex() {
        assertNull("银行卡号 16-19 位数字范围", RegexSafetyGuard.check("[0-9]{16,19}"));
        assertNull("身份证基础模式", RegexSafetyGuard.check("[1-9][0-9]{16}[0-9Xx]"));
        assertNull("环视脱敏（前 3 + 后 4 保留）", RegexSafetyGuard.check("(?<=.{3}).*(?=.{4})"));
        assertNull("简单邮箱（无嵌套量词）", RegexSafetyGuard.check("[^@]+@[^@]+"));
        assertNull("固定字面量", RegexSafetyGuard.check("^prefix_"));
        assertNull("普通字符类带量词", RegexSafetyGuard.check("\\d{4}-\\d{4}-\\d{4}-\\d{4}"));
    }

    // ============ 嵌套量词（应拒绝） ============

    @Test
    public void shouldRejectNestedQuantifierPlusPlus() {
        String reason = RegexSafetyGuard.check("(a+)+");
        assertNotNull("(a+)+ 是经典灾难性回溯模式", reason);
        assertTrue(reason, reason.contains("嵌套量词"));
    }

    @Test
    public void shouldRejectNestedQuantifierStarStar() {
        assertNotNull(RegexSafetyGuard.check("(a*)*"));
    }

    @Test
    public void shouldRejectNestedQuantifierPlusStar() {
        assertNotNull(RegexSafetyGuard.check("(a+)*"));
        assertNotNull(RegexSafetyGuard.check("(a*)+"));
    }

    @Test
    public void shouldRejectNestedQuantifierWithCharClass() {
        assertNotNull("字符类嵌套量词也应拦", RegexSafetyGuard.check("(\\w+)+"));
    }

    // ============ 歧义分支 + 量词（应拒绝） ============

    @Test
    public void shouldRejectAmbiguousAlternationIdentical() {
        String reason = RegexSafetyGuard.check("(a|a)+");
        assertNotNull("(a|a)+ 同样指数级回溯", reason);
        assertTrue(reason, reason.contains("歧义分支"));
    }

    @Test
    public void shouldRejectAmbiguousAlternationOverlap() {
        assertNotNull("(abc|abd)+ 前缀重叠，回溯爆炸",
                RegexSafetyGuard.check("(abc|abd)+"));
    }

    // ============ 连续贪婪量词（应拒绝） ============

    @Test
    public void shouldRejectConsecutiveGreedy() {
        String reason = RegexSafetyGuard.check("a*a*");
        assertNotNull("a*a* 可拆分歧义", reason);
        assertTrue(reason, reason.contains("连续贪婪量词"));
    }

    // ============ 长度上限 ============

    @Test
    public void shouldRejectOverlongRegex() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < RegexSafetyGuard.MAX_REGEX_LEN + 10; i++) {
            sb.append('a');
        }
        String reason = RegexSafetyGuard.check(sb.toString());
        assertNotNull(reason);
        assertTrue(reason, reason.contains("超过上限"));
    }

    // ============ checkAndCompile：编译可行性 ============

    @Test
    public void checkAndCompileShouldRejectInvalidSyntax() {
        String reason = RegexSafetyGuard.checkAndCompile("[unclosed");
        assertNotNull("语法错误的正则应被拒", reason);
        assertTrue(reason, reason.contains("编译失败"));
    }

    @Test
    public void checkAndCompileShouldAllowValidRegex() {
        assertNull(RegexSafetyGuard.checkAndCompile("[0-9]+"));
    }

    @Test
    public void checkAndCompileShouldShortCircuitOnUnsafePattern() {
        // 嵌套量词应在静态检查阶段就被拒，不会进入 Pattern.compile（Pattern.compile 其实接受 (a+)+ 不报错）
        String reason = RegexSafetyGuard.checkAndCompile("(a+)+");
        assertNotNull(reason);
        assertTrue("应被静态分析拒绝而非编译错误", reason.contains("嵌套量词"));
    }
}
