package com.common.business.enums;

import com.common.business.constant.BusinessNoConstant;
import com.common.business.constant.RedisCacheConstants;
import lombok.AllArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: 系统编码
 * @date 2022/11/22 10:26
 */
@AllArgsConstructor
public enum BusinessNoTypeEnum {

    SKU_NO("1", "sku_no","sku编号",""),
    SPU_NO("2", "spu_no","spu编号",""),
    CODE_USER("15", "user_no","用户编号", ""),


    Bom_NO("bom_no", "bom_no","bom编号", BusinessNoConstant.BOM),
    CODE_BH("bh", "bh","备货申请单编号", BusinessNoConstant.BH),
    CODE_GYS("gys", "gys","供应商编号", BusinessNoConstant.GYS),
    CODE_PL("pl", "pl","采购申请单编号", BusinessNoConstant.PL),
    CODE_CGJM("cgjm", "cgjm","采购价目编号", BusinessNoConstant.CGJM),
    CODE_CGTJ("cgtj", "cgtj","采购价目调价编号", BusinessNoConstant.CGTJ),
    CODE_PO("po", "po","采购订单", BusinessNoConstant.PO),
    CODE_POC("poc", "poc","采购变更单", BusinessNoConstant.POC),
    CODE_ZJGZ("zjgz", "zjgz","质检规则单", BusinessNoConstant.ZJGZ),
    CODE_CGSH("cgsh", "cgsh","采购收货单", BusinessNoConstant.CGSH),
    CODE_CGRK("cgrk", "cgrk","采购入库单", BusinessNoConstant.CGRK),
    CODE_CGTH("cgth", "cgth","采购退货单", BusinessNoConstant.CGTH),
    CODE_QC("qc", "qc","质检单", BusinessNoConstant.QC),
    CODE_DEPT("bm", "bm","部门编号", BusinessNoConstant.BM),
    CODE_RKYB("rkyb", "rkyb","入库预报", BusinessNoConstant.RKYB),
    CODE_DBSQ("dbsq", "dbsq","调拨申请", BusinessNoConstant.DBSQ),
    CODE_CUST("cust", "cust","客户", BusinessNoConstant.CUST),
    CODE_INIT_STOCK("init_stock", "init_stock","期初库存", BusinessNoConstant.QCKC),
    CODE_THDD("thdd", "thdd","销售退货订单", BusinessNoConstant.THDD),
    CODE_THTZ("thtz", "thtz","销售退货通知单", BusinessNoConstant.THTZ),
    CODE_XSD("xsd", "xsd","销售单", BusinessNoConstant.XSD),
    CODE_ZJDB("zjdb", "zjdb","直接调拨单",  BusinessNoConstant.ZJDB),
    CODE_ZZCX("zzcx", "zzcx","加工单", BusinessNoConstant.ZZCX),
    CODE_THQS("thqs", "thqs","退货签收单", BusinessNoConstant.THQS),
    CODE_XSCK("xsck", "xsck","销售出库单",  BusinessNoConstant.XSCK),
    CODE_QTRK("qtrk", "qtrk","其他入库单", BusinessNoConstant.QTRK),
    CODE_QTCK("qtck", "qtck","其他出库单", BusinessNoConstant.QTCK),
    CODE_XSBG("xsbg", "xsbg","销售变更单", BusinessNoConstant.XSBG),
    CODE_FHTZ("fhtz", "fhtz","发货通知单", BusinessNoConstant.FHTZ),
    CODE_FBDC("fbdc", "fbdc","分步式调拨调出", BusinessNoConstant.FBDC),
    CODE_FBDR("fbdr", "fbdr","分步式调拨调入", BusinessNoConstant.FBDR),
    CODE_XSTH("xsth", "xsth","销售退货入库单", BusinessNoConstant.XSTH),
    CODE_KHDZ("khdz", "khdz","客户地址编号", BusinessNoConstant.KHDZ),
    CODE_KHLXR("khlxr", "khlxr","客户联系人编号", BusinessNoConstant.KHLXR),
    CODE_SUB("sub", "sub","委外订单", BusinessNoConstant.SUB),
    CODE_SUBCH("subch", "subch","委外变更单", BusinessNoConstant.SUBCH),
    STOCKTAKING_PLAN("pdjh", "pdjh","盘点计划",BusinessNoConstant.PDJH),
    STOCKTAKING_TASK("pdrw", "pdrw","盘点任务",BusinessNoConstant.PDRW),
    STOCKTAKING_PROFIT("pyd", "pyd","盘盈单",BusinessNoConstant.PYD),
    STOCKTAKING_LOSS("pkd", "pkd","盘盈单",BusinessNoConstant.PKD),
    CODE_SO_B2C("xsds", "xsds","b2c销售订单",BusinessNoConstant.XSDS),
    CODE_CWYD("cwyd", "cwyd","仓位移动",BusinessNoConstant.CWYD),
    CODE_KHDZC("khdzc", "khdzc","b2c客户地址编号",BusinessNoConstant.KHDZC),
    CODE_KHLXRC("khlxrc", "khlxrc","b2c客户联系人编号",BusinessNoConstant.KHLXRC),
    CODE_CUSTC("custc", "custc","客户",BusinessNoConstant.CUSTC),
    CODE_FBAS("fbas", "fbas","FBA货件",BusinessNoConstant.FBAS),
    CODE_FHD("fhd", "fhd","头程发货单",BusinessNoConstant.FHD),
    CODE_XSDD("xsdd", "xsdd","b2c销售订单(平台)",BusinessNoConstant.XSDD),
    CODE_FHJH("fhjh", "fhjh","海外仓发货计划",BusinessNoConstant.FHJH),
    CODE_YHSQ("yhsq", "yhsq","要货申请",BusinessNoConstant.YHSQ),
    CODE_FHDC("fhdc", "fhdc","b2c发货单",BusinessNoConstant.FHDC),
    CODE_FHLJ("fhlj", "fhlj","b2c发货拦截单",BusinessNoConstant.FHLJ),
    CODE_FLD("fld", "fld","委外发料单",BusinessNoConstant.FLD),
    CODE_SHD("shd", "shd","送货单",BusinessNoConstant.SHD),
    CODE_DZD("dzd", "dzd","对账单",BusinessNoConstant.DZD),
    CODE_ZZBG("zzbg", "zzbg","中转报关",BusinessNoConstant.ZZBG),
    CODE_ZB("zb", "zb","组包预报",BusinessNoConstant.ZB),

