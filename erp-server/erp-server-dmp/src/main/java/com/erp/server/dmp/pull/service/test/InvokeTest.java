package com.erp.server.dmp.pull.service.test;

public class InvokeTest {

	public static void main(String[] args) throws Exception {
		InvokeHelper.POST_K3CloudURL = "http://47.106.224.95:8089/k3cloud/html5/index.aspx?ud=%7Bdbid%3A%27604578a4a54a6f%27%2Cusername%3A%27%E7%9E%BF%E8%82%B2%E5%BF%A0%27%2Cappid%3A%27237496_016p4bjt3qA%2FS4Xv2Z0r77%2BM5N781AMo%27%2Csigneddata%3A%27579dda45d669a25142827729369454aa7ede7ce7%27%2Ctimestamp%3A%271669695378%27%2Clcid%3A%272052%27%2Corigintype%3A%27simpas%27%7D";
		String dbId = "55bb19192ebcde";
		String uid = "Demo";
		String pwd = "888888";
		int lang = 2052;
		
		if (InvokeHelper.Login(dbId, uid, pwd, lang)) {

			// ���۶����������
			// ҵ�����Id
			String FormId = "SAL_SaleOrder";
			String FieldKeys  = "FID,FBillNo,FDate,FBillTypeID,FDocumentStatus,FCustId,FSaleDeptId,FSalerId,FReceiveAddress,FLinkMan,FLinkPhone,FApproverId,FApproveDate,FCloseStatus,FCancelStatus,FChangerId,FReceiveId,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId,FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifyDate,FSaleOrgId,FVersionNo,FSignStatus,FSOFrom," +
					"FReturnType,FRowType,FMaterialName,FMaterialGroup,FMaterialId,FMaterialModel,FQty,FPriceUnitQty,FUnitID,FAuxPropId,FPrice,FEntryTaxRate,FTaxPrice,FIsFree,FEntryTaxAmount,FMaterialType,FAmount,FBarcode,FMapName,FMapId";

			String jsonData = "{\"CreateOrgId\":0,\"Numbers\":[],\"Ids\":\"\",\"NetworkCtrl\":\"\"}";
			//��Ҫ���������
			// �����ֶο�����Ҫ�����Լ�ʵ��ֵ���޸�
			// FCustId FSalerId FMaterialId FUnitID
			String sContent = "{\"Creator\":\"String\",\"NeedUpDateFields\":[\"FBillTypeID\",\"FDate\",\"FBusinessType\",\"FSaleOrgId\",\"FCustId\",\"FSettleCurrId\",\"FSalerId\",\"SAL_SaleOrder__FSaleOrderEntry\",\"FMaterialId\",\"FSettleOrgIds\",\"FUnitID\",\"FQty\",\"SAL_SaleOrder__FSaleOrderFinance\",\"FSettleCurrId\",\"FLocalCurrId\",\"FIsIncludedTax\",\"FBillTaxAmount\",\"FBillAmount\",\"FBillAllAmount\",\"FExchangeTypeId\",\"FExchangeRate\"],\"Model\":{\"FID\":0,\"FBillTypeID\":{\"FNumber\":\"XSDD01_SYS\"},\"FBusinessType\":\"NORMAL\",\"FSaleOrgId\":{\"FNUMBER\":\"100\"},\"FCustId\":{\"FNUMBER\":\"CUST0001\"},\"FSettleCurrId\":{\"FNUMBER\":\"PRE001\"},\"FSalerId\":{\"FNUMBER\":\"0002\"},\"SAL_SaleOrder__FSaleOrderFinance\":{\"FExchangeRate\":6.8123},\"SAL_SaleOrder__FSaleOrderEntry\":[{\"FMaterialId\":{\"FNUMBER\":\"001\"},\"FSettleOrgIds\":{\"FNUMBER\":\"100\"},\"FUnitID\":{\"FNumber\":\"��\"},\"FQty\":10}]}}";
			InvokeHelper.Save(FormId, sContent);

			System.out.println("hola success");
		}
	}
}
