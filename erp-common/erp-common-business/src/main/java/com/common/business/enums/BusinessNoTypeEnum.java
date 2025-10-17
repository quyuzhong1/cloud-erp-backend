package com.common.business.enums;

import com.common.business.constant.BusinessNoConstant;
import lombok.AllArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: 系统编码
 * @date 2022/11/22 10:26
 */
@AllArgsConstructor
public enum BusinessNoTypeEnum {

    SKU_NO(1, "sku_no","sku编号",""),
    SPU_NO(2, "spu_no","spu编号",""),
    Bom_NO(3, "bom_no","bom编号", BusinessNoConstant.BOM),
    CODE_BH(4, "bh","备货申请单编号", BusinessNoConstant.BH),
    CODE_GYS(5, "gys","供应商编号", BusinessNoConstant.GYS),
    CODE_PL(6, "pl","采购申请单编号", BusinessNoConstant.PL),
    CODE_CGJM(7, "cgjm","采购价目编号", BusinessNoConstant.CGJM),
    CODE_CGTJ(8, "cgtj","采购价目调价编号", BusinessNoConstant.CGTJ),
    CODE_PO(9, "po","采购订单", BusinessNoConstant.PO),
    CODE_POC(10, "poc","采购变更单", BusinessNoConstant.POC),
    CODE_ZJGZ(11, "zjgz","质检规则单", BusinessNoConstant.ZJGZ),
    CODE_CGSH(12, "cgsh","采购收货单", BusinessNoConstant.CGSH),
    CODE_CGRK(13, "cgrk","采购入库单", BusinessNoConstant.CGRK),
    CODE_CGTH(14, "cgth","采购退货单", BusinessNoConstant.CGTH),
    CODE_USER(15, "user_no","用户编号", ""),
    CODE_QC(16, "qc","质检单", BusinessNoConstant.QC),
    CODE_DEPT(17, "bm","部门编号", BusinessNoConstant.BM),
    CODE_RKYB(18, "rkyb","入库预报", BusinessNoConstant.RKYB),
    CODE_DBSQ(19, "dbsq","调拨申请", BusinessNoConstant.DBSQ),
    CODE_CUST(20, "cust","客户", BusinessNoConstant.CUST),
    CODE_INIT_STOCK(21, "init_stock","期初库存", BusinessNoConstant.QCKC),
    CODE_THDD(22, "thdd","销售退货订单", BusinessNoConstant.THDD),
    CODE_THTZ(23, "thtz","销售退货通知单", BusinessNoConstant.THTZ),
    CODE_XSD(24, "xsd","销售单", BusinessNoConstant.XSD),
    CODE_ZJDB(25, "zjdb","直接调拨单",  BusinessNoConstant.ZJDB),
    CODE_ZZCX(26, "zzcx","加工单", BusinessNoConstant.ZZCX),
    CODE_THQS(27, "thqs","退货签收单", BusinessNoConstant.THQS),
    CODE_XSCK(28, "xsck","销售出库单",  BusinessNoConstant.XSCK),
    CODE_QTRK(29, "qtrk","其他入库单", BusinessNoConstant.QTRK),
    CODE_QTCK(30, "qtck","其他出库单", BusinessNoConstant.QTCK),
    CODE_XSBG(31, "xsbg","销售变更单", BusinessNoConstant.XSBG),
    CODE_FHTZ(32, "fhtz","发货通知单", BusinessNoConstant.FHTZ),
    CODE_FBDC(33, "fbdc","分步式调拨调出", BusinessNoConstant.FBDC),
    CODE_FBDR(34, "fbdr","分步式调拨调入", BusinessNoConstant.FBDR),
    CODE_XSTH(35, "xsth","销售退货入库单", BusinessNoConstant.XSTH),
    CODE_KHDZ(36, "khdz","客户地址编号", BusinessNoConstant.KHDZ),
    CODE_KHLXR(37, "khlxr","客户联系人编号", BusinessNoConstant.KHLXR),
    CODE_SUB(38, "sub","委外订单", BusinessNoConstant.SUB),
    CODE_SUBCH(39, "subch","委外变更单", BusinessNoConstant.SUBCH),
    STOCKTAKING_PLAN(40, "pdjh","盘点计划",BusinessNoConstant.PDJH),
    STOCKTAKING_TASK(41, "pdrw","盘点任务",BusinessNoConstant.PDRW),
    STOCKTAKING_PROFIT(42, "pyd","盘盈单",BusinessNoConstant.PYD),
    STOCKTAKING_LOSS(43, "pkd","盘盈单",BusinessNoConstant.PKD),
    CODE_SO_B2C(44, "xsds","b2c销售订单",BusinessNoConstant.XSDS),
    CODE_CWYD(45, "cwyd","仓位移动",BusinessNoConstant.CWYD),
    CODE_KHDZC(46, "khdzc","b2c客户地址编号",BusinessNoConstant.KHDZC),
    CODE_KHLXRC(47, "khlxrc","b2c客户联系人编号",BusinessNoConstant.KHLXRC),
    CODE_CUSTC(48, "custc","客户",BusinessNoConstant.CUSTC),
    CODE_FBAS(49, "fbas","FBA货件",BusinessNoConstant.FBAS),
    CODE_FHD(50, "fhd","头程发货单",BusinessNoConstant.FHD),
    CODE_XSDD(51, "xsdd","b2c销售订单(平台)",BusinessNoConstant.XSDD),
    CODE_FHJH(52, "fhjh","海外仓发货计划",BusinessNoConstant.FHJH),
    CODE_YHSQ(53, "yhsq","要货申请",BusinessNoConstant.YHSQ),
    CODE_FHDC(54, "fhdc","b2c发货单",BusinessNoConstant.FHDC),
    CODE_FHLJ(55, "fhlj","b2c发货拦截单",BusinessNoConstant.FHLJ),
    CODE_FLD(56, "fld","委外发料单",BusinessNoConstant.FLD),
    CODE_SHD(57, "shd","送货单",BusinessNoConstant.SHD),
    CODE_DZD(58, "dzd","对账单",BusinessNoConstant.DZD),
    CODE_ZZBG(59, "zzbg","中转报关",BusinessNoConstant.ZZBG),
    CODE_ZB(60, "zb","组包预报",BusinessNoConstant.ZB),

