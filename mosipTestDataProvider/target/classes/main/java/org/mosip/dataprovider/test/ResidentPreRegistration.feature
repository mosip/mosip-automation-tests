Feature: DataProvider
Scenario: Generate Resident
Given Adult "Male" from "IN"
When request otp for his/her "email"
And fetch otp
Then verify otp ""
And  PreRegister him
Then upload "POI" document
And book first available appointment
