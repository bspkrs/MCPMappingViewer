package bspkrs.mmv;

public class MethodSrgData implements Comparable<MethodSrgData>
{
    private final String  obfOwner;
    private final String  obfName;
    private final String  obfDescriptor;
    private final String  srgOwner;
    private final String  srgPkg;
    private final String  srgName;
    private final String  srgDescriptor;
    private final boolean isClientOnly;
    
    public MethodSrgData(String obfOwner, String obfName, String obfDescriptor, String srgOwner, String srgPkg, String srgName, String srgDescriptor, boolean isClientOnly)
    {
        this.obfOwner = obfOwner;
        this.obfName = obfName;
        this.obfDescriptor = obfDescriptor;
        this.srgOwner = srgOwner;
        this.srgPkg = srgPkg;
        this.srgName = srgName;
        this.srgDescriptor = srgDescriptor;
        this.isClientOnly = isClientOnly;
    }
    
    public String getObfOwner()
    {
        return obfOwner;
    }
    
    public String getObfName()
    {
        return obfName;
    }
    
    public String getObfDescriptor()
    {
        return obfDescriptor;
    }
    
    public String getSrgOwner()
    {
        return srgOwner;
    }
    
    public String getSrgName()
    {
        return srgName;
    }
    
    public String getSrgDescriptor()
    {
        return srgDescriptor;
    }
    
    public boolean isClientOnly()
    {
        return isClientOnly;
    }
    
    public String getSrgPkg()
    {
        return srgPkg;
    }
    
    @Override
    public int compareTo(MethodSrgData o)
    {
        if (o != null)
            return srgName.compareToIgnoreCase(o.srgName);
        else
            return 1;
    }
}
