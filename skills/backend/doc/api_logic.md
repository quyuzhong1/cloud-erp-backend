---
name: api_logic
description: 项目接口（Controller & Service & Mapper & DTO）开发规范，包括了 CRUD 的统一契约和防击穿/防越权准则。
---

# 项目接口开范
**(Controller & Service & Mapper & DTO)**

在进行开发或重构项目的单端接口时，必须严格遵循以下独立规范约束。任何省略操作都将被视为 BUG。主要覆盖四大核心操作体系及配套的复杂脱水拦截处理。

## 0. 核心公共规范 (Core Common)
- **署名标准化**: 所有新创建或大幅重构的 Java 类（Controller, Service, Mapper, DTO, Entity, Handler 等），其 Javadoc 中的 `@author` 必须统一锁定为 **`jack`**；已有类的小范围修改不强制改历史署名，避免产生无关 diff。
- **文档元数据**: DTO 中的**每一个**业务字段必须配有清晰的 Javadoc/Swagger 注释，禁止出现意义不明的“脱水”属性。
- **注释强制化**: **每一个**公开接口（Controller 方法）及核心业务方法（Service 接口及其实现类）必须配备标准的 Javadoc 注释，明确描述功能、入参含义及返回结果（严禁出现无意义的空注释）。
- **别名命名法**: SQL 中的主表别名要求使用“表名单词首字母组合”。例如：`qc_standard` 别名为 `qs`，`qc_standard_detail` 别名为 `qsd`。
- **业务 Key 枚举同步**: 凡涉及新业务模块或单据，必须同步在中心字典枚举 `SourceTypeEnum` 中新增对应的业务项（如：`QC_INFO("qcInfo", "质检单","qc_info")`），用于审批流、操作日志等跨模块业务标识。若已存在则无需重复添加。
- **审计模块枚举同步**: 凡涉及新业务模块，必须同步在 `ModuleTypeEnum` 中新增对应的业务项。
  - **命名规范**: 枚举名必须为全大写的表名（如：`QC_STANDARD`）。
  - **调用要求**: 在 Service 层必须配合 `operateLogService.addModuleOperateLog(...)` 使用，确保审计日志能通过模块代码 (`getCode()`) 准确归类。

## 1. 新增体系 (`/add`)
- **API 通道**: `POST /{module}/{tableName}/add`
- **DTO 契约**: 
  - 入参必须为加了 `@Validated` 的 `XxxDTO.AddDTO`。
  - 该类自身无独立属性，强制继承 `CommonDTO` 主体。
  - **严禁**在 `CommonDTO` 内暴露业务闭环中由系统和底层状态机去维系的字段（如 `approveStatus`、`code`、系统审计时间与人等）。
  - 所有 `CommonDTO` 上的业务属性必须要按照数据库元数据标注 JSR-303 安全防空限制（如 `@NotBlank` 或 `@NotNull`）。
- **控制层约定**: 单个入口方法需强制挂载 `@LogAction(value = LogActionEnum.INSERT)` 日志切面。
- **业务层纪律**:
  - 核心动作要加挂事务注解 `@Transactional`。
  - 凡是具备业务流水的实体，其入库前必须调用单据引擎 `docNoGenHelper.generateCode(BusinessNoTypeEnum.XXX)` 生成流水号。
    - **前缀维护规则**: 单号前缀（通常源自需求说明）必须在 `BusinessNoTypeEnum`（枚举类）与 `BusinessNoConstant`（常量类）中同步定义，严禁在业务代码中硬编码。
  - 数据入库 (`save`) 成功后必须调用业务审计日记记录 `operateLogService.addModuleOperateLog(...)` 进行落库。
- **持久层 (Mapper)**: 严禁在该基础保存场景下编写手写 XML/SQL，必须坚决依赖 MyBatis-Plus (MP) 的原生 `baseMapper.insert()` 或 `Service.save()` 进行写入。

## 2. 修改体系 (`/update`)
- **API 通道**: `POST /{module}/{tableName}/update`
- **DTO 契约**: 
  - 入参必须为带 `@Validated` 的 `XxxDTO.UpdateDTO`。
  - 强制继承 `CommonDTO`，内部只允许单独扩展带有 `@NotBlank` 约束的主键 `id` 字段。
