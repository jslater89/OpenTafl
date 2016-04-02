mkdir package
mkdir package/engines
mkdir package/saved-games
mkdir package/saved-games/replays

cp -R unbuilt/* package
cp out/artifacts/TaflEngine_jar/OpenTafl.jar package/
cp README.txt package/
cp LICENSE.txt package/
cd package
zip OpenTafl.zip OpenTafl.* OpenTafl*bat engines/* saved-games/* saved-games/replays/* README.txt LICENSE.txt
rm OpenTafl.jar
cd ..
mv package/OpenTafl.zip .

rm -rf package/
