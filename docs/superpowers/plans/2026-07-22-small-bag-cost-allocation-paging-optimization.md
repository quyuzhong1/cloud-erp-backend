# 小包费用分摊分页优化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. 步骤使用 checkbox（`- [ ]`）语法跟踪。

**目标：** 降低 `SmallBagCostAllocationController#queryByPage` 的响应耗时，同时严格保持 `small_bag_cost_allocation_detail.id` 粒度的实时精确总数、结果集合、排序、数据权限和高级查询语义。

**架构：** 用显式 `COUNT(DISTINCT h.id)` 替换 MyBatis-Plus 对宽查询自动生成的 count；列表查询拆分为“共享筛选条件的明细 ID 分页”和“按当前页 ID 回查展示字段”两阶段，Service 按第一阶段 ID 顺序恢复结果。将 TMS 中逐个“期间 + 币种”调用的 Feign 汇率查询替换为一次批量 DMP RPC，并复用 DMP 当前的汇率选取规则。

**技术栈：** Java 8、Spring Boot 2.3、MyBatis/MyBatis-Plus、PostgreSQL、Spring Cloud OpenFeign、Lombok、JUnit 4、Mockito、Maven。

## 全局约束

- 保持 `POST /smallBagCostAllocation/paging`、请求 DTO、`PagingVO` 结构、响应字段和异常行为兼容。
- `small_bag_cost_allocation_detail h.id` 是唯一的分页和总数统计粒度。
- 总数必须同步、实时、精确，并使用 `COUNT(DISTINCT h.id)`；不得估算、延迟、缓存或省略。
- count 与 ID 分页必须使用相同的关联、高级查询 SQL（`params.sqlMap.default`）、软删条件、数据权限 SQL 和用户可见的排序语义。
- ID 分页仅额外追加 `h.id` 作为稳定的最终 tie-breaker。详情查询不得独立筛选、排序、分页或重新计算权限。
- 保持已确认的 `t ↔ g` 和 `k ↔ l` 一对一关系；不得将这两条关系描述为结果行膨胀原因。
- 本次不修改 `@DataPermission`、`@WebAdvanceQuery`、`SmallBagCostAllocationQueryHandler`、既有查询条件配置或动态排序协议。
- TMS 对每页所有非 CNY 的 `(reportDate, unitCurrency)` 组合只发起一次批量汇率 RPC。若响应缺少汇率，仍抛出 `ServiceException("汇率为空，请维护汇率后再查询")`。
- 不允许应用代码、测试、迁移脚本或 Agent 执行数据库 DDL/DML。所有数据库 SQL 和 `EXPLAIN` 由用户执行。
- 使用模块级 Maven 测试。仓库没有本地 PostgreSQL Mapper integration-test 基座，SQL 等价性和执行计划须由用户在目标环境验证。

---

## 文件结构与职责

- 修改 `erp-server/erp-server-tms/src/main/java/com/erp/server/tms/mapper/SmallBagCostAllocationMapper.java`：将自动分页宽查询替换为显式 count、ID 分页和按 ID 查询详情的方法。
- 修改 `erp-server/erp-server-tms/src/main/resources/mapper/SmallBagCostAllocationMapper.xml`：定义公共 join/where/order 片段和三条显式 SQL。
- 修改 `erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImpl.java`：编排精确 count → ID 分页 → 详情回查，恢复 ID 顺序并调用批量汇率接口。
- 新增 `erp-server/erp-server-tms/src/test/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImplTest.java`：使用 Mockito 验证分页编排、顺序恢复和汇率缺失行为。
- 修改 `erp-model/erp-model-dmp/src/main/java/com/erp/model/dmp/dto/BiSettlementExchangeRateDTO.java`：增加批量汇率 RPC 的请求/响应 DTO。
- 修改 `erp-rpc/erp-rpc-dmp/src/main/java/com/erp/rpc/dmp/feign/DmpTaskFeign.java`：增加批量汇率 Feign 契约。
- 修改 `erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/controller/feign/DmpFeignController.java`：提供批量 Feign endpoint。
- 修改 `erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/service/BiSettlementExchangeRateService.java` 和 `.../impl/BiSettlementExchangeRateServiceImpl.java`：按当前单条查询规则批量解析汇率。
- 新增 `erp-server/erp-server-dmp/src/test/java/com/erp/server/dmp/service/impl/BiSettlementExchangeRateServiceImplTest.java`：验证批量结果与现有单条汇率选择规则一致。
- 新增 `docs/sql/2026-07-22-small-bag-cost-allocation-paging-index-review.sql`：仅供用户执行的索引盘点、候选 DDL、回滚和 `EXPLAIN` 模板。
- 新增 `docs/superpowers/plans/2026-07-22-small-bag-cost-allocation-paging-verification.md`：目标环境的一致性与性能记录清单。

