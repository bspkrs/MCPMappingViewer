/*
 * Copyright (C) 2014 Alex "immibis" Campbell
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
package immibis.bon;

/**
 * E.g. "1.5.1 obfuscated", "1.5.1 searge", "1.5.1 MCP" are NameSets.
 */
public class NameSet
{
    public static enum Type
    {
        OBF,
        SRG,
        MCP
    }

    public static enum Side
    {
        UNIVERSAL,
        CLIENT,
        SERVER
    }

    public final Type   type;
    public final String mcVersion;
    public final Side   side;

    public NameSet(Type type, Side side, String mcVersion)
    {
        this.type = type;
        this.side = side;
        this.mcVersion = mcVersion;
    }

    @Override
    public boolean equals(Object obj)
    {
        try
        {
            NameSet ns = (NameSet) obj;
            return ns.type == type && ns.side == side && ns.mcVersion.equals(mcVersion);

        }
        catch (ClassCastException e)
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        return (side.ordinal() << 8) + type.ordinal() + mcVersion.hashCode();
    }

    @Override
    public String toString()
    {
        return mcVersion + " " + type + " " + side;
    }
}
