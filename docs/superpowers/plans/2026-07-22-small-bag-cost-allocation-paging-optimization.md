# Small Bag Cost Allocation Paging Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reduce `SmallBagCostAllocationController#queryByPage` latency while preserving the exact, real-time `small_bag_cost_allocation_detail.id` result set, order, total, permissions, and advanced-query semantics.

**Architecture:** Replace the MyBatis-Plus generated wide-query count with an explicit `COUNT(DISTINCT h.id)`. Split list retrieval into a shared-filter ID query followed by a page-ID-bounded detail query; the service restores the first-stage ID order before enriching results. Replace TMS's per-period/currency Feign loop with one batch DMP RPC that preserves DMP's existing rate-selection rules.

**Tech Stack:** Java 8, Spring Boot 2.3, MyBatis/MyBatis-Plus, PostgreSQL, Spring Cloud OpenFeign, Lombok, JUnit 4, Mockito, Maven.

## Global Constraints

- Keep `POST /smallBagCostAllocation/paging`, its DTOs, `PagingVO` structure, response fields, and exception behavior compatible.
- `small_bag_cost_allocation_detail h.id` is the sole pagination and total-count grain.
- Total count is synchronous, real-time, exact, and computed as `COUNT(DISTINCT h.id)`; never estimate, defer, cache, or omit it.
- Both count and ID paging must use the same joins, advanced-query SQL (`params.sqlMap.default`), soft-delete predicates, data-permission SQL, and user-visible sorting semantics.
- The page-ID query appends `h.id` only as a deterministic final tie-breaker. The detail query must not independently filter, sort, paginate, or recalculate permissions.
- Preserve the confirmed one-to-one relationships `t ↔ g` and `k ↔ l`; do not claim an artificial multiplication from those relations.
- Do not change `@DataPermission`, `@WebAdvanceQuery`, `SmallBagCostAllocationQueryHandler`, the existing query-condition configuration, or the current dynamic sort contract in this work.
- TMS performs one batch rate RPC per non-CNY `(reportDate, unitCurrency)` set. A missing returned rate must still throw `ServiceException("汇率为空，请维护汇率后再查询")`.
- No application code, test, migration, or agent executes database DDL/DML. The user runs all supplied SQL and `EXPLAIN` commands.
- Use module-level Maven tests. There is no local PostgreSQL mapper-test harness; SQL equivalence and plans require user-run target-environment verification.

---

## File Structure

- Modify `erp-server/erp-server-tms/src/main/java/com/erp/server/tms/mapper/SmallBagCostAllocationMapper.java` — replace the auto-paginated wide mapper contract with explicit count, ID-page, and detail-by-ID methods.
- Modify `erp-server/erp-server-tms/src/main/resources/mapper/SmallBagCostAllocationMapper.xml` — define shared joins/conditions/order fragments and the three explicit statements.
- Modify `erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImpl.java` — orchestrate exact count → IDs → details, restore order, and invoke batch rate enrichment.
- Create `erp-server/erp-server-tms/src/test/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImplTest.java` — isolated Mockito tests for pagination orchestration, order recovery, and rate-missing behavior.
- Modify `erp-model/erp-model-dmp/src/main/java/com/erp/model/dmp/dto/BiSettlementExchangeRateDTO.java` — add request/result DTOs for one DMP batch-rate RPC.
- Modify `erp-rpc/erp-rpc-dmp/src/main/java/com/erp/rpc/dmp/feign/DmpTaskFeign.java` — publish the batch-rate Feign contract.
- Modify `erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/controller/feign/DmpFeignController.java` — expose the Feign endpoint.
- Modify `erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/service/BiSettlementExchangeRateService.java` and `.../impl/BiSettlementExchangeRateServiceImpl.java` — resolve a batch by reusing the existing valid-period/latest-update rate rule.
- Create `erp-server/erp-server-dmp/src/test/java/com/erp/server/dmp/service/impl/BiSettlementExchangeRateServiceImplTest.java` — verify batch result selection preserves existing single-rate behavior.
- Create `docs/sql/2026-07-22-small-bag-cost-allocation-paging-index-review.sql` — commented candidate index DDL, rollbacks, index inventory, and `EXPLAIN` templates for the user to execute.
- Create `docs/superpowers/plans/2026-07-22-small-bag-cost-allocation-paging-verification.md` — target-environment equivalence and performance-recording checklist.

