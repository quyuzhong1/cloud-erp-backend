# SmallBagCostAllocation 分页性能优化设计

## 背景与目标

`POST /smallBagCostAllocation/paging`（`SmallBagCostAllocationController#queryByPage`）在大数据量下响应时间过长。慢 SQL 报告显示，当前数据 SQL 与自动生成的 count SQL 平均耗时约为 23–28 秒。

涉及的大表包括：

- `small_bag_cost_allocation`：约 294 万行；
- `small_bag_cost_allocation_detail`：约 227 万行；
- `small_bag_cost_allocation_main`：约 23 万行；
- `logistics_bill`、`logistics_bill_cost`、`logistics_bill_detail`：约 925–1085 万行。

本设计的目标是在不改变接口契约、筛选权限、排序规则、字段内容和统计语义的前提下，缩短分页、精确总数统计及当前页数据补全耗时。

## 已确认约束

1. 分页和总数的唯一业务粒度是 `small_bag_cost_allocation_detail`，以 `h.id` 识别一条结果。
2. 总数必须实时且精确；不得取消、延迟、估算或改为异步统计。
3. `small_bag_cost_allocation_main` 与 `small_bag_cost_allocation` 为一对一；`logistics_bill` 与 `logistics_bill_detail` 为一对一。
4. 可调整 SQL 形态并新建/调整数据库索引；所有数据库操作、执行计划和回滚操作均由用户执行。
5. 两阶段查询必须保持高级查询、数据权限、排序、返回明细集合和字段结果与改造前一致。
6. 当前页汇率补全允许改为批量 RPC；不引入跨请求缓存，也不得改变汇率口径。

## 现状

当前 Mapper 的单条宽查询从分摊主表 `t` 依次关联分摊表 `g`、分摊明细 `h`、物流费用单 `j`、物流单 `k`、物流单明细 `l` 与字典表 `n`，在关联结果上直接执行动态排序和分页。

MyBatis-Plus 自动从该宽查询生成 count SQL，且分页拦截器配置为不优化 join。当前 count 没有显式按 `h.id` 去重，且与展示字段查询共用宽关联结构。分页数据返回后，服务还会批量补全 SKU、渠道和供应商信息，并按每个“核算期间 + 币种”组合逐条调用汇率 Feign 接口。

`@WebAdvanceQuery` 在 Controller 调用前生成 `${params.sqlMap.default}`；`@DataPermission` 注入数据权限 SQL。二者是现有筛选语义的一部分，不能在优化中绕过或重写为不同语义。

## 方案：两阶段明细 ID 分页、显式精确计数与批量补全

### 公共候选明细集

在 `SmallBagCostAllocationMapper.xml` 中抽取可复用的候选明细集 SQL 片段。该片段以 `h.id` 为候选明细标识，保留当前所有用于筛选的关联与谓词：

- 各表的 `is_deleted = false`；
- 高级查询生成的 `${params.sqlMap.default}`；
- `@DataPermission` 注入的 `permissionSql`；
- 当前查询所依赖的 `t/g/h/j/k/l/n` 关联字段。

候选集是 count 与 ID 阶段共享的唯一筛选来源。不得在任一分支复制、删减或语义改写高级查询和数据权限条件。

### 精确总数

新增显式 count Mapper SQL：对公共候选集执行 `COUNT(DISTINCT h.id)`。

该 count 在每次分页请求中同步执行，精确反映当前高级查询和权限范围内的分摊明细条数。服务层关闭 MyBatis-Plus 自动 count，避免再次生成宽查询 count；最终 `Page` 总数使用该显式 count 的结果。

### 第一阶段：有序明细 ID 分页

第一阶段仅从公共候选集选择 `h.id`：

1. 按当前 `sortList` 使用既有排序字段和方向；
2. 在现有排序项后追加 `h.id` 作为唯一、稳定的最终排序键；
3. 应用 `LIMIT/OFFSET` 返回当前页的有序明细 ID 列表。