---

### 任务 1：建立显式分页 Mapper 契约和公共 SQL 片段

**文件：**
- 修改：`erp-server/erp-server-tms/src/main/java/com/erp/server/tms/mapper/SmallBagCostAllocationMapper.java:3-40`
- 修改：`erp-server/erp-server-tms/src/main/resources/mapper/SmallBagCostAllocationMapper.xml:38-106`

**接口：**

```java
Long pagingCount(@Param("params") PagingParamDTO params);

List<String> pagingDetailIds(@Param("params") PagingParamDTO params,
                              @Param("offset") long offset,
                              @Param("pageSize") long pageSize);

List<ListDTO> selectByDetailIds(@Param("detailIds") List<String> detailIds);
```

- [ ] **步骤 1：替换旧 Mapper 分页方法声明**

  删除 `IPage<ListDTO> paging(Page query, ...)`，新增上述三个方法。删除不再使用的 `IPage` 和 `Page` import；保留 `tabList`、`listByReportPeriodStr`、`listSmallBagCost` 不变。

- [ ] **步骤 2：抽取公共的 `FROM` 与 `WHERE` SQL 片段**

  在 XML 中增加以下片段。关联范围与当前实现保持一致，只将 `h` 调整为驱动表：

  ```xml
  <sql id="smallBagPagingFrom">
      FROM small_bag_cost_allocation_detail h
      JOIN small_bag_cost_allocation g
        ON g.id = h.main_id AND g.is_deleted = false
      JOIN small_bag_cost_allocation_main t
        ON t.id = g.main_id AND t.is_deleted = false
      LEFT JOIN logistics_bill_cost j
        ON t.cost_id = j.id AND j.is_deleted = false
      LEFT JOIN logistics_bill k
        ON j.logistics_bill_id = k.id AND k.is_deleted = false
      LEFT JOIN logistics_bill_detail l
        ON k.id = l.main_id AND l.is_deleted = false
      LEFT JOIN dict_basic n
        ON h.fee_type = n.code AND n.type = 'dictCostCategory'
  </sql>

  <sql id="smallBagPagingWhere">
      <where>
          h.is_deleted = false
          ${params.sqlMap.default}
          <if test="params.permissionSql != null and params.permissionSql != ''">
              ${params.permissionSql}
          </if>
      </where>
  </sql>
  ```

  不新增任何用户可控的 `${...}`。保留当前由高级查询和数据权限切面生成的 SQL 注入点。

- [ ] **步骤 3：抽取稳定排序片段**

  ```xml
  <sql id="smallBagPagingOrder">
      ORDER BY
      <if test="params.sortList != null and params.sortList.size() > 0">
          <foreach collection="params.sortList" item="item" separator="," close=",">
              ${item.field} ${item.sort}
          </foreach>
      </if>
      t.id DESC, g.id, n.index ASC, h.id ASC
  </sql>
  ```

  该片段保留当前传入排序字段与默认排序，仅增加最终稳定排序键 `h.id ASC`。

