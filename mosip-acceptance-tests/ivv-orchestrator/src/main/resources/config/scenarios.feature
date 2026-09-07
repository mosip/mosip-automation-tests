# MOSIP DSL scenarios ? readable English Gherkin steps (parameters in plain language)
Feature: MOSIP DSL end-to-end acceptance tests

  @scenario_0
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Before Suite setup
Given I load known issues by env
And I user where user action is ADD_User, and user index or master user is dsl-0, and password or zone flag is Techno@123
And I user where user action is ADD_User, and user index or master user is 0, and password or zone flag is Techno@123
And I user where user action is ADD_User, and user index or master user is 1, and password or zone flag is Techno@123 and store result in registration officer 1
And I center where call type is CREATE, and user details is the saved registration officer 1, and center index is 1, and center active flag is T and store result in registration center 1
And I machine where call type is CREATE, and center details is the saved registration center 1, and center index is 1 and store result in environment 1 details
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 1, and password or zone flag is Techno@123, and center index or details is the saved environment 1 details and store result in environment 1 details
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved environment 1 details and store result in environment 1 details
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved environment 1 details
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved environment 1 details
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved environment 1 details, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved environment 1 details, and password or zone flag is 1
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved environment 1 details, and password or zone flag is T
And I write pre req where environment details is the saved environment 1 details, and pre-requisite data index is 1
And I user where user action is ADD_User, and user index or master user is 2, and password or zone flag is Techno@123 and store result in registration officer 2
And I center where call type is CREATE, and user details is the saved registration officer 2, and center index is 2, and center active flag is T and store result in registration center 2
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is true
And I machine where call type is CREATE, and center details is the saved registration center 2, and center index is 2 and store result in environment 2 details
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 2, and password or zone flag is Techno@123, and center index or details is the saved environment 2 details and store result in environment 2 details
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved environment 2 details and store result in environment 2 details
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved environment 2 details
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved environment 2 details
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved environment 2 details, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved environment 2 details, and password or zone flag is 2
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved environment 2 details, and password or zone flag is T
And I write pre req where environment details is the saved environment 2 details, and pre-requisite data index is 2
And I set context where context key is env_context, and pre-requisite details is the saved environment 2 details, and generate private key is true
And I user where user action is ADD_User, and user index or master user is 3, and password or zone flag is Techno@123 and store result in registration officer 3
And I center where call type is CREATE, and user details is the saved registration officer 3, and center index is 3, and center active flag is T and store result in registration center 3
And I machine where call type is CREATE, and center details is the saved registration center 3, and center index is 3 and store result in environment 3 details
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 3, and password or zone flag is Techno@123, and center index or details is the saved environment 3 details and store result in environment 3 details
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved environment 3 details and store result in environment 3 details
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved environment 3 details
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved environment 3 details
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved environment 3 details, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved environment 3 details, and password or zone flag is 3
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved environment 3 details, and password or zone flag is T
And I write pre req where environment details is the saved environment 3 details, and pre-requisite data index is 3
And I set context where context key is env_context, and pre-requisite details is the saved environment 3 details, and generate private key is true
And I user where user action is ADD_User_External_Packet, and user index or master user is 4, and password or zone flag is Techno@123 and store result in external packet environment details
And I write pre req where environment details is the saved external packet environment details, and pre-requisite data index is 4
And I clear device cert cache
And I generate auth certifcates
And I upload device certificate
And I warm run cache

  @scenario_1
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: Resident booked pre-registration with support documents walks into registration center completes the process and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I send otp where persona file path is the saved persona file path and store result in email
And I validate otp where persona file path is the saved persona file path, and email is the saved email
And I pre register where persona file path is the saved persona file path and store result in pre-registration ID
And I upload documents where persona file path is the saved persona file path, and pre-registration ID is the saved pre-registration ID
And I update pre reg status where status code is 0, and pre-registration ID is the saved pre-registration ID, and validation mode is valid
And I book appointment where holiday booking flag is false, and pre-registration ID is the saved pre-registration ID, and slot number is 1
And I generate and upload packet where pre-registration ID is the saved pre-registration ID, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_2
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check ridstage where registration ID is the saved registration ID, and RID stage is BIOGRAPHIC_VERIFICATION, and stage status is SUCCESS
Then I check tags where registration ID is the saved registration ID
And I delete packet data

  @scenario_4
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_Update
  Scenario: Resident walks into registration center completes the process and gets UIN card. Later updates biometrics and downloads UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I credential request where UIN is the saved second UIN, and email is the saved email and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I download card where credential request ID is the saved credential request ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
