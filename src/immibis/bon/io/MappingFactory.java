/*
 * Copyright (C) 2013 Alex "immibis" Campbell
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation 
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, 
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package immibis.bon.io;

import immibis.bon.IProgressListener;
import immibis.bon.JoinMapping;
import immibis.bon.Mapping;
import immibis.bon.NameSet;
import immibis.bon.mcp.MappingLoader_MCP;
import immibis.bon.mcp.MappingLoader_MCP.CantLoadMCPMappingException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MappingFactory
{
    
    public static class MappingUnavailableException extends Exception
    {
        private static final long serialVersionUID = 1L;
        
        public MappingUnavailableException(String message)
        {
            super(message);
        }
        
        public MappingUnavailableException(NameSet from, NameSet to, String reason)
        {
            super("Can't create mapping from " + from + " to " + to + " - " + reason);
        }
    }
    
    private static Map<String, MappingLoader_MCP> mcpInstances = new HashMap<>();
    
    public static void registerMCPInstance(String mcVersion, NameSet.Side side, File mcpPath, IProgressListener progress) throws IOException, CantLoadMCPMappingException
    {
        mcpInstances.put(mcVersion + " " + side, new MappingLoader_MCP(mcVersion, side, mcpPath, progress));
    }
    
    @SuppressWarnings("incomplete-switch")
    public static Mapping getMapping(NameSet from, NameSet to, IProgressListener progress) throws MappingUnavailableException
    {
        if (!from.mcVersion.equals(to.mcVersion))
            throw new MappingUnavailableException(from, to, "different Minecraft version");
        
        if (from.type == to.type)
            throw new MappingUnavailableException(from, to, "");
        
        MappingLoader_MCP mcpLoader = mcpInstances.get(from.mcVersion + " " + from.side);
        
        if (mcpLoader != null)
        {
            MappingLoader_MCP loader = mcpLoader;
            switch (from.type)
            {
                case MCP:
                    switch (to.type)
                    {
                        case OBF:
                            return new JoinMapping(
                                    loader.getReverseCSV(),
                                    loader.getReverseSRG());
                        case SRG:
                            return loader.getReverseCSV();
                    }
                    break;
                case OBF:
                    switch (to.type)
                    {
                        case MCP:
                            return new JoinMapping(
                                    loader.getForwardSRG(),
                                    loader.getForwardCSV());
                        case SRG:
                            return loader.getForwardSRG();
                    }
                    break;
                case SRG:
                    switch (to.type)
                    {
                        case OBF:
                            return loader.getReverseSRG();
                        case MCP:
                            return loader.getForwardCSV();
                    }
                    break;
            }
            throw new MappingUnavailableException(from, to, "not supported");
        }
        
        throw new MappingUnavailableException(from, to, "no known MCP folder for " + from.mcVersion);
    }
    
}
