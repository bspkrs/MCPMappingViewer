/*
 * Copyright (C) 2014 bspkrs
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
package bspkrs.mmv;

public class ExcData implements Comparable<ExcData>
{
    private final String   srgOwner;
    private final String   srgName;
    private final String   descriptor;
    private final String[] exceptions;
    private final String[] parameters;
    
    public ExcData(String srgOwner, String srgName, String descriptor, String[] exceptions, String[] parameters)
    {
        this.srgOwner = srgOwner;
        this.srgName = srgName;
        this.descriptor = descriptor;
        this.exceptions = exceptions;
        this.parameters = parameters;
    }
    
    public String getSrgClassOwner()
    {
        return srgOwner;
    }
    
    public String getSrgMethodName()
    {
        return srgName;
    }
    
    public String getDescriptor()
    {
        return descriptor;
    }
    
    public String[] getExceptions()
    {
        return exceptions;
    }
    
    public String[] getParameters()
    {
        return parameters;
    }
    
    public boolean contains(String s)
    {
        return srgName.contains(s);
    }
    
    @Override
    public int compareTo(ExcData o)
    {
        return this.srgName.compareTo(o.srgName);
    }
}