---

### Task 1: Lock down explicit pagination mapper contracts

**Files:**
- Modify: `erp-server/erp-server-tms/src/main/java/com/erp/server/tms/mapper/SmallBagCostAllocationMapper.java:3-40`
- Modify: `erp-server/erp-server-tms/src/main/resources/mapper/SmallBagCostAllocationMapper.xml:38-106`

**Interfaces:**
- Consumes: `SmallBagCostAllocationDTO.PagingParamDTO`, current generated `params.sqlMap.default`, and `params.permissionSql`.
- Produces:
  ```java
  Long pagingCount(@Param("params") PagingParamDTO params);
  List<String> pagingDetailIds(@Param("params") PagingParamDTO params,
                                @Param("offset") long offset,
                                @Param("pageSize") long pageSize);
  List<ListDTO> selectByDetailIds(@Param("detailIds") List<String> detailIds);
  ```

- [ ] **Step 1: Remove the old mapper pagination declaration and add the explicit contracts**

  Replace the current `IPage<ListDTO> paging(Page query, ...)` declaration with:

  ```java
  Long pagingCount(@Param("params") PagingParamDTO params);

  List<String> pagingDetailIds(@Param("params") PagingParamDTO params,
                                @Param("offset") long offset,
                                @Param("pageSize") long pageSize);

  List<ListDTO> selectByDetailIds(@Param("detailIds") List<String> detailIds);
  ```

  Remove now-unused `IPage` and `Page` imports. Keep `tabList`, `listByReportPeriodStr`, and `listSmallBagCost` unchanged.

- [ ] **Step 2: Replace the old `<select id="paging">` with common SQL fragments**

  Add a `smallBagPagingFrom` fragment with the *same* current join graph, only changing the join direction to start at `h`:

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
  ```

  Add a `smallBagPagingWhere` fragment:

  ```xml
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

  Keep the current raw SQL injection points unchanged: they are generated by the existing validated advanced-query and data-permission aspects. Do not introduce any new user-controlled `${...}` value.

- [ ] **Step 3: Add the shared sort fragment with a stable detail tie-breaker**

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

  This preserves every supplied sort item and existing default order, adding only a deterministic `h.id ASC` final order.

- [ ] **Step 4: Add explicit count and first-stage ID statements**

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

  Do not pass a MyBatis-Plus `Page` argument to these methods; it prevents the pagination interceptor from generating a second automatic count.

- [ ] **Step 5: Add the detail query bounded only by the page IDs**

  Copy the existing display projection exactly into `<select id="selectByDetailIds">`, retaining its casts and aliases. Use the same display joins but no `smallBagPagingWhere`, no dynamic `ORDER BY`, and no `LIMIT/OFFSET`:

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

- [ ] **Step 6: Compile the TMS module**

  Run:

  ```bash
  mvn -pl erp-server/erp-server-tms -am -DskipTests compile
  ```

  Expected: `BUILD SUCCESS` and no unresolved mapper method or XML statement errors.

- [ ] **Step 7: Commit the mapper contract change**

  ```bash
  git add erp-server/erp-server-tms/src/main/java/com/erp/server/tms/mapper/SmallBagCostAllocationMapper.java \
          erp-server/erp-server-tms/src/main/resources/mapper/SmallBagCostAllocationMapper.xml
  git commit -m "perf: split small bag paging mapper queries"
  ```

### Task 2: Orchestrate exact count, ID paging, and order recovery in TMS

**Files:**
- Modify: `erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImpl.java:203-216,218-299`
- Create: `erp-server/erp-server-tms/src/test/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImplTest.java`

**Interfaces:**
- Consumes: Task 1's mapper methods.
- Produces: `PagingVO<ListDTO>` with exact `totalCount`, requested page metadata, and records in `pagingDetailIds` order.