- **控制层约定**: 必须标注 `@LogAction(value = LogActionEnum.UPDATE)`。
- **业务层纪律**:
  - **强制防击穿**：需要绑定防并发防击穿的注解 `@DistributeLocker(keyName = "xxxDTO.getId()")`。
  - **前置预检**：如果在工作流管控下的单据，必须前置限制当且仅当处于合法可修改的生命周期状态（如 `WAIT_SUBMIT` 或 `REJECT`）时，才能放行更新核心逻辑；否则应该直接阻断。
  - **差分比较日志**：在动作前后和保存后必须利用工具抓出实体，并调用能力抛出差分审计记录：`operateLogService.addModuleOperateLogByObj(oldObj, newObj, ...)`。
  - **差分更新能力**：（重点规则）若是级联带明细主从表，不推荐原版的先删后插或简单的 `updateById`，请使用封装好的差分更新：`commonService.updateDetail(...)` 去追平关联关系。
- **持久层 (Mapper)**: 直接调用 MP 扩展能力进行原生刷新。

## 3. 分组统计、列表与异步导出共生体系 (`/tabList` & `/paging` & `/export`)
这三个接口在企业级架构中属于高低频绑定的强关联组合。列表与导出负责数据的筛选，Tab 负责顶部的状态计数并联动筛选；这三者需共用底层的虚拟查询器。

### 3.1 `/tabList` (数量大盘)
- **入参与返参**: 取参统一取用最精简的 `PermissionsDTO` 防止内存逃逸越权；必须返回标准泛型 `ApiResult<List<XxxDTO.TabListDTO>>`。
- **Service 层实现准则 (重要)**:
  - 必须构造 `PagingParamDTO` 实体并显式透传 `permissionSql`。
  - 必须在返回结果的首位（Index 0）手动入列一条“全部”统计项，标识固定为 `"all"`。
- **代码实现模板**:
  ```java
  @Override
  public List<XxxDTO.TabListDTO> tabList(PermissionsDTO permissionsDTO) {
      XxxDTO.PagingParamDTO searchParam = new XxxDTO.PagingParamDTO();
      searchParam.setPermissionSql(permissionsDTO.getPermissionSql());
      
      List<XxxDTO.TabListDTO> list = this.baseMapper.tabList(searchParam);
      if (list == null) {
          list = new ArrayList<>();
      }
      // 强制首位注入“全部”项
      list.add(0, new XxxDTO.TabListDTO("all", "全部", 0));
      return list;
  }
  ```
- **核心逻辑与防脏扫**:
  1. **无审批流主表**：依靠 `baseMapper.tabList(searchParam)` 借助原生 `GROUP BY xxx_status` 抓取分组后合并返回。
  2. **有中心审批流**：必须先构建查询信赖区 `TaskKeyInfoDTO` 提取待审核 ID 集合，再下钻 XML 过滤。

### 3.2 `/paging` (分页) 与 `/export` (脱水导出)
- **注解开关**: 必须按需配置 `@DataPermission`（Alias 必须与 XML 别名对齐）与检索拦截器 `@WebAdvanceQuery`。
- **Service 层实现准则 (分页执行链)**:
  - **权限透传**: 必须将 `PagingDTO` 外层的 `permissionSql` 同步注入到 `params` 内部属性中。
  - **空记录熔断**: 若查询结果记录为空，应直接返回空的 `PagingVO`，避免后续无效计算。
  - **业务数据填充 (脱壳翻译)**: 必须强制调用私有填充方法（如 `fillList`）进行字典翻译、关联名称补充等脱壳操作。
- **代码实现模板**:
  ```java
  @Override
  public PagingVO<XxxDTO.ListDTO> paging(PagingDTO<XxxDTO.PagingParamDTO> pagingParamDTO) {
      // 1. 权限透传
      pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
      
      // 2. 构造 Page 对象并执行查询
      Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
      IPage<XxxDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
      
      // 3. 空值熔断
      if (CollUtil.isEmpty(pageData.getRecords())) {
          return new PagingVO(pageData);
      }
      
      // 4. 局部翻译与数据补充 (填充 List)
      fillList(pageData.getRecords());
      return new PagingVO(pageData);
  }
  ```
- **DTO 契约强同步**: 分页和导出两者必须完全复用相同的包装结构 `PagingDTO<XxxDTO.PagingParamDTO>`。
- **分页性能保障 (去 Total 化)**:
  - 默认不支持 `total` 统计（除非业务强要求），Service 返回时 `totalCount` 可固定为 `0`。
- **导出的强制异步设计**:
  - 导出动作必须通过 `downloadTaskFeign.saveDownloadTask(...)` 走异步脱水流程。
- **XML 隔离与防越权铁律**: 查询的底座 `mapper.xml` 中，不论标签是 `<select id="tabList">` 还是 `<select id="paging">`，不仅要硬置基本过滤 `is_deleted=FALSE` 拦截，还需要**绝对要在标签体内部内嵌防跨域级别隔离拼接钩子 `<#noparse>${params.permissionSql}</#noparse>`**！！