    CODE_SJDB("sjdb", "sjdb","数据对比","SJDB"),
    CODE_BGZD("bgzd", "bgzd","报关账单","BGZD"),
    CODE_TCZD("tczd", "tczd","头程账单","TCZD"),
    CODE_XNC("xnc", "xnc","虚拟仓", BusinessNoConstant.XNC),
    CODE_XLS("xls", "xls","虚拟仓库存流水", BusinessNoConstant.XLS),
    CODE_FH("FH", "FH","虚拟仓分货单", BusinessNoConstant.FH),

    CODE_DQDD("DQDD", "DQDD","多渠道订单(平台)",BusinessNoConstant.DQDD),
    ZXRW("ZXRW", "ZXRW","装箱任务",BusinessNoConstant.ZXRW),
    CODE_JHD("JHD", "JHD","拣货单",BusinessNoConstant.JHD),
    CODE_JHC("JHC", "JHC","拣货车管理",BusinessNoConstant.JHC),
    CODE_JHBC("JHBC", "JHBC","拣货波次",BusinessNoConstant.JHBC),
    CODE_QCFT("QCFT", "QCFT","期初头程分摊",BusinessNoConstant.QCFT),
    CODE_CHCB("CHCB", "CHCB","SKU成本分摊",BusinessNoConstant.CHCB),
    CODE_SCLC("SCLC", "SCLC","试产量产单",BusinessNoConstant.SCLC),
    THD("THD", "THD","B2C退货单",BusinessNoConstant.THD),