- [ ] **Step 1: Write the failing service orchestration tests**

  Use JUnit 4/Mockito and `ReflectionTestUtils.setField`, matching `TmsAsyncTaskRecordServiceImplPageBatchBusinessIdsTest`. Create a spy and change `handleDataPaging` from `private` to `protected` so tests can suppress unrelated Feign enrichment:

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

  In `setUp`, inject `SmallBagCostAllocationMapper` into the inherited `baseMapper` field and stub `doNothing().when(service).handleDataPaging(anyList())`.

- [ ] **Step 2: Run the tests to verify they fail**

  ```bash
  mvn -pl erp-server/erp-server-tms -am -Dtest=SmallBagCostAllocationServiceImplTest -DfailIfNoTests=false test
  ```

  Expected: failure because the service still calls the removed `paging` method or does not expose the required orchestration.

- [ ] **Step 3: Implement the two-stage service flow**

  Replace `paging` with:

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

  Keep `handleDataPaging` behavior unchanged in this task except for its visibility (`protected`) needed by the unit test. Do not invoke detail SQL if no page IDs are returned; retain the exact count in that out-of-range-page response.

- [ ] **Step 4: Add boundary tests and run them**

  Add cases for offset calculation (`page=3`, `pageSize=50` must pass `100L`), `total > 0` with an empty ID page, and a detail query that omits a deleted ID. The omitted record is not replaced with a placeholder; count remains unchanged.

  Run:

  ```bash
  mvn -pl erp-server/erp-server-tms -am -Dtest=SmallBagCostAllocationServiceImplTest -DfailIfNoTests=false test
  ```

  Expected: all service orchestration tests pass.

- [ ] **Step 5: Commit the TMS pagination flow**

  ```bash
  git add erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImpl.java \
          erp-server/erp-server-tms/src/test/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImplTest.java
  git commit -m "perf: page small bag allocation details by id"
  ```

### Task 3: Define the batch exchange-rate RPC contract

**Files:**
- Modify: `erp-model/erp-model-dmp/src/main/java/com/erp/model/dmp/dto/BiSettlementExchangeRateDTO.java`
- Modify: `erp-rpc/erp-rpc-dmp/src/main/java/com/erp/rpc/dmp/feign/DmpTaskFeign.java:72-90`

**Interfaces:**
- Produces:
  ```java
  List<BiSettlementExchangeRateDTO.BatchRateResultDTO> getRates(
      @RequestBody List<BiSettlementExchangeRateDTO.BatchRateParamDTO> params);
  ```
- Request pair: `date` is the same `reportDate + "-01"` passed to the existing single-rate endpoint; `sourceCurrencyCode` is the existing source currency.
- Result pair: echoes `date` and `sourceCurrencyCode`, and carries nullable `BigDecimal exchangeRate`.

- [ ] **Step 1: Add transport DTOs in the DMP model**

  Add nested classes:

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

- [ ] **Step 2: Add the Feign endpoint**

  In `DmpTaskFeign`, next to `getRate`, add:

  ```java
  @PostMapping("feign/getRates")
  List<BiSettlementExchangeRateDTO.BatchRateResultDTO> getRates(
      @RequestBody List<BiSettlementExchangeRateDTO.BatchRateParamDTO> params);
  ```

  Import `BiSettlementExchangeRateDTO` and `java.util.List` if absent. Keep `getRate` for existing callers.

- [ ] **Step 3: Compile the contract modules**

  ```bash
  mvn -pl erp-rpc/erp-rpc-dmp -am -DskipTests compile
  ```

  Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit the RPC contract**

  ```bash
  git add erp-model/erp-model-dmp/src/main/java/com/erp/model/dmp/dto/BiSettlementExchangeRateDTO.java \
          erp-rpc/erp-rpc-dmp/src/main/java/com/erp/rpc/dmp/feign/DmpTaskFeign.java
  git commit -m "feat: add batch settlement exchange rate rpc"
  ```

### Task 4: Implement DMP batch-rate resolution with the current selection rule

**Files:**
- Modify: `erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/service/BiSettlementExchangeRateService.java:19-43`
- Modify: `erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/service/impl/BiSettlementExchangeRateServiceImpl.java:71-120`
- Modify: `erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/controller/feign/DmpFeignController.java:149-171`
- Create: `erp-server/erp-server-dmp/src/test/java/com/erp/server/dmp/service/impl/BiSettlementExchangeRateServiceImplTest.java`