- [ ] **步骤 4：增加精确 count 与第一阶段 ID SQL**

  ```xml
  <select id="pagingCount" resultType="java.lang.Long">
      SELECT COUNT(DISTINCT h.id)
      <include refid="smallBagPagingFrom"/>
      <include refid="smallBagPagingWhere"/>
  </select>

  <select id="pagingDetailIds" resultType="java.lang.String">
      SELECT h.id
      <include refid="smallBagPagingFrom"/>
      <include refid="smallBagPagingWhere"/>
      <include refid="smallBagPagingOrder"/>
      LIMIT #{pageSize} OFFSET #{offset}
  </select>
  ```

  不使用 MyBatis-Plus `Page` 参数，防止分页拦截器再次生成自动 count。

- [ ] **步骤 5：增加按当前页 ID 回查详情的 SQL**

  将原 `<select id="paging">` 中的展示字段 projection 完整复制到 `<select id="selectByDetailIds">`，保留原有 `CAST`、字段别名和展示 join，但不使用 `smallBagPagingWhere`、动态 `ORDER BY`、`LIMIT` 或 `OFFSET`：

  ```xml
  <select id="selectByDetailIds" resultType="com.erp.model.tms.dto.SmallBagCostAllocationDTO$ListDTO">
      SELECT
          t.id main_id, g.id, h.id AS detailId,
          t.cost_id, t.report_date, t.account_date, t.report_status,
          j.reconciliation_status, t.big_table_status, j.channel_id,
          k.outstock_code, j.transport_no, j.track_no, k.delivery_time,
          l.sign_time, l.track_status, j.confirm_time, j.pay_type,
          k.shop_name, k.shop_id, k.to_country, g.sku_no, g.sku_id,
          CAST(g.unit_cost AS NUMERIC(16, 6)) unit_cost, g.unit_currency,
          g.delivery_qty, CAST(g.billing_weight AS NUMERIC(16, 4)) billing_weight,
          CAST(j.billing_weight_logistics AS NUMERIC(16, 4)) billing_weight_logistics,
          CAST(g.sku_weight AS NUMERIC(16, 4)) sku_weight, t.fee_source,
          h.fee_type, CAST(h.bill_amount_exchange AS NUMERIC(16, 4)) billAmount,
          CAST(h.allocated_amount_exchange AS NUMERIC(16, 2)) allocatedAmount,
          CAST(h.product_allocated_amount_exchange AS NUMERIC(16, 6)) productAllocatedAmount,
          h.org_id, h.org_name orgName, h.fee_allocation_type,
          h.fee_allocation_type feeAllocationTypeName, h.weight_allocation_type,
          h.weight_allocation_type weightAllocationTypeName, t.create_time createTime,
          t.update_user_name updateUserName, t.update_time updateTime,
          h.allocated_currency currency, k.platform_code AS platformCode
      <include refid="smallBagPagingFrom"/>
      WHERE h.is_deleted = false
        AND h.id IN
        <foreach collection="detailIds" item="detailId" open="(" separator="," close=")">
            #{detailId}
        </foreach>
  </select>
  ```

- [ ] **步骤 6：编译 TMS 模块**

  ```bash
  mvn -pl erp-server/erp-server-tms -am -DskipTests compile
  ```

  预期：`BUILD SUCCESS`，不存在 Mapper 方法或 XML statement 解析错误。

- [ ] **步骤 7：提交 Mapper 改动**

  ```bash
  git add erp-server/erp-server-tms/src/main/java/com/erp/server/tms/mapper/SmallBagCostAllocationMapper.java \
          erp-server/erp-server-tms/src/main/resources/mapper/SmallBagCostAllocationMapper.xml
  git commit -m "perf: split small bag paging mapper queries"
  ```

### 任务 2：在 TMS Service 编排精确 count、ID 分页和顺序恢复

**文件：**
- 修改：`erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImpl.java:203-216,218-299`
- 新增：`erp-server/erp-server-tms/src/test/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImplTest.java`

**接口：** 输入任务 1 的三个 Mapper 方法，输出包含精确 `totalCount`、请求页码信息并与 `pagingDetailIds` 顺序一致的 `PagingVO<ListDTO>`。

