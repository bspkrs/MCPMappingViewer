package bspkrs.mmv;

public class FieldSrgData
{
    private final String  obfOwner;
    private final String  obfName;
    private final String  srgOwner;
    private final String  srgName;
    private final boolean isClientOnly;
    
    public FieldSrgData(String obfOwner, String obfName, String srgOwner, String srgName, boolean isClientOnly)
    {
        this.obfOwner = obfOwner;
        this.obfName = obfName;
        this.srgOwner = srgOwner;
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
}
