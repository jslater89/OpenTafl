cp out/artifacts/TaflEngine_jar/OpenTafl.jar package/
cd package
zip OpenTafl.zip OpenTafl.* OpenTafl*bat ansicon-x86/*.* ansicon-x64/*.* README.txt LICENSE.txt
rm OpenTafl.jar
cd ..
mv package/OpenTafl.zip .
