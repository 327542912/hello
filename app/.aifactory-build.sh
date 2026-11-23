set -e
P='/data/data/com.aifactory.app/files/home/projects/snake'
B='/data/data/com.aifactory.app/files/home/projects/snake/app/build'
AAPT2='/data/data/com.aifactory.app/files/home/android-sdk/build-tools/36.0.0/aapt2'
JAR='/data/data/com.aifactory.app/files/home/android-sdk/platforms/android-36/android.jar'
BT='/data/data/com.aifactory.app/files/home/android-sdk/build-tools/36.0.0'
rm -rf $B
mkdir -p $B/gen $B/classes $B/dex
rm -rf $B/flat && mkdir -p $B/flat
$AAPT2 compile --dir '/data/data/com.aifactory.app/files/home/projects/snake/app/src/main/res' -o $B/flat
FL=$(find $B/flat -name '*.flat' | sort | tr '\n' ' ')
if [ -z "$FL" ]; then echo '没有编译出任何 .flat（res 目录为空？）'; exit 9; fi
$AAPT2 link -o $B/base.apk -I $JAR --manifest '/data/data/com.aifactory.app/files/home/projects/snake/app/src/main/AndroidManifest.xml' --auto-add-overlay --java $B/gen --min-sdk-version 26 --target-sdk-version 28 $FL
find '/data/data/com.aifactory.app/files/home/projects/snake/app/src/main/java' $B/gen -name "*.java" > $B/srcs
'/data/data/com.aifactory.app/files/usr/bin/javac' -source 8 -target 8 -bootclasspath $JAR -d $B/classes @$B/srcs
find $B/classes -name "*.class" > $B/classes.lst
'/data/data/com.aifactory.app/files/usr/bin/java' -Xmx768m -cp $BT/lib/d8.jar com.android.tools.r8.D8 --lib $JAR --min-api 26 --output $B/dex $(cat $B/classes.lst)
cp $B/base.apk $B/u.apk
cd $B/dex && jar uf $B/u.apk classes.dex && cd $P
$BT/zipalign -f 4 $B/u.apk $B/a.apk || cp $B/u.apk $B/a.apk
'/data/data/com.aifactory.app/files/usr/bin/java' -jar $BT/lib/apksigner.jar sign --min-sdk-version 26 --ks '/data/data/com.aifactory.app/files/home/.aifactory/release.jks' --ks-pass env:AIF_KS_PASS --key-pass env:AIF_KS_PASS $B/a.apk
mkdir -p $B/outputs/apk/debug
cp $B/a.apk $B/outputs/apk/debug/app-debug.apk
ls -l $B/outputs/apk/debug/app-debug.apk
