# Setup instructions for linux:

* Download the packet-utility zip folder and extract to desired location.
* Download bio.zip and extract it to : <home>\.m2\repository\io\mosip
* In the packet-utility folder, there should be 3 folders: deploy, mosip-packet-creator and mosipTestDataProvider
* run "mvn clean install" in mosipTestDataProvider
* run addtorepo.sh as a bash script in mosipTestDataProvider
* run "mvn clean install" in mosip-packet-creator
* download a tpm simulator of your choice. 
  * If you want a direct option: 
    * ```sh
        sudo snap install tpm2-simulator-chrisccoulson --edge
      ```
  * To run this simulator: 
    * ```sh
        tpm2-simulator-chrisccoulson.tpm2-simulator
      ```
* Run the tpm simulator
* In deploy/mockmds, run "run.sh" as bash script(you may have to change file format to unix 
  * (https://stackoverflow.com/questions/82726/convert-dos-line-endings-to-linux-line-endings-in-vim)
* copy the jar file from mosip-packet-creator/target to deploy
* from Deploy:
  * run:    
    * ```sh
        java -Dfile.encoding=UTF-8  -jar mosip-packet-creator-0.0.1-SNAPSHOT.jar --spring.config.location=./config/application.properties
      ```
  * wait for initialization to complete, no error is expected here.
* After the spring app startup is complete, go to a browser and go to "http://localhost:8080/swagger-ui.html". You should be able to see the swagger ui.

	