- [ ] **步骤 1：先编写失败的 Service 编排测试**

  使用 JUnit 4、Mockito 和 `ReflectionTestUtils.setField`，与现有 `TmsAsyncTaskRecordServiceImplPageBatchBusinessIdsTest` 风格保持一致。将 `handleDataPaging` 从 `private` 改为 `protected`，使测试可通过 spy 屏蔽无关 Feign 补全逻辑。

  ```java
  @Test
  public void pagingUsesExactCountThenReturnsDetailsInIdPageOrder() {
      when(mapper.pagingCount(same(params))).thenReturn(3L);
      when(mapper.pagingDetailIds(same(params), eq(0L), eq(3L)))
          .thenReturn(Arrays.asList("h-3", "h-1", "h-2"));
      when(mapper.selectByDetailIds(Arrays.asList("h-3", "h-1", "h-2")))
          .thenReturn(Arrays.asList(dto("h-1"), dto("h-3"), dto("h-2")));

      PagingVO<ListDTO> result = service.paging(request(1, 3, params));

      assertEquals(3, result.getTotalCount());
      assertEquals(Arrays.asList("h-3", "h-1", "h-2"), detailIds(result));
      verify(mapper).pagingCount(same(params));
      verify(mapper).pagingDetailIds(same(params), eq(0L), eq(3L));
  }

  @Test
  public void pagingReturnsEmptyListWithoutIdOrDetailQueryWhenExactCountIsZero() {
      when(mapper.pagingCount(same(params))).thenReturn(0L);

      PagingVO<ListDTO> result = service.paging(request(2, 20, params));

      assertEquals(0, result.getTotalCount());
      assertTrue(result.getList().isEmpty());
      verify(mapper, never()).pagingDetailIds(any(), anyLong(), anyLong());
      verify(mapper, never()).selectByDetailIds(anyList());
  }
  ```

- [ ] **步骤 2：运行测试确认失败**

  ```bash
  mvn -pl erp-server/erp-server-tms -am -Dtest=SmallBagCostAllocationServiceImplTest -DfailIfNoTests=false test
  ```

  预期：由于 Service 仍调用旧 `paging` 方法或没有两阶段编排而失败。

- [ ] **步骤 3：实现两阶段分页流程**

  将 `paging` 替换为：

  ```java
  @Override
  public PagingVO<ListDTO> paging(PagingDTO<PagingParamDTO> dto) {
      PagingParamDTO params = dto.getParams();
      params.setPermissionSql(dto.getPermissionSql());

      long total = this.baseMapper.pagingCount(params);
      if (total == 0L) {
          return new PagingVO<>(Collections.emptyList(), 0, dto.getPageSize(), dto.getCurrPage());
      }

      long offset = ((long) dto.getCurrPage() - 1L) * dto.getPageSize();
      List<String> detailIds = this.baseMapper.pagingDetailIds(params, offset, dto.getPageSize());
      if (CollectionUtils.isEmpty(detailIds)) {
          return new PagingVO<>(Collections.emptyList(), (int) total, dto.getPageSize(), dto.getCurrPage());
      }

      List<ListDTO> loaded = this.baseMapper.selectByDetailIds(detailIds);
      Map<String, ListDTO> byDetailId = loaded.stream().collect(Collectors.toMap(
          ListDTO::getDetailId, Function.identity(), (first, ignored) -> first));
      List<ListDTO> records = detailIds.stream().map(byDetailId::get)
          .filter(Objects::nonNull).collect(Collectors.toList());
      handleDataPaging(records);
      return new PagingVO<>(records, (int) total, dto.getPageSize(), dto.getCurrPage());
  }
  ```

  当前页没有 ID 时，不执行详情 SQL，仍返回精确总数。详情回查因并发删除而缺少 ID 时，不插入空 DTO，总数保持 count 的真实结果。

- [ ] **步骤 4：补充边界测试并运行**

  增加 `page=3`、`pageSize=50` 时传入 `offset=100L` 的断言；验证 `total > 0` 但 ID 页为空时返回空列表和正确总数；验证详情查询缺失一个 ID 时结果不含空对象。

  ```bash
  mvn -pl erp-server/erp-server-tms -am -Dtest=SmallBagCostAllocationServiceImplTest -DfailIfNoTests=false test
  ```

  预期：所有 Service 编排测试通过。

