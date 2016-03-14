cp out/artifacts/TaflEngine_jar/OpenTafl.jar package/
cp README.txt package/
cp LICENSE.txt package/
cp engines/opentafl.ini package/engines
cd package
zip OpenTafl.zip OpenTafl.* OpenTafl*bat engines/* README.txt LICENSE.txt
rm OpenTafl.jar
cd ..
mv package/OpenTafl.zip .
