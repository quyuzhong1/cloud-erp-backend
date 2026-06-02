# 下拉框 Option ID 完整数据

本文件用于 Cursor / AI 在生成 **高级查询**（`cfg_query_condition.query_option_id`）及 **通知配置字段** 时匹配下拉数据源。

## 使用规则

1. 本文件不是开发规范，不应作为 Cursor Rule 全局生效。
2. 仅在生成高级查询配置 SQL、通知字段映射时读取。
3. 匹配优先级：URL 中 `key` / `type` / `typeName` 精确匹配 → 名称与模块语义模糊匹配。
4. 同 URL 多条记录时，以 **ID** 区分（名称不同、绑定字段可能不同）。
5. 未经用户明确要求，不允许自动修改本文件。
6. 若新增了 `cfg_query_option` 记录，请同步补充到本文件。

**记录数**：338（来源：cfg_query_option 导出，is_deleted=false）

## 字段说明

| 列 | 含义 |
| --- | --- |
| ID | cfg_query_option 主键，写入 query_option_id |
| 名称 | api_name，业务可读名 |
| 模块 | 由 api_url 前缀推断 |
| API URL | 下拉数据源接口路径 |
| 方法 | request_method：get / postJson |
| 显示字段 | select_label |
| 值字段 | select_value |
| 禁用字段 | select_disabled |
| 搜索键 | search_key_field |
| Props | 前端扩展（children、searchKeyField 等） |
| 匹配关键词 | URL 中 key/type 或完整路径 |

## 全量索引

