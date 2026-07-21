package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.IsConstant;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.OperateLogSelectDTO;
import com.erp.model.plm.dto.OperateLogShowDTO;
import com.erp.server.plm.mapper.OperateLogMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 操作日志 content 脱敏测试（不启动 Spring 容器）。
 *
 * 运行全部用例：
 * mvn -pl erp-server/erp-server-plm -Dtest=OperateLogServiceImplContentMaskTest test
 *
 * 运行单个用例：
 * mvn -pl erp-server/erp-server-plm -Dtest=OperateLogServiceImplContentMaskTest#shouldMaskOldValueToNewValue test
 */
public class OperateLogServiceImplContentMaskTest {

    private OperateLogServiceImpl operateLogService;
    private OperateLogMapper operateLogMapper;

    @Before
    public void setUp() {
        operateLogService = new OperateLogServiceImpl();
        operateLogMapper = Mockito.mock(OperateLogMapper.class);
        ReflectionTestUtils.setField(operateLogService, "baseMapper", operateLogMapper);
    }

    @Test
    public void shouldMaskOldValueToNewValue() throws Exception {
        String raw = "编辑了[实际不含税成本]由[12.5]变更为[15.0]";
        String masked = invokeMaskSensitiveOperateLogContent(raw);
        Assert.assertEquals("编辑了[实际不含税成本]由***变更为***", masked);
    }

    @Test
    public void shouldMaskEmptyOldValueToNewValue() throws Exception {
        String raw = "编辑了[实际不含税成本]由空值变更为[15.0]";
        String masked = invokeMaskSensitiveOperateLogContent(raw);
        Assert.assertEquals("编辑了[实际不含税成本]由***变更为***", masked);
    }

    @Test
    public void shouldNotChangeWhenFieldIsNotSensitive() throws Exception {
        String raw = "编辑了[产品名称]由[旧品]变更为[新品]";
        String masked = invokeMaskSensitiveOperateLogContent(raw);
        Assert.assertEquals(raw, masked);
    }

    @Test
    public void shouldNotChangeWhenBlankContent() throws Exception {
        Assert.assertEquals("", invokeMaskSensitiveOperateLogContent(""));
        Assert.assertNull(invokeMaskSensitiveOperateLogContent(null));
    }

    @Test
    public void shouldMaskMultipleSensitiveSegments() throws Exception {
        String raw = "编辑了[实际不含税成本]由[1]变更为[2]；编辑了[实际不含税成本]由空值变更为[3]";
        String masked = invokeMaskSensitiveOperateLogContent(raw);
        Assert.assertEquals(
                "编辑了[实际不含税成本]由***变更为***；编辑了[实际不含税成本]由***变更为***",
                masked);
    }

    @Test
    public void shouldKeepOtherFieldsWhenMixedContent() throws Exception {
        String raw = "编辑了[产品名称]由[A]变更为[B]；编辑了[实际不含税成本]由[12.5]变更为[15.0]";
        String masked = invokeMaskSensitiveOperateLogContent(raw);
        Assert.assertEquals(
                "编辑了[产品名称]由[A]变更为[B]；编辑了[实际不含税成本]由***变更为***",
                masked);
    }

    @Test
    public void pagingShouldApplyMaskOnRecords() {
        OperateLogShowDTO record = new OperateLogShowDTO();
        record.setContent("编辑了[实际不含税成本]由[12.5]变更为[15.0]");

        Page<OperateLogShowDTO> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(record));
        page.setTotal(1);

        when(operateLogMapper.paging(any(Page.class), any(OperateLogSelectDTO.class), eq(IsConstant.YES)))
                .thenReturn(page);

        PagingDTO<OperateLogSelectDTO> dto = new PagingDTO<>();
        dto.setCurrPage(1);
        dto.setPageSize(10);
        dto.setParams(new OperateLogSelectDTO());

        PagingVO<OperateLogShowDTO> result = operateLogService.paging(dto);

        Assert.assertEquals(1, result.getList().size());
        Assert.assertEquals("编辑了[实际不含税成本]由***变更为***", result.getList().get(0).getContent());
    }

    @Test
    public void pagingShouldMaskEachRecordInPage() {
        OperateLogShowDTO sensitive = new OperateLogShowDTO();
        sensitive.setContent("编辑了[实际不含税成本]由空值变更为[99.9]");

        OperateLogShowDTO normal = new OperateLogShowDTO();
        normal.setContent("编辑了[备注]由[旧]变更为[新]");

        Page<OperateLogShowDTO> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(sensitive, normal));
        page.setTotal(2);

        when(operateLogMapper.paging(any(Page.class), any(OperateLogSelectDTO.class), eq(IsConstant.YES)))
                .thenReturn(page);

        PagingDTO<OperateLogSelectDTO> dto = new PagingDTO<>();
        dto.setCurrPage(1);
        dto.setPageSize(10);
        dto.setParams(new OperateLogSelectDTO());

        PagingVO<OperateLogShowDTO> result = operateLogService.paging(dto);

        Assert.assertEquals("编辑了[实际不含税成本]由***变更为***", result.getList().get(0).getContent());
        Assert.assertEquals("编辑了[备注]由[旧]变更为[新]", result.getList().get(1).getContent());
    }

    private static String invokeMaskSensitiveOperateLogContent(String content) throws Exception {
        Method method = OperateLogServiceImpl.class.getDeclaredMethod("maskSensitiveOperateLogContent", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, content);
    }
}
