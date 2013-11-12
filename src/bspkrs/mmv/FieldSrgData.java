package bspkrs.mmv;

public class FieldSrgData implements Comparable<FieldSrgData>
{
    private final String  obfOwner;
    private final String  obfName;
    private final String  srgOwner;
    private final String  srgPkg;
    private final String  srgName;
    private final boolean isClientOnly;
    
    public FieldSrgData(String obfOwner, String obfName, String srgOwner, String srgPkg, String srgName, boolean isClientOnly)
    {
        this.obfOwner = obfOwner;
        this.obfName = obfName;
        this.srgOwner = srgOwner;
        this.srgPkg = srgPkg;
        this.srgName = srgName;
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
    
    public String getSrgOwner()
    {
        return srgOwner;
    }
    
    public String getSrgName()
    {
        return srgName;
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
    public int compareTo(FieldSrgData o)
    {
        if (o != null)
            return srgName.compareToIgnoreCase(o.srgName);
        else
            return 1;
    }
}