    CODE_TLD("tld", "tld","委外发退料单",BusinessNoConstant.TLD),
    CODE_FHBG("FHBG", "FHBG","发货通知变更单",BusinessNoConstant.FHBG),
    CODE_JSRQ("JSRQ", "JSRQ","计算日期",BusinessNoConstant.JSRQ),
    CODE_S("S", "S","发货建议",BusinessNoConstant.S),
    CODE_P("P", "P","采购建议",BusinessNoConstant.P),
    CODE_PP("PP", "PP","采购建议",BusinessNoConstant.PP),
    CODE_FHJY("FHJY", "FHJY","发货建议",BusinessNoConstant.FHJY),
    CODE_CGJY("CGJY", "CGJY","采购建议",BusinessNoConstant.CGJY),

    CODE_TKD("TKD", "TKD","退款单",BusinessNoConstant.TKD),
    CODE_XLSS("XLSS", "XLSS","销量试算",BusinessNoConstant.XLSS),
    CODE_YHBG("YHBG", "YHBG","要货申请变更单",BusinessNoConstant.YHBG),
    CODE_MOLD("MJ", "MJ","模具管理",BusinessNoConstant.MJ),
    CODE_N("N", "N","库龄批次号",BusinessNoConstant.N),
    CODE_INV("INV", "INV","发票号",BusinessNoConstant.INV),
    CODE_XSBH("XSBH", "XSBH","全托管订单",BusinessNoConstant.XSBH),
    CODE_SHSQ("SHSQ", "SHSQ","售后",BusinessNoConstant.SHSQ),

    CODE_XSJM("xsjm", "xsjm","销售价目编号", BusinessNoConstant.XSJM),
    CODE_XSTJ("xstj", "xstj","销售价目调价编号", BusinessNoConstant.XSTJ),
    CODE_ZJTZ("ZJTZ", "ZJTZ","质检通知",BusinessNoConstant.ZJTZ),
    CODE_TCTZ("TCTZ", "TCTZ","头程调整记录",BusinessNoConstant.TCTZ),
    CODE_XNKC("XNKC", "XNKC","虚拟库存调整",BusinessNoConstant.XNKC),
    CODE_GYSHT("GYSHT", "GYSHT","合同管理编码",BusinessNoConstant.GYSHT),

    CODE_LCWT("LCWT", "LCWT","委托审批",BusinessNoConstant.LCWT),
    CODE_SFSP("SFSP", "SFSP","ERP审批同步配置",BusinessNoConstant.SFSP),
    CODE_LCPZ("LCPZ", "LCPZ","流程配置",BusinessNoConstant.LCPZ),
    CODE_WFHD("whud", "whud","b2c三方仓发货单",BusinessNoConstant.WFHD),
    CODE_SFSC("SFSC", "SFSC","三方审批生成配置",BusinessNoConstant.SFSC),
    CODE_MB("MB", "MB","模板管理",BusinessNoConstant.MB),
    CODE_GYSDM("GYSDM", "GYSDM","供应商代码", BusinessNoConstant.GYSDM),
    CODE_YPLY("YPLY", "YPLY","样品领用", BusinessNoConstant.YPLY),

    CODE_YPZF("YPBF", "YPBF","样品作废单", BusinessNoConstant.YPBF),
    CODE_YPJY("YPJY", "YPJY","样品借用单", BusinessNoConstant.YPJY),
    CODE_YPGH("YPGH", "YPGH","样品归还单", BusinessNoConstant.YPGH),
    CODE_ZHXS("ZHXS", "ZHXS","展会订单", BusinessNoConstant.ZHXS),
    CODE_YPTH("YPTH", "YPTH","样品退回单", BusinessNoConstant.YPTH),
    CODE_QCTZ("QCTZ", "QCTZ","期初台账", BusinessNoConstant.QCTZ),
    CODE_SKD("SKD", "SKD","收款单", BusinessNoConstant.SKD),
    CODE_KHSX("KHSX", "KHSX","客户授信", BusinessNoConstant.KHSX),

