@echo off

echo *** Cleaning bin folder...
if exist bin rmdir /S /Q bin
mkdir bin

echo *** Compiling MCPMappingViewer class files...
javac -d bin -sourcepath . bspkrs\mmv\gui\GuiMain.java

echo *** Packing MCPMappingViewer.jar...
cd bin
jar cvfm MCPMappingViewer.jar ../META-INF/MANIFEST.MF .
cd ..

echo *** Build complete!
pause
