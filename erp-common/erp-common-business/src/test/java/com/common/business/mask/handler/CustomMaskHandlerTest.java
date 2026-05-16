package com.common.business.mask.handler;

import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskStrategy;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * {@link CustomMaskHandler} 单元测试
 *
 * <p>验证三道防线：</p>
 * <ol>
 *   <li>正常正则正确脱敏</li>
 *   <li>命中 ReDoS 黑名单的正则被编译成 INVALID 哨兵，运行时原样返回</li>
 *   <li>超长输入跳过匹配，原样返回</li>
 * </ol>
 *
 * @author cloud-erp
 */
public class CustomMaskHandlerTest {

    private CustomMaskHandler handler;

    @Before
    public void setUp() {
        handler = new CustomMaskHandler();
    }

    @Test
    public void strategyShouldBeCustom() {
        assertEquals(MaskStrategy.CUSTOM, handler.strategy());
    }

    // ============ 正常 ============

    @Test
    public void shouldReplaceDigitsWithValidRegex() {
        MaskContext ctx = MaskContext.builder()
                .classPath("com.demo.Foo").fieldName("phone")
                .regex("[0-9]").replacement("*").build();
        assertEquals("abc***def", handler.handle("abc123def", ctx));
    }

    @Test
    public void shouldMaskMiddleWithLookaround() {
        MaskContext ctx = MaskContext.builder()
                .classPath("com.demo.Foo").fieldName("plate")
                .regex("(?<=.{2}).*(?=.{2})").replacement("*").build();
        assertEquals("京A*45", handler.handle("京A12345", ctx));
    }

    // ============ 哨兵：编译失败 / 命中黑名单 ============

    @Test
    public void shouldReturnOriginalWhenRegexUnsafe() {
        // (a+)+ 会被 RegexSafetyGuard 拒绝，编译成 INVALID 哨兵
        MaskContext ctx = MaskContext.builder()
                .classPath("com.demo.Foo").fieldName("evil")
                .regex("(a+)+").replacement("*").build();
        String input = "aaaaaab";
        // 关键：不仅要返回原值，更重要的是不应该卡死（如果真的执行了正则，会指数级回溯）
        long start = System.nanoTime();
        Object out = handler.handle(input, ctx);
        long costMs = (System.nanoTime() - start) / 1_000_000L;
        assertEquals(input, out);
        assertSame("INVALID 哨兵应原样返回，不进入 matcher", input, out);
        // 防御性断言：编译路径 < 50ms，远低于实际执行 (a+)+ 的回溯耗时（秒级）
        org.junit.Assert.assertTrue("ReDoS 防护必须在 50ms 内决断，实际 " + costMs + "ms", costMs < 50);
    }

    @Test
    public void shouldReturnOriginalWhenRegexInvalidSyntax() {
        MaskContext ctx = MaskContext.builder()
                .classPath("com.demo.Foo").fieldName("x")
                .regex("[unclosed").replacement("*").build();
        assertEquals("hello", handler.handle("hello", ctx));
    }

    // ============ 防御：非 String / 空 regex ============

    @Test
    public void shouldReturnAsIsForNonStringValue() {
        MaskContext ctx = MaskContext.builder()
                .regex("[0-9]+").replacement("*").build();
        Integer in = 12345;
        assertSame(in, handler.handle(in, ctx));
    }

    @Test
    public void shouldReturnAsIsForBlankRegex() {
        MaskContext ctx = MaskContext.builder().regex("").replacement("*").build();
        assertEquals("abc123", handler.handle("abc123", ctx));
    }

    @Test
    public void shouldReturnAsIsForNullValue() {
        MaskContext ctx = MaskContext.builder()
                .regex("[0-9]+").replacement("*").build();
        assertNull(handler.handle(null, ctx));
    }

    // ============ 长度护栏 ============

    @Test
    public void shouldSkipOversizedInput() {
        StringBuilder sb = new StringBuilder(RegexSafetyGuard.MAX_INPUT_LEN + 100);
        for (int i = 0; i < RegexSafetyGuard.MAX_INPUT_LEN + 100; i++) {
            sb.append('a');
        }
        String big = sb.toString();
        MaskContext ctx = MaskContext.builder()
                .classPath("com.demo.Foo").fieldName("bigText")
                .regex("a").replacement("*").build();
        assertSame("超长输入应原样返回（同一引用）", big, handler.handle(big, ctx));
    }

    @Test
    public void shouldProcessJustBelowLengthLimit() {
        StringBuilder sb = new StringBuilder(RegexSafetyGuard.MAX_INPUT_LEN);
        for (int i = 0; i < RegexSafetyGuard.MAX_INPUT_LEN; i++) {
            sb.append('a');
        }
        String big = sb.toString();
        MaskContext ctx = MaskContext.builder()
                .classPath("com.demo.Foo").fieldName("ok")
                .regex("a").replacement("*").build();
        Object out = handler.handle(big, ctx);
        org.junit.Assert.assertNotSame("等于阈值应被处理而非跳过", big, out);
    }

    // ============ Pattern 缓存（行为验证） ============

    @Test
    public void shouldReuseCachedPatternAcrossCalls() {
        MaskContext ctx1 = MaskContext.builder()
                .classPath("X").fieldName("y")
                .regex("[0-9]+").replacement("*").build();
        MaskContext ctx2 = MaskContext.builder()
                .classPath("X").fieldName("z")
                .regex("[0-9]+").replacement("*").build();
        assertEquals("a*b", handler.handle("a123b", ctx1));
        assertEquals("c*d", handler.handle("c456d", ctx2));
    }
}