    CODE_ZCWZ("ZCWZ", "ZCWZ","资产位置", BusinessNoConstant.ZCWZ),
    CODE_YSD("YSD", "YSD","资产验收单", BusinessNoConstant.YSD),
    CODE_ZCKP("ZCKP", "ZCKP","资产卡片", BusinessNoConstant.ZCKP),
    CODE_PDFA("PDFA", "PDFA","盘点方案", BusinessNoConstant.PDFA),
    CODE_ZCPDB("ZCPDB", "ZCPDB","资产盘点表", BusinessNoConstant.ZCPDB),
    CODE_PYPKD("PYPKD", "PYPKD","盘盈盘亏单", BusinessNoConstant.PYPKD),
    CODE_PRODIS("PRODIS", "PRODIS","资产处置单", BusinessNoConstant.PRODIS),

    CODE_MOLD_REF_SKU("MRS", "MRS","模具关联SKU",BusinessNoConstant.MRS),
    CODE_ZBJH("ZBJH", "ZBJH","组包计划", BusinessNoConstant.ZBJH),
    CODE_YPZY("YPZY", "YPZY","样品转移单", BusinessNoConstant.YPZY),
    CODE_YPTZ("YPTZ", "YPTZ","样品调整单", BusinessNoConstant.YPTZ),

    CODE_DC("DC", "DC","资质字典表",BusinessNoConstant.DC),
    CODE_CYCL("HD", "HD","差异策略配置表",BusinessNoConstant.CYCL),
    CODE_SFFH("SFFH", "SFFH","B2B三方发货单",BusinessNoConstant.SFFH),
    CODE_AWD("AWD", "AWD","AWD出库货件",BusinessNoConstant.AWD),
    CODE_MPL("MPL", "MPL","资产通知单",BusinessNoConstant.MPL),
    CODE_MPO("MPO", "MPO","资产采购单",BusinessNoConstant.MPO),
    CODE_MPOCC("MPOCC", "MPOCC","资产采购变更单",BusinessNoConstant.MPOCC),
    CODE_ZC("ZC", "ZC","资产编码",BusinessNoConstant.ZC),

    CODE_HP("HP", "HP","回片列表",BusinessNoConstant.HP),
    CODE_KOLB("KOLB", "KOLB","B2B寄样申请单",BusinessNoConstant.KOLB),


    CODE_DR("DR", "DR","企业达人库",BusinessNoConstant.DR),
    CODE_KOLC("KOLC", "KOLC","B2C寄样申请单",BusinessNoConstant.KOLC),
    CODE_DZ("DZ", "DZ","物流费用导入",BusinessNoConstant.DZ),
    CODE_FYPZ("FYPZ", "FYPZ","费用配置",BusinessNoConstant.FYPZ),

    CODE_BG("BG", "BG","产品信息变更单",BusinessNoConstant.BG),
    CODE_ZJSQ("ZJSQ", "ZJSQ","质检申请",BusinessNoConstant.ZJSQ),

    CODE_CYFA("CYFA", "CYFA","抽样方案",BusinessNoConstant.CYFA),
    CODE_WDGL("WDGL", "WDGL","文件管理",BusinessNoConstant.WDGL),

    ;




    private String code;
    private String name;
    private String desc;

    // 单号前缀
    private String prefix;

    // redis单号key
    public static final String REDIS_GEN_KEY = RedisCacheConstants.REDIS_GEN_KEY;

    // 补0位数
    public static final Integer FILL_0_DIGIT = 5;


    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }


    public static String getCodeByName(String name) {
        BusinessNoTypeEnum[] businessNoTypeEnums = values();
        for (BusinessNoTypeEnum businessNoTypeEnum : businessNoTypeEnums) {
            if (businessNoTypeEnum.getName().equals(name)) {
                return businessNoTypeEnum.getCode();
            }
        }
        return null;
    }
}

