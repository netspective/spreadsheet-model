package gov.omb.io.a11.exhibit.ex53;

import gov.omb.data.format.micro.DataFormatFactory;
import gov.omb.data.format.micro.UPI;
import org.netspective.io.spreadsheet.cache.CacheManager;
import org.netspective.io.spreadsheet.cache.ColumnValueCache;
import org.netspective.io.spreadsheet.cache.MultipleRowsForCacheValueException;
import org.netspective.io.spreadsheet.cache.NoRowsForCacheValueException;
import org.netspective.io.spreadsheet.cache.TableRowCache;
import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.outline.DefaultTableOutline;
import org.netspective.io.spreadsheet.outline.DefaultTableOutlineNode;
import org.netspective.io.spreadsheet.outline.TableOutline;
import org.netspective.io.spreadsheet.outline.TableOutlineCreator;
import org.netspective.io.spreadsheet.outline.TableOutlineNode;
import org.netspective.io.spreadsheet.template.Column;
import org.netspective.io.spreadsheet.template.ColumnGroup;
import org.netspective.io.spreadsheet.template.DefaultColumn;
import org.netspective.io.spreadsheet.template.DefaultColumnGroup;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;
import org.netspective.io.spreadsheet.util.Util;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.validate.cell.CellValidationMessage;
import org.netspective.io.spreadsheet.validate.cell.CellValidationRule;
import org.netspective.io.spreadsheet.validate.cell.DataRequiredRule;
import org.netspective.io.spreadsheet.validate.cell.IntegerEnumerationRule;
import org.netspective.io.spreadsheet.validate.cell.NumericRangeRule;
import org.netspective.io.spreadsheet.validate.cell.TextLengthRule;
import org.netspective.io.spreadsheet.validate.cell.TextRegExRule;
import org.netspective.io.spreadsheet.validate.outline.DefaultOutlineValidationMessage;
import org.netspective.io.spreadsheet.validate.outline.NodeValidationMessage;
import org.netspective.io.spreadsheet.validate.outline.NodeValidationRule;
import org.netspective.io.spreadsheet.validate.outline.OutlineValidationMessage;
import org.netspective.io.spreadsheet.validate.outline.ValidateNodeColumnData;
import org.netspective.io.spreadsheet.validate.row.RowValidationRule;
import org.netspective.io.spreadsheet.validate.row.ValidateColumnData;
import org.netspective.io.spreadsheet.validate.template.TemplateValidationRule;
import org.netspective.io.spreadsheet.validate.template.ValidateColumnGroupNamesRule;
import org.netspective.io.spreadsheet.validate.template.ValidateColumnHeadingsRule;
import org.netspective.io.spreadsheet.value.DoubleValueHandler;
import org.netspective.io.spreadsheet.value.IntegerArrayValueHandler;
import org.netspective.io.spreadsheet.value.StringArrayValueHandler;
import org.netspective.io.spreadsheet.value.StringValueHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Exhibit53WorksheetTemplate implements TableOutlineCreator, WorksheetTemplate, CacheManager, ValidationContext
{
    private final int budgetYear;
    private final String agencyCode;
    private final List<String> bureauCodes;
    private final List<ColumnGroup> columnGroups = new ArrayList<ColumnGroup>();
    private final List<Column> columns = new ArrayList<Column>();
    private final Map<Integer, Column> columnsMapByIndex = new HashMap<Integer, Column>();
    private final List<TableRowCache> rowCaches = new ArrayList<TableRowCache>();
    private final List<TemplateValidationRule> templateValidationRules = new ArrayList<TemplateValidationRule>();
    private final List<RowValidationRule> rowValidationRules = new ArrayList<RowValidationRule>();

    private final Column activeUPIColumn;
    private final PortfolioSectionsCache portfolioSectionsCacheDefn = new PortfolioSectionsCache();
    private final Column investmentTitleColumn;
    private final Column descrColumn;

    private final InvestmentNodeRule investmentNodeRule;

    public class PercentageRule implements CellValidationRule
    {
        private final String messageCode;
        private final NumericRangeRule rangeRule;

        public PercentageRule(String messageCode)
        {
            this.messageCode = messageCode;
            this.rangeRule = new NumericRangeRule(messageCode, 0.00d, 100.00d, "Found percentage value of %2$3.2f in %1$s, expected it to be between %3$3.2f and %4$3.2f.", "Found invalid percentage value of %2$s in %1$s, expected it to be between %3$3.2f and %4$3.2f.");
        }

        public boolean isValid(final ValidationContext vc, final Table table, final TableRow row, final TableCell cell, final List<CellValidationMessage> messages)
        {
            return rangeRule.isValid(vc, table, row, cell, messages);
        }
    }

    public class InvestmentNodeRule implements NodeValidationRule
    {
        private final NodeValidationRule delegateRule;

        public InvestmentNodeRule()
        {
            final TextRegExRule descrTruncatedRule = new TextRegExRule(MessageCodeFactory.DESCR_TRUNCATED, ".+[\\.\\,\\!\\?\\(\\)\\{\\}\\[\\]\\<\\>\\'\\\"]$", "An investment description value seems to be truncated at %s. Please ensure that it is properly summarized, and not simply truncated (should end with proper sentence punctuation like '%3$s').");

            final Map<Column, CellValidationRule[]> cellValidationRules = new HashMap<Column, CellValidationRule[]>();
            cellValidationRules.put(descrColumn, new CellValidationRule[] { descrTruncatedRule });

            delegateRule = new ValidateNodeColumnData(cellValidationRules);
        }

        public boolean isValid(final ValidationContext vc, final TableOutline outline, final TableOutlineNode node,
                               final List<NodeValidationMessage> messages)
        {
            final boolean valid = delegateRule.isValid(vc, outline, node, messages);            
            System.out.printf("Valid: %s - %s", valid, messages);
            return valid;
        }
    }

    public Exhibit53WorksheetTemplate(final int budgetYear, final String agencyCode, final List<String> bureauCodes)
    {
        final StringValueHandler stringValueHandler = new StringValueHandler();
        final DoubleValueHandler doubleValueHandler = new DoubleValueHandler();
        final IntegerArrayValueHandler intArrayValueHandler = new IntegerArrayValueHandler(new StringArrayValueHandler(stringValueHandler, ","));

        this.budgetYear = budgetYear;
        this.agencyCode = agencyCode;
        this.bureauCodes = bureauCodes;

        final int groupNamesRowNumber = 7;
        final int columnHeadingsRowNumber = 8;

        final UpiRule upiRule = new UpiRule(MessageCodeFactory.UPI_INVALID, budgetYear, agencyCode, bureauCodes);

        CellValidationRule[] columnValidations = new CellValidationRule[] { upiRule };
        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 2, "2010 UPI (17-digits required for all legacy investments)", columnValidations));

        columnValidations= new CellValidationRule[] { new DataRequiredRule(MessageCodeFactory.UPI_REQUIRED, "No UPI provided in %s."), upiRule };
        activeUPIColumn = new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 3, "2011 UPI (17-digits required for all)", columnValidations);
        columns.add(activeUPIColumn);

        columnValidations = new CellValidationRule[] { new DataRequiredRule(MessageCodeFactory.TITLE_REQUIRED, "No investment title supplied in %s."), new TextLengthRule(MessageCodeFactory.TITLE_LENGTH, 255, "Investment title in %1$s is %3$d characters long. It must be between %4$d and %5$d characters.") };
        investmentTitleColumn = new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 4, "Investment Title", columnValidations);
        columns.add(investmentTitleColumn);
        rowCaches.add(new ColumnValueCache(activeUPIColumn));

        columnValidations = new CellValidationRule[] { new TextLengthRule(MessageCodeFactory.DESCR_LENGTH, 255, "Investment description in %1$s is %3$d characters long. It must be between %4$d and %5$d characters.") };
        descrColumn = new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 5, "Investment Description (limited to 255 characters)", columnValidations);
        columns.add(descrColumn);

        final ColumnGroup primaryFEAMapping = new DefaultColumnGroup(groupNamesRowNumber, "Primary FEA Mapping (BRM)", 6, 7);
        columnGroups.add(primaryFEAMapping);
        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 6, "Line of Business (3 digit code)", primaryFEAMapping));
        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 7, "Sub-Function (3 digit code)", primaryFEAMapping));

        columns.add(new DefaultColumn(doubleValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 8, "Core Financial System (%)", new CellValidationRule[] { new PercentageRule(MessageCodeFactory.FINPCT_INVALID) } ));

        final ColumnGroup hspd12 = new DefaultColumnGroup(groupNamesRowNumber, "HSPD-12 ($M)", 9, 9);
        columnGroups.add(hspd12);
        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 9, "PY", hspd12));

        final ColumnGroup homelandSecurity = new DefaultColumnGroup(groupNamesRowNumber, "Homeland Security", 10, 10);
        columnGroups.add(homelandSecurity);

        final Set<Integer> validHSPriorities = new HashSet<Integer>();
        for(int i = 1; i <= 6; i++) validHSPriorities.add(i);
        columnValidations = new CellValidationRule[] { new IntegerEnumerationRule(MessageCodeFactory.HS_INVALID_PRIORITY, validHSPriorities) };
        columns.add(new DefaultColumn(intArrayValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,10, "Priority Identifier (Select all that apply)", columnValidations, homelandSecurity));

        final ColumnGroup dme = new DefaultColumnGroup(groupNamesRowNumber, "DME ($M)", 11, 13);
        columnGroups.add(dme);
        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,11, "PY", dme));
        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,12, "CY", dme));
        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,13, "BY", dme));

        final ColumnGroup steadyState = new DefaultColumnGroup(groupNamesRowNumber, "Steady State ($M)", 14, 15);
        columnGroups.add(steadyState);
        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,14, "PY", steadyState));
        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,15, "CY", steadyState));
        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,16, "BY", steadyState));

        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,17, "Segment Architecture (6 digit code)"));

        for(final Column column : columns)
            columnsMapByIndex.put(column.getColumnIndex(), column);

        templateValidationRules.add(new ValidateColumnGroupNamesRule());
        templateValidationRules.add(new ValidateColumnHeadingsRule());
        rowValidationRules.add(new ValidateColumnData());

        rowCaches.add(portfolioSectionsCacheDefn);

        investmentNodeRule = new InvestmentNodeRule();
    }

    public ValidationContext createValidationContext()
    {
        return this;
    }

    public String getValidationMessageRowSummary(final TableRow row, final CellValidationMessage[] cellMessages)
    {
        return String.format("Row %d: %d error(s).", row.getRowNumberInSheet(), cellMessages.length);
    }

    public String getValidationMessageCellLocator(final TableCell cell, final boolean useInFormatSpec)
    {
        final String cellLocation = Util.getCellLocator(cell.getCell());
        final String columnName = cell.getColumn().getQualifiedColumnName();
        final String filteredName = useInFormatSpec ? columnName.replace("%", "%%") : columnName;
        return String.format("%s (\"%s\")", cellLocation, filteredName);
    }

    public void cache(final Table table)
    {
        for(final TableRow row : table.getRows())
            for(final TableRowCache rowCache : rowCaches)
                rowCache.cache(row);
    }

    public TableRow findUniqueRow(final Table table, final TableRowCache cache, final String cacheValue, final List<OutlineValidationMessage> messages)
    {
        try
        {
            return cache.findUniqueTableRow(cacheValue);
        }
        catch (final Exception e)
        {
            messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_NOT_FOUND, "Required UPI %s not found anywhere in the sheet.", cacheValue));
            return null;
        }
    }

    public String getAgencyCode()
    {
        return agencyCode;
    }

    public List<String> getBureauCode()
    {
        return bureauCodes;
    }

    protected List<TableOutlineNode> createInvestmentTypeStructure(final Table table, final String validateSectionId,
                                                                     final String validateMissionAreaId,
                                                                     final int groupFirstDataRowIndex,
                                                                     final int groupLastDataRowIndex,
                                                                     final List<OutlineValidationMessage> messages)
    {
        final List<TableRow> allTableRows = table.getRows();
        int errors = 0;
        final List<TableRow> investmentRows = new ArrayList<TableRow>();
        final List<List<TableOutlineNode>> investmentChildNodes = new ArrayList<List<TableOutlineNode>>();

        String activeInvestment = "";
        List<TableOutlineNode> activeInvestmentChildNodes = null;

        for(int rowIndex = groupFirstDataRowIndex; rowIndex <= groupLastDataRowIndex; rowIndex++)
        {
            final TableRow investmentDataRow = allTableRows.get(rowIndex);
            final String upiText = (String) investmentDataRow.findCellForColumn(activeUPIColumn).getValue("");
            final UPI upi = DataFormatFactory.getInstance().createUPI(budgetYear, upiText, agencyCode);

            if(! upi.isValid())
            {
                String[] upiIssues = upi.getIssues();
                errors += upiIssues.length;
                messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_INVALID, "Unable to resolve investment type on row %d because UPI is invalid.", investmentDataRow.getRowNumberInSheet()));
                continue;
            }

            if(validateSectionId != null && ! upi.getExhibit53PartIdentifier().equals(validateSectionId))
            {
                errors++;
                messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_PORTFOLIO_PART_INVALID, "Unable to resolve investment type on row %d: part code '%s' should be '%s'.", investmentDataRow.getRowNumberInSheet(), upi.getExhibit53PartIdentifier(), validateSectionId));
                continue;
            }

            if(validateMissionAreaId != null && ! upi.getMissionAreaIdentifier().equals(validateMissionAreaId))
            {
                errors++;
                messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_MISSION_AREA_INVALID, "Unable to resolve investment type on row %d: mission area '%s' should be '%s'.", investmentDataRow.getRowNumberInSheet(), upi.getMissionAreaIdentifier(), validateMissionAreaId));
                continue;
            }

            final String thisRowInvestmentID = String.format("%s-%s", upi.getInvestmentTypeIdentifier(), upi.getInvestmentIdentificationNumber());
            if(activeInvestment.equals(thisRowInvestmentID))
                activeInvestmentChildNodes.add(new DefaultTableOutlineNode(investmentDataRow, rowIndex, rowIndex));
            else
            {
                activeInvestment = thisRowInvestmentID;
                activeInvestmentChildNodes = new ArrayList<TableOutlineNode>();
                investmentRows.add(investmentDataRow);
                investmentChildNodes.add(activeInvestmentChildNodes);
            }
        }

        final List<TableOutlineNode> investmentTypeNodes = new ArrayList<TableOutlineNode>();
        if(errors == 0)
        {
            for(int i = 0; i < investmentRows.size(); i++)
            {
                final boolean isLast = i == investmentRows.size() - 1;
                final TableRow investmentTypeStartRow = investmentRows.get(i);
                final int invLinesFirstDataRowIndex = allTableRows.indexOf(investmentTypeStartRow) + 1;
                final int invLinesLastDataRowIndex = (isLast ? groupLastDataRowIndex : allTableRows.indexOf(investmentRows.get(i+1))) - 1;
                final List<TableOutlineNode> investmentLineNodes = investmentChildNodes.get(i);
                investmentTypeNodes.add(new DefaultTableOutlineNode(investmentTypeStartRow, invLinesFirstDataRowIndex, invLinesLastDataRowIndex, investmentLineNodes, new NodeValidationRule[] { investmentNodeRule }));
            }
        }
        return investmentTypeNodes;
    }

    protected List<TableOutlineNode> createMissionAreaStructure(final Table table, final String validateSectionId,
                                                                  final int missionAreaFirstDataRowIndex,
                                                                  final int missionAreaLastDataRowIndex,
                                                                  final List<OutlineValidationMessage> messages)
    {
        final List<TableRow> allTableRows = table.getRows();
        int errors = 0;
        String activeMissionArea = "";
        final List<TableRow> missionAreaRows = new ArrayList<TableRow>();
        final List<String> missionAreaIds = new ArrayList<String>();
        for(int rowIndex = missionAreaFirstDataRowIndex; rowIndex <= missionAreaLastDataRowIndex; rowIndex++)
        {
            final TableRow sectionDataRow = allTableRows.get(rowIndex);
            final String upiText = (String) sectionDataRow.findCellForColumn(activeUPIColumn).getValue("");
            final UPI upi = DataFormatFactory.getInstance().createUPI(budgetYear, upiText, agencyCode);

            if(! upi.isValid())
            {
                String[] upiIssues = upi.getIssues();
                errors += upiIssues.length;
                messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_INVALID, "Unable to resolve mission area on row %d because UPI is invalid.", sectionDataRow.getRowNumberInSheet()));
                continue;
            }

            if(validateSectionId != null && ! upi.getExhibit53PartIdentifier().equals(validateSectionId))
            {
                errors++;
                messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_PORTFOLIO_PART_INVALID, "Unable to resolve mission area on row %d: part code '%s' should be '%s'.", sectionDataRow.getRowNumberInSheet(), upi.getExhibit53PartIdentifier(), validateSectionId));
                continue;
            }

            if(! activeMissionArea.equals(upi.getMissionAreaIdentifier()))
            {
                activeMissionArea = upi.getMissionAreaIdentifier();
                missionAreaIds.add(activeMissionArea);
                missionAreaRows.add(sectionDataRow);
            }
        }

        final List<TableOutlineNode> missionAreaNodes = new ArrayList<TableOutlineNode>();
        if(errors == 0)
        {
            for(int i = 0; i < missionAreaRows.size(); i++)
            {
                final boolean isLast = i == missionAreaRows.size() - 1;
                final TableRow missionAreaStartRow = missionAreaRows.get(i);
                final int investmentFirstDataRowIndex = allTableRows.indexOf(missionAreaStartRow) + 1;
                final int investmentLastDataRowIndex = (isLast ? missionAreaLastDataRowIndex : allTableRows.indexOf(missionAreaRows.get(i+1))) - 1;
                final List<TableOutlineNode> investmentTypeNodes = createInvestmentTypeStructure(table, validateSectionId, missionAreaIds.get(i), investmentFirstDataRowIndex, investmentLastDataRowIndex, messages);
                missionAreaNodes.add(new DefaultTableOutlineNode(missionAreaStartRow, investmentFirstDataRowIndex, investmentLastDataRowIndex, investmentTypeNodes));
            }
        }
        return missionAreaNodes;
    }

    protected List<TableOutlineNode> createSectionStructure(final Table table, final List<OutlineValidationMessage> messages)
    {
        final String[] parts = new String[]
        {
                "Section 01",
                "Section 02",
                "Section 03",
                "Section 04",
                "Section 05",
                "Section 06",
        };

        int errors = 0;
        final List<TableRow> portfolioSectionRows = new ArrayList<TableRow>();
        for(final String part : parts)
        {
            final TableRow sectionContainer = findUniqueRow(table, portfolioSectionsCacheDefn, part, messages);
            if(sectionContainer == null)
                errors++;
            else
                portfolioSectionRows.add(sectionContainer);
        }

        final List<TableOutlineNode> portfolioSectionNodes = new ArrayList<TableOutlineNode>();
        if(errors == 0)
        {
            final List<TableRow> allTableRows = table.getRows();
            for(int sectionNum = 0; sectionNum < portfolioSectionRows.size(); sectionNum++)
            {
                final boolean isLast = sectionNum == portfolioSectionRows.size() - 1;
                final TableRow sectionStartRow = portfolioSectionRows.get(sectionNum);
                final int firstDataRowIndex = allTableRows.indexOf(sectionStartRow) + 1;
                final int lastDataRowIndex = (isLast ? allTableRows.size() : allTableRows.indexOf(portfolioSectionRows.get(sectionNum+1))) - 1;
                if(sectionNum == 0)
                {
                    final List<TableOutlineNode> missionAreaNodes = createMissionAreaStructure(table, String.format("%02d", sectionNum+1), firstDataRowIndex, lastDataRowIndex, messages);
                    portfolioSectionNodes.add(new DefaultTableOutlineNode(sectionStartRow, firstDataRowIndex, lastDataRowIndex, missionAreaNodes));
                }
                else
                {
                    final List<TableOutlineNode> investmentTypeNodes = createInvestmentTypeStructure(table, String.format("%02d", sectionNum+1), null, firstDataRowIndex, lastDataRowIndex, messages);
                    portfolioSectionNodes.add(new DefaultTableOutlineNode(sectionStartRow, firstDataRowIndex, lastDataRowIndex, investmentTypeNodes));
                }
            }
        }
        return portfolioSectionNodes;
    }

    public TableOutline createOutline(final Table table, final List<OutlineValidationMessage> messages)
    {
        final List<TableRow> allTableRows = table.getRows();

        final TableRow portfolioRow = findUniqueRow(table, portfolioSectionsCacheDefn, PortfolioSectionsCache.AGENCY_TOTAL_IT_INVESTMENT_PORTFOLIO, messages);
        if(portfolioRow == null)
            return new DefaultTableOutline(table, new ArrayList<TableOutlineNode>());

        final List<TableOutlineNode> rootNodes = new ArrayList<TableOutlineNode>();
        final List<TableOutlineNode> portfolioSectionNodes = createSectionStructure(table, messages);
        rootNodes.add(new DefaultTableOutlineNode(portfolioRow, allTableRows.indexOf(portfolioRow) + 1, allTableRows.size()-1, portfolioSectionNodes));
        return new DefaultTableOutline(table, rootNodes);
    }

    public List<ColumnGroup> getColumnGroups()
    {
        return Collections.unmodifiableList(columnGroups);
    }

    public boolean hasColumnGroups()
    {
        return true;
    }

    public List<Column> getColumns()
    {
        return Collections.unmodifiableList(columns);
    }

    public Map<Integer, Column> getColumnsMapByIndex()
    {
        return Collections.unmodifiableMap(columnsMapByIndex);
    }

    public List<TemplateValidationRule> getTemplateValidationRules()
    {
        return Collections.unmodifiableList(templateValidationRules);
    }

    public List<RowValidationRule> getRowValidationRules()
    {
        return Collections.unmodifiableList(rowValidationRules);
    }

    public boolean isWarning(final Message message)
    {
        return message.getCode().startsWith("W-");
    }

    public Column getInvestmentTitleColumn()
    {
        return investmentTitleColumn;
    }

    public class PortfolioSectionsCache implements TableRowCache
    {
        private static final String AGENCY_TOTAL_IT_INVESTMENT_PORTFOLIO = "Agency Total IT Investment Portfolio";
        private static final String SECTION_IDENTIFIER = "Section";

        private final Pattern totalItInvestmentPattern = Pattern.compile("\\d\\d\\d-\\d\\d-00-00-00-0000-00");
        private final Pattern sectionIdentifierPattern = Pattern.compile("\\d\\d\\d-\\d\\d-(0[1-6])-00-00-0000-00");

        private final Map<String, List<TableRow>> rowCache = new HashMap<String, List<TableRow>>();

        public PortfolioSectionsCache()
        {
        }

        public String getCacheName()
        {
            return PortfolioSectionsCache.class.getName();
        }

        public void cache(final TableRow row)
        {
            final String upiText = (String) row.findCellForColumn(activeUPIColumn).getValue(null);
            if(upiText == null)
                return;

            final String cacheValue;

            final Matcher totalITInvestmentMatcher = totalItInvestmentPattern.matcher(upiText);
            if(totalITInvestmentMatcher.matches())
                cacheValue = AGENCY_TOTAL_IT_INVESTMENT_PORTFOLIO;
            else
            {
                final Matcher sectionIdMatcher = sectionIdentifierPattern.matcher(upiText);
                if(sectionIdMatcher.matches())
                    cacheValue = String.format("%s %s", SECTION_IDENTIFIER, sectionIdMatcher.group(1));
                else
                    cacheValue = null;
            }

            if(cacheValue != null)
            {
                List<TableRow> rowsForPortfolioSection = rowCache.get(cacheValue);
                if(rowsForPortfolioSection == null)
                {
                    rowsForPortfolioSection = new ArrayList<TableRow>();
                    rowCache.put(cacheValue, rowsForPortfolioSection);
                }
                rowsForPortfolioSection.add(row);
            }
        }

        public String createValueNotFoundErrorMessage(final String value)
        {
            return String.format("Unable to find portfolio row '%s'.", value);
        }

        public String createMultipleValuesFoundErrorMessage(final String value, final List<TableRow> tableRowsFound)
        {
            return String.format("Found %d rows for portfolio row '%s'. Only one expected.", tableRowsFound.size(), value);
        }

        public TableRow findUniqueTableRow(final String value) throws NoRowsForCacheValueException, MultipleRowsForCacheValueException
        {
            final List<TableRow> foundRows = findAllTableRows(value);
            if(foundRows == null)
                throw new NoRowsForCacheValueException(this, value);
            if(foundRows.size() != 1)
                throw new MultipleRowsForCacheValueException(this, value, foundRows);
            return foundRows.get(0);
        }

        public List<TableRow> findAllTableRows(final String value)
        {
            return rowCache.get(value);
        }

    }

}
