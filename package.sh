cp out/artifacts/TaflEngine_jar/OpenTafl.jar package/
cd package
zip OpenTafl.zip OpenTafl.* OpenTafl*bat ansicon-x86/*.* ansicon-x64/*.*
rm OpenTafl.jar
cd ..
mv package/OpenTafl.zip .
