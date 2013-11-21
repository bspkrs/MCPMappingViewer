#!/bin/sh

echo "*** Cleaning bin folder..."
if [ -e bin ]
then
  rm -rf bin
fi
if [ -e dist ]
then
  rm -rf dist
fi
mkdir bin
mkdir dist
cd src

echo "*** Compiling MCPMappingViewer class files..."
javac -d ../bin -sourcepath . bspkrs/mmv/gui/MappingGui.java


echo "*** Packing MCPMappingViewer.jar..."
cd ../bin
jar cvfm ../dist/MCPMappingViewer.jar ../META-INF/MANIFEST.MF .
cd ..
jar uf dist/MCPMappingViewer.jar LICENSE

echo "*** Build complete!"