| ID | 名称 | 模块 | API URL | 方法 | 显示字段 | 值字段 | 禁用字段 | 搜索键 | Props | 匹配关键词 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1832018115441840129 | 中台推送状态 | dmp | /dmp/common/enumDropDown?type=DmpOutputTaskRecordStatus | get | value | code | disabled |  |  | ?type=DmpOutputTaskRecordStatus |
| 1832022881509167106 | 中台系统 | dmp | /dmp/dmpBasicSystem/listDmpBasicSystem | get | name | code | disabled |  |  | /dmp/dmpBasicSystem/listDmpBasicSystem |
| 2016087025393451010 | 中台系统谷云 | dmp | /dmp/dmpBasicSystem/listDmpBasicSystem | get | name | name | disabled |  |  | /dmp/dmpBasicSystem/listDmpBasicSystem |
| 1838065382399369218 | 查询所有输入配置 | dmp | /dmp/dmpCfgInput/allDmpCfgInput | get | name | id | disabled |  |  | /dmp/dmpCfgInput/allDmpCfgInput |
| 1803629715676729346 | 获取任务同步状态 | dmp | /dmp/drop/down/dict/list?key=syncStatus | get | value | code | disabled |  |  | ?key=syncStatus |
| 1872192695474393090 | 获取单据同步状态 | dmp | /dmp/common/enumDropDown?type=DmpOutputTaskRecordStatus | get | value | code | disabled |  |  | ?type=DmpOutputTaskRecordStatus |
| 1988148930103894017 | 获取差异单据类型 | dmp | /dmp/drop/down/dict/list?key=dictBillType | get | value | code | disabled |  |  | ?key=dictBillType |
| 1988149154746617858 | 获取差异标签 | dmp | /dmp/drop/down/dict/list?key=dictDiffTag | get | value | code | disabled |  |  | ?key=dictDiffTag |
| 1803630742350073858 | 获取拉取任务单据类型 | dmp | /dmp/drop/down/dict/list?key=pullSourceType | get | value | code | disabled |  |  | ?key=pullSourceType |
| 1803629485564628994 | 获取推送任务单据类型 | dmp | /dmp/drop/down/dict/list?key=pushSourceType | get | value | code | disabled |  |  | ?key=pushSourceType |
| 1829445922634346498 | 下载中心单据类型 | file | /file/common/enumDropDown?type=FileTaskEvent | get | value | code | disabled |  |  | ?type=FileTaskEvent |
| 1980902471560339983 | 单据类型 | fms | /fms/common/enumDropDown?type=AssetProfitLossTypeEnum | get | name | code | disabled |  |  | ?type=AssetProfitLossTypeEnum |
| 1980902471560339451 | 卡片来源 | fms | /fms/dict/list?key=cardSource | get | name | value | disabled |  |  | ?key=cardSource |
| 1980902471560339455 | 处置情况 | fms | /fms/dict/list?key=disposalStatus | get | name | value | disabled |  |  | ?key=disposalStatus |
| 1964970131239817308 | 审核状态 | fms | /fms/drop/down/approveStatus/list | get | value | code | disabled |  |  | /fms/drop/down/approveStatus/list |
| 1964970131239817400 | 审核状态 | fms | /fms/drop/down/approveStatus/list | get | value | code | disabled |  |  | /fms/drop/down/approveStatus/list |
| 1980902471560339454 | 审核状态 | fms | /fms/drop/down/approveStatus/list | get | value | code | disabled |  |  | /fms/drop/down/approveStatus/list |
| 1980902471560339981 | 盘点方案 | fms | /fms/assetStocktakingPlan/dropDownList | get | planName | id | disabled | keyword | {"searchKeyField": "keyword"} | /fms/assetStocktakingPlan/dropDownList |
| 1980902471560339453 | 资产位置 | fms | /fms/assetLocation/drop/down/list | get | name | id | disabled |  |  | /fms/assetLocation/drop/down/list |
| 1985529808804665099 | 资产处置方式 | fms | /fms/common/enumDropDown?type=AssetDisposalDisposalMethod | get | value | code | disabled |  |  | ?type=AssetDisposalDisposalMethod |
| 1980902471560339986 | 资产盘点表 | fms | /fms/assetStocktaking/dropDownList | get | code | id | disabled | keyword | {"searchKeyField": "keyword"} | /fms/assetStocktaking/dropDownList |
| 1980902471560339452 | 资产类别 | fms | /fms/dict/list?key=assetCategory | get | name | value | disabled |  |  | ?key=assetCategory |
| 1857041728750010370 | 补货建议平台类型 | mrp | /mrp/common/enumDropDown?type=CfgRulePlatformType | get | value | code | disabled |  |  | ?type=CfgRulePlatformType |
| 1851206512162652161 | 补货建议数据类型 | mrp | /mrp/common/enumDropDown?type=CreateType | get | value | code | disabled |  |  | ?type=CreateType |
| 1838130564031078401 | 补货建议标签 | mrp | /mrp/labelInfo/search/label | get | name | id | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /mrp/labelInfo/search/label |
| 1838498188099571713 | 补货建议标识查询 | mrp | /mrp/dictBasic/treeByType?type=suggestedMarkType | get | name | code | disabled |  | {"children": "childrenList"} | ?type=suggestedMarkType |
| 1851197257560829953 | 补货建议状态 | mrp | /mrp/common/enumDropDown?type=SuggestStatus | get | value | code | disabled |  |  | ?type=SuggestStatus |
| 2011325939681894402 | B2B销售订单关联状态 | oms | /oms/common/enumDropDown?type=KolB2bRefStatus | get | value | code | disabled |  |  | ?type=KolB2bRefStatus |
| 1778357812619907074 | B2C中转状态 | oms | /oms/drop/down/dict/list?key=soB2cTransferStatus | get | value | code | disabled |  |  | ?key=soB2cTransferStatus |
| 1778353521612034050 | B2C付款状态 | oms | /oms/drop/down/dict/list?key=soB2cPayStatus | get | value | code | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | ?key=soB2cPayStatus |
| 1778356621932498946 | B2C异常信息 | oms | /oms/drop/down/dict/list?key=soB2cAbnormalType | get | value | code | disabled |  |  | ?key=soB2cAbnormalType |
| 1778357002477506561 | B2C异常订单类型 | oms | /oms/drop/down/dict/list?key=b2cOrderErrorType | get | value | code | disabled |  |  | ?key=b2cOrderErrorType |
| 1778357487196442625 | B2C组包状态 | oms | /oms/drop/down/dict/list?key=soB2cPackageStatus | get | value | code | disabled |  |  | ?key=soB2cPackageStatus |
| 1778354896173535233 | B2C订单分类 | oms | /oms/orderCategory/list | get | name | id | disabled |  |  | /oms/orderCategory/list |
| 1783112970209857538 | B2C订单待处理类型 | oms | /oms/drop/down/dict/list?key=SoB2cWaitHandleType | get | value | code | disabled |  |  | ?key=SoB2cWaitHandleType |
| 1778356050781540353 | B2C订单标签 | oms | /oms/drop/down/dict/list?key=soB2cLable | get | value | code | disabled |  |  | ?key=soB2cLable |
| 1778353155952611330 | B2C订单状态 | oms | /oms/drop/down/dict/list?key=soB2cBillStatus | get | value | code | disabled |  |  | ?key=soB2cBillStatus |
| 1912762635962269698 | NF-e发票状态 | oms | /oms/common/enumDropDown?type=SoB2cNfeStatus | get | value | code | disabled |  |  | ?type=SoB2cNfeStatus |
| 1760514025772290049 | OMS单据类型 | oms | /oms/common/enumDropDown?type=BillType | get | value | code | disabled |  |  | ?type=BillType |
| 1899364184284504066 | VAT发票状态 | oms | /oms/common/enumDropDown?type=SoB2cVatStatus | get | value | code | disabled |  |  | ?type=SoB2cVatStatus |
| 1777599520310300673 | b2b销售订单单据类型 | oms | /oms/drop/down/dict/list?key=soB2BBillType | get | value | code | disabled |  |  | ?key=soB2BBillType |
| 1802612036289331202 | b2c销售订单发货类型 | oms | /oms/drop/down/dict/list?key=b2cOrderDeliveryType | get | value | code | disabled |  |  | ?key=b2cOrderDeliveryType |
| 1845753231284609025 | b2c销售退货原因 | oms | /oms/common/enumDropDown?type=SoB2cReturnReason | get | value | code | disabled |  |  | ?type=SoB2cReturnReason |
| 1845752900224000001 | b2c销售退货类型 | oms | /oms/common/enumDropDown?type=SoB2cReturnType | get | value | code | disabled |  |  | ?type=SoB2cReturnType |
| 1947549740291871937 | listingInfo平台状态 | oms | /oms/common/enumDropDown?type=ListingInfoPlatformStatus | get | value | code | disabled |  |  | ?type=ListingInfoPlatformStatus |
| 1827892644711706626 | 三方仓列表 | oms | /oms/common/enumDropDown?type=OmsPlatform | get | value | code | disabled |  |  | ?type=OmsPlatform |
| 1899303495230517249 | 上传状态类型 | oms | /oms/drop/down/dict/list?key=uploadStatus | get | value | code | disabled |  |  | ?key=uploadStatus |
| 1782247631460765698 | 下拉全部店铺 | oms | /oms/shop/list | get | name | id | disabled |  |  | /oms/shop/list |
| 1881516346628116481 | 作废类型 | oms | /oms/drop/down/dict/list?key=invalidType | get | value | code | disabled |  |  | ?key=invalidType |
| 1906634840376127489 | 全托管平台状态 | oms | /oms/drop/down/dict/list?key=fullyManagedPlatformStatus | get | value | code | disabled |  |  | ?key=fullyManagedPlatformStatus |
| 1906639172408487937 | 全托管订单来源 | oms | /oms/drop/down/dict/list?key=orderSourceType | get | value | code | disabled |  |  | ?key=orderSourceType |
| 1958788190125260801 | 创建状态 | oms | /oms/drop/down/dict/list?key=createStatus | get | value | code | disabled |  |  | ?key=createStatus |
| 1988139364931194882 | 单据子类型 | oms | /oms/common/enumDropDown?type=OrderSubType | get | value | code | disabled |  |  | ?type=OrderSubType |
| 1995673188139008003 | 发布形式 | oms | /oms/drop/down/dict/list?key=publishType | get | name | value | disabled |  |  | ?key=publishType |
| 1899304480615133186 | 发票模板类型 | oms | /oms/drop/down/dict/list?key=invoiceTemplateType | get | value | code | disabled |  |  | ?key=invoiceTemplateType |
| 1899303570505691138 | 发票状态类型 | oms | /oms/drop/down/dict/list?key=invoiceStatus | get | value | code | disabled |  |  | ?key=invoiceStatus |
| 1899304433131417602 | 发票类型 | oms | /oms/drop/down/dict/list?key=invoiceType | get | value | code | disabled |  |  | ?key=invoiceType |
| 1911734299764355074 | 发票账号下拉 | oms | /oms/cfgInvoiceInvalid/getCompanyName | get | code | value | disabled |  |  | /oms/cfgInvoiceInvalid/getCompanyName |
| 1899303401970167809 | 发票配置类型 | oms | /oms/drop/down/dict/list?key=cfgInvoiceType | get | value | code | disabled |  |  | ?key=cfgInvoiceType |
| 1995787832613951578 | 合作类型 | oms | /oms/cfgKolOption/select?type=cooperationType | get | name | id | disable |  |  | ?type=cooperationType |
| 1926921419541331970 | 头程调整字段类型 | oms | /oms/drop/down/dict/list?key=firstMileCategoryFieldType | get | value | code | disabled |  |  | ?key=firstMileCategoryFieldType |
| 1996489851283120130 | 寄样类型 | oms | /oms/cfgKolOption/select?type=kolSampleType | get | name | name | disabled |  |  | ?type=kolSampleType |
| 1930152562183393540 | 小程序销售平台 | oms | /oms/drop/down/dict/listInternalSalesPlatform?key=miniProgramSalesPlatform | get | value | code | disable | {} | {"searchKeyField": "{}"} | ?key=miniProgramSalesPlatform |
| 1876256933315940353 | 库存SKU下拉 | oms | /oms/listing/select/list | postJson | value | code | disabled | remoteSearchSku | {"searchKeyField": "remoteSearchSku"} | /oms/listing/select/list |
| 1768450132191219713 | 店铺下拉 | oms | /oms/shop/listAuth | postJson | name | id | disabled |  |  | /oms/shop/listAuth |
| 1795318754901495810 | 店铺授权状态 | oms | /oms/common/enumDropDown?type=AuthStatus | get | value | code | disabled |  |  | ?type=AuthStatus |
| 1963412863100375042 | 授信类型 | oms | /oms/drop/down/dict/list?key=creditType | get | value | code | disabled |  |  | ?key=creditType |
| 1962363239052607489 | 收款方式 | oms | /oms/drop/down/dict/list?key=receiveMethod | get | value | code | disabled |  |  | ?key=receiveMethod |
| 1995673188139008030 | 来源平台 | oms | /oms/drop/down/dict/list?key=socialMediaPlatform | get | value | code | disabled |  |  | ?key=socialMediaPlatform |
| 1937725714494869505 | 海外仓平台 | oms | /oms/common/enumDropDown?type=OmsPlatformEnum | get | name | code | disabled |  |  | ?type=OmsPlatformEnum |
| 1795594363602997250 | 禁用状态 | oms | /oms/drop/down/dict/list?key=disabledStatus | get | value | code | disabled |  |  | ?key=disabledStatus |
| 1767086563482669057 | 自动匹配类型 | oms | /oms/common/enumDropDown?type=SkuMappingRule | get | value | code | disabled |  |  | ?type=SkuMappingRule |
| 2026960219825614900 | 获取TikTok店铺 | oms | /oms/shop/getShopifyByPlatform | get | name | id | disabled |  |  | /oms/shop/getShopifyByPlatform |
| 1754025014362902529 | 获取亚马逊店铺 | oms | /oms/shop/listShopByAmazon | get | name | id | disabled |  |  | /oms/shop/listShopByAmazon |
| 1753952456133316610 | 获取客户（下拉框） | oms | /oms/customer/listEnable | postJson | name | id | false |  |  | /oms/customer/listEnable |
| 1778350878693003266 | 获取销售平台 | oms | /oms/drop/down/dict/list?key=salesPlatform | get | value | code | disabled |  |  | ?key=salesPlatform |
| 1753988202969960449 | 获取销售平台类型 | oms | /oms/drop/down/dict/list?key=salesPlatform | get | value | code | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | ?key=salesPlatform |
| 1995787832613951577 | 语言 | oms | /oms/dictLanguage/drop/down | postJson | nameZh | id | disable |  |  | /oms/dictLanguage/drop/down |
| 1995673188139008020 | 费用名称 | oms | /oms/cfgKolOption/select?type=costType | get | name | id | disabled |  |  | ?type=costType |
| 1995673188139008002 | 达人昵称 | oms | /oms/kolPartnerInfo/drop/down | postJson | nickname | id | disabled |  |  | /oms/kolPartnerInfo/drop/down |
| 1995787832613951579 | 达人类型 | oms | /oms/cfgKolOption/select?type=partnerType | get | name | id | disable |  |  | ?type=partnerType |
| 1972120405168500737 | 银行账号 | oms | /oms/bankAccount/selectAll | get | value | code | disabled |  |  | /oms/bankAccount/selectAll |
| 1797627231879565313 | 销售平台（国内） | oms | /oms/drop/down/dict/listByType?type=salesPlatform&subType=internalSalesPlatform | get | value | code | disabled |  |  | ?type=salesPlatform |
| 1797627144902283266 | 销售平台（国外） | oms | /oms/drop/down/dict/listByType?type=salesPlatform&subType=overseasSalesPlatform | get | value | code | disabled |  |  | ?type=salesPlatform |
| 1996509701767977072 | 销售订单关联状态 | oms | /oms/common/enumDropDown?type=KolSubB2cApplicationOrderStatus | get | value | code | disabled |  |  | ?type=KolSubB2cApplicationOrderStatus |
| 1996509701767977073 | 销售订单关联状态 | oms | /oms/common/enumDropDown?type=KolSubB2cApplicationDeliveryStatus | get | value | code | disabled |  |  | ?type=KolSubB2cApplicationDeliveryStatus |
| 1762650623888592897 | 销售订单类型 | oms | /oms/common/enumDropDown?type=BillType | get | value | code | disabled |  |  | ?type=BillType |
| 1851164200174825473 | 销售退货原因 | oms | /oms/common/enumDropDown?type=ReturnReason | get | value | code | disabled |  |  | ?type=ReturnReason |
| 1760514616300933121 | 销售退货类型 | oms | /oms/soB2cReturn/getSoReturnType | get | value | code | disabled |  |  | /oms/soB2cReturn/getSoReturnType |
| 1996509701767977074 | 项目标签 | oms | /oms/cfgKolOption/select?type=projectTag | get | name | id | disable |  |  | ?type=projectTag |
| 1816305334647930882 | SKU下拉 | plm | /plm/product/detail/search/sku | get | skuNo | skuId | disabled | remoteSearchSku | {"searchKeyField": "remoteSearchSku"} | /plm/product/detail/search/sku |
| 1873744152649216001 | plm产品任务状态 | plm | /plm/task/getTaskStatusSelect?searchKeyword= | get | label | value | disabled | code | {"searchKeyField": "code"} | /plm/task/getTaskStatusSelect?searchKeyword= |
| 1839481044783783937 | 产品分类 | plm | /plm/category/tree | get | name | id | disable |  | {"children": "childrenList"} | /plm/category/tree |
| 2027326674634227713 | 产品变更字段 | plm | /plm/productChange/getProductChangeFieldEnum | postJson | name | code | disabled |  |  | /plm/productChange/getProductChangeFieldEnum |
| 1991069377072234498 | 产品品牌 | plm | /plm/product/detail/listProductBrand | get | name | id | disabled |  |  | /plm/product/detail/listProductBrand |
| 1873934933351686146 | 产品标签 | plm | /plm/basicLabel/list | postJson | name | id | disabled | code | {"searchKeyField": "code"} | /plm/basicLabel/list |
| 1872276268504252418 | 产品等级 | plm | /plm/dict/list?type=productGrade | get | name | id | disable |  |  | ?type=productGrade |
| 1762065932915576833 | 产品销售状态 | plm | /plm/common/enumDropDown?type=SaleState | get | value | code | disabled |  |  | ?type=SaleState |
| 1873985413910097922 | 任务优先级 | plm | /plm/task/getTaskPrioritySelect | get | label | value | disabled | code | {"searchKeyField": "code"} | /plm/task/getTaskPrioritySelect |
| 1959880763110146071 | 使用方 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1959880763110146080 | 使用方 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1897920976528625884 | 保险属性 | plm | /plm/dict/list?type=insuranceProperty | get | name | value | disable |  |  | ?type=insuranceProperty |
| 1961251818584240132 | 创建人 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1961251818584240204 | 创建人 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1838509312517853186 | 品牌 | plm | /plm/dict/list?type=productBrand | get | name | id | disabled |  |  | ?type=productBrand |
| 1961251818584240130 | 审核人 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1961251818584240203 | 审核人 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1879155632453337090 | 应用分类 | plm | /plm/applicationCategory/list | get | name | id | disabled |  |  | /plm/applicationCategory/list |
| 1991069377072234499 | 开发团队 | plm | /plm/product/detail/listProductRDTTeam | get | name | id | disabled |  |  | /plm/product/detail/listProductRDTTeam |
| 1959880763110146072 | 归属人 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1959880763110146081 | 归属人 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1961251818584240201 | 归属人 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1904509339083755750 | 新品首批 | plm | /plm/dict/list?type=firstMassProductType | get | name | value | disabled |  |  | ?type=firstMassProductType |
| 1865962091050786818 | 模具类型 | plm | /plm/cfgMouldSetting/mouldList | postJson | name | id | disable |  |  | /plm/cfgMouldSetting/mouldList |
| 1959880763110146055 | 获取用户 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1995673188139008004 | 获取用户 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1742885076630179841 | 获取用户 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 2001213973844975618 | 获取用户 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1871880740373024769 | 证书类型 | plm | /plm/dict/list?type=certificateType | get | name | value | disable |  |  | ?type=certificateType |
| 1871881430965178369 | 证书项目 | plm | /plm/dict/list?type=certificateProject | get | name | value | disable |  |  | ?type=certificateProject |
| 1978390642597844689 | 返还标准 | plm | /plm/common/enumDropDown?type=CfgMoldReturnAlertRuleCountDim | get | value | code | disabled |  |  | ?type=CfgMoldReturnAlertRuleCountDim |
| 1978390642597844690 | 返还标准 | plm | /plm/common/enumDropDown?type=MoldMonitorReturnStatus | get | value | code | disabled |  |  | ?type=MoldMonitorReturnStatus |
| 1961251818584240131 | 退回人 | plm | /plm/common/findUserList | postJson | userName | userId | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /plm/common/findUserList |
| 1872278410933788673 | 项目属性 | plm | /plm/dict/list?type=productProperty | get | name | id | disable |  |  | ?type=productProperty |
| 1873925575024787457 | 项目状态 | plm | /plm/dict/drop/down?type=projectState | get | name | code | disabled | code | {"searchKeyField": "code"} | ?type=projectState |
| 1879378583576535041 | 项目进展 | plm | /plm/product/getProgressStatus | get | name | status | disabled |  |  | /plm/product/getProgressStatus |
| 1873944236196323329 | 项目阶段 | plm | /plm/task/phase/listPhaseName | get | label | label | disabled | code | {"searchKeyField": "code"} | /plm/task/phase/listPhaseName |
| 1742865001332285442 | scm获取到货状态 | scm | /scm/drop/down/arrivalStatus/list | get | value | code | disabled |  |  | /scm/drop/down/arrivalStatus/list |
| 1933366202189139969 | 付款条件 | scm | /scm/kingdeePaymentCondition/select?type=paymentCondition | get | value | code | disabled |  |  | ?type=paymentCondition |
| 2046543913989107714 | 供应商体系认证 | scm | /scm/dict/list?key=certificate | get | name | value | disable |  |  | ?key=certificate |
| 1747815870534520833 | 供应商分类 | scm | /scm/dict/list?key=supplierCategory | get | name | id | disabled |  |  | ?key=supplierCategory |
| 1747817166222135297 | 供应商等级 | scm | /scm/supplier/grade/list | get | name | id | disabled |  |  | /scm/supplier/grade/list |
| 1747817733711466497 | 供应商阶段 | scm | /scm/drop/down/supplier/phase/list | get | value | code | disabled |  |  | /scm/drop/down/supplier/phase/list |
| 1871458762847473666 | 供应商阶段操作类型 | scm | /scm/common/enumDropDown?type=SupplierPhaseOperateType | get | value | code | disable |  |  | ?type=SupplierPhaseOperateType |
| 1760918359152529409 | 关联采购单状态 | scm | /scm/drop/down/createPoType/list | get | value | code | disabled |  |  | /scm/drop/down/createPoType/list |
| 1921824840134709494 | 合同盖章状态 | scm | /scm/common/enumDropDown?type=ContractStampStatus | get | value | code | disabled |  |  | ?type=ContractStampStatus |
| 1939534049882165521 | 合同类型 | scm | /scm/dict/list?key=contractType | get | name | value | disabled |  |  | ?key=contractType |
| 1948205707132163267 | 合同类型 | scm | /scm/dict/list?key=contractType | get | name | value | disabled |  |  | ?key=contractType |
| 2015667046764167170 | 委外订单类型 | scm | /scm/dict/list?key=subcontractOrderType | get | name | value | disabled | disabled | {"searchKeyField": "disabled"} | ?key=subcontractOrderType |
| 1747910220232933377 | 执行状态 | scm | /scm/dict/list?key=executionStatus | get | name | value | disabled |  |  | ?key=executionStatus |
| 1938075877396992270 | 拜访结果 | scm | /scm/common/enumDropDown?type=SupplierVisitResult | get | value | code | disabled |  |  | ?type=SupplierVisitResult |
| 1933410334933014986 | 日均销量类型 | scm | /scm/common/enumDropDown?type=CfgSupplierSalesDailySalesType | get | value | code | disabled |  |  | ?type=CfgSupplierSalesDailySalesType |
| 1937051917880416721 | 日均销量类型 | scm | /scm/common/enumDropDown?type=SupplierCredentialStatus | get | value | code | disabled |  |  | ?type=SupplierCredentialStatus |
| 1747823527035146241 | 结算方式 | scm | /scm/dict/list?key=supplierPayMode | get | name | value | disabled |  |  | ?key=supplierPayMode |
| 1742860958987919362 | 获取供应商 | scm | /scm/drop/down/supplier/allList | get | value | code | disabled | code | {"searchKeyField": "code"} | /scm/drop/down/supplier/allList |
| 1742864822164201473 | 获取审核状态 | scm | /scm/drop/down/approveStatus/list | get | value | code | disabled |  |  | /scm/drop/down/approveStatus/list |
| 1748238796769681410 | 采购订单类型 | scm | /scm/dict/list?key=purchaseOrderType | get | name | value | disabled |  |  | ?key=purchaseOrderType |
| 1933410334933014987 | 销量比例类型 | scm | /scm/common/enumDropDown?type=CfgSupplierSalesSalesRatioType | get | value | code | disabled |  |  | ?type=CfgSupplierSalesSalesRatioType |
| 1747513846660075521 | SRM发货单收货状态枚举 | srm | /srm/common/enumDropDown?type=ReceiptStatus | get | value | code | disabled |  |  | ?type=ReceiptStatus |
| 1750083664919662593 | 对账明细来源类型 | srm | /srm/drop/down/dict/list?key=poReconciliationSourceType | get | value | code | disabled |  |  | ?key=poReconciliationSourceType |
| 1750076721375219713 | 获取对账单对账状态 | srm | /srm/drop/down/dict/list?key=poReconciliationStatus | get | value | code | disabled |  |  | ?key=poReconciliationStatus |
| 1988077485304946691 | 送货单收货状态 | srm | /srm/drop/down/dict/list?key=deliveryOrderReceiptStatus | get | value | code | disabled |  |  | ?key=deliveryOrderReceiptStatus |
| 1933364711139233793 | 采购对账明细对账状态 | srm | /srm/drop/down/dict/list?key=serachPoReconciliationDetailStatus | get | value | code | disabled |  |  | ?key=serachPoReconciliationDetailStatus |
| 1986605910903750657 | 中台监控单据类型 | sys | /sys/dictBasic/list?type=sourceType | get | remark | value | disabled |  |  | ?type=sourceType |
| 1809067559207870465 | 其他出库单-业务类型 | sys | /sys/dictKingdee/drop/down?typeName=其他出库单业务类型 | get | name | code | disabled |  |  | ?typeName=其他出库单业务类型 |
| 1809067479931330562 | 其他出库单-出库类型 | sys | /sys/dictKingdee/drop/down?typeName=其他出库单类型 | get | name | code | disabled |  |  | ?typeName=其他出库单类型 |
| 1876445030684053506 | 军区列表 | sys | /sys/dictPartition/drop/down | postJson | name | id | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /sys/dictPartition/drop/down |
| 2016700620313743361 | 区域下拉 | sys | /sys/dict/global/area/list | get | value | code | disabled |  |  | /sys/dict/global/area/list |
| 1982693025867481090 | 单据业务类型 | sys | /sys/dictBasic/list?type=sourceType | get | name | value | disabled |  |  | ?type=sourceType |
| 2009117155366295721 | 发货通知变更单类型 | sys | /sys/common/enumDropDown?type=SysUserInfoThirdAuthType | get | value | code | disabled |  |  | ?type=SysUserInfoThirdAuthType |
| 1985529808804665100 | 币种 | sys | /sys/currency/list | get | name | id | disabled |  |  | /sys/currency/list |
| 2039227404495462401 | 币种(显示ID) | sys | /sys/currency/list | get | id | id | disabled |  |  | /sys/currency/list |
| 1961251818584240202 | 归属部门 | sys | /sys/department/drop/down | get | deptName | deptId | disabled | deptName | {"searchKeyField": "deptName"} | /sys/department/drop/down |
| 1873652875718873089 | 用户角色 | sys | /sys/role/list | get | roleName | roleName | disabled | code | {"searchKeyField": "code"} | /sys/role/list |
| 1879469244741140481 | 用户角色ids | sys | /sys/role/list | get | roleName | id | disabled | code | {"searchKeyField": "code"} | /sys/role/list |
| 1926933548095262975 | 第三方通知单据类型 | sys | /sys/dictBasic/list?type=thirdNoticeBusinessType | get | name | value | disabled |  |  | ?type=thirdNoticeBusinessType |
| 1983055108086128641 | 系统代码 | sys | /sys/common/enumDropDown?type=SystemCode | get | value | code | disabled |  |  | ?type=SystemCode |
| 1753954793224671233 | 获取国家 | sys | /sys/dict/country/list | get | nameCn | id | disabled |  |  | /sys/dict/country/list |
| 1753954793224671234 | 获取国家(带默认) | sys | /sys/dict/country/listWithDefault | get | nameCn | id | disabled |  |  | /sys/dict/country/listWithDefault |
| 1865304737355427841 | 获取国家名称 | sys | /sys/dict/country/list | get | nameCn | nameCn | disabled |  |  | /sys/dict/country/list |
| 1747800607470653442 | 获取系统用户是否绑定微信 | sys | /sys/dictBasic/list?type=is_bind_wechat | get | name | value | disabled |  |  | ?type=is_bind_wechat |
| 1747166094306578433 | 获取系统用户状态 | sys | /sys/dictBasic/list?type=sys_user_state | get | name | value | disabled |  |  | ?type=sys_user_state |
| 1747893210916749314 | 获取组织 | sys | /sys/company/list | get | companyName | id | disabled |  |  | /sys/company/list |
| 1980902471560339450 | 获取组织 | sys | /sys/company/list | get | companyName | id | disabled | name | {"searchKeyField": "name"} | /sys/company/list |
| 2058721633179127810 | 获取组织名称 | sys | /sys/company/list | get | companyName | companyName | disabled |  |  | /sys/company/list |
| 1753953984822906881 | 获取部门（下拉框） | sys | /sys/department/drop/down | get | deptName | deptId | disabled |  |  | /sys/department/drop/down |
| 1959880763110146052 | 部门 | sys | /sys/department/drop/down | get | deptName | deptId | disabled |  |  | /sys/department/drop/down |
| 1933470858403954690 | 部门级联 | sys | /sys/department/cascadeTree | get | value | code | disabled |  | {"children": "childTreeList"} | /sys/department/cascadeTree |
| 1866446715808272385 | b2c对账状态 | tms | /tms/common/enumDropDown?type=TmsB2cDeclareReconciliationStatus | get | value | code | disable |  |  | ?type=TmsB2cDeclareReconciliationStatus |
| 1783701677866487809 | 中转渠道级联 | tms | /tms/transferLogisticsSupplier/tree | get | name | id | disabled |  | {"children": "children"} | /tms/transferLogisticsSupplier/tree |
| 1798306321519349762 | 中转状态-物流商 | tms | /tms/drop/down/dict/list?key=transferStatus | get | value | code | disabled |  |  | ?key=transferStatus |
| 1798307442237378561 | 入库预报状态 | tms | /tms/drop/down/dict/list?key=instockForecastStatus | get | value | code | disabled |  |  | ?key=instockForecastStatus |
| 1798305700368093185 | 出库状态 | tms | /tms/drop/down/dict/list?key=transferOutstockStatus | get | value | code | disabled |  |  | ?key=transferOutstockStatus |
| 1816377575825973250 | 发货类型-TMS | tms | /tms/drop/down/dict/list?key=shipmentType | get | value | code | disabled |  |  | ?key=shipmentType |
| 1871878501281521665 | 地址类型 | tms | /tms/common/enumDropDown?type=LogisticsAddressType | get | value | code | disable |  |  | ?type=LogisticsAddressType |
| 1948644446006562818 | 头程对账单类型 | tms | /tms/drop/down/dict/list?key=firstSupplierType | get | value | code | disable |  |  | ?key=firstSupplierType |
| 1772567505542320129 | 头程物流单获取发票状态 | tms | /tms/common/enumDropDown?type=InvoicesStatus | get | value | code | disabled |  |  | ?type=InvoicesStatus |
| 1772567660639293442 | 头程物流单获取对账状态 | tms | /tms/common/enumDropDown?type=ReconciliationStatus | get | value | code | disabled |  |  | ?type=ReconciliationStatus |
| 1772567238075748354 | 头程物流单获取物流状态 | tms | /tms/common/enumDropDown?type=FmLogisticTrackStatus | get | value | code | disabled |  |  | ?type=FmLogisticTrackStatus |
| 1865305927543713794 | 小包分摊费用分摊 | tms | /tms/common/enumDropDown?type=CostAllocation | get | value | code | disable |  |  | ?type=CostAllocation |
| 1865302297495236609 | 小包分摊费用来源 | tms | /tms/drop/down/dict/list?key=smallBagFeeSource | get | value | code | disable |  |  | ?key=smallBagFeeSource |
| 1865326252675330050 | 小包重量分摊方式 | tms | /tms/common/enumDropDown?type=WeightAllocationSmallBag | get | value | code | disable |  |  | ?type=WeightAllocationSmallBag |
| 1866015904524898306 | 小包重量大表状态 | tms | /tms/common/enumDropDown?type=SmallBagCostAllocationMainBigTableStatus | get | value | code | disable |  |  | ?type=SmallBagCostAllocationMainBigTableStatus |
| 1866015765408223234 | 小包重量核算状态 | tms | /tms/common/enumDropDown?type=SmallBagCostAllocationMainReportStatus | get | value | code | disable |  |  | ?type=SmallBagCostAllocationMainReportStatus |
| 2049666606199492610 | 报关单状态 | tms | /tms/common/enumDropDown?type=DeclareStatus | get | value | code | disabled |  |  | ?type=DeclareStatus |
| 1774637641103314946 | 报关单获取报关类型 | tms | /tms/drop/down/dict/list?key=declareDeclareType | get | value | code | disable | {} | {"searchKeyField": "{}"} | ?key=declareDeclareType |
| 1872460427956805633 | 报关物流商授权状态 | tms | /tms/drop/down/dict/list?key=transferLogisticsAuthStatus | get | value | code | disabled |  |  | ?key=transferLogisticsAuthStatus |
| 1769982169465229313 | 查询中转物流商 | tms | /tms/transferLogisticsSupplier/listAll | get | value | code | disabled |  |  | /tms/transferLogisticsSupplier/listAll |
| 1769985454695780354 | 查询产品备案状态 | tms | /tms/common/enumDropDown?type=Status | get | value | code | disabled |  |  | ?type=Status |
| 1772808062134915074 | 核对类型 | tms | /tms/common/enumDropDown?type=CfgReconciliationType | get | value | code | disable | {} | {"searchKeyField": "{}"} | ?type=CfgReconciliationType |
| 1828248289629798402 | 核算期间下拉列表 | tms | /tms/reportPeriodMonth/listLocalDate | postJson | reportPeriodStr | reportPeriodStr | disabled |  |  | /tms/reportPeriodMonth/listLocalDate |
| 1768455056119566337 | 渠道下拉 | tms | /tms/logisticsChannel/listAll | get | value | code | disabled |  |  | /tms/logisticsChannel/listAll |
| 2046129612890592746 | 渠道下拉 | tms | /tms/logisticsChannel/listWithAll | get | value | code | disabled |  |  | /tms/logisticsChannel/listWithAll |
| 1788739005089583106 | 物流单运输状态 | tms | /tms/drop/down/dict/list?key=logisticTrackStatus | get | value | code | disable | {} | {"searchKeyField": "{}"} | ?key=logisticTrackStatus |
| 1769908748240818177 | 物流商下拉 | tms | /tms/logisticsSupplier/listAll | get | value | code | disabled |  |  | /tms/logisticsSupplier/listAll |
| 2043962114653049079 | 物流商下拉(简称) | tms | /tms/logisticsSupplier/listAllShort | get | value | code | disabled |  |  | /tms/logisticsSupplier/listAllShort |
| 2046129612890592745 | 物流商下拉(简称) | tms | /tms/logisticsSupplier/listWithAll | get | value | code | disabled |  |  | /tms/logisticsSupplier/listWithAll |
| 1778719803935035394 | 物流渠道级联 | tms | /tms/logisticsChannel/tree | get | value | code | disabled |  | {"children": "childTreeList"} | /tms/logisticsChannel/tree |
| 2026918290210706900 | 物流系统任务单据类型 | tms | /tms/common/enumDropDown?type=TmsAsyncTaskRecordBusinessType | get | value | code | disabled |  |  | ?type=TmsAsyncTaskRecordBusinessType |
| 1869672878375919618 | 物流费用单据类型 | tms | /tms/common/enumDropDown?type=CostBillType | get | value | code | disable |  |  | ?type=CostBillType |
| 1789914150890115073 | 物流费用对账状态 | tms | /tms/drop/down/dict/list?key=reconciliationStatus | get | value | code | disable | {} | {"searchKeyField": "{}"} | ?key=reconciliationStatus |
| 2026918290210706899 | 状态 | tms | /tms/common/enumDropDown?type=TmsAsyncTaskRecordStatus | get | value | code | disabled |  |  | ?type=TmsAsyncTaskRecordStatus |
| 1864626097244135426 | 自发货费用支付状态 | tms | /tms/common/enumDropDown?type=LogisticsBillCostPayStatus | get | value | code | disable |  |  | ?type=LogisticsBillCostPayStatus |
| 1798304173486247937 | 订单上传状态 | tms | /tms/drop/down/dict/list?key=transferDeclareUploadStatus | get | value | code | disabled |  |  | ?key=transferDeclareUploadStatus |
| 1816376967828054018 | 订单类型-TMS | tms | /tms/drop/down/dict/list?key=salesOrderType | get | value | code | disabled |  |  | ?key=salesOrderType |
| 1828250731448729601 | 账单来源 | tms | /tms/drop/down/dict/list?key=billSourceType | get | value | code | disabled |  |  | ?key=billSourceType |
| 1828251561027534850 | 费用分类 | tms | /tms/drop/down/dict/list?key=dictCostCategory | get | value | code | disabled |  |  | ?key=dictCostCategory |
| 1949731277506969602 | 费用归属 | tms | /tms/drop/down/dict/list?key=dictCostAttribution | get | value | code | disabled |  |  | ?key=dictCostAttribution |
| 2013885652633933262 | 费用配置--配置单据 | tms | /tms/drop/down/dict/list?key=cfgCostBusinessKey | get | value | code | disabled | searchKeyword |  | ?key=cfgCostBusinessKey |
| 1952221620488400898 | 轨迹推送类型 | tms | /tms/drop/down/dict/list?key=trackPushType | get | value | code | disabled |  |  | ?key=trackPushType |
| 1952220893116395522 | 轨迹服务商 | tms | /tms/drop/down/dict/list?key=trackPlatformType | get | value | code | disabled |  |  | ?key=trackPlatformType |
| 1996121046274809857 | B2B三方发货单单据类型 | wms | /wms/dict/drop/down?type=thirdDeliveryStatus | get | name | code | disable |  |  | ?type=thirdDeliveryStatus |
| 1754025662391259138 | FBA发货状态 | wms | /wms/common/enumDropDown?type=FbaDeliveryStatus | get | value | code | disabled |  |  | ?type=FbaDeliveryStatus |
| 1762024591724646402 | WMS发货状态 | wms | /wms/common/enumDropDown?type=DeliveryStatus | get | value | code | disabled |  |  | ?type=DeliveryStatus |
| 1768453576897597442 | b2c发货单拣货类型 | wms | /wms/common/enumDropDown?type=PickingType | get | value | code | disabled |  |  | ?type=PickingType |
| 1768557895831523330 | b2c发货单物流类型 | wms | /wms/dict/drop/down?type=b2cDeliveryLogisticType | get | name | code | disabled |  |  | ?type=b2cDeliveryLogisticType |
| 1768452674581172226 | b2c发货单状态 | wms | /wms/common/enumDropDown?type=SoB2cDeliveryStatus | get | value | code | disabled |  |  | ?type=SoB2cDeliveryStatus |
| 1754027674801541122 | fba平台货件状态 | wms | /wms/common/enumDropDown?type=FbaPlatformShipmentStatus | get | value | code | disabled |  |  | ?type=FbaPlatformShipmentStatus |
| 2034112170870812673 | fbt平台货件状态 | wms | /wms/common/enumDropDown?type=FbtPlatformShipmentStatus | get | value | code | disabled |  |  | ?type=FbtPlatformShipmentStatus |
| 2036343441480364033 | 一般缺陷AQL | wms | /wms/dict/drop/down?type=generalDefectAql | get | name | code | disable |  |  | ?type=generalDefectAql |
| 1783396399870644225 | 上传状态 | wms | /wms/dict/drop/down?type=packageForecastUploadStatus | get | name | code | status |  |  | ?type=packageForecastUploadStatus |
| 1914282002903748842 | 上架状态 | wms | /wms/common/enumDropDown?type=PutawayStatus | get | value | code | disabled |  |  | ?type=PutawayStatus |
| 2036343542743445505 | 严重缺陷AQL | wms | /wms/dict/drop/down?type=majorDefectAql | get | name | code | disable |  |  | ?type=majorDefectAql |
| 1961251818584240133 | 交货仓库 | wms | /wms/warehouse/list | get | name | id | disabled |  |  | /wms/warehouse/list |
| 1996122759081762818 | 交货方式 | wms | /wms/dict/drop/down?type=deliveryMethod | get | name | code | disable |  |  | ?type=deliveryMethod |
| 1797470828225368065 | 仓位状态字典 | wms | /wms/dict/drop/down?type=warehouseAreaType | get | name | code | disabled |  |  | ?type=warehouseAreaType |
| 2059534686716338178 | 仓位移动操作类型 | wms | /wms/common/enumDropDown?type=WarehouseLocationMoveOperateType | get | value | code | disabled |  |  | ?type=WarehouseLocationMoveOperateType |
| 1860879818901192705 | 仓位移动来源类型 | wms | /wms/common/enumDropDown?type=MarehouseMoveSourceType | get | value | code | disabled |  |  | ?type=MarehouseMoveSourceType |
| 1959880763110146051 | 仓库 | wms | /wms/warehouse/list | get | name | id | disabled |  |  | /wms/warehouse/list |
| 2058721052234469377 | 仓库名称下拉 | wms | /wms/warehouse/list | get | name | name | disabled |  |  | /wms/warehouse/list |
| 1798612747626442753 | 仓库库存状态 | wms | /wms/common/enumDropDown?type=InventoryStatus | get | value | code | disabled |  |  | ?type=InventoryStatus |
| 1798611409198870529 | 仓库操作类型 | wms | /wms/common/enumDropDown?type=InventoryOperationMode | get | value | code | disabled |  |  | ?type=InventoryOperationMode |
| 1996121069712580609 | 仓库操作类型 | wms | /wms/dict/drop/down?type=warehouseOperationType | get | name | code | disable |  |  | ?type=warehouseOperationType |
| 1871501884889550849 | 仓库类型 | wms | /wms/dict/list?key=warehouseType | get | name | id | disable |  |  | ?key=warehouseType |
| 1871510335543889921 | 仓库经营类型 | wms | /wms/dict/drop/down?type=warehouseManageType | get | name | code | disable |  |  | ?type=warehouseManageType |
| 1899354823365533697 | 入库类型 | wms | /wms/dict/drop/down?type=instockType | get | name | code | disable |  |  | ?type=instockType |
| 1872210324297662466 | 出入库单据类型 | wms | /wms/common/enumDropDown?type=InventorySourceType | get | value | code | disable |  |  | ?type=InventorySourceType |
| 1871771082979102722 | 分单规则 | wms | /wms/common/enumDropDown?type=SeparateRule | get | value | code | disable |  |  | ?type=SeparateRule |
| 1755056191752376322 | 加工单事务类型 | wms | /wms/dict/drop/down?type=workType | get | name | code | disabeld |  |  | ?type=workType |
| 1765269036247027713 | 加工单来源类型 | wms | /wms/common/enumDropDown?type=MachineSourceType | get | value | code | disabled |  |  | ?type=MachineSourceType |
| 1868542168394650026 | 单据下推类型 | wms | /wms/dict/drop/down?type=billPushDownStatus | get | name | code | disable | {} | {"searchKeyField": "{}"} | ?type=billPushDownStatus |
| 1961251818584240134 | 单据状态 | wms | /wms/drop/down/approveStatus/list | get | value | code | disabled |  |  | /wms/drop/down/approveStatus/list |
| 1961251818584240200 | 单据状态 | wms | /wms/drop/down/approveStatus/list | get | value | code | disabled |  |  | /wms/drop/down/approveStatus/list |
| 1790687710097182722 | 发货计划单据类型 | wms | /wms/dict/drop/down?type=deliveryPlanType | get | name | code | disabled |  |  | ?type=deliveryPlanType |
| 1856584184717701122 | 发货通知变更单类型 | wms | /wms/common/enumDropDown?type=SoDeliveryNoticeChangeType | get | value | code | disabled |  |  | ?type=SoDeliveryNoticeChangeType |
| 1871510592197545986 | 地理位置 | wms | /wms/dict/drop/down?type=geographyLocation | get | name | code | disable |  |  | ?type=geographyLocation |
| 1783395903109861377 | 大包交接状态 | wms | /wms/dict/drop/down?type=packageHandoverStatus | get | name | code | status |  |  | ?type=packageHandoverStatus |
| 1760223839335223298 | 头程发货备货类型 | wms | /wms/common/enumDropDown?type=FbaDemandType | get | value | code | disabled |  |  | ?type=FbaDemandType |
| 1760224942701416449 | 头程发货装箱状态 | wms | /wms/common/enumDropDown?type=PackingStatus | get | value | code | disabled |  |  | ?type=PackingStatus |
| 1959880763110146049 | 审核状态 | wms | /wms/drop/down/approveStatus/list | get | value | code | disabled |  |  | /wms/drop/down/approveStatus/list |
| 1783396296690765825 | 小包交接状态 | wms | /wms/dict/drop/down?type=packageSubHandoverStatus | get | name | code | status |  |  | ?type=packageSubHandoverStatus |
| 1805887014982127617 | 库区 | wms | /wms/warehouse-area/listAllArea | get | name | code | disabled |  |  | /wms/warehouse-area/listAllArea |
| 1796375416324231170 | 库区类型 | wms | /wms/dict/drop/down?type=warehouseAreaType | get | name | code | disabled |  |  | ?type=warehouseAreaType |
| 1762011801026826241 | 库存方向 | wms | /wms/dict/drop/down?type=inventoryDirection | get | name | code | disabled |  |  | ?type=inventoryDirection |
| 1749646048415977474 | 异常处理人下拉 | wms | /wms/purchaseReturnOrder/unusualHandleUserOption | get | name | id | disabled |  |  | /wms/purchaseReturnOrder/unusualHandleUserOption |
| 1783399374672367617 | 打印状态 | wms | /wms/common/enumDropDown?type=PackagePrintStatus | get | value | code | disabled |  |  | ?type=PackagePrintStatus |
| 1959880763110146054 | 执行状态 | wms | /wms/dict/list?key=executionStatus | get | name | value | disabled |  |  | ?key=executionStatus |
| 1770342079650598913 | 报关状态 | wms | /wms/dict/list?key=fmDeliveryDeclareStatus | get | name | value | disabled |  |  | ?key=fmDeliveryDeclareStatus |
| 1811357043509731329 | 拣货车下拉 | wms | /wms/pickingCart/dropDown | get | code | code | disabled |  |  | /wms/pickingCart/dropDown |
| 1805078184169836546 | 拣货车类型下拉 | wms | /wms/pickingCartType/select | postJson | name | id | disabled |  |  | /wms/pickingCartType/select |
| 1996127189130813442 | 推送类型 | wms | /wms/dict/drop/down?type=deliveryPushType | get | name | code | disable |  |  | ?type=deliveryPushType |
| 1959880763110146083 | 操作类型 | wms | /wms/dict/list?key=dictBizType | get | name | value | disabled |  |  | ?key=dictBizType |
| 1980902471560339771 | 操作类型 | wms | /wms/dict/list?key=sampleLedgerFlowSourceType | get | name | value | disabled |  |  | ?key=sampleLedgerFlowSourceType |
| 2036343634166689794 | 文件类型 | wms | /wms/dict/drop/down?type=fileType | get | name | code | disable |  |  | ?type=fileType |
| 2036343282763706370 | 方案类型 | wms | /wms/dict/drop/down?type=planType | get | name | code | disable |  |  | ?type=planType |
| 1815965527446925313 | 查询虚拟仓库列表 | wms | /wms/virtualWarehouse/list | postJson | name | id | disabled | searchKeyword | {"searchKeyField": "searchKeyword"} | /wms/virtualWarehouse/list |
| 2036343360895201281 | 检验水平 | wms | /wms/dict/drop/down?type=qcLevel | get | name | code | disable |  |  | ?type=qcLevel |
| 1809947974290542594 | 模糊查询拣货车 | wms | /wms/pickingCart/searchByKeyword | get | code | code | disabled | picking_cart_code | {"searchKeyField": "picking_cart_code"} | /wms/pickingCart/searchByKeyword |
| 1805509888042864641 | 波次类型 | wms | /wms/dict/drop/down?type=waveType | get | name | code | disabled |  |  | ?type=waveType |
| 1876566805787181058 | 海外/平台绑定仓库 | wms | /wms/warehouse/listOverseasWarehouse | get | name | id | disabled |  |  | /wms/warehouse/listOverseasWarehouse |
| 1762404952099000322 | 海外中转仓 | wms | /wms/overseasWarehouseInbound/transferWareHouseList | get | name | id | disabled |  |  | /wms/overseasWarehouseInbound/transferWareHouseList |
| 1876259669067501570 | 海外仓下拉 | wms | /wms/warehouse/listOverseasWarehouse | get | name | id | disabled | remoteSearchSku | {"searchKeyField": "remoteSearchSku"} | /wms/warehouse/listOverseasWarehouse |
| 1762401438048194562 | 海外仓交货方式 | wms | /wms/common/enumDropDown?type=OverseasDeliveryMode | get | value | code | disabled |  |  | ?type=OverseasDeliveryMode |
| 1762404427609673729 | 海外仓入库状态 | wms | /wms/common/enumDropDown?type=OverseasInstockStatus | get | value | code | disabled |  |  | ?type=OverseasInstockStatus |
| 1762401230564364290 | 海外仓入库类型 | wms | /wms/common/enumDropDown?type=OverseasInstockType | get | value | code | disabled |  |  | ?type=OverseasInstockType |
| 1762405372682833922 | 海外仓物流方式 | wms | /wms/common/enumDropDown?type=LogisticsMethod | get | value | code | disabled |  |  | ?type=LogisticsMethod |
| 1772567906664583170 | 物流单获取运输方式 | wms | /wms/common/enumDropDown?type=LogisticsMethod | get | value | code | disabled |  |  | ?type=LogisticsMethod |
| 1760225160356433921 | 物流方式 | wms | /wms/common/enumDropDown?type=LogisticsMethod | get | value | code | disabled |  |  | ?type=LogisticsMethod |
| 1770342167718400002 | 物流状态 | wms | /wms/dict/list?key=fmDeliveryLogisticsStatus | get | name | value | disabled |  |  | ?key=fmDeliveryLogisticsStatus |
| 1959880763110146050 | 用途 | wms | /wms/dict/list?key=sampleUsage | get | name | value | disabled |  |  | ?key=sampleUsage |
| 1959880763110146053 | 用途范围 | wms | /wms/dict/list?key=sampleUsageScope | get | name | value | disabled |  |  | ?key=sampleUsageScope |
| 1762309582165643266 | 盘点单类型 | wms | /wms/dict/drop/down?type=stocktakingProfitLossType | get | name | code | disabled |  |  | ?type=stocktakingProfitLossType |
| 1871770457016979457 | 盘点方式 | wms | /wms/common/enumDropDown?type=StocktakingMode | get | value | code | disable |  |  | ?type=StocktakingMode |
| 1871771391449190402 | 盘点状态 | wms | /wms/common/enumDropDown?type=StocktakingStatus | get | value | code | disable |  |  | ?type=StocktakingStatus |
| 1871770660453306369 | 盘点类型 | wms | /wms/common/enumDropDown?type=StocktakingType | get | value | code | disable |  |  | ?type=StocktakingType |
| 1938416839916646401 | 第三方发货类型 | wms | /wms/dict/drop/down?type=thirdDeliveryType | get | name | code | disabled |  |  | ?type=thirdDeliveryType |
| 1762314531368865793 | 获取仓位 | wms | /wms/warehouseLocation/all | postJson | name | code | disabled |  |  | /wms/warehouseLocation/all |
| 2006208070032408577 | 获取仓位-关键词 | wms | /wms/warehouseLocation/searchByKeyword | get | name | code | disabled | keyword | {"searchKeyField": "keyword"} | /wms/warehouseLocation/searchByKeyword |
| 1742864320944873474 | 获取仓库（根据名称排序） | wms | /wms/warehouse/listOrderByName | get | name | id | disabled |  |  | /wms/warehouse/listOrderByName |
| 1747812425610199041 | 获取委外发料类型 | wms | /wms/dict/drop/down?type=issueType | get | name | code | disabled |  |  | ?type=issueType |
| 1772439213631868930 | 获取数据对比任务状态 | wms | /wms/common/enumDropDown?type=WmsDataCompareTaskStatus | get | value | code | disabled |  |  | ?type=WmsDataCompareTaskStatus |
| 1772439818949627905 | 获取数据对比系统单据 | wms | /wms/common/enumDropDown?type=WmsDataCompareTaskBillType | get | value | code | disabled |  |  | ?type=WmsDataCompareTaskBillType |
| 1787666018408075266 | 获取速卖通店铺 | wms | /wms/aliexpressDelivery/listUserAuthShop | get | shopName | shopId | disabled |  |  | /wms/aliexpressDelivery/listUserAuthShop |
| 1801263694038962178 | 虚拟仓分货单同步状态 | wms | /wms/dict/drop/down?type=vwAllocationSyncStatus | get | name | code | disabled |  |  | ?type=vwAllocationSyncStatus |
| 1801263614569484289 | 虚拟仓分货单状态 | wms | /wms/dict/drop/down?type=vwAllocationStatus | get | name | code | disabled |  |  | ?type=vwAllocationStatus |
| 1801262872597106690 | 虚拟仓分货单类型 | wms | /wms/dict/drop/down?type=vwAllocationType | get | name | code | disabled |  |  | ?type=vwAllocationType |
| 1840677070916296706 | 虚拟仓报表单据来源 | wms | /wms/dict/drop/down?type=virtualReportSourceType | get | name | code | disabled |  |  | ?type=virtualReportSourceType |
| 1868917296836239362 | 虚拟仓操作类型名称 | wms | /wms/dict/drop/down?type=virtualOperateTypeName | get | name | code | disable | {} | {"searchKeyField": "{}"} | ?type=virtualOperateTypeName |
| 1803965039556694017 | 虚拟库存流水来源类型 | wms | /wms/dict/drop/down?type=VirtualInventorySourceType | get | name | code | disabled |  |  | ?type=VirtualInventorySourceType |
| 1935219897332228098 | 虚拟库存状态 | wms | /wms/dict/drop/down?type=inventoryStatus | get | name | code | disabled |  |  | ?type=inventoryStatus |
| 1810528060924530690 | 装箱任务-单据类型 | wms | /wms/dict/drop/down?type=packingSourceType | get | name | code | disabled |  |  | ?type=packingSourceType |
| 1810528419378139138 | 装箱任务-称重状态-总箱 | wms | /wms/dict/drop/down?type=weightingStatus | get | name | code | disabled |  |  | ?type=weightingStatus |
| 1810527563505242113 | 装箱状态-总箱 | wms | /wms/dict/drop/down?type=packingStatus | get | name | code | disabled |  |  | ?type=packingStatus |
| 1760184279867854849 | 要货申请单据状态 | wms | /wms/common/enumDropDown?type=RequisitionApplicationStatus | get | value | code | disabled |  |  | ?type=RequisitionApplicationStatus |
| 1760158912167809025 | 要货申请要货类型 | wms | /wms/common/enumDropDown?type=RequisitionApplicationType | get | value | code | disabled |  |  | ?type=RequisitionApplicationType |
| 1762366306163625986 | 调拨方向 | wms | /wms/dict/drop/down?type=transferDirection | get | name | code | disabled |  |  | ?type=transferDirection |
| 1990307149536665618 | 调整类型 | wms | /wms/dict/list?key=adjustmentType | get | name | value | disabled |  |  | ?key=adjustmentType |
| 1784502767637762050 | 质检处理措施 | wms | /wms/dict/list?key=handleModeType | get | name | value | disabled |  |  | ?key=handleModeType |
| 1784500498741596162 | 质检状态 | wms | /wms/common/enumDropDown?type=QcBillStatus | get | value | code | disabled |  |  | ?type=QcBillStatus |
| 1920036555414589684 | 质检状态 | wms | /wms/common/enumDropDown?type=QcNoticeStatus | get | value | code | disabled |  |  | ?type=QcNoticeStatus |
| 1869941458271334402 | 质检类型 | wms | /wms/common/enumDropDown?type=QcType | get | value | code | disabled |  |  | ?type=QcType |
| 2036343175733456897 | 质检类型 | wms | /wms/dict/drop/down?type=qcType | get | name | code | disable |  |  | ?type=qcType |
| 1747908553483489282 | 退货方式 | wms | /wms/common/enumDropDown?type=ReturnMode | get | value | code | disabled |  |  | ?type=ReturnMode |
| 1747907831635382273 | 退货来源 | wms | /wms/common/enumDropDown?type=ReturnOrderSource | get | value | code | disabled |  |  | ?type=ReturnOrderSource |
| 1789901956551020546 | 采购收货单入库状态 | wms | /wms/dict/drop/down?type=poReceiveInStockStatus | get | name | code | disable | {} | {"searchKeyField": "{}"} | ?type=poReceiveInStockStatus |
| 1747905644846911490 | 采购退货单单据状态 | wms | /wms/dict/list?key=poReturnStatus | get | name | value | disabled |  |  | ?key=poReturnStatus |
| 1749644512755453954 | 采购退货单异常类型 | wms | /wms/common/enumDropDown?type=PoReturnUnusualType | get | value | code | disabled |  |  | ?type=PoReturnUnusualType |
| 1749643506869080066 | 采购退货单确认状态 | wms | /wms/common/enumDropDown?type=PoReturnConfirmStatus | get | value | code | disabled |  |  | ?type=PoReturnConfirmStatus |
| 1922825020363526396 | 单据类型 | workflow | /workflow/work/menu/drop/down | get | name | code | disabled |  |  | /workflow/work/menu/drop/down |
| 1922125177323814913 | 委托审批状态 | workflow | /workflow/dict/basic/drop/down?type=processDelegateStatus | get | name | code | disabled |  |  | ?type=processDelegateStatus |
| 1922825020363526395 | 审批分组 | workflow | /workflow/dict/basic/drop/down?type=approveGroup | get | name | code | disabled |  |  | ?type=approveGroup |
| 1922123045573001218 | 流程单据下拉 | workflow | /workflow/work/menu/drop/down | get | name | code | disabled |  |  | /workflow/work/menu/drop/down |
| 1922973552974295042 | 流程名称 | workflow | /workflow/process/definition/drop/down?type=push | get | name | code | disabled |  |  | ?type=push |
| 1927259775169200129 | 流程执行状态 | workflow | /workflow/dict/basic/drop/down?type=approveTaskStatus | get | name | code | disabled |  |  | ?type=approveTaskStatus |
| 1927299877970972674 | 流程来源 | workflow | /workflow/dict/basic/drop/down?type=sourcePlatform | get | name | code | disabled |  |  | ?type=sourcePlatform |
| 1925737387599650818 | 流程状态 | workflow | /workflow/dict/basic/drop/down?type=processStatus | get | name | code | disabled |  |  | ?type=processStatus |
| 1927260022037544962 | 流程类型 | workflow | /workflow/dict/basic/drop/down?type=approveTaskType | get | name | code | disabled |  |  | ?type=approveTaskType |
| 1927299707564789762 | 生成/更新配置 | workflow | /workflow/dict/basic/drop/down?type=operateType | get | name | code | disabled |  |  | ?type=operateType |

