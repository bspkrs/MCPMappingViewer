package bspkrs.mmv;

public class ClassSrgData
{
    private final String  obfName;
    private final String  srgName;
    private String        srgPkgName;
    private final boolean isClientOnly;
    
    public ClassSrgData(String obfName, String srgName, String srgPkgName, boolean isClientOnly)
    {
        this.obfName = obfName;
        this.srgName = srgName;
        this.srgPkgName = srgPkgName;
        this.isClientOnly = isClientOnly;
    }
    
    public String getObfName()
    {
        return this.obfName;
    }
    
    public String getSrgName()
    {
        return this.srgName;
    }
    
    public String getSrgPkgName()
    {
        return this.srgPkgName;
    }
    
    public ClassSrgData setSrgPkgName(String pkg)
    {
        this.srgPkgName = pkg;
        return this;
    }

    public boolean isClientOnly()
    {
        return isClientOnly;
    }
}
