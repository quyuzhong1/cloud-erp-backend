package com.erp.server.dmp.push.service.wdt;

import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.server.dmp.service.DictBasicService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WdtErrorMsgTranslatorTest {

    @Test
    public void shouldTranslateEnglishErrorWhenKeywordMatched() {
        List<DictBasicEntity> mappings = Arrays.asList(
                mapping("check_fail", "旺店通审核校验失败", 90),
                mapping("Unknown Database Error", "旺店通侧数据库异常，非数大臣数据问题，请联系旺店通支持", 100)
        );
        String display = WdtErrorMsgTranslator.resolveDisplayMsg("check_fail Unknown Database Error", mappings);
        Assert.assertEquals("【旺店通侧数据库异常，非数大臣数据问题，请联系旺店通支持】原始信息：check_fail Unknown Database Error",
                display);
    }

    @Test
    public void shouldReturnRawMsgWhenPureChinese() {
        List<DictBasicEntity> mappings = Collections.singletonList(
                mapping("check_fail", "旺店通审核校验失败", 90)
        );
        Assert.assertEquals("货位不存在", WdtErrorMsgTranslator.resolveDisplayMsg("货位不存在", mappings));
        Assert.assertEquals("库存不足", WdtErrorMsgTranslator.resolveDisplayMsg("库存不足", mappings));
    }

    @Test
    public void shouldReturnRawMsgWhenChineseWithLocationOrSkuCode() {
        List<DictBasicEntity> mappings = Collections.singletonList(
                mapping("check_fail", "旺店通审核校验失败", 90)
        );
        Assert.assertEquals("货位不存在 A-01-01",
                WdtErrorMsgTranslator.resolveDisplayMsg("货位不存在 A-01-01", mappings));
        Assert.assertEquals("货位不存在 WH001",
                WdtErrorMsgTranslator.resolveDisplayMsg("货位不存在 WH001", mappings));
        Assert.assertEquals("msg=库存不足",
                WdtErrorMsgTranslator.resolveDisplayMsg("msg=库存不足", mappings));
    }

    @Test
    public void shouldReturnRawMsgWhenEnglishNotMatched() {
        List<DictBasicEntity> mappings = Collections.singletonList(
                mapping("check_fail", "旺店通审核校验失败", 90)
        );
        String display = WdtErrorMsgTranslator.resolveDisplayMsg("some_new_error_xyz", mappings);
        Assert.assertEquals("some_new_error_xyz", display);
    }

    @Test
    public void shouldDetectEnglishMappingEligibility() {
        Assert.assertTrue(WdtErrorMsgTranslator.shouldTryEnglishMapping("check_fail Unknown Database Error"));
        Assert.assertTrue(WdtErrorMsgTranslator.shouldTryEnglishMapping("some_new_error_xyz"));
        Assert.assertFalse(WdtErrorMsgTranslator.shouldTryEnglishMapping("货位不存在"));
        Assert.assertFalse(WdtErrorMsgTranslator.shouldTryEnglishMapping("货位不存在 A-01-01"));
        Assert.assertFalse(WdtErrorMsgTranslator.shouldTryEnglishMapping("货位不存在 WH001"));
        Assert.assertFalse(WdtErrorMsgTranslator.shouldTryEnglishMapping("msg=库存不足"));
        Assert.assertTrue(WdtErrorMsgTranslator.containsCjk("货位不存在"));
        Assert.assertFalse(WdtErrorMsgTranslator.containsCjk("check_fail"));
    }

    @Test
    public void translate_shouldReturnRawWhenDictEmpty() {
        DictBasicService dictBasicService = mock(DictBasicService.class);
        when(dictBasicService.getByKey(eq(WdtErrorMsgTranslator.DICT_TYPE))).thenReturn(Collections.emptyList());
        WdtErrorMsgTranslator translator = newTranslator(dictBasicService);

        Assert.assertEquals("some_new_error_xyz", translator.translate(20, "some_new_error_xyz"));
    }

    @Test
    public void translate_shouldReturnFormattedWhenMappingMatched() {
        DictBasicService dictBasicService = mock(DictBasicService.class);
        when(dictBasicService.getByKey(eq(WdtErrorMsgTranslator.DICT_TYPE))).thenReturn(Collections.singletonList(
                mapping("Unknown Database Error", "旺店通侧数据库异常，非数大臣数据问题，请联系旺店通支持", 100)
        ));
        WdtErrorMsgTranslator translator = newTranslator(dictBasicService);

        Assert.assertEquals("【旺店通侧数据库异常，非数大臣数据问题，请联系旺店通支持】原始信息：check_fail Unknown Database Error",
                translator.translate(20, "check_fail Unknown Database Error"));
    }

    @Test
    public void translate_shouldReturnRawWhenDictLoadFails() {
        DictBasicService dictBasicService = mock(DictBasicService.class);
        when(dictBasicService.getByKey(eq(WdtErrorMsgTranslator.DICT_TYPE))).thenThrow(new RuntimeException("dict load failed"));
        WdtErrorMsgTranslator translator = newTranslator(dictBasicService);

        Assert.assertEquals("check_fail error", translator.translate(20, "check_fail error"));
    }

    private static WdtErrorMsgTranslator newTranslator(DictBasicService dictBasicService) {
        WdtErrorMsgTranslator translator = new WdtErrorMsgTranslator();
        ReflectionTestUtils.setField(translator, "dictBasicService", dictBasicService);
        return translator;
    }

    private static DictBasicEntity mapping(String name, String value, int sort) {
        DictBasicEntity entity = new DictBasicEntity();
        entity.setName(name);
        entity.setValue(value);
        entity.setRemark(WdtErrorMsgTranslator.MATCH_TYPE_KEYWORD);
        entity.setSort(sort);
        entity.setStatus(Boolean.TRUE);
        return entity;
    }
}