## 按模块速查

### dmp（10）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 1832018115441840129 | 中台推送状态 | ?type=DmpOutputTaskRecordStatus |
| 1832022881509167106 | 中台系统 | /dmp/dmpBasicSystem/listDmpBasicSystem |
| 2016087025393451010 | 中台系统谷云 | /dmp/dmpBasicSystem/listDmpBasicSystem |
| 1838065382399369218 | 查询所有输入配置 | /dmp/dmpCfgInput/allDmpCfgInput |
| 1803629715676729346 | 获取任务同步状态 | ?key=syncStatus |
| 1872192695474393090 | 获取单据同步状态 | ?type=DmpOutputTaskRecordStatus |
| 1988148930103894017 | 获取差异单据类型 | ?key=dictBillType |
| 1988149154746617858 | 获取差异标签 | ?key=dictDiffTag |
| 1803630742350073858 | 获取拉取任务单据类型 | ?key=pullSourceType |
| 1803629485564628994 | 获取推送任务单据类型 | ?key=pushSourceType |

### file（1）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 1829445922634346498 | 下载中心单据类型 | ?type=FileTaskEvent |

### fms（11）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 1980902471560339983 | 单据类型 | ?type=AssetProfitLossTypeEnum |
| 1980902471560339451 | 卡片来源 | ?key=cardSource |
| 1980902471560339455 | 处置情况 | ?key=disposalStatus |
| 1964970131239817308 | 审核状态 | /fms/drop/down/approveStatus/list |
| 1964970131239817400 | 审核状态 | /fms/drop/down/approveStatus/list |
| 1980902471560339454 | 审核状态 | /fms/drop/down/approveStatus/list |
| 1980902471560339981 | 盘点方案 | /fms/assetStocktakingPlan/dropDownList |
| 1980902471560339453 | 资产位置 | /fms/assetLocation/drop/down/list |
| 1985529808804665099 | 资产处置方式 | ?type=AssetDisposalDisposalMethod |
| 1980902471560339986 | 资产盘点表 | /fms/assetStocktaking/dropDownList |
| 1980902471560339452 | 资产类别 | ?key=assetCategory |