    CODE_SJDB(61, "sjdb","数据对比","SJDB"),
    CODE_BGZD(61, "bgzd","报关账单","BGZD"),
    CODE_TCZD(62, "tczd","头程账单","TCZD"),
    CODE_XNC(63, "xnc","虚拟仓", BusinessNoConstant.XNC),
    CODE_XLS(64, "xls","虚拟仓库存流水", BusinessNoConstant.XLS),
    CODE_FH(65, "FH","虚拟仓分货单", BusinessNoConstant.FH),

    CODE_DQDD(53, "DQDD","多渠道订单(平台)",BusinessNoConstant.DQDD),
    ZXRW(53, "ZXRW","装箱任务",BusinessNoConstant.ZXRW),
    CODE_JHD(64, "JHD","拣货单",BusinessNoConstant.JHD),
    CODE_JHC(65, "JHC","拣货车管理",BusinessNoConstant.JHC),
    CODE_JHBC(66, "JHBC","拣货波次",BusinessNoConstant.JHBC),
    CODE_QCFT(67, "QCFT","期初头程分摊",BusinessNoConstant.QCFT),
    CODE_CHCB(68, "CHCB","SKU成本分摊",BusinessNoConstant.CHCB),
    CODE_SCLC(69, "SCLC","试产量产单",BusinessNoConstant.SCLC),
    THD(70, "THD","B2C退货单",BusinessNoConstant.THD),

    CODE_TLD(67, "tld","委外发退料单",BusinessNoConstant.TLD),
    CODE_FHBG(70, "FHBG","发货通知变更单",BusinessNoConstant.FHBG),
    CODE_JSRQ(70, "JSRQ","计算日期",BusinessNoConstant.JSRQ),
    CODE_S(71, "S","发货建议",BusinessNoConstant.S),
    CODE_P(72, "P","采购建议",BusinessNoConstant.P),
    CODE_PP(73, "PP","采购建议",BusinessNoConstant.PP),
    CODE_FHJY(71, "FHJY","发货建议",BusinessNoConstant.FHJY),
    CODE_CGJY(72, "CGJY","采购建议",BusinessNoConstant.CGJY),

