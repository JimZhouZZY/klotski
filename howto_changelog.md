git add somefile
git commit -m "some msg"
./changelog.sh
git add .
git commit --ammend --no-edit
git push
