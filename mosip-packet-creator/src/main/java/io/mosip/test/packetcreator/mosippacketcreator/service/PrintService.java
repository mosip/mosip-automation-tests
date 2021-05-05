package io.mosip.test.packetcreator.mosippacketcreator.service;


/*
@Component
public class PrintService {

	@Autowired
	APIRequestUtil apiRequestUtil;
	
	public Map<String, byte[]> getDocuments(String credential, String credentialType, String encryptionPin,
			String requestId, String sign,
			String cardType,
			boolean isPasswordProtected) {
		
			String credentialSubject = getCrdentialSubject(credential);
		
			org.json.JSONObject credentialSubjectJson = new org.json.JSONObject(credentialSubject);
			org.json.JSONObject decryptedJson = decryptAttribute(credentialSubjectJson, encryptionPin, credential);
		individualBio = decryptedJson.getString("biometrics");
		String individualBiometric = new String(individualBio);
		uin = decryptedJson.getString("UIN");
		boolean isPhotoSet = setApplicantPhoto(individualBiometric, attributes);
		if (!isPhotoSet) {
			printLogger.debug(LoggerFileConstant.SESSIONID.toString(),
					LoggerFileConstant.REGISTRATIONID.toString(), uin,
					PlatformErrorMessages.PRT_PRT_APPLICANT_PHOTO_NOT_SET.name());
		}
		setTemplateAttributes(decryptedJson.toString(), attributes);
		attributes.put(IdType.UIN.toString(), uin);

		byte[] textFileByte = createTextFile(decryptedJson.toString());
		byteMap.put(UIN_TEXT_FILE, textFileByte);

		boolean isQRcodeSet = setQrCode(decryptedJson.toString(), attributes);
		if (!isQRcodeSet) {
			printLogger.debug(LoggerFileConstant.SESSIONID.toString(),
					LoggerFileConstant.REGISTRATIONID.toString(), uin,
					PlatformErrorMessages.PRT_PRT_QRCODE_NOT_SET.name());
		}

		template = setTemplateForMaskedUIN(cardType, uin, vid, attributes, template);

		// getting template and placing original valuespng
		InputStream uinArtifact = templateGenerator.getTemplate(template, attributes, primaryLang);
		if (uinArtifact == null) {
			printLogger.error(LoggerFileConstant.SESSIONID.toString(),
					LoggerFileConstant.REGISTRATIONID.toString(), "UIN",
					PlatformErrorMessages.PRT_TEM_PROCESSING_FAILURE.name());
			throw new TemplateProcessingFailureException(
					PlatformErrorMessages.PRT_TEM_PROCESSING_FAILURE.getCode());
		}

		String password = null;
		if (isPasswordProtected) {
			password = getPassword(uin);
		}

		// generating pdf
		byte[] pdfbytes = uinCardGenerator.generateUinCard(uinArtifact, UinCardType.PDF, password);
		byteMap.put(UIN_CARD_PDF, pdfbytes);

		byte[] uinbyte = attributes.get("UIN").toString().getBytes();
		byteMap.put("UIN", uinbyte);
		printStatusUpdate(requestId, pdfbytes, credentialType);
		isTransactionSuccessful = true;

		return null;
	}
	private String getCrdentialSubject(String crdential) {
		
		org.json.JSONObject jsonObject = new org.json.JSONObject(crdential);
		String credentialSubject = jsonObject.get("credentialSubject").toString();
	
		return credentialSubject;
	}
	public org.json.JSONObject decryptAttribute(org.json.JSONObject data, String encryptionPin, String credential) {

		org.json.JSONObject jsonObj = new org.json.JSONObject(credential);

		RequestWrapper<DecryptRequestDto> request = new RequestWrapper<>();
		ResponseWrapper<DecryptResponseDto> response = new ResponseWrapper<DecryptResponseDto>();
		LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
		request.setRequesttime(now);
		String strq = null;
		org.json.JSONArray jsonArray = (org.json.JSONArray) jsonObj.get("protectedAttributes");
		if (!jsonArray.isEmpty()) {
		for (Object str : jsonArray) {
			try {
				DecryptRequestDto decryptRequestDto = new DecryptRequestDto();
				DecryptResponseDto decryptResponseDto = new DecryptResponseDto();
				decryptRequestDto.setUserPin(encryptionPin);
				decryptRequestDto.setData(data.getString(str.toString()));
				request.setRequest(decryptRequestDto);
				// response=(DecryptResponseDto)restApiClient.postApi(env.getProperty(ApiName.DECRYPTPINBASSED.name()),
				// "", de, DecryptResponseDto.class)
				apiRequestUtil.post(baseUrl, url, jsonRequest);
				
				response = (ResponseWrapper) restClientService.postApi(ApiName.DECRYPTPINBASSED, "", "", request,
						ResponseWrapper.class);

				decryptResponseDto = JsonUtil.readValue(JsonUtil.writeValueAsString(response.getResponse()),
						DecryptResponseDto.class);
				data.put((String) str, decryptResponseDto.getData());
			} catch (ApisResourceAccessException e) {
				printLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
						null, "Error while parsing Json file" + ExceptionUtils.getStackTrace(e));
				throw new ParsingException(PlatformErrorMessages.PRT_RGS_JSON_PARSING_EXCEPTION.getMessage(), e);
			} catch (IOException e) {
				printLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
						null, "Error while parsing Json file" + ExceptionUtils.getStackTrace(e));
				throw new ParsingException(PlatformErrorMessages.PRT_RGS_JSON_PARSING_EXCEPTION.getMessage(), e);
			}
		}
	}
}
*/