- [ ] **步骤 5：提交 TMS 分页流程**

  ```bash
  git add erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImpl.java \
          erp-server/erp-server-tms/src/test/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImplTest.java
  git commit -m "perf: page small bag allocation details by id"
  ```

### 任务 3：定义批量汇率 RPC 契约

**文件：**
- 修改：`erp-model/erp-model-dmp/src/main/java/com/erp/model/dmp/dto/BiSettlementExchangeRateDTO.java`
- 修改：`erp-rpc/erp-rpc-dmp/src/main/java/com/erp/rpc/dmp/feign/DmpTaskFeign.java:72-90`

**接口：**

```java
List<BiSettlementExchangeRateDTO.BatchRateResultDTO> getRates(
    @RequestBody List<BiSettlementExchangeRateDTO.BatchRateParamDTO> params);
```

请求中的 `date` 与当前单条接口一致，即 `reportDate + "-01"`；`sourceCurrencyCode` 为现有源币种。结果返回相同的 `date`、`sourceCurrencyCode` 和可为空的 `BigDecimal exchangeRate`。

- [ ] **步骤 1：增加 DMP Model 传输 DTO**

  在 `BiSettlementExchangeRateDTO` 中增加：

  ```java
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BatchRateParamDTO {
      @NotBlank(message = "日期不能为空")
      private String date;
      @NotBlank(message = "币别不能为空")
      private String sourceCurrencyCode;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BatchRateResultDTO {
      private String date;
      private String sourceCurrencyCode;
      private BigDecimal exchangeRate;
  }
  ```

- [ ] **步骤 2：增加 Feign endpoint**

  在 `DmpTaskFeign` 的 `getRate` 相邻位置增加：

  ```java
  @PostMapping("feign/getRates")
  List<BiSettlementExchangeRateDTO.BatchRateResultDTO> getRates(
      @RequestBody List<BiSettlementExchangeRateDTO.BatchRateParamDTO> params);
  ```

  保留当前 `getRate`，避免影响其他调用方。

- [ ] **步骤 3：编译契约模块并提交**

  ```bash
  mvn -pl erp-rpc/erp-rpc-dmp -am -DskipTests compile

  git add erp-model/erp-model-dmp/src/main/java/com/erp/model/dmp/dto/BiSettlementExchangeRateDTO.java \
          erp-rpc/erp-rpc-dmp/src/main/java/com/erp/rpc/dmp/feign/DmpTaskFeign.java
  git commit -m "feat: add batch settlement exchange rate rpc"
  ```

  预期：`BUILD SUCCESS`。

### 任务 4：以现有选择规则实现 DMP 批量汇率查询

**文件：**
- 修改：`erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/service/BiSettlementExchangeRateService.java:19-43`
- 修改：`erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/service/impl/BiSettlementExchangeRateServiceImpl.java:71-120`
- 修改：`erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/controller/feign/DmpFeignController.java:149-171`
- 新增：`erp-server/erp-server-dmp/src/test/java/com/erp/server/dmp/service/impl/BiSettlementExchangeRateServiceImplTest.java`

**接口：**

```java
List<BiSettlementExchangeRateDTO.BatchRateResultDTO> findRates(
    List<BiSettlementExchangeRateDTO.BatchRateParamDTO> params);
```

针对每个去重后的 `(date, sourceCurrencyCode)` 返回一个结果。没有有效汇率时，结果对象存在但 `exchangeRate` 为 `null`。

- [ ] **步骤 1：先编写与单条查询一致性的失败测试**

  使用 JUnit 4/Mockito mock `BiSettlementExchangeRateMapper`。构造同币种的两条有效汇率，其区间均覆盖 `2026-07-01`，但 `updateTime` 不同；断言选择更新时间更晚的汇率。再验证日期不在任一区间时返回 `exchangeRate == null`。

  ```java
  List<BatchRateResultDTO> result = service.findRates(Arrays.asList(
      new BatchRateParamDTO("2026-07-01", "USD"),
      new BatchRateParamDTO("2026-07-01", "USD")));

  assertEquals(1, result.size());
  assertEquals(new BigDecimal("7.2000"), result.get(0).getExchangeRate());
  verify(mapper, times(1)).listByCurrencyCode("CNY", "USD");
  ```

