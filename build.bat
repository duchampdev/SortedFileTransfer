rmdir /s /q runtimeImage
CALL gradlew.bat assemble
"%JAVA_HOME%\bin\jlink"^
 --module-path "%JAVA_HOME%\jmods";build\libs\SortedFileTransfer-1.3.jar^
 --add-modules SortedFileTransfer^
 --launcher SortedFileTransfer=SortedFileTransfer/duchampdev.sft.Main^
 --output runtimeImage