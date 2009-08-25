package org.netspective.io.spreadsheet;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class PackageAttributes
{
    public static final PackageAttributes PACKAGE = new PackageAttributes();

    public static final String PRODUCT_NAME = "Spreadsheet Model Library";
    public static final String PRODUCT_ID = "spreadsheet-model";

    public static final int PRODUCT_RELEASE_NUMBER = 1;
    public static final int PRODUCT_VERSION_MAJOR = 0;

    private final Properties properties;

    public PackageAttributes()
    {
        properties = new Properties();
        try
        {
            properties.load(getClass().getResourceAsStream("/org/netspective/io/spreadsheet/PackageAttributes.properties"));
        }
        catch(final IOException e)
        {
            System.err.println("Unable to read properties for PackageAttributes.");
        }
    }

    public String getProductId()
    {
        return PRODUCT_ID;
    }

    public String getProductName()
    {
        return PRODUCT_NAME;
    }

    public final int getReleaseNumber()
    {
        return PRODUCT_RELEASE_NUMBER;
    }

    public final int getVersionMajor()
    {
        return PRODUCT_VERSION_MAJOR;
    }

    public final int getVersionMinor()
    {
        return Integer.parseInt(properties.getProperty("build.number", "-1"));
    }

    public final Date getBuildDate()
    {
        final String buildDate = properties.getProperty("build.date");
        try
        {
            return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).parse(buildDate);
        }
        catch (ParseException e)
        {
            System.err.printf("Unable to parse build date %s in PackageAttributes.properties: %s %s\n", buildDate, e.getClass().getName(), e.getMessage());
            return new Date();
        }
    }

    public final String getVersion()
    {
        return String.format("%d.%d.%d", getReleaseNumber(), getVersionMajor(), getVersionMinor());
    }
}