- [ ] **步骤 2：运行测试确认失败**

  ```bash
  mvn -pl erp-server/erp-server-dmp -am -Dtest=BiSettlementExchangeRateServiceImplTest -DfailIfNoTests=false test
  ```

  预期：`findRates` 尚不存在，测试失败。

- [ ] **步骤 3：增加 Service 契约并复用已有汇率选择规则**

  在 `BiSettlementExchangeRateService` 增加 `findRates` 方法。将 `listRedisByCurrencyCodeType` 中的日期区间匹配与按 `updateTime DESC` 取最新的逻辑抽取为私有 helper，使单条查询与批量查询共同调用它。

  `findRates` 的实现要求：

  ```java
  public List<BatchRateResultDTO> findRates(List<BatchRateParamDTO> params) {
      if (CollectionUtils.isEmpty(params)) {
          return Collections.emptyList();
      }
      Map<String, List<BiSettlementExchangeRateEntity>> ratesByCurrency = new HashMap<>();
      // 按 date + sourceCurrencyCode 去重；每种币别最多读取一次 mapper；
      // 使用与单条方法完全相同的日期区间和最新 updateTime 选择逻辑。
  }
  ```

  `CNY -> CNY` 继续返回 `BigDecimal.ONE`。保持原有参数校验、有效汇率集合、区间边界和最新更新时间优先规则。

- [ ] **步骤 4：提供 Feign Controller endpoint**

  在 `DmpFeignController` 的 `/getRate` 后增加：

  ```java
  @PostMapping("/getRates")
  public List<BiSettlementExchangeRateDTO.BatchRateResultDTO> getRates(
      @RequestBody @Valid List<BiSettlementExchangeRateDTO.BatchRateParamDTO> params) {
      return biSettlementExchangeRateService.findRates(params);
  }
  ```

  不修改现有 `/getRate`、`/getMonthRate`。

- [ ] **步骤 5：运行 DMP 验证并提交**

  ```bash
  mvn -pl erp-server/erp-server-dmp -am -Dtest=BiSettlementExchangeRateServiceImplTest -DfailIfNoTests=false test
  mvn -pl erp-server/erp-server-tms -am -DskipTests compile

  git add erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/service/BiSettlementExchangeRateService.java \
          erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/service/impl/BiSettlementExchangeRateServiceImpl.java \
          erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/controller/feign/DmpFeignController.java \
          erp-server/erp-server-dmp/src/test/java/com/erp/server/dmp/service/impl/BiSettlementExchangeRateServiceImplTest.java
  git commit -m "feat: batch resolve settlement exchange rates"
  ```

### 任务 5：将 TMS 逐条汇率 Feign 调用替换为批量 RPC

**文件：**
- 修改：`erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImpl.java:218-299`
- 修改：`erp-server/erp-server-tms/src/test/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImplTest.java`

**接口：** 消费 `DmpTaskFeign.getRates(List<BatchRateParamDTO>)`，每个非空页最多调用一次 DMP 汇率 RPC，并保留当前 `unitCost`、`totalCost` 格式化行为。

- [ ] **步骤 1：编写批量汇率补全失败测试**

  准备两条相同 `(reportDate="2026-07", unitCurrency="USD")` 记录和一条 CNY 记录。mock SKU、渠道、供应商批量服务返回空列表，并 stub：

  ```java
  when(dmpTaskFeign.getRates(Arrays.asList(
      new BatchRateParamDTO("2026-07-01", "USD"))))
      .thenReturn(Arrays.asList(new BatchRateResultDTO("2026-07-01", "USD", new BigDecimal("7.2"))));
  ```

  断言 `getRates` 只调用一次，`getRate` 从不调用。再增加返回 `exchangeRate = null` 时抛出 `ServiceException("汇率为空，请维护汇率后再查询")` 的测试。