### mrp（5）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 1857041728750010370 | 补货建议平台类型 | ?type=CfgRulePlatformType |
| 1851206512162652161 | 补货建议数据类型 | ?type=CreateType |
| 1838130564031078401 | 补货建议标签 | /mrp/labelInfo/search/label |
| 1838498188099571713 | 补货建议标识查询 | ?type=suggestedMarkType |
| 1851197257560829953 | 补货建议状态 | ?type=SuggestStatus |

### oms（63）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 2011325939681894402 | B2B销售订单关联状态 | ?type=KolB2bRefStatus |
| 1778357812619907074 | B2C中转状态 | ?key=soB2cTransferStatus |
| 1778353521612034050 | B2C付款状态 | ?key=soB2cPayStatus |
| 1778356621932498946 | B2C异常信息 | ?key=soB2cAbnormalType |
| 1778357002477506561 | B2C异常订单类型 | ?key=b2cOrderErrorType |
| 1778357487196442625 | B2C组包状态 | ?key=soB2cPackageStatus |
| 1778354896173535233 | B2C订单分类 | /oms/orderCategory/list |
| 1783112970209857538 | B2C订单待处理类型 | ?key=SoB2cWaitHandleType |
| 1778356050781540353 | B2C订单标签 | ?key=soB2cLable |
| 1778353155952611330 | B2C订单状态 | ?key=soB2cBillStatus |
| 1912762635962269698 | NF-e发票状态 | ?type=SoB2cNfeStatus |
| 1760514025772290049 | OMS单据类型 | ?type=BillType |
| 1899364184284504066 | VAT发票状态 | ?type=SoB2cVatStatus |
| 1777599520310300673 | b2b销售订单单据类型 | ?key=soB2BBillType |
| 1802612036289331202 | b2c销售订单发货类型 | ?key=b2cOrderDeliveryType |
| 1845753231284609025 | b2c销售退货原因 | ?type=SoB2cReturnReason |
| 1845752900224000001 | b2c销售退货类型 | ?type=SoB2cReturnType |
| 1947549740291871937 | listingInfo平台状态 | ?type=ListingInfoPlatformStatus |
| 1827892644711706626 | 三方仓列表 | ?type=OmsPlatform |
| 1899303495230517249 | 上传状态类型 | ?key=uploadStatus |
| 1782247631460765698 | 下拉全部店铺 | /oms/shop/list |
| 1881516346628116481 | 作废类型 | ?key=invalidType |
| 1906634840376127489 | 全托管平台状态 | ?key=fullyManagedPlatformStatus |
| 1906639172408487937 | 全托管订单来源 | ?key=orderSourceType |
| 1958788190125260801 | 创建状态 | ?key=createStatus |
| 1988139364931194882 | 单据子类型 | ?type=OrderSubType |
| 1995673188139008003 | 发布形式 | ?key=publishType |
| 1899304480615133186 | 发票模板类型 | ?key=invoiceTemplateType |
| 1899303570505691138 | 发票状态类型 | ?key=invoiceStatus |
| 1899304433131417602 | 发票类型 | ?key=invoiceType |
| 1911734299764355074 | 发票账号下拉 | /oms/cfgInvoiceInvalid/getCompanyName |
| 1899303401970167809 | 发票配置类型 | ?key=cfgInvoiceType |
| 1995787832613951578 | 合作类型 | ?type=cooperationType |
| 1926921419541331970 | 头程调整字段类型 | ?key=firstMileCategoryFieldType |
| 1996489851283120130 | 寄样类型 | ?type=kolSampleType |
| 1930152562183393540 | 小程序销售平台 | ?key=miniProgramSalesPlatform |
| 1876256933315940353 | 库存SKU下拉 | /oms/listing/select/list |
| 1768450132191219713 | 店铺下拉 | /oms/shop/listAuth |
| 1795318754901495810 | 店铺授权状态 | ?type=AuthStatus |
| 1963412863100375042 | 授信类型 | ?key=creditType |
| 1962363239052607489 | 收款方式 | ?key=receiveMethod |
| 1995673188139008030 | 来源平台 | ?key=socialMediaPlatform |
| 1937725714494869505 | 海外仓平台 | ?type=OmsPlatformEnum |
| 1795594363602997250 | 禁用状态 | ?key=disabledStatus |
| 1767086563482669057 | 自动匹配类型 | ?type=SkuMappingRule |
| 2026960219825614900 | 获取TikTok店铺 | /oms/shop/getShopifyByPlatform |
| 1754025014362902529 | 获取亚马逊店铺 | /oms/shop/listShopByAmazon |
| 1753952456133316610 | 获取客户（下拉框） | /oms/customer/listEnable |
| 1778350878693003266 | 获取销售平台 | ?key=salesPlatform |
| 1753988202969960449 | 获取销售平台类型 | ?key=salesPlatform |
| 1995787832613951577 | 语言 | /oms/dictLanguage/drop/down |
| 1995673188139008020 | 费用名称 | ?type=costType |
| 1995673188139008002 | 达人昵称 | /oms/kolPartnerInfo/drop/down |
| 1995787832613951579 | 达人类型 | ?type=partnerType |
| 1972120405168500737 | 银行账号 | /oms/bankAccount/selectAll |
| 1797627231879565313 | 销售平台（国内） | ?type=salesPlatform |
| 1797627144902283266 | 销售平台（国外） | ?type=salesPlatform |
| 1996509701767977072 | 销售订单关联状态 | ?type=KolSubB2cApplicationOrderStatus |
| 1996509701767977073 | 销售订单关联状态 | ?type=KolSubB2cApplicationDeliveryStatus |
| 1762650623888592897 | 销售订单类型 | ?type=BillType |
| 1851164200174825473 | 销售退货原因 | ?type=ReturnReason |
| 1760514616300933121 | 销售退货类型 | /oms/soB2cReturn/getSoReturnType |
| 1996509701767977074 | 项目标签 | ?type=projectTag |

