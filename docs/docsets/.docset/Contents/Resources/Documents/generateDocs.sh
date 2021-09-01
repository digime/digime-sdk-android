cd $(dirname $0)
jazzy --objc --umbrella-header ~/Desktop/jazzy\ test/Header.h \
-o . \
--documentation="guides/*.md" \
--readme ../README.md \
--theme fullwidth \
--github_url https://github.com/digime/digime-sdk-android \
--github-file-prefix https://github.com/digime/digime-sdk-android/tree/master \
--root-url https://github.io/digime/digime-sdk-android