**Interfaces:**
- Consumes: Task 3's `BatchRateParamDTO`.
- Produces: one `BatchRateResultDTO` per distinct input `(date, sourceCurrencyCode)`; no result-rate means the existing service has no active approved rate for that pair.

- [ ] **Step 1: Write failing unit tests for existing-rate equivalence**

  Use JUnit 4/Mockito to mock `BiSettlementExchangeRateMapper`. Build two `BiSettlementExchangeRateEntity` entries for the same currency whose date ranges both cover `2026-07-01`, with distinct `updateTime`; assert the newer rate is selected. Also test that a request outside all ranges yields a result DTO with `exchangeRate == null`.

  Core assertion:

  ```java
  List<BatchRateResultDTO> result = service.findRates(Arrays.asList(
      new BatchRateParamDTO("2026-07-01", "USD"),
      new BatchRateParamDTO("2026-07-01", "USD")));

  assertEquals(1, result.size());
  assertEquals(new BigDecimal("7.2000"), result.get(0).getExchangeRate());
  verify(mapper, times(1)).listByCurrencyCode("CNY", "USD");
  ```

- [ ] **Step 2: Run tests to verify failure**

  ```bash
  mvn -pl erp-server/erp-server-dmp -am -Dtest=BiSettlementExchangeRateServiceImplTest -DfailIfNoTests=false test
  ```

  Expected: failure because `findRates` does not exist.

- [ ] **Step 3: Add the service contract and implementation**

  Add to `BiSettlementExchangeRateService`:

  ```java
  List<BiSettlementExchangeRateDTO.BatchRateResultDTO> findRates(
      List<BiSettlementExchangeRateDTO.BatchRateParamDTO> params);
  ```

  Refactor the current date-range selection in `listRedisByCurrencyCodeType` into a private helper accepting the already-loaded `List<BiSettlementExchangeRateEntity>`. Both existing single methods and new `findRates` must call that helper, so they share the exact inclusive-date and descending-`updateTime` behavior.

  Implement `findRates` as follows:

  ```java
  public List<BatchRateResultDTO> findRates(List<BatchRateParamDTO> params) {
      if (CollectionUtils.isEmpty(params)) {
          return Collections.emptyList();
      }
      Map<String, List<BiSettlementExchangeRateEntity>> ratesByCurrency = new HashMap<>();
      return params.stream()
          .filter(Objects::nonNull)
          .collect(Collectors.collectingAndThen(
              Collectors.toMap(item -> item.getDate() + "|" + item.getSourceCurrencyCode(),
                  Function.identity(), (first, ignored) -> first, LinkedHashMap::new),
              map -> map.values().stream().map(item -> {
                  List<BiSettlementExchangeRateEntity> rates = ratesByCurrency.computeIfAbsent(
                      item.getSourceCurrencyCode(), currency -> baseMapper.listByCurrencyCode("CNY", currency));
                  return new BatchRateResultDTO(item.getDate(), item.getSourceCurrencyCode(),
                      resolveRate(item.getDate(), "CNY", item.getSourceCurrencyCode(), rates, false));
              }).collect(Collectors.toList())));
  }
  ```

  The helper must return `BigDecimal.ONE` for CNY-to-CNY and otherwise retain the current validation, active-rate filtering result, inclusive period match, and latest `updateTime` precedence.

- [ ] **Step 4: Expose the controller endpoint**

  In `DmpFeignController`:

  ```java
  @PostMapping("/getRates")
  public List<BiSettlementExchangeRateDTO.BatchRateResultDTO> getRates(
      @RequestBody @Valid List<BiSettlementExchangeRateDTO.BatchRateParamDTO> params) {
      return biSettlementExchangeRateService.findRates(params);
  }
  ```

  Place it immediately after `getRate`; do not alter `/getRate` and `/getMonthRate`.

- [ ] **Step 5: Run DMP tests and compile TMS against the new contract**

  ```bash
  mvn -pl erp-server/erp-server-dmp -am -Dtest=BiSettlementExchangeRateServiceImplTest -DfailIfNoTests=false test
  mvn -pl erp-server/erp-server-tms -am -DskipTests compile
  ```

  Expected: both commands succeed.

