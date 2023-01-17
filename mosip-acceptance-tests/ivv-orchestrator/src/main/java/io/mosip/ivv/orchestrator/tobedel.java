package io.mosip.ivv.orchestrator;

import org.json.JSONObject;

public class tobedel {
public static void main(String[] args) {
	String data="{\"attachments\":[],\"headers\":{},\"headerLines\":[{\"key\":\"date\",\"line\":\"Date: Wed, 11 Jan 2023 10:47:31 +0000 (UTC)\"},{\"key\":\"from\",\"line\":\"From: do-not-reply@mosip.io\"},{\"key\":\"to\",\"line\":\"To: ckesiraju@gmail.com\"},{\"key\":\"message-id\",\"line\":\"Message-ID: <1293173200.61982.1673434051583@notifier-85b56cf8b6-d7tqq>\"},{\"key\":\"subject\",\"line\":\"Subject: =?UTF-8?Q?UIN_XXXXXXXX12:_OTP_Request=0A=0A$_idvidType_$_?=\\r\\n =?UTF-8?Q?idvid:_=D8=B7=D9=84=D8=A8_OTP=0A=0AUIN_X?=\\r\\n =?UTF-8?Q?XXXXXXX12:_Requ=C3=AAte_OTP?=\"},{\"key\":\"mime-version\",\"line\":\"MIME-Version: 1.0\"},{\"key\":\"content-type\",\"line\":\"Content-Type: multipart/mixed; \\r\\n\\tboundary=\\\"----=_Part_61980_615769008.1673434051448\\\"\"}],\"html\":\"Dear jhgjjhgg OTP for UIN XXXXXXXX12 is 111111 and is valid for 3 minutes. (Generated on 11-01-2023 at 16:17:31 Hrs)\\n\\n????? $ name OTP ?? $ idvidType $ idvid ?? $ otp ??? ???? ?? $ validTime ?????. (?? ?????? ?? $ date ?? $ time Hrs)\\n\\nCher $name_fra, OTP pour UIN XXXXXXXX12 est 111111 et est valide pour 3 minutes. (Généré le 11-01-2023 à 16:17:31 Hrs)\",\"subject\":\"UIN XXXXXXXX12: OTP Request\\n\\n$ idvidType $ idvid: ??? OTP\\n\\nUIN XXXXXXXX12: Requête OTP\",\"date\":\"2023-01-11T10:47:31.000Z\",\"to\":{\"value\":[{\"address\":\"ckesiraju@gmail.com\",\"name\":\"\"}],\"html\":\"<span class=\\\"mp_address_group\\\"><a href=\\\"mailto:ckesiraju@gmail.com\\\" class=\\\"mp_address_email\\\">ckesiraju@gmail.com</a></span>\",\"text\":\"ckesiraju@gmail.com\"},\"from\":{\"value\":[{\"address\":\"do-not-reply@mosip.io\",\"name\":\"\"}],\"html\":\"<span class=\\\"mp_address_group\\\"><a href=\\\"mailto:do-not-reply@mosip.io\\\" class=\\\"mp_address_email\\\">do-not-reply@mosip.io</a></span>\",\"text\":\"do-not-reply@mosip.io\"},\"messageId\":\"<1293173200.61982.1673434051583@notifier-85b56cf8b6-d7tqq>\"}}";
JSONObject j=new JSONObject(data);
System.out.println("html" + j.get("html"));

System.out.println("text" + j.getJSONObject("to").get("text"));

}
}
