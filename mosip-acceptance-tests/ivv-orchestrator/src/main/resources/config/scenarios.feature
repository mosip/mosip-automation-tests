# MOSIP DSL scenarios — readable English Gherkin steps (parameters in plain language)
Feature: MOSIP DSL end-to-end acceptance tests

  @scenario_0
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Before Suite setup
    Given I user where user action is ADD_User, and master user is dsl-0, and password is Techno@123
    And I user where user action is ADD_User, and user index is 0, and password is Techno@123
    And I user where user action is ADD_User, and user index is 1, and password is Techno@123 and store result in $$user1
    And I center where argument 1 is CREATE, and argument 2 is $$user1, and center index is 1, and center active flag is T and store result in $$center1
    And I machine where argument 1 is CREATE, and argument 2 is $$center1, and center index is 1 and store result in $$details1
    And I user where user action is DELETE_CENTERMAPPING, and user index is 1, and password is Techno@123, and password or details is $$details1 and store result in $$details1
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details1 and store result in $$details1
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details1
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details1
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details1, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details1, and center index is 1
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details1, and user center mapping flag is T
    And I write pre req where argument 1 is $$details1, and pre requisite data index is 1
    And I user where user action is ADD_User, and user index is 2, and password is Techno@123 and store result in $$user2
    And I center where argument 1 is CREATE, and argument 2 is $$user2, and center index is 2, and center active flag is T and store result in $$center2
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is true
    And I wait where argument 1 is 60
    And I machine where argument 1 is CREATE, and argument 2 is $$center2, and center index is 2 and store result in $$details2
    And I user where user action is DELETE_CENTERMAPPING, and user index is 2, and password is Techno@123, and password or details is $$details2 and store result in $$details2
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details2 and store result in $$details2
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details2
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details2
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details2, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details2, and center index is 2
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details2, and user center mapping flag is T
    And I write pre req where argument 1 is $$details2, and pre requisite data index is 2
    And I set context where argument 1 is env_context, and argument 2 is $$details2, and generate private key is true
    And I user where user action is ADD_User, and user index is 3, and password is Techno@123 and store result in $$user3
    And I center where argument 1 is CREATE, and argument 2 is $$user3, and center index is 3, and center active flag is T and store result in $$center3
    And I machine where argument 1 is CREATE, and argument 2 is $$center3, and center index is 3 and store result in $$details3
    And I user where user action is DELETE_CENTERMAPPING, and user index is 3, and password is Techno@123, and password or details is $$details3 and store result in $$details3
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details3 and store result in $$details3
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details3
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details3
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details3, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details3, and center index is 3
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details3, and user center mapping flag is T
    And I write pre req where argument 1 is $$details3, and pre requisite data index is 3
    And I set context where argument 1 is env_context, and argument 2 is $$details3, and generate private key is true
    And I user where user action is ADD_User_External_Packet, and user index is 4, and password is Techno@123 and store result in $$details4
    And I write pre req where argument 1 is $$details4, and pre requisite data index is 4
    And I clear device cert cache
    And I generate auth certifcates
    And I upload device certificate
    And I reset context data

  @scenario_1
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: ResidentMaleAdult - Resident booked pre-registration with support documents walks into registrati...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I send otp where argument 1 is $$personaFilePath and store result in $$email
    And I validate otp where argument 1 is $$personaFilePath, and argument 2 is $$email
    And I pre register where argument 1 is $$personaFilePath and store result in $$prid
    And I upload documents where argument 1 is $$personaFilePath, and argument 2 is $$prid
    And I update pre reg status where status code is 0, and argument 2 is $$prid, and scenario without pending appointment is valid
    And I book appointment where holiday booking flag is false, and argument 2 is $$prid, and slot number is 1
    And I generate and upload packet where argument 1 is $$prid, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_2
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into registration center completes the process and gets UIN card
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check ridstage where argument 1 is $$rid, and argument 2 is BIOGRAPHIC_VERIFICATION, and argument 3 is SUCCESS
    Then I check tags where argument 1 is $$rid
    And I delete packet data

  @scenario_4
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_Update
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I credential request where argument 1 is $$uin2, and argument 2 is $$email and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I download card where argument 1 is $$requestId
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    Then I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I demo authentication where argument 1 is name, and argument 2 is $$uin2, and argument 3 is $$personaFilePath, and argument 4 is $$vid
    And I delete packet data

  @scenario_5
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor Child walks into registration center wants to get UIN without ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is minor, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is ERROR
    And I delete packet data

  @scenario_6
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor Child walks into registration center gets UIN with Guardian RI...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with rid where argument 1 is $$parentPersona, and argument 2 is $$parentRid
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$parentRid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_7
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New_Exception
  Scenario: ResidentMaleAdult - A differently abled resident with exception in left and right index finger wa...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left IndexFinger, and password is Finger:Right IndexFinger
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_8
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A differently abled resident walks into registration center completes the pro...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male, and argument 4 is leftiris, and password is rightIris and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I wait where argument 1 is UIN_WAIT_TIME
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I credential request where argument 1 is $$uin2, and argument 2 is $$email and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I verify notification where argument 1 is Credential Issuance Status, and argument 2 is $$email
    And I download card where argument 1 is $$requestId
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_9
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center wants to register his child with his ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I get resident data where persona type is adult, and guardian flag is true, and argument 3 is Male, and argument 4 is gender and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I update resident with rid where argument 1 is $$parentPersona, and argument 2 is $$parentRid
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is REREGISTER, and argument 2 is $$childRid
    Then I check ridstage where argument 1 is $$parentRid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is ERROR
    And I delete packet data

  @scenario_10
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$newTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$ridLost
    And I post mock mv where argument 1 is $$ridLost, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$ridLost
    Then I check ridstage where argument 1 is $$ridLost, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_11
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Iris:Left
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$newTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid2
    And I post mock mv where argument 1 is $$rid2, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$rid2
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_12
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center gives demo details of already registe...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I credential request where argument 1 is $$uin1, and argument 2 is $$email and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I download card where argument 1 is $$requestId
    And I update demo or bio details where bio type is iris, and password is face@@finger, and update attributes is 0, and persona file is 0, and argument 5 is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$newTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid2
    And I post mock mv where argument 1 is $$rid2, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$rid2
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_13
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center and tries to register again by provid...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$newTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid2
    And I check status where packet status is REREGISTER, and argument 2 is $$rid2
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_14
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center and tries to retrieve UIN without pro...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Male, and argument 4 is face, and password is leftEye@@rightEye@@rightIndex@@rightLittle@@rightRing@@rightMiddle@@leftIndex@@leftLittle@@leftRing@@leftMiddle@@leftThumb@@rightThumb and store result in $$personaFilePath
    And I get packet template where packet type is LOST, and argument 2 is $$personaFilePath and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    And I wait where argument 1 is 90
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_15
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is gender=Male, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I wait where argument 1 is UIN_WAIT_TIME
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I credential request where argument 1 is $$uin2, and argument 2 is $$email and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I verify notification where argument 1 is Credential Issuance Status, and argument 2 is $$email
    And I download card where argument 1 is $$requestId
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_16
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A non registered Resident walks into registration center without UIN and trie...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is LOST, and argument 2 is $$personaFilePath and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    And I wait where argument 1 is 90
    Then I check ridstage where argument 1 is $$rid, and argument 2 is BIOGRAPHIC_VERIFICATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_17
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center and completes the process and gets UI...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male, and argument 4 is POA, and password is POI@@POR@@POE@@POB and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_18
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center tries to get UIN without providing bi...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male, and password is false@@false@@false and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_19
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center tries to get Lost UIN without providi...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is face, and password is iris@@finger, and update attributes is 0, and persona file is 0, and argument 5 is $$personaFilePath
    And I get packet template where packet type is LOST, and argument 2 is $$personaFilePath and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$lostRid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$lostRid, and argument 2 is BIOGRAPHIC_VERIFICATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_20
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Non-resident walks into registration center and completes the process gets UI...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is langcode=eng, and password is residencestatus=NFR, and argument 5 is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_21
  @Postive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center with his child and completes the proc...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is minor, and argument 2 is true, and argument 3 is Male and store result in $$childPersona
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I packetcreator where packet type is NEW, and argument 2 is $$parentTemplate and store result in $$parentZipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$parentZipPacketPath and store result in $$parentRid
    And I update resident with rid where argument 1 is $$parentPersona, and argument 2 is $$parentRid
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I packetcreator where packet type is NEW, and argument 2 is $$childTemplate and store result in $$childZipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$childZipPacketPath and store result in $$childRid
    And I packetsync where argument 1 is $$parentZipPacketPath
    And I packetsync where argument 1 is $$childZipPacketPath
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is REPROCESS
    And I delete packet data

  @scenario_22
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident booked pre-registration with support documents. walk-ins to registra...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I send otp where argument 1 is $$personaFilePath and store result in $$email
    And I validate otp where argument 1 is $$personaFilePath, and argument 2 is $$email
    And I pre register where argument 1 is $$personaFilePath and store result in $$prid
    And I upload documents where argument 1 is $$personaFilePath, and argument 2 is $$prid
    And I update pre reg status where status code is 0, and argument 2 is $$prid, and argument 3 is valid
    And I book appointment where holiday booking flag is false, and argument 2 is $$prid, and slot number is 2
    And I generate and upload packet where argument 1 is $$prid, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I oidc client where  and store result in $$clientId
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId1 and store result in $$transactionId1
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId2 and store result in $$transactionId2
    And I bio esignet authentication where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$transactionId1, and argument 5 is $$vid, and argument 6 is $$transactionId2
    And I user info where argument 1 is $$transactionId, and argument 2 is $$clientId
    And I delete packet data

  @scenario_23
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A differently abled resident walks in to registration center completes the pr...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Iris:Left, and password is Iris:Right
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is leftLittleDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I oidc client where  and store result in $$clientId
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId1 and store result in $$transactionId1
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId2 and store result in $$transactionId2
    And I bio esignet authentication where argument 1 is leftLittleDevice, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$transactionId1, and argument 5 is $$vid, and argument 6 is $$transactionId2
    And I user info where argument 1 is $$transactionId, and argument 2 is $$clientId
    And I delete packet data

  @scenario_24
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Male, and argument 4 is rightlittleFinger and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is rightThumbDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I oidc client where  and store result in $$clientId
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId1 and store result in $$transactionId1
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId2 and store result in $$transactionId2
    And I bio esignet authentication where argument 1 is rightThumbDevice, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$transactionId1, and argument 5 is $$vid, and argument 6 is $$transactionId2
    And I user info where argument 1 is $$transactionId, and argument 2 is $$clientId
    And I delete packet data

  @scenario_25
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I get email by uin where argument 1 is $$uin and store result in $$email1
    And I credential request where argument 1 is $$uin, and argument 2 is $$email1 and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I verify notification where argument 1 is Credential Issuance Status, and argument 2 is $$email1
    And I download card where argument 1 is $$requestId
    And I update demo or bio details where bio type is finger, and password is face, and miss fields is 0, and update attributes is 0, and argument 5 is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email1
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email1 and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email1
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is faceDevice, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_26
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I credential request where argument 1 is $$uin, and argument 2 is $$email and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I verify notification where argument 1 is Credential Issuance Status, and argument 2 is $$email
    And I download card where argument 1 is $$requestId
    And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is faceDevice, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_27
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I credential request where argument 1 is $$uin, and argument 2 is $$email and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I verify notification where argument 1 is Credential Issuance Status, and argument 2 is $$email
    And I download card where argument 1 is $$requestId
    And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is faceDevice, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_28
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phoneNumber=3938333736, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I execute action "ekycData=e2e_ekycOtp(uin,$$uin2,vid,$$vid,$$email)"
    And I validate kyc data where argument 1 is photo, and argument 2 is ekycData
    And I delete packet data

  @scenario_29
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phoneNumber=3938333736, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I otp authentication where argument 1 is uin, and argument 2 is $$uin2, and argument 3 is vid, and argument 4 is $$vid, and argument 5 is $$email
    And I oidc client where  and store result in $$clientId
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId1 and store result in $$transactionId1
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId2 and store result in $$transactionId2
    And I esignet authentication where argument 1 is $$transactionId1, and argument 2 is $$uin, and argument 3 is OTP, and argument 4 is $$email, and argument 5 is $$vid, and argument 6 is $$transactionId2
    And I user info where argument 1 is $$transactionId1, and argument 2 is $$clientId
    And I delete packet data

  @scenario_30
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I multi factor authentication where argument 1 is faceDevice, and argument 2 is dob, and argument 3 is UIN, and argument 4 is $$uin, and argument 5 is $$personaFilePath, and argument 6 is $$vid, and argument 7 is $$email
    And I delete packet data

  @scenario_31
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I multi factor authentication where argument 1 is faceDevice, and argument 2 is dob, and argument 3 is UIN, and argument 4 is $$uin, and argument 5 is $$personaFilePath, and argument 6 is $$vid, and argument 7 is $$email
    And I delete packet data

  @scenario_32
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is addressLine1=bnglr, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I get email by uin where argument 1 is $$uin2 and store result in $$email
    And I credential request where argument 1 is $$uin2, and argument 2 is $$email and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I verify notification where argument 1 is Credential Issuance Status, and argument 2 is $$email
    And I download card where argument 1 is $$requestId
    And I delete packet data

  @scenario_33
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is /*BIO_TYPE*/0, and miss fields is 0, and update attributes is email=test, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I get email by uin where argument 1 is $$uin2 and store result in $$email
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I otp authentication where argument 1 is uin, and argument 2 is $$uin2, and argument 3 is vid, and argument 4 is $$vid, and argument 5 is $$email
    And I oidc client where  and store result in $$clientId
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId1 and store result in $$transactionId1
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId2 and store result in $$transactionId2
    And I esignet authentication where argument 1 is $$transactionId1, and argument 2 is $$uin, and argument 3 is OTP, and argument 4 is $$email, and argument 5 is $$vid, and argument 6 is $$transactionId2
    And I user info where argument 1 is $$transactionId1, and argument 2 is $$clientId
    And I delete packet data

  @scenario_34
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident child walks into registration center completes the process and gets ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is minor, and argument 2 is true, and argument 3 is Male and store result in $$childPersona
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I wait where argument 1 is UIN_WAIT_TIME
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$childPersona
    And I update resident with uin where argument 1 is $$childPersona, and argument 2 is $$childUin
    And I get packet template where packet type is UPDATE, and argument 2 is $$childPersona and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$updateTemplate and store result in $$childRid2
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid2
    And I get uinby rid where argument 1 is $$childRid2 and store result in $$childUin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email1
    And I wait where argument 1 is UIN_WAIT_TIME
    And I credential request where argument 1 is $$childUin2, and argument 2 is $$email1 and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I verify notification where argument 1 is Credential Issuance Status, and argument 2 is $$email1
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$childUin2, and argument 3 is $$email1 and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email1
    And I wait where argument 1 is 90
    And I demo authentication where argument 1 is name, and argument 2 is $$childUin2, and argument 3 is $$childPersona, and argument 4 is $$vid
    And I delete packet data

  @scenario_35
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I get email by uin where argument 1 is $$uin and store result in $$email1
    And I credential request where argument 1 is $$uin, and argument 2 is $$email1 and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I verify notification where argument 1 is Credential Issuance Status, and argument 2 is $$email1
    And I download card where argument 1 is $$requestId
    And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email1
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email1 and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email1
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is faceDevice, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_36
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A differently abled resident walksin to registration center and completes the...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male, and argument 4 is leftiris, and password is rightIris and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_37
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I upload packet with invalid hash where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    Then I user where user action is ADD_User, and user index is 4, and password is Techno@123 and store result in $$user4
    Then I center where argument 1 is CREATE, and argument 2 is $$user4, and center index is 4, and center active flag is T and store result in $$center4
    Then I machine where argument 1 is CREATE, and argument 2 is $$center4, and center index is 4 and store result in $$details4
    Then I user where user action is DELETE_CENTERMAPPING, and user index is 4, and password is Techno@123, and password or details is $$details4 and store result in $$details4
    Then I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details4 and store result in $$details4
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details4
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details4
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details4, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details4, and center index is 4
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details4, and user center mapping flag is T
    And I write pre req where argument 1 is $$details4, and pre requisite data index is 4
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details4, and generate private key is true
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$newTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I delete packet data

  @scenario_38
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident booked pre-registration with support documents. walks into registrat...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 2 and store result in $$details2
    And I set context where argument 1 is env_context, and argument 2 is $$details2, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I send otp where argument 1 is $$personaFilePath and store result in $$email
    And I validate otp where argument 1 is $$personaFilePath, and argument 2 is $$email
    And I pre register where argument 1 is $$personaFilePath and store result in $$prid
    And I upload documents where argument 1 is $$personaFilePath, and argument 2 is $$prid
    And I update pre reg status where argument 1 is 0, and argument 2 is $$prid, and argument 3 is valid
    And I book appointment where holiday booking flag is false, and argument 2 is $$prid, and slot number is 2
    And I generate and upload packet where argument 1 is $$prid, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I read pre req where pre requisite data index is 2 and store result in $$details2
    And I set context where argument 1 is env_context, and argument 2 is $$details2, and generate private key is false
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath2
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath2 and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid2
    And I packetsync where argument 1 is $$zipPacketPath
    And I post mock mv where argument 1 is $$rid2, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$rid2
    And I delete packet data

  @scenario_39
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration same center where his previous application g...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I upload packet with invalid hash where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$newTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I delete packet data

  @scenario_40
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_41
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is rightRingDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_42
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is RightIris, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_43
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A differently abled resident walks into registration center completes the pro...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Iris:Left, and password is Iris:Right
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_44
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Temporary, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_45
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Temporary, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is leftRingDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_46
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Temporary, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is LeftIris, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_47
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A differently abled resident walks into registration center completes the pro...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Male, and argument 4 is leftiris, and password is rightIris and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Temporary, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_75
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I wait where argument 1 is 35
    And I user where user action is ADD_User, and user index is 75, and password is Techno@123 and store result in $$user75
    And I center where argument 1 is CREATE, and user details is $$user75, and center index is 75, and center active flag is T and store result in $$center75
    And I machine where argument 1 is CREATE, and argument 2 is $$center75, and center index is 75 and store result in $$details75
    And I user where user action is DELETE_CENTERMAPPING, and user index is 75, and password is Techno@123, and password or details is $$details75 and store result in $$details75
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details75 and store result in $$details75
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details75
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details75
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details75, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details75, and center index is 75
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details75, and user center mapping flag is T
    And I write pre req where argument 1 is $$details75, and pre requisite data index is 75
    And I read pre req where pre requisite data index is 75 and store result in $$details75
    And I set context where argument 1 is env_context, and argument 2 is $$details75, and generate private key is true
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I user where user action is UPDATE_UIN, and user index is 75, and password is Techno@123, and password or details is $$uin
    And I set context where argument 1 is env_context, and argument 2 is $$details75, and generate private key is false, and put scenario details in context is null, and add supervisor id is valid, and password is null/*SUPERVISOR_PASSWORD*/@@valid/*ADD_REGCLIENT_USER_ID*/@@null/*REGCLIENT_PASSWORD*/@@operatorBiometrics_bio_CBEFF@@supervisorBiometrics_bio_CBEFF
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath1
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath1 and store result in $$templatePath1
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath1 and store result in $$zipPacketPath1
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath1 and store result in $$rid1
    And I packetsync where argument 1 is $$zipPacketPath1
    And I set context where argument 1 is env_context, and argument 2 is $$details75, and generate private key is false
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    Then I check ridstage where argument 1 is $$rid1, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I machine where argument 1 is DCOM, and argument 2 is $$details75
    Then I user where user action is DELETE_CENTERMAPPING, and user index is 75, and password is Techno@123, and password or details is $$details75 and store result in $$details75
    Then I center where argument 1 is DCOM, and argument 2 is $$details75, and center index is 75
    And I delete packet data

  @scenario_48
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process but while the p...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I corrupt packet where argument 1 is 1024, and data to encode is hello automation, and argument 3 is $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    And I wait where argument 1 is 90
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is UPLOAD_PACKET, and argument 3 is ERROR
    And I delete packet data

  @scenario_49
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident Minor Child walks into registration center to get UIN card. Later tr...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is minor, and argument 2 is true, and argument 3 is Male and store result in $$childPersona
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona1
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona1 and store result in $$parentTemplate1
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona1, and argument 2 is $$parentTemplate1 and store result in $$parentRid1
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid1
    And I get uinby rid where argument 1 is $$parentRid1 and store result in $$parentUin1
    And I get email by uin where argument 1 is $$parentUin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with rid where argument 1 is $$parentPersona1, and argument 2 is $$parentRid1
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona1, and argument 2 is $$childPersona
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona2
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona2 and store result in $$parentTemplate2
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona2, and argument 2 is $$parentTemplate2 and store result in $$parentRid2
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid2
    And I get uinby rid where argument 1 is $$parentRid2 and store result in $$parentUin2
    And I get email by uin where argument 1 is $$parentUin2 and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I update resident with rid where argument 1 is $$parentPersona2, and argument 2 is $$parentRid2
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona2, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_50
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident Minor Child walks into registration center to get UIN card. Later tr...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona1
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona1 and store result in $$parentTemplate1
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona1, and argument 2 is $$parentTemplate1 and store result in $$parentRid1
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid1
    And I get uinby rid where argument 1 is $$parentRid1 and store result in $$parentUin1
    And I get email by uin where argument 1 is $$parentUin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with rid where argument 1 is $$parentPersona1, and argument 2 is $$parentRid1
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona1, and argument 2 is $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona1, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_3
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I get packet template where packet type is LOST, and argument 2 is $$personaFilePath and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$ridLost
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is PROCESSED, and argument 2 is $$ridLost
    And I get uinby rid where argument 1 is $$ridLost and store result in $$uin2
    And I get email by uin where argument 1 is $$uin2 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$ridLost, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_51
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process but due to low ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attribute is email=additionalReqId_51, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath, and argument 3 is 10 and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$rid, and argument 2 is INTERNAL_WORKFLOW_ACTION, and argument 3 is SUCCESS, and argument 4 is RPR-WIA-001
    Then I get additional req id where argument 1 is additionalReqId_51 and store result in $$additionalReqId
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath2
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_51, and persona file is $$personaFilePath2
    And I get packet template where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$personaFilePath2 and store result in $$templatePath2
    And I packetcreator where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$templatePath2, and argument 3 is $$additionalReqId and store result in $$zipPacketPath2
    And I ridsync where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$zipPacketPath2, and argument 3 is $$additionalReqId and store result in $$rid2
    And I packetsync where argument 1 is $$zipPacketPath2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_58
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process but while the p...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where context key value is qa4_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I skip
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I upload packet with invalid hash where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_59
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center with his child and completes the proc...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$parentZipPacketPath
    And I rid sync rejected where packet type is NEW, and argument 2 is $$parentZipPacketPath and store result in $$parentRid
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update resident with rid where argument 1 is $$parentPersona, and argument 2 is $$parentRid
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I packetcreator where packet type is NEW, and argument 2 is $$childTemplate and store result in $$childZipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$childZipPacketPath and store result in $$childRid
    And I packetsync where argument 1 is $$parentZipPacketPath
    And I packetsync where argument 1 is $$childZipPacketPath
    And I check status where packet status is REREGISTER, and argument 2 is $$childRid
    And I check status where packet status is REREGISTER, and argument 2 is $$parentRid
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is ERROR
    Then I check ridstage where argument 1 is $$parentRid, and argument 2 is VALIDATE_PACKET, and argument 3 is REJECTED
    And I delete packet data

  @scenario_60
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident booked pre-registration without documents trying to update prereg st...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I send otp where argument 1 is $$personaFilePath and store result in $$email
    And I validate otp where argument 1 is $$personaFilePath, and argument 2 is $$email
    And I pre register where argument 1 is $$personaFilePath and store result in $$prid
    And I update pre reg status where status code is 0, and argument 2 is $$prid, and scenario with pending appointment is invalid
    And I delete packet data

  @scenario_61
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process but supervisor ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I rid sync rejected where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is REJECTED
    And I delete packet data

  @scenario_62
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into get UIN card and different resident tries to get UIN both...
    Given I get ping health where argument 1 is packetcreator
    And I delete mock expect
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is leftIris, and password is rigthIris, and miss fields is 0, and update attributes is 0, and argument 5 is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid2
    And I post mock mv where argument 1 is $$rid2, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$rid2
    And I delete packet data

  @scenario_63
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process but the packet ...
    Given I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$newTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid2
    And I post mock mv where argument 1 is $$rid2, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$rid2
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_64
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$ridNew
    And I post mock mv where argument 1 is $$ridNew, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$ridNew
    Then I check ridstage where argument 1 is $$ridNew, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_65
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid2
    And I post mock mv where argument 1 is $$rid2, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$rid2
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is DEMOGRAPHIC_VERIFICATION, and argument 3 is IN_PROGRESS
    And I delete packet data

  @scenario_66
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePathNew
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePathNew and store result in $$ridNew
    And I post mock mv where argument 1 is $$ridNew, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$ridNew
    Then I check ridstage where argument 1 is $$ridNew, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_67
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But packet get...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 2 and store result in $$details2
    And I set context where argument 1 is env_context, and argument 2 is $$details2, and generate private key is false, and argument 4 is null, and argument 5 is /*SUPERVISORID_ID*/null, and password is null/*SUPERVISORID_PASSWORD*/@@valid/*REGCLIENT_USER_ID*/@@valid
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_68
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But packet get...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 2 and store result in $$details2
    And I set context where argument 1 is env_context, and argument 2 is $$details2, and argument 3 is false, and argument 4 is null, and argument 5 is invalid, and password is valid@@valid@@valid
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is SUPERVISOR_VALIDATION, and argument 3 is ERROR
    And I delete packet data

  @scenario_69
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But packet get...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 3 and store result in $$details3
    And I set context where argument 1 is env_context, and argument 2 is $$details3, and generate private key is false, and put scenario details in context is null, and supervisor id is valid, and password is invalid/*SUPERVISOR_PASSWORD*/@@valid/*REGCLIENT_USER_ID*/@@valid
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is SUPERVISOR_VALIDATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_70
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But packet get...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 2 and store result in $$details2
    And I set context where argument 1 is env_context, and argument 2 is $$details2, and generate private key is false, and put scenario details in context is null, and supervisor id is valid, and password is valid/*SUPERVISOR_PASSWORD*/@@null/*REGCLIENT_USER_ID*/@@null
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_71
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But packet get...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 3 and store result in $$details3
    And I set context where argument 1 is env_context, and argument 2 is $$details3, and generate private key is false, and put scenario details in context is null, and supervisor id is valid, and password is valid/*SUPERVISOR_PASSWORD*/@@invalid/*REGCLIENT_USER_ID*/@@valid
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is OPERATOR_VALIDATION, and argument 3 is ERROR
    And I delete packet data

  @scenario_72
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But packet get...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 2 and store result in $$details2
    And I set context where argument 1 is env_context, and argument 2 is $$details2, and argument 3 is false, and argument 4 is null, and argument 5 is valid, and password is valid@@valid@@invalid
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is OPERATOR_VALIDATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_73
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But packet get...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 2 and store result in $$details2
    And I set context where argument 1 is env_context, and argument 2 is $$details2, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I user where user action is ADD_User, and user index is 2, and password is Techno@123, and password or details is $$uin
    And I set context where argument 1 is env_context, and argument 2 is $$details2, and generate private key is false, and put scenario details in context is null, and supervisor id is null, and password is null/*SUPERVISOR_PASSWORD*/@@null/*REGCLIENT_ID*/@@null/*REGCLIENT_PASSWORD*/@@OperatorBiometrics_bio_CBEFF@@SupervisorBiometrics_bio_CBEFF
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid1
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is REREGISTER, and argument 2 is $$rid1
    Then I check ridstage where argument 1 is $$rid1, and argument 2 is INTERNAL_WORKFLOW_ACTION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_74
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But packet get...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 2 and store result in $$details2
    And I set context where argument 1 is env_context, and argument 2 is $$details2, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I user where user action is ADD_User, and user index is 3, and password is Techno@123, and password or details is $$uin
    And I set context where argument 1 is env_context, and argument 2 is $$details3, and argument 3 is false, and argument 4 is null, and argument 5 is null, and password is null@@null@@null@@null@@null
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is INTERNAL_WORKFLOW_ACTION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_76
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I wait where argument 1 is 45
    And I user where user action is ADD_User, and user index is 76, and password is Techno@123 and store result in $$user76
    And I center where argument 1 is CREATE, and argument 2 is $$user76, and center index is 76, and center active flag is T and store result in $$center76
    And I machine where argument 1 is CREATE, and argument 2 is $$center76, and center index is 76 and store result in $$details76
    And I user where user action is DELETE_CENTERMAPPING, and user index is 76, and password is Techno@123, and password or details is $$details76 and store result in $$details76
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details76 and store result in $$details76
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details76
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details76
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details76, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details76, and center index is 76
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details76, and user center mapping flag is T
    And I write pre req where argument 1 is $$details76, and pre requisite data index is 76
    And I read pre req where pre requisite data index is 76 and store result in $$details76
    And I set context where argument 1 is env_context, and argument 2 is $$details76, and generate private key is true
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I user where user action is UPDATE_UIN, and user index is 76, and password is Techno@123, and password or details is $$uin
    And I set context where argument 1 is env_context, and argument 2 is $$details76, and generate private key is true, and put scenario details in context is null, and supervisor id is valid, and password is null/*SUPERVISOR_PASSWORD*/@@null/*REGCLIENT_USER_ID*/@@null/*REGCLIENT_PASSWORD*/@@null/*OPERATOR_CBEFF*/@@supervisorBiometrics_bio_CBEFF
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath1
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath1 and store result in $$templatePath1
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath1 and store result in $$zipPacketPath1
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath1 and store result in $$rid1
    And I packetsync where argument 1 is $$zipPacketPath1
    And I set context where argument 1 is env_context, and argument 2 is $$details76, and generate private key is true
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    Then I check ridstage where argument 1 is $$rid1, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I machine where argument 1 is DCOM, and argument 2 is $$details76
    Then I user where user action is DELETE_CENTERMAPPING, and user index is 76, and password is Techno@123, and password or details is $$details76 and store result in $$details76
    Then I center where argument 1 is DCOM, and argument 2 is $$details76, and center index is 76
    And I delete packet data

  @scenario_78
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident booked pre-registration with support documents. Later cancels booked...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I send otp where argument 1 is $$personaFilePath and store result in $$email
    And I validate otp where argument 1 is $$personaFilePath, and argument 2 is $$email
    And I pre register where argument 1 is $$personaFilePath and store result in $$prid
    And I upload documents where argument 1 is $$personaFilePath, and argument 2 is $$prid
    And I update pre reg status where argument 1 is 0, and argument 2 is $$prid, and argument 3 is valid
    And I book appointment where holiday booking flag is false, and argument 2 is $$prid, and slot number is 2
    And I cancel appointment where argument 1 is cancel, and argument 2 is $$prid
    And I book appointment where holiday booking flag is false, and argument 2 is $$prid, and slot number is 3
    And I generate and upload packet where argument 1 is $$prid, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_79
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident attempts to obtain UIN but Packet Creation Date is past date and get...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and argument 4 is invalidCreationDate=-1y
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I delete packet data

  @scenario_80
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident booked pre-registration with support documents and already used the ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I send otp where argument 1 is $$personaFilePath and store result in $$email
    And I validate otp where argument 1 is $$personaFilePath, and argument 2 is $$email
    And I pre register where argument 1 is $$personaFilePath and store result in $$prid
    And I upload documents where argument 1 is $$personaFilePath, and argument 2 is $$prid
    And I update pre reg status where argument 1 is 0, and argument 2 is $$prid, and argument 3 is valid
    And I book appointment where holiday booking flag is false, and argument 2 is $$prid, and slot number is 1
    And I generate and upload packet where argument 1 is $$prid, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I generate and upload packet where argument 1 is $$prid, and argument 2 is $$templatePath and store result in $$rid2
    And I post mock mv where argument 1 is $$rid2, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$rid2
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is DEMOGRAPHIC_VERIFICATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_52
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Check Syncdata response with upper key index and user with valid roles
    Given I get ping health where argument 1 is packetcreator
    And I user where user action is ADD_User, and user index is 52, and password is Techno@123 and store result in $$user52
    And I center where argument 1 is CREATE, and argument 2 is $$user52, and center index is 52, and center active flag is T and store result in $$center52
    And I machine where argument 1 is CREATE, and argument 2 is $$center52, and center index is 52 and store result in $$details52
    And I user where user action is DELETE_CENTERMAPPING, and user index is 52, and password is Techno@123, and password or details is $$details52 and store result in $$details52
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details52 and store result in $$details52
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details52
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details52
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details52, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details52, and center index is 52
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details52, and user center mapping flag is T
    And I write pre req where argument 1 is $$details52, and pre requisite data index is 52
    And I read pre req where pre requisite data index is 52 and store result in $$details52
    And I wait where argument 1 is 9
    And I sync data where argument 1 is TPM_VERIFY, and argument 2 is $$details52, and key index is UPPER and store result in $$keyIndex
    And I sync data where argument 1 is CLIENT_SETTINGS_VALID, and argument 2 is $$keyIndex, and center index is 52
    And I sync data where argument 1 is LATEST_ID_SCHEMA
    And I sync data where argument 1 is CONFIGS_KEYINDEX, and argument 2 is $$keyIndex
    And I sync data where argument 1 is USER_DETAILS, and argument 2 is $$details52
    And I machine where argument 1 is DCOM, and argument 2 is $$details52
    And I user where user action is DELETE_CENTERMAPPING, and user index is 52, and password is Techno@123, and password or details is $$details52 and store result in $$details52
    And I center where argument 1 is DCOM, and argument 2 is $$details52, and center index is 52
    And I delete packet data

  @scenario_53
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Update machine from centerA to centerB and verify syncdata client settings wi...
    Given I get ping health where argument 1 is packetcreator
    And I wait where argument 1 is 30
    And I user where user action is ADD_User, and user index is 53, and password is Techno@123 and store result in $$user53
    And I center where argument 1 is CREATE, and argument 2 is $$user53, and center index is 53, and center active flag is T and store result in $$center53
    And I machine where argument 1 is CREATE, and argument 2 is $$center53, and center index is 53 and store result in $$details53
    And I user where user action is DELETE_CENTERMAPPING, and user index is 53, and password is Techno@123, and password or details is $$details53 and store result in $$details53
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details53 and store result in $$details53
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details53
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details53
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details53, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details53, and center index is 53
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details53, and user center mapping flag is T
    And I write pre req where argument 1 is $$details53, and pre requisite data index is 53
    And I user where user action is ADD_User, and user index is 531, and password is Techno@123 and store result in $$user531
    And I center where argument 1 is CREATE, and argument 2 is $$user531, and center index is 531, and center active flag is T and store result in $$center531
    And I machine where argument 1 is CREATE, and argument 2 is $$center531, and center index is 531 and store result in $$details531
    And I user where user action is DELETE_CENTERMAPPING, and user index is 531, and password is Techno@123, and password or details is $$details531 and store result in $$details531
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details531 and store result in $$details531
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details531
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details531
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details531, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details531, and center index is 531
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details531, and user center mapping flag is T
    And I write pre req where argument 1 is $$details531, and pre requisite data index is 531
    And I read pre req where pre requisite data index is 53 and store result in $$details53
    And I wait where argument 1 is 9
    And I sync data where argument 1 is TPM_VERIFY, and argument 2 is $$details53, and key index is UPPER and store result in $$keyIndex
    And I sync data where argument 1 is CLIENT_SETTINGS_VALID, and argument 2 is $$keyIndex, and center index is 53
    And I read pre req where pre requisite data index is 531 and store result in $$details531
    And I machine where packet type is UPDATE, and argument 2 is $$details531, and center index is 531 and store result in $$details53
    And I sync data where argument 1 is CLIENT_SETTINGS_INVALID, and argument 2 is $$keyIndex, and center index is 531
    And I machine where packet type is UPDATE, and argument 2 is $$details53, and center index is 53 and store result in $$details53
    And I machine where argument 1 is DCOM, and user details is $$details531
    And I user where user action is DELETE_CENTERMAPPING, and user index is 531, and password is Techno@123, and password or details is $$details531 and store result in $$details531
    And I center where argument 1 is DCOM, and argument 2 is $$details531, and center index is 531
    And I delete packet data

  @scenario_54
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Inactive machine and verify syncdata client settings calls
    Given I get ping health where argument 1 is packetcreator
    And I wait where argument 1 is 40
    And I user where user action is ADD_User, and user index is 54, and password is Techno@123 and store result in $$user54
    And I center where argument 1 is CREATE, and argument 2 is $$user54, and center index is 54, and center active flag is T and store result in $$center54
    And I machine where argument 1 is CREATE, and argument 2 is $$center54, and center index is 54 and store result in $$details54
    And I user where user action is DELETE_CENTERMAPPING, and user index is 54, and password is Techno@123, and password or details is $$details54 and store result in $$details54
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details54 and store result in $$details54
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details54
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details54
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details54, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details54, and center index is 54
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details54, and user center mapping flag is T
    And I write pre req where argument 1 is $$details54, and pre requisite data index is 54
    And I read pre req where pre requisite data index is 54 and store result in $$details54
    And I machine where argument 1 is ACTIVE_FLAG, and argument 2 is $$details54, and center index is 54, and machine activation flag is F
    And I wait where argument 1 is 9
    And I sync data where argument 1 is TPM_VERIFY, and argument 2 is $$details54, and key index is UPPER and store result in $$keyIndex
    And I sync data where argument 1 is CLIENT_SETTINGS_VALID, and argument 2 is $$keyIndex, and center index is 54
    And I sync data where argument 1 is CONFIGS_KEYINDEX, and argument 2 is $$keyIndex
    And I sync data where argument 1 is USER_DETAILS, and argument 2 is $$details54
    And I machine where argument 1 is ACTIVE_FLAG, and argument 2 is $$details54, and center index is 54, and machine activation flag is T
    And I machine where argument 1 is DCOM, and argument 2 is $$details54
    And I user where user action is DELETE_CENTERMAPPING, and user index is 54, and password is Techno@123, and password or details is $$details54 and store result in $$details54
    And I center where argument 1 is DCOM, and argument 2 is $$details54, and center index is 54
    And I delete packet data

  @scenario_55
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Decomission machine and verify syncdata client settings calls
    Given I get ping health where argument 1 is packetcreator
    And I wait where argument 1 is 50
    And I user where user action is ADD_User, and user index is 55, and password is Techno@123 and store result in $$user55
    And I center where argument 1 is CREATE, and argument 2 is $$user55, and center index is 55, and center active flag is T and store result in $$center55
    And I machine where argument 1 is CREATE, and argument 2 is $$center55, and center index is 55 and store result in $$details55
    And I user where user action is DELETE_CENTERMAPPING, and user index is 55, and password is Techno@123, and password or details is $$details55 and store result in $$details55
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details55 and store result in $$details55
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details55
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details55
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details55, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details55, and center index is 55
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details55, and user center mapping flag is T
    And I write pre req where argument 1 is $$details55, and pre requisite data index is 55
    And I read pre req where pre requisite data index is 55 and store result in $$details55
    And I machine where argument 1 is DCOM, and argument 2 is $$details55
    And I wait where argument 1 is 9
    And I sync data where argument 1 is TPM_VERIFY_INVALID, and argument 2 is $$details55, and key index is UPPER and store result in $$keyIndex
    And I user where user action is DELETE_CENTERMAPPING, and user index is 55, and password is Techno@123, and password or details is $$details55 and store result in $$details55
    And I center where argument 1 is DCOM, and argument 2 is $$details55, and center index is 55
    And I delete packet data

  @scenario_56
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Decommission center and verify USER_DETAILS syncdata calls 
    Given I get ping health where argument 1 is packetcreator
    And I wait where argument 1 is 60
    And I user where user action is ADD_User, and user index is 56, and password is Techno@123 and store result in $$user56
    And I center where argument 1 is CREATE, and argument 2 is $$user56, and center index is 56, and center active flag is T and store result in $$center56
    And I machine where argument 1 is CREATE, and argument 2 is $$center56, and center index is 56 and store result in $$details56
    And I user where user action is DELETE_CENTERMAPPING, and user index is 56, and password is Techno@123, and password or details is $$details56 and store result in $$details56
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details56 and store result in $$details56
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details56
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details56
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details56, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details56, and center index is 56
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details56, and user center mapping flag is T
    And I write pre req where argument 1 is $$details56, and pre requisite data index is 56
    And I read pre req where pre requisite data index is 56 and store result in $$details56
    And I user where user action is DELETE_CENTERMAPPING, and user index is 56, and password is Techno@123, and password or details is $$details56 and store result in $$details56
    And I machine where argument 1 is REMOVE_CENTER, and argument 2 is $$details56
    And I center where argument 1 is DCOM, and argument 2 is $$details56, and center index is 56
    And I wait where argument 1 is 9
    And I sync data where argument 1 is TPM_VERIFY, and argument 2 is $$details56, and key index is UPPER and store result in $$keyIndex
    And I sync data where argument 1 is CONFIGS_KEYINDEX, and argument 2 is $$keyIndex
    And I delete packet data

  @scenario_57
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Deactivate center and verify CLIENT_SETTINGS syncdata client settings calls 
    Given I get ping health where argument 1 is packetcreator
    And I wait where argument 1 is 70
    And I user where user action is ADD_User, and user index is 57, and password is Techno@123 and store result in $$user57
    And I center where argument 1 is CREATE, and argument 2 is $$user57, and center index is 57, and center active flag is T and store result in $$center57
    And I machine where argument 1 is CREATE, and argument 2 is $$center57, and center index is 57 and store result in $$details57
    And I user where user action is DELETE_CENTERMAPPING, and user index is 57, and password is Techno@123, and password or details is $$details57 and store result in $$details57
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details57 and store result in $$details57
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details57
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details57
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details57, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details57, and center index is 57
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details57, and user center mapping flag is T
    And I write pre req where argument 1 is $$details57, and append key for prerequisite data is 57
    And I read pre req where pre requisite data index is 57 and store result in $$details57
    And I user where user action is DELETE_CENTERMAPPING, and user index is 57, and password is Techno@123, and password or details is $$details57 and store result in $$details57
    And I machine where argument 1 is REMOVE_CENTER, and argument 2 is $$details57
    And I center where argument 1 is ACTIVE_FLAG, and argument 2 is $$details57, and center index is 57, and center active flag is F
    And I wait where argument 1 is 9
    And I sync data where argument 1 is TPM_VERIFY, and argument 2 is $$details57, and key index is UPPER and store result in $$keyIndex
    And I sync data where argument 1 is CONFIGS_KEYINDEX, and argument 2 is $$keyIndex
    And I sync data where argument 1 is CLIENT_SETTINGS_INVALID, and argument 2 is $$keyIndex, and center index is 57
    And I machine where argument 1 is DCOM, and argument 2 is $$details57
    And I center where argument 1 is DCOM, and argument 2 is $$details57, and center index is 57
    And I delete packet data

  @scenario_81
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process but packet gets...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I get bio modality hash where check persona presence is -1, and argument 2 is Right IndexFinger, and password is Left LittleFinger, and argument 4 is $$personaFilePath and store result in $$modalityHashValue
    And I configure mock abis where check persona presence is -1, and argument 2 is Right IndexFinger, and argument 3 is false, and argument 4 is Right IndexFinger, and argument 5 is $$personaFilePath, and argument 6 is $$modalityHashValue, and default mock delay is -1, and password is Success
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I delete mock expect where argument 1 is $$modalityHashValue
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_82
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process but packet gets...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I get bio modality hash where check persona presence is -1, and argument 2 is Right IndexFinger, and password is Left LittleFinger, and argument 4 is $$personaFilePath and store result in $$modalityHashValue
    And I configure mock abis where check persona presence is -1, and argument 2 is Right IndexFinger, and password is Left LittleFinger, and argument 4 is false, and argument 5 is Right IndexFinger, and password is Left LittleFinger, and argument 7 is $$personaFilePath, and argument 8 is $$modalityHashValue, and delay from actuator is delay, and error code is 10, and password is Error
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is BIOGRAPHIC_VERIFICATION, and argument 3 is REPROCESS
    Then I delete mock expect where argument 1 is $$modalityHashValue
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_83
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process with biometric ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Iris:Left, and password is Iris:Right
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is face, and password is iris, and miss fields is 0, and update attributes is 0, and argument 5 is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I oidc client where  and store result in $$clientId
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId1 and store result in $$transactionId1
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId2 and store result in $$transactionId2
    And I bio esignet authentication where argument 1 is faceDevice, and argument 2 is $$uin2, and argument 3 is $$personaFilePath, and argument 4 is $$transactionId1, and argument 5 is $$vid, and argument 6 is $$transactionId2
    And I user info where argument 1 is $$transactionId, and argument 2 is $$clientId
    And I delete packet data

  @scenario_77
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I wait where argument 1 is 55
    And I user where user action is ADD_User, and user index is 77, and password is Techno@123 and store result in $$user77
    And I center where argument 1 is CREATE, and argument 2 is $$user77, and center index is 77, and center active flag is T and store result in $$center77
    And I machine where argument 1 is CREATE, and argument 2 is $$center77, and argument 3 is 77 and store result in $$details77
    And I user where user action is DELETE_CENTERMAPPING, and user index or master user is 77, and password is Techno@123, and password or details is $$details77 and store result in $$details77
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details77 and store result in $$details77
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details77
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details77
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details77, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details77, and password is 77
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details77, and user center mapping flag is T
    And I write pre req where argument 1 is $$details77, and pre requisite data index is 77
    And I read pre req where pre requisite data index is 77 and store result in $$details77
    And I set context where argument 1 is env_context, and argument 2 is $$details77, and generate private key is true
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I user where user action is UPDATE_UIN, and user index is 77, and password is Techno@123, and password or details is $$uin
    And I set context where argument 1 is env_context, and argument 2 is $$details77, and argument 3 is false, and argument 4 is null, and argument 5 is null, and password is null@@valid@@null@@operatorBiometrics_bio_CBEFF@@null
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath1
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath1 and store result in $$templatePath1
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath1 and store result in $$zipPacketPath1
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath1 and store result in $$rid1
    And I packetsync where argument 1 is $$zipPacketPath1
    And I set context where argument 1 is env_context, and argument 2 is $$details77, and generate private key is false
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    Then I check ridstage where argument 1 is $$rid1, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I machine where argument 1 is DCOM, and argument 2 is $$details77
    Then I user where user action is DELETE_CENTERMAPPING, and user index or master user is 77, and password is Techno@123/**PASSWORD/, and password or details is $$details77 and store result in $$details77
    Then I center where argument 1 is DCOM, and argument 2 is $$details77, and center index is 77
    And I delete packet data

  @scenario_84
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process but the device ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and invalid device certificate is true
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is reregister, and argument 2 is $$rid
    And I delete packet data

  @scenario_85
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But due to low...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_85, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath, and biometric quality score is 15 and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath2
    And I get packet template where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$personaFilePath2 and store result in $$templatePath2
    And I packetcreator where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$templatePath2, and argument 3 is 11111111111111111111111111111 and store result in $$zipPacketPath2
    And I ridsync where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$zipPacketPath2, and request info for correction packet is 11111111111111111111111111111 and store result in $$rid2
    And I packetsync where argument 1 is $$zipPacketPath2, and expected to pass flag is false
    And I delete packet data

  @scenario_86
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But due to low...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_86, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath, and biometric quality score is 10 and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    And I get additional req id where argument 1 is additionalReqId_86 and store result in $$additionalReqId
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath1
    And I get packet template where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$personaFilePath1 and store result in $$templatePath1
    And I get bio modality hash where check persona presence is -1, and argument 2 is Right IndexFinger, and password is Left LittleFinger, and argument 4 is $$personaFilePath1 and store result in $$modalityHashValue
    And I configure mock abis where check persona presence is -1, and argument 2 is Right IndexFinger, and duplicate flag is true, and argument 4 is Right IndexFinger, and argument 5 is $$personaFilePath1, and argument 6 is $$modalityHashValue, and default mock delay is -1, and password is Duplicate
    And I packetcreator where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$templatePath1, and argument 3 is $$additionalReqId and store result in $$zipPacketPath1
    And I ridsync where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$zipPacketPath1, and argument 3 is $$additionalReqId and store result in $$rid1
    And I packetsync where argument 1 is $$zipPacketPath1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I delete packet data

  @scenario_87
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But due to low...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_87, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath, and biometric quality score is 10 and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$rid, and argument 2 is INTERNAL_WORKFLOW_ACTION, and argument 3 is SUCCESS
    Then I get additional req id where argument 1 is additionalReqId_87 and store result in $$additionalReqId
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath2
    And I get packet template where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$personaFilePath2 and store result in $$templatePath2
    And I packetcreator where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$templatePath2, and argument 3 is $$additionalReqId and store result in $$zipPacketPath2
    And I ridsync where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$zipPacketPath2, and argument 3 is $$additionalReqId and store result in $$rid2
    And I packetsync where argument 1 is $$zipPacketPath2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath3
    And I get packet template where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$personaFilePath3 and store result in $$templatePath3
    And I packetcreator where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$templatePath3, and argument 3 is $$additionalReqId and store result in $$zipPacketPath3
    And I ridsync where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$zipPacketPath3, and argument 3 is $$additionalReqId and store result in $$rid3
    And I packetsync where argument 1 is $$zipPacketPath3
    Then I check ridstage where argument 1 is $$rid3, and argument 2 is SECUREZONE_NOTIFICATION, and argument 3 is REJECTED
    And I delete packet data

  @scenario_#88
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But due to low...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_88, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath, and biometric quality score is 15 and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_89
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But due to low...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_89, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath, and biometric quality score is 15 and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$rid, and argument 2 is INTERNAL_WORKFLOW_ACTION, and argument 3 is SUCCESS, and argument 4 is RPR-WIA-001
    Then I get additional req id where argument 1 is additionalReqId_89 and store result in $$additionalReqId
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath2
    And I get packet template where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$personaFilePath2, and biometric quality score is 20 and store result in $$templatePath2
    And I packetcreator where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$templatePath2, and argument 3 is $$additionalReqId and store result in $$zipPacketPath2
    And I ridsync where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$zipPacketPath2, and argument 3 is $$additionalReqId and store result in $$rid2
    And I packetsync where argument 1 is $$zipPacketPath2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get additional req id where argument 1 is additionalReqId_89 and store result in $$additionalReqId2
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath3
    And I get packet template where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$personaFilePath3, and biometric quality score is 30 and store result in $$templatePath3
    And I packetcreator where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$templatePath3, and argument 3 is $$additionalReqId2 and store result in $$zipPacketPath3
    And I ridsync where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$zipPacketPath3, and argument 3 is $$additionalReqId2 and store result in $$rid3
    And I packetsync where argument 1 is $$zipPacketPath3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I check status where packet status is REJECTED, and argument 2 is $$rid
    And I delete packet data

  @scenario_90
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But due to low...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_90, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath, and biometric quality score is 15 and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$rid, and argument 2 is INTERNAL_WORKFLOW_ACTION, and argument 3 is SUCCESS, and argument 4 is RPR-WIA-001
    Then I get additional req id where argument 1 is additionalReqId_90 and store result in $$additionalReqId
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath2
    And I get packet template where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$personaFilePath2 and store result in $$templatePath2
    And I packetcreator where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$templatePath2, and argument 3 is $$additionalReqId and store result in $$zipPacketPath2
    And I wait till reprocessor interval
    And I ridsync where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$zipPacketPath2, and argument 3 is $$additionalReqId and store result in $$rid2
    And I packetsync where argument 1 is $$zipPacketPath2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I delete packet data

  @scenario_91
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process. But due to low...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=additionalReqId_91, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath, and biometric quality score is 15 and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$rid, and argument 2 is INTERNAL_WORKFLOW_ACTION, and argument 3 is SUCCESS, and argument 4 is RPR-WIA-001
    Then I get additional req id where argument 1 is additionalReqId_91 and store result in $$additionalReqId
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath2
    And I get packet template where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$personaFilePath2 and store result in $$templatePath2
    And I packetcreator where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$templatePath2, and argument 3 is $$additionalReqId and store result in $$zipPacketPath2
    And I wait till reprocessor interval
    And I ridsync where packet type is BIOMETRIC_CORRECTION, and argument 2 is $$zipPacketPath2, and argument 3 is $$additionalReqId and store result in $$rid2
    And I packetsync where argument 1 is $$zipPacketPath2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I delete packet data

  @scenario_92
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident Minor Child walks into registration center wants to get UIN Guardian...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is minor, and argument 2 is true, and argument 3 is Male and store result in $$childPersona
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with rid where argument 1 is $$parentPersona, and argument 2 is $$parentRid
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid2
    And I post mock mv where argument 1 is $$childRid2, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$childRid2
    And I delete packet data

  @scenario_93
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Inji - Resident walks into registration center completes the process with low...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=john, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I delete packet data

  @scenario_94
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Inji - Resident walks into registration center completes the process with hig...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=rob, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I delete packet data

  @scenario_95
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Two resident walks into registration center tries to get UIN with different d...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I wait where argument 1 is 10
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid1, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and password is 99
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePathNew
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePathNew and store result in $$rid2
    And I wait where argument 1 is 10
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I get email by uin where argument 1 is $$uin2 and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_96
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Two resident walks into registration center tries to get UIN with same demogr...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=john, and persona file is $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phone=9513209874, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I wait where argument 1 is 10
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid1, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and password is 99
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=john, and persona file is $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phone=9513209874, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePathNew
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePathNew and store result in $$rid2
    And I wait where argument 1 is 10
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_97
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process with only face ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and finger biometric flag is Male, and password is false/*IRIS_BIOMETRIC_FLAG*/@@false@@true and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_98
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process without biometr...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and generate private key is false, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@false and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_99
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor Child walks into registration center gets UIN with parent RID ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_100
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor Child walks into registration center tries to get UIN with par...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@false and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is REREGISTER, and argument 2 is $$childRid
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_101
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor Child walks into registration center tries to get UIN with par...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_102
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: ResidentMaleAdult - Resident Infant walks into registration center gets UIN with parent RID details
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_103
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: ResidentMaleAdult - Resident Infant walks into registration center tries to get UIN with parent R...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@false and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_104
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor Child walks into registration center completes the process and...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I wait where argument 1 is UIN_WAIT_TIME
    Then I credential request where argument 1 is $$childUin, and argument 2 is $$email and store result in $$requestId
    Then I check credential status where argument 1 is $$requestId
    Then I verify notification where argument 1 is Credential Issuance Status, and argument 2 is $$email
    Then I generate vidwithout otp where argument 1 is Perpetual, and argument 2 is $$childUin and store result in $$vidwithoutotp
    And I wait where argument 1 is 90
    And I demo authentication where argument 1 is name, and argument 2 is $$childUin, and argument 3 is $$childPersona, and argument 4 is $$vidwithoutotp
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$childUin, and argument 3 is $$vidwithoutotp, and argument 4 is $$childPersona
    And I bio authentication where argument 1 is LeftIris, and argument 2 is $$childUin, and argument 3 is $$vidwithoutotp, and argument 4 is $$childPersona
    And I bio authentication where argument 1 is leftRingDevice, and argument 2 is $$childUin, and argument 3 is $$vidwithoutotp, and argument 4 is $$childPersona
    And I otp authentication where argument 1 is uin, and argument 2 is $$childUin, and argument 3 is vid, and argument 4 is $$vidwithoutotp, and argument 5 is $$email
    And I delete packet data

  @scenario_105
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: ResidentMaleAdult - Resident Minor Child walks into registration center completes the process and...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I wait where argument 1 is UIN_WAIT_TIME
    Then I generate vidwithout otp where argument 1 is Perpetual, and argument 2 is $$childUin and store result in $$vidwithoutotp
    Then I wait where argument 1 is UIN_WAIT_TIME
    Then I demo authentication where argument 1 is name, and argument 2 is $$childUin, and argument 3 is $$childPersona, and argument 4 is $$vidwithoutotp
    And I delete packet data

  @scenario_106
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor Child walks into registration center tries get Lost UIN
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I get packet template where packet type is LOST, and argument 2 is $$childPersona and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$ridLost
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is PROCESSED, and argument 2 is $$ridLost
    And I get uinby rid where argument 1 is $$ridLost and store result in $$uin2
    And I get email by uin where argument 1 is $$uin2 and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    Then I check ridstage where argument 1 is $$ridLost, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_107
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center tries get Lost UIN but Biometric did ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is face, and password is iris@@finger, and update attributes is 0, and update attributes is 0, and argument 5 is $$personaFilePath
    And I get packet template where packet type is LOST, and argument 2 is $$personaFilePath and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$ridLost
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is REJECTED, and argument 2 is $$ridLost
    And I delete packet data

  @scenario_108
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident Minor Child walks  into registration center tries get UIN when the p...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I get bio modality hash where check persona presence is -1, and argument 2 is Right IndexFinger, and password is Left LittleFinger, and argument 4 is $$parentPersona and store result in $$modalityHashValue
    And I configure mock abis where check persona presence is -1, and argument 2 is Right IndexFinger, and duplicate flag is true, and argument 4 is Right IndexFinger, and argument 5 is $$parentPersona, and argument 6 is $$modalityHashValue, and default mock delay is -1, and password is Duplicate
    And I packetcreator where packet type is NEW, and argument 2 is $$parentTemplate and store result in $$parentZipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$parentZipPacketPath and store result in $$parentRid
    And I update resident with rid where argument 1 is $$parentPersona, and argument 2 is $$parentRid
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I packetcreator where packet type is NEW, and argument 2 is $$childTemplate and store result in $$childZipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$childZipPacketPath and store result in $$childRid
    And I packetsync where argument 1 is $$parentZipPacketPath
    And I packetsync where argument 1 is $$childZipPacketPath
    And I post mock mv where argument 1 is $$parentRid, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$parentRid
    And I check status where packet status is REJECTED, and argument 2 is $$childRid
    And I delete packet data

  @scenario_109
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident Infant tries to get UIN without Introducer
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is ERROR
    And I delete packet data

  @scenario_110
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male, and argument 4 is rightlittleFinger and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I credential request where argument 1 is $$uin, and argument 2 is $$email and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I verify notification where argument 1 is Credential Issuance Status, and argument 2 is $$email
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I demo authentication where argument 1 is name, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$vid
    And I delete packet data

  @scenario_111
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident Minor walks into registration center completes the process and gets ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I generate vidwithout otp where argument 1 is Perpetual, and argument 2 is $$childUin and store result in $$vidwithoutotp
    And I wait where argument 1 is 90
    And I execute action "ekycData=e2e_ekycDemo(name,$$childUin,$$childPersona,$$vidwithoutotp)"
    And I validate kyc data where argument 1 is photo, and argument 2 is ekycData
    And I delete packet data

  @scenario_112
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident Infant walks into registration center completes the process and gets...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    And I wait where argument 1 is 90
    And I generate vidwithout otp where argument 1 is Perpetual, and argument 2 is $$childUin and store result in $$vidwithoutotp
    And I wait where argument 1 is 90
    And I execute action "ekycData=e2e_ekycDemo(name,$$childUin,$$childPersona,$$vidwithoutotp)"
    And I validate kyc data where argument 1 is photo, and argument 2 is ekycData
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_113
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center gets the lost UIN updates his demo gr...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I get packet template where packet type is LOST, and argument 2 is $$personaFilePath and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$ridLost
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is PROCESSED, and argument 2 is $$ridLost
    And I get uinby rid where argument 1 is $$ridLost and store result in $$uin2
    And I get email by uin where argument 1 is $$uin2 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$ridLost, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    Then I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    Then I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$newTemplate
    Then I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$lostUin
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I generate vidwithout otp where argument 1 is Perpetual, and argument 2 is $$lostUin and store result in $$vidwithoutotp
    And I wait where argument 1 is UIN_WAIT_TIME
    And I get email by uin where argument 1 is $$lostUin and store result in $$email
    And I credential request where argument 1 is $$lostUin, and argument 2 is $$email and store result in $$requestId
    And I check credential status where argument 1 is $$requestId
    And I demo authentication where argument 1 is name, and argument 2 is $$lostUin, and argument 3 is $$personaFilePath, and argument 4 is $$vidwithoutotp
    And I delete packet data

  @scenario_114
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center gets the lost UIN updates his Biometr...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I wait where argument 1 is 90
    And I get packet template where packet type is LOST, and argument 2 is $$personaFilePath and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$ridLost
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is PROCESSED, and argument 2 is $$ridLost
    And I get uinby rid where argument 1 is $$ridLost and store result in $$uin2
    Then I check ridstage where argument 1 is $$ridLost, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I update demo or bio details where bio type is face, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    Then I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    Then I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$newTemplate
    Then I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$lostUin
    And I generate vidwithout otp where argument 1 is Perpetual, and argument 2 is $$lostUin and store result in $$vidwithoutotp
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$lostUin, and argument 3 is $$vidwithoutotp, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_115
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: ResidentMaleAdult - Resident Infant walks into registration center gets the UIN with Preregistrat...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is infant, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with rid where argument 1 is $$parentPersona, and argument 2 is $$parentRid
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I send otp where argument 1 is $$childPersona and store result in $$email
    And I validate otp where argument 1 is $$childPersona, and argument 2 is $$email
    And I pre register where argument 1 is $$childPersona and store result in $$prid
    And I upload documents where argument 1 is $$childPersona, and argument 2 is $$prid
    And I update pre reg status where status code is 0, and argument 2 is $$prid, and argument 3 is valid
    And I book appointment where holiday booking flag is false, and argument 2 is $$prid, and slot number is 1
    And I generate and upload packet where argument 1 is $$prid, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    And I generate vidwithout otp where argument 1 is Perpetual, and argument 2 is $$childUin and store result in $$vidwithoutotp
    And I wait where argument 1 is UIN_WAIT_TIME
    And I demo authentication where argument 1 is name, and argument 2 is $$childUin, and argument 3 is $$childPersona, and argument 4 is $$vidwithoutotp
    Then I check ridstage where argument 1 is $$parentRid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_116
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center gets the UIN but during packet genera...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath, and biometric quality score is 80, and generate valid cbeff flag is false and store result in $$templatePath
    And I generate and upload packet skipping prereg with invalid cbeff where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_117
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center tries to update the UIN with invalid UIN
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is 1234
    And I update demo or bio details where bio type is face, and password is iris@@finger, and update attributes is 0, and update attributes is 0, and argument 5 is $$personaFilePath
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is ERROR
    And I delete packet data

  @scenario_118
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident Infant walks into registration center gets the UIN with finger and e...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left IndexFinger, and password is Finger:Right IndexFinger@@Iris:Left
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_119
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Minor_Exc
  Scenario: ResidentMaleAdult - Resident Minor walks into registration center gets the UIN with parent and ch...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I update bio exception in persona where argument 1 is $$parentPersona, and argument 2 is Finger:Left IndexFinger, and password is Finger:Right IndexFinger@@Iris:Left
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I check tags where argument 1 is $$parentRid
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update bio exception in persona where argument 1 is $$childPersona, and argument 2 is Finger:Left IndexFinger, and password is Finger:Right IndexFinger@@Iris:Left
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_120
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Minor_Exc
  Scenario: ResidentMaleAdult - Resident Minor walks into registration center gets the UIN with few exceptions
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update bio exception in persona where argument 1 is $$childPersona, and argument 2 is Finger:Left IndexFinger, and password is Finger:Right IndexFinger@@Iris:Left
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I check tags where argument 1 is $$childRid
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_121
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center gets the UIN  with All finger and eye...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left Thumb, and password is Finger:Left IndexFinger@@Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@Finger:Right LittleFinger@@Iris:Left@@Iris:Right
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I check tags where argument 1 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_122
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor walks into registration center gets the UIN with all finger an...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update bio exception in persona where argument 1 is $$childPersona, and argument 2 is Finger:Left Thumb, and password is Finger:Left IndexFinger@@Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@Finger:Right LittleFinger@@Iris:Left@@Iris:Right
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I check tags where argument 1 is $$childRid
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_123
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant_Exc
  Scenario: ResidentMaleAdult - Resident Infant walks into registration center gets the UIN with introducer e...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I update bio exception in persona where argument 1 is $$parentPersona, and argument 2 is Finger:Left IndexFinger, and password is Finger:Right IndexFinger@@Iris:Left
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I check tags where argument 1 is $$parentRid
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_124
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: ResidentMaleAdult - Resident walks into registration center updates biometrics with all exception...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left Thumb, and password is Finger:Left IndexFinger@@Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@Finger:Right LittleFinger@@Iris:Left@@Iris:Right
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check tags where argument 1 is $$rid2
    And I delete packet data

  @scenario_125
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: ResidentMaleAdult - Resident walks into registration center updates biometrics with finger and ey...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left IndexFinger, and password is Finger:Right IndexFinger@@Iris:Left
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I check tags where argument 1 is $$rid2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_126
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor walks into registration center updates demo details and biomet...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$childPersona
    And I update bio exception in persona where argument 1 is $$childPersona, and argument 2 is Finger:Left Thumb, and password is Finger:Left IndexFinger@@Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@Finger:Right LittleFinger@@Iris:Left@@Iris:Right
    And I update resident with uin where argument 1 is $$childPersona, and argument 2 is $$childUin
    And I get packet template where packet type is UPDATE, and argument 2 is $$childPersona and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I check tags where argument 1 is $$rid2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_127
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor walks into registration center updates demo and biometrics wit...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$childPersona
    And I update bio exception in persona where argument 1 is $$childPersona, and argument 2 is Finger:Left IndexFinger, and password is Finger:Right IndexFinger@@Iris:Left
    And I update resident with uin where argument 1 is $$childPersona, and argument 2 is $$childUin
    And I get packet template where packet type is UPDATE, and argument 2 is $$childPersona and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I check tags where argument 1 is $$rid2
    And I verify notification where argument 1 is updated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_128
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: ResidentMaleAdult - Resident walks into registration center updates demo and biometrics with fing...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left IndexFinger, and password is Finger:Right IndexFinger@@Iris:Left
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I check tags where argument 1 is $$rid2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I wait where argument 1 is UIN_WAIT_TIME
    Then I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    Then I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I demo authentication where argument 1 is name, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$vid
    And I bio authentication where argument 1 is rightThumbDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_129
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: ResidentMaleAdult - Resident walks into registration center updates demo and biometrics with all ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left Thumb, and password is Finger:Left IndexFinger@@Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@Finger:Right LittleFinger@@Iris:Left@@Iris:Right
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I check tags where argument 1 is $$rid2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I wait where argument 1 is UIN_WAIT_TIME
    Then I credential request where argument 1 is $$uin2, and argument 2 is $$email and store result in $$requestId
    Then I check credential status where argument 1 is $$requestId
    Then I verify notification where argument 1 is Credential Issuance Status, and argument 2 is $$email
    Then I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    Then I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I demo authentication where argument 1 is name, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$vid
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_130
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is addressLine1=bnglr, and password is phoneNumber=3938333736, and argument 5 is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is LOST, and argument 2 is $$personaFilePath and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$ridLost
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is PROCESSED, and argument 2 is $$ridLost
    And I get uinby rid where argument 1 is $$ridLost and store result in $$uin2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$ridLost, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_131
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_NA
  Scenario: ResidentFemaleAdult - Resident walks into registration center tries to register with predefined blo...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Female and store result in $$personaFilePath
    And I get blocklisted word where argument 1 is CREATE, and blocklist type is dslautomation and store result in $$blocklistedWord
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name=$$blocklistedWord, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_132
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New_Exception
  Scenario: ResidentMaleAdult - A differently abled resident with exception in left eye walks into registrati...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Iris:Left
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_133
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor Child with age less than 1 year walks into registration center...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=2023/08/24, and persona file is $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_134
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: ResidentMaleAdult - Resident Infant with age less than 1 year walks into registration center gets...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=2023/08/24, and persona file is $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_135
  @Negative_Test
  @persona_NonResidentMaleAdult
  @group_Adult_New
  Scenario: NonResidentMaleAdult - NonResident adult whose phone number is 11 digts walk-ins to registration cen...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phoneNumber=39383337361, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_136
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: ResidentMaleAdult - Resident adult without phone number and email walks into registration center ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is phoneNumber=, and password is email=, and argument 5 is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_137
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New_Exception
  Scenario: ResidentMaleAdult - A differently abled resident with exception in left index finger walks into r...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left IndexFinger
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is iris, and password is finger, and update attributes is 0, and update attributes is 0, and argument 5 is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I check for bdbabsence where argument 1 is $$uin2, and argument 2 is FINGER_Left IndexFinger
    And I delete packet data

  @scenario_138
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: ResidentMaleAdult - Resident walks into registration center and gets UIN. Later updates exception...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left Thumb
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I check tags where argument 1 is $$rid2
    And I check for bdbpresence where argument 1 is $$uin2, and argument 2 is FINGER_Left RingFinger, and password is FINGER_Right LittleFinger@@FACE@@FINGER_Left LittleFinger@@IRIS_Right@@FINGER_Left MiddleFinger@@FINGER_Left IndexFinger@@FINGER_Right IndexFinger@@IRIS_Left@@FINGER_Right RingFinger@@FINGER_Right MiddleFinger@@FINGER_Right Thumb, and expect exception is false
    And I delete packet data

  @scenario_139
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New_Exception
  Scenario: ResidentMaleAdult - A differently abled resident walks into registration center completes the pro...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Iris:Left, and password is Iris:Right
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is iris, and password is finger, and update attributes is 0, and update attributes is 0, and argument 5 is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I check for bdbabsence where argument 1 is $$uin2, and argument 2 is IRIS_Left, and password is IRIS_Right
    And I delete packet data

  @scenario_140
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check for bdbpresence where argument 1 is $$rid, and argument 2 is FINGER_Left RingFinger, and password is FINGER_Right LittleFinger@@FACE@@FINGER_Left LittleFinger@@IRIS_Right@@FINGER_Left MiddleFinger@@FINGER_Left IndexFinger@@FINGER_Right IndexFinger@@IRIS_Left@@FINGER_Right RingFinger@@FINGER_Left Thumb@@FINGER_Right MiddleFinger@@FINGER_Right Thumb, and argument 4 is false/EXCEPTION_FLAG/
    And I delete packet data

  @scenario_141
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process by providing th...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and argument 3 is false/GENERATE_PRIVATE_KEY/, and put scenario details in context is null, and consent flag is yes
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_142
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process by not providin...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and consent flag is no
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    And I delete packet data

  @scenario_143
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=08/24/2023, and persona file is $$personaFilePath
    Then I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$newTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid2
    And I check status where packet status is REREGISTER, and argument 2 is $$rid2
    And I delete packet data

  @scenario_144
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Machine got unmapped from the center before generating the offline packet
    Given I user where user action is ADD_User, and user index is 6, and password is Techno@123 and store result in $$user6
    And I center where argument 1 is CREATE, and argument 2 is $$user6, and center index is 6, and center active flag is T and store result in $$center6
    And I machine where argument 1 is CREATE, and argument 2 is $$center6, and center index is 6 and store result in $$details6
    And I user where user action is DELETE_CENTERMAPPING, and user index is 6, and password is Techno@123, and password or details is $$details6 and store result in $$details6
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details6 and store result in $$details6
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details6
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details6
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details6, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details6, and center index is 6
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details6, and user center mapping flag is T
    And I write pre req where argument 1 is $$details6, and pre requisite data index is 6
    And I read pre req where pre requisite data index is 6 and store result in $$details6
    And I set context where argument 1 is env_context, and argument 2 is $$details6, and generate private key is true
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I machine where argument 1 is REMOVE_CENTER, and argument 2 is $$center6, and center index is 6
    And I user where user action is DELETE_CENTERMAPPING, and user index is 6, and password is Techno@123, and password or details is $$details6 and store result in $$details6
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$rid, and argument 2 is CMD_VALIDATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_145
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - An offline packet is generated and machine got unmapped from the center befor...
    Given I user where user action is ADD_User, and user index is 7, and password is Techno@123 and store result in $$user7
    And I center where argument 1 is CREATE, and argument 2 is $$user7, and center index is 7, and center active flag is T and store result in $$center7
    And I machine where argument 1 is CREATE, and argument 2 is $$center7, and center index is 7 and store result in $$details7
    And I user where user action is DELETE_CENTERMAPPING, and user index is 7, and password is Techno@123, and password or details is $$details7 and store result in $$details7
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details7 and store result in $$details7
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details7
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details7
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details7, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details7, and center index is 7
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details7, and user center mapping flag is T
    And I write pre req where argument 1 is $$details7, and pre requisite data index is 7
    And I read pre req where pre requisite data index is 7 and store result in $$details7
    And I set context where argument 1 is env_context, and argument 2 is $$details7, and generate private key is true
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I machine where argument 1 is REMOVE_CENTER, and argument 2 is $$center7, and center index is 7
    And I user where user action is DELETE_CENTERMAPPING, and user index is 7, and password is Techno@123, and password or details is $$details7 and store result in $$details7
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_146
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_New_Exception
  Scenario: ResidentMaleAdult - A differently abled resident with exception in left and right index finger wa...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and consent flag is null, and switch case for supervisor name is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left IndexFinger, and password is Finger:Right IndexFinger
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_147
  @Negative_Test
  @persona_SeniorNonResidentMale
  @group_Senior_New
  Scenario: SeniorNonResidentMale - Senior Non Resident walks into registration center completes the process and ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where age category is senior, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_148
  @Positive_Test
  @persona_SeniorResidentMale
  @group_Senior_New
  Scenario: SeniorResidentMale - Senior Resident walks into registration center completes the process and gets...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where age category is senior, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_149
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: ResidentMaleAdult - Resident walks into registration center provides future date as DOB and tries...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=08/24/2026, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_150
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: ResidentMaleAdult - Resident Infant walks into registration center gets UIN with parent RID detai...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid1
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid1
    And I get uinby rid where argument 1 is $$childRid1 and store result in $$childUin1
    And I get email by uin where argument 1 is $$childUin1 and store result in $$email2
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid1, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$childRid1, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    Then I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid2
    Then I verify notification where argument 1 is UIN Generated, and argument 2 is $$email2
    Then I check ridstage where argument 1 is $$childRid2, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$childRid2, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_151
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into registration center completes the process but while the p...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and consent flag is null, and switch case for supervisor name is null, and argument 7 is invalidEncryptedHash
    And I get ping health where argument 1 is targetenv
    And I get resident data where age category is adult, and guardian flag is false, and gender is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_152
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into registration center and tries to complete the process. Bu...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and consent flag is null, and switch case for supervisor name is null, and upload packet invalid encrypted hash flag is null, and argument 8 is invalidCheckSum
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath, and expected to pass flag is false
    And I delete packet data

  @scenario_153
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: ResidentMaleAdult - Resident walks into registration center gets UIN card . Another resident trie...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left Thumb, and password is Finger:Left IndexFinger@@Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@Finger:Right LittleFinger@@Iris:Left@@Iris:Right
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath1
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath1 and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I check tags where argument 1 is $$rid1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid1, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_154
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into center to get UIN but supervisor rejects the packet . Sam...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I rid sync rejected where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is REJECTED
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$newTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid1, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_155
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into center to get Uin but packet is created with invalid hash...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I upload packet with invalid hash where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$newTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid1, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_156
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Packet is created and uploaded with inactive center
    Given I user where user action is ADD_User, and user index is 156, and password is Techno@123 and store result in $$user156
    And I center where argument 1 is CREATE, and argument 2 is $$user156, and center index is 156, and center active flag is T and store result in $$center156
    And I machine where argument 1 is CREATE, and argument 2 is $$center156, and center index is 156 and store result in $$details156
    And I user where user action is DELETE_CENTERMAPPING, and user index is 156, and password is Techno@123, and password or details is $$details156 and store result in $$details156
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details156 and store result in $$details156
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details156
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details156
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details156, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details156, and center index is 156
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details156, and user center mapping flag is T
    And I write pre req where argument 1 is $$details156, and pre requisite data index is 156
    And I read pre req where append key to read prerequisite data is 156 and store result in $$details156
    And I set context where argument 1 is env_context, and argument 2 is $$details156, and generate private key is true
    And I user where user action is DELETE_CENTERMAPPING, and user index is 156, and password is Techno@123, and password or details is $$details156 and store result in $$details156
    And I machine where argument 1 is REMOVE_CENTER, and argument 2 is $$details156
    And I center where argument 1 is ACTIVE_FLAG, and argument 2 is $$details156, and center index is 156, and center active flag is F
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    Then I machine where argument 1 is DCOM, and argument 2 is $$details156
    Then I center where argument 1 is DCOM, and argument 2 is $$details156, and center index is 156
    And I delete packet data

  @scenario_157
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walk into registration center on a holiday completes the process and...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and argument 4 is invalidCreationDate=-1d
    And I get ping health where argument 1 is targetenv
    And I holiday declaration where argument 1 is holidayDate=-1d and store result in $$holidayId
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I delete holiday where argument 1 is $$holidayId
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is CMD_VALIDATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_158
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Packet is created and uploaded with inactive machine
    Given I user where user action is ADD_User, and user index is 8, and password is Techno@123 and store result in $$user8
    And I center where argument 1 is CREATE, and argument 2 is $$user8, and center index is 8, and center active flag is T and store result in $$center8
    And I machine where argument 1 is CREATE, and argument 2 is $$center8, and center index is 8 and store result in $$details8
    And I user where user action is DELETE_CENTERMAPPING, and user index is 8, and password is Techno@123, and password or details is $$details8 and store result in $$details8
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details8 and store result in $$details8
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details8
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details8
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details8, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details8, and center index is 8
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details8, and user center mapping flag is T
    And I write pre req where argument 1 is $$details8, and pre requisite data index is 8
    And I read pre req where append key to read prerequisite data is 8 and store result in $$details8
    And I set context where argument 1 is env_context, and argument 2 is $$details8, and generate private key is true
    And I machine where argument 1 is ACTIVE_FLAG, and argument 2 is $$details8, and machine index is 8, and machine active flag is F
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    Then I machine where argument 1 is DCOM, and argument 2 is $$details8
    Then I user where user action is DELETE_CENTERMAPPING, and user index is 8, and password is Techno@123, and password or details is $$details8 and store result in $$details8
    Then I center where argument 1 is DCOM, and argument 2 is $$details8, and center index is 8
    And I delete packet data

  @scenario_159
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into registration center completes the process tries to get ui...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and invalid schema version is invalidIdSchema
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is REPROCESS
    And I delete packet data

  @scenario_160
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I get phone by uin where argument 1 is $$uin and store result in $$phone
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$phone and store result in $$vid
    And I wait where argument 1 is 90
    And I ekyc bio where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_161
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Male, and argument 4 is rightlittleFinger and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I get phone by uin where argument 1 is $$uin and store result in $$phone
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$phone and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is rightThumbDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I oidc client where  and store result in $$clientId
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId1 and store result in $$transactionId1
    And I oauth details request where argument 1 is $$clientId, and argument 2 is transactionId2 and store result in $$transactionId2
    And I bio esignet authentication where argument 1 is rightThumbDevice, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$transactionId1, and argument 5 is $$vid, and argument 6 is $$transactionId2
    And I user info where argument 1 is $$transactionId, and argument 2 is $$clientId
    And I delete packet data

  @scenario_162
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Packet is created and uploaded with invalid machine
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I update machine in prereq data where argument 1 is $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and add commment is 1, and password is 2, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath, and argument 3 is invalidMachine
    And I delete packet data

  @scenario_163
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Upload the resident packet again which has been uplodaded and processed already 
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I wait where argument 1 is PACKET_UPLOAD_WAIT_TIME
    Then I packetsync where argument 1 is $$zipPacketPath
    Then I check status where packet status is REREGISTER, and argument 2 is $$rid
    And I delete packet data

  @scenario_164
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Skip biometric classification for resident if individual biometric parameter ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and skip bio classification flag is skipBiometricClassification
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I wait where argument 1 is PACKET_UPLOAD_WAIT_TIME
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_165
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Applicant documents are missing in the packet 
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and skip applicant documents flag is skipApplicantDocuments
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_166
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A non registered resident walks into registration center without UIN and trie...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is LOST, and argument 2 is $$personaFilePath and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$lostRid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$lostRid, and argument 2 is BIOGRAPHIC_VERIFICATION, and argument 3 is FAILED
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_167
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male, and argument 4 is rightlittleFinger and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I demo authentication where argument 1 is age, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$vid, and age decrement flag is ageDecrease
    And I delete packet data

  @scenario_168
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male, and argument 4 is rightlittleFinger and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I demo authentication where argument 1 is age, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$vid
    And I delete packet data

  @scenario_169
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident attempts to obtain UIN but Packet Creation Date is Null 
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and invalid packet creation date is invalidCreationDate
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is CMD_VALIDATION, and argument 3 is ERROR
    And I delete packet data

  @scenario_170
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - ResidentA and ResidentB got their UINs and  ResidentB is trying to update Res...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath1
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath1 and store result in $$templatePath1
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath1, and argument 2 is $$templatePath1 and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and password is 99
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath2
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath2 and store result in $$templatePath2
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath2, and argument 2 is $$templatePath2 and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I get email by uin where argument 1 is $$uin2 and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I update demo or bio details where bio type is face, and password is finger@@iris, and update attributes is 0, and update attributes is 0, and persona to update is $$personaFilePath1, and persona to update with is $$personaFilePath2
    And I update resident with uin where argument 1 is $$personaFilePath1, and argument 2 is $$uin2
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath1 and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath1, and argument 2 is $$updateTemplate and store result in $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I delete packet data

  @scenario_171
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult -  ResidentA and ResidentB got their UINs and  ResidentB is trying to update Re...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath1
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath1 and store result in $$templatePath1
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath1, and argument 2 is $$templatePath1 and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and password is 99
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath2
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath2 and store result in $$templatePath2
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath2, and argument 2 is $$templatePath2 and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I get email by uin where argument 1 is $$uin2 and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona to update is $$personaFilePath1, and persona to update with is $$personaFilePath2
    And I update resident with uin where argument 1 is $$personaFilePath1, and argument 2 is $$uin2
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath1 and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath1, and argument 2 is $$updateTemplate and store result in $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I delete packet data

  @scenario_172
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Minor Resident A and Adult Resident B got their UINs and  Resident B is tryin...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and password is 99
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I update demo or bio details where bio type is face, and password is finger@@iris, and update attributes is 0, and update attributes is 0, and persona to update is $$childPersona, and persona to update with is $$personaFilePath
    And I update resident with uin where argument 1 is $$childPersona, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$childPersona and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$updateTemplate and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I delete packet data

  @scenario_173
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: ResidentMaleAdult -  Infant Resident A and Adult Resident B got their UINs and  Resident B is try...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and password is 99
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I update demo or bio details where bio type is face, and miss fields is 0, and update attributes is 0, and persona to update is $$childPersona, and persona to update with is $$personaFilePath
    And I update resident with uin where argument 1 is $$childPersona, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$childPersona and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$updateTemplate and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I delete packet data

  @scenario_174
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into registration center completes the process tries to get UI...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and invalid packet creation date is invalidOfficerID
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_175
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Generate packet with size greater than 2MB
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is updateLargeDocInPersona, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I delete packet data

  @scenario_176
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Generate UIN1 by creating RID1 with Iris exceptions in Profile1 then update U...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Iris:Left, and password is Iris:Right
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I check tags where argument 1 is $$rid1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is iris, and password is finger, and update attributes is 0, and update attributes is 0, and persona to update is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin1
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I delete packet data

  @scenario_177
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Generate UIN1 by creating RID1 with all finger exceptions in Profile1 then up...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left Thumb, and password is Finger:Left IndexFinger@@Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@Finger:Right LittleFinger
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I check tags where argument 1 is $$rid1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is finger, and password is iris, and update attributes is 0, and update attributes is 0, and persona to update is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin1
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I check tags where argument 1 is $$rid2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I delete packet data

  @scenario_178
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into registration center and Generates UIN1 by creating RID1 w...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I get bio modality hash where check persona presence is -1, and argument 2 is Right IndexFinger, and password is Left LittleFinger, and argument 4 is $$personaFilePath and store result in $$modalityHashValue
    And I configure mock abis where check persona presence is -1, and argument 2 is Right IndexFinger, and duplicate flag is false, and argument 4 is Right IndexFinger, and argument 5 is $$personaFilePath, and argument 6 is $$modalityHashValue, and default mock delay is -1, and password is Duplicate
    And I update demo or bio details where bio type is face, and password is finger@@iris, and update attributes is 0, and update attributes is 0, and persona to update is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin1
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is FAILED, and argument 2 is $$rid2
    And I delete packet data

  @scenario_179
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - ResidentA get uin and ResidentB marking FPs and Iris as exception got their U...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath1
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath1 and store result in $$templatePath1
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath1, and argument 2 is $$templatePath1 and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and password is 99
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath2
    And I update bio exception in persona where argument 1 is $$personaFilePath2, and argument 2 is Finger:Left IndexFinger, and password is Finger:Right IndexFinger@@Iris:Left
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath2 and store result in $$templatePath2
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath2, and argument 2 is $$templatePath2 and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I check tags where argument 1 is $$rid2
    And I get email by uin where argument 1 is $$uin2 and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona to update is $$personaFilePath1, and persona to update with is $$personaFilePath2
    And I update resident with uin where argument 1 is $$personaFilePath1, and argument 2 is $$uin2
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath1 and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath1, and argument 2 is $$updateTemplate and store result in $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I delete packet data

  @scenario_180
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident goes to reg-center and gets UIN same person goes to center and updat...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin1
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin2
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid3, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check updated uin where argument 1 is $$uin1, and argument 2 is $$uin3
    And I delete packet data

  @scenario_181
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident goes to reg-center and gets UIN same person goes to center and updat...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin1
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin2
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath2
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath2 and store result in $$templatePath2
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath2, and argument 2 is $$templatePath2 and store result in $$rid4
    And I check status where packet status is PROCESSED, and argument 2 is $$rid4
    And I get uinby rid where argument 1 is $$rid4 and store result in $$uin4
    And I get email by uin where argument 1 is $$uin4 and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin4
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid5
    And I check status where packet status is PROCESSED, and argument 2 is $$rid5
    And I get uinby rid where argument 1 is $$rid5 and store result in $$uin5
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid5, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_182
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into reg-center and generates UIN1 same person tried to get UI...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath2
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath2 and store result in $$rid2
    And I post mock mv where argument 1 is $$rid2, and argument 2 is PROCESSED
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I get email by uin where argument 1 is $$uin2 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$updateTemplate3
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate3 and store result in $$rid3
    And I post mock mv where argument 1 is $$rid3, and argument 2 is PROCESSED
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I get email by uin where argument 1 is $$uin3 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I get bio modality hash where check persona presence is -1, and argument 2 is Right IndexFinger, and password is Left LittleFinger, and argument 4 is $$personaFilePath and store result in $$modalityHashValue
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin1
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate4
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate4 and store result in $$rid4
    And I check status where packet status is PROCESSED, and argument 2 is $$rid4
    And I get uinby rid where argument 1 is $$rid4 and store result in $$uin4
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid4, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is SUCCESS
    Then I check updated uin where argument 1 is $$uin1, and argument 2 is $$uin4
    And I delete packet data

  @scenario_183
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into reg-center and generated UIN1 same person tried to get UI...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid2
    And I post mock mv where argument 1 is $$rid2, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$rid2
    And I update demo or bio details where bio type is iris, and password is finger, and update attributes is 0, and persona file is 0, and argument 5 is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin1
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid3, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check updated uin where argument 1 is $$uin1, and argument 2 is $$uin3
    And I delete packet data

  @scenario_184
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A differently abled resident walks into registration center completes the pro...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Iris:Left, and password is Iris:Right
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I bio authentication where argument 1 is rightThumbDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I bio authentication where argument 1 is LeftIris, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath, and argument 5 is ERROR
    And I delete packet data

  @scenario_185
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A differently abled resident walks into registration center completes the pro...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left Thumb, and password is Finger:Left IndexFinger@@Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@Finger:Right LittleFinger
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I bio authentication where argument 1 is LeftIris, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I bio authentication where argument 1 is rightThumbDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath, and argument 5 is ERROR
    And I delete packet data

  @scenario_186
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A differently abled resident walks into registration center completes the pro...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left Thumb, and password is Finger:Left IndexFinger@@Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger@@Finger:Right Thumb@@Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@Finger:Right LittleFinger@@Iris:Left@@Iris:Right
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is rightThumbDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath, and argument 5 is ERROR
    And I bio authentication where argument 1 is LeftIris, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath, and argument 5 is ERROR
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_187
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A differently abled resident walks into registration center completes the pro...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is rightThumbDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I bio authentication where argument 1 is LeftIris, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_188
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A differently abled resident walks into registration center completes the pro...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Left Thumb, and password is Finger:Right Thumb
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I check tags where argument 1 is $$rid
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is rightThumbDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath, and argument 5 is ERROR
    And I bio authentication where argument 1 is LeftIris, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I bio authentication where argument 1 is leftLittleDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_189
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - User creates UIN through reg center then deactivates it in IDRepo then create...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check tags where argument 1 is $$rid
    Then I get email by uin where argument 1 is $$uin and store result in $$email
    Then I deactivate uin where argument 1 is $$uin, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is REREGISTER, and argument 2 is $$rid2
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is VALIDATE_PACKET, and argument 3 is ERROR
    And I delete packet data

  @scenario_190
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - User creates UIN through reg center then deactivates it in IDRepo then reproc...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check tags where argument 1 is $$rid
    Then I get email by uin where argument 1 is $$uin and store result in $$email
    Then I deactivate uin where argument 1 is $$uin, and argument 2 is $$email
    Then I reprocess packet where argument 1 is $$rid
    Then I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is UIN_GENERATOR, and argument 3 is ERROR
    And I delete packet data

  @scenario_191
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident created Infant packet and got UIN and later updated the UIN to a Min...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=minor, and persona file is $$childPersona
    Then I update demo or bio details where bio type is face, and password is iris@@finger, and update attributes is 0, and update attributes is 0, and argument 5 is $$childPersona
    Then I update resident with uin where argument 1 is $$childPersona, and argument 2 is $$childUin
    Then I get packet template where packet type is UPDATE, and argument 2 is $$childPersona and store result in $$updateTemplate
    Then I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$minorUin
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I generate vidwithout otp where argument 1 is Perpetual, and argument 2 is $$minorUin and store result in $$vid
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$minorUin, and argument 3 is $$vid, and argument 4 is $$childPersona
    And I delete packet data

  @scenario_192
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident created Infant packet and got UIN and later updated the UIN to a adu...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=adult, and persona file is $$childPersona
    Then I update demo or bio details where bio type is face, and password is iris@@finger, and update attributes is 0, and update attributes is 0, and argument 5 is $$childPersona
    Then I update resident with uin where argument 1 is $$childPersona, and argument 2 is $$childUin
    Then I get packet template where packet type is UPDATE, and argument 2 is $$childPersona and store result in $$updateTemplate
    Then I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I generate vidwithout otp where argument 1 is Perpetual, and argument 2 is $$uin and store result in $$vid
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$childPersona
    And I delete packet data

  @scenario_193
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Minor_New
  Scenario: ResidentMaleAdult - Resident Minor walks into registration center gets UIN with Guardian RID deta...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with rid where argument 1 is $$parentPersona, and argument 2 is $$parentRid
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$parentRid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=adult, and persona file is $$childPersona
    Then I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is $$childPersona
    Then I update resident with uin where argument 1 is $$childPersona, and argument 2 is $$childUin
    Then I get packet template where packet type is UPDATE, and argument 2 is $$childPersona and store result in $$updateTemplate
    Then I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I generate vidwithout otp where argument 1 is Perpetual, and argument 2 is $$uin and store result in $$vid
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$childPersona
    And I delete packet data

  @scenario_194
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: ResidentFemaleAdult - Infant external packet creation and process with out introducerInfoToken
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details4, and generate private key is false, and packet type is EXTERNAL
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is infant, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I create and upload external packet where source is CRVS1, and packet type is CRVS_NEW, and argument 3 is $$personaFilePath and store result in $$rid
    And I sync external packet where argument 1 is $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_195
  @Postive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I get email by uin where argument 1 is $$uin and store result in $$email1
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email1 and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email1
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$vid
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$Rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$Rid2
    And I demo authentication where argument 1 is name, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$vid
    And I delete packet data

  @scenario_196
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walk-ins to registration center create a packet empty signature
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and argument 4 is 0, and argument 5 is emptySignature
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_197
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into registration center completes the process with invalid si...
    Given I get ping health where argument 1 is packetcreator
    And I user where user action is ADD_User, and user index is 5, and password is Techno@123 and store result in $$user5
    And I center where argument 1 is CREATE, and argument 2 is $$user5, and center index is 5, and center active flag is T and store result in $$center5
    And I machine where argument 1 is CREATE, and argument 2 is $$center5, and center index is 5 and store result in $$details5
    And I user where user action is DELETE_CENTERMAPPING, and user index is 5, and password is Techno@123, and password or details is $$details5 and store result in $$details5
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details5 and store result in $$details5
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details5
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details5
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details5, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details5, and center index is 5
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details5, and user center mapping flag is T
    And I write pre req where argument 1 is $$details5, and pre requisite data index is 5
    And I read pre req where pre requisite data index is 5 and store result in $$details5
    And I set context where argument 1 is env_context, and argument 2 is $$details5, and generate private key is true, and argument 4 is 0, and argument 5 is invalidSignature
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_198
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: ResidentFemaleAdult - Infant external packet creation and process with introducerInfoToken
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details4, and generate private key is false, and packet type is EXTERNAL
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is infant, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I create and upload external packet where source is CRVS1, and packet type is CRVS_NEW, and argument 3 is $$personaFilePath, and introducerinfotoken is true and store result in $$rid
    And I sync external packet where argument 1 is $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_199
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check tags where argument 1 is $$rid
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details4, and generate private key is false, and packet type is EXTERNAL
    And I create and upload external packet where source is CRVS1, and packet type is CRVS_DEATH, and argument 3 is $$personaFilePath, and infotoken is true, and argument 5 is $$uin and store result in $$rid2
    And I sync external packet where argument 1 is $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_200
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: ResidentFemaleAdult - Adult external packet creation and process with out introducerInfoToken
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details4, and generate private key is false, and packet type is EXTERNAL
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Male and store result in $$personaFilePath
    And I create and upload external packet where source is CRVS1, and packet type is CRVS_NEW, and argument 3 is $$personaFilePath and store result in $$rid
    And I sync external packet where argument 1 is $$rid
    And I check status where packet status is FAILED, and argument 2 is $$rid
    And I delete packet data

  @scenario_201
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: ResidentFemaleAdult - Minor external packet creation and process with out introducerInfoToken
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details4, and generate private key is false, and packet type is EXTERNAL
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is minor, and argument 2 is false, and argument 3 is Male and store result in $$personaFilePath
    And I create and upload external packet where source is CRVS1, and packet type is CRVS_NEW, and argument 3 is $$personaFilePath and store result in $$rid
    And I sync external packet where argument 1 is $$rid
    And I check status where packet status is FAILED, and argument 2 is $$rid
    And I delete packet data

  @scenario_202
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: ResidentFemaleAdult - Creation and processing of an external infant packet without an introducerInf...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details4, and generate private key is false, and packet type is EXTERNAL
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is infant, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I create and upload external packet where source is CRVS11@invalid, and packet type is CRVS_NEW1, and argument 3 is $$personaFilePath and store result in $$rid
    And I delete packet data

  @scenario_203
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: ResidentFemaleAdult - Infant external packet creation with DOB as future dates and process with out...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and packet type is EXTERNAL
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is infant, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=04/24/2026, and persona file is $$personaFilePath
    And I create and upload external packet where source is CRVS1, and packet type is CRVS_NEW, and argument 3 is $$personaFilePath and store result in $$rid
    And I sync external packet where argument 1 is $$rid
    And I check status where packet status is FAILED, and argument 2 is $$rid
    And I delete packet data

  @scenario_204
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_External_New
  Scenario: ResidentFemaleAdult - Infant external packet creation without necessary data and process with out i...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details4, and generate private key is false, and packet type is EXTERNAL
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is infant, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=24/04/2026, and persona file is $$personaFilePath
    And I create and upload external packet where source is CRVS1, and packet type is CRVS_NEW, and argument 3 is $$personaFilePath and store result in $$rid
    And I sync external packet where argument 1 is $$rid
    And I check status where packet status is FAILED, and argument 2 is $$rid
    And I delete packet data

  @scenario_205
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into registration center completes the process with inactive u...
    Given I get ping health where argument 1 is packetcreator
    And I user where user action is ADD_User, and user index is 5, and password is Techno@123 and store result in $$user5
    And I center where argument 1 is CREATE, and argument 2 is $$user5, and center index is 5, and center active flag is T and store result in $$center5
    And I machine where argument 1 is CREATE, and argument 2 is $$center5, and center index is 5 and store result in $$details5
    And I user where user action is DELETE_CENTERMAPPING, and user index is 5, and password is Techno@123, and password or details is $$details5 and store result in $$details5
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details5 and store result in $$details5
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details5
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details5
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details5, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details5, and center index is 5
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details5, and user center mapping flag is T
    And I user where user action is DELETE_CENTERMAPPING, and user index is 5, and password is Techno@123, and password or details is $$details5 and store result in $$details5
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details5
    And I write pre req where argument 1 is $$details5, and pre requisite data index is 5
    And I read pre req where pre requisite data index is 5 and store result in $$details5
    And I set context where argument 1 is env_context, and argument 2 is $$details5, and generate private key is true
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is FAILED, and argument 2 is $$rid
    And I delete packet data

  @scenario_206
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center with his child and completes the proc...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$parentZipPacketPath
    And I get resident data where persona type is minor, and guardian flag is true, and argument 3 is Male and store result in $$childPersona
    And I update resident with rid where argument 1 is $$parentPersona, and argument 2 is $$parentRid
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I packetcreator where packet type is NEW, and argument 2 is $$childTemplate and store result in $$childZipPacketPath
    And I rid sync rejected where packet type is NEW, and argument 2 is $$parentZipPacketPath and store result in $$parentRid
    And I ridsync where packet type is NEW, and argument 2 is $$childZipPacketPath and store result in $$childRid
    And I packetsync where argument 1 is $$parentZipPacketPath
    And I packetsync where argument 1 is $$childZipPacketPath
    And I check status where packet status is REREGISTER, and argument 2 is $$childRid
    And I check status where packet status is REREGISTER, and argument 2 is $$parentRid
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is ERROR
    Then I check ridstage where argument 1 is $$parentRid, and argument 2 is VALIDATE_PACKET, and argument 3 is REJECTED
    And I delete packet data

  @scenario_#207
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I update identity with array handles where argument 1 is $$uin
    And I get handles by uin where argument 1 is $$uin and store result in $$handles
    And I demo authentication where argument 1 is name, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$vid, and argument 5 is 0, and argument 6 is $$handles
    And I delete packet data

  @scenario_208
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Adult resident walks into registration center completes the process and gets ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check tags where argument 1 is $$rid
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details4, and generate private key is false, and packet type is EXTERNAL
    And I create and upload external packet where source is CRVS1, and packet type is CRVS_DEATH, and argument 3 is $$personaFilePath, and argument 4 is $$uin and store result in $$rid2
    And I sync external packet where argument 1 is $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_209
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Infant external packet creation and process with introducerInfoToken and gets...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details4, and generate private key is false, and packet type is EXTERNAL
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is infant, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I create and upload external packet where source is CRVS1, and packet type is CRVS_NEW, and argument 3 is $$personaFilePath and store result in $$rid
    And I sync external packet where argument 1 is $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I create and upload external packet where source is CRVS1, and packet type is CRVS_DEATH, and argument 3 is $$personaFilePath, and infotoken is true, and argument 5 is $$uin and store result in $$rid2
    And I sync external packet where argument 1 is $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_210
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident Infant walks into registration center gets UIN with parent RID detai...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I create and upload external packet where source is CRVS1, and packet type is CRVS_DEATH, and argument 3 is $$childPersona, and infotoken is true, and argument 5 is $$uin and store result in $$rid2
    And I sync external packet where argument 1 is $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_211
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    Then I check tags where argument 1 is $$rid
    And I read pre req where pre requisite data index is 4 and store result in $$details4
    And I set context where argument 1 is env_context, and argument 2 is $$details4, and generate private key is false, and packet type is EXTERNAL
    And I create and upload external packet where source is CRVS11@invalid, and packet type is CRVS_DEATH1, and argument 3 is $$personaFilePath, and infotoken is true, and argument 5 is $$uin and store result in $$rid2
    And I delete packet data

  @scenario_212
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I get email by uin where argument 1 is $$uin and store result in $$email1
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email1 and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email1
    And I update demo or bio details where bio type is finger, and password is face, and miss fields is 0, and update attributes is 0, and argument 5 is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$vid
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$Rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$Rid2
    And I get uinby rid where argument 1 is $$Rid2 and store result in $$Uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email1
    And I ekyc bio where argument 1 is faceDevice, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_213
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_Update
  Scenario: ResidentFemaleAdult - Resident walks into reg-center and uploads a packet and tries to update the n...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name=Asa2DFG@, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I delete packet data

  @scenario_214
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - A differently abled resident walk-ins to registration center completes the pr...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Iris:Left, and password is Iris:Right
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I oidc client where  and store result in $$clientId
    And I bio delegated authentication where argument 1 is faceDevice, and argument 2 is $$uin, and argument 3 is $$vid, and argument 4 is $$personaFilePath, and argument 5 is $$clientId
    And I delete packet data

  @scenario_215
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_Update
  Scenario: ResidentMaleAdult - Resident walks into reg-center and uploads a packet and tries to update the d...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is REPROCESS
    And I delete packet data

  @scenario_216
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into the registration center accompanied by a non-registered p...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath1
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath1 and store result in $$templatePath1
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath1, and argument 2 is $$templatePath1 and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and password is 99
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath2
    And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona to update is $$personaFilePath1, and persona to update with is $$personaFilePath2
    And I update resident with uin where argument 1 is $$personaFilePath1, and argument 2 is $$uin1
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath1 and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath1, and argument 2 is $$updateTemplate and store result in $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I delete packet data

  @scenario_217
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident A and Resident B successfully obtain their UINs. Resident A attempts...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath1
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath1 and store result in $$templatePath1
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath1, and argument 2 is $$templatePath1 and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and put scenario details in context is null, and password is 99
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath2
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath2 and store result in $$templatePath2
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath2, and argument 2 is $$templatePath2 and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I get email by uin where argument 1 is $$uin2 and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I update demo or bio details where bio type is rightMiddle, and miss fields is 0, and update attributes is 0, and persona to update is $$personaFilePath1, and persona to update with is $$personaFilePath2
    And I update demo or bio details where bio type is leftThumb, and password is leftRing, and update attributes is 0, and update attributes is 0, and persona to update is $$personaFilePath2, and persona to update with is $$personaFilePath1
    And I update resident with uin where argument 1 is $$personaFilePath1, and argument 2 is $$uin2
    And I update resident with uin where argument 1 is $$personaFilePath2, and argument 2 is $$uin1
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath1 and store result in $$updateTemplate
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath2 and store result in $$updateTemplate2
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath1, and argument 2 is $$updateTemplate and store result in $$rid3
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath2, and argument 2 is $$updateTemplate2 and store result in $$rid4
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid4
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I get uinby rid where argument 1 is $$rid4 and store result in $$uin4
    And I verify notification where argument 1 is updated, and argument 2 is $$email1
    Then I check ridstage where argument 1 is $$rid3, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$rid4, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_218
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: ResidentMaleAdult - Resident updates UIN biometrics by capturing left-hand fingerprints and irise...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Right Thumb, and password is Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@Finger:Right LittleFinger@@
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I check tags where argument 1 is $$rid2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_219
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Update_Adult
  Scenario: ResidentMaleAdult - Resident walks into the registration center and performs a biometric update f...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update bio exception in persona where argument 1 is $$personaFilePath, and argument 2 is Finger:Right Thumb, and password is Finger:Right IndexFinger@@Finger:Right MiddleFinger@@Finger:Right RingFinger@@Finger:Right LittleFinger@@Finger:Left Thumb@@Finger:Left IndexFinger@@Finger:Left MiddleFinger@@Finger:Left RingFinger@@Finger:Left LittleFinger
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I check tags where argument 1 is $$rid2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid2, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_220
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - An infant resident is registered and issued a UIN then later updates to adult...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get email by uin where argument 1 is $$parentUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update resident with uin where argument 1 is $$parentPersona, and argument 2 is $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male, and password is false/*FINGER_BIOMETRIC_FLAG*/@@false/*IRIS_BIOMETRIC_FLAG*/@@true and store result in $$childPersona
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    And I get email by uin where argument 1 is $$childUin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath1
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath1 and store result in $$templatePath1
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath1, and argument 2 is $$templatePath1 and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email1
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email1
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=adult, and persona file is $$childPersona
    And I update demo or bio details where bio type is iris, and password is finger, and update attributes is 0, and update attributes is 0, and persona to update is $$childPersona, and persona to update with is $$personaFilePath1
    And I update resident with uin where argument 1 is $$childPersona, and argument 2 is $$childUin
    And I get packet template where packet type is UPDATE, and argument 2 is $$childPersona and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$updateTemplate and store result in $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid3, and argument 2 is BIOGRAPHIC_VERIFICATION, and argument 3 is FAILED
    Then I check ridstage where argument 1 is $$rid3, and argument 2 is MANUAL_ADJUDICATION, and argument 3 is SUCCESS

  @scenario_221
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident 1 generates a UIN and updates biometrics twice successfully Resident...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid1
    And I check status where packet status is PROCESSED, and argument 2 is $$rid1
    And I get uinby rid where argument 1 is $$rid1 and store result in $$uin1
    And I get email by uin where argument 1 is $$uin1 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin1
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin2
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath2
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath2 and store result in $$templatePath2
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath2, and argument 2 is $$templatePath2 and store result in $$rid4
    And I check status where packet status is PROCESSED, and argument 2 is $$rid4
    And I get uinby rid where argument 1 is $$rid4 and store result in $$uin4
    And I get email by uin where argument 1 is $$uin4 and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is iris, and password is finger@@face, and update attributes is 0, and update attributes is 0, and persona to update is $$personaFilePath, and persona to update with is $$personaFilePath2
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin4
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid5
    And I check status where packet status is REJECTED, and argument 2 is $$rid5
    Then I check ridstage where argument 1 is $$rid5, and argument 2 is BIOGRAPHIC_VERIFICATION, and argument 3 is FAILED
    And I delete packet data

  @scenario_222
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident generates UIN ABIS response is delayed and first UIN biometric is up...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath2
    And I get bio modality hash where check persona presence is -1, and argument 2 is Right IndexFinger, and password is Left LittleFinger, and argument 4 is $$personaFilePath and store result in $$modalityHashValue
    And I configure mock abis where check persona presence is -1, and argument 2 is Right IndexFinger, and argument 3 is false, and argument 4 is Right IndexFinger, and argument 5 is $$personaFilePath, and argument 6 is $$modalityHashValue, and delay from actuator is delay, and error code is 10, and password is Error
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath2 and store result in $$rid2
    And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I delete mock expect where argument 1 is $$modalityHashValue
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_223
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process with invalid ty...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I ridsync where packet type is UPDATE, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is ERROR, and argument 4 is RPR-SYS-EXCEPTION-001
    And I delete packet data

  @scenario_224
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident registration where the officer is inactive during packet creation bu...
    Given I get ping health where argument 1 is packetcreator
    And I user where user action is ADD_User, and user index is 219, and password is Techno@123 and store result in $$user219
    And I center where argument 1 is CREATE, and user details is $$user219, and center index is 219, and center active flag is T and store result in $$center219
    And I machine where argument 1 is CREATE, and argument 2 is $$center219, and center index is 219 and store result in $$details219
    And I user where user action is DELETE_CENTERMAPPING, and user index is 219, and password is Techno@123, and password or details is $$details219 and store result in $$details219
    And I user where user action is CREATE_ZONESEARCH, and user index or master user is $$details219 and store result in $$details219
    And I wait where argument 1 is 10
    And I user where user action is DELETE_ZONEMAPPING, and user index or master user is $$details219
    And I write pre req where argument 1 is $$details219, and pre requisite data index is 219
    And I read pre req where pre requisite data index is 219 and store result in $$details219
    And I set context where argument 1 is env_context, and argument 2 is $$details219, and generate private key is true
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I packetcreator where packet type is NEW, and argument 2 is $$templatePath and store result in $$zipPacketPath
    And I user where user action is CREATE_ZONEMAPPING, and user index or master user is $$details219
    And I user where user action is ACTIVATE_ZONEMAPPING, and user index or master user is $$details219, and zone mapping activation flag is T
    And I user where user action is CREATE_CENTERMAPPING, and user index or master user is $$details219, and center index is 219
    And I user where user action is ACTIVATE_CENTERMAPPING, and user index or master user is $$details219, and user center mapping flag is T
    And I ridsync where packet type is NEW, and argument 2 is $$zipPacketPath and store result in $$rid
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I delete packet data

  @scenario_225
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_Update
  Scenario: ResidentMaleAdult - Resident walks into registration center uploads packet and gets UIN. Resident...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin2
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate2
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate2 and store result in $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is gender=Male, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin3
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate3
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate3 and store result in $$rid4
    And I check status where packet status is PROCESSED, and argument 2 is $$rid4
    And I get uinby rid where argument 1 is $$rid4 and store result in $$uin4
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=adult, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin4
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate4
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate4 and store result in $$rid5
    And I check status where packet status is PROCESSED, and argument 2 is $$rid5
    And I get uinby rid where argument 1 is $$rid5 and store result in $$uin5
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=test, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin5
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate5
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate5 and store result in $$rid6
    And I check status where packet status is PROCESSED, and argument 2 is $$rid6
    And I get uinby rid where argument 1 is $$rid6 and store result in $$uin6
    And I get email by uin where argument 1 is $$uin6 and store result in $$email1
    And I verify notification where argument 1 is updated, and argument 2 is $$email1
    And I delete packet data

  @scenario_226
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process  gets UIN card ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name=salman khan, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$newTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$newTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name=salman khan, and persona file is $$personaFilePath
    And I demo authentication where argument 1 is name, and argument 2 is $$uin2, and argument 3 is $$personaFilePath, and argument 4 is $$vid, and argument 5 is ERROR
    And I delete packet data

  @scenario_227
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into the registration center completes the process with gender...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is gender=Others, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_228
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into the registration center completes the process with empty ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=empty, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_229
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into the registration center completes the process with gender...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is gender=invalid, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_230
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_Adult_Update
  Scenario: ResidentMaleAdult - The resident walks into the registration center uploads the packet and gets t...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is finger, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I update demo or bio details where bio type is face, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin2
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate2
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate2 and store result in $$rid3
    And I check status where packet status is PROCESSED, and argument 2 is $$rid3
    And I get uinby rid where argument 1 is $$rid3 and store result in $$uin3
    And I update demo or bio details where bio type is iris, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin3
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate3
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate3 and store result in $$rid4
    And I check status where packet status is PROCESSED, and argument 2 is $$rid4
    And I get uinby rid where argument 1 is $$rid4 and store result in $$uin4
    And I delete packet data

  @scenario_231
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident updates only one fingerprint that was previously marked exception an...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is rightThumb, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is rightThumbDevice, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_232
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident updates only one iris that was previously marked exception and perfo...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I update demo or bio details where bio type is leftIris, and miss fields is 0, and update attributes is 0, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is LeftIris, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I delete packet data

  @scenario_233
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident enters DOB as 29-Feb in non-leap year and registration is rejected
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is dob=29-02-2019, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_234
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident enters an invalid email address missing the required symbol and regi...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=invalidemail, and persona file is $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_235
  @Negative_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident attempts to obtain UIN but Packet Creation Date is Future date and g...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and argument 4 is invalidCreationDate=+1y
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is CMD_VALIDATION, and argument 3 is ERROR
    And I delete packet data

  @scenario_236
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident booked pre-registration with support documents later changes the app...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I send otp where argument 1 is $$personaFilePath and store result in $$email
    And I validate otp where argument 1 is $$personaFilePath, and argument 2 is $$email
    And I pre register where argument 1 is $$personaFilePath and store result in $$prid
    And I upload documents where argument 1 is $$personaFilePath, and argument 2 is $$prid
    And I update pre reg status where argument 1 is 0, and argument 2 is $$prid, and argument 3 is valid
    And I book appointment where holiday booking flag is false, and argument 2 is $$prid, and slot number is 2
    And I book appointment where holiday booking flag is false, and argument 2 is $$prid, and slot number is 3
    And I generate and upload packet where argument 1 is $$prid, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_237
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident walks into registration center completes the process and gets UIN ca...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I update identity with array handles where argument 1 is $$uin, and argument 2 is $$personaFilePath
    And I get handles by uin where argument 1 is $$uin and store result in $$handles
    And I demo authentication where argument 1 is name, and argument 2 is $$uin, and argument 3 is $$personaFilePath, and argument 4 is $$vid, and argument 5 is 0, and argument 6 is $$handles
    And I delete packet data

  @scenario_238
  @Positive_Test
  @persona_ResidentFemaleAdult
  @group_Adult_New
  Scenario: ResidentFemaleAdult - Resident walks into registration center completes the process tries to get ui...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false, and invalid schema version is oldIdSchema
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and argument 2 is false, and argument 3 is Female and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I delete packet data

  @scenario_239
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: ResidentMaleAdult - Twin infants with similar demographics walk into registration center and use ...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male and store result in $$childPersona
    And I update resident with rid where argument 1 is $$parentPersona, and argument 2 is $$parentRid
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    Then I update demo or bio details where bio type is iris, and password is face@@finger, and update attributes is 0, and persona file is 0, and argument 5 is $$childPersona
    Then I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate2
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate2 and store result in $$childRid2
    Then I check ridstage where argument 1 is $$childRid2, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$childRid2, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_240
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Infant
  Scenario: ResidentMaleAdult - Identical twins with similar facial features walk into registration center an...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$parentPersona
    And I get packet template where packet type is NEW, and argument 2 is $$parentPersona and store result in $$parentTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$parentPersona, and argument 2 is $$parentTemplate and store result in $$parentRid
    And I check status where packet status is PROCESSED, and argument 2 is $$parentRid
    And I get uinby rid where argument 1 is $$parentRid and store result in $$parentUin
    And I get resident data where persona type is infant, and guardian flag is true, and gender is Male and store result in $$childPersona
    And I update resident with rid where argument 1 is $$parentPersona, and argument 2 is $$parentRid
    And I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate and store result in $$childRid
    And I check status where packet status is PROCESSED, and argument 2 is $$childRid
    And I get uinby rid where argument 1 is $$childRid and store result in $$childUin
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$childRid, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$childPersona
    Then I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is email=john, and persona file is $$childPersona
    Then I update resident with guardian skipping pre reg where argument 1 is $$parentPersona, and argument 2 is $$childPersona
    And I get packet template where packet type is NEW, and argument 2 is $$childPersona and store result in $$childTemplate2
    And I generate and upload packet skipping prereg where argument 1 is $$childPersona, and argument 2 is $$childTemplate2 and store result in $$childRid2
    Then I check ridstage where argument 1 is $$childRid2, and argument 2 is INTRODUCER_VALIDATION, and argument 3 is SUCCESS
    Then I check ridstage where argument 1 is $$childRid2, and argument 2 is VERIFICATION, and argument 3 is SUCCESS
    And I delete packet data

  @scenario_241
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Adult
  Scenario: ResidentMaleAdult - Resident walks to the center and creates a packet with a large face image size
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and gender is Male, and large face is true and store result in $$residentData
    And I get packet template where packet type is NEW, and argument 2 is $$residentData and store result in $$template
    And I generate and upload packet skipping prereg where argument 1 is $$residentData, and argument 2 is $$template and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I delete packet data

  @scenario_242
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_New_Adult
  Scenario: ResidentMaleAdult - Resident walks to the center and creates a packet with face obstruction image...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male, and large face is false, and obstructed face is true and store result in $$residentData
    And I get packet template where packet type is NEW, and argument 2 is $$residentData and store result in $$template
    And I generate and upload packet skipping prereg where argument 1 is $$residentData, and argument 2 is $$template and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I delete packet data

  @scenario_243
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_New
  Scenario: ResidentMaleAdult - Resident uploads only low-quality document and packet gets rejected during QC
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male, and argument 4 is LowQualityDocument=true and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I send otp where argument 1 is $$personaFilePath and store result in $$email
    And I validate otp where argument 1 is $$personaFilePath, and argument 2 is $$email
    And I pre register where argument 1 is $$personaFilePath and store result in $$prid
    And I upload documents where argument 1 is $$personaFilePath, and argument 2 is $$prid
    And I update pre reg status where status code is 0, and argument 2 is $$prid, and scenario without pending appointment is valid
    And I book appointment where holiday booking flag is false, and argument 2 is $$prid, and slot number is 1
    And I generate and upload packet where argument 1 is $$prid, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    And I delete packet data

  @scenario_244
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_Update
  Scenario: ResidentMaleAdult - Resident gets UIN updates iris and face biometrics authenticates successfully...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I skip
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I clone resident data where argument 1 is $$personaFilePath and store result in $$oldBioPersonaFilePath
    And I update demo or bio details where bio type is iris, and password is face, and miss fields is 0, and update attributes is 0, and argument 5 is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I update resident with uin where argument 1 is $$oldBioPersonaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I bio authentication where argument 1 is LeftIris, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$personaFilePath
    And I bio authentication where argument 1 is faceDevice, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$oldBioPersonaFilePath, and argument 5 is ERROR
    And I bio authentication where argument 1 is LeftIris, and argument 2 is $$uin2, and argument 3 is $$vid, and argument 4 is $$oldBioPersonaFilePath, and argument 5 is ERROR
    And I delete packet data

  @scenario_245
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_Adult_Update
  Scenario: ResidentMaleAdult - Resident gets UIN updates demographic details authenticates successfully with...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I clone resident data where argument 1 is $$personaFilePath and store result in $$oldDemoPersonaFilePath
    And I update demo or bio details where bio type is 0, and miss fields is 0, and update attributes is name, and persona file is $$personaFilePath
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I update resident with uin where argument 1 is $$oldDemoPersonaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is UPDATE, and argument 2 is $$personaFilePath and store result in $$updateTemplate
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$updateTemplate and store result in $$rid2
    And I check status where packet status is PROCESSED, and argument 2 is $$rid2
    And I get uinby rid where argument 1 is $$rid2 and store result in $$uin2
    And I verify notification where argument 1 is updated, and argument 2 is $$email
    And I wait where argument 1 is UIN_WAIT_TIME
    And I generate vid where argument 1 is Perpetual, and argument 2 is $$uin2, and argument 3 is $$email and store result in $$vid
    And I verify notification where argument 1 is Successful Generation of VID, and argument 2 is $$email
    And I wait where argument 1 is 90
    And I demo authentication where argument 1 is name, and argument 2 is $$uin2, and argument 3 is $$personaFilePath, and argument 4 is $$vid
    And I demo authentication where argument 1 is name, and argument 2 is $$uin2, and argument 3 is $$oldDemoPersonaFilePath, and argument 4 is $$vid, and argument 5 is ERROR
    And I delete packet data

  @scenario_246
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Mock ABIS holds the packet in queue longer than the reprocessor interval (del...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I get bio modality hash where check persona presence is -1, and argument 2 is Right IndexFinger, and password is Left LittleFinger, and argument 4 is $$personaFilePath and store result in $$modalityHashValue
    And I configure mock abis where check persona presence is -1, and argument 2 is Right IndexFinger, and password is Left LittleFinger, and duplicate flag is true, and argument 5 is Right IndexFinger, and password is Left LittleFinger, and argument 7 is $$personaFilePath, and argument 8 is $$modalityHashValue, and delay from actuator is delay, and error code is 10, and password is Error
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is BIOGRAPHIC_VERIFICATION, and argument 3 is REPROCESS
    Then I verify bio dedup skipped after reprocess where argument 1 is $$rid
    Then I delete mock expect
    And I check status where packet status is PROCESSED, and argument 2 is $$rid
    And I get uinby rid where argument 1 is $$rid and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$rid, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_247
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Resident creates a Lost packet that gets rejected then reuses the same biomet...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is LOST, and argument 2 is $$personaFilePath and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$ridLost
    And I packetsync where argument 1 is $$zipPacketPath
    And I post mock mv where argument 1 is $$ridLost, and argument 2 is REJECTED
    And I check status where packet status is REJECTED, and argument 2 is $$ridLost
    Then I check ridstage where argument 1 is $$ridLost, and argument 2 is BIOGRAPHIC_VERIFICATION, and argument 3 is FAILED
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$ridNew
    And I check status where packet status is PROCESSED, and argument 2 is $$ridNew
    And I get uinby rid where argument 1 is $$ridNew and store result in $$uin
    And I get email by uin where argument 1 is $$uin and store result in $$email
    And I verify notification where argument 1 is UIN Generated, and argument 2 is $$email
    Then I check ridstage where argument 1 is $$ridNew, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_248
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Create a new registration packet and process it then create a Lost packet for...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$ridNew
    And I check status where packet status is PROCESSED, and argument 2 is $$ridNew
    And I get uinby rid where argument 1 is $$ridNew and store result in $$uin
    And I update resident with uin where argument 1 is $$personaFilePath, and argument 2 is $$uin
    And I get packet template where packet type is LOST, and argument 2 is $$personaFilePath and store result in $$lostTemplate
    And I packetcreator where packet type is LOST, and argument 2 is $$lostTemplate and store result in $$zipPacketPath
    And I ridsync where packet type is LOST, and argument 2 is $$zipPacketPath and store result in $$ridLost
    And I packetsync where argument 1 is $$zipPacketPath
    And I check status where packet status is PROCESSED, and argument 2 is $$ridLost
    Then I check ridstage where argument 1 is $$ridLost, and argument 2 is PRINT_SERVICE, and argument 3 is PROCESSED
    And I delete packet data

  @scenario_249
  @Negative_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Registration packet is built without required name demographic fields in iden...
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I get ping health where argument 1 is targetenv
    And I get resident data where persona type is adult, and guardian flag is false, and argument 3 is Male, and argument 4 is name and store result in $$personaFilePath
    And I get packet template where packet type is NEW, and argument 2 is $$personaFilePath and store result in $$templatePath
    And I generate and upload packet skipping prereg where argument 1 is $$personaFilePath, and argument 2 is $$templatePath and store result in $$rid
    And I check status where packet status is REREGISTER, and argument 2 is $$rid
    Then I check ridstage where argument 1 is $$rid, and argument 2 is VALIDATE_PACKET, and argument 3 is FAILED
    And I delete packet data

  @scenario_AFTER_SUITE
  @Positive_Test
  @persona_ResidentMaleAdult
  @group_NA
  Scenario: ResidentMaleAdult - Test suite run Pre-Requisite data tear down
    Given I skip
    Given I get ping health where argument 1 is packetcreator
    And I read pre req where pre requisite data index is 1 and store result in $$details1
    And I set context where argument 1 is env_context, and argument 2 is $$details1, and generate private key is false
    And I skip
    And I delete mock expect
    And I machine where argument 1 is DCOM, and argument 2 is $$details1
    And I user where user action is DELETE_CENTERMAPPING, and user index is 1, and password is Techno@123, and password or details is $$details1 and store result in $$details1
    And I center where argument 1 is DCOM, and argument 2 is $$details1, and center index is 1
    And I read pre req where pre requisite data index is 2 and store result in $$details2
    And I machine where argument 1 is DCOM, and argument 2 is $$details2
    And I user where user action is DELETE_CENTERMAPPING, and user index is 2, and password is Techno@123, and password or details is $$details2 and store result in $$details2
    And I center where argument 1 is DCOM, and argument 2 is $$details2, and center index is 2
    And I read pre req where pre requisite data index is 3 and store result in $$details3
    And I machine where argument 1 is DCOM, and argument 2 is $$details3
    And I user where user action is DELETE_CENTERMAPPING, and user index is 3, and password is Techno@123, and password or details is $$details3 and store result in $$details3
    And I center where argument 1 is DCOM, and argument 2 is $$details3, and center index is 3
    And I user where user action is DELETE_User, and master user is dsl-0, and password is Techno@123
    And I delete certificates and onboarding partners
    And I masterdata delete
    And I write persona data