- [ ] **Step 6: Commit the DMP provider**

  ```bash
  git add erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/service/BiSettlementExchangeRateService.java \
          erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/service/impl/BiSettlementExchangeRateServiceImpl.java \
          erp-server/erp-server-dmp/src/main/java/com/erp/server/dmp/controller/feign/DmpFeignController.java \
          erp-server/erp-server-dmp/src/test/java/com/erp/server/dmp/service/impl/BiSettlementExchangeRateServiceImplTest.java
  git commit -m "feat: batch resolve settlement exchange rates"
  ```

### Task 5: Replace per-rate TMS Feign calls with the batch RPC

**Files:**
- Modify: `erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImpl.java:218-299`
- Modify: `erp-server/erp-server-tms/src/test/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImplTest.java`

**Interfaces:**
- Consumes: `DmpTaskFeign.getRates(List<BatchRateParamDTO>)`.
- Produces: the same unit-cost and total-cost formatting as before, with a maximum of one DMP rate RPC per nonempty page.

- [ ] **Step 1: Add the failing batch-enrichment test**

  Make `handleDataPaging` callable through the service spy. Supply records with duplicate `("2026-07", "USD")` and one CNY row. Stub supporting SKU/channel/supplier batches with empty lists and stub:

  ```java
  when(dmpTaskFeign.getRates(Arrays.asList(
      new BatchRateParamDTO("2026-07-01", "USD"))))
      .thenReturn(Arrays.asList(new BatchRateResultDTO("2026-07-01", "USD", new BigDecimal("7.2"))));
  ```

  Verify `getRates` is called once and `getRate` is never called. Add a missing-rate test that returns `exchangeRate = null` and expects `ServiceException` with `汇率为空，请维护汇率后再查询`.

- [ ] **Step 2: Build the one-call rate map before formatting rows**

  Replace the current `rateMap` lazy per-row block with:

  ```java
  List<BatchRateParamDTO> rateParams = records.stream()
      .filter(item -> StringUtils.isNotBlank(item.getUnitCurrency()))
      .filter(item -> !"CNY".equals(item.getUnitCurrency()))
      .map(item -> new BatchRateParamDTO(item.getReportDate() + "-01", item.getUnitCurrency()))
      .collect(Collectors.collectingAndThen(
          Collectors.toMap(item -> item.getDate() + "_" + item.getSourceCurrencyCode(),
              Function.identity(), (first, ignored) -> first, LinkedHashMap::new),
          map -> new ArrayList<>(map.values())));
  Map<String, BigDecimal> rateMap = dmpTaskFeign.getRates(rateParams).stream()
      .collect(Collectors.toMap(item -> item.getDate() + "_" + item.getSourceCurrencyCode(),
          BiSettlementExchangeRateDTO.BatchRateResultDTO::getExchangeRate));
  ```

  If `rateParams` is empty, use `Collections.emptyMap()` and do not call Feign. In the existing row loop, key with `reportDate + "-01_" + unitCurrency`, retrieve the batch result, and retain the existing log message and `ServiceException` if the rate is absent or null.

- [ ] **Step 3: Run TMS tests**

  ```bash
  mvn -pl erp-server/erp-server-tms -am -Dtest=SmallBagCostAllocationServiceImplTest -DfailIfNoTests=false test
  ```

  Expected: the test proves exactly one batch RPC is made and no `getRate` invocation remains in this flow.

- [ ] **Step 4: Commit the TMS batch-rate consumer**

  ```bash
  git add erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImpl.java \
          erp-server/erp-server-tms/src/test/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImplTest.java
  git commit -m "perf: batch small bag exchange rate lookups"
  ```

### Task 6: Supply user-executed index and query-plan validation material

**Files:**
- Create: `docs/sql/2026-07-22-small-bag-cost-allocation-paging-index-review.sql`
- Create: `docs/superpowers/plans/2026-07-22-small-bag-cost-allocation-paging-verification.md`

**Interfaces:**
- Consumes: production-equivalent bind values supplied by the user.
- Produces: no database action; only scripts and a result-recording checklist for the user/DBA.

