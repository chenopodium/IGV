::Get the current batch file's short path
for %%x in (%0) do set BatchPath=%%~dpsx
for %%x in (%BatchPath%) do set BatchPath=%%~dpsx
java  -Xmx1200m -Djava.net.preferIPv4Stack=true -Dsun.java2d.noddraw=true -jar %BatchPath%\igv.jar  %*
