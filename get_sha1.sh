#!/bin/bash

echo "=== SHA-1 Fingerprint для Debug Keystore ==="
echo ""
echo "Debug keystore обычно находится по пути: ~/.android/debug.keystore"
echo ""

if [ -f ~/.android/debug.keystore ]; then
    echo "Debug keystore найден!"
    echo ""
    echo "Получение SHA-1 fingerprint..."
    echo ""
    keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep "SHA1:"
    echo ""
    echo "Скопируйте SHA-1 fingerprint и добавьте его в Firebase Console:"
    echo "Project Settings → Your apps → Android app → Add fingerprint"
else
    echo "Debug keystore не найден по стандартному пути!"
    echo "Попробуйте найти его вручную или создайте новый проект в Android Studio."
fi

echo ""
echo "=== SHA-1 Fingerprint для Release Keystore ==="
echo ""
echo "Если у вас есть release keystore, используйте команду:"
echo "keytool -list -v -keystore /path/to/your/release.keystore -alias your_alias"