### plm（37）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 1816305334647930882 | SKU下拉 | /plm/product/detail/search/sku |
| 1873744152649216001 | plm产品任务状态 | /plm/task/getTaskStatusSelect?searchKeyword= |
| 1839481044783783937 | 产品分类 | /plm/category/tree |
| 2027326674634227713 | 产品变更字段 | /plm/productChange/getProductChangeFieldEnum |
| 1991069377072234498 | 产品品牌 | /plm/product/detail/listProductBrand |
| 1873934933351686146 | 产品标签 | /plm/basicLabel/list |
| 1872276268504252418 | 产品等级 | ?type=productGrade |
| 1762065932915576833 | 产品销售状态 | ?type=SaleState |
| 1873985413910097922 | 任务优先级 | /plm/task/getTaskPrioritySelect |
| 1959880763110146071 | 使用方 | /plm/common/findUserList |
| 1959880763110146080 | 使用方 | /plm/common/findUserList |
| 1897920976528625884 | 保险属性 | ?type=insuranceProperty |
| 1961251818584240132 | 创建人 | /plm/common/findUserList |
| 1961251818584240204 | 创建人 | /plm/common/findUserList |
| 1838509312517853186 | 品牌 | ?type=productBrand |
| 1961251818584240130 | 审核人 | /plm/common/findUserList |
| 1961251818584240203 | 审核人 | /plm/common/findUserList |
| 1879155632453337090 | 应用分类 | /plm/applicationCategory/list |
| 1991069377072234499 | 开发团队 | /plm/product/detail/listProductRDTTeam |
| 1959880763110146072 | 归属人 | /plm/common/findUserList |
| 1959880763110146081 | 归属人 | /plm/common/findUserList |
| 1961251818584240201 | 归属人 | /plm/common/findUserList |
| 1904509339083755750 | 新品首批 | ?type=firstMassProductType |
| 1865962091050786818 | 模具类型 | /plm/cfgMouldSetting/mouldList |
| 1959880763110146055 | 获取用户 | /plm/common/findUserList |
| 1995673188139008004 | 获取用户 | /plm/common/findUserList |
| 1742885076630179841 | 获取用户 | /plm/common/findUserList |
| 2001213973844975618 | 获取用户 | /plm/common/findUserList |
| 1871880740373024769 | 证书类型 | ?type=certificateType |
| 1871881430965178369 | 证书项目 | ?type=certificateProject |
| 1978390642597844689 | 返还标准 | ?type=CfgMoldReturnAlertRuleCountDim |
| 1978390642597844690 | 返还标准 | ?type=MoldMonitorReturnStatus |
| 1961251818584240131 | 退回人 | /plm/common/findUserList |
| 1872278410933788673 | 项目属性 | ?type=productProperty |
| 1873925575024787457 | 项目状态 | ?type=projectState |
| 1879378583576535041 | 项目进展 | /plm/product/getProgressStatus |
| 1873944236196323329 | 项目阶段 | /plm/task/phase/listPhaseName |

