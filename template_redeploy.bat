@ECHO OFF
echo Building Server and Deploying Docker Image!

set LINUX_USER=
set LINUX_HOST=
set LINUX_DEST=
set WINDOWS_SRC=export.tar
set REMOTE_COMMAND="sh /home/badger/reinstall.sh"
set PPK_PATH=

npm run build --prefix credible-badger-ui && mvn clean install && docker rm credible-badger-server -f && docker image rm credible-badger-server && docker compose create && docker save -o export.tar credible-badger-server && pscp -i %PPK_PATH% %WINDOWS_SRC% %LINUX_USER%@%LINUX_HOST%:%LINUX_DEST% && plink -batch -i %PPK_PATH% %LINUX_USER%@%LINUX_HOST% %REMOTE_COMMAND% && echo Deploy Successful!