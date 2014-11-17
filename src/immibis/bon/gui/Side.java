/*
 * Copyright (C) 2014 Alex "immibis" Campbell, bspkrs
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
package immibis.bon.gui;

import immibis.bon.NameSet;

public enum Side
{
    Universal(NameSet.Side.UNIVERSAL, "bin/minecraft", new int[] { 2, 0, 1 }),
    Client(NameSet.Side.CLIENT, "bin/minecraft", new int[] { 0 }),
    Server(NameSet.Side.SERVER, "bin/minecraft_server", new int[] { 1 });

    private Side(NameSet.Side nsside, String referencePath, int[] side)
    {
        this.nsside = nsside;
        this.referencePath = referencePath;
        this.intSide = side;
    }

    public final NameSet.Side nsside;
    public final String       referencePath;
    public final int[]        intSide;
}