- [ ] **步骤 2：在格式化循环前构造一次性 rate map**

  从当前页记录中筛选非 CNY 且币种非空的 `(reportDate + "-01", unitCurrency)`，以 `date + "_" + sourceCurrencyCode` 去重。若集合非空，只调用一次 `dmpTaskFeign.getRates(rateParams)`；若集合为空，使用 `Collections.emptyMap()`，不调用 Feign。

  在原循环中通过 `reportDate + "-01_" + unitCurrency` 获取汇率；返回结果不存在或 `exchangeRate == null` 时，保留当前日志和 `ServiceException`。随后继续执行原有六位小数 `unitCost` 与 `totalCost` 格式化。

- [ ] **步骤 3：运行 TMS 测试并提交**

  ```bash
  mvn -pl erp-server/erp-server-tms -am -Dtest=SmallBagCostAllocationServiceImplTest -DfailIfNoTests=false test

  git add erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImpl.java \
          erp-server/erp-server-tms/src/test/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImplTest.java
  git commit -m "perf: batch small bag exchange rate lookups"
  ```

  预期：该流程不再调用 `DmpTaskFeign.getRate`。

### 任务 6：提供由用户执行的索引与执行计划材料

**文件：**
- 新增：`docs/sql/2026-07-22-small-bag-cost-allocation-paging-index-review.sql`
- 新增：`docs/superpowers/plans/2026-07-22-small-bag-cost-allocation-paging-verification.md`

**接口：** 输入用户提供的生产等价参数；输出只读盘点、候选 DDL、回滚 SQL 和验证记录清单。该任务不执行任何数据库操作。

- [ ] **步骤 1：写入索引盘点 SQL 和注释状态的候选 DDL**

  文件头必须说明：用户/DBA 先执行索引盘点，再选择性执行候选命令；计划、应用和 Agent 均不会执行 DDL。

  索引盘点 SQL：

  ```sql
  SELECT tbl.relname AS table_name, idx.relname AS index_name,
         pg_get_indexdef(i.indexrelid) AS index_definition,
         pg_get_expr(i.indpred, i.indrelid) AS predicate
  FROM pg_index i
  JOIN pg_class idx ON idx.oid = i.indexrelid
  JOIN pg_class tbl ON tbl.oid = i.indrelid
  JOIN pg_namespace ns ON ns.oid = tbl.relnamespace
  WHERE ns.nspname = 'public'
    AND tbl.relname IN ('small_bag_cost_allocation_main', 'small_bag_cost_allocation',
                        'small_bag_cost_allocation_detail', 'logistics_bill_cost',
                        'logistics_bill', 'logistics_bill_detail', 'dict_basic')
  ORDER BY tbl.relname, idx.relname;
  ```

  以下 DDL 与回滚命令全部保持注释：

  ```sql
  -- CREATE INDEX CONCURRENTLY idx_sba_active_main_id
  --     ON public.small_bag_cost_allocation (main_id) WHERE is_deleted = false;
  -- DROP INDEX CONCURRENTLY IF EXISTS public.idx_sba_active_main_id;

  -- CREATE INDEX CONCURRENTLY idx_sbad_active_main_id
  --     ON public.small_bag_cost_allocation_detail (main_id) WHERE is_deleted = false;
  -- DROP INDEX CONCURRENTLY IF EXISTS public.idx_sbad_active_main_id;

  -- CREATE INDEX CONCURRENTLY idx_sbam_active_report_status
  --     ON public.small_bag_cost_allocation_main (report_date, report_status, id)
  --     WHERE is_deleted = false;
  -- DROP INDEX CONCURRENTLY IF EXISTS public.idx_sbam_active_report_status;

  -- CREATE INDEX CONCURRENTLY idx_sbad_active_main_create_time
  --     ON public.small_bag_cost_allocation_detail (main_id, create_time)
  --     WHERE is_deleted = false;
  -- DROP INDEX CONCURRENTLY IF EXISTS public.idx_sbad_active_main_create_time;
  ```

