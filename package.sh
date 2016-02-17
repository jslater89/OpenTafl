cp out/artifacts/TaflEngine_jar/OpenTafl.jar package/
cp README.txt package/
cp LICENSE.txt package/
cd package
zip OpenTafl.zip OpenTafl.* OpenTafl*bat README.txt LICENSE.txt
rm OpenTafl.jar
cd ..
mv package/OpenTafl.zip .