Then I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I demo authentication where demo field is name, and UIN is the saved second UIN, and persona file path is the saved persona file path, and VID is the saved VID
And I delete packet data

  @scenario_5
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor Child walks into registration center wants to get UIN without Guardian Details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is minor, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is ERROR
And I delete packet data

  @scenario_6
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor Child walks into registration center gets UIN with Guardian RID details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with rid where persona file path is the saved parent persona file path, and registration ID is the saved parent registration ID
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved parent registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_7
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New_Exception
  Scenario: A differently abled resident with exception in left and right index finger walks into registration center completes the process and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left IndexFinger and Finger:Right IndexFinger
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_8
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A differently abled resident walks into registration center completes the process and gets UIN card. Later update his iris and downloads UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is leftiris and rightIris and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I wait where wait seconds is UIN_WAIT_TIME
And I verify notification where notification type is updated, and email is the saved email
And I credential request where UIN is the saved second UIN, and email is the saved email and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I verify notification where notification type is Credential Issuance Status, and email is the saved email
And I download card where credential request ID is the saved credential request ID
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_9
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center wants to register his child with his RID. But the child packet goes on hold as his packet got rejected
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I get resident data where persona type is adult, and guardian flag is true, and gender is Male, and missing biometric fields is gender and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I update resident with rid where persona file path is the saved parent persona file path, and registration ID is the saved parent registration ID
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved child registration ID
Then I check ridstage where registration ID is the saved parent registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is ERROR
And I delete packet data

  @scenario_10
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. Same resident tries to get another UIN again
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in new packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in rid lost
And I post mock mv where registration ID is rid lost, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is rid lost
Then I check ridstage where registration ID is rid lost, and RID stage is MANUAL_ADJUDICATION, and stage status is FAILED
And I delete packet data

  @scenario_11
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. Same resident tries to get another UIN again with biometric exception
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Iris:Left
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in new packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in second registration ID
And I post mock mv where registration ID is the saved second registration ID, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is the saved second registration ID
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is MANUAL_ADJUDICATION, and stage status is FAILED
And I delete packet data

  @scenario_12
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center gives demo details of already registered another resident but different biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I credential request where UIN is the saved uin1, and email is the saved email and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I download card where credential request ID is the saved credential request ID
And I update demo or bio details where bio type is iris and face and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in new packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I delete packet data

  @scenario_13
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center and tries to register again by providing different demo details but same biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in new packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is MANUAL_ADJUDICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_14
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center and tries to retrieve UIN without providing biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is face and leftEye and rightEye and rightIndex and rightLittle and rightRing and rightMiddle and leftIndex and leftLittle and leftRing and leftMiddle and leftThumb and rightThumb and store result in persona file path
And I get packet template where packet type is LOST, and persona file path is the saved persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
And I wait where wait seconds is 90
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_15
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. Later updates his demo details in registration center and post successful processing downloads the EUIN using resident Portal
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is gender=Male, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I wait where wait seconds is UIN_WAIT_TIME
And I verify notification where notification type is updated, and email is the saved email
And I credential request where UIN is the saved second UIN, and email is the saved email and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I verify notification where notification type is Credential Issuance Status, and email is the saved email
And I download card where credential request ID is the saved credential request ID
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_16
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A non registered Resident walks into registration center without UIN and tries to retrieve the UIN
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is LOST, and persona file path is the saved persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
And I wait where wait seconds is 90
Then I check ridstage where registration ID is the saved registration ID, and RID stage is BIOGRAPHIC_VERIFICATION, and stage status is FAILED
And I delete packet data

  @scenario_17
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center and completes the process and gets UIN card without providing documents
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is POA and POI and POR and POE and POB and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_18
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center tries to get UIN without providing biometric
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and false and false and false and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_19
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center tries to get Lost UIN without providing biometric
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is face and iris and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I get packet template where packet type is LOST, and persona file path is the saved persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in lost rid
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is lost rid, and RID stage is BIOGRAPHIC_VERIFICATION, and stage status is FAILED
And I delete packet data

  @scenario_20
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Non-resident walks into registration center and completes the process gets UIN for him
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is langcode=eng and residencestatus=NFR, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_21
  @Postive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center with his child and completes the process gets UIN cards for both
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I packetcreator where packet type is NEW, and template path is the saved parent packet template path and store result in parent zip packet path
And I ridsync where packet type is NEW, and packet zip path is parent zip packet path and store result in parent registration ID
And I update resident with rid where persona file path is the saved parent persona file path, and registration ID is the saved parent registration ID
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I packetcreator where packet type is NEW, and template path is the saved child packet template path and store result in child zip packet path
And I ridsync where packet type is NEW, and packet zip path is child zip packet path and store result in child registration ID
And I packetsync where packet zip path is parent zip packet path
And I packetsync where packet zip path is child zip packet path
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I delete packet data

  @scenario_22
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident booked pre-registration with support documents. walk-ins to registration center completes the process and gets UIN card. Later performs bio authentication with face
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I send otp where persona file path is the saved persona file path and store result in email
And I validate otp where persona file path is the saved persona file path, and email is the saved email
And I pre register where persona file path is the saved persona file path and store result in pre-registration ID
And I upload documents where persona file path is the saved persona file path, and pre-registration ID is the saved pre-registration ID
And I update pre reg status where status code is 0, and pre-registration ID is the saved pre-registration ID, and validation mode is valid
And I book appointment where holiday booking flag is false, and pre-registration ID is the saved pre-registration ID, and slot number is 2
And I generate and upload packet where pre-registration ID is the saved pre-registration ID, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I wait where wait seconds is 90
And I bio authentication where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I oidc client and store result in OIDC client ID
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id1 and store result in transaction id1
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id2 and store result in transaction id2
And I bio esignet authentication where device info file is faceDevice, and UIN is the saved UIN, and persona file path is the saved persona file path, and UIN transaction ID is the saved transaction id1, and VID is the saved VID, and VID transaction ID is the saved transaction id2
And I user info where transaction ID is the saved transaction id, and OIDC client ID is the saved OIDC client ID
And I delete packet data

  @scenario_23
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A differently abled resident walks in to registration center completes the process and gets UIN card. Later performs biometric authentication using left little finger
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Iris:Left and Iris:Right
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is leftLittleDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I oidc client and store result in OIDC client ID
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id1 and store result in transaction id1
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id2 and store result in transaction id2
And I bio esignet authentication where device info file is leftLittleDevice, and UIN is the saved UIN, and persona file path is the saved persona file path, and UIN transaction ID is the saved transaction id1, and VID is the saved VID, and VID transaction ID is the saved transaction id2
And I user info where transaction ID is the saved transaction id, and OIDC client ID is the saved OIDC client ID
And I delete packet data

  @scenario_24
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later performs biometric authentication using right finger both using UIN and VID. Also performs eSignet biometric authentication using right finger both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is rightlittleFinger and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is rightThumbDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I oidc client and store result in OIDC client ID
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id1 and store result in transaction id1
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id2 and store result in transaction id2
And I bio esignet authentication where device info file is rightThumbDevice, and UIN is the saved UIN, and persona file path is the saved persona file path, and UIN transaction ID is the saved transaction id1, and VID is the saved VID, and VID transaction ID is the saved transaction id2
And I user info where transaction ID is the saved transaction id, and OIDC client ID is the saved OIDC client ID
And I delete packet data

  @scenario_25
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. Later updates both finger face biometrics and does eKYC using face biometric
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I get email by uin where resident UIN is the saved UIN and store result in email1
And I credential request where UIN is the saved UIN, and email is the saved email1 and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I verify notification where notification type is Credential Issuance Status, and email is the saved email1
And I download card where credential request ID is the saved credential request ID
And I update demo or bio details where bio type is finger and face, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email1
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email1 and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email1
And I wait where wait seconds is 90
And I ekyc bio where device info file is faceDevice, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_26
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later updates iris biometric and does eKYC using face biometric both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I credential request where UIN is the saved UIN, and email is the saved email and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I verify notification where notification type is Credential Issuance Status, and email is the saved email
And I download card where credential request ID is the saved credential request ID
And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc bio where device info file is faceDevice, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_27
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. Later updates left index biometrics and does eKYC using face biometric
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I credential request where UIN is the saved UIN, and email is the saved email and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I verify notification where notification type is Credential Issuance Status, and email is the saved email
And I download card where credential request ID is the saved credential request ID
And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc bio where device info file is faceDevice, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_28
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later updates his phone number and does EKYC with OTP both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phoneNumber=3938333736, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc otp where UIN ID type is the saved UIN, and UIN is the saved second UIN, and VID ID type is the saved VID, and VID is the saved VID, and email is the saved email and store result in ekycData
And I validate kyc data where KYC field is photo, and response variable is ekycData
And I delete packet data

  @scenario_29
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later updates his phone number address and performs OTP authentication both using UIN and VID. Also performs eSignet OTP authentication using right finger both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phoneNumber=3938333736, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I otp authentication where UIN is the saved UIN, and VID is the saved second UIN, and email is the saved VID, and parameter 4 is the saved VID, and parameter 5 is the saved email
And I oidc client and store result in OIDC client ID
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id1 and store result in transaction id1
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id2 and store result in transaction id2
And I esignet authentication where UIN transaction ID is the saved transaction id1, and UIN is the saved UIN, and authentication factor is OTP, and email is the saved email, and VID is the saved VID, and VID transaction ID is the saved transaction id2
And I user info where transaction ID is the saved transaction id1, and OIDC client ID is the saved OIDC client ID
And I delete packet data

  @scenario_30
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later performs multi factor authentication using face biometrics phone number and OTP both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I multi factor authentication where bio device list is faceDevice, and demo fields is dob, and individual ID type is the saved UIN, and individual ID is the saved UIN, and persona file path is the saved persona file path, and VID is the saved VID, and email is the saved email
And I delete packet data

  @scenario_31
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later performs multi factor authentication using face biometrics date of birth and OTP both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I multi factor authentication where bio device list is faceDevice, and demo fields is dob, and individual ID type is the saved UIN, and individual ID is the saved UIN, and persona file path is the saved persona file path, and VID is the saved VID, and email is the saved email
And I delete packet data

  @scenario_32
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. Later updates his address and performs demographic authentication to download EUIN
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is addressLine1=bnglr, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I get email by uin where resident UIN is the saved second UIN and store result in email
And I credential request where UIN is the saved second UIN, and email is the saved email and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I verify notification where notification type is Credential Issuance Status, and email is the saved email
And I download card where credential request ID is the saved credential request ID
And I delete packet data

  @scenario_33
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later updates his email and perform OTP authentication both using UIN and VID. Also performs eSignet OTP authentication using right finger both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=test, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I get email by uin where resident UIN is the saved second UIN and store result in email
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I otp authentication where UIN is the saved UIN, and VID is the saved second UIN, and email is the saved VID, and parameter 4 is the saved VID, and parameter 5 is the saved email
And I oidc client and store result in OIDC client ID
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id1 and store result in transaction id1
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id2 and store result in transaction id2
And I esignet authentication where UIN transaction ID is the saved transaction id1, and UIN is the saved UIN, and authentication factor is OTP, and email is the saved email, and VID is the saved VID, and VID transaction ID is the saved transaction id2
And I user info where transaction ID is the saved transaction id1, and OIDC client ID is the saved OIDC client ID
And I delete packet data

  @scenario_34
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident child walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later updates his name and perform demographic authentication both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I wait where wait seconds is UIN_WAIT_TIME
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved child persona file path
And I update resident with uin where persona file path is the saved child persona file path, and UIN is the saved child UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved child persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved update packet template path and store result in child rid2
And I check status where packet status is PROCESSED, and registration ID is the saved child rid2
And I get uin by rid where source registration ID is the saved child rid2 and store result in child uin2
And I verify notification where notification type is updated, and email is the saved email1
And I wait where wait seconds is UIN_WAIT_TIME
And I credential request where UIN is the saved child uin2, and email is the saved email1 and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I verify notification where notification type is Credential Issuance Status, and email is the saved email1
And I generate vid where VID type is Perpetual, and UIN is the saved child uin2, and email or phone is the saved email1 and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email1
And I wait where wait seconds is 90
And I demo authentication where demo field is name, and UIN is the saved child uin2, and persona file path is the saved child persona file path, and VID is the saved VID
And I delete packet data

  @scenario_35
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later updates left index biometrics and perform biometric authentication with face both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I get email by uin where resident UIN is the saved UIN and store result in email1
And I credential request where UIN is the saved UIN, and email is the saved email1 and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I verify notification where notification type is Credential Issuance Status, and email is the saved email1
And I download card where credential request ID is the saved credential request ID
And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email1
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email1 and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email1
And I wait where wait seconds is 90
And I ekyc bio where device info file is faceDevice, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_36
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A differently abled resident walksin to registration center and completes the process and gets UIN card. Later perform EKYC Bio both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is leftiris and rightIris and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc bio where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_37
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card after previous UIN application is rejected with different center
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I upload packet with invalid hash where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
Then I user where user action is ADD_User, and user index or master user is 4, and password or zone flag is Techno@123 and store result in user4
Then I center where call type is CREATE, and user details is the saved user4, and center index is 4, and center active flag is T and store result in center4
Then I machine where call type is CREATE, and center details is the saved center4, and center index is 4 and store result in external packet environment details
Then I user where user action is DELETE_CENTERMAPPING, and user index or master user is 4, and password or zone flag is Techno@123, and center index or details is the saved external packet environment details and store result in external packet environment details
Then I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved external packet environment details and store result in external packet environment details
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved external packet environment details
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved external packet environment details
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved external packet environment details, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved external packet environment details, and password or zone flag is 4
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved external packet environment details, and password or zone flag is T
And I write pre req where environment details is the saved external packet environment details, and pre-requisite data index is 4
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved external packet environment details, and generate private key is true
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in new packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I delete packet data

  @scenario_38
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident booked pre-registration with support documents. walks into registration center tries to get UIN after previous UIN application is in progress with different center
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 2 and store result in environment 2 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 2 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I send otp where persona file path is the saved persona file path and store result in email
And I validate otp where persona file path is the saved persona file path, and email is the saved email
And I pre register where persona file path is the saved persona file path and store result in pre-registration ID
And I upload documents where persona file path is the saved persona file path, and pre-registration ID is the saved pre-registration ID
And I update pre reg status where status code is 0, and pre-registration ID is the saved pre-registration ID, and validation mode is valid
And I book appointment where holiday booking flag is false, and pre-registration ID is the saved pre-registration ID, and slot number is 2
And I generate and upload packet where pre-registration ID is the saved pre-registration ID, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I read pre req where pre-requisite data index is 2 and store result in environment 2 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 2 details, and generate private key is false
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in template path2
And I packetcreator where packet type is NEW, and template path is the saved template path2 and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in second registration ID
And I packetsync where packet zip path is the saved packet zip path
And I post mock mv where registration ID is the saved second registration ID, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is the saved second registration ID
And I delete packet data

  @scenario_39
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration same center where his previous application got rejected and completes the process and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I upload packet with invalid hash where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in new packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I delete packet data

  @scenario_40
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later gets eKYC done both using UIN VID and face auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc bio where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_41
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later gets eKYC done both using UIN VID and right ring finger auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc bio where device info file is rightRingDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_42
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later gets eKYC done both using UIN VID and right iris auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc bio where device info file is RightIris, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_43
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A differently abled resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later gets eKYC done both using UIN VID and face auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Iris:Left and Iris:Right
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc bio where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_44
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates temporary VID. Later gets eKYC done both using UIN VID and face auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Temporary, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc bio where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_45
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates temporary VID. Later gets eKYC done both using UIN VID and left ring finger auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Temporary, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc bio where device info file is leftRingDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_46
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates temporary VID. Later gets eKYC done both using UIN VID and left iris auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Temporary, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc bio where device info file is LeftIris, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_47
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A differently abled resident walks into registration center completes the process and gets UIN card and generates temporary VID. Later gets eKYC done both using UIN VID and face auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is leftiris and rightIris and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Temporary, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I ekyc bio where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_75
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. When the packet is created and uploaded with supervisor and operator biometrics
Given I get ping health where component is packetcreator
And I wait where wait seconds is 35
And I user where user action is ADD_User, and user index or master user is 75, and password or zone flag is Techno@123 and store result in user75
And I center where call type is CREATE, and user details is the saved user75, and center index is 75, and center active flag is T and store result in center75
And I machine where call type is CREATE, and center details is the saved center75, and center index is 75 and store result in details75
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 75, and password or zone flag is Techno@123, and center index or details is the saved details75 and store result in details75
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details75 and store result in details75
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details75
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details75
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details75, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details75, and password or zone flag is 75
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details75, and password or zone flag is T
And I write pre req where environment details is the saved details75, and pre-requisite data index is 75
And I read pre req where pre-requisite data index is 75 and store result in details75
And I set context where context key is env_context, and pre-requisite details is the saved details75, and generate private key is true
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I user where user action is UPDATE_UIN, and user index or master user is 75, and password or zone flag is Techno@123, and center index or details is the saved UIN
And I set context where context key is env_context, and pre-requisite details is the saved details75, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is valid and null and valid and null and operatorBiometrics_bio_CBEFF and supervisorBiometrics_bio_CBEFF
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path1
And I get packet template where packet type is NEW, and persona file path is the saved persona file path1 and store result in template path1
And I packetcreator where packet type is NEW, and template path is the saved template path1 and store result in zip packet path1
And I ridsync where packet type is NEW, and packet zip path is the saved zip packet path1 and store result in rid1
And I packetsync where packet zip path is the saved zip packet path1
And I set context where context key is env_context, and pre-requisite details is the saved details75, and generate private key is false
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
Then I check ridstage where registration ID is the saved rid1, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I machine where call type is DCOM, and center details is the saved details75
Then I user where user action is DELETE_CENTERMAPPING, and user index or master user is 75, and password or zone flag is Techno@123, and center index or details is the saved details75 and store result in details75
Then I center where call type is DCOM, and user details is the saved details75, and center index is 75
And I delete packet data

  @scenario_48
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process but while the packet getting uploaded packet got Corrupted
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I corrupt packet where byte offset is 1024, and data to write is hello automation, and packet zip path is the saved packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
And I wait where wait seconds is 90
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is UPLOAD_PACKET, and stage status is ERROR
And I delete packet data

  @scenario_49
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident Minor Child walks into registration center to get UIN card. Later tries to get another UIN by providing different Guardian
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona1
And I get packet template where packet type is NEW, and persona file path is the saved parent persona1 and store result in parent template1
And I generate and upload packet skipping prereg where persona file path is the saved parent persona1, and packet template path is the saved parent template1 and store result in parent rid1
And I check status where packet status is PROCESSED, and registration ID is the saved parent rid1
And I get uin by rid where source registration ID is the saved parent rid1 and store result in parent uin1
And I get email by uin where resident UIN is the saved parent uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with rid where persona file path is the saved parent persona1, and registration ID is the saved parent rid1
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona1, and child persona file path is the saved child persona file path
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona2
And I get packet template where packet type is NEW, and persona file path is the saved parent persona2 and store result in parent template2
And I generate and upload packet skipping prereg where persona file path is the saved parent persona2, and packet template path is the saved parent template2 and store result in parent rid2
And I check status where packet status is PROCESSED, and registration ID is the saved parent rid2
And I get uin by rid where source registration ID is the saved parent rid2 and store result in parent uin2
And I get email by uin where resident UIN is the saved parent uin2 and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I update resident with rid where persona file path is the saved parent persona2, and registration ID is the saved parent rid2
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona2, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_50
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident Minor Child walks into registration center to get UIN card. Later tries to get another UIN by providing same Guardian
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona1
And I get packet template where packet type is NEW, and persona file path is the saved parent persona1 and store result in parent template1
And I generate and upload packet skipping prereg where persona file path is the saved parent persona1, and packet template path is the saved parent template1 and store result in parent rid1
And I check status where packet status is PROCESSED, and registration ID is the saved parent rid1
And I get uin by rid where source registration ID is the saved parent rid1 and store result in parent uin1
And I get email by uin where resident UIN is the saved parent uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with rid where persona file path is the saved parent persona1, and registration ID is the saved parent rid1
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona1, and child persona file path is the saved child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona1, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_3
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. Resident lost UIN and walks in to registration center to retrieve the UIN
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I get packet template where packet type is LOST, and persona file path is the saved persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in rid lost
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is PROCESSED, and registration ID is rid lost
And I get uin by rid where source registration ID is rid lost and store result in second UIN
And I get email by uin where resident UIN is the saved second UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is rid lost, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_51
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process but due to low biometric image quality correction flow is initiated. Resident provides biometrics with good quality and gets the UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_51, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path, and biometric quality score is 10 and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is the saved registration ID, and RID stage is INTERNAL_WORKFLOW_ACTION, and stage status is SUCCESS, and sub-status is RPR-WIA-001
Then I get additional req id where email prefix is additionalReqId_51 and store result in additional req id
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path2
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_51, and persona file is the saved persona file path2
And I get packet template where packet type is BIOMETRIC_CORRECTION, and persona file path is the saved persona file path2 and store result in template path2
And I packetcreator where packet type is BIOMETRIC_CORRECTION, and template path is the saved template path2, and additional info request ID is additional req id and store result in zip packet path2
And I ridsync where packet type is BIOMETRIC_CORRECTION, and packet zip path is the saved zip packet path2, and additional info request ID is additional req id and store result in second registration ID
And I packetsync where packet zip path is the saved zip packet path2
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_58
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process but while the packet getting created packet has invalid hash
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is qa4_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I skip
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I upload packet with invalid hash where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_59
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center with his child and completes the process. But Guardian packet rejected hence introducer RID is not valid in infant packet
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in parent zip packet path
And I rid sync rejected where packet type is NEW, and packet zip path is parent zip packet path and store result in parent registration ID
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update resident with rid where persona file path is the saved parent persona file path, and registration ID is the saved parent registration ID
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I packetcreator where packet type is NEW, and template path is the saved child packet template path and store result in child zip packet path
And I ridsync where packet type is NEW, and packet zip path is child zip packet path and store result in child registration ID
And I packetsync where packet zip path is parent zip packet path
And I packetsync where packet zip path is child zip packet path
And I check status where packet status is REREGISTER, and registration ID is the saved parent registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved child registration ID
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is ERROR
Then I check ridstage where registration ID is the saved parent registration ID, and RID stage is VALIDATE_PACKET, and stage status is REJECTED
And I delete packet data

  @scenario_60
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident booked pre-registration without documents trying to update prereg status
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I send otp where persona file path is the saved persona file path and store result in email
And I validate otp where persona file path is the saved persona file path, and email is the saved email
And I pre register where persona file path is the saved persona file path and store result in pre-registration ID
And I update pre reg status where status code is 0, and pre-registration ID is the saved pre-registration ID, and validation mode is invalid
And I delete packet data

  @scenario_61
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process but supervisor rejects packet during packet processing
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I rid sync rejected where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is REJECTED
And I delete packet data

  @scenario_62
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into get UIN card and different resident tries to get UIN both resident having same demo and different biometric details
Given I get ping health where component is packetcreator
And I delete mock expect
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is leftIris and rigthIris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in second registration ID
And I post mock mv where registration ID is the saved second registration ID, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is the saved second registration ID
And I delete packet data

  @scenario_63
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process but the packet goes for manual adjudication as biometric matches with other resident
Given I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in new packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in second registration ID
And I post mock mv where registration ID is the saved second registration ID, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is the saved second registration ID
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is MANUAL_ADJUDICATION, and stage status is FAILED
And I delete packet data

  @scenario_64
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. Same resident tries to get another UIN with different name
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid new
And I post mock mv where registration ID is rid new, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is rid new
Then I check ridstage where registration ID is rid new, and RID stage is MANUAL_ADJUDICATION, and stage status is FAILED
And I delete packet data

  @scenario_65
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. Same resident tries to get another UIN with same demo details and same biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in second registration ID
And I post mock mv where registration ID is the saved second registration ID, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is the saved second registration ID
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is DEMOGRAPHIC_VERIFICATION, and stage status is IN_PROGRESS
And I delete packet data

  @scenario_66
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. Same resident tries to get another UIN by providing different demo details and same biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in template path new
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is template path new and store result in rid new
And I post mock mv where registration ID is rid new, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is rid new
Then I check ridstage where registration ID is rid new, and RID stage is MANUAL_ADJUDICATION, and stage status is FAILED
And I delete packet data

  @scenario_67
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But packet gets uploaded without supervisor Id  without supervisor Password and valid operator details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 2 and store result in environment 2 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 2 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is null and null and valid and valid
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_68
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But packet gets uploaded with invalid supervisor Id  invalid supervisor Password and valid operator details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 2 and store result in environment 2 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 2 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is invalid and valid and valid and valid
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is SUPERVISOR_VALIDATION, and stage status is ERROR
And I delete packet data

  @scenario_69
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But packet gets uploaded with valid supervisor Id invalid supervisor Password and valid operator details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 3 and store result in environment 3 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 3 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is valid and invalid and valid and valid
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is SUPERVISOR_VALIDATION, and stage status is FAILED
And I delete packet data

  @scenario_70
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But packet gets uploaded with null operator Id  null operator password and valid operator details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 2 and store result in environment 2 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 2 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is valid and valid and null and null
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_71
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But packet gets uploaded with Invalid operator Id  Valid operator password and valid operator details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 3 and store result in environment 3 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 3 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is valid and valid and invalid and valid
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is OPERATOR_VALIDATION, and stage status is ERROR
And I delete packet data

  @scenario_72
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But packet gets uploaded with Valid operator Id  invalid operator password and valid supervisor details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 2 and store result in environment 2 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 2 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is valid and valid and valid and invalid
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is OPERATOR_VALIDATION, and stage status is FAILED
And I delete packet data

  @scenario_73
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But packet gets uploaded with supervisor and operator cbeff file and without password auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 2 and store result in environment 2 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 2 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I user where user action is ADD_User, and user index or master user is 2, and password or zone flag is Techno@123, and center index or details is the saved UIN
And I set context where context key is env_context, and pre-requisite details is the saved environment 2 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is null and null and null and null and OperatorBiometrics_bio_CBEFF and SupervisorBiometrics_bio_CBEFF
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in rid1
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is REREGISTER, and registration ID is the saved rid1
Then I check ridstage where registration ID is the saved rid1, and RID stage is INTERNAL_WORKFLOW_ACTION, and stage status is SUCCESS
And I delete packet data

  @scenario_74
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But packet gets uploaded without supervisor and operator cbeff file and without password auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 2 and store result in environment 2 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 2 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I user where user action is ADD_User, and user index or master user is 3, and password or zone flag is Techno@123, and center index or details is the saved UIN
And I set context where context key is env_context, and pre-requisite details is the saved environment 3 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is null and null and null and null and null and null
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is INTERNAL_WORKFLOW_ACTION, and stage status is SUCCESS
And I delete packet data

  @scenario_76
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. When the packet is created and uploaded with supervisor biometric and without operator biometrics
Given I get ping health where component is packetcreator
And I wait where wait seconds is 45
And I user where user action is ADD_User, and user index or master user is 76, and password or zone flag is Techno@123 and store result in user76
And I center where call type is CREATE, and user details is the saved user76, and center index is 76, and center active flag is T and store result in center76
And I machine where call type is CREATE, and center details is the saved center76, and center index is 76 and store result in details76
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 76, and password or zone flag is Techno@123, and center index or details is the saved details76 and store result in details76
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details76 and store result in details76
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details76
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details76
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details76, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details76, and password or zone flag is 76
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details76, and password or zone flag is T
And I write pre req where environment details is the saved details76, and pre-requisite data index is 76
And I read pre req where pre-requisite data index is 76 and store result in details76
And I set context where context key is env_context, and pre-requisite details is the saved details76, and generate private key is true
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I user where user action is UPDATE_UIN, and user index or master user is 76, and password or zone flag is Techno@123, and center index or details is the saved UIN
And I set context where context key is env_context, and pre-requisite details is the saved details76, and generate private key is true, and registration status or invalidation flag is null, and negative test or signature flag is valid and null and null and null and null and supervisorBiometrics_bio_CBEFF
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path1
And I get packet template where packet type is NEW, and persona file path is the saved persona file path1 and store result in template path1
And I packetcreator where packet type is NEW, and template path is the saved template path1 and store result in zip packet path1
And I ridsync where packet type is NEW, and packet zip path is the saved zip packet path1 and store result in rid1
And I packetsync where packet zip path is the saved zip packet path1
And I set context where context key is env_context, and pre-requisite details is the saved details76, and generate private key is true
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
Then I check ridstage where registration ID is the saved rid1, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I machine where call type is DCOM, and center details is the saved details76
Then I user where user action is DELETE_CENTERMAPPING, and user index or master user is 76, and password or zone flag is Techno@123, and center index or details is the saved details76 and store result in details76
Then I center where call type is DCOM, and user details is the saved details76, and center index is 76
And I delete packet data

  @scenario_78
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident booked pre-registration with support documents. Later cancels booked appointment and changes the slot. walk-ins to registration center completes the process and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I send otp where persona file path is the saved persona file path and store result in email
And I validate otp where persona file path is the saved persona file path, and email is the saved email
And I pre register where persona file path is the saved persona file path and store result in pre-registration ID
And I upload documents where persona file path is the saved persona file path, and pre-registration ID is the saved pre-registration ID
And I update pre reg status where status code is 0, and pre-registration ID is the saved pre-registration ID, and validation mode is valid
And I book appointment where holiday booking flag is false, and pre-registration ID is the saved pre-registration ID, and slot number is 2
And I cancel appointment where cancel status type is cancel, and pre-registration ID is the saved pre-registration ID
And I book appointment where holiday booking flag is false, and pre-registration ID is the saved pre-registration ID, and slot number is 3
And I generate and upload packet where pre-registration ID is the saved pre-registration ID, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_79
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident attempts to obtain UIN but Packet Creation Date is past date and gets the UIN successfully
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is invalidCreationDate=-1y
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I delete packet data

  @scenario_80
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident booked pre-registration with support documents and already used the appoinment. walks in to registration center with consumed PRID to get the UIN
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I send otp where persona file path is the saved persona file path and store result in email
And I validate otp where persona file path is the saved persona file path, and email is the saved email
And I pre register where persona file path is the saved persona file path and store result in pre-registration ID
And I upload documents where persona file path is the saved persona file path, and pre-registration ID is the saved pre-registration ID
And I update pre reg status where status code is 0, and pre-registration ID is the saved pre-registration ID, and validation mode is valid
And I book appointment where holiday booking flag is false, and pre-registration ID is the saved pre-registration ID, and slot number is 1
And I generate and upload packet where pre-registration ID is the saved pre-registration ID, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I generate and upload packet where pre-registration ID is the saved pre-registration ID, and packet template path is the saved packet template path and store result in second registration ID
And I post mock mv where registration ID is the saved second registration ID, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is the saved second registration ID
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is DEMOGRAPHIC_VERIFICATION, and stage status is FAILED
And I delete packet data

  @scenario_52
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Check Syncdata response with upper key index and user with valid roles
Given I get ping health where component is packetcreator
And I user where user action is ADD_User, and user index or master user is 52, and password or zone flag is Techno@123 and store result in user52
And I center where call type is CREATE, and user details is the saved user52, and center index is 52, and center active flag is T and store result in center52
And I machine where call type is CREATE, and center details is the saved center52, and center index is 52 and store result in details52
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 52, and password or zone flag is Techno@123, and center index or details is the saved details52 and store result in details52
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details52 and store result in details52
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details52
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details52
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details52, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details52, and password or zone flag is 52
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details52, and password or zone flag is T
And I write pre req where environment details is the saved details52, and pre-requisite data index is 52
And I read pre req where pre-requisite data index is 52 and store result in details52
And I wait where wait seconds is 9
And I sync data where call type is TPM_VERIFY, and machine details is the saved details52, and key case or center index is UPPER and store result in key index
And I sync data where call type is CLIENT_SETTINGS_VALID, and machine details is key index, and key case or center index is 52
And I sync data where call type is LATEST_ID_SCHEMA
And I sync data where call type is CONFIGS_KEYINDEX, and machine details is key index
And I sync data where call type is USER_DETAILS, and machine details is the saved details52
And I machine where call type is DCOM, and center details is the saved details52
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 52, and password or zone flag is Techno@123, and center index or details is the saved details52 and store result in details52
And I center where call type is DCOM, and user details is the saved details52, and center index is 52
And I delete packet data

  @scenario_53
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Update machine from centerA to centerB and verify syncdata client settings with centerA
Given I get ping health where component is packetcreator
And I wait where wait seconds is 30
And I user where user action is ADD_User, and user index or master user is 53, and password or zone flag is Techno@123 and store result in user53
And I center where call type is CREATE, and user details is the saved user53, and center index is 53, and center active flag is T and store result in center53
And I machine where call type is CREATE, and center details is the saved center53, and center index is 53 and store result in details53
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 53, and password or zone flag is Techno@123, and center index or details is the saved details53 and store result in details53
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details53 and store result in details53
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details53
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details53
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details53, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details53, and password or zone flag is 53
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details53, and password or zone flag is T
And I write pre req where environment details is the saved details53, and pre-requisite data index is 53
And I user where user action is ADD_User, and user index or master user is 531, and password or zone flag is Techno@123 and store result in user531
And I center where call type is CREATE, and user details is the saved user531, and center index is 531, and center active flag is T and store result in center531
And I machine where call type is CREATE, and center details is the saved center531, and center index is 531 and store result in details531
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 531, and password or zone flag is Techno@123, and center index or details is the saved details531 and store result in details531
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details531 and store result in details531
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details531
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details531
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details531, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details531, and password or zone flag is 531
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details531, and password or zone flag is T
And I write pre req where environment details is the saved details531, and pre-requisite data index is 531
And I read pre req where pre-requisite data index is 53 and store result in details53
And I wait where wait seconds is 9
And I sync data where call type is TPM_VERIFY, and machine details is the saved details53, and key case or center index is UPPER and store result in key index
And I sync data where call type is CLIENT_SETTINGS_VALID, and machine details is key index, and key case or center index is 53
And I read pre req where pre-requisite data index is 531 and store result in details531
And I machine where call type is UPDATE, and center details is the saved details531, and center index is 531 and store result in details53
And I sync data where call type is CLIENT_SETTINGS_INVALID, and machine details is key index, and key case or center index is 531
And I machine where call type is UPDATE, and center details is the saved details53, and center index is 53 and store result in details53
And I machine where call type is DCOM, and center details is the saved details531
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 531, and password or zone flag is Techno@123, and center index or details is the saved details531 and store result in details531
And I center where call type is DCOM, and user details is the saved details531, and center index is 531
And I delete packet data

  @scenario_54
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Inactive machine and verify syncdata client settings calls
Given I get ping health where component is packetcreator
And I wait where wait seconds is 40
And I user where user action is ADD_User, and user index or master user is 54, and password or zone flag is Techno@123 and store result in user54
And I center where call type is CREATE, and user details is the saved user54, and center index is 54, and center active flag is T and store result in center54
And I machine where call type is CREATE, and center details is the saved center54, and center index is 54 and store result in details54
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 54, and password or zone flag is Techno@123, and center index or details is the saved details54 and store result in details54
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details54 and store result in details54
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details54
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details54
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details54, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details54, and password or zone flag is 54
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details54, and password or zone flag is T
And I write pre req where environment details is the saved details54, and pre-requisite data index is 54
And I read pre req where pre-requisite data index is 54 and store result in details54
And I machine where call type is ACTIVE_FLAG, and center details is the saved details54, and center index is 54, and machine active flag is F
And I wait where wait seconds is 9
And I sync data where call type is TPM_VERIFY, and machine details is the saved details54, and key case or center index is UPPER and store result in key index
And I sync data where call type is CLIENT_SETTINGS_VALID, and machine details is key index, and key case or center index is 54
And I sync data where call type is CONFIGS_KEYINDEX, and machine details is key index
And I sync data where call type is USER_DETAILS, and machine details is the saved details54
And I machine where call type is ACTIVE_FLAG, and center details is the saved details54, and center index is 54, and machine active flag is T
And I machine where call type is DCOM, and center details is the saved details54
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 54, and password or zone flag is Techno@123, and center index or details is the saved details54 and store result in details54
And I center where call type is DCOM, and user details is the saved details54, and center index is 54
And I delete packet data

  @scenario_55
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Decomission machine and verify syncdata client settings calls
Given I get ping health where component is packetcreator
And I wait where wait seconds is 50
And I user where user action is ADD_User, and user index or master user is 55, and password or zone flag is Techno@123 and store result in user55
And I center where call type is CREATE, and user details is the saved user55, and center index is 55, and center active flag is T and store result in center55
And I machine where call type is CREATE, and center details is the saved center55, and center index is 55 and store result in details55
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 55, and password or zone flag is Techno@123, and center index or details is the saved details55 and store result in details55
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details55 and store result in details55
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details55
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details55
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details55, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details55, and password or zone flag is 55
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details55, and password or zone flag is T
And I write pre req where environment details is the saved details55, and pre-requisite data index is 55
And I read pre req where pre-requisite data index is 55 and store result in details55
And I machine where call type is DCOM, and center details is the saved details55
And I wait where wait seconds is 9
And I sync data where call type is TPM_VERIFY_INVALID, and machine details is the saved details55, and key case or center index is UPPER and store result in key index
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 55, and password or zone flag is Techno@123, and center index or details is the saved details55 and store result in details55
And I center where call type is DCOM, and user details is the saved details55, and center index is 55
And I delete packet data

  @scenario_56
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Decommission center and verify USER_DETAILS syncdata calls
Given I get ping health where component is packetcreator
And I wait where wait seconds is 60
And I user where user action is ADD_User, and user index or master user is 56, and password or zone flag is Techno@123 and store result in user56
And I center where call type is CREATE, and user details is the saved user56, and center index is 56, and center active flag is T and store result in center56
And I machine where call type is CREATE, and center details is the saved center56, and center index is 56 and store result in details56
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 56, and password or zone flag is Techno@123, and center index or details is the saved details56 and store result in details56
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details56 and store result in details56
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details56
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details56
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details56, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details56, and password or zone flag is 56
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details56, and password or zone flag is T
And I write pre req where environment details is the saved details56, and pre-requisite data index is 56
And I read pre req where pre-requisite data index is 56 and store result in details56
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 56, and password or zone flag is Techno@123, and center index or details is the saved details56 and store result in details56
And I machine where call type is REMOVE_CENTER, and center details is the saved details56
And I center where call type is DCOM, and user details is the saved details56, and center index is 56
And I wait where wait seconds is 9
And I sync data where call type is TPM_VERIFY, and machine details is the saved details56, and key case or center index is UPPER and store result in key index
And I sync data where call type is CONFIGS_KEYINDEX, and machine details is key index
And I delete packet data

  @scenario_57
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Deactivate center and verify CLIENT_SETTINGS syncdata client settings calls
Given I get ping health where component is packetcreator
And I wait where wait seconds is 70
And I user where user action is ADD_User, and user index or master user is 57, and password or zone flag is Techno@123 and store result in user57
And I center where call type is CREATE, and user details is the saved user57, and center index is 57, and center active flag is T and store result in center57
And I machine where call type is CREATE, and center details is the saved center57, and center index is 57 and store result in details57
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 57, and password or zone flag is Techno@123, and center index or details is the saved details57 and store result in details57
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details57 and store result in details57
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details57
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details57
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details57, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details57, and password or zone flag is 57
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details57, and password or zone flag is T
And I write pre req where environment details is the saved details57, and pre-requisite data index is 57
And I read pre req where pre-requisite data index is 57 and store result in details57
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 57, and password or zone flag is Techno@123, and center index or details is the saved details57 and store result in details57
And I machine where call type is REMOVE_CENTER, and center details is the saved details57
And I center where call type is ACTIVE_FLAG, and user details is the saved details57, and center index is 57, and center active flag is F
And I wait where wait seconds is 9
And I sync data where call type is TPM_VERIFY, and machine details is the saved details57, and key case or center index is UPPER and store result in key index
And I sync data where call type is CONFIGS_KEYINDEX, and machine details is key index
And I sync data where call type is CLIENT_SETTINGS_INVALID, and machine details is key index, and key case or center index is 57
And I machine where call type is DCOM, and center details is the saved details57
And I center where call type is DCOM, and user details is the saved details57, and center index is 57
And I delete packet data

  @scenario_81
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process but packet gets reprocessed before Abis returns for success check
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I get bio modality hash where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path and store result in modality hash value
And I configure mock abis where persona ID is -1, and modality subtypes is Right IndexFinger, and duplicate match flag is false, and hash modality keys is Right IndexFinger, and persona path is the saved persona file path, and modality hash map is the saved modality hash value, and delay seconds is -1, and mock ABIS status is Success
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I delete mock expect where modality hash value is the saved modality hash value
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_82
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process but packet gets reprocessed before Abis returns for fail check
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I get bio modality hash where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path and store result in modality hash value
And I configure mock abis where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and duplicate match flag is false, and hash modality keys is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path, and modality hash map is the saved modality hash value, and delay seconds is delay, and mock ABIS status is 10 and Error
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is BIOGRAPHIC_VERIFICATION, and stage status is REPROCESS
And I delete mock expect
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_83
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process with biometric exception for iris and gets UIN card. Later updates Biometric data for face and iris and performs authentication with face biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Iris:Left and Iris:Right
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is face and iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is faceDevice, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I oidc client and store result in OIDC client ID
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id1 and store result in transaction id1
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id2 and store result in transaction id2
And I bio esignet authentication where device info file is faceDevice, and UIN is the saved second UIN, and persona file path is the saved persona file path, and UIN transaction ID is the saved transaction id1, and VID is the saved VID, and VID transaction ID is the saved transaction id2
And I user info where transaction ID is the saved transaction id, and OIDC client ID is the saved OIDC client ID
And I delete packet data

  @scenario_77
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. When the packet is created and uploaded with operator and without supervisor biometrics
Given I get ping health where component is packetcreator
And I wait where wait seconds is 55
And I user where user action is ADD_User, and user index or master user is 77, and password or zone flag is Techno@123 and store result in user77
And I center where call type is CREATE, and user details is the saved user77, and center index is 77, and center active flag is T and store result in center77
And I machine where call type is CREATE, and center details is the saved center77, and center index is 77 and store result in details77
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 77, and password or zone flag is Techno@123, and center index or details is the saved details77 and store result in details77
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details77 and store result in details77
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details77
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details77
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details77, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details77, and password or zone flag is 77
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details77, and password or zone flag is T
And I write pre req where environment details is the saved details77, and pre-requisite data index is 77
And I read pre req where pre-requisite data index is 77 and store result in details77
And I set context where context key is env_context, and pre-requisite details is the saved details77, and generate private key is true
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I user where user action is UPDATE_UIN, and user index or master user is 77, and password or zone flag is Techno@123, and center index or details is the saved UIN
And I set context where context key is env_context, and pre-requisite details is the saved details77, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is null and null and valid and null and operatorBiometrics_bio_CBEFF and null
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path1
And I get packet template where packet type is NEW, and persona file path is the saved persona file path1 and store result in template path1
And I packetcreator where packet type is NEW, and template path is the saved template path1 and store result in zip packet path1
And I ridsync where packet type is NEW, and packet zip path is the saved zip packet path1 and store result in rid1
And I packetsync where packet zip path is the saved zip packet path1
And I set context where context key is env_context, and pre-requisite details is the saved details77, and generate private key is false
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
Then I check ridstage where registration ID is the saved rid1, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I machine where call type is DCOM, and center details is the saved details77
Then I user where user action is DELETE_CENTERMAPPING, and user index or master user is 77, and password or zone flag is Techno@123/**PASSWORD/, and center index or details is the saved details77 and store result in details77
Then I center where call type is DCOM, and user details is the saved details77, and center index is 77
And I delete packet data

  @scenario_84
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process but the device certificate got expired before uploading the packet
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is true
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is reregister, and registration ID is the saved registration ID
And I delete packet data

  @scenario_85
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But due to low biometric image quality.. correction flow is initiated. Resident provides biometrics with good quality but invalid correction request ID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_85, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path, and biometric quality score is 15 and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path2
And I get packet template where packet type is BIOMETRIC_CORRECTION, and persona file path is the saved persona file path2 and store result in template path2
And I packetcreator where packet type is BIOMETRIC_CORRECTION, and template path is the saved template path2, and additional info request ID is 11111111111111111111111111111 and store result in zip packet path2
And I ridsync where packet type is BIOMETRIC_CORRECTION, and packet zip path is the saved zip packet path2, and additional info request ID is 11111111111111111111111111111 and store result in second registration ID
And I packetsync where packet zip path is the saved zip packet path2, and expected sync result is false
And I delete packet data

  @scenario_86
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But due to low biometric image quality. correction flow is initiated. Resident provides biometrics with good quality but biometrics matches with other resident biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_86, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path, and biometric quality score is 10 and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
And I get additional req id where email prefix is additionalReqId_86 and store result in additional req id
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path1
And I get packet template where packet type is BIOMETRIC_CORRECTION, and persona file path is the saved persona file path1 and store result in template path1
And I get bio modality hash where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path1 and store result in modality hash value
And I configure mock abis where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and duplicate match flag is true, and hash modality keys is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path1, and modality hash map is the saved modality hash value, and delay seconds is -1, and mock ABIS status is Duplicate
And I packetcreator where packet type is BIOMETRIC_CORRECTION, and template path is the saved template path1, and additional info request ID is additional req id and store result in zip packet path1
And I ridsync where packet type is BIOMETRIC_CORRECTION, and packet zip path is the saved zip packet path1, and additional info request ID is additional req id and store result in rid1
And I packetsync where packet zip path is the saved zip packet path1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I delete packet data

  @scenario_87
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But due to low biometric image quality. correction flow is initiated. Resident provides biometrics with good quality but multiple correction packets with same correction Request ID. Only first correction packet will be processed and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_87, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path, and biometric quality score is 10 and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is the saved registration ID, and RID stage is INTERNAL_WORKFLOW_ACTION, and stage status is SUCCESS
Then I get additional req id where email prefix is additionalReqId_87 and store result in additional req id
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path2
And I get packet template where packet type is BIOMETRIC_CORRECTION, and persona file path is the saved persona file path2 and store result in template path2
And I packetcreator where packet type is BIOMETRIC_CORRECTION, and template path is the saved template path2, and additional info request ID is additional req id and store result in zip packet path2
And I ridsync where packet type is BIOMETRIC_CORRECTION, and packet zip path is the saved zip packet path2, and additional info request ID is additional req id and store result in second registration ID
And I packetsync where packet zip path is the saved zip packet path2
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path3
And I get packet template where packet type is BIOMETRIC_CORRECTION, and persona file path is the saved persona file path3 and store result in template path3
And I packetcreator where packet type is BIOMETRIC_CORRECTION, and template path is the saved template path3, and additional info request ID is additional req id and store result in zip packet path3
And I ridsync where packet type is BIOMETRIC_CORRECTION, and packet zip path is the saved zip packet path3, and additional info request ID is additional req id and store result in rid3
And I packetsync where packet zip path is the saved zip packet path3
Then I check ridstage where registration ID is the saved rid3, and RID stage is SECUREZONE_NOTIFICATION, and stage status is REJECTED
And I delete packet data

  @scenario_89
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But due to low biometric image quality. correction flow is initiated. Resident provides biometrics but still quality is not good. After max number of corrections (correction packets)the original packet gets rejected
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_89, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path, and biometric quality score is 15 and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is the saved registration ID, and RID stage is INTERNAL_WORKFLOW_ACTION, and stage status is SUCCESS, and sub-status is RPR-WIA-001
Then I get additional req id where email prefix is additionalReqId_89 and store result in additional req id
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path2
And I get packet template where packet type is BIOMETRIC_CORRECTION, and persona file path is the saved persona file path2, and biometric quality score is 20 and store result in template path2
And I packetcreator where packet type is BIOMETRIC_CORRECTION, and template path is the saved template path2, and additional info request ID is additional req id and store result in zip packet path2
And I ridsync where packet type is BIOMETRIC_CORRECTION, and packet zip path is the saved zip packet path2, and additional info request ID is additional req id and store result in second registration ID
And I packetsync where packet zip path is the saved zip packet path2
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get additional req id where email prefix is additionalReqId_89 and store result in additional req id2
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path3
And I get packet template where packet type is BIOMETRIC_CORRECTION, and persona file path is the saved persona file path3, and biometric quality score is 30 and store result in template path3
And I packetcreator where packet type is BIOMETRIC_CORRECTION, and template path is the saved template path3, and additional info request ID is additional req id2 and store result in zip packet path3
And I ridsync where packet type is BIOMETRIC_CORRECTION, and packet zip path is the saved zip packet path3, and additional info request ID is additional req id2 and store result in rid3
And I packetsync where packet zip path is the saved zip packet path3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I check status where packet status is REJECTED, and registration ID is the saved registration ID
And I delete packet data

  @scenario_90
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But due to low biometric image quality. correction flow is initiated. Resident provides biometrics with good quality but by then packet kicks-in for the original packet
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_90, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path, and biometric quality score is 15 and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is the saved registration ID, and RID stage is INTERNAL_WORKFLOW_ACTION, and stage status is SUCCESS, and sub-status is RPR-WIA-001
Then I get additional req id where email prefix is additionalReqId_90 and store result in additional req id
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path2
And I get packet template where packet type is BIOMETRIC_CORRECTION, and persona file path is the saved persona file path2 and store result in template path2
And I packetcreator where packet type is BIOMETRIC_CORRECTION, and template path is the saved template path2, and additional info request ID is additional req id and store result in zip packet path2
And I wait till reprocessor interval
And I ridsync where packet type is BIOMETRIC_CORRECTION, and packet zip path is the saved zip packet path2, and additional info request ID is additional req id and store result in second registration ID
And I packetsync where packet zip path is the saved zip packet path2
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I delete packet data

  @scenario_91
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process. But due to low biometric image quality. correction flow is initiated. Resident provides biometrics with good quality but by the time correction packet timeout happens. So the original packet gets rejected
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_91, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path, and biometric quality score is 15 and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is the saved registration ID, and RID stage is INTERNAL_WORKFLOW_ACTION, and stage status is SUCCESS, and sub-status is RPR-WIA-001
Then I get additional req id where email prefix is additionalReqId_91 and store result in additional req id
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path2
And I get packet template where packet type is BIOMETRIC_CORRECTION, and persona file path is the saved persona file path2 and store result in template path2
And I packetcreator where packet type is BIOMETRIC_CORRECTION, and template path is the saved template path2, and additional info request ID is additional req id and store result in zip packet path2
And I wait till reprocessor interval
And I ridsync where packet type is BIOMETRIC_CORRECTION, and packet zip path is the saved zip packet path2, and additional info request ID is additional req id and store result in second registration ID
And I packetsync where packet zip path is the saved zip packet path2
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I delete packet data

  @scenario_92
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident Minor Child walks into registration center wants to get UIN Guardian Details. Later again tries to get another UIN
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with rid where persona file path is the saved parent persona file path, and registration ID is the saved parent registration ID
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child rid2
And I post mock mv where registration ID is the saved child rid2, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is the saved child rid2
And I delete packet data

  @scenario_93
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Inji - Resident walks into registration center completes the process with low(30KB) face image size and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=john, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I delete packet data

  @scenario_94
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Inji - Resident walks into registration center completes the process with high(276KB) face image size and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=rob, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I delete packet data

  @scenario_95
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Two resident walks into registration center tries to get UIN with different demographic details and same biometrics except one finger
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I wait where wait seconds is 10
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved rid1, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null and 99
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in template path new
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved template path new and store result in second registration ID
And I wait where wait seconds is 10
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I get email by uin where resident UIN is the saved second UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_96
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Two resident walks into registration center tries to get UIN with same demographic details and same biometrics except one finger
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=john, and persona file is the saved persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phone=9513209874, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I wait where wait seconds is 10
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved rid1, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null and 99
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=john, and persona file is the saved persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phone=9513209874, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in template path new
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved template path new and store result in second registration ID
And I wait where wait seconds is 10
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_97
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process with only face biometrics and without exception
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and false and false and true and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_98
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process without biometrics and exception
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and false and false and false and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_99
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor Child walks into registration center gets UIN with parent RID details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
And I delete packet data

  @scenario_100
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor Child walks into registration center tries to get UIN with parent RID details without biometrics and without Exception
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and false and false and false and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved child registration ID
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_101
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor Child walks into registration center tries to get UIN with parent RID details with only face biometrics and without Exception
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_102
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: Resident Infant walks into registration center gets UIN with parent RID details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_103
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: Resident Infant walks into registration center tries to get UIN with parent RID details without biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and false and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_104
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor Child walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later performs biometric demographic OTP authentication both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I wait where wait seconds is UIN_WAIT_TIME
Then I credential request where UIN is the saved child UIN, and email is the saved email and store result in credential request ID
Then I check credential status where credential request ID is the saved credential request ID
Then I verify notification where notification type is Credential Issuance Status, and email is the saved email
Then I generate vidwithout otp where VID type is Perpetual, and UIN is the saved child UIN and store result in vidwithoutotp
And I wait where wait seconds is 90
And I demo authentication where demo field is name, and UIN is the saved child UIN, and persona file path is the saved child persona file path, and VID is the saved vidwithoutotp
And I bio authentication where device info file is faceDevice, and UIN is the saved child UIN, and VID is the saved vidwithoutotp, and persona file path is the saved child persona file path
And I bio authentication where device info file is LeftIris, and UIN is the saved child UIN, and VID is the saved vidwithoutotp, and persona file path is the saved child persona file path
And I bio authentication where device info file is leftRingDevice, and UIN is the saved child UIN, and VID is the saved vidwithoutotp, and persona file path is the saved child persona file path
And I otp authentication where UIN is the saved UIN, and VID is the saved child UIN, and email is the saved VID, and parameter 4 is the saved vidwithoutotp, and parameter 5 is the saved email
And I delete packet data

  @scenario_105
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: Resident Minor Child walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later performs biometric demographic authentication both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I wait where wait seconds is UIN_WAIT_TIME
Then I generate vidwithout otp where VID type is Perpetual, and UIN is the saved child UIN and store result in vidwithoutotp
Then I wait where wait seconds is UIN_WAIT_TIME
Then I demo authentication where demo field is name, and UIN is the saved child UIN, and persona file path is the saved child persona file path, and VID is the saved vidwithoutotp
And I delete packet data

  @scenario_106
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor Child walks into registration center tries get Lost UIN
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I get packet template where packet type is LOST, and persona file path is the saved child persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in rid lost
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is PROCESSED, and registration ID is rid lost
And I get uin by rid where source registration ID is rid lost and store result in second UIN
And I get email by uin where resident UIN is the saved second UIN and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
Then I check ridstage where registration ID is rid lost, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_107
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center tries get Lost UIN but Biometric did not match
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is face and iris and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I get packet template where packet type is LOST, and persona file path is the saved persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in rid lost
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is REJECTED, and registration ID is rid lost
And I delete packet data

  @scenario_108
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident Minor Child walks  into registration center tries get UIN when the parent packet is in the queue for manual verification reject child packet only when parent packet is rejected
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I get bio modality hash where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and persona path is the saved parent persona file path and store result in modality hash value
And I configure mock abis where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and duplicate match flag is true, and hash modality keys is Right IndexFinger and Left LittleFinger, and persona path is the saved parent persona file path, and modality hash map is the saved modality hash value, and delay seconds is -1, and mock ABIS status is Duplicate
And I packetcreator where packet type is NEW, and template path is the saved parent packet template path and store result in parent zip packet path
And I ridsync where packet type is NEW, and packet zip path is parent zip packet path and store result in parent registration ID
And I update resident with rid where persona file path is the saved parent persona file path, and registration ID is the saved parent registration ID
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I packetcreator where packet type is NEW, and template path is the saved child packet template path and store result in child zip packet path
And I ridsync where packet type is NEW, and packet zip path is child zip packet path and store result in child registration ID
And I packetsync where packet zip path is parent zip packet path
And I packetsync where packet zip path is child zip packet path
And I post mock mv where registration ID is the saved parent registration ID, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is the saved parent registration ID
And I check status where packet status is REJECTED, and registration ID is the saved child registration ID
And I delete packet data

  @scenario_109
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident Infant tries to get UIN without Introducer
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is ERROR
And I delete packet data

  @scenario_110
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later performs demographic authentication using name both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is rightlittleFinger and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I credential request where UIN is the saved UIN, and email is the saved email and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I verify notification where notification type is Credential Issuance Status, and email is the saved email
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I demo authentication where demo field is name, and UIN is the saved UIN, and persona file path is the saved persona file path, and VID is the saved VID
And I delete packet data

  @scenario_111
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident Minor walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later performs eKYC demographic authentication using name both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I generate vidwithout otp where VID type is Perpetual, and UIN is the saved child UIN and store result in vidwithoutotp
And I wait where wait seconds is 90
And I ekyc demo where demo field is name, and UIN is the saved child UIN, and persona file path is the saved child persona file path, and VID is the saved vidwithoutotp and store result in ekycData
And I validate kyc data where KYC field is photo, and response variable is ekycData
And I delete packet data

  @scenario_112
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident Infant walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later performs demographic authentication using name both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
And I wait where wait seconds is 90
And I generate vidwithout otp where VID type is Perpetual, and UIN is the saved child UIN and store result in vidwithoutotp
And I wait where wait seconds is 90
And I ekyc demo where demo field is name, and UIN is the saved child UIN, and persona file path is the saved child persona file path, and VID is the saved vidwithoutotp and store result in ekycData
And I validate kyc data where KYC field is photo, and response variable is ekycData
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_113
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center gets the lost UIN updates his demo graphic details Later performs demo Auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I get packet template where packet type is LOST, and persona file path is the saved persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in rid lost
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is PROCESSED, and registration ID is rid lost
And I get uin by rid where source registration ID is rid lost and store result in second UIN
And I get email by uin where resident UIN is the saved second UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is rid lost, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
Then I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
Then I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in new packet template path
Then I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in lost uin
And I verify notification where notification type is updated, and email is the saved email
And I generate vidwithout otp where VID type is Perpetual, and UIN is lost uin and store result in vidwithoutotp
And I wait where wait seconds is UIN_WAIT_TIME
And I get email by uin where resident UIN is lost uin and store result in email
And I credential request where UIN is lost uin, and email is the saved email and store result in credential request ID
And I check credential status where credential request ID is the saved credential request ID
And I demo authentication where demo field is name, and UIN is lost uin, and persona file path is the saved persona file path, and VID is the saved vidwithoutotp
And I delete packet data

  @scenario_114
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center gets the lost UIN updates his Biometric data Later performs Bio Auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I wait where wait seconds is 90
And I get packet template where packet type is LOST, and persona file path is the saved persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in rid lost
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is PROCESSED, and registration ID is rid lost
And I get uin by rid where source registration ID is rid lost and store result in second UIN
Then I check ridstage where registration ID is rid lost, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I update demo or bio details where bio type is face, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
Then I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
Then I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in new packet template path
Then I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in lost uin
And I generate vidwithout otp where VID type is Perpetual, and UIN is lost uin and store result in vidwithoutotp
And I wait where wait seconds is 90
And I bio authentication where device info file is faceDevice, and UIN is lost uin, and VID is the saved vidwithoutotp, and persona file path is the saved persona file path
And I delete packet data

  @scenario_115
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: Resident Infant walks into registration center gets the UIN with Preregistration with Gaurdian details and later performs demo auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with rid where persona file path is the saved parent persona file path, and registration ID is the saved parent registration ID
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I send otp where persona file path is the saved child persona file path and store result in email
And I validate otp where persona file path is the saved child persona file path, and email is the saved email
And I pre register where persona file path is the saved child persona file path and store result in pre-registration ID
And I upload documents where persona file path is the saved child persona file path, and pre-registration ID is the saved pre-registration ID
And I update pre reg status where status code is 0, and pre-registration ID is the saved pre-registration ID, and validation mode is valid
And I book appointment where holiday booking flag is false, and pre-registration ID is the saved pre-registration ID, and slot number is 1
And I generate and upload packet where pre-registration ID is the saved pre-registration ID, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
And I generate vidwithout otp where VID type is Perpetual, and UIN is the saved child UIN and store result in vidwithoutotp
And I wait where wait seconds is UIN_WAIT_TIME
And I demo authentication where demo field is name, and UIN is the saved child UIN, and persona file path is the saved child persona file path, and VID is the saved vidwithoutotp
Then I check ridstage where registration ID is the saved parent registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_116
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center gets the UIN but during packet generation CBEFF is invalid
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path, and biometric quality score is 80, and generate valid CBEFF is false and store result in packet template path
And I generate and upload packet skipping prereg with invalid cbeff where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_117
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center tries to update the UIN with invalid UIN
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is 1234
And I update demo or bio details where bio type is face and iris and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is ERROR
And I delete packet data

  @scenario_118
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident Infant walks into registration center gets the UIN with finger and eye exception
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left IndexFinger and Finger:Right IndexFinger and Iris:Left
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_119
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Minor_Exc
  Scenario: Resident Minor walks into registration center gets the UIN with parent and child exception mark
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I update bio exception in persona where persona file path is the saved parent persona file path, and biometric exception modalities is Finger:Left IndexFinger and Finger:Right IndexFinger and Iris:Left
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I check tags where registration ID is the saved parent registration ID
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update bio exception in persona where persona file path is the saved child persona file path, and biometric exception modalities is Finger:Left IndexFinger and Finger:Right IndexFinger and Iris:Left
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
And I delete packet data

  @scenario_120
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Minor_Exc
  Scenario: Resident Minor walks into registration center gets the UIN with few exceptions
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update bio exception in persona where persona file path is the saved child persona file path, and biometric exception modalities is Finger:Left IndexFinger and Finger:Right IndexFinger and Iris:Left
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I check tags where registration ID is the saved child registration ID
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
And I delete packet data

  @scenario_121
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center gets the UIN  with All finger and eye exception mark walk-ins to reg-client to get UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left Thumb and Finger:Left IndexFinger and Finger:Left MiddleFinger and Finger:Left RingFinger and Finger:Left LittleFinger and Finger:Right Thumb and Finger:Right IndexFinger and Finger:Right MiddleFinger and Finger:Right RingFinger and Finger:Right LittleFinger and Iris:Left and Iris:Right
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I check tags where registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_122
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor walks into registration center gets the UIN with all finger and eye exception mark
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update bio exception in persona where persona file path is the saved child persona file path, and biometric exception modalities is Finger:Left Thumb and Finger:Left IndexFinger and Finger:Left MiddleFinger and Finger:Left RingFinger and Finger:Left LittleFinger and Finger:Right Thumb and Finger:Right IndexFinger and Finger:Right MiddleFinger and Finger:Right RingFinger and Finger:Right LittleFinger and Iris:Left and Iris:Right
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I check tags where registration ID is the saved child registration ID
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
And I delete packet data

  @scenario_123
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant_Exc
  Scenario: Resident Infant walks into registration center gets the UIN with introducer exception mark
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I update bio exception in persona where persona file path is the saved parent persona file path, and biometric exception modalities is Finger:Left IndexFinger and Finger:Right IndexFinger and Iris:Left
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I check tags where registration ID is the saved parent registration ID
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_124
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: Resident walks into registration center updates biometrics with all exceptions and then downloads the UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left Thumb and Finger:Left IndexFinger and Finger:Left MiddleFinger and Finger:Left RingFinger and Finger:Left LittleFinger and Finger:Right Thumb and Finger:Right IndexFinger and Finger:Right MiddleFinger and Finger:Right RingFinger and Finger:Right LittleFinger and Iris:Left and Iris:Right
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check tags where registration ID is the saved second registration ID
And I delete packet data

  @scenario_125
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: Resident walks into registration center updates biometrics with finger and eye exception and then downloads the UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left IndexFinger and Finger:Right IndexFinger and Iris:Left
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I check tags where registration ID is the saved second registration ID
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_126
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor walks into registration center updates demo details and biometrics with all exceptions and then downloads the UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved child persona file path
And I update bio exception in persona where persona file path is the saved child persona file path, and biometric exception modalities is Finger:Left Thumb and Finger:Left IndexFinger and Finger:Left MiddleFinger and Finger:Left RingFinger and Finger:Left LittleFinger and Finger:Right Thumb and Finger:Right IndexFinger and Finger:Right MiddleFinger and Finger:Right RingFinger and Finger:Right LittleFinger and Iris:Left and Iris:Right
And I update resident with uin where persona file path is the saved child persona file path, and UIN is the saved child UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved child persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I check tags where registration ID is the saved second registration ID
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_127
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor walks into registration center updates demo and biometrics with few exceptions and then downloads the UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved child persona file path
And I update bio exception in persona where persona file path is the saved child persona file path, and biometric exception modalities is Finger:Left IndexFinger and Finger:Right IndexFinger and Iris:Left
And I update resident with uin where persona file path is the saved child persona file path, and UIN is the saved child UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved child persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I check tags where registration ID is the saved second registration ID
And I verify notification where notification type is updated, and email is the saved second email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_128
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: Resident walks into registration center updates demo and biometrics with finger and eye exception and then performs demo and bio auth without exception finger
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left IndexFinger and Finger:Right IndexFinger and Iris:Left
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I check tags where registration ID is the saved second registration ID
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I wait where wait seconds is UIN_WAIT_TIME
Then I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
Then I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I demo authentication where demo field is name, and UIN is the saved UIN, and persona file path is the saved persona file path, and VID is the saved VID
And I bio authentication where device info file is rightThumbDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_129
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: Resident walks into registration center updates demo and biometrics with all exception and then performs demo and bio auth with face
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left Thumb and Finger:Left IndexFinger and Finger:Left MiddleFinger and Finger:Left RingFinger and Finger:Left LittleFinger and Finger:Right Thumb and Finger:Right IndexFinger and Finger:Right MiddleFinger and Finger:Right RingFinger and Finger:Right LittleFinger and Iris:Left and Iris:Right
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I check tags where registration ID is the saved second registration ID
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I wait where wait seconds is UIN_WAIT_TIME
Then I credential request where UIN is the saved second UIN, and email is the saved email and store result in credential request ID
Then I check credential status where credential request ID is the saved credential request ID
Then I verify notification where notification type is Credential Issuance Status, and email is the saved email
Then I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
Then I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I demo authentication where demo field is name, and UIN is the saved UIN, and persona file path is the saved persona file path, and VID is the saved VID
And I bio authentication where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_130
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card. Resident lost UIN and walks into registration center and updates his phone number and address and then retrieves the UIN
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is addressLine1=bnglr and phoneNumber=3938333736, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is LOST, and persona file path is the saved persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in rid lost
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is PROCESSED, and registration ID is rid lost
And I get uin by rid where source registration ID is rid lost and store result in second UIN
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is rid lost, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_131
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_NA
  Scenario: Resident walks into registration center tries to register with predefined blocklisted word in her name
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get blocklisted word where call type is CREATE, and blocklisted word is dslautomation and store result in blocklisted word
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name=blocklisted word, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_132
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New_Exception
  Scenario: A differently abled resident with exception in left eye walks into registration center completes the process and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Iris:Left
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_133
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor Child with age less than 1 year walks into registration center gets UIN with parent RID details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=2023/08/24, and persona file is the saved child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
And I delete packet data

  @scenario_134
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: Resident Infant with age less than 1 year walks into registration center gets UIN with parent RID details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=2023/08/24, and persona file is the saved child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_135
  @Negative_Test
  @persona_NonResidentMaleAdult
  @group_Adult_New
  Scenario: NonResident adult whose phone number is 11 digts walk-ins to registration center gets UIN
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phoneNumber=39383337361, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_136
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: Resident adult without phone number and email walks into registration center and tries to get UIN
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phoneNumber= and email=, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_137
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New_Exception
  Scenario: A differently abled resident with exception in left index finger walks into registration center completes the process and gets UIN card. Later updates all biometrics and using uin check for absence of exception marked modality
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left IndexFinger
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is iris and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I check for bdbabsence where UIN is the saved second UIN, and biometric modalities is FINGER_Left IndexFinger
And I delete packet data

  @scenario_138
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: Resident walks into registration center and gets UIN. Later updates exception for Left Thumb and using UIN check if all modalities are present
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left Thumb
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I check tags where registration ID is the saved second registration ID
And I check for bdbpresence where UIN is the saved second UIN, and biometric modalities is FINGER_Left RingFinger and FINGER_Right LittleFinger and FACE and FINGER_Left LittleFinger and IRIS_Right and FINGER_Left MiddleFinger and FINGER_Left IndexFinger and FINGER_Right IndexFinger and IRIS_Left and FINGER_Right RingFinger and FINGER_Right MiddleFinger and FINGER_Right Thumb, and biometric exception flag is false
And I delete packet data

  @scenario_139
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New_Exception
  Scenario: A differently abled resident walks into registration center completes the process and gets UIN card . Later updates all biometrics and using uin check for the absence of exception marked modality
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Iris:Left and Iris:Right
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is iris and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I check for bdbabsence where UIN is the saved second UIN, and biometric modalities is IRIS_Left and IRIS_Right
And I delete packet data

  @scenario_140
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process and gets UIN card . Using rid check the presence of all the modalities
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check for bdbpresence where UIN is the saved registration ID, and biometric modalities is FINGER_Left RingFinger and FINGER_Right LittleFinger and FACE and FINGER_Left LittleFinger and IRIS_Right and FINGER_Left MiddleFinger and FINGER_Left IndexFinger and FINGER_Right IndexFinger and IRIS_Left and FINGER_Right RingFinger and FINGER_Left Thumb and FINGER_Right MiddleFinger and FINGER_Right Thumb, and biometric exception flag is false/EXCEPTION_FLAG/
And I delete packet data

  @scenario_141
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process by providing the consent and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false/GENERATE_PRIVATE_KEY/, and registration status or invalidation flag is null, and negative test or signature flag is yes
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_142
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process by not providing the consent
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is no
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
And I delete packet data

  @scenario_143
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process and gets UIN card. Later updates dob with invalid format
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=08/24/2023, and persona file is the saved persona file path
Then I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in new packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in second registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved second registration ID
And I delete packet data

  @scenario_144
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Machine got unmapped from the center before generating the offline packet
Given I user where user action is ADD_User, and user index or master user is 6, and password or zone flag is Techno@123 and store result in user6
And I center where call type is CREATE, and user details is the saved user6, and center index is 6, and center active flag is T and store result in center6
And I machine where call type is CREATE, and center details is the saved center6, and center index is 6 and store result in details6
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 6, and password or zone flag is Techno@123, and center index or details is the saved details6 and store result in details6
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details6 and store result in details6
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details6
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details6
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details6, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details6, and password or zone flag is 6
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details6, and password or zone flag is T
And I write pre req where environment details is the saved details6, and pre-requisite data index is 6
And I read pre req where pre-requisite data index is 6 and store result in details6
And I set context where context key is env_context, and pre-requisite details is the saved details6, and generate private key is true
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I machine where call type is REMOVE_CENTER, and center details is the saved center6, and center index is 6
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 6, and password or zone flag is Techno@123, and center index or details is the saved details6 and store result in details6
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is the saved registration ID, and RID stage is CMD_VALIDATION, and stage status is FAILED
And I delete packet data

  @scenario_145
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: An offline packet is generated and machine got unmapped from the center before packet is uploaded
Given I user where user action is ADD_User, and user index or master user is 7, and password or zone flag is Techno@123 and store result in user7
And I center where call type is CREATE, and user details is the saved user7, and center index is 7, and center active flag is T and store result in center7
And I machine where call type is CREATE, and center details is the saved center7, and center index is 7 and store result in details7
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 7, and password or zone flag is Techno@123, and center index or details is the saved details7 and store result in details7
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details7 and store result in details7
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details7
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details7
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details7, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details7, and password or zone flag is 7
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details7, and password or zone flag is T
And I write pre req where environment details is the saved details7, and pre-requisite data index is 7
And I read pre req where pre-requisite data index is 7 and store result in details7
And I set context where context key is env_context, and pre-requisite details is the saved details7, and generate private key is true
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I machine where call type is REMOVE_CENTER, and center details is the saved center7, and center index is 7
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 7, and password or zone flag is Techno@123, and center index or details is the saved details7 and store result in details7
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_146
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New_Exception
  Scenario: A differently abled resident with exception in left and right index finger walks into registration center completes the process reviewer authentication happens and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is null, and change supervisor name flag is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left IndexFinger and Finger:Right IndexFinger
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_147
  @Negative_Test
  @persona_SeniorNonResidentMale
  @group_Senior_New
  Scenario: Senior Non Resident walks into registration center completes the process and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is senior, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_148
  @Positive_Test
  @persona_SeniorResidentMale
  @group_Senior_New
  Scenario: Senior Resident walks into registration center completes the process and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is senior, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_149
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center provides future date as DOB and tries to get uin
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=2027/08/09, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_150
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: Resident Infant walks into registration center gets UIN with parent RID details . Another infant tries to get UIN with the same demographics and parent details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child rid1
And I check status where packet status is PROCESSED, and registration ID is the saved child rid1
And I get uin by rid where source registration ID is the saved child rid1 and store result in child uin1
And I get email by uin where resident UIN is the saved child uin1 and store result in second email
And I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved child rid1, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved child rid1, and RID stage is VERIFICATION, and stage status is SUCCESS
Then I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child rid2
Then I verify notification where notification type is UIN Generated, and email is the saved second email
Then I check ridstage where registration ID is the saved child rid2, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved child rid2, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_151
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process but while the packet getting created packet has invalid encrypted hash
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is null, and change supervisor name flag is null, and invalid encrypted hash flag is invalidEncryptedHash
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_152
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center and tries to complete the process. But during packet sync the checksum is invalid
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null, and negative test or signature flag is null, and change supervisor name flag is null, and invalid encrypted hash flag is null, and invalid checksum flag is invalidCheckSum
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path, and expected sync result is false
And I delete packet data

  @scenario_153
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center gets UIN card . Another resident tries to get UIN with the same demographics and exception marked for all modalities except face
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left Thumb and Finger:Left IndexFinger and Finger:Left MiddleFinger and Finger:Left RingFinger and Finger:Left LittleFinger and Finger:Right Thumb and Finger:Right IndexFinger and Finger:Right MiddleFinger and Finger:Right RingFinger and Finger:Right LittleFinger and Iris:Left and Iris:Right
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in template path1
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved template path1 and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I check tags where registration ID is the saved rid1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved rid1, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_154
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into center to get UIN but supervisor rejects the packet . Same resident tries to get Uin again
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I rid sync rejected where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is REJECTED
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in new packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved rid1, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_155
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into center to get Uin but packet is created with invalid hash. Same resident tries to get Uin by providing all details again
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I upload packet with invalid hash where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in new packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved rid1, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_156
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Packet is created and uploaded with inactive center
Given I user where user action is ADD_User, and user index or master user is 156, and password or zone flag is Techno@123 and store result in user156
And I center where call type is CREATE, and user details is the saved user156, and center index is 156, and center active flag is T and store result in center156
And I machine where call type is CREATE, and center details is the saved center156, and center index is 156 and store result in details156
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 156, and password or zone flag is Techno@123, and center index or details is the saved details156 and store result in details156
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details156 and store result in details156
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details156
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details156
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details156, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details156, and password or zone flag is 156
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details156, and password or zone flag is T
And I write pre req where environment details is the saved details156, and pre-requisite data index is 156
And I read pre req where pre-requisite data index is 156 and store result in details156
And I set context where context key is env_context, and pre-requisite details is the saved details156, and generate private key is true
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 156, and password or zone flag is Techno@123, and center index or details is the saved details156 and store result in details156
And I machine where call type is REMOVE_CENTER, and center details is the saved details156
And I center where call type is ACTIVE_FLAG, and user details is the saved details156, and center index is 156, and center active flag is F
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
Then I machine where call type is DCOM, and center details is the saved details156
Then I center where call type is DCOM, and user details is the saved details156, and center index is 156
And I delete packet data

  @scenario_157
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walk into registration center on a holiday completes the process and tries to get UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is invalidCreationDate=-1d
And I get ping health where component is targetenv
And I holiday declaration where parameter 1 is holidayDate=-1d and store result in holiday id
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I delete holiday where parameter 1 is holiday id
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is CMD_VALIDATION, and stage status is FAILED
And I delete packet data

  @scenario_158
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Packet is created and uploaded with inactive machine
Given I user where user action is ADD_User, and user index or master user is 8, and password or zone flag is Techno@123 and store result in user8
And I center where call type is CREATE, and user details is the saved user8, and center index is 8, and center active flag is T and store result in center8
And I machine where call type is CREATE, and center details is the saved center8, and center index is 8 and store result in details8
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 8, and password or zone flag is Techno@123, and center index or details is the saved details8 and store result in details8
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details8 and store result in details8
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details8
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details8
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details8, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details8, and password or zone flag is 8
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details8, and password or zone flag is T
And I write pre req where environment details is the saved details8, and pre-requisite data index is 8
And I read pre req where pre-requisite data index is 8 and store result in details8
And I set context where context key is env_context, and pre-requisite details is the saved details8, and generate private key is true
And I machine where call type is ACTIVE_FLAG, and center details is the saved details8, and center index is 8, and machine active flag is F
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
Then I machine where call type is DCOM, and center details is the saved details8
Then I user where user action is DELETE_CENTERMAPPING, and user index or master user is 8, and password or zone flag is Techno@123, and center index or details is the saved details8 and store result in details8
Then I center where call type is DCOM, and user details is the saved details8, and center index is 8
And I delete packet data

  @scenario_159
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process tries to get uin with invalid ID Schema version
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is invalidIdSchema
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is REPROCESS
And I delete packet data

  @scenario_160
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID by phone number. Later gets eKYC done both using UIN VID and face auth
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I get phone by uin where parameter 1 is the saved UIN and store result in phone number
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved phone number and store result in VID
And I wait where wait seconds is 90
And I ekyc bio where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_161
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID through phone number. Later performs biometric authentication using right finger both using UIN and VID. Also performs eSignet biometric authentication using right finger both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is rightlittleFinger and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I get phone by uin where parameter 1 is the saved UIN and store result in phone number
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved phone number and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is rightThumbDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I oidc client and store result in OIDC client ID
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id1 and store result in transaction id1
And I oauth details request where OIDC client ID is the saved OIDC client ID, and transaction ID slot is the saved transaction id2 and store result in transaction id2
And I bio esignet authentication where device info file is rightThumbDevice, and UIN is the saved UIN, and persona file path is the saved persona file path, and UIN transaction ID is the saved transaction id1, and VID is the saved VID, and VID transaction ID is the saved transaction id2
And I user info where transaction ID is the saved transaction id, and OIDC client ID is the saved OIDC client ID
And I delete packet data

  @scenario_162
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Packet is created and uploaded with invalid machine
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I update machine in prereq data where machine details is the saved environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is 1 and 2, and registration status or invalidation flag is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path, and sync RID flag is invalidMachine
And I delete packet data

  @scenario_163
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Upload the resident packet again which has been uplodaded and processed already
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is NEW, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I wait where wait seconds is PACKET_UPLOAD_WAIT_TIME
Then I packetsync where packet zip path is the saved packet zip path
Then I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I delete packet data

  @scenario_164
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Skip biometric classification for resident if individual biometric parameter is missing from id json
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is skipBiometricClassification
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I wait where wait seconds is PACKET_UPLOAD_WAIT_TIME
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_165
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Applicant documents are missing in the packet
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is skipApplicantDocuments
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_166
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A non registered resident walks into registration center without UIN and tries to retrieve the UIN.  Now new Resident tries to get UIN with the same demographic details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is LOST, and persona file path is the saved persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in lost rid
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is lost rid, and RID stage is BIOGRAPHIC_VERIFICATION, and stage status is FAILED
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_167
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later performs demographic authentication with age less than actual age both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is rightlittleFinger and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I demo authentication where demo field is age, and UIN is the saved UIN, and persona file path is the saved persona file path, and VID is the saved VID, and age update flag is ageDecrease
And I delete packet data

  @scenario_168
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later performs demographic authentication with exact dob both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is rightlittleFinger and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I demo authentication where demo field is age, and UIN is the saved UIN, and persona file path is the saved persona file path, and VID is the saved VID
And I delete packet data

  @scenario_169
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident attempts to obtain UIN but Packet Creation Date is Null
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is invalidCreationDate
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is CMD_VALIDATION, and stage status is ERROR
And I delete packet data

  @scenario_170
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentA and ResidentB got their UINs and  ResidentB is trying to update ResidentA UIN with his biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path1
And I get packet template where packet type is NEW, and persona file path is the saved persona file path1 and store result in template path1
And I generate and upload packet skipping prereg where persona file path is the saved persona file path1, and packet template path is the saved template path1 and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null and 99
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path2
And I get packet template where packet type is NEW, and persona file path is the saved persona file path2 and store result in template path2
And I generate and upload packet skipping prereg where persona file path is the saved persona file path2, and packet template path is the saved template path2 and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I get email by uin where resident UIN is the saved second UIN and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I update demo or bio details where bio type is face and finger and iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path1, and parameter 5 is the saved persona file path2
And I update resident with uin where persona file path is the saved persona file path1, and UIN is the saved second UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path1 and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path1, and packet template path is the saved update packet template path and store result in rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I verify notification where notification type is updated, and email is the saved email
And I delete packet data

  @scenario_171
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentA and ResidentB got their UINs and  ResidentB is trying to update ResidentA UIN with  his (Resident-B) IRIS biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path1
And I get packet template where packet type is NEW, and persona file path is the saved persona file path1 and store result in template path1
And I generate and upload packet skipping prereg where persona file path is the saved persona file path1, and packet template path is the saved template path1 and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null and 99
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path2
And I get packet template where packet type is NEW, and persona file path is the saved persona file path2 and store result in template path2
And I generate and upload packet skipping prereg where persona file path is the saved persona file path2, and packet template path is the saved template path2 and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I get email by uin where resident UIN is the saved second UIN and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path1, and parameter 5 is the saved persona file path2
And I update resident with uin where persona file path is the saved persona file path1, and UIN is the saved second UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path1 and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path1, and packet template path is the saved update packet template path and store result in rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I verify notification where notification type is updated, and email is the saved email
And I delete packet data

  @scenario_172
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Minor Resident A and Adult Resident B got their UINs and  Resident B is trying to update Resident A UIN with his biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null and 99
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I update demo or bio details where bio type is face and finger and iris, and miss fields is 0, and update attributes is 0, and persona file is the saved child persona file path, and parameter 5 is the saved persona file path
And I update resident with uin where persona file path is the saved child persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved child persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved update packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I verify notification where notification type is updated, and email is the saved email
And I delete packet data

  @scenario_173
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: Infant Resident A and Adult Resident B got their UINs and  Resident B is trying to update Resident A UIN with his biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is VERIFICATION, and stage status is SUCCESS
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null and 99
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I update demo or bio details where bio type is face, and miss fields is 0, and update attributes is 0, and persona file is the saved child persona file path, and parameter 5 is the saved persona file path
And I update resident with uin where persona file path is the saved child persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved child persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved update packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I verify notification where notification type is updated, and email is the saved email
And I delete packet data

  @scenario_174
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process tries to get UIN card with invalid Officer ID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is invalidOfficerID
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_175
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Generate packet with size greater than 2MB
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is updateLargeDocInPersona, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I delete packet data

  @scenario_176
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Generate UIN1 by creating RID1 with Iris exceptions in Profile1 then update UIN1 with Profile1 by capturing all biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Iris:Left and Iris:Right
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I check tags where registration ID is the saved rid1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is iris and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin1
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I delete packet data

  @scenario_177
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Generate UIN1 by creating RID1 with all finger exceptions in Profile1 then update UIN1 with Profile1 by capturing all biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left Thumb and Finger:Left IndexFinger and Finger:Left MiddleFinger and Finger:Left RingFinger and Finger:Left LittleFinger and Finger:Right Thumb and Finger:Right IndexFinger and Finger:Right MiddleFinger and Finger:Right RingFinger and Finger:Right LittleFinger
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I check tags where registration ID is the saved rid1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is finger and iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin1
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I check tags where registration ID is the saved second registration ID
And I verify notification where notification type is updated, and email is the saved email
And I delete packet data

  @scenario_178
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center and Generates UIN1 by creating RID1 with Profile1 then updates UIN1 with Profile1 by capturing all biometric with Abis false
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I get bio modality hash where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path and store result in modality hash value
And I configure mock abis where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and duplicate match flag is false, and hash modality keys is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path, and modality hash map is the saved modality hash value, and delay seconds is -1, and mock ABIS status is Duplicate
And I update demo or bio details where bio type is face and finger and iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin1
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is FAILED, and registration ID is the saved second registration ID
And I delete packet data

  @scenario_179
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentA get uin and ResidentB marking FPs and Iris as exception got their UINs and  ResidentA is trying to update ResidentB with his biometric
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path1
And I get packet template where packet type is NEW, and persona file path is the saved persona file path1 and store result in template path1
And I generate and upload packet skipping prereg where persona file path is the saved persona file path1, and packet template path is the saved template path1 and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null and 99
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path2
And I update bio exception in persona where persona file path is the saved persona file path2, and biometric exception modalities is Finger:Left IndexFinger and Finger:Right IndexFinger and Iris:Left
And I get packet template where packet type is NEW, and persona file path is the saved persona file path2 and store result in template path2
And I generate and upload packet skipping prereg where persona file path is the saved persona file path2, and packet template path is the saved template path2 and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I check tags where registration ID is the saved second registration ID
And I get email by uin where resident UIN is the saved second UIN and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path1, and parameter 5 is the saved persona file path2
And I update resident with uin where persona file path is the saved persona file path1, and UIN is the saved second UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path1 and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path1, and packet template path is the saved update packet template path and store result in rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I verify notification where notification type is updated, and email is the saved email
And I delete packet data

  @scenario_180
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident goes to reg-center and gets UIN same person goes to center and updates his biometrics again he visits the reg-center and updates biometric for his UIN and updates biometrics successfully
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin1
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved second UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved rid3, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check updated uin where parameter 1 is the saved uin1, and parameter 2 is the saved uin3
And I delete packet data

  @scenario_181
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident goes to reg-center and gets UIN same person goes to center and updates his biometrics for 2 consecutive times at the reg-center and updates his/her biometrics successfully resident 2 goes to center and generates UIN later resident 2 tries to update his biometrics with resident 1 biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin1
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved second UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I verify notification where notification type is updated, and email is the saved email
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path2
And I get packet template where packet type is NEW, and persona file path is the saved persona file path2 and store result in template path2
And I generate and upload packet skipping prereg where persona file path is the saved persona file path2, and packet template path is the saved template path2 and store result in rid4
And I check status where packet status is PROCESSED, and registration ID is the saved rid4
And I get uin by rid where source registration ID is the saved rid4 and store result in uin4
And I get email by uin where resident UIN is the saved uin4 and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin4
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in rid5
And I check status where packet status is PROCESSED, and registration ID is the saved rid5
And I get uin by rid where source registration ID is the saved rid5 and store result in uin5
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved rid5, and RID stage is MANUAL_ADJUDICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_182
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into reg-center and generates UIN1 same person tried to get UIN2 and UIN3 (based on MV decision) resident tries to update UIN1 with biometrics and uploads packet and updates successfully
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in template path2
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved template path2 and store result in second registration ID
And I post mock mv where registration ID is the saved second registration ID, and manual verification decision is PROCESSED
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I get email by uin where resident UIN is the saved second UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in update template3
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update template3 and store result in rid3
And I post mock mv where registration ID is the saved rid3, and manual verification decision is PROCESSED
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I get email by uin where resident UIN is the saved uin3 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I get bio modality hash where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path and store result in modality hash value
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin1
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update template4
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update template4 and store result in rid4
And I check status where packet status is PROCESSED, and registration ID is the saved rid4
And I get uin by rid where source registration ID is the saved rid4 and store result in uin4
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved rid4, and RID stage is MANUAL_ADJUDICATION, and stage status is SUCCESS
Then I check updated uin where parameter 1 is the saved uin1, and parameter 2 is the saved uin4
And I delete packet data

  @scenario_183
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into reg-center and generated UIN1 same person tried to get UIN 2 and failed to UIN as per MV decision resident tries to update biometrics with UIN1 and updates successfully
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in second registration ID
And I post mock mv where registration ID is the saved second registration ID, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is the saved second registration ID
And I update demo or bio details where bio type is iris and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin1
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved rid3, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check updated uin where parameter 1 is the saved uin1, and parameter 2 is the saved uin3
And I delete packet data

  @scenario_184
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A differently abled resident walks into registration center completes the process with IRIS as exception and gets UIN card Later performs biometric authentication using Face Fingerprint and IRIS
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Iris:Left and Iris:Right
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I bio authentication where device info file is rightThumbDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I bio authentication where device info file is LeftIris, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path, and scenario flow is ERROR
And I delete packet data

  @scenario_185
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A differently abled resident walks into registration center completes the process with Fingerprints as exception and gets UIN card Later performs biometric authentication using Face Fingerprint and IRIS
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left Thumb and Finger:Left IndexFinger and Finger:Left MiddleFinger and Finger:Left RingFinger and Finger:Left LittleFinger and Finger:Right Thumb and Finger:Right IndexFinger and Finger:Right MiddleFinger and Finger:Right RingFinger and Finger:Right LittleFinger
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I bio authentication where device info file is LeftIris, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I bio authentication where device info file is rightThumbDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path, and scenario flow is ERROR
And I delete packet data

  @scenario_186
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A differently abled resident walks into registration center completes the process with IRIS and Finger as exception and gets UIN card Later performs biometric authentication using Face Fingerprint and IRIS
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left Thumb and Finger:Left IndexFinger and Finger:Left MiddleFinger and Finger:Left RingFinger and Finger:Left LittleFinger and Finger:Right Thumb and Finger:Right IndexFinger and Finger:Right MiddleFinger and Finger:Right RingFinger and Finger:Right LittleFinger and Iris:Left and Iris:Right
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is rightThumbDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path, and scenario flow is ERROR
And I bio authentication where device info file is LeftIris, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path, and scenario flow is ERROR
And I bio authentication where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_187
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A differently abled resident walks into registration center completes the process with out any exception and gets UIN card Later performs biometric authentication using Face Fingerprint and IRIS
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is rightThumbDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I bio authentication where device info file is LeftIris, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I bio authentication where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_188
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A differently abled resident walks into registration center completes the process with both thumbs as exception and gets UIN card Later performs biometric authentication using IRIS Face Fingerprint that are exception and with fingerprints that are not marked as exception
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Left Thumb and Finger:Right Thumb
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I check tags where registration ID is the saved registration ID
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is rightThumbDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path, and scenario flow is ERROR
And I bio authentication where device info file is LeftIris, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I bio authentication where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I bio authentication where device info file is leftLittleDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_189
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: User creates UIN through reg center then deactivates it in IDRepo then create an update packet to see if it is rejected at packet validator stage
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check tags where registration ID is the saved registration ID
Then I get email by uin where resident UIN is the saved UIN and store result in email
Then I deactivate uin where parameter 1 is the saved UIN, and parameter 2 is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved second registration ID
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is VALIDATE_PACKET, and stage status is ERROR
And I delete packet data

  @scenario_190
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: User creates UIN through reg center then deactivates it in IDRepo then reprocess the packet to see if it is rejected at packet validator stage
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check tags where registration ID is the saved registration ID
Then I get email by uin where resident UIN is the saved UIN and store result in email
Then I deactivate uin where parameter 1 is the saved UIN, and parameter 2 is the saved email
Then I reprocess packet where registration ID is the saved registration ID
Then I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is UIN_GENERATOR, and stage status is ERROR
And I delete packet data

  @scenario_191
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident created Infant packet and got UIN and later updated the UIN to a Minor then do bio authentication
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=minor, and persona file is the saved child persona file path
Then I update demo or bio details where bio type is face and iris and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved child persona file path
Then I update resident with uin where persona file path is the saved child persona file path, and UIN is the saved child UIN
Then I get packet template where packet type is UPDATE, and persona file path is the saved child persona file path and store result in update packet template path
Then I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in minor uin
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is 90
And I generate vidwithout otp where VID type is Perpetual, and UIN is minor uin and store result in VID
And I wait where wait seconds is 90
And I bio authentication where device info file is faceDevice, and UIN is minor uin, and VID is the saved VID, and persona file path is the saved child persona file path
And I delete packet data

  @scenario_192
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident created Infant packet and got UIN and later updated the UIN to a adult then do bio authentication
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=adult, and persona file is the saved child persona file path
Then I update demo or bio details where bio type is face and iris and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved child persona file path
Then I update resident with uin where persona file path is the saved child persona file path, and UIN is the saved child UIN
Then I get packet template where packet type is UPDATE, and persona file path is the saved child persona file path and store result in update packet template path
Then I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is 90
And I generate vidwithout otp where VID type is Perpetual, and UIN is the saved UIN and store result in VID
And I wait where wait seconds is 90
And I bio authentication where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved child persona file path
And I delete packet data

  @scenario_193
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: Resident Minor walks into registration center gets UIN with Guardian RID details later updated the UIN to a adult then do bio authentication
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with rid where persona file path is the saved parent persona file path, and registration ID is the saved parent registration ID
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved parent registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=adult, and persona file is the saved child persona file path
Then I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is the saved child persona file path
Then I update resident with uin where persona file path is the saved child persona file path, and UIN is the saved child UIN
Then I get packet template where packet type is UPDATE, and persona file path is the saved child persona file path and store result in update packet template path
Then I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is 90
And I generate vidwithout otp where VID type is Perpetual, and UIN is the saved UIN and store result in VID
And I wait where wait seconds is 90
And I bio authentication where device info file is faceDevice, and UIN is the saved UIN, and VID is the saved VID, and persona file path is the saved child persona file path
And I delete packet data

  @scenario_194
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: Infant external packet creation and process with out introducerInfoToken
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved external packet environment details, and generate private key is false, and registration status or invalidation flag is EXTERNAL
And I get ping health where component is targetenv
And I get resident data where persona type is infant, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I create and upload external packet where persona file path is CRVS1, and packet template path is CRVS_NEW, and parameter 3 is the saved persona file path and store result in registration ID
And I sync external packet where packet zip path is the saved registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_195
  @Postive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later updates name with VID and perform biometric authentiction with name both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I get email by uin where resident UIN is the saved UIN and store result in email1
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email1 and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email1
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved VID
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I demo authentication where demo field is name, and UIN is the saved UIN, and persona file path is the saved persona file path, and VID is the saved VID
And I delete packet data

  @scenario_196
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walk-ins to registration center create a packet empty signature
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is 0, and negative test or signature flag is emptySignature
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_197
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process with invalid signature and try to gets UIN card
Given I get ping health where component is packetcreator
And I user where user action is ADD_User, and user index or master user is 5, and password or zone flag is Techno@123 and store result in user5
And I center where call type is CREATE, and user details is the saved user5, and center index is 5, and center active flag is T and store result in center5
And I machine where call type is CREATE, and center details is the saved center5, and center index is 5 and store result in details5
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 5, and password or zone flag is Techno@123, and center index or details is the saved details5 and store result in details5
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details5 and store result in details5
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details5
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details5
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details5, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details5, and password or zone flag is 5
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details5, and password or zone flag is T
And I write pre req where environment details is the saved details5, and pre-requisite data index is 5
And I read pre req where pre-requisite data index is 5 and store result in details5
And I set context where context key is env_context, and pre-requisite details is the saved environment5details, and generate private key is true, and registration status or invalidation flag is 0, and negative test or signature flag is invalidSignature
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_198
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: Infant external packet creation and process with introducerInfoToken
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved external packet environment details, and generate private key is false, and registration status or invalidation flag is EXTERNAL
And I get ping health where component is targetenv
And I get resident data where persona type is infant, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I create and upload external packet where persona file path is CRVS1, and packet template path is CRVS_NEW, and parameter 3 is the saved persona file path, and parameter 4 is true and store result in registration ID
And I sync external packet where packet zip path is the saved registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_199
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process and gets UIN card later we perform crvs external packet death flow
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check tags where registration ID is the saved registration ID
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved external packet environment details, and generate private key is false, and registration status or invalidation flag is EXTERNAL
And I create and upload external packet where persona file path is CRVS1, and packet template path is CRVS_DEATH, and parameter 3 is the saved persona file path, and parameter 4 is true, and parameter 5 is the saved UIN and store result in second registration ID
And I sync external packet where packet zip path is the saved second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_200
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: Adult external packet creation and process with out introducerInfoToken
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved external packet environment details, and generate private key is false, and registration status or invalidation flag is EXTERNAL
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I create and upload external packet where persona file path is CRVS1, and packet template path is CRVS_NEW, and parameter 3 is the saved persona file path and store result in registration ID
And I sync external packet where packet zip path is the saved registration ID
And I check status where packet status is FAILED, and registration ID is the saved registration ID
And I delete packet data

  @scenario_201
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: Minor external packet creation and process with out introducerInfoToken
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved external packet environment details, and generate private key is false, and registration status or invalidation flag is EXTERNAL
And I get ping health where component is targetenv
And I get resident data where persona type is minor, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I create and upload external packet where persona file path is CRVS1, and packet template path is CRVS_NEW, and parameter 3 is the saved persona file path and store result in registration ID
And I sync external packet where packet zip path is the saved registration ID
And I check status where packet status is FAILED, and registration ID is the saved registration ID
And I delete packet data

  @scenario_202
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: Creation and processing of an external infant packet without an introducerInfoToken and with an invalid source and packet type
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved external packet environment details, and generate private key is false, and registration status or invalidation flag is EXTERNAL
And I get ping health where component is targetenv
And I get resident data where persona type is infant, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I create and upload external packet where persona file path is CRVS11@invalid, and packet template path is CRVS_NEW1, and parameter 3 is the saved persona file path and store result in registration ID
And I delete packet data

  @scenario_203
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: Infant external packet creation with DOB as future dates and process with out introducerInfoToken
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is EXTERNAL
And I get ping health where component is targetenv
And I get resident data where persona type is infant, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=04/24/2026, and persona file is the saved persona file path
And I create and upload external packet where persona file path is CRVS1, and packet template path is CRVS_NEW, and parameter 3 is the saved persona file path and store result in registration ID
And I sync external packet where packet zip path is the saved registration ID
And I check status where packet status is FAILED, and registration ID is the saved registration ID
And I delete packet data

  @scenario_204
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: Infant external packet creation without necessary data and process with out introducerInfoToken
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved external packet environment details, and generate private key is false, and registration status or invalidation flag is EXTERNAL
And I get ping health where component is targetenv
And I get resident data where persona type is infant, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=24/04/2026, and persona file is the saved persona file path
And I create and upload external packet where persona file path is CRVS1, and packet template path is CRVS_NEW, and parameter 3 is the saved persona file path and store result in registration ID
And I sync external packet where packet zip path is the saved registration ID
And I check status where packet status is FAILED, and registration ID is the saved registration ID
And I delete packet data

  @scenario_205
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process with inactive user and try to gets UIN card
Given I get ping health where component is packetcreator
And I user where user action is ADD_User, and user index or master user is 5, and password or zone flag is Techno@123 and store result in user5
And I center where call type is CREATE, and user details is the saved user5, and center index is 5, and center active flag is T and store result in center5
And I machine where call type is CREATE, and center details is the saved center5, and center index is 5 and store result in details5
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 5, and password or zone flag is Techno@123, and center index or details is the saved details5 and store result in details5
And I user where user action is CREATE_ZONESEARCH, and user index or master user is the saved details5 and store result in details5
And I wait where wait seconds is 10
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details5
And I user where user action is CREATE_ZONEMAPPING, and user index or master user is the saved details5
And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is the saved details5, and password or zone flag is T
And I user where user action is CREATE_CENTERMAPPING, and user index or master user is the saved details5, and password or zone flag is 5
And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is the saved details5, and password or zone flag is T
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 5, and password or zone flag is Techno@123, and center index or details is the saved details5 and store result in details5
And I user where user action is DELETE_ZONEMAPPING, and user index or master user is the saved details5
And I write pre req where environment details is the saved details5, and pre-requisite data index is 5
And I read pre req where pre-requisite data index is 5 and store result in details5
And I set context where context key is env_context, and pre-requisite details is the saved details5, and generate private key is true
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is FAILED, and registration ID is the saved registration ID
And I delete packet data

  @scenario_206
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center with his child and completes the process. But Adult and Child packet uploaded at same time in Child packet adult RID is captured as introducer but Introducer packet is Rejected
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in parent zip packet path
And I get resident data where persona type is minor, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update resident with rid where persona file path is the saved parent persona file path, and registration ID is the saved parent registration ID
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I packetcreator where packet type is NEW, and template path is the saved child packet template path and store result in child zip packet path
And I rid sync rejected where packet type is NEW, and packet zip path is parent zip packet path and store result in parent registration ID
And I ridsync where packet type is NEW, and packet zip path is child zip packet path and store result in child registration ID
And I packetsync where packet zip path is parent zip packet path
And I packetsync where packet zip path is child zip packet path
And I check status where packet status is REREGISTER, and registration ID is the saved child registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved parent registration ID
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is ERROR
Then I check ridstage where registration ID is the saved parent registration ID, and RID stage is VALIDATE_PACKET, and stage status is REJECTED
And I delete packet data

  @scenario_208
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Adult resident walks into registration center completes the process and gets UIN card later we perform crvs external packet death flow without the token
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check tags where registration ID is the saved registration ID
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved external packet environment details, and generate private key is false, and registration status or invalidation flag is EXTERNAL
And I create and upload external packet where persona file path is CRVS1, and packet template path is CRVS_DEATH, and parameter 3 is the saved persona file path, and parameter 4 is the saved UIN and store result in second registration ID
And I sync external packet where packet zip path is the saved second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_209
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Infant external packet creation and process with introducerInfoToken and gets UIN card later we perform crvs external packet death flow
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved external packet environment details, and generate private key is false, and registration status or invalidation flag is EXTERNAL
And I get ping health where component is targetenv
And I get resident data where persona type is infant, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I create and upload external packet where persona file path is CRVS1, and packet template path is CRVS_NEW, and parameter 3 is the saved persona file path and store result in registration ID
And I sync external packet where packet zip path is the saved registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I create and upload external packet where persona file path is CRVS1, and packet template path is CRVS_DEATH, and parameter 3 is the saved persona file path, and parameter 4 is true, and parameter 5 is the saved UIN and store result in second registration ID
And I sync external packet where packet zip path is the saved second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_210
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident Infant walks into registration center gets UIN with parent RID details and later we perform crvs external packet death flow
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I create and upload external packet where persona file path is CRVS1, and packet template path is CRVS_DEATH, and parameter 3 is the saved child persona file path, and parameter 4 is true, and parameter 5 is the saved UIN and store result in second registration ID
And I sync external packet where packet zip path is the saved second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_211
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process and gets UIN card later we perform crvs external packet death flow with invalid source and process
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
Then I check tags where registration ID is the saved registration ID
And I read pre req where pre-requisite data index is 4 and store result in external packet environment details
And I set context where context key is env_context, and pre-requisite details is the saved external packet environment details, and generate private key is false, and registration status or invalidation flag is EXTERNAL
And I create and upload external packet where persona file path is CRVS11@invalid, and packet template path is CRVS_DEATH1, and parameter 3 is the saved persona file path, and parameter 4 is true, and parameter 5 is the saved UIN and store result in second registration ID
And I delete packet data

  @scenario_212
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process and gets UIN card and generates Perpetual VID. Later updates face and finger with VID and perform biometric authentiction with face both using UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I get email by uin where resident UIN is the saved UIN and store result in email1
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email1 and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email1
And I update demo or bio details where bio type is finger and face, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved VID
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email1
And I ekyc bio where device info file is faceDevice, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_213
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_Update
  Scenario: Resident walks into reg-center and uploads a packet and tries to update the name using mixed cases.
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name=Asa2DFG@, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I delete packet data

  @scenario_214
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: A differently abled resident walk-ins to registration center completes the process and gets UIN card. Later performs biometric delegated  authentication using face modality with UIN and VID
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Iris:Left and Iris:Right
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I oidc client and store result in OIDC client ID
And I bio delegated authentication where parameter 1 is faceDevice, and parameter 2 is the saved UIN, and parameter 3 is the saved VID, and parameter 4 is the saved persona file path, and parameter 5 is the saved OIDC client ID
And I delete packet data

  @scenario_216
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into the registration center accompanied by a non-registered person and tries to update UIN biometrics using the non-registered person?s biometrics.
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path1
And I get packet template where packet type is NEW, and persona file path is the saved persona file path1 and store result in template path1
And I generate and upload packet skipping prereg where persona file path is the saved persona file path1, and packet template path is the saved template path1 and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null and 99
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path2
And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path1, and parameter 5 is the saved persona file path2
And I update resident with uin where persona file path is the saved persona file path1, and UIN is the saved uin1
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path1 and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path1, and packet template path is the saved update packet template path and store result in rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I verify notification where notification type is updated, and email is the saved email
And I delete packet data

  @scenario_217
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident A and Resident B successfully obtain their UINs. Resident A attempts to update biometrics for UIN1 by capturing right-hand fingerprints from Resident A (Profile-1) and left-hand fingerprints from Resident B (Profile-2) resulting in bio-dedupe detection and manual adjudication.
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path1
And I get packet template where packet type is NEW, and persona file path is the saved persona file path1 and store result in template path1
And I generate and upload packet skipping prereg where persona file path is the saved persona file path1, and packet template path is the saved template path1 and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is null and 99
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path2
And I get packet template where packet type is NEW, and persona file path is the saved persona file path2 and store result in template path2
And I generate and upload packet skipping prereg where persona file path is the saved persona file path2, and packet template path is the saved template path2 and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I get email by uin where resident UIN is the saved second UIN and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I update demo or bio details where bio type is rightMiddle, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path1, and parameter 5 is the saved persona file path2
And I update demo or bio details where bio type is leftThumb and leftRing, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path2, and parameter 5 is the saved persona file path1
And I update resident with uin where persona file path is the saved persona file path1, and UIN is the saved second UIN
And I update resident with uin where persona file path is the saved persona file path2, and UIN is the saved uin1
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path1 and store result in update packet template path
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path2 and store result in update template2
And I generate and upload packet skipping prereg where persona file path is the saved persona file path1, and packet template path is the saved update packet template path and store result in rid3
And I generate and upload packet skipping prereg where persona file path is the saved persona file path2, and packet template path is the saved update template2 and store result in rid4
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid4
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I verify notification where notification type is updated, and email is the saved email
And I get uin by rid where source registration ID is the saved rid4 and store result in uin4
And I verify notification where notification type is updated, and email is the saved email1
Then I check ridstage where registration ID is the saved rid3, and RID stage is MANUAL_ADJUDICATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved rid4, and RID stage is MANUAL_ADJUDICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_218
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: Resident updates UIN biometrics by capturing left-hand fingerprints and irises while marking right-hand fingerprints as valid exceptions.
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Right Thumb and Finger:Right IndexFinger and Finger:Right MiddleFinger and Finger:Right RingFinger and Finger:Right LittleFinger
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I check tags where registration ID is the saved second registration ID
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_219
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: Resident walks into the registration center and performs a biometric update for the UIN by capturing iris and face while marking all fingerprints as valid exceptions.
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update bio exception in persona where persona file path is the saved persona file path, and biometric exception modalities is Finger:Right Thumb and Finger:Right IndexFinger and Finger:Right MiddleFinger and Finger:Right RingFinger and Finger:Right LittleFinger and Finger:Left Thumb and Finger:Left IndexFinger and Finger:Left MiddleFinger and Finger:Left RingFinger and Finger:Left LittleFinger
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I check tags where registration ID is the saved second registration ID
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved second registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_220
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: An infant resident is registered and issued a UIN then later updates to adult using biometrics of another registered resident.
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get email by uin where resident UIN is the saved parent UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update resident with uin where persona file path is the saved parent persona file path, and UIN is the saved parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and false and false and true and store result in child persona file path
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
And I get email by uin where resident UIN is the saved child UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path1
And I get packet template where packet type is NEW, and persona file path is the saved persona file path1 and store result in template path1
And I generate and upload packet skipping prereg where persona file path is the saved persona file path1, and packet template path is the saved template path1 and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email1
And I verify notification where notification type is UIN Generated, and email is the saved email1
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=adult, and persona file is the saved child persona file path
And I update demo or bio details where bio type is iris and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved child persona file path, and parameter 5 is the saved persona file path1
And I update resident with uin where persona file path is the saved child persona file path, and UIN is the saved child UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved child persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved update packet template path and store result in rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I verify notification where notification type is updated, and email is the saved email
Then I check ridstage where registration ID is the saved rid3, and RID stage is BIOGRAPHIC_VERIFICATION, and stage status is FAILED
Then I check ridstage where registration ID is the saved rid3, and RID stage is MANUAL_ADJUDICATION, and stage status is SUCCESS

  @scenario_221
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident 1 generates a UIN and updates biometrics twice successfully Resident 2 generates a UIN and attempts to update biometrics using Resident 1?s biometrics resulting in packet rejection
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid1
And I check status where packet status is PROCESSED, and registration ID is the saved rid1
And I get uin by rid where source registration ID is the saved rid1 and store result in uin1
And I get email by uin where resident UIN is the saved uin1 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin1
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved second UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I verify notification where notification type is updated, and email is the saved email
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path2
And I get packet template where packet type is NEW, and persona file path is the saved persona file path2 and store result in template path2
And I generate and upload packet skipping prereg where persona file path is the saved persona file path2, and packet template path is the saved template path2 and store result in rid4
And I check status where packet status is PROCESSED, and registration ID is the saved rid4
And I get uin by rid where source registration ID is the saved rid4 and store result in uin4
And I get email by uin where resident UIN is the saved uin4 and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is iris and finger and face, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path, and parameter 5 is the saved persona file path2
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin4
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in rid5
And I check status where packet status is REJECTED, and registration ID is the saved rid5
Then I check ridstage where registration ID is the saved rid5, and RID stage is BIOGRAPHIC_VERIFICATION, and stage status is FAILED
And I delete packet data

  @scenario_222
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident generates UIN ABIS response is delayed and first UIN biometric is updated successfully during packet reprocessing
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in template path2
And I get bio modality hash where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path and store result in modality hash value
And I configure mock abis where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and duplicate match flag is false, and hash modality keys is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path, and modality hash map is the saved modality hash value, and delay seconds is delay, and mock ABIS status is 10 and Error
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved template path2 and store result in second registration ID
And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I delete mock expect where modality hash value is the saved modality hash value
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_223
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process with invalid type of packet and gets error at upload stage
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I packetcreator where packet type is NEW, and template path is the saved packet template path and store result in packet zip path
And I ridsync where packet type is UPDATE, and packet zip path is the saved packet zip path and store result in registration ID
And I packetsync where packet zip path is the saved packet zip path
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is ERROR, and sub-status is RPR-SYS-EXCEPTION-001
And I delete packet data

  @scenario_225
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_Update
  Scenario: Resident walks into registration center uploads packet and gets UIN. Resident then performs multiple sequential updates name DOB gender and email address.
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved second UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update template2
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update template2 and store result in rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I verify notification where notification type is updated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is gender=Male, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin3
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update template3
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update template3 and store result in rid4
And I check status where packet status is PROCESSED, and registration ID is the saved rid4
And I get uin by rid where source registration ID is the saved rid4 and store result in uin4
And I verify notification where notification type is updated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=adult, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin4
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update template4
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update template4 and store result in rid5
And I check status where packet status is PROCESSED, and registration ID is the saved rid5
And I get uin by rid where source registration ID is the saved rid5 and store result in uin5
And I verify notification where notification type is updated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=test, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin5
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update template5
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update template5 and store result in rid6
And I check status where packet status is PROCESSED, and registration ID is the saved rid6
And I get uin by rid where source registration ID is the saved rid6 and store result in uin6
And I get email by uin where resident UIN is the saved uin6 and store result in email1
And I verify notification where notification type is updated, and email is the saved email1
And I delete packet data

  @scenario_226
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into registration center completes the process  gets UIN card . Resident then updates name and gets updated UIN try to authenticate with old name
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name=salman khan, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in new packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved new packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name=salman khan, and persona file is the saved persona file path
And I demo authentication where demo field is name, and UIN is the saved second UIN, and persona file path is the saved persona file path, and VID is the saved VID, and age update flag is ERROR
And I delete packet data

  @scenario_227
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident walks into the registration center completes the process with gender marked as Other and gets the UIN card.
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is gender=Others, and persona file is the saved persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_230
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_Update
  Scenario: The resident walks into the registration center uploads the packet and gets the UIN. Later the resident updates the iris face and fingerprints and each update packet is processed successfully.
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I wait where wait seconds is 90
And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I update demo or bio details where bio type is face, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved second UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update template2
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update template2 and store result in rid3
And I check status where packet status is PROCESSED, and registration ID is the saved rid3
And I get uin by rid where source registration ID is the saved rid3 and store result in uin3
And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved uin3
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update template3
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update template3 and store result in rid4
And I check status where packet status is PROCESSED, and registration ID is the saved rid4
And I get uin by rid where source registration ID is the saved rid4 and store result in uin4
And I delete packet data

  @scenario_231
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident updates only one fingerprint that was previously marked exception and performs bio authentication
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is rightThumb, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is rightThumbDevice, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_232
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident updates only one iris that was previously marked exception and performs bio authentication.
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is 90
And I update demo or bio details where bio type is leftIris, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is LeftIris, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I delete packet data

  @scenario_236
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident booked pre-registration with support documents later changes the appointment slot. walk-ins to registration center completes the process and gets UIN card
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I send otp where persona file path is the saved persona file path and store result in email
And I validate otp where persona file path is the saved persona file path, and email is the saved email
And I pre register where persona file path is the saved persona file path and store result in pre-registration ID
And I upload documents where persona file path is the saved persona file path, and pre-registration ID is the saved pre-registration ID
And I update pre reg status where status code is 0, and pre-registration ID is the saved pre-registration ID, and validation mode is valid
And I book appointment where holiday booking flag is false, and pre-registration ID is the saved pre-registration ID, and slot number is 2
And I book appointment where holiday booking flag is false, and pre-registration ID is the saved pre-registration ID, and slot number is 3
And I generate and upload packet where pre-registration ID is the saved pre-registration ID, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

#  @scenario_237
#  @Positive_Test
#  @persona_ResidentMaleAdult
#  @group_NA
#  Scenario: Resident walks into registration center completes the process and gets UIN card with handle. Later updates his name and handle and perform demographic authentication both using UIN and handle
#Given I get ping health where component is packetcreator
#And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
#And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
#And I get ping health where component is targetenv
#And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
#And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
#And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
#And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
#And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
#And I get email by uin where resident UIN is the saved UIN and store result in email
#And I verify notification where notification type is UIN Generated, and email is the saved email
#And I wait where wait seconds is 90
#And I generate vid where VID type is Perpetual, and UIN is the saved UIN, and email or phone is the saved email and store result in VID
#And I verify notification where notification type is Successful Generation of VID, and email is the saved email
#And I update identity with array handles where persona file path is the saved UIN, and parameter 2 is the saved persona file path
#And I get handles by uin where parameter 1 is the saved UIN and store result in handles
#And I demo authentication where demo field is name, and UIN is the saved UIN, and persona file path is the saved persona file path, and VID is the saved VID, and age update flag is 0, and handle key is handles
#And I delete packet data

  @scenario_238
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: Resident walks into registration center completes the process tries to get uin with old ID Schema version
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false, and registration status or invalidation flag is oldIdSchema
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Female and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I delete packet data

  @scenario_239
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: Twin infants with similar demographics walk into registration center and use same parent RID details for enrollment
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update resident with rid where persona file path is the saved parent persona file path, and registration ID is the saved parent registration ID
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is VERIFICATION, and stage status is SUCCESS
Then I update demo or bio details where bio type is iris and face and finger, and miss fields is 0, and update attributes is 0, and persona file is the saved child persona file path
Then I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child template2
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child template2 and store result in child rid2
Then I check ridstage where registration ID is the saved child rid2, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved child rid2, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_240
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: Identical twins with similar facial features walk into registration center and use same parent RID details while changing dob name gender and emailid
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in parent persona file path
And I get packet template where packet type is NEW, and persona file path is the saved parent persona file path and store result in parent packet template path
And I generate and upload packet skipping prereg where persona file path is the saved parent persona file path, and packet template path is the saved parent packet template path and store result in parent registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved parent registration ID
And I get uin by rid where source registration ID is the saved parent registration ID and store result in parent UIN
And I get resident data where persona type is infant, and guardian flag is true, and gender and biometric flags is Male and store result in child persona file path
And I update resident with rid where persona file path is the saved parent persona file path, and registration ID is the saved parent registration ID
And I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child packet template path
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child packet template path and store result in child registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved child registration ID
And I get uin by rid where source registration ID is the saved child registration ID and store result in child UIN
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved child registration ID, and RID stage is VERIFICATION, and stage status is SUCCESS
Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved child persona file path
Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=john, and persona file is the saved child persona file path
Then I update resident with guardian skipping pre reg where guardian persona file path is the saved parent persona file path, and child persona file path is the saved child persona file path
And I get packet template where packet type is NEW, and persona file path is the saved child persona file path and store result in child template2
And I generate and upload packet skipping prereg where persona file path is the saved child persona file path, and packet template path is the saved child template2 and store result in child rid2
Then I check ridstage where registration ID is the saved child rid2, and RID stage is INTRODUCER_VALIDATION, and stage status is SUCCESS
Then I check ridstage where registration ID is the saved child rid2, and RID stage is VERIFICATION, and stage status is SUCCESS
And I delete packet data

  @scenario_241
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Adult
  Scenario: Resident walks to the center and creates a packet with a large face image size
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is true and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path, and sync RID flag or expected error code is INVALID_PACKET_SIZE
And I delete packet data

  @scenario_242
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Adult
  Scenario: Resident walks to the center and creates a packet with face obstruction image (mask cap and glare)
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where resident count is adult, and persona type is false, and guardian flag is Male, and gender is false, and missing biometric fields is true and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I delete packet data

  @scenario_244
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_Update
  Scenario: Resident gets UIN updates iris and face biometrics authenticates successfully with new biometrics and fails with old biometrics
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I clone resident data where persona file path is the saved persona file path and store result in old bio persona file path
And I update demo or bio details where bio type is iris and face, and miss fields is 0, and update attributes is 0, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I update resident with uin where persona file path is old bio persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I bio authentication where device info file is faceDevice, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I bio authentication where device info file is LeftIris, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is the saved persona file path
And I bio authentication where device info file is faceDevice, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is old bio persona file path, and scenario flow is ERROR
And I bio authentication where device info file is LeftIris, and UIN is the saved second UIN, and VID is the saved VID, and persona file path is old bio persona file path, and scenario flow is ERROR
And I delete packet data

  @scenario_245
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_Update
  Scenario: Resident gets UIN updates demographic details authenticates successfully with new details and fails with old details
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I clone resident data where persona file path is the saved persona file path and store result in old demo persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is the saved persona file path
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I update resident with uin where persona file path is old demo persona file path, and UIN is the saved UIN
And I get packet template where packet type is UPDATE, and persona file path is the saved persona file path and store result in update packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved update packet template path and store result in second registration ID
And I check status where packet status is PROCESSED, and registration ID is the saved second registration ID
And I get uin by rid where source registration ID is the saved second registration ID and store result in second UIN
And I verify notification where notification type is updated, and email is the saved email
And I wait where wait seconds is UIN_WAIT_TIME
And I generate vid where VID type is Perpetual, and UIN is the saved second UIN, and email or phone is the saved email and store result in VID
And I verify notification where notification type is Successful Generation of VID, and email is the saved email
And I wait where wait seconds is 90
And I demo authentication where demo field is name, and UIN is the saved second UIN, and persona file path is the saved persona file path, and VID is the saved VID
And I demo authentication where demo field is name, and UIN is the saved second UIN, and persona file path is old demo persona file path, and VID is the saved VID, and age update flag is ERROR
And I delete packet data

  @scenario_246
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Mock ABIS holds the packet in queue longer than the reprocessor interval (delay from actuator) reprocessor runs while ABIS is still pending then bio dedupe completes. Asserts BIOGRAPHIC_VERIFICATION REPROCESS and post-reprocess outcome explicit biometric dedupe skip when regproc records it otherwise a second bio dedupe cycle (pass strictSkip as second arg to require skip-only).
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I get bio modality hash where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path and store result in modality hash value
And I configure mock abis where persona ID is -1, and modality subtypes is Right IndexFinger and Left LittleFinger, and duplicate match flag is true, and hash modality keys is Right IndexFinger and Left LittleFinger, and persona path is the saved persona file path, and modality hash map is the saved modality hash value, and delay seconds is delay, and mock ABIS status is 10 and Error
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is BIOGRAPHIC_VERIFICATION, and stage status is REPROCESS
Then I verify bio dedup skipped after reprocess where registration ID is the saved registration ID
Then I delete mock expect
And I check status where packet status is PROCESSED, and registration ID is the saved registration ID
And I get uin by rid where source registration ID is the saved registration ID and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is the saved registration ID, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_247
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Resident creates a Lost packet that gets rejected then reuses the same biometrics from the rejected Lost flow packet to create a new packet which gets processed successfully
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is LOST, and persona file path is the saved persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in rid lost
And I packetsync where packet zip path is the saved packet zip path
And I post mock mv where registration ID is rid lost, and manual verification decision is REJECTED
And I check status where packet status is REJECTED, and registration ID is rid lost
Then I check ridstage where registration ID is rid lost, and RID stage is BIOGRAPHIC_VERIFICATION, and stage status is FAILED
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid new
And I check status where packet status is PROCESSED, and registration ID is rid new
And I get uin by rid where source registration ID is rid new and store result in UIN
And I get email by uin where resident UIN is the saved UIN and store result in email
And I verify notification where notification type is UIN Generated, and email is the saved email
Then I check ridstage where registration ID is rid new, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_248
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Create a new registration packet and process it then create a Lost packet for the same persona UIN with same biometrics and verify packet upload transaction and processing
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender and biometric flags is Male and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in rid new
And I check status where packet status is PROCESSED, and registration ID is rid new
And I get uin by rid where source registration ID is rid new and store result in UIN
And I update resident with uin where persona file path is the saved persona file path, and UIN is the saved UIN
And I get packet template where packet type is LOST, and persona file path is the saved persona file path and store result in lost template
And I packetcreator where packet type is LOST, and template path is lost template and store result in packet zip path
And I ridsync where packet type is LOST, and packet zip path is the saved packet zip path and store result in rid lost
And I packetsync where packet zip path is the saved packet zip path
And I check status where packet status is PROCESSED, and registration ID is rid lost
Then I check ridstage where registration ID is rid lost, and RID stage is PRINT_SERVICE, and stage status is PROCESSED
And I delete packet data

  @scenario_249
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Registration packet is built without required name demographic fields in identity JSON; processing fails at packet validation
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I get ping health where component is targetenv
And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and missing biometric fields is name and store result in persona file path
And I get packet template where packet type is NEW, and persona file path is the saved persona file path and store result in packet template path
And I generate and upload packet skipping prereg where persona file path is the saved persona file path, and packet template path is the saved packet template path and store result in registration ID
And I check status where packet status is REREGISTER, and registration ID is the saved registration ID
Then I check ridstage where registration ID is the saved registration ID, and RID stage is VALIDATE_PACKET, and stage status is FAILED
And I delete packet data

  @scenario_AFTER_SUITE
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: Test suite run Pre-Requisite data tear down
Given I skip
Given I get ping health where component is packetcreator
And I read pre req where pre-requisite data index is 1 and store result in environment 1 details
And I set context where context key is env_context, and pre-requisite details is the saved environment 1 details, and generate private key is false
And I skip
And I delete mock expect
And I machine where call type is DCOM, and center details is the saved environment 1 details
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 1, and password or zone flag is Techno@123, and center index or details is the saved environment 1 details and store result in environment 1 details
And I center where call type is DCOM, and user details is the saved environment 1 details, and center index is 1
And I read pre req where pre-requisite data index is 2 and store result in environment 2 details
And I machine where call type is DCOM, and center details is the saved environment 2 details
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 2, and password or zone flag is Techno@123, and center index or details is the saved environment 2 details and store result in environment 2 details
And I center where call type is DCOM, and user details is the saved environment 2 details, and center index is 2
And I read pre req where pre-requisite data index is 3 and store result in environment 3 details
And I machine where call type is DCOM, and center details is the saved environment 3 details
And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 3, and password or zone flag is Techno@123, and center index or details is the saved environment 3 details and store result in environment 3 details
And I center where call type is DCOM, and user details is the saved environment 3 details, and center index is 3
And I user where user action is DELETE_User, and user index or master user is dsl-0, and password or zone flag is Techno@123
And I delete certificates and onboarding partners
And I masterdata delete
And I write persona data
And I clear run cache