这一阶段是筛选、权限和排序的唯一裁决点。没有排序参数时，沿用现有默认排序语义，并将 `h.id` 加入最终稳定排序。

### 第二阶段：当前页详情回查

第二阶段仅接收第一阶段的有序 `h.id` 集合。通过 PostgreSQL `unnest(array) WITH ORDINALITY` 或等价的带序号 `VALUES` 集合，把 ID 与页内位置关联后回查完整展示字段。

第二阶段不重新执行高级查询、权限查询或独立排序；只关联展示字段、按页内序号输出，并保持请求页的 ID 顺序。结果集因此不会被详情阶段的关联改变。

### 接口兼容

保持以下内容不变：

- 路径：`POST /smallBagCostAllocation/paging`；
- Controller 注解：`@WebAdvanceQuery`、`@DataPermission`；
- 请求 DTO、返回 `ApiResult<Page<SmallBagCostAllocationDTO>>`、响应字段与错误语义；
- 现有排序入参及其字段/方向约定；
- 现有高级查询字段和数据权限行为。

## 当前页远程数据补全

保留 SKU、渠道、供应商的每页批量查询。

汇率补全调整为：收集当前页去重后的“报告日期 + 单位币种”组合，一次调用批量汇率 RPC，然后按组合映射回 DTO。批量结果缺失时须保持当前单条调用的异常或空值语义；不得引入跨请求缓存，也不得变更汇率取值口径。

对应 RPC 契约、Feign 实现与服务调用应一同调整，避免服务层保留循环单条远程调用。

## 数据库索引交付与操作边界

代码变更中提供但不执行以下内容：

1. 候选 PostgreSQL 索引 DDL；
2. 每个新增索引的 `DROP INDEX` 回滚脚本；
3. 覆盖代表性筛选参数的 `EXPLAIN (ANALYZE, BUFFERS)` 验证命令；
4. 执行计划结果记录模板。

索引候选方向应由实际计划确认，优先覆盖：

- `h.main_id` 与软删过滤；
- `t` 的核算期间、核算状态、创建时间、创建用户等高频筛选和数据权限字段；
- `g.main_id`、`j.id`、`j.logistics_bill_id`、`k.id`、`l.main_id` 等关联路径；
- 店铺筛选链路；
- 经 `EXPLAIN` 证实能改善排序或过滤的联合/部分索引。

用户负责在测试或生产变更窗口执行 DDL、回滚及 `EXPLAIN`。实现方不得直接连接、修改或操作数据库。

## 验证策略

### 语义等价性

对同一套生产代表参数，在改造前后比较：

1. 无筛选、每个高级查询字段、组合高级筛选；
2. 不同数据权限范围；
3. 默认排序与每种允许的自定义排序；
4. 首页、中间页与深页；
5. 总数、当前页 `h.id` 集合、ID 顺序及每个返回字段。

任何差异均视为实现缺陷，必须在上线前修正。

### 自动化测试

- Mapper/集成测试覆盖 `COUNT(DISTINCT h.id)` 的明细粒度、候选集筛选、稳定排序、第二阶段顺序恢复；
- Service 测试覆盖显式 count 写入分页结果、批量汇率请求去重、结果映射和缺失数据语义；
- 回归测试覆盖高级查询、权限 SQL 与排序参数未因两阶段拆分而发生漂移。

### 性能验收

用户在代表性数据规模和参数下分别记录改造前后：

- 精确 count SQL 耗时与执行计划；
- ID 阶段 SQL 耗时与执行计划；
- 详情阶段 SQL 耗时与执行计划；
- 远程补全耗时与调用次数；
- 接口端到端耗时。

验收以功能结果完全一致为前提，再比较上述分段耗时是否显著下降。

## 非目标

本轮不引入物化视图、预计算读模型、异步统计、近似统计、跨请求缓存或接口字段/协议变更。