### scm（21）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 1742865001332285442 | scm获取到货状态 | /scm/drop/down/arrivalStatus/list |
| 1933366202189139969 | 付款条件 | ?type=paymentCondition |
| 2046543913989107714 | 供应商体系认证 | ?key=certificate |
| 1747815870534520833 | 供应商分类 | ?key=supplierCategory |
| 1747817166222135297 | 供应商等级 | /scm/supplier/grade/list |
| 1747817733711466497 | 供应商阶段 | /scm/drop/down/supplier/phase/list |
| 1871458762847473666 | 供应商阶段操作类型 | ?type=SupplierPhaseOperateType |
| 1760918359152529409 | 关联采购单状态 | /scm/drop/down/createPoType/list |
| 1921824840134709494 | 合同盖章状态 | ?type=ContractStampStatus |
| 1939534049882165521 | 合同类型 | ?key=contractType |
| 1948205707132163267 | 合同类型 | ?key=contractType |
| 2015667046764167170 | 委外订单类型 | ?key=subcontractOrderType |
| 1747910220232933377 | 执行状态 | ?key=executionStatus |
| 1938075877396992270 | 拜访结果 | ?type=SupplierVisitResult |
| 1933410334933014986 | 日均销量类型 | ?type=CfgSupplierSalesDailySalesType |
| 1937051917880416721 | 日均销量类型 | ?type=SupplierCredentialStatus |
| 1747823527035146241 | 结算方式 | ?key=supplierPayMode |
| 1742860958987919362 | 获取供应商 | /scm/drop/down/supplier/allList |
| 1742864822164201473 | 获取审核状态 | /scm/drop/down/approveStatus/list |
| 1748238796769681410 | 采购订单类型 | ?key=purchaseOrderType |
| 1933410334933014987 | 销量比例类型 | ?type=CfgSupplierSalesSalesRatioType |