- [ ] **步骤 2：写入参数化执行计划模板**

  为精确 count、ID 分页和详情 ID 回查各写一条 `EXPLAIN (VERBOSE, BUFFERS)` 模板，使用 `:report_date`、`:report_status`、`:permission_sql`、`:advanced_sql`、`:offset`、`:page_size`、`:detail_id_array` 等占位符。明确要求用户以同一请求的实际参数替换后执行。

  写明 `EXPLAIN (ANALYZE, BUFFERS)` 会实际执行查询，只能在 DBA 批准的低峰窗口由用户执行。

- [ ] **步骤 3：写入结果等价性检查清单**

  验证文档至少包含：

  ```text
  场景：无筛选；每一种已配置高级查询字段；组合高级查询；
        仅 t 权限；含 k.shop_id 的店铺权限；默认排序；每一种配置的自定义排序；
        首页、中间页、深页和越界页。
  对比项：totalCount；有序 detailId 列表；全部响应字段；
          count SQL 耗时；ID SQL 耗时；详情 SQL 耗时；汇率 RPC 次数；端到端耗时。
  通过条件：改造前后所有功能结果完全一致。
  ```

- [ ] **步骤 4：只提交文档**

  ```bash
  git add docs/sql/2026-07-22-small-bag-cost-allocation-paging-index-review.sql \
          docs/superpowers/plans/2026-07-22-small-bag-cost-allocation-paging-verification.md
  git commit -m "docs: add small bag paging validation scripts"
  ```

### 任务 7：最终集成验证与审查

**文件：** 验证任务 1 至任务 6 的所有改动。

- [ ] **步骤 1：运行聚焦测试**

  ```bash
  mvn -pl erp-server/erp-server-tms -am -Dtest=SmallBagCostAllocationServiceImplTest -DfailIfNoTests=false test
  mvn -pl erp-server/erp-server-dmp -am -Dtest=BiSettlementExchangeRateServiceImplTest -DfailIfNoTests=false test
  ```

  预期：两组测试均通过。

- [ ] **步骤 2：编译两个受影响服务**

  ```bash
  mvn -pl erp-server/erp-server-tms,erp-server/erp-server-dmp -am -DskipTests compile
  ```

  预期：`BUILD SUCCESS`。

- [ ] **步骤 3：静态审查 SQL 形态**

  确认：

  ```text
  pagingCount 与 pagingDetailIds 都包含 smallBagPagingFrom + smallBagPagingWhere；
  仅 pagingDetailIds 包含 smallBagPagingOrder + LIMIT/OFFSET；
  selectByDetailIds 只包含当前页 h.id 限制，不含高级查询、权限、排序或分页条件；
  pagingCount 使用 COUNT(DISTINCT h.id)；
  本接口 Mapper 流程不再存在自动 Page/IPage count；
  handleDataPaging 中不再调用 DmpTaskFeign.getRate。
  ```

- [ ] **步骤 4：仅由用户执行目标环境验证**

  将任务 6 的文件交给用户，由用户执行索引盘点、代表性结果对比和 DBA 批准的执行计划。不得代替用户运行数据库命令。将用户提供的结果记录到验证文档。

- [ ] **步骤 5：执行代码审查门禁并确认工作区状态**

  运行项目要求的 code review，修复经确认的问题后执行：

  ```bash
  git status --short
  ```

  预期：所有已验证修正均已提交，工作区无未提交文件。

## 计划自检

- 规格覆盖：任务 1 和任务 2 实现共享筛选条件下的精确 count 与两阶段分页；任务 3 至任务 5 实现批量 DMP 汇率 RPC；任务 6 提供用户执行的索引与计划材料；任务 7 覆盖编译、行为、SQL 形态与目标环境交接。
- 可执行性：每个任务都明确列出文件、接口、代码形态、验证命令和提交节点。
- 类型一致性：TMS 调用 `DmpTaskFeign.getRates(List<BatchRateParamDTO>)`；DMP Controller 调用 `BiSettlementExchangeRateService.findRates(...)`；请求/响应 DTO 定义在 `BiSettlementExchangeRateDTO`；TMS Mapper 方法由任务 1 定义并由任务 2 使用。