## 4. 高级检索与虚拟查询条件脱水器 (XxxQueryHandler)
`XxxQueryHandler` 不仅仅用于处理高级查询面板，它更是联结 `/tabList` 面板状态下压与底层 SQL 直接转译的引擎枢纽核心模块。
- **基类与生效原则**: 凡是有动态条件查询诉求的高级面板业务，必建挂载了 `@Component` 注解且继承 `AbstractQueryHandler` 的处理类。
- **双生契约对应 (重中之重)**:
  - 若 `Service.tabList()` 在界面上展出了一个**其实在表里不真实存在作为一列状态存在的组合**（例如订单通过各种退库退款子状态复合展示的：“待发货”、“部分发货”或是审批向中心索要来的“待我审核”），在前端点击该 Tab 会向下发类似 `{"field":"tab", "value":"xx"}`。
  - 此时必须在对应的 `XxxQueryHandler` 重写核心拦截路段 `handleSqlLogic`，以对虚拟名进行解析阻拦并打入真实 SQL 切片中：
    `if ("tab".equals(field))` :
    依靠 `super.buildDefaultDTO("表主别名.真实枚举列名", StatusEnum.XX.getCode())` 或拼接原生更加多维的 `AND` 闭环。
  - 当查询截获了如 `APPROVE_ING` （工作流待我审批），需要利用 Handler 拦截并在拦截器内部发网关外呼到 `workflowFeign` 拿到对应业务类型的被授权者 `ids`后，使用 `super.buildSplicingSQLDTO("XX.id", QueryConditionEnum.IN_LIST, ids, ...)` 执行一次强制平铺覆盖防止出现长连表性能灾难。

## 5. 组装式复合状态反转与单据批流传 (`/delete` & 审批特批链)
包含所有涉及多选（List）的数据变更体系集合，涵盖且不仅包括：删除、提交审核、审批通过 (`/approve`)、驳回、作废、撤销 等变更端点。
- **DTO 契约**: 统一接受并包裹诸如 `BaseIdsDTO.IdsDTO` 作为核心交互结构的主键集，特殊需求可增挂一层具有审批意见与流程号的附加壳。
- **微步熔断循环体 (重中之重)**: 
  - 针对带有 `List<String> ids` 提交来的任何状态更改动作，**Controller 严禁把集合抛给 Service 后直白使用 DB 扫向底层去做 Update `IN (...)` !**！
  - 必须：在 Service 剥离老数据库记录将其转为 Map `(id, entity)` 或者直接遍历 IDs。
  - 然后利用 `for(String id : ids )` 微步循环。对每次业务的独立变更状态机推进以及防重复过滤动作加上 `try-catch` 包裹。将当前这条数据的推进结果 and 产生的报错封装后，打入到结果统计集合 `List<BatchResultDTO>` 中。并凭借结果进行流传状态记录变更（日志等），最终使用并配合 `allMatch()` 等工具汇总判别最后给前端的回传通知结果响应块。

## 6. DDL 规范与枚举自动化 (DDL Standard)
- **公共字段占位**: 表首必须按序包含 `id`, `create_user_id`, `create_user_name`, `create_time`, `update_user_id`, `update_user_name`, `update_time`, `version`, `is_deleted`。
- **非空约束**: 所有业务字段必须 `NOT NULL`，字符类默认 `''`，数字类默认 `0`，时间类默认 `now()`。
- **枚举注释触发规则**: 状态/类型类字段 (`varchar(50)`) 的注释必须严格遵循格式：`{字段业务解释}：枚举code1=枚举中文1,枚举code2=枚举中文2`。这将触发后端 Enum 自动构建及前端字典自动翻译机制。

## 7. 附件处理规范 (Attachment Standardization)
为了确保全系统附件交互的一致性与可维护性，必须遵循以下标准化链路。

### 7.1 DTO 契约标准化
附件不再直接平铺在 `CommonDTO` 中，必须通过内部类 `AttachDTO` 的集合进行包装接收：

-**场景 A：附件有多个类型、维度**
```java
    /**
     * 参考图片/附件
     */
    private List<AttachDTO> attachmentList;

    @Data
    public static class AttachDTO {
        private String type; // 类型标识
        private List<String> attachmentNameList; // 附件名称集合
        private List<String> attachmentUrlList;  // 附件URL集合
    }
```
-**场景 B：附件只有一个类型**
```java
    /**
     * 参考图片/附件
     */
    private List<String> attachmentNameList; // 附件名称集合
    private List<String> attachmentUrlList;  // 附件URL集合
```
- **配对原则**: `attachmentNameList` 与 `attachmentUrlList` 的索引必须严格一一对应。
- **维度区分**: 若同单据下有多个维度的附件集合，必须通过 `type` 字段进行逻辑或物理隔离。

