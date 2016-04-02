mkdir package/engines
mkdir package/saved-games
mkdir package/saved-games/replays

cp unbuilt/* package
cp out/artifacts/TaflEngine_jar/OpenTafl.jar package/
cp README.txt package/
cp LICENSE.txt package/
cp engines/opentafl.ini package/engines
cp saved-games/replays/* package/saved-games/replays
cd package
zip OpenTafl.zip OpenTafl.* OpenTafl*bat engines/* saved-games/* saved-games/replays/* README.txt LICENSE.txt
rm OpenTafl.jar
cd ..
mv package/OpenTafl.zip .

rm -rf package/*