### srm（5）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 1747513846660075521 | SRM发货单收货状态枚举 | ?type=ReceiptStatus |
| 1750083664919662593 | 对账明细来源类型 | ?key=poReconciliationSourceType |
| 1750076721375219713 | 获取对账单对账状态 | ?key=poReconciliationStatus |
| 1988077485304946691 | 送货单收货状态 | ?key=deliveryOrderReceiptStatus |
| 1933364711139233793 | 采购对账明细对账状态 | ?key=serachPoReconciliationDetailStatus |

### sys（25）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 1986605910903750657 | 中台监控单据类型 | ?type=sourceType |
| 1809067559207870465 | 其他出库单-业务类型 | ?typeName=其他出库单业务类型 |
| 1809067479931330562 | 其他出库单-出库类型 | ?typeName=其他出库单类型 |
| 1876445030684053506 | 军区列表 | /sys/dictPartition/drop/down |
| 2016700620313743361 | 区域下拉 | /sys/dict/global/area/list |
| 1982693025867481090 | 单据业务类型 | ?type=sourceType |
| 2009117155366295721 | 发货通知变更单类型 | ?type=SysUserInfoThirdAuthType |
| 1985529808804665100 | 币种 | /sys/currency/list |
| 2039227404495462401 | 币种(显示ID) | /sys/currency/list |
| 1961251818584240202 | 归属部门 | /sys/department/drop/down |
| 1873652875718873089 | 用户角色 | /sys/role/list |
| 1879469244741140481 | 用户角色ids | /sys/role/list |
| 1926933548095262975 | 第三方通知单据类型 | ?type=thirdNoticeBusinessType |
| 1983055108086128641 | 系统代码 | ?type=SystemCode |
| 1753954793224671233 | 获取国家 | /sys/dict/country/list |
| 1753954793224671234 | 获取国家(带默认) | /sys/dict/country/listWithDefault |
| 1865304737355427841 | 获取国家名称 | /sys/dict/country/list |
| 1747800607470653442 | 获取系统用户是否绑定微信 | ?type=is_bind_wechat |
| 1747166094306578433 | 获取系统用户状态 | ?type=sys_user_state |
| 1747893210916749314 | 获取组织 | /sys/company/list |
| 1980902471560339450 | 获取组织 | /sys/company/list |
| 2058721633179127810 | 获取组织名称 | /sys/company/list |
| 1753953984822906881 | 获取部门（下拉框） | /sys/department/drop/down |
| 1959880763110146052 | 部门 | /sys/department/drop/down |
| 1933470858403954690 | 部门级联 | /sys/department/cascadeTree |

### tms（43）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 1866446715808272385 | b2c对账状态 | ?type=TmsB2cDeclareReconciliationStatus |
| 1783701677866487809 | 中转渠道级联 | /tms/transferLogisticsSupplier/tree |
| 1798306321519349762 | 中转状态-物流商 | ?key=transferStatus |
| 1798307442237378561 | 入库预报状态 | ?key=instockForecastStatus |
| 1798305700368093185 | 出库状态 | ?key=transferOutstockStatus |
| 1816377575825973250 | 发货类型-TMS | ?key=shipmentType |
| 1871878501281521665 | 地址类型 | ?type=LogisticsAddressType |
| 1948644446006562818 | 头程对账单类型 | ?key=firstSupplierType |
| 1772567505542320129 | 头程物流单获取发票状态 | ?type=InvoicesStatus |
| 1772567660639293442 | 头程物流单获取对账状态 | ?type=ReconciliationStatus |
| 1772567238075748354 | 头程物流单获取物流状态 | ?type=FmLogisticTrackStatus |
| 1865305927543713794 | 小包分摊费用分摊 | ?type=CostAllocation |
| 1865302297495236609 | 小包分摊费用来源 | ?key=smallBagFeeSource |
| 1865326252675330050 | 小包重量分摊方式 | ?type=WeightAllocationSmallBag |
| 1866015904524898306 | 小包重量大表状态 | ?type=SmallBagCostAllocationMainBigTableStatus |
| 1866015765408223234 | 小包重量核算状态 | ?type=SmallBagCostAllocationMainReportStatus |
| 2049666606199492610 | 报关单状态 | ?type=DeclareStatus |
| 1774637641103314946 | 报关单获取报关类型 | ?key=declareDeclareType |
| 1872460427956805633 | 报关物流商授权状态 | ?key=transferLogisticsAuthStatus |
| 1769982169465229313 | 查询中转物流商 | /tms/transferLogisticsSupplier/listAll |
| 1769985454695780354 | 查询产品备案状态 | ?type=Status |
| 1772808062134915074 | 核对类型 | ?type=CfgReconciliationType |
| 1828248289629798402 | 核算期间下拉列表 | /tms/reportPeriodMonth/listLocalDate |
| 1768455056119566337 | 渠道下拉 | /tms/logisticsChannel/listAll |
| 2046129612890592746 | 渠道下拉 | /tms/logisticsChannel/listWithAll |
| 1788739005089583106 | 物流单运输状态 | ?key=logisticTrackStatus |
| 1769908748240818177 | 物流商下拉 | /tms/logisticsSupplier/listAll |
| 2043962114653049079 | 物流商下拉(简称) | /tms/logisticsSupplier/listAllShort |
| 2046129612890592745 | 物流商下拉(简称) | /tms/logisticsSupplier/listWithAll |
| 1778719803935035394 | 物流渠道级联 | /tms/logisticsChannel/tree |
| 2026918290210706900 | 物流系统任务单据类型 | ?type=TmsAsyncTaskRecordBusinessType |
| 1869672878375919618 | 物流费用单据类型 | ?type=CostBillType |
| 1789914150890115073 | 物流费用对账状态 | ?key=reconciliationStatus |
| 2026918290210706899 | 状态 | ?type=TmsAsyncTaskRecordStatus |
| 1864626097244135426 | 自发货费用支付状态 | ?type=LogisticsBillCostPayStatus |
| 1798304173486247937 | 订单上传状态 | ?key=transferDeclareUploadStatus |
| 1816376967828054018 | 订单类型-TMS | ?key=salesOrderType |
| 1828250731448729601 | 账单来源 | ?key=billSourceType |
| 1828251561027534850 | 费用分类 | ?key=dictCostCategory |
| 1949731277506969602 | 费用归属 | ?key=dictCostAttribution |
| 2013885652633933262 | 费用配置--配置单据 | ?key=cfgCostBusinessKey |
| 1952221620488400898 | 轨迹推送类型 | ?key=trackPushType |
| 1952220893116395522 | 轨迹服务商 | ?key=trackPlatformType |

