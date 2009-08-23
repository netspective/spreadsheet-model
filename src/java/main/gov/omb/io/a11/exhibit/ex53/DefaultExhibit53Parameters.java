package gov.omb.io.a11.exhibit.ex53;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultExhibit53Parameters implements Exhibit53Parameters
{
    protected final static String INVERTED_FLAG_PREFIX = "dont-";
    public static final Parameter[] PARAMETERS;
    public static final Map<String, Parameter> PARAMETERS_MAP = new HashMap<String, Parameter>();

    static
    {
        PARAMETERS = new Parameter[]
        {
            new ParameterImpl("workbook-name", new ParameterArgumentImpl("file", ArgumentType.STRING), true),
            new ParameterImpl("worksheet-name", new ParameterArgumentImpl("excel-sheet", ArgumentType.STRING), false),
            new ParameterImpl("budget-year", new ParameterArgumentImpl("year", ArgumentType.INT), true),
            new ParameterImpl("agency-code", new ParameterArgumentImpl("code", ArgumentType.STRING), true),
            new ParameterImpl("bureau-codes", new ParameterArgumentImpl("code,code", ArgumentType.STRING), false),
            new ParameterImpl("debug", new ParameterArgumentImpl(), false),
            new ParameterImpl("dont-validate-any-subtotals", new ParameterArgumentImpl(), false),
            new ParameterImpl("dont-validate-funding-source-subtotals", new ParameterArgumentImpl(), false),
            new ParameterImpl("dont-validate-investment-line-subtotal-with-funding-source-subtotals", new ParameterArgumentImpl(), false),
            new ParameterImpl("dont-validate-investments-group-subtotals", new ParameterArgumentImpl(), false),
            new ParameterImpl("dont-validate-mission-area-subtotals", new ParameterArgumentImpl(), false),
            new ParameterImpl("dont-validate-part-subtotals", new ParameterArgumentImpl(), false),
            new ParameterImpl("dont-validate-portfolio-totals", new ParameterArgumentImpl(), false),
            new ParameterImpl("dont-validate-budget-accounts-codes-in-funding-sources", new ParameterArgumentImpl(), false),
        };

        for(final Parameter p : PARAMETERS)
        {
            final String name = p.getName();
            if(name.startsWith(INVERTED_FLAG_PREFIX))
            {
                final String actualName = name.substring(INVERTED_FLAG_PREFIX.length());
                PARAMETERS_MAP.put(actualName, p);
                PARAMETERS_MAP.put(parameterNameToJavaIdentifier(actualName, false), p);
            }
            else
            {
                PARAMETERS_MAP.put(name, p);
                PARAMETERS_MAP.put(parameterNameToJavaIdentifier(name, false), p);
            }
        }
    }

    private String workbookName;
    private String worksheetName;
    private boolean debug;
    private int budgetYear;
    private String agencyCode;
    private List<String> bureauCodes = new ArrayList<String>();

    private boolean validateAnySubtotals = true;
    private boolean validateFundingSourceSubtotals = true;
    private boolean validateInvestmentLineSubtotalWithFundingSourceSubtotals = true;
    private boolean validateInvestmentsGroupSubtotals = true;
    private boolean validateMissionAreaSubtotals = true;
    private boolean validatePartSubtotals = true;
    private boolean validatePortfolioTotals = true;

    private boolean validateBudgetAccountsCodesInFundingSources = true;

    private final List<String> errors = new ArrayList<String>();
    private Workbook workbook;
    private Sheet sheet;

    public DefaultExhibit53Parameters(final int budgetYear, final String agencyCode, final List<String> bureauCodes)
    {
        setBudgetYear(budgetYear);
        setAgencyCode(agencyCode);
        this.bureauCodes = bureauCodes;
    }

    public DefaultExhibit53Parameters(final Map<String, String> parameters)
    {
        final DynaBean thisBean = new WrapDynaBean(this);
        final DynaClass dynaClass = thisBean.getDynaClass();
        for(final Map.Entry<String, String> entry : parameters.entrySet())
        {
            boolean invertedFlag = entry.getKey().startsWith(INVERTED_FLAG_PREFIX);
            final String parameterName = invertedFlag ? entry.getKey().substring(INVERTED_FLAG_PREFIX.length()) : entry.getKey();
            final Parameter parameter = PARAMETERS_MAP.get(parameterName);
            if(parameter == null)
            {
                errors.add(String.format("Unable to find parameter %s.", parameterName));
                continue;
            }

            DynaProperty dynaProperty = dynaClass.getDynaProperty(parameterName);
            final String property;
            if(dynaProperty == null)
            {
                property = parameterNameToJavaIdentifier(parameterName, false);
                dynaProperty = dynaClass.getDynaProperty(property);
                if(dynaProperty == null)
                {
                    errors.add(String.format("Unable to find property for %s or %s %s", parameterName, property, invertedFlag ? "(inverted flag)" : ""));
                    continue;
                }
            }
            else
                property = parameterName;

            try
            {
                final Object value = parameter.getArgument().convertValue(entry.getKey(), entry.getValue());
                try
                {
                    thisBean.set(property, value);
                }
                catch (Exception e)
                {
                    errors.add(String.format("Unable to set property %s or %s to %s (%s): %s %s", parameterName, property, value, value != null ? value.getClass().getName() : "NULL", e.getClass().getName(), e.getMessage()));
                }
            }
            catch (Exception e)
            {
                errors.add(String.format("Unable to convert property %s or %s to '%s': %s %s",parameterName, property, entry.getValue(), e.getClass().getName(), e.getMessage()));
            }
        }

        if(errors.size() > 0 && isDebug())
        {
            for(final DynaProperty property : dynaClass.getDynaProperties())
                errors.add(String.format("    Available property: %s (%s, %s)", property.getName(), property.getType(), property.getContentType()));
        }
    }

    public String getWorkbookAbsolutePath()
    {
        return new File(workbookName).getAbsolutePath();
    }

    public String getWorkbookNameProvided()
    {
        return workbookName;
    }

    public void setWorkbookName(final String workbookName)
    {
        this.workbookName = workbookName;
        final File workBookFile = new File(workbookName);
        if(! workBookFile.exists() && workBookFile.isFile())
        {
            errors.add(String.format("Workbook file '%s' does not exist.", workbookName));
            return;
        }

        if(! workBookFile.getName().endsWith(".xls"))
        {
            errors.add(String.format("Only Excel 2003 *.xls workbooks are supported.\nUnable to validate '%s'.", workbookName));
            return;
        }

        try
        {
            workbook = WorkbookFactory.create(new FileInputStream(workbookName));
        }
        catch (Exception e)
        {
            errors.add(String.format("Unable to open to '%s'. [%s: %s]", workbookName, e.getClass().getName(), e.getMessage()));
        }
    }

    public String getWorksheetName()
    {
        return worksheetName;
    }

    public void setWorksheetName(final String worksheetName)
    {
        this.worksheetName = worksheetName;
        if(worksheetName != null)
        {
            if(workbook == null)
            {
                errors.add("No workbook available. Please set sheet name after workbook is set.");
                return;
            }

            sheet = workbook.getSheet(worksheetName);
            if(sheet == null)
                errors.add(String.format("Unable to locate worksheet '%s' in workbook '%s'.", worksheetName, workbookName));
        }
        else
            sheet = workbook.getSheetAt(0);
    }

    public Workbook getWorkbook()
    {
        return workbook;
    }

    public Sheet getSheet()
    {
        return sheet == null ? workbook.getSheetAt(0) : sheet;
    }

    public boolean isDebug()
    {
        return debug;
    }

    public void setDebug(final boolean debug)
    {
        this.debug = debug;
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public boolean isValid()
    {
        return errors.size() == 0;
    }

    public int getBudgetYear()
    {
        return budgetYear;
    }

    public void setBudgetYear(final int budgetYear)
    {
        this.budgetYear = budgetYear;
    }

    public String getAgencyCode()
    {
        return agencyCode;
    }

    public void setAgencyCode(final String agencyCode)
    {
        this.agencyCode = agencyCode;
    }

    public List<String> getBureauCodes()
    {
        return bureauCodes;
    }

    public void setBureauCodes(final String bureauCodes)
    {
        this.bureauCodes.clear();
        if(bureauCodes != null)
            this.bureauCodes.addAll(Arrays.asList(bureauCodes.split(",")));
    }

    public boolean isValidateAnySubtotals()
    {
        return validateAnySubtotals;
    }

    public void setValidateAnySubtotals(final boolean validateAnySubtotals)
    {
        this.validateAnySubtotals = validateAnySubtotals;
    }

    public boolean isValidateFundingSourceSubtotals()
    {
        return validateFundingSourceSubtotals;
    }

    public void setValidateFundingSourceSubtotals(final boolean validateFundingSourceSubtotals)
    {
        this.validateFundingSourceSubtotals = validateFundingSourceSubtotals;
    }

    public boolean isValidateInvestmentLineSubtotalWithFundingSourceSubtotals()
    {
        return validateInvestmentLineSubtotalWithFundingSourceSubtotals;
    }

    public void setValidateInvestmentLineSubtotalWithFundingSourceSubtotals(final boolean validateInvestmentLineSubtotalWithFundingSourceSubtotals)
    {
        this.validateInvestmentLineSubtotalWithFundingSourceSubtotals = validateInvestmentLineSubtotalWithFundingSourceSubtotals;
    }

    public boolean isValidateInvestmentsGroupSubtotals()
    {
        return validateInvestmentsGroupSubtotals;
    }

    public void setValidateInvestmentsGroupSubtotals(final boolean validateInvestmentsGroupSubtotals)
    {
        this.validateInvestmentsGroupSubtotals = validateInvestmentsGroupSubtotals;
    }

    public boolean isValidateMissionAreaSubtotals()
    {
        return validateMissionAreaSubtotals;
    }

    public void setValidateMissionAreaSubtotals(final boolean validateMissionAreaSubtotals)
    {
        this.validateMissionAreaSubtotals = validateMissionAreaSubtotals;
    }

    public boolean isValidatePartSubtotals()
    {
        return validatePartSubtotals;
    }

    public void setValidatePartSubtotals(final boolean validatePartSubtotals)
    {
        this.validatePartSubtotals = validatePartSubtotals;
    }

    public boolean isValidatePortfolioTotals()
    {
        return validatePortfolioTotals;
    }

    public void setValidatePortfolioTotals(final boolean validatePortfolioTotals)
    {
        this.validatePortfolioTotals = validatePortfolioTotals;
    }

    public boolean isValidateBudgetAccountsCodesInFundingSources()
    {
        return validateBudgetAccountsCodesInFundingSources;
    }

    public void setValidateBudgetAccountsCodesInFundingSources(final boolean validateBudgetAccountsCodesInFundingSources)
    {
        this.validateBudgetAccountsCodesInFundingSources = validateBudgetAccountsCodesInFundingSources;
    }

    /**
     * Given a text string, return a string that would be suitable for that string to be used
     * as a Java identifier (as a variable or method name). Depending upon whether ucaseInitial
     * is set, the string starts out with a lowercase or uppercase letter. Then, the rule is
     * to convert all periods into underscores and title case any words separated by
     * underscores. This has the effect of removing all underscores and creating mixed case
     * words. For example, Person_Address becomes personAddress or PersonAddress depending upon
     * whether ucaseInitial is set to true or false. Person.Address would become Person_Address.
     */
    public static String parameterNameToJavaIdentifier(final String xml, final boolean ucaseInitial)
    {
        if(xml == null || xml.length() == 0)
            return xml;

        final StringBuffer identifier = new StringBuffer();
        char ch = xml.charAt(0);
        if(Character.isJavaIdentifierStart(ch))
            identifier.append(ucaseInitial ? Character.toUpperCase(ch) : Character.toLowerCase(ch));
        else
        {
            identifier.append('_');
            if(Character.isJavaIdentifierPart(ch))
                identifier.append(ucaseInitial ? Character.toUpperCase(ch) : Character.toLowerCase(ch));
        }

        boolean uCase = false;
        for(int i = 1; i < xml.length(); i++)
        {
            ch = xml.charAt(i);
            if(ch == '.')
            {
                identifier.append('_');
            }
            else if(ch != '_' && Character.isJavaIdentifierPart(ch))
            {
                identifier.append(Character.isUpperCase(ch)
                                  ? ch : (uCase ? Character.toUpperCase(ch) : Character.toLowerCase(ch)));
                uCase = false;
            }
            else
                uCase = true;
        }
        return identifier.toString();
    }

    /**
     * Given a method name, return a string that would be suitable for that string to be used
     * as a parameter name. Basically, what this does is allows something like setAbcDef() to
     * match both "abcDef" and "abc-def" as parameters. It turns a java identifier into
     * a reasonable parameter name.
     *
     * @param javaIdentifier The java identifier that we would like to convert
     * @return A string that represents a string parameter name
     */
    public static String javaIdentifierToParameterName(final String javaIdentifier)
    {
        if(javaIdentifier == null || javaIdentifier.length() == 0)
            return javaIdentifier;

        StringBuffer nodeName = new StringBuffer();
        nodeName.append(javaIdentifier.charAt(0));
        for(int i = 1; i < javaIdentifier.length(); i++)
        {
            //TODO: Might be a good idea to replace _ with - and to lower the case of any uppercase letters
            char ch = javaIdentifier.charAt(i);
            if(Character.isLowerCase(ch))
                nodeName.append(ch);
            else
            {
                nodeName.append('-');
                nodeName.append(ch);
            }
        }

        return nodeName.toString();
    }

    public interface Parameter
    {
        public String getName();
        public boolean isRequired();
        public ParameterArgument getArgument();
    }

    public interface ParameterArgument
    {
        public String getName();
        public boolean isFlag();
        public Object convertValue(final String key, final String value);
    }

    public static class ParameterImpl implements Parameter
    {
        private final String name;
        private final boolean required;
        private final ParameterArgument argument;

        public ParameterImpl(final String name, final ParameterArgument argument, final boolean required)
        {
            this.name = name;
            this.argument = argument;
            this.required = required;
        }

        public ParameterImpl(final String name, final ParameterArgument argument)
        {
            this(name, argument, false);
        }

        public String getName()
        {
            return name;
        }

        public boolean isRequired()
        {
            return required;
        }

        public ParameterArgument getArgument()
        {
            return argument;
        }
    }

    public enum ArgumentType { STRING, INT, DOUBLE, BOOLEAN }

    public static class ParameterArgumentImpl implements ParameterArgument
    {
        private final String name;
        private final ArgumentType type;

        public ParameterArgumentImpl()
        {
            this.name = "(flag)";
            this.type = ArgumentType.BOOLEAN;
        }

        public ParameterArgumentImpl(final String name, final ArgumentType type)
        {
            this.name = name;
            this.type = type;
        }

        public boolean isFlag()
        {
            return type == ArgumentType.BOOLEAN;
        }

        public String getName()
        {
            return name;
        }

        public Object convertValue(final String key, final String value)
        {
            switch(type)
            {
                case STRING:
                    return value;

                case INT:
                    return Integer.parseInt(value);

                case DOUBLE:
                    return Double.parseDouble(value);

                case BOOLEAN:
                    return !key.startsWith(INVERTED_FLAG_PREFIX);
            }

            return null;
        }
    }
}