    CODE_TKD(71, "TKD","退款单",BusinessNoConstant.TKD),
    CODE_XLSS(80, "XLSS","销量试算",BusinessNoConstant.XLSS),
    CODE_YHBG(75, "YHBG","要货申请变更单",BusinessNoConstant.YHBG),
    CODE_MOULD(90, "MJ","模具管理",BusinessNoConstant.MJ),
    CODE_N(81, "N","库龄批次号",BusinessNoConstant.N),
    CODE_INV(82, "INV","发票号",BusinessNoConstant.INV),
    CODE_XSBH(83, "XSBH","全托管订单",BusinessNoConstant.XSBH),
    CODE_SHSQ(83, "SHSQ","售后",BusinessNoConstant.SHSQ),

    CODE_XSJM(83, "xsjm","销售价目编号", BusinessNoConstant.XSJM),
    CODE_XSTJ(84, "xstj","销售价目调价编号", BusinessNoConstant.XSTJ),
    CODE_ZJTZ(86, "ZJTZ","质检通知",BusinessNoConstant.ZJTZ),
    CODE_TCTZ(87, "TCTZ","头程调整记录",BusinessNoConstant.TCTZ),
    CODE_XNKC(88, "XNKC","虚拟库存调整",BusinessNoConstant.XNKC),
    CODE_GYSHT(88, "GYSHT","合同管理编码",BusinessNoConstant.GYSHT),

    CODE_LCWT(87, "LCWT","委托审批",BusinessNoConstant.LCWT),
    CODE_SFSP(88, "SFSP","ERP审批同步配置",BusinessNoConstant.SFSP),
    CODE_LCPZ(89, "LCPZ","流程配置",BusinessNoConstant.LCPZ),
    CODE_WFHD(88, "whud","b2c三方仓发货单",BusinessNoConstant.WFHD),
    CODE_SFSC(90, "SFSC","三方审批生成配置",BusinessNoConstant.SFSC),
    CODE_GYSDM(91, "GYSDM","供应商代码", BusinessNoConstant.GYSDM),
    CODE_SKD(92, "SKD","收款单", BusinessNoConstant.SKD),
    CODE_KHSX(93, "KHSX","客户授信", BusinessNoConstant.KHSX),
    CODE_YPLY(93, "YPLY","样品领用", BusinessNoConstant.YPLY),

    CODE_YPZF(97, "YPBF","样品作废单", BusinessNoConstant.YPBF),
    CODE_YPJY(98, "YPJY","样品借用单", BusinessNoConstant.YPJY),
    CODE_YPGH(99, "YPGH","样品归还单", BusinessNoConstant.YPGH),
    CODE_ZHXS(100, "ZHXS","展会订单", BusinessNoConstant.ZHXS),
    CODE_YPTH(101, "YPTH","样品退回单", BusinessNoConstant.YPTH),
    CODE_QCTZ(102, "QCTZ","期初台账", BusinessNoConstant.QCTZ),

    ;




    private Integer code;
    private String name;
    private String desc;

    // 单号前缀
    private String prefix;

    // redis单号key
    public static final String REDIS_GEN_KEY = "gen_doc_no";

    // 补0位数
    public static final Integer FILL_0_DIGIT = 5;


    public void setCode(Integer code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public static String getNameByCode(Integer code) {
        BusinessNoTypeEnum[] businessNoTypeEnums = values();
        for (BusinessNoTypeEnum businessNoTypeEnum : businessNoTypeEnums) {
            if (businessNoTypeEnum.getCode().equals(code)) {
                return businessNoTypeEnum.getName();
            }
        }
        return null;
    }

    public static BusinessNoTypeEnum getEnumByType(String code) {
        BusinessNoTypeEnum[] businessNoTypeEnums = values();
        for (BusinessNoTypeEnum businessNoTypeEnum : businessNoTypeEnums) {
            if (businessNoTypeEnum.getCode().toString().equals(code)) {
                return businessNoTypeEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        BusinessNoTypeEnum[] businessNoTypeEnums = values();
        for (BusinessNoTypeEnum businessNoTypeEnum : businessNoTypeEnums) {
            if (businessNoTypeEnum.getName().equals(name)) {
                return businessNoTypeEnum.getCode();
            }
        }
        return null;
    }
}