- [ ] **Step 1: Create the index inventory and candidate DDL file**

  Include this read-only inventory query:

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

  Add the following DDL and matching rollback commands as comments only:

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

  State in a file header that the user/DBA must first inspect existing indexes and then selectively run commands; this plan does not execute them.

- [ ] **Step 2: Add parameterized plan templates**

  Include three `EXPLAIN (VERBOSE, BUFFERS)` templates for the exact count, ID page, and detail-ID query. Use named placeholders such as `:report_date`, `:report_status`, `:permission_sql`, `:advanced_sql`, `:offset`, `:page_size`, and `:detail_id_array`; require the user to replace them with the same approved request values before execution.

  Include a warning that `EXPLAIN (ANALYZE, BUFFERS)` is only for a DBA-approved low-traffic window because it runs the query.

- [ ] **Step 3: Create the equivalence checklist**

  Record the before/after comparisons the user must execute:

  ```text
  Cases: no filter; each supported advanced field; combined advanced fields;
         t-only permission; k.shop_id permission; default sort; each configured custom sort;
         first, middle, deep, and out-of-range pages.
  For each case compare: totalCount; ordered detailId list; all response fields;
         count SQL time; ID SQL time; detail SQL time; rate-RPC count; end-to-end time.
  Pass condition: every functional value matches before/after exactly.
  ```

- [ ] **Step 4: Commit documentation only**

  ```bash
  git add docs/sql/2026-07-22-small-bag-cost-allocation-paging-index-review.sql \
          docs/superpowers/plans/2026-07-22-small-bag-cost-allocation-paging-verification.md
  git commit -m "docs: add small bag paging validation scripts"
  ```

### Task 7: Final integration verification and review

**Files:**
- Verify: all files from Tasks 1–6.

- [ ] **Step 1: Run focused tests**

  ```bash
  mvn -pl erp-server/erp-server-tms -am -Dtest=SmallBagCostAllocationServiceImplTest -DfailIfNoTests=false test
  mvn -pl erp-server/erp-server-dmp -am -Dtest=BiSettlementExchangeRateServiceImplTest -DfailIfNoTests=false test
  ```

  Expected: both focused suites pass.

- [ ] **Step 2: Compile both affected services**

  ```bash
  mvn -pl erp-server/erp-server-tms,erp-server/erp-server-dmp -am -DskipTests compile
  ```

  Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Perform static SQL review**

  Check that:

  ```text
  pagingCount and pagingDetailIds both include smallBagPagingFrom + smallBagPagingWhere;
  pagingDetailIds alone includes smallBagPagingOrder + LIMIT/OFFSET;
  selectByDetailIds has only h.id page restriction and no advanced/permission/sort/paging clauses;
  pagingCount uses COUNT(DISTINCT h.id);
  no automatic Page/IPage remains in this endpoint's mapper flow;
  no call to DmpTaskFeign.getRate remains in handleDataPaging.
  ```

- [ ] **Step 4: Run target-environment verification only through the user**

  Give the user the Task 6 files. Ask them to execute index inventory, representative result comparisons, and DBA-approved plans. Do not run database commands. Record any supplied results in the verification document.

- [ ] **Step 5: Run the repository review gate and commit any final correction**

  Run the project-required code review and then:

  ```bash
  git status --short
  ```

  Expected: no uncommitted files remain after any verified corrections are committed.

## Plan Self-Review

- Spec coverage: Tasks 1–2 implement the shared exact-count/two-stage query semantics; Tasks 3–5 implement the batch DMP rate RPC; Task 6 supplies user-executed index and plan material; Task 7 verifies compile, behavior, SQL shape, and target-environment handoff.
- No placeholder scan: every implementation step specifies its files, interfaces, code shape, command, and expected result.
- Type consistency: TMS consumes `DmpTaskFeign.getRates(List<BatchRateParamDTO>)`; DMP controller delegates to `BiSettlementExchangeRateService.findRates(...)`; the DTO pair is defined in `BiSettlementExchangeRateDTO`; TMS Mapper method names and argument types are defined in Task 1 and consumed in Task 2.