### wms（107）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 1996121046274809857 | B2B三方发货单单据类型 | ?type=thirdDeliveryStatus |
| 1754025662391259138 | FBA发货状态 | ?type=FbaDeliveryStatus |
| 1762024591724646402 | WMS发货状态 | ?type=DeliveryStatus |
| 1768453576897597442 | b2c发货单拣货类型 | ?type=PickingType |
| 1768557895831523330 | b2c发货单物流类型 | ?type=b2cDeliveryLogisticType |
| 1768452674581172226 | b2c发货单状态 | ?type=SoB2cDeliveryStatus |
| 1754027674801541122 | fba平台货件状态 | ?type=FbaPlatformShipmentStatus |
| 2034112170870812673 | fbt平台货件状态 | ?type=FbtPlatformShipmentStatus |
| 2036343441480364033 | 一般缺陷AQL | ?type=generalDefectAql |
| 1783396399870644225 | 上传状态 | ?type=packageForecastUploadStatus |
| 1914282002903748842 | 上架状态 | ?type=PutawayStatus |
| 2036343542743445505 | 严重缺陷AQL | ?type=majorDefectAql |
| 1961251818584240133 | 交货仓库 | /wms/warehouse/list |
| 1996122759081762818 | 交货方式 | ?type=deliveryMethod |
| 1797470828225368065 | 仓位状态字典 | ?type=warehouseAreaType |
| 2059534686716338178 | 仓位移动操作类型 | ?type=WarehouseLocationMoveOperateType |
| 1860879818901192705 | 仓位移动来源类型 | ?type=MarehouseMoveSourceType |
| 1959880763110146051 | 仓库 | /wms/warehouse/list |
| 2058721052234469377 | 仓库名称下拉 | /wms/warehouse/list |
| 1798612747626442753 | 仓库库存状态 | ?type=InventoryStatus |
| 1798611409198870529 | 仓库操作类型 | ?type=InventoryOperationMode |
| 1996121069712580609 | 仓库操作类型 | ?type=warehouseOperationType |
| 1871501884889550849 | 仓库类型 | ?key=warehouseType |
| 1871510335543889921 | 仓库经营类型 | ?type=warehouseManageType |
| 1899354823365533697 | 入库类型 | ?type=instockType |
| 1872210324297662466 | 出入库单据类型 | ?type=InventorySourceType |
| 1871771082979102722 | 分单规则 | ?type=SeparateRule |
| 1755056191752376322 | 加工单事务类型 | ?type=workType |
| 1765269036247027713 | 加工单来源类型 | ?type=MachineSourceType |
| 1868542168394650026 | 单据下推类型 | ?type=billPushDownStatus |
| 1961251818584240134 | 单据状态 | /wms/drop/down/approveStatus/list |
| 1961251818584240200 | 单据状态 | /wms/drop/down/approveStatus/list |
| 1790687710097182722 | 发货计划单据类型 | ?type=deliveryPlanType |
| 1856584184717701122 | 发货通知变更单类型 | ?type=SoDeliveryNoticeChangeType |
| 1871510592197545986 | 地理位置 | ?type=geographyLocation |
| 1783395903109861377 | 大包交接状态 | ?type=packageHandoverStatus |
| 1760223839335223298 | 头程发货备货类型 | ?type=FbaDemandType |
| 1760224942701416449 | 头程发货装箱状态 | ?type=PackingStatus |
| 1959880763110146049 | 审核状态 | /wms/drop/down/approveStatus/list |
| 1783396296690765825 | 小包交接状态 | ?type=packageSubHandoverStatus |
| 1805887014982127617 | 库区 | /wms/warehouse-area/listAllArea |
| 1796375416324231170 | 库区类型 | ?type=warehouseAreaType |
| 1762011801026826241 | 库存方向 | ?type=inventoryDirection |
| 1749646048415977474 | 异常处理人下拉 | /wms/purchaseReturnOrder/unusualHandleUserOption |
| 1783399374672367617 | 打印状态 | ?type=PackagePrintStatus |
| 1959880763110146054 | 执行状态 | ?key=executionStatus |
| 1770342079650598913 | 报关状态 | ?key=fmDeliveryDeclareStatus |
| 1811357043509731329 | 拣货车下拉 | /wms/pickingCart/dropDown |
| 1805078184169836546 | 拣货车类型下拉 | /wms/pickingCartType/select |
| 1996127189130813442 | 推送类型 | ?type=deliveryPushType |
| 1959880763110146083 | 操作类型 | ?key=dictBizType |
| 1980902471560339771 | 操作类型 | ?key=sampleLedgerFlowSourceType |
| 2036343634166689794 | 文件类型 | ?type=fileType |
| 2036343282763706370 | 方案类型 | ?type=planType |
| 1815965527446925313 | 查询虚拟仓库列表 | /wms/virtualWarehouse/list |
| 2036343360895201281 | 检验水平 | ?type=qcLevel |
| 1809947974290542594 | 模糊查询拣货车 | /wms/pickingCart/searchByKeyword |
| 1805509888042864641 | 波次类型 | ?type=waveType |
| 1876566805787181058 | 海外/平台绑定仓库 | /wms/warehouse/listOverseasWarehouse |
| 1762404952099000322 | 海外中转仓 | /wms/overseasWarehouseInbound/transferWareHouseList |
| 1876259669067501570 | 海外仓下拉 | /wms/warehouse/listOverseasWarehouse |
| 1762401438048194562 | 海外仓交货方式 | ?type=OverseasDeliveryMode |
| 1762404427609673729 | 海外仓入库状态 | ?type=OverseasInstockStatus |
| 1762401230564364290 | 海外仓入库类型 | ?type=OverseasInstockType |
| 1762405372682833922 | 海外仓物流方式 | ?type=LogisticsMethod |
| 1772567906664583170 | 物流单获取运输方式 | ?type=LogisticsMethod |
| 1760225160356433921 | 物流方式 | ?type=LogisticsMethod |
| 1770342167718400002 | 物流状态 | ?key=fmDeliveryLogisticsStatus |
| 1959880763110146050 | 用途 | ?key=sampleUsage |
| 1959880763110146053 | 用途范围 | ?key=sampleUsageScope |
| 1762309582165643266 | 盘点单类型 | ?type=stocktakingProfitLossType |
| 1871770457016979457 | 盘点方式 | ?type=StocktakingMode |
| 1871771391449190402 | 盘点状态 | ?type=StocktakingStatus |
| 1871770660453306369 | 盘点类型 | ?type=StocktakingType |
| 1938416839916646401 | 第三方发货类型 | ?type=thirdDeliveryType |
| 1762314531368865793 | 获取仓位 | /wms/warehouseLocation/all |
| 2006208070032408577 | 获取仓位-关键词 | /wms/warehouseLocation/searchByKeyword |
| 1742864320944873474 | 获取仓库（根据名称排序） | /wms/warehouse/listOrderByName |
| 1747812425610199041 | 获取委外发料类型 | ?type=issueType |
| 1772439213631868930 | 获取数据对比任务状态 | ?type=WmsDataCompareTaskStatus |
| 1772439818949627905 | 获取数据对比系统单据 | ?type=WmsDataCompareTaskBillType |
| 1787666018408075266 | 获取速卖通店铺 | /wms/aliexpressDelivery/listUserAuthShop |
| 1801263694038962178 | 虚拟仓分货单同步状态 | ?type=vwAllocationSyncStatus |
| 1801263614569484289 | 虚拟仓分货单状态 | ?type=vwAllocationStatus |
| 1801262872597106690 | 虚拟仓分货单类型 | ?type=vwAllocationType |
| 1840677070916296706 | 虚拟仓报表单据来源 | ?type=virtualReportSourceType |
| 1868917296836239362 | 虚拟仓操作类型名称 | ?type=virtualOperateTypeName |
| 1803965039556694017 | 虚拟库存流水来源类型 | ?type=VirtualInventorySourceType |
| 1935219897332228098 | 虚拟库存状态 | ?type=inventoryStatus |
| 1810528060924530690 | 装箱任务-单据类型 | ?type=packingSourceType |
| 1810528419378139138 | 装箱任务-称重状态-总箱 | ?type=weightingStatus |
| 1810527563505242113 | 装箱状态-总箱 | ?type=packingStatus |
| 1760184279867854849 | 要货申请单据状态 | ?type=RequisitionApplicationStatus |
| 1760158912167809025 | 要货申请要货类型 | ?type=RequisitionApplicationType |
| 1762366306163625986 | 调拨方向 | ?type=transferDirection |
| 1990307149536665618 | 调整类型 | ?key=adjustmentType |
| 1784502767637762050 | 质检处理措施 | ?key=handleModeType |
| 1784500498741596162 | 质检状态 | ?type=QcBillStatus |
| 1920036555414589684 | 质检状态 | ?type=QcNoticeStatus |
| 1869941458271334402 | 质检类型 | ?type=QcType |
| 2036343175733456897 | 质检类型 | ?type=qcType |
| 1747908553483489282 | 退货方式 | ?type=ReturnMode |
| 1747907831635382273 | 退货来源 | ?type=ReturnOrderSource |
| 1789901956551020546 | 采购收货单入库状态 | ?type=poReceiveInStockStatus |
| 1747905644846911490 | 采购退货单单据状态 | ?key=poReturnStatus |
| 1749644512755453954 | 采购退货单异常类型 | ?type=PoReturnUnusualType |
| 1749643506869080066 | 采购退货单确认状态 | ?type=PoReturnConfirmStatus |

### workflow（10）

| ID | 名称 | 匹配关键词 |
| --- | --- | --- |
| 1922825020363526396 | 单据类型 | /workflow/work/menu/drop/down |
| 1922125177323814913 | 委托审批状态 | ?type=processDelegateStatus |
| 1922825020363526395 | 审批分组 | ?type=approveGroup |
| 1922123045573001218 | 流程单据下拉 | /workflow/work/menu/drop/down |
| 1922973552974295042 | 流程名称 | ?type=push |
| 1927259775169200129 | 流程执行状态 | ?type=approveTaskStatus |
| 1927299877970972674 | 流程来源 | ?type=sourcePlatform |
| 1925737387599650818 | 流程状态 | ?type=processStatus |
| 1927260022037544962 | 流程类型 | ?type=approveTaskType |
| 1927299707564789762 | 生成/更新配置 | ?type=operateType |
