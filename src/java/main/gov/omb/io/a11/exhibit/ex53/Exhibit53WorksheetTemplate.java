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
import org.netspective.io.spreadsheet.validate.cell.DefaultCellValidationMessage;
import org.netspective.io.spreadsheet.validate.cell.IntegerEnumerationRule;
import org.netspective.io.spreadsheet.validate.cell.NumericRangeRule;
import org.netspective.io.spreadsheet.validate.cell.TextEnumerationRule;
import org.netspective.io.spreadsheet.validate.cell.TextLengthRule;
import org.netspective.io.spreadsheet.validate.cell.TextRegExRule;
import org.netspective.io.spreadsheet.validate.outline.DefaultOutlineValidationMessage;
import org.netspective.io.spreadsheet.validate.outline.NodeValidationMessage;
import org.netspective.io.spreadsheet.validate.outline.OutlineValidationMessage;
import org.netspective.io.spreadsheet.validate.outline.TableOutlineNodeColumnsValidator;
import org.netspective.io.spreadsheet.validate.row.RowValidationRule;
import org.netspective.io.spreadsheet.validate.row.ValidateColumnData;
import org.netspective.io.spreadsheet.validate.template.TemplateValidationRule;
import org.netspective.io.spreadsheet.validate.template.ValidateColumnGroupNamesRule;
import org.netspective.io.spreadsheet.validate.template.ValidateColumnHeadingsRule;
import org.netspective.io.spreadsheet.value.BigDecimalValueHandler;
import org.netspective.io.spreadsheet.value.DoubleValueHandler;
import org.netspective.io.spreadsheet.value.IntegerArrayValueHandler;
import org.netspective.io.spreadsheet.value.StringArrayValueHandler;
import org.netspective.io.spreadsheet.value.StringValueHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static final String VERSION = "@VERSION@";
    public static final int DOLLAR_VALUE_BIG_DECIMAL_SCALE = 6;

    private final Exhibit53Parameters parameters;
    private final List<ColumnGroup> columnGroups = new ArrayList<ColumnGroup>();
    private final List<Column> columns = new ArrayList<Column>();
    private final Map<Integer, Column> columnsMapByIndex = new HashMap<Integer, Column>();
    private final List<TableRowCache> rowCaches = new ArrayList<TableRowCache>();
    private final List<TemplateValidationRule> templateValidationRules = new ArrayList<TemplateValidationRule>();
    private final List<RowValidationRule> rowValidationRules = new ArrayList<RowValidationRule>();

    private final PortfolioSectionsCache portfolioSectionsCache = new PortfolioSectionsCache();

    private final Column activeUPIColumn;
    private final Column investmentTitleColumn;
    private final Column descrColumn;
    private final Column homelandSecurityPrioritiesColumn;
    private final Column finSystemPctColumn;
    private final Column segArchColumn;
    private final Column feaLobColumn;
    private final Column feaSubFColumn;
    private final List<Column> invAmtsRollupColumns;

    public class PercentageRule implements CellValidationRule
    {
        private final NumericRangeRule rangeRule;

        public PercentageRule(final String messageCode)
        {
            this.rangeRule = new NumericRangeRule(messageCode, 0.00d, 100.00d, "Found percentage value of %2$3.2f in %1$s, expected it to be between %3$3.2f and %4$3.2f.", "Found invalid percentage value of %2$s in %1$s, expected it to be between %3$3.2f and %4$3.2f.");
        }

        public boolean isValid(final ValidationContext vc, final Table table, final TableRow row, final TableCell cell, final List<CellValidationMessage> messages)
        {
            return rangeRule.isValid(vc, table, row, cell, messages);
        }
    }

    public Exhibit53WorksheetTemplate(final Exhibit53Parameters parameters)
    {
        this.parameters = parameters;

        final StringValueHandler stringValueHandler = new StringValueHandler();
        final BigDecimalValueHandler dollarValueHandler = new BigDecimalValueHandler(DOLLAR_VALUE_BIG_DECIMAL_SCALE);
        final DoubleValueHandler doubleValueHandler = new DoubleValueHandler();
        final IntegerArrayValueHandler intArrayValueHandler = new IntegerArrayValueHandler(new StringArrayValueHandler(stringValueHandler, ","));

        final int groupNamesRowNumber = 7;
        final int columnHeadingsRowNumber = 8;

        final UpiRule upiRule = new UpiRule(MessageCodeFactory.UPI_INVALID, parameters.getBudgetYear(), parameters);

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
        feaLobColumn = new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 6, "Line of Business (3 digit code)", primaryFEAMapping);
        feaSubFColumn = new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 7, "Sub-Function (3 digit code)", primaryFEAMapping);
        columns.add(feaLobColumn);
        columns.add(feaSubFColumn);

        finSystemPctColumn = new DefaultColumn(doubleValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 8, "Core Financial System (%)");
        columns.add(finSystemPctColumn);

        final ColumnGroup hspd12 = new DefaultColumnGroup(groupNamesRowNumber, "HSPD-12 ($M)", 9, 9);
        columnGroups.add(hspd12);
        columns.add(new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber, 9, "PY", hspd12));

        final ColumnGroup homelandSecurity = new DefaultColumnGroup(groupNamesRowNumber, "Homeland Security", 10, 10);
        columnGroups.add(homelandSecurity);
        homelandSecurityPrioritiesColumn = new DefaultColumn(intArrayValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,10, "Priority Identifier (Select all that apply)", homelandSecurity);
        columns.add(homelandSecurityPrioritiesColumn);

        invAmtsRollupColumns = new ArrayList<Column>();

        final ColumnGroup dme = new DefaultColumnGroup(groupNamesRowNumber, "DME ($M)", 11, 13);
        columnGroups.add(dme);
        invAmtsRollupColumns.add(new DefaultColumn(dollarValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,11, "PY", dme));
        invAmtsRollupColumns.add(new DefaultColumn(dollarValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,12, "CY", dme));
        invAmtsRollupColumns.add(new DefaultColumn(dollarValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,13, "BY", dme));

        final ColumnGroup steadyState = new DefaultColumnGroup(groupNamesRowNumber, "Steady State ($M)", 14, 15);
        columnGroups.add(steadyState);
        invAmtsRollupColumns.add(new DefaultColumn(dollarValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,14, "PY", steadyState));
        invAmtsRollupColumns.add(new DefaultColumn(dollarValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,15, "CY", steadyState));
        invAmtsRollupColumns.add(new DefaultColumn(dollarValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,16, "BY", steadyState));

        columns.addAll(invAmtsRollupColumns);

        segArchColumn = new DefaultColumn(stringValueHandler, groupNamesRowNumber, columnHeadingsRowNumber,17, "Segment Architecture (6 digit code)");
        columns.add(segArchColumn);

        for(final Column column : columns)
            columnsMapByIndex.put(column.getColumnIndex(), column);

        templateValidationRules.add(new ValidateColumnGroupNamesRule(MessageCodeFactory.TEMPLATE_INVALID_COLUMN_GROUP_NAME));
        templateValidationRules.add(new ValidateColumnHeadingsRule(MessageCodeFactory.TEMPLATE_INVALID_COLUMN_HEADING));
        rowValidationRules.add(new ValidateColumnData(MessageCodeFactory.TEMPLATE_INVALID_COLUMN));

        rowCaches.add(portfolioSectionsCache);
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

    public TableOutline createOutline(final Table table, final List<OutlineValidationMessage> messages)
    {
        return new Exhibit53(table, messages);
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
        return parameters.isWarning(message, message.getCode().startsWith("W-"));
    }

    public Column getActiveUPIColumn()
    {
        return activeUPIColumn;
    }

    public Column getInvestmentTitleColumn()
    {
        return investmentTitleColumn;
    }

    public Column getDescrColumn()
    {
        return descrColumn;
    }

    public Column getHomelandSecurityPrioritiesColumn()
    {
        return homelandSecurityPrioritiesColumn;
    }

    public Column getFinSystemPctColumn()
    {
        return finSystemPctColumn;
    }

    public Column getSegArchColumn()
    {
        return segArchColumn;
    }

    public Column getFeaLobColumn()
    {
        return feaLobColumn;
    }

    public Column getFeaSubFColumn()
    {
        return feaSubFColumn;
    }

    public List<Column> getInvAmtsRollupColumns()
    {
        return invAmtsRollupColumns;
    }

    public Exhibit53Parameters getParameters()
    {
        return parameters;
    }

    public class PortfolioSectionsCache implements TableRowCache
    {
        private static final String AGENCY_TOTAL_IT_INVESTMENT_PORTFOLIO = "Agency Total IT Investment Portfolio";
        private static final String SECTION_IDENTIFIER = "Section";

        private final Pattern totalItInvestmentPattern = Pattern.compile("[0-9]{3}-[0-9]{2}-00-00-00-0000-00");
        private final Pattern sectionIdentifierPattern = Pattern.compile("[0-9]{3}-[0-9]{2}-(0[1-6])-00-00-0000-00");

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

    public class ColumnSubtotalsValidator
    {
        private final String nodeMessageCode;
        private final String cellMessageCode;
        private final TableOutline outline;
        private final List<Column> columnsToSubtotal;
        private final List<TableOutlineNode> nodesToCompare;
        private final TableOutlineNode compareTo;

        public ColumnSubtotalsValidator(final String nodeMessageCode, final String cellMessageCode,
                                        final TableOutline outline, final List<Column> columnsToSubtotal,
                                        final List<TableOutlineNode> nodesToCompare, final TableOutlineNode compareTo)
        {
            this.nodeMessageCode = nodeMessageCode;
            this.cellMessageCode = cellMessageCode;
            this.outline = outline;
            this.columnsToSubtotal = columnsToSubtotal;
            this.nodesToCompare = nodesToCompare;
            this.compareTo = compareTo;
        }

        public boolean isValid(final ValidationContext vc, final List<NodeValidationMessage> messages)
        {
            final Map<Column, BigDecimal> sums = new HashMap<Column, BigDecimal>();
            final BigDecimal zero = new BigDecimal("0");
            for(final Column c : columnsToSubtotal)
                sums.put(c, zero);

            final List<Integer> rowsSummed = new ArrayList<Integer>();
            for(final TableOutlineNode node : nodesToCompare)
            {
                final TableRow lineRow = node.getTableRow();
                rowsSummed.add(lineRow.getRowNumberInSheet());
                for(final Column c : columnsToSubtotal)
                {
                    final BigDecimal currentSum = sums.get(c);
                    final TableCell cell = lineRow.findCellForColumn(c);
                    sums.put(c, currentSum.add((BigDecimal) cell.getValue(zero)));
                }
            }

            final List<CellValidationMessage> cellMessages = new ArrayList<CellValidationMessage>();
            for(final Column c : columnsToSubtotal)
            {
                final BigDecimal subtotalComputed = sums.get(c);
                final TableCell cell = compareTo.getTableRow().findCellForColumn(c);
                final BigDecimal subtotalGiven = (BigDecimal) cell.getValue(zero);
                final BigDecimal difference = subtotalGiven.subtract(subtotalComputed);
                if(difference.abs().compareTo(zero) > 0)
                {
                    cellMessages.add(new DefaultCellValidationMessage(outline.getTable(), compareTo.getTableRow(), cell, cellMessageCode,
                            "In %s the subtotal computed %3.6f doesn't match the provided subtotal %3.6f for rows %s. The difference of given minus computed is %3.6f.",
                            vc.getValidationMessageCellLocator(cell, false), subtotalComputed,
                            subtotalGiven, rowsSummed, difference));
                }
            }

            if(cellMessages.size() > 0)
            {
                messages.add(new NodeValidationMessage()
                {
                    public TableOutline getOutline() { return outline; }
                    public TableOutlineNode getNode() { return compareTo; }
                    public TableRow getRow() { return compareTo.getTableRow(); }
                    public CellValidationMessage[] getCellValidationErrors() { return cellMessages.toArray(new CellValidationMessage[cellMessages.size()]); }
                    public String getCode() { return nodeMessageCode; }
                    public String getMessage() { return String.format("Values for row %d (\"%s\") do not sum properly.", getRow().getRowNumberInSheet(), getRow().findCellForColumn(investmentTitleColumn).getValue()); }
                });
                return false;
            }

            return true;
        }
    }

    public interface Exhibit53Part extends TableOutlineNode
    {
        public String getPart();
    }

    public class Exhibit53 implements TableOutline
    {
        private final Table table;
        private final List<TableOutlineNode> rootNodes = new ArrayList<TableOutlineNode>();
        private final List<OutlineValidationMessage> messages;
        private final Map<String, List<TableRow>> uniqueInvestmentIds = new HashMap<String, List<TableRow>>();
        private final Portfolio portfolio;

        public Exhibit53(final Table table, final List<OutlineValidationMessage> messages)
        {
            this.table = table;
            this.messages = messages;

            final TableRow portfolioRow = findUniqueRow(table, portfolioSectionsCache, PortfolioSectionsCache.AGENCY_TOTAL_IT_INVESTMENT_PORTFOLIO, messages);
            if(portfolioRow == null)
            {
                portfolio = null;
                return;
            }

            portfolio = new Portfolio(portfolioRow);
            rootNodes.add(portfolio);
        }

        public Portfolio getPortfolio()
        {
            return portfolio;
        }

        public Table getTable()
        {
            return table;
        }

        public List<TableOutlineNode> getRootNodes()
        {
            return rootNodes;
        }

        public boolean isValid(final ValidationContext vc, final List<NodeValidationMessage> messages)
        {
            int errors = 0;
             for(final TableOutlineNode child : getRootNodes())
                 errors += child.isValid(vc, messages) ? 0 : 1;

             if(parameters.isValidateAnySubtotals() && parameters.isValidatePortfolioTotals())
             {
                 final TableOutlineNode portfolio = rootNodes.get(0);
                 final ColumnSubtotalsValidator csv = new ColumnSubtotalsValidator(
                         MessageCodeFactory.PORTFOLIO_ROW_SUBTOTAL_INVALID,
                         MessageCodeFactory.PORTFOLIO_ROW_COLUMN_SUBTOTAL_INVALID,
                         Exhibit53.this, invAmtsRollupColumns, portfolio.getChildren(), portfolio);
                 errors += csv.isValid(vc, messages) ? 1 : 0;
             }

             return errors == 0;
        }

        public abstract class InvestmentLine implements TableOutlineNode
        {
            private final TableRow tableRow;
            private final int firstDataRowIndexInTable;
            private final int lastDataRowIndexInTable;

            public InvestmentLine(final TableRow tableRow, final int firstDataRowIndexInTable, final int lastDataRowIndexInTable)
            {
                this.tableRow = tableRow;
                this.firstDataRowIndexInTable = firstDataRowIndexInTable;
                this.lastDataRowIndexInTable = lastDataRowIndexInTable;
            }

            public TableRow getTableRow() { return tableRow; }
            public int getFirstDataRowIndexInTable() { return firstDataRowIndexInTable; }
            public int getLastDataRowIndexInTable() { return lastDataRowIndexInTable; }
        }

        public class Investment extends InvestmentLine
        {
            private final TableOutlineNodeColumnsValidator columnsValidator;
            private final List<TableOutlineNode> lines = new ArrayList<TableOutlineNode>();

            public Investment(final TableRow tableRow, final int firstDataRowIndexInTable, final int lastDataRowIndexInTable)
            {
                super(tableRow, firstDataRowIndexInTable, lastDataRowIndexInTable);

                final List<TableRow> allTableRows = table.getRows();
                for(int rowIndex = firstDataRowIndexInTable; rowIndex <= lastDataRowIndexInTable; rowIndex++)
                {
                    final TableRow investmentDataRow = allTableRows.get(rowIndex);
                    final String upiText = (String) investmentDataRow.findCellForColumn(activeUPIColumn).getValue("");
                    final UPI upi = DataFormatFactory.getInstance().createUPI(parameters.getBudgetYear(), upiText, parameters);

                    if(upi.isFundingSourceLine())
                        lines.add(new FundingSource(investmentDataRow, rowIndex, rowIndex));
                    else if(upi.isSubtotalLine())
                        lines.add(new Subtotal(investmentDataRow, rowIndex, rowIndex));
                    else
                    {
                        messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_INV_LINE_TYPE_INVALID, "Unable to resolve investment line on row %d: don't know what to do with line type '%s'.", investmentDataRow.getRowNumberInSheet(), upi.getLineType()));
                    }
                }

                final TextRegExRule descrTruncatedRule = new TextRegExRule(MessageCodeFactory.DESCR_TRUNCATED, ".+[\\.\\!\\?\\(\\)\\{\\}\\[\\]\\<\\>\\'\\\"]$", "An investment description value seems to be truncated at %s. Please ensure that it is properly summarized, and not simply truncated (should end with proper sentence punctuation like with the following valid characters: . , ! ? ( ) { } [ ] < > ' \").");
                final TextRegExRule segArchRule = new TextRegExRule(MessageCodeFactory.SEG_ARCH_INVALID_CODE, "^[0-9]{3}-[0]{3}$", "Invalid segement architecture code '%2$s' at %1$s. It should look like ###-000.");
                final Set<Integer> validHSPriorities = new HashSet<Integer>();
                for(int i = 1; i <= 6; i++) validHSPriorities.add(i);

                final Map<Column, CellValidationRule[]> cellValidationRules = new HashMap<Column, CellValidationRule[]>();
                cellValidationRules.put(descrColumn, new CellValidationRule[] { descrTruncatedRule });
                cellValidationRules.put(homelandSecurityPrioritiesColumn, new CellValidationRule[] { new IntegerEnumerationRule(MessageCodeFactory.HS_INVALID_PRIORITY, validHSPriorities) });
                cellValidationRules.put(finSystemPctColumn, new CellValidationRule[] { new PercentageRule(MessageCodeFactory.FINPCT_INVALID) });
                cellValidationRules.put(segArchColumn, new CellValidationRule[] { new DataRequiredRule(MessageCodeFactory.SEG_ARCH_REQUIRED, "Segment architecture code is required in %s."), segArchRule });
                cellValidationRules.put(feaLobColumn, new CellValidationRule[] { new TextEnumerationRule(MessageCodeFactory.FEA_LOB_INVALID, FEACodesManager.getInstance().getLineOfBusinessOrServiceTypesCache().keySet() )});
                cellValidationRules.put(feaSubFColumn, new CellValidationRule[] { new TextEnumerationRule(MessageCodeFactory.FEA_SUBF_INVALID, FEACodesManager.getInstance().getSubFunctionOrSvcComponentsCache().keySet() )});

                columnsValidator = new TableOutlineNodeColumnsValidator(MessageCodeFactory.INVESTMENT_COLUMN_INVALID, cellValidationRules);
            }

            public boolean isValid(final ValidationContext vc, final List<NodeValidationMessage> messages)
            {
                int errors = 0;
                errors += columnsValidator.isValid(vc, Exhibit53.this, this, messages) ? 0 : 1;

                if(getChildren().size() == 0)
                {
                    messages.add(new NodeValidationMessage()
                    {
                        public TableOutline getOutline() { return Exhibit53.this; }
                        public TableOutlineNode getNode() { return Investment.this; }
                        public TableRow getRow() { return Investment.this.getTableRow(); }
                        public CellValidationMessage[] getCellValidationErrors() { return new CellValidationMessage[0]; }
                        public String getCode() { return MessageCodeFactory.INVESTMENT_NO_CHILDREN; }
                        public String getMessage() { return String.format("Investment lines for row %d should have funding sources and a subtotal.", getRow().getRowNumberInSheet()); }
                    });
                    return false;
                }

                for(final TableOutlineNode child : getChildren())
                    errors += child.isValid(vc, messages) ? 0 : 1;

                if(parameters.isValidateAnySubtotals() && parameters.isValidateInvestmentLineSubtotalWithFundingSourceSubtotals())
                {
                    final List<TableOutlineNode> nodesToCompare = new ArrayList<TableOutlineNode>();
                    for(final TableOutlineNode line : getChildren())
                        if(line instanceof Subtotal) nodesToCompare.add(line);
                    if(nodesToCompare.size() > 0)
                    {
                        final ColumnSubtotalsValidator csv = new ColumnSubtotalsValidator(
                                MessageCodeFactory.INVESTMENT_ROW_SUBTOTAL_INVALID,
                                MessageCodeFactory.INVESTMENT_ROW_COLUMN_SUBTOTAL_INVALID,
                                Exhibit53.this, invAmtsRollupColumns, nodesToCompare, this);
                        errors += csv.isValid(vc, messages) ? 1 : 0;
                    }
                    else
                    {
                        messages.add(new NodeValidationMessage()
                        {
                            public TableOutline getOutline() { return Exhibit53.this; }
                            public TableOutlineNode getNode() { return Investment.this; }
                            public TableRow getRow() { return Investment.this.getTableRow(); }
                            public CellValidationMessage[] getCellValidationErrors() { return new CellValidationMessage[0]; }
                            public String getCode() { return MessageCodeFactory.INVESTMENT_NO_FUNDSRC_SUBTOTALS; }
                            public String getMessage() { return String.format("Invesment lines for row %d should have funding sources subtotals.", getRow().getRowNumberInSheet()); }
                        });
                        return false;

                    }
                }
                return errors == 0;
            }

            public List<TableOutlineNode> getChildren()
            {
                return lines;
            }

            public class FundingSource extends InvestmentLine
            {
                private final TableOutlineNodeColumnsValidator columnsValidator;

                public FundingSource(final TableRow tableRow, final int firstDataRowIndexInTable, final int lastDataRowIndexInTable)
                {
                    super(tableRow, firstDataRowIndexInTable, lastDataRowIndexInTable);

                    if(parameters.isValidateBudgetAccountsCodesInFundingSources())
                    {
                        final TextRegExRule budgetAccountRule = new TextRegExRule(MessageCodeFactory.TITLE_INVALID_FUNDING_SRC_BUDGET_ACCOUNT, "^[0-9]{3}-[0-9]{2}-[0-9]{4}-[0-9]$", "Invalid budget account '%2$s' in funding source at %1$s. It should look like 000-00-0000-0.");
                        final Map<Column, CellValidationRule[]> cellValidationRules = new HashMap<Column, CellValidationRule[]>();
                        cellValidationRules.put(investmentTitleColumn, new CellValidationRule[] { budgetAccountRule });
                        columnsValidator = new TableOutlineNodeColumnsValidator(MessageCodeFactory.FUNDSRC_ROW_COLUMN_INVALID, cellValidationRules);
                    }
                    else
                        columnsValidator = null;
                }

                public boolean isValid(final ValidationContext vc,
                                       final List<NodeValidationMessage> messages)
                {
                    return columnsValidator == null || columnsValidator.isValid(vc, Exhibit53.this, this, messages);
                }

                public List<TableOutlineNode> getChildren() { return new ArrayList<TableOutlineNode>(); }
            }

            public class Subtotal extends InvestmentLine
            {
                public Subtotal(final TableRow tableRow, final int firstDataRowIndexInTable, final int lastDataRowIndexInTable)
                {
                    super(tableRow, firstDataRowIndexInTable, lastDataRowIndexInTable);
                }

                public boolean isValid(final ValidationContext vc, final List<NodeValidationMessage> messages)
                {
                    if(! (parameters.isValidateAnySubtotals() && parameters.isValidateFundingSourceSubtotals()))
                        return true;

                    final List<TableOutlineNode> nodesToCompare = new ArrayList<TableOutlineNode>();
                    for(final TableOutlineNode line : Investment.this.getChildren())
                        if(line != this) nodesToCompare.add(line);

                    final ColumnSubtotalsValidator csv = new ColumnSubtotalsValidator(
                            MessageCodeFactory.FUNDSRC_ROW_SUBTOTAL_INVALID,
                            MessageCodeFactory.FUNDSRC_ROW_COLUMN_SUBTOTAL_INVALID,
                            Exhibit53.this, invAmtsRollupColumns, nodesToCompare, this);
                    
                    return csv.isValid(vc, messages);
                }

                public List<TableOutlineNode> getChildren() { return new ArrayList<TableOutlineNode>(); }
            }
        }

        public class Investments implements Exhibit53Part
        {
            private final String partId;
            private final TableRow investmentsRow;
            private final String missionAreaId;
            private final int firstDataRowIndexInTable;
            private final int lastDataRowIndexInTable;
            private final List<TableOutlineNode> investments = new ArrayList<TableOutlineNode>();

            protected Investments(final Portfolio portfolio, final TableRow investmentsRow, final String partId,
                                  final String missionAreaId, final int groupFirstDataRowIndex, final int groupLastDataRowIndex)
            {
                this.investmentsRow = investmentsRow;
                this.partId = partId;
                this.missionAreaId = missionAreaId;
                this.firstDataRowIndexInTable = groupFirstDataRowIndex;
                this.lastDataRowIndexInTable = groupLastDataRowIndex;

                final List<TableRow> allTableRows = table.getRows();
                int errors = 0;
                final List<TableRow> investmentRows = new ArrayList<TableRow>();

                String activeInvestment = "";
                for(int rowIndex = groupFirstDataRowIndex; rowIndex <= groupLastDataRowIndex; rowIndex++)
                {
                    final TableRow investmentDataRow = allTableRows.get(rowIndex);
                    final String upiText = (String) investmentDataRow.findCellForColumn(activeUPIColumn).getValue("");
                    final UPI upi = DataFormatFactory.getInstance().createUPI(parameters.getBudgetYear(), upiText, parameters);

                    if(! upi.isValid())
                    {
                        String[] upiIssues = upi.getIssues();
                        errors += upiIssues.length;
                        messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_INVALID, "Unable to resolve investment line on row %d because UPI '%s' is invalid: %s", investmentDataRow.getRowNumberInSheet(), upi.getUPIText(), Arrays.asList(upi.getIssues())));
                        continue;
                    }

                    if(partId != null && ! upi.getExhibit53PartIdentifier().equals(partId))
                    {
                        errors++;
                        messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_PORTFOLIO_PART_INVALID, "Unable to resolve investment line on row %d: part code '%s' should be '%s'.", investmentDataRow.getRowNumberInSheet(), upi.getExhibit53PartIdentifier(), partId));
                        continue;
                    }

                    if(missionAreaId != null && ! upi.getMissionAreaIdentifier().equals(missionAreaId))
                    {
                        errors++;
                        messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_MISSION_AREA_INVALID, "Unable to resolve investment line on row %d: mission area '%s' should be '%s'.", investmentDataRow.getRowNumberInSheet(), upi.getMissionAreaIdentifier(), missionAreaId));
                        continue;
                    }

                    final String thisRowInvestmentID = String.format("%s-%s", upi.getInvestmentTypeIdentifier(), upi.getInvestmentIdentificationNumber());
                    if(! activeInvestment.equals(thisRowInvestmentID))
                    {
                        activeInvestment = thisRowInvestmentID;
                        investmentRows.add(investmentDataRow);

                        final List<TableRow> duplicateIdRows = uniqueInvestmentIds.get(upi.getInvestmentIdentificationNumber());
                        if(duplicateIdRows != null)
                        {
                            errors++;
                            final StringBuilder dupeRowNumbers = new StringBuilder();
                            for(final TableRow dupeRow : duplicateIdRows)
                            {
                                if(dupeRowNumbers.length() > 0) dupeRowNumbers.append(", ");
                                dupeRowNumbers.append(dupeRow.getRowNumberInSheet());
                            }
                            messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_INV_ID_DUPLICATED, "Investment ID '%s' duplicated on row %d. Already exists on %s.", upi.getInvestmentIdentificationNumber(), investmentDataRow.getRowNumberInSheet(), dupeRowNumbers));
                            duplicateIdRows.add(investmentDataRow);
                        }
                        else
                        {
                            final List<TableRow> idOnRows = new ArrayList<TableRow>();
                            idOnRows.add(investmentDataRow);
                            uniqueInvestmentIds.put(upi.getInvestmentIdentificationNumber(), idOnRows);
                        }
                    }
                }

                if(errors == 0)
                {
                    for(int i = 0; i < investmentRows.size(); i++)
                    {
                        final boolean isLast = i == investmentRows.size() - 1;
                        final TableRow investmentTypeStartRow = investmentRows.get(i);
                        final int invLinesFirstDataRowIndex = allTableRows.indexOf(investmentTypeStartRow) + 1;
                        final int invLinesLastDataRowIndex = (isLast ? groupLastDataRowIndex : (allTableRows.indexOf(investmentRows.get(i+1))-1));
                        final Investment investment = new Investment(investmentTypeStartRow, invLinesFirstDataRowIndex, invLinesLastDataRowIndex);
                        investments.add(investment);
                        portfolio.getInvestments().add(investment);
                    }
                }
            }

            public boolean isValid(final ValidationContext vc,
                                   final List<NodeValidationMessage> messages)
            {
                int errors = 0;
                for(final TableOutlineNode child : getChildren())
                    errors += child.isValid(vc, messages) ? 0 : 1;

                if(parameters.isValidateAnySubtotals() && parameters.isValidateInvestmentsGroupSubtotals())
                {
                    final ColumnSubtotalsValidator csv = new ColumnSubtotalsValidator(
                            MessageCodeFactory.INVESTMENTS_ROW_SUBTOTAL_INVALID,
                            MessageCodeFactory.INVESTMENTS_ROW_COLUMN_SUBTOTAL_INVALID,
                            Exhibit53.this, invAmtsRollupColumns, getChildren(), this);
                    errors += csv.isValid(vc, messages) ? 1 : 0;
                }

                return errors == 0;
            }

            public String getPart() { return partId; }
            public TableRow getTableRow() { return investmentsRow; }
            public List<TableOutlineNode> getChildren() { return investments; }
            public int getFirstDataRowIndexInTable() { return firstDataRowIndexInTable; }
            public int getLastDataRowIndexInTable() { return lastDataRowIndexInTable; }
            public String getMissionAreaId() { return missionAreaId; }
        }

        public class MissionAreas implements Exhibit53Part
        {
            private final TableRow missionAreasRow;
            private final String partId;
            private final int firstDataRowIndexInTable;
            private final int lastDataRowIndexInTable;
            private final List<TableOutlineNode> missionAreas = new ArrayList<TableOutlineNode>();
            private final Map<String, Investments> missionAreasById = new HashMap<String, Investments>();

            public MissionAreas(final Portfolio portfolio, final TableRow missionAreasRow, final String partId,
                                final int firstDataRowIndexInPart, final int lastDataRowIndexInPart)
            {
                this.missionAreasRow = missionAreasRow;
                this.partId = partId;
                this.firstDataRowIndexInTable = firstDataRowIndexInPart;
                this.lastDataRowIndexInTable = lastDataRowIndexInPart;

                final List<TableRow> allTableRows = table.getRows();
                int errors = 0;
                String activeMissionArea = "";
                final List<TableRow> missionAreaRows = new ArrayList<TableRow>();
                final List<String> missionAreaIds = new ArrayList<String>();
                for(int rowIndex = firstDataRowIndexInPart; rowIndex <= lastDataRowIndexInPart; rowIndex++)
                {
                    final TableRow sectionDataRow = allTableRows.get(rowIndex);
                    final String upiText = (String) sectionDataRow.findCellForColumn(activeUPIColumn).getValue("");
                    final UPI upi = DataFormatFactory.getInstance().createUPI(parameters.getBudgetYear(), upiText, parameters);

                    if(! upi.isValid())
                    {
                        String[] upiIssues = upi.getIssues();
                        errors += upiIssues.length;
                        messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_INVALID, "Unable to resolve mission area on row %d because UPI '%s' is invalid: %s", sectionDataRow.getRowNumberInSheet(), upi.getUPIText(), Arrays.asList(upi.getIssues())));
                        continue;
                    }

                    if(! upi.getExhibit53PartIdentifier().equals(partId))
                    {
                        errors++;
                        messages.add(new DefaultOutlineValidationMessage(table, MessageCodeFactory.UPI_PORTFOLIO_PART_INVALID, "Unable to resolve mission area on row %d: part code '%s' should be '%s'.", sectionDataRow.getRowNumberInSheet(), upi.getExhibit53PartIdentifier(), partId));
                        continue;
                    }

                    if(! activeMissionArea.equals(upi.getMissionAreaIdentifier()))
                    {
                        activeMissionArea = upi.getMissionAreaIdentifier();
                        missionAreaIds.add(activeMissionArea);
                        missionAreaRows.add(sectionDataRow);
                    }
                }

                if(errors == 0)
                {
                    for(int i = 0; i < missionAreaRows.size(); i++)
                    {
                        final boolean isLast = i == missionAreaRows.size() - 1;
                        final TableRow missionAreaStartRow = missionAreaRows.get(i);
                        final int investmentFirstDataRowIndex = allTableRows.indexOf(missionAreaStartRow) + 1;
                        final int investmentLastDataRowIndex = (isLast ? lastDataRowIndexInPart : allTableRows.indexOf(missionAreaRows.get(i+1))-1);
                        final Investments investments = new Investments(portfolio, missionAreaStartRow, partId, missionAreaIds.get(i), investmentFirstDataRowIndex, investmentLastDataRowIndex);
                        missionAreas.add(investments);
                        missionAreasById.put(investments.getMissionAreaId(), investments);
                    }
                }
            }

            public String getPart() { return partId; }
            public TableRow getTableRow() { return missionAreasRow; }
            public List<TableOutlineNode> getChildren() { return missionAreas; }
            public int getFirstDataRowIndexInTable() { return firstDataRowIndexInTable; }
            public int getLastDataRowIndexInTable() { return lastDataRowIndexInTable; }
            public Investments getMissionAreaInvestments(final String areaId) { return missionAreasById.get(areaId); }

            public boolean isValid(final ValidationContext vc, final List<NodeValidationMessage> messages)
            {
                int errors = 0;
                for(final TableOutlineNode child : getChildren())
                    errors += child.isValid(vc, messages) ? 0 : 1;

                if(parameters.isValidateAnySubtotals() && parameters.isValidateMissionAreaSubtotals())
                {
                    final ColumnSubtotalsValidator csv = new ColumnSubtotalsValidator(
                            MessageCodeFactory.MISSAREA_ROW_SUBTOTAL_INVALID,
                            MessageCodeFactory.MISSAREA_ROW_COLUMN_SUBTOTAL_INVALID,
                            Exhibit53.this, invAmtsRollupColumns, getChildren(), this);
                    errors += csv.isValid(vc, messages) ? 1 : 0;
                }

                return errors == 0;
            }
        }

        public class Portfolio implements TableOutlineNode
        {
            private final TableRow portfolioRow;
            private final int firstDataRowIndexInTable;
            private final int lastDataRowIndexInTable;
            private final List<TableOutlineNode> parts = new ArrayList<TableOutlineNode>();
            private final List<Investment> investments = new ArrayList<Investment>();
            private MissionAreas missionAreas;

            protected Portfolio(final TableRow portfolioRow)
            {
                this.portfolioRow = portfolioRow;
                this.firstDataRowIndexInTable = table.getRows().indexOf(portfolioRow) + 1;
                this.lastDataRowIndexInTable = table.getRows().size()-1;

                final String[] partCacheNames = new String[]
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
                for(final String partId : partCacheNames)
                {
                    final TableRow sectionContainer = findUniqueRow(table, portfolioSectionsCache, partId, messages);
                    if(sectionContainer == null)
                        errors++;
                    else
                        portfolioSectionRows.add(sectionContainer);
                }

                if(errors == 0)
                {
                    final List<TableRow> allTableRows = table.getRows();
                    for(int sectionNum = 0; sectionNum < portfolioSectionRows.size(); sectionNum++)
                    {
                        final boolean isLast = sectionNum == portfolioSectionRows.size() - 1;
                        final TableRow partStartRow = portfolioSectionRows.get(sectionNum);
                        final int firstDataRowIndex = allTableRows.indexOf(partStartRow) + 1;
                        final int lastDataRowIndex = (isLast ? allTableRows.size() : allTableRows.indexOf(portfolioSectionRows.get(sectionNum+1))) - 1;
                        if(sectionNum == 0)
                        {
                            missionAreas = new MissionAreas(this, partStartRow, String.format("%02d", sectionNum + 1), firstDataRowIndex, lastDataRowIndex);
                            parts.add(missionAreas);
                        }
                        else
                            parts.add(new Investments(this, partStartRow, String.format("%02d", sectionNum+1), null, firstDataRowIndex, lastDataRowIndex));
                    }
                }

                if(missionAreas == null)
                    messages.add(new DefaultOutlineValidationMessage(getTable(), MessageCodeFactory.MISSAREA_PART_MISSING, "Unable to find mission area part."));
            }

            public TableRow getTableRow() { return portfolioRow; }
            public List<TableOutlineNode> getChildren() { return parts; }
            public List<Investment> getInvestments() { return investments; }
            public int getFirstDataRowIndexInTable() { return firstDataRowIndexInTable; }
            public int getLastDataRowIndexInTable() { return lastDataRowIndexInTable; }
            public MissionAreas getMissionAreas() { return missionAreas; }

            public boolean isValid(final ValidationContext vc, final List<NodeValidationMessage> messages)
            {
                int errors = 0;
                for(final TableOutlineNode child : getChildren())
                    errors += child.isValid(vc, messages) ? 0 : 1;

                if(parameters.isValidateAnySubtotals() && parameters.isValidatePartSubtotals())
                {
                    final ColumnSubtotalsValidator csv = new ColumnSubtotalsValidator(
                            MessageCodeFactory.PART_ROW_SUBTOTAL_INVALID,
                            MessageCodeFactory.PART_ROW_COLUMN_SUBTOTAL_INVALID,
                            Exhibit53.this, invAmtsRollupColumns, getChildren(), this);
                    errors += csv.isValid(vc, messages) ? 1 : 0;
                }

                return errors == 0;
            }
        }
    }
}