### 7.2 业务层 (ServiceImpl) 处理逻辑
- **各服务自治**: 若无特殊说明，一般指代当前微服务所属的 `attachment` 表。
- **新增 (Add)**: 遍历 `attachmentList`，将 `type`、`businessId` 与 URL/Name 关联后批量 `saveBatch` 写入当前服务的附件表。
- **差分更新 (Update)**: (核心要求) 严禁全量先删后插！必须实现**差分检测逻辑**：
  1. 通过 `businessId` 加载旧附件记录。
  2. 比对 URL 集合：`oldList - newList` 执行批量删除；`newList - oldList` 执行增量新增。
- **详情回显 (View)**: 根据 `businessId` 提取所有关联附件，利用 Stream 流按 `type` 分组 (`Collectors.groupingBy`) 后，重新组装成 `List<AttachDTO>` 给前端。
- **物理清理 (Delete)**: 在单据执行删除动作时，必须同步调用 `attachmentService` 物理清理该业务主键下的所有附件记录。

## 8. 审计日志埋点规范 (Operation Log Positioning Standard)
审计日志是系统追溯变更的核心依据。在 `ServiceImpl` 实现类中，必须在关键业务动作完成（及事务提交前）进行埋点记录。

### 8.1 核心埋点位置与方法
- **新增 (Add)**: 保存主表及明细后记录。
  - 方法: `operateLogService.addModuleOperateLog(content, moduleType, businessId, "新增操作")`
- **修改 (Update)**: 推荐使用对象差异对比日志。
  - 方法: `operateLogService.addModuleOperateLogByObj(oldObj, newObj, moduleType, businessId, content)`
- **删除 (Delete)**: 在物理/逻辑删除动作后记录。
  - 方法: 同新增，操作类型传 `"删除操作"`。
- **状态变更 (StatusChange)**: 开启/禁用、作废、提交、驳回等。
  - 必须记录变更前后的状态描述（如：`"开启操作"`、`"作废操作"`）。

### 8.2 埋点时机与顺序 (Sequence)
- **先主后从**: 在 `add` 或 `update` 方法中，必须**先记录主表日志**，再执行子表明细的更新逻辑及记录明细日志。
- **事务原子性**: 埋点代码应置于 `@Transactional` 方法内，确保业务失败时日志不孤立存在。

### 8.3 批量埋点与兼容性 (Compatibility)
- **Pair 构造**: 鉴于部分环境 `Pair.of` 不可用，批量埋点时必须使用显式构造函数。
  - 正确写法: `new Pair<>(key, value)`
- **批量增/删**: 推荐使用 `batchAddModuleOperateLog` 以减少日志条目冗余。
  - 示例: `operateLogService.batchAddModuleOperateLog("编辑：删除SKU【%s】", moduleType, pairList, "编辑操作")`
- **单行差异**: 遍历更新明细时，对每一行进行 `addModuleOperateLogByObj` 差异对比，并附带明细标识（如 SKU、质检项名）。

## 9. 各层流水线开发规范 (Layered Development Standard)
为了确保代码风格的高度统一，各层必须遵循以下技术栈与注解约束：

### 9.1 控制层 (Controller)
- **注解集合**: 必须标注 `@RestController` 和 `@RequestMapping("/{path}")`；类名统一以 `Controller` 结尾，继承 `BaseController`。
- **切面约束**: 
  - 动作跟踪：写操作方法必须挂载 `@LogAction` 并描述业务含义.
  - 权限拦截：所有涉及数据过滤的方法必须挂载 `@DataPermission`，并准确配置 `menuCode` 与 `tableAlias`。
  - 高级检索：分页方法必须挂载 `@WebAdvanceQuery` 并指派对应的 `QueryHandler`。

### 9.2 业务层 (Service)
- **接口定义**: 统一继承 `IService<Entity>`；方法名应具备清晰的语义化（如 `add`, `update`, `paging`）。
- **实现逻辑**:
  - **注解**: 实现类必须标注 `@Service`；核心写逻辑必须加挂 `@Transactional(rollbackFor = Exception.class)`。
  - **安全**: 针对主键或业务唯一键的更新，必须使用 `@DistributeLocker` 预防并发冲突。
  - **审计**: 必须注入并调用 `operateLogService` 进行变更留痕。
  - **转换**: 推荐使用 `BeanMapperUtils` 进行 DTO 与 Entity 的互转。

### 9.3 持久层 (Mapper)
- **Java 接口**: 必须标注 MyBatis 的 **`@Mapper`** 注解；统一继承 `BaseMapper<Entity>`。
- **XML 定义**: 命名空间必须准确指向接口类；必须实现 `paging` 和 `tabList` 的原生 SQL，并在其中内置权限